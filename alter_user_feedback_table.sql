-- ============================================================
-- 用户反馈表字段精简
-- 删除AI相关字段，保留核心功能字段
-- ============================================================

-- 删除不需要的字段
ALTER TABLE user_feedback DROP COLUMN IF EXISTS ai_diagnosis;
ALTER TABLE user_feedback DROP COLUMN IF EXISTS ai_top_disease;
ALTER TABLE user_feedback DROP COLUMN IF EXISTS ai_confidence;
ALTER TABLE user_feedback DROP COLUMN IF EXISTS symptom_quiz;
ALTER TABLE user_feedback DROP COLUMN IF EXISTS user_answers;
ALTER TABLE user_feedback DROP COLUMN IF EXISTS match_score;
ALTER TABLE user_feedback DROP COLUMN IF EXISTS knowledge_doc_id;

-- 更新表注释
COMMENT ON TABLE user_feedback IS '用户反馈表 - 存储农户提交的果园问题反馈';

-- 更新字段注释
COMMENT ON COLUMN user_feedback.review_status IS '审核状态：0=待审核, 1=已采纳, 2=已修正, 3=已驳回';
COMMENT ON COLUMN user_feedback.dataset_tag IS '训练集标签，用于导出模型训练数据';
