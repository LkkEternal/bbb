package com.emotion.chat.controller;

import com.emotion.chat.dto.request.AssessmentAnswerRequest;
import com.emotion.chat.dto.request.AssessmentPrevRequest;
import com.emotion.chat.dto.request.AssessmentStartRequest;
import com.emotion.chat.dto.response.*;
import com.emotion.chat.service.AssessmentService;
import com.emotion.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 心理测评控制器
 * 负责处理心理测评问卷的流程管理，包括：
 * - 问卷列表获取
 * - 开始测评
 * - 答题交互
 * - 评估结果提交
 * - 历史记录查询
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * 获取所有激活的问卷列表
     *
     * @return 问卷列表响应，包含问卷的基本信息（ID、名称、代码、描述、题目总数）
     */
    @GetMapping("/list")
    public Result<QuestionnaireListResponse> getQuestionnaires() {
        QuestionnaireListResponse data = assessmentService.getActiveQuestionnaires();
        return Result.success(data);
    }

    /**
     * 开始一个新的测评
     *
     * @param auth 当前登录用户的认证信息
     * @param request 包含问卷ID的请求对象
     * @return 评估开始响应，包含评估记录ID和第一道题目信息
     */
    @PostMapping("/start")
    public Result<AssessmentStartResponse> startAssessment(
            Authentication auth,
            @Valid @RequestBody AssessmentStartRequest request) {
        Long userId = (Long) auth.getPrincipal();
        AssessmentStartResponse data = assessmentService.startAssessment(userId, request);
        return Result.success("评估已开始", data);
    }

    /**
     * 回答当前题目
     *
     * @param auth 当前登录用户的认证信息
     * @param request 包含题目ID和用户选择的选项
     * @return 答题响应，如果完成则返回结果，否则返回下一题信息
     */
    @PostMapping("/answer")
    public Result<AssessmentAnswerResponse> answerQuestion(
            Authentication auth,
            @Valid @RequestBody AssessmentAnswerRequest request) {
        Long userId = (Long) auth.getPrincipal();
        AssessmentAnswerResponse data = assessmentService.answerQuestion(userId, request);
        if (data.getFinished()) {
            return Result.success("评估已完成", data);
        }
        return Result.success(data);
    }

    /**
     * 返回上一题（回退到上一题）
     *
     * @param auth 当前登录用户的认证信息
     * @param request 包含评估记录ID
     * @return 答题响应，返回上一题的信息和进度
     */
    @PostMapping("/prev")
    public Result<AssessmentAnswerResponse> prevQuestion(
            Authentication auth,
            @Valid @RequestBody AssessmentPrevRequest request) {
        Long userId = (Long) auth.getPrincipal();
        AssessmentAnswerResponse data = assessmentService.goBackQuestion(userId, request.getRecordId());
        return Result.success(data);
    }

    /**
     * 提交评估结果
     *
     * @param auth 当前登录用户的认证信息
     * @param request 包含评估记录ID
     * @return 提交响应，返回最终评估结果
     */
    @PostMapping("/submit")
    public Result<AssessmentAnswerResponse> submitAssessment(
            Authentication auth,
            @Valid @RequestBody AssessmentPrevRequest request) {
        Long userId = (Long) auth.getPrincipal();
        AssessmentAnswerResponse data = assessmentService.submitAssessment(userId, request.getRecordId());
        return Result.success("评估结果已保存", data);
    }

    /**
     * 获取用户的测评历史记录
     *
     * @param auth 当前登录用户的认证信息
     * @param page 页码（从1开始）
     * @param size 每页记录数（最大50）
     * @return 历史记录响应，包含分页信息和记录详情
     */
    @GetMapping("/history")
    public Result<AssessmentHistoryResponse> getHistory(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        if (size > 50) size = 50;
        AssessmentHistoryResponse data = assessmentService.getHistory(userId, page, size);
        return Result.success(data);
    }

    /**
     * 获取测评记录的详细信息
     *
     * @param auth 当前登录用户的认证信息
     * @param id 测评记录ID
     * @return 测评详情响应，包含完整的测评信息
     */
    @GetMapping("/detail/{id}")
    public Result<AssessmentDetailResponse> getDetail(
            Authentication auth,
            @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        AssessmentDetailResponse data = assessmentService.getDetail(userId, id);
        return Result.success(data);
    }
}
