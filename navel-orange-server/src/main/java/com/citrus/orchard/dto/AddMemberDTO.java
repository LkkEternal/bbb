package com.citrus.orchard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加果园成员请求参数
 */
@Data
public class AddMemberDTO {

    /**
     * 被添加用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 角色类型：manager=管理员, viewer=查看者
     */
    @NotBlank(message = "角色类型不能为空")
    private String roleType;
}
