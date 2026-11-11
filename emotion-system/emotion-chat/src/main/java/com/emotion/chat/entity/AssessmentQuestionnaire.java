package com.emotion.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("assessment_questionnaire")
public class AssessmentQuestionnaire {

    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String description;

    private Integer totalQuestions;

    private Integer isActive;

    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}
