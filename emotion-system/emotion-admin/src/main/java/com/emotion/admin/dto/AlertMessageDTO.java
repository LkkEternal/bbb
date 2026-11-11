package com.emotion.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预警消息响应 DTO
 */
@Data
public class AlertMessageDTO {

    private Long id;

    private Integer ruleId;

    private String ruleName;

    private String location;

    private String cityName;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fxDateStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fxDateEnd;

    private String alertContent;

    private Integer active;

    private Integer status;

    private Integer emailSent;

    /** 情绪预警触发次数（天气预警为NULL） */
    private Integer emotionCount;

    /** 触发情绪标签（多个用/分隔，天气预警为NULL） */
    private String emotionLabel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastDetectTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
