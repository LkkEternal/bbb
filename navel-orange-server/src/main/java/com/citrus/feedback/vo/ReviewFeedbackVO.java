package com.citrus.feedback.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核反馈响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFeedbackVO {

    /**
     * 反馈ID
     */
    private Long feedbackId;

    /**
     * 审核状态：1=已采纳, 2=已修正, 3=已驳回
     */
    private Integer reviewStatus;

    /**
     * 是否纳入训练集
     */
    private Boolean datasetIncluded;
}
