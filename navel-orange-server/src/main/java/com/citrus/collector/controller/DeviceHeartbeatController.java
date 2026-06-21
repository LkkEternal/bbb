package com.citrus.collector.controller;

import com.citrus.collector.dto.DeviceHeartbeatDTO;
import com.citrus.collector.service.DeviceHeartbeatService;
import com.citrus.common.result.Result;
import com.citrus.orchard.entity.Device;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "设备心跳", description = "设备心跳上报接口（API Key认证）")
public class DeviceHeartbeatController {

    private final DeviceHeartbeatService deviceHeartbeatService;

    @PostMapping("/heartbeat")
    @Operation(summary = "设备心跳", description = "设备定时上报环境传感器数据，设备身份通过X-Api-Key自动解析")
    public Result<Map<String, Object>> heartbeat(
            @RequestAttribute("device") Device device,
            @RequestBody(required = false) DeviceHeartbeatDTO dto) {
        if (dto == null) {
            dto = new DeviceHeartbeatDTO();
        }
        Map<String, Object> data = deviceHeartbeatService.handleHeartbeat(device, dto);
        return Result.success(data);
    }
}
