-- 风险预测结果表
CREATE TABLE IF NOT EXISTS risk_prediction (
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
