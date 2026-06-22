package com.citrus.calendar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.calendar.dto.CalendarDTO;
import com.citrus.calendar.entity.FarmingCalendar;
import com.citrus.calendar.mapper.FarmingCalendarMapper;
import com.citrus.calendar.service.FarmingCalendarService;
import com.citrus.calendar.vo.CalendarVO;
import com.citrus.calendar.vo.CurrentReminderVO;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmingCalendarServiceImpl implements FarmingCalendarService {

    private final FarmingCalendarMapper calendarMapper;

    @Override
    public List<CalendarVO> listCalendar(String phenologyName) {
        LambdaQueryWrapper<FarmingCalendar> wrapper = new LambdaQueryWrapper<>();
        if (phenologyName != null && !phenologyName.isEmpty()) {
            wrapper.like(FarmingCalendar::getPhenologyName, phenologyName);
        }
        wrapper.orderByAsc(FarmingCalendar::getSortOrder);

        List<FarmingCalendar> list = calendarMapper.selectList(wrapper);
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public Long createCalendar(CalendarDTO dto) {
        FarmingCalendar entity = new FarmingCalendar();
        entity.setPhenologyName(dto.getPhenologyName());
        entity.setSolarTerm(dto.getSolarTerm());
        entity.setStartMonth(dto.getStartMonth());
        entity.setStartDay(dto.getStartDay());
        entity.setEndMonth(dto.getEndMonth());
        entity.setEndDay(dto.getEndDay());
        entity.setAdviceContent(dto.getAdviceContent());
        entity.setDiseaseRelated(dto.getDiseaseRelated());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setEnabled(true);

        calendarMapper.insert(entity);
        log.info("创建农事日历成功: id={}, phenologyName={}", entity.getId(), entity.getPhenologyName());
        return entity.getId();
    }

    @Override
    public void updateCalendar(Long id, CalendarDTO dto) {
        FarmingCalendar entity = calendarMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农事日历不存在");
        }

        if (dto.getPhenologyName() != null) entity.setPhenologyName(dto.getPhenologyName());
        if (dto.getSolarTerm() != null) entity.setSolarTerm(dto.getSolarTerm());
        if (dto.getStartMonth() != null) entity.setStartMonth(dto.getStartMonth());
        if (dto.getStartDay() != null) entity.setStartDay(dto.getStartDay());
        if (dto.getEndMonth() != null) entity.setEndMonth(dto.getEndMonth());
        if (dto.getEndDay() != null) entity.setEndDay(dto.getEndDay());
        if (dto.getAdviceContent() != null) entity.setAdviceContent(dto.getAdviceContent());
        if (dto.getDiseaseRelated() != null) entity.setDiseaseRelated(dto.getDiseaseRelated());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());

        calendarMapper.updateById(entity);
        log.info("更新农事日历成功: id={}", id);
    }

    @Override
    public void deleteCalendar(Long id) {
        FarmingCalendar entity = calendarMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农事日历不存在");
        }
        calendarMapper.deleteById(id);
        log.info("删除农事日历成功: id={}", id);
    }

    @Override
    public CurrentReminderVO getCurrentReminder() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        LambdaQueryWrapper<FarmingCalendar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FarmingCalendar::getEnabled, true)
               .orderByAsc(FarmingCalendar::getSortOrder);

        List<FarmingCalendar> all = calendarMapper.selectList(wrapper);

        FarmingCalendar current = null;
        FarmingCalendar upcoming = null;

        for (int i = 0; i < all.size(); i++) {
            FarmingCalendar cal = all.get(i);
            if (isInPeriod(month, day, cal)) {
                current = cal;
                if (i + 1 < all.size()) {
                    upcoming = all.get(i + 1);
                }
                break;
            }
        }

        // 如果没找到当前物候期，找下一个即将开始的
        if (current == null) {
            for (FarmingCalendar cal : all) {
                if (isAfterStart(month, day, cal)) {
                    upcoming = cal;
                    break;
                }
            }
        }

        CurrentReminderVO vo = new CurrentReminderVO();
        if (current != null) {
            vo.setCurrent(toPhenologyInfo(current, null));
        }
        if (upcoming != null) {
            int daysLeft = calculateDaysLeft(month, day, upcoming);
            vo.setUpcoming(toPhenologyInfo(upcoming, daysLeft));
        }
        return vo;
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

    private boolean isAfterStart(int month, int day, FarmingCalendar cal) {
        int date = month * 100 + day;
        int start = cal.getStartMonth() * 100 + cal.getStartDay();
        return date < start;
    }

    private int calculateDaysLeft(int month, int day, FarmingCalendar cal) {
        LocalDate today = LocalDate.of(LocalDate.now().getYear(), month, day);
        LocalDate start = LocalDate.of(LocalDate.now().getYear(), cal.getStartMonth(), cal.getStartDay());
        if (start.isBefore(today)) {
            start = start.plusYears(1);
        }
        return (int) (start.toEpochDay() - today.toEpochDay());
    }

    private CurrentReminderVO.PhenologyInfo toPhenologyInfo(FarmingCalendar cal, Integer daysLeft) {
        CurrentReminderVO.PhenologyInfo info = new CurrentReminderVO.PhenologyInfo();
        info.setId(cal.getId());
        info.setPhenologyName(cal.getPhenologyName());
        info.setSolarTerm(cal.getSolarTerm());
        info.setPeriod(cal.getStartMonth() + "月" + cal.getStartDay() + "日 - " + cal.getEndMonth() + "月" + cal.getEndDay() + "日");
        info.setAdviceContent(cal.getAdviceContent());
        info.setDaysLeft(daysLeft);

        // 解析关联病害
        if (cal.getDiseaseRelated() != null) {
            List<CurrentReminderVO.DiseaseInfo> diseases = new ArrayList<>();
            for (String code : cal.getDiseaseRelated()) {
                CurrentReminderVO.DiseaseInfo disease = new CurrentReminderVO.DiseaseInfo();
                disease.setDiseaseCode(code);
                disease.setDiseaseName(getDiseaseName(code));
                diseases.add(disease);
            }
            info.setDiseaseRelated(diseases);
        }
        return info;
    }

    private String getDiseaseName(String code) {
        return switch (code) {
            case "S1" -> "健康果实";
            case "S2" -> "健康叶片";
            case "S3" -> "斑驳花纹";
            case "S4" -> "红鼻果";
            case "S10" -> "炭疽病";
            case "S11" -> "脂点黄斑病";
            case "S12" -> "疮痂病";
            case "S13" -> "煤污病";
            case "S14" -> "溃疡病";
            default -> code;
        };
    }

    private CalendarVO toVO(FarmingCalendar entity) {
        CalendarVO vo = new CalendarVO();
        vo.setId(entity.getId());
        vo.setPhenologyName(entity.getPhenologyName());
        vo.setSolarTerm(entity.getSolarTerm());
        vo.setStartMonth(entity.getStartMonth());
        vo.setStartDay(entity.getStartDay());
        vo.setEndMonth(entity.getEndMonth());
        vo.setEndDay(entity.getEndDay());
        vo.setAdviceContent(entity.getAdviceContent());
        vo.setSortOrder(entity.getSortOrder());
        vo.setDiseaseRelated(entity.getDiseaseRelated());
        return vo;
    }
}
