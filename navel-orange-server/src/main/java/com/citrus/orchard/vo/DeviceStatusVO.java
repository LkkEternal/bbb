package com.citrus.orchard.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备状态响应VO
 */
@Data
public class DeviceStatusVO {

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备类型
     */
    private String type;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 安装位置描述
     */
    private String locationDesc;

    /**
     * 状态：0=离线 1=在线 2=故障
     */
    private Integer status;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * IoTDA设备状态
     */
    private String iotdaStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
