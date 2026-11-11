package com.emotion.weather.service;

import com.emotion.weather.dto.CurrentWeatherResponse;
import com.emotion.weather.dto.ForecastResponse;

public interface WeatherService {

    CurrentWeatherResponse getCurrentWeather(String location);

    CurrentWeatherResponse getCurrentWeatherByUserId(Long userId);

    CurrentWeatherResponse getLatestWeatherByUserId(Long userId);

    ForecastResponse getForecast(String location);
}
