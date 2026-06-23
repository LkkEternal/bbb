package com.citrus.knowledge.dto;

import lombok.Data;

/**
 * 知识库检索请求
 */
@Data
public class KnowledgeSearchDTO {
    private String query;
    private String diseaseName;
    private Long orchardId;
}
