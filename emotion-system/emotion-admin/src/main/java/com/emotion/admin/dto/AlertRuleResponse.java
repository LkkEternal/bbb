package com.emotion.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleResponse {

    private Integer id;
    private String ruleName;
    private String ruleCode;
    private Boolean enabled;
    private Integer priority;
    private String emotionLabels;
    private BigDecimal minConfidence;
    private BigDecimal maxConfidence;
    private String weatherConditions;
    private BigDecimal minTemperature;
    private BigDecimal maxTemperature;
    private Integer minHumidity;
    private Integer maxHumidity;
    private Integer timeWindowHours;
    private Integer minOccurrences;
    private String actionType;
    private String actionContentTemplate;
    private Integer cooldownMinutes;
}
