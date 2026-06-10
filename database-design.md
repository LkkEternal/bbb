# 脐橙病虫害监测系统 - 数据库设计文档

## 1. 数据库环境信息

### 1.1 服务器配置

| 项目 | 配置 |
|------|------|
| 服务器IP | 10.168.89.200 |
| 操作系统 | CentOS 7 (AltArch) |
| 架构 | aarch64 |
| 内存 | 39GB |
| CPU | 20核 |
| 磁盘 | 45GB (可用44GB) |

### 1.2 数据库服务

| 服务 | 版本 | 端口 | 用途 |
|------|------|------|------|
| PostgreSQL | 16.14 | 5432 | 主数据库 (支持向量检索) |
| Redis | 7.4.9 | 6379 | 缓存数据库 |

---

## 2. 数据库连接信息

### 2.1 PostgreSQL 连接

```
主机: 10.168.89.200
端口: 5432
用户: root
密码: Zjhw@123
默认数据库: postgres
```

**JDBC连接字符串:**
```
jdbc:postgresql://10.168.89.200:5432/postgres
```

**命令行连接:**
```bash
docker exec -it postgres psql -U root -d postgres
```

### 2.2 Redis 连接

```
主机: 10.168.89.200
端口: 6379
密码: Zjhw@123
```

**连接字符串:**
```
redis://:Zjhw@123@10.168.89.200:6379
```

---

## 3. 数据库架构设计

### 3.1 业务数据库

```sql
-- 创建业务数据库
CREATE DATABASE citrus_monitoring 
    WITH ENCODING='UTF8' 
    LC_COLLATE='C' 
    LC_CTYPE='C' 
    TEMPLATE=template0;
```

### 3.2 核心表设计

#### 用户表 (users)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 病虫害类型表 (disease_types)

```sql
CREATE TABLE disease_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),  -- 'disease' 或 'pest'
    description TEXT,
    severity_level INTEGER DEFAULT 1,  -- 1-5
    treatment_suggestions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 监测站点表 (monitoring_stations)

```sql
CREATE TABLE monitoring_stations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    address TEXT,
    altitude DECIMAL(8, 2),
    area_hectares DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 图像采集记录表 (image_records)

```sql
CREATE TABLE image_records (
    id BIGSERIAL PRIMARY KEY,
    station_id INTEGER REFERENCES monitoring_stations(id),
    image_url VARCHAR(500) NOT NULL,
    image_path VARCHAR(500),
    capture_time TIMESTAMP NOT NULL,
    device_info JSONB,
    weather_info JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 病虫害检测结果表 (detection_results)

```sql
CREATE TABLE detection_results (
    id BIGSERIAL PRIMARY KEY,
    image_id BIGINT REFERENCES image_records(id),
    disease_type_id INTEGER REFERENCES disease_types(id),
    confidence DECIMAL(5, 4),  -- 置信度 0-1
    bbox_x INTEGER,
    bbox_y INTEGER,
    bbox_width INTEGER,
    bbox_height INTEGER,
    severity VARCHAR(20),  -- 'mild', 'moderate', 'severe'
    is_confirmed BOOLEAN DEFAULT FALSE,
    confirmed_by BIGINT REFERENCES users(id),
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 向量特征表 (image_features) - 向量数据库核心表

```sql
-- 启用向量扩展 (pgvector)
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE image_features (
    id BIGSERIAL PRIMARY KEY,
    image_id BIGINT REFERENCES image_records(id),
    feature_vector vector(512),  -- 512维特征向量
    model_name VARCHAR(100),  -- 使用的模型名称
    model_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建向量索引 (用于相似度检索)
CREATE INDEX idx_image_features_vector 
    ON image_features 
    USING ivfflat (feature_vector vector_cosine_ops)
    WITH (lists = 100);
```

#### 预警记录表 (alerts)

```sql
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    station_id INTEGER REFERENCES monitoring_stations(id),
    disease_type_id INTEGER REFERENCES disease_types(id),
    alert_level VARCHAR(20),  -- 'info', 'warning', 'critical'
    alert_message TEXT,
    detection_count INTEGER DEFAULT 1,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_by BIGINT REFERENCES users(id),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. 向量数据库功能

### 4.1 pgvector 扩展

本系统使用 **pgvector 0.8.2** 扩展实现向量数据库功能，支持：

- 向量存储和检索
- 余弦相似度、L2距离、内积计算
- IVFFlat 和 HNSW 索引

### 4.2 向量检索示例

```sql
-- 插入向量数据
INSERT INTO image_features (image_id, feature_vector, model_name, model_version)
VALUES (
    1,
    '[0.1, 0.2, 0.3, ...]'::vector(512),
    'ResNet50',
    '1.0'
);

-- 相似度检索 (余弦相似度)
SELECT 
    if2.image_id,
    ir.image_url,
    1 - (if1.feature_vector <=> if2.feature_vector) AS similarity
FROM image_features if1
CROSS JOIN image_features if2
JOIN image_records ir ON if2.image_id = ir.id
WHERE if1.image_id = 1
  AND if1.id != if2.id
ORDER BY if1.feature_vector <=> if2.feature_vector
LIMIT 10;
```

### 4.3 支持的向量操作

| 操作符 | 说明 | 示例 |
|--------|------|------|
| `<->` | L2距离 | `vector1 <-> vector2` |
| `<#>` | 内积 | `vector1 <#> vector2` |
| `<=>` | 余弦距离 | `vector1 <=> vector2` |

---

## 5. Redis 缓存设计

### 5.1 缓存策略

| Key模式 | 说明 | TTL |
|---------|------|-----|
| `user:{id}` | 用户信息缓存 | 30分钟 |
| `station:{id}:latest` | 站点最新数据 | 5分钟 |
| `detection:count:{date}` | 每日检测统计 | 24小时 |
| `alert:unread` | 未读预警 | 10分钟 |
| `vector:search:lock:{id}` | 向量检索锁 | 30秒 |

### 5.2 示例命令

```bash
# 设置缓存
SET station:1:latest '{"temperature": 25, "humidity": 60}' EX 300

# 获取缓存
GET user:1

# 增加计数器
INCR detection:count:2026-06-10
```

---

## 6. 部署架构

### 6.1 Docker 容器

| 容器名 | 镜像 | 端口映射 | 状态 |
|--------|------|----------|------|
| postgres | pgvector/pgvector:pg16 | 5432:5432 | 运行中 |
| redis | redis:7-alpine | 6379:6379 | 运行中 |

### 6.2 管理命令

```bash
# 查看容器状态
docker ps

# PostgreSQL 管理
docker start postgres
docker stop postgres
docker logs postgres

# Redis 管理
docker start redis
docker stop redis
docker logs redis

# 进入 PostgreSQL 容器
docker exec -it postgres psql -U root -d postgres

# 进入 Redis 容器
docker exec -it redis redis-cli -a Zjhw@123
```

---

## 7. 备份策略

### 7.1 PostgreSQL 备份

```bash
# 全量备份
docker exec postgres pg_dump -U root postgres > backup_$(date +%Y%m%d).sql

# 恢复备份
cat backup.sql | docker exec -i postgres psql -U root -d postgres
```

### 7.2 Redis 备份

```bash
# 触发RDB快照
docker exec redis redis-cli -a Zjhw@123 BGSAVE

# 复制备份文件
docker cp redis:/data/dump.rdb ./redis_backup.rdb
```

---

## 8. 安全配置

### 8.1 网络安全

- 服务器防火墙已关闭
- 数据库端口仅内网可访问
- 建议配置VPN或SSH隧道进行远程访问

### 8.2 数据库安全

```sql
-- 修改密码加密方式
SET password_encryption = 'scram-sha-256';

-- 限制连接来源
ALTER SYSTEM SET listen_addresses = '0.0.0.0';
```

---

## 9. 监控告警

### 9.1 健康检查

```bash
# 检查 PostgreSQL 状态
docker exec postgres pg_isready -U root

# 检查 Redis 状态
docker exec redis redis-cli -a Zjhw@123 PING
```

### 9.2 性能监控

```sql
-- 查看连接数
SELECT count(*) FROM pg_stat_activity;

-- 查看慢查询
SELECT * FROM pg_stat_activity WHERE state = 'active' AND query_start < now() - interval '5 seconds';

-- 查看数据库大小
SELECT pg_size_pretty(pg_database_size('postgres'));
```

---

## 10. 版本信息

| 组件 | 版本 | 更新日期 |
|------|------|----------|
| PostgreSQL | 16.14 | 2026-06-10 |
| pgvector | 0.8.2 | 2026-06-10 |
| Redis | 7.4.9 | 2026-06-10 |
| Docker | 20.10.9 | 2026-06-10 |
| 文档版本 | 2.0 | 2026-06-10 |

---

*文档更新时间: 2026-06-10*
*更新内容: 数据库从 openGauss 6.0.5 迁移至 PostgreSQL 16.14 + pgvector 0.8.2*
