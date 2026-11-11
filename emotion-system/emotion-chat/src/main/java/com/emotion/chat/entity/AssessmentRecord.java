package com.emotion.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 心理测评记录实体类
 * 存储用户完成的测评信息
 *
 * 字段说明：
 * - id: 主键ID
 * - userId: 用户ID
 * - questionnaireId: 问卷ID
 * - totalScore: 总得分
 * - levelCode: 等级代码
 * - levelName: 等级名称
 * - adviceText: 心理建议文本
 * - status: 状态（completed/abandoned）
 * - startedAt: 开始时间
 * - completedAt: 完成时间
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("assessment_record")
public class AssessmentRecord {

    /** 主键ID */
    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 问卷ID */
    private Long questionnaireId;

    /** 总得分 */
    private Integer totalScore;

    /** 等级代码 */
    private String levelCode;

    /** 等级名称 */
    private String levelName;

    /** 心理建议文本 */
    private String adviceText;

    /** 状态（completed/abandoned） */
    private String status;

    /** 开始时间 */
    private java.time.LocalDateTime startedAt;

    /** 完成时间 */
    private java.time.LocalDateTime completedAt;
}
