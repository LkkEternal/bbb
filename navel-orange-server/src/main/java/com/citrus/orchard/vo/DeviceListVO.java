package com.citrus.orchard.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备列表响应VO
 */
@Data
public class DeviceListVO {

    /**
     * 设备ID
     */
    private Long id;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备类型：camera=摄像头 sensor=传感器
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
     * 创建时间
     */
    private LocalDateTime createdAt;
}
