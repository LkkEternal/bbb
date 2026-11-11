package com.emotion.chat.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AssessmentHistoryResponse {

    private Long total;

    private List<HistoryRecord> records;

    @Data
    public static class HistoryRecord {
        private Long recordId;
        private Long questionnaireId;
        private String questionnaireName;
        private String questionnaireCode;
        private Integer totalScore;
        private String levelCode;
        private String levelName;
        private String adviceText;
        private String completedAt;
    }
}
