package com.emotion.chat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.chat.dto.request.AssessmentAnswerRequest;
import com.emotion.chat.dto.request.AssessmentStartRequest;
import com.emotion.chat.dto.response.*;
import com.emotion.chat.entity.*;
import com.emotion.chat.mapper.*;
import com.emotion.chat.service.AssessmentService;
import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 心理测评服务实现类
 * 负责心理测评的核心业务逻辑：
 * - 问卷信息管理
 * - 测评流程控制
 * - 进度跟踪
 * - 评分计算
 * - 结果生成
 *
 * 使用 Redis 缓存存储测评进度，确保实时性和数据一致性
 *
 * @author Emotion Team
 * @since 2025-07-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentQuestionnaireMapper questionnaireMapper;  // 问卷数据访问对象
    private final AssessmentQuestionMapper questionMapper;              // 题目数据访问对象
    private final AssessmentGradeRuleMapper gradeRuleMapper;            // 评分规则数据访问对象
    private final AssessmentRecordMapper recordMapper;                  // 测评记录数据访问对象
    private final StringRedisTemplate redisTemplate;                    // Redis模板用于进度缓存

    /** Redis 缓存键前缀：测评进度缓存 */
    private static final String REDIS_PROGRESS_KEY = "assessment:progress:";
    /** Redis 缓存过期时间（秒）：30分钟 */
    private static final long REDIS_TTL_SECONDS = 1800;

    /**
     * 获取所有激活的问卷列表
     *
     * @return 问卷列表响应，包含所有启用的问卷的基本信息
     */
    @Override
    public QuestionnaireListResponse getActiveQuestionnaires() {
        List<AssessmentQuestionnaire> list = questionnaireMapper.selectActiveList();
        QuestionnaireListResponse resp = new QuestionnaireListResponse();
        resp.setQuestionnaires(list.stream().map(q -> {
            QuestionnaireListResponse.QuestionnaireItem item = new QuestionnaireListResponse.QuestionnaireItem();
            item.setId(q.getId());
            item.setName(q.getName());
            item.setCode(q.getCode());
            item.setDescription(q.getDescription());
            item.setTotalQuestions(q.getTotalQuestions());
            return item;
        }).collect(Collectors.toList()));
        return resp;
    }

    /**
     * 开始一个新的测评
     *
     * 流程说明：
     * 1. 验证问卷是否有效且已启用
     * 2. 加载问卷的所有题目
     * 3. 生成唯一的评估记录ID
     * 4. 在 Redis 中初始化进度缓存
     * 5. 返回第一道题目供用户回答
     *
     * @param userId 用户ID
     * @param request 包含问卷ID的请求
     * @return 评估开始响应
     * @throws BizException 如果问卷不存在、未启用或无题目
     */
    @Override
    public AssessmentStartResponse startAssessment(Long userId, AssessmentStartRequest request) {
        // 1. 验证问卷是否存在且已启用
        AssessmentQuestionnaire questionnaire = questionnaireMapper.selectById(request.getQuestionnaireId());
        if (questionnaire == null || questionnaire.getIsActive() != 1) {
            throw new BizException(ErrorCode.BAD_REQUEST, "问卷不存在或未启用");
        }

        // 2. 加载该问卷的所有题目
        List<AssessmentQuestion> questions = questionMapper.selectByQuestionnaireId(request.getQuestionnaireId());
        if (questions.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该问卷暂无题目");
        }

        // 3. 生成唯一的评估记录ID（使用 Redis 自增）
        Long recordId = redisTemplate.opsForValue().increment("assessment:tempIdSeq");

        // 4. 在 Redis 中初始化进度缓存
        String redisKey = REDIS_PROGRESS_KEY + userId;
        Map<String, String> progress = new HashMap<>();
        progress.put("recordId", String.valueOf(recordId));
        progress.put("questionnaireId", String.valueOf(request.getQuestionnaireId()));
        progress.put("currentIndex", "0");
        progress.put("totalScore", "0");
        progress.put("startedAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(redisKey, progress);
        redisTemplate.expire(redisKey, REDIS_TTL_SECONDS, TimeUnit.SECONDS);

        // 5. 构建第一题的响应信息
        AssessmentQuestion firstQuestion = questions.get(0);
        AssessmentStartResponse resp = new AssessmentStartResponse();
        resp.setRecordId(recordId);
        resp.setQuestionnaireId(request.getQuestionnaireId());
        resp.setQuestionnaireName(questionnaire.getName());
        resp.setTotalQuestions(questions.size());
        resp.setCurrentQuestion(buildQuestionInfo(firstQuestion));
        return resp;
    }

    /**
     * 回答当前题目
     *
     * 流程说明：
     * 1. 从 Redis 获取当前进度
     * 2. 验证题目有效性
     * 3. 根据选择的选项计算分数
     * 4. 更新当前索引和总分
     * 5. 检查是否完成所有题目
     * 6. 如果完成，匹配评分规则并生成结果；否则返回下一题
     *
     * @param userId 用户ID
     * @param request 包含题目ID和选项的请求
     * @return 答题响应
     * @throws BizException 如果进度超时、记录不匹配或题目无效
     */
    @Override
    @Transactional
    public AssessmentAnswerResponse answerQuestion(Long userId, AssessmentAnswerRequest request) {
        // 1. 从 Redis 获取当前进度
        String redisKey = REDIS_PROGRESS_KEY + userId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BizException(ErrorCode.ASSESSMENT_TIMEOUT);
        }

        String recordIdStr = (String) redisTemplate.opsForHash().get(redisKey, "recordId");
        String currentIndexStr = (String) redisTemplate.opsForHash().get(redisKey, "currentIndex");
        String totalScoreStr = (String) redisTemplate.opsForHash().get(redisKey, "totalScore");

        // 2. 验证评估记录ID
        if (recordIdStr == null || !recordIdStr.equals(String.valueOf(request.getRecordId()))) {
            throw new BizException(ErrorCode.BAD_REQUEST, "评估记录不匹配");
        }

        // 3. 验证题目有效性
        AssessmentQuestion question = questionMapper.selectActiveById(request.getQuestionId());
        if (question == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "题目不存在或已禁用");
        }

        // 4. 计算得分
        int score = getScoreByOption(question, request.getSelectedOption());

        // 5. 更新进度
        int currentIndex = Integer.parseInt(currentIndexStr) + 1;
        int totalScore = Integer.parseInt(totalScoreStr) + score;

        List<AssessmentQuestion> allQuestions = questionMapper.selectByQuestionnaireId(question.getQuestionnaireId());
        boolean isFinished = currentIndex >= allQuestions.size();

        // 6. 记录所有选项的得分（用于可能的回退）
        String scoresStr = (String) redisTemplate.opsForHash().get(redisKey, "scores");
        scoresStr = (scoresStr == null || scoresStr.isBlank()) ? "" : scoresStr + ",";
        scoresStr = scoresStr + score;

        redisTemplate.opsForHash().put(redisKey, "currentIndex", String.valueOf(currentIndex));
        redisTemplate.opsForHash().put(redisKey, "totalScore", String.valueOf(totalScore));
        redisTemplate.opsForHash().put(redisKey, "scores", scoresStr);
        redisTemplate.expire(redisKey, REDIS_TTL_SECONDS, TimeUnit.SECONDS);

        AssessmentAnswerResponse resp = new AssessmentAnswerResponse();

        // 7. 如果完成所有题目
        if (isFinished) {
            // 匹配评分规则
            AssessmentGradeRule matchedRule = gradeRuleMapper.matchRule(question.getQuestionnaireId(), totalScore);
            if (matchedRule == null) {
                throw new BizException(ErrorCode.INTERNAL_ERROR, "未找到匹配的评分规则");
            }

            resp.setAllAnswered(true);
            AssessmentAnswerResponse.AssessmentResult result = new AssessmentAnswerResponse.AssessmentResult();
            result.setRecordId(request.getRecordId());
            result.setTotalScore(totalScore);
            result.setLevelCode(matchedRule.getLevelCode());
            result.setLevelName(matchedRule.getLevelName());
            result.setAdvice(matchedRule.getAdviceText());
            resp.setResult(result);
            return resp;
        } else {
            // 返回下一题
            AssessmentQuestion nextQuestion = allQuestions.get(currentIndex);
            resp.setFinished(false);
            resp.setCurrentQuestion(buildQuestionInfo(nextQuestion));
            AssessmentAnswerResponse.ProgressInfo progress = new AssessmentAnswerResponse.ProgressInfo();
            progress.setCurrent(currentIndex + 1);
            progress.setTotal(allQuestions.size());
            resp.setProgress(progress);
            return resp;
        }
    }

    /**
     * 返回上一题
     *
     * 流程说明：
     * 1. 验证进度是否存在且记录ID匹配
     * 2. 检查是否已经是第一题
     * 3. 从分数记录中移除上一题的分数
     * 4. 减少当前索引和总分
     * 5. 更新 Redis 进度
     * 6. 返回上一题的信息
     *
     * @param userId 用户ID
     * @param recordId 评估记录ID
     * @return 答题响应，包含上一题信息
     * @throws BizException 如果进度超时、记录不匹配或已到第一题
     */
    @Override
    public AssessmentAnswerResponse goBackQuestion(Long userId, Long recordId) {
        // 1. 验证进度是否存在
        String redisKey = REDIS_PROGRESS_KEY + userId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BizException(ErrorCode.ASSESSMENT_TIMEOUT);
        }

        String recordIdStr = (String) redisTemplate.opsForHash().get(redisKey, "recordId");
        String currentIndexStr = (String) redisTemplate.opsForHash().get(redisKey, "currentIndex");
        String totalScoreStr = (String) redisTemplate.opsForHash().get(redisKey, "totalScore");
        String scoresStr = (String) redisTemplate.opsForHash().get(redisKey, "scores");

        // 2. 验证评估记录ID
        if (recordIdStr == null || !recordIdStr.equals(String.valueOf(recordId))) {
            throw new BizException(ErrorCode.BAD_REQUEST, "评估记录不匹配");
        }

        // 3. 检查是否是第一题
        int currentIndex = Integer.parseInt(currentIndexStr);
        if (currentIndex <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "已经是第一题，无法回退");
        }

        currentIndex--;

        // 4. 从分数记录中移除上一题的分数
        int lastScore = 0;
        if (scoresStr != null && !scoresStr.isBlank()) {
            String[] parts = scoresStr.split(",");
            lastScore = Integer.parseInt(parts[parts.length - 1]);
            scoresStr = String.join(",", Arrays.copyOf(parts, parts.length - 1));
        }

        // 5. 更新总分和进度
        int totalScore = Integer.parseInt(totalScoreStr) - lastScore;

        redisTemplate.opsForHash().put(redisKey, "currentIndex", String.valueOf(currentIndex));
        redisTemplate.opsForHash().put(redisKey, "totalScore", String.valueOf(totalScore));
        redisTemplate.opsForHash().put(redisKey, "scores", scoresStr);
        redisTemplate.expire(redisKey, REDIS_TTL_SECONDS, TimeUnit.SECONDS);

        // 6. 获取上一题并返回
        String questionnaireIdStr = (String) redisTemplate.opsForHash().get(redisKey, "questionnaireId");
        List<AssessmentQuestion> allQuestions = questionMapper.selectByQuestionnaireId(Long.parseLong(questionnaireIdStr));
        AssessmentQuestion prevQuestion = allQuestions.get(currentIndex);

        AssessmentAnswerResponse resp = new AssessmentAnswerResponse();
        resp.setFinished(false);
        resp.setCurrentQuestion(buildQuestionInfo(prevQuestion));
        AssessmentAnswerResponse.ProgressInfo progress = new AssessmentAnswerResponse.ProgressInfo();
        progress.setCurrent(currentIndex + 1);
        progress.setTotal(allQuestions.size());
        resp.setProgress(progress);
        return resp;
    }

    /**
     * 提交评估结果
     *
     * 流程说明：
     * 1. 验证进度是否存在
     * 2. 验证评估记录ID
     * 3. 匹配评分规则
     * 4. 将测评记录保存到数据库
     * 5. 清除 Redis 中的进度缓存
     * 6. 返回最终结果
     *
     * @param userId 用户ID
     * @param recordId 评估记录ID
     * @return 提交响应
     * @throws BizException 如果进度超时、记录不匹配或未找到评分规则
     */
    @Override
    @Transactional
    public AssessmentAnswerResponse submitAssessment(Long userId, Long recordId) {
        // 1. 验证进度是否存在
        String redisKey = REDIS_PROGRESS_KEY + userId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BizException(ErrorCode.ASSESSMENT_TIMEOUT);
        }

        // 2. 获取最终分数
        String recordIdStr = (String) redisTemplate.opsForHash().get(redisKey, "recordId");
        String totalScoreStr = (String) redisTemplate.opsForHash().get(redisKey, "totalScore");
        String questionnaireIdStr = (String) redisTemplate.opsForHash().get(redisKey, "questionnaireId");
        String startedAtStr = (String) redisTemplate.opsForHash().get(redisKey, "startedAt");

        // 3. 验证评估记录ID
        if (recordIdStr == null || !recordIdStr.equals(String.valueOf(recordId))) {
            throw new BizException(ErrorCode.BAD_REQUEST, "评估记录不匹配");
        }

        int totalScore = Integer.parseInt(totalScoreStr);

        // 4. 匹配评分规则
        AssessmentGradeRule matchedRule = gradeRuleMapper.matchRule(Long.parseLong(questionnaireIdStr), totalScore);
        if (matchedRule == null) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "未找到匹配的评分规则");
        }

        // 5. 保存测评记录到数据库
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setQuestionnaireId(Long.parseLong(questionnaireIdStr));
        record.setTotalScore(totalScore);
        record.setLevelCode(matchedRule.getLevelCode());
        record.setLevelName(matchedRule.getLevelName());
        record.setAdviceText(matchedRule.getAdviceText());
        record.setStatus("completed");
        record.setStartedAt(startedAtStr != null
                ? LocalDateTime.ofEpochSecond(Long.parseLong(startedAtStr) / 1000, 0, java.time.ZoneOffset.ofHours(8))
                : LocalDateTime.now());
        record.setCompletedAt(LocalDateTime.now());
        recordMapper.insert(record);

        // 6. 清除 Redis 中的进度缓存
        redisTemplate.delete(redisKey);

        // 7. 返回最终结果
        AssessmentAnswerResponse resp = new AssessmentAnswerResponse();
        resp.setFinished(true);
        AssessmentAnswerResponse.AssessmentResult result = new AssessmentAnswerResponse.AssessmentResult();
        result.setRecordId(record.getId());
        result.setTotalScore(totalScore);
        result.setLevelCode(matchedRule.getLevelCode());
        result.setLevelName(matchedRule.getLevelName());
        result.setAdvice(matchedRule.getAdviceText());
        result.setCompletedAt(record.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        resp.setResult(result);
        return resp;
    }

    /**
     * 获取用户的测评历史记录
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页记录数（默认10，最大50）
     * @return 历史记录响应，包含分页信息和记录详情
     */
    @Override
    public AssessmentHistoryResponse getHistory(Long userId, int page, int size) {
        // 分页查询
        Page<AssessmentRecord> pageParam = new Page<>(page, size);
        recordMapper.selectHistoryPage(pageParam, userId);

        // 构建响应数据
        AssessmentHistoryResponse resp = new AssessmentHistoryResponse();
        resp.setTotal(pageParam.getTotal());

        List<AssessmentHistoryResponse.HistoryRecord> records = pageParam.getRecords().stream().map(r -> {
            AssessmentHistoryResponse.HistoryRecord hr = new AssessmentHistoryResponse.HistoryRecord();
            hr.setRecordId(r.getId());
            hr.setQuestionnaireId(r.getQuestionnaireId());
            hr.setTotalScore(r.getTotalScore());
            hr.setLevelCode(r.getLevelCode());
            hr.setLevelName(r.getLevelName());
            hr.setAdviceText(r.getAdviceText());

            // 获取问卷名称和代码
            AssessmentQuestionnaire q = questionnaireMapper.selectById(r.getQuestionnaireId());
            if (q != null) {
                hr.setQuestionnaireName(q.getName());
                hr.setQuestionnaireCode(q.getCode());
            }

            // 格式化完成时间
            if (r.getCompletedAt() != null) {
                hr.setCompletedAt(r.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            return hr;
        }).collect(Collectors.toList());
        resp.setRecords(records);
        return resp;
    }

    /**
     * 获取测评记录的详细信息
     *
     * @param userId 用户ID
     * @param recordId 测评记录ID
     * @return 测评详情响应，包含完整的测评信息
     * @throws BizException 如果记录不存在或无权访问
     */
    @Override
    public AssessmentDetailResponse getDetail(Long userId, Long recordId) {
        // 查询测评记录（验证所有权）
        AssessmentRecord record = recordMapper.selectByIdAndUser(recordId, userId);
        if (record == null) {
            throw new BizException(ErrorCode.ASSESSMENT_NOT_FOUND);
        }

        // 构建响应数据
        AssessmentDetailResponse resp = new AssessmentDetailResponse();
        resp.setRecordId(record.getId());
        resp.setQuestionnaireId(record.getQuestionnaireId());
        resp.setTotalScore(record.getTotalScore());
        resp.setLevelCode(record.getLevelCode());
        resp.setLevelName(record.getLevelName());
        resp.setAdvice(record.getAdviceText());

        // 获取问卷信息
        AssessmentQuestionnaire q = questionnaireMapper.selectById(record.getQuestionnaireId());
        if (q != null) {
            resp.setQuestionnaireName(q.getName());
            resp.setQuestionnaireCode(q.getCode());
        }

        // 格式化时间
        if (record.getStartedAt() != null) {
            resp.setStartedAt(record.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (record.getCompletedAt() != null) {
            resp.setCompletedAt(record.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return resp;
    }

    /**
     * 构建题目信息对象
     * 将数据库实体转换为响应对象，包含题目文本和选项列表
     *
     * @param question 问卷题目实体
     * @return 题目信息响应对象
     */
    private AssessmentStartResponse.QuestionInfo buildQuestionInfo(AssessmentQuestion question) {
        AssessmentStartResponse.QuestionInfo info = new AssessmentStartResponse.QuestionInfo();
        info.setQuestionId(question.getId());
        info.setQuestionIndex(question.getQuestionIndex());
        info.setQuestionText(question.getQuestionText());

        List<AssessmentStartResponse.OptionInfo> options = new ArrayList<>();
        options.add(buildOption("A", question.getOptionA(), question.getScoreA()));
        options.add(buildOption("B", question.getOptionB(), question.getScoreB()));
        options.add(buildOption("C", question.getOptionC(), question.getScoreC()));
        options.add(buildOption("D", question.getOptionD(), question.getScoreD()));
        info.setOptions(options);
        return info;
    }

    /**
     * 构建选项信息对象
     *
     * @param label 选项标签（A/B/C/D）
     * @param text 选项文本
     * @param value 选项对应的分数
     * @return 选项信息响应对象
     */
    private AssessmentStartResponse.OptionInfo buildOption(String label, String text, Integer value) {
        AssessmentStartResponse.OptionInfo opt = new AssessmentStartResponse.OptionInfo();
        opt.setLabel(label);
        opt.setText(text);
        opt.setValue(value);
        return opt;
    }

    /**
     * 根据选项获取对应分数
     *
     * @param question 问卷题目实体
     * @param option 用户选择的选项（A/B/C/D）
     * @return 选项对应的分数
     * @throws IllegalArgumentException 如果选项无效
     */
    private int getScoreByOption(AssessmentQuestion question, String option) {
        return switch (option) {
            case "A" -> question.getScoreA();
            case "B" -> question.getScoreB();
            case "C" -> question.getScoreC();
            case "D" -> question.getScoreD();
            default -> throw new IllegalArgumentException("无效选项: " + option);
        };
    }
}
