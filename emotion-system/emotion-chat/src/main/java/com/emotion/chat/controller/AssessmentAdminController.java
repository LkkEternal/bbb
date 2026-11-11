package com.emotion.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emotion.chat.dto.response.QuestionnaireAdminListResponse;
import com.emotion.chat.dto.response.QuestionnaireDetailResponse;
import com.emotion.chat.entity.AssessmentGradeRule;
import com.emotion.chat.entity.AssessmentQuestion;
import com.emotion.chat.entity.AssessmentQuestionnaire;
import com.emotion.chat.mapper.AssessmentGradeRuleMapper;
import com.emotion.chat.mapper.AssessmentQuestionMapper;
import com.emotion.chat.mapper.AssessmentQuestionnaireMapper;
import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import com.emotion.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assessment/admin")
@RequiredArgsConstructor
public class AssessmentAdminController {

    private final AssessmentQuestionnaireMapper questionnaireMapper;
    private final AssessmentQuestionMapper questionMapper;
    private final AssessmentGradeRuleMapper gradeRuleMapper;

    @GetMapping("/questionnaires")
    public Result<QuestionnaireAdminListResponse> listQuestionnaires(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (size > 100) size = 100;
        Page<AssessmentQuestionnaire> pageParam = new Page<>(page, size);
        pageParam = questionnaireMapper.selectPage(pageParam, null);

        QuestionnaireAdminListResponse resp = new QuestionnaireAdminListResponse();
        resp.setTotal(pageParam.getTotal());
        resp.setPage(page);
        resp.setSize(size);

        List<QuestionnaireAdminListResponse.QuestionnaireAdminItem> items = pageParam.getRecords().stream().map(q -> {
            QuestionnaireAdminListResponse.QuestionnaireAdminItem item = new QuestionnaireAdminListResponse.QuestionnaireAdminItem();
            item.setId(q.getId());
            item.setName(q.getName());
            item.setCode(q.getCode());
            item.setDescription(q.getDescription());
            item.setTotalQuestions(q.getTotalQuestions());
            item.setIsActive(q.getIsActive());
            if (q.getCreatedAt() != null) {
                item.setCreatedAt(q.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (q.getUpdatedAt() != null) {
                item.setUpdatedAt(q.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            return item;
        }).collect(Collectors.toList());
        resp.setQuestionnaires(items);
        return Result.success(resp);
    }

    @GetMapping("/questionnaire/{id}")
    public Result<QuestionnaireDetailResponse> getQuestionnaireDetail(@PathVariable Long id) {
        AssessmentQuestionnaire q = questionnaireMapper.selectById(id);
        if (q == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "问卷不存在");
        }
        QuestionnaireDetailResponse resp = new QuestionnaireDetailResponse();
        resp.setId(q.getId());
        resp.setName(q.getName());
        resp.setCode(q.getCode());
        resp.setDescription(q.getDescription());
        resp.setTotalQuestions(q.getTotalQuestions());
        resp.setIsActive(q.getIsActive());
        if (q.getCreatedAt() != null) {
            resp.setCreatedAt(q.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (q.getUpdatedAt() != null) {
            resp.setUpdatedAt(q.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        List<AssessmentQuestion> questions = questionMapper.selectList(
                new QueryWrapper<AssessmentQuestion>().eq("questionnaire_id", id).orderByAsc("question_index"));
        resp.setQuestions(questions.stream().map(qi -> {
            QuestionnaireDetailResponse.QuestionDetail d = new QuestionnaireDetailResponse.QuestionDetail();
            d.setId(qi.getId());
            d.setQuestionIndex(qi.getQuestionIndex());
            d.setQuestionText(qi.getQuestionText());
            d.setOptionA(qi.getOptionA());
            d.setOptionB(qi.getOptionB());
            d.setOptionC(qi.getOptionC());
            d.setOptionD(qi.getOptionD());
            d.setScoreA(qi.getScoreA());
            d.setScoreB(qi.getScoreB());
            d.setScoreC(qi.getScoreC());
            d.setScoreD(qi.getScoreD());
            d.setIsActive(qi.getIsActive());
            return d;
        }).collect(Collectors.toList()));

        List<AssessmentGradeRule> rules = gradeRuleMapper.selectList(
                new QueryWrapper<AssessmentGradeRule>().eq("questionnaire_id", id).orderByAsc("sort_order"));
        resp.setGradeRules(rules.stream().map(r -> {
            QuestionnaireDetailResponse.GradeRuleDetail d = new QuestionnaireDetailResponse.GradeRuleDetail();
            d.setId(r.getId());
            d.setMinScore(r.getMinScore());
            d.setMaxScore(r.getMaxScore());
            d.setLevelCode(r.getLevelCode());
            d.setLevelName(r.getLevelName());
            d.setAdviceText(r.getAdviceText());
            d.setIsActive(r.getIsActive());
            return d;
        }).collect(Collectors.toList()));

        return Result.success(resp);
    }

    @PutMapping("/questionnaire/{id}/toggle-active")
    public Result<Void> toggleActive(@PathVariable Long id) {
        AssessmentQuestionnaire q = questionnaireMapper.selectById(id);
        if (q == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "问卷不存在");
        }
        int newActive = q.getIsActive() == 1 ? 0 : 1;
        q.setIsActive(newActive);
        q.setUpdatedAt(LocalDateTime.now());
        questionnaireMapper.updateById(q);
        String msg = newActive == 1 ? "已启用" : "已禁用";
        return Result.success(msg, null);
    }
}
