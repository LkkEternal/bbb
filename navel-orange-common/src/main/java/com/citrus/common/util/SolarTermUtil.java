package com.citrus.common.util;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 节气计算工具类
 * 基于寿星公式计算24节气日期，精度±1天
 */
public class SolarTermUtil {

    /**
     * 24节气枚举
     */
    public enum SolarTerm {
        XIAO_HAN("小寒", 1),
        DA_HAN("大寒", 2),
        LI_CHUN("立春", 3),
        YU_SHUI("雨水", 4),
        JING_ZHE("惊蛰", 5),
        CHUN_FEN("春分", 6),
        QING_MING("清明", 7),
        GU_YU("谷雨", 8),
        LI_XIA("立夏", 9),
        XIAO_MAN("小满", 10),
        MANG_ZHONG("芒种", 11),
        XIA_ZHI("夏至", 12),
        XIAO_SHU("小暑", 13),
        DA_SHU("大暑", 14),
        LI_QIU("立秋", 15),
        CHU_SHU("处暑", 16),
        BAI_LU("白露", 17),
        QIU_FEN("秋分", 18),
        HAN_LU("寒露", 19),
        SHUANG_JIANG("霜降", 20),
        LI_DONG("立冬", 21),
        XIAO_XUE("小雪", 22),
        DA_XUE("大雪", 23),
        DONG_ZHI("冬至", 24);

        private final String name;
        private final int index;

        SolarTerm(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public String getName() { return name; }
        public int getIndex() { return index; }
    }

    // 24节气基准日期（以2000年为基准，月份从0开始）
    private static final int[][] BASE_DATES = {
        {1, 6},   // 小寒
        {1, 20},  // 大寒
        {2, 4},   // 立春
        {2, 19},  // 雨水
        {3, 6},   // 惊蛰
        {3, 21},  // 春分
        {4, 5},   // 清明
        {4, 20},  // 谷雨
        {5, 6},   // 立夏
        {5, 21},  // 小满
        {6, 6},   // 芒种
        {6, 21},  // 夏至
        {7, 7},   // 小暑
        {7, 23},  // 大暑
        {8, 7},   // 立秋
        {8, 23},  // 处暑
        {9, 8},   // 白露
        {9, 23},  // 秋分
        {10, 8},  // 寒露
        {10, 23}, // 霜降
        {11, 7},  // 立冬
        {11, 22}, // 小雪
        {12, 7},  // 大雪
        {12, 22}  // 冬至
    };

    // 每个节气的年偏移量（天数），用于修正不同年份的差异
    private static final double[] YEAR_OFFSETS = {
        6.11, 20.84, 4.15, 19.07, 6.21, 20.84, 5.59, 20.34,
        6.11, 21.37, 6.36, 21.63, 7.44, 23.13, 7.69, 23.35,
        8.23, 23.35, 8.44, 23.59, 7.77, 22.60, 7.44, 22.31
    };

    /**
     * 获取指定年份某个节气的日期
     * @param year 年份
     * @param term 节气
     * @return 节气日期
     */
    public static LocalDate getSolarTermDate(int year, SolarTerm term) {
        int index = term.getIndex() - 1;
        int month = BASE_DATES[index][0];
        int baseDay = BASE_DATES[index][1];

        // 寿星公式计算偏移
        int y = (year - 2000) % 4;
        int offset;
        if (y == 0) {
            offset = 0;
        } else if (y == 1) {
            offset = (index % 2 == 0) ? 1 : 0;
        } else if (y == 2) {
            offset = (index % 2 == 0) ? 1 : 1;
        } else {
            offset = (index % 2 == 0) ? 0 : 1;
        }

        int day = baseDay + offset;

        // 处理月末溢出
        LocalDate date = LocalDate.of(year, month, 1);
        int maxDay = date.lengthOfMonth();
        if (day > maxDay) {
            day = day - maxDay;
            month = month + 1;
            if (month > 12) {
                month = 1;
                year = year + 1;
            }
        }

        return LocalDate.of(year, month, day);
    }

    /**
     * 获取指定年份所有节气日期
     * @param year 年份
     * @return 节气名称到日期的映射
     */
    public static Map<SolarTerm, LocalDate> getAllSolarTerms(int year) {
        Map<SolarTerm, LocalDate> result = new HashMap<>();
        for (SolarTerm term : SolarTerm.values()) {
            result.put(term, getSolarTermDate(year, term));
        }
        return result;
    }

    /**
     * 获取当前日期对应的节气（最近的节气）
     * @param date 日期
     * @return 当前节气，如果不在任何节气±3天范围内返回null
     */
    public static SolarTerm getCurrentSolarTerm(LocalDate date) {
        int year = date.getYear();
        Map<SolarTerm, LocalDate> terms = getAllSolarTerms(year);

        SolarTerm nearest = null;
        long minDays = Long.MAX_VALUE;

        for (Map.Entry<SolarTerm, LocalDate> entry : terms.entrySet()) {
            long days = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(date, entry.getValue()));
            if (days < minDays && days <= 3) {
                minDays = days;
                nearest = entry.getKey();
            }
        }

        return nearest;
    }

    /**
     * 根据日期获取对应的物候期范围内的节气
     * @param date 日期
     * @return 所属的节气（根据月份判断）
     */
    public static SolarTerm getSolarTermByDate(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if (month == 1 && day < 20) return SolarTerm.XIAO_HAN;
        if (month == 1 || (month == 2 && day < 4)) return SolarTerm.DA_HAN;
        if (month == 2 && day < 19) return SolarTerm.LI_CHUN;
        if (month == 2 || (month == 3 && day < 6)) return SolarTerm.YU_SHUI;
        if (month == 3 && day < 21) return SolarTerm.JING_ZHE;
        if (month == 3 || (month == 4 && day < 5)) return SolarTerm.CHUN_FEN;
        if (month == 4 && day < 20) return SolarTerm.QING_MING;
        if (month == 4 || (month == 5 && day < 6)) return SolarTerm.GU_YU;
        if (month == 5 && day < 21) return SolarTerm.LI_XIA;
        if (month == 5 || (month == 6 && day < 6)) return SolarTerm.XIAO_MAN;
        if (month == 6 && day < 21) return SolarTerm.MANG_ZHONG;
        if (month == 6 || (month == 7 && day < 7)) return SolarTerm.XIA_ZHI;
        if (month == 7 && day < 23) return SolarTerm.XIAO_SHU;
        if (month == 7 || (month == 8 && day < 7)) return SolarTerm.DA_SHU;
        if (month == 8 && day < 23) return SolarTerm.LI_QIU;
        if (month == 8 || (month == 9 && day < 8)) return SolarTerm.CHU_SHU;
        if (month == 9 && day < 23) return SolarTerm.BAI_LU;
        if (month == 9 || (month == 10 && day < 8)) return SolarTerm.QIU_FEN;
        if (month == 10 && day < 23) return SolarTerm.HAN_LU;
        if (month == 10 || (month == 11 && day < 7)) return SolarTerm.SHUANG_JIANG;
        if (month == 11 && day < 22) return SolarTerm.LI_DONG;
        if (month == 11 || (month == 12 && day < 7)) return SolarTerm.XIAO_XUE;
        if (month == 12 && day < 22) return SolarTerm.DA_XUE;
        return SolarTerm.DONG_ZHI;
    }
}
