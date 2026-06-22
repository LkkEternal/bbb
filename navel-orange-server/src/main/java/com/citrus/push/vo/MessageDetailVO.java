package com.citrus.push.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDetailVO {

    private Long id;
    private String messageType;
    private String title;
    private String summary;
    private String content;
    private String advice;
    private Integer level;
    private Long orchardId;
    private String orchardName;
    private Integer ruleId;
    private String ruleCode;
    private String relatedType;
    private Long relatedId;
    private String extraData;
    private Integer readStatus;
    private LocalDateTime readTime;
    private String pushChannel;
    private Integer pushStatus;
    private LocalDateTime pushTime;
    private LocalDateTime createdAt;
}
