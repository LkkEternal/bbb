package com.citrus.calendar.controller;

import com.citrus.calendar.dto.CalendarDTO;
import com.citrus.calendar.service.FarmingCalendarService;
import com.citrus.calendar.vo.CalendarVO;
import com.citrus.calendar.vo.CurrentReminderVO;
import com.citrus.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 农事日历控制器
 * 提供日历CRUD和当前物候期提醒接口
 */
@RestController
@RequiredArgsConstructor
public class FarmingCalendarController {

    private final FarmingCalendarService calendarService;

    /**
     * 查询日历列表
     * @param phenologyName 物候期名称筛选（可选）
     * @return 日历列表
     */
    @GetMapping("/api/admin/calendar")
    public Result<List<CalendarVO>> listCalendar(@RequestParam(required = false) String phenologyName) {
        return Result.success(calendarService.listCalendar(phenologyName));
    }

    /**
     * 创建日历条目
     * @param dto 请求参数
     * @return 新建条目ID
     */
    @PostMapping("/api/admin/calendar")
    public Result<Long> createCalendar(@RequestBody CalendarDTO dto) {
        return Result.success(calendarService.createCalendar(dto));
    }

    /**
     * 更新日历条目
     * @param id 条目ID
     * @param dto 请求参数
     */
    @PutMapping("/api/admin/calendar/{id}")
    public Result<Void> updateCalendar(@PathVariable Long id, @RequestBody CalendarDTO dto) {
        calendarService.updateCalendar(id, dto);
        return Result.success();
    }

    /**
     * 删除日历条目
     * @param id 条目ID
     */
    @DeleteMapping("/api/admin/calendar/{id}")
    public Result<Void> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return Result.success();
    }

    /**
     * 获取当前物候期提醒（小程序端）
     * @return 当前物候期和下一个物候期信息
     */
    @GetMapping("/api/miniapp/calendar/current")
    public Result<CurrentReminderVO> getCurrentReminder() {
        return Result.success(calendarService.getCurrentReminder());
    }
}
