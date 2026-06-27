package com.citrus.feedback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.citrus.config.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 * 对应数据库表：user_feedback
 */
@Data
@TableName(value = "user_feedback", autoResultMap = true)
public class UserFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提交用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 果园ID
     */
    @TableField("orchard_id")
    private Long orchardId;

    /**
     * 图片URL数组（JSONB格式）
     */
    @TableField(value = "image_urls", typeHandler = JsonbTypeHandler.class)
    private String imageUrls;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 反馈类型：manual_diagnosis=人工诊断, issue_report=问题上报
     */
    @TableField("feedback_type")
    private String feedbackType;

    /**
     * 审核状态：0=待审核, 1=已采纳, 2=已修正, 3=已驳回
     */
    @TableField("review_status")
    private Integer reviewStatus;

    /**
     * 审核人ID
     */
    @TableField("reviewed_by")
    private Long reviewedBy;

    /**
     * 审核时间
     */
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 审核意见
     */
    @TableField("review_comment")
    private String reviewComment;

    /**
     * 训练集标签
     */
    @TableField("dataset_tag")
    private String datasetTag;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
