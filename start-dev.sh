#!/bin/bash
# 脐橙监测系统 - 开发环境启动脚本 (Linux/Mac)

set -e

echo "============================================"
echo " 脐橙病虫害监测系统 - 开发环境启动"
echo "============================================"
echo ""

# 获取项目根目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ── 启动 Python RAG 服务 ──
echo "[1/3] 检查 Python RAG 服务状态 ..."
if curl -sf http://localhost:8001/health > /dev/null 2>&1; then
    echo "  ✅ Python RAG 服务已在运行"
else
    echo "  ⏳ 启动 Python RAG 服务 ..."
    cd "$SCRIPT_DIR/navel-orange-rag"
    uv run uvicorn app.main:app --host 127.0.0.1 --port 8001 --no-reload &
    RAG_PID=$!
    echo "  ⏳ 等待服务就绪 (PID=$RAG_PID) ..."
    for i in $(seq 1 30); do
        if curl -sf http://localhost:8001/health > /dev/null 2>&1; then
            echo "  ✅ Python RAG 服务已就绪"
            break
        fi
        sleep 2
    done
    cd "$SCRIPT_DIR"
fi

# ── 启动 Spring Boot ──
echo ""
echo "[2/3] 启动 Spring Boot 后端 ..."
echo ""
mvn spring-boot:run -pl navel-orange-server -am

# ── 清理 ──
echo ""
echo "[3/3] 服务已停止。"
if [ -n "$RAG_PID" ]; then
    read -p "是否关闭 Python RAG 服务？(Y/n): " STOP_RAG
    if [ "$STOP_RAG" != "n" ]; then
        kill $RAG_PID 2>/dev/null || true
        echo "  ✅ Python RAG 服务已关闭"
    fi
fi
