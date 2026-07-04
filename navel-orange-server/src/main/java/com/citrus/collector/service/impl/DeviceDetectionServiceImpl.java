package com.citrus.collector.service.impl;

import com.citrus.collector.dto.DeviceDetectionDTO;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.event.DetectionCreatedEvent;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.service.DeviceDetectionService;
import com.citrus.orchard.entity.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDetectionServiceImpl implements DeviceDetectionService {

    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${huawei.obs.url-prefix:}")
    private String obsUrlPrefix;

    @Override
    @Transactional
    public Map<String, Object> handleDetection(Device device, DeviceDetectionDTO dto) {
        List<Long> detectionIds = new ArrayList<>();

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
            record.setProcessed(false);

            try {
                record.setAllProbabilities(objectMapper.writeValueAsString(probs));
                record.setExtraData(objectMapper.writeValueAsString(extra));
            } catch (Exception e) {
                log.error("序列化JSON失败: {}", e.getMessage());
            }

            diseaseDetectionMapper.insert(record);
            detectionIds.add(record.getId());

            log.info("设备[{}]上报病害[{}]，置信度: {}，记录ID: {}",
                    device.getDeviceCode(), classCode, item.getConf(), record.getId());
        }

        eventPublisher.publishEvent(new DetectionCreatedEvent(detectionIds));

        Map<String, Object> result = new HashMap<>();
        result.put("detectionIds", detectionIds);
        result.put("count", detectionIds.size());
        return result;
    }

    @Override
    @Transactional
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
