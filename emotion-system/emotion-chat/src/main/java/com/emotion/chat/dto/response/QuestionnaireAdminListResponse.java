package com.emotion.chat.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireAdminListResponse {
    private List<QuestionnaireAdminItem> questionnaires;
    private long total;
    private int page;
    private int size;

    @Data
    public static class QuestionnaireAdminItem {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Integer totalQuestions;
        private Integer isActive;
        private String createdAt;
        private String updatedAt;
    }
}
