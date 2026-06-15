package com.citrus.common.util;

import com.citrus.common.enums.PhenologyPeriod;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class SolarTermUtilTest {

    @Test
    public void testSolarTerm2026() {
        log.info("===== 2026年节气日期 =====");
        for (SolarTermUtil.SolarTerm term : SolarTermUtil.SolarTerm.values()) {
            LocalDate date = SolarTermUtil.getSolarTermDate(2026, term);
            log.info("{}: {} ({}月{}日)", term.getName(), date, date.getMonthValue(), date.getDayOfMonth());
        }
    }

    @Test
    public void testSolarTerm2025() {
        log.info("===== 2025年节气日期 =====");
        for (SolarTermUtil.SolarTerm term : SolarTermUtil.SolarTerm.values()) {
            LocalDate date = SolarTermUtil.getSolarTermDate(2025, term);
            log.info("{}: {} ({}月{}日)", term.getName(), date, date.getMonthValue(), date.getDayOfMonth());
        }
    }

    @Test
    public void testCurrentSolarTerm() {
        LocalDate today = LocalDate.now();
        log.info("===== 当前日期: {} =====", today);

        SolarTermUtil.SolarTerm current = SolarTermUtil.getCurrentSolarTerm(today);
        if (current != null) {
            log.info("当前节气: {}", current.getName());
        } else {
            log.info("当前不在任何节气±3天范围内");
        }

        SolarTermUtil.SolarTerm byDate = SolarTermUtil.getSolarTermByDate(today);
        log.info("所属节气区间: {}", byDate.getName());
    }

    @Test
    public void testPhenologyPeriod() {
        log.info("===== 测试物候期匹配 =====");

        // 测试几个关键日期
        int[][] testDates = {
            {1, 15},   // 冬季，不在物候期
            {3, 1},    // 春梢萌发期
            {4, 10},   // 花蕾-开花期
            {5, 1},    // 谢花-幼果期
            {6, 15},   // 夏梢期
            {8, 1},    // 果实膨大期
            {9, 15},   // 秋梢期
            {11, 1},   // 果实成熟期
        };

        for (int[] d : testDates) {
            PhenologyPeriod period = PhenologyPeriod.getByDate(d[0], d[1]);
            String name = (period != null) ? period.getName() : "无";
            String advice = (period != null) ? period.getAdvice() : "-";
            log.info("{}月{}日 -> 物候期: {}", d[0], d[1], name);
            log.info("  建议: {}", advice);
        }
    }

    @Test
    public void testPhenologyCoverage() {
        log.info("===== 验证物候期是否覆盖全年 =====");
        for (int month = 1; month <= 12; month++) {
            PhenologyPeriod period = PhenologyPeriod.getByDate(month, 15);
            String name = (period != null) ? period.getName() : "无（休眠期）";
            log.info("{}月: {}", month, name);
        }
    }
}
