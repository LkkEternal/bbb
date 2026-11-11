package com.emotion.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.emotion.admin.entity.AlertMessage;
import com.emotion.admin.entity.AlertRule;
import com.emotion.admin.mapper.AlertMessageMapper;
import com.emotion.admin.mapper.AlertRuleMapper;
import com.emotion.detection.entity.DetectionRecord;
import com.emotion.detection.mapper.DetectionRecordMapper;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 情绪预警评估定时任务
 * <p>
 * 每天 0:06 触发，遍历预警规则表中 emotion_labels 非空的规则（情绪类规则）：
 * 按 time_window_hours 定义时间范围，统计该用户 detection_record 中
 * 命中规则情绪标签且置信度在区间内的记录数，达到 min_occurrences 即触发预警。
 * 仅处理 push_enabled=1（接收预警消息）的用户。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionAlertService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** 深夜负面情绪规则的夜间时段：22:00~06:00 */
    private static final String NIGHT_RULE_CODE = "NIGHT_NEGATIVE";
    private static final int NIGHT_START_HOUR = 22;
    private static final int NIGHT_END_HOUR = 6;

    private final UserMapper userMapper;
    private final DetectionRecordMapper detectionRecordMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final AlertMessageMapper alertMessageMapper;

    @Scheduled(cron = "0 6 0 * * ?")
    public void evaluateEmotionRules() {
        log.info("[EmotionAlert] 开始情绪预警评估定时任务: time={}", LocalDateTime.now());

        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getEnabled, 1)
                        .isNotNull(AlertRule::getEmotionLabels)
                        .ne(AlertRule::getEmotionLabels, ""));
        if (rules.isEmpty()) {
            log.info("[EmotionAlert] 无启用的情绪类预警规则，跳过");
            return;
        }

        Map<Long, User> enabledUserMap = loadPushEnabledUsers();
        if (enabledUserMap.isEmpty()) {
            log.info("[EmotionAlert] 没有 push_enabled=1 的用户，跳过");
            return;
        }

        int triggered = 0;
        for (AlertRule rule : rules) {
            triggered += evaluateRule(rule, enabledUserMap);
        }
        log.info("[EmotionAlert] 情绪预警评估完成: rules={}, triggered={}", rules.size(), triggered);
    }

    /**
     * 评估单条规则：对每个启用用户统计窗口内命中记录数，达标即触发
     */
    private int evaluateRule(AlertRule rule, Map<Long, User> enabledUserMap) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(
                rule.getTimeWindowHours() != null ? rule.getTimeWindowHours() : 24);

        LambdaQueryWrapper<DetectionRecord> wrapper = new LambdaQueryWrapper<DetectionRecord>()
                .ge(DetectionRecord::getCreatedAt, windowStart)
                .in(DetectionRecord::getEmotionLabel, splitCsv(rule.getEmotionLabels()));
        if (rule.getMinConfidence() != null) {
            wrapper.ge(DetectionRecord::getConfidence, rule.getMinConfidence());
        }
        if (rule.getMaxConfidence() != null) {
            wrapper.le(DetectionRecord::getConfidence, rule.getMaxConfidence());
        }
        List<DetectionRecord> records = detectionRecordMapper.selectList(wrapper);

        boolean isNightRule = NIGHT_RULE_CODE.equals(rule.getRuleCode());
        Map<Long, List<DetectionRecord>> byUser = new LinkedHashMap<>();
        for (DetectionRecord r : records) {
            if (isNightRule && !isNightTime(r.getCreatedAt())) {
                continue;
            }
            byUser.computeIfAbsent(r.getUserId(), k -> new ArrayList<>()).add(r);
        }

        int minOccurrences = rule.getMinOccurrences() != null ? rule.getMinOccurrences() : 1;
        Set<Long> matchedUserIds = new HashSet<>();
        for (Map.Entry<Long, List<DetectionRecord>> entry : byUser.entrySet()) {
            User user = enabledUserMap.get(entry.getKey());
            if (user == null) {
                continue;
            }
            if (entry.getValue().size() >= minOccurrences) {
                upsertAlert(rule, user, entry.getValue());
                matchedUserIds.add(entry.getKey());
            }
        }

        deactivateNotMatched(rule, matchedUserIds);
        return matchedUserIds.size();
    }

    /**
     * 写入预警消息（同 rule_id + user_id + active=1 已存在则跳过）
     */
    private void upsertAlert(AlertRule rule, User user, List<DetectionRecord> matchedRecords) {
        Long exists = alertMessageMapper.selectCount(
                new LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getRuleId, rule.getId())
                        .eq(AlertMessage::getUserId, user.getId())
                        .eq(AlertMessage::getActive, 1));
        if (exists != null && exists > 0) {
            return;
        }

        AlertMessage message = new AlertMessage();
        message.setRuleId(rule.getId());
        message.setRuleName(rule.getRuleName());
        message.setUserId(user.getId());
        message.setLocation(user.getLocationId());
        message.setCityName(user.getCityName());
        message.setAlertContent(buildContent(rule, matchedRecords));
        message.setEmotionCount(matchedRecords.size());
        message.setEmotionLabel(extractEmotion(matchedRecords));
        message.setLastDetectTime(matchedRecords.stream()
                .map(DetectionRecord::getCreatedAt)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        message.setActive(1);
        message.setStatus(0);
        message.setCreatedAt(LocalDateTime.now());
        alertMessageMapper.insert(message);
        log.info("[EmotionAlert] 触发情绪预警: rule={}, userId={}, count={}",
                rule.getRuleName(), user.getId(), matchedRecords.size());
    }

    /**
     * 解除该规则下未命中用户的生效预警
     */
    private void deactivateNotMatched(AlertRule rule, Set<Long> matchedUserIds) {
        List<AlertMessage> activeList = alertMessageMapper.selectList(
                new LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getRuleId, rule.getId())
                        .eq(AlertMessage::getActive, 1)
                        .isNotNull(AlertMessage::getUserId));
        for (AlertMessage msg : activeList) {
            if (!matchedUserIds.contains(msg.getUserId())) {
                alertMessageMapper.update(null, new LambdaUpdateWrapper<AlertMessage>()
                        .eq(AlertMessage::getId, msg.getId())
                        .set(AlertMessage::getActive, 0));
                log.info("[EmotionAlert] 解除情绪预警: rule={}, userId={}", rule.getRuleName(), msg.getUserId());
            }
        }
    }

    /**
     * 渲染预警文案（模板占位符：{count} {emotion} {last_time}；模板为空用默认文案）
     */
    private String buildContent(AlertRule rule, List<DetectionRecord> records) {
        int count = records.size();
        String emotion = extractEmotion(records);
        LocalDateTime lastTime = records.stream()
                .map(DetectionRecord::getCreatedAt)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        String lastTimeStr = lastTime != null ? lastTime.format(TIME_FMT) : "";

        String template = rule.getActionContentTemplate();
        if (template == null || template.isBlank()) {
            return "【" + rule.getRuleName() + "】检测到您的情绪波动，共" + count + "条相关记录，"
                    + (lastTimeStr.isEmpty() ? "" : "最近一次检测时间：" + lastTimeStr + "。")
                    + "请注意调节情绪，必要时寻求帮助。";
        }
        return template
                .replace("{count}", String.valueOf(count))
                .replace("{emotion}", emotion.isBlank() ? "负面" : emotion)
                .replace("{last_time}", lastTimeStr)
                .replace("{weather}", "")
                .replace("{temperature}", "");
    }

    /**
     * 提取去重的情绪标签（用 / 连接）
     */
    private String extractEmotion(List<DetectionRecord> records) {
        return records.stream()
                .map(DetectionRecord::getEmotionCn)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .collect(Collectors.joining("/"));
    }

    private boolean isNightTime(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        int hour = time.getHour();
        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR;
    }

    /**
     * 加载 push_enabled=1 的用户（只对这些用户写预警）
     */
    private Map<Long, User> loadPushEnabledUsers() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPushEnabled, 1)
                        .isNotNull(User::getPushEnabled));
        Map<Long, User> map = new LinkedHashMap<>();
        for (User user : users) {
            map.put(user.getId(), user);
        }
        return map;
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
