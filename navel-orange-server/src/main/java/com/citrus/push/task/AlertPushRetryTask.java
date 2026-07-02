package com.citrus.push.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.SmsPushService;
import com.citrus.push.service.WechatPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertPushRetryTask {

    private static final int MAX_RETRY_COUNT = 3;

    private final AlertMessageMapper alertMessageMapper;
    private final UserMapper userMapper;
    private final WechatPushService wechatPushService;
    private final SmsPushService smsPushService;

    @Scheduled(fixedRate = 600000)
    public void retryFailedPushes() {
        List<AlertMessage> failures = alertMessageMapper.selectList(
                new LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getPushStatus, 2)
                        .eq(AlertMessage::getMessageType, "alert")
                        .lt(AlertMessage::getCreatedAt, LocalDateTime.now().minusMinutes(5))
                        .last("LIMIT 50")
        );

        if (failures.isEmpty()) {
            return;
        }

        log.info("推送重试任务: 发现{}条失败记录", failures.size());

        for (AlertMessage msg : failures) {
            try {
                User user = userMapper.selectById(msg.getUserId());
                if (user == null) {
                    log.warn("重试跳过: 用户不存在, messageId={}", msg.getId());
                    continue;
                }

                int retryCount = 0;
                if (msg.getExtraData() != null) {
                    try {
                        com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(msg.getExtraData());
                        if (root.has("retryCount")) {
                            retryCount = root.get("retryCount").asInt();
                        }
                    } catch (Exception ignored) {}
                }

                if (retryCount >= MAX_RETRY_COUNT) {
                    log.warn("重试已达上限({}), 标记为最终失败, messageId={}", MAX_RETRY_COUNT, msg.getId());
                    LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(AlertMessage::getId, msg.getId())
                           .set(AlertMessage::getPushStatus, 3);
                    alertMessageMapper.update(null, wrapper);
                    continue;
                }

                boolean allSuccess = true;

                if (user.getWechatOpenid() != null) {
                    boolean wechatOk = wechatPushService.sendAlertNotification(
                            msg.getId(), user.getId(), msg.getTitle(),
                            msg.getSummary(), msg.getAdvice(),
                            msg.getLevel(), user.getWechatOpenid());
                    if (!wechatOk) allSuccess = false;
                }

                if (msg.getLevel() == 3 && user.getPhone() != null) {
                    boolean smsOk = smsPushService.sendAlertSms(
                            user.getId(), user.getPhone(), msg.getContent(), msg.getLevel());
                    if (!smsOk) allSuccess = false;
                }

                if (allSuccess) {
                    msg.setPushStatus(1);
                    msg.setPushTime(LocalDateTime.now());
                    alertMessageMapper.updateById(msg);
                    log.info("重试推送成功, messageId={}", msg.getId());
                } else {
                    retryCount++;
                    updateRetryCount(msg, retryCount);
                    log.warn("重试推送仍失败, messageId={}, retryCount={}", msg.getId(), retryCount);
                }
            } catch (Exception e) {
                log.error("重试推送异常, messageId={}, err={}", msg.getId(), e.getMessage());
            }
        }
    }

    private void updateRetryCount(AlertMessage msg, int retryCount) {
        try {
            java.util.Map<String, Object> extra = new java.util.LinkedHashMap<>();
            if (msg.getExtraData() != null) {
                extra = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(msg.getExtraData(), java.util.LinkedHashMap.class);
            }
            extra.put("retryCount", retryCount);
            msg.setExtraData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(extra));
            alertMessageMapper.updateById(msg);
        } catch (Exception e) {
            log.warn("更新重试次数失败, messageId={}", msg.getId());
        }
    }
}
