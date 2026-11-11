package com.emotion.chat.dto.response;

import lombok.Data;

@Data
public class AssessmentDetailResponse {

    private Long recordId;

    private Long questionnaireId;

    private String questionnaireName;

    private String questionnaireCode;

    private Integer totalScore;

    private String levelCode;

    private String levelName;

    private String advice;

    private String startedAt;

    private String completedAt;
}
