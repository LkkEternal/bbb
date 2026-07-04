-- 修复：sys_user 表缺少 wechat_openid 列
-- 该列在实体类 User.java 中定义，但建表时被遗漏
ALTER TABLE sys_user
ADD COLUMN IF NOT EXISTS wechat_openid VARCHAR(64) UNIQUE;

CREATE INDEX IF NOT EXISTS idx_user_wechat ON sys_user(wechat_openid);
