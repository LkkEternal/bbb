package com.citrus.auth.vo;

import com.citrus.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录/注册响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private Long id;

    private String phone;

    private String username;

    private String avatarUrl;

    private String accessToken;

    private String refreshTokenId;

    private Long expiresIn;

    private List<Role> roles;

    /** 是否新用户（微信小程序登录时使用） */
    private Boolean isNewUser;

    /** 微信openid（未绑定时返回，用于引导绑定） */
    private String wechatOpenid;

    /** 是否需要引导绑定（true=需要前端展示绑定/注册引导） */
    private Boolean needsBind;
}
