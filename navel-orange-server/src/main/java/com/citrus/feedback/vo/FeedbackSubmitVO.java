package com.citrus.feedback.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交反馈响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSubmitVO {

    /**
     * 反馈ID
     */
    private Long feedbackId;

    /**
     * 状态：pending_quiz=待完成问卷, pending_review=待审核
     */
    private String status;
}
