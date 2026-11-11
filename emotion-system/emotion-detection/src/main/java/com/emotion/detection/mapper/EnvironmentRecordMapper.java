package com.emotion.detection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.detection.entity.EnvironmentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnvironmentRecordMapper extends BaseMapper<EnvironmentRecord> {

    @Select("SELECT * FROM environment_record WHERE detection_record_id = #{detectionRecordId}")
    EnvironmentRecord selectByDetectionRecordId(@Param("detectionRecordId") Long detectionRecordId);
}
