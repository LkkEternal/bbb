package com.citrus.orchard.vo;

import lombok.Data;

/**
 * IoTDA设备信息VO
 */
@Data
public class IotdaDeviceVO {

    /**
     * 设备ID（device_id）
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 产品ID
     */
    private String productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 设备状态：ONLINE, OFFLINE, INACTIVE
     */
    private String status;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 固件版本
     */
    private String fwVersion;

    /**
     * 软件版本
     */
    private String swVersion;

    /**
     * 是否已注册到本地系统
     */
    private Boolean registered;

    /**
     * 本地设备ID（已注册到本地系统的设备才有值）
     */
    private Long localDeviceId;
}
