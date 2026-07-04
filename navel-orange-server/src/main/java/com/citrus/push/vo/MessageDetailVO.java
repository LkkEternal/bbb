package com.citrus.push.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息详情VO（View Object）
 *
 * 用于消息详情接口的响应数据
 * 包含消息的完整信息，包括内容、防治建议、推送状态等
 */
@Data
public class MessageDetailVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 消息类型
     * alert=预警消息 / calendar=农事提醒 / system=系统通知
     */
    private String messageType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息摘要
     */
    private String summary;

    /**
     * 消息完整内容
     */
    private String content;

    /**
     * 防治建议
     * 来自RAG检索或规则模板
     */
    private String advice;

    /**
     * 预警等级
     * 1=L1 低风险 / 2=L2 中风险 / 3=L3 高风险
     */
    private Integer level;

    /**
     * 关联果园ID
     */
    private Long orchardId;

    /**
     * 果园名称
     */
    private String orchardName;

    /**
     * 触发规则ID
     */
    private Integer ruleId;

    /**
     * 触发规则编码
     * 格式：R001, R002等
     */
    private String ruleCode;

    /**
     * 关联资源类型
     * detection=识别记录 / feedback=用户反馈
     */
    private String relatedType;

    /**
     * 关联资源ID
     */
    private Long relatedId;

    /**
     * 扩展数据（JSON格式字符串）
     * 包含病害名称、概率、图片URL等额外信息
     */
    private String extraData;

    /**
     * 已读状态
     * 0=未读 / 1=已读
     */
    private Integer readStatus;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 推送渠道
     * app=应用内消息 / sms=短信
     */
    private String pushChannel;

    /**
     * 推送状态
     * 0=待推送 / 1=已推送 / 2=推送失败
     */
    private Integer pushStatus;

    /**
     * 邮件推送状态描述
     * sent=已发送 / failed=推送失败 / pending=待推送
     * 由 pushStatus 整数映射生成，方便前端直接展示
     */
    private String pushEmailStatus;

    /**
     * 实际推送时间
     */
    private LocalDateTime pushTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /** 是否已处理 */
    private Boolean processed;

    /** 处理证据图片（JSON数组） */
    private String treatmentImages;
}
