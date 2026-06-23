package com.citrus.alert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则实体类
 * 对应数据库表：alert_rule
 */
@Data
@TableName("alert_rule")
public class AlertRule {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则编码：R001, R002, R003...
     */
    private String code;

    /**
     * 预警等级：1=L1 低风险 / 2=L2 中风险 / 3=L3 高风险
     */
    private Integer level;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 建议模板
     */
    @TableField("advice_template")
    private String adviceTemplate;

    /**
     * 温度下限
     */
    @TableField("temp_min")
    private BigDecimal tempMin;

    /**
     * 温度上限
     */
    @TableField("temp_max")
    private BigDecimal tempMax;

    /**
     * 湿度下限
     */
    @TableField("humidity_min")
    private BigDecimal humidityMin;

    /**
     * 湿度上限
     */
    @TableField("humidity_max")
    private BigDecimal humidityMax;

    /**
     * 关联病害编码
     */
    @TableField("disease_code")
    private String diseaseCode;

    /**
     * 识别次数阈值
     */
    @TableField("detection_count")
    private Integer detectionCount;

    /**
     * 统计天数窗口
     */
    @TableField("detection_days")
    private Integer detectionDays;

    /**
     * 未来降雨量下限（mm）
     */
    @TableField("forecast_rain_min")
    private BigDecimal forecastRainMin;

    /**
     * 未来风速下限
     */
    @TableField("forecast_wind_min")
    private BigDecimal forecastWindMin;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 冷却时间（小时）
     */
    @TableField("cooldown_hours")
    private Integer cooldownHours;

    /**
     * 创建人
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
