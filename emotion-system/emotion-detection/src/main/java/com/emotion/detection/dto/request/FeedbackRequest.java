package com.emotion.detection.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {

    private Long recordId;

    @NotBlank(message = "correctLabel不能为空")
    @Pattern(regexp = "^(angry|disgust|fear|happy|neutral|sad|surprise|unknown)$",
            message = "correctLabel必须是有效的表情标签")
    private String correctLabel;

    @Size(max = 500, message = "补充描述最长500字")
    private String description;
}