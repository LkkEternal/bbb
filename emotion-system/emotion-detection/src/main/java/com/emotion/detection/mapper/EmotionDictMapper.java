package com.emotion.detection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.detection.entity.EmotionDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmotionDictMapper extends BaseMapper<EmotionDict> {

    @Select("SELECT * FROM emotion_dict WHERE emotion_label = #{label} AND is_active = 1 LIMIT 1")
    EmotionDict selectByLabel(@Param("label") String label);
}
