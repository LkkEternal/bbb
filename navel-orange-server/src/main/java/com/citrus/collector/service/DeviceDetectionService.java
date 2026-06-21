package com.citrus.collector.service;

import com.citrus.collector.dto.DeviceDetectionDTO;
import com.citrus.orchard.entity.Device;

import java.util.List;
import java.util.Map;

public interface DeviceDetectionService {

    Map<String, Object> handleDetection(Device device, DeviceDetectionDTO dto);

    Map<String, Object> handleBatchDetection(Device device, List<DeviceDetectionDTO> dtoList);
}
