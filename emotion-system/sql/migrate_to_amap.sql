-- =====================================================
-- 迁移脚本：从和风天气切换到高德地图API
-- 执行日期：2026-07-27
-- 说明：location_id 字段从和风天气城市ID改为高德地图区域编码(adcode)
-- =====================================================

-- 1. 修改 user 表字段注释（location_id 现在存储高德区域编码）
ALTER TABLE `user` 
MODIFY COLUMN `location_id` varchar(10) DEFAULT NULL COMMENT '高德地图区域编码(adcode)，如：440100=广州';

-- 2. 清空旧的 location_id 数据（和风天气ID格式为9位数字，高德adcode为6位数字）
-- 注意：执行此操作后，已有用户需要重新注册以获取正确的高德区域编码
UPDATE `user` SET `location_id` = NULL WHERE `location_id` IS NOT NULL AND LENGTH(`location_id`) != 6;

-- 3. 验证数据迁移结果
SELECT 
    id,
    username,
    city_name,
    location_id,
    CASE 
        WHEN location_id IS NULL THEN '未设置'
        WHEN LENGTH(location_id) = 6 THEN '有效(adcode)'
        ELSE '格式错误'
    END AS location_id_status
FROM `user`
LIMIT 10;

-- =====================================================
-- 附录：常用城市区域编码(adcode)对照表
-- =====================================================
-- 北京: 110000
-- 上海: 310000
-- 广州: 440100
-- 深圳: 440300
-- 杭州: 330100
-- 成都: 510100
-- 武汉: 420100
-- 南昌: 360100
-- 赣州: 360700

-- =====================================================
-- 注意事项：
-- 1. 执行此脚本前请先备份数据库
-- 2. 已有用户的 location_id 会被清空，需要重新注册
-- 3. 新注册用户将自动通过高德地图API获取区域编码
-- =====================================================
