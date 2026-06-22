package com.citrus.calendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "farming_calendar", autoResultMap = true)
public class FarmingCalendar {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("orchard_id")
    private Long orchardId;

    @TableField("phenology_name")
    private String phenologyName;

    @TableField("solar_term")
    private String solarTerm;

    @TableField("start_month")
    private Integer startMonth;

    @TableField("start_day")
    private Integer startDay;

    @TableField("end_month")
    private Integer endMonth;

    @TableField("end_day")
    private Integer endDay;

    @TableField("advice_content")
    private String adviceContent;

    @TableField(value = "disease_related", typeHandler = JacksonTypeHandler.class)
    private List<String> diseaseRelated;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
