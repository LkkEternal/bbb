package com.citrus.push.service.impl;

import com.citrus.push.service.WechatPushService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPushServiceImpl implements WechatPushService {

    private final ObjectMapper objectMapper;
    private HttpClient httpClient;

    {
        initHttpClient();
    }

    private void initHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // 包级可见，仅用于测试注入
    void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    @Value("${wechat.template.alert:}")
    private String alertTemplateId;

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String SUBSCRIBE_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    @Override
    public String getAccessToken() {
        try {
            String url = TOKEN_URL + "?grant_type=client_credential&appid=" + appid + "&secret=" + secret;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(resp.body());
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {
                log.error("获取微信access_token失败: {}", root);
                return null;
            }
            return root.get("access_token").asText();
        } catch (Exception e) {
            log.error("获取微信access_token异常: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean sendAlertNotification(Long messageId, Long userId, String title,
                                          String content, String advice, Integer level, String openid) {
        try {
            String token = getAccessToken();
            if (token == null) {
                log.warn("推送失败，无法获取access_token, messageId={}", messageId);
                return false;
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("touser", openid);
            body.put("template_id", alertTemplateId);
            body.put("page", "pages/alert/detail?id=" + messageId);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("thing1", Map.of("value", truncate(title, 20)));
            data.put("thing2", Map.of("value", truncate(content, 20)));
            data.put("thing3", Map.of("value", truncate(advice != null ? advice : "请及时查看", 20)));
            data.put("phrase4", Map.of("value", "L" + level));
            data.put("date3", Map.of("value", new java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm")
                    .format(new java.util.Date())));
            body.put("data", data);

            String jsonBody = objectMapper.writeValueAsString(body);
            String url = SUBSCRIBE_SEND_URL + "?access_token=" + token;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(resp.body());
            int errcode = root.has("errcode") ? root.get("errcode").asInt() : -1;
            if (errcode == 0) {
                log.info("微信推送成功, messageId={}, openid={}", messageId, openid);
                return true;
            } else if (errcode == 43101) {
                log.warn("用户拒收小程序订阅消息, messageId={}, openid={}", messageId, openid);
                return false;
            } else {
                log.error("微信推送失败, errcode={}, message={}", errcode, root);
                return false;
            }
        } catch (Exception e) {
            log.error("微信推送异常, messageId={}, err={}", messageId, e.getMessage());
            return false;
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
