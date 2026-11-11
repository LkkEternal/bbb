package com.emotion.admin.dto;

import lombok.Data;

/**
 * 预警邮件结构化数据（用于填充固定静态邮件模板）
 */
@Data
public class AlertEmailData {

    /** 收件人邮箱 */
    private String to;

    /** 邮件主题 */
    private String subject;

    /** 标题（首行，如：检测到您近期情绪持续低落） */
    private String title;

    /** 用户称呼（昵称/用户名） */
    private String userName;

    /** 触发情绪标签（如 悲伤/焦虑） */
    private String emotion;

    /** 触发次数 */
    private Integer count;

    /** 建议内容（情绪词典 defaultAdvice） */
    private String advice;

    /** 最近一次检测时间（yyyy-MM-dd HH:mm:ss） */
    private String lastTime;
}
