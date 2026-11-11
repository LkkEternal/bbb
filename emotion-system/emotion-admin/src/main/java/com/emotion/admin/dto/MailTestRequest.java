package com.emotion.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 预警邮件测试发送请求（结构化字段，对应固定邮件模板占位符）
 */
@Data
public class MailTestRequest {

    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String to;

    /** 邮件主题（不填默认"表情检测预警提醒"） */
    private String subject;

    /** 标题（首行） */
    private String title;

    /** 用户称呼（昵称/用户名） */
    private String userName;

    /** 触发情绪标签（如 悲伤） */
    private String emotion;

    /** 触发次数 */
    private Integer count;

    /** 建议内容 */
    private String advice;

    /** 最近一次检测时间（yyyy-MM-dd HH:mm:ss） */
    private String lastTime;
}
