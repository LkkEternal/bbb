package com.emotion.chat.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireListResponse {

    private List<QuestionnaireItem> questionnaires;

    @Data
    public static class QuestionnaireItem {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Integer totalQuestions;
    }
}
