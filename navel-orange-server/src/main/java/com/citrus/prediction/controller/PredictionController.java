package com.citrus.prediction.controller;

import com.citrus.common.result.Result;
import com.citrus.prediction.entity.RiskPrediction;
import com.citrus.prediction.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 风险预测查询接口（前端用）
 */
@Slf4j
@RestController
@RequestMapping("/api/prediction")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * 获取某个果园的最新预测结果
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatest(@RequestParam Long orchardId) {
        try {
            RiskPrediction latest = predictionService.getLatestPrediction(orchardId);
            if (latest == null) {
                return ResponseEntity.ok(Result.success(null));
            }
            return ResponseEntity.ok(Result.success(latest));
        } catch (Exception e) {
            log.error("获取最新预测失败 orchardId={}: {}", orchardId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "获取预测失败: " + e.getMessage()));
        }
    }

    /**
     * 获取某个果园的历史预测记录
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam Long orchardId,
            @RequestParam(defaultValue = "30") int limit) {
        try {
            return ResponseEntity.ok(Result.success(predictionService.getPredictionHistory(orchardId, limit)));
        } catch (Exception e) {
            log.error("获取预测历史失败 orchardId={}: {}", orchardId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "获取预测历史失败: " + e.getMessage()));
        }
    }

    /**
     * 手动触发某个果园的预测（管理员用）
     */
    @PostMapping("/trigger")
    public ResponseEntity<?> trigger(
            @RequestParam Long orchardId,
            @RequestParam(defaultValue = "manual") String triggerType) {
        try {
            RiskPrediction result = predictionService.evaluateOrchard(orchardId, triggerType);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("触发预测失败 orchardId={}: {}", orchardId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "触发预测失败: " + e.getMessage()));
        }
    }
}
