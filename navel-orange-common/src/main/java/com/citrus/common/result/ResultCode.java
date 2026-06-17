package com.citrus.common.result;

import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证/Token过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    UNPROCESSABLE(422, "业务逻辑错误"),
    TOO_MANY_REQUESTS(429, "请求频率限制"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 认证相关 1001-1099
    SMS_CODE_ERROR(1001, "验证码错误或已过期"),
    SMS_SEND_FREQUENT(1002, "验证码发送过于频繁"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    PHONE_ALREADY_REGISTERED(1005, "手机号已注册"),
    PHONE_NOT_REGISTERED(1006, "手机号未注册"),
    SMS_DAILY_LIMIT_EXCEEDED(1007, "今日验证码发送次数已达上限"),
    OLD_PASSWORD_ERROR(1008, "原密码错误"),
    NEW_PASSWORD_SAME_AS_OLD(1009, "新密码不能与原密码相同"),
    PASSWORD_NOT_MATCH(1010, "两次输入的密码不一致"),

    // 果园相关 2001-2099
    ORCHARD_NOT_FOUND(2001, "果园不存在"),
    ORCHARD_NO_PERMISSION(2002, "无权操作该果园"),
    ORCHARD_LIMIT_EXCEEDED(2003, "果园创建数量已达上限，请联系管理员"),
    GEOCODE_FAILED(2004, "地址无法识别，请输入更详细的地址"),

    // 设备相关 3001-3099
    DEVICE_NOT_FOUND(3001, "设备不存在"),
    DEVICE_CODE_EXISTS(3002, "设备编码已存在"),
    API_KEY_INVALID(3003, "API Key无效"),

    // 消息相关 4001-4099
    MESSAGE_NOT_FOUND(4001, "消息不存在"),

    // 知识库相关 5001-5099
    FILE_FORMAT_NOT_SUPPORTED(5001, "上传文件格式不支持"),
    FILE_TOO_LARGE(5002, "上传文件过大"),
    DOCUMENT_PROCESS_FAILED(5003, "文献处理失败"),

    // 反馈相关 6001-6099
    FEEDBACK_NOT_FOUND(6001, "反馈不存在"),
    QUIZ_ALREADY_SUBMITTED(6002, "问卷已提交");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
