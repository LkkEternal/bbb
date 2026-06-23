"""智能文本分块 —— 句子边界优先 + 领域保护词"""

from __future__ import annotations

import re
from typing import Any

# 领域保护词：包含这些词的句子不会被切断
PROTECTED_PATTERNS: list[re.Pattern] = [
    re.compile(r"柑橘黄单胞杆菌"),
    re.compile(r"[-/a-zA-Z\u4e00-\u9fff]{4,}(?:菌|病|虫|螨|剂)"),  # 长名词+专业后缀
    re.compile(r"[A-Z][a-z]+(?:\s[A-Z][a-z]+)+\s(?:bacteria|virus|fungus)", re.IGNORECASE),
]


def _has_protected(text: str) -> bool:
    return any(p.search(text) for p in PROTECTED_PATTERNS)


def _split_sentences(text: str) -> list[str]:
    """按中文/英文句号、感叹号、问号、换行分割句子，并保留分隔符"""
    # 处理换行：连续换行视为段落分隔
    parts = re.split(r"(\n\s*\n)", text)
    raw_sentences: list[str] = []
    for p in parts:
        if p.strip():
            raw_sentences.append(p.strip())

    # 再按句子边界拆分（不拆分有保护词的句子）
    sentences: list[str] = []
    for part in raw_sentences:
        # 中英文句号、感叹号、问号、分号
        chunks = re.split(r"(?<=[。！？；.!?])\s*", part)
        for c in chunks:
            stripped = c.strip()
            if stripped:
                sentences.append(stripped)
    return sentences if sentences else [text.strip()]


def chunk_text(
    text: str,
    chunk_size: int = 3,
    overlap: int = 1,
    metadata: dict[str, Any] | None = None,
) -> list[dict[str, Any]]:
    """
    智能分块。

    Args:
        text: 输入文本
        chunk_size: 每个 chunk 包含的句子数
        overlap: 相邻 chunk 重叠的句子数
        metadata: 附加到每个 chunk 的元数据

    Returns:
        [{"content": str, "index": int, "metadata": dict}, ...]
    """
    sentences = _split_sentences(text)
    if not sentences:
        return []

    # 保护词合并：如果某个句子含保护词，且下一个句子也含保护词，合并
    merged = []
    buffer = ""
    for s in sentences:
        if _has_protected(s) and buffer:
            buffer += " " + s
        else:
            if buffer:
                merged.append(buffer)
            buffer = s
    if buffer:
        merged.append(buffer)
    sentences = merged

    # 滑动窗口分块
    chunks: list[dict[str, Any]] = []
    step = max(chunk_size - overlap, 1)
    for i in range(0, len(sentences), step):
        chunk_sentences = sentences[i: i + chunk_size]
        if not chunk_sentences:
            break
        content = "".join(chunk_sentences)
        chunk_meta = dict(metadata or {})
        chunk_meta["start_sentence"] = i
        chunk_meta["end_sentence"] = i + len(chunk_sentences)
        chunks.append({
            "content": content,
            "index": len(chunks),
            "metadata": chunk_meta,
        })

    return chunks
