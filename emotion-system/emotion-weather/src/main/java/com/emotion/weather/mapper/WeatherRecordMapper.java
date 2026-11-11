package com.emotion.weather.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emotion.weather.entity.WeatherRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeatherRecordMapper extends BaseMapper<WeatherRecord> {
}
