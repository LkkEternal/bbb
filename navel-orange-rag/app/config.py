"""RAG 服务全局配置"""

from __future__ import annotations

from pathlib import Path
from typing import Literal

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    # ---------- 服务基础 ----------
    host: str = "0.0.0.0"
    port: int = 8001
    log_level: str = "info"

    # ---------- Embedding ----------
    embedding_model_name: str = "BAAI/bge-small-zh-v1.5"
    embedding_device: Literal["cpu", "cuda", "auto"] = "auto"
    embedding_dim: int = 512

    # ---------- LLM 路由 ----------
    # 默认模型: "deepseek" | "qwen-local"
    llm_default_model: str = "deepseek"

    # DeepSeek API
    deepseek_api_key: str = ""
    deepseek_base_url: str = "https://api.deepseek.com/v1"
    deepseek_model: str = "deepseek-chat"
    deepseek_timeout: int = 60

    # 本地 Qwen3-32B (vLLM / Ollama 兼容 OpenAI 接口)
    qwen_local_base_url: str = "http://localhost:8000/v1"
    qwen_local_model: str = "Qwen3-32B"
    qwen_local_timeout: int = 120

    # ---------- LLM 生成参数 ----------
    llm_max_tokens: int = 2048
    llm_temperature: float = 0.3
    llm_top_p: float = 0.9

    # ---------- 文档处理 ----------
    temp_dir: str = "/tmp/navel-orange-rag"
    chunk_size_sentences: int = 3
    chunk_overlap_sentences: int = 1
    domain_keywords: list[str] = [
        "脐橙", "柑橘", "橙子", "柑桔",
        "溃疡病", "炭疽病", "黄龙病", "疮痂病", "煤污病",
        "红蜘蛛", "木虱", "潜叶蛾", "蚜虫", "蓟马",
        "黄单胞杆菌", "真菌", "细菌", "病毒",
        "波尔多液", "噻菌铜", "代森锰锌", "石硫合剂",
        "春梢", "夏梢", "秋梢", "果实膨大",
        "物候期", "节气", "惊蛰", "芒种",
    ]
    domain_keyword_min_hits: int = 2

    # ---------- RAG 检索 ----------
    similarity_min_score: float = 0.60
    similarity_fuzzy_min: float = 0.75
    retrieval_top_k: int = 10
    retrieval_final_k: int = 5

    # ---------- 缓存 ----------
    semantic_cache_ttl: int = 3600  # 1 hour
    semantic_cache_threshold: float = 0.95


settings = Settings()
