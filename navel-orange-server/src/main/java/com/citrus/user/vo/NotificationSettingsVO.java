package com.citrus.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知设置响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsVO {

    /**
     * 是否开启短信通知
     */
    private Boolean smsEnabled;

    /**
     * 是否开启推送通知
     */
    private Boolean pushEnabled;
}
