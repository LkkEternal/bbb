package com.citrus.calendar.dto;

import lombok.Data;

import java.util.List;

/**
 * 农事日历请求参数DTO
 */
@Data
public class CalendarDTO {

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
