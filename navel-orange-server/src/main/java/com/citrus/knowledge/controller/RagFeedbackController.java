package com.citrus.knowledge.controller;

import com.citrus.common.result.Result;
import com.citrus.knowledge.dto.RagFeedbackDTO;
import com.citrus.knowledge.entity.RagFeedback;
import com.citrus.knowledge.mapper.RagFeedbackMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * RAG 问答反馈接口
 */
@Tag(name = "M7-知识库", description = "RAG 问答反馈")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class RagFeedbackController {

    private final RagFeedbackMapper feedbackMapper;

    @Operation(summary = "提交问答反馈", description = "农户对 RAG 回答进行评价（有用/没用）")
    @PostMapping("/qa/feedback")
    public Result<Void> submitFeedback(@Valid @RequestBody RagFeedbackDTO dto, Principal principal) {
        RagFeedback feedback = new RagFeedback();
        feedback.setSessionId(dto.getSessionId());
        feedback.setFeedbackType(dto.getFeedbackType());
        feedback.setRemark(dto.getRemark());
        if (principal != null) {
            feedback.setUserId(1L);  // 简化处理
        }
        feedbackMapper.insert(feedback);
        return Result.success();
    }
}
