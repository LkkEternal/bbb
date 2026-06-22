package com.citrus.alert.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新规则DTO
 * 用于更新规则阈值接口的请求参数
 */
@Data
public class UpdateRuleDTO {

    /**
     * 最低温度
     */
    private BigDecimal tempMin;

    /**
     * 最高温度
     */
    private BigDecimal tempMax;

    /**
     * 最低湿度
     */
    private BigDecimal humidityMin;

    /**
     * 最高湿度
     */
    private BigDecimal humidityMax;

    /**
     * 病害编码
     */
    private String diseaseCode;

    /**
     * 识别次数阈值
     */
    private Integer detectionCount;

    /**
     * 统计天数
     */
    private Integer detectionDays;

    /**
     * 冷却时间（小时）
     */
    private Integer cooldownHours;
}
