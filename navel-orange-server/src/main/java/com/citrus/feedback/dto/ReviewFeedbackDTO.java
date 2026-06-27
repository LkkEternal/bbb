package com.citrus.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 审核反馈请求参数
 */
@Data
public class ReviewFeedbackDTO {

    /**
     * 审核操作：accept=采纳, revise=修正, reject=拒绝
     */
    @NotBlank(message = "审核操作不能为空")
    @Pattern(regexp = "^(accept|revise|reject)$", message = "审核操作只能是 accept/revise/reject")
    private String action;

    /**
     * 审核意见
     */
    private String comment;

    /**
     * 修正后的病害名称（action=revise时必填）
     */
    private String correctedDisease;
}
