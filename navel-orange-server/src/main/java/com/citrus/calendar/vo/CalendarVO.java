package com.citrus.calendar.vo;

import lombok.Data;

import java.util.List;

/**
 * 农事日历响应VO
 */
@Data
public class CalendarVO {

    /** 主键ID */
    private Long id;

    /** 物候期名称 */
    private String phenologyName;

    /** 关联节气 */
    private String solarTerm;

    /** 开始月份 */
    private Integer startMonth;

    /** 开始日期 */
    private Integer startDay;

    /** 结束月份 */
    private Integer endMonth;

    /** 结束日期 */
    private Integer endDay;

    /** 农事建议内容 */
    private String adviceContent;

    /** 关联病害编码列表 */
    private List<String> diseaseRelated;

    /** 排序序号 */
    private Integer sortOrder;
}
