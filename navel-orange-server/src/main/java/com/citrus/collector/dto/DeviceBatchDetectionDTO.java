package com.citrus.collector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "设备批量病害识别上报请求参数")
public class DeviceBatchDetectionDTO {

    @Schema(description = "识别结果列表")
    private List<DeviceDetectionDTO> detections;
}
