package com.citrus.alert.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则列表VO
 * 用于规则列表接口的响应数据
 *
 * 响应格式严格遵守API接口文档
 */
@Data
public class RuleListVO {

    /**
     * 规则ID
     */
    private Integer id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则编码（如 R001, R002）
     */
    private String code;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 关联病害编码
     */
    private String diseaseCode;

    /**
     * 关联病害名称
     */
    private String diseaseName;

    /**
     * 规则条件
     */
    private Map<String, Object> conditions;

    /**
     * 预警等级：1=L1 / 2=L2 / 3=L3
     */
    private Integer level;

    /**
     * 冷却时间（小时）
     */
    private Integer cooldownHours;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
