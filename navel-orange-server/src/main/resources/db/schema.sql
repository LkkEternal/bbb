-- ============================================================
-- 脐橙病虫害监测与数据收集系统 - 数据库初始化脚本
-- 数据库: PostgreSQL 16 + pgvector 0.8.2
-- 字符集: UTF-8
-- 时区: Asia/Shanghai
-- ============================================================

-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================
-- 1. 用户权限域
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGSERIAL       PRIMARY KEY,
    username      VARCHAR(50)     NOT NULL,
    password      VARCHAR(255)    NOT NULL,
    phone         VARCHAR(20)     UNIQUE,
    avatar_url    VARCHAR(500),
    status        SMALLINT        NOT NULL DEFAULT 1,
    email_enabled  BOOLEAN         NOT NULL DEFAULT FALSE,
    push_enabled  BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       BOOLEAN         NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_user_phone ON sys_user(phone);
CREATE INDEX IF NOT EXISTS idx_user_status ON sys_user(status);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          SERIAL       PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200) DEFAULT '',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表（一个用户只能有一个角色）
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id INT    NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- Refresh Token 备份表
CREATE TABLE IF NOT EXISTS sys_refresh_token (
    id          BIGSERIAL    PRIMARY KEY,
    token_id    VARCHAR(64)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(128) NOT NULL,
    device_info VARCHAR(200),
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rt_user ON sys_refresh_token(user_id);
CREATE INDEX IF NOT EXISTS idx_rt_token_id ON sys_refresh_token(token_id);
CREATE INDEX IF NOT EXISTS idx_rt_token ON sys_refresh_token(token_hash);
CREATE INDEX IF NOT EXISTS idx_rt_expires ON sys_refresh_token(expires_at);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT,
    module        VARCHAR(30),
    action        VARCHAR(50)  NOT NULL,
    resource_type VARCHAR(50),
    resource_id   VARCHAR(50),
    description   VARCHAR(200),
    detail        TEXT,
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(500),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_log_user ON sys_operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_log_action ON sys_operation_log(action);
CREATE INDEX IF NOT EXISTS idx_log_time ON sys_operation_log(created_at);

-- ============================================================
-- 2. 果园管理域
-- ============================================================

-- 果园表
CREATE TABLE IF NOT EXISTS orchard (
    id          BIGSERIAL      PRIMARY KEY,
    owner_id    BIGINT         NOT NULL,
    name        VARCHAR(100)   NOT NULL,
    province    VARCHAR(20)    DEFAULT '',
    city        VARCHAR(20)    DEFAULT '',
    district    VARCHAR(20)    DEFAULT '',
    address     VARCHAR(200)   DEFAULT '',
    longitude   DECIMAL(10,7),
    latitude    DECIMAL(10,7),
    area_mu     DECIMAL(10,2),
    tree_age    INT,
    variety     VARCHAR(50)    DEFAULT '脐橙',
    status      SMALLINT       NOT NULL DEFAULT 1,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     BOOLEAN        NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_orchard_owner ON orchard(owner_id);
CREATE INDEX IF NOT EXISTS idx_orchard_location ON orchard(latitude, longitude);

-- 果园用户关联表
CREATE TABLE IF NOT EXISTS orchard_user (
    orchard_id  BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    role_type   VARCHAR(20) NOT NULL DEFAULT 'viewer',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (orchard_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_ou_user ON orchard_user(user_id);

-- 边缘设备表
CREATE TABLE IF NOT EXISTS device (
    id              BIGSERIAL    PRIMARY KEY,
    device_code     VARCHAR(64)  NOT NULL UNIQUE,
    orchard_id      BIGINT       NOT NULL,
    name            VARCHAR(100) DEFAULT '',
    type            VARCHAR(30)  NOT NULL DEFAULT 'camera',
    model           VARCHAR(50),
    api_key         VARCHAR(128) NOT NULL UNIQUE,
    location_desc   VARCHAR(100),
    status          SMALLINT     NOT NULL DEFAULT 1,
    last_heartbeat  TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_device_code ON device(device_code);
CREATE INDEX IF NOT EXISTS idx_device_orchard ON device(orchard_id);
CREATE INDEX IF NOT EXISTS idx_device_apikey ON device(api_key);

-- ============================================================
-- 3. 数据字典域
-- ============================================================

-- 病害字典表
CREATE TABLE IF NOT EXISTS disease_catalog (
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

-- ============================================================
-- 4. 数据采集域
-- ============================================================

-- 病害识别记录表
CREATE TABLE IF NOT EXISTS disease_detection (
    id                  BIGSERIAL     PRIMARY KEY,
    device_id           BIGINT        NOT NULL,
    orchard_id          BIGINT        NOT NULL,
    disease_catalog_id  INT           NOT NULL,
    disease_name_raw    VARCHAR(50),
    top_class_code      VARCHAR(20)   NOT NULL,
    top_probability     DECIMAL(5,4)  NOT NULL,
    all_probabilities   JSONB,
    image_url           VARCHAR(500),
    device_timestamp    TIMESTAMP     NOT NULL,
    extra_data          JSONB,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_detection_device ON disease_detection(device_id);
CREATE INDEX IF NOT EXISTS idx_detection_orchard ON disease_detection(orchard_id);
CREATE INDEX IF NOT EXISTS idx_detection_catalog ON disease_detection(disease_catalog_id);
CREATE INDEX IF NOT EXISTS idx_detection_class ON disease_detection(top_class_code);
CREATE INDEX IF NOT EXISTS idx_detection_time ON disease_detection(device_timestamp);
CREATE INDEX IF NOT EXISTS idx_detection_orchard_time ON disease_detection(orchard_id, device_timestamp);

-- 环境数据表
CREATE TABLE IF NOT EXISTS environment_data (
    id            BIGSERIAL     PRIMARY KEY,
    orchard_id    BIGINT        NOT NULL,
    data_source   VARCHAR(20)   NOT NULL DEFAULT 'sensor',
    temperature   DECIMAL(5,2),
    humidity      DECIMAL(5,2),
    rainfall      DECIMAL(8,2),
    wind_speed    DECIMAL(5,2),
    record_time   TIMESTAMP     NOT NULL,
    extra_data    JSONB,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_env_orchard ON environment_data(orchard_id);
CREATE INDEX IF NOT EXISTS idx_env_time ON environment_data(record_time);
CREATE INDEX IF NOT EXISTS idx_env_orchard_time ON environment_data(orchard_id, record_time);

-- 天气预报表
CREATE TABLE IF NOT EXISTS weather_forecast (
    id                BIGSERIAL     PRIMARY KEY,
    orchard_id        BIGINT        NOT NULL,
    fxtime            TIMESTAMP     NOT NULL,
    temp_max          DECIMAL(5,2),
    temp_min          DECIMAL(5,2),
    humidity          DECIMAL(5,2),
    pop               DECIMAL(5,2),
    precip            DECIMAL(8,2),
    text              VARCHAR(50),
    windspeed         DECIMAL(5,2),
    api_source        VARCHAR(20)   NOT NULL,
    fetch_time        TIMESTAMP     NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_forecast UNIQUE (orchard_id, fxtime, api_source)
);
CREATE INDEX IF NOT EXISTS idx_forecast_orchard ON weather_forecast(orchard_id);
CREATE INDEX IF NOT EXISTS idx_forecast_fxtime ON weather_forecast(fxtime);

-- ============================================================
-- 5. 预警消息域
-- ============================================================

-- 预警规则表
CREATE TABLE IF NOT EXISTS alert_rule (
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

-- 预警消息表
CREATE TABLE IF NOT EXISTS alert_message (
    id              BIGSERIAL     PRIMARY KEY,
    orchard_id      BIGINT        NOT NULL,
    user_id         BIGINT        NOT NULL,
    rule_id         INT,
    calendar_id     INT,
    message_type    VARCHAR(20)   NOT NULL DEFAULT 'alert',
    level           SMALLINT      NOT NULL,
    title           VARCHAR(200)  NOT NULL,
    summary         VARCHAR(500),
    content         TEXT          NOT NULL,
    advice          TEXT,
    related_type    VARCHAR(30),
    related_id      BIGINT,
    extra_data      JSONB,
    push_channel    VARCHAR(20)   NOT NULL DEFAULT 'app',
    push_status     SMALLINT      NOT NULL DEFAULT 0,
    push_time       TIMESTAMP,
    read_status     SMALLINT      NOT NULL DEFAULT 0,
    read_time       TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_alert_user ON alert_message(user_id);
CREATE INDEX IF NOT EXISTS idx_alert_orchard ON alert_message(orchard_id);
CREATE INDEX IF NOT EXISTS idx_alert_level ON alert_message(level);
CREATE INDEX IF NOT EXISTS idx_alert_status ON alert_message(push_status, created_at);
CREATE INDEX IF NOT EXISTS idx_alert_user_read ON alert_message(user_id, read_status);

-- ============================================================
-- 6. 农事日历域
-- ============================================================

-- 农事日历表
CREATE TABLE IF NOT EXISTS farming_calendar (
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

-- ============================================================
-- 7. 知识库域
-- ============================================================

-- 知识文献表
CREATE TABLE IF NOT EXISTS knowledge_document (
    id              BIGSERIAL     PRIMARY KEY,
    title           VARCHAR(200)  NOT NULL,
    file_url        VARCHAR(500),
    file_type       VARCHAR(20),
    file_size       BIGINT,
    content_text    TEXT,
    category        VARCHAR(50)   DEFAULT '',
    source          VARCHAR(30)   NOT NULL DEFAULT 'ADMIN_UPLOAD',
    source_id       BIGINT,
    chunk_count     INT           NOT NULL DEFAULT 0,
    embedding_model VARCHAR(50),
    status          SMALLINT      NOT NULL DEFAULT 0,
    review_status   SMALLINT      NOT NULL DEFAULT 0,
    review_comment  VARCHAR(500),
    reviewed_by     BIGINT,
    reviewed_at     TIMESTAMP,
    error_message   TEXT,
    uploaded_by     BIGINT,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_doc_status ON knowledge_document(status);
CREATE INDEX IF NOT EXISTS idx_doc_category ON knowledge_document(category);
CREATE INDEX IF NOT EXISTS idx_doc_source ON knowledge_document(source);
CREATE INDEX IF NOT EXISTS idx_doc_review ON knowledge_document(review_status);

-- 知识分块表（使用 pgvector 向量类型）
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id           BIGSERIAL  PRIMARY KEY,
    document_id  BIGINT     NOT NULL,
    chunk_index  INT        NOT NULL,
    content      TEXT       NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    embedding    vector(512),
    metadata     JSONB,
    token_count  INT,
    created_at   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_chunk_doc ON knowledge_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_hash ON knowledge_chunk(content_hash);

-- 向量索引（HNSW 索引，适合中小规模数据集）
CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON knowledge_chunk
    USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

-- ============================================================
-- 8. 用户反馈域
-- ============================================================

-- 用户反馈表
CREATE TABLE IF NOT EXISTS user_feedback (
    id               BIGSERIAL     PRIMARY KEY,
    user_id          BIGINT        NOT NULL,
    orchard_id       BIGINT        NOT NULL,
    image_urls       JSONB,
    description      TEXT,
    feedback_type    VARCHAR(20)   NOT NULL,
    ai_diagnosis     JSONB,
    ai_top_disease   VARCHAR(50),
    ai_confidence    DECIMAL(5,4),
    symptom_quiz     JSONB,
    user_answers     JSONB,
    match_score      DECIMAL(3,2),
    review_status    SMALLINT      NOT NULL DEFAULT 0,
    reviewed_by      BIGINT,
    reviewed_at      TIMESTAMP,
    review_comment   VARCHAR(500),
    knowledge_doc_id BIGINT,
    dataset_tag      VARCHAR(50),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_fb_user ON user_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_fb_orchard ON user_feedback(orchard_id);
CREATE INDEX IF NOT EXISTS idx_fb_status ON user_feedback(review_status);
CREATE INDEX IF NOT EXISTS idx_fb_type ON user_feedback(feedback_type);
CREATE INDEX IF NOT EXISTS idx_fb_created ON user_feedback(created_at);

-- RAG 问答反馈表
CREATE TABLE IF NOT EXISTS rag_feedback (
    id              BIGSERIAL     PRIMARY KEY,
    session_id      VARCHAR(64)   NOT NULL,
    question        TEXT          NOT NULL,
    answer          TEXT          NOT NULL,
    model           VARCHAR(100),
    similarity      DECIMAL(5,4),
    feedback_type   SMALLINT      NOT NULL DEFAULT 0,
    user_id         BIGINT,
    remark          VARCHAR(500),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rag_fb_session ON rag_feedback(session_id);
CREATE INDEX IF NOT EXISTS idx_rag_fb_type ON rag_feedback(feedback_type);

-- ============================================================
-- 9. 触发器函数（自动更新 updated_at）
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
        'alert_rule', 'farming_calendar', 'knowledge_document'
    ]) LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%s_update BEFORE UPDATE ON %s '
            'FOR EACH ROW EXECUTE FUNCTION update_timestamp()',
            tbl, tbl
        );
    END LOOP;
END $$;

-- ============================================================
-- 10. 初始数据
-- ============================================================

-- 角色初始数据
INSERT INTO sys_role (code, name, description) VALUES
    ('ROLE_FARMER', '农户', '查看自己的果园数据、历史预警、知识库检索、提交反馈'),
    ('ROLE_ADMIN', '管理员', '管理用户、导入文献、查看所有果园、调整规则、审核反馈、查看审计日志')
ON CONFLICT (code) DO NOTHING;

-- 病害字典初始数据
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

-- 预警规则初始数据
INSERT INTO alert_rule (name, code, level, description, temp_min, humidity_min, disease_code, detection_count, detection_days, cooldown_hours, enabled) VALUES
    ('高温高湿溃疡病预警', 'R001', 3, '温度≥25且湿度≥85且近7天溃疡病识别≥3次', 25.00, 85.00, 'S14', 3, 7, 24, TRUE),
    ('黄龙病早期预警',     'R002', 3, '近7天斑驳花纹或红鼻果识别≥2次',         NULL, NULL, 'S3',  2, 7, 48, TRUE),
    ('炭疽病高发预警',     'R003', 2, '温度25-32且湿度≥80且近7天炭疽病识别≥2次', 25.00, 80.00, 'S10', 2, 7, 24, TRUE)
ON CONFLICT (code) DO NOTHING;
