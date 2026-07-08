package com.citrus.config;

import com.citrus.common.util.HuaweiObsClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OBS 图片代理 Controller
 * 前端图片不走 OBS 域名，改为走后端代理，避免强制下载问题
 *
 * 使用方式：
 *   前端 url-prefix 改为 /api/images/proxy
 *   设备上报的 imageUrl 变为 /api/images/proxy/disease_records/xxx.jpg
 *   后端从 OBS 下载原图并返回（带正确的 Content-Type 头）
 */
@Slf4j
@RestController
@RequestMapping("/api/images/proxy")
@RequiredArgsConstructor
public class ImageProxyController {

    private final HuaweiObsClient huaweiObsClient;

    @GetMapping("/**")
    public ResponseEntity<byte[]> proxyImage(HttpServletRequest request) {
        // 提取对象路径：/api/images/proxy/disease_records/xxx.jpg → disease_records/xxx.jpg
        String fullPath = request.getRequestURI();
        String prefix = "/api/images/proxy/";
        String objectName = fullPath;
        if (objectName.startsWith(prefix)) {
            objectName = objectName.substring(prefix.length());
        }

        if (objectName == null || objectName.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            HuaweiObsClient.ObsObjectContent obj = huaweiObsClient.getObject(objectName);
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(obj.getContentType());
            } catch (Exception e) {
                mediaType = guessMediaType(objectName);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")  // 浏览器直接预览，不下载
                    .body(obj.getData());
        } catch (Exception e) {
            log.warn("OBS代理图片失败: {} - {}", objectName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType guessMediaType(String name) {
        if (name == null) return MediaType.APPLICATION_OCTET_STREAM;
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.valueOf("image/webp");
        if (lower.endsWith(".mp4")) return MediaType.valueOf("video/mp4");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
