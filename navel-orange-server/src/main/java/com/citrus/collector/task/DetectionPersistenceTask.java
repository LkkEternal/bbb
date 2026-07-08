package com.citrus.collector.task;

import com.citrus.collector.cache.RedisDetectionCache;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.event.DetectionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务 — 将 Redis 缓存中的检测记录批量持久化到 PostgreSQL
 *
 * 设计目的：
 * - 设备上报时，先写入 Redis 实时缓存 + WebSocket 推送（毫秒级可见）
 * - 记录同时加入待持久化队列
 * - 此定时任务每隔 N 秒消费队列，批量写入 DB
 * - 批量写入后发布 DetectionCreatedEvent，触发预警评估和推送
 *
 * 这样前端通过 WebSocket 可以实时看到新数据，
 * 后端通过批量写入减少数据库压力。
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
                // 防止重复插入：检查 DB 中是否已有相同记录
                // （设备上报时 DeviceDetectionServiceImpl 已立即落库，
                //   此任务作为兜底/延迟优化，可能重复）
                if (record.getId() != null && diseaseDetectionMapper.selectById(record.getId()) != null) {
                    continue;
                }
                diseaseDetectionMapper.insert(record);
                detectionIds.add(record.getId());
            }

            if (!detectionIds.isEmpty()) {
                // 发布事件触发后续处理（预警评估、推送）
                eventPublisher.publishEvent(new DetectionCreatedEvent(detectionIds));
                log.info("定时持久化: {} 条检测记录入库并发布事件", detectionIds.size());
            } else {
                log.debug("定时持久化: 队列中 {} 条均为重复记录，已跳过", batch.size());
            }
        } catch (Exception e) {
            log.error("定时持久化检测记录失败: {}", e.getMessage());
        }
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
