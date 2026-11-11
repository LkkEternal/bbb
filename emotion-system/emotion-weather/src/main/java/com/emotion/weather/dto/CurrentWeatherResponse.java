package com.emotion.weather.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrentWeatherResponse {

    private String cityName;
    private String weatherMain;
    private BigDecimal temperature;
    private Integer humidity;
    private String windDir;
    private String windScale;
    private String obsTime;
}
