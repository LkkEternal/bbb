package com.emotion.detection.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class DetectionStatsResponse {

    private long totalCount;

    private Map<String, Long> emotionCounts;
}
