package com.citrus.collector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "设备心跳请求参数")
public class DeviceHeartbeatDTO {

    @Schema(description = "空气温度（℃）", example = "23.6")
    private BigDecimal airTemp;

    @Schema(description = "空气湿度（%）", example = "67.6")
    private BigDecimal humidity;

    @Schema(description = "土壤含水率（%）", example = "60")
    private BigDecimal moisture;

    @Schema(description = "土壤温度（℃）", example = "25.5")
    private BigDecimal soilTemp;

    @Schema(description = "电导率（uS/cm）", example = "0")
    private BigDecimal conductivity;

    @Schema(description = "PH值", example = "7.6")
    private BigDecimal ph;
}
