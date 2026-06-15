package com.citrus.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求参数
 */
@Data
public class RefreshTokenDTO {

    @NotBlank(message = "refreshTokenId不能为空")
    private String refreshTokenId;
}
