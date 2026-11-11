package com.emotion.chat.service;

import com.emotion.chat.dto.request.AssessmentAnswerRequest;
import com.emotion.chat.dto.request.AssessmentStartRequest;
import com.emotion.chat.dto.response.*;

public interface AssessmentService {

    QuestionnaireListResponse getActiveQuestionnaires();

    AssessmentStartResponse startAssessment(Long userId, AssessmentStartRequest request);

    AssessmentAnswerResponse answerQuestion(Long userId, AssessmentAnswerRequest request);

    AssessmentAnswerResponse goBackQuestion(Long userId, Long recordId);

    AssessmentAnswerResponse submitAssessment(Long userId, Long recordId);

    AssessmentHistoryResponse getHistory(Long userId, int page, int size);

    AssessmentDetailResponse getDetail(Long userId, Long recordId);
}
