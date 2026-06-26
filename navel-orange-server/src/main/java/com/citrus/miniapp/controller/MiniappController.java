package com.citrus.miniapp.controller;

import com.citrus.alert.mapper.AlertRuleMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.common.constant.Constants;
import com.citrus.common.result.Result;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.orchard.mapper.DeviceMapper;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.collector.entity.DiseaseDetection;
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
    private final EnvironmentDataMapper environmentDataMapper;
    private final AlertMessageMapper alertMessageMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final SecurityUtil securityUtil;

    /**
     * 首页看板 — 按角色返回不同数据
     * 管理员：全局统计
     * 园主/管理员：自己果园统计
     * 无果园用户：空数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(@RequestParam(required = false) Long orchardId) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 获取用户关联的果园ID列表
        List<Long> userOrchardIds = null;
        if (!isAdmin) {
            userOrchardIds = orchardUserMapper.selectList(
                    new LambdaQueryWrapper<OrchardUser>().eq(OrchardUser::getUserId, userId)
            ).stream().map(OrchardUser::getOrchardId).collect(Collectors.toList());

            // 也包含用户作为 owner 的果园
            var ownedOrchards = orchardMapper.selectList(
                    new LambdaQueryWrapper<Orchard>().eq(Orchard::getOwnerId, userId)
            ).stream().map(Orchard::getId).collect(Collectors.toList());
            Set<Long> allIds = new LinkedHashSet<>(userOrchardIds);
            allIds.addAll(ownedOrchards);
            userOrchardIds = new ArrayList<>(allIds);
        }

        Map<String, Object> data = new LinkedHashMap<>();

        // ── 统计信息 ──
        Map<String, Object> stats = new LinkedHashMap<>();
        if (isAdmin) {
            stats.put("orchardCount", orchardMapper.selectCount(null));
            stats.put("todayDetections", detectionMapper.selectCount(
                    new LambdaQueryWrapper<DiseaseDetection>()
                            .ge(DiseaseDetection::getCreatedAt, LocalDate.now().atStartOfDay())
            ));
            var deviceWrapper = new LambdaQueryWrapper<Device>();
            stats.put("deviceOnline", deviceMapper.selectCount(
                    deviceWrapper.eq(Device::getStatus, Constants.DEVICE_ONLINE)));
            stats.put("deviceTotal", deviceMapper.selectCount(null));
        } else if (userOrchardIds != null && !userOrchardIds.isEmpty()) {
            stats.put("orchardCount", userOrchardIds.size());
            stats.put("todayDetections", detectionMapper.selectCount(
                    new LambdaQueryWrapper<DiseaseDetection>()
                            .in(DiseaseDetection::getOrchardId, userOrchardIds)
                            .ge(DiseaseDetection::getCreatedAt, LocalDate.now().atStartOfDay())
            ));
            stats.put("deviceOnline", deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .in(Device::getOrchardId, userOrchardIds)
                            .eq(Device::getStatus, Constants.DEVICE_ONLINE)));
            stats.put("deviceTotal", deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .in(Device::getOrchardId, userOrchardIds)));
        } else {
            stats.put("orchardCount", 0);
            stats.put("todayDetections", 0);
            stats.put("deviceOnline", 0);
            stats.put("deviceTotal", 0);
        }
        data.put("statistics", stats);

        // ── 最新环境数据 ──
        LambdaQueryWrapper<EnvironmentData> envWrapper = new LambdaQueryWrapper<EnvironmentData>()
                .orderByDesc(EnvironmentData::getRecordTime)
                .last("LIMIT 1");
        if (!isAdmin && userOrchardIds != null && !userOrchardIds.isEmpty()) {
            envWrapper.in(EnvironmentData::getOrchardId, userOrchardIds);
        } else if (!isAdmin) {
            // 无果园，不返回天气
            data.put("currentWeather", null);
            data.put("recentAlerts", Collections.emptyList());
            data.put("diseaseDistribution", Collections.emptyList());
            return Result.success(data);
        }
        var envData = environmentDataMapper.selectOne(envWrapper);
        if (envData != null) {
            Map<String, Object> weather = new LinkedHashMap<>();
            weather.put("temperature", envData.getTemp());
            weather.put("humidity", envData.getHumidity());
            weather.put("updateTime", envData.getRecordTime());
            data.put("currentWeather", weather);
        }

        // ── 近期预警 ──
        LambdaQueryWrapper<AlertMessage> alertWrapper = new LambdaQueryWrapper<AlertMessage>()
                .orderByDesc(AlertMessage::getCreatedAt)
                .last("LIMIT 5");
        if (!isAdmin && userOrchardIds != null && !userOrchardIds.isEmpty()) {
            alertWrapper.in(AlertMessage::getOrchardId, userOrchardIds);
        } else if (!isAdmin) {
            alertWrapper.eq(AlertMessage::getUserId, userId);
        }
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

        // ── 今日病害分布 ──
        var todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<DiseaseDetection> detWrapper = new LambdaQueryWrapper<DiseaseDetection>()
                .ge(DiseaseDetection::getCreatedAt, todayStart);
        if (!isAdmin && userOrchardIds != null && !userOrchardIds.isEmpty()) {
            detWrapper.in(DiseaseDetection::getOrchardId, userOrchardIds);
        }
        var detectionList = detectionMapper.selectList(detWrapper);
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
        data.put("location", orchard.getLocation());
        data.put("areaMu", orchard.getAreaMu());
        data.put("treeAge", orchard.getTreeAge());
        data.put("variety", orchard.getVariety());
        data.put("createdAt", orchard.getCreatedAt());
        data.put("updatedAt", orchard.getUpdatedAt());

        // 成员列表
        var members = orchardUserMapper.selectList(
                new LambdaQueryWrapper<OrchardUser>().eq(OrchardUser::getOrchardId, id)
        );
        List<Map<String, Object>> memberList = members.stream().map(m -> {
            Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("userId", m.getUserId());
            mm.put("roleType", m.getRoleType());
            mm.put("joinedAt", m.getCreatedAt());
            User user = userMapper.selectById(m.getUserId());
            if (user != null) {
                mm.put("username", user.getUsername());
                mm.put("phone", user.getPhone());
            }
            return mm;
        }).collect(Collectors.toList());
        data.put("members", memberList);

        // 设备列表
        var devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().eq(Device::getOrchardId, id)
        );
        List<Map<String, Object>> deviceList = devices.stream().map(d -> {
            Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("id", d.getId());
            dm.put("deviceCode", d.getDeviceCode());
            dm.put("name", d.getName());
            dm.put("type", d.getType());
            dm.put("model", d.getModel());
            dm.put("status", d.getStatus());
            dm.put("lastHeartbeat", d.getLastHeartbeat());
            dm.put("createdAt", d.getCreatedAt());
            return dm;
        }).collect(Collectors.toList());
        data.put("devices", deviceList);

        return Result.success(data);
    }

    /**
     * 果园识别记录（小程序端）
     */
    @GetMapping("/orchards/{id}/detections")
    public Result<Map<String, Object>> orchardDetections(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        var wrapper = new LambdaQueryWrapper<DiseaseDetection>()
                .eq(DiseaseDetection::getOrchardId, id)
                .orderByDesc(DiseaseDetection::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        var list = detectionMapper.selectList(wrapper);

        var countWrapper = new LambdaQueryWrapper<DiseaseDetection>()
                .eq(DiseaseDetection::getOrchardId, id);
        long total = detectionMapper.selectCount(countWrapper);

        List<Map<String, Object>> records = list.stream().map(d -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("imageUrl", d.getImageUrl());
            m.put("diseaseCode", d.getTopClassCode());
            m.put("confidence", d.getTopProbability());
            m.put("deviceTimestamp", d.getDeviceTimestamp());
            m.put("createdAt", d.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /**
     * 果园环境趋势（小程序端）
     */
    @GetMapping("/orchards/{id}/environment/trend")
    public Result<Map<String, Object>> orchardEnvTrend(@PathVariable Long id) {
        var wrapper = new LambdaQueryWrapper<EnvironmentData>()
                .eq(EnvironmentData::getOrchardId, id)
                .orderByDesc(EnvironmentData::getRecordTime)
                .last("LIMIT 168");
        var list = environmentDataMapper.selectList(wrapper);

        List<String> dates = new ArrayList<>();
        List<BigDecimal> temp = new ArrayList<>();
        List<BigDecimal> humidity = new ArrayList<>();
        List<BigDecimal> windSpeed = new ArrayList<>();
        for (var e : list) {
            dates.add(e.getRecordTime() != null ? e.getRecordTime().toString() : "");
            temp.add(e.getTemp());
            humidity.add(e.getHumidity());
            windSpeed.add(e.getWindSpeed());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dates", dates);
        data.put("temp", temp);
        data.put("humidity", humidity);
        data.put("windSpeed", windSpeed);
        return Result.success(data);
    }

    /**
     * 预警消息列表（小程序端）
     */
    @GetMapping("/alerts")
    public Result<Map<String, Object>> alertsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        var wrapper = new LambdaQueryWrapper<AlertMessage>()
                .orderByDesc(AlertMessage::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);
        var list = alertMessageMapper.selectList(wrapper);
        long total = alertMessageMapper.selectCount(null);

        List<Map<String, Object>> records = list.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("content", a.getContent());
            m.put("level", a.getLevel());
            m.put("orchardId", a.getOrchardId());
            m.put("read", a.getReadStatus() == 1);
            m.put("createdAt", a.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }
}
