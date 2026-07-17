# 🚀 阿里云部署配置文档

> **项目**：脐橙病虫害监测与数据收集系统  
> **环境**：阿里云 ECS（CentOS 7+ / Alibaba Cloud Linux）  
> **日期**：2026-07-15  
> **架构**：x86_64

---

## 一、部署架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                    Nginx (反向代理)                          │
│               http://阿里云公网IP:80                         │
├───────────────────┬───────────────────┬─────────────────────┤
│  Vue 3 前端        │  Spring Boot 后端  │  Python RAG 服务    │
│  端口 80           │  端口 8080         │  端口 18001         │
│  Docker            │  Docker            │  Docker            │
│  navel-orange-     │  navel-orange-    │  navel-orange-rag  │
│  nginx             │  server           │                    │
├───────────────────┴───────────────────┴─────────────────────┤
│               PostgreSQL + pgvector + PostGIS                │
│               端口 5432                                      │
├─────────────────────────────────────────────────────────────┤
│                    Redis                                     │
│               端口 6379                                      │
└─────────────────────────────────────────────────────────────┘
```

### 容器网络说明

所有容器通过 Docker Bridge 网络通信：
- PostgreSQL 容器 IP：`172.17.0.2`
- 后端容器 IP：`172.17.0.3`
- 宿主机 Gateway：`172.17.0.1`

> **为什么不用 `localhost`？**  
> 容器内的 `localhost` 指向容器自身，不是宿主机。因此容器间通信需要用 Docker 网关 IP 或自定义网络。

---

## 二、各服务配置详解

### 1. PostgreSQL 数据库

```bash
docker run -d --name postgres \
  -e POSTGRES_USER=root \           # 数据库用户名
  -e POSTGRES_PASSWORD=Zjhw@123 \   # 数据库密码
  -p 5432:5432 \                     # 端口映射（宿主机:容器）
  --restart unless-stopped \         # 容器退出自动重启
  postgis/postgis:16-3.4             # 含 PostGIS 扩展的 PG 16
```

#### 连接信息

| 项目 | 值 |
|------|-----|
| 连接地址 | `172.17.0.2:5432` 或宿主机内网 IP |
| 数据库名 | `postgres` |
| 用户名 | `root` |
| 密码 | `Zjhw@123` |

#### 已安装扩展
- `postgis` — 地理空间数据支持
- `postgis_topology` — 拓扑支持
- `vector` — pgvector 向量检索（用于 RAG 知识库）

---

### 2. Spring Boot 后端

#### Dockerfile

```dockerfile
FROM amazoncorretto:17-alpine       # JDK 17（ARM64/x86_64 都支持）
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime   # 设置东八区
WORKDIR /app
COPY target/*.jar app.jar           # 将编译好的 jar 复制进去
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
```

#### 启动命令

```bash
docker run -d --name navel-orange-server \
  -p 8080:8080 \                            # 宿主机 8080 → 容器 8080
  -v /root/navel-orange-config:/app/config/ \  # 挂载外部配置（优先级高）
  --restart unless-stopped \
  navel-orange-server
```

#### 配置详解

**`/root/navel-orange-config/application-dev.yml`**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://172.17.0.2:5432/postgres   # 数据库连接
    username: root
    password: Zjhw@123
  flyway:
    enabled: false                     # 禁用 Flyway（表已手动创建）
  data:
    redis:
      host: 172.17.0.2                 # Redis 地址
      port: 6379
      password: Zjhw@123

rag:
  base-url: http://localhost:18001      # RAG 服务地址
  rag-dir: /root/navel-orange-rag      # RAG 项目路径

jwt:
  secret: citrus-navel-orange-monitoring-jwt-secret-key-2026
  access-token-expire-hours: 2
```

---

### 3. Nginx 前端

#### nginx.conf

```nginx
server {
    listen 80;
    server_name localhost;
    
    location / {
        root /usr/share/nginx/html;    # 前端静态文件目录
        index index.html;
        try_files $uri $uri/ /index.html;  # SPA 路由支持
    }

    location /api/ {                    # API 反向代理
        proxy_pass http://172.17.0.3:8080;  # 代理到后端容器
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### 启动命令

```bash
docker run -d --name navel-orange-nginx \
  -p 80:80 \                            # 宿主机 80 → 容器 80
  --restart unless-stopped \
  navel-orange-nginx
```

---

### 4. RAG 服务

#### Dockerfile

```dockerfile
FROM python:3.11-slim                   # Python 3.11

# 安装系统依赖（PDF 解析需要）
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libgl1 libglib2.0-0

# 安装 Python 依赖
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
EXPOSE 8001

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
```

#### 启动命令

```bash
docker run -d --name navel-orange-rag \
  -v /root/navel-orange-rag:/app -w /app \   # 挂载代码目录
  -p 18001:18001 \                            # ⚠️ 注意：Dockerfile 是 8001，映射到宿主机 18001
  --env-file /root/navel-orange-rag/.env \    # 加载环境变量
  --restart unless-stopped \
  navel-orange-rag
```

#### .env 配置

```bash
HOST=0.0.0.0
PORT=18001                              # 实际运行端口
HF_ENDPOINT=https://hf-mirror.com       # HuggingFace 国内镜像
EMBEDDING_MODEL_NAME=BAAI/bge-small-zh-v1.5
EMBEDDING_DEVICE=auto
DEEPSEEK_API_KEY=sk-your-deepseek-api-key  # 需要替换
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1
DEEPSEEK_MODEL=deepseek-chat
```

> **注意**：Dockerfile 内是 `8001`，`.env` 是 `18001`，启动时 `-p 18001:18001` 映射正确即可。

---

## 三、安全组配置

登录阿里云控制台 → **网络与安全** → **安全组**，添加入方向规则：

| 端口 | 用途 | 建议 |
|------|------|------|
| 22 | SSH（FinalShell） | ✅ 默认已开 |
| 80 | 前端 Nginx | ✅ 对外开放 |
| 8080 | 后端 API（Swagger） | 🔒 仅对内或白名单 |
| 5432 | PostgreSQL | ⛔ **禁止对外开放** |
| 6379 | Redis | ⛔ **禁止对外开放** |
| 18001 | RAG 服务 | 🔒 仅对内 |

---

## 四、常用运维命令

### 查看服务状态

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### 查看日志

```bash
docker logs navel-orange-server --tail 50    # 后端日志
docker logs navel-orange-nginx --tail 30     # 前端日志
docker logs navel-orange-rag --tail 30       # RAG 日志
docker logs postgres --tail 30               # 数据库日志
```

### 重启服务

```bash
docker restart navel-orange-server           # 重启后端
docker restart navel-orange-nginx            # 重启前端
docker restart navel-orange-rag              # 重启 RAG
```

### 重新构建后端

```bash
cd /root && mvn clean package -DskipTests
cd /root/navel-orange-server
docker build -t navel-orange-server .
docker stop navel-orange-server && docker rm navel-orange-server
docker run -d --name navel-orange-server \
  -p 8080:8080 \
  -v /root/navel-orange-config:/app/config/ \
  --restart unless-stopped \
  navel-orange-server
```

### 重新构建前端

```bash
cd /root/navel-orange-admin
docker run --rm -v /root/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install && pnpm build"
docker build -t navel-orange-nginx .
docker stop navel-orange-nginx && docker rm navel-orange-nginx
docker run -d --name navel-orange-nginx -p 80:80 --restart unless-stopped navel-orange-nginx
```

---

## 五、修改配置

### 修改数据库连接

```bash
vi /root/navel-orange-config/application-dev.yml
# 修改 url、username、password 等
docker restart navel-orange-server
```

### 修改 Redis 连接

```bash
vi /root/navel-orange-config/application-dev.yml
# 修改 host、port、password
docker restart navel-orange-server
```

### 修改 RAG 配置

```bash
vi /root/navel-orange-rag/.env
# 修改 DEEPSEEK_API_KEY、模型等
docker restart navel-orange-rag
```

### 修改 JWT 密钥

```bash
vi /root/navel-orange-config/application-dev.yml
# 修改 jwt.secret
docker restart navel-orange-server
```

### 重新导入数据库

```bash
# 导入建表 SQL
docker exec -i postgres psql -U root -d postgres < /root/create_tables.sql
# 导入测试数据
docker exec -i postgres psql -U root -d postgres < /root/insert_test_data.sql
```

---

## 六、更换阿里云 IP 后的操作

### 背景说明

使用了**节省停机模式**停止实例后，再次启动时：
- ✅ **私网 IP 不变**（如 `172.22.102.90`）→ Docker 容器无需任何改动
- ⚠️ **公网 IP 可能变化** → 你需要用浏览器访问新的公网 IP

### 重点：需要重新部署吗？

**完全不需要重新部署！** 所有 Docker 容器、数据库、代码都不需要重新部署。

只需以下简单步骤：

#### 1️⃣ 启动实例和容器

```bash
# 阿里云控制台 → 启动实例

# SSH 连接后，启动所有容器
docker start postgres navel-orange-server navel-orange-nginx navel-orange-rag

# 等待 10 秒确认所有服务正常
sleep 10
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

#### 2️⃣ 查看新 IP

```bash
# 查看私网 IP（通常不会变）
hostname -I
# 输出示例：172.22.102.90 172.17.0.1

# 查看公网 IP（可能变化）
curl -s ifconfig.me
```

#### 3️⃣ 判断是否需要修改配置

| 情况 | 需要修改？ | 操作 |
|------|-----------|------|
| 私网 IP 没变（最常见） | ❌ 什么都不用改 | 直接用新公网 IP 访问 |
| 私网 IP 变了 | ⚠️ 只需改后端配置 | 见下方步骤 |

#### 4️⃣ 如果私网 IP 变了（仅需 2 步）

```bash
# 第 1 步：修改后端配置中的旧私网 IP 为新私网 IP
sed -i 's|旧私网IP|新私网IP|g' /root/navel-orange-config/application-dev.yml

# 确认修改
grep "新私网IP" /root/navel-orange-config/application-dev.yml

# 第 2 步：重启后端容器
docker restart navel-orange-server

# 验证
sleep 10
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui/index.html
# 返回 200 表示正常
```

#### 5️⃣ 验证所有服务

```bash
# 测试后端
curl -s -o /dev/null -w "后端: %{http_code}\n" http://localhost:8080/swagger-ui/index.html

# 测试前端
curl -s -o /dev/null -w "前端: %{http_code}\n" http://localhost:80

# 测试 RAG
curl -s http://localhost:18001/health

# 查看整体状态
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

#### 6️⃣ 用新 IP 访问

在浏览器打开：**http://新的阿里云公网IP**

---

### 完整示例：更换 IP 后的操作流程

```bash
# 1. 阿里云控制台启动实例 → SSH 连接

# 2. 启动容器
docker start postgres navel-orange-server navel-orange-nginx navel-orange-rag

# 3. 查看新私网 IP
hostname -I
# 假设输出：172.22.102.95 172.17.0.1（私网 IP 变了）

# 4. 更新配置（只改私网 IP 变化的部分）
sed -i 's|172.22.102.90|172.22.102.95|g' /root/navel-orange-config/application-dev.yml

# 5. 重启后端
docker restart navel-orange-server

# 6. 等待 10 秒验证
sleep 10 && curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui/index.html

# 7. 用新公网 IP 访问浏览器
# http://新的公网IP
```

---

### 每日使用流程

**启动（用的时候）：**
```bash
# 1. 阿里云控制台 → 启动实例
# 2. SSH 连接
# 3. 启动 Docker 容器
docker start postgres navel-orange-server navel-orange-nginx navel-orange-rag
```

**关闭（不用的时候）：**
```bash
# 1. 先停止所有容器（保护数据）
docker stop navel-orange-rag navel-orange-nginx navel-orange-server postgres

# 2. 阿里云控制台 → 停止实例 → 选「节省停机模式」
```

### 无需修改的内容

| 配置 | 原因 |
|------|------|
| 数据库连接 `172.17.0.2` | Docker 内部 IP，公网/私网 IP 变化都不影响 |
| 数据库密码 | 不变 |
| JWT 密钥 | 不变 |
| Docker 容器 | 不需要重新创建 |
| 项目代码 | 不需要重新编译 |
| Nginx 配置 | 已配为内部 IP，不受影响 |
| 安全组规则 | 绑定的是实例不是 IP，自动生效 |

---

## 七、常见问题排查

### 后端返回 000

```bash
docker logs navel-orange-server --tail 30
```

常见原因：
1. 数据库连接超时 → 检查 `application-dev.yml` 中的 IP
2. Flyway 迁移失败 → 配置 `flyway.enabled: false`
3. 端口映射错误 → 检查 `docker ps` 端口是否正常

### 前端页面白屏

```bash
# 检查构建产物是否存在
ls /root/navel-orange-admin/dist/

# 检查 Nginx 日志
docker logs navel-orange-nginx --tail 30
```

### RAG 启动慢

首次启动需要下载 Embedding 模型（约 400MB），已配置 `HF_ENDPOINT=https://hf-mirror.com` 国内镜像加速。

---

## 八、端口一览

| 端口 | 服务 | 协议 | 说明 |
|------|------|------|------|
| 80 | Nginx 前端 | HTTP | 用户访问 |
| 8080 | Spring Boot 后端 | HTTP | API 服务 |
| 18001 | Python RAG 服务 | HTTP | AI 知识库 |
| 5432 | PostgreSQL | TCP | 数据库 |
| 6379 | Redis | TCP | 缓存 |
