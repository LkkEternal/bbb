package com.citrus.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
