package com.citrus.common.constant;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {}

    // ==================== 用户状态 ====================
    public static final int USER_ENABLED = 1;
    public static final int USER_DISABLED = 0;

    // ==================== 设备状态 ====================
    public static final int DEVICE_ONLINE = 1;
    public static final int DEVICE_OFFLINE = 0;
    public static final int DEVICE_FAULT = 2;

    // ==================== 角色编码 ====================
    public static final String ROLE_FARMER = "ROLE_FARMER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // ==================== 果园成员角色 ====================
    public static final String ORCHARD_ROLE_OWNER = "owner";
    public static final String ORCHARD_ROLE_MANAGER = "manager";
    public static final String ORCHARD_ROLE_VIEWER = "viewer";

    // ==================== 果园限制 ====================
    public static final int MAX_ORCHARD_PER_USER = 6;  // 每个用户最多创建果园数（作为园主）

    // ==================== 地址精度限制 ====================
    // 允许的最小地址精度级别（至少需要乡镇级或村庄级）
    // 高德API level 字段可能的值：国家、省、市、区县、乡镇、村庄、街道、门牌号、兴趣点
    public static final java.util.List<String> ALLOWED_GEOCODE_LEVELS = java.util.List.of(
            "乡镇", "村庄", "街道", "门牌号", "兴趣点"
    );

    // ==================== JWT 相关 ====================
    public static final long ACCESS_TOKEN_EXPIRE_HOURS = 2;
    public static final long REFRESH_TOKEN_EXPIRE_DAYS = 30;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_API_KEY = "X-Api-Key";

    // ==================== Redis Key 前缀 ====================
    public static final String REDIS_REFRESH_TOKEN = "refresh_token:";
    public static final String REDIS_SMS_CODE = "sms:";
    public static final String REDIS_USER_TOKEN = "user_token:";
    public static final String REDIS_RATE_LIMIT = "rate_limit:";

    // ==================== 短信验证码 ====================
    public static final int SMS_CODE_LENGTH = 6;
    public static final int SMS_CODE_EXPIRE_MINUTES = 5;
    public static final int SMS_SEND_INTERVAL_SECONDS = 60;
    public static final int SMS_DAILY_LIMIT = 10;  // 每日发送上限
    public static final String REDIS_SMS_DAILY = "sms:daily:";  // 每日计数Key前缀

    // ==================== 病害编码 ====================
    public static final String DISEASE_CODE_PREFIX = "S";

    // ==================== 文献状态 ====================
    public static final int DOC_STATUS_PENDING = 0;
    public static final int DOC_STATUS_PROCESSING = 1;
    public static final int DOC_STATUS_COMPLETED = 2;
    public static final int DOC_STATUS_FAILED = 3;

    // ==================== 预警等级 ====================
    public static final int ALERT_LEVEL_L1 = 1;
    public static final int ALERT_LEVEL_L2 = 2;
    public static final int ALERT_LEVEL_L3 = 3;

    // ==================== 推送状态 ====================
    public static final int PUSH_STATUS_PENDING = 0;
    public static final int PUSH_STATUS_SENT = 1;
    public static final int PUSH_STATUS_FAILED = 2;

    // ==================== 已读状态 ====================
    public static final int READ_STATUS_UNREAD = 0;
    public static final int READ_STATUS_READ = 1;

    // ==================== 微信小程序 ====================
    public static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
}
