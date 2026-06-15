package com.citrus.collector.service.impl;

import com.citrus.collector.service.WeatherService;
import com.citrus.common.weather.QWeatherClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final QWeatherClient qWeatherClient;

    @Override
    public QWeatherClient.NowResponse getWeatherNow(String location) {
        return qWeatherClient.getWeatherNow(location);
    }

    @Override
    public QWeatherClient.HourlyResponse getWeatherHourly(String location) {
        return qWeatherClient.getWeatherHourly(location);
    }

    @Override
    public QWeatherClient.DailyResponse getWeatherDaily(String location) {
        return qWeatherClient.getWeatherDaily(location);
    }
}
