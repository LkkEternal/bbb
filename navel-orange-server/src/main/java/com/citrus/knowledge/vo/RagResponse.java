package com.citrus.knowledge.vo;

import lombok.Data;

import java.util.List;

/**
 * RAG 问答响应
 */
@Data
public class RagResponse {
    private String question;
    private String answer;
    private List<ChunkSearchResult> references;
    private Double confidence;
    private String model;

    /** 会话 ID，用于后续反馈追踪 */
    private String sessionId;
}
