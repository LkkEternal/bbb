package com.citrus.config;

import com.citrus.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置接口
 */
@Tag(name = "M0-系统配置", description = "前端运行时配置")
@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    private final VideoStreamProperties videoStreamProperties;
    private final JdbcTemplate jdbcTemplate;

    public SystemConfigController(VideoStreamProperties videoStreamProperties, JdbcTemplate jdbcTemplate) {
        this.videoStreamProperties = videoStreamProperties;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Operation(summary = "获取视频流配置")
    @GetMapping("/video")
    public Result<Map<String, Object>> getVideoConfig() {
        return Result.success(Map.of(
                "host", videoStreamProperties.getHost(),
                "wsPort", videoStreamProperties.getWsPort(),
                "httpPort", videoStreamProperties.getHttpPort()
        ));
    }

    /**
     * 修复数据库中图片URL — 将 test.jpg / test_now.jpg 替换为 OBS 实际图片
     */
    @Operation(summary = "修复图片URL（测试数据→真实图片）")
    @GetMapping("/fix-images")
    public Result<Map<String, Object>> fixImageUrls() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 1. 修正影子图片与实际OBS图片不匹配的问题
        int fixed = jdbcTemplate.update(
            "UPDATE disease_detection SET image_url = '/api/images/proxy/disease_records/citrus_1783496652.jpg' WHERE id = 37");
        fixed += jdbcTemplate.update(
            "UPDATE disease_detection SET image_url = '/api/images/proxy/disease_records/citrus_1783496719.jpg' WHERE id = 40");
        result.put("detection_fixed", fixed);
        int cleaned = jdbcTemplate.update(
            "UPDATE disease_detection SET image_url = NULL WHERE image_url NOT LIKE '/api/images/proxy/%' AND image_url IS NOT NULL AND image_url NOT LIKE 'http%orange-86e%'");
        result.put("detection_cleaned", cleaned);

        // 3. 查询剩余记录
        List<Map<String, Object>> detections = jdbcTemplate.queryForList(
            "SELECT id, image_url FROM disease_detection ORDER BY id");
        result.put("detections", detections);

        return Result.success(result);
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "video.stream")
    public static class VideoStreamProperties {
        private String host = "localhost";
        private int wsPort = 8001;
        private int httpPort = 8000;
    }
}
