package com.emotion.detection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.detection.entity.EmotionFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmotionFeedbackMapper extends BaseMapper<EmotionFeedback> {

    @Select("SELECT COUNT(*) FROM emotion_feedback WHERE record_id = #{recordId}")
    int countByRecordId(@Param("recordId") Long recordId);

    @Select("SELECT * FROM emotion_feedback WHERE record_id = #{recordId} AND user_id = #{userId}")
    EmotionFeedback selectByRecordIdAndUserId(@Param("recordId") Long recordId, @Param("userId") Long userId);
}
