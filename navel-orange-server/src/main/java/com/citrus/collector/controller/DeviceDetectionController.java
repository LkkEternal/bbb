package com.citrus.collector.controller;

import com.citrus.collector.dto.DeviceBatchDetectionDTO;
import com.citrus.collector.dto.DeviceDetectionDTO;
import com.citrus.collector.service.DeviceDetectionService;
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
@Tag(name = "病害上报", description = "设备病害识别结果上报接口（API Key认证）")
public class DeviceDetectionController {

    private final DeviceDetectionService deviceDetectionService;

    @PostMapping("/detection")
    @Operation(summary = "上报识别结果", description = "设备上报病害识别结果，设备身份通过X-Api-Key自动解析")
    public Result<Map<String, Object>> reportDetection(
            @RequestAttribute("device") Device device,
            @RequestBody DeviceDetectionDTO dto) {
        Map<String, Object> data = deviceDetectionService.handleDetection(device, dto);
        return Result.success("上报成功", data);
    }

    @PostMapping("/detection/batch")
    @Operation(summary = "批量上报识别结果", description = "设备批量上报多条病害识别结果")
    public Result<Map<String, Object>> reportBatchDetection(
            @RequestAttribute("device") Device device,
            @RequestBody DeviceBatchDetectionDTO dto) {
        Map<String, Object> data = deviceDetectionService.handleBatchDetection(device, dto.getDetections());
        return Result.success("批量上报成功", data);
    }
}
