package com.citrus.orchard.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新设备请求参数
 */
@Data
public class UpdateDeviceDTO {

    /**
     * 设备名称
     */
    @Size(max = 100, message = "设备名称最长100字")
    private String name;

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
