package com.citrus.knowledge.config;

import com.citrus.knowledge.dto.RagQueryDTO;
import com.citrus.knowledge.vo.RagResponse;
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
import java.util.List;
import java.util.Map;

/**
 * Python RAG 服务 HTTP 客户端
 * <p>
 * 负责调用 navel-orange-rag 服务提供的 /embed、/chat、/process-document 接口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonRagClient {

    private final RagProperties properties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ── Embedding ─────────────────────────────────────────

    /**
     * 调用 Python 服务进行文本向量化
     *
     * @param text 输入文本
     * @return 512 维 float 数组
     */
    public float[] embed(String text) {
        try {
            String url = properties.getBaseUrl() + "/embed";
            String body = objectMapper.writeValueAsString(Map.of("texts", List.of(text)));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(properties.getTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("Embedding service returned " + resp.statusCode() + ": " + resp.body());
            }

            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode embArray = root.get("embeddings").get(0);
            float[] result = new float[embArray.size()];
            for (int i = 0; i < embArray.size(); i++) {
                result[i] = (float) embArray.get(i).asDouble();
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to call embedding service: {}", e.getMessage());
            throw new RuntimeException("Embedding service unavailable", e);
        }
    }

    /**
     * 批量向量化
     */
    public List<float[]> embedBatch(List<String> texts) {
        try {
            String url = properties.getBaseUrl() + "/embed";
            String body = objectMapper.writeValueAsString(Map.of("texts", texts));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(properties.getTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("Embedding service returned " + resp.statusCode());
            }

            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode embsNode = root.get("embeddings");
            return objectMapper.readValue(
                    embsNode.traverse(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, float[].class)
            );
        } catch (Exception e) {
            log.error("Failed to call batch embedding service: {}", e.getMessage());
            throw new RuntimeException("Embedding service unavailable", e);
        }
    }

    // ── LLM Chat ──────────────────────────────────────────

    /**
     * 调用 LLM 生成回答（Python 侧内置了 DeepSeek→Qwen 自动降级）
     *
     * @param messages  OpenAI 格式的消息列表
     * @param model     模型名: deepseek / qwen-local，null 则使用配置默认
     * @return (回答内容, 实际模型名)
     */
    public ChatResponse chat(List<Map<String, String>> messages, String model) {
        try {
            String url = properties.getBaseUrl() + "/chat";
            Map<String, Object> requestBody = Map.of(
                    "messages", messages,
                    "model", model != null ? model : properties.getDefaultModel(),
                    "stream", false
            );

            String body = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(properties.getTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("Chat service returned " + resp.statusCode() + ": " + resp.body());
            }

            JsonNode root = objectMapper.readTree(resp.body());
            String content = root.get("content").asText();
            String actualModel = root.has("model") ? root.get("model").asText() : model;

            return new ChatResponse(content, actualModel);
        } catch (Exception e) {
            log.error("Failed to call chat service: {}", e.getMessage());
            throw new RuntimeException("LLM service unavailable", e);
        }
    }

    // ── Document Processing ───────────────────────────────

    /**
     * 调用文档处理接口
     *
     * @param fileUrl  文件 URL（阿里云 OSS）
     * @param fileType pdf / docx / txt
     * @return 处理结果 JSON 字符串
     */
    public String processDocument(String fileUrl, String fileType, Long documentId) {
        try {
            String url = properties.getBaseUrl() + "/process-document";
            Map<String, Object> reqBody = Map.of(
                    "file_url", fileUrl,
                    "file_type", fileType,
                    "document_id", documentId != null ? documentId : 0
            );
            String body = objectMapper.writeValueAsString(reqBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(properties.getTimeout() * 3))  // 文档处理可能较慢
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("Document processing returned " + resp.statusCode() + ": " + resp.body());
            }
            return resp.body();
        } catch (Exception e) {
            log.error("Failed to process document: {}", e.getMessage());
            throw new RuntimeException("Document processing service unavailable", e);
        }
    }

    // ── Health ────────────────────────────────────────────

    public boolean health() {
        try {
            String url = properties.getBaseUrl() + "/health";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Inner Types ───────────────────────────────────────

    public record ChatResponse(String content, String model) {
    }
}
