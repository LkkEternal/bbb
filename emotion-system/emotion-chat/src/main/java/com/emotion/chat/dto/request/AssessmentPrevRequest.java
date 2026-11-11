package com.emotion.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentPrevRequest {

    @NotNull(message = "评估记录ID不能为空")
    private Long recordId;
}
