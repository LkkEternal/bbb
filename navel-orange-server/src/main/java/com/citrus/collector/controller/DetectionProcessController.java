package com.citrus.collector.controller;

import com.citrus.collector.dto.ProcessDetectionDTO;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.common.result.Result;
import com.citrus.common.util.HuaweiObsClient;
import com.citrus.knowledge.service.RagService;
import com.citrus.knowledge.vo.RagResponse;
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
    private final RagService ragService;
    private final com.citrus.collector.mapper.EnvironmentDataMapper environmentDataMapper;
    private final com.citrus.calendar.mapper.FarmingCalendarMapper farmingCalendarMapper;

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
        String symptomDesc = null;
        String diseaseNameEn = null;
        String diseaseType = null;
        String category = null;
        if (det.getDiseaseCatalogId() != null) {
            DiseaseCatalog catalog = diseaseCatalogMapper.selectById(det.getDiseaseCatalogId());
            if (catalog != null) {
                if (catalog.getDiseaseName() != null) {
                    diseaseName = catalog.getDiseaseName();
                }
                preventionGuide = catalog.getPreventionGuide();
                severityLevel = catalog.getSeverityLevel();
                symptomDesc = catalog.getSymptomDesc();
                diseaseNameEn = catalog.getDiseaseNameEn();
                diseaseType = catalog.getDiseaseType();
                category = catalog.getCategory();
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

        // 从 extra_data 读取 RAG 分析结果（事件处理器在记录创建时已写入）
        String ragAnswer = null;
        Double ragConfidence = null;
        String ragSources = null;
        String ragUrgency = null;
        String ragTreatment = null;
        String ragConsequences = null;
        String ragPrevention = null;
        try {
            if (det.getExtraData() != null && !det.getExtraData().isEmpty()) {
                var om = new com.fasterxml.jackson.databind.ObjectMapper();
                var extra = om.readValue(det.getExtraData(), java.util.Map.class);
                if (extra.containsKey("ragAnswer")) ragAnswer = (String) extra.get("ragAnswer");
                if (extra.containsKey("ragConfidence")) ragConfidence = om.convertValue(extra.get("ragConfidence"), Double.class);
                if (extra.containsKey("ragSources")) ragSources = (String) extra.get("ragSources");
                if (extra.containsKey("ragUrgency")) ragUrgency = (String) extra.get("ragUrgency");
                if (extra.containsKey("ragTreatment")) ragTreatment = (String) extra.get("ragTreatment");
                if (extra.containsKey("ragConsequences")) ragConsequences = (String) extra.get("ragConsequences");
                if (extra.containsKey("ragPrevention")) ragPrevention = (String) extra.get("ragPrevention");
            }
        } catch (Exception e) {
            log.warn("读取 RAG 结果异常: {}", e.getMessage());
        }
        // 如果 extra_data 中没有 RAG 结果（旧记录），调用 RAG 实时查询
        if (ragAnswer == null) {
            try {
                Double temp = null;
                Double humidity = null;
                if (det.getOrchardId() != null) {
                    var env = environmentDataMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.citrus.collector.entity.EnvironmentData>()
                                    .eq(com.citrus.collector.entity.EnvironmentData::getOrchardId, det.getOrchardId())
                                    .orderByDesc(com.citrus.collector.entity.EnvironmentData::getRecordTime)
                                    .last("LIMIT 1"));
                    if (env != null) {
                        temp = env.getTemp() != null ? env.getTemp().doubleValue() : null;
                        humidity = env.getHumidity() != null ? env.getHumidity().doubleValue() : null;
                    }
                }
                String phenology = null;
                var cals = farmingCalendarMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.citrus.calendar.entity.FarmingCalendar>()
                                .eq(com.citrus.calendar.entity.FarmingCalendar::getEnabled, true)
                                .orderByAsc(com.citrus.calendar.entity.FarmingCalendar::getSortOrder));
                if (cals != null && !cals.isEmpty()) {
                    int month = java.time.LocalDate.now().getMonthValue();
                    int day = java.time.LocalDate.now().getDayOfMonth();
                    for (var cal : cals) {
                        int now = month * 100 + day;
                        int start = cal.getStartMonth() * 100 + cal.getStartDay();
                        int end = cal.getEndMonth() * 100 + cal.getEndDay();
                        boolean inPeriod = start <= end ? (now >= start && now <= end) : (now >= start || now <= end);
                        if (inPeriod) {
                            phenology = cal.getPhenologyName();
                            break;
                        }
                    }
                }
                RagResponse ragResp = ragService.queryForDetection(diseaseName, temp, humidity, phenology);
                if (ragResp != null && ragResp.getAnswer() != null) {
                    ragAnswer = ragResp.getAnswer();
                    ragConfidence = ragResp.getConfidence();
                    if (ragResp.getReferences() != null) {
                        ragSources = String.join("; ", ragResp.getReferences().stream()
                                .map(r -> r.getDocumentTitle() != null ? r.getDocumentTitle() : "")
                                .filter(s -> !s.isEmpty()).distinct().collect(java.util.stream.Collectors.toList()));
                    }
                    ragUrgency = extractSection(ragAnswer, "【紧急程度】");
                    ragTreatment = extractSection(ragAnswer, "【防治措施】");
                    ragConsequences = extractSection(ragAnswer, "【不处理的后果】");
                    ragPrevention = extractSection(ragAnswer, "【预防措施】");
                }
            } catch (Exception e) {
                log.warn("RAG 实时查询异常: {}", e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", det.getId());
        result.put("topClassCode", det.getTopClassCode());
        result.put("diseaseName", diseaseName);
        result.put("diseaseNameEn", diseaseNameEn);
        result.put("diseaseType", diseaseType);
        result.put("category", category);
        result.put("topProbability", det.getTopProbability());
        result.put("imageUrl", rewriteImageUrl(det.getImageUrl()));
        result.put("deviceTimestamp", det.getDeviceTimestamp());
        result.put("createdAt", det.getCreatedAt());
        result.put("severityLevel", severityLevel);
        result.put("symptomDesc", symptomDesc);
        result.put("preventionGuide", preventionGuide);
        result.put("processed", det.getProcessed());
        result.put("processedAt", det.getProcessedAt());
        result.put("treatmentImages", images);
        // RAG 结果
        result.put("ragAnswer", ragAnswer);
        result.put("ragConfidence", ragConfidence);
        result.put("ragSources", ragSources);
        result.put("ragUrgency", ragUrgency);
        result.put("ragTreatment", ragTreatment);
        result.put("ragConsequences", ragConsequences);
        result.put("ragPrevention", ragPrevention);
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

    /** 从RAG回答中提取指定段落 */
    private String extractSection(String text, String section) {
        if (text == null) return null;
        int start = text.indexOf(section);
        if (start < 0) return null;
        start += section.length();
        int end = text.indexOf("【", start);
        if (end < 0) end = text.length();
        String result = text.substring(start, end).trim();
        return result.isEmpty() ? null : result;
    }
}
