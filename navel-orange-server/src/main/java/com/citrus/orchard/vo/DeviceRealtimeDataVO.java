package com.citrus.orchard.vo;

import lombok.Data;

import java.util.Map;

/**
 * 设备实时数据VO
 * 包含IoTDA影子数据和设备属性
 */
@Data
public class DeviceRealtimeDataVO {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备状态：ONLINE, OFFLINE, INACTIVE
     */
    private String status;

    /**
     * 最后上报的属性数据
     * key为属性名，value为属性值
     */
    private Map<String, Object> properties;

    /**
     * 数据上报时间
     */
    private String reportTime;
}
