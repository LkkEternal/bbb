package com.citrus.calendar.task;

import com.citrus.calendar.entity.FarmingCalendar;
import com.citrus.calendar.mapper.FarmingCalendarMapper;
import com.citrus.calendar.service.impl.FarmingCalendarServiceImpl;
import com.citrus.knowledge.service.RagService;
import com.citrus.knowledge.vo.RagResponse;
import com.citrus.push.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 农事日历定时提醒任务
 * <p>
 * 每天 6:00 执行，匹配当前物候期，
 * 调用 RAG 生成个性化农事提醒，写入 alert_message。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarReminderTask {

    private final FarmingCalendarMapper calendarMapper;
    private final RagService ragService;
    private final MessageService messageService;

    /**
     * 每天 6:00 执行
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void sendPhenologyReminders() {
        log.info("开始生成农事日历提醒 ...");

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        // 查询当前物候期
        List<FarmingCalendar> allCalendars = calendarMapper.selectList(null);
        FarmingCalendar current = null;

        for (FarmingCalendar cal : allCalendars) {
            if (Boolean.FALSE.equals(cal.getEnabled())) continue;
            if (isInPeriod(month, day, cal)) {
                current = cal;
                break;
            }
        }

        if (current == null) {
            log.info("当前没有匹配的物候期");
            return;
        }

        // 构建 RAG 查询
        String diseaseInfo = "";
        if (current.getDiseaseRelated() != null && !current.getDiseaseRelated().isEmpty()) {
            diseaseInfo = String.join("、", current.getDiseaseRelated());
        }

        String question = String.format("%s %s 农事提醒 防治重点",
                current.getPhenologyName(),
                current.getSolarTerm() != null ? current.getSolarTerm() : "");

        String advice;
        try {
            RagResponse resp = ragService.query(question, diseaseInfo, null, null);
            advice = (resp != null && resp.getAnswer() != null) ? resp.getAnswer() : current.getAdviceContent();
        } catch (Exception e) {
            log.warn("RAG phenology reminder failed, fallback to template: {}", e.getMessage());
            advice = current.getAdviceContent();
        }

        // 创建提醒消息（全局提醒，userId=null）
        String title = "📅 " + current.getPhenologyName() + "农事提醒";
        String content = "当前物候期：" + current.getPhenologyName();
        if (current.getSolarTerm() != null) {
            content += "（" + current.getSolarTerm() + "）";
        }

        messageService.createAlertMessage(
                null, null, null, 1,
                title, content, advice, "calendar"
        );

        log.info("农事日历提醒已生成: phenology={}, advice_length={}",
                current.getPhenologyName(), advice != null ? advice.length() : 0);
    }

    private boolean isInPeriod(int month, int day, FarmingCalendar cal) {
        int date = month * 100 + day;
        int start = cal.getStartMonth() * 100 + cal.getStartDay();
        int end = cal.getEndMonth() * 100 + cal.getEndDay();

        if (start <= end) {
            return date >= start && date <= end;
        } else {
            return date >= start || date <= end;
        }
    }
}
