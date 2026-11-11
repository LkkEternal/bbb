package com.emotion.admin.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleUpdateRequest {

    private Boolean enabled;

    private String ruleName;

    private String emotionLabels;

    private String weatherConditions;

    private BigDecimal minTemperature;

    private BigDecimal maxTemperature;

    private Integer minHumidity;

    private Integer maxHumidity;

    @Min(value = 1, message = "最少出现次数不能小于1")
    private Integer minOccurrences;

    @Min(value = 1, message = "统计窗口不能小于1小时")
    private Integer timeWindowHours;

    @Min(value = 1, message = "冷却时间不能小于1分钟")
    private Integer cooldownMinutes;
}
