package com.citrus.collector.controller;

import com.citrus.collector.dto.ProcessDetectionDTO;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.common.result.Result;
import com.citrus.common.util.HuaweiObsClient;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.mapper.AlertMessageMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
@Tag(name = "病害处理", description = "病害识别记录的处理与证据上传")
public class DetectionProcessController {

    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final HuaweiObsClient huaweiObsClient;
    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final AlertMessageMapper alertMessageMapper;

    /**
     * 获取单条检测记录详情（含处理状态）
     */
    @GetMapping("/{id}")
    @Operation(summary = "检测记录详情", description = "获取单条检测记录完整信息，包含病害名称、图片、防治建议等")
    public Result<Map<String, Object>> getDetectionDetail(@PathVariable Long id) {
        DiseaseDetection det = diseaseDetectionMapper.selectById(id);
        if (det == null) {
            return Result.error(404, "检测记录不存在");
        }

        // 查询病害目录获取详细信息
        String diseaseName = det.getDiseaseNameRaw();
        String preventionGuide = null;
        Integer severityLevel = null;
        if (det.getDiseaseCatalogId() != null) {
            DiseaseCatalog catalog = diseaseCatalogMapper.selectById(det.getDiseaseCatalogId());
            if (catalog != null) {
                if (catalog.getDiseaseName() != null) {
                    diseaseName = catalog.getDiseaseName();
                }
                preventionGuide = catalog.getPreventionGuide();
                severityLevel = catalog.getSeverityLevel();
            }
        }

        List<String> images = new ArrayList<>();
        if (det.getTreatmentImages() != null) {
            try {
                images = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        det.getTreatmentImages(),
                        com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
            } catch (Exception ignored) {}
        }

        // 解析 extraData 获取RAG数据
        String urgency = null;
        String consequences = null;
        if (det.getExtraData() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> extra = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        det.getExtraData(), java.util.LinkedHashMap.class);
                if (extra.get("urgency") != null) urgency = extra.get("urgency").toString();
                if (extra.get("consequences") != null) consequences = extra.get("consequences").toString();
            } catch (Exception ignored) {}
        }

        // 查关联预警消息获取防治建议数据
        String advice = null;
        List<AlertMessage> alerts = alertMessageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlertMessage>()
                        .eq(AlertMessage::getRelatedType, "detection")
                        .eq(AlertMessage::getRelatedId, id)
                        .orderByDesc(AlertMessage::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (!alerts.isEmpty()) {
            AlertMessage alert = alerts.get(0);
            advice = alert.getAdvice();
            // Try to get urgency/consequences from extraData (overwrites detection-level data if present)
            if (alert.getExtraData() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> extra = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                            alert.getExtraData(), java.util.LinkedHashMap.class);
                    if (extra.get("urgency") != null) urgency = extra.get("urgency").toString();
                    if (extra.get("consequences") != null) consequences = extra.get("consequences").toString();
                } catch (Exception ignored) {}
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", det.getId());
        result.put("diseaseName", diseaseName);
        result.put("topProbability", det.getTopProbability());
        result.put("imageUrl", rewriteImageUrl(det.getImageUrl()));
        result.put("deviceTimestamp", det.getDeviceTimestamp());
        result.put("createdAt", det.getCreatedAt());
        result.put("processed", det.getProcessed() != null && det.getProcessed());
        result.put("processedAt", det.getProcessedAt());
        result.put("treatmentImages", images);
        result.put("preventionGuide", preventionGuide);
        result.put("severityLevel", severityLevel);
        result.put("advice", advice);
        result.put("urgency", urgency);
        result.put("consequences", consequences);
        return Result.success(result);
    }

    @PostMapping("/{id}/upload-treatment-image")
    @Operation(summary = "上传防治处理图片")
    public Result<String> uploadTreatmentImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        DiseaseDetection det = diseaseDetectionMapper.selectById(id);
        if (det == null) {
            return Result.error(404, "检测记录不存在");
        }

        String url = huaweiObsClient.upload(file, "treatment");

        List<String> urls = new ArrayList<>();
        if (det.getTreatmentImages() != null) {
            try {
                urls = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        det.getTreatmentImages(),
                        com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
            } catch (Exception ignored) {}
        }
        urls.add(url);
        try {
            det.setTreatmentImages(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(urls));
        } catch (Exception e) {
            log.warn("序列化treatment_images失败: {}", e.getMessage());
        }
        diseaseDetectionMapper.updateById(det);

        // 同步上传到关联的预警消息
        try {
            List<AlertMessage> alerts = alertMessageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlertMessage>()
                    .eq(AlertMessage::getRelatedType, "detection")
                    .eq(AlertMessage::getRelatedId, id)
                    .orderByDesc(AlertMessage::getCreatedAt)
                    .last("LIMIT 1")
            );
            if (!alerts.isEmpty()) {
                AlertMessage alert = alerts.get(0);
                List<String> alertImages = new ArrayList<>();
                if (alert.getTreatmentImages() != null && !alert.getTreatmentImages().isBlank()) {
                    try {
                        alertImages = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                                alert.getTreatmentImages(),
                                com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
                    } catch (Exception ignored) {}
                }
                alertImages.add(url);
                alert.setTreatmentImages(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(alertImages));
                alertMessageMapper.updateById(alert);
            }
        } catch (Exception e) {
            log.warn("同步更新预警消息图片失败: {}", e.getMessage());
        }

        log.info("防治图片上传成功, detectionId={}, url={}", id, url);
        return Result.success("上传成功", url);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "标记病害已处理")
    public Result<Void> processDetection(
            @PathVariable Long id,
            @RequestBody ProcessDetectionDTO dto) {
        DiseaseDetection det = diseaseDetectionMapper.selectById(id);
        if (det == null) {
            return Result.error(404, "检测记录不存在");
        }

        List<String> urls = dto.getImageUrls();
        if (urls != null && !urls.isEmpty()) {
            try {
                det.setTreatmentImages(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(urls));
            } catch (Exception e) {
                log.warn("序列化treatment_images失败: {}", e.getMessage());
            }
        }

        det.setProcessed(true);
        det.setProcessedAt(LocalDateTime.now());
        diseaseDetectionMapper.updateById(det);

        // 同步更新关联的预警消息
        try {
            List<AlertMessage> alerts = alertMessageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlertMessage>()
                    .eq(AlertMessage::getRelatedType, "detection")
                    .eq(AlertMessage::getRelatedId, id)
                    .orderByDesc(AlertMessage::getCreatedAt)
                    .last("LIMIT 1")
            );
            if (!alerts.isEmpty()) {
                AlertMessage alert = alerts.get(0);
                alert.setProcessed(true);
                if (urls != null && !urls.isEmpty()) {
                    alert.setTreatmentImages(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(urls));
                }
                alertMessageMapper.updateById(alert);
            }
        } catch (Exception e) {
            log.warn("同步更新预警消息失败: {}", e.getMessage());
        }

        log.info("病害已标记处理, detectionId={}, imageCount={}", id, urls != null ? urls.size() : 0);
        return Result.success();
    }

    /** 将 OBS 直连 URL 重写为后端代理 URL */
    private String rewriteImageUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        String obsPrefix = "https://orange-86ee.obs.cn-south-1.myhuaweicloud.com";
        String proxyPrefix = "/api/images/proxy";
        if (url.startsWith(obsPrefix)) {
            return proxyPrefix + url.substring(obsPrefix.length());
        }
        return url;
    }
}
