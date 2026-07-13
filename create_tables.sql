-- ============================================================
-- 脐橙病虫害监测与数据收集系统 - 建表SQL（PostgreSQL 16）
-- 数据库: PostgreSQL 16 + pgvector 0.8.2
-- 字符集: UTF-8
-- 时区: Asia/Shanghai
-- 说明: 该文件汇总了项目全部 DDL，使用 DROP TABLE IF EXISTS
--       重新建表（适用于全新初始化），生产环境请使用 Flyway 迁移。
-- ============================================================

-- 启用必要扩展
-- pgvector：用于知识库文本向量检索
CREATE EXTENSION IF NOT EXISTS vector;
-- PostGIS：用于果园位置 GEOGRAPHY(POINT) 类型
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================
-- 1. 用户权限域
-- ============================================================

DROP TABLE IF EXISTS sys_user CASCADE;
CREATE TABLE sys_user (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    phone           VARCHAR(20)     UNIQUE,
    wechat_openid   VARCHAR(64)     UNIQUE,
    avatar_url      VARCHAR(500),
    status          SMALLINT        NOT NULL DEFAULT 1,
    email_enabled   BOOLEAN         NOT NULL DEFAULT FALSE,
    push_enabled    BOOLEAN         NOT NULL DEFAULT TRUE,
    email           VARCHAR(100),
    last_login_at   TIMESTAMP,
    last_login_ip   VARCHAR(45),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_user_phone ON sys_user(phone);
CREATE INDEX IF NOT EXISTS idx_user_wechat ON sys_user(wechat_openid);
CREATE INDEX IF NOT EXISTS idx_user_status ON sys_user(status);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.status IS '状态：0=禁用 1=启用';
COMMENT ON COLUMN sys_user.email_enabled IS '是否接收邮件推送';
COMMENT ON COLUMN sys_user.push_enabled IS '是否接收小程序推送';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除标记';

-- 角色表
DROP TABLE IF EXISTS sys_role CASCADE;
CREATE TABLE sys_role (
    id              SERIAL          PRIMARY KEY,
    code            VARCHAR(30)     NOT NULL UNIQUE,
    name            VARCHAR(50)     NOT NULL,
    description     VARCHAR(200)    DEFAULT '',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_role IS '角色表';

INSERT INTO sys_role (code, name, description) VALUES
    ('ROLE_FARMER', '农户', '查看自己的果园数据、历史预警、知识库检索、提交反馈'),
    ('ROLE_ADMIN', '管理员', '管理用户、导入文献、查看所有果园、调整规则、审核反馈、查看审计日志')
ON CONFLICT (code) DO NOTHING;

-- 用户角色关联表（一个用户只能有一个角色）
DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
    user_id         BIGINT          NOT NULL,
    role_id         INT             NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- Refresh Token 备份表
DROP TABLE IF EXISTS sys_refresh_token CASCADE;
CREATE TABLE sys_refresh_token (
    id              BIGSERIAL       PRIMARY KEY,
    token_id        VARCHAR(64)     NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL,
    token_hash      VARCHAR(128)    NOT NULL,
    device_info     VARCHAR(200),
    expires_at      TIMESTAMP       NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rt_user ON sys_refresh_token(user_id);
CREATE INDEX IF NOT EXISTS idx_rt_token_id ON sys_refresh_token(token_id);
CREATE INDEX IF NOT EXISTS idx_rt_token ON sys_refresh_token(token_hash);
CREATE INDEX IF NOT EXISTS idx_rt_expires ON sys_refresh_token(expires_at);

COMMENT ON TABLE sys_refresh_token IS 'Refresh Token备份表';
COMMENT ON COLUMN sys_refresh_token.token_id IS '刷新令牌标识ID（返回给前端）';
COMMENT ON COLUMN sys_refresh_token.token_hash IS 'RefreshToken的SHA256哈希';

-- 操作日志表
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
CREATE INDEX IF NOT EXISTS idx_log_user ON sys_operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_log_action ON sys_operation_log(action);
CREATE INDEX IF NOT EXISTS idx_log_time ON sys_operation_log(created_at);

COMMENT ON TABLE sys_operation_log IS '操作日志表';
COMMENT ON COLUMN sys_operation_log.module IS '操作模块：auth/user/orchard/device/knowledge/feedback';
COMMENT ON COLUMN sys_operation_log.action IS '操作类型：login/logout/create/update/delete/export';

-- ============================================================
-- 2. 果园管理域
-- ============================================================

-- 果园表
DROP TABLE IF EXISTS orchard CASCADE;
CREATE TABLE orchard (
    id                  BIGSERIAL       PRIMARY KEY,
    owner_id            BIGINT          NOT NULL,
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
CREATE INDEX IF NOT EXISTS idx_orchard_owner ON orchard(owner_id);
CREATE INDEX IF NOT EXISTS idx_orchard_location ON orchard USING GIST(location);

COMMENT ON TABLE orchard IS '果园表';
COMMENT ON COLUMN orchard.owner_id IS '园主ID（主要负责人）';
COMMENT ON COLUMN orchard.formatted_address IS '详细地址（高德API地理编码后的完整地址）';
COMMENT ON COLUMN orchard.location IS '经纬度，格式：经度,纬度';
COMMENT ON COLUMN orchard.status IS '状态：0=停用 1=正常';

-- 果园用户关联表
DROP TABLE IF EXISTS orchard_user CASCADE;
CREATE TABLE orchard_user (
    orchard_id      BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    role_type       VARCHAR(20)     NOT NULL DEFAULT 'viewer',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (orchard_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_ou_user ON orchard_user(user_id);

COMMENT ON TABLE orchard_user IS '果园用户关联表';
COMMENT ON COLUMN orchard_user.role_type IS '角色：owner=园主/manager=管理员/viewer=只读';

-- 边缘设备表
DROP TABLE IF EXISTS device CASCADE;
CREATE TABLE device (
    id              BIGSERIAL       PRIMARY KEY,
    device_code     VARCHAR(64)     NOT NULL UNIQUE,
    orchard_id      BIGINT          NOT NULL,
    name            VARCHAR(100)    DEFAULT '',
    type            VARCHAR(30)     NOT NULL DEFAULT 'camera',
    model           VARCHAR(50),
    api_key         VARCHAR(128)    NOT NULL UNIQUE,
    location_desc   VARCHAR(100),
    status          SMALLINT        NOT NULL DEFAULT 1,
    last_heartbeat  TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_device_code ON device(device_code);
CREATE INDEX IF NOT EXISTS idx_device_orchard ON device(orchard_id);
CREATE INDEX IF NOT EXISTS idx_device_apikey ON device(api_key);

COMMENT ON TABLE device IS '边缘设备表';
COMMENT ON COLUMN device.device_code IS '设备编码（硬件烧录）';
COMMENT ON COLUMN device.type IS '设备类型：camera=摄像头 sensor=传感器';
COMMENT ON COLUMN device.api_key IS '设备API密钥（SHA256存储）';
COMMENT ON COLUMN device.status IS '状态：0=离线 1=在线 2=故障';

-- IoTDA设备信息表（从华为云IoTDA平台同步的设备信息）
DROP TABLE IF EXISTS iotda_device CASCADE;
CREATE TABLE iotda_device (
    id              BIGSERIAL       PRIMARY KEY,
    device_id       VARCHAR(128)    UNIQUE NOT NULL,
    device_name     VARCHAR(200),
    product_id      VARCHAR(128),
    product_name    VARCHAR(200),
    status          VARCHAR(20),
    description     TEXT,
    fw_version      VARCHAR(50),
    sw_version      VARCHAR(50),
    local_device_id BIGINT,
    sync_time       TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_iotda_device_id ON iotda_device(device_id);
CREATE INDEX IF NOT EXISTS idx_iotda_local_device ON iotda_device(local_device_id);

COMMENT ON TABLE iotda_device IS 'IoTDA设备信息表';
COMMENT ON COLUMN iotda_device.device_id IS 'IoTDA设备ID';
COMMENT ON COLUMN iotda_device.device_name IS '设备名称';
COMMENT ON COLUMN iotda_device.product_id IS '产品ID';
COMMENT ON COLUMN iotda_device.product_name IS '产品名称';
COMMENT ON COLUMN iotda_device.status IS '设备状态：ONLINE, OFFLINE, INACTIVE';
COMMENT ON COLUMN iotda_device.local_device_id IS '关联的本地设备ID（device表的id）';
COMMENT ON COLUMN iotda_device.sync_time IS '同步时间';

-- ============================================================
-- 3. 数据字典域
-- ============================================================

-- 病害字典表
DROP TABLE IF EXISTS disease_catalog CASCADE;
CREATE TABLE disease_catalog (
    id                   SERIAL       PRIMARY KEY,
    disease_code         VARCHAR(20)  NOT NULL UNIQUE,
    disease_name         VARCHAR(50)  NOT NULL UNIQUE,
    disease_name_en      VARCHAR(100),
    disease_type         VARCHAR(20)  NOT NULL,
    category             VARCHAR(30)  NOT NULL,
    severity_level       SMALLINT     NOT NULL DEFAULT 1,
    symptom_desc         TEXT,
    prevention_guide     TEXT,
    symptom_quiz_template JSONB,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_dc_code ON disease_catalog(disease_code);
CREATE INDEX IF NOT EXISTS idx_dc_type ON disease_catalog(disease_type);

COMMENT ON TABLE disease_catalog IS '病害字典表';

INSERT INTO disease_catalog (disease_code, disease_name, disease_type, category, severity_level) VALUES
    ('S1',  '健康果实',   'healthy',    '健康',   1),
    ('S2',  '健康叶片',   'healthy',    '健康',   1),
    ('S3',  '斑驳花纹',   'disease',    '黄龙病', 3),
    ('S4',  '红鼻果',     'disease',    '黄龙病', 3),
    ('S5',  '缺锌',       'deficiency', '缺素症', 1),
    ('S6',  '叶脉黄化',   'deficiency', '缺素症', 1),
    ('S7',  '均匀黄化',   'deficiency', '缺素症', 1),
    ('S8',  '缺镁',       'deficiency', '缺素症', 1),
    ('S9',  '缺硼',       'deficiency', '缺素症', 1),
    ('S10', '炭疽病',     'disease',    '真菌病害', 2),
    ('S11', '脂点黄斑病', 'disease',    '真菌病害', 2),
    ('S12', '藻斑与苔藓', 'other',      '藻类',   1),
    ('S13', '煤污病',     'disease',    '真菌病害', 1),
    ('S14', '溃疡病',     'disease',    '细菌病害', 2)
ON CONFLICT (disease_code) DO NOTHING;

-- ============================================================
-- 4. 数据采集域
-- ============================================================

-- 病害识别记录表
DROP TABLE IF EXISTS disease_detection CASCADE;
CREATE TABLE disease_detection (
    id                  BIGSERIAL       PRIMARY KEY,
    device_id           BIGINT          NOT NULL,
    orchard_id          BIGINT          NOT NULL,
    disease_catalog_id  INT             NOT NULL,
    disease_name_raw    VARCHAR(50),
    top_class_code      VARCHAR(20)     NOT NULL,
    top_probability     DECIMAL(5,4)    NOT NULL,
    all_probabilities   JSONB,
    image_url           VARCHAR(500),
    device_timestamp    TIMESTAMP       NOT NULL,
    extra_data          JSONB,
    processed           BOOLEAN         DEFAULT FALSE,
    processed_at        TIMESTAMP,
    treatment_images    JSONB,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_detection_device ON disease_detection(device_id);
CREATE INDEX IF NOT EXISTS idx_detection_orchard ON disease_detection(orchard_id);
CREATE INDEX IF NOT EXISTS idx_detection_catalog ON disease_detection(disease_catalog_id);
CREATE INDEX IF NOT EXISTS idx_detection_class ON disease_detection(top_class_code);
CREATE INDEX IF NOT EXISTS idx_detection_time ON disease_detection(device_timestamp);
CREATE INDEX IF NOT EXISTS idx_detection_orchard_time ON disease_detection(orchard_id, device_timestamp);

COMMENT ON TABLE disease_detection IS '病害识别记录表';

-- 环境数据表
DROP TABLE IF EXISTS environment_data CASCADE;
CREATE TABLE environment_data (
    id              BIGSERIAL       PRIMARY KEY,
    orchard_id      BIGINT          NOT NULL,
    data_source     VARCHAR(20)     NOT NULL DEFAULT 'sensor',
    temp            DECIMAL(5,2),
    humidity        DECIMAL(5,2),
    precip          DECIMAL(8,2),
    windSpeed       DECIMAL(5,2),
    record_time     TIMESTAMP       NOT NULL,
    extra_data      JSONB,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_env_orchard ON environment_data(orchard_id);
CREATE INDEX IF NOT EXISTS idx_env_time ON environment_data(record_time);
CREATE INDEX IF NOT EXISTS idx_env_orchard_time ON environment_data(orchard_id, record_time);

COMMENT ON TABLE environment_data IS '环境数据表';
COMMENT ON COLUMN environment_data.data_source IS '数据来源：sensor=传感器/weather_api=天气API';
COMMENT ON COLUMN environment_data.temp IS '温度（℃）（对应和风天气API temp字段）';
COMMENT ON COLUMN environment_data.humidity IS '相对湿度（%）（对应和风天气API humidity字段）';
COMMENT ON COLUMN environment_data.precip IS '降雨量（mm）（对应和风天气API precip字段）';
COMMENT ON COLUMN environment_data.windSpeed IS '风速（km/h）（对应和风天气API windSpeed字段）';

-- 天气预报表
DROP TABLE IF EXISTS weather_forecast CASCADE;
CREATE TABLE weather_forecast (
    id              BIGSERIAL       PRIMARY KEY,
    orchard_id      BIGINT          NOT NULL,
    fxtime          TIMESTAMP       NOT NULL,
    temp_max        DECIMAL(5,2),
    temp_min        DECIMAL(5,2),
    humidity        DECIMAL(5,2),
    pop             DECIMAL(5,2),
    precip          DECIMAL(8,2),
    text            VARCHAR(50),
    windspeed       DECIMAL(5,2),
    api_source      VARCHAR(20)     NOT NULL,
    fetch_time      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_forecast UNIQUE (orchard_id, fxtime, api_source)
);
CREATE INDEX IF NOT EXISTS idx_forecast_orchard ON weather_forecast(orchard_id);
CREATE INDEX IF NOT EXISTS idx_forecast_fxtime ON weather_forecast(fxtime);

COMMENT ON TABLE weather_forecast IS '天气预报表';
COMMENT ON COLUMN weather_forecast.fxtime IS '预报时间（对应和风天气API fxTime字段）';
COMMENT ON COLUMN weather_forecast.temp_max IS '最高温度（℃）';
COMMENT ON COLUMN weather_forecast.temp_min IS '最低温度（℃）';
COMMENT ON COLUMN weather_forecast.humidity IS '湿度（%）';
COMMENT ON COLUMN weather_forecast.pop IS '降水概率（%）';
COMMENT ON COLUMN weather_forecast.precip IS '预计降雨量（mm）';
COMMENT ON COLUMN weather_forecast.text IS '天气描述';
COMMENT ON COLUMN weather_forecast.windspeed IS '风速（km/h）';
COMMENT ON COLUMN weather_forecast.api_source IS 'API来源：qweather（和风天气）';

-- ============================================================
-- 5. 预警消息域
-- ============================================================

-- 预警规则表
DROP TABLE IF EXISTS alert_rule CASCADE;
CREATE TABLE alert_rule (
    id                SERIAL        PRIMARY KEY,
    name              VARCHAR(100)  NOT NULL,
    code              VARCHAR(50)   NOT NULL UNIQUE,
    level             SMALLINT      NOT NULL,
    description       VARCHAR(500)  DEFAULT '',
    advice_template   TEXT,
    temp_min          DECIMAL(5,2),
    temp_max          DECIMAL(5,2),
    humidity_min      DECIMAL(5,2),
    humidity_max      DECIMAL(5,2),
    disease_code      VARCHAR(20),
    detection_count   INT,
    detection_days    INT,
    forecast_rain_min DECIMAL(8,2),
    forecast_wind_min DECIMAL(5,2),
    enabled           BOOLEAN       NOT NULL DEFAULT TRUE,
    cooldown_hours    INT           NOT NULL DEFAULT 24,
    created_by        BIGINT,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE alert_rule IS '预警规则表';

INSERT INTO alert_rule (name, code, level, description, temp_min, humidity_min, disease_code, detection_count, detection_days, cooldown_hours, enabled) VALUES
    ('高温高湿溃疡病预警', 'R001', 3, '温度≥25且湿度≥85且近7天溃疡病识别≥3次', 25.00, 85.00, 'S14', 3, 7, 24, TRUE),
    ('黄龙病早期预警',     'R002', 3, '近7天斑驳花纹或红鼻果识别≥2次',         NULL, NULL, 'S3',  2, 7, 48, TRUE),
    ('炭疽病高发预警',     'R003', 2, '温度25-32且湿度≥80且近7天炭疽病识别≥2次', 25.00, 80.00, 'S10', 2, 7, 24, TRUE)
ON CONFLICT (code) DO NOTHING;

-- 预警消息表
DROP TABLE IF EXISTS alert_message CASCADE;
CREATE TABLE alert_message (
    id              BIGSERIAL       PRIMARY KEY,
    orchard_id      BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    rule_id         INT,
    calendar_id     INT,
    message_type    VARCHAR(20)     NOT NULL DEFAULT 'alert',
    level           SMALLINT        NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    summary         VARCHAR(500),
    content         TEXT            NOT NULL,
    advice          TEXT,
    related_type    VARCHAR(30),
    related_id      BIGINT,
    extra_data      JSONB,
    push_channel    VARCHAR(20)     NOT NULL DEFAULT 'app',
    push_status     SMALLINT        NOT NULL DEFAULT 0,
    push_time       TIMESTAMP,
    read_status     SMALLINT        NOT NULL DEFAULT 0,
    read_time       TIMESTAMP,
    processed       BOOLEAN         DEFAULT FALSE,
    treatment_images JSONB,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_alert_user ON alert_message(user_id);
CREATE INDEX IF NOT EXISTS idx_alert_orchard ON alert_message(orchard_id);
CREATE INDEX IF NOT EXISTS idx_alert_level ON alert_message(level);
CREATE INDEX IF NOT EXISTS idx_alert_status ON alert_message(push_status, created_at);
CREATE INDEX IF NOT EXISTS idx_alert_user_read ON alert_message(user_id, read_status);

COMMENT ON TABLE alert_message IS '预警消息表';
COMMENT ON COLUMN alert_message.message_type IS 'alert=预警消息 / calendar=农事提醒 / system=系统通知';
COMMENT ON COLUMN alert_message.level IS '1=L1 低风险-关注 / 2=L2 中风险-预警 / 3=L3 高风险-紧急';
COMMENT ON COLUMN alert_message.push_channel IS 'app=应用内消息 / sms=短信';

-- ============================================================
-- 6. 农事日历域
-- ============================================================

-- 农事日历表
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
CREATE INDEX IF NOT EXISTS idx_calendar_orchard ON farming_calendar(orchard_id);
CREATE INDEX IF NOT EXISTS idx_calendar_term ON farming_calendar(solar_term);

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
    ('果实成熟期', '霜降', 10, 8, 12, 7, '适时采收，注意防治炭疽病（蒂腐）。做好冬季清园准备。', '["S10","S13"]', 7)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 7. 知识库域
-- ============================================================

-- 知识文献表
DROP TABLE IF EXISTS knowledge_document CASCADE;
CREATE TABLE knowledge_document (
    id              BIGSERIAL       PRIMARY KEY,
    title           VARCHAR(200)    NOT NULL,
    file_url        VARCHAR(500),
    file_type       VARCHAR(20),
    file_size       BIGINT,
    content_text    TEXT,
    category        VARCHAR(50)     DEFAULT '',
    source          VARCHAR(30)     NOT NULL DEFAULT 'ADMIN_UPLOAD',
    source_id       BIGINT,
    chunk_count     INT             NOT NULL DEFAULT 0,
    embedding_model VARCHAR(50),
    status          SMALLINT        NOT NULL DEFAULT 0,
    review_status   SMALLINT        NOT NULL DEFAULT 0,
    review_comment  VARCHAR(500),
    reviewed_by     BIGINT,
    reviewed_at     TIMESTAMP,
    error_message   TEXT,
    uploaded_by     BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_doc_status ON knowledge_document(status);
CREATE INDEX IF NOT EXISTS idx_doc_category ON knowledge_document(category);
CREATE INDEX IF NOT EXISTS idx_doc_source ON knowledge_document(source);
CREATE INDEX IF NOT EXISTS idx_doc_review ON knowledge_document(review_status);

COMMENT ON TABLE knowledge_document IS '知识文献表';
COMMENT ON COLUMN knowledge_document.source IS '来源：ADMIN_UPLOAD=管理员上传 / RAG_FLOW=自动采集';
COMMENT ON COLUMN knowledge_document.status IS '处理状态：0=待处理 1=处理中 2=已完成 3=失败';
COMMENT ON COLUMN knowledge_document.review_status IS '审核状态：0=待审核 1=通过 2=驳回';

-- 知识分块表（使用 pgvector 向量类型）
DROP TABLE IF EXISTS knowledge_chunk CASCADE;
CREATE TABLE knowledge_chunk (
    id           BIGSERIAL       PRIMARY KEY,
    document_id  BIGINT          NOT NULL,
    chunk_index  INT             NOT NULL,
    content      TEXT            NOT NULL,
    content_hash VARCHAR(64)     NOT NULL,
    embedding    vector(512),
    metadata     JSONB,
    token_count  INT,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_chunk_doc ON knowledge_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_hash ON knowledge_chunk(content_hash);

-- 向量索引（HNSW 索引，适合中小规模数据集）
CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON knowledge_chunk
    USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

COMMENT ON TABLE knowledge_chunk IS '知识分块表（使用 pgvector 向量类型）';
COMMENT ON COLUMN knowledge_chunk.embedding IS '512维文本嵌入向量';

-- ============================================================
-- 8. 用户反馈域
-- ============================================================

-- 用户反馈表
DROP TABLE IF EXISTS user_feedback CASCADE;
CREATE TABLE user_feedback (
    id               BIGSERIAL       PRIMARY KEY,
    user_id          BIGINT          NOT NULL,
    orchard_id       BIGINT          NOT NULL,
    image_urls       JSONB,
    description      TEXT,
    feedback_type    VARCHAR(20)     NOT NULL,
    ai_diagnosis     JSONB,
    ai_top_disease   VARCHAR(50),
    ai_confidence    DECIMAL(5,4),
    symptom_quiz     JSONB,
    user_answers     JSONB,
    match_score      DECIMAL(3,2),
    review_status    SMALLINT        NOT NULL DEFAULT 0,
    reviewed_by      BIGINT,
    reviewed_at      TIMESTAMP,
    review_comment   VARCHAR(500),
    knowledge_doc_id BIGINT,
    dataset_tag      VARCHAR(50),
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_fb_user ON user_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_fb_orchard ON user_feedback(orchard_id);
CREATE INDEX IF NOT EXISTS idx_fb_status ON user_feedback(review_status);
CREATE INDEX IF NOT EXISTS idx_fb_type ON user_feedback(feedback_type);
CREATE INDEX IF NOT EXISTS idx_fb_created ON user_feedback(created_at);

COMMENT ON TABLE user_feedback IS '用户反馈表';
COMMENT ON COLUMN user_feedback.feedback_type IS '反馈类型：disease=病害识别 / growth=生长情况 / pest=虫害 / other=其他';
COMMENT ON COLUMN user_feedback.review_status IS '审核状态：0=待审核 1=已采纳 2=已驳回';

-- RAG 问答反馈表
DROP TABLE IF EXISTS rag_feedback CASCADE;
CREATE TABLE rag_feedback (
    id              BIGSERIAL       PRIMARY KEY,
    session_id      VARCHAR(64)     NOT NULL,
    question        TEXT            NOT NULL,
    answer          TEXT            NOT NULL,
    model           VARCHAR(100),
    similarity      DECIMAL(5,4),
    feedback_type   SMALLINT        NOT NULL DEFAULT 0,
    user_id         BIGINT,
    remark          VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rag_fb_session ON rag_feedback(session_id);
CREATE INDEX IF NOT EXISTS idx_rag_fb_type ON rag_feedback(feedback_type);

COMMENT ON TABLE rag_feedback IS 'RAG问答反馈表';
COMMENT ON COLUMN rag_feedback.feedback_type IS '反馈类型：0=无反馈 1=有用 2=无用 3=不准确';

-- ============================================================
-- 9. OTA 固件升级域
-- ============================================================

-- 固件包管理表
DROP TABLE IF EXISTS firmware_package CASCADE;
CREATE TABLE firmware_package (
    id              BIGSERIAL       PRIMARY KEY,
    file_name       VARCHAR(200)    NOT NULL,
    version         VARCHAR(50)     NOT NULL,
    file_size       BIGINT          NOT NULL DEFAULT 0,
    file_url        VARCHAR(1000)   NOT NULL,
    md5             VARCHAR(64),
    description     TEXT,
    status          SMALLINT        NOT NULL DEFAULT 0
                    CHECK (status IN (0, 1, 2)),
    ota_product_id  VARCHAR(128),
    ota_package_id  VARCHAR(128),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE firmware_package IS '固件包管理';
COMMENT ON COLUMN firmware_package.file_name IS '文件名（如 test_01.bin）';
COMMENT ON COLUMN firmware_package.version IS '版本号（如 v1.0.1）';
COMMENT ON COLUMN firmware_package.status IS '状态: 0=待发布 1=已发布 2=已废弃';
COMMENT ON COLUMN firmware_package.ota_product_id IS '华为云 IoTDA 产品 ID';
COMMENT ON COLUMN firmware_package.ota_package_id IS '华为云 IoTDA 升级包 ID';

-- ============================================================
-- 10. 风险预测域
-- ============================================================

-- 风险预测结果表
DROP TABLE IF EXISTS risk_prediction CASCADE;
CREATE TABLE risk_prediction (
    id              BIGSERIAL       PRIMARY KEY,
    orchard_id      BIGINT          NOT NULL REFERENCES orchard(id),
    risk_level      SMALLINT        NOT NULL DEFAULT 0,
    risk_score      DECIMAL(5,2),
    probabilities   JSONB,
    llm_analysis    TEXT,
    suggestions     TEXT,
    top_factors     JSONB,
    trigger_type    VARCHAR(20)     NOT NULL DEFAULT 'scheduled',
    trigger_detail  VARCHAR(200),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_prediction_orchard   ON risk_prediction(orchard_id);
CREATE INDEX IF NOT EXISTS idx_prediction_time      ON risk_prediction(created_at);
CREATE INDEX IF NOT EXISTS idx_prediction_level     ON risk_prediction(risk_level);

COMMENT ON TABLE risk_prediction IS '风险预测结果表';
COMMENT ON COLUMN risk_prediction.risk_level IS '风险等级：0=低 1=中 2=高 3=严重';
COMMENT ON COLUMN risk_prediction.trigger_type IS '触发方式：scheduled=定时 / manual=手动';

-- ============================================================
-- 11. 触发器函数（自动更新 updated_at）
-- ============================================================

CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为所有需要自动更新 updated_at 的表创建触发器
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'sys_user', 'orchard', 'device',
        'iotda_device',
        'alert_rule', 'farming_calendar', 'knowledge_document',
        'firmware_package'
    ]) LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%s_update BEFORE UPDATE ON %s '
            'FOR EACH ROW EXECUTE FUNCTION update_timestamp()',
            tbl, tbl
        );
    END LOOP;
END $$;

-- ============================================================
-- 完成提示
-- ============================================================
-- 共创建 22 张表，涵盖以下域：
--   1. 用户权限域:  sys_user, sys_role, sys_user_role, sys_refresh_token, sys_operation_log
--   2. 果园管理域:  orchard, orchard_user, device, iotda_device
--   3. 数据字典域:  disease_catalog
--   4. 数据采集域:  disease_detection, environment_data, weather_forecast
--   5. 预警消息域:  alert_rule, alert_message
--   6. 农事日历域:  farming_calendar
--   7. 知识库域:    knowledge_document, knowledge_chunk (pgvector)
--   8. 用户反馈域:  user_feedback, rag_feedback
--   9. OTA 固件域:  firmware_package
--  10. 风险预测域:  risk_prediction
