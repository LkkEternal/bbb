package com.citrus.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人信息响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 手机号（脱敏：138****8000）
     */
    private String phone;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 角色列表
     */
    private List<RoleVO> roles;

    /**
     * 状态：0=禁用 1=启用
     */
    private Integer status;

    /**
     * 邮件通知开关
     */
    private Boolean emailEnabled;

    /**
     * 推送通知开关
     */
    private Boolean pushEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 邮箱
     */
    private String email;
}
