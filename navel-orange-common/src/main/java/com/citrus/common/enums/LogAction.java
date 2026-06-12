package com.citrus.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志操作枚举
 */
@Getter
@AllArgsConstructor
public enum LogAction {

    LOGIN("login", "登录"),
    LOGOUT("logout", "退出"),
    REGISTER("register", "注册"),
    CREATE("create", "新增"),
    UPDATE("update", "修改"),
    DELETE("delete", "删除"),
    EXPORT("export", "导出"),
    IMPORT("import", "导入"),
    UPLOAD("upload", "上传"),
    DOWNLOAD("download", "下载"),
    ASSIGN("assign", "分配"),
    REVIEW("review", "审核"),
    ENABLE("enable", "启用"),
    DISABLE("disable", "禁用"),
    SEND("send", "发送");

    private final String code;
    private final String name;
}
