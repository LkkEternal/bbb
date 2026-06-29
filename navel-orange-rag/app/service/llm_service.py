"""LLM 服务 —— DeepSeek API + 本地 Qwen3-32B 双模切换"""

from __future__ import annotations

import json
import logging
import time
from typing import Any

import httpx
from openai import APIError, OpenAI, AsyncOpenAI

from app.config import settings

logger = logging.getLogger(__name__)


class LLMService:
    """统一的 LLM 调用层，支持两个 provider 无缝切换"""

    DEEPSEEK = "deepseek"
    QWEN_LOCAL = "qwen-local"

    # ── 初始化 ──────────────────────────────────────────

    def __init__(self) -> None:
        self._deepseek_client: OpenAI | None = None
        self._qwen_local_client: OpenAI | None = None
        self._async_deepseek: AsyncOpenAI | None = None
        self._async_qwen_local: AsyncOpenAI | None = None

    def _get_deepseek(self) -> OpenAI:
        if self._deepseek_client is None:
            if not settings.deepseek_api_key:
                raise RuntimeError("DeepSeek API key not configured (set DEEPSEEK_API_KEY)")
            self._deepseek_client = OpenAI(
                api_key=settings.deepseek_api_key,
                base_url=settings.deepseek_base_url,
                timeout=httpx.Timeout(settings.deepseek_timeout, connect=10),
                max_retries=0,  # 不重试，避免网络不通时卡死事件循环
            )
        return self._deepseek_client

    def _get_qwen_local(self) -> OpenAI:
        if self._qwen_local_client is None:
            # 自签名证书，跳过 SSL 验证
            ssl_ctx = httpx.create_ssl_context()
            ssl_ctx.check_hostname = False
            ssl_ctx.verify_mode = False
            self._qwen_local_client = OpenAI(
                api_key=settings.qwen_local_api_key or "not-needed",
                base_url=settings.qwen_local_base_url,
                timeout=httpx.Timeout(settings.qwen_local_timeout, connect=10),
                max_retries=0,
                http_client=httpx.Client(verify=ssl_ctx),
            )
        return self._qwen_local_client

    # ── 核心方法 ──────────────────────────────────────────

    @property
    def default_model(self) -> str:
        return settings.llm_default_model

    @property
    def deepseek_configured(self) -> bool:
        return bool(settings.deepseek_api_key)

    @property
    def qwen_local_reachable(self) -> bool:
        """检查本地 Qwen 服务是否可达（轻量探测）"""
        try:
            client = self._get_qwen_local()
            client.models.list(timeout=3)
            return True
        except Exception:
            return False

    def chat(
        self,
        messages: list[dict[str, str]],
        model: str | None = None,
        stream: bool = False,
        temperature: float | None = None,
        max_tokens: int | None = None,
        response_format: dict | None = None,
    ) -> tuple[str, dict[str, int], str]:
        """
        统一的 chat 入口。

        返回: (content, usage_dict, actual_model_name)
        - content: 生成文本
        - usage: {"prompt_tokens":..., "completion_tokens":...}
        - actual_model: 实际使用的模型标识
        """
        provider = model or settings.llm_default_model
        temperature = temperature if temperature is not None else settings.llm_temperature
        max_tokens = max_tokens if max_tokens is not None else settings.llm_max_tokens

        if provider == self.DEEPSEEK:
            return self._call_deepseek(messages, stream, temperature, max_tokens, response_format)
        elif provider == self.QWEN_LOCAL:
            return self._call_qwen_local(messages, stream, temperature, max_tokens, response_format)
        else:
            logger.warning("Unknown LLM provider '%s', falling back to default", provider)
            return self._call_deepseek(messages, stream, temperature, max_tokens, response_format)

    def chat_with_fallback(
        self,
        messages: list[dict[str, str]],
        model: str | None = None,
        **kwargs: Any,
    ) -> tuple[str, dict[str, int], str]:
        """
        带降级的 chat: DeepSeek 失败 → 自动切换到本地 Qwen3-32B。

        返回同 .chat()
        """
        provider = model or settings.llm_default_model

        # 第一次尝试
        try:
            return self.chat(messages, model=provider, **kwargs)
        except Exception as exc:
            logger.warning("LLM primary call failed (%s: %s), trying fallback ...", type(exc).__name__, exc)

        # 降级：如果 primary 是 DeepSeek，切 Qwen；反之亦然
        fallback = self.QWEN_LOCAL if provider == self.DEEPSEEK else self.DEEPSEEK
        try:
            logger.info("Falling back to %s", fallback)
            return self.chat(messages, model=fallback, **kwargs)
        except Exception as exc:
            logger.error("Fallback LLM also failed: %s", exc)
            raise RuntimeError("All LLM providers unavailable") from exc

    async def chat_stream_async(self, messages: list[dict], model: str | None = None,
                                temperature: float | None = None, max_tokens: int | None = None):
        """异步流式生成，逐个 yield token（不阻塞事件循环）"""
        provider = model or settings.llm_default_model
        temp = temperature if temperature is not None else settings.llm_temperature
        mt = max_tokens if max_tokens is not None else settings.llm_max_tokens

        if provider == self.DEEPSEEK:
            client = await self._aget_deepseek()
            model_name = settings.deepseek_model
        else:
            client = await self._aget_qwen_local()
            model_name = settings.qwen_local_model

        stream = await client.chat.completions.create(
            model=model_name,
            messages=messages,
            stream=True,
            temperature=temp,
            max_tokens=mt,
        )
        async for chunk in stream:
            delta = chunk.choices[0].delta if chunk.choices else None
            if delta and delta.content:
                yield delta.content

    async def _aget_deepseek(self) -> AsyncOpenAI:
        if self._async_deepseek is None:
            if not settings.deepseek_api_key:
                raise RuntimeError("DeepSeek API key not configured")
            self._async_deepseek = AsyncOpenAI(
                api_key=settings.deepseek_api_key,
                base_url=settings.deepseek_base_url,
                timeout=httpx.Timeout(settings.deepseek_timeout, connect=10),
                max_retries=0,
            )
        return self._async_deepseek

    async def _aget_qwen_local(self) -> AsyncOpenAI:
        if self._async_qwen_local is None:
            import httpx as _httpx
            ssl_ctx = _httpx.create_ssl_context()
            ssl_ctx.check_hostname = False
            ssl_ctx.verify_mode = False
            self._async_qwen_local = AsyncOpenAI(
                api_key="not-needed",
                base_url=settings.qwen_local_base_url,
                timeout=_httpx.Timeout(settings.qwen_local_timeout, connect=10),
                max_retries=0,
                http_client=_httpx.AsyncClient(verify=ssl_ctx),
            )
        return self._async_qwen_local

    # ── 内部 Provider 调用 ──────────────────────────────

    def _call_deepseek(
        self,
        messages: list[dict[str, str]],
        stream: bool,
        temperature: float,
        max_tokens: int,
        response_format: dict | None,
    ) -> tuple[str, dict[str, int], str]:
        client = self._get_deepseek()
        model_name = settings.deepseek_model
        kwargs = self._build_kwargs(stream, temperature, max_tokens, response_format)

        t0 = time.time()
        resp = client.chat.completions.create(
            model=model_name,
            messages=messages,
            **kwargs,
        )
        elapsed = time.time() - t0
        logger.info("DeepSeek LLM call: %s tokens in %.2fs", resp.usage.total_tokens if resp.usage else "?", elapsed)

        return (
            resp.choices[0].message.content or "",
            {"prompt_tokens": resp.usage.prompt_tokens if resp.usage else 0,
             "completion_tokens": resp.usage.completion_tokens if resp.usage else 0},
            f"deepseek/{model_name}",
        )

    def _call_qwen_local(
        self,
        messages: list[dict[str, str]],
        stream: bool,
        temperature: float,
        max_tokens: int,
        response_format: dict | None,
    ) -> tuple[str, dict[str, int], str]:
        client = self._get_qwen_local()
        model_name = settings.qwen_local_model
        kwargs = self._build_kwargs(stream, temperature, max_tokens, response_format)

        t0 = time.time()
        resp = client.chat.completions.create(
            model=model_name,
            messages=messages,
            **kwargs,
        )
        elapsed = time.time() - t0
        logger.info("Qwen-local LLM call: %s tokens in %.2fs", resp.usage.total_tokens if resp.usage else "?", elapsed)

        return (
            resp.choices[0].message.content or "",
            {"prompt_tokens": resp.usage.prompt_tokens if resp.usage else 0,
             "completion_tokens": resp.usage.completion_tokens if resp.usage else 0},
            f"qwen-local/{model_name}",
        )

    # ── 工具 ──────────────────────────────────────────────

    @staticmethod
    def _build_kwargs(
        stream: bool,
        temperature: float,
        max_tokens: int,
        response_format: dict | None,
    ) -> dict:
        kwargs: dict = {
            "stream": stream,
            "temperature": temperature,
            "max_tokens": max_tokens,
            "top_p": settings.llm_top_p,
        }
        if response_format:
            kwargs["response_format"] = response_format
        return kwargs


# 全局单例
llm_service = LLMService()
