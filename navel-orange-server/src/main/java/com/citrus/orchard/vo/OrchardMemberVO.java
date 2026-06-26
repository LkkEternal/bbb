package com.citrus.orchard.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 果园成员信息VO
 */
@Data
public class OrchardMemberVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色类型：owner=园主 / manager=管理员 / viewer=只读
     */
    private String roleType;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;
}
