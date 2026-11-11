package com.emotion.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeedbackPageResponse {

    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;
    private List<FeedbackDTO> records;

    @Data
    public static class FeedbackDTO {
        private Long feedbackId;
        private Long userId;
        private String username;
        private String imageUrl;
        private String systemLabel;
        private String correctLabel;
        private String description;
        private Integer status;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }
}
