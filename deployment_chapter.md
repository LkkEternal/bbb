## 2 系统部署

本章介绍脐橙病虫害监测系统的服务器部署方法，将从安装要求和部署步骤两个方面进行详细介绍。

### 2.1 安装要求

本服务器安装所需最基本的配置。

#### 2.1.1 硬件配置

系统部署所需的最低硬件资源配置要求如下：

| 资源项 | 最低配置 | 推荐配置 | 说明 |
|--------|---------|---------|------|
| CPU | 4 核 | 8 核及以上 | 后端服务与 RAG 服务并行运行 |
| 内存 | 16 GB | 32 GB 及以上 | RAG 服务的 Embedding 模型加载需约 4 GB |
| 硬盘 | 100 GB | 500 GB 及以上 | 存储系统日志、数据库数据、知识库文档及 OTA 固件 |
| 网络 | 100 Mbps | 1000 Mbps | 与华为云 IoTDA、OBS 及第三方 API 通信 |
| GPU（可选） | — | NVIDIA GPU ≥ 8 GB 显存 | 用于 RAG 服务的本地 LLM 推理加速（如 Qwen3-32B） |

> **注：** 若使用 DeepSeek API 等云端 LLM 服务，可无需 GPU。若部署本地 Qwen3-32B 模型（昇腾 910B NPU），需额外配备 NPU 环境和相应驱动。

#### 2.1.2 系统软件配置

系统运行所需的环境依赖如下：

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| 操作系统 | Windows Server 2019+ / Ubuntu 22.04+ / CentOS 7+ | 服务器运行环境 |
| JDK | 17+ | 运行 Spring Boot 后端服务 |
| Maven | 3.9+ | 构建后端 Java 项目 |
| PostgreSQL | 14+ | 关系型数据库（需启用 pgvector 插件） |
| pgvector | 0.6+ | 向量数据库扩展，用于 RAG 知识库语义检索 |
| Redis | 6.2+ | 缓存、JWT Token 黑名单等 |
| Node.js | 20+ | 构建和运行前端项目 |
| pnpm | 9+ | 前端包管理器 |
| Python | 3.11+ | 运行 RAG 智能问答服务 |
| uv | 0.4+ | Python 包管理器（可选，替代 pip） |
| Nginx（可选） | 1.24+ | 反向代理前端静态资源和后端 API |

**数据库依赖：**

- **PostgreSQL + pgvector**：用于存储业务数据及知识库向量化数据。在 Ubuntu 下安装 pgvector 的参考命令如下：

```bash
# 安装 PostgreSQL
sudo apt install postgresql-16 postgresql-16-pgvector
systemctl start postgresql

# 创建数据库并启用 pgvector
sudo -u postgres psql -c "CREATE DATABASE navel_orange;"
sudo -u postgres psql -d navel_orange -c "CREATE EXTENSION vector;"
```

- **Redis**：用于缓存和会话管理。

```bash
# Ubuntu 安装 Redis
sudo apt install redis-server
systemctl start redis
```

**Node.js 环境：**

```bash
# 安装 Node.js 20+（推荐使用 nvm）
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
nvm install 20

# 安装 pnpm
npm install -g pnpm
```

**Python 环境：**

```bash
# 安装 Python 3.11+ 和 uv
sudo apt install python3.11 python3.11-venv
pip install uv
```

### 2.2 部署步骤

#### 2.2.1 服务器部署

系统整体架构由四个核心服务组件构成，部署流程按依赖顺序进行。

```
┌─────────────────────────────────────────────────────┐
│                    Nginx (反向代理)                    │
│                http://server-ip:80                    │
├──────────────────┬──────────────────┬─────────────────┤
│  Vue 3 前端       │  Spring Boot 后端 │ Python RAG 服务  │
│  端口 3000        │  端口 8080       │ 端口 18001       │
│  (构建后静态文件)  │  navel-orange-   │ navel-orange-rag │
│  navel-orange-    │  server + common │ FastAPI +       │
│  admin/dist       │                  │ sentence-        │
│                   │                  │ transformers     │
├──────────────────┴──────────────────┴─────────────────┤
│               PostgreSQL + pgvector                     │
│               10.168.89.200:5432                        │
├───────────────────────────────────────────────────────┤
│                    Redis                                │
│               10.168.89.200:6379                        │
└───────────────────────────────────────────────────────┘
```

> **⚠️ 安全注意事项：**
> - 生产环境部署前务必将 `application-dev.yml` 中的数据库密码、API Key、Secret Key 等敏感信息替换为生产环境凭据，建议使用环境变量或密钥管理服务。
> - 关闭 Swagger/Knife4j 文档暴露（设置 `knife4j.enable: false`）。
> - 配置防火墙，仅开放必要的端口（如 80/443、8080、5432 等）。
> - 定期更新依赖版本以修复已知安全漏洞。

##### 步骤一：搭建基础环境

按 2.1.2 节要求安装 PostgreSQL（含 pgvector）、Redis、JDK 17、Node.js 20+、Python 3.11+ 及 Maven。

##### 步骤二：初始化数据库

执行项目根目录下的建表脚本初始化数据库：

```bash
# 导入核心表结构
psql -h 10.168.89.200 -U root -d postgres -f create_tables.sql

# 导入测试数据（可选）
psql -h 10.168.89.200 -U root -d postgres -f insert_test_data.sql
```

系统会自动通过 Flyway 或 `DatabaseInitializer` 完成剩余表结构的迁移。

##### 步骤三：构建并启动 Spring Boot 后端

```bash
# 进入项目根目录
cd navel-orange-monitoring

# 构建项目（跳过测试）
mvn clean package -DskipTests

# 启动后端服务
java -jar navel-orange-server/target/navel-orange-server-1.0.0-SNAPSHOT.jar \
    --spring.profiles.active=dev
```

后端服务默认监听 `http://localhost:8080`，可通过 `application.yml` 中的 `server.port` 修改端口。

启动后可通过以下地址验证：

- API 文档：`http://localhost:8080/swagger-ui.html`
- 健康检查：`http://localhost:8080/actuator/health`（若启用 Actuator）

**常见问题：**

| 问题 | 原因 | 解决方法 |
|------|------|---------|
| 数据库连接失败 | PostgreSQL 未启动或连接信息错误 | 检查 `application-dev.yml` 中数据库地址、端口、用户名、密码 |
| Redis 连接失败 | Redis 服务未启动 | 启动 Redis 服务或检查连接配置 |
| Maven 构建失败 | 依赖下载超时 | 检查网络，或使用华为云 Maven 镜像（已在 pom.xml 中配置） |
| 端口被占用 | 8080 端口已被其他程序使用 | 修改 `server.port` 或关闭占用程序 |

##### 步骤四：构建并启动 Vue 3 前端

```bash
# 进入前端目录
cd navel-orange-admin

# 安装依赖
pnpm install

# 开发模式启动（热更新）
pnpm dev
# 访问 http://localhost:3000

# 生产构建
pnpm build
# 构建产物位于 navel-orange-admin/dist/
```

开发模式下 Vite 会自动代理 `/api` 请求到 `http://localhost:8080`（配置见 `vite.config.ts`）。

生产环境建议将构建后的 `dist/` 目录部署到 Nginx，并配置 API 反向代理。

**Nginx 配置示例：**

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    root /path/to/navel-orange-admin/dist;
    index index.html;

    # 单页应用路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

##### 步骤五：启动 Python RAG 服务

```bash
# 进入 RAG 服务目录
cd navel-orange-rag

# 安装依赖（使用 uv，推荐）
uv sync

# 启动 RAG 服务
uv run uvicorn app.main:app --host 127.0.0.1 --port 18001 --no-reload

# 若使用 pip，则：
# pip install -r requirements.txt
# python -m uvicorn app.main:app --host 127.0.0.1 --port 18001 --no-reload
```

RAG 服务监听 `http://localhost:18001`，后端 Spring Boot 通过配置项 `rag.base-url` 与之通信。

**RAG 服务环境配置（`navel-orange-rag/.env`）：**

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `HOST` | 监听地址 | `0.0.0.0` |
| `PORT` | 监听端口 | `18001` |
| `EMBEDDING_MODEL_NAME` | Embedding 模型 | `BAAI/bge-small-zh-v1.5` |
| `EMBEDDING_DEVICE` | Embedding 运行设备 | `auto`（自动选择 CPU/GPU） |
| `LLM_DEFAULT_MODEL` | 默认 LLM | `deepseek`（可选 `qwen-local`） |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 | 首次使用需填写 |
| `QWEN_LOCAL_BASE_URL` | 本地 Qwen3-32B 地址 | `https://10.168.89.186:5443/...` |

> **注意：** RAG 服务首次启动时需下载 Embedding 模型（约 400 MB），请确保网络畅通。

##### 步骤六：一键启动（开发环境）

项目提供了自动化启动脚本，可依次启动 Python RAG 服务和 Spring Boot 后端：

**Windows：**

```batch
start-dev.bat
```

**Linux / macOS：**

```bash
chmod +x start-dev.sh
./start-dev.sh
```

脚本会自动检测 RAG 服务是否已在运行，若未运行则自动启动并等待就绪，然后再启动 Spring Boot 后端。前端需在另一终端中手动启动：

```bash
cd navel-orange-admin
pnpm dev
```

##### 步骤七：验证部署

按以下顺序验证系统各组件是否正常运行：

1. **数据库验证：** `psql -h <host> -U <user> -d postgres -c "\dt"` 确认所有数据表已创建
2. **Redis 验证：** `redis-cli -h <host> ping` 应返回 `PONG`
3. **后端验证：** 访问 `http://localhost:8080/swagger-ui.html` 确认 API 文档可打开
4. **RAG 服务验证：** `curl http://localhost:18001/health` 应返回 `{"status":"ok"}`
5. **前端验证：** 访问 `http://localhost:3000` 确认登录页面正常显示，可正常登录

#### 2.2.2 配套软件的部署

##### Docker 部署（RAG 服务）

RAG 服务支持 Docker 容器化部署，适用于生产环境和需要隔离依赖的场景：

```bash
cd navel-orange-rag

# 构建并启动
docker-compose up -d

# 查看运行状态
docker ps | grep navel-orange-rag

# 查看日志
docker logs -f navel-orange-rag
```

Docker 部署方式会将 RAG 服务容器化，通过环境变量文件（`.env`）传递配置，确保宿主机与容器环境隔离。如需 GPU 加速，取消 `docker-compose.yml` 中 GPU 相关注释并安装 `nvidia-container-toolkit`。

##### OTA 固件升级服务

系统支持通过华为云 OBS 对象存储服务管理 IoT 设备固件，实现设备 OTA 升级功能：

- 固件包上传至华为云 OBS 桶（`orange-86ee`）
- 后端通过 OTA 管理页面下发升级指令
- IoT 设备通过 `sendMessage` 方式接收 `{"cmd":"ota_update","url":"..."}` 格式的升级命令
- 固件文件大小限制：50 MB（可在 `application.yml` 中调整）

**OTA 升级部署要点：**

1. 确保已正确配置华为云 OBS 的 `access-key`、`secret-key` 和 `bucket-name`
2. 设备端需实现 `ota_update` 命令的解析和下载逻辑
3. 建议在生产环境中配置 OBS 的 CDN 加速以提升固件下载速度

##### 微信小程序部署

微信小程序代码位于项目目录下的 `navel-orange-app/` 中（经合并后移入），部署步骤如下：

1. 使用微信开发者工具打开 `navel-orange-app/` 目录
2. 修改 `app.js` 或对应配置文件中的后端 API 地址
3. 在微信公众平台中配置服务器域名白名单，添加后端服务域名
4. 提交审核并发布

##### 外部服务依赖

系统正常运行依赖以下第三方服务，部署前需确保各服务可用：

| 服务 | 用途 | 配置位置 | 是否必需 |
|------|------|---------|---------|
| 华为云 IoTDA | IoT 设备接入与管理 | `application-dev.yml` → `huawei.iotda` | 是 |
| 华为云 OBS | 文件存储（图片、固件等） | `application-dev.yml` → `huawei.obs` | 是 |
| 高德地图 API | 果园地图展示与定位 | `application-dev.yml` → `map.amap` | 是 |
| 和风天气 API | 天气数据获取 | `application-dev.yml` → `weather.qweather` | 否 |
| DeepSeek API / 本地 Qwen3-32B | RAG 问答 LLM 推理 | `navel-orange-rag/.env` | 是 |
| 网易邮箱 SMTP | 预警邮件推送 | `application-dev.yml` → `spring.mail` | 否 |
| 阿里云短信 | 短信验证码发送 | `application-dev.yml` → `aliyun.sms` | 否 |

> **注意：** 各第三方服务的 API Key 和 Secret 请妥善保管，建议生产环境中通过环境变量注入而非硬编码在配置文件中。例如使用 `SPRING_DATASOURCE_PASSWORD` 环境变量替代明文密码。
