-- ============================================================
-- AI聊天与心理健康评估系统 - 建表脚本
-- 数据库: bqjc (复用现有数据库)
-- 字符集: utf8mb4
-- 引擎: InnoDB
-- 说明: 管理员上传问卷文档后，题目数据通过管理后台录入
-- ============================================================

-- 1. 评估问卷定义表
-- 存储管理员创建的问卷信息，包含上传的源文档元数据
CREATE TABLE IF NOT EXISTS assessment_questionnaire (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name              VARCHAR(100) NOT NULL COMMENT '问卷名称，如"PHQ-9抑郁筛查量表"',
    code              VARCHAR(50) NOT NULL UNIQUE COMMENT '问卷编码，如 PHQ9 / GAD7，用于程序识别',
    description       VARCHAR(500) DEFAULT NULL COMMENT '问卷描述，说明该问卷的用途和适用人群',
    total_questions   INT NOT NULL DEFAULT 0 COMMENT '题目总数',
    is_active         TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1=启用，0=禁用，禁用后用户端不可选择',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    INDEX idx_q_code (code),
    INDEX idx_q_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评估问卷定义表，存储管理员创建的所有问卷及上传的源文档信息';


-- 2. 问卷题目表
-- 每个题目包含四个选项和各自的分值，通过不同的分值设计实现各维度的量化评估
CREATE TABLE IF NOT EXISTS assessment_question (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    questionnaire_id  BIGINT NOT NULL COMMENT '所属问卷ID，关联 assessment_questionnaire.id',
    question_index    INT NOT NULL COMMENT '题目序号，从1开始递增，表示题目在问卷中的顺序',
    question_text     VARCHAR(500) NOT NULL COMMENT '题目内容，如"做事时提不起劲或没有兴趣"',
    option_a          VARCHAR(200) NOT NULL COMMENT '选项A的文字描述',
    option_b          VARCHAR(200) NOT NULL COMMENT '选项B的文字描述',
    option_c          VARCHAR(200) NOT NULL COMMENT '选项C的文字描述',
    option_d          VARCHAR(200) NOT NULL COMMENT '选项D的文字描述',
    score_a           INT NOT NULL DEFAULT 0 COMMENT '选项A对应的分值，通常表示程度最轻',
    score_b           INT NOT NULL DEFAULT 1 COMMENT '选项B对应的分值',
    score_c           INT NOT NULL DEFAULT 2 COMMENT '选项C对应的分值',
    score_d           INT NOT NULL DEFAULT 3 COMMENT '选项D对应的分值，通常表示程度最重',
    is_active         TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1=启用，0=禁用，禁用后该题不参与评估',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id) ON DELETE CASCADE,
    INDEX idx_qn_qid (questionnaire_id),
    INDEX idx_qn_index (questionnaire_id, question_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='问卷题目表，存储每份问卷的全部题目及各选项的分值配置';


-- 3. 评分等级规则表（核心）
-- 定义各分数区间对应的等级和建议，替代硬编码
-- 通过总分匹配 [min_score, max_score] 得出等级和建议
CREATE TABLE IF NOT EXISTS assessment_grade_rule (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    questionnaire_id  BIGINT NOT NULL COMMENT '关联问卷ID，同一问卷可有多个分数区间规则',
    min_score         INT NOT NULL COMMENT '该等级的最低分（包含），如 10 表示≥10分进入此区间',
    max_score         INT NOT NULL COMMENT '该等级的最高分（包含），如 14 表示≤14分进入此区间',
    level_code        VARCHAR(30) NOT NULL COMMENT '等级编码，如 normal/mild/moderate/severe/extremely_severe，程序用',
    level_name        VARCHAR(50) NOT NULL COMMENT '等级中文名称，如 正常/轻度/中度/中重度/重度，展示给用户',
    advice_text       TEXT NOT NULL COMMENT '该等级对应的建议内容，管理员根据临床指南预先编写',
    sort_order        INT NOT NULL DEFAULT 0 COMMENT '排序顺序，数字越小越靠前',
    is_active         TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1=启用，0=禁用',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id) ON DELETE CASCADE,
    INDEX idx_gr_qid (questionnaire_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评分等级规则表，定义各分数区间对应的等级和硬编码建议，评估完成后通过查此表匹配结果';


-- 4. 用户评估记录表
-- 每次评估生成一条记录，只存最终总分和匹配出的等级建议，不存答题明细
CREATE TABLE IF NOT EXISTS assessment_record (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id           BIGINT NOT NULL COMMENT '用户ID，关联 sys_user.id，标识谁做的评估',
    questionnaire_id  BIGINT NOT NULL COMMENT '问卷ID，关联 assessment_questionnaire.id，标识做的哪份问卷',
    total_score       INT NOT NULL DEFAULT 0 COMMENT '总分，所有题目得分之和',
    level_code        VARCHAR(30) DEFAULT NULL COMMENT '等级编码，从 grade_rule 匹配得出',
    level_name        VARCHAR(50) DEFAULT NULL COMMENT '等级中文名称，从 grade_rule 匹配得出',
    advice_text       TEXT COMMENT '建议内容，从 grade_rule 匹配后写入，评估完成时冻结快照',
    status            VARCHAR(20) NOT NULL DEFAULT 'in_progress' COMMENT '状态：in_progress=进行中，completed=已完成，timeout=评估超时',
    started_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始评估的时间',
    completed_at      DATETIME DEFAULT NULL COMMENT '完成评估的时间，为NULL表示未完成',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id),
    INDEX idx_ar_user (user_id),
    INDEX idx_ar_status (user_id, status),
    INDEX idx_ar_completed (user_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户评估记录表，存储每次评估的最终结果，每开始一次评估生成一条记录';
