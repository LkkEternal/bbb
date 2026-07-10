package com.citrus.collector.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.collector.cache.RedisDetectionCache;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.event.DetectionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务 — 将 Redis 队列中的检测记录批量持久化到 PostgreSQL
 *
 * 设计目的：
 * - 设备上报时，先写入 Redis 实时缓存 + WebSocket 推送（毫秒级可见）
 * - 记录同时加入待持久化队列（detection:persist:queue）
 * - 此定时任务每隔 5 秒消费队列，批量写入 DB
 * - 批量写入后发布 DetectionCreatedEvent，触发预警评估和推送
 *
 * 去重策略：
 * - 按 device_id + top_class_code + device_timestamp（±5 秒窗口）判断是否已入库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DetectionPersistenceTask {

    private final RedisDetectionCache redisDetectionCache;
    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final ApplicationEventPublisher eventPublisher;

    /** 每次批量处理的最大条数 */
    private static final int BATCH_SIZE = 50;

    /**
     * 每 5 秒执行一次，将 Redis 队列中的检测记录批量写入 DB
     */
    @Scheduled(fixedDelay = 5000)
    public void flushPendingDetections() {
        long queueSize = redisDetectionCache.getPersistQueueSize();
        if (queueSize == 0) {
            return;
        }

        List<DiseaseDetection> batch = redisDetectionCache.pollPersistBatch(BATCH_SIZE);
        if (batch.isEmpty()) {
            return;
        }

        List<Long> detectionIds = new ArrayList<>(batch.size());
        try {
            for (DiseaseDetection record : batch) {
                // 按业务字段去重：相同设备 + 相同病害 + 时间差 5 秒内
                if (isDuplicate(record)) {
                    continue;
                }
                record.setCreatedAt(LocalDateTime.now());
                diseaseDetectionMapper.insert(record);
                detectionIds.add(record.getId());
            }

            if (!detectionIds.isEmpty()) {
                eventPublisher.publishEvent(new DetectionCreatedEvent(detectionIds));
                log.info("持久化 {} 条检测记录（跳过 {} 条重复）",
                        detectionIds.size(), batch.size() - detectionIds.size());
            } else {
                log.debug("队列中 {} 条均为重复记录，已跳过", batch.size());
            }
        } catch (Exception e) {
            log.error("持久化检测记录失败: {}", e.getMessage());
        }
    }

    /**
     * 检查是否已存在相同的检测记录
     */
    private boolean isDuplicate(DiseaseDetection record) {
        if (record.getDeviceId() == null || record.getTopClassCode() == null
                || record.getDeviceTimestamp() == null) {
            return false;
        }
        LocalDateTime timeMin = record.getDeviceTimestamp().minusSeconds(5);
        LocalDateTime timeMax = record.getDeviceTimestamp().plusSeconds(5);
        Long exists = diseaseDetectionMapper.selectCount(
                new LambdaQueryWrapper<DiseaseDetection>()
                        .eq(DiseaseDetection::getDeviceId, record.getDeviceId())
                        .eq(DiseaseDetection::getTopClassCode, record.getTopClassCode())
                        .between(DiseaseDetection::getDeviceTimestamp, timeMin, timeMax)
                        .last("LIMIT 1"));
        return exists != null && exists > 0;
    }

    /**
     * 每 60 秒报告一次队列积压情况
     */
    @Scheduled(fixedDelay = 60000)
    public void reportQueueStatus() {
        long queueSize = redisDetectionCache.getPersistQueueSize();
        if (queueSize > 0) {
            log.info("检测记录待持久化队列长度: {}", queueSize);
        }
    }
}
