package com.emotion.detection.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 情感检测记录实体类
 * 存储用户的每一次情感检测结果
 *
 * 字段说明：
 * - id: 主键ID
 * - userId: 用户ID
 * - imageUrl: 图片URL（七牛云）
 * - objectKey: 七牛云对象存储key
 * - emotionLabel: 情感英文标签（angry、happy等）
 * - emotionCn: 情感中文标签（生气、开心等）
 * - confidence: 置信度（0.0-1.0）
 * - faceCount: 检测到的人脸总数
 * - validFaceCount: 有效人脸数（未跳过）
 * - detectDetail: 检测详情（JSON格式，包含所有人脸的详细结果）
 * - adviceText: 情感建议文本
 * - adviceSource: 建议来源（dict/ai等）
 * - adviceSourceType: 建议类型（none/dict/ai）
 * - processingTimeMs: 处理耗时（毫秒）
 * - createdAt: 创建时间
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName(value = "detection_record", autoResultMap = true)
public class DetectionRecord {

    /** 主键ID */
    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 图片URL（七牛云） */
    private String imageUrl;

    /** 七牛云对象存储key */
    private String objectKey;

    /** 情感英文标签（angry、happy、sad等） */
    private String emotionLabel;

    /** 情感中文标签（生气、开心、悲伤等） */
    private String emotionCn;

    /** 置信度（0.0-1.0） */
    private BigDecimal confidence;

    /** 检测到的人脸总数 */
    private Integer faceCount;

    /** 有效人脸数（未跳过） */
    private Integer validFaceCount;

    /** 性别（前端本地模型检测，男/女） */
    private String modelGender;

    /** 年龄（前端本地模型估算） */
    private Integer modelAge;

    /** 年龄段（前端本地模型：儿童/青少年/青年/中年/老年） */
    private String modelRange;

    /** 检测详情（JSON格式，包含所有人脸的详细结果） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> detectDetail;

    /** 情感建议文本 */
    private String adviceText;

    /** 建议来源（dict/ai等） */
    private String adviceSource;

    /** 建议类型（none/dict/ai） */
    private String adviceSourceType;

    /** 处理耗时（毫秒） */
    private Integer processingTimeMs;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
