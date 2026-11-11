package com.emotion.chat.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AiParseResultResponse {

    private String name;

    private String code;

    private String description;

    private List<QuestionItem> questions;

    private List<GradeRuleItem> gradeRules;

    @Data
    public static class QuestionItem {
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
    }

    @Data
    public static class GradeRuleItem {
        private Integer minScore;
        private Integer maxScore;
        private String levelCode;
        private String levelName;
        private String adviceText;
    }
}
