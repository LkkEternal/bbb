package com.citrus.knowledge.service.impl;

import com.citrus.knowledge.config.PythonRagClient;
import com.citrus.knowledge.config.RagProperties;
import com.citrus.knowledge.mapper.KnowledgeChunkMapper;
import com.citrus.knowledge.service.RagService;
import com.citrus.knowledge.vo.ChunkSearchResult;
import com.citrus.knowledge.vo.RagResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * RAG 核心编排服务
 * <p>
 * 流程: 策略路由 → query构建 → 向量化 → pgvector检索 → 相关性门控 → LLM生成 → 返回
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final KnowledgeChunkMapper chunkMapper;
    private final PythonRagClient pythonRagClient;
    private final RagProperties properties;

    // ── 纯检索 ─────────────────────────────────────────────

    @Override
    public List<ChunkSearchResult> search(String query, String diseaseName, int topK) {
        // 1. 向量化查询
        float[] queryEmbedding = pythonRagClient.embed(query);

        // 2. 混合检索（向量+关键词）
        return chunkMapper.hybridSearch(queryEmbedding, query, properties.getMinScore(), topK);
    }

    // ── RAG 问答 ───────────────────────────────────────────

    @Override
    public RagResponse query(String question, String diseaseName, Long orchardId, String model) {
        // Step 1: 确定检索策略 → 构建 query text
        String strategy = resolveStrategy(question, diseaseName);
        String queryText = buildQueryText(question, diseaseName, strategy);

        // Step 2: 向量化
        float[] queryEmbedding = pythonRagClient.embed(queryText);

        // Step 3: 混合检索（向量+关键词）
        List<ChunkSearchResult> chunks = chunkMapper.hybridSearch(
                queryEmbedding, queryText, properties.getMinScore(), properties.getTopK());

        if (chunks.isEmpty()) {
            log.warn("No relevant chunks found for question: {}", question);
            // Relevance gate: 没有找到相关内容
            return buildNoAnswer(question, "知识库中暂未找到与您问题相关的信息，建议联系当地农技员获取专业指导。");
        }

        double maxScore = chunks.get(0).getSimilarity();
        log.info("RAG search: max_similarity={}, chunks={}", String.format("%.4f", maxScore), chunks.size());

        // Step 4: 取 top-5 作为 LLM 上下文
        List<ChunkSearchResult> topChunks = chunks.size() > 5 ? chunks.subList(0, 5) : chunks;

        // Step 6: 构建 prompt
        String prompt = buildPrompt(question, topChunks, diseaseName, strategy);

        // Step 7: Relevance Gate 2 — 模糊匹配时走 LLM，但降低温度
        double temperature = maxScore >= properties.getFuzzyMinScore() ? 0.3 : 0.2;

        // Step 8: 调用 LLM
        try {
            PythonRagClient.ChatResponse chatResp = pythonRagClient.chat(
                    List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", prompt)
                    ),
                    model
            );

            // Step 9: 组装返回
            RagResponse response = new RagResponse();
            response.setQuestion(question);
            response.setAnswer(chatResp.content());
            response.setReferences(topChunks);
            response.setConfidence(maxScore);
            response.setModel(chatResp.model());
            response.setSessionId(UUID.randomUUID().toString().replace("-", ""));
            return response;

        } catch (Exception e) {
            log.error("LLM call failed after fallback, returning raw search results: {}", e.getMessage());
            // 降级：LLM 不可用时，返回检索片段
            return buildRawSearchResponse(question, topChunks, maxScore);
        }
    }

    // ── 预警场景快速检索 ──────────────────────────────────

    @Override
    public RagResponse queryForAlert(String diseaseName, Double temperature, Double humidity) {
        // 防治检索策略
        String queryText = String.format("%s 防治方案 药剂 温度%.1f℃ 湿度%.0f%%",
                diseaseName, temperature != null ? temperature : 0, humidity != null ? humidity : 0);

        float[] queryEmbedding = pythonRagClient.embed(queryText);
        List<ChunkSearchResult> chunks = chunkMapper.hybridSearch(
                queryEmbedding, queryText, properties.getMinScore(), 5);

        if (chunks.isEmpty()) {
            return buildNoAnswer(diseaseName + "防治方案",
                    "当前知识库中没有找到" + diseaseName + "的防治资料，请使用默认防治方案。");
        }

        double maxScore = chunks.get(0).getSimilarity();

        try {
            String prompt = String.format(
                    ALERT_PROMPT_TEMPLATE,
                    diseaseName,
                    temperature != null ? temperature : "未知",
                    humidity != null ? humidity : "未知",
                    formatChunksForPrompt(chunks)
            );

            PythonRagClient.ChatResponse chatResp = pythonRagClient.chat(
                    List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", prompt)
                    ),
                    null
            );

            RagResponse response = new RagResponse();
            response.setQuestion(diseaseName + "防治方案");
            response.setAnswer(chatResp.content());
            response.setReferences(chunks);
            response.setConfidence(maxScore);
            response.setModel(chatResp.model());
            response.setSessionId(UUID.randomUUID().toString().replace("-", ""));
            return response;

        } catch (Exception e) {
            log.warn("LLM unavailable for alert, using raw chunks: {}", e.getMessage());
            return buildRawSearchResponse(diseaseName + "防治方案", chunks, maxScore);
        }
    }

    // ── 策略路由 ──────────────────────────────────────────

    private String resolveStrategy(String question, String diseaseName) {
        if (diseaseName != null && !diseaseName.isBlank()) {
            // 如果指定了病害名，优先走防治检索
            return "prevention";
        }
        // 判断是否关于症状描述
        String q = question.toLowerCase();
        if (q.contains("症状") || q.contains("什么样") || q.contains("特征") || q.contains("怎么判断")) {
            return "diagnosis";
        }
        // 包含节气或物候词
        if (q.contains("节气") || q.contains("物候") || q.contains("季节") || q.contains("月份")) {
            return "phenology";
        }
        // 默认防治
        return "prevention";
    }

    private String buildQueryText(String question, String diseaseName, String strategy) {
        return switch (strategy) {
            case "diagnosis" -> question + " 症状 特征 诊断";
            case "phenology" -> question + " 防治重点 农事";
            default -> {
                if (diseaseName != null && !diseaseName.isBlank()) {
                    yield diseaseName + " 防治方案 " + question;
                }
                yield question;
            }
        };
    }

    // ── Prompt 构建 ───────────────────────────────────────

    private String buildPrompt(String question, List<ChunkSearchResult> chunks,
                               String diseaseName, String strategy) {
        return String.format(
                QA_PROMPT_TEMPLATE,
                strategy,
                diseaseName != null ? diseaseName : "未知",
                formatChunksForPrompt(chunks),
                question
        );
    }

    private String formatChunksForPrompt(List<ChunkSearchResult> chunks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            ChunkSearchResult c = chunks.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append("相关性: ").append(String.format("%.2f", c.getSimilarity()))
                    .append("\n来源: ").append(c.getDocumentTitle() != null ? c.getDocumentTitle() : "知识库")
                    .append("\n内容: ").append(c.getContent())
                    .append("\n\n");
        }
        return sb.toString();
    }

    // ── 响应构建 ──────────────────────────────────────────

    private RagResponse buildNoAnswer(String question, String message) {
        RagResponse resp = new RagResponse();
        resp.setQuestion(question);
        resp.setAnswer(message);
        resp.setReferences(List.of());
        resp.setConfidence(0.0);
        resp.setModel("none");
        resp.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        return resp;
    }

    private RagResponse buildRawSearchResponse(String question, List<ChunkSearchResult> chunks, double confidence) {
        RagResponse resp = new RagResponse();
        resp.setQuestion(question);
        resp.setReferences(chunks);
        resp.setConfidence(confidence);
        resp.setModel("none (LLM unavailable)");
        resp.setSessionId(UUID.randomUUID().toString().replace("-", ""));

        // 将检索到的内容拼接为纯文本返回
        StringBuilder sb = new StringBuilder();
        sb.append("以下是从知识库中检索到的相关信息（仅供参考）：\n\n");
        for (ChunkSearchResult c : chunks) {
            sb.append("■ ").append(c.getDocumentTitle()).append("\n");
            sb.append(c.getContent()).append("\n\n");
        }
        resp.setAnswer(sb.toString());
        return resp;
    }

    // ── 检测事件 RAG 查询 ────────────────────────────────

    @Override
    public RagResponse queryForDetection(String diseaseName, Double temperature, Double humidity, String phenology) {
        String queryText = String.format("%s 防治方案 紧急程度 不处理的后果",
                diseaseName);

        float[] queryEmbedding = pythonRagClient.embed(queryText);
        List<ChunkSearchResult> chunks = chunkMapper.searchByVector(
                queryEmbedding, properties.getMinScore(), 5);

        if (chunks.isEmpty()) {
            return buildNoAnswer(diseaseName + "防治方案",
                    "当前知识库中没有找到" + diseaseName + "的防治资料，请咨询当地农技员。");
        }

        double maxScore = chunks.get(0).getSimilarity();
        if (maxScore < properties.getMinScore()) {
            return buildNoAnswer(diseaseName + "防治方案",
                    "当前知识库中没有找到" + diseaseName + "的防治资料，请咨询当地农技员。");
        }

        try {
            String prompt = String.format(
                    DETECTION_PROMPT_TEMPLATE,
                    diseaseName,
                    temperature != null ? String.format("%.1f", temperature) : "未知",
                    humidity != null ? String.format("%.0f", humidity) : "未知",
                    phenology != null && !phenology.isBlank() ? phenology : "未知",
                    formatChunksForPrompt(chunks)
            );

            PythonRagClient.ChatResponse chatResp = pythonRagClient.chat(
                    List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", prompt)
                    ),
                    null
            );

            RagResponse response = new RagResponse();
            response.setQuestion(diseaseName + "防治方案");
            response.setAnswer(chatResp.content());
            response.setReferences(chunks);
            response.setConfidence(maxScore);
            response.setModel(chatResp.model());
            response.setSessionId(UUID.randomUUID().toString().replace("-", ""));
            return response;

        } catch (Exception e) {
            log.warn("LLM unavailable for detection, using raw chunks: {}", e.getMessage());
            return buildRawSearchResponse(diseaseName + "防治方案", chunks, maxScore);
        }
    }

    // ── Prompt 模板 ───────────────────────────────────────

    private static final String SYSTEM_PROMPT = """
            你是一个专业的脐橙（柑橘）病虫害防治专家。
            回答规则：
            1. 严格基于提供的知识库内容回答，不要编造信息
            2. 如果知识库内容不足以回答问题，如实告知用户
            3. 回答要结构化：用数字列表列出关键要点
            4. 在回答末尾标注参考来源编号
            5. 使用农民能听懂的中文，避免过于专业的术语，必要时加括号解释
            6. 如果涉及药剂使用，务必加上安全提醒
            7. 语言简洁，突出重点，不要啰嗦
            """;

    private static final String QA_PROMPT_TEMPLATE = """
            【检索策略】%s
            【关联病害】%s
            
            以下是知识库中检索到的相关内容：
            %s
            
            请基于以上内容回答用户的问题。
            如果检索到的内容不足以回答问题，请告知用户哪些信息缺失。
            
            用户问题：%s
            """;

    private static final String ALERT_PROMPT_TEMPLATE = """
             请针对以下情况提供防治建议：
             
             病害：%s
             当前温度：%s℃
             当前湿度：%s%%
             
             知识库参考信息：
             %s
             
             请提供：
             1. 当前情况的风险评估
             2. 具体的防治措施（药剂名称、使用方法）
             3. 注意事项和安全提醒
             """;

    private static final String DETECTION_PROMPT_TEMPLATE = """
             用户果园检测到以下病害，请提供专业分析：
             
             病害：%s
             当前温度：%s℃
             当前湿度：%s%%
             当前物候期：%s
             
             知识库参考信息：
             %s
             
             请按以下结构输出，每个部分用【】括号包裹标题：
             
             【紧急程度】
             请判断该病害当前条件下的紧急程度，分为：高、中、低，并简要说明原因。
             
             【防治措施】
             列出具体的防治措施，包括药剂名称、使用浓度、使用方法。
             
             【不处理的后果】
             如果不及时处理，可能导致的后果。
             
             【防治建议摘要】
             用一句话总结当前最重要的防治建议。
             
             【预防措施】
             列出后续预防该病害的具体农事管理措施。
             """;
}
