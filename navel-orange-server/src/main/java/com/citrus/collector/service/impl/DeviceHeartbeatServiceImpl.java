package com.citrus.collector.service.impl;

import com.citrus.collector.dto.DeviceHeartbeatDTO;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.collector.service.DeviceHeartbeatService;
import com.citrus.common.constant.Constants;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.mapper.DeviceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceHeartbeatServiceImpl implements DeviceHeartbeatService {

    private final DeviceMapper deviceMapper;
    private final EnvironmentDataMapper environmentDataMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Map<String, Object> handleHeartbeat(Device device, DeviceHeartbeatDTO dto) {
        LocalDateTime now = LocalDateTime.now();

        device.setStatus(Constants.DEVICE_ONLINE);
        device.setLastHeartbeat(now);
        deviceMapper.updateById(device);

        EnvironmentData envData = new EnvironmentData();
        envData.setOrchardId(device.getOrchardId());
        envData.setDataSource("sensor");
        envData.setTemp(dto.getAirTemp());
        envData.setHumidity(dto.getHumidity());
        envData.setRecordTime(now);

        Map<String, Object> extraData = new LinkedHashMap<>();
        if (dto.getMoisture() != null) extraData.put("soil_moisture", dto.getMoisture());
        if (dto.getSoilTemp() != null) extraData.put("soil_temp", dto.getSoilTemp());
        if (dto.getConductivity() != null) extraData.put("conductivity", dto.getConductivity());
        if (dto.getPh() != null) extraData.put("ph", dto.getPh());

        if (!extraData.isEmpty()) {
            try {
                envData.setExtraData(objectMapper.writeValueAsString(extraData));
            } catch (Exception e) {
                log.error("序列化extraData失败: {}", e.getMessage());
            }
        }

        environmentDataMapper.insert(envData);

        log.info("设备[{}]心跳处理成功，果园ID: {}", device.getDeviceCode(), device.getOrchardId());

        Map<String, Object> result = new HashMap<>();
        result.put("serverTime", System.currentTimeMillis());
        result.put("configVersion", 1);
        return result;
    }
}
