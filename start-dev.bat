@echo off
chcp 65001 >nul
title 脐橙监测系统 - 开发启动

echo ============================================
echo  脐橙病虫害监测系统 - 开发环境启动
echo ============================================
echo.

REM ── 检查 Python RAG 服务是否已在运行 ──
echo [1/3] 检查 Python RAG 服务状态 ...
curl -s http://localhost:18001/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   ✅ Python RAG 服务已在运行
    set "RAG_NEED_START=0"
) else (
    echo   ⏳ Python RAG 服务未启动，正在启动 ...
    set "RAG_NEED_START=1"
)

REM ── 启动 Python RAG 服务 ──
if "%RAG_NEED_START%"=="1" (
    start "RAG服务" cmd /c "cd /d %~dp0navel-orange-rag && uv run uvicorn app.main:app --host 127.0.0.1 --port 8001 --no-reload"
    echo   ⏳ 等待 Python RAG 服务就绪 ...
    :wait_rag
    timeout /t 2 /nobreak >nul
    curl -s http://localhost:8001/health >nul 2>&1
    if %ERRORLEVEL% NEQ 0 goto wait_rag
    echo   ✅ Python RAG 服务已就绪
)

echo.
echo [2/3] 启动 Spring Boot 后端 ...
echo.
mvn spring-boot:run -pl navel-orange-server -am

echo.
echo [3/3] 服务已停止。
echo.

REM ── Spring Boot 停止后，询问是否关闭 RAG ──
set /p "STOP_RAG=是否同时关闭 Python RAG 服务？(Y/n): "
if /i "%STOP_RAG%" NEQ "n" (
    echo 正在关闭 Python RAG 服务 ...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8001 ^| findstr LISTENING') do (
        taskkill /f /pid %%a >nul 2>&1
    )
    echo ✅ Python RAG 服务已关闭
)

echo.
pause
