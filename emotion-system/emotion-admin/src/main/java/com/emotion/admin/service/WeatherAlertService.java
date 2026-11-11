package com.emotion.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.emotion.admin.entity.AlertMessage;
import com.emotion.admin.entity.AlertRule;
import com.emotion.admin.mapper.AlertMessageMapper;
import com.emotion.admin.mapper.AlertRuleMapper;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import com.emotion.weather.entity.WeatherRecord;
import com.emotion.weather.mapper.WeatherRecordMapper;
import com.emotion.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 天气预警服务
 * <p>
 * 由两个独立的定时任务协同完成：
 * 1. {@link #refreshForecast()}：每 4 天 0 点拉取高德预报写入 weather_record（record_type=2，含白天湿度）；
 * 2. {@link #evaluateAlertRules()}：预报更新后（0:05）遍历预警规则表（alert_rule）逐条评估，
 *    某 location 最新 4 天预报完全满足规则全部条件即触发预警写入 alert_message。
 * <p>
 * 周期说明：两个任务均在每天 cron 触发，方法内以 {@code LocalDate.now().toEpochDay() % 4 == 0}
 * 守卫实现固定 4 天周期（相位锚定 Unix 纪元，仅保证每 4 天执行一次）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherAlertService {

    private static final int FORECAST_INTERVAL_DAYS = 4;
    private static final int CONSECUTIVE_DAYS = 4;
    private static final long CALL_DELAY_MS = 200L;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserMapper userMapper;
    private final WeatherService weatherService;
    private final WeatherRecordMapper weatherRecordMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final AlertMessageMapper alertMessageMapper;

    /**
     * 预报拉取定时任务：每 4 天 0 点执行一次
     * 拉取高德预报写入 weather_record（record_type=2，含白天湿度），不做规则评估
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshForecast() {
        if (!isScheduledDay()) {
            log.debug("[WeatherAlert] 非4天周期执行日，跳过: date={}", LocalDate.now());
            return;
        }
        log.info("[WeatherAlert] 开始预报拉取定时任务: date={}", LocalDate.now());

        Map<String, String> locationCityMap = findLocationCityMap();
        if (locationCityMap.isEmpty()) {
            log.info("[WeatherAlert] 没有配置 location_id 的用户，跳过");
            return;
        }

        int success = 0;
        for (String location : locationCityMap.keySet()) {
            try {
                weatherService.getForecast(location);
                success++;
            } catch (Exception e) {
                log.error("[WeatherAlert] 拉取预报失败: location={}", location, e);
            }
            sleepQuietly();
        }
        log.info("[WeatherAlert] 预报拉取完成: total={}, success={}", locationCityMap.size(), success);
    }

    /**
     * 预警规则评估定时任务：预报数据更新后触发，遍历预警规则表逐条评估
     * 对每个 location 取最新 4 条连续预报，全部满足规则条件即触发预警写入 alert_message，
     * 不满足则将同规则同位置的旧生效消息置为已解除
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void evaluateAlertRules() {
        if (!isScheduledDay()) {
            log.debug("[WeatherAlert] 非4天周期执行日，跳过: date={}", LocalDate.now());
            return;
        }
        log.info("[WeatherAlert] 开始预警规则评估定时任务: date={}", LocalDate.now());

        Map<String, String> locationCityMap = findLocationCityMap();
        if (locationCityMap.isEmpty()) {
            log.info("[WeatherAlert] 没有配置 location_id 的用户，跳过");
            return;
        }

        int triggered = 0;
        for (Map.Entry<String, String> entry : locationCityMap.entrySet()) {
            try {
                triggered += evaluateAlerts(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("[WeatherAlert] 预警评估失败: location={}", entry.getKey(), e);
            }
        }
        log.info("[WeatherAlert] 预警规则评估完成: locations={}, triggered={}", locationCityMap.size(), triggered);
    }

    /**
     * 是否为 4 天周期的执行日（相位锚定 Unix 纪元，仅保证每 4 天执行一次）
     */
    private boolean isScheduledDay() {
        return LocalDate.now().toEpochDay() % FORECAST_INTERVAL_DAYS == 0;
    }

    /**
     * 多次接口调用间的短暂延迟，避免触发高德 QPS 限制
     */
    private void sleepQuietly() {
        try {
            Thread.sleep(CALL_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[WeatherAlert] 线程被中断");
        }
    }

    /**
     * 遍历所有配置了 location_id 的用户，按 location（高德 adcode）去重，返回 location -> cityName
     */
    private Map<String, String> findLocationCityMap() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .isNotNull(User::getLocationId)
                        .ne(User::getLocationId, ""));
        Map<String, String> map = new LinkedHashMap<>();
        for (User user : users) {
            String location = user.getLocationId();
            if (location == null || location.isBlank()) {
                continue;
            }
            map.putIfAbsent(location, user.getCityName());
        }
        return map;
    }

    /**
     * 对某 location 进行预警评估：
     * 取最新 4 条连续预报（fx_date 连续，record_type=2），逐条比对启用中的含天气条件规则，
     * 全部满足则写入/去重预警消息，不满足则解除同规则同位置的旧生效消息。
     *
     * @return 触发预警的规则数
     */
    private int evaluateAlerts(String location, String cityName) {
        List<DayForecast> window = getLatestConsecutiveWindow(location);
        if (window == null) {
            deactivateAll(location);
            log.info("[WeatherAlert] location={} 无连续4天预报窗口，解除该地区全部生效预警", location);
            return 0;
        }

        LocalDate start = window.get(0).fxDate;
        LocalDate end = window.get(CONSECUTIVE_DAYS - 1).fxDate;

        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>().eq(AlertRule::getEnabled, 1));
        Set<Integer> triggeredRuleIds = new HashSet<>();
        for (AlertRule rule : rules) {
            if (!isWeatherRule(rule)) {
                continue;
            }
            if (matchesAll(rule, window)) {
                upsertAlert(rule, location, cityName, start, end, window);
                triggeredRuleIds.add(rule.getId());
            } else {
                deactivateForRule(rule, location);
            }
        }

        if (triggeredRuleIds.isEmpty()) {
            deactivateAll(location);
        } else {
            alertMessageMapper.update(null, new LambdaUpdateWrapper<AlertMessage>()
                    .eq(AlertMessage::getLocation, location)
                    .eq(AlertMessage::getActive, 1)
                    .notIn(AlertMessage::getRuleId, triggeredRuleIds)
                    .set(AlertMessage::getActive, 0));
        }
        return triggeredRuleIds.size();
    }

    /**
     * 取某 location 最新 4 条连续预报（fx_date 连续，record_type=2）；
     * 不足 4 条或日期不连续返回 null。
     */
    private List<DayForecast> getLatestConsecutiveWindow(String location) {
        List<WeatherRecord> records = weatherRecordMapper.selectList(
                new LambdaQueryWrapper<WeatherRecord>()
                        .eq(WeatherRecord::getLocation, location)
                        .eq(WeatherRecord::getRecordType, 2)
                        .isNotNull(WeatherRecord::getFxDate)
                        .orderByDesc(WeatherRecord::getFxDate));
        Map<LocalDate, WeatherRecord> latestByDate = new LinkedHashMap<>();
        for (WeatherRecord record : records) {
            latestByDate.putIfAbsent(record.getFxDate(), record);
        }
        List<LocalDate> dates = new ArrayList<>(latestByDate.keySet());
        if (dates.size() < CONSECUTIVE_DAYS) {
            return null;
        }
        List<DayForecast> window = new ArrayList<>();
        for (int i = 0; i < CONSECUTIVE_DAYS; i++) {
            if (i > 0 && !dates.get(i - 1).minusDays(1).equals(dates.get(i))) {
                return null;
            }
            WeatherRecord record = latestByDate.get(dates.get(i));
            window.add(new DayForecast(record.getFxDate(), record.getWeatherMain(),
                    record.getTemperature(), record.getHumidity()));
        }
        return window;
    }

    /**
     * 4 天预报是否全部满足规则条件（未配置的条件视为不限制）
     */
    private boolean matchesAll(AlertRule rule, List<DayForecast> window) {
        List<String> weatherList = parseCsv(rule.getWeatherConditions());
        for (DayForecast day : window) {
            if (!matches(rule, day, weatherList)) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(AlertRule rule, DayForecast day, List<String> weatherList) {
        if (!weatherList.isEmpty()) {
            if (day.weather == null || !weatherList.contains(day.weather)) {
                return false;
            }
        }
        if (rule.getMinTemperature() != null) {
            if (day.temperature == null || day.temperature.compareTo(rule.getMinTemperature()) < 0) {
                return false;
            }
        }
        if (rule.getMaxTemperature() != null) {
            if (day.temperature == null || day.temperature.compareTo(rule.getMaxTemperature()) > 0) {
                return false;
            }
        }
        if (rule.getMinHumidity() != null) {
            if (day.humidity == null || day.humidity < rule.getMinHumidity()) {
                return false;
            }
        }
        if (rule.getMaxHumidity() != null) {
            if (day.humidity == null || day.humidity > rule.getMaxHumidity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否属于天气预警规则（配置了天气/温度/湿度任一条件）
     */
    private boolean isWeatherRule(AlertRule rule) {
        return (rule.getWeatherConditions() != null && !rule.getWeatherConditions().isBlank())
                || rule.getMinTemperature() != null
                || rule.getMaxTemperature() != null
                || rule.getMinHumidity() != null
                || rule.getMaxHumidity() != null;
    }

    /**
     * 写入预警消息（同 rule_id + location + fx_date_start 已存在则跳过），
     * 并将同规则同位置的其他生效窗口置为已解除。
     */
    private void upsertAlert(AlertRule rule, String location, String cityName,
                             LocalDate start, LocalDate end, List<DayForecast> window) {
        Long exists = alertMessageMapper.selectCount(
                new LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getRuleId, rule.getId())
                        .eq(AlertMessage::getLocation, location)
                        .eq(AlertMessage::getFxDateStart, start)
                        .eq(AlertMessage::getActive, 1));
        if (exists == null || exists == 0) {
            AlertMessage message = new AlertMessage();
            message.setRuleId(rule.getId());
            message.setRuleName(rule.getRuleName());
            message.setLocation(location);
            message.setCityName(cityName);
            message.setFxDateStart(start);
            message.setFxDateEnd(end);
            message.setAlertContent(buildContent(rule, start, end, window));
            message.setActive(1);
            message.setStatus(0);
            message.setCreatedAt(LocalDateTime.now());
            alertMessageMapper.insert(message);
            log.info("[WeatherAlert] 触发预警并写入消息: rule={}, location={}, range={}~{}",
                    rule.getRuleName(), location, start, end);
        }
        alertMessageMapper.update(null, new LambdaUpdateWrapper<AlertMessage>()
                .eq(AlertMessage::getRuleId, rule.getId())
                .eq(AlertMessage::getLocation, location)
                .eq(AlertMessage::getActive, 1)
                .ne(AlertMessage::getFxDateStart, start)
                .set(AlertMessage::getActive, 0));
    }

    /**
     * 解除某规则在某位置的生效预警
     */
    private void deactivateForRule(AlertRule rule, String location) {
        alertMessageMapper.update(null, new LambdaUpdateWrapper<AlertMessage>()
                .eq(AlertMessage::getRuleId, rule.getId())
                .eq(AlertMessage::getLocation, location)
                .eq(AlertMessage::getActive, 1)
                .set(AlertMessage::getActive, 0));
    }

    /**
     * 解除某位置全部生效预警
     */
    private void deactivateAll(String location) {
        alertMessageMapper.update(null, new LambdaUpdateWrapper<AlertMessage>()
                .eq(AlertMessage::getLocation, location)
                .eq(AlertMessage::getActive, 1)
                .set(AlertMessage::getActive, 0));
    }

    /**
     * 生成预警文案
     */
    private String buildContent(AlertRule rule, LocalDate start, LocalDate end, List<DayForecast> window) {
        Set<String> weathers = new LinkedHashSet<>();
        BigDecimal minTemp = null;
        BigDecimal maxTemp = null;
        Integer minHumidity = null;
        Integer maxHumidity = null;
        for (DayForecast day : window) {
            if (day.weather != null) {
                weathers.add(day.weather);
            }
            if (day.temperature != null) {
                minTemp = minTemp == null ? day.temperature : minTemp.min(day.temperature);
                maxTemp = maxTemp == null ? day.temperature : maxTemp.max(day.temperature);
            }
            if (day.humidity != null) {
                minHumidity = minHumidity == null ? day.humidity : Math.min(minHumidity, day.humidity);
                maxHumidity = maxHumidity == null ? day.humidity : Math.max(maxHumidity, day.humidity);
            }
        }
        StringBuilder sb = new StringBuilder("【").append(rule.getRuleName()).append("】");
        sb.append(start.format(DATE_FMT)).append("至").append(end.format(DATE_FMT)).append("连续4天");
        if (!weathers.isEmpty()) {
            sb.append("，天气：").append(String.join("/", weathers));
        }
        if (minTemp != null || maxTemp != null) {
            sb.append("，温度：")
                    .append(minTemp != null ? minTemp.stripTrailingZeros().toPlainString() : "?")
                    .append("~")
                    .append(maxTemp != null ? maxTemp.stripTrailingZeros().toPlainString() : "?")
                    .append("℃");
        }
        if (minHumidity != null) {
            sb.append("，湿度：").append(minHumidity).append("%");
            if (maxHumidity != null && !maxHumidity.equals(minHumidity)) {
                sb.append("~").append(maxHumidity).append("%");
            }
        }
        sb.append("。请关注天气变化，注意情绪与健康防护。");
        return sb.toString();
    }

    private List<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 单日预报数据（评估用）
     */
    private static class DayForecast {
        private final LocalDate fxDate;
        private final String weather;
        private final BigDecimal temperature;
        private final Integer humidity;

        DayForecast(LocalDate fxDate, String weather, BigDecimal temperature, Integer humidity) {
            this.fxDate = fxDate;
            this.weather = weather;
            this.temperature = temperature;
            this.humidity = humidity;
        }
    }
}
