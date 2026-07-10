package com.citrus.prediction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.citrus.config.JsonbTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风险预测结果表 — 记录每次预测的完整结果
 */
@Data
@TableName(value = "risk_prediction", autoResultMap = true)
public class RiskPrediction {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("orchard_id")
    private Long orchardId;

    /** 风险等级 0=无 1=低 2=中 3=高 */
    @TableField("risk_level")
    private Integer riskLevel;

    /** 最大概率值 */
    @TableField("risk_score")
    private BigDecimal riskScore;

    /** 4个等级的概率数组 JSONB */
    @TableField(value = "probabilities", typeHandler = JsonbTypeHandler.class)
    private String probabilities;

    /** LLM 生成的详细风险分析 */
    @TableField("llm_analysis")
    private String llmAnalysis;

    /** 防治建议 */
    @TableField("suggestions")
    private String suggestions;

    /** Top-5 风险因子 JSONB */
    @TableField(value = "top_factors", typeHandler = JsonbTypeHandler.class)
    private String topFactors;

    /** 触发类型: scheduled / detection_burst / seasonal */
    @TableField("trigger_type")
    private String triggerType;

    /** 触发详情 */
    @TableField("trigger_detail")
    private String triggerDetail;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
