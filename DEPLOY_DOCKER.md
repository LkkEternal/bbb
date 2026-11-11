# 脐橙病虫害监测系统 - Docker部署指南

## 1. 环境准备

### 1.1 服务器要求
- Linux服务器（推荐Ubuntu 22.04）
- 内存 >= 4GB（推荐8GB）
- 磁盘 >= 50GB
- Docker >= 24.0
- Docker Compose >= v2.20

### 1.2 安装Docker
```bash
# Ubuntu
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## 2. 上传文件到服务器

将整个项目上传到服务器，例如：`/home/ubuntu/orange-monitoring/`

```bash
# 使用scp或rsync
scp -r ./* ubuntu@你的服务器IP:/home/ubuntu/orange-monitoring/
```

## 3. 配置环境变量

进入项目目录，修改 `.env` 文件中的配置：

```bash
cd /home/ubuntu/orange-monitoring
cp .env .env.bak
vim .env
```

**重要配置项：**
- `POSTGRES_PASSWORD` - 数据库密码
- `REDIS_PASSWORD` - Redis密码
- `JWT_SECRET` - JWT密钥（建议换成随机字符串）
- 其他第三方API密钥（如需修改）

## 4. 数据库初始化

数据库表和测试数据已经在本地准备好，位于 `orange.sql`。

### 方式一：使用PostgreSQL容器初始化（推荐）
在 `deploy/init-db/` 目录下放置SQL文件，PostgreSQL容器首次启动时会自动执行。

```bash
cp orange.sql deploy/init-db/01-init.sql
```

### 方式二：启动后手动导入
```bash
# 启动postgres
docker compose up -d postgres
# 等待就绪
sleep 10
# 导入数据
docker exec -i navel-orange-postgres psql -U postgres -d navel_orange < orange.sql
```

**注意：** orange.sql 已包含pgvector相关类型和函数，且你已在服务器上准备好pgvector扩展，使用 `ankane/pgvector` 镜像即可。

## 5. 构建并启动

```bash
# 构建并启动所有服务（首次启动会较慢，需要下载镜像和构建）
docker compose up -d --build

# 查看日志
docker compose logs -f

# 查看容器状态
docker compose ps
```

## 6. 验证部署

### 6.1 检查容器状态
所有容器应该都是 `Up (healthy)` 状态：
```bash
docker compose ps
```

### 6.2 访问系统
- 前端管理后台：`http://你的服务器IP/`
- 后端API：`http://你的服务器IP/api/`
- Swagger文档：`http://你的服务器IP/swagger-ui.html`
- RAG服务：`http://你的服务器IP:8001/docs`（如需直接访问）

### 6.3 测试登录
使用orange.sql中的测试账号：
- 用户名：`测试果农` 密码：`123456`（BCrypt加密的默认密码）
- 用户名：`果园管理员` 密码：`123456`

## 7. 常用命令

```bash
# 停止所有服务
docker compose down

# 停止并删除数据卷（慎用，会清空数据库数据）
docker compose down -v

# 重启服务
docker compose restart

# 查看某个服务日志
docker compose logs -f backend

# 进入容器
docker exec -it navel-orange-backend bash
docker exec -it navel-orange-postgres psql -U postgres -d navel_orange

# 更新代码后重新部署
docker compose up -d --build
```

## 8. 注意事项

1. **端口冲突**：如果80端口被占用，修改 `.env` 中的 `NGINX_HTTP_PORT`
2. **防火墙**：确保开放80、8080端口（或只开放80）
3. **数据持久化**：postgres_data、redis_data等卷会持久化数据，不要随意删除
4. **安全**：生产环境建议配置HTTPS、修改所有默认密码、限制SSH访问
5. **RAG服务**：如果不需要知识库功能，可以在docker-compose.yml中注释rag服务
6. **日志清理**：定期清理Docker日志，避免磁盘占满
