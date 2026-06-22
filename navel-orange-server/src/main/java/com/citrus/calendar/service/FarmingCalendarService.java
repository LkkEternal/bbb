package com.citrus.calendar.service;

import com.citrus.calendar.dto.CalendarDTO;
import com.citrus.calendar.vo.CalendarVO;
import com.citrus.calendar.vo.CurrentReminderVO;

import java.util.List;

public interface FarmingCalendarService {

    List<CalendarVO> listCalendar(String phenologyName);

    Long createCalendar(CalendarDTO dto);

    void updateCalendar(Long id, CalendarDTO dto);

    void deleteCalendar(Long id);

    CurrentReminderVO getCurrentReminder();
}
