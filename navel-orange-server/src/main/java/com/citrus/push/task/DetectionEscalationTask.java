package com.citrus.push.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.SmsPushService;
import com.citrus.push.service.WechatPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionEscalationTask {

    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final OrchardMapper orchardMapper;
    private final UserMapper userMapper;
    private final AlertMessageMapper alertMessageMapper;
    private final WechatPushService wechatPushService;
    private final SmsPushService smsPushService;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void checkAndEscalate(Long orchardId) {
        if (orchardId == null) {
            return;
        }

        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null || Boolean.TRUE.equals(orchard.getDeleted())) {
            return;
        }

        User owner = userMapper.selectById(orchard.getOwnerId());
        if (owner == null) {
            return;
        }

        LocalDateTime deadline = LocalDateTime.now().minusHours(24);

        List<DiseaseDetection> unprocessed = diseaseDetectionMapper.selectList(
                new QueryWrapper<DiseaseDetection>()
                        .eq("orchard_id", orchardId)
                        .eq("processed", false)
                        .lt("created_at", deadline)
        );

        if (unprocessed.isEmpty()) {
            return;
        }

        for (DiseaseDetection det : unprocessed) {
            try {
                Long existingCount = alertMessageMapper.selectCount(
                        new QueryWrapper<AlertMessage>()
                                .eq("related_type", "detection")
                                .eq("related_id", det.getId())
                                .eq("message_type", "escalation")
                );

                if (existingCount > 0) {
                    continue;
                }

                String diseaseName = det.getDiseaseNameRaw();
                if (det.getDiseaseCatalogId() != null) {
                    DiseaseCatalog catalog = diseaseCatalogMapper.selectById(det.getDiseaseCatalogId());
                    if (catalog != null && catalog.getDiseaseName() != null) {
                        diseaseName = catalog.getDiseaseName();
                    }
                }

                int originalLevel = 1;
                if (det.getDiseaseCatalogId() != null) {
                    DiseaseCatalog catalog = diseaseCatalogMapper.selectById(det.getDiseaseCatalogId());
                    if (catalog != null && catalog.getSeverityLevel() != null) {
                        originalLevel = catalog.getSeverityLevel();
                    }
                }

                String title = "严重超时未处理";
                String summary = String.format("【原等级L%d】%s已超过24小时未处理，请立即处理！", originalLevel, diseaseName);
                String content = summary;
                String advice = "请尽快登录小程序查看防治建议并上传处理证据。";

                AlertMessage msg = new AlertMessage();
                msg.setOrchardId(orchardId);
                msg.setUserId(owner.getId());
                msg.setLevel(originalLevel);
                msg.setTitle(title);
                msg.setSummary(summary);
                msg.setContent(content);
                msg.setAdvice(advice);
                msg.setMessageType("escalation");
                msg.setRelatedType("detection");
                msg.setRelatedId(det.getId());
                msg.setReadStatus(0);

                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("diseaseName", diseaseName);
                extra.put("originalLevel", originalLevel);
                extra.put("detectionCreatedAt", det.getCreatedAt() != null ? det.getCreatedAt().toString() : null);
                try {
                    msg.setExtraData(objectMapper.writeValueAsString(extra));
                } catch (Exception e) {
                    log.warn("序列化extra_data失败: {}", e.getMessage());
                }

                msg.setCreatedAt(LocalDateTime.now());
                alertMessageMapper.insert(msg);

                boolean smsOk = false;
                if (owner.getPhone() != null) {
                    String smsContent = "【脐橙监测】" + diseaseName + "已超24小时未处理，请立即登录小程序查看。";
                    smsOk = smsPushService.sendAlertSms(owner.getId(), owner.getPhone(), smsContent, originalLevel);
                }

                int pushStatus = smsOk ? 1 : 2;

                UpdateWrapper<AlertMessage> wrapper = new UpdateWrapper<>();
                wrapper.eq("id", msg.getId())
                       .set("push_channel", smsOk ? "sms" : "")
                       .set("push_status", pushStatus);
                if (pushStatus == 1) {
                    wrapper.set("push_time", LocalDateTime.now());
                }
                alertMessageMapper.update(null, wrapper);

                log.info("升级推送完成: detectionId={}, level=L{}, messageId={}, pushStatus={}",
                        det.getId(), originalLevel, msg.getId(), pushStatus);

            } catch (Exception e) {
                log.error("升级处理异常, detectionId={}, err={}", det.getId(), e.getMessage(), e);
            }
        }
    }
}
