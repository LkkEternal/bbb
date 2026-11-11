package com.emotion.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.admin.dto.*;
import com.emotion.admin.entity.AlertRule;
import com.emotion.admin.mapper.AlertRuleMapper;
import com.emotion.admin.service.AdminService;
import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.detection.entity.EmotionDict;
import com.emotion.detection.entity.EmotionFeedback;
import com.emotion.detection.mapper.EmotionDictMapper;
import com.emotion.detection.mapper.EmotionFeedbackMapper;
import com.emotion.infrastructure.config.QiniuConfig;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import com.qiniu.util.Auth;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 系统管理服务实现类
 * 提供系统管理和运营支持功能：
 * - 预警规则管理
 * - 情感词典管理
 * - 用户反馈审核
 * - 反馈数据导出
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AlertRuleMapper alertRuleMapper;        // 预警规则数据访问
    private final EmotionDictMapper emotionDictMapper;    // 情感词典数据访问
    private final EmotionFeedbackMapper emotionFeedbackMapper;  // 情感反馈数据访问
    private final UserMapper userMapper;                  // 用户数据访问
    private final QiniuConfig qiniuConfig;                // 七牛云配置
    private final Auth qiniuAuth;                         // 七牛云认证

    /**
     * 获取所有预警规则
     *
     * @return 预警规则响应列表，按优先级升序排列
     */
    @Override
    public List<AlertRuleResponse> getAlertRules() {
        List<AlertRule> rules = alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>().orderByAsc(AlertRule::getPriority));
        return rules.stream().map(this::toAlertRuleResponse).collect(Collectors.toList());
    }

    /**
     * 更新预警规则
     *
     * 支持更新的字段：
     * - enabled: 启用/禁用状态
     * - ruleName: 规则名称
     * - emotionLabels: 触发的情感标签（逗号分隔）
     * - minOccurrences: 最小触发次数
     * - timeWindowHours: 时间窗口（小时）
     * - cooldownMinutes: 冷却时间（分钟）
     *
     * @param id 预警规则ID
     * @param request 更新请求（部分字段可选）
     * @throws BizException 如果规则不存在
     */
    @Override
    @Transactional
    public void updateAlertRule(Integer id, AlertRuleUpdateRequest request) {
        // 1. 验证规则是否存在
        AlertRule existing = alertRuleMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预警规则不存在");
        }
        if (request == null) {
            return;
        }

        // 2. 构建更新条件
        LambdaUpdateWrapper<AlertRule> update = new LambdaUpdateWrapper<>();
        update.eq(AlertRule::getId, id);

        // 3. 设置可更新的字段
        if (request.getEnabled() != null) {
            update.set(AlertRule::getEnabled, request.getEnabled() ? 1 : 0);
        }
        if (request.getRuleName() != null) {
            update.set(AlertRule::getRuleName, request.getRuleName());
        }
        if (request.getEmotionLabels() != null) {
            update.set(AlertRule::getEmotionLabels, request.getEmotionLabels());
        }
        if (request.getWeatherConditions() != null) {
            update.set(AlertRule::getWeatherConditions, request.getWeatherConditions());
        }
        if (request.getMinTemperature() != null) {
            update.set(AlertRule::getMinTemperature, request.getMinTemperature());
        }
        if (request.getMaxTemperature() != null) {
            update.set(AlertRule::getMaxTemperature, request.getMaxTemperature());
        }
        if (request.getMinHumidity() != null) {
            update.set(AlertRule::getMinHumidity, request.getMinHumidity());
        }
        if (request.getMaxHumidity() != null) {
            update.set(AlertRule::getMaxHumidity, request.getMaxHumidity());
        }
        if (request.getMinOccurrences() != null) {
            update.set(AlertRule::getMinOccurrences, request.getMinOccurrences());
        }
        if (request.getTimeWindowHours() != null) {
            update.set(AlertRule::getTimeWindowHours, request.getTimeWindowHours());
        }
        if (request.getCooldownMinutes() != null) {
            update.set(AlertRule::getCooldownMinutes, request.getCooldownMinutes());
        }

        // 4. 执行更新
        alertRuleMapper.update(null, update);
    }

    @Override
    public List<EmotionDictResponse> getEmotionDictList() {
        List<EmotionDict> dicts = emotionDictMapper.selectList(
                new LambdaQueryWrapper<EmotionDict>().orderByAsc(EmotionDict::getSortOrder));
        return dicts.stream().map(this::toEmotionDictResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateEmotionDict(Integer id, EmotionDictUpdateRequest request) {
        EmotionDict existing = emotionDictMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "表情字典不存在");
        }
        if (request == null || (request.getDefaultAdvice() == null && request.getIsActive() == null)) {
            return;
        }
        LambdaUpdateWrapper<EmotionDict> update = new LambdaUpdateWrapper<>();
        update.eq(EmotionDict::getId, id);
        if (request.getDefaultAdvice() != null) {
            update.set(EmotionDict::getDefaultAdvice, request.getDefaultAdvice());
        }
        if (request.getIsActive() != null) {
            update.set(EmotionDict::getIsActive, request.getIsActive() ? 1 : 0);
        }
        emotionDictMapper.update(null, update);
    }

    @Override
    public FeedbackPageResponse getFeedbacks(FeedbackPageRequest request) {
        Page<EmotionFeedback> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<EmotionFeedback> wrapper = new LambdaQueryWrapper<>();
        if (request.getStatus() != null) {
            wrapper.eq(EmotionFeedback::getStatus, request.getStatus());
        }
        if (request.getCorrectLabel() != null && !request.getCorrectLabel().isEmpty()) {
            wrapper.eq(EmotionFeedback::getCorrectLabel, request.getCorrectLabel());
        }
        wrapper.orderByDesc(EmotionFeedback::getCreatedAt);

        Page<EmotionFeedback> result = emotionFeedbackMapper.selectPage(page, wrapper);

        FeedbackPageResponse response = new FeedbackPageResponse();
        response.setTotal(result.getTotal());
        response.setPage((int) result.getCurrent());
        response.setSize((int) result.getSize());
        response.setPages((int) result.getPages());

        List<FeedbackPageResponse.FeedbackDTO> dtos = result.getRecords().stream()
                .map(this::toFeedbackDTO)
                .collect(Collectors.toList());
        response.setRecords(dtos);

        return response;
    }

    @Override
    @Transactional
    public void reviewFeedback(Long id, FeedbackReviewRequest request, Long adminUserId) {
        EmotionFeedback feedback = emotionFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "反馈记录不存在");
        }
        if (feedback.getStatus() != 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该反馈已审核，不能重复审核");
        }

        LambdaUpdateWrapper<EmotionFeedback> update = new LambdaUpdateWrapper<>();
        update.eq(EmotionFeedback::getId, id);
        update.set(EmotionFeedback::getStatus, request.getStatus());
        update.set(EmotionFeedback::getReviewedBy, adminUserId);
        update.set(EmotionFeedback::getReviewedAt, LocalDateTime.now());
        if (request.getCorrectLabel() != null) {
            update.set(EmotionFeedback::getCorrectLabel, request.getCorrectLabel());
        }
        emotionFeedbackMapper.update(null, update);
    }

    @Override
    public void exportFeedbacks(HttpServletResponse response) throws Exception {
        List<EmotionFeedback> feedbacks = emotionFeedbackMapper.selectList(
                new LambdaQueryWrapper<EmotionFeedback>().eq(EmotionFeedback::getStatus, 1));

        if (feedbacks.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "暂无已采用的反馈数据可导出");
        }

        Map<String, List<EmotionFeedback>> grouped = feedbacks.stream()
                .collect(Collectors.groupingBy(EmotionFeedback::getCorrectLabel));

        String fileName = "emotion_feedback_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".zip";

        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            int fileCounter = 0;
            for (Map.Entry<String, List<EmotionFeedback>> entry : grouped.entrySet()) {
                String label = entry.getKey();
                List<EmotionFeedback> labelFeedbacks = entry.getValue();

                for (EmotionFeedback fb : labelFeedbacks) {
                    try {
                        String downloadUrl = qiniuAuth.privateDownloadUrl(
                                qiniuConfig.getDomain() + "/" + fb.getObjectKey());
                        URL url = new URL(downloadUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(10000);
                        conn.setReadTimeout(30000);

                        if (conn.getResponseCode() == 200) {
                            String ext = getFileExtension(fb.getObjectKey());
                            String entryName = label + "/fb_" +
                                    String.format("%03d", ++fileCounter) + "." + ext;

                            zos.putNextEntry(new ZipEntry(entryName));
                            try (InputStream is = conn.getInputStream()) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    zos.write(buffer, 0, bytesRead);
                                }
                            }
                            zos.closeEntry();
                        } else {
                            log.warn("[Admin] 下载反馈图片失败, feedbackId={}, statusCode={}",
                                    fb.getId(), conn.getResponseCode());
                        }
                        conn.disconnect();
                    } catch (Exception e) {
                        log.error("[Admin] 导出反馈图片异常, feedbackId={}", fb.getId(), e);
                    }
                }
            }
        }
    }

    private AlertRuleResponse toAlertRuleResponse(AlertRule rule) {
        AlertRuleResponse resp = new AlertRuleResponse();
        resp.setId(rule.getId());
        resp.setRuleName(rule.getRuleName());
        resp.setRuleCode(rule.getRuleCode());
        resp.setEnabled(rule.getEnabled() == 1);
        resp.setPriority(rule.getPriority());
        resp.setEmotionLabels(rule.getEmotionLabels());
        resp.setMinConfidence(rule.getMinConfidence());
        resp.setMaxConfidence(rule.getMaxConfidence());
        resp.setWeatherConditions(rule.getWeatherConditions());
        resp.setMinTemperature(rule.getMinTemperature());
        resp.setMaxTemperature(rule.getMaxTemperature());
        resp.setMinHumidity(rule.getMinHumidity());
        resp.setMaxHumidity(rule.getMaxHumidity());
        resp.setTimeWindowHours(rule.getTimeWindowHours());
        resp.setMinOccurrences(rule.getMinOccurrences());
        resp.setActionType(rule.getActionType());
        resp.setActionContentTemplate(rule.getActionContentTemplate());
        resp.setCooldownMinutes(rule.getCooldownMinutes());
        return resp;
    }

    private EmotionDictResponse toEmotionDictResponse(EmotionDict dict) {
        EmotionDictResponse resp = new EmotionDictResponse();
        resp.setId(dict.getId());
        resp.setEmotionLabel(dict.getEmotionLabel());
        resp.setEmotionCn(dict.getEmotionCn());
        resp.setEmojiIcon(dict.getEmojiIcon());
        resp.setDefaultAdvice(dict.getDefaultAdvice());
        resp.setAdviceSource(dict.getAdviceSource());
        resp.setSortOrder(dict.getSortOrder());
        resp.setIsActive(dict.getIsActive() != null && dict.getIsActive() == 1);
        return resp;
    }

    private FeedbackPageResponse.FeedbackDTO toFeedbackDTO(EmotionFeedback fb) {
        FeedbackPageResponse.FeedbackDTO dto = new FeedbackPageResponse.FeedbackDTO();
        dto.setFeedbackId(fb.getId());
        dto.setUserId(fb.getUserId());
        dto.setImageUrl(fb.getImageUrl());
        dto.setSystemLabel(fb.getSystemLabel());
        dto.setCorrectLabel(fb.getCorrectLabel());
        dto.setDescription(fb.getDescription());
        dto.setStatus(fb.getStatus());
        dto.setCreatedAt(fb.getCreatedAt());

        User user = userMapper.selectById(fb.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
        }
        return dto;
    }

    private String getFileExtension(String objectKey) {
        if (objectKey == null) return "jpg";
        int dot = objectKey.lastIndexOf('.');
        if (dot > 0) {
            return objectKey.substring(dot + 1).toLowerCase();
        }
        return "jpg";
    }
}
