package com.emotion.chat.service.impl;

import com.emotion.chat.dto.response.AiParseResultResponse;
import com.emotion.chat.entity.AssessmentGradeRule;
import com.emotion.chat.entity.AssessmentQuestion;
import com.emotion.chat.entity.AssessmentQuestionnaire;
import com.emotion.chat.mapper.AssessmentGradeRuleMapper;
import com.emotion.chat.mapper.AssessmentQuestionMapper;
import com.emotion.chat.mapper.AssessmentQuestionnaireMapper;
import com.emotion.chat.service.DocumentParseService;
import com.emotion.common.exception.BizException;
import com.emotion.common.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseServiceImpl implements DocumentParseService {

    private final AssessmentQuestionnaireMapper questionnaireMapper;
    private final AssessmentQuestionMapper questionMapper;
    private final AssessmentGradeRuleMapper gradeRuleMapper;

    private static final byte[] OOXML_MAGIC = { 0x50, 0x4B };
    private static final byte[] OLE2_MAGIC = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 };

    @Override
    public String extractText(byte[] fileBytes, String fileName) {
        if (fileName == null || !fileName.toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("仅支持 .docx 格式的文件");
        }
        if (fileBytes.length < 4) {
            throw new IllegalArgumentException("文件内容为空或已损坏");
        }
        if (startsWith(fileBytes, OLE2_MAGIC)) {
            throw new IllegalArgumentException("文件格式为旧版 .doc（OLE2），请另存为 .docx 后重新上传");
        }
        if (!startsWith(fileBytes, OOXML_MAGIC)) {
            throw new IllegalArgumentException("无法识别的文件格式，请确认是有效的 .docx 文件");
        }
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(fileBytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            if (text == null || text.isBlank()) {
                throw new RuntimeException("文档内容为空，无法解析");
            }
            log.info("DOCX解析完成: {}, 提取文本长度: {}", fileName, text.length());
            return text;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("DOCX解析失败: {}", fileName, e);
            throw new RuntimeException("文档解析失败: " + e.getMessage());
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    @Override
    @Transactional
    public Long saveParsedResult(AiParseResultResponse parsed) {
        if (questionnaireMapper.selectByCode(parsed.getCode()) != null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "问卷编码 " + parsed.getCode() + " 已存在");
        }

        AssessmentQuestionnaire questionnaire = new AssessmentQuestionnaire();
        questionnaire.setName(parsed.getName());
        questionnaire.setCode(parsed.getCode());
        questionnaire.setDescription(parsed.getDescription());
        questionnaire.setTotalQuestions(parsed.getQuestions().size());
        questionnaire.setIsActive(1);
        questionnaire.setCreatedAt(LocalDateTime.now());
        questionnaire.setUpdatedAt(LocalDateTime.now());
        questionnaireMapper.insert(questionnaire);

        Long qId = questionnaire.getId();

        for (AiParseResultResponse.QuestionItem qi : parsed.getQuestions()) {
            AssessmentQuestion question = new AssessmentQuestion();
            question.setQuestionnaireId(qId);
            question.setQuestionIndex(qi.getQuestionIndex());
            question.setQuestionText(qi.getQuestionText());
            question.setOptionA(qi.getOptionA());
            question.setOptionB(qi.getOptionB());
            question.setOptionC(qi.getOptionC());
            question.setOptionD(qi.getOptionD());
            question.setScoreA(qi.getScoreA());
            question.setScoreB(qi.getScoreB());
            question.setScoreC(qi.getScoreC());
            question.setScoreD(qi.getScoreD());
            question.setIsActive(1);
            question.setCreatedAt(LocalDateTime.now());
            questionMapper.insert(question);
        }

        int sortOrder = 0;
        for (AiParseResultResponse.GradeRuleItem gri : parsed.getGradeRules()) {
            AssessmentGradeRule rule = new AssessmentGradeRule();
            rule.setQuestionnaireId(qId);
            rule.setMinScore(gri.getMinScore());
            rule.setMaxScore(gri.getMaxScore());
            rule.setLevelCode(gri.getLevelCode());
            rule.setLevelName(gri.getLevelName());
            rule.setAdviceText(gri.getAdviceText());
            rule.setSortOrder(sortOrder++);
            rule.setIsActive(1);
            rule.setCreatedAt(LocalDateTime.now());
            rule.setUpdatedAt(LocalDateTime.now());
            gradeRuleMapper.insert(rule);
        }

        log.info("问卷入库成功: id={}, name={}, 题目数={}, 规则数={}",
                qId, parsed.getName(), parsed.getQuestions().size(), parsed.getGradeRules().size());
        return qId;
    }
}
