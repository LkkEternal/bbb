package com.citrus.calendar.vo;

import lombok.Data;

import java.util.List;

@Data
public class CurrentReminderVO {

    private PhenologyInfo current;

    private PhenologyInfo upcoming;

    @Data
    public static class PhenologyInfo {
        private Long id;
        private String phenologyName;
        private String solarTerm;
        private String period;
        private String adviceContent;
        private List<DiseaseInfo> diseaseRelated;
        private Integer daysLeft;
    }

    @Data
    public static class DiseaseInfo {
        private String diseaseCode;
        private String diseaseName;
    }
}
