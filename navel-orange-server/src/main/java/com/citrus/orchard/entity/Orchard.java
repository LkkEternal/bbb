package com.citrus.orchard.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.citrus.orchard.handler.GeographyTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 果园实体
 */
@Data
@TableName("orchard")
public class Orchard {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 园主ID
     */
    private Long ownerId;

    /**
     * 果园名称
     */
    private String name;

    /**
     * 详细地址
     */
    private String formattedAddress;

    /**
     * 经纬度，格式：经度,纬度
     */
    @TableField(typeHandler = GeographyTypeHandler.class)
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
     * 状态：0=停用 1=正常
     */
    private Integer status;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
