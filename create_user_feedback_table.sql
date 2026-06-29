-- ============================================================
-- 用户反馈表 (user_feedback)
-- 用于存储农户提交的果园问题反馈
-- ============================================================

CREATE TABLE IF NOT EXISTS user_feedback (
    id               BIGSERIAL     PRIMARY KEY,
    user_id          BIGINT        NOT NULL,
    orchard_id       BIGINT        NOT NULL,
    image_urls       JSONB,
    description      TEXT,
    feedback_type    VARCHAR(20)   NOT NULL,
    review_status    SMALLINT      NOT NULL DEFAULT 0,
    reviewed_by      BIGINT,
    reviewed_at      TIMESTAMP,
    review_comment   VARCHAR(500),
    dataset_tag      VARCHAR(50),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 表注释
-- ============================================================
COMMENT ON TABLE user_feedback IS '用户反馈表 - 存储农户提交的果园问题反馈';

-- ============================================================
-- 字段注释（鼠标悬浮时显示的中文说明）
-- ============================================================
COMMENT ON COLUMN user_feedback.id               IS '主键ID，自增';
COMMENT ON COLUMN user_feedback.user_id          IS '提交用户ID，关联sys_user表';
COMMENT ON COLUMN user_feedback.orchard_id       IS '果园ID，关联orchard表';
COMMENT ON COLUMN user_feedback.image_urls       IS '图片URL数组（JSONB格式），如：["https://xxx/img1.jpg","https://xxx/img2.jpg"]';
COMMENT ON COLUMN user_feedback.description      IS '问题描述，最长500字';
COMMENT ON COLUMN user_feedback.feedback_type    IS '反馈类型：manual_diagnosis=人工诊断, issue_report=问题上报';
COMMENT ON COLUMN user_feedback.review_status    IS '审核状态：0=待审核, 1=已采纳, 2=已修正, 3=已驳回';
COMMENT ON COLUMN user_feedback.reviewed_by      IS '审核人ID，关联sys_user表';
COMMENT ON COLUMN user_feedback.reviewed_at      IS '审核时间';
COMMENT ON COLUMN user_feedback.review_comment   IS '审核意见，管理员填写的审核备注';
COMMENT ON COLUMN user_feedback.dataset_tag      IS '训练集标签，用于导出模型训练数据';
COMMENT ON COLUMN user_feedback.created_at       IS '创建时间，反馈提交时间';

-- ============================================================
-- 索引
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_fb_user    ON user_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_fb_orchard ON user_feedback(orchard_id);
CREATE INDEX IF NOT EXISTS idx_fb_status  ON user_feedback(review_status);
CREATE INDEX IF NOT EXISTS idx_fb_type    ON user_feedback(feedback_type);
CREATE INDEX IF NOT EXISTS idx_fb_created ON user_feedback(created_at);

-- ============================================================
-- 外键约束（可选，根据实际需要启用）
-- ============================================================
-- ALTER TABLE user_feedback ADD CONSTRAINT fk_fb_user FOREIGN KEY (user_id) REFERENCES sys_user(id);
-- ALTER TABLE user_feedback ADD CONSTRAINT fk_fb_orchard FOREIGN KEY (orchard_id) REFERENCES orchard(id);
-- ALTER TABLE user_feedback ADD CONSTRAINT fk_fb_reviewer FOREIGN KEY (reviewed_by) REFERENCES sys_user(id);
