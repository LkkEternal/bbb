package com.emotion.detection.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DetectionResponse {

    private Long recordId;

    private String imageUrl;

    private String emotionLabel;

    private String emotionCn;

    private Double confidence;

    private Integer faceCount;

    private Integer validFaceCount;

    private DetectDetail detectDetail;

    private String adviceText;

    private String adviceSource;

    private String adviceSourceType;

    private Integer processingTimeMs;

    @Data
    public static class DetectDetail {
        private List<FaceResult> faces;
        private String pipelineVersion;
        private String modelVersion;
    }

    @Data
    public static class FaceResult {
        private List<Integer> bbox;
        private Double faceConfidence;
        private String rejectReason;
        private List<EmotionProb> allEmotions;
    }

    @Data
    public static class EmotionProb {
        private String label;
        private Double confidence;
    }
}
