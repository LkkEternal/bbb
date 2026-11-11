package com.emotion.detection.controller;

import com.emotion.common.response.PageResult;
import com.emotion.common.response.Result;
import com.emotion.detection.dto.request.FeedbackRequest;
import com.emotion.detection.dto.response.*;
import com.emotion.detection.service.DetectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 情感检测控制器
 * 负责处理用户上传的图片情感检测请求：
 * - 图片上传和情感分析
 * - 检测历史查询
 * - 统计信息查询
 * - 检测详情查看
 * - 反馈提交和管理
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@RestController
@RequestMapping("/api/detection")
@RequiredArgsConstructor
public class DetectionController {

    private final DetectionService detectionService;

    /**
     * 上传图片进行情感检测
     *
     * 流程说明：
     * 1. 验证图片格式和大小（JPEG/PNG，最大10MB）
     * 2. 上传图片到七牛云存储
     * 3. 调用Python服务进行情感分析
     * 4. 验证检测到的有效人脸数量
     * 5. 识别主要人脸的情感
     * 6. 查询情感词典获取建议
     * 7. 保存检测记录到数据库
     * 8. 异步保存环境数据（天气等）
     *
     * @param auth 当前登录用户的认证信息
     * @param file 用户上传的图片文件
     * @return 检测响应，包含检测到的情感、置信度、建议等信息
     * @throws BizException 如果图片格式错误、过大、检测失败或未检测到人脸
     */
    @PostMapping("/detect")
    public Result<DetectionResponse> detect(Authentication auth,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam(required = false) String modelGender,
                                            @RequestParam(required = false) Integer modelAge,
                                            @RequestParam(required = false) String modelRange) {
        Long userId = (Long) auth.getPrincipal();
        DetectionResponse response = detectionService.detect(userId, file, modelGender, modelAge, modelRange);
        return Result.success("检测成功", response);
    }

    /**
     * 获取检测历史记录（支持筛选）
     *
     * 支持的筛选条件：
     * - emotionLabel: 情感标签（开心、悲伤、生气等）
     * - startDate: 开始日期
     * - endDate: 结束日期
     *
     * @param auth 当前登录用户的认证信息
     * @param page 页码（从1开始）
     * @param size 每页记录数（最大50）
     * @param emotionLabel 可选，情感标签筛选
     * @param startDate 可选，开始日期筛选
     * @param endDate 可选，结束日期筛选
     * @return 分页的历史记录响应
     */
    @GetMapping("/history")
    public Result<PageResult<DetectionHistoryResponse>> getHistory(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String emotionLabel,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = (Long) auth.getPrincipal();
        if (size > 50) size = 50;
        PageResult<DetectionHistoryResponse> result = detectionService.getHistory(
                userId, page, size, emotionLabel, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 获取用户的情感检测统计信息
     *
     * 返回每个情感类型的检测次数统计
     *
     * @param auth 当前登录用户的认证信息
     * @return 统计响应，包含各情感的检测次数
     */
    @GetMapping("/stats")
    public Result<DetectionStatsResponse> getStats(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        DetectionStatsResponse result = detectionService.getStats(userId);
        return Result.success(result);
    }

    /**
     * 获取单条检测记录的详细信息
     *
     * 包含：
     * - 完整的检测详情（所有检测到的人脸）
     * - 情感建议
     * - 用户反馈信息
     * - 环境信息（天气等）
     *
     * @param auth 当前登录用户的认证信息
     * @param id 检测记录ID
     * @return 检测详情响应
     * @throws BizException 如果记录不存在或无权访问
     */
    @GetMapping("/{id}")
    public Result<DetectionDetailResponse> getDetail(Authentication auth,
                                                     @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        DetectionDetailResponse response = detectionService.getDetail(userId, id);
        return Result.success(response);
    }

    /**
     * 提交检测反馈
     *
     * 流程说明：
     * 1. 验证检测记录是否存在且归当前用户所有
     * 2. 检查是否已提交过反馈
     * 3. 保存反馈信息（系统标签、正确标签、描述）
     * 4. 状态默认为"待审核"
     *
     * @param auth 当前登录用户的认证信息
     * @param recordId 检测记录ID
     * @param request 反馈请求
     * @return 成功响应
     * @throws BizException 如果记录不存在、无权访问或已提交反馈
     */
    @PostMapping("/{recordId}/feedback")
    public Result<String> submitFeedback(Authentication auth,
                                       @PathVariable Long recordId,
                                       @Valid @RequestBody FeedbackRequest request) {
        Long userId = (Long) auth.getPrincipal();
        detectionService.submitFeedback(userId, recordId, request);
        return Result.success("反馈提交成功");
    }

    /**
     * 提交检测反馈（备选接口）
     *
     * 从请求体中获取 recordId，与路径参数方式不同
     *
     * @param auth 当前登录用户的认证信息
     * @param request 反馈请求
     * @return 成功响应
     * @throws IllegalArgumentException 如果 recordId 为空
     */
    @PostMapping("/feedback")
    public Result<String> submitFeedbackAlt(Authentication auth,
                                          @Valid @RequestBody FeedbackRequest request) {
        Long userId = (Long) auth.getPrincipal();
        if (request.getRecordId() == null) {
            throw new IllegalArgumentException("recordId不能为空");
        }
        detectionService.submitFeedback(userId, request.getRecordId(), request);
        return Result.success("反馈提交成功");
    }

    /**
     * 获取反馈状态
     *
     * 返回反馈的审核状态、系统标签、正确标签等
     *
     * @param auth 当前登录用户的认证信息
     * @param recordId 检测记录ID
     * @return 反馈状态响应，如果未提交反馈则返回null
     */
    @GetMapping("/{recordId}/feedback/status")
    public Result<FeedbackStatusResponse> getFeedbackStatus(Authentication auth,
                                                            @PathVariable Long recordId) {
        Long userId = (Long) auth.getPrincipal();
        FeedbackStatusResponse response = detectionService.getFeedbackStatus(userId, recordId);
        return Result.success(response);
    }
}