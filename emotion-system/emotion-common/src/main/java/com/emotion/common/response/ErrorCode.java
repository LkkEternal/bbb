package com.emotion.common.response;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证/Token过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    CAPTCHA_ERROR(1001, "验证码错误或已过期"),
    CAPTCHA_FREQUENT(1002, "验证码发送过于频繁"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    ACCOUNT_LOCKED(1005, "账号已被锁定"),
    USERNAME_EXISTS(1006, "用户名已存在"),
    EMAIL_EXISTS(1007, "邮箱已注册"),
    PHONE_EXISTS(1008, "手机号已注册"),
    OLD_PASSWORD_ERROR(1009, "原密码错误"),
    NEW_PASSWORD_SAME(1010, "新密码不能与原密码相同"),
    USER_NOT_FOUND(1011, "用户不存在"),
    FILE_FORMAT_ERROR(1012, "文件格式不支持，仅支持jpg/jpeg/png"),
    FILE_TOO_LARGE(1013, "文件大小超出限制，最大2MB"),
    FILE_UPLOAD_FAILED(1014, "文件上传失败"),

    DETECTION_FAILED(4001, "检测失败"),
    NO_FACE_DETECTED(4002, "未检测到人脸"),
    IMAGE_FORMAT_ERROR(4003, "图片格式不支持"),
    IMAGE_TOO_LARGE(4004, "图片文件过大"),
    FEEDBACK_ALREADY_SUBMITTED(4005, "该检测记录已提交过反馈"),
    DETECTION_RECORD_NOT_FOUND(4006, "检测记录不存在"),
    ADDRESS_RESOLVE_FAILED(4007, "地址解析失败，请输入正确的城市名称"),

    CHAT_FAILED(5001, "AI服务调用失败"),
    ASSESSMENT_NOT_FOUND(5002, "评估记录不存在"),
    ASSESSMENT_COMPLETED(5003, "评估已完成"),
    ASSESSMENT_TIMEOUT(5004, "评估已超时"),

    WEATHER_API_ERROR(6001, "天气API请求失败"),
    WEATHER_DATA_EMPTY(6002, "天气数据为空"),
    WEATHER_DATA_PARSE_ERROR(6003, "天气数据解析失败"),

    ALERT_RULE_NOT_FOUND(7001, "预警规则不存在"),
    EMOTION_DICT_NOT_FOUND(7002, "表情字典不存在"),
    FEEDBACK_NOT_FOUND(7003, "反馈记录不存在"),
    FEEDBACK_ALREADY_REVIEWED(7004, "该反馈已审核"),
    FEEDBACK_EXPORT_EMPTY(7005, "暂无已采用的反馈数据可导出");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
