package com.emotion.detection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackStatusResponse {

    private Long feedbackId;

    private String systemLabel;

    private String correctLabel;

    private Integer status;

    private String statusText;

    private String adminNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
}
