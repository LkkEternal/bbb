package com.citrus.push.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.WechatPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyAlertSummaryTask {

    private final AlertMessageMapper alertMessageMapper;
    private final UserMapper userMapper;
    private final OrchardMapper orchardMapper;
    private final WechatPushService wechatPushService;

    @Scheduled(cron = "0 0 20 * * ?")
    public void sendDailySummary() {
        log.info("========== L1每日汇总推送触发 [cron=0 0 20 * * ?] ==========");

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        List<AlertMessage> l1Messages = alertMessageMapper.selectList(
                new QueryWrapper<AlertMessage>()
                        .eq("level", 1)
                        .eq("message_type", "alert")
                        .eq("push_status", 0)
                        .ge("created_at", todayStart)
                        .le("created_at", todayEnd)
        );

        if (l1Messages.isEmpty()) {
            log.info("今日无L1低风险预警消息，跳过汇总推送");
            return;
        }

        Map<Long, List<AlertMessage>> byUser = l1Messages.stream()
                .collect(Collectors.groupingBy(AlertMessage::getUserId));

        for (Map.Entry<Long, List<AlertMessage>> entry : byUser.entrySet()) {
            try {
                Long userId = entry.getKey();
                List<AlertMessage> msgs = entry.getValue();

                User user = userMapper.selectById(userId);
                if (user == null || !Boolean.TRUE.equals(user.getPushEnabled()) || user.getWechatOpenid() == null) {
                    log.warn("用户不支持推送或openid为空, userId={}, 跳过汇总", userId);
                    continue;
                }

                StringBuilder summaryContent = new StringBuilder();
                summaryContent.append("今日低风险预警汇总（").append(msgs.size()).append("条）\n\n");
                for (AlertMessage msg : msgs) {
                    Orchard orchard = orchardMapper.selectById(msg.getOrchardId());
                    String orchardName = orchard != null ? orchard.getName() : "未知果园";
                    summaryContent.append("• ").append(orchardName).append(": ").append(msg.getTitle()).append("\n");
                }
                summaryContent.append("\n请及时查看处理。");

                AlertMessage summaryMsg = new AlertMessage();
                summaryMsg.setOrchardId(null);
                summaryMsg.setUserId(userId);
                summaryMsg.setLevel(1);
                summaryMsg.setTitle("低风险预警汇总");
                summaryMsg.setSummary(summaryContent.length() > 200 ? summaryContent.substring(0, 200) : summaryContent.toString());
                summaryMsg.setContent(summaryContent.toString());
                summaryMsg.setMessageType("summary");
                summaryMsg.setPushChannel("wechat");
                summaryMsg.setPushStatus(0);
                summaryMsg.setReadStatus(0);
                summaryMsg.setCreatedAt(LocalDateTime.now());
                alertMessageMapper.insert(summaryMsg);

                boolean pushSuccess = wechatPushService.sendAlertNotification(
                        summaryMsg.getId(), userId, "低风险预警汇总",
                        "今日共" + msgs.size() + "条低风险预警待查看",
                        "请点击查看详情", 1, user.getWechatOpenid());

                if (pushSuccess) {
                    summaryMsg.setPushStatus(1);
                    summaryMsg.setPushTime(LocalDateTime.now());
                    alertMessageMapper.updateById(summaryMsg);

                    for (AlertMessage msg : msgs) {
                        UpdateWrapper<AlertMessage> wrapper = new UpdateWrapper<>();
                        wrapper.eq("id", msg.getId())
                               .set("push_status", 4);
                        alertMessageMapper.update(null, wrapper);
                    }
                    log.info("L1汇总推送成功, userId={}, summaryId={}, count={}", userId, summaryMsg.getId(), msgs.size());
                } else {
                    summaryMsg.setPushStatus(2);
                    alertMessageMapper.updateById(summaryMsg);
                    log.warn("L1汇总推送失败, userId={}, summaryId={}", userId, summaryMsg.getId());
                }
            } catch (Exception e) {
                log.error("L1汇总推送异常, userId={}, err={}", entry.getKey(), e.getMessage());
            }
        }

        log.info("L1每日汇总推送完成");
    }
}
