package com.emotion.weather.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.emotion.infrastructure.client.AmapClient;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import com.emotion.weather.dto.CurrentWeatherResponse;
import com.emotion.weather.dto.ForecastResponse;
import com.emotion.weather.entity.WeatherRecord;
import com.emotion.weather.mapper.WeatherRecordMapper;
import com.emotion.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final AmapClient amapClient;
    private final WeatherRecordMapper weatherRecordMapper;
    private final UserMapper userMapper;

    @Override
    public CurrentWeatherResponse getCurrentWeatherByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getLocationId() == null || user.getLocationId().isBlank()) {
            log.warn("用户位置信息不存在: userId={}", userId);
            return null;
        }
        log.info("根据用户位置获取天气: userId={}, locationId={}", userId, user.getLocationId());
        CurrentWeatherResponse response = getCurrentWeather(user.getLocationId());
        if (response != null) {
            response.setCityName(user.getCityName());
        }
        return response;
    }

    @Override
    public CurrentWeatherResponse getCurrentWeather(String location) {
        AmapClient.WeatherLiveResult liveResult = amapClient.getLiveWeather(location);
        if (liveResult == null || liveResult.getLives() == null || liveResult.getLives().isEmpty()) {
            return null;
        }
        AmapClient.WeatherLive live = liveResult.getLives().get(0);
        CurrentWeatherResponse result = new CurrentWeatherResponse();
        result.setWeatherMain(live.getWeather());
        result.setTemperature(parseBigDecimal(live.getTemperature()));
        result.setHumidity(parseInt(live.getHumidity()));
        result.setWindDir(live.getWindDirection());
        result.setWindScale(live.getWindPower());
        result.setObsTime(live.getReportTime());

        saveCurrentWeather(location, live);
        return result;
    }

    @Override
    public CurrentWeatherResponse getLatestWeatherByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getLocationId() == null || user.getLocationId().isBlank()) {
            log.warn("用户位置信息不存在，无法回显天气: userId={}", userId);
            return null;
        }
        WeatherRecord latest = weatherRecordMapper.selectOne(
                new LambdaQueryWrapper<WeatherRecord>()
                        .eq(WeatherRecord::getLocation, user.getLocationId())
                        .eq(WeatherRecord::getRecordType, 1)
                        .orderByDesc(WeatherRecord::getCreatedAt)
                        .orderByDesc(WeatherRecord::getId)
                        .last("LIMIT 1"));
        if (latest == null) {
            log.info("天气记录不存在，无法回显: userId={}, location={}", userId, user.getLocationId());
            return null;
        }
        CurrentWeatherResponse result = new CurrentWeatherResponse();
        result.setCityName(user.getCityName());
        result.setWeatherMain(latest.getWeatherMain());
        result.setTemperature(latest.getTemperature());
        result.setHumidity(latest.getHumidity());
        result.setWindDir(latest.getWindDir());
        result.setWindScale(latest.getWindScale());
        LocalDateTime obsTime = latest.getObsTime() != null ? latest.getObsTime() : latest.getCreatedAt();
        if (obsTime != null) {
            result.setObsTime(obsTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return result;
    }

    @Override
    public ForecastResponse getForecast(String location) {
        AmapClient.WeatherForecastResult forecastResult = amapClient.getForecastWeather(location);
        if (forecastResult == null || forecastResult.getForecasts() == null || forecastResult.getForecasts().isEmpty()) {
            return null;
        }
        AmapClient.WeatherForecast forecast = forecastResult.getForecasts().get(0);
        ForecastResponse result = new ForecastResponse();
        List<ForecastResponse.DailyForecast> dailyList = new ArrayList<>();
        if (forecast.getCasts() != null) {
            for (AmapClient.ForecastCast cast : forecast.getCasts()) {
                ForecastResponse.DailyForecast daily = new ForecastResponse.DailyForecast();
                daily.setFxDate(cast.getDate());
                daily.setTempMax(parseInt(cast.getDayTemp()));
                daily.setTempMin(parseInt(cast.getNightTemp()));
                daily.setTextDay(cast.getDayWeather());
                daily.setTextNight(cast.getNightWeather());
                dailyList.add(daily);
            }
        }
        result.setDaily(dailyList);

        saveForecastWeather(location, forecast);
        return result;
    }

    @Async
    public void saveCurrentWeather(String location, AmapClient.WeatherLive live) {
        try {
            WeatherRecord record = new WeatherRecord();
            record.setLocation(location);
            record.setRecordType(1);
            record.setWeatherMain(live.getWeather());
            record.setTemperature(parseBigDecimal(live.getTemperature()));
            record.setHumidity(parseInt(live.getHumidity()));
            record.setWindDir(live.getWindDirection());
            record.setWindScale(live.getWindPower());
            if (live.getReportTime() != null) {
                try {
                    record.setObsTime(LocalDateTime.parse(live.getReportTime(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    record.setObsTime(LocalDateTime.now());
                }
            }
            record.setCreatedAt(LocalDateTime.now());
            weatherRecordMapper.insert(record);
            log.info("天气实时数据已保存: location={}, id={}", location, record.getId());
        } catch (Exception e) {
            log.error("保存天气实时数据失败: location={}", location, e);
        }
    }

    public void saveForecastWeather(String location, AmapClient.WeatherForecast forecast) {
        try {
            if (forecast.getCasts() == null) return;
            for (AmapClient.ForecastCast cast : forecast.getCasts()) {
                WeatherRecord record = new WeatherRecord();
                record.setLocation(location);
                record.setRecordType(2);
                record.setWeatherMain(cast.getDayWeather());
                if (cast.getDate() != null) {
                    record.setFxDate(LocalDate.parse(cast.getDate()));
                }
                record.setTemperature(parseBigDecimal(cast.getDayTemp()));
                record.setHumidity(parseInt(cast.getDayHumidity()));
                record.setCreatedAt(LocalDateTime.now());
                weatherRecordMapper.insert(record);
            }
            log.info("天气预报数据已保存: location={}, days={}", location, forecast.getCasts().size());
        } catch (Exception e) {
            log.error("保存天气预报数据失败: location={}", location, e);
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }
}
