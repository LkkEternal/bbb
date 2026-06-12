package com.citrus.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志模块枚举
 */
@Getter
@AllArgsConstructor
public enum LogModule {

    AUTH("auth", "认证模块"),
    USER("user", "用户管理"),
    ORCHARD("orchard", "果园管理"),
    DEVICE("device", "设备管理"),
    DETECTION("detection", "数据采集"),
    ALERT("alert", "预警规则"),
    PUSH("push", "消息推送"),
    KNOWLEDGE("knowledge", "知识库"),
    MINIAPP("miniapp", "小程序"),
    CALENDAR("calendar", "农事日历"),
    FEEDBACK("feedback", "用户反馈");

    private final String code;
    private final String name;
}
