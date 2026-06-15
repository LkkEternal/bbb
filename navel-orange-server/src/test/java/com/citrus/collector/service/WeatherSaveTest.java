package com.citrus.collector.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.collector.entity.WeatherForecast;
import com.citrus.collector.mapper.WeatherForecastMapper;
import com.citrus.common.weather.QWeatherClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WeatherSaveTest {

    @Autowired
    private QWeatherClient qWeatherClient;

    @Autowired
    private WeatherForecastMapper weatherForecastMapper;

    private static final Long TEST_ORCHARD_ID = 2001L;
    private static final String TEST_LOCATION = "114.93,25.38";

    @Test
    @Order(1)
    public void testSaveHourlyForecast() {
        log.info("===== 测试保存24小时天气预报到数据库 =====");

        // 1. 调用API获取数据
        QWeatherClient.HourlyResponse response = qWeatherClient.getWeatherHourly(TEST_LOCATION);
        log.info("API返回码: {}", response.getCode());

        if (response.getHourly() == null || response.getHourly().isEmpty()) {
            log.error("未获取到天气数据");
            return;
        }

        // 2. 转换并保存
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        int successCount = 0;

        for (QWeatherClient.HourlyResponse.Hourly hourly : response.getHourly()) {
            try {
                WeatherForecast forecast = new WeatherForecast();
                forecast.setOrchardId(TEST_ORCHARD_ID);
                forecast.setFxTime(LocalDateTime.parse(hourly.getFxTime(), formatter));
                forecast.setTemp(new BigDecimal(hourly.getTemp()));
                forecast.setHumidity(new BigDecimal(hourly.getHumidity()));
                forecast.setPop(new BigDecimal(hourly.getPop()));
                forecast.setPrecip(new BigDecimal(hourly.getPrecip()));
                forecast.setText(hourly.getText());
                forecast.setWindSpeed(new BigDecimal(hourly.getWindSpeed()));
                forecast.setApiSource("qweather");
                forecast.setFetchTime(now);
                forecast.setCreatedAt(now);

                int rows = weatherForecastMapper.insert(forecast);
                if (rows > 0) {
                    successCount++;
                    log.info("保存成功: {} - {}℃ - {}", hourly.getFxTime(), hourly.getTemp(), hourly.getText());
                } else {
                    log.warn("保存返回0行: {}", hourly.getFxTime());
                }
            } catch (Exception e) {
                log.error("保存失败: {}", hourly.getFxTime(), e);
            }
        }

        log.info("保存完成，成功: {}/{}", successCount, response.getHourly().size());
    }

    @Test
    @Order(2)
    public void testQueryForecast() {
        log.info("===== 测试查询天气预报数据 =====");

        LambdaQueryWrapper<WeatherForecast> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeatherForecast::getOrchardId, TEST_ORCHARD_ID)
               .orderByDesc(WeatherForecast::getFxTime)
               .last("LIMIT 10");

        List<WeatherForecast> list = weatherForecastMapper.selectList(wrapper);
        log.info("查询到 {} 条记录", list.size());

        for (WeatherForecast forecast : list) {
            log.info("时间: {}, 温度: {}℃, 天气: {}, 湿度: {}%",
                    forecast.getFxTime(),
                    forecast.getTemp(),
                    forecast.getText(),
                    forecast.getHumidity());
        }
    }

    @Test
    @Order(3)
    public void testSaveDailyForecast() {
        log.info("===== 测试保存3天天气预报到数据库 =====");

        // 1. 调用API获取数据
        QWeatherClient.DailyResponse response = qWeatherClient.getWeatherDaily(TEST_LOCATION);
        log.info("API返回码: {}", response.getCode());

        if (response.getDaily() == null || response.getDaily().isEmpty()) {
            log.error("未获取到天气数据");
            return;
        }

        // 2. 转换并保存
        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;

        for (QWeatherClient.DailyResponse.Daily daily : response.getDaily()) {
            try {
                WeatherForecast forecast = new WeatherForecast();
                forecast.setOrchardId(TEST_ORCHARD_ID);
                forecast.setFxTime(LocalDateTime.parse(daily.getFxDate() + "T08:00:00"));
                forecast.setTemp(new BigDecimal(daily.getTempMax()));
                forecast.setHumidity(new BigDecimal(daily.getHumidity()));
                forecast.setPop(BigDecimal.ZERO);
                forecast.setPrecip(new BigDecimal(daily.getPrecip()));
                forecast.setText(daily.getTextDay());
                forecast.setWindSpeed(new BigDecimal(daily.getWindSpeedDay()));
                forecast.setApiSource("qweather");
                forecast.setFetchTime(now);
                forecast.setCreatedAt(now);

                int rows = weatherForecastMapper.insert(forecast);
                if (rows > 0) {
                    successCount++;
                    log.info("保存成功: {} - 最高{}℃ - {}", daily.getFxDate(), daily.getTempMax(), daily.getTextDay());
                }
            } catch (Exception e) {
                log.error("保存失败: {}", daily.getFxDate(), e);
            }
        }

        log.info("保存完成，成功: {}/{}", successCount, response.getDaily().size());
    }
}
