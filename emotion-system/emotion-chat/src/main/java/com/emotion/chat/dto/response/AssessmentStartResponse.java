package com.emotion.chat.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AssessmentStartResponse {

    private Long recordId;

    private Long questionnaireId;

    private String questionnaireName;

    private Integer totalQuestions;

    private QuestionInfo currentQuestion;

    @Data
    public static class QuestionInfo {
        private Long questionId;
        private Integer questionIndex;
        private String questionText;
        private List<OptionInfo> options;
    }

    @Data
    public static class OptionInfo {
        private String label;
        private String text;
        private Integer value;
    }
}
