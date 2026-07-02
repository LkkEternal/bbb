package com.citrus.push.service;

public interface SmsPushService {

    boolean sendAlertSms(Long userId, String phone, String content, Integer level);
}
