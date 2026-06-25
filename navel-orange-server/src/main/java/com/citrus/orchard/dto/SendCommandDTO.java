package com.citrus.orchard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 设备命令下发请求
 */
@Data
@Schema(description = "设备命令下发请求")
public class SendCommandDTO {

    @NotBlank(message = "命令名称不能为空")
    @Schema(description = "命令名称，如 control_ptz")
    private String commandName;

    @Schema(description = "命令参数字典，如 {\"pitch\":0, \"roll\":0, \"yaw\":45}")
    private Map<String, Object> paras;
}
