package com.citrus.collector.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 24小时天气预报响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "24小时天气预报数据")
public class WeatherHourlyVO {

    @Schema(description = "逐小时预报列表")
    private List<HourlyItem> hourly;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "逐小时预报项")
    public static class HourlyItem {

        @Schema(description = "预报时间", example = "2026-06-10T19:00+08:00")
        private String fxTime;

        @Schema(description = "温度（℃）", example = "22")
        private String temp;

        @Schema(description = "天气状况文字描述", example = "多云")
        private String text;

        @Schema(description = "风向", example = "西南风")
        private String windDir;

        @Schema(description = "风力等级", example = "1-3")
        private String windScale;

        @Schema(description = "风速（km/h）", example = "7")
        private String windSpeed;

        @Schema(description = "相对湿度（%）", example = "66")
        private String humidity;

        @Schema(description = "降水概率（%）", example = "0")
        private String pop;

        @Schema(description = "降水量（mm）", example = "0.0")
        private String precip;
    }
}
