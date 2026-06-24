"""文档处理服务 —— PDF/Word/TXT 解析、分块、去重、向量化"""

from __future__ import annotations

import hashlib
import logging
import os
import tempfile
from pathlib import Path
from typing import Any

import aiofiles
import httpx
import pdfplumber
from docx import Document as DocxDocument
from numpy import ndarray

from app.config import settings
from app.models import ChunkResult, ProcessDocumentResponse
from app.service.chunker import chunk_text
from app.service.embedding_service import embedding_service

logger = logging.getLogger(__name__)


class DocumentProcessingError(Exception):
    """文档处理失败"""
    pass


async def _download_file(file_url: str) -> str:
    """从 URL 下载文件到临时目录，返回本地路径"""
    tmp_dir = Path(settings.temp_dir)
    tmp_dir.mkdir(parents=True, exist_ok=True)

    suffix = ".tmp"
    if "pdf" in file_url.lower():
        suffix = ".pdf"
    elif "docx" in file_url.lower():
        suffix = ".docx"

    fd, local_path = tempfile.mkstemp(suffix=suffix, dir=str(tmp_dir))
    os.close(fd)

    async with httpx.AsyncClient(timeout=120, follow_redirects=True) as client:
        resp = await client.get(file_url)
        resp.raise_for_status()
        async with aiofiles.open(local_path, "wb") as f:
            await f.write(resp.content)

    logger.info("Downloaded %s -> %s (%d bytes)", file_url, local_path, len(resp.content))
    return local_path


def _extract_text(file_path: str, file_type: str) -> str:
    """提取文本"""
    path = Path(file_path)
    if not path.exists():
        raise DocumentProcessingError(f"File not found: {file_path}")

    if file_type == "pdf":
        return _extract_pdf(path)
    elif file_type == "docx":
        return _extract_docx(path)
    elif file_type == "txt":
        return path.read_text(encoding="utf-8", errors="ignore")
    else:
        raise DocumentProcessingError(f"Unsupported file type: {file_type}")


def _extract_pdf(path: Path) -> str:
    """PDF 文本提取（保留布局）"""
    text_parts: list[str] = []
    with pdfplumber.open(str(path)) as pdf:
        for page in pdf.pages:
            page_text = page.extract_text()
            if page_text:
                text_parts.append(page_text)
    result = "\n".join(text_parts)
    if not result.strip():
        raise DocumentProcessingError("PDF text extraction returned empty content")
    return result


def _extract_docx(path: Path) -> str:
    """Word 文本提取"""
    doc = DocxDocument(str(path))
    text_parts = []
    for para in doc.paragraphs:
        if para.text.strip():
            text_parts.append(para.text.strip())
    result = "\n".join(text_parts)
    if not result.strip():
        raise DocumentProcessingError("DOCX text extraction returned empty content")
    return result


def _domain_keyword_check(text: str) -> bool:
    """领域关键词验证：至少命中 N 个关键词"""
    text_lower = text.lower()
    hits = sum(1 for kw in settings.domain_keywords if kw.lower() in text_lower)
    if hits < settings.domain_keyword_min_hits:
        logger.warning("Domain keyword check failed: only %d hits (need %d)", hits, settings.domain_keyword_min_hits)
        return False
    logger.info("Domain keyword check passed: %d hits", hits)
    return True


def _compute_hash(content: str) -> str:
    return hashlib.sha256(content.encode("utf-8")).hexdigest()


def _deduplicate_chunks(chunks: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """
    两层去重：
    1. SHA256 精确去重
    2. Embedding 余弦相似度 > 0.95 语义去重
    """
    # 第一层：精确去重
    seen_hashes: set[str] = set()
    unique: list[dict[str, Any]] = []
    for c in chunks:
        h = _compute_hash(c["content"])
        if h not in seen_hashes:
            seen_hashes.add(h)
            unique.append(c)

    # 第二层：语义去重（量少时做，量太大跳过）
    if len(unique) <= 200:
        texts = [c["content"] for c in unique]
        embs = embedding_service.embed_batch(texts)
        keep_indices = _semantic_dedup(embs, threshold=0.95)
        unique = [unique[i] for i in keep_indices]

    return unique


def _semantic_dedup(embeddings: list[list[float]], threshold: float = 0.95) -> list[int]:
    """语义去重：保留第一个，移除与其相似度 > threshold 的后续"""
    import numpy as np
    embs = np.array(embeddings)
    keep = [0]
    for i in range(1, len(embs)):
        sims = np.dot(embs, embs[i])
        if np.max(sims[keep]) < threshold:
            keep.append(i)
    return keep


async def process_document(file_url: str, file_type: str) -> ProcessDocumentResponse:
    """
    完整的文档处理流水线：
    下载 → 文本提取 → 关键词验证 → 分块 → 去重 + 向量化（一次 embedding） → 返回
    """
    logger.info("Processing document: %s (type=%s)", file_url, file_type)

    # 1. 下载
    local_path = await _download_file(file_url)

    try:
        # 2. 文本提取
        text = _extract_text(local_path, file_type)
        logger.info("Extracted %d characters", len(text))

        # 3. 领域关键词验证
        if not _domain_keyword_check(text):
            raise DocumentProcessingError("Content not relevant to domain (keyword check failed)")

        # 4. 分块
        raw_chunks = chunk_text(
            text,
            chunk_size=settings.chunk_size_sentences,
            overlap=settings.chunk_overlap_sentences,
        )
        logger.info("Generated %d raw chunks", len(raw_chunks))

        # 5. SHA256 精确去重
        seen_hashes: set[str] = set()
        unique_chunks: list[dict] = []
        for c in raw_chunks:
            h = hashlib.sha256(c["content"].encode("utf-8")).hexdigest()
            if h not in seen_hashes:
                seen_hashes.add(h)
                unique_chunks.append(c)
        logger.info("After SHA256 dedup: %d chunks", len(unique_chunks))

        # 6. 批量向量化（一次 embedding，同时用于去重和最终结果）
        texts = [c["content"] for c in unique_chunks]
        embeddings = embedding_service.embed_batch(texts)
        logger.info("Generated %d embeddings", len(embeddings))

        # 7. 语义去重（≤200 块才做，直接用已算好的 embedding）
        if len(unique_chunks) <= 200:
            deduped_indices = _semantic_dedup(embeddings, threshold=0.95)
        else:
            deduped_indices = list(range(len(unique_chunks)))
        deduped_chunks = [unique_chunks[i] for i in deduped_indices]
        deduped_embeddings = [embeddings[i] for i in deduped_indices]
        logger.info("After semantic dedup: %d chunks", len(deduped_chunks))

        # 8. 组装结果
        chunk_results = [
            ChunkResult(
                content=c["content"],
                chunk_index=c["index"],
                embedding=emb.tolist() if isinstance(emb, ndarray) else emb,
                token_count=len(c["content"]),
                metadata=c.get("metadata", {}),
            )
            for c, emb in zip(deduped_chunks, deduped_embeddings)
        ]

        return ProcessDocumentResponse(
            chunks=chunk_results,
            total_chunks=len(chunk_results),
            model=settings.embedding_model_name,
        )

    finally:
        # 清理临时文件
        try:
            Path(local_path).unlink(missing_ok=True)
        except Exception:
            pass
