package com.citrus.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新Token响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenVO {

    /**
     * 新的访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌标识ID
     */
    private String refreshTokenId;

    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;
}
