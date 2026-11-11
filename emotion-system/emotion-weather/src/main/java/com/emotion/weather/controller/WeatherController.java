package com.emotion.weather.controller;

import com.emotion.common.response.Result;
import com.emotion.weather.dto.CurrentWeatherResponse;
import com.emotion.weather.dto.ForecastResponse;
import com.emotion.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    public Result<CurrentWeatherResponse> getCurrentWeather(@RequestParam(required = false) String location) {
        CurrentWeatherResponse response;
        if (location != null && !location.isBlank()) {
            response = weatherService.getCurrentWeather(location);
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) auth.getPrincipal();
            response = weatherService.getCurrentWeatherByUserId(userId);
        }
        return Result.success(response);
    }

    @GetMapping("/latest")
    public Result<CurrentWeatherResponse> getLatestWeather() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        return Result.success(weatherService.getLatestWeatherByUserId(userId));
    }

    @GetMapping("/forecast")
    public Result<ForecastResponse> getForecast(@RequestParam String location) {
        ForecastResponse response = weatherService.getForecast(location);
        return Result.success(response);
    }
}
