package com.citrus.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关联实体类
 */
@Data
@TableName("sys_user_role")
public class UserRole {

    private Long userId;

    private Integer roleId;
}
