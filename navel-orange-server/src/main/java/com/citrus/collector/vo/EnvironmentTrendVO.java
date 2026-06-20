package com.citrus.collector.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 环境趋势图VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "环境趋势数据")
public class EnvironmentTrendVO {

    @Schema(description = "日期列表", example = "[\"2026-06-01\", \"2026-06-02\", \"2026-06-03\"]")
    private List<String> dates;

    @Schema(description = "温度列表（℃）")
    private List<BigDecimal> temp;

    @Schema(description = "湿度列表（%）")
    private List<BigDecimal> humidity;

    @Schema(description = "降水数据")
    private PrecipData precip;

    @Schema(description = "风速列表（km/h）")
    private List<BigDecimal> windSpeed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "降水数据")
    public static class PrecipData {

        @Schema(description = "每日降水量列表（mm）")
        private List<BigDecimal> daily;

        @Schema(description = "总降水量（mm）", example = "21.5")
        private BigDecimal total;
    }
}
