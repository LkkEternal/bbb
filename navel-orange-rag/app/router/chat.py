"""Chat 路由 —— LLM 对话（DeepSeek API + 本地 Qwen3-32B）"""

import json
import time

from fastapi import APIRouter, HTTPException
from sse_starlette.sse import EventSourceResponse

from app.models import ChatRequest, ChatResponse
from app.service.llm_service import llm_service

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
    """LLM 对话（流式 SSE），支持双模型切换"""
    messages = [m.model_dump() for m in req.messages]
    model = req.model or llm_service.default_model
    temperature = req.temperature
    max_tokens = req.max_tokens
    response_format = req.response_format

    async def event_generator():
        try:
            from openai import Stream

            # 选择 provider
            if model == "deepseek":
                client = llm_service._get_deepseek()
                model_name = llm_service._deepseek_model
            else:
                client = llm_service._get_qwen_local()
                model_name = llm_service._qwen_local_model

            stream: Stream = client.chat.completions.create(
                model=model_name,
                messages=messages,
                stream=True,
                temperature=temperature or 0.3,
                max_tokens=max_tokens or 2048,
            )

            for chunk in stream:
                delta = chunk.choices[0].delta if chunk.choices else None
                if delta and delta.content:
                    yield {"event": "token", "data": json.dumps({"content": delta.content})}

            yield {"event": "done", "data": "{}"}
        except Exception as e:
            yield {"event": "error", "data": json.dumps({"detail": str(e)})}

    return EventSourceResponse(event_generator())
