package com.citrus.collector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 天气预报表实体类
 * 对应数据库表：weather_forecast
 */
@Data
@TableName("weather_forecast")
public class WeatherForecast {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 果园ID
     */
    @TableField("orchard_id")
    private Long orchardId;

    /**
     * 预报时间
     */
    @TableField("fxtime")
    private LocalDateTime fxTime;

    /**
     * 最高温度（℃）
     */
    @TableField("temp_max")
    private BigDecimal tempMax;

    /**
     * 最低温度（℃）
     */
    @TableField("temp_min")
    private BigDecimal tempMin;

    /**
     * 湿度（%）
     */
    private BigDecimal humidity;

    /**
     * 降水概率（%）
     */
    private BigDecimal pop;

    /**
     * 降水量（mm）
     */
    private BigDecimal precip;

    /**
     * 天气描述
     */
    private String text;

    /**
     * 风速（km/h）
     */
    @TableField("windspeed")
    private BigDecimal windSpeed;

    /**
     * API来源
     */
    @TableField("api_source")
    private String apiSource;

    /**
     * 数据获取时间
     */
    @TableField("fetch_time")
    private LocalDateTime fetchTime;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
