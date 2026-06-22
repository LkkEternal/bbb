package com.citrus.calendar.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前物候期提醒响应VO
 */
@Data
public class CurrentReminderVO {

    /** 当前物候期信息 */
    private PhenologyInfo current;

    /** 即将到来的物候期信息 */
    private PhenologyInfo upcoming;

    /**
     * 物候期信息
     */
    @Data
    public static class PhenologyInfo {
        /** 物候期ID */
        private Long id;
        /** 物候期名称 */
        private String phenologyName;
        /** 关联节气 */
        private String solarTerm;
        /** 时间范围，如"2月19日 - 3月5日" */
        private String period;
        /** 农事建议内容 */
        private String adviceContent;
        /** 关联病害列表 */
        private List<DiseaseInfo> diseaseRelated;
        /** 距离开始的天数（仅upcoming有值） */
        private Integer daysLeft;
    }

    /**
     * 病害信息
     */
    @Data
    public static class DiseaseInfo {
        /** 病害编码 */
        private String diseaseCode;
        /** 病害名称 */
        private String diseaseName;
    }
}
