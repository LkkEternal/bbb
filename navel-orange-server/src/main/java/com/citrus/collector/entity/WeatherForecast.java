package com.citrus.collector.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("weather_forecast")
public class WeatherForecast {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("orchard_id")
    private Long orchardId;

    @TableField("fxtime")
    private LocalDateTime fxTime;

    private BigDecimal temp;

    private BigDecimal humidity;

    private BigDecimal pop;

    private BigDecimal precip;

    private String text;

    @TableField("windspeed")
    private BigDecimal windSpeed;

    @TableField("api_source")
    private String apiSource;

    @TableField("fetch_time")
    private LocalDateTime fetchTime;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
