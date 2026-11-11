package com.emotion.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentStartRequest {

    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;
}
