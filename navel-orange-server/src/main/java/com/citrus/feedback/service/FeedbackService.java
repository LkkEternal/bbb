package com.citrus.feedback.service;

import com.citrus.feedback.dto.FeedbackSubmitDTO;
import com.citrus.feedback.dto.ReviewFeedbackDTO;
import com.citrus.feedback.vo.AdminFeedbackDetailVO;
import com.citrus.feedback.vo.FeedbackListVO;
import com.citrus.feedback.vo.FeedbackSubmitVO;
import com.citrus.feedback.vo.ReviewFeedbackVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * 用户反馈服务接口
 */
public interface FeedbackService {

    /**
     * 提交反馈
     *
     * @param userId 当前用户ID
     * @param dto    反馈参数
     * @return 反馈ID和状态
     */
    FeedbackSubmitVO submitFeedback(Long userId, FeedbackSubmitDTO dto);

    /**
     * 获取用户反馈列表
     *
     * @param userId 当前用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 分页数据
     */
    Map<String, Object> getFeedbackList(Long userId, int page, int size);

    /**
     * 获取管理员反馈列表
     *
     * @param page         页码
     * @param size         每页数量
     * @param reviewStatus 审核状态（可选）
     * @return 分页数据
     */
    Map<String, Object> getAdminFeedbackList(int page, int size, String reviewStatus);

    /**
     * 获取反馈详情（管理员）
     *
     * @param feedbackId 反馈ID
     * @return 反馈详情
     */
    AdminFeedbackDetailVO getFeedbackDetail(Long feedbackId);

    /**
     * 审核反馈
     *
     * @param feedbackId 反馈ID
     * @param reviewerId 审核人ID
     * @param dto        审核参数
     * @return 审核结果
     */
    ReviewFeedbackVO reviewFeedback(Long feedbackId, Long reviewerId, ReviewFeedbackDTO dto);

    /**
     * 导出训练集
     *
     * @param format      导出格式：csv/json
     * @param startDate   开始日期（可选）
     * @param endDate     结束日期（可选）
     * @param diseaseCode 病害编码（可选）
     * @param response    HTTP响应
     */
    void exportDataset(String format, String startDate, String endDate, String diseaseCode, HttpServletResponse response);
}
