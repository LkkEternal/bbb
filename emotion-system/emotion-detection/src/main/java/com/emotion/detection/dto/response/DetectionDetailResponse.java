package com.emotion.detection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class DetectionDetailResponse {

    private Long recordId;

    private String imageUrl;

    private String objectKey;

    private String emotionLabel;

    private String emotionCn;

    private BigDecimal confidence;

    private Integer faceCount;

    private Integer validFaceCount;

    /** 性别（模型检测的数据）：男/女 */
    private String modelGender;

    /** 年龄（模型检测的数据） */
    private Integer modelAge;

    /** 年龄段：儿童/青少年/青年/中年/老年 */
    private String modelRange;

    private Map<String, Object> detectDetail;

    private String adviceText;

    private String adviceSource;

    private String adviceSourceType;

    private Integer processingTimeMs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private FeedbackInfo feedback;

    private EnvironmentInfo environment;

    @Data
    public static class FeedbackInfo {
        private Boolean isHelpful;
        private String feedbackText;
    }

    @Data
    public static class EnvironmentInfo {
        private String weatherMain;
        private BigDecimal temperature;
        private Integer humidity;
        private String windDir;
        private String windScale;
        private String locationName;
        private Integer indoorOutDoor;
    }
}
