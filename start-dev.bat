@echo off
chcp 65001 >nul
title 脐橙监测系统 - 本地启动（后端）
setlocal
cd /d "%~dp0"

echo ============================================
echo  脐橙病虫害监测系统 - 本地后端启动
echo ============================================
echo.

set "PG_HOME=D:\PostgreSQL\18"
set "PG_DATA=D:\PostgreSQL\data"
set "PG_LOG=D:\PostgreSQL\logs\server.log"
set "PATH=%PG_HOME%\bin;%PATH%"

if not exist "%PG_HOME%\bin\pg_ctl.exe" (
    echo [错误] 未找到 PostgreSQL：%PG_HOME%
    echo 请确认 PostgreSQL 已解压到该目录，或修改本脚本中的 PG_HOME。
    pause
    exit /b 1
)

"%PG_HOME%\bin\pg_isready.exe" -h localhost -p 5432 -U postgres >nul 2>&1
if errorlevel 1 (
    echo 正在启动 PostgreSQL ...
    if not exist "%PG_DATA%\PG_VERSION" (
        echo [错误] 未找到数据库目录：%PG_DATA%
        pause
        exit /b 1
    )
    "%PG_HOME%\bin\pg_ctl.exe" -D "%PG_DATA%" -l "%PG_LOG%" start
    "%SystemRoot%\System32\timeout.exe" /t 3 /nobreak >nul
)

echo 数据库：localhost:5432 / navel_orange / postgres

set "REDIS_HOME=D:\redis工具"
set "REDIS_EXE=%REDIS_HOME%\redis-server.exe"
netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
if errorlevel 1 (
    if exist "%REDIS_EXE%" (
        echo 正在启动 Redis ...
        pushd "%REDIS_HOME%"
        start "Redis" /min "%REDIS_EXE%" "%REDIS_HOME%\redis.windows.conf"
        popd
        "%SystemRoot%\System32\timeout.exe" /t 2 /nobreak >nul
    ) else (
        echo [警告] 未找到 Redis：%REDIS_EXE%
        echo 请手动启动 Redis，否则登录/缓存功能可能不可用。
    )
) else (
    echo Redis：localhost:6379 已运行
)
echo.

set "MAVEN_CMD=mvn"
where mvn >nul 2>&1
if errorlevel 1 (
    if exist "D:\maven\apache-maven-3.6.3\bin\mvn.cmd" (
        set "MAVEN_CMD=D:\maven\apache-maven-3.6.3\bin\mvn.cmd"
    ) else (
        echo [错误] 未找到 Maven，请安装 Maven 或修改本脚本中的 MAVEN_CMD 路径。
        pause
        exit /b 1
    )
)

echo 正在启动 Spring Boot 后端 ...
echo 前端请另外执行：cd navel-orange-admin ^&^& pnpm dev
echo.

call "%MAVEN_CMD%" -q -DskipTests install -pl navel-orange-common
if errorlevel 1 (
    echo [错误] navel-orange-common 构建失败。
    pause
    exit /b 1
)

call "%MAVEN_CMD%" -q -f navel-orange-server\pom.xml -DskipTests spring-boot:run

echo.
echo 后端服务已停止。
pause
