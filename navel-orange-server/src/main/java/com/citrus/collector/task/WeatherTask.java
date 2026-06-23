package com.citrus.collector.task;

import com.citrus.collector.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 天气数据定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherTask {

    private final WeatherService weatherService;

    /**
     * 每2小时获取所有果园的实时天气数据
     * cron表达式：秒 分 时 日 月 周
     * 0 0 0/2 * * ? 表示每2小时执行一次（0分0秒）
     */
    @Scheduled(cron = "0 0 0/2 * * ?")
    public void fetchWeatherDataTask() {
        log.info("========== 开始执行天气数据定时任务 ==========");
        long startTime = System.currentTimeMillis();
        
        try {
            weatherService.fetchAllOrchardsWeather();
        } catch (Exception e) {
            log.error("天气数据定时任务执行失败: {}", e.getMessage(), e);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("========== 天气数据定时任务执行完成，耗时: {}ms ==========", duration);
    }

    /**
     * 每6小时获取所有果园的7天天气预报数据
     * cron表达式：秒 分 时 日 月 周
     * 0 0 0/6 * * ? 表示每6小时执行一次（0分0秒）
     */
    @Scheduled(cron = "0 0 0/6 * * ?")
    public void fetchWeather7dDataTask() {
        log.info("========== 开始执行7天天气预报定时任务 ==========");
        long startTime = System.currentTimeMillis();
        
        try {
            weatherService.fetchAllOrchardsWeather7d();
        } catch (Exception e) {
            log.error("7天天气预报定时任务执行失败: {}", e.getMessage(), e);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("========== 7天天气预报定时任务执行完成，耗时: {}ms ==========", duration);
    }
}
