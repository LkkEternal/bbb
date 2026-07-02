package com.citrus.push.service.impl;

import com.citrus.push.service.SmsPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsPushServiceImpl implements SmsPushService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${sms.provider-url:}")
    private String providerUrl;

    @Value("${sms.app-key:}")
    private String appKey;

    @Value("${sms.alert-template-id:}")
    private String alertTemplateId;

    @Override
    public boolean sendAlertSms(Long userId, String phone, String content, Integer level) {
        try {
            if (providerUrl == null || providerUrl.isBlank()) {
                log.warn("短信服务未配置, 跳过短信推送, userId={}", userId);
                return false;
            }

            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("phone", phone);
            body.put("templateId", alertTemplateId);
            body.put("params", Map.of(
                    "content", content,
                    "level", "L" + level
            ));

            String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(providerUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + appKey)
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                log.info("短信推送成功, userId={}, phone={}", userId, phone);
                return true;
            } else {
                log.warn("短信推送失败, status={}, userId={}", resp.statusCode(), userId);
                return false;
            }
        } catch (Exception e) {
            log.error("短信推送异常, userId={}, err={}", userId, e.getMessage());
            return false;
        }
    }
}
