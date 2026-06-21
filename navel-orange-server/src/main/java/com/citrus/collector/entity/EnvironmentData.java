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
 * 环境数据实体类
 */
@Data
@TableName(value = "environment_data", autoResultMap = true)
public class EnvironmentData {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("orchard_id")
    private Long orchardId;

    @TableField("data_source")
    private String dataSource;

    private BigDecimal temp;

    private BigDecimal humidity;

    private BigDecimal precip;

    @TableField("windSpeed")
    private BigDecimal windSpeed;

    @TableField("record_time")
    private LocalDateTime recordTime;

    @TableField(value = "extra_data", typeHandler = JsonbTypeHandler.class)
    private String extraData;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
