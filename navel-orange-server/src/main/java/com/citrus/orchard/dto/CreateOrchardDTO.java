package com.citrus.orchard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建果园请求参数
 */
@Data
public class CreateOrchardDTO {

    /**
     * 果园名称
     */
    @NotBlank(message = "果园名称不能为空")
    @Size(max = 25, message = "果园名称最长25字")
    private String name;

    /**
     * 地址
     */
    @NotBlank(message = "地址不能为空")
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
