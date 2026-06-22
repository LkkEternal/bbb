package com.citrus.calendar.service;

import com.citrus.calendar.dto.CalendarDTO;
import com.citrus.calendar.vo.CalendarVO;
import com.citrus.calendar.vo.CurrentReminderVO;

import java.util.List;

/**
 * 农事日历服务接口
 */
public interface FarmingCalendarService {

    /**
     * 查询日历列表
     * @param phenologyName 物候期名称（可选，模糊查询）
     * @return 日历列表
     */
    List<CalendarVO> listCalendar(String phenologyName);

    /**
     * 创建日历条目
     * @param dto 请求参数
     * @return 新建条目ID
     */
    Long createCalendar(CalendarDTO dto);

    /**
     * 更新日历条目
     * @param id 条目ID
     * @param dto 请求参数
     */
    void updateCalendar(Long id, CalendarDTO dto);

    /**
     * 删除日历条目
     * @param id 条目ID
     */
    void deleteCalendar(Long id);

    /**
     * 获取当前物候期提醒
     * @return 当前物候期和下一个物候期信息
     */
    CurrentReminderVO getCurrentReminder();
}
