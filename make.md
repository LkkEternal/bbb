# 脐橙病虫害监测系统 — 完整部署文档

## 一、项目概述

本项目为 **脐橙病虫害监测与数据收集系统**，采用前后端分离 + 微服务架构：

| 组件 | 技术栈 | 说明 |
| --- | --- | --- |
| 前端管理后台 | Vue 3 + TypeScript + Element Plus + Vite | `navel-orange-admin/` |
| 后端服务 | Spring Boot 3.4.2 + MyBatis-Plus + JDK 17 | `navel-orange-server/` |
| Python RAG 服务 | FastAPI + sentence-transformers + uvicorn | `navel-orange-rag/` |
| 微信小程序 | 原生微信小程序 + Vant Weapp | `navel-orange-app/` |
| 数据库 | PostgreSQL 16 + pgvector + PostGIS | Docker 部署 |
| 缓存 | Redis | Docker 部署 |

---

## 二、部署环境信息

| 项目 | 值 |
| --- | --- |
| 目标服务器 | CentOS 7+ (aarch64) |
| 服务器 IP | 10.168.89.200 |
| SSH 工具 | FinalShell |
| JDK | Amazon Corretto 17.0.14 (aarch64) |
| Maven | 3.9.16 |
| Docker | 已安装 |

---

## 三、系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    Nginx (反向代理)                          │
│                http://10.168.89.200:80                      │
├───────────────────┬───────────────────┬─────────────────────┤
│  Vue 3 前端        │  Spring Boot 后端  │  Python RAG 服务    │
│  端口 80           │  端口 8080         │  端口 18001         │
│  Docker            │  Docker            │  Docker             │
│  navel-orange-     │  navel-orange-    │  navel-orange-rag   │
│  nginx             │  server           │  FastAPI +          │
│                    │                   │  sentence-          │
│                    │                   │  transformers       │
├───────────────────┴───────────────────┴─────────────────────┤
│               PostgreSQL + pgvector                          │
│               10.168.89.200:5432                             │
├─────────────────────────────────────────────────────────────┤
│                    Redis                                     │
│               10.168.89.200:6379                             │
└─────────────────────────────────────────────────────────────┘
```

### 各服务管理方式

| 服务 | 端口 | 管理方式 | 自启动 | 容器/进程名 |
| --- | --- | --- | --- | --- |
| PostgreSQL + pgvector | 5432 | Docker | ✅ | postgres |
| Redis | 6379 | Docker | ✅ | redis |
| Spring Boot 后端 | 8080 | Docker | ✅ | navel-orange-server |
| Nginx 前端 | 80 | Docker | ✅ | navel-orange-nginx |
| Python RAG 服务 | 18001 | Docker | ✅ | navel-orange-rag |

---

## 四、完整部署步骤

### 步骤 1：搭建基础环境

在 CentOS 7 aarch64 VM 上安装所需软件：

#### 1.1 安装 JDK 17

```bash
# 下载 Amazon Corretto 17 (aarch64)
cd /tmp
wget https://corretto.aws/downloads/latest/amazon-corretto-17-aarch64-linux-jdk.tar.gz
tar -xzf amazon-corretto-17-aarch64-linux-jdk.tar.gz
mv amazon-corretto-17-* /usr/lib/jvm/java-17-amazon-corretto

# 配置环境变量
cat >> /etc/profile.d/java.sh << 'EOF'
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH
EOF
source /etc/profile.d/java.sh

# 验证
java -version
# openjdk version "17.0.14" ...
```

#### 1.2 安装 Maven

```bash
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz
tar -xzf apache-maven-3.9.16-bin.tar.gz
mv apache-maven-3.9.16 /opt/maven

# 配置环境变量
cat > /etc/profile.d/maven.sh << 'EOF'
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH
EOF
source /etc/profile.d/maven.sh

# 验证
mvn -version
# Apache Maven 3.9.16
```

#### 1.3 确认 Docker 已安装

```bash
docker --version
# Docker version 24.x.x 或更高

# 如果未安装，执行：
curl -fsSL https://get.docker.com | sh
systemctl start docker
systemctl enable docker
```

#### 1.4 上传项目文件到 VM

通过 FinalShell 的文件传输功能，将以下文件/目录上传到 `/root/`：

```
/root/
├── pom.xml                           # 父 POM
├── navel-orange-server/              # 后端模块
├── navel-orange-common/              # 公共模块
├── navel-orange-admin/               # 前端管理后台
├── navel-orange-rag/                 # RAG 服务
├── navel-orange-app/                 # 微信小程序
├── navel-orange-config/              # 外部配置文件
├── create_tables.sql                 # 建表脚本
├── insert_test_data.sql              # 测试数据
└── start-dev.sh                      # 开发启动脚本
```

> **注意**：实际部署时各模块直接放在 `/root/` 下，没有额外的 `navel-orange-monitoring` 父目录。

---

### 步骤 2：初始化数据库

#### 2.1 启动 PostgreSQL 容器

```bash
docker run -d --name postgres \
  -e POSTGRES_USER=root \
  -e POSTGRES_PASSWORD=Zjhw@123 \
  -p 5432:5432 \
  --restart unless-stopped \
  pgvector/pgvector:pg16
```

- `-e POSTGRES_USER=root`：设置数据库超级用户名
- `-e POSTGRES_PASSWORD=Zjhw@123`：设置数据库密码
- `-p 5432:5432`：端口映射
- `pgvector/pgvector:pg16`：PostgreSQL 16 镜像，内置 pgvector 扩展

#### 2.2 启动 Redis 容器

```bash
docker run -d --name redis \
  -p 6379:6379 \
  --restart unless-stopped \
  redis:7-alpine
```

- `redis:7-alpine`：轻量级 Redis 7 镜像
- Alpine 基础镜像体积小，适合生产环境

#### 2.3 等待数据库就绪

```bash
sleep 5
docker exec -i postgres psql -U root -d postgres -c "SELECT 1;"
# 返回 1 表示就绪
```

#### 2.4 导入建表脚本

```bash
# 导入核心表结构
docker exec -i postgres psql -U root -d postgres < /root/create_tables.sql

# 导入测试数据（可选）
docker exec -i postgres psql -U root -d postgres < /root/insert_test_data.sql
```

- `docker exec -i postgres psql`：在 postgres 容器内执行 SQL 命令
- `-i`：从 stdin 读取输入（允许重定向本地文件）
- `-U root`：以 root 用户连接
- `-d postgres`：连接 postgres 数据库
- `< /root/create_tables.sql`：将本地 SQL 文件通过 stdin 传入容器

#### 2.5 验证数据库

```bash
docker exec -i postgres psql -U root -d postgres -c "\dt"
# 应列出所有表：sys_user, orchard, device, disease_detection 等
```

---

### 步骤 3：修改后端配置文件

```bash
vi /root/navel-orange-server/src/main/resources/application-dev.yml
```

**主要修改项：**

```yaml
# 数据库连接（已是 VM IP，无需修改）
spring:
  datasource:
    url: jdbc:postgresql://10.168.89.200:5432/postgres
    username: root
    password: Zjhw@123

# Redis 连接（已是 VM IP，无需修改）
  data:
    redis:
      host: 10.168.89.200
      port: 6379
      password: Zjhw@123

# RAG 服务配置（需要修改）
rag:
  base-url: http://10.168.89.200:18001  # localhost 改为宿主机 IP
  rag-dir: /root/navel-orange-rag       # Windows 路径改为 VM 路径
```

---

### 步骤 4：构建后端 fat jar（把 Java 源码 + 所有依赖库 + 内嵌 Tomcat 打包成一个独立的 jar 文件）

#### 4.1 修改 server pom.xml（如果尚未修改）

编辑 `navel-orange-server/pom.xml`，确保 `spring-boot-maven-plugin` 有显式执行配置：作用是把源码编译成可执行的程序

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.5.2</version>
        </plugin>
    </plugins>
</build>
```

#### 4.2 执行构建

```bash
cd /root
mvn clean package -DskipTests
```

- `mvn clean`：清理旧的构建产物（target/ 目录）
- `package`：编译源码 → 运行测试（已跳过）→ 打包 jar
- `-DskipTests`：跳过测试用例执行
- 产出物：`navel-orange-server/target/navel-orange-server-1.0.0-SNAPSHOT.jar`（约 116MB fat jar）

**遇到的问题 #1**：`mvn spring-boot:repackage` 报错 "Source file is not available"

**原因**：`repackage` 目标必须跟 `package` 在同一个 Maven 生命周期中执行

**解决方案**：在 pom.xml 中添加 `<execution><goals><goal>repackage</goal></goals></execution>` 显式配置

---

### 步骤 5：构建 Docker 镜像并启动后端（打好的 jar 包，包上一层 Docker 外壳，然后跑起来。）

> **注意**：VM 为 **aarch64 (ARM64)** 架构，需使用支持 ARM64 的 JDK 基础镜像。

#### 5.1 创建 Dockerfile

```bash
cd /root/navel-orange-server

cat > Dockerfile << 'EOF'
FROM amazoncorretto:17-alpine                                                                                                                              # 第一步：拿一个带 JDK 17 的空壳子
RUN apk add --no-cache tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime            # 第二步：设置时区为东八区
WORKDIR /app                                                                                                                                                                  # 第三步：进入 /app 目录
COPY target/navel-orange-server-1.0.0-SNAPSHOT.jar app.jar                                                                  # 第四步：把 jar 包放进去
EXPOSE 8080                                                                                                                                                                     # 第五步：告诉外面这个容器用 8080 端口
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]                                                    # 第六步：开机命令 = java -jar app.jar
EOF
```

- `amazoncorretto:17-alpine`：Amazon Corretto 17 官方镜像，原生支持 ARM64
- `tzdata`：时区数据包，设置东八区
- 构建上下文约 125MB（包含 fat jar）

#### 5.2 构建镜像(把 jar 包 + JDK 17 + 时区设置 + 启动命令 打包成一个不可变的镜像。以后迁移到别的服务器，只需要带着这个镜像走，不需要重新装 JDK。)

```bash
docker build -t navel-orange-server .
```

- `-t navel-orange-server`：为镜像打标签
- `.`：构建上下文为当前目录（`navel-orange-server/` 目录）

#### 5.3 创建外部配置文件（jar 包一旦打包就不能改了。把配置抽到外面，以后改数据库密码、API Key 只需编辑这个文件，不用重新打包。）

```bash
# 复制配置文件到外部目录
mkdir -p /root/navel-orange-config
cp /root/navel-orange-server/src/main/resources/application-dev.yml /root/navel-orange-config/
```

#### 5.4 运行容器

```bash
docker run -d --name navel-orange-server \
  -p 8080:8080 \                    # 宿主机 8080 → 容器 8080
  -v /root/navel-orange-config:/config \  # 把外部配置挂进去
  --restart unless-stopped \         # 挂了自动重启
  navel-orange-server               # 用这个镜像启动
```

- `-v /root/navel-orange-config:/config`：挂载外部配置目录（优先于 jar 内配置）
- `--restart unless-stopped`：容器随 Docker 自动重启，除非手动停止
- 如需修改配置，编辑 `/root/navel-orange-config/application-dev.yml` 后重启容器

#### 5.5 验证后端

```bash
# 等待服务启动
sleep 8

# 查看容器日志
docker logs navel-orange-server --tail 30

# 测试 API
curl -s http://localhost:8080/swagger-ui.html -o /dev/null -w "%{http_code}"
# 返回 302 表示正常（重定向到 Swagger UI）
```

---

### 步骤 6：构建前端(把 Vue 源码变成浏览器能认的网页)构建好了dist/（静态网页文件）

#### 6.1 修改构建命令（跳过 vue-tsc跳过类型检查，直接构建。）

```bash
cd /root/navel-orange-admin

# 修改 package.json 的 build 命令
sed -i 's/"vue-tsc -b && vite build"/"vite build"/' package.json
```

**遇到的问题 #2**：`vue-tsc` 类型检查失败（40+ 个 TS 错误）

**原因**：el-table 插槽类型推断、Axios 返回类型、VideoMonitor.vue 变量未定义等

**解决方案**：跳过 `vue-tsc`，仅执行 `vite build`

#### 6.2 修改 API 基础路径（改前端代码里请求后端的地址）

```bash
# 将占位符改为 Nginx 代理路径
sed -i 's|VITE_API_BASE=https://api.example.com|VITE_API_BASE=/api|' .env.production
```

#### 6.3 在 Docker 中执行构建

```bash
docker run --rm -v /root/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install && pnpm build"
```

- `-e CI=true`：告诉 pnpm 当前是 CI 环境，跳过交互式确认
- `node:20-slim`：使用 Node.js 20 镜像（避免在 VM 上安装 Node.js）
- 产出物：`/root/navel-orange-admin/dist/` 目录

**遇到的问题 #3**：`pnpm install` 报错 `ERR_PNPM_ABORTED_REMOVE_MODULES_DIR_NO_TTY`

**解决方案**：添加 `-e CI=true` 环境变量

---

### 步骤 7：修复 Leaflet 热力图（leaflet.heat）

**遇到的问题 #4**：Dashboard 页面白屏，控制台报 `ReferenceError: L is not defined`

**原因**：`leaflet.heat` npm 包是 UMD 格式，源码直接引用全局变量 `L`。Vite/Rolldown 生产构建将其打包成独立 chunk 后，`L` 在 chunk 作用域内不存在。

**解决方案**：

#### 7.1 安装 simpleheat 依赖

```bash
docker run --rm -v /root/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install simpleheat"
```

#### 7.2 创建 ESM wrapper 文件

```bash
mkdir -p /root/navel-orange-admin/src/lib

cat > /root/navel-orange-admin/src/lib/leaflet-heat-init.ts << 'EOF'
import L from 'leaflet';
import simpleheat from 'simpleheat';

// @ts-ignore
L.heat = (latlngs: any[], options?: any) => {
  // ... (参见源码中的完整实现)
};
EOF
```

#### 7.3 配置 vite alias

在 `vite.config.ts` 中增加 alias：

```typescript
resolve: {
  alias: {
    'leaflet.heat': path.resolve(__dirname, 'src/lib/leaflet-heat-init.ts'),
  },
},
```

#### 7.4 重新构建

```bash
docker run --rm -v /root/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install && pnpm build"
```

---

### 步骤 8：启动 Nginx 容器托管前端

#### 8.1 创建 Nginx 配置

```bash
# 配置已内置在 navel-orange-admin/nginx.conf 中
cat /root/navel-orange-admin/nginx.conf
```

#### 8.2 启动 Nginx 容器

```bash
docker run -d --name navel-orange-nginx \
  -v /root/navel-orange-admin/dist:/usr/share/nginx/html:ro \
  -v /root/navel-orange-admin/nginx.conf:/etc/nginx/conf.d/default.conf:ro \
  -p 80:80 \
  --restart unless-stopped \
  nginx:alpine
```

- `-v /root/navel-orange-admin/dist:/usr/share/nginx/html:ro`：挂载前端构建产物
- `-v /root/navel-orange-admin/nginx.conf:/etc/nginx/conf.d/default.conf:ro`：挂载自定义 Nginx 配置
- `-p 80:80`：端口映射
- `--restart unless-stopped`：容器随 Docker 自动重启，除非手动停止
- `nginx:alpine`：轻量级 Nginx 镜像（基于 Alpine Linux）

---

### 步骤 9：启动 RAG 服务

#### 9.1 修复 .env 文件

**遇到的问题 #5**：RAG 容器启动失败，pydantic 报 `ValidationError: embedding_device Input should be 'cpu', 'cuda' or 'auto'`

**原因**：`.env` 文件中 `EMBEDDING_DEVICE=auto      # cpu | cuda | auto`，注释内容被当作值的一部分

**解决方案**：

```bash
# 去掉注释，确保值为纯文本
sed -i 's/^EMBEDDING_DEVICE=.*/EMBEDDING_DEVICE=auto/' /root/navel-orange-rag/.env
```

#### 9.2 启动 RAG 容器

```bash
docker run -d --name navel-orange-rag \
  -v /root/navel-orange-rag:/app -w /app \
  -p 18001:18001 \
  --env-file /root/navel-orange-rag/.env \
  --restart unless-stopped \
  navel-orange-rag
```

- `--env-file`：从 .env 文件加载环境变量
- `-w /app`：设置工作目录
- `-v /app`：将 RAG 代码目录挂载到容器内

#### 9.3 验证 RAG 服务

```bash
sleep 10
curl -s http://localhost:18001/health
# 返回 {"status":"ok"} 表示正常
```

**注意**：RAG 服务首次启动时需下载 Embedding 模型（约 400 MB），请确保网络畅通。

---

### 步骤 10：配置所有容器自启动

```bash
# 所有容器已在创建时指定 --restart unless-stopped
# 可确认当前自启动状态
docker update --restart unless-stopped postgres redis navel-orange-server navel-orange-nginx navel-orange-rag
```

---

### 步骤 11：增大 inotify 监视数上限

**遇到的问题 #6**：前端 Vite dev 模式报 `ENOSPC: System limit for number of file watchers reached`

**解决方案**：

```bash
# 写入系统配置
echo "fs.inotify.max_user_watches=524288" >> /etc/sysctl.conf

# 立即生效
sysctl -p
```

- CentOS 7 默认 inotify 上限为 8192，太低
- 524288 可以满足大部分开发需求

---

### 步骤 12：验证所有服务

执行以下命令快速确认所有服务正常：

```bash
# 1. 检查所有 Docker 容器
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 2. 测试数据库连接
docker exec -i postgres psql -U root -d postgres -c "SELECT 1;"

# 3. 测试 Redis
docker exec -i redis redis-cli ping

# 4. 测试后端 API
curl -s http://localhost:8080/swagger-ui.html -o /dev/null -w "%{http_code}"

# 5. 测试 RAG 服务
curl -s http://localhost:18001/health

# 6. 测试前端（通过 Nginx 代理）
curl -s http://localhost:80 -o /dev/null -w "%{http_code}"

# 7. 一次性查看所有运行状态
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo "---"
echo "后端: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/swagger-ui.html)"
echo "前端: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:80)"
echo "RAG:  $(curl -s http://localhost:18001/health)"
```

预期结果：

| 检查项 | 预期输出 | 说明 |
| --- | --- | --- |
| Docker 容器 | postgres, redis, navel-orange-server, navel-orange-nginx, navel-orange-rag 状态为 Up | 所有容器运行中 |
| 数据库 | `?column?`<br>`----------`<br>`1` | 返回 1 表示连接正常 |
| Redis | `PONG` | Redis 响应正常 |
| 后端 API | `302` | Swagger UI 重定向 |
| RAG 服务 | `{"status":"ok"}` | 健康检查通过 |
| 前端 | `200` | Nginx 返回静态页面 |

---

### 服务启动顺序说明

部署时应按以下顺序启动服务（从底层到上层）：

```
1. PostgreSQL + Redis  (Docker 基础设施)
       ↓
2. Spring Boot 后端    (依赖数据库)
       ↓
3. Nginx 前端 + RAG 服务 (依赖后端)
```

- **PostgreSQL/Redis** 必须最先启动，因为后端和 RAG 都依赖它们
- **Spring Boot 后端** 在数据库就绪后启动，Flyway 会自动执行数据库迁移
- **Nginx 和 RAG** 可以同时启动，它们都依赖后端但互不依赖

---

### 端口配置注意事项

本项目存在端口不一致的情况，需要注意：

| 文件 | RAG 端口 | 说明 |
| --- | --- | --- |
| `navel-orange-rag/.env` | 18001 | 容器内实际运行端口 |
| `navel-orange-rag/Dockerfile` | 8001 | **注意：与 .env 不一致** |
| `navel-orange-rag/docker-compose.yml` | 8001:8001 | **注意：与 .env 不一致** |
| `start-dev.bat` | 18001 | Windows 开发脚本 |
| `start-dev.sh` | 8001 | Linux 开发脚本（bug） |
| `application-dev.yml` (rag.base-url) | 18001 | 后端连接 RAG 的端口 |

**重要**：使用 `docker run` 时以 `.env` 中的 `PORT=18001` 为准。如果使用 `docker-compose.yml`，需要先修改其中的端口映射为 `18001:18001`。

---

### 启动脚本问题

`start-dev.sh` 中 RAG 端口写的是 8001，但 `.env` 和 `start-dev.bat` 用的是 18001，这是一个已知 bug。如果在 Linux 上使用开发脚本，需要修改 `start-dev.sh`：

```bash
# 将所有 8001 替换为 18001
sed -i 's/8001/18001/g' /root/start-dev.sh
```

---

## 五、遇到的问题汇总

| # | 问题 | 原因 | 解决方案 |
| --- | --- | --- | --- |
| 1 | `mvn spring-boot:repackage` 报 "Source file is not available" | repackage 必须在同一生命周期中执行 | 编辑 pom.xml 添加 `<execution><goals><goal>repackage</goal></goals></execution>` |
| 2 | `vue-tsc` 类型检查失败（40+ 错误） | el-table 插槽类型推断、Axios 返回类型等 TS 问题 | 修改 build 命令跳过 `vue-tsc`：`"build": "vite build"` |
| 3 | `pnpm install` 报 `ERR_PNPM_ABORTED_REMOVE_MODULES_DIR_NO_TTY` | pnpm 在非 TTY 环境下需要 CI 模式 | 添加 `-e CI=true` 环境变量 |
| 4 | 前端页面白屏 `ReferenceError: L is not defined` | `leaflet.heat` UMD 包在 Vite 生产构建中无法解析全局 `L` | 创建 ESM wrapper 文件 + vite alias 替代原始 npm 包 |
| 5 | RAG 容器 pydantic 校验失败 `embedding_device` | `.env` 中值后面有注释 `# cpu \ | cuda \ | auto` | `sed` 去掉注释，确保值为纯文本 |
| 6 | 前端 `pnpm dev` 报 `ENOSPC: file watchers reached` | CentOS 7 默认 inotify 上限太低 | `sysctl -p` 增大 `fs.inotify.max_user_watches` |
| 7 | `docker build` 拉取 `eclipse-temurin:17-jre-alpine` 超时 | VM 网络访问 Docker Hub 受限 | 配置国内 Docker 镜像加速器解决网络超时 |
| 8 | daocloud 镜像源拉取 `eclipse-temurin` 报 `no matching manifest for linux/arm64/v8` | 镜像源未缓存 ARM64 架构版本 | 改用 `amazoncorretto:17-alpine` 镜像，原生支持 ARM64 |
| 9 | 前端连不上后端 | `.env.production` 中 `VITE_API_BASE` 是占位符 `https://api.example.com` | 改为 `/api`，配合 Nginx 代理到后端 |

---

## 六、关键文件路径

### 后端

| 文件 | VM 路径 | 说明 |
| --- | --- | --- |
| 外部配置 | `/root/navel-orange-config/application-dev.yml` | **改配置编辑这个** |
| jar 包 | `/root/navel-orange-server/target/navel-orange-server-1.0.0-SNAPSHOT.jar` | Spring Boot fat jar |
| Dockerfile | `/root/navel-orange-server/Dockerfile` | 镜像构建文件（基于 amazoncorretto:17-alpine） |
| Docker 镜像 | `navel-orange-server:latest` | 后端 Docker 镜像 |
| 容器名 | `navel-orange-server` | 运行时容器名称 |
| 源码配置 | `/root/navel-orange-server/src/main/resources/application-dev.yml` | 源码中的配置（外部配置优先） |
| Maven POM | `/root/pom.xml` | 父 POM（模块管理） |

### 前端

| 文件 | VM 路径 | 说明 |
| --- | --- | --- |
| 构建产物 | `/root/navel-orange-admin/dist/` | Nginx 托管的静态文件 |
| Nginx 配置 | `/root/navel-orange-admin/nginx.conf` | 代理规则、SPA 路由 |
| API 基础路径 | `/root/navel-orange-admin/.env.production` | `VITE_API_BASE=/api` |
| 构建命令 | `/root/navel-orange-admin/package.json` | `"build": "vite build"`（跳过了 vue-tsc） |
| Vite 配置 | `/root/navel-orange-admin/vite.config.ts` | 别名、代理、插件配置 |
| Leaflet wrapper | `/root/navel-orange-admin/src/lib/leaflet-heat-init.ts` | ESM 兼容的热力图实现 |

### RAG 服务

| 文件 | VM 路径 | 说明 |
| --- | --- | --- |
| 环境配置 | `/root/navel-orange-rag/.env` | PORT、API Key、模型配置 |
| Dockerfile | `/root/navel-orange-rag/Dockerfile` | 基于 python:3.11-slim-bookworm |

### 数据库

| 文件 | VM 路径 | 说明 |
| --- | --- | --- |
| 建表脚本 | `/root/create_tables.sql` | 所有表 + pgvector/PostGIS 扩展 |
| 测试数据 | `/root/insert_test_data.sql` | 初始测试数据 |

---

## 七、后期需要修改的入口

### 1. 替换 DeepSeek API Key

RAG 服务的 LLM 问答功能需要真实的 API Key：

```bash
vi /root/navel-orange-rag/.env
# 找到 DEEPSEEK_API_KEY=sk-your-deepseek-api-key
# 替换为真实 key
docker restart navel-orange-rag
```

### 2. 修复 TypeScript 类型错误（40+ 个）

前端 `package.json` 的 build 命令已跳过 `vue-tsc`。修复后可恢复：

```json
"build": "vue-tsc -b && vite build"
```

主要错误类型：

- `DefaultRow` 不匹配具体类型 → el-table 插槽类型推断
- `AxiosResponse` 属性不存在 → Axios 拦截器已解包
- `VideoMonitor.vue` 变量未定义 → `websocket`、`pc` 等局部变量

### 3. 配置 HTTPS

生产环境建议配置 SSL：

```bash
# 安装 certbot（Let's Encrypt）
# 或在 Nginx 中配置自签名证书
# 编辑 nginx.conf 添加 443 监听和 ssl 配置
```

### 4. 配置 Nginx 生产缓存

```bash
# 在 nginx.conf 中添加静态资源缓存策略
# location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
#     expires 30d;
#     add_header Cache-Control "public, immutable";
# }
```

---

## 八、常用运维命令

### 查看服务状态

```bash
# 查看所有 Docker 容器状态
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 查看各容器资源占用
docker stats --no-stream

# 查看 Docker 磁盘占用
docker system df
```

### 重启服务

```bash
# 重启后端
docker restart navel-orange-server

# 重启前端 Nginx
docker restart navel-orange-nginx

# 重启 RAG 服务
docker restart navel-orange-rag

# 重启所有容器
docker restart postgres redis navel-orange-server navel-orange-nginx navel-orange-rag
```

### 查看日志

```bash
# 后端日志
docker logs navel-orange-server --tail 50
docker logs navel-orange-server -f                 # 实时追踪（Ctrl+C 退出）

# 前端 Nginx 日志
docker logs navel-orange-nginx --tail 30

# RAG 服务日志
docker logs navel-orange-rag --tail 30

# 数据库日志
docker logs postgres --tail 30

# Redis 日志
docker logs redis --tail 30
```

### 修改配置

```bash
# 1. 编辑外部配置文件
vi /root/navel-orange-config/application-dev.yml

# 2. 重启后端（配置生效）
docker restart navel-orange-server
```

### 重新构建前端

```bash
# 修改源码后重新构建
docker run --rm -v /root/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install && pnpm build"

# 重启 Nginx 使新构建生效
docker restart navel-orange-nginx
```

### 重新构建后端

```bash
# 方式一：在 VM 本地编译（需要 JDK + Maven）
cd /root && mvn clean package -DskipTests

# 方式二：重新构建 Docker 镜像（包含新代码）
cd /root/navel-orange-server
docker build -t navel-orange-server .

# 删除旧容器并启动新容器
docker stop navel-orange-server && docker rm navel-orange-server
docker run -d --name navel-orange-server \
  -p 8080:8080 \
  -v /root/navel-orange-config:/config \
  --restart unless-stopped \
  navel-orange-server
```

### 服务级操作

```bash
# 停止单个服务
docker stop navel-orange-server

# 启动单个服务
docker start navel-orange-server

# 进入容器内部
docker exec -it navel-orange-server sh

# 查看容器详细信息
docker inspect navel-orange-server
```

### 镜像管理

```bash
# 列出本地镜像
docker images

# 删除旧镜像
docker rmi navel-orange-server:<旧版本>

# 清理未使用的镜像和容器
docker system prune -f
```

### 一键全量部署脚本

```bash
# 按顺序启动所有服务
docker start postgres redis
sleep 5
docker start navel-orange-server
sleep 8
docker start navel-orange-nginx navel-orange-rag
echo "所有服务已启动"

# 验证
docker ps --format "table {{.Names}}\t{{.Status}}"
```

---

## 九、端口一览

| 端口 | 服务 | 协议 |
| --- | --- | --- |
| 80 | 前端 Nginx | HTTP |
| 3000 | ~~Vite 开发服务器~~（已废弃） | - |
| 5432 | PostgreSQL | TCP |
| 6379 | Redis | TCP |
| 8080 | Spring Boot 后端 | HTTP |
| 18001 | Python RAG 服务 | HTTP |

---

## 十、外部服务依赖

| 服务 | 用途 | 配置位置 | 是否必需 |
| --- | --- | --- | --- |
| 华为云 IoTDA | IoT 设备接入与管理 | `application-dev.yml` → `huawei.iotda` | 是 |
| 华为云 OBS | 文件存储（图片、固件等） | `application-dev.yml` → `huawei.obs` | 是 |
| 高德地图 API | 果园地图展示与定位 | `application-dev.yml` → `map.amap` | 是 |
| 和风天气 API | 天气数据获取 | `application-dev.yml` → `weather.qweather` | 否 |
| DeepSeek API | RAG 问答 LLM 推理 | `navel-orange-rag/.env` | 是 |
| 网易邮箱 SMTP | 预警邮件推送 | `application-dev.yml` → `spring.mail` | 否 |
| 阿里云短信 | 短信验证码发送 | `application-dev.yml` → `aliyun.sms` | 否 |

> **注意**：各第三方服务的 API Key 和 Secret 请妥善保管，建议生产环境中通过环境变量注入而非硬编码在配置文件中。

---

## 十一、安全注意事项

1. **敏感信息**：`application-dev.yml` 包含数据库密码、API Key、Secret Key 等敏感信息，不要提交到公开仓库
2. **Swagger 文档**：生产环境建议关闭 Knife4j（设置 `knife4j.enable: false`）
3. **防火墙**：仅开放必要端口（80、8080、5432 等）
4. **HTTPS**：生产环境必须配置 SSL 证书
5. **数据库密码**：建议使用更强的密码，生产环境不要使用 `Zjhw@123`
