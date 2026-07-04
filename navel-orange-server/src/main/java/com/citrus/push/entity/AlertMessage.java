package com.citrus.push.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.citrus.config.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警消息实体类
 * 对应数据库表：alert_message
 *
 * 用于存储系统产生的各类消息，包括：
 * - 预警消息（alert）：由预警规则触发产生的病害预警
 * - 农事提醒（calendar）：由农事日历触发的农事提醒
 * - 系统通知（system）：系统管理员发送的通知
 */
@Data
@TableName(value = "alert_message", autoResultMap = true)
public class AlertMessage {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联果园ID
     * 消息所属的果园
     */
    @TableField("orchard_id")
    private Long orchardId;

    /**
     * 接收用户ID
     * 消息的目标接收者
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 触发规则ID
     * 当消息由预警规则触发时，记录对应的规则ID
     * 农事日历触发时为NULL
     */
    @TableField("rule_id")
    private Integer ruleId;

    /**
     * 触发日历ID
     * 当消息由农事日历触发时，记录对应的日历ID
     * 预警规则触发时为NULL
     */
    @TableField("calendar_id")
    private Integer calendarId;

    /**
     * 消息类型
     * alert=预警消息 / calendar=农事提醒 / system=系统通知
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 预警等级
     * 1=L1 低风险-关注 / 2=L2 中风险-预警 / 3=L3 高风险-紧急
     */
    private Integer level;

    /**
     * 消息标题
     * 简短的消息标题，用于列表展示
     */
    private String title;

    /**
     * 消息摘要
     * 消息的简要描述，用于列表展示
     */
    private String summary;

    /**
     * 消息完整内容
     * 消息的详细内容
     */
    private String content;

    /**
     * 防治建议
     * 来自RAG检索或规则模板的防治建议
     */
    private String advice;

    /**
     * 关联资源类型
     * detection=识别记录 / feedback=用户反馈
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 关联资源ID
     * 与related_type配合，指向具体的资源记录
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 扩展数据（JSONB格式）
     * 存储额外信息，如病害名称、概率、图片URL等
     * 示例：{"diseaseName":"溃疡病","probability":0.92,"imageUrl":"..."}
     */
    @TableField(value = "extra_data", typeHandler = JsonbTypeHandler.class)
    private String extraData;

    /**
     * 推送渠道
     * app=应用内消息 / sms=短信
     */
    @TableField("push_channel")
    private String pushChannel;

    /**
     * 推送状态
     * 0=待推送 / 1=已推送 / 2=推送失败
     */
    @TableField("push_status")
    private Integer pushStatus;

    /**
     * 实际推送时间
     * 消息成功推送到用户的时间
     */
    @TableField("push_time")
    private LocalDateTime pushTime;

    /**
     * 已读状态
     * 0=未读 / 1=已读
     */
    @TableField("read_status")
    private Integer readStatus;

    /**
     * 阅读时间
     * 用户阅读消息的时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 创建时间
     * 消息生成的时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 是否已处理
     */
    @TableField("processed")
    private Boolean processed;

    /**
     * 处理证据图片（JSONB 数组）
     */
    @TableField(value = "treatment_images", typeHandler = JsonbTypeHandler.class)
    private String treatmentImages;
}
