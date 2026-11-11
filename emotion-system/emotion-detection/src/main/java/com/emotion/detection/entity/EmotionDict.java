package com.emotion.detection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@TableName("emotion_dict")
public class EmotionDict {

    @EqualsAndHashCode.Include
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String emotionLabel;

    private String emotionCn;

    private String emojiIcon;

    private String defaultAdvice;

    private String adviceSource;

    private Integer sortOrder;

    private Integer isActive;
}
