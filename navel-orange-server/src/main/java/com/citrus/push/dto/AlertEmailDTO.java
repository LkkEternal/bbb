package com.citrus.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 预警邮件数据模型
 * <p>
 * 由 AlertEmailDTOBuilder 从 AlertMessage + 关联数据组装，
 * 然后传递给 Thymeleaf 模板渲染成 HTML。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEmailDTO {

    /** 果园名称 */
    private String orchardName;

    /** 预警等级 1/2/3 */
    private int level;

    /** 等级标签：L1 低风险 / L2 中风险 / L3 高风险 */
    private String levelLabel;

    /** 等级颜色：绿/橙/红 */
    private String levelColor;

    /** 等级图标：🟢/⚠️/🔴 */
    private String levelBadge;

    /** 风险评分 0-100 */
    private int riskScore;

    /** 发出时间 */
    private String sendTime;

    // ─── 风险摘要 ───

    /** 触发规则描述 */
    private String summary;

    /** 当前物候 */
    private String phenology;

    /** 温度 */
    private Double temperature;

    /** 湿度 */
    private Double humidity;

    // ─── 检测详情 ───

    /** 病害名称 */
    private String diseaseName;

    /** 识别置信度 0-1 */
    private Double probability;

    /** 识别时间 */
    private String deviceTimestamp;

    /** 识别图片 URL */
    private String imageUrl;

    // ─── 防治建议 ───

    /** 防治建议列表（自动拆分） */
    private List<String> adviceItems;

    /** 不处理的后果 */
    private String consequences;

    /** 紧急程度 */
    private String urgency;
}
