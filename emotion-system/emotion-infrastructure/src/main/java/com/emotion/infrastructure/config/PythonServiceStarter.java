package com.emotion.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
@ConditionalOnProperty(name = "python.service.auto-start", havingValue = "true")
public class PythonServiceStarter {

    @Value("${python.service.path:}")
    private String pythonServicePath;

    @Value("${python.service.url:http://localhost:8001}")
    private String pythonServiceUrl;

    private Process process;

    @PostConstruct
    public void start() {
        if (pythonServicePath == null || pythonServicePath.isEmpty()) {
            log.warn("[PythonStarter] python.service.path 未配置，跳过自动启动");
            return;
        }

        log.info("[PythonStarter] 正在启动 Python 推理服务...");
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "-m", "app.main");
            pb.directory(new java.io.File(pythonServicePath));
            pb.redirectErrorStream(true);
            process = pb.start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[Python] {}", line);
                    }
                } catch (Exception e) {
                    log.error("[PythonStarter] 读取输出失败", e);
                }
            }, "python-output").start();

            waitForReady();
        } catch (Exception e) {
            log.error("[PythonStarter] 启动失败", e);
        }
    }

    private void waitForReady() {
        String healthUrl = pythonServiceUrl + "/api/inference/health";
        int maxWait = 120;
        int waited = 0;

        log.info("[PythonStarter] 等待模型加载完成...");

        while (waited < maxWait) {
            try {
                Thread.sleep(2000);
                waited += 2;

                HttpURLConnection conn = (HttpURLConnection) new URL(healthUrl).openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                int code = conn.getResponseCode();
                conn.disconnect();

                if (code == 200) {
                    log.info("[PythonStarter] Python 推理服务启动成功 ({}s)", waited);
                    return;
                }
            } catch (Exception e) {
                // 继续等待
            }
            log.info("[PythonStarter] 等待中... ({}/{}s)", waited, maxWait);
        }

        log.warn("[PythonStarter] 等待超时，Python 服务可能仍在加载中");
    }

    @PreDestroy
    public void stop() {
        if (process != null && process.isAlive()) {
            log.info("[PythonStarter] 正在关闭 Python 推理服务...");
            process.destroyForcibly();
            log.info("[PythonStarter] Python 服务已关闭");
        }
    }
}
