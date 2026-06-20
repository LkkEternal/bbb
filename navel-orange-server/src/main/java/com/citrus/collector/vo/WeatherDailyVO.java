package com.citrus.collector.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 7天天气预报响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "7天天气预报数据")
public class WeatherDailyVO {

    @Schema(description = "每日预报列表")
    private List<DailyItem> daily;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日预报项")
    public static class DailyItem {

        @Schema(description = "预报日期", example = "2026-06-11")
        private String fxDate;

        @Schema(description = "日出时间", example = "04:46")
        private String sunrise;

        @Schema(description = "日落时间", example = "19:43")
        private String sunset;

        @Schema(description = "最高温度（℃）", example = "31")
        private String tempMax;

        @Schema(description = "最低温度（℃）", example = "19")
        private String tempMin;

        @Schema(description = "白天天气状况文字描述", example = "晴")
        private String textDay;

        @Schema(description = "夜间天气状况文字描述", example = "多云")
        private String textNight;

        @Schema(description = "白天风向", example = "南风")
        private String windDirDay;

        @Schema(description = "白天风力等级", example = "1-3")
        private String windScaleDay;

        @Schema(description = "白天风速（km/h）", example = "3")
        private String windSpeedDay;

        @Schema(description = "夜间风向", example = "西南风")
        private String windDirNight;

        @Schema(description = "夜间风力等级", example = "1-3")
        private String windScaleNight;

        @Schema(description = "夜间风速（km/h）", example = "3")
        private String windSpeedNight;

        @Schema(description = "相对湿度（%）", example = "38")
        private String humidity;

        @Schema(description = "降水量（mm）", example = "0.0")
        private String precip;

        @Schema(description = "紫外线指数", example = "11")
        private String uvIndex;
    }
}
