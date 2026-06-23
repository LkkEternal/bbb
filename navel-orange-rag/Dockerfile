# ── Build ────────────────────────────────────────────────
FROM python:3.11-slim AS builder

WORKDIR /app
COPY pyproject.toml .

RUN pip install --no-cache-dir uv && \
    uv export --no-dev --format requirements-txt > requirements.txt

# ── Runtime ──────────────────────────────────────────────
FROM python:3.11-slim

WORKDIR /app

# 安装系统依赖（PDF 解析需要）
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libgl1-mesa-glx \
        libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8001

# 启动时自动加载模型
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
