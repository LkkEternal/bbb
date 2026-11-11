package com.emotion.weather.dto;

import lombok.Data;

import java.util.List;

@Data
public class ForecastResponse {

    private List<DailyForecast> daily;

    @Data
    public static class DailyForecast {
        private String fxDate;
        private Integer tempMax;
        private Integer tempMin;
        private String textDay;
        private String textNight;
        private Integer humidity;
        private Integer uvIndex;
        private String sunrise;
        private String sunset;
    }
}
