package com.citrus.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * RAG 问答反馈表
 */
@Data
@TableName("rag_feedback")
public class RagFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private String question;

    private String answer;

    private String model;

    private Double similarity;

    /**
     * 反馈类型：0=无反馈 1=有用 2=没用
     */
    private Integer feedbackType;

    private Long userId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
