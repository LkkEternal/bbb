package com.citrus.collector.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.collector.entity.DiseaseDetection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病害识别记录 Mapper
 */
@Mapper
public interface DiseaseDetectionMapper extends BaseMapper<DiseaseDetection> {
}
