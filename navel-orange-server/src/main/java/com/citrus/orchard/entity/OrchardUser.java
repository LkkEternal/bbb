package com.citrus.orchard.entity;

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
     * 果园ID
     */
    private Long orchardId;

    /**
     * 用户ID
     */
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
