package com.emotion.admin.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmotionDictUpdateRequest {

    @Size(max = 2000, message = "兜底建议内容不能超过2000字")
    private String defaultAdvice;

    private Boolean isActive;
}
