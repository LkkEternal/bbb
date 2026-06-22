-- ============================================================
-- 脐橙病虫害监测系统 - 建表SQL（PostgreSQL）
-- 主机: 10.168.89.200:5432
-- 数据库: postgres
-- ============================================================

-- 启用PostGIS扩展（用于GEOGRAPHY类型）
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================
-- 1. 用户表 sys_user
-- ============================================================
DROP TABLE IF EXISTS sys_user CASCADE;
CREATE TABLE sys_user (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     UNIQUE NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    phone           VARCHAR(20)     UNIQUE,
    wechat_openid   VARCHAR(64)     UNIQUE,
    avatar_url      VARCHAR(500),
    status          SMALLINT        NOT NULL DEFAULT 1,
    sms_enabled     BOOLEAN         NOT NULL DEFAULT FALSE,
    push_enabled    BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMP,
    last_login_ip   VARCHAR(45),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_user_phone ON sys_user(phone);
CREATE INDEX idx_user_wechat ON sys_user(wechat_openid);
CREATE INDEX idx_user_status ON sys_user(status);

CREATE OR REPLACE FUNCTION update_sys_user_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sys_user_update
    BEFORE UPDATE ON sys_user
    FOR EACH ROW
    EXECUTE FUNCTION update_sys_user_timestamp();

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.status IS '状态：0=禁用 1=启用';
COMMENT ON COLUMN sys_user.sms_enabled IS '是否接收短信推送';
COMMENT ON COLUMN sys_user.push_enabled IS '是否接收小程序推送';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除标记';

-- ============================================================
-- 2. 角色表 sys_role
-- ============================================================
DROP TABLE IF EXISTS sys_role CASCADE;
CREATE TABLE sys_role (
    id              SERIAL          PRIMARY KEY,
    code            VARCHAR(30)     UNIQUE NOT NULL,
    name            VARCHAR(50)     NOT NULL,
    description     VARCHAR(200)    DEFAULT '',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_role IS '角色表';

INSERT INTO sys_role (code, name, description) VALUES
('ROLE_FARMER', '农户', '查看自己的果园数据、历史预警、知识库检索、提交反馈'),
('ROLE_ADMIN', '管理员', '管理用户、导入文献、查看所有果园、调整规则、审核反馈、查看审计日志');

-- ============================================================
-- 3. 用户角色关联表 sys_user_role
-- ============================================================
DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
    user_id         BIGINT          NOT NULL REFERENCES sys_user(id),
    role_id         INT             NOT NULL REFERENCES sys_role(id),
    PRIMARY KEY (user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- ============================================================
-- 4. Refresh Token备份表 sys_refresh_token
-- ============================================================
DROP TABLE IF EXISTS sys_refresh_token CASCADE;
CREATE TABLE sys_refresh_token (
    id              BIGSERIAL       PRIMARY KEY,
    token_id        VARCHAR(64)     UNIQUE NOT NULL,
    user_id         BIGINT          NOT NULL REFERENCES sys_user(id),
    token_hash      VARCHAR(128)    NOT NULL,
    device_info     VARCHAR(200),
    expires_at      TIMESTAMP       NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rt_user ON sys_refresh_token(user_id);
CREATE INDEX idx_rt_token_id ON sys_refresh_token(token_id);
CREATE INDEX idx_rt_token ON sys_refresh_token(token_hash);
CREATE INDEX idx_rt_expires ON sys_refresh_token(expires_at);

COMMENT ON TABLE sys_refresh_token IS 'Refresh Token备份表';
COMMENT ON COLUMN sys_refresh_token.token_id IS '刷新令牌标识ID（返回给前端）';
COMMENT ON COLUMN sys_refresh_token.token_hash IS 'RefreshToken的SHA256哈希';

-- ============================================================
-- 5. 操作日志表 sys_operation_log
-- ============================================================
DROP TABLE IF EXISTS sys_operation_log CASCADE;
CREATE TABLE sys_operation_log (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT,
    module          VARCHAR(30),
    action          VARCHAR(50)     NOT NULL,
    resource_type   VARCHAR(50),
    resource_id     VARCHAR(50),
    description     VARCHAR(200),
    detail          TEXT,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_log_user ON sys_operation_log(user_id);
CREATE INDEX idx_log_action ON sys_operation_log(action);
CREATE INDEX idx_log_time ON sys_operation_log(created_at);

COMMENT ON TABLE sys_operation_log IS '操作日志表';
COMMENT ON COLUMN sys_operation_log.module IS '操作模块：auth/user/orchard/device/knowledge/feedback';
COMMENT ON COLUMN sys_operation_log.action IS '操作类型：login/logout/create/update/delete/export';

-- ============================================================
-- 6. 果园表 orchard
-- ============================================================
DROP TABLE IF EXISTS orchard CASCADE;
CREATE TABLE orchard (
    id                  BIGSERIAL       PRIMARY KEY,
    owner_id            BIGINT          NOT NULL REFERENCES sys_user(id),
    name                VARCHAR(100)    NOT NULL,
    formatted_address   VARCHAR(200)    DEFAULT '',
    location            GEOGRAPHY(POINT),
    area_mu             DECIMAL(10,2),
    tree_age            INT,
    variety             VARCHAR(50)     DEFAULT '脐橙',
    status              SMALLINT        NOT NULL DEFAULT 1,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_orchard_owner ON orchard(owner_id);
CREATE INDEX idx_orchard_location ON orchard USING GIST(location);

CREATE OR REPLACE FUNCTION update_orchard_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_orchard_update
    BEFORE UPDATE ON orchard
    FOR EACH ROW
    EXECUTE FUNCTION update_orchard_timestamp();

COMMENT ON TABLE orchard IS '果园表';
COMMENT ON COLUMN orchard.owner_id IS '园主ID（主要负责人）';
COMMENT ON COLUMN orchard.formatted_address IS '详细地址（高德API地理编码后的完整地址）';
COMMENT ON COLUMN orchard.location IS '经纬度（高德API地理编码后的位置）';
COMMENT ON COLUMN orchard.status IS '状态：0=停用 1=正常';

-- ============================================================
-- 7. 果园用户关联表 orchard_user
-- ============================================================
DROP TABLE IF EXISTS orchard_user CASCADE;
CREATE TABLE orchard_user (
    orchard_id      BIGINT          NOT NULL REFERENCES orchard(id),
    user_id         BIGINT          NOT NULL REFERENCES sys_user(id),
    role_type       VARCHAR(20)     NOT NULL DEFAULT 'viewer',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (orchard_id, user_id)
);

CREATE INDEX idx_ou_user ON orchard_user(user_id);

COMMENT ON TABLE orchard_user IS '果园用户关联表';
COMMENT ON COLUMN orchard_user.role_type IS '角色：owner=园主/manager=管理员/viewer=只读';

-- ============================================================
-- 8. 环境数据表 environment_data
-- ============================================================
DROP TABLE IF EXISTS environment_data CASCADE;
CREATE TABLE environment_data (
    id              BIGSERIAL       PRIMARY KEY,
    orchard_id      BIGINT          NOT NULL REFERENCES orchard(id),
    data_source     VARCHAR(20)     NOT NULL DEFAULT 'sensor',
    temp            DECIMAL(5,2),
    humidity        DECIMAL(5,2),
    precip          DECIMAL(8,2),
    windSpeed       DECIMAL(5,2),
    record_time     TIMESTAMP       NOT NULL,
    extra_data      JSONB,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_env_orchard ON environment_data(orchard_id);
CREATE INDEX idx_env_time ON environment_data(record_time);
CREATE INDEX idx_env_orchard_time ON environment_data(orchard_id, record_time);

COMMENT ON TABLE environment_data IS '环境数据表';
COMMENT ON COLUMN environment_data.data_source IS '数据来源：sensor=传感器/weather_api=天气API';
COMMENT ON COLUMN environment_data.temp IS '温度（℃）（对应和风天气API temp字段）';
COMMENT ON COLUMN environment_data.humidity IS '相对湿度（%）（对应和风天气API humidity字段）';
COMMENT ON COLUMN environment_data.precip IS '降雨量（mm）（对应和风天气API precip字段）';
COMMENT ON COLUMN environment_data.windSpeed IS '风速（km/h）（对应和风天气API windSpeed字段）';

-- ============================================================
-- 9. 天气预报表 weather_forecast
-- ============================================================
DROP TABLE IF EXISTS weather_forecast CASCADE;
CREATE TABLE weather_forecast (
    id              BIGSERIAL       PRIMARY KEY,
    orchard_id      BIGINT          NOT NULL REFERENCES orchard(id),
    fxTime          TIMESTAMP       NOT NULL,
    temp            DECIMAL(5,2),
    humidity        DECIMAL(5,2),
    pop             DECIMAL(5,2),
    precip          DECIMAL(8,2),
    text            VARCHAR(50),
    windSpeed       DECIMAL(5,2),
    api_source      VARCHAR(20)     NOT NULL DEFAULT 'qweather',
    fetch_time      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (orchard_id, fxTime, api_source)
);

CREATE INDEX idx_forecast_orchard ON weather_forecast(orchard_id);
CREATE INDEX idx_forecast_fxtime ON weather_forecast(fxTime);

COMMENT ON TABLE weather_forecast IS '天气预报表';
COMMENT ON COLUMN weather_forecast.fxTime IS '预报时间（对应和风天气API fxTime字段）';
COMMENT ON COLUMN weather_forecast.temp IS '温度（对应和风天气API temp字段）';
COMMENT ON COLUMN weather_forecast.humidity IS '湿度（对应和风天气API humidity字段）';
COMMENT ON COLUMN weather_forecast.pop IS '降水概率（%）（对应和风天气API pop字段）';
COMMENT ON COLUMN weather_forecast.precip IS '预计降雨量（mm）（对应和风天气API precip字段）';
COMMENT ON COLUMN weather_forecast.text IS '天气描述（对应和风天气API text字段）';
COMMENT ON COLUMN weather_forecast.windSpeed IS '风速（km/h）（对应和风天气API windSpeed字段）';
COMMENT ON COLUMN weather_forecast.api_source IS 'API来源：qweather（和风天气）';

-- ============================================================
-- 10. 农事日历表 farming_calendar
-- ============================================================
DROP TABLE IF EXISTS farming_calendar CASCADE;
CREATE TABLE farming_calendar (
    id               SERIAL       PRIMARY KEY,
    orchard_id       BIGINT,
    phenology_name   VARCHAR(50)  NOT NULL,
    solar_term       VARCHAR(20),
    start_month      SMALLINT     NOT NULL,
    start_day        SMALLINT     NOT NULL,
    end_month        SMALLINT     NOT NULL,
    end_day          SMALLINT     NOT NULL,
    advice_content   TEXT         NOT NULL,
    disease_related  JSONB,
    sort_order       INT          NOT NULL DEFAULT 0,
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_calendar_orchard ON farming_calendar(orchard_id);
CREATE INDEX idx_calendar_term ON farming_calendar(solar_term);

CREATE OR REPLACE FUNCTION update_farming_calendar_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_farming_calendar_update
    BEFORE UPDATE ON farming_calendar
    FOR EACH ROW
    EXECUTE FUNCTION update_farming_calendar_timestamp();

COMMENT ON TABLE farming_calendar IS '农事日历表';
COMMENT ON COLUMN farming_calendar.orchard_id IS 'NULL=全局通用，指定=某果园专属';
COMMENT ON COLUMN farming_calendar.phenology_name IS '物候期名称';
COMMENT ON COLUMN farming_calendar.solar_term IS '关联节气';
COMMENT ON COLUMN farming_calendar.disease_related IS '关联病害编码数组：["S14","S10"]';

-- 插入脐橙7个物候期预置数据
INSERT INTO farming_calendar (phenology_name, solar_term, start_month, start_day, end_month, end_day, advice_content, disease_related, sort_order) VALUES
('春梢萌发期', '惊蛰', 2, 19, 3, 5, '加强水肥管理，促进春梢生长。注意防治疮痂病、红蜘蛛、蚜虫。', '["S12","S14"]', 1),
('花蕾-开花期', '清明', 3, 20, 4, 20, '保花保果，注意防治蓟马、花蕾蛆。避免使用高毒农药。', '["S12","S14"]', 2),
('谢花-幼果期', '谷雨', 4, 20, 5, 20, '保果稳果，重点防治溃疡病、炭疽病。加强水肥管理。', '["S14","S10"]', 3),
('夏梢期', '芒种', 5, 21, 7, 7, '控制夏梢生长，防治潜叶蛾、柑橘木虱。注意排水防涝。', '["S1","S2"]', 4),
('果实膨大期', '大暑', 7, 7, 9, 7, '加强水肥管理，注意抗旱。重点防治锈壁虱、红蜘蛛。注意防日灼。', '["S1","S2"]', 5),
('秋梢期', '白露', 9, 7, 10, 8, '放好秋梢，防治潜叶蛾、木虱。注意防治炭疽病。', '["S1","S2"]', 6),
('果实成熟期', '霜降', 10, 8, 12, 7, '适时采收，注意防治炭疽病（蒂腐）。做好冬季清园准备。', '["S10","S13"]', 7);

-- ============================================================
-- 完成提示
-- ============================================================
-- 共创建10张表
-- 1. sys_user - 用户表
-- 2. sys_role - 角色表
-- 3. sys_user_role - 用户角色关联表
-- 4. sys_refresh_token - Refresh Token备份表
-- 5. sys_operation_log - 操作日志表
-- 6. orchard - 果园表
-- 7. orchard_user - 果园用户关联表
-- 8. environment_data - 环境数据表
-- 9. weather_forecast - 天气预报表
-- 10. farming_calendar - 农事日历表
