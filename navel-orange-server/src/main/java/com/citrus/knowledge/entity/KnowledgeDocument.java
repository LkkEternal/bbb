package com.citrus.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文献表
 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private String contentText;

    private String category;

    private String source;

    private Long sourceId;

    private Integer chunkCount;

    private String embeddingModel;

    /**
     * 状态：0=待处理 1=处理中 2=已完成 3=处理失败
     */
    private Integer status;

    /**
     * 审核状态：0=待审核 1=通过 2=驳回（仅来源=USER_FEEDBACK时有意义）
     */
    private Integer reviewStatus;

    private String reviewComment;

    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    private String errorMessage;

    private Long uploadedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
