package com.emotion.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则实体类
 * 定义系统自动触发的预警条件
 *
 * 字段说明：
 * - id: 主键ID
 * - ruleName: 规则名称
 * - ruleCode: 规则编码（唯一标识）
 * - enabled: 是否启用（1=启用，0=禁用）
 * - priority: 优先级（数字越小优先级越高）
 * - emotionLabels: 触发的情感标签（逗号分隔，如"happy,sad"）
 * - minConfidence: 最小置信度
 * - maxConfidence: 最大置信度
 * - weatherConditions: 天气条件（如"sunny,rainy"）
 * - minTemperature: 最低温度
 * - maxTemperature: 最高温度
 * - minHumidity: 最低湿度（0-100）
 * - maxHumidity: 最高湿度（0-100）
 * - timeWindowHours: 时间窗口（小时）
 * - minOccurrences: 时间窗口内最小触发次数
 * - actionType: 动作类型（alert/notification等）
 * - actionContentTemplate: 动作内容模板
 * - cooldownMinutes: 冷却时间（分钟）
 * - createdAt: 创建时间
 * - updatedAt: 更新时间
 *
 * 使用场景：
 * - 连续检测到特定情感超过一定次数时触发预警
 * - 结合时间和情感条件设置智能预警
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("alert_rule")
public class AlertRule {

    /** 主键ID */
    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 规则名称 */
    private String ruleName;

    /** 规则编码（唯一标识） */
    private String ruleCode;

    /** 是否启用（1=启用，0=禁用） */
    private Integer enabled;

    /** 优先级（数字越小优先级越高） */
    private Integer priority;

    /** 触发的情感标签（逗号分隔） */
    private String emotionLabels;

    /** 最小置信度 */
    private BigDecimal minConfidence;

    /** 最大置信度 */
    private BigDecimal maxConfidence;

    /** 天气条件 */
    private String weatherConditions;

    /** 最低温度 */
    private BigDecimal minTemperature;

    /** 最高温度 */
    private BigDecimal maxTemperature;

    /** 最低湿度（0-100） */
    private Integer minHumidity;

    /** 最高湿度（0-100） */
    private Integer maxHumidity;

    /** 时间窗口（小时） */
    private Integer timeWindowHours;

    /** 时间窗口内最小触发次数 */
    private Integer minOccurrences;

    /** 动作类型（alert/notification等） */
    private String actionType;

    /** 动作内容模板 */
    private String actionContentTemplate;

    /** 冷却时间（分钟） */
    private Integer cooldownMinutes;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
