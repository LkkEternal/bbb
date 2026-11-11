# ── Runtime ──────────────────────────────────────────────
FROM python:3.11-slim

WORKDIR /app

# 直接使用项目已有的 requirements.txt，不装系统依赖（避免网络问题）
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 复制源码
COPY . .

EXPOSE 8001

# 启动时自动加载模型
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
