package com.citrus.config;

import com.citrus.common.util.HuaweiObsClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OBS 图片代理 Controller
 * 使用华为云 OBS SDK 鉴权获取图片
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images/proxy")
public class ImageProxyController {

    private final HuaweiObsClient huaweiObsClient;

    @GetMapping("/**")
    public void proxyImage(HttpServletRequest request, HttpServletResponse response) {
        String fullPath = request.getRequestURI();
        String prefix = "/api/images/proxy/";
        String imagePath = fullPath;
        if (imagePath.startsWith(prefix)) {
            imagePath = imagePath.substring(prefix.length());
        }
        if (imagePath == null || imagePath.isEmpty()) {
            response.setStatus(400);
            return;
        }

        log.debug("OBS代理: {} (SDK鉴权)", imagePath);
        try {
            HuaweiObsClient.ObsObjectContent obj = huaweiObsClient.getObject(imagePath);
            if (obj == null) {
                response.setStatus(404);
                return;
            }

            response.setContentType(obj.getContentType());
            response.setHeader("Cache-Control", "public, max-age=86400");

            try (InputStream in = new ByteArrayInputStream(obj.getData());
                 OutputStream out = response.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.flush();
            }
        } catch (Exception e) {
            log.warn("OBS代理图片失败: {} - {}", imagePath, e.getMessage());
            response.setStatus(404);
        }
    }
}
