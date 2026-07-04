package com.citrus.push.service;

/**
 * 邮件推送服务（网易 163 邮箱）
 */
public interface EmailPushService {

    /**
     * 发送预警邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（HTML）
     * @return true=发送成功
     */
    boolean sendAlertEmail(String to, String subject, String content);
}
