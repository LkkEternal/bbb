package com.emotion.detection.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("environment_record")
public class EnvironmentRecord {

    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long detectionRecordId;

    private Integer sourceType;

    private String weatherMain;

    private BigDecimal temperature;

    private Integer humidity;

    private String windDir;

    private String windScale;

    private String locationName;

    private Integer indoorOutdoor;

    private String extraNotes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
