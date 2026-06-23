package com.citrus.collector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.citrus.config.JsonbTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病害识别记录实体类
 */
@Data
@TableName(value = "disease_detection", autoResultMap = true)
public class DiseaseDetection {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("device_id")
    private Long deviceId;

    @TableField("orchard_id")
    private Long orchardId;

    @TableField("disease_catalog_id")
    private Integer diseaseCatalogId;

    @TableField("disease_name_raw")
    private String diseaseNameRaw;

    @TableField("top_class_code")
    private String topClassCode;

    @TableField("top_probability")
    private BigDecimal topProbability;

    @TableField(value = "all_probabilities", typeHandler = JsonbTypeHandler.class)
    private String allProbabilities;

    @TableField("image_url")
    private String imageUrl;

    @TableField("device_timestamp")
    private LocalDateTime deviceTimestamp;

    @TableField(value = "extra_data", typeHandler = JsonbTypeHandler.class)
    private String extraData;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
