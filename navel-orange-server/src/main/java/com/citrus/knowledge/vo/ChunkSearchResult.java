package com.citrus.knowledge.vo;

import lombok.Data;

/**
 * pgvector 向量检索结果
 */
@Data
public class ChunkSearchResult {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private Integer chunkIndex;
    private String content;
    private String metadata;
    private Double similarity;
}
