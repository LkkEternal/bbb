package com.citrus.orchard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 设备控制命令请求参数
 */
@Data
public class DeviceCommandDTO {

    /**
     * 服务ID
     */
    @NotBlank(message = "服务ID不能为空")
    private String serviceId;

    /**
     * 命令名称
     */
    @NotBlank(message = "命令名称不能为空")
    private String commandName;

    /**
     * 命令参数
     */
    @NotNull(message = "命令参数不能为空")
    private Map<String, Object> paras;
}
