package com.citrus.orchard.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 果园列表响应VO
 */
@Data
public class OrchardListVO {

    /**
     * 果园ID
     */
    private Long id;

    /**
     * 果园名称
     */
    private String name;

    /**
     * 详细地址
     */
    private String formattedAddress;

    /**
     * 经纬度
     */
    private String location;

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

    /**
     * 用户角色：owner=园主 / manager=管理员 / viewer=只读
     */
    private String roleType;

    /**
     * 设备数量
     */
    private Integer deviceCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
