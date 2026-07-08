package com.citrus.collector.task;

import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.common.iotda.HuaweiIotdaClient;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.mapper.DeviceMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务 — 从华为云 IoTDA 设备影子拉取传感器数据
 *
 * 每 60 秒拉取一次，写入 environment_data 表
 * 数据来源：IoTDA 设备影子 SensorDate 服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IotdaShadowPullingTask {

    private final DeviceMapper deviceMapper;
    private final EnvironmentDataMapper environmentDataMapper;
    private final HuaweiIotdaClient iotdaClient;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000) // 每 60 秒
    public void pullSensorDataFromIotda() {
        // 1. 查询所有本地已注册的在线设备（含 orchar_id）
        List<Device> devices = deviceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Device>()
                        .isNotNull(Device::getOrchardId)
                        .select(Device::getId, Device::getDeviceCode, Device::getOrchardId, Device::getName)
        );

        if (devices.isEmpty()) {
            return;
        }

        int successCount = 0;
        for (Device device : devices) {
            try {
                pullAndSave(device);
                successCount++;
            } catch (Exception e) {
                log.warn("拉取设备[{}]影子数据失败: {}", device.getDeviceCode(), e.getMessage());
            }
        }

        if (successCount > 0) {
            log.debug("IoTDA影子拉取完成: {}/{} 个设备成功", successCount, devices.size());
        }
    }

    private void pullAndSave(Device device) {
        // 1. 从 IoTDA 获取设备影子
        HuaweiIotdaClient.DeviceShadowResponse shadow = iotdaClient.getDeviceShadow(device.getDeviceCode());
        if (shadow == null || shadow.getShadow() == null || shadow.getShadow().isEmpty()) {
            return;
        }

        // 2. 查找 SensorDate 服务
        for (HuaweiIotdaClient.ShadowItem item : shadow.getShadow()) {
            if (!"SensorDate".equals(item.getServiceId())) {
                continue;
            }
            if (item.getReported() == null || item.getReported().getProperties() == null) {
                continue;
            }

            Map<String, Object> props = item.getReported().getProperties();
            String eventTime = item.getReported().getEventTime();

            // 3. 解析传感器数据
            BigDecimal airTemp = toBigDecimal(props.get("air_temp"));
            BigDecimal humidity = toBigDecimal(props.get("humidity"));
            BigDecimal soilTemp = toBigDecimal(props.get("soil_temp"));
            BigDecimal moisture = toBigDecimal(props.get("moisture"));
            BigDecimal conductivity = toBigDecimal(props.get("conductivity"));
            BigDecimal ph = toBigDecimal(props.get("ph"));
            BigDecimal battery = toBigDecimal(props.get("battery"));

            // 如果空气温湿度都为空，跳过
            if (airTemp == null && humidity == null) {
                return;
            }

            // 4. 解析时间
            LocalDateTime recordTime = parseEventTime(eventTime);

            // 5. 构建额外数据 JSON
            Map<String, Object> extra = new LinkedHashMap<>();
            if (soilTemp != null) extra.put("soil_temp", soilTemp);
            if (moisture != null) extra.put("moisture", moisture);
            if (conductivity != null) extra.put("conductivity", conductivity);
            if (ph != null) extra.put("ph", ph);
            if (battery != null) extra.put("battery", battery);
            if (device.getName() != null) extra.put("deviceName", device.getName());

            String extraJson = null;
            try {
                extraJson = objectMapper.writeValueAsString(extra);
            } catch (Exception e) {
                log.warn("序列化 extra_data 失败: {}", e.getMessage());
            }

            // 6. 写入数据库（每60秒执行一次，无需去重）
            EnvironmentData envData = new EnvironmentData();
            envData.setOrchardId(device.getOrchardId());
            envData.setDataSource("iotda_shadow");
            envData.setTemp(airTemp);
            envData.setHumidity(humidity);
            envData.setRecordTime(recordTime);
            envData.setExtraData(extraJson);
            envData.setCreatedAt(LocalDateTime.now());
            environmentDataMapper.insert(envData);

            log.info("设备[{}]传感器数据已入库: temp={}, humidity={}, soil_temp={}, moisture={}, ph={}",
                    device.getDeviceCode(), airTemp, humidity, soilTemp, moisture, ph);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseEventTime(String eventTime) {
        if (eventTime == null || eventTime.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            // IoTDA 时间格式: 20260707T075717Z
            return LocalDateTime.parse(eventTime, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(eventTime, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e2) {
                return LocalDateTime.now();
            }
        }
    }
}
