package com.emotion.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("assessment_grade_rule")
public class AssessmentGradeRule {

    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionnaireId;

    private Integer minScore;

    private Integer maxScore;

    private String levelCode;

    private String levelName;

    private String adviceText;

    private Integer sortOrder;

    private Integer isActive;

    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}
