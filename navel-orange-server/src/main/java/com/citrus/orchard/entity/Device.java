package com.citrus.orchard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 边缘设备实体
 */
@Data
@TableName("device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备编码（硬件烧录）
     */
    private String deviceCode;

    /**
     * 所属果园ID
     */
    private Long orchardId;

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
     * 设备API密钥（SHA256存储）
     */
    private String apiKey;

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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
