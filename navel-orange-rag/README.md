# navel-orange-rag

脐橙病虫害监测系统 - Python RAG 智能服务。

## 架构

```
┌──────────────────────────────────────────────────────┐
│  FastAPI 服务 (端口 8001)                             │
│                                                      │
│  /embed          → bge-small-zh 文本向量化           │
│  /chat           → LLM 对话（DeepSeek API / Qwen）   │
│  /chat/stream    → 流式 SSE 对话                     │
│  /process-document → 文档处理流水线                   │
│  /health         → 健康检查                           │
└──────────────────────────────────────────────────────┘
```

## 快速开始

```bash
# 1. 复制配置
cp .env.example .env
# 编辑 .env，填入 DEEPSEEK_API_KEY

# 2. 安装依赖
pip install uv
uv sync

# 3. 启动
uv run uvicorn app.main:app --reload --port 8001

# 4. 测试
curl http://localhost:8001/health
```

## API 一览

### POST /embed
```json
{"texts": ["溃疡病防治方法"]}
→ {"embeddings": [[...]], "dim": 512, "model": "BAAI/bge-small-zh-v1.5"}
```

### POST /chat
```json
{"messages": [{"role": "user", "content": "溃疡病怎么治？"}], "model": "deepseek"}
→ {"content": "...", "model": "deepseek/deepseek-chat", "usage": {...}}
```

### POST /process-document
```json
{"file_url": "https://oss/xxx.pdf", "file_type": "pdf"}
→ {"chunks": [...], "total_chunks": 45, "model": "BAAI/bge-small-zh-v1.5"}
```

## 模型切换

- `model: "deepseek"` → 调用 DeepSeek API
- `model: "qwen-local"` → 调用本地 Qwen3-32B（需先部署 vLLM）
- 不设 model → 使用 `LLM_DEFAULT_MODEL` 配置
- **自动降级**：Primary 失败自动切到另一个 provider
