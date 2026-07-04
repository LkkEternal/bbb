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

## 3. 数据库总览

本系统共设计 **9张表**，按功能域划分如下：

| 功能域 | 表名 | 说明 |
|--------|------|------|
| 用户权限 | `sys_user` | 用户表 |
| 用户权限 | `sys_role` | 角色表 |
| 用户权限 | `sys_user_role` | 用户角色关联表 |
| 用户权限 | `sys_refresh_token` | Refresh Token备份表 |
| 用户权限 | `sys_operation_log` | 操作日志表 |
| 果园管理 | `orchard` | 果园表 |
| 果园管理 | `orchard_user` | 果园用户关联表 |
| 数据采集 | `environment_data` | 环境数据表 |
| 数据采集 | `weather_forecast` | 天气预报表 |

---

## 4. ER关系图

```
sys_user 1──N orchard_user ──N orchard     (多对多：用户-果园)
sys_user N──N sys_role                      (通过 sys_user_role 关联)
sys_user 1──N sys_refresh_token             (一个用户多个Refresh Token)
orchard 1──N environment_data               (一个果园多条环境数据)
orchard 1──N weather_forecast               (一个果园多条天气预报)
```

---

## 5. 表结构详细设计

### 5.1 用户表 `sys_user`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | BIGSERIAL | PK | 自增 | 主键 |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL | - | 用户名 |
| `password` | VARCHAR(255) | NOT NULL | - | 密码（BCrypt加密） |
| `phone` | VARCHAR(20) | UNIQUE | NULL | 手机号 |
| `wechat_openid` | VARCHAR(64) | UNIQUE | NULL | 微信小程序openid |
| `avatar_url` | VARCHAR(500) | - | NULL | 头像URL地址 |
| `status` | SMALLINT | NOT NULL | 1 | 状态：0=禁用 1=启用 |
| `email_enabled` | BOOLEAN | NOT NULL | FALSE | 是否接收邮件推送 |
| `push_enabled` | BOOLEAN | NOT NULL | TRUE | 是否接收小程序推送 |
| `last_login_at` | TIMESTAMP | - | NULL | 最后登录时间 |
| `last_login_ip` | VARCHAR(45) | - | NULL | 最后登录IP |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | BOOLEAN | NOT NULL | FALSE | 逻辑删除标记 |

**索引：**
- `idx_user_phone` ON `phone`
- `idx_user_wechat` ON `wechat_openid`
- `idx_user_status` ON `status`

---

### 5.2 角色表 `sys_role`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | SERIAL | PK | 自增 | 主键 |
| `code` | VARCHAR(30) | UNIQUE, NOT NULL | - | 角色编码 |
| `name` | VARCHAR(50) | NOT NULL | - | 角色名称 |
| `description` | VARCHAR(200) | - | '' | 角色描述 |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

**初始数据：**
```sql
INSERT INTO sys_role (code, name, description) VALUES
('ROLE_FARMER', '农户', '查看自己的果园数据、历史预警、知识库检索、提交反馈'),
('ROLE_ADMIN', '管理员', '管理用户、导入文献、查看所有果园、调整规则、审核反馈、查看审计日志');
```

---

### 5.3 用户角色关联表 `sys_user_role`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `user_id` | BIGINT | PK, FK→sys_user.id | - | 用户ID |
| `role_id` | INT | PK, FK→sys_role.id | - | 角色ID |

**主键：** 联合主键 `(user_id, role_id)`

**说明：** 用户只能拥有一个角色，不支持多角色。

---

### 5.4 Refresh Token备份表 `sys_refresh_token`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | BIGSERIAL | PK | 自增 | 主键 |
| `token_id` | VARCHAR(64) | UNIQUE, NOT NULL | - | 刷新令牌标识ID |
| `user_id` | BIGINT | FK→sys_user.id, NOT NULL | - | 用户ID |
| `token_hash` | VARCHAR(128) | NOT NULL | - | RefreshToken的SHA256哈希 |
| `device_info` | VARCHAR(200) | - | NULL | 登录设备信息 |
| `expires_at` | TIMESTAMP | NOT NULL | - | 过期时间 |
| `revoked` | BOOLEAN | NOT NULL | FALSE | 是否主动吊销 |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- `idx_rt_user` ON `user_id`
- `idx_rt_token_id` ON `token_id`
- `idx_rt_token` ON `token_hash`
- `idx_rt_expires` ON `expires_at`

---

### 5.5 操作日志表 `sys_operation_log`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | BIGSERIAL | PK | 自增 | 主键 |
| `user_id` | BIGINT | FK→sys_user.id | NULL | 操作用户ID |
| `module` | VARCHAR(30) | - | NULL | 操作模块 |
| `action` | VARCHAR(50) | NOT NULL | - | 操作类型 |
| `resource_type` | VARCHAR(50) | - | NULL | 资源类型 |
| `resource_id` | VARCHAR(50) | - | NULL | 资源ID |
| `description` | VARCHAR(200) | - | NULL | 操作描述 |
| `detail` | TEXT | - | NULL | 操作详情（JSON格式） |
| `ip_address` | VARCHAR(45) | - | NULL | 操作IP |
| `user_agent` | VARCHAR(500) | - | NULL | 客户端标识 |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 操作时间 |

**索引：**
- `idx_log_user` ON `user_id`
- `idx_log_action` ON `action`
- `idx_log_time` ON `created_at`

---

### 5.6 果园表 `orchard`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | BIGSERIAL | PK | 自增 | 主键 |
| `owner_id` | BIGINT | FK→sys_user.id, NOT NULL | - | 园主ID（主要负责人） |
| `name` | VARCHAR(100) | NOT NULL | - | 果园名称 |
| `formatted_address` | VARCHAR(200) | - | '' | 详细地址（高德API地理编码后的完整地址） |
| `location` | GEOGRAPHY(POINT) | - | NULL | 经纬度（高德API地理编码后的位置） |
| `area_mu` | DECIMAL(10,2) | - | NULL | 面积（亩） |
| `tree_age` | INT | - | NULL | 树龄（年） |
| `variety` | VARCHAR(50) | - | '脐橙' | 品种 |
| `status` | SMALLINT | NOT NULL | 1 | 状态：0=停用 1=正常 |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |
| `deleted` | BOOLEAN | NOT NULL | FALSE | 逻辑删除标记 |

**索引：**
- `idx_orchard_owner` ON `owner_id`
- `idx_orchard_location` ON `location` (GIST索引)

---

### 5.7 果园用户关联表 `orchard_user`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `orchard_id` | BIGINT | PK, FK→orchard.id | - | 果园ID |
| `user_id` | BIGINT | PK, FK→sys_user.id | - | 用户ID |
| `role_type` | VARCHAR(20) | NOT NULL | 'viewer' | 角色：owner=园主 / manager=管理员 / viewer=只读 |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

**主键：** 联合主键 `(orchard_id, user_id)`

**索引：**
- `idx_ou_user` ON `user_id`

---

### 5.8 环境数据表 `environment_data`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | BIGSERIAL | PK | 自增 | 主键 |
| `orchard_id` | BIGINT | FK→orchard.id, NOT NULL | - | 果园ID |
| `data_source` | VARCHAR(20) | NOT NULL | 'sensor' | 数据来源：sensor/weather_api |
| `temp` | DECIMAL(5,2) | - | NULL | 温度（℃） |
| `humidity` | DECIMAL(5,2) | - | NULL | 相对湿度（%） |
| `precip` | DECIMAL(8,2) | - | NULL | 降雨量（mm） |
| `windSpeed` | DECIMAL(5,2) | - | NULL | 风速（km/h） |
| `record_time` | TIMESTAMP | NOT NULL | - | 数据记录时间 |
| `extra_data` | JSONB | - | NULL | 扩展数据 |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 入库时间 |

**索引：**
- `idx_env_orchard` ON `orchard_id`
- `idx_env_time` ON `record_time`
- `idx_env_orchard_time` ON `(orchard_id, record_time)`

---

### 5.9 天气预报表 `weather_forecast`

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | BIGSERIAL | PK | 自增 | 主键 |
| `orchard_id` | BIGINT | FK→orchard.id, NOT NULL | - | 果园ID |
| `fxTime` | TIMESTAMP | NOT NULL | - | 预报时间 |
| `temp` | DECIMAL(5,2) | - | NULL | 温度 |
| `humidity` | DECIMAL(5,2) | - | NULL | 湿度 |
| `pop` | DECIMAL(5,2) | - | NULL | 降水概率（%） |
| `precip` | DECIMAL(8,2) | - | NULL | 预计降雨量（mm） |
| `text` | VARCHAR(50) | - | NULL | 天气描述 |
| `windSpeed` | DECIMAL(5,2) | - | NULL | 风速（km/h） |
| `api_source` | VARCHAR(20) | NOT NULL | 'qweather' | API来源 |
| `fetch_time` | TIMESTAMP | NOT NULL | - | 数据获取时间 |
| `created_at` | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 入库时间 |

**唯一约束：** `uk_forecast` ON `(orchard_id, fxTime, api_source)`

**索引：**
- `idx_forecast_orchard` ON `orchard_id`
- `idx_forecast_fxtime` ON `fxTime`

---

## 6. Redis 缓存设计

### 6.1 缓存策略

| Key模式 | 说明 | TTL |
|---------|------|-----|
| `user:{id}` | 用户信息缓存 | 30分钟 |
| `orchard:{id}` | 果园信息缓存 | 30分钟 |
| `detection:count:{date}` | 每日检测统计 | 24小时 |
| `alert:unread:{userId}` | 未读预警 | 10分钟 |

### 6.2 示例命令

```bash
# 设置缓存
SET user:1 '{"username":"果农张三","phone":"13800138000"}' EX 1800

# 获取缓存
GET user:1

# 增加计数器
INCR detection:count:2026-06-10
```

---

## 7. 部署架构

### 7.1 Docker 容器

| 容器名 | 镜像 | 端口映射 | 状态 |
|--------|------|----------|------|
| postgres | pgvector/pgvector:pg16 | 5432:5432 | 运行中 |
| redis | redis:7-alpine | 6379:6379 | 运行中 |

### 7.2 管理命令

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

## 8. 备份策略

### 8.1 PostgreSQL 备份

```bash
# 全量备份
docker exec postgres pg_dump -U root postgres > backup_$(date +%Y%m%d).sql

# 恢复备份
cat backup.sql | docker exec -i postgres psql -U root -d postgres
```

### 8.2 Redis 备份

```bash
# 触发RDB快照
docker exec redis redis-cli -a Zjhw@123 BGSAVE

# 复制备份文件
docker cp redis:/data/dump.rdb ./redis_backup.rdb
```

---

## 9. 安全配置

### 9.1 网络安全

- 服务器防火墙已关闭
- 数据库端口仅内网可访问
- 建议配置VPN或SSH隧道进行远程访问

### 9.2 数据库安全

```sql
-- 修改密码加密方式
SET password_encryption = 'scram-sha-256';

-- 限制连接来源
ALTER SYSTEM SET listen_addresses = '0.0.0.0';
```

---

## 10. 监控告警

### 10.1 健康检查

```bash
# 检查 PostgreSQL 状态
docker exec postgres pg_isready -U root

# 检查 Redis 状态
docker exec redis redis-cli -a Zjhw@123 PING
```

### 10.2 性能监控

```sql
-- 查看连接数
SELECT count(*) FROM pg_stat_activity;

-- 查看慢查询
SELECT * FROM pg_stat_activity WHERE state = 'active' AND query_start < now() - interval '5 seconds';

-- 查看数据库大小
SELECT pg_size_pretty(pg_database_size('postgres'));
```

---

## 11. 版本信息

| 组件 | 版本 | 更新日期 |
|------|------|----------|
| PostgreSQL | 16.14 | 2026-06-11 |
| pgvector | 0.8.2 | 2026-06-11 |
| Redis | 7.4.9 | 2026-06-11 |
| Docker | 20.10.9 | 2026-06-11 |
| 文档版本 | 3.1 | 2026-06-11 |

---

*文档更新时间: 2026-06-11*
*更新内容: 精简为9张核心表*
