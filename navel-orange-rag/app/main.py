"""FastAPI 主入口"""

from __future__ import annotations

import logging

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.router import embed, chat, document, health, classify
from app.service.embedding_service import embedding_service

# ── 日志 ────────────────────────────────────────────────
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)

# ── FastAPI ─────────────────────────────────────────────
app = FastAPI(
    title="脐橙 RAG 服务",
    description="脐橙病虫害监测系统 - RAG 智能服务（Embedding / LLM / 文档处理）",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── 注册路由 ────────────────────────────────────────────
app.include_router(health.router)
app.include_router(embed.router)
app.include_router(chat.router)
app.include_router(document.router)
app.include_router(classify.router)


# ── 生命周期 ─────────────────────────────────────────────

@app.on_event("startup")
async def startup():
    """启动时预加载 embedding 模型"""
    logger.info("Pre-loading embedding model ...")
    embedding_service.load()
    logger.info("Service started. Embedding dim=%d, device=%s",
                embedding_service.dim, embedding_service.device)


@app.on_event("shutdown")
async def shutdown():
    """释放资源"""
    logger.info("Shutting down, releasing model ...")
    embedding_service.unload()


# ── 启动入口 ────────────────────────────────────────────

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        log_level=settings.log_level,
        reload=False,
    )
