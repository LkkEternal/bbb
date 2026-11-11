package com.emotion.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 天气预警消息实体类
 * 记录天气预警规则触发后生成的消息
 */
@Data
@TableName("alert_message")
public class AlertMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer ruleId;

    private String ruleName;

    private String location;

    private String cityName;

    private Long userId;

    private LocalDate fxDateStart;

    private LocalDate fxDateEnd;

    private String alertContent;

    private Integer active;

    private Integer status;

    private Integer emailSent;

    /** 情绪预警触发次数（天气预警为NULL） */
    private Integer emotionCount;

    /** 触发情绪标签（多个用/分隔，天气预警为NULL） */
    private String emotionLabel;

    /** 最近一次命中检测时间（天气预警为NULL） */
    private LocalDateTime lastDetectTime;

    private LocalDateTime createdAt;
}
