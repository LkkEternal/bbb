"""Embedding 模型服务 —— 加载 bge-small-zh，提供文本向量化"""

from __future__ import annotations

import logging
from functools import lru_cache

import numpy as np
import torch
from sentence_transformers import SentenceTransformer

from app.config import settings

logger = logging.getLogger(__name__)


class EmbeddingService:
    """线程安全的 embedding 服务，模型全局单例"""

    def __init__(self) -> None:
        self._model: SentenceTransformer | None = None
        self._device: str = "cpu"

    # ── 生命周期 ──────────────────────────────────────────

    def load(self) -> None:
        """首次加载模型（生产环境会在启动时 warm-up）"""
        if self._model is not None:
            return

        device = self._resolve_device()
        logger.info("Loading embedding model %s on %s ...", settings.embedding_model_name, device)

        self._model = SentenceTransformer(
            settings.embedding_model_name,
            device=device,
            trust_remote_code=True,
        )
        self._device = device
        logger.info("Embedding model loaded.  Max tokens: %s, Dim: %s",
                     self._model.get_max_seq_length(), self._model.get_sentence_embedding_dimension())

    def unload(self) -> None:
        """释放 GPU 显存"""
        if self._model is not None:
            self._model = None
            torch.cuda.empty_cache()
            logger.info("Embedding model unloaded.")

    @property
    def is_loaded(self) -> bool:
        return self._model is not None

    @property
    def device(self) -> str:
        return self._device

    # ── 核心方法 ──────────────────────────────────────────

    @lru_cache(maxsize=4096)
    def _cached_encode(self, text: str) -> np.ndarray:
        """对纯文本做 LRU 缓存，避免重复计算"""
        emb = self._model.encode(text, normalize_embeddings=True)
        return emb  # np.ndarray

    def embed(self, text: str) -> list[float]:
        """单条文本 → 归一化向量"""
        if self._model is None:
            self.load()
        emb = self._cached_encode(text)
        return emb.tolist()

    def embed_batch(self, texts: list[str]) -> list[list[float]]:
        """批量文本 → 归一化向量列表，不经过 LRU 缓存（直接批量推理更快）"""
        if self._model is None:
            self.load()
        embs = self._model.encode(texts, normalize_embeddings=True, show_progress_bar=False)
        return [e.tolist() for e in embs]

    def similarity(self, a: list[float], b: list[float]) -> float:
        """计算两个归一化向量的余弦相似度（点积）"""
        return float(np.dot(a, b))

    # ── 内部 ──────────────────────────────────────────────

    @staticmethod
    def _resolve_device() -> str:
        if settings.embedding_device == "cuda":
            return "cuda" if torch.cuda.is_available() else "cpu"
        if settings.embedding_device == "auto":
            return "cuda" if torch.cuda.is_available() else "cpu"
        return "cpu"

    @property
    def model_name(self) -> str:
        return settings.embedding_model_name

    @property
    def dim(self) -> int:
        return settings.embedding_dim


# 全局单例
embedding_service = EmbeddingService()
