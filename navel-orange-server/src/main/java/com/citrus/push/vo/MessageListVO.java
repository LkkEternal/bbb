package com.citrus.push.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息列表VO（View Object）
 *
 * 用于消息列表接口的响应数据
 * 包含消息的基本信息，不包含完整内容和防治建议
 */
@Data
public class MessageListVO {

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
     * 通过关联查询orchard表获取
     */
    private String orchardName;

    /**
     * 触发规则编码
     * 格式：R001, R002等
     * 由rule_id转换而来
     */
    private String ruleCode;

    /**
     * 已读状态
     * 0=未读 / 1=已读
     */
    private Integer readStatus;

    /**
     * 处理状态
     * true=已处理 / false=未处理
     */
    private Boolean processed;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
