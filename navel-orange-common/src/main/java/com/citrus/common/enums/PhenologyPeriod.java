package com.citrus.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物候期枚举
 * 对应脐橙种植的7个主要物候期
 */
@Getter
@AllArgsConstructor
public enum PhenologyPeriod {

    SPRING_SHOOT("春梢萌发期", "惊蛰", 2, 19, 3, 5, "加强水肥管理，促进春梢生长。注意防治疮痂病、红蜘蛛、蚜虫。"),
    FLOWERING("花蕾-开花期", "清明", 3, 20, 4, 20, "保花保果，注意防治蓟马、花蕾蛆。避免使用高毒农药。"),
    YOUNG_FRUIT("谢花-幼果期", "谷雨", 4, 20, 5, 20, "保果稳果，重点防治溃疡病、炭疽病。加强水肥管理。"),
    SUMMER_SHOOT("夏梢期", "芒种", 5, 21, 7, 7, "控制夏梢生长，防治潜叶蛾、柑橘木虱。注意排水防涝。"),
    FRUIT_EXPANSION("果实膨大期", "大暑", 7, 7, 9, 7, "加强水肥管理，注意抗旱。重点防治锈壁虱、红蜘蛛。注意防日灼。"),
    AUTUMN_SHOOT("秋梢期", "白露", 9, 7, 10, 8, "放好秋梢，防治潜叶蛾、木虱。注意防治炭疽病。"),
    FRUIT_MATURITY("果实成熟期", "霜降", 10, 8, 12, 7, "适时采收，注意防治炭疽病（蒂腐）。做好冬季清园准备。");

    /**
     * 物候期名称
     */
    private final String name;

    /**
     * 关联节气
     */
    private final String solarTerm;

    /**
     * 开始月份
     */
    private final int startMonth;

    /**
     * 开始日期
     */
    private final int startDay;

    /**
     * 结束月份
     */
    private final int endMonth;

    /**
     * 结束日期
     */
    private final int endDay;

    /**
     * 默认农事建议
     */
    private final String advice;

    /**
     * 根据日期获取当前物候期
     * @param month 月份
     * @param day 日期
     * @return 物候期，如果不在任何物候期范围内返回null
     */
    public static PhenologyPeriod getByDate(int month, int day) {
        for (PhenologyPeriod period : values()) {
            if (isInPeriod(month, day, period)) {
                return period;
            }
        }
        return null;
    }

    /**
     * 判断指定日期是否在物候期内
     */
    private static boolean isInPeriod(int month, int day, PhenologyPeriod period) {
        int date = month * 100 + day;
        int start = period.startMonth * 100 + period.startDay;
        int end = period.endMonth * 100 + period.endDay;

        if (start <= end) {
            return date >= start && date <= end;
        } else {
            // 跨年情况（如12月到次年2月）
            return date >= start || date <= end;
        }
    }
}
