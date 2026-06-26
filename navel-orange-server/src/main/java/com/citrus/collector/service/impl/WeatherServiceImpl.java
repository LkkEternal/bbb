package com.citrus.collector.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.entity.WeatherForecast;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.collector.mapper.WeatherForecastMapper;
import com.citrus.collector.service.WeatherService;
import com.citrus.collector.vo.WeatherDailyVO;
import com.citrus.collector.vo.WeatherHourlyVO;
import com.citrus.collector.vo.WeatherNowVO;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.citrus.common.weather.QWeatherClient;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final QWeatherClient qWeatherClient;
    private final EnvironmentDataMapper environmentDataMapper;
    private final WeatherForecastMapper weatherForecastMapper;
    private final OrchardMapper orchardMapper;

    @Override
    public WeatherNowVO getWeatherNow(String location) {
        QWeatherClient.NowResponse response = qWeatherClient.getWeatherNow(location);
        
        // 转换为VO
        QWeatherClient.NowResponse.Now now = response.getNow();
        return WeatherNowVO.builder()
                .obsTime(now.getObsTime())
                .temp(now.getTemp())
                .feelsLike(now.getFeelsLike())
                .text(now.getText())
                .windDir(now.getWindDir())
                .windScale(now.getWindScale())
                .windSpeed(now.getWindSpeed())
                .humidity(now.getHumidity())
                .precip(now.getPrecip())
                .vis(now.getVis())
                .build();
    }

    @Override
    public WeatherHourlyVO getWeatherHourly(String location) {
        QWeatherClient.HourlyResponse response = qWeatherClient.getWeatherHourly(location);
        
        // 转换为VO
        List<WeatherHourlyVO.HourlyItem> hourlyItems = response.getHourly().stream()
                .map(item -> WeatherHourlyVO.HourlyItem.builder()
                        .fxTime(item.getFxTime())
                        .temp(item.getTemp())
                        .text(item.getText())
                        .windDir(item.getWindDir())
                        .windScale(item.getWindScale())
                        .windSpeed(item.getWindSpeed())
                        .humidity(item.getHumidity())
                        .pop(item.getPop())
                        .precip(item.getPrecip())
                        .build())
                .collect(Collectors.toList());
        
        return WeatherHourlyVO.builder()
                .hourly(hourlyItems)
                .build();
    }

    @Override
    public WeatherDailyVO getWeather7d(String location) {
        QWeatherClient.DailyResponse response = qWeatherClient.getWeather7d(location);
        
        // 转换为VO
        List<WeatherDailyVO.DailyItem> dailyItems = response.getDaily().stream()
                .map(item -> WeatherDailyVO.DailyItem.builder()
                        .fxDate(item.getFxDate())
                        .sunrise(item.getSunrise())
                        .sunset(item.getSunset())
                        .tempMax(item.getTempMax())
                        .tempMin(item.getTempMin())
                        .textDay(item.getTextDay())
                        .textNight(item.getTextNight())
                        .windDirDay(item.getWindDirDay())
                        .windScaleDay(item.getWindScaleDay())
                        .windSpeedDay(item.getWindSpeedDay())
                        .windDirNight(item.getWindDirNight())
                        .windScaleNight(item.getWindScaleNight())
                        .windSpeedNight(item.getWindSpeedNight())
                        .humidity(item.getHumidity())
                        .precip(item.getPrecip())
                        .uvIndex(item.getUvIndex())
                        .build())
                .collect(Collectors.toList());
        
        return WeatherDailyVO.builder()
                .daily(dailyItems)
                .build();
    }

    @Override
    @Transactional
    public void fetchAndSaveWeatherNow(Long orchardId, String location) {
        log.info("获取果园[{}]实时天气数据，位置: {}", orchardId, location);
        
        // 验证location格式
        if (!isValidLocation(location)) {
            log.warn("果园[{}]位置信息无效: {}", orchardId, location);
            throw new BizException(ResultCode.BAD_REQUEST, "位置信息格式无效，应为'经度,纬度'格式");
        }
        
        try {
            // 获取实时天气
            QWeatherClient.NowResponse response = qWeatherClient.getWeatherNow(location);
            
            if (response == null || response.getNow() == null) {
                log.warn("果园[{}]天气数据获取失败", orchardId);
                return;
            }
            
            QWeatherClient.NowResponse.Now now = response.getNow();
            
            // 解析观测时间
            LocalDateTime obsTime = parseObsTime(now.getObsTime());
            
            // 创建环境数据实体
            EnvironmentData envData = new EnvironmentData();
            envData.setOrchardId(orchardId);
            envData.setDataSource("weather_api");
            envData.setTemp(parseBigDecimal(now.getTemp()));
            envData.setHumidity(parseBigDecimal(now.getHumidity()));
            envData.setPrecip(parseBigDecimal(now.getPrecip()));
            envData.setWindSpeed(parseBigDecimal(now.getWindSpeed()));
            envData.setRecordTime(obsTime);
            
            // 保存到数据库
            environmentDataMapper.insert(envData);
            log.info("果园[{}]天气数据保存成功，温度: {}℃, 湿度: {}%", orchardId, now.getTemp(), now.getHumidity());
            
        } catch (Exception e) {
            log.error("果园[{}]天气数据获取或保存失败: {}", orchardId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void fetchAllOrchardsWeather() {
        log.info("========== 开始获取所有果园实时天气数据 ==========");
        
        // 查询所有正常状态的果园
        List<Orchard> orchards = orchardMapper.selectList(
                new LambdaQueryWrapper<Orchard>()
                        .eq(Orchard::getStatus, 1)
                        .eq(Orchard::getDeleted, false)
        );
        
        log.info("查询到 {} 个果园 (status=1, deleted=false)", orchards.size());
        for (Orchard o : orchards) {
            log.info("  果园[{}] {} location原始值: {}", o.getId(), o.getName(), o.getLocation());
        }
        
        if (orchards.isEmpty()) {
            log.warn("⚠ 没有符合条件的果园，天气任务跳过");
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Orchard orchard : orchards) {
            try {
                String rawLocation = orchard.getLocation();
                String location = normalizeLocation(rawLocation);
                if (location == null) {
                    log.warn("⚠ 果园[{}] {} 位置解析失败，原始值: [{}]", orchard.getId(), orchard.getName(), rawLocation);
                    failCount++;
                    continue;
                }
                
                log.info("果园[{}] {} 位置解析成功: {} -> {}", orchard.getId(), orchard.getName(), rawLocation, location);
                fetchAndSaveWeatherNow(orchard.getId(), location);
                successCount++;
            } catch (Exception e) {
                log.error("果园[{}]天气数据处理失败: {}", orchard.getId(), e.getMessage(), e);
                failCount++;
            }
        }
        
        log.info("========== 所有果园天气数据获取完成: 成功={}, 失败={}, 总数={} ==========", successCount, failCount, orchards.size());
    }

    @Override
    @Transactional
    public void fetchAndSaveWeather7d(Long orchardId, String location) {
        log.info("获取果园[{}] 7天天气预报数据，位置: {}", orchardId, location);
        
        // 验证location格式
        if (!isValidLocation(location)) {
            log.warn("果园[{}]位置信息无效: {}", orchardId, location);
            throw new BizException(ResultCode.BAD_REQUEST, "位置信息格式无效，应为'经度,纬度'格式");
        }
        
        try {
            // 获取7天天气预报
            QWeatherClient.DailyResponse response = qWeatherClient.getWeather7d(location);
            
            if (response == null || response.getDaily() == null || response.getDaily().isEmpty()) {
                log.warn("果园[{}] 7天天气预报数据获取失败", orchardId);
                return;
            }
            
            LocalDateTime fetchTime = LocalDateTime.now();
            int savedCount = 0;
            
            for (QWeatherClient.DailyResponse.Daily daily : response.getDaily()) {
                try {
                    // 解析预报日期
                    LocalDateTime fxTime = parseFxDate(daily.getFxDate());
                    
                    // 创建天气预报实体
                    WeatherForecast forecast = new WeatherForecast();
                    forecast.setOrchardId(orchardId);
                    forecast.setFxTime(fxTime);
                    forecast.setTempMax(parseBigDecimal(daily.getTempMax()));
                    forecast.setTempMin(parseBigDecimal(daily.getTempMin()));
                    forecast.setHumidity(parseBigDecimal(daily.getHumidity()));
                    forecast.setPop(BigDecimal.ZERO); // 7天预报没有pop字段，设为0
                    forecast.setPrecip(parseBigDecimal(daily.getPrecip()));
                    forecast.setText(daily.getTextDay());
                    forecast.setWindSpeed(parseBigDecimal(daily.getWindSpeedDay()));
                    forecast.setApiSource("qweather");
                    forecast.setFetchTime(fetchTime);
                    
                    // 使用upsert逻辑：根据orchard_id、fxTime、api_source判断是否存在
                    LambdaQueryWrapper<WeatherForecast> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(WeatherForecast::getOrchardId, orchardId)
                           .eq(WeatherForecast::getFxTime, fxTime)
                           .eq(WeatherForecast::getApiSource, "qweather");
                    
                    WeatherForecast existing = weatherForecastMapper.selectOne(wrapper);
                    if (existing != null) {
                        // 更新现有记录
                        forecast.setId(existing.getId());
                        weatherForecastMapper.updateById(forecast);
                    } else {
                        // 插入新记录
                        weatherForecastMapper.insert(forecast);
                    }
                    savedCount++;
                } catch (Exception e) {
                    log.error("果园[{}] 7天预报数据保存失败，日期: {}", orchardId, daily.getFxDate(), e);
                }
            }
            
            log.info("果园[{}] 7天天气预报数据保存成功，共保存{}条记录", orchardId, savedCount);
            
        } catch (Exception e) {
            log.error("果园[{}] 7天天气预报数据获取或保存失败: {}", orchardId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void fetchAllOrchardsWeather7d() {
        log.info("========== 开始获取所有果园7天天气预报数据 ==========");
        
        // 查询所有正常状态的果园
        List<Orchard> orchards = orchardMapper.selectList(
                new LambdaQueryWrapper<Orchard>()
                        .eq(Orchard::getStatus, 1)
                        .eq(Orchard::getDeleted, false)
        );
        
        log.info("查询到 {} 个果园 (status=1, deleted=false)", orchards.size());
        for (Orchard o : orchards) {
            log.info("  果园[{}] {} location原始值: {}", o.getId(), o.getName(), o.getLocation());
        }
        
        if (orchards.isEmpty()) {
            log.warn("⚠ 没有符合条件的果园，7天天气预报任务跳过");
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Orchard orchard : orchards) {
            try {
                String rawLocation = orchard.getLocation();
                String location = normalizeLocation(rawLocation);
                if (location == null) {
                    log.warn("⚠ 果园[{}] {} 位置解析失败，原始值: [{}]", orchard.getId(), orchard.getName(), rawLocation);
                    failCount++;
                    continue;
                }
                
                log.info("果园[{}] {} 位置解析成功: {} -> {}", orchard.getId(), orchard.getName(), rawLocation, location);
                fetchAndSaveWeather7d(orchard.getId(), location);
                successCount++;
            } catch (Exception e) {
                log.error("果园[{}] 7天天气预报数据处理失败: {}", orchard.getId(), e.getMessage(), e);
                failCount++;
            }
        }
        
        log.info("========== 所有果园7天天气预报数据获取完成: 成功={}, 失败={}, 总数={} ==========", successCount, failCount, orchards.size());
    }

    /**
     * 解析观测时间
     * 格式：2026-06-10T10:08+08:00
     */
    private LocalDateTime parseObsTime(String obsTime) {
        try {
            if (obsTime != null && !obsTime.isEmpty()) {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(obsTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                return zonedDateTime.toLocalDateTime();
            }
        } catch (Exception e) {
            log.warn("解析观测时间失败: {}", obsTime, e);
        }
        return LocalDateTime.now();
    }

    /**
     * 解析BigDecimal
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value != null && !value.isEmpty()) {
                return new BigDecimal(value);
            }
        } catch (NumberFormatException e) {
            log.warn("解析数值失败: {}", value, e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 验证location格式是否有效
     * 有效格式：经度,纬度（如：114.93,25.38）
     * @param location 经纬度字符串
     * @return 是否有效
     */
    private boolean isValidLocation(String location) {
        if (location == null || location.isEmpty()) {
            return false;
        }
        // 检查格式：经度,纬度
        String[] parts = location.split(",");
        if (parts.length != 2) {
            return false;
        }
        try {
            double longitude = Double.parseDouble(parts[0].trim());
            double latitude = Double.parseDouble(parts[1].trim());
            // 验证经纬度范围
            if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 标准化位置字符串
     * 支持两种格式：
     * 1. "经度,纬度"（如：114.93,25.38）
     * 2. PostGIS WKB 十六进制字符串（如：0101000020E6100000...）
     * @param location 原始位置字符串
     * @return "经度,纬度" 格式，无效则返回 null
     */
    private String normalizeLocation(String location) {
        if (location == null || location.isEmpty()) {
            return null;
        }
        // 如果已经是 "经度,纬度" 格式
        if (location.contains(",")) {
            return isValidLocation(location) ? location : null;
        }
        // 尝试作为 WKB 十六进制解析
        return parseWkbHexToLocation(location);
    }

    /**
     * 从 PostGIS WKB 十六进制字符串解析经纬度
     */
    private String parseWkbHexToLocation(String hex) {
        if (hex == null || hex.length() < 42) {
            return null;
        }
        try {
            byte[] bytes = new byte[hex.length() / 2];
            for (int i = 0; i < hex.length(); i += 2) {
                bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            byte byteOrder = buffer.get();
            if (byteOrder == 0) {
                buffer.order(ByteOrder.BIG_ENDIAN);
            }

            int geometryType = buffer.getInt();
            boolean hasSrid = (geometryType & 0x20000000) != 0;
            int pointType = geometryType & 0xFF;
            if (pointType != 1) {
                return null;
            }
            if (hasSrid) {
                buffer.getInt();
            }

            double longitude = buffer.getDouble();
            double latitude = buffer.getDouble();

            if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
                return null;
            }
            return String.format("%.6f,%.6f", longitude, latitude);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析预报日期
     * 格式：2026-06-11
     */
    private LocalDateTime parseFxDate(String fxDate) {
        try {
            if (fxDate != null && !fxDate.isEmpty()) {
                java.time.LocalDate date = java.time.LocalDate.parse(fxDate);
                return date.atStartOfDay();
            }
        } catch (Exception e) {
            log.warn("解析预报日期失败: {}", fxDate, e);
        }
        return LocalDateTime.now();
    }
}
