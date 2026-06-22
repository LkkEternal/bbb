package com.citrus.push.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.citrus.config.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "alert_message", autoResultMap = true)
public class AlertMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("orchard_id")
    private Long orchardId;

    @TableField("user_id")
    private Long userId;

    @TableField("rule_id")
    private Integer ruleId;

    @TableField("calendar_id")
    private Integer calendarId;

    @TableField("message_type")
    private String messageType;

    private Integer level;

    private String title;

    private String summary;

    private String content;

    private String advice;

    @TableField("related_type")
    private String relatedType;

    @TableField("related_id")
    private Long relatedId;

    @TableField(value = "extra_data", typeHandler = JsonbTypeHandler.class)
    private String extraData;

    @TableField("push_channel")
    private String pushChannel;

    @TableField("push_status")
    private Integer pushStatus;

    @TableField("push_time")
    private LocalDateTime pushTime;

    @TableField("read_status")
    private Integer readStatus;

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
