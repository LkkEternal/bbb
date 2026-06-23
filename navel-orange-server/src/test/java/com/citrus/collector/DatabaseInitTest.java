package com.citrus.collector;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@SpringBootTest
public class DatabaseInitTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void initFarmingCalendarTable() {
        log.info("===== 创建 farming_calendar 表 =====");

        // 1. 删除旧表
        jdbcTemplate.execute("DROP TABLE IF EXISTS farming_calendar CASCADE");
        log.info("已删除旧表");

        // 2. 创建表
        jdbcTemplate.execute("""
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
            )
        """);
        log.info("表创建成功");

        // 3. 创建索引
        jdbcTemplate.execute("CREATE INDEX idx_calendar_orchard ON farming_calendar(orchard_id)");
        jdbcTemplate.execute("CREATE INDEX idx_calendar_term ON farming_calendar(solar_term)");
        log.info("索引创建成功");

        // 4. 创建触发器
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION update_farming_calendar_timestamp()
            RETURNS TRIGGER AS $$
            BEGIN
                NEW.updated_at = CURRENT_TIMESTAMP;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql
        """);
        jdbcTemplate.execute("""
            CREATE TRIGGER trg_farming_calendar_update
                BEFORE UPDATE ON farming_calendar
                FOR EACH ROW
                EXECUTE FUNCTION update_farming_calendar_timestamp()
        """);
        log.info("触发器创建成功");

        // 5. 添加注释
        jdbcTemplate.execute("COMMENT ON TABLE farming_calendar IS '农事日历表'");
        jdbcTemplate.execute("COMMENT ON COLUMN farming_calendar.orchard_id IS 'NULL=全局通用，指定=某果园专属'");
        jdbcTemplate.execute("COMMENT ON COLUMN farming_calendar.phenology_name IS '物候期名称'");
        jdbcTemplate.execute("COMMENT ON COLUMN farming_calendar.solar_term IS '关联节气'");
        jdbcTemplate.execute("COMMENT ON COLUMN farming_calendar.disease_related IS '关联病害编码数组'");
        log.info("注释添加成功");

        // 6. 插入预置数据
        jdbcTemplate.execute("""
            INSERT INTO farming_calendar (phenology_name, solar_term, start_month, start_day, end_month, end_day, advice_content, disease_related, sort_order) VALUES
            ('春梢萌发期', '惊蛰', 2, 19, 3, 5, '加强水肥管理，促进春梢生长。注意防治疮痂病、红蜘蛛、蚜虫。', '["S12","S14"]', 1),
            ('花蕾-开花期', '清明', 3, 20, 4, 20, '保花保果，注意防治蓟马、花蕾蛆。避免使用高毒农药。', '["S12","S14"]', 2),
            ('谢花-幼果期', '谷雨', 4, 20, 5, 20, '保果稳果，重点防治溃疡病、炭疽病。加强水肥管理。', '["S14","S10"]', 3),
            ('夏梢期', '芒种', 5, 21, 7, 7, '控制夏梢生长，防治潜叶蛾、柑橘木虱。注意排水防涝。', '["S1","S2"]', 4),
            ('果实膨大期', '大暑', 7, 7, 9, 7, '加强水肥管理，注意抗旱。重点防治锈壁虱、红蜘蛛。注意防日灼。', '["S1","S2"]', 5),
            ('秋梢期', '白露', 9, 7, 10, 8, '放好秋梢，防治潜叶蛾、木虱。注意防治炭疽病。', '["S1","S2"]', 6),
            ('果实成熟期', '霜降', 10, 8, 12, 7, '适时采收，注意防治炭疽病（蒂腐）。做好冬季清园准备。', '["S10","S13"]', 7)
        """);
        log.info("预置数据插入成功（7条物候期）");

        // 7. 验证数据
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM farming_calendar", Integer.class);
        log.info("验证：farming_calendar 表共有 {} 条记录", count);

        log.info("===== farming_calendar 表创建完成 =====");
    }
}
