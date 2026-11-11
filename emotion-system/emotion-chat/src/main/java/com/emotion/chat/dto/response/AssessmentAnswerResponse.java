package com.emotion.chat.dto.response;

import lombok.Data;

@Data
public class AssessmentAnswerResponse {

    private Boolean finished;

    private Boolean allAnswered;

    private AssessmentStartResponse.QuestionInfo currentQuestion;

    private ProgressInfo progress;

    private AssessmentResult result;

    @Data
    public static class ProgressInfo {
        private Integer current;
        private Integer total;
    }

    @Data
    public static class AssessmentResult {
        private Long recordId;
        private Integer totalScore;
        private String levelCode;
        private String levelName;
        private String advice;
        private String completedAt;
    }
}
