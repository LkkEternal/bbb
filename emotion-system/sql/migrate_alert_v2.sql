-- =====================================================
-- 预警模块 v2：通知设置字段 + 预警消息表扩展
-- 需求编号：31（情绪预警 + 邮件提醒）
-- 说明：
--   1. sys_user 新增 push_enabled / email_enabled 通知设置
--   2. alert_message 新增 user_id（情绪预警归属用户）、email_sent（邮件只发一次标记）
-- =====================================================

-- 1. sys_user 增加通知设置字段
ALTER TABLE `sys_user`
    ADD COLUMN `push_enabled`  TINYINT NOT NULL DEFAULT 1 COMMENT '是否接受预警消息：1=接收 0=不接收（情绪+邮件定时任务对该用户关闭，天气预警按地区不受影响）' AFTER `deleted`,
    ADD COLUMN `email_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启邮箱推送：1=开启 0=关闭' AFTER `push_enabled`;

-- 2. alert_message 增加情绪预警归属与邮件发送标记
ALTER TABLE `alert_message`
    ADD COLUMN `user_id`    BIGINT  DEFAULT NULL COMMENT '情绪预警关联 sys_user.id，天气预警为NULL' AFTER `city_name`,
    ADD COLUMN `email_sent` TINYINT NOT NULL DEFAULT 0 COMMENT '0=未发邮件 1=已发（只发一次）' AFTER `status`;

ALTER TABLE `alert_message` ADD KEY `idx_alert_rule_user` (`rule_id`, `user_id`, `active`);
