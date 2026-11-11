package com.emotion.chat.controller;

import com.emotion.chat.dto.response.AiParseResultResponse;
import com.emotion.chat.service.AiExtractService;
import com.emotion.chat.service.DocumentParseService;
import com.emotion.common.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/assessment/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentParseService documentParseService;
    private final AiExtractService aiExtractService;

    @PostMapping("/parse")
    public Result<AiParseResultResponse> parseDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "请上传文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".docx")) {
            return Result.error(400, "仅支持 .docx 格式的文件");
        }

        try {
            byte[] fileBytes = file.getBytes();
            String text = documentParseService.extractText(fileBytes, fileName);
            AiParseResultResponse result = aiExtractService.extractFromText(text);
            result.setName(result.getName() != null ? result.getName() : fileName.replace(".docx", ""));
            return Result.success("文档解析成功，请预览后确认", result);
        } catch (IOException e) {
            log.error("文件读取失败", e);
            return Result.error(400, "文件读取失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("文档解析异常", e);
            return Result.error(500, "文档解析失败: " + e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public Result<Long> confirmDocument(@RequestBody AiParseResultResponse parsed) {
        if (parsed.getName() == null || parsed.getName().isBlank()) {
            return Result.error(400, "问卷名称不能为空");
        }
        if (parsed.getCode() == null || parsed.getCode().isBlank()) {
            return Result.error(400, "问卷编码不能为空");
        }
        if (parsed.getQuestions() == null || parsed.getQuestions().isEmpty()) {
            return Result.error(400, "题目列表不能为空");
        }
        if (parsed.getGradeRules() == null || parsed.getGradeRules().isEmpty()) {
            return Result.error(400, "评分规则不能为空");
        }

        try {
            Long questionnaireId = documentParseService.saveParsedResult(parsed);
            return Result.success("问卷保存成功", questionnaireId);
        } catch (Exception e) {
            log.error("问卷保存失败", e);
            return Result.error(500, "问卷保存失败: " + e.getMessage());
        }
    }
}
