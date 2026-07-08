package com.citrus.collector.cache;

import com.citrus.collector.entity.DiseaseDetection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket 实时推送的检测事件 DTO（轻量，不含大字段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionEventDTO {
    private Long id;
    private Long orchardId;
    private String orchardName;
    private String deviceCode;
    private String deviceName;
    private String topClassCode;
    private String topClassName;
    private BigDecimal topProbability;
    private String imageUrl;
    private LocalDateTime deviceTimestamp;
    private LocalDateTime createdAt;
    private Integer level;       // L1/L2/L3 预警等级（由事件处理器填充）
    private String advice;       // 防治建议

    /** 从 DiseaseDetection 实体构建 DTO（轻量字段） */
    public static DetectionEventDTO from(DiseaseDetection d) {
        return DetectionEventDTO.builder()
                .id(d.getId())
                .orchardId(d.getOrchardId())
                .topClassCode(d.getTopClassCode())
                .topClassName(d.getDiseaseNameRaw())
                .topProbability(d.getTopProbability())
                .imageUrl(d.getImageUrl())
                .deviceTimestamp(d.getDeviceTimestamp())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
