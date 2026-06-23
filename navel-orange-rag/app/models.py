"""Pydantic 请求/响应模型"""

from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field


# ── Embed ────────────────────────────────────────────────

class EmbedRequest(BaseModel):
    texts: list[str] = Field(min_length=1, max_length=32)


class EmbedResponse(BaseModel):
    embeddings: list[list[float]]
    dim: int
    model: str


# ── Chat ─────────────────────────────────────────────────

class ChatMessage(BaseModel):
    role: str = Field(pattern=r"^(system|user|assistant)$")
    content: str


class ChatRequest(BaseModel):
    messages: list[ChatMessage] = Field(min_length=1)
    model: str | None = None  # None = use settings.llm_default_model
    stream: bool = False
    temperature: float | None = None
    max_tokens: int | None = None
    response_format: dict | None = None  # {"type": "json_object"}


class ChatResponse(BaseModel):
    content: str
    model: str
    usage: dict[str, int] | None = None


# ── Document Processing ──────────────────────────────────

class ProcessDocumentRequest(BaseModel):
    file_url: str = Field(max_length=2048)
    file_type: str = Field(pattern=r"^(pdf|docx|txt)$")
    document_id: int | None = None


class ChunkResult(BaseModel):
    content: str
    chunk_index: int
    embedding: list[float]
    token_count: int
    metadata: dict[str, Any] = {}


class ProcessDocumentResponse(BaseModel):
    document_id: int | None = None
    chunks: list[ChunkResult]
    total_chunks: int
    model: str


# ── Health ───────────────────────────────────────────────

class HealthResponse(BaseModel):
    status: str  # "ok" | "degraded" | "error"
    embedding_model: str
    embedding_loaded: bool
    llm_deepseek_configured: bool
    llm_qwen_local_configured: bool
    device: str
