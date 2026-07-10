package com.citrus.collector.task;

import com.citrus.collector.cache.DetectionEventDTO;
import com.citrus.collector.cache.RedisDetectionCache;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.mapper.DeviceMapper;
import com.citrus.websocket.DetectionWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 定时任务 — 从华为云 IoTDA 设备影子拉取传感器 + 病害识别数据
 *
 * 每 60 秒拉取一次：
 * - SensorDate 服务 → environment_data 表
 * - DiseaseMonitor 服务 → disease_detection 表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IotdaShadowPullingTask {

    private final DeviceMapper deviceMapper;
    private final EnvironmentDataMapper environmentDataMapper;
    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final HuaweiIotdaClient iotdaClient;
    private final ObjectMapper objectMapper;
    private final com.citrus.common.util.HuaweiObsClient huaweiObsClient;
    private final RedisDetectionCache redisDetectionCache;
    private final DetectionWebSocketHandler webSocketHandler;

    @Value("${huawei.obs.url-prefix:}")
    private String obsUrlPrefix;

    @Scheduled(fixedDelay = 60000)
    public void pullFromIotda() {
        List<Device> devices = deviceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Device>()
                        .isNotNull(Device::getOrchardId)
                        .select(Device::getId, Device::getDeviceCode, Device::getOrchardId, Device::getName)
        );
        if (devices.isEmpty()) return;

        for (Device device : devices) {
            try {
                processDevice(device);
            } catch (Exception e) {
                log.warn("处理设备[{}]影子数据异常: {}", device.getDeviceCode(), e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processDevice(Device device) {
        HuaweiIotdaClient.DeviceShadowResponse shadow = iotdaClient.getDeviceShadow(device.getDeviceCode());
        if (shadow == null || shadow.getShadow() == null) return;

        for (HuaweiIotdaClient.ShadowItem item : shadow.getShadow()) {
            if (item.getReported() == null || item.getReported().getProperties() == null) continue;

            if ("SensorDate".equals(item.getServiceId())) {
                handleSensorData(device, item);
            } else if ("DiseaseMonitor".equals(item.getServiceId())) {
                handleDiseaseData(device, item);
            }
        }
    }

    // ── SensorDate 处理（原有逻辑） ──
    private void handleSensorData(Device device, HuaweiIotdaClient.ShadowItem item) {
        Map<String, Object> props = item.getReported().getProperties();
        String eventTime = item.getReported().getEventTime();

        BigDecimal airTemp = toBigDecimal(props.get("air_temp"));
        BigDecimal humidity = toBigDecimal(props.get("humidity"));
        if (airTemp == null && humidity == null) return;

        BigDecimal soilTemp = toBigDecimal(props.get("soil_temp"));
        BigDecimal moisture = toBigDecimal(props.get("moisture"));
        BigDecimal conductivity = toBigDecimal(props.get("conductivity"));
        BigDecimal ph = toBigDecimal(props.get("ph"));
        BigDecimal battery = toBigDecimal(props.get("battery"));

        LocalDateTime recordTime = parseIotdaTime(eventTime);

        Map<String, Object> extra = new LinkedHashMap<>();
        if (soilTemp != null) extra.put("soil_temp", soilTemp);
        if (moisture != null) extra.put("moisture", moisture);
        if (conductivity != null) extra.put("conductivity", conductivity);
        if (ph != null) extra.put("ph", ph);
        if (battery != null) extra.put("battery", battery);
        if (device.getName() != null) extra.put("deviceName", device.getName());

        EnvironmentData envData = new EnvironmentData();
        envData.setOrchardId(device.getOrchardId());
        envData.setDataSource("iotda_shadow");
        envData.setTemp(airTemp);
        envData.setHumidity(humidity);
        envData.setRecordTime(recordTime);
        try { envData.setExtraData(objectMapper.writeValueAsString(extra)); } catch (Exception ignored) {}
        envData.setCreatedAt(LocalDateTime.now());
        environmentDataMapper.insert(envData);

        log.debug("设备[{}]传感器数据入库: air_temp={}, humidity={}", device.getDeviceCode(), airTemp, humidity);
    }

    @SuppressWarnings("unchecked")
    private void handleDiseaseData(Device device, HuaweiIotdaClient.ShadowItem item) {
        Map<String, Object> props = item.getReported().getProperties();
        Object diseaseDataObj = props.get("diseaseData");
        if (diseaseDataObj == null) {
            log.warn("设备[{}] 影子中无 diseaseData 属性，支持的属性: {}", device.getDeviceCode(), props.keySet());
            return;
        }

        Map<String, Object> diseaseData;
        if (diseaseDataObj instanceof Map) {
            diseaseData = (Map<String, Object>) diseaseDataObj;
        } else {
            try {
                diseaseData = objectMapper.readValue(diseaseDataObj.toString(), Map.class);
            } catch (Exception e) {
                log.warn("解析 diseaseData 失败: {}", e.getMessage());
                return;
            }
        }

        List<Map<String, Object>> diseases;
        Object diseasesObj = diseaseData.get("diseases");
        if (diseasesObj instanceof List) {
            diseases = (List<Map<String, Object>>) diseasesObj;
        } else {
            log.warn("设备[{}] diseaseData.diseases 不是数组或为空，实际值类型: {}", device.getDeviceCode(),
                    diseasesObj != null ? diseasesObj.getClass().getName() : "null");
            return;
        }

        String deviceInfo = (String) diseaseData.get("device");
        Object gpsObj = diseaseData.get("gps");
        Map<String, Object> gps = gpsObj instanceof Map ? (Map<String, Object>) gpsObj : null;

        log.info("设备[{}] 影子拉取到 {} 条病害数据，已全部入队列", device.getDeviceCode(), diseases.size());

        for (Map<String, Object> disease : diseases) {
            try {
                Object idObj = disease.get("id");
                String name = (String) disease.get("name");
                Object confObj = disease.get("conf");
                String img = (String) disease.get("img");
                Object tsObj = disease.get("ts");

                if (name == null || confObj == null) continue;

                // 提取病害编码：优先用 id 字段（设备 CLASS_CN 索引 → disease_catalog.id）
                // 设备上报格式: $citrus,{class_id},{label},{conf},{img},{cnt},{ts}#
                // class_id 是 CLASS_CN 数组的索引，映射到 database_catalog 的 id
                String classCode = null;
                Integer catalogId = null;
                String diseaseNameRaw = name;
                if (idObj instanceof Number) {
                    int classId = ((Number) idObj).intValue();
                    if (classId > 0 && classId <= 14) {
                        // CLASS_CN 索引直接对应 disease_catalog.id
                        DiseaseCatalog catById = diseaseCatalogMapper.selectById(classId);
                        if (catById != null) {
                            classCode = catById.getDiseaseCode();   // e.g. "S11"
                            catalogId = catById.getId();            // e.g. 11
                            diseaseNameRaw = catById.getDiseaseName(); // "脂点黄斑病"
                        }
                    }
                }
                // 兜底：从 name 字段提取（兼容旧格式 "S14_VeinYel" → "S14"）
                if (classCode == null) {
                    if (name.startsWith("S")) {
                        int idx = name.indexOf('_');
                        classCode = idx > 0 ? name.substring(0, idx) : name;
                    } else {
                        classCode = name;
                    }
                    DiseaseCatalog cat = diseaseCatalogMapper.selectByCode(classCode);
                    if (cat != null) {
                        catalogId = cat.getId();
                        diseaseNameRaw = cat.getDiseaseName();
                    }
                }

                // 直接从上报的 img 字段构造图片 URL（由前端代理从 OBS 拉取）
                String imageUrl = (img != null && !img.isEmpty())
                        ? obsUrlPrefix + "/disease_records/" + img
                        : null;

                // 解析设备时间
                LocalDateTime deviceTime = LocalDateTime.now();
                if (tsObj instanceof Number) {
                    deviceTime = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(((Number) tsObj).longValue()),
                            ZoneId.of("Asia/Shanghai"));
                }

                // 构建概率 JSON
                Map<String, BigDecimal> probs = new HashMap<>();
                probs.put(classCode, toBigDecimal(confObj));

                // 构建 extra_data
                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("source", "iotda_shadow");
                if (gps != null) extra.put("gps", gps);
                if (deviceInfo != null) extra.put("device", deviceInfo);

                // 检查是否已存在相同记录（按设备+时间+病害编码去重）
                LocalDateTime timeMin = deviceTime.minusSeconds(10);
                LocalDateTime timeMax = deviceTime.plusSeconds(10);
                Long exists = diseaseDetectionMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DiseaseDetection>()
                                .eq(DiseaseDetection::getDeviceId, device.getId())
                                .eq(DiseaseDetection::getTopClassCode, classCode)
                                .between(DiseaseDetection::getDeviceTimestamp, timeMin, timeMax)
                                .last("LIMIT 1"));
                if (exists != null && exists > 0) {
                    // 记录已存在，但可能缺少 image_url，尝试补充
                    try {
                        var existing = diseaseDetectionMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DiseaseDetection>()
                                        .eq(DiseaseDetection::getDeviceId, device.getId())
                                        .eq(DiseaseDetection::getTopClassCode, classCode)
                                        .between(DiseaseDetection::getDeviceTimestamp, timeMin, timeMax)
                                        .isNull(DiseaseDetection::getImageUrl)
                                        .last("LIMIT 1"));
                        if (existing != null && imageUrl != null) {
                            existing.setImageUrl(imageUrl);
                            diseaseDetectionMapper.updateById(existing);
                            log.info("已补充检测记录[{}]的图片URL: {}", existing.getId(), imageUrl);
                        }
                    } catch (Exception ignored) {}
                    continue;
                }

                // 创建检测记录
                DiseaseDetection record = new DiseaseDetection();
                record.setDeviceId(device.getId());
                record.setOrchardId(device.getOrchardId());
                record.setDiseaseCatalogId(catalogId);
                record.setDiseaseNameRaw(diseaseNameRaw);
                record.setTopClassCode(classCode);
                record.setTopProbability(toBigDecimal(confObj));
                record.setImageUrl(imageUrl);
                record.setDeviceTimestamp(deviceTime);
                record.setProcessed(false);
                try {
                    record.setAllProbabilities(objectMapper.writeValueAsString(probs));
                    record.setExtraData(objectMapper.writeValueAsString(extra));
                } catch (Exception ignored) {}
                record.setCreatedAt(LocalDateTime.now());

                // 推送到 Redis 队列（由 DetectionPersistenceTask 异步落库）
                redisDetectionCache.cacheDetection(record);

                // WebSocket 广播（前端实时可见）
                DetectionEventDTO eventDTO = DetectionEventDTO.from(record);
                eventDTO.setDeviceCode(device.getDeviceCode());
                eventDTO.setDeviceName(device.getName());
                webSocketHandler.broadcast("detection_new", eventDTO);

                log.info("设备[{}]影子病害数据入队列: disease={}, conf={}, img={}",
                        device.getDeviceCode(), classCode, confObj, img);
            } catch (Exception e) {
                log.warn("处理单条病害数据失败: {}", e.getMessage());
            }
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try { return new BigDecimal(value.toString()); } catch (Exception e) { return null; }
    }

    private LocalDateTime parseIotdaTime(String eventTime) {
        if (eventTime == null || eventTime.isEmpty()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(eventTime, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        } catch (Exception e) {
            try { return LocalDateTime.parse(eventTime, DateTimeFormatter.ISO_DATE_TIME); }
            catch (Exception e2) { return LocalDateTime.now(); }
        }
    }

    /**
     * 查找 OBS 中实际存在的图片：先试影子报告的 img，再试附近时间戳 ±30 秒
     */
    private String findImageUrl(String img, Number tsObj) {
        if (img == null || img.isEmpty()) return null;
        long baseTs = tsObj != null ? tsObj.longValue() : 0;
        String obsPrefix = "/api/images/proxy";

        // 1. 先试原始文件名
        String name = img.contains("/") ? img.substring(img.lastIndexOf("/") + 1) : img;
        String objectName = "disease_records/" + name;
        if (huaweiObsClient.doesObjectExist(objectName)) {
            log.info("找到匹配图片: {}", name);
            return obsPrefix + "/disease_records/" + name;
        }

        // 2. 如果基础文件名带 ts，尝试附近 ±60 秒
        if (baseTs > 0 && name.startsWith("citrus_")) {
            for (long offset = -60; offset <= 60; offset++) {
                if (offset == 0) continue;
                long tryTs = baseTs + offset;
                String tryName = "citrus_" + tryTs + ".jpg";
                String tryKey = "disease_records/" + tryName;
                try {
                    if (huaweiObsClient.doesObjectExist(tryKey)) {
                        log.info("找到附近图片: {} (偏移={}s)", tryName, offset);
                        return obsPrefix + "/disease_records/" + tryName;
                    }
                } catch (Exception ignored) {}
            }
        }

        return null;
    }
}
