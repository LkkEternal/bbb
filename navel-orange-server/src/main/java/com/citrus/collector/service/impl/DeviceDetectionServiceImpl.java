package com.citrus.collector.service.impl;

import com.citrus.collector.cache.DetectionEventDTO;
import com.citrus.collector.cache.RedisDetectionCache;
import com.citrus.collector.dto.DeviceDetectionDTO;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.service.DeviceDetectionService;
import com.citrus.common.util.HuaweiObsClient;
import com.citrus.orchard.entity.Device;
import com.citrus.websocket.DetectionWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 设备检测上报服务 — 缓存优先 + WebSocket 推送 + 异步落库
 * <p>
 * 处理流程：
 * 1. 解析设备上报数据，构建 DiseaseDetection 实体
 * 2. 写入 Redis 实时缓存 + 待持久化队列（立刻，前端 WebSocket 实时可见）
 * 3. 通过 WebSocket 广播检测事件给所有前端客户端
 * 4. DetectionPersistenceTask 异步从队列读取并批量写入 DB，随后发布事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDetectionServiceImpl implements DeviceDetectionService {

    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final ObjectMapper objectMapper;
    private final RedisDetectionCache redisDetectionCache;
    private final DetectionWebSocketHandler webSocketHandler;
    private final HuaweiObsClient huaweiObsClient;

    @Value("${huawei.obs.url-prefix:}")
    private String obsUrlPrefix;

    @Override
    public Map<String, Object> handleDetection(Device device, DeviceDetectionDTO dto) {
        int count = 0;

        for (DeviceDetectionDTO.DiseaseItem item : dto.getDiseases()) {
            String classCode = extractClassCode(item.getName());
            DiseaseCatalog catalog = diseaseCatalogMapper.selectByCode(classCode);
            Integer catalogId = catalog != null ? catalog.getId() : null;

            String img = item.getImg();
            String imageUrl = findImageUrl(img, item.getTs());

            LocalDateTime deviceTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(item.getTs()), ZoneId.of("Asia/Shanghai"));

            Map<String, BigDecimal> probs = new HashMap<>();
            probs.put(classCode, item.getConf());

            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("cnt", item.getCnt());
            extra.put("gps", dto.getGps());
            extra.put("device", dto.getDevice());

            DiseaseDetection record = new DiseaseDetection();
            record.setDeviceId(device.getId());
            record.setOrchardId(device.getOrchardId());
            record.setDiseaseCatalogId(catalogId);
            record.setDiseaseNameRaw(item.getName());
            record.setTopClassCode(classCode);
            record.setTopProbability(item.getConf());
            record.setImageUrl(imageUrl);
            record.setDeviceTimestamp(deviceTime);
            record.setCreatedAt(LocalDateTime.now());
            record.setProcessed(false);

            try {
                record.setAllProbabilities(objectMapper.writeValueAsString(probs));
                record.setExtraData(objectMapper.writeValueAsString(extra));
            } catch (Exception e) {
                log.error("序列化JSON失败: {}", e.getMessage());
            }

            // ① 写入 Redis 实时缓存 + 待持久化队列（异步落库由此队列消费）
            redisDetectionCache.cacheDetection(record);

            // ② WebSocket 广播检测事件（前端实时收到）
            DetectionEventDTO eventDTO = DetectionEventDTO.from(record);
            eventDTO.setDeviceCode(device.getDeviceCode());
            eventDTO.setDeviceName(device.getName());
            webSocketHandler.broadcast("detection_new", eventDTO);

            count++;

            log.info("设备[{}]上报病害[{}]，置信度: {}，已入队列",
                    device.getDeviceCode(), classCode, item.getConf());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    @Override
    public Map<String, Object> handleBatchDetection(Device device, List<DeviceDetectionDTO> dtoList) {
        int totalCount = 0;
        int failCount = 0;

        for (DeviceDetectionDTO dto : dtoList) {
            try {
                Map<String, Object> result = handleDetection(device, dto);
                totalCount += (int) result.getOrDefault("count", 0);
            } catch (Exception e) {
                log.error("批量上报中单条处理失败: {}", e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", dtoList.size() - failCount);
        result.put("failCount", failCount);
        result.put("count", totalCount);
        return result;
    }

    private String extractClassCode(String name) {
        if (name != null && name.startsWith("S")) {
            int idx = name.indexOf('_');
            return idx > 0 ? name.substring(0, idx) : name;
        }
        return name;
    }

    /**
     * 查找 OBS 中实际存在的图片：先试设备上报的 img，再试附近时间戳 ±120 秒
     * 与 IotdaShadowPullingTask.findImageUrl 逻辑一致
     */
    private String findImageUrl(String img, Number tsObj) {
        if (img == null || img.isEmpty()) return null;
        long baseTs = tsObj != null ? tsObj.longValue() : 0;

        // 1. 先试原始文件名
        String name = img.contains("/") ? img.substring(img.lastIndexOf("/") + 1) : img;
        String objectName = "disease_records/" + name;
        try {
            if (huaweiObsClient.doesObjectExist(objectName)) {
                log.info("找到匹配图片: {}", name);
                return obsUrlPrefix + "/disease_records/" + name;
            }
        } catch (Exception e) {
            log.warn("检查OBS图片存在性失败: {}", e.getMessage());
        }

        // 2. 如果文件名带时间戳，尝试附近 ±120 秒
        if (baseTs > 0 && name.startsWith("citrus_")) {
            for (long offset = -120; offset <= 120; offset++) {
                if (offset == 0) continue;
                long tryTs = baseTs + offset;
                String tryName = "citrus_" + tryTs + ".jpg";
                String tryKey = "disease_records/" + tryName;
                try {
                    if (huaweiObsClient.doesObjectExist(tryKey)) {
                        log.info("找到附近图片: {} (偏移={}s)", tryName, offset);
                        return obsUrlPrefix + "/disease_records/" + tryName;
                    }
                } catch (Exception ignored) {}
            }
        }

        log.warn("OBS中未找到图片: {}，将imageUrl置为null", name);
        return null;
    }
}
