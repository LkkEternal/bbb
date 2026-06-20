package com.citrus.collector.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实时天气响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实时天气数据")
public class WeatherNowVO {

    @Schema(description = "数据观测时间", example = "2026-06-10T10:08+08:00")
    private String obsTime;

    @Schema(description = "温度（℃）", example = "23")
    private String temp;

    @Schema(description = "体感温度（℃）", example = "23")
    private String feelsLike;

    @Schema(description = "天气状况文字描述", example = "阴")
    private String text;

    @Schema(description = "风向", example = "东风")
    private String windDir;

    @Schema(description = "风力等级", example = "1")
    private String windScale;

    @Schema(description = "风速（km/h）", example = "5")
    private String windSpeed;

    @Schema(description = "相对湿度（%）", example = "55")
    private String humidity;

    @Schema(description = "降水量（mm）", example = "0.0")
    private String precip;

    @Schema(description = "能见度（km）", example = "28")
    private String vis;
}
