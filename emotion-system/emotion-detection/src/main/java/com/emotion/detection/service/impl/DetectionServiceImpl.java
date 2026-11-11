package com.emotion.detection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.common.response.PageResult;
import com.emotion.detection.dto.request.FeedbackRequest;
import com.emotion.detection.dto.response.*;
import com.emotion.detection.entity.*;
import com.emotion.detection.mapper.*;
import com.emotion.detection.service.DetectionService;
import com.emotion.infrastructure.client.PythonServiceClient;
import com.emotion.infrastructure.client.AmapClient;
import com.emotion.infrastructure.service.QiniuService;
import com.emotion.user.entity.User;
import com.emotion.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 情感检测服务实现类
 * 负责情感检测的核心业务逻辑：
 * - 图片上传和检测
 * - 情感分析（调用Python服务）
 * - 检测记录管理
 * - 统计信息生成
 * - 反馈管理
 * - 环境数据异步保存
 *
 * 核心功能：
 * 1. 使用七牛云存储图片
 * 2. 调用Python YOLOv8模型进行人脸检测和情感识别
 * 3. 识别主要人脸（最大面积）
 * 4. 从情感词典获取对应建议
 * 5. 异步保存天气环境数据
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionServiceImpl implements DetectionService {

    private final DetectionRecordMapper detectionRecordMapper;      // 检测记录数据访问
    private final EmotionFeedbackMapper emotionFeedbackMapper;      // 情感反馈数据访问
    private final EnvironmentRecordMapper environmentRecordMapper;  // 环境记录数据访问
    private final EmotionDictMapper emotionDictMapper;              // 情感词典数据访问
    private final UserMapper userMapper;                            // 用户数据访问
    private final PythonServiceClient pythonServiceClient;           // Python服务客户端
    private final QiniuService qiniuService;                         // 七牛云存储服务
    private final AmapClient amapClient;                             // 高德地图客户端
    private final ObjectMapper objectMapper;                         // JSON序列化工具

    /** 情感英文标签到中文标签的映射表 */
    private static final Map<String, String> EMOTION_CN_MAP = Map.of(
            "angry", "生气",
            "disgust", "厌恶",
            "fear", "恐惧",
            "happy", "开心",
            "neutral", "平静",
            "sad", "悲伤",
            "surprise", "惊讶",
            "unknown", "未知"
    );

    /**
     * 执行图片情感检测
     *
     * 完整流程：
     * 1. 验证文件格式和大小
     * 2. 上传图片到七牛云存储
     * 3. 调用Python服务进行情感分析
     * 4. 验证检测结果
     * 5. 识别主要人脸（最大面积）
     * 6. 提取情感标签和置信度
     * 7. 转换为中文标签
     * 8. 构建检测详情
     * 9. 查询情感词典获取建议
     * 10. 保存检测记录到数据库
     * 11. 异步保存环境数据（天气、温度、湿度等）
     *
     * 返回结果包含：
     * - 检测记录ID
     * - 图片URL
     * - 情感标签（英文和中文）
     * - 置信度
     * - 人脸数量
     * - 完整检测详情
     * - 情感建议
     *
     * @param userId 用户ID
     * @param file 图片文件
     * @return 检测响应
     * @throws BizException 如果图片格式错误、上传失败、检测失败、未检测到人脸
     */
    @Override
    @Transactional
    public DetectionResponse detect(Long userId, MultipartFile file, String modelGender, Integer modelAge, String modelRange) {
        // 1. 验证文件
        validateFile(file);

        // 2. 上传图片到七牛云
        QiniuService.UploadResult uploadResult;
        try {
            uploadResult = qiniuService.uploadDetectImage(file);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BizException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // 3. 调用Python服务进行情感分析
        Map<String, Object> pythonResult;
        try {
            pythonResult = pythonServiceClient.detectEmotion(file);
        } catch (Exception e) {
            log.error("Python检测服务调用失败", e);
            throw new BizException(ErrorCode.DETECTION_FAILED);
        }

        // 4. 验证检测结果
        if (pythonResult == null || !Boolean.TRUE.equals(pythonResult.get("success"))) {
            throw new BizException(ErrorCode.DETECTION_FAILED);
        }

        int validCount = toInt(pythonResult.get("valid_count"));
        if (validCount == 0) {
            throw new BizException(ErrorCode.NO_FACE_DETECTED);
        }

        // 5. 获取检测到的人脸列表
        List<Map<String, Object>> faces = (List<Map<String, Object>>) pythonResult.get("faces");
        Map<String, Object> mainFace = findMainFace(faces);

        // 6. 提取主要人脸的情感信息
        Map<String, Object> emotion = (Map<String, Object>) mainFace.get("emotion");
        String emotionLabel = emotion != null ? (String) emotion.get("label") : null;
        double confidence = emotion != null ? toDouble(emotion.get("confidence")) : 0.0;
        if (emotionLabel == null || emotionLabel.isBlank()) {
            emotionLabel = "unknown";
        }
        String emotionCn = EMOTION_CN_MAP.getOrDefault(emotionLabel, "未知");

        // 7. 构建检测详情
        DetectionResponse.DetectDetail detail = buildDetectDetail(faces, pythonResult);

        // 8. 查询情感词典获取建议
        EmotionDict dict = emotionDictMapper.selectByLabel(emotionLabel);
        String adviceText = null;
        String adviceSource = null;
        String adviceSourceType = "none";
        if (dict != null) {
            adviceText = dict.getDefaultAdvice();
            adviceSource = dict.getAdviceSource();
            adviceSourceType = "dict";
        }

        // 9. 保存检测记录到数据库
        DetectionRecord record = new DetectionRecord();
        record.setUserId(userId);
        record.setImageUrl(uploadResult.url());
        record.setObjectKey(uploadResult.objectKey());
        record.setEmotionLabel(emotionLabel);
        record.setEmotionCn(emotionCn);
        record.setConfidence(new BigDecimal(String.valueOf(confidence)));
        record.setFaceCount(toInt(pythonResult.get("faces_count")));
        record.setValidFaceCount(validCount);
        record.setModelGender(modelGender);
        record.setModelAge(modelAge);
        record.setModelRange(modelRange);
        record.setDetectDetail(objectMapper.convertValue(detail, new TypeReference<Map<String, Object>>() {}));
        record.setAdviceText(adviceText);
        record.setAdviceSource(adviceSource);
        record.setAdviceSourceType(adviceSourceType);
        record.setProcessingTimeMs(toInt(pythonResult.get("latency_ms")));
        record.setCreatedAt(LocalDateTime.now());
        detectionRecordMapper.insert(record);

        // 10. 异步保存环境数据
        asyncSaveEnvironment(userId, record.getId());

        // 11. 构建响应返回
        DetectionResponse response = new DetectionResponse();
        response.setRecordId(record.getId());
        response.setImageUrl(uploadResult.url());
        response.setEmotionLabel(emotionLabel);
        response.setEmotionCn(emotionCn);
        response.setConfidence(confidence);
        response.setFaceCount(record.getFaceCount());
        response.setValidFaceCount(validCount);
        response.setDetectDetail(detail);
        response.setAdviceText(adviceText);
        response.setAdviceSource(adviceSource);
        response.setAdviceSourceType(adviceSourceType);
        response.setProcessingTimeMs(record.getProcessingTimeMs());
        return response;
    }

    /**
     * 获取检测历史记录（分页查询）
     *
     * 支持多条件筛选：
     * - emotionLabel: 情感标签筛选
     * - startDate: 开始时间筛选
     * - endDate: 结束时间筛选
     *
     * 默认按创建时间倒序排列
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页记录数
     * @param emotionLabel 情感标签筛选（可选）
     * @param startDate 开始日期筛选（可选）
     * @param endDate 结束日期筛选（可选）
     * @return 分页的历史记录
     */
    @Override
    public PageResult<DetectionHistoryResponse> getHistory(Long userId, int page, int size,
                                                            String emotionLabel, String startDate, String endDate) {
        // 分页查询
        Page<DetectionRecord> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<DetectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DetectionRecord::getUserId, userId);

        // 情感标签筛选
        if (emotionLabel != null && !emotionLabel.isBlank()) {
            wrapper.eq(DetectionRecord::getEmotionLabel, emotionLabel);
        }

        // 日期范围筛选
        if (startDate != null && !startDate.isBlank()) {
            wrapper.ge(DetectionRecord::getCreatedAt, parseDateTime(startDate));
        }
        if (endDate != null && !endDate.isBlank()) {
            wrapper.le(DetectionRecord::getCreatedAt, parseDateTime(endDate + " 23:59:59"));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(DetectionRecord::getCreatedAt);

        // 执行查询
        detectionRecordMapper.selectPage(pageParam, wrapper);

        // 构建响应数据
        List<DetectionHistoryResponse> records = pageParam.getRecords().stream().map(r -> {
            DetectionHistoryResponse resp = new DetectionHistoryResponse();
            resp.setRecordId(r.getId());
            resp.setImageUrl(r.getImageUrl());
            resp.setEmotionLabel(r.getEmotionLabel());
            resp.setEmotionCn(r.getEmotionCn());
            resp.setConfidence(r.getConfidence());
            resp.setAdviceSourceType(r.getAdviceSourceType());
            resp.setCreatedAt(r.getCreatedAt());
            return resp;
        }).toList();

        return PageResult.of(pageParam.getTotal(), page, size, records);
    }

    @Override
    public DetectionDetailResponse getDetail(Long userId, Long recordId) {
        DetectionRecord record = detectionRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.DETECTION_RECORD_NOT_FOUND);
        }

        DetectionDetailResponse resp = new DetectionDetailResponse();
        resp.setRecordId(record.getId());
        resp.setImageUrl(record.getImageUrl());
        resp.setObjectKey(record.getObjectKey());
        resp.setEmotionLabel(record.getEmotionLabel());
        resp.setEmotionCn(record.getEmotionCn());
        resp.setConfidence(record.getConfidence());
        resp.setFaceCount(record.getFaceCount());
        resp.setValidFaceCount(record.getValidFaceCount());
        resp.setModelGender(record.getModelGender());
        resp.setModelAge(record.getModelAge());
        resp.setModelRange(record.getModelRange());
        resp.setDetectDetail(record.getDetectDetail());
        resp.setAdviceText(record.getAdviceText());
        resp.setAdviceSource(record.getAdviceSource());
        resp.setAdviceSourceType(record.getAdviceSourceType());
        resp.setProcessingTimeMs(record.getProcessingTimeMs());
        resp.setCreatedAt(record.getCreatedAt());

        EmotionFeedback feedback = emotionFeedbackMapper.selectByRecordIdAndUserId(recordId, userId);
        if (feedback != null) {
            DetectionDetailResponse.FeedbackInfo fb = new DetectionDetailResponse.FeedbackInfo();
            fb.setIsHelpful(feedback.getStatus() == 1);
            fb.setFeedbackText(feedback.getDescription());
            resp.setFeedback(fb);
        }

        EnvironmentRecord env = environmentRecordMapper.selectByDetectionRecordId(recordId);
        if (env != null) {
            DetectionDetailResponse.EnvironmentInfo envInfo = new DetectionDetailResponse.EnvironmentInfo();
            envInfo.setWeatherMain(env.getWeatherMain());
            envInfo.setTemperature(env.getTemperature());
            envInfo.setHumidity(env.getHumidity());
            envInfo.setWindDir(env.getWindDir());
            envInfo.setWindScale(env.getWindScale());
            envInfo.setLocationName(env.getLocationName());
            envInfo.setIndoorOutDoor(env.getIndoorOutdoor());
            resp.setEnvironment(envInfo);
        }

        return resp;
    }

    @Override
    @Transactional
    public void submitFeedback(Long userId, Long recordId, FeedbackRequest request) {
        DetectionRecord record = detectionRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.DETECTION_RECORD_NOT_FOUND);
        }

        int count = emotionFeedbackMapper.countByRecordId(recordId);
        if (count > 0) {
            throw new BizException(ErrorCode.FEEDBACK_ALREADY_SUBMITTED);
        }

        EmotionFeedback feedback = new EmotionFeedback();
        feedback.setUserId(userId);
        feedback.setRecordId(recordId);
        feedback.setImageUrl(record.getImageUrl());
        feedback.setObjectKey(record.getObjectKey());
        feedback.setSystemLabel(record.getEmotionLabel());
        feedback.setCorrectLabel(request.getCorrectLabel());
        feedback.setDescription(request.getDescription());
        feedback.setStatus(0);
        feedback.setCreatedAt(LocalDateTime.now());
        emotionFeedbackMapper.insert(feedback);
    }

    @Override
    public FeedbackStatusResponse getFeedbackStatus(Long userId, Long recordId) {
        EmotionFeedback feedback = emotionFeedbackMapper.selectByRecordIdAndUserId(recordId, userId);
        if (feedback == null) {
            return null;
        }

        FeedbackStatusResponse resp = new FeedbackStatusResponse();
        resp.setFeedbackId(feedback.getId());
        resp.setSystemLabel(feedback.getSystemLabel());
        resp.setCorrectLabel(feedback.getCorrectLabel());
        resp.setStatus(feedback.getStatus());
        resp.setStatusText(resolveStatusText(feedback.getStatus()));
        resp.setAdminNote(feedback.getReviewedBy() != null ? "已审核" : null);
        resp.setCreatedAt(feedback.getCreatedAt());
        resp.setReviewedAt(feedback.getReviewedAt());
        return resp;
    }

    @Override
    public DetectionStatsResponse getStats(Long userId) {
        List<DetectionRecord> records = detectionRecordMapper.selectList(
                new LambdaQueryWrapper<DetectionRecord>()
                        .eq(DetectionRecord::getUserId, userId));

        DetectionStatsResponse resp = new DetectionStatsResponse();
        resp.setTotalCount(records.size());

        List<String> emotionOrder = List.of(
                "happy", "sad", "angry", "neutral", "fear", "surprise", "disgust", "unknown");
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String label : emotionOrder) {
            counts.put(label, 0L);
        }
        for (DetectionRecord r : records) {
            String label = r.getEmotionLabel();
            if (counts.containsKey(label)) {
                counts.merge(label, 1L, Long::sum);
            } else {
                counts.merge("unknown", 1L, Long::sum);
            }
        }
        resp.setEmotionCounts(counts);
        return resp;
    }

    @Async
    public void asyncSaveEnvironment(Long userId, Long detectionRecordId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null || user.getLocationId() == null) {
                return;
            }

            AmapClient.WeatherLiveResult liveResult = amapClient.getLiveWeather(user.getLocationId());
            if (liveResult == null || liveResult.getLives() == null || liveResult.getLives().isEmpty()) {
                return;
            }

            AmapClient.WeatherLive live = liveResult.getLives().get(0);
            EnvironmentRecord env = new EnvironmentRecord();
            env.setUserId(userId);
            env.setDetectionRecordId(detectionRecordId);
            env.setSourceType(1);
            env.setWeatherMain(live.getWeather());
            env.setTemperature(parseBigDecimal(live.getTemperature()));
            env.setHumidity(parseInt(live.getHumidity()));
            env.setWindDir(live.getWindDirection());
            env.setWindScale(live.getWindPower());
            env.setLocationName(user.getCityName());
            env.setCreatedAt(LocalDateTime.now());
            environmentRecordMapper.insert(env);
            log.info("环境数据已保存: userId={}, recordId={}", userId, detectionRecordId);
        } catch (Exception e) {
            log.error("异步保存环境数据失败: userId={}, recordId={}", userId, detectionRecordId, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.IMAGE_FORMAT_ERROR);
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                && !contentType.equals("image/jpg"))) {
            throw new BizException(ErrorCode.IMAGE_FORMAT_ERROR);
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BizException(ErrorCode.IMAGE_TOO_LARGE);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findMainFace(List<Map<String, Object>> faces) {
        Map<String, Object> mainFace = null;
        double maxArea = 0;
        for (Map<String, Object> face : faces) {
            Boolean skip = (Boolean) face.get("skip");
            if (Boolean.TRUE.equals(skip)) continue;

            List<Integer> bbox = (List<Integer>) face.get("bbox");
            if (bbox == null || bbox.size() < 4) continue;

            double area = (bbox.get(2) - bbox.get(0)) * (bbox.get(3) - bbox.get(1));
            if (area > maxArea) {
                maxArea = area;
                mainFace = face;
            }
        }
        if (mainFace == null) {
            throw new BizException(ErrorCode.NO_FACE_DETECTED);
        }
        return mainFace;
    }

    @SuppressWarnings("unchecked")
    private DetectionResponse.DetectDetail buildDetectDetail(List<Map<String, Object>> faces, Map<String, Object> pythonResult) {
        DetectionResponse.DetectDetail detail = new DetectionResponse.DetectDetail();
        List<DetectionResponse.FaceResult> faceResults = new ArrayList<>();

        for (Map<String, Object> face : faces) {
            DetectionResponse.FaceResult fr = new DetectionResponse.FaceResult();
            fr.setBbox((List<Integer>) face.get("bbox"));
            fr.setFaceConfidence(toDouble(face.get("face_confidence")));
            fr.setRejectReason((String) face.get("reject_reason"));

            List<Map<String, Object>> emotionsRaw = (List<Map<String, Object>>) face.get("all_emotions");
            if (emotionsRaw != null) {
                List<DetectionResponse.EmotionProb> emotions = new ArrayList<>();
                for (Map<String, Object> e : emotionsRaw) {
                    DetectionResponse.EmotionProb ep = new DetectionResponse.EmotionProb();
                    ep.setLabel((String) e.get("label"));
                    ep.setConfidence(toDouble(e.get("confidence")));
                    emotions.add(ep);
                }
                fr.setAllEmotions(emotions);
            }
            faceResults.add(fr);
        }

        detail.setFaces(faceResults);
        detail.setPipelineVersion("v1.0");
        detail.setModelVersion("yolov8n-cls-8class");
        return detail;
    }

    private String resolveStatusText(int status) {
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已采用";
            case 2 -> "已驳回";
            default -> "未知";
        };
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.isEmpty()) return null;
        try { return new BigDecimal(val); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String val) {
        if (val == null || val.isEmpty()) return null;
        try { return Integer.parseInt(val); } catch (Exception e) { return null; }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return LocalDateTime.now().minusDays(30);
        }
    }
}
