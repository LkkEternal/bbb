-- 插入测试用户
INSERT INTO sys_user (id, username, password, phone, status) 
VALUES (10001, '测试果农', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 1)
ON CONFLICT (id) DO NOTHING;

-- 插入用户角色
INSERT INTO sys_user_role (user_id, role_id) 
VALUES (10001, 1)
ON CONFLICT DO NOTHING;

-- 插入测试果园（经纬度：114.93,25.38 信丰县）
INSERT INTO orchard (id, owner_id, name, formatted_address, location, area_mu, tree_age, variety, status) 
VALUES (
    2001, 
    10001, 
    '测试果园', 
    '江西省赣州市信丰县嘉定镇马路坑村',
    ST_SetSRID(ST_MakePoint(114.93, 25.38), 4326)::geography,
    50.5,
    8,
    '纽荷尔脐橙',
    1
)
ON CONFLICT (id) DO NOTHING;
