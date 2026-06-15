package com.citrus.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户状态请求参数
 */
@Data
public class UpdateUserStatusDTO {

    /**
     * 状态：0=禁用, 1=启用
     */
    @NotNull(message = "状态值不能为空")
    private Integer status;
}
