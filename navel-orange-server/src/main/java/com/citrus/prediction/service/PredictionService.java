package com.citrus.prediction.service;

import com.citrus.prediction.client.PredictionClient;
import com.citrus.prediction.entity.RiskPrediction;
import com.citrus.prediction.mapper.RiskPredictionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 风险预测业务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionClient predictionClient;
    private final RiskPredictionMapper riskPredictionMapper;
    private final ObjectMapper objectMapper;

    /**
     * 对指定果园执行风险预测并保存结果
     *
     * @param orchardId   果园 ID
     * @param triggerType 触发类型
     * @return 保存后的预测结果实体
     */
    @Transactional
    public RiskPrediction evaluateOrchard(Long orchardId, String triggerType) {
        return evaluateOrchard(orchardId, triggerType, null);
    }

    /**
     * 对指定果园执行风险预测并保存结果
     *
     * @param orchardId     果园 ID
     * @param triggerType   触发类型
     * @param triggerDetail 触发详情（可选）
     * @return 保存后的预测结果实体
     */
    @Transactional
    public RiskPrediction evaluateOrchard(Long orchardId, String triggerType, String triggerDetail) {
        // 1. 调用 RAG 预测服务
        Map<String, Object> result = predictionClient.evaluateOrchard(orchardId);

        // 2. 构建实体
        RiskPrediction entity = new RiskPrediction();
        entity.setOrchardId(orchardId);
        entity.setRiskLevel((Integer) result.getOrDefault("risk_level", 0));
        entity.setRiskScore(BigDecimal.valueOf(
                ((Number) result.getOrDefault("risk_score",
                        ((List<Double>) result.getOrDefault("probabilities", List.of(0.0))).stream()
                                .mapToDouble(Double::doubleValue).max().orElse(0.0))
                ).doubleValue()
        ));

        try {
            entity.setProbabilities(objectMapper.writeValueAsString(result.get("probabilities")));
        } catch (Exception e) {
            entity.setProbabilities("[1,0,0,0]");
        }

        entity.setLlmAnalysis((String) result.getOrDefault("llm_analysis", ""));
        entity.setSuggestions((String) result.getOrDefault("suggestion", ""));

        try {
            entity.setTopFactors(objectMapper.writeValueAsString(result.get("top_factors")));
        } catch (Exception e) {
            entity.setTopFactors("[]");
        }

        entity.setTriggerType(triggerType);
        entity.setTriggerDetail(triggerDetail);
        entity.setCreatedAt(LocalDateTime.now());

        // 3. 入库
        riskPredictionMapper.insert(entity);

        log.info("Prediction saved: orchardId={}, riskLevel={}, triggerType={}",
                orchardId, entity.getRiskLevel(), triggerType);

        return entity;
    }

    /**
     * 获取某个果园的最新预测结果
     */
    public RiskPrediction getLatestPrediction(Long orchardId) {
        return riskPredictionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RiskPrediction>()
                        .eq(RiskPrediction::getOrchardId, orchardId)
                        .orderByDesc(RiskPrediction::getCreatedAt)
                        .last("LIMIT 1")
        );
    }

    /**
     * 获取某个果园的历史预测记录
     */
    public List<RiskPrediction> getPredictionHistory(Long orchardId, int limit) {
        return riskPredictionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RiskPrediction>()
                        .eq(RiskPrediction::getOrchardId, orchardId)
                        .orderByDesc(RiskPrediction::getCreatedAt)
                        .last("LIMIT " + limit)
        );
    }
}
