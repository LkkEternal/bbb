"""Chat 路由 —— LLM 对话（DeepSeek API + 本地 Qwen3-32B）"""

import json
import logging

from fastapi import APIRouter, HTTPException
from sse_starlette.sse import EventSourceResponse

from app.models import ChatRequest, ChatResponse
from app.service.llm_service import llm_service

logger = logging.getLogger(__name__)
router = APIRouter(tags=["chat"])


@router.post("/chat", response_model=ChatResponse)
async def chat(req: ChatRequest):
    """LLM 对话（非流式），支持双模型切换和自动降级"""
    messages = [m.model_dump() for m in req.messages]
    model = req.model or llm_service.default_model

    try:
        content, usage, actual_model = llm_service.chat_with_fallback(
            messages,
            model=model,
            temperature=req.temperature,
            max_tokens=req.max_tokens,
            response_format=req.response_format,
        )
        return ChatResponse(content=content, model=actual_model, usage=usage)
    except RuntimeError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/chat/stream")
async def chat_stream(req: ChatRequest):
    """LLM 对话（流式 SSE），尝试异步流式 → 降级到同步非流式"""
    messages = [m.model_dump() for m in req.messages]
    model = req.model or llm_service.default_model

    async def event_generator():
        # 方案 A：尝试异步流式
        try:
            gen = llm_service.chat_stream_async(
                messages, model=model,
                temperature=req.temperature,
                max_tokens=req.max_tokens,
            )
            async for token in gen:
                yield {"event": "token", "data": json.dumps({"content": token})}
            yield {"event": "done", "data": "{}"}
            return
        except Exception as e:
            logger.warning("Async streaming failed (%s), falling back to sync", e)

        # 方案 B：同步非流式（通过 asyncio.to_thread 不阻塞事件循环）
        try:
            import asyncio

            def _sync():
                c, _, m = llm_service.chat_with_fallback(
                    messages, model=model,
                    temperature=req.temperature,
                    max_tokens=req.max_tokens,
                )
                return c, m

            content, actual_model = await asyncio.to_thread(_sync)
            yield {"event": "token", "data": json.dumps({"content": content})}
            yield {"event": "done", "data": "{}"}
        except Exception as e:
            yield {"event": "error", "data": json.dumps({"detail": str(e)})}

    return EventSourceResponse(event_generator())
