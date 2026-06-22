package com.citrus.calendar.dto;

import lombok.Data;

import java.util.List;

@Data
public class CalendarDTO {

    private String phenologyName;

    private String solarTerm;

    private Integer startMonth;

    private Integer startDay;

    private Integer endMonth;

    private Integer endDay;

    private String adviceContent;

    private List<String> diseaseRelated;

    private Integer sortOrder;
}
