package com.citrus.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.citrus.common.util.HuaweiObsClient;
import com.citrus.feedback.dto.FeedbackSubmitDTO;
import com.citrus.feedback.dto.ReviewFeedbackDTO;
import com.citrus.feedback.entity.UserFeedback;
import com.citrus.feedback.mapper.UserFeedbackMapper;
import com.citrus.feedback.service.FeedbackService;
import com.citrus.feedback.vo.AdminFeedbackDetailVO;
import com.citrus.feedback.vo.AdminFeedbackListVO;
import com.citrus.feedback.vo.FeedbackListVO;
import com.citrus.feedback.vo.FeedbackSubmitVO;
import com.citrus.feedback.vo.ReviewFeedbackVO;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用户反馈服务实现
 */
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    private final UserFeedbackMapper feedbackMapper;
    private final OrchardMapper orchardMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final UserMapper userMapper;
    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final HuaweiObsClient huaweiObsClient;
    private final ObjectMapper objectMapper;

    /**
     * 最大图片数量
     */
    private static final int MAX_IMAGE_COUNT = 5;

    /**
     * 最大图片大小（10MB）
     */
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    @Override
    @Transactional
    public FeedbackSubmitVO submitFeedback(Long userId, FeedbackSubmitDTO dto) {
        // 1. 校验图片数量和大小
        MultipartFile[] images = dto.getImages();
        if (images == null || images.length == 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "请至少上传一张图片");
        }
        if (images.length > MAX_IMAGE_COUNT) {
            throw new BizException(ResultCode.BAD_REQUEST, "最多上传" + MAX_IMAGE_COUNT + "张图片");
        }
        for (MultipartFile image : images) {
            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new BizException(ResultCode.BAD_REQUEST, "图片大小不能超过10MB");
            }
        }

        // 2. 验证果园存在
        Orchard orchard = orchardMapper.selectById(dto.getOrchardId());
        if (orchard == null || orchard.getDeleted()) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 3. 验证用户是否有该果园的权限
        boolean hasPermission = checkOrchardPermission(userId, dto.getOrchardId());
        if (!hasPermission) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
        }

        // 4. 上传图片到华为云OSS
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String url = huaweiObsClient.upload(image, "feedback");
            imageUrls.add(url);
        }
        log.info("反馈图片上传成功: count={}, urls={}", imageUrls.size(), imageUrls);

        // 5. 创建反馈记录
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setOrchardId(dto.getOrchardId());
        feedback.setImageUrls(toJson(imageUrls));
        feedback.setDescription(dto.getDescription());
        feedback.setFeedbackType(dto.getFeedbackType());
        feedback.setReviewStatus(0); // 待审核
        feedback.setCreatedAt(LocalDateTime.now());

        // 6. 保存反馈
        feedbackMapper.insert(feedback);

        log.info("用户反馈提交成功: feedbackId={}, userId={}, orchardId={}", feedback.getId(), userId, dto.getOrchardId());

        // 7. 返回结果
        return FeedbackSubmitVO.builder()
                .feedbackId(feedback.getId())
                .status("pending")
                .build();
    }

    @Override
    public Map<String, Object> getFeedbackList(Long userId, int page, int size) {
        // 查询用户反馈总数
        LambdaQueryWrapper<UserFeedback> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(UserFeedback::getUserId, userId);
        long total = feedbackMapper.selectCount(countWrapper);

        // 查询用户反馈列表（分页）
        LambdaQueryWrapper<UserFeedback> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFeedback::getUserId, userId)
                .orderByDesc(UserFeedback::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);

        List<UserFeedback> feedbackList = feedbackMapper.selectList(queryWrapper);

        // 转换为VO
        List<FeedbackListVO> records = new ArrayList<>();
        for (UserFeedback feedback : feedbackList) {
            FeedbackListVO vo = new FeedbackListVO();
            vo.setId(feedback.getId());
            vo.setFeedbackType(feedback.getFeedbackType());
            vo.setDescription(feedback.getDescription());
            vo.setReviewStatus(feedback.getReviewStatus());
            vo.setCreatedAt(feedback.getCreatedAt());

            // 解析图片URL
            vo.setImageUrls(parseImageUrls(feedback.getImageUrls()));

            // 查询果园名称
            if (feedback.getOrchardId() != null) {
                Orchard orchard = orchardMapper.selectById(feedback.getOrchardId());
                if (orchard != null) {
                    vo.setOrchardName(orchard.getName());
                }
            }

            records.add(vo);
        }

        // 构建返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);

        return result;
    }

    @Override
    public Map<String, Object> getAdminFeedbackList(int page, int size, String reviewStatus) {
        // 构建查询条件（用于 count）
        LambdaQueryWrapper<UserFeedback> countWrapper = new LambdaQueryWrapper<>();
        if (reviewStatus != null && !reviewStatus.isEmpty()) {
            countWrapper.eq(UserFeedback::getReviewStatus, parseReviewStatus(reviewStatus));
        }

        // 查询总数（不带 ORDER BY）
        long total = feedbackMapper.selectCount(countWrapper);

        // 构建查询条件（用于分页数据）
        LambdaQueryWrapper<UserFeedback> queryWrapper = new LambdaQueryWrapper<>();
        if (reviewStatus != null && !reviewStatus.isEmpty()) {
            queryWrapper.eq(UserFeedback::getReviewStatus, parseReviewStatus(reviewStatus));
        }
        queryWrapper.orderByDesc(UserFeedback::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);

        List<UserFeedback> feedbackList = feedbackMapper.selectList(queryWrapper);

        // 转换为VO
        List<AdminFeedbackListVO> records = new ArrayList<>();
        for (UserFeedback feedback : feedbackList) {
            records.add(convertToAdminListVO(feedback));
        }

        // 构建返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);

        return result;
    }

    @Override
    public AdminFeedbackDetailVO getFeedbackDetail(Long feedbackId) {
        // 查询反馈记录
        UserFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BizException(ResultCode.NOT_FOUND, "反馈不存在");
        }

        // 转换为详情VO
        AdminFeedbackDetailVO detail = new AdminFeedbackDetailVO();
        detail.setId(feedback.getId());
        detail.setUserId(feedback.getUserId());
        detail.setOrchardId(feedback.getOrchardId());
        detail.setFeedbackType(feedback.getFeedbackType());
        detail.setDescription(feedback.getDescription());
        detail.setReviewStatus(feedback.getReviewStatus());
        detail.setStatus(feedback.getReviewStatus()); // 前端期望的字段名
        detail.setDatasetTag(feedback.getDatasetTag());
        detail.setReviewedBy(feedback.getReviewedBy());
        detail.setReviewedAt(feedback.getReviewedAt());
        detail.setReviewComment(feedback.getReviewComment());
        detail.setCreatedAt(feedback.getCreatedAt());

        // 查询用户信息
        if (feedback.getUserId() != null) {
            User user = userMapper.selectById(feedback.getUserId());
            if (user != null) {
                detail.setUsername(user.getUsername());
                detail.setPhone(maskPhone(user.getPhone()));
            }
        }

        // 查询果园名称
        if (feedback.getOrchardId() != null) {
            Orchard orchard = orchardMapper.selectById(feedback.getOrchardId());
            if (orchard != null) {
                detail.setOrchardName(orchard.getName());
            }
        }

        // 查询审核人信息
        if (feedback.getReviewedBy() != null) {
            User reviewer = userMapper.selectById(feedback.getReviewedBy());
            if (reviewer != null) {
                detail.setReviewerName(reviewer.getUsername());
            }
        }

        // 解析图片URL
        List<String> imageUrls = parseImageUrls(feedback.getImageUrls());
        detail.setImageUrls(imageUrls);
        // 设置第一个图片为 imageUrl（前端期望的字段名）
        if (!imageUrls.isEmpty()) {
            detail.setImageUrl(imageUrls.get(0));
        }

        return detail;
    }

    /**
     * 检查用户是否有果园权限
     */
    private boolean checkOrchardPermission(Long userId, Long orchardId) {
        // 检查是否是园主
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard != null && userId.equals(orchard.getOwnerId())) {
            return true;
        }

        // 检查是否是成员
        OrchardUser member = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, userId)
        );
        return member != null;
    }

    /**
     * 解析审核状态字符串为数据库值
     */
    private Integer parseReviewStatus(String status) {
        return switch (status) {
            case "pending_quiz" -> 0;
            case "pending_review" -> 1;
            case "accepted" -> 2;
            case "revised" -> 3;
            case "rejected" -> 4;
            default -> null;
        };
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 解析图片URL
     */
    private List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imageUrlsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("解析图片URL失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 转换为管理员列表VO
     */
    private AdminFeedbackListVO convertToAdminListVO(UserFeedback feedback) {
        AdminFeedbackListVO vo = new AdminFeedbackListVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setOrchardId(feedback.getOrchardId());
        vo.setFeedbackType(feedback.getFeedbackType());
        vo.setDescription(feedback.getDescription());
        vo.setReviewStatus(feedback.getReviewStatus());
        vo.setStatus(feedback.getReviewStatus()); // 前端期望的字段名
        vo.setDatasetTag(feedback.getDatasetTag());
        vo.setCreatedAt(feedback.getCreatedAt());

        // 查询用户信息
        if (feedback.getUserId() != null) {
            User user = userMapper.selectById(feedback.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
        }

        // 查询果园名称
        if (feedback.getOrchardId() != null) {
            Orchard orchard = orchardMapper.selectById(feedback.getOrchardId());
            if (orchard != null) {
                vo.setOrchardName(orchard.getName());
            }
        }

        // 解析图片URL
        List<String> imageUrls = parseImageUrls(feedback.getImageUrls());
        vo.setImageUrls(imageUrls);
        // 设置第一个图片为 imageUrl（前端期望的字段名）
        if (!imageUrls.isEmpty()) {
            vo.setImageUrl(imageUrls.get(0));
        }

        return vo;
    }

    /**
     * 对象转JSON字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败: {}", e.getMessage());
            return "[]";
        }
    }

    @Override
    @Transactional
    public ReviewFeedbackVO reviewFeedback(Long feedbackId, Long reviewerId, ReviewFeedbackDTO dto) {
        // 1. 查询反馈记录
        UserFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BizException(ResultCode.NOT_FOUND, "反馈不存在");
        }

        // 2. 验证反馈状态（只能审核待审核的反馈）
        if (feedback.getReviewStatus() != null && feedback.getReviewStatus() != 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该反馈已被审核");
        }

        // 3. 根据 action 设置审核状态
        int reviewStatus;
        boolean datasetIncluded = false;
        switch (dto.getAction()) {
            case "accept":
                reviewStatus = 1; // 已采纳
                datasetIncluded = true;
                break;
            case "revise":
                reviewStatus = 2; // 已修正
                datasetIncluded = true;
                // 修正时必须提供 correctedDisease
                if (dto.getCorrectedDisease() == null || dto.getCorrectedDisease().isEmpty()) {
                    throw new BizException(ResultCode.BAD_REQUEST, "修正时必须提供修正后的病害名称");
                }
                break;
            case "reject":
                reviewStatus = 3; // 已驳回
                break;
            default:
                throw new BizException(ResultCode.BAD_REQUEST, "无效的审核操作");
        }

        // 4. 更新反馈记录
        feedback.setReviewStatus(reviewStatus);
        feedback.setReviewedBy(reviewerId);
        feedback.setReviewedAt(LocalDateTime.now());
        feedback.setReviewComment(dto.getComment());

        // 5. 设置训练集标签
        if (datasetIncluded) {
            if ("revise".equals(dto.getAction())) {
                feedback.setDatasetTag(dto.getCorrectedDisease());
            }
            // accept 时 dataset_tag 保持原样或由管理员后续设置
        }

        feedbackMapper.updateById(feedback);

        log.info("反馈审核完成: feedbackId={}, action={}, reviewerId={}", feedbackId, dto.getAction(), reviewerId);

        // 6. 返回结果
        return ReviewFeedbackVO.builder()
                .feedbackId(feedbackId)
                .reviewStatus(reviewStatus)
                .datasetIncluded(datasetIncluded)
                .build();
    }

    @Override
    public void exportDataset(String format, String startDate, String endDate, String diseaseCode, HttpServletResponse response) {
        // 1. 构建查询条件（只查询已审核的反馈）
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserFeedback::getReviewStatus, 1, 2); // 已采纳或已修正

        // 日期筛选
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(UserFeedback::getCreatedAt, LocalDateTime.parse(startDate, formatter));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(UserFeedback::getCreatedAt, LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        // 病害编码筛选
        if (diseaseCode != null && !diseaseCode.isEmpty()) {
            wrapper.eq(UserFeedback::getDatasetTag, diseaseCode);
        }

        wrapper.orderByDesc(UserFeedback::getCreatedAt);

        // 2. 查询数据
        List<UserFeedback> feedbackList = feedbackMapper.selectList(wrapper);

        // 3. 根据格式导出
        if ("json".equalsIgnoreCase(format)) {
            exportAsJson(feedbackList, response);
        } else {
            exportAsCsv(feedbackList, response);
        }
    }

    /**
     * 导出为CSV格式（符合API接口文档定义）
     */
    private void exportAsCsv(List<UserFeedback> feedbackList, HttpServletResponse response) {
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"dataset_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv\"");

            PrintWriter writer = response.getWriter();

            // 写入CSV头（符合API接口文档定义）
            writer.println("id,image_url,disease_code,disease_name,feedback_type,review_status,review_comment,created_at");

            // 写入数据
            for (UserFeedback feedback : feedbackList) {
                List<String> imageUrls = parseImageUrls(feedback.getImageUrls());
                String imageUrl = imageUrls.isEmpty() ? "" : imageUrls.get(0);

                // 获取病害名称
                String diseaseCode = feedback.getDatasetTag() != null ? feedback.getDatasetTag() : "";
                String diseaseName = "";
                if (!diseaseCode.isEmpty()) {
                    DiseaseCatalog catalog = diseaseCatalogMapper.selectByCode(diseaseCode);
                    if (catalog != null) {
                        diseaseName = catalog.getDiseaseName();
                    }
                }

                writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"",
                        feedback.getId(),
                        imageUrl,
                        diseaseCode,
                        diseaseName,
                        feedback.getFeedbackType(),
                        feedback.getReviewStatus(),
                        feedback.getReviewComment() != null ? feedback.getReviewComment().replace("\"", "\"\"") : "",
                        feedback.getCreatedAt()));
            }

            writer.flush();
            writer.close();

            log.info("导出训练集完成: format=csv, count={}", feedbackList.size());
        } catch (IOException e) {
            log.error("导出CSV失败: {}", e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "导出失败");
        }
    }

    /**
     * 导出为JSON格式
     */
    private void exportAsJson(List<UserFeedback> feedbackList, HttpServletResponse response) {
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"dataset_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".json\"");

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (UserFeedback feedback : feedbackList) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", feedback.getId());
                item.put("imageUrls", parseImageUrls(feedback.getImageUrls()));
                item.put("datasetTag", feedback.getDatasetTag());
                item.put("feedbackType", feedback.getFeedbackType());
                item.put("reviewStatus", feedback.getReviewStatus());
                item.put("description", feedback.getDescription());
                item.put("createdAt", feedback.getCreatedAt());
                dataList.add(item);
            }

            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(dataList));
            writer.flush();
            writer.close();

            log.info("导出训练集完成: format=json, count={}", feedbackList.size());
        } catch (IOException e) {
            log.error("导出JSON失败: {}", e.getMessage());
            throw new BizException(ResultCode.INTERNAL_ERROR, "导出失败");
        }
    }
}
