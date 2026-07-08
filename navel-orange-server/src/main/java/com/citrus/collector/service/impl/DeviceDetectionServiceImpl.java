package com.citrus.collector.service.impl;

import com.citrus.collector.cache.DetectionEventDTO;
import com.citrus.collector.cache.RedisDetectionCache;
import com.citrus.collector.dto.DeviceDetectionDTO;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.event.DetectionCreatedEvent;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.service.DeviceDetectionService;
import com.citrus.orchard.entity.Device;
import com.citrus.websocket.DetectionWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 设备检测上报服务 — 缓存优先 + WebSocket 推送 + 异步落库
 *
 * 处理流程：
 * 1. 解析设备上报数据，构建 DiseaseDetection 实体
 * 2. 写入 Redis 实时缓存（immediate，前端 WebSocket 实时可见）
 * 3. 通过 WebSocket 广播检测事件给所有前端客户端
 * 4. 将记录加入待持久化队列
 * 5. 异步定时任务批量写入 PostgreSQL（DetectionPersistenceTask）
 * 6. 发布 DetectionCreatedEvent（预警评估、推送等后续处理）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDetectionServiceImpl implements DeviceDetectionService {

    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisDetectionCache redisDetectionCache;
    private final DetectionWebSocketHandler webSocketHandler;

    @Value("${huawei.obs.url-prefix:}")
    private String obsUrlPrefix;

    @Override
    public Map<String, Object> handleDetection(Device device, DeviceDetectionDTO dto) {
        List<Long> detectionIds = new ArrayList<>();
        List<DiseaseDetection> records = new ArrayList<>();

        for (DeviceDetectionDTO.DiseaseItem item : dto.getDiseases()) {
            String classCode = extractClassCode(item.getName());
            DiseaseCatalog catalog = diseaseCatalogMapper.selectByCode(classCode);
            Integer catalogId = catalog != null ? catalog.getId() : null;

            String imageUrl = obsUrlPrefix + "/disease_records/" + item.getImg();

            LocalDateTime deviceTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(item.getTs()), ZoneId.of("Asia/Shanghai"));

            Map<String, BigDecimal> probs = new HashMap<>();
            probs.put(classCode, item.getConf());

            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("cnt", item.getCnt());
            extra.put("gps", dto.getGps());
            extra.put("device", dto.getDevice());

            DiseaseDetection record = new DiseaseDetection();
            record.setDeviceId(device.getId());
            record.setOrchardId(device.getOrchardId());
            record.setDiseaseCatalogId(catalogId);
            record.setDiseaseNameRaw(item.getName());
            record.setTopClassCode(classCode);
            record.setTopProbability(item.getConf());
            record.setImageUrl(imageUrl);
            record.setDeviceTimestamp(deviceTime);
            record.setCreatedAt(LocalDateTime.now());
            record.setProcessed(false);

            try {
                record.setAllProbabilities(objectMapper.writeValueAsString(probs));
                record.setExtraData(objectMapper.writeValueAsString(extra));
            } catch (Exception e) {
                log.error("序列化JSON失败: {}", e.getMessage());
            }

            // ① 写入 Redis 实时缓存（前端可立即查到）
            redisDetectionCache.cacheDetection(record);

            // ② WebSocket 广播检测事件（前端实时收到）
            DetectionEventDTO eventDTO = DetectionEventDTO.from(record);
            eventDTO.setDeviceCode(device.getDeviceCode());
            eventDTO.setDeviceName(device.getName());
            webSocketHandler.broadcast("detection_new", eventDTO);

            records.add(record);
            detectionIds.add(record.getId());

            log.info("设备[{}]上报病害[{}]，置信度: {}，已入缓存队列",
                    device.getDeviceCode(), classCode, item.getConf());
        }

        // ③ 发布事件（预警评估、推送等后续处理仍照常运行）
        //    事件监听器 DetectionCreatedEventHandler 中需要自行读取 DB，
        //    因此必须先落库才能发布事件，所以我们在此先批量插入 DB
        //    （这是即时写入，定时任务只负责"额外"的缓存队列落库）
        batchInsertAndPublish(records, detectionIds);

        Map<String, Object> result = new HashMap<>();
        result.put("detectionIds", detectionIds);
        result.put("count", detectionIds.size());
        return result;
    }

    /**
     * 批量插入 DB 并发布事件（落库是必须的，因为事件监听器依赖 DB 中的记录）
     * 缓存通过 Redis 缓存层实现前端实时可见
     */
    private void batchInsertAndPublish(List<DiseaseDetection> records, List<Long> detectionIds) {
        if (records.isEmpty()) return;
        try {
            for (DiseaseDetection record : records) {
                diseaseDetectionMapper.insert(record);
            }
            eventPublisher.publishEvent(new DetectionCreatedEvent(detectionIds));
            log.info("批量插入检测记录 {} 条，已发布事件", records.size());
        } catch (Exception e) {
            log.error("批量插入检测记录失败: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> handleBatchDetection(Device device, List<DeviceDetectionDTO> dtoList) {
        List<Long> allIds = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (DeviceDetectionDTO dto : dtoList) {
            try {
                Map<String, Object> result = handleDetection(device, dto);
                @SuppressWarnings("unchecked")
                List<Long> ids = (List<Long>) result.get("detectionIds");
                allIds.addAll(ids);
                successCount++;
            } catch (Exception e) {
                log.error("批量上报中单条处理失败: {}", e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("detectionIds", allIds);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        return result;
    }

    private String extractClassCode(String name) {
        if (name != null && name.startsWith("S")) {
            int idx = name.indexOf('_');
            return idx > 0 ? name.substring(0, idx) : name;
        }
        return name;
    }
}
