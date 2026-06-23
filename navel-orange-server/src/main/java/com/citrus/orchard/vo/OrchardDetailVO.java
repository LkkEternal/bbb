package com.citrus.orchard.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 果园详情响应VO
 */
@Data
public class OrchardDetailVO {

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
    @JsonProperty("address")
    private String formattedAddress;

    /**
     * 经纬度
     */
    private String location;

    /**
     * 面积（亩）
     */
    @JsonProperty("area")
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
     * 当前用户的角色
     */
    private String roleType;

    /**
     * 成员列表
     */
    private List<OrchardMemberVO> members;

    /**
     * 设备列表
     */
    private List<DeviceListVO> devices;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
