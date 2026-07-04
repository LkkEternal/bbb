package com.citrus.push.service;

public interface WechatPushService {

    boolean sendAlertNotification(Long messageId, Long userId, String title, String content,
                                  String advice, Integer level, String openid);

    String getAccessToken();
}
