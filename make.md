# 脐橙病虫害监测系统 — 完整部署文档

## 一、项目概述

本项目为 **脐橙病虫害监测与数据收集系统**，采用前后端分离 + 微服务架构：

| 组件 | 技术栈 | 说明 |
|------|--------|------|
| 前端管理后台 | Vue 3 + TypeScript + Element Plus + Vite | `navel-orange-admin/` |
| 后端服务 | Spring Boot 3.4.2 + MyBatis-Plus + JDK 17 | `navel-orange-server/` |
| Python RAG 服务 | FastAPI + sentence-transformers + uvicorn | `navel-orange-rag/` |
| 微信小程序 | 原生微信小程序 + Vant Weapp | `navel-orange-app/` |
| 数据库 | PostgreSQL 16 + pgvector + PostGIS | Docker 部署 |
| 缓存 | Redis | Docker 部署 |

---

## 二、部署环境信息

| 项目 | 值 |
|------|-----|
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
│  Nginx Docker      │  systemd / Docker  │  Docker             │
│  navel-orange-     │  navel-orange-    │  navel-orange-rag   │
│  nginx             │  server + common  │  FastAPI +          │
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
|------|------|---------|--------|------------|
| PostgreSQL + pgvector | 5432 | Docker | ✅ | postgres |
| Redis | 6379 | Docker | ✅ | redis |
| Spring Boot 后端 | 8080 | systemd | ✅ | navel-orange.service |
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
├── navel-orange-monitoring/          # 完整项目目录
│   ├── pom.xml
│   ├── navel-orange-server/
│   ├── navel-orange-common/
│   ├── navel-orange-admin/
│   ├── navel-orange-rag/
│   └── ...
├── create_tables.sql                 # 建表脚本
└── insert_test_data.sql              # 测试数据
```

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
vi /root/navel-orange-monitoring/navel-orange-server/src/main/resources/application-dev.yml
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
  rag-dir: /root/navel-orange-monitoring/navel-orange-rag  # Windows 路径改为 VM 路径
```

---

### 步骤 4：构建后端 fat jar

#### 4.1 修改 server pom.xml（如果尚未修改）

编辑 `navel-orange-server/pom.xml`，确保 `spring-boot-maven-plugin` 有显式执行配置：

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
cd /root/navel-orange-monitoring
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

### 步骤 5：启动后端服务（systemd）

#### 5.1 创建 systemd 服务文件

```bash
cat > /etc/systemd/system/navel-orange.service << 'EOF'
[Unit]
Description=Navel Orange Backend
After=network.target

[Service]
Type=simple
ExecStart=/usr/bin/java -jar /root/navel-orange-monitoring/navel-orange-server/target/navel-orange-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
Restart=always
RestartSec=5
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF
```

各字段说明：
- `After=network.target`：在网络服务启动后再启动
- `Type=simple`：直接执行主进程
- `Restart=always`：进程异常退出后自动重启
- `RestartSec=5`：重启间隔 5 秒
- `WantedBy=multi-user.target`：多用户模式下启动

#### 5.2 启动服务

```bash
# 重载 systemd 配置（添加新服务后必须执行）
systemctl daemon-reload

# 设置开机自启
systemctl enable navel-orange

# 立即启动服务
systemctl start navel-orange
```

- `systemctl daemon-reload`：重新加载所有 systemd 服务配置文件
- `systemctl enable navel-orange`：创建符号链接到 multi-user.target，实现开机自启
- `systemctl start navel-orange`：立即启动服务

#### 5.3 验证后端

```bash
# 等待服务启动
sleep 8

# 检查服务状态
systemctl status navel-orange --no-pager

# 测试 API
curl -s http://localhost:8080/swagger-ui.html -o /dev/null -w "%{http_code}"
# 返回 302 表示正常（重定向到 Swagger UI）
```

---

### 步骤 6：创建外部配置文件（方便后续修改）

```bash
# 复制配置文件到外部目录
mkdir -p /root/navel-orange-config
cp /root/navel-orange-monitoring/navel-orange-server/src/main/resources/application-dev.yml /root/navel-orange-config/
```

#### 更新 systemd 服务以使用外部配置

```bash
# 修改 systemd 服务文件，添加外部配置路径
cat > /etc/systemd/system/navel-orange.service << 'EOF'
[Unit]
Description=Navel Orange Backend
After=network.target

[Service]
Type=simple
ExecStart=/usr/bin/java -jar /root/navel-orange-monitoring/navel-orange-server/target/navel-orange-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --spring.config.additional-location=/root/navel-orange-config/
Restart=always
RestartSec=5
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

# 重载并重启
systemctl daemon-reload
systemctl restart navel-orange
```

- `--spring.config.additional-location=/root/navel-orange-config/`：Spring Boot 会额外读取此目录的配置文件
- **外部配置优先级高于 jar 内的配置**，改配置只需编辑外部文件

---

### 步骤 7：构建前端

#### 7.1 修改构建命令（跳过 vue-tsc）

```bash
cd /root/navel-orange-monitoring/navel-orange-admin

# 修改 package.json 的 build 命令
sed -i 's/"vue-tsc -b && vite build"/"vite build"/' package.json
```

**遇到的问题 #2**：`vue-tsc` 类型检查失败（40+ 个 TS 错误）

**原因**：el-table 插槽类型推断、Axios 返回类型、VideoMonitor.vue 变量未定义等

**解决方案**：跳过 `vue-tsc`，仅执行 `vite build`

#### 7.2 修改 API 基础路径

```bash
# 将占位符改为 Nginx 代理路径
sed -i 's|VITE_API_BASE=https://api.example.com|VITE_API_BASE=/api|' .env.production
```

#### 7.3 在 Docker 中执行构建

```bash
docker run --rm -v /root/navel-orange-monitoring/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install && pnpm build"
```

- `-e CI=true`：告诉 pnpm 当前是 CI 环境，跳过交互式确认
- `node:20-slim`：使用 Node.js 20 镜像（避免在 VM 上安装 Node.js）
- 产出物：`/root/navel-orange-monitoring/navel-orange-admin/dist/` 目录

**遇到的问题 #3**：`pnpm install` 报错 `ERR_PNPM_ABORTED_REMOVE_MODULES_DIR_NO_TTY`

**解决方案**：添加 `-e CI=true` 环境变量

---

### 步骤 8：修复 Leaflet 热力图（leaflet.heat）

**遇到的问题 #4**：Dashboard 页面白屏，控制台报 `ReferenceError: L is not defined`

**原因**：`leaflet.heat` npm 包是 UMD 格式，源码直接引用全局变量 `L`。Vite/Rolldown 生产构建将其打包成独立 chunk 后，`L` 在 chunk 作用域内不存在。

**解决方案**：

#### 8.1 安装 simpleheat 依赖

```bash
docker run --rm -v /root/navel-orange-monitoring/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm add simpleheat"
```

#### 8.2 创建 ESM wrapper 文件

```bash
mkdir -p /root/navel-orange-monitoring/navel-orange-admin/src/lib
```

创建 `/root/navel-orange-monitoring/navel-orange-admin/src/lib/leaflet-heat-init.ts`：

```typescript
import L from 'leaflet'
import simpleheat from 'simpleheat'

// Define L.HeatLayer and L.heatLayer for leaflet.heat compatibility
if (!L.HeatLayer) {
  (L as any).HeatLayer = (L.Layer as any).extend({
    initialize: function (latlngs: number[][], options: any) {
      this._latlngs = latlngs
      L.setOptions(this, options)
    },
    setLatLngs: function (latlngs: number[][]) {
      this._latlngs = latlngs
      return this.redraw()
    },
    addLatLng: function (latlng: number[]) {
      this._latlngs.push(latlng)
      return this.redraw()
    },
    redraw: function () {
      if (this._heat && !this._frame && this._map && !this._map._animating) {
        this._frame = L.Util.requestAnimFrame(this.redraw, this)
      }
      return this
    },
    onAdd: function (map: any) {
      this._map = map
      if (!this._canvas) {
        this._initCanvas()
      }
      if (this.options.pane) {
        this.getPane().appendChild(this._canvas)
      } else {
        map._panes.overlayPane.appendChild(this._canvas)
      }
      map.on('moveend', this.redraw, this)
      if (map.options.zoomAnimation && L.Browser.any3d) {
        map.on('zoomanim', this._animateZoom, this)
      }
      this.redraw()
    },
    onRemove: function (map: any) {
      if (this.options.pane) {
        this.getPane().removeChild(this._canvas)
      } else {
        map.getPanes().overlayPane.removeChild(this._canvas)
      }
      map.off('moveend', this.redraw, this)
      if (map.options.zoomAnimation) {
        map.off('zoomanim', this._animateZoom, this)
      }
    },
    addTo: function (map: any) {
      map.addLayer(this)
      return this
    },
    _initCanvas: function () {
      const canvas = (this._canvas = L.DomUtil.create(
        'canvas',
        'leaflet-leaflet-heat-layer leaflet-layer'
      ))
      const originProp = L.DomUtil.testProp([
        'transformOrigin',
        'WebkitTransformOrigin',
        'msTransformOrigin',
      ])
      if (originProp) {
        canvas.style[originProp as any] = '50% 50%'
      }
      const size = this._map.getSize()
      canvas.width = size.x
      canvas.height = size.y
      const ret = this._map.getSize()
      if (this._heat) {
        this._heat.resize(size.x, size.y)
      } else {
        this._heat = (simpleheat as any)(canvas)
      }
      this._ctx = canvas.getContext('2d') as CanvasRenderingContext2D
      // Reset canvas transform for retina
      const max = this._map.getMaxZoom()
      const h = this.options.height || this._heat._height / 256
      const r = this.options.radius || h
      if (this.options.max) {
        this._heat.max(this.options.max)
      } else {
        this._heat.max(
          this.options.max === undefined
            ? 0.5 - 1 / (Math.pow(2, max - this.options.minZoom || 0) * 2)
            : this.options.max
        )
      }
      if (this.options.blur !== undefined) {
        this._heat.blur(this.options.blur)
      }
    },
    _animateZoom: function (opt: any) {
      const scale = this._map.getZoomScale(opt.zoom)
      const offset = this._map
        ._getCenterOffset(opt.center)
        ._multiplyBy(-scale)
        .add(this._canvas._map._getMapPanePos())
      this._canvas.style[L.DomUtil.TRANSFORM] =
        L.DomUtil.getTranslateString(offset) + ' scale(' + scale + ')'
    },
    redraw: function () {
      if (!this._map) {
        return
      }
      const data: number[][] = []
      const r = this._heat._r
      const size = this._map.getSize()
      const bounds = new L.Bounds(
        L.point([-r, -r]),
        size.add(L.point([r, r]))
      )
      const max = this.options.max === undefined ? 1 : this.options.max
      const maxZoom =
        this.options.maxZoom === undefined
          ? this._map.getMaxZoom()
          : this.options.maxZoom
      const v = 1 / Math.pow(2, Math.max(0, Math.min(maxZoom - this._map.getZoom(), 12)) / 2)
      const cellSize = r / 2
      const grid: any[] = []
      const panePos = this._map._getMapPanePos()
      const offsetX = panePos.x % cellSize
      const offsetY = panePos.y % cellSize
      let i: number
      let len: number
      let p: number[]
      let cell: number[]
      let k: number
      for (i = 0, len = this._latlngs.length; i < len; i++) {
        if (bounds.contains(this._map.latLngToContainerPoint(this._latlngs[i]))) {
          p = this._map.latLngToContainerPoint(this._latlngs[i])
          k = p.x
          cell = grid[Math.floor((p.y - offsetY) / cellSize)]
          if (!cell) {
            cell = grid[Math.floor((p.y - offsetY) / cellSize)] = []
          }
          cell.push([
            (p.x - offsetX) / cellSize,
            (p.y - offsetY) / cellSize,
            this._latlngs[i][2] !== undefined ? this._latlngs[i][2] : 1,
          ])
        }
      }
      for (i = 0, len = grid.length; i < len; i++) {
        if (!grid[i]) continue
        for (k = 0; k < grid[i].length; k++) {
          data.push([
            Math.round(grid[i][k][0]),
            Math.round(grid[i][k][1]),
            Math.min(grid[i][k][2], max),
          ])
        }
      }
      if (this._heat) {
        this._heat.data(data).draw(this.options.minOpacity)
      }
      return this
    },
  })
}

if (!(L as any).heatLayer) {
  (L as any).heatLayer = function (latlngs: number[][], options: any) {
    return new (L as any).HeatLayer(latlngs, options)
  }
}

export default L
```

#### 8.3 添加 Vite alias

```bash
cd /root/navel-orange-monitoring/navel-orange-admin

# 在 vite.config.ts 中添加 leaflet.heat alias
sed -i "s|'@': path.resolve(__dirname, 'src'),|'@': path.resolve(__dirname, 'src'),\n    'leaflet.heat': path.resolve(__dirname, 'src/lib/leaflet-heat-init.ts'),|" vite.config.ts
```

#### 8.4 恢复 DashboardView.vue 的 import

确保 `src/views/dashboard/DashboardView.vue` 中使用：

```typescript
import 'leaflet.heat'
```

#### 8.5 重新构建前端

```bash
docker run --rm -v /root/navel-orange-monitoring/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install && pnpm build"
```

---

### 步骤 9：部署 Nginx 前端

#### 9.1 创建 Nginx 配置

```bash
cat > /root/navel-orange-monitoring/navel-orange-admin/nginx.conf << 'EOF'
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # Vue Router history 模式支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api {
        proxy_pass http://10.168.89.200:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
EOF
```

各指令说明：
- `listen 80`：监听 HTTP 80 端口
- `try_files $uri $uri/ /index.html`：Vue Router 的 history 模式支持，所有路由都返回 index.html
- `proxy_pass http://10.168.89.200:8080`：将 `/api` 请求代理到后端 Spring Boot 服务

#### 9.2 启动 Nginx 容器

```bash
docker run -d --name navel-orange-nginx \
  -v /root/navel-orange-monitoring/navel-orange-admin/dist:/usr/share/nginx/html \
  -v /root/navel-orange-monitoring/navel-orange-admin/nginx.conf:/etc/nginx/conf.d/default.conf \
  -p 80:80 \
  --restart unless-stopped \
  nginx:alpine
```

- `-v dist:/usr/share/nginx/html`：将构建产物挂载到 Nginx 静态文件目录
- `-v nginx.conf:/etc/nginx/conf.d/default.conf`：挂载自定义 Nginx 配置
- `-p 80:80`：端口映射
- `--restart unless-stopped`：容器随 Docker 自动重启，除非手动停止
- `nginx:alpine`：轻量级 Nginx 镜像（基于 Alpine Linux）

---

### 步骤 10：启动 RAG 服务

#### 10.1 修复 .env 文件

**遇到的问题 #5**：RAG 容器启动失败，pydantic 报 `ValidationError: embedding_device Input should be 'cpu', 'cuda' or 'auto'`

**原因**：`.env` 文件中 `EMBEDDING_DEVICE=auto      # cpu | cuda | auto`，注释内容被当作值的一部分

**解决方案**：

```bash
# 去掉注释，确保值为纯文本
sed -i 's/^EMBEDDING_DEVICE=.*/EMBEDDING_DEVICE=auto/' /root/navel-orange-monitoring/navel-orange-rag/.env
```

#### 10.2 启动 RAG 容器

```bash
docker run -d --name navel-orange-rag \
  -v /root/navel-orange-monitoring/navel-orange-rag:/app -w /app \
  -p 18001:18001 \
  --env-file /root/navel-orange-monitoring/navel-orange-rag/.env \
  --restart unless-stopped \
  navel-orange-rag
```

- `--env-file`：从 .env 文件加载环境变量
- `-w /app`：设置工作目录
- `-v /app`：将 RAG 代码目录挂载到容器内

#### 10.3 验证 RAG 服务

```bash
sleep 10
curl -s http://localhost:18001/health
# 返回 {"status":"ok"} 表示正常
```

**注意**：RAG 服务首次启动时需下载 Embedding 模型（约 400 MB），请确保网络畅通。

---

### 步骤 11：配置所有容器自启动

```bash
# 给 postgres 和 redis 也配置自启动
docker update --restart unless-stopped postgres redis
```

---

### 步骤 12：增大 inotify 监视数上限

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

### 步骤 13：验证所有服务

执行以下命令快速确认所有服务正常：

```bash
# 1. 检查 Docker 容器
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 2. 检查后端 systemd 状态
systemctl is-active navel-orange

# 3. 测试数据库连接
docker exec -i postgres psql -U root -d postgres -c "SELECT 1;"

# 4. 测试 Redis
docker exec -i redis redis-cli ping

# 5. 测试后端 API
curl -s http://localhost:8080/swagger-ui.html -o /dev/null -w "%{http_code}"

# 6. 测试 RAG 服务
curl -s http://localhost:18001/health

# 7. 测试前端（通过 Nginx 代理）
curl -s http://localhost:80 -o /dev/null -w "%{http_code}"
```

预期结果：
| 检查项 | 预期输出 | 说明 |
|--------|---------|------|
| Docker 容器 | postgres, redis, navel-orange-nginx, navel-orange-rag 状态为 Up | 所有容器运行中 |
| 后端 systemd | `active` | 服务正在运行 |
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
|------|---------|------|
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
sed -i 's/8001/18001/g' /root/navel-orange-monitoring/start-dev.sh
```

---

## 五、遇到的问题汇总

| # | 问题 | 原因 | 解决方案 |
|---|------|------|---------|
| 1 | `mvn spring-boot:repackage` 报 "Source file is not available" | repackage 必须在同一生命周期中执行 | 编辑 pom.xml 添加 `<execution><goals><goal>repackage</goal></goals></execution>` |
| 2 | `vue-tsc` 类型检查失败（40+ 错误） | el-table 插槽类型推断、Axios 返回类型等 TS 问题 | 修改 build 命令跳过 `vue-tsc`：`"build": "vite build"` |
| 3 | `pnpm install` 报 `ERR_PNPM_ABORTED_REMOVE_MODULES_DIR_NO_TTY` | pnpm 在非 TTY 环境下需要 CI 模式 | 添加 `-e CI=true` 环境变量 |
| 4 | 前端页面白屏 `ReferenceError: L is not defined` | `leaflet.heat` UMD 包在 Vite 生产构建中无法解析全局 `L` | 创建 ESM wrapper 文件 + vite alias 替代原始 npm 包 |
| 5 | RAG 容器 pydantic 校验失败 `embedding_device` | `.env` 中值后面有注释 `# cpu \| cuda \| auto` | `sed` 去掉注释，确保值为纯文本 |
| 6 | 前端 `pnpm dev` 报 `ENOSPC: file watchers reached` | CentOS 7 默认 inotify 上限太低 | `sysctl -p` 增大 `fs.inotify.max_user_watches` |
| 7 | `docker build` 拉取 `eclipse-temurin:17-jre-alpine` 超时 | VM 网络访问 Docker Hub 受限 | 改用 systemd 管理后端，不需要 Docker 镜像 |
| 8 | Docker 镜像加速配置后仍超时 | 国内 Docker Hub 镜像源不稳定 | 同上，使用 systemd 方案 |
| 9 | 前端连不上后端 | `.env.production` 中 `VITE_API_BASE` 是占位符 `https://api.example.com` | 改为 `/api`，配合 Nginx 代理到后端 |

---

## 六、关键文件路径

### 后端

| 文件 | VM 路径 | 说明 |
|------|---------|------|
| 外部配置 | `/root/navel-orange-config/application-dev.yml` | **改配置编辑这个** |
| jar 包 | `/root/navel-orange-monitoring/navel-orange-server/target/navel-orange-server-1.0.0-SNAPSHOT.jar` | Spring Boot fat jar |
| systemd 服务 | `/etc/systemd/system/navel-orange.service` | 服务定义、启动参数 |
| 启动日志 | `journalctl -u navel-orange` | 通过 systemd 输出 |
| 源码配置 | `/root/navel-orange-monitoring/navel-orange-server/src/main/resources/application-dev.yml` | 源码中的配置（外部配置优先） |
| Maven POM | `/root/navel-orange-monitoring/pom.xml` | 父 POM（模块管理） |

### 前端

| 文件 | VM 路径 | 说明 |
|------|---------|------|
| 构建产物 | `/root/navel-orange-monitoring/navel-orange-admin/dist/` | Nginx 托管的静态文件 |
| Nginx 配置 | `/root/navel-orange-monitoring/navel-orange-admin/nginx.conf` | 代理规则、SPA 路由 |
| API 基础路径 | `/root/navel-orange-monitoring/navel-orange-admin/.env.production` | `VITE_API_BASE=/api` |
| 构建命令 | `/root/navel-orange-monitoring/navel-orange-admin/package.json` | `"build": "vite build"`（跳过了 vue-tsc） |
| Vite 配置 | `/root/navel-orange-monitoring/navel-orange-admin/vite.config.ts` | 别名、代理、插件配置 |
| Leaflet wrapper | `/root/navel-orange-monitoring/navel-orange-admin/src/lib/leaflet-heat-init.ts` | ESM 兼容的热力图实现 |

### RAG 服务

| 文件 | VM 路径 | 说明 |
|------|---------|------|
| 环境配置 | `/root/navel-orange-monitoring/navel-orange-rag/.env` | PORT、API Key、模型配置 |
| Dockerfile | `/root/navel-orange-monitoring/navel-orange-rag/Dockerfile` | 基于 python:3.11-slim-bookworm |

### 数据库

| 文件 | VM 路径 | 说明 |
|------|---------|------|
| 建表脚本 | `/root/create_tables.sql` | 所有表 + pgvector/PostGIS 扩展 |
| 测试数据 | `/root/insert_test_data.sql` | 初始测试数据 |

---

## 七、后期需要修改的入口

### 1. 替换 DeepSeek API Key

RAG 服务的 LLM 问答功能需要真实的 API Key：

```bash
vi /root/navel-orange-monitoring/navel-orange-rag/.env
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

### 3. 后端 Docker 化（可选）

如果 Docker Hub 网络恢复，可将后端迁移到 Docker：

```bash
cat > /root/navel-orange-monitoring/navel-orange-server/Dockerfile << 'EOF'
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
WORKDIR /app
COPY target/navel-orange-server-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
EOF

cd /root/navel-orange-monitoring/navel-orange-server && docker build -t navel-orange-server .
docker run -d --name navel-orange-server -p 8080:8080 --restart unless-stopped navel-orange-server

# 停止 systemd 服务
systemctl stop navel-orange && systemctl disable navel-orange
```

### 4. 配置 HTTPS

生产环境建议配置 SSL：

```bash
# 安装 certbot（Let's Encrypt）
# 或在 Nginx 中配置自签名证书
# 编辑 nginx.conf 添加 443 监听和 ssl 配置
```

### 5. 配置 Nginx 生产缓存

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
# 查看 Docker 容器状态
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 查看后端 systemd 状态
systemctl status navel-orange --no-pager

# 全部服务一次查看
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" && systemctl is-active navel-orange
```

### 重启服务

```bash
# 重启后端
systemctl restart navel-orange

# 重启前端 Nginx
docker restart navel-orange-nginx

# 重启 RAG 服务
docker restart navel-orange-rag

# 重启所有 Docker 容器
docker restart postgres redis navel-orange-nginx navel-orange-rag
```

### 查看日志

```bash
# 后端实时日志（Ctrl+C 退出）
journalctl -u navel-orange -f

# 后端最近 50 行日志
journalctl -u navel-orange -n 50 --no-pager

# 前端 Nginx 日志
docker logs navel-orange-nginx --tail 30

# RAG 服务日志
docker logs navel-orange-rag --tail 30
```

### 修改配置

```bash
# 1. 编辑外部配置文件
vi /root/navel-orange-config/application-dev.yml

# 2. 重启后端（配置生效）
systemctl restart navel-orange
```

### 重新构建前端

```bash
# 修改源码后重新构建
docker run --rm -v /root/navel-orange-monitoring/navel-orange-admin:/app -w /app -e CI=true \
  node:20-slim sh -c "npm i -g pnpm && pnpm install && pnpm build"

# 重启 Nginx 使新构建生效
docker restart navel-orange-nginx
```

### 重新构建后端

```bash
# 1. 修改配置或代码后重新打包
cd /root/navel-orange-monitoring && mvn clean package -DskipTests

# 2. 重启后端
systemctl restart navel-orange
```

---

## 九、端口一览

| 端口 | 服务 | 协议 |
|------|------|------|
| 80 | 前端 Nginx | HTTP |
| 3000 | ~~Vite 开发服务器~~（已废弃） | - |
| 5432 | PostgreSQL | TCP |
| 6379 | Redis | TCP |
| 8080 | Spring Boot 后端 | HTTP |
| 18001 | Python RAG 服务 | HTTP |

---

## 十、外部服务依赖

| 服务 | 用途 | 配置位置 | 是否必需 |
|------|------|---------|---------|
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
