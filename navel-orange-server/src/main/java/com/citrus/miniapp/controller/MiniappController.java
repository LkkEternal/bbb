package com.citrus.miniapp.controller;

import com.citrus.alert.mapper.AlertRuleMapper;
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
        List<Long> userOrchardIds = getUserOrchardIds(userId, isAdmin);

        // 确定目标果园ID
        Long targetOrchardId = orchardId;
        if (targetOrchardId == null && !userOrchardIds.isEmpty()) {
            targetOrchardId = userOrchardIds.get(0);
        }

        Map<String, Object> data = new LinkedHashMap<>();

        // ── 统计信息（前端期望的字段名） ──
        Map<String, Object> statistics = new LinkedHashMap<>();
        if (isAdmin) {
            // 管理员：全局统计
            statistics.put("orchardCount", orchardMapper.selectCount(null));
            statistics.put("todayDetections", detectionMapper.selectCount(
                    new LambdaQueryWrapper<DiseaseDetection>()
                            .ge(DiseaseDetection::getCreatedAt, LocalDate.now().atStartOfDay())
            ));
            statistics.put("deviceOnline", deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>().eq(Device::getStatus, Constants.DEVICE_ONLINE)));
            statistics.put("deviceTotal", deviceMapper.selectCount(null));
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
        }

        // ── 近期预警 ──
        LambdaQueryWrapper<AlertMessage> alertWrapper = new LambdaQueryWrapper<AlertMessage>()
                .orderByDesc(AlertMessage::getCreatedAt)
                .last("LIMIT 5");
        if (targetOrchardId != null) {
            alertWrapper.eq(AlertMessage::getOrchardId, targetOrchardId);
        }
        var recentAlerts = alertMessageMapper.selectList(alertWrapper).stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("level", a.getLevel());
            m.put("createdAt", a.getCreatedAt());
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
    private List<Long> getUserOrchardIds(Long userId, boolean isAdmin) {
        if (isAdmin) {
            return Collections.emptyList();
        }
        List<Long> orchardIds = orchardUserMapper.selectList(
                new LambdaQueryWrapper<OrchardUser>().eq(OrchardUser::getUserId, userId)
        ).stream().map(OrchardUser::getOrchardId).collect(Collectors.toList());

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
            m.put("imageUrl", d.getImageUrl());
            m.put("thumbnailUrl", d.getImageUrl()); // 暂用原图替代
            m.put("diseaseCode", d.getTopClassCode());
            // 翻译病害名称
            DiseaseCatalog catalog = diseaseCatalogMapper.selectByCode(d.getTopClassCode());
            m.put("diseaseName", catalog != null ? catalog.getDiseaseName() : d.getTopClassCode());
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
                    .divide(BigDecimal.valueOf(dayData.size()), 1, BigDecimal.ROUND_HALF_UP);
            temperature.add(avgTemp);

            BigDecimal avgHumidity = dayData.stream()
                    .map(EnvironmentData::getHumidity)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(dayData.size()), 1, BigDecimal.ROUND_HALF_UP);
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
}
