package com.citrus.prediction.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * 预测服务 HTTP 客户端
 * <p>
 * 调用 navel-orange-rag 服务的 /api/prediction/evaluate 接口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PredictionClient {

    private final PredictionProperties properties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 评估单个果园的风险
     *
     * @param orchardId 果园 ID
     * @return 预测结果 Map，包含 riskLevel / probabilities / llmAnalysis 等字段
     */
    public Map<String, Object> evaluateOrchard(Long orchardId) {
        String url = properties.getBaseUrl() + "/api/prediction/evaluate?orchard_id=" + orchardId;
        try {
            log.debug("Prediction request: orchardId={}, url={}", orchardId, url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(properties.getTimeout()))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> resp = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8)
            );

            if (resp.statusCode() != 200) {
                log.warn("Prediction failed: status={}, body={}", resp.statusCode(), resp.body());
                return fallback(orchardId);
            }

            JsonNode root = objectMapper.readTree(resp.body());
            return objectMapper.convertValue(root, new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            log.error("Prediction HTTP error for orchard {}: {}", orchardId, e.getMessage());
            return fallback(orchardId);
        }
    }

    /**
     * 降级方案：RAG 服务不可用时返回默认结果
     */
    private Map<String, Object> fallback(Long orchardId) {
        return Map.of(
                "orchard_id", orchardId,
                "risk_level", 0,
                "risk_label", "评估失败",
                "probabilities", java.util.List.of(1.0, 0.0, 0.0, 0.0),
                "suggestion", "预测服务暂不可用，请稍后重试",
                "llm_analysis", "预测服务暂不可用",
                "top_factors", java.util.List.of()
        );
    }
}
