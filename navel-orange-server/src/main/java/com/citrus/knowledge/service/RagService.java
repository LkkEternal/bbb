package com.citrus.knowledge.service;

import com.citrus.knowledge.vo.ChunkSearchResult;
import com.citrus.knowledge.vo.RagResponse;

import java.util.List;

/**
 * RAG 检索与问答服务
 */
public interface RagService {

    /**
     * 纯向量检索（不调用 LLM）
     *
     * @param query       检索文本
     * @param diseaseName 限定病害（可选）
     * @param topK        返回条数
     */
    List<ChunkSearchResult> search(String query, String diseaseName, int topK);

    /**
     * RAG 问答（检索 + LLM 生成）
     *
     * @param question    用户问题
     * @param diseaseName 限定病害（可选）
     * @param orchardId   果园上下文（可选，用于获取环境数据）
     * @param model       指定 LLM 模型（可选）
     */
    RagResponse query(String question, String diseaseName, Long orchardId, String model);

    /**
     * 针对预警场景的快速检索（用于规则引擎触发后获取防治建议）
     *
     * @param diseaseName 病害名
     * @param temperature 当前温度
     * @param humidity    当前湿度
     */
    RagResponse queryForAlert(String diseaseName, Double temperature, Double humidity);

    /**
     * 针对病害识别触发的事件，查询防治建议（包含紧急程度、防治措施、后果等结构化输出）
     *
     * @param diseaseName 病害名
     * @param temperature 当前温度（可选）
     * @param humidity    当前湿度（可选）
     * @param phenology   当前物候期（可选）
     */
    RagResponse queryForDetection(String diseaseName, Double temperature, Double humidity, String phenology);
}
