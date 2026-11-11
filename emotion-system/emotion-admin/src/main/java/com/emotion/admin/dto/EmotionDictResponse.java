package com.emotion.admin.dto;

import lombok.Data;

@Data
public class EmotionDictResponse {

    private Integer id;
    private String emotionLabel;
    private String emotionCn;
    private String emojiIcon;
    private String defaultAdvice;
    private String adviceSource;
    private Integer sortOrder;
    private Boolean isActive;
}
