-- ============================================================
-- V1: 创建固件包管理表 (firmware_package)
-- 用于 OTA 固件升级功能
-- ============================================================

CREATE TABLE IF NOT EXISTS firmware_package (
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
COMMENT ON COLUMN firmware_package.file_size IS '文件大小（字节）';
COMMENT ON COLUMN firmware_package.file_url IS 'OBS 下载 URL';
COMMENT ON COLUMN firmware_package.md5 IS '文件 MD5 校验值';
COMMENT ON COLUMN firmware_package.description IS '版本更新说明';
COMMENT ON COLUMN firmware_package.status IS '状态: 0=待发布 1=已发布 2=已废弃';
COMMENT ON COLUMN firmware_package.ota_product_id IS '华为云 IoTDA 产品 ID';
COMMENT ON COLUMN firmware_package.ota_package_id IS '华为云 IoTDA 升级包 ID';
COMMENT ON COLUMN firmware_package.created_at IS '创建时间';
COMMENT ON COLUMN firmware_package.updated_at IS '更新时间';
