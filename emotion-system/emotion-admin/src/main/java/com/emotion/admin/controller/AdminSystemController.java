package com.emotion.admin.controller;

import com.emotion.admin.dto.*;
import com.emotion.admin.service.AdminService;
import com.emotion.admin.service.MailService;
import com.emotion.common.annotation.OperationLog;
import com.emotion.common.response.ErrorCode;
import com.emotion.common.response.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统管理控制器
 * 提供系统管理和运营支持功能：
 * - 预警规则管理（查看、修改）
 * - 情感词典管理（查看、修改）
 * - 用户反馈审核（审核、导出）
 *
 * 主要功能：
 * 1. 预警规则：设置情感检测的预警条件（时间窗口、频率、情感类型等）
 * 2. 情感词典：管理表情标签的映射、建议文本、状态等
 * 3. 反馈审核：审核用户提交的反馈，采用正确的标签或建议
 * 4. 数据导出：导出已审核通过的反馈数据（图片压缩包）
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSystemController {

    private final AdminService adminService;
    private final MailService mailService;

    /**
     * 获取所有预警规则
     *
     * @return 预警规则列表，按优先级升序排列
     */
    @GetMapping("/alert-rules")
    public Result<List<AlertRuleResponse>> getAlertRules() {
        List<AlertRuleResponse> data = adminService.getAlertRules();
        return Result.success(data);
    }

    /**
     * 修改预警规则
     *
     * 可修改的字段：
     * - enabled: 启用/禁用
     * - ruleName: 规则名称
     * - emotionLabels: 触发的情感标签
     * - minOccurrences: 最小触发次数
     * - timeWindowHours: 时间窗口（小时）
     * - cooldownMinutes: 冷却时间（分钟）
     *
     * @param id 预警规则ID
     * @param request 更新请求（部分字段可选）
     * @return 成功响应
     */
    @OperationLog(module = "system", action = "update", description = "修改预警规则")
    @PutMapping("/alert-rules/{id}")
    public Result<Void> updateAlertRule(@PathVariable Integer id,
                                        @Valid @RequestBody(required = false) AlertRuleUpdateRequest request) {
        adminService.updateAlertRule(id, request);
        return Result.success("修改成功", null);
    }

    /**
     * 获取所有情感词典
     *
     * @return 情感词典列表，按排序顺序排列
     */
    @GetMapping("/emotion-dict")
    public Result<List<EmotionDictResponse>> getEmotionDictList() {
        List<EmotionDictResponse> data = adminService.getEmotionDictList();
        return Result.success(data);
    }

    /**
     * 修改情感词典
     *
     * 可修改的字段：
     * - defaultAdvice: 默认建议文本
     * - isActive: 是否启用
     *
     * @param id 情感词典ID
     * @param request 更新请求（部分字段可选）
     * @return 成功响应
     */
    @OperationLog(module = "system", action = "update", description = "修改表情字典")
    @PutMapping("/emotion-dict/{id}")
    public Result<Void> updateEmotionDict(@PathVariable Integer id,
                                          @Valid @RequestBody(required = false) EmotionDictUpdateRequest request) {
        adminService.updateEmotionDict(id, request);
        return Result.success("修改成功", null);
    }

    /**
     * 获取用户反馈列表（分页查询）
     *
     * 支持筛选条件：
     * - status: 反馈状态（0=待审核，1=已采用，2=已驳回）
     * - correctLabel: 正确标签
     *
     * 默认按创建时间倒序排列
     *
     * @param request 分页和筛选请求
     * @return 分页的反馈列表
     */
    @GetMapping("/feedbacks")
    public Result<FeedbackPageResponse> getFeedbacks(FeedbackPageRequest request) {
        FeedbackPageResponse data = adminService.getFeedbacks(request);
        return Result.success(data);
    }

    /**
     * 审核用户反馈
     *
     * 流程说明：
     * 1. 验证反馈是否存在
     * 2. 检查是否可审核（状态必须是"待审核"）
     * 3. 更新反馈状态（已采用/已驳回）
     * 4. 记录审核人和审核时间
     * 5. 如已采用，更新正确标签
     *
     * @param id 反馈记录ID
     * @param request 审核请求（包含状态和正确标签）
     * @return 成功响应
     * @throws BizException 如果反馈不存在、已审核或无权访问
     */
    @OperationLog(module = "detection", action = "review", description = "审核反馈")
    @PutMapping("/feedbacks/{id}/review")
    public Result<Void> reviewFeedback(@PathVariable Long id,
                                       @Valid @RequestBody FeedbackReviewRequest request) {
        Long adminUserId = getCurrentUserId();
        adminService.reviewFeedback(id, request, adminUserId);
        return Result.success("审核成功", null);
    }

    /**
     * 导出已审核通过的反馈数据
     *
     * 导出格式：
     * - ZIP压缩包
     * - 按正确标签分类文件夹
     * - 包含用户反馈图片
     *
     * 文件命名格式：
     * emotion_feedback_export_20250730.zip
     *
     * @param response HTTP响应对象
     * @throws Exception 导出失败时抛出异常
     */
    @GetMapping("/feedbacks/export")
    public void exportFeedbacks(HttpServletResponse response) throws Exception {
        adminService.exportFeedbacks(response);
    }

    /**
     * 发送测试预警邮件
     *
     * 用于验证 SMTP 配置与邮件样式，向指定邮箱发送一封固定静态版式预警邮件。
     *
     * @param request 测试邮件请求（结构化字段，填充固定模板占位符）
     * @return 发送结果
     */
    @PostMapping("/alert/email/test")
    public Result<Void> sendTestAlertEmail(@Valid @RequestBody MailTestRequest request) {
        AlertEmailData data = new AlertEmailData();
        data.setTo(request.getTo());
        data.setSubject(request.getSubject());
        data.setTitle(request.getTitle());
        data.setUserName(request.getUserName());
        data.setEmotion(request.getEmotion());
        data.setCount(request.getCount());
        data.setAdvice(request.getAdvice());
        data.setLastTime(request.getLastTime());
        boolean ok = mailService.sendAlertEmail(data);
        if (!ok) {
            return Result.error(ErrorCode.INTERNAL_ERROR, "邮件发送失败，请检查SMTP配置与日志");
        }
        return Result.success("测试邮件已发送", null);
    }

    /**
     * 获取当前登录管理员的用户ID
     *
     * @return 管理员用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
