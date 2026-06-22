package com.citrus.calendar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citrus.calendar.entity.FarmingCalendar;
import org.apache.ibatis.annotations.Mapper;

/**
 * 农事日历Mapper接口
 */
@Mapper
public interface FarmingCalendarMapper extends BaseMapper<FarmingCalendar> {
}
