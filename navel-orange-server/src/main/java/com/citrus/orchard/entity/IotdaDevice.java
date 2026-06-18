package com.citrus.orchard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IoTDA设备信息表
 * 存储从华为云IoTDA平台同步的设备信息
 */
@Data
@TableName("iotda_device")
public class IotdaDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * IoTDA设备ID（唯一标识）
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
     * 关联的本地设备ID（device表的id）
     */
    private Long localDeviceId;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
