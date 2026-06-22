package com.citrus.alert.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险评估结果VO
 * 用于风险评估接口的响应数据
 */
@Data
public class RiskAssessmentVO {

    /**
     * 果园ID
     */
    private Long orchardId;

    /**
     * 果园名称
     */
    private String orchardName;

    /**
     * 整体风险等级：1=L1 / 2=L2 / 3=L3
     */
    private Integer riskLevel;

    /**
     * 风险分数（0-100）
     */
    private Integer riskScore;

    /**
     * 风险因子列表
     */
    private List<RiskFactor> factors;

    /**
     * 建议列表
     */
    private List<String> suggestions;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 风险因子内部类
     */
    @Data
    public static class RiskFactor {

        /**
         * 因子类型：disease=病害 / environment=环境 / weather=天气
         */
        private String type;

        /**
         * 病害名称（仅type=disease时有值）
         */
        private String diseaseName;

        /**
         * 风险等级
         */
        private Integer riskLevel;

        /**
         * 风险描述
         */
        private String description;
    }
}
