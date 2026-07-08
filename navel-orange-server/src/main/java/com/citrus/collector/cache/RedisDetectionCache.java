package com.citrus.collector.cache;

import com.citrus.collector.entity.DiseaseDetection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 检测数据缓存服务
 * 设备上报时先写入缓存（实时可见），再异步落库
 */
@Slf4j
@Service
public class RedisDetectionCache {

    /** 实时检测数据 Key 前缀 */
    private static final String KEY_DETECTION = "detection:recent:";
    /** 待持久化队列 Key */
    private static final String KEY_PERSIST_QUEUE = "detection:persist:queue";
    /** 缓存 TTL（5分钟） */
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    /** 缓存最大条数 */
    private static final int MAX_CACHED = 500;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisDetectionCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 将检测记录同时写入实时缓存 + 待持久化队列
     */
    public void cacheDetection(DiseaseDetection detection) {
        try {
            String json = objectMapper.writeValueAsString(detection);
            String key = KEY_DETECTION + detection.getOrchardId();

            // LPUSH 将新记录插入列表头部
            redisTemplate.opsForList().leftPush(key, json);
            // LTRIM 只保留前 MAX_CACHED 条
            redisTemplate.opsForList().trim(key, 0, MAX_CACHED - 1);
            // 设置 TTL
            redisTemplate.expire(key, CACHE_TTL);

            // 加入待持久化队列（右推，供定时任务从左侧消费）
            redisTemplate.opsForList().rightPush(KEY_PERSIST_QUEUE, json);
            redisTemplate.expire(KEY_PERSIST_QUEUE, Duration.ofHours(1));

        } catch (Exception e) {
            log.error("缓存检测记录到Redis失败: {}", e.getMessage());
        }
    }

    /**
     * 从待持久化队列中批量取出待入库的记录
     * @param batchSize 批量大小
     * @return 待持久化的检测记录列表
     */
    public List<DiseaseDetection> pollPersistBatch(int batchSize) {
        try {
            List<String> jsons = redisTemplate.opsForList().leftPop(KEY_PERSIST_QUEUE, batchSize);
            if (jsons == null || jsons.isEmpty()) {
                return Collections.emptyList();
            }
            List<DiseaseDetection> list = new ArrayList<>(jsons.size());
            for (String json : jsons) {
                try {
                    DiseaseDetection d = objectMapper.readValue(json, DiseaseDetection.class);
                    list.add(d);
                } catch (Exception e) {
                    log.warn("反序列化检测记录失败: {}", e.getMessage());
                }
            }
            return list;
        } catch (Exception e) {
            log.error("从Redis取出待持久化记录失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取果园最近的缓存检测记录
     * @param orchardId 果园ID
     * @param limit     最多取多少条
     */
    public List<DiseaseDetection> getRecentCached(Long orchardId, int limit) {
        try {
            String key = KEY_DETECTION + orchardId;
            List<String> jsons = redisTemplate.opsForList().range(key, 0, limit - 1);
            if (jsons == null || jsons.isEmpty()) {
                return Collections.emptyList();
            }
            List<DiseaseDetection> list = new ArrayList<>(jsons.size());
            for (String json : jsons) {
                try {
                    list.add(objectMapper.readValue(json, DiseaseDetection.class));
                } catch (Exception e) {
                    log.warn("反序列化缓存检测记录失败: {}", e.getMessage());
                }
            }
            return list;
        } catch (Exception e) {
            log.error("读取Redis缓存检测记录失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 查询队列长度 */
    public long getPersistQueueSize() {
        try {
            Long size = redisTemplate.opsForList().size(KEY_PERSIST_QUEUE);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
