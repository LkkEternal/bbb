package com.citrus.miniapp.controller;

import com.citrus.alert.mapper.AlertRuleMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.common.result.Result;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.mapper.AlertMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小程序/仪表盘聚合接口
 */
@Slf4j
@RestController
@RequestMapping("/api/miniapp")
@RequiredArgsConstructor
public class MiniappController {

    private final OrchardMapper orchardMapper;
    private final DiseaseDetectionMapper detectionMapper;
    private final EnvironmentDataMapper environmentDataMapper;
    private final AlertMessageMapper alertMessageMapper;
    private final AlertRuleMapper alertRuleMapper;

    /**
     * 首页看板
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(@RequestParam(required = false) Long orchardId) {
        Map<String, Object> data = new LinkedHashMap<>();

        // 统计信息
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("orchardCount", orchardMapper.selectCount(null));
        stats.put("todayDetections", detectionMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.citrus.collector.entity.DiseaseDetection>()
                        .ge(com.citrus.collector.entity.DiseaseDetection::getCreatedAt, LocalDate.now().atStartOfDay())
        ));
        data.put("statistics", stats);

        // 最新环境数据
        var envWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.citrus.collector.entity.EnvironmentData>()
                .orderByDesc(com.citrus.collector.entity.EnvironmentData::getRecordTime)
                .last("LIMIT 1");
        var envData = environmentDataMapper.selectOne(envWrapper);
        if (envData != null) {
            Map<String, Object> weather = new LinkedHashMap<>();
            weather.put("temperature", envData.getTemp());
            weather.put("humidity", envData.getHumidity());
            weather.put("updateTime", envData.getRecordTime());
            data.put("currentWeather", weather);
        }

        // 近期预警
        var alertWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.citrus.push.entity.AlertMessage>()
                .orderByDesc(com.citrus.push.entity.AlertMessage::getCreatedAt)
                .last("LIMIT 5");
        var recentAlerts = alertMessageMapper.selectList(alertWrapper).stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("level", a.getLevel());
            m.put("createdAt", a.getCreatedAt());
            m.put("read", a.getReadStatus() == 1);
            return m;
        }).collect(Collectors.toList());
        data.put("recentAlerts", recentAlerts);

        // 今日病害分布
        var todayStart = LocalDate.now().atStartOfDay();
        var detectionList = detectionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.citrus.collector.entity.DiseaseDetection>()
                        .ge(com.citrus.collector.entity.DiseaseDetection::getCreatedAt, todayStart)
        );
        Map<String, Long> dist = detectionList.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getTopClassCode() != null ? d.getTopClassCode() : "unknown",
                        Collectors.counting()
                ));
        List<Map<String, Object>> distribution = dist.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            return m;
        }).collect(Collectors.toList());
        data.put("diseaseDistribution", distribution);

        return Result.success(data);
    }
}
