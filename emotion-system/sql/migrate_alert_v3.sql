-- =====================================================
-- 预警模块 v3：alert_message 增加情绪预警结构化数据列
-- 需求编号：31（情绪预警 + 邮件提醒 - 静态模板填充）
-- 说明：
--   情绪预警触发时写入 emotion_count / emotion_label / last_detect_time，
--   邮件任务从这三列读取结构化数据填充固定静态邮件模板。
--   天气预警（user_id 为 NULL）这三列仍为 NULL。
-- =====================================================

ALTER TABLE `alert_message`
    ADD COLUMN `emotion_count`    INT      DEFAULT NULL COMMENT '情绪预警触发次数（天气预警为NULL）' AFTER `email_sent`,
    ADD COLUMN `emotion_label`    VARCHAR(50) DEFAULT NULL COMMENT '触发情绪标签（如 悲伤，多个用/分隔；天气预警为NULL）' AFTER `emotion_count`,
    ADD COLUMN `last_detect_time` DATETIME DEFAULT NULL COMMENT '最近一次命中检测时间（天气预警为NULL）' AFTER `emotion_label`;
