package com.emotion.chat.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireDetailResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer totalQuestions;
    private Integer isActive;
    private String createdAt;
    private String updatedAt;
    private List<QuestionDetail> questions;
    private List<GradeRuleDetail> gradeRules;

    @Data
    public static class QuestionDetail {
        private Long id;
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
    }

    @Data
    public static class GradeRuleDetail {
        private Long id;
        private Integer minScore;
        private Integer maxScore;
        private String levelCode;
        private String levelName;
        private String adviceText;
        private Integer isActive;
    }
}
