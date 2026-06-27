package com.citrus.feedback.controller;

import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.feedback.dto.ReviewFeedbackDTO;
import com.citrus.feedback.service.FeedbackService;
import com.citrus.feedback.vo.AdminFeedbackDetailVO;
import com.citrus.feedback.vo.ReviewFeedbackVO;
import com.citrus.auth.model.LoginUser;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 - 用户反馈接口
 */
@Tag(name = "M10-用户反馈（管理员）")
@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;
    private final SecurityUtil securityUtil;

    /**
     * 检查管理员权限并返回用户ID
     */
    private Long checkAdminPermission() {
        LoginUser loginUser = securityUtil.getCurrentUser();
        if (loginUser == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
        return loginUser.getUserId();
    }

    /**
     * 待审核列表
     */
    @Operation(summary = "待审核列表", description = "管理员查看待审核反馈")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String reviewStatus) {
        checkAdminPermission();
        Map<String, Object> data = feedbackService.getAdminFeedbackList(page, size, reviewStatus);
        return Result.success(data);
    }

    /**
     * 反馈详情
     */
    @Operation(summary = "反馈详情", description = "查看反馈详情")
    @GetMapping("/{id}")
    public Result<AdminFeedbackDetailVO> detail(@PathVariable Long id) {
        checkAdminPermission();
        AdminFeedbackDetailVO detail = feedbackService.getFeedbackDetail(id);
        return Result.success(detail);
    }

    /**
     * 审核反馈
     */
    @Operation(summary = "审核反馈", description = "采纳/修正/驳回")
    @PostMapping("/{id}/review")
    public Result<ReviewFeedbackVO> review(@PathVariable Long id, @Valid @RequestBody ReviewFeedbackDTO dto) {
        Long reviewerId = checkAdminPermission();
        ReviewFeedbackVO vo = feedbackService.reviewFeedback(id, reviewerId, dto);
        return Result.success("审核完成", vo);
    }

    /**
     * 导出训练集
     */
    @Operation(summary = "导出训练集", description = "导出已验证图片")
    @GetMapping("/export-dataset")
    public void exportDataset(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String diseaseCode,
            HttpServletResponse response) {
        checkAdminPermission();
        feedbackService.exportDataset(format, startDate, endDate, diseaseCode, response);
    }
}
