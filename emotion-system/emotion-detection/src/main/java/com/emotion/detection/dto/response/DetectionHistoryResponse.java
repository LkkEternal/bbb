package com.emotion.detection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DetectionHistoryResponse {

    private Long recordId;

    private String imageUrl;

    private String emotionLabel;

    private String emotionCn;

    private BigDecimal confidence;

    private String adviceSourceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
