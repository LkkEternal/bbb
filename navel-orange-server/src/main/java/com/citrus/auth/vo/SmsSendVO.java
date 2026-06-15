package com.citrus.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendVO {

    /**
     * 验证码过期时间（秒）
     */
    private Integer expireSeconds;
}
