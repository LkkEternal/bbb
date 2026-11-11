# ==================== 构建阶段 ====================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制父pom和子模块pom
COPY pom.xml .
COPY navel-orange-common/pom.xml ./navel-orange-common/
COPY navel-orange-server/pom.xml ./navel-orange-server/

# 下载依赖（缓存优化）
RUN mvn dependency:go-offline -B

# 复制源码
COPY navel-orange-common ./navel-orange-common
COPY navel-orange-server ./navel-orange-server

# 构建jar包（跳过测试）
RUN mvn clean package -DskipTests -pl navel-orange-server -am

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre

WORKDIR /app

# 安装必要工具
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# 复制jar包
COPY --from=builder /app/navel-orange-server/target/navel-orange-server-*.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
