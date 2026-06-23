package com.citrus.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * RAG 问答反馈请求
 */
@Data
public class RagFeedbackDTO {
    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    @NotNull(message = "反馈类型不能为空")
    private Integer feedbackType;  // 1=有用 2=没用

    private String remark;
}
