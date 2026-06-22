package com.citrus.calendar.controller;

import com.citrus.calendar.dto.CalendarDTO;
import com.citrus.calendar.service.FarmingCalendarService;
import com.citrus.calendar.vo.CalendarVO;
import com.citrus.calendar.vo.CurrentReminderVO;
import com.citrus.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FarmingCalendarController {

    private final FarmingCalendarService calendarService;

    @GetMapping("/api/admin/calendar")
    public Result<List<CalendarVO>> listCalendar(@RequestParam(required = false) String phenologyName) {
        return Result.success(calendarService.listCalendar(phenologyName));
    }

    @PostMapping("/api/admin/calendar")
    public Result<Long> createCalendar(@RequestBody CalendarDTO dto) {
        return Result.success(calendarService.createCalendar(dto));
    }

    @PutMapping("/api/admin/calendar/{id}")
    public Result<Void> updateCalendar(@PathVariable Long id, @RequestBody CalendarDTO dto) {
        calendarService.updateCalendar(id, dto);
        return Result.success();
    }

    @DeleteMapping("/api/admin/calendar/{id}")
    public Result<Void> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return Result.success();
    }

    @GetMapping("/api/miniapp/calendar/current")
    public Result<CurrentReminderVO> getCurrentReminder() {
        return Result.success(calendarService.getCurrentReminder());
    }
}
