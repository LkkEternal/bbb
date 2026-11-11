-- 检查系统操作日志表结构
DESC sys_operation_log;

-- 查看最近的登录日志
SELECT * FROM sys_operation_log
WHERE action = 'login'
ORDER BY operation_time DESC
LIMIT 10;

-- 检查是否有用户的登录日志，以及用户ID是否为空
SELECT
    id,
    user_id,
    module,
    action,
    description,
    ip_address,
    operation_time
FROM sys_operation_log
WHERE action = 'login'
ORDER BY operation_time DESC
LIMIT 20;

-- 统计登录日志中用户ID的记录情况
SELECT
    CASE
        WHEN user_id IS NULL THEN 'NULL'
        WHEN user_id = 0 THEN '0'
        ELSE '有值'
    END AS user_id_status,
    COUNT(*) as count
FROM sys_operation_log
WHERE action = 'login'
GROUP BY user_id_status;

-- 查看具体的登录日志详情（如果有）
SELECT
    id,
    user_id,
    module,
    action,
    description,
    ip_address,
    operation_time,
    detail
FROM sys_operation_log
WHERE action = 'login'
  AND user_id IS NULL
ORDER BY operation_time DESC
LIMIT 10;

-- 查看注册日志作为对比
SELECT
    id,
    user_id,
    module,
    action,
    description,
    ip_address,
    operation_time
FROM sys_operation_log
WHERE action = 'register'
ORDER BY operation_time DESC
LIMIT 10;
