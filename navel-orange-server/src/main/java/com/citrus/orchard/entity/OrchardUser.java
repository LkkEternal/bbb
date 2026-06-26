package com.citrus.orchard.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 果园用户关联实体
 */
@Data
@TableName("orchard_user")
public class OrchardUser {

    /**
     * 果园ID（联合主键）
     */
    @TableField("orchard_id")
    private Long orchardId;

    /**
     * 用户ID（联合主键）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色类型：owner=园主 / manager=管理员 / viewer=只读
     */
    private String roleType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
