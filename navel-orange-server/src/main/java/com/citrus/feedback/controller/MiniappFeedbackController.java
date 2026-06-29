package com.citrus.feedback.controller;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.feedback.dto.FeedbackSubmitDTO;
import com.citrus.feedback.service.FeedbackService;
import com.citrus.feedback.vo.FeedbackSubmitVO;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端 - 用户反馈接口
 */
@Tag(name = "M10-用户反馈（小程序）")
@RestController
@RequestMapping("/api/miniapp/feedback")
@RequiredArgsConstructor
public class MiniappFeedbackController {

    private final FeedbackService feedbackService;
    private final SecurityUtil securityUtil;

    /**
     * 提交反馈
     */
    @Operation(summary = "提交反馈", description = "农户拍照+描述提交问题")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FeedbackSubmitVO> submit(@Valid FeedbackSubmitDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        FeedbackSubmitVO vo = feedbackService.submitFeedback(userId, dto);
        return Result.success("提交成功", vo);
    }
}
