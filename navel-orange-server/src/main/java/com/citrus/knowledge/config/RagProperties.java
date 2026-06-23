package com.citrus.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG 服务配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /** Python RAG 服务地址 */
    private String baseUrl = "http://localhost:8001";

    /** HTTP 调用超时（毫秒） */
    private int timeout = 30000;

    /** 连接超时（毫秒） */
    private int connectTimeout = 10000;

    /** 默认 LLM 模型: deepseek / qwen-local */
    private String defaultModel = "deepseek";

    /** 向量检索最低相似度 */
    private double minScore = 0.60;

    /** 向量检索 fuzzy 最低相似度（高于此分数才走 LLM 生成） */
    private double fuzzyMinScore = 0.75;

    /** 检索返回 topK */
    private int topK = 10;

    /** 是否在 Spring Boot 启动时自动拉起 Python RAG 服务 */
    private boolean autoStart = true;

    /** Python RAG 项目目录路径（用于自动启动时定位 navel-orange-rag 目录） */
    private String ragDir = "";
}
