package com.citrus.collector.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 环境数据列表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "环境数据列表项")
public class EnvironmentListVO {

    @Schema(description = "记录ID", example = "60001")
    private Long id;

    @Schema(description = "设备编码", example = "DEV-2026-001")
    private String deviceCode;

    @Schema(description = "果园ID", example = "2001")
    private Long orchardId;

    @Schema(description = "温度（℃）", example = "28.5")
    private BigDecimal temp;

    @Schema(description = "相对湿度（%）", example = "75.2")
    private BigDecimal humidity;

    @Schema(description = "降水量（mm）", example = "0.0")
    private BigDecimal precip;

    @Schema(description = "风速（km/h）", example = "2.3")
    private BigDecimal windSpeed;

    @Schema(description = "数据来源", example = "weather_api")
    private String dataSource;

    @Schema(description = "记录时间", example = "2026-06-07 08:00:00")
    private String recordTime;
}
