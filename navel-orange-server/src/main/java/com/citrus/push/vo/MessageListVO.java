package com.citrus.push.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageListVO {

    private Long id;
    private String messageType;
    private String title;
    private String summary;
    private Integer level;
    private Long orchardId;
    private String orchardName;
    private String ruleCode;
    private Integer readStatus;
    private LocalDateTime createdAt;
}
