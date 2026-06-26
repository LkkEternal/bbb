package com.citrus.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citrus.common.result.Result;

import java.util.Map;

/**
 * 系统配置接口
 */
@Tag(name = "M0-系统配置", description = "前端运行时配置")
@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    private final VideoStreamProperties videoStreamProperties;

    public SystemConfigController(VideoStreamProperties videoStreamProperties) {
        this.videoStreamProperties = videoStreamProperties;
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

    @Data
    @Component
    @ConfigurationProperties(prefix = "video.stream")
    public static class VideoStreamProperties {
        private String host = "localhost";
        private int wsPort = 8001;
        private int httpPort = 8000;
    }
}
