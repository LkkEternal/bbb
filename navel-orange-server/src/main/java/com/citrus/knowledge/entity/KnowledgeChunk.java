package com.citrus.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识分块表（使用 pgvector 向量类型）
 */
@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Integer chunkIndex;

    private String content;

    private String contentHash;

    /**
     * pgvector vector(512) 类型，通过 VectorTypeHandler 序列化/反序列化
     */
    private float[] embedding;

    private String metadata;

    private Integer tokenCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
