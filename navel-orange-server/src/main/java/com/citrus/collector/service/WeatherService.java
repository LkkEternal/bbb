package com.citrus.collector.service;

import com.citrus.collector.vo.WeatherDailyVO;
import com.citrus.collector.vo.WeatherHourlyVO;
import com.citrus.collector.vo.WeatherNowVO;

public interface WeatherService {

    WeatherNowVO getWeatherNow(String location);

    WeatherHourlyVO getWeatherHourly(String location);

    WeatherDailyVO getWeather7d(String location);

    /**
     * 获取实时天气并保存到环境数据表
     * @param orchardId 果园ID
     * @param location 经纬度，格式："经度,纬度"
     */
    void fetchAndSaveWeatherNow(Long orchardId, String location);

    /**
     * 获取所有果园的实时天气并保存
     */
    void fetchAllOrchardsWeather();

    /**
     * 获取7天天气预报并保存到天气预报表
     * @param orchardId 果园ID
     * @param location 经纬度，格式："经度,纬度"
     */
    void fetchAndSaveWeather7d(Long orchardId, String location);

    /**
     * 获取所有果园的7天天气预报并保存
     */
    void fetchAllOrchardsWeather7d();
}
