package com.citrus.collector.service;

import com.citrus.collector.dto.DeviceHeartbeatDTO;
import com.citrus.orchard.entity.Device;

import java.util.Map;

public interface DeviceHeartbeatService {

    Map<String, Object> handleHeartbeat(Device device, DeviceHeartbeatDTO dto);
}
