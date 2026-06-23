package com.citrus.knowledge.dto;

import lombok.Data;

/**
 * 知识库环境上下文（用于构建检索 query）
 */
@Data
public class KnowledgeContextDTO {
    private String diseaseName;
    private Double temperature;
    private Double humidity;
    private String phenologyName;
    private String solarTerm;
    private String scenario;  // prevention / diagnosis / phenology
}
