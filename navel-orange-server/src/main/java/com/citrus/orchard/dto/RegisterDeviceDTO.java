package com.citrus.orchard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册设备请求参数
 */
@Data
public class RegisterDeviceDTO {

    /**
     * 设备编码，唯一
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 64, message = "设备编码最长64位")
    private String deviceCode;

    /**
     * 绑定果园ID
     */
    @NotNull(message = "果园ID不能为空")
    private Long orchardId;

    /**
     * 设备名称
     * 留空则自动从IoTDA获取
     */
    @Size(max = 100, message = "设备名称最长100字")
    private String name;

    /**
     * 设备类型：camera=摄像头 sensor=传感器
     */
    @NotBlank(message = "设备类型不能为空")
    private String type;

    /**
     * 设备型号
     */
    @Size(max = 50, message = "设备型号最长50字")
    private String model;

    /**
     * 安装位置描述
     */
    @Size(max = 100, message = "安装位置描述最长100字")
    private String locationDesc;
}
