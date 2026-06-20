package com.citrus.collector.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.collector.entity.WeatherForecast;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeatherForecastMapper extends BaseMapper<WeatherForecast> {
}
