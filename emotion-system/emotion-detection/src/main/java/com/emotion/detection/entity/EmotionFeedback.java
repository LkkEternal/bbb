package com.emotion.detection.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("emotion_feedback")
public class EmotionFeedback {

    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long recordId;

    private String imageUrl;

    private String objectKey;

    private String systemLabel;

    private String correctLabel;

    private String description;

    private Integer status;

    private Long reviewedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
