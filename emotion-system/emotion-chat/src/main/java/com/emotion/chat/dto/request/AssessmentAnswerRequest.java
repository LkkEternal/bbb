package com.emotion.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AssessmentAnswerRequest {

    @NotNull(message = "评估记录ID不能为空")
    private Long recordId;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotBlank(message = "请选择答案")
    @Pattern(regexp = "^[ABCD]$", message = "答案必须为 A/B/C/D")
    private String selectedOption;
}
