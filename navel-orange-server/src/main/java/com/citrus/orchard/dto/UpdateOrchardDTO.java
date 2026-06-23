package com.citrus.orchard.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新果园请求参数
 */
@Data
public class UpdateOrchardDTO {

    /**
     * 果园名称
     */
    private String name;

    /**
     * 地址
     */
    private String formattedAddress;

    /**
     * 面积（亩）
     */
    private BigDecimal areaMu;

    /**
     * 树龄（年）
     */
    private Integer treeAge;

    /**
     * 品种
     */
    private String variety;
}
