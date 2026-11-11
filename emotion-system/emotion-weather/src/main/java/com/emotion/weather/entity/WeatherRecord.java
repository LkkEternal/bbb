package com.emotion.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("weather_record")
public class WeatherRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String location;
    private Integer recordType;
    private String weatherMain;
    private BigDecimal temperature;
    private Integer humidity;
    private String windDir;
    private String windScale;
    private LocalDate fxDate;
    private LocalDateTime obsTime;
    private String rawJson;
    private LocalDateTime createdAt;
}
