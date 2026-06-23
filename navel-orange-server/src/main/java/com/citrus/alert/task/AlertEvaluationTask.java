package com.citrus.alert.task;

import com.citrus.alert.service.AlertService;
import com.citrus.alert.vo.RiskAssessmentVO;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.orchard.entity.Orchard;
import com.citrus.push.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 预警定时评估任务
 * <p>
 * 每 5 分钟扫描所有果园，评估风险等级，
 * 对 L2/L3 级别自动生成预警消息。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEvaluationTask {

    private final OrchardMapper orchardMapper;
    private final AlertService alertService;
    private final MessageService messageService;

    /**
     * 每 5 分钟执行一次评估
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void evaluateAllOrchards() {
        log.info("开始定时预警评估 ...");

        // 查询所有未删除的果园
        List<Orchard> orchards = orchardMapper.selectList(null);
        int totalAlerts = 0;

        for (Orchard orchard : orchards) {
            if (Boolean.TRUE.equals(orchard.getDeleted())) {
                continue;
            }

            try {
                RiskAssessmentVO risk = alertService.assessRisk(orchard.getId());

                // 只对 L2 及以上级别创建预警消息
                if (risk.getRiskLevel() >= 2) {
                    String title = generateTitle(risk);
                    String content = generateContent(risk);
                    String advice = String.join("\n", risk.getSuggestions());

                    // 为果园的所有关联用户创建消息（简化版：记录果园级别预警）
                    Long messageId = messageService.createAlertMessage(
                            orchard.getId(),
                            null,   // userId = null 表示果园级预警
                            null,   // ruleId 留空（多条规则触发时）
                            risk.getRiskLevel(),
                            title,
                            content,
                            advice,
                            "alert"
                    );
                    totalAlerts++;
                    log.info("预警消息已生成: orchardId={}, level=L{}, messageId={}",
                            orchard.getId(), risk.getRiskLevel(), messageId);
                }
            } catch (Exception e) {
                log.error("评估果园预警失败: orchardId={}, error={}", orchard.getId(), e.getMessage());
            }
        }

        log.info("定时预警评估完成: 共评估 {} 个果园, 生成 {} 条预警消息", orchards.size(), totalAlerts);
    }

    private String generateTitle(RiskAssessmentVO risk) {
        return switch (risk.getRiskLevel()) {
            case 3 -> "🚨 高风险预警";
            case 2 -> "⚠️ 中风险预警";
            default -> "📋 低风险关注";
        };
    }

    private String generateContent(RiskAssessmentVO risk) {
        StringBuilder sb = new StringBuilder();
        sb.append("果园：").append(risk.getOrchardName()).append("\n");
        sb.append("风险等级：L").append(risk.getRiskLevel());
        sb.append("（").append(getLevelName(risk.getRiskLevel())).append("）\n");
        sb.append("风险评分：").append(risk.getRiskScore()).append("分\n\n");

        if (risk.getFactors() != null && !risk.getFactors().isEmpty()) {
            sb.append("触发因素：\n");
            for (RiskAssessmentVO.RiskFactor factor : risk.getFactors()) {
                sb.append("• ").append(factor.getDescription()).append("\n");
            }
        }

        return sb.toString();
    }

    private String getLevelName(int level) {
        return switch (level) {
            case 3 -> "紧急";
            case 2 -> "预警";
            default -> "关注";
        };
    }
}
