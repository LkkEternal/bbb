package com.citrus.knowledge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 数据库初始化器 —— 自动创建缺失的知识库相关表
 * <p>
 * 仅在运行 `schema.sql` 后仍有遗漏时做补充，避免全量执行 schema.sql 的风险。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        // 检查 knowledge_document 表是否存在
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM knowledge_document", Integer.class);
            log.info("知识库表已存在，跳过初始化");
            return;
        } catch (Exception e) {
            log.info("知识库表不存在，开始自动建表 ...");
        }

        // 创建知识文献表
        jdbcTemplate.execute("""
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
            )
        """);

        // 创建知识分块表（含 pgvector 向量类型）
        try {
            jdbcTemplate.execute("""
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
                )
            """);
            // 创建 HNSW 索引
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON knowledge_chunk
                    USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64)
            """);
        } catch (Exception ex) {
            log.warn("knowledge_chunk 表创建失败（pgvector 可能未安装）: {}", ex.getMessage());
        }

        // 创建 RAG 反馈表
        jdbcTemplate.execute("""
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
            )
        """);

        log.info("知识库相关表初始化完成");
    }
}
