package com.citrus.collector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病害字典实体类
 */
@Data
@TableName("disease_catalog")
public class DiseaseCatalog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String diseaseCode;

    private String diseaseName;

    private String diseaseNameEn;

    private String diseaseType;

    private String category;

    private Integer severityLevel;

    private String symptomDesc;

    private String preventionGuide;

    private LocalDateTime createdAt;
}
