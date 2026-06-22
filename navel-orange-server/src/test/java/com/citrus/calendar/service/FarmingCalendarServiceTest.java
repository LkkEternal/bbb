package com.citrus.calendar.service;

import com.citrus.calendar.dto.CalendarDTO;
import com.citrus.calendar.vo.CalendarVO;
import com.citrus.calendar.vo.CurrentReminderVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FarmingCalendarServiceTest {

    @Autowired
    private FarmingCalendarService calendarService;

    @Test
    @Order(1)
    public void testListCalendar() {
        log.info("===== 测试查询日历列表 =====");
        List<CalendarVO> list = calendarService.listCalendar(null);
        log.info("查询到 {} 条记录", list.size());
        for (CalendarVO vo : list) {
            log.info("  {} - {} ({}月{}日-{}月{}日)", vo.getPhenologyName(), vo.getSolarTerm(),
                    vo.getStartMonth(), vo.getStartDay(), vo.getEndMonth(), vo.getEndDay());
        }
    }

    @Test
    @Order(2)
    public void testCreateCalendar() {
        log.info("===== 测试创建日历 =====");
        CalendarDTO dto = new CalendarDTO();
        dto.setPhenologyName("测试物候期");
        dto.setSolarTerm("小满");
        dto.setStartMonth(5);
        dto.setStartDay(15);
        dto.setEndMonth(5);
        dto.setEndDay(25);
        dto.setAdviceContent("测试农事建议");
        dto.setDiseaseRelated(List.of("S1", "S2"));
        dto.setSortOrder(99);

        Long id = calendarService.createCalendar(dto);
        log.info("创建成功，ID: {}", id);
    }

    @Test
    @Order(3)
    public void testUpdateCalendar() {
        log.info("===== 测试更新日历 =====");
        List<CalendarVO> list = calendarService.listCalendar("测试物候期");
        if (!list.isEmpty()) {
            Long id = list.get(0).getId();
            CalendarDTO dto = new CalendarDTO();
            dto.setAdviceContent("更新后的农事建议");
            calendarService.updateCalendar(id, dto);
            log.info("更新成功，ID: {}", id);
        }
    }

    @Test
    @Order(4)
    public void testDeleteCalendar() {
        log.info("===== 测试删除日历 =====");
        List<CalendarVO> list = calendarService.listCalendar("测试物候期");
        if (!list.isEmpty()) {
            Long id = list.get(0).getId();
            calendarService.deleteCalendar(id);
            log.info("删除成功，ID: {}", id);
        }
    }

    @Test
    @Order(5)
    public void testGetCurrentReminder() {
        log.info("===== 测试获取当前物候期提醒 =====");
        CurrentReminderVO vo = calendarService.getCurrentReminder();

        if (vo.getCurrent() != null) {
            log.info("当前物候期: {}", vo.getCurrent().getPhenologyName());
            log.info("  节气: {}", vo.getCurrent().getSolarTerm());
            log.info("  时间: {}", vo.getCurrent().getPeriod());
            log.info("  建议: {}", vo.getCurrent().getAdviceContent());
            if (vo.getCurrent().getDiseaseRelated() != null) {
                log.info("  关联病害:");
                vo.getCurrent().getDiseaseRelated().forEach(d ->
                    log.info("    {} ({})", d.getDiseaseName(), d.getDiseaseCode()));
            }
        } else {
            log.info("当前不在任何物候期内");
        }

        if (vo.getUpcoming() != null) {
            log.info("下一个物候期: {}", vo.getUpcoming().getPhenologyName());
            log.info("  距离: {} 天", vo.getUpcoming().getDaysLeft());
        }
    }
}
