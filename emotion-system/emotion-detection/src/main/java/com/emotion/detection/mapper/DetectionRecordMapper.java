package com.emotion.detection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.detection.entity.DetectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DetectionRecordMapper extends BaseMapper<DetectionRecord> {

    @Select("<script>" +
            "SELECT * FROM detection_record WHERE user_id = #{userId}" +
            "<if test='emotionLabel != null'> AND emotion_label = #{emotionLabel}</if>" +
            "<if test='startDate != null'> AND created_at &gt;= #{startDate}</if>" +
            "<if test='endDate != null'> AND created_at &lt;= #{endDate}</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    List<DetectionRecord> selectHistoryPage(@Param("userId") Long userId,
                                            @Param("emotionLabel") String emotionLabel,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);
}
