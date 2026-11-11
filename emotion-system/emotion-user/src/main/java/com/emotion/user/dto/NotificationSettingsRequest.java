package com.emotion.user.dto;

import lombok.Data;

/**
 * 通知设置更新请求（部分更新，未传字段不修改）
 */
@Data
public class NotificationSettingsRequest {

    private Boolean emailEnabled;

    private Boolean pushEnabled;
}
