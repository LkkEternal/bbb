package com.emotion.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("assessment_question")
public class AssessmentQuestion {

    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionnaireId;

    private Integer questionIndex;

    private String questionText;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private Integer scoreA;

    private Integer scoreB;

    private Integer scoreC;

    private Integer scoreD;

    private Integer isActive;

    private java.time.LocalDateTime createdAt;
}
