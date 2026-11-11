package com.emotion.detection.service;

import com.emotion.common.response.PageResult;
import com.emotion.detection.dto.request.FeedbackRequest;
import com.emotion.detection.dto.response.*;

public interface DetectionService {

    DetectionResponse detect(Long userId, org.springframework.web.multipart.MultipartFile file,
                             String modelGender, Integer modelAge, String modelRange);

    PageResult<DetectionHistoryResponse> getHistory(Long userId, int page, int size,
                                                     String emotionLabel, String startDate, String endDate);

    DetectionDetailResponse getDetail(Long userId, Long recordId);

    void submitFeedback(Long userId, Long recordId, FeedbackRequest request);

    FeedbackStatusResponse getFeedbackStatus(Long userId, Long recordId);

    DetectionStatsResponse getStats(Long userId);
}
