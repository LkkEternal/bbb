package com.emotion.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.emotion.admin.dto.AlertEmailData;
import com.emotion.admin.entity.AlertMessage;
import com.emotion.admin.mapper.AlertMessageMapper;
import com.emotion.detection.entity.EmotionDict;
import com.emotion.detection.mapper.EmotionDictMapper;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预警邮件提醒定时任务
 * <p>
 * 每天 22:00 触发，遍历 alert_message 中未处理（status=0）且仍生效（active=1）
 * 的情绪预警（user_id 非空），对开启了预警接收（push_enabled=1）和邮箱推送
 * （email_enabled=1）的用户发送邮件提醒。发送成功即标记 status=1（已处理）
 * 且 email_sent=1（只发一次），失败仅记日志。
 * <p>
 * 邮件使用固定静态模板（alert_email.html），发送时仅填充结构化数据：
 * 标题/称呼/情绪/次数/建议/时间；💡建议内容按情绪标签查 emotion_dict.defaultAdvice。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAlertTask {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** 兜底建议文案 */
    private static final String FALLBACK_ADVICE =
            "请留意情绪变化，尝试深呼吸、适度运动或与信任的人交流；如持续不适，请拨打24小时心理援助热线 400-161-9995。";

    private final AlertMessageMapper alertMessageMapper;
    private final UserMapper userMapper;
    private final EmotionDictMapper emotionDictMapper;
    private final MailService mailService;

    @Scheduled(cron = "0 0 22 * * ?")
    public void sendPendingAlertEmails() {
        log.info("[EmailAlert] 开始预警邮件提醒任务: time={}", LocalDateTime.now());

        List<AlertMessage> pending = alertMessageMapper.selectList(
                new LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getActive, 1)
                        .eq(AlertMessage::getStatus, 0)
                        .eq(AlertMessage::getEmailSent, 0)
                        .isNotNull(AlertMessage::getUserId));
        if (pending.isEmpty()) {
            log.info("[EmailAlert] 无待发送的预警邮件，跳过");
            return;
        }

        List<Long> userIds = pending.stream()
                .map(AlertMessage::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<String, String> adviceMap = loadAdviceMap(pending);

        int sent = 0;
        int skipped = 0;
        for (AlertMessage msg : pending) {
            User user = userMap.get(msg.getUserId());
            if (user == null || !isEnabled(user)) {
                skipped++;
                continue;
            }
            boolean ok = mailService.sendAlertEmail(buildData(user, msg, adviceMap));
            if (ok) {
                alertMessageMapper.update(null, new LambdaUpdateWrapper<AlertMessage>()
                        .eq(AlertMessage::getId, msg.getId())
                        .set(AlertMessage::getStatus, 1)
                        .set(AlertMessage::getEmailSent, 1));
                sent++;
            }
        }
        log.info("[EmailAlert] 预警邮件任务完成: total={}, sent={}, skipped={}", pending.size(), sent, skipped);
    }

    /**
     * 组装结构化邮件数据（固定模板占位符）
     */
    private AlertEmailData buildData(User user, AlertMessage msg, Map<String, String> adviceMap) {
        AlertEmailData data = new AlertEmailData();
        data.setTo(user.getEmail());
        data.setSubject("【情绪预警】" + msg.getRuleName());
        data.setTitle(msg.getRuleName());
        data.setUserName(user.getNickname() != null && !user.getNickname().isBlank()
                ? user.getNickname() : user.getUsername());
        data.setEmotion(msg.getEmotionLabel());
        data.setCount(msg.getEmotionCount());
        data.setAdvice(buildAdvice(msg.getEmotionLabel(), adviceMap));
        data.setLastTime(msg.getLastDetectTime() != null ? msg.getLastDetectTime().format(TIME_FMT) : "");
        return data;
    }

    /**
     * 加载涉及的每个情绪标签对应的建议文案
     */
    private Map<String, String> loadAdviceMap(List<AlertMessage> pending) {
        List<String> labels = pending.stream()
                .map(AlertMessage::getEmotionLabel)
                .filter(l -> l != null && !l.isBlank())
                .flatMap(l -> Arrays.stream(l.split("/")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (labels.isEmpty()) {
            return Map.of();
        }
        List<EmotionDict> dicts = emotionDictMapper.selectList(
                new LambdaQueryWrapper<EmotionDict>()
                        .in(EmotionDict::getEmotionCn, labels)
                        .eq(EmotionDict::getIsActive, 1));
        return dicts.stream()
                .filter(d -> d.getEmotionCn() != null && d.getDefaultAdvice() != null)
                .collect(Collectors.toMap(
                        EmotionDict::getEmotionCn,
                        EmotionDict::getDefaultAdvice,
                        (a, b) -> a));
    }

    /**
     * 按情绪标签拼接建议（多个情绪取第一个命中的，否则兜底）
     */
    private String buildAdvice(String emotionLabel, Map<String, String> adviceMap) {
        if (emotionLabel == null || emotionLabel.isBlank()) {
            return FALLBACK_ADVICE;
        }
        for (String label : emotionLabel.split("/")) {
            String advice = adviceMap.get(label.trim());
            if (advice != null && !advice.isBlank()) {
                return advice;
            }
        }
        return FALLBACK_ADVICE;
    }

    /**
     * 用户必须同时开启预警接收与邮箱推送才发送邮件
     */
    private boolean isEnabled(User user) {
        return Integer.valueOf(1).equals(user.getPushEnabled())
                && Integer.valueOf(1).equals(user.getEmailEnabled());
    }
}
