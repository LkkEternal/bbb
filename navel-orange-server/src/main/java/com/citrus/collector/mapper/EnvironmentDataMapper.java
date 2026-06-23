package com.citrus.collector.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.collector.entity.EnvironmentData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 环境数据 Mapper
 */
@Mapper
public interface EnvironmentDataMapper extends BaseMapper<EnvironmentData> {
}
