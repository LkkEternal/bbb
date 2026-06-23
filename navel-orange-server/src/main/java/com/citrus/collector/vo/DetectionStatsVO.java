package com.citrus.collector.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "识别统计数据")
public class DetectionStatsVO {

    @Schema(description = "识别总数", example = "5000")
    private Long totalCount;

    @Schema(description = "预警数量（暂未实现，返回0）", example = "0")
    private Long alertCount;

    @Schema(description = "病害分布")
    private List<DiseaseDistribution> diseaseDistribution;

    @Schema(description = "每日趋势")
    private List<DailyTrend> dailyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "病害分布项")
    public static class DiseaseDistribution {

        @Schema(description = "病害编码", example = "S4")
        private String diseaseCode;

        @Schema(description = "病害名称", example = "红鼻果")
        private String diseaseName;

        @Schema(description = "数量", example = "2000")
        private Long count;

        @Schema(description = "百分比", example = "40.0")
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日趋势项")
    public static class DailyTrend {

        @Schema(description = "日期", example = "2026-06-01")
        private String date;

        @Schema(description = "数量", example = "180")
        private Long count;

        @Schema(description = "预警数量（暂未实现，返回0）", example = "0")
        private Long alertCount;
    }
}
