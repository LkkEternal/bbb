-- =====================================================
-- 天气预警模块：预警消息表 + 预置预警规则
-- 需求编号：31
-- 说明：
--   1. alert_message 为新增表，用于存放天气预警触发产生的消息
--   2. alert_rule 规则种子按 rule_code 幂等写入（重复执行不会产生重复数据）
-- =====================================================

-- 1. 新建预警消息表
CREATE TABLE IF NOT EXISTS `alert_message` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_id`       INT          NOT NULL COMMENT '关联 alert_rule.id',
    `rule_name`     VARCHAR(100) DEFAULT NULL COMMENT '规则名称（冗余）',
    `location`      VARCHAR(10)  DEFAULT NULL COMMENT '高德 adcode',
    `city_name`     VARCHAR(50)  DEFAULT NULL COMMENT '城市名',
    `fx_date_start` DATE         DEFAULT NULL COMMENT '满足条件的4天窗口起始日',
    `fx_date_end`   DATE         DEFAULT NULL COMMENT '满足条件的4天窗口结束日',
    `alert_content` VARCHAR(500) DEFAULT NULL COMMENT '预警文案（日期区间/天气/温度等）',
    `active`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0=已解除 1=生效',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0=未读 1=已读',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    PRIMARY KEY (`id`),
    KEY `idx_alert_rule_loc` (`rule_id`, `location`, `active`),
    KEY `idx_alert_loc_active` (`location`, `active`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='天气预警消息表';

-- 2. 预置规则一：雨天低温悲伤预警
INSERT INTO `alert_rule` (
    `rule_name`, `rule_code`, `enabled`, `priority`,
    `emotion_labels`, `min_confidence`, `max_confidence`,
    `weather_conditions`, `min_temperature`, `max_temperature`,
    `min_humidity`, `max_humidity`,
    `time_window_hours`, `min_occurrences`,
    `action_type`, `action_content_template`, `cooldown_minutes`
)
SELECT
    '雨天低温悲伤预警', 'RAINY_SAD_ALERT', 1, 10,
    NULL, 0.60, 1.00,
    '小雨,中雨,大雨,暴雨,雷阵雨,毛毛雨,小雪,中雪,大雪,暴雪', -50.0, 15.0,
    60, NULL,
    24, 1,
    'push', NULL, 60
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'RAINY_SAD_ALERT');

-- 3. 预置规则二：高温烦躁预警（无天气/湿度条件，仅温度条件）
INSERT INTO `alert_rule` (
    `rule_name`, `rule_code`, `enabled`, `priority`,
    `emotion_labels`, `min_confidence`, `max_confidence`,
    `weather_conditions`, `min_temperature`, `max_temperature`,
    `min_humidity`, `max_humidity`,
    `time_window_hours`, `min_occurrences`,
    `action_type`, `action_content_template`, `cooldown_minutes`
)
SELECT
    '高温烦躁预警', 'HIGH_TEMP_ALERT', 1, 20,
    NULL, 0.60, 1.00,
    NULL, 35.0, 45.0,
    NULL, NULL,
    24, 1,
    'push', NULL, 60
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `alert_rule` WHERE `rule_code` = 'HIGH_TEMP_ALERT');
