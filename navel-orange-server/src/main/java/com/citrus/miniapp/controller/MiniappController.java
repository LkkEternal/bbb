package com.citrus.miniapp.controller;

import com.citrus.alert.mapper.AlertRuleMapper;
import com.citrus.alert.entity.AlertRule;
import com.citrus.calendar.mapper.FarmingCalendarMapper;
import com.citrus.calendar.entity.FarmingCalendar;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.common.constant.Constants;
import com.citrus.common.result.Result;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.orchard.mapper.DeviceMapper;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.entity.Device;
import com.citrus.auth.entity.User;
import com.citrus.auth.model.LoginUser;
import com.citrus.push.entity.AlertMessage;
import com.citrus.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final EnvironmentDataMapper environmentDataMapper;
    private final AlertMessageMapper alertMessageMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final FarmingCalendarMapper farmingCalendarMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final SecurityUtil securityUtil;

    /**
     * 首页看板
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(@RequestParam(required = false) Long orchardId) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 获取用户关联的果园ID列表
        List<Long> userOrchardIds = getUserOrchardIds(userId);

        // 确定目标果园ID
        Long targetOrchardId = orchardId;
        if (targetOrchardId == null && !userOrchardIds.isEmpty()) {
            targetOrchardId = userOrchardIds.get(0);
        }

        Map<String, Object> data = new LinkedHashMap<>();

        // ── 统计信息（前端期望的字段名） ──
        Map<String, Object> statistics = new LinkedHashMap<>();
        if (isAdmin) {
            // 管理员：统计全部果园/设备数据
            statistics.put("orchardCount", orchardMapper.selectCount(
                    new LambdaQueryWrapper<Orchard>()
                            .eq(Orchard::getDeleted, false)
                            .eq(Orchard::getStatus, 1)));
            statistics.put("todayDetections", detectionMapper.selectCount(
                    new LambdaQueryWrapper<DiseaseDetection>()
                            .ge(DiseaseDetection::getCreatedAt, LocalDate.now().atStartOfDay())
            ));
            statistics.put("deviceOnline", deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getStatus, Constants.DEVICE_ONLINE)));
            statistics.put("deviceTotal", deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()));
        } else if (targetOrchardId != null) {
            // 普通用户：指定果园统计
            statistics.put("orchardCount", userOrchardIds.size());
            statistics.put("todayDetections", detectionMapper.selectCount(
                    new LambdaQueryWrapper<DiseaseDetection>()
                            .eq(DiseaseDetection::getOrchardId, targetOrchardId)
                            .ge(DiseaseDetection::getCreatedAt, LocalDate.now().atStartOfDay())
            ));
            statistics.put("deviceOnline", deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getOrchardId, targetOrchardId)
                            .eq(Device::getStatus, Constants.DEVICE_ONLINE)));
            statistics.put("deviceTotal", deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>().eq(Device::getOrchardId, targetOrchardId)));
        } else {
            statistics.put("orchardCount", 0);
            statistics.put("todayDetections", 0);
            statistics.put("deviceOnline", 0);
            statistics.put("deviceTotal", 0);
        }
        data.put("statistics", statistics);

        // ── 天气数据（前端期望的字段名） ──
        LambdaQueryWrapper<EnvironmentData> envWrapper = new LambdaQueryWrapper<EnvironmentData>()
                .orderByDesc(EnvironmentData::getRecordTime)
                .last("LIMIT 1");
        if (targetOrchardId != null) {
            envWrapper.eq(EnvironmentData::getOrchardId, targetOrchardId);
        }
        var envData = environmentDataMapper.selectOne(envWrapper);
        if (envData != null) {
            Map<String, Object> currentWeather = new LinkedHashMap<>();
            currentWeather.put("temperature", envData.getTemp());
            currentWeather.put("humidity", envData.getHumidity());
            currentWeather.put("updateTime", envData.getRecordTime());
            data.put("currentWeather", currentWeather);

            // 解析 extra_data 中的全部传感器数据
            Map<String, Object> sensors = new LinkedHashMap<>();
            sensors.put("air_temp", envData.getTemp());
            sensors.put("humidity", envData.getHumidity());
            String extraDataStr = envData.getExtraData();
            if (extraDataStr != null && !extraDataStr.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> extra = om.readValue(extraDataStr, Map.class);
                    sensors.putAll(extra);
                } catch (Exception e) {
                    log.warn("解析 extra_data 失败: {}", e.getMessage());
                }
            }
            data.put("sensors", sensors);
        }

        // ── 近期预警 ──
        LambdaQueryWrapper<AlertMessage> alertWrapper = new LambdaQueryWrapper<AlertMessage>()
                .orderByDesc(AlertMessage::getCreatedAt)
                .last("LIMIT 50");
        if (targetOrchardId != null) {
            alertWrapper.eq(AlertMessage::getOrchardId, targetOrchardId);
        }
        var recentAlerts = alertMessageMapper.selectList(alertWrapper).stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("level", a.getLevel());
            m.put("messageType", a.getMessageType());
            m.put("relatedType", a.getRelatedType());
            m.put("ruleId", a.getRuleId());
            m.put("calendarId", a.getCalendarId());
            // 解析触发规则名称
            String triggerRuleName = null;
            if (a.getRuleId() != null) {
                AlertRule rule = alertRuleMapper.selectById(a.getRuleId());
                if (rule != null) triggerRuleName = rule.getName();
            } else if (a.getCalendarId() != null) {
                FarmingCalendar cal = farmingCalendarMapper.selectById(a.getCalendarId());
                if (cal != null) triggerRuleName = cal.getPhenologyName() + "农事提醒";
            }
            m.put("triggerRuleName", triggerRuleName);
            m.put("createdAt", a.getCreatedAt());
            m.put("advice", a.getAdvice());
            m.put("content", a.getContent());
            m.put("extraData", a.getExtraData());
            // 推送状态
            String pushEmailStatus = "pending";
            if (a.getPushStatus() != null) {
                switch (a.getPushStatus()) {
                    case 1: pushEmailStatus = "sent"; break;
                    case 2: pushEmailStatus = "failed"; break;
                }
            }
            m.put("pushEmailStatus", pushEmailStatus);
            // 处理状态
            m.put("processed", a.getProcessed() != null && a.getProcessed());
            // 果园名称
            if (a.getOrchardId() != null) {
                Orchard orchard = orchardMapper.selectById(a.getOrchardId());
                m.put("orchardName", orchard != null ? orchard.getName() : null);
            } else {
                m.put("orchardName", null);
            }
            return m;
        }).collect(Collectors.toList());
        data.put("recentAlerts", recentAlerts);

        // ── 病害分布（前端期望的字段名） ──
        LambdaQueryWrapper<DiseaseDetection> detWrapper = new LambdaQueryWrapper<DiseaseDetection>()
                .ge(DiseaseDetection::getCreatedAt, LocalDate.now().atStartOfDay());
        if (targetOrchardId != null) {
            detWrapper.eq(DiseaseDetection::getOrchardId, targetOrchardId);
        }
        var detectionList = detectionMapper.selectList(detWrapper);
        Map<String, Long> dist = detectionList.stream()
                .filter(d -> d.getTopClassCode() != null)
                .collect(Collectors.groupingBy(DiseaseDetection::getTopClassCode, Collectors.counting()));

        List<Map<String, Object>> diseaseDistribution = dist.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    DiseaseCatalog catalog = diseaseCatalogMapper.selectByCode(e.getKey());
                    m.put("name", catalog != null ? catalog.getDiseaseName() : e.getKey());
                    m.put("value", e.getValue());
                    return m;
                }).collect(Collectors.toList());
        data.put("diseaseDistribution", diseaseDistribution);

        return Result.success(data);
    }

    /**
     * 获取用户关联的果园ID列表
     */
    private List<Long> getUserOrchardIds(Long userId) {
        List<Long> orchardIds = orchardUserMapper.selectList(
                new LambdaQueryWrapper<OrchardUser>().eq(OrchardUser::getUserId, userId)
        ).stream().map(OrchardUser::getOrchardId).collect(Collectors.toList());

        // 过滤掉已软删除的果园关联
        if (!orchardIds.isEmpty()) {
            orchardIds = orchardMapper.selectList(
                    new LambdaQueryWrapper<Orchard>()
                            .in(Orchard::getId, orchardIds)
                            .select(Orchard::getId)
            ).stream().map(Orchard::getId).collect(Collectors.toList());
        }

        var ownedOrchards = orchardMapper.selectList(
                new LambdaQueryWrapper<Orchard>().eq(Orchard::getOwnerId, userId)
        ).stream().map(Orchard::getId).collect(Collectors.toList());

        Set<Long> allIds = new LinkedHashSet<>(orchardIds);
        allIds.addAll(ownedOrchards);
        return new ArrayList<>(allIds);
    }

    /**
     * 果园详情（小程序端）
     */
    @GetMapping("/orchards/{id}")
    public Result<Map<String, Object>> orchardDetail(@PathVariable Long id) {
        Orchard orchard = orchardMapper.selectById(id);
        if (orchard == null) {
            return Result.success(null);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", orchard.getId());
        data.put("name", orchard.getName());
        data.put("formattedAddress", orchard.getFormattedAddress());
        data.put("areaMu", orchard.getAreaMu());
        data.put("treeAge", orchard.getTreeAge());
        data.put("variety", orchard.getVariety());

        // 设备统计
        var devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().eq(Device::getOrchardId, id)
        );
        data.put("deviceCount", devices.size());
        data.put("onlineDeviceCount", devices.stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == Constants.DEVICE_ONLINE)
                .count());

        // 当前用户角色
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        OrchardUser orchardUser = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, id)
                        .eq(OrchardUser::getUserId, userId)
        );
        String memberRole = orchardUser != null ? orchardUser.getRoleType() : null;
        // 如果是果园owner
        if (memberRole == null && orchard.getOwnerId() != null && orchard.getOwnerId().equals(userId)) {
            memberRole = "owner";
        }
        data.put("memberRole", memberRole);

        return Result.success(data);
    }

    /**
     * 果园识别记录（小程序端）
     */
    @GetMapping("/orchards/{id}/detections")
    public Result<Map<String, Object>> orchardDetections(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String diseaseCode) {

        var wrapper = new LambdaQueryWrapper<DiseaseDetection>()
                .eq(DiseaseDetection::getOrchardId, id);
        if (diseaseCode != null && !diseaseCode.isEmpty()) {
            wrapper.eq(DiseaseDetection::getTopClassCode, diseaseCode);
        }
        wrapper.orderByDesc(DiseaseDetection::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        var list = detectionMapper.selectList(wrapper);

        var countWrapper = new LambdaQueryWrapper<DiseaseDetection>()
                .eq(DiseaseDetection::getOrchardId, id);
        if (diseaseCode != null && !diseaseCode.isEmpty()) {
            countWrapper.eq(DiseaseDetection::getTopClassCode, diseaseCode);
        }
        long total = detectionMapper.selectCount(countWrapper);

        // 批量查询设备名称
        Map<Long, String> deviceNameMap = new HashMap<>();
        list.stream().map(DiseaseDetection::getDeviceId).distinct().forEach(deviceId -> {
            if (deviceId != null) {
                Device device = deviceMapper.selectById(deviceId);
                if (device != null) {
                    deviceNameMap.put(deviceId, device.getName());
                }
            }
        });

        List<Map<String, Object>> records = list.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("imageUrl", rewriteImageUrl(d.getImageUrl()));
            m.put("thumbnailUrl", rewriteImageUrl(d.getImageUrl())); // 暂用原图替代
            m.put("diseaseCode", d.getTopClassCode());
            // 翻译病害名称
            DiseaseCatalog catalog = diseaseCatalogMapper.selectByCode(d.getTopClassCode());
            m.put("diseaseName", catalog != null ? catalog.getDiseaseName() : d.getTopClassCode());
            m.put("severityLevel", catalog != null ? catalog.getSeverityLevel() : null);
            m.put("probability", d.getTopProbability());
            m.put("deviceName", d.getDeviceId() != null ? deviceNameMap.get(d.getDeviceId()) : null);
            m.put("detectedAt", d.getDeviceTimestamp());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        data.put("pages", (total + size - 1) / size);
        return Result.success(data);
    }

    /**
     * 果园环境趋势（小程序端）
     */
    @GetMapping("/orchards/{id}/environment/trend")
    public Result<Map<String, Object>> orchardEnvTrend(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") int days) {

        // 限制天数范围
        if (days != 7 && days != 15 && days != 30) {
            days = 7;
        }

        var wrapper = new LambdaQueryWrapper<EnvironmentData>()
                .eq(EnvironmentData::getOrchardId, id)
                .ge(EnvironmentData::getRecordTime, LocalDate.now().minusDays(days).atStartOfDay())
                .orderByAsc(EnvironmentData::getRecordTime);
        var list = environmentDataMapper.selectList(wrapper);

        // 按天聚合
        Map<String, List<EnvironmentData>> groupedByDay = list.stream()
                .filter(e -> e.getRecordTime() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getRecordTime().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<String> dates = new ArrayList<>();
        List<BigDecimal> temperature = new ArrayList<>();
        List<BigDecimal> humidity = new ArrayList<>();

        groupedByDay.forEach((dateStr, dayData) -> {
            // 格式化为 MM-dd
            String formattedDate = dateStr.substring(5); // "2026-06-01" -> "06-01"
            dates.add(formattedDate);

            // 计算当天平均值
            BigDecimal avgTemp = dayData.stream()
                    .map(EnvironmentData::getTemp)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(dayData.size()), 1, java.math.RoundingMode.HALF_UP);
            temperature.add(avgTemp);

            BigDecimal avgHumidity = dayData.stream()
                    .map(EnvironmentData::getHumidity)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(dayData.size()), 1, java.math.RoundingMode.HALF_UP);
            humidity.add(avgHumidity);
        });

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dates", dates);
        data.put("temperature", temperature);
        data.put("humidity", humidity);
        return Result.success(data);
    }

    /**
     * 预警消息列表（小程序端）
     */
    @GetMapping("/alerts")
    public Result<Map<String, Object>> alertsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer level) {

        var wrapper = new LambdaQueryWrapper<AlertMessage>();
        if (level != null) {
            wrapper.eq(AlertMessage::getLevel, level);
        }
        wrapper.orderByDesc(AlertMessage::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        var list = alertMessageMapper.selectList(wrapper);

        var countWrapper = new LambdaQueryWrapper<AlertMessage>();
        if (level != null) {
            countWrapper.eq(AlertMessage::getLevel, level);
        }
        long total = alertMessageMapper.selectCount(countWrapper);

        // 批量查询果园名称
        Map<Long, String> orchardNameMap = new HashMap<>();
        list.stream().map(AlertMessage::getOrchardId).distinct().forEach(orchardId -> {
            if (orchardId != null) {
                Orchard orchard = orchardMapper.selectById(orchardId);
                if (orchard != null) {
                    orchardNameMap.put(orchardId, orchard.getName());
                }
            }
        });

        List<Map<String, Object>> records = list.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("summary", a.getSummary());
            // level 数字转字符串
            m.put("level", convertLevelToString(a.getLevel()));
            m.put("orchardName", a.getOrchardId() != null ? orchardNameMap.get(a.getOrchardId()) : null);
            m.put("isRead", a.getReadStatus() != null && a.getReadStatus() == 1);
            m.put("createdAt", a.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        data.put("pages", (total + size - 1) / size);
        return Result.success(data);
    }

    /**
     * 预警等级数字转字符串
     */
    private String convertLevelToString(Integer level) {
        if (level == null) return "info";
        return switch (level) {
            case 1 -> "info";
            case 2 -> "warning";
            case 3 -> "danger";
            default -> "info";
        };
    }

    /**
     * 果园最新病害预警详情
     * 返回识别出的病害信息：病害名称、置信度、检测时间、防治建议、扩散风险、紧急程度
     */
    @GetMapping("/orchards/{id}/latest-alert")
    public Result<Map<String, Object>> latestAlert(@PathVariable Long id) {
        // 1. 查询最新未处理的病害预警消息
        AlertMessage alert = alertMessageMapper.selectOne(
                new LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getOrchardId, id)
                        .eq(AlertMessage::getMessageType, "alert")
                        .eq(AlertMessage::getRelatedType, "detection")
                        .orderByDesc(AlertMessage::getCreatedAt)
                        .last("LIMIT 1")
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orchardId", id);

        // 获取果园名称
        Orchard orchard = orchardMapper.selectById(id);
        data.put("orchardName", orchard != null ? orchard.getName() : "");

        if (alert == null) {
            data.put("hasAlert", false);
            return Result.success(data);
        }

        data.put("hasAlert", true);
        data.put("alertId", alert.getId());
        data.put("urgencyLevel", alert.getLevel());
        data.put("advice", alert.getAdvice() != null ? alert.getAdvice() : "建议定期巡园，观察果树生长状况，注意果园通风，保持适宜的温湿度。");

        // 2. 解析 extraData JSON
        String extraDataStr = alert.getExtraData();
        String diseaseName = null;
        BigDecimal probability = null;
        String imageUrl = null;
        if (extraDataStr != null && !extraDataStr.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> extra = mapper.readValue(extraDataStr, Map.class);
                diseaseName = (String) extra.get("diseaseName");
                if (extra.get("probability") != null) {
                    probability = new BigDecimal(extra.get("probability").toString());
                }
                imageUrl = (String) extra.get("imageUrl");
                if (imageUrl != null) imageUrl = rewriteImageUrl(imageUrl);
            } catch (Exception e) {
                log.warn("解析预警消息extraData失败: {}", e.getMessage());
            }
        }

        // 3. 从关联检测记录补充
        if ((diseaseName == null || probability == null) && alert.getRelatedId() != null) {
            DiseaseDetection detection = detectionMapper.selectById(alert.getRelatedId());
            if (detection != null) {
                data.put("detectedAt", detection.getDeviceTimestamp());
                if (detection.getTopProbability() != null) probability = detection.getTopProbability();
                if (imageUrl == null) imageUrl = rewriteImageUrl(detection.getImageUrl());
                DiseaseCatalog catalog = diseaseCatalogMapper.selectByCode(detection.getTopClassCode());
                if (catalog != null) {
                    diseaseName = catalog.getDiseaseName();
                    if (alert.getAdvice() == null && catalog.getPreventionGuide() != null) {
                        data.put("advice", catalog.getPreventionGuide());
                    }
                }
                if (diseaseName == null) diseaseName = detection.getDiseaseNameRaw();
                data.put("diseaseCode", detection.getTopClassCode());
            }
        }

        data.put("diseaseName", diseaseName != null ? diseaseName : "未知病害");
        data.put("confidence", probability);
        data.put("imageUrl", imageUrl != null ? imageUrl : "");

        // 4. 扩散风险
        int level = alert.getLevel() != null ? alert.getLevel() : 1;
        String spreadRisk;
        switch (level) {
            case 3: spreadRisk = "该病害扩散风险极高，已检测到周边果园存在相同病害，若不及时处理可能导致大面积感染，严重影响产量和果实品质。"; break;
            case 2: spreadRisk = "该病害存在中等扩散风险，当前环境条件适宜病害发展，建议尽快采取防治措施，防止蔓延至更多果树。"; break;
            default: spreadRisk = "目前病害范围较小，扩散风险较低，但仍需保持观察，防止条件变化后加速扩散。";
        }
        data.put("spreadRisk", spreadRisk);

        if (!data.containsKey("detectedAt") || data.get("detectedAt") == null) {
            data.put("detectedAt", alert.getCreatedAt());
        }

        return Result.success(data);
    }

    /** 将 OBS 直连 URL 重写为后端代理 URL */
    private String rewriteImageUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        String obsPrefix = "https://orange-86e.obs.cn-south-1.myhuaweicloud.com";
        String proxyPrefix = "/api/images/proxy";
        if (url.startsWith(obsPrefix)) {
            return proxyPrefix + url.substring(obsPrefix.length());
        }
        return url;
    }
}
