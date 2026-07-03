package com.citrus.user.dto;

import lombok.Data;

/**
 * 通知设置请求DTO
 */
@Data
public class NotificationSettingsDTO {

    /**
     * 是否开启邮件通知
     */
    private Boolean emailEnabled;

    /**
     * 是否开启推送通知
     */
    private Boolean pushEnabled;
}
