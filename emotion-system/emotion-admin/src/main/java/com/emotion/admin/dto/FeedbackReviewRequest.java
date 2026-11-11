package com.emotion.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FeedbackReviewRequest {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @Pattern(regexp = "^(angry|disgust|fear|happy|neutral|sad|surprise|unknown)$",
            message = "表情标签格式不正确")
    private String correctLabel;
}
