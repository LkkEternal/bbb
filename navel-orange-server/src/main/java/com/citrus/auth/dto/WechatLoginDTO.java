package com.citrus.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求参数
 */
@Data
public class WechatLoginDTO {

    @NotBlank(message = "微信登录code不能为空")
    private String code;
}
