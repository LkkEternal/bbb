package com.citrus.calendar.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 农事日历实体类
 * 对应数据库表 farming_calendar
 */
@Data
@TableName("farming_calendar")
public class FarmingCalendar {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 果园ID，NULL表示全局通用 */
    @TableField("orchard_id")
    private Long orchardId;

    /** 物候期名称，如"春梢萌发期" */
    @TableField("phenology_name")
    private String phenologyName;

    /** 关联节气，如"惊蛰" */
    @TableField("solar_term")
    private String solarTerm;

    /** 开始月份 */
    @TableField("start_month")
    private Integer startMonth;

    /** 开始日期 */
    @TableField("start_day")
    private Integer startDay;

    /** 结束月份 */
    @TableField("end_month")
    private Integer endMonth;

    /** 结束日期 */
    @TableField("end_day")
    private Integer endDay;

    /** 农事建议内容 */
    @TableField("advice_content")
    private String adviceContent;

    /** 关联病害编码JSON数组，如 ["S14","S10"] */
    @TableField("disease_related")
    private String diseaseRelated;

    /** 排序序号 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否启用 */
    @TableField("enabled")
    private Boolean enabled;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
