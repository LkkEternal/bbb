package com.emotion.admin.service;

import com.emotion.admin.dto.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface AdminService {

    List<AlertRuleResponse> getAlertRules();

    void updateAlertRule(Integer id, AlertRuleUpdateRequest request);

    List<EmotionDictResponse> getEmotionDictList();

    void updateEmotionDict(Integer id, EmotionDictUpdateRequest request);

    FeedbackPageResponse getFeedbacks(FeedbackPageRequest request);

    void reviewFeedback(Long id, FeedbackReviewRequest request, Long adminUserId);

    void exportFeedbacks(HttpServletResponse response) throws Exception;
}
