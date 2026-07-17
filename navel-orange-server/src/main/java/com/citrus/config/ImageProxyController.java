package com.citrus.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * OBS 图片代理 Controller
 * 前端图片不走 OBS 域名，改为走后端代理
 *
 * 直接从华为云 OBS 公共读 URL 获取图片并返回
 */
@Slf4j
@RestController
@RequestMapping("/api/images/proxy")
public class ImageProxyController {

    private static final String OBS_BASE_URL = "https://orange-86e.obs.cn-south-1.myhuaweicloud.com/";

    @GetMapping("/**")
    public void proxyImage(HttpServletRequest request, HttpServletResponse response) {
        // 提取对象路径
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

        String obsUrl = OBS_BASE_URL + imagePath;
        log.debug("OBS代理: {} -> {}", imagePath, obsUrl);

        try {
            URL url = new URL(obsUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            int status = conn.getResponseCode();
            if (status != 200) {
                log.warn("OBS返回非200: {} for {}", status, obsUrl);
                response.setStatus(404);
                return;
            }

            // 设置响应头
            String contentType = conn.getContentType();
            if (contentType == null) {
                if (imagePath.toLowerCase().endsWith(".jpg") || imagePath.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (imagePath.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else {
                    contentType = "application/octet-stream";
                }
            }
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=86400");

            try (InputStream in = conn.getInputStream();
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
