package com.citrus.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 提交反馈请求参数
 */
@Data
public class FeedbackSubmitDTO {

    /**
     * 果园ID
     */
    @NotNull(message = "果园ID不能为空")
    private Long orchardId;

    /**
     * 上传图片文件数组（最多5张，每张最大10MB）
     */
    @NotNull(message = "请至少上传一张图片")
    private MultipartFile[] images;

    /**
     * 问题描述，最长500字
     */
    @Size(max = 500, message = "问题描述不能超过500字")
    private String description;

    /**
     * 反馈类型：manual_diagnosis=人工诊断, issue_report=问题上报
     */
    @NotBlank(message = "反馈类型不能为空")
    private String feedbackType;
}
