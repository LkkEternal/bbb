package com.citrus.ota.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 固件包
 */
@Data
@TableName("firmware_package")
public class FirmwarePackage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String version;

    private Long fileSize;

    private String fileUrl;

    private String md5;

    private String description;

    /** 0=待发布 1=已发布 2=已废弃 */
    private Integer status;

    /** 华为云 IoTDA 产品 ID */
    private String otaProductId;

    /** 华为云 IoTDA 升级包 ID */
    private String otaPackageId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
