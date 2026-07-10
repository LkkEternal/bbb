-- ============================================================
-- 15. 风险预测结果表
-- ============================================================
CREATE TABLE IF NOT EXISTS risk_prediction (
    id              BIGSERIAL       PRIMARY KEY,
    orchard_id      BIGINT          NOT NULL REFERENCES orchard(id),
    risk_level      SMALLINT        NOT NULL DEFAULT 0,    -- 0/1/2/3
    risk_score      DECIMAL(5,2),                          -- max 概率
    probabilities   JSONB,                                 -- [p0, p1, p2, p3]
    llm_analysis    TEXT,                                  -- LLM 详细分析
    suggestions     TEXT,                                  -- 防治建议
    top_factors     JSONB,                                 -- Top-5 风险因子
    trigger_type    VARCHAR(20)     NOT NULL DEFAULT 'scheduled',  -- scheduled / detection_burst / seasonal
    trigger_detail  VARCHAR(200),                          -- 触发详情
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_prediction_orchard   ON risk_prediction(orchard_id);
CREATE INDEX IF NOT EXISTS idx_prediction_time      ON risk_prediction(created_at);
CREATE INDEX IF NOT EXISTS idx_prediction_level     ON risk_prediction(risk_level);
