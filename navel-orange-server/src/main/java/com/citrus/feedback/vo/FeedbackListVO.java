package com.citrus.feedback.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈列表VO
 */
@Data
public class FeedbackListVO {

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 果园名称
     */
    private String orchardName;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 反馈类型：manual_diagnosis=人工诊断, issue_report=问题上报
     */
    private String feedbackType;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 审核状态：0=待审核, 1=已采纳, 2=已修正, 3=已驳回
     */
    private Integer reviewStatus;

    /**
     * AI诊断结果（简化版）
     */
    private AiResultVO aiResult;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * AI诊断结果VO
     */
    @Data
    public static class AiResultVO {
        /**
         * 病害名称
         */
        private String diseaseName;

        /**
         * 置信度
         */
        private Double probability;
    }
}
