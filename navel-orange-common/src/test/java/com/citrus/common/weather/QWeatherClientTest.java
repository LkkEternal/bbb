package com.citrus.common.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = QWeatherClientTest.TestConfig.class)
public class QWeatherClientTest {

    @SpringBootApplication
    static class TestConfig {
    }

    @Autowired
    private QWeatherClient qWeatherClient;

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    public void testGetWeatherNow() throws JsonProcessingException {
        log.info("===== 测试获取实时天气 =====");
        QWeatherClient.NowResponse response = qWeatherClient.getWeatherNow("114.93,25.38");

        log.info("响应码: {}", response.getCode());
        if (response.getNow() != null) {
            QWeatherClient.NowResponse.Now now = response.getNow();
            log.info("温度: {}℃, 天气: {}, 湿度: {}%", now.getTemp(), now.getText(), now.getHumidity());
        }
        log.info("完整响应:\n{}", objectMapper.writeValueAsString(response));
    }

    @Test
    public void testGetWeatherHourly() throws JsonProcessingException {
        log.info("===== 测试获取24小时天气预报 =====");
        QWeatherClient.HourlyResponse response = qWeatherClient.getWeatherHourly("114.93,25.38");

        log.info("响应码: {}", response.getCode());
        if (response.getHourly() != null) {
            log.info("预报小时数: {}", response.getHourly().size());
        }
        log.info("完整响应:\n{}", objectMapper.writeValueAsString(response));
    }

    @Test
    public void testGetWeatherDaily() throws JsonProcessingException {
        log.info("===== 测试获取3天天气预报 =====");
        QWeatherClient.DailyResponse response = qWeatherClient.getWeatherDaily("114.93,25.38");

        log.info("响应码: {}", response.getCode());
        if (response.getDaily() != null) {
            log.info("预报天数: {}", response.getDaily().size());
        }
        log.info("完整响应:\n{}", objectMapper.writeValueAsString(response));
    }
}
