package com.citrus.collector.controller;

import com.citrus.collector.dto.ProcessDetectionDTO;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.common.result.Result;
import com.citrus.common.util.HuaweiObsClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
@Tag(name = "病害处理", description = "病害识别记录的处理与证据上传")
public class DetectionProcessController {

    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final HuaweiObsClient huaweiObsClient;

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

        log.info("病害已标记处理, detectionId={}, imageCount={}", id, urls != null ? urls.size() : 0);
        return Result.success();
    }
}
