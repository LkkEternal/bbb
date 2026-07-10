package com.citrus.prediction.task;

import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.prediction.entity.RiskPrediction;
import com.citrus.prediction.mapper.RiskPredictionMapper;
import com.citrus.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险预测定时任务
 * <p>
 * - 每天 6:00 全量预测（所有果园）
 * - 病害高发期（4-9月）额外在 14:00 执行一次
 * - 对上次预测为高风险(2/3)的果园，自动跟随下次全量扫描
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PredictionScheduledTask {

    /** 赣南脐橙病害高发月份 */
    private static final List<Integer> HIGH_RISK_MONTHS = List.of(4, 5, 6, 7, 8, 9);

    private final PredictionService predictionService;
    private final OrchardMapper orchardMapper;
    private final RiskPredictionMapper riskPredictionMapper;

    /**
     * 每天 6:00 执行全量预测（基础频率）
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void dailyPrediction() {
        runPrediction("每日定时预测");
    }

    /**
     * 高发期（4-9月）14:00 增加一次预测
     */
    @Scheduled(cron = "0 0 14 * * ?")
    public void peakSeasonAfternoonPrediction() {
        int month = LocalDateTime.now().getMonthValue();
        if (HIGH_RISK_MONTHS.contains(month)) {
            log.info("[PredictionTask] 高发期月份({}月), 执行午后预测", month);
            runPrediction("高发期午后预测");
        } else {
            log.debug("[PredictionTask] 非高发期月份({}月), 跳过午后预测", month);
        }
    }

    /**
     * 执行全量预测
     */
    private void runPrediction(String triggerDetail) {
        log.info("[PredictionTask] Starting prediction: triggerDetail={}", triggerDetail);

        List<Orchard> orchards = orchardMapper.selectList(null);
        int total = 0, highRisk = 0, failed = 0;

        for (Orchard orchard : orchards) {
            if (orchard.getDeleted() || orchard.getStatus() != 1) {
                continue;
            }
            try {
                var entity = predictionService.evaluateOrchard(
                        orchard.getId(), "scheduled", triggerDetail
                );
                total++;
                if (entity.getRiskLevel() >= 2) {
                    highRisk++;
                }
            } catch (Exception e) {
                failed++;
                log.error("  orchardId={} failed: {}", orchard.getId(), e.getMessage());
            }
        }

        log.info("[PredictionTask] Done. trigger={}, total={}, highRisk={}, failed={}",
                triggerDetail, total, highRisk, failed);
    }
}
