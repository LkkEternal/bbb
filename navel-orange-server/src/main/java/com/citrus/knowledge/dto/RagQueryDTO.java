package com.citrus.knowledge.dto;

import lombok.Data;

/**
 * RAG 问答请求
 */
@Data
public class RagQueryDTO {
    private String question;
    private String diseaseName;
    private Long orchardId;

    /** 可选：指定 LLM 模型 deepseek / qwen-local */
    private String model;
}
