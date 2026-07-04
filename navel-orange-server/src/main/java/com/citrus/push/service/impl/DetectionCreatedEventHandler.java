package com.citrus.push.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.calendar.entity.FarmingCalendar;
import com.citrus.calendar.mapper.FarmingCalendarMapper;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.event.DetectionCreatedEvent;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.knowledge.service.RagService;
import com.citrus.knowledge.vo.RagResponse;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.EmailPushService;
import com.citrus.push.service.EmailTemplateRenderer;
import com.citrus.push.service.SmsPushService;
import com.citrus.push.service.WechatPushService;
import com.citrus.push.task.DetectionEscalationTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionCreatedEventHandler {

    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final EnvironmentDataMapper environmentDataMapper;
    private final FarmingCalendarMapper farmingCalendarMapper;
    private final OrchardMapper orchardMapper;
    private final UserMapper userMapper;
    private final AlertMessageMapper alertMessageMapper;
    private final RagService ragService;
    private final WechatPushService wechatPushService;
    private final SmsPushService smsPushService;
    private final EmailPushService emailPushService;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final ObjectMapper objectMapper;
    private final DetectionEscalationTask detectionEscalationTask;


    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDetectionCreated(DetectionCreatedEvent event) {
        List<Long> ids = event.detectionIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<DiseaseDetection> detections = diseaseDetectionMapper.selectBatchIds(ids);
        if (detections.isEmpty()) {
            return;
        }

        Long orchardId = detections.get(0).getOrchardId();

        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            log.warn("果园不存在, orchardId={}, 跳过推送", orchardId);
            return;
        }

        User owner = userMapper.selectById(orchard.getOwnerId());
        if (owner == null) {
            log.warn("园主不存在, ownerId={}, 跳过推送", orchard.getOwnerId());
            return;
        }

        EnvironmentData envData = environmentDataMapper.selectOne(
                new QueryWrapper<EnvironmentData>()
                        .eq("orchard_id", orchardId)
                        .orderByDesc("record_time")
                        .last("LIMIT 1")
        );
        Double temp = envData != null && envData.getTemp() != null ? envData.getTemp().doubleValue() : null;
        Double humidity = envData != null && envData.getHumidity() != null ? envData.getHumidity().doubleValue() : null;

        List<FarmingCalendar> currentCalendars = farmingCalendarMapper.selectList(
                new QueryWrapper<FarmingCalendar>()
                        .eq("enabled", true)
                        .orderByAsc("sort_order")
        );
        String phenology = currentCalendars.stream()
                .map(FarmingCalendar::getPhenologyName)
                .filter(n -> n != null)
                .collect(Collectors.joining("、"));

        for (DiseaseDetection det : detections) {
            processSingleDetection(det, owner, orchard, temp, humidity, phenology);
        }

        detectionEscalationTask.checkAndEscalate(orchardId);
    }

    private void processSingleDetection(DiseaseDetection det, User owner, Orchard orchard,
                                         Double temp, Double humidity, String phenology) {
        try {
            String diseaseName = det.getDiseaseNameRaw();
            if (det.getDiseaseCatalogId() != null) {
                DiseaseCatalog catalog = diseaseCatalogMapper.selectById(det.getDiseaseCatalogId());
                if (catalog != null && catalog.getDiseaseName() != null) {
                    diseaseName = catalog.getDiseaseName();
                }
            }

            RagResponse ragResp = null;
            try {
                ragResp = ragService.queryForDetection(diseaseName, temp, humidity, phenology);
            } catch (Exception e) {
                log.warn("RAG 服务不可用, 跳过 AI 建议: diseaseName={}, err={}", diseaseName, e.getMessage());
            }

            int level = 1;
            if (det.getDiseaseCatalogId() != null) {
                DiseaseCatalog catalog = diseaseCatalogMapper.selectById(det.getDiseaseCatalogId());
                if (catalog != null && catalog.getSeverityLevel() != null) {
                    level = catalog.getSeverityLevel();
                }
            }

            String title = diseaseName + "识别提醒";
            String summary = "在果园「" + orchard.getName() + "」检测到" + diseaseName;
            String content = ragResp != null ? ragResp.getAnswer() : summary;

            String advice = null;
            String urgency = null;
            String consequences = null;
            if (ragResp != null && ragResp.getAnswer() != null) {
                String answer = ragResp.getAnswer();
                advice = extractSection(answer, "【防治措施】");
                urgency = extractSection(answer, "【紧急程度】");
                consequences = extractSection(answer, "【不处理的后果】");
                if (advice == null) {
                    advice = answer;
                }
            }

            AlertMessage msg = new AlertMessage();
            msg.setOrchardId(orchard.getId());
            msg.setUserId(owner.getId());
            msg.setLevel(level);
            msg.setTitle(title);
            msg.setSummary(summary);
            msg.setContent(content);
            msg.setAdvice(advice);
            msg.setMessageType("alert");
            msg.setRelatedType("detection");
            msg.setRelatedId(det.getId());
            msg.setPushChannel("app");
            msg.setPushStatus(0);
            msg.setReadStatus(0);

            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("diseaseName", diseaseName);
            extra.put("topClassCode", det.getTopClassCode());
            extra.put("probability", det.getTopProbability());
            extra.put("imageUrl", det.getImageUrl());
            extra.put("deviceTimestamp", det.getDeviceTimestamp());
            extra.put("urgency", urgency);
            extra.put("consequences", consequences);
            extra.put("phenology", phenology);
            extra.put("temperature", temp);
            extra.put("humidity", humidity);
            extra.put("orchardName", orchard.getName());
            try {
                msg.setExtraData(objectMapper.writeValueAsString(extra));
            } catch (Exception e) {
                log.warn("序列化extra_data失败: {}", e.getMessage());
            }

            msg.setCreatedAt(LocalDateTime.now());
            alertMessageMapper.insert(msg);

            boolean pushSuccess = false;
            // Wechat push removed

            if (pushSuccess) {
                msg.setPushStatus(1);
                msg.setPushTime(LocalDateTime.now());
                alertMessageMapper.updateById(msg);
            } else {
                msg.setPushStatus(2);
                alertMessageMapper.updateById(msg);
            }

            if (level == 3 && Boolean.TRUE.equals(owner.getEmailEnabled()) && owner.getPhone() != null) {
                String smsContent = "【" + orchard.getName() + "】" + diseaseName
                        + (urgency != null ? "，" + urgency : "")
                        + (advice != null ? "。防治建议：" + advice : "");
                boolean smsOk = smsPushService.sendAlertSms(owner.getId(), owner.getPhone(), smsContent, level);
                if (smsOk) {
                    log.info("L3短信已发送, userId={}, detectionId={}", owner.getId(), det.getId());
                }
            }

            // 发送邮件推送（L2/L3）
            if (level >= 2 && owner.getEmail() != null && !owner.getEmail().isBlank()) {
                try {
                    String html = emailTemplateRenderer.render(msg);
                    String emailSubject = "【脐橙监测】" + title;
                    boolean emailOk = emailPushService.sendAlertEmail(owner.getEmail(), emailSubject, html);
                    if (emailOk) {
                        log.info("预警邮件已发送, userId={}, email={}, detectionId={}",
                                owner.getId(), owner.getEmail(), det.getId());
                    }
                } catch (Exception e) {
                    log.error("发送预警邮件异常, userId={}, email={}, err={}",
                            owner.getId(), owner.getEmail(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("处理检测事件异常, detectionId={}, err={}", det.getId(), e.getMessage(), e);
        }
    }

    private String extractSection(String text, String section) {
        if (text == null) return null;
        int start = text.indexOf(section);
        if (start < 0) return null;
        start += section.length();
        int end = text.indexOf("【", start);
        if (end < 0) {
            end = text.indexOf("\n\n", start);
        }
        if (end < 0 || end > text.length()) {
            end = text.length();
        }
        return text.substring(start, end).trim();
    }
}
