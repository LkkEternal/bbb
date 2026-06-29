package com.citrus.feedback.controller;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.feedback.service.FeedbackService;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序端 - 用户反馈列表接口
 */
@Tag(name = "M10-用户反馈（小程序）")
@RestController
@RequestMapping("/api/miniapp/feedbacks")
@RequiredArgsConstructor
public class MiniappFeedbackListController {

    private final FeedbackService feedbackService;
    private final SecurityUtil securityUtil;

    /**
     * 我的反馈列表
     */
    @Operation(summary = "我的反馈列表", description = "查看历史反馈")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        Map<String, Object> data = feedbackService.getFeedbackList(userId, page, size);
        return Result.success(data);
    }
}
