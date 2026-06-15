package com.citrus.collector.service;

import com.citrus.common.weather.QWeatherClient;

public interface WeatherService {

    QWeatherClient.NowResponse getWeatherNow(String location);

    QWeatherClient.HourlyResponse getWeatherHourly(String location);

    QWeatherClient.DailyResponse getWeatherDaily(String location);
}
