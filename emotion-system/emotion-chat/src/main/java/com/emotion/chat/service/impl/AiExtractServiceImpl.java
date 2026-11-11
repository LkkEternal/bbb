package com.emotion.chat.service.impl;

import com.emotion.chat.config.ZhipuProperties;
import com.emotion.chat.dto.response.AiParseResultResponse;
import com.emotion.chat.service.AiExtractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiExtractServiceImpl implements AiExtractService {

    private final ZhipuProperties zhipuProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public AiParseResultResponse extractFromText(String text) {
        String url = zhipuProperties.getBaseUrl() + "/chat/completions";

        String prompt = """
你是一位心理量表文档结构化解析专家。你所接收的文档严格遵循以下结构：

- 首行为量表名称
- 接着一段描述文字
- 然后出现表格"题目列表"，包含列：题号、题目内容、所属维度、0分、1分、2分、3分
- 接着"总分范围"
- 然后"总分区间对应的状况"表格，包含分数区间和程度判定
- 最后"建议"部分，包含每个区间的建议文字

请按以下规则提取：

1. name：从文档首行标题提取
2. code：从标题英文名提取缩写，如 Smiling Depression → SD；若文档无明确英文，根据内容合理取缩写
3. description：文档中标题后的描述段落
4. questions：从"题目列表"表格解析，questionIndex 取题号，questionText 取"题目内容"，optionA/optionB/optionC/optionD 分别对应 0分/1分/2分/3分 的列文字，scoreA=0, scoreB=1, scoreC=2, scoreD=3
5. gradeRules：从"总分区间对应的状况"表格解析，minScore/maxScore 取分数区间，levelName 取程度判定（如"无明显倾向"），levelCode 取拼音缩写（如 wxqxt/wdqx/zdqx/cdqx）；adviceText 从"建议"部分取对应区间的建议文字，尽可能完整保留原文

文档内容：
%s

返回 JSON 格式（必须严格符合此结构，不可增减字段）：
{
  "name": "量表名称",
  "code": "缩写编码",
  "description": "量表描述文字",
  "questions": [
    {
      "questionIndex": 1,
      "questionText": "题目文字",
      "optionA": "0分对应的选项",
      "optionB": "1分对应的选项",
      "optionC": "2分对应的选项",
      "optionD": "3分对应的选项",
      "scoreA": 0,
      "scoreB": 1,
      "scoreC": 2,
      "scoreD": 3
    }
  ],
  "gradeRules": [
    {
      "minScore": 0,
      "maxScore": 7,
      "levelCode": "wxqxt",
      "levelName": "无明显倾向",
      "adviceText": "建议文字"
    }
  ]
}
""".formatted(text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(zhipuProperties.getApiKey());

        Map<String, Object> requestBody = Map.of(
                "model", zhipuProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", "你是一个专业的心理量表文档结构化解析专家，擅长从标准化表格中提取题目、选项分值和评分规则。"),
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object")
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("调用智谱API解析文档...");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("智谱API调用失败: " + response.getStatusCode());
            }

            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("智谱API返回内容为空");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            log.info("智谱API解析完成, 返回长度: {}", content.length());

            return objectMapper.readValue(content, AiParseResultResponse.class);
        } catch (Exception e) {
            log.error("智谱API调用异常", e);
            throw new RuntimeException("AI解析失败: " + e.getMessage());
        }
    }
}
