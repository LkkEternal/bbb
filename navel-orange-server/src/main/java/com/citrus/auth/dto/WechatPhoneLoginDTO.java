package com.citrus.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatPhoneLoginDTO {

    @NotBlank(message = "微信登录code不能为空")
    private String code;

    @NotBlank(message = "加密数据不能为空")
    private String encryptedData;

    @NotBlank(message = "加密向量不能为空")
    private String iv;
}
