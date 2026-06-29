package com.citrus.feedback.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员端反馈列表VO
 */
@Data
public class AdminFeedbackListVO {

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 果园ID
     */
    private Long orchardId;

    /**
     * 果园名称
     */
    private String orchardName;

    /**
     * 图片URL（第一个图片，用于列表展示）
     */
    private String imageUrl;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 反馈类型
     */
    private String feedbackType;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 状态（前端期望的字段名）
     */
    private Integer status;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 训练集标签
     */
    private String datasetTag;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
