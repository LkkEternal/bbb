"""
ZhiPu AI <-> Anthropic API Proxy
=================================
将 Anthropic API 格式 (Claude Code 使用) 翻译为智谱 OpenAI 兼容格式，
使局域网内其他用户通过 Claude Code 直接调用智谱服务。

用法:
  1. pip install fastapi uvicorn httpx
  2. 修改下方 CONFIG 区域的配置
  3. python zhipu_proxy.py

客户端配置 (Claude Code settings.json):
  {
    "env": {
      "ANTHROPIC_BASE_URL": "http://<你的IP>:23456",
      "ANTHROPIC_API_KEY": "<ACCESS_TOKEN>"
    }
  }
"""

import json
import uuid
import sys
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import StreamingResponse, JSONResponse
import httpx

# ======================== CONFIG ========================
REAL_ZHIPU_KEY = "c999d0deba524ca69e460f95cba6cf17.qBdz6RWRP4KJpbjG"  # 你的真实智谱 API Key
ACCESS_TOKEN   = "mypassword"     # 局域网用户的访问密码 (客户端填为 ANTHROPIC_API_KEY)
PORT           = 23456            # 服务端口
HOST           = "0.0.0.0"        # 监听地址

# 模型映射: Claude 模型名 → 智谱模型名（国内免费版）
MODEL_MAP = {
    "claude-opus-4-7":             "glm-4-flash",
    "claude-opus-4-20250918":      "glm-4-flash",
    "claude-sonnet-4-20250514":    "glm-4-flash",
    "claude-sonnet-4-6":           "glm-4-flash",
    "claude-4-sonnet":             "glm-4-flash",
    "claude-haiku-4-5-20251001":   "glm-4-flash",
    "claude-3-5-sonnet-20241022":  "glm-4-flash",
    "claude-3-5-haiku-20241022":   "glm-4-flash",
    "claude-3-opus-20240229":      "glm-4-flash",
}
DEFAULT_MODEL = "glm-4-flash"

ZHIPU_API_BASE = "https://open.bigmodel.cn/api/paas/v4"
# ========================================================

app = FastAPI()
http_client = httpx.AsyncClient(timeout=httpx.Timeout(300.0, connect=30.0))

STOP_REASON = {
    "stop": "end_turn",
    "length": "max_tokens",
    "tool_calls": "tool_use",
    "content_filter": "end_turn",
}


# ─── Auth ───────────────────────────────────────────────

def verify_token(request: Request):
    key = request.headers.get("x-api-key", "").strip()
    if key and key == ACCESS_TOKEN:
        return
    auth = request.headers.get("authorization", "").strip()
    if auth.startswith("Bearer ") and auth[7:].strip() == ACCESS_TOKEN:
        return
    raise HTTPException(
        status_code=401,
        detail={"type": "error", "error": {
            "type": "authentication_error",
            "message": "Invalid API key",
        }},
    )


# ─── Request Translation: Anthropic → OpenAI ───────────

def translate_messages(messages: list) -> list:
    """将 Anthropic 消息格式转为 OpenAI 格式。
    Anthropic 的 tool_result 嵌在 user content 数组里，
    OpenAI 用独立的 role=tool 消息，所以一条可能拆成多条。"""
    out = []

    for msg in messages:
        role = msg["role"]
        content = msg["content"]

        if role == "user":
            if isinstance(content, str):
                out.append({"role": "user", "content": content})
                continue

            texts = []
            for block in content:
                if not isinstance(block, dict):
                    continue
                bt = block.get("type", "")
                if bt == "text":
                    texts.append(block.get("text", ""))
                elif bt == "tool_result":
                    tc = block.get("content", "")
                    if isinstance(tc, list):
                        tc = "\n".join(
                            b.get("text", str(b))
                            for b in tc if isinstance(b, dict)
                        )
                    out.append({
                        "role": "tool",
                        "tool_call_id": block.get("tool_use_id", ""),
                        "content": str(tc) if tc else "",
                    })
                elif bt == "image":
                    src = block.get("source", {})
                    if src.get("type") == "base64":
                        out.append({"role": "user", "content": [{
                            "type": "image_url",
                            "image_url": {"url": f"data:{src.get('media_type','image/png')};base64,{src.get('data','')}"},
                        }]})
            if texts:
                out.append({"role": "user", "content": "\n".join(texts)})

        elif role == "assistant":
            if isinstance(content, str):
                out.append({"role": "assistant", "content": content})
                continue

            texts, tools = [], []
            for block in content:
                if not isinstance(block, dict):
                    continue
                bt = block.get("type", "")
                if bt == "text":
                    texts.append(block.get("text", ""))
                elif bt == "tool_use":
                    tools.append({
                        "id": block.get("id", f"call_{uuid.uuid4().hex[:24]}"),
                        "type": "function",
                        "function": {
                            "name": block.get("name", ""),
                            "arguments": json.dumps(block.get("input", {}), ensure_ascii=False),
                        },
                    })
            m = {"role": "assistant", "content": "\n".join(texts) if texts else None}
            if tools:
                m["tool_calls"] = tools
            out.append(m)

    return out


def translate_tools(tools: list) -> list:
    """Anthropic tool 定义 → OpenAI function calling 格式"""
    return [
        {
            "type": "function",
            "function": {
                "name": t["name"],
                "description": t.get("description", ""),
                "parameters": t.get("input_schema", {}),
            },
        }
        for t in tools
    ]


def build_openai_request(body: dict) -> dict:
    """将 Anthropic /v1/messages 请求体完整转为 OpenAI 格式"""
    messages = []

    # system 提示词
    system = body.get("system")
    if system:
        if isinstance(system, str):
            messages.append({"role": "system", "content": system})
        elif isinstance(system, list):
            text = "\n".join(
                b.get("text", "") for b in system
                if isinstance(b, dict) and b.get("type") == "text"
            )
            if text:
                messages.append({"role": "system", "content": text})

    messages.extend(translate_messages(body.get("messages", [])))

    req = {
        "model": MODEL_MAP.get(body.get("model", ""), DEFAULT_MODEL),
        "messages": messages,
        "stream": body.get("stream", False),
    }

    for key in ("max_tokens", "temperature", "top_p"):
        if key in body:
            req[key] = body[key]
    if "stop_sequences" in body:
        req["stop"] = body["stop_sequences"]

    if "tools" in body:
        req["tools"] = translate_tools(body["tools"])
        if "tool_choice" in body:
            tc = body["tool_choice"]
            if tc == "auto" or (isinstance(tc, dict) and tc.get("type") == "auto"):
                req["tool_choice"] = "auto"
            elif isinstance(tc, dict) and tc.get("type") == "any":
                req["tool_choice"] = "required"
            elif isinstance(tc, dict) and tc.get("type") == "tool":
                req["tool_choice"] = {"type": "function", "function": {"name": tc.get("name", "")}}

    return req


# ─── Response Translation: OpenAI → Anthropic ──────────

def build_anthropic_response(resp: dict, model: str) -> dict:
    """非流式: OpenAI 响应 → Anthropic 响应"""
    choice = resp.get("choices", [{}])[0]
    message = choice.get("message", {})
    content = []

    text = message.get("content")
    if text:
        content.append({"type": "text", "text": text})

    for tc in message.get("tool_calls", []):
        try:
            inp = json.loads(tc["function"].get("arguments", "{}"))
        except (json.JSONDecodeError, KeyError):
            inp = {}
        content.append({
            "type": "tool_use",
            "id": tc["id"],
            "name": tc["function"]["name"],
            "input": inp,
        })

    usage = resp.get("usage", {})
    return {
        "id": f"msg_{uuid.uuid4().hex[:24]}",
        "type": "message",
        "role": "assistant",
        "content": content,
        "model": model,
        "stop_reason": STOP_REASON.get(choice.get("finish_reason", "stop"), "end_turn"),
        "stop_sequence": None,
        "usage": {
            "input_tokens": usage.get("prompt_tokens", 0),
            "output_tokens": usage.get("completion_tokens", 0),
        },
    }


# ─── Streaming Translation ─────────────────────────────

def sse(event: str, data: dict) -> str:
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"


async def translate_stream(response, model: str):
    """将 OpenAI SSE 流逐块翻译为 Anthropic SSE 流"""
    msg_id = f"msg_{uuid.uuid4().hex[:24]}"
    started = False
    next_block_index = 0       # 实际 content block 计数器
    text_block_index = None    # 当前 text block 的 index（如果有）
    tool_block_map = {}        # OpenAI tool index → Anthropic block index

    def emit_start():
        return sse("message_start", {
            "type": "message_start",
            "message": {
                "id": msg_id, "type": "message", "role": "assistant",
                "content": [], "model": model,
                "stop_reason": None, "stop_sequence": None,
                "usage": {"input_tokens": 0, "output_tokens": 0},
            },
        })

    async for line in response.aiter_lines():
        line = line.strip()
        if not line or not line.startswith("data: "):
            continue
        if line == "data: [DONE]":
            break

        try:
            chunk = json.loads(line[6:])
        except json.JSONDecodeError:
            continue

        choices = chunk.get("choices", [])
        if not choices:
            continue
        delta = choices[0].get("delta", {})
        finish = choices[0].get("finish_reason")

        # ── Tool calls ──
        if "tool_calls" in delta:
            # 关闭已打开的 text block
            if text_block_index is not None:
                yield sse("content_block_stop", {"type": "content_block_stop", "index": text_block_index})
                text_block_index = None
            if not started:
                yield emit_start()
                yield sse("ping", {"type": "ping"})
                started = True

            for tc in delta["tool_calls"]:
                idx = tc.get("index", 0)
                func = tc.get("function", {})
                tc_id = tc.get("id")

                if tc_id:
                    # 新 tool call 开始
                    if idx in tool_block_map:
                        old_bi = tool_block_map.pop(idx)
                        yield sse("content_block_stop", {"type": "content_block_stop", "index": old_bi})

                    bi = next_block_index
                    next_block_index += 1
                    tool_block_map[idx] = bi

                    yield sse("content_block_start", {
                        "type": "content_block_start",
                        "index": bi,
                        "content_block": {
                            "type": "tool_use",
                            "id": tc_id,
                            "name": func.get("name", ""),
                            "input": {},
                        },
                    })

                    # 智谱模型可能把 arguments 和 id 放在同一个 chunk 里
                    args_first = func.get("arguments", "")
                    if args_first:
                        yield sse("content_block_delta", {
                            "type": "content_block_delta",
                            "index": bi,
                            "delta": {"type": "input_json_delta", "partial_json": args_first},
                        })
                else:
                    # tool call 参数增量
                    args_delta = func.get("arguments", "")
                    if args_delta and idx in tool_block_map:
                        bi = tool_block_map[idx]
                        yield sse("content_block_delta", {
                            "type": "content_block_delta",
                            "index": bi,
                            "delta": {"type": "input_json_delta", "partial_json": args_delta},
                        })
            continue

        # ── Text content ──
        text = delta.get("content")
        if text:
            if not started:
                yield emit_start()
                text_block_index = next_block_index
                next_block_index += 1
                yield sse("content_block_start", {
                    "type": "content_block_start", "index": text_block_index,
                    "content_block": {"type": "text", "text": ""},
                })
                yield sse("ping", {"type": "ping"})
                started = True

            yield sse("content_block_delta", {
                "type": "content_block_delta", "index": text_block_index,
                "delta": {"type": "text_delta", "text": text},
            })

        # ── Finish ──
        if finish:
            if text_block_index is not None:
                yield sse("content_block_stop", {"type": "content_block_stop", "index": text_block_index})
            for idx in sorted(tool_block_map):
                yield sse("content_block_stop", {"type": "content_block_stop", "index": tool_block_map[idx]})

            yield sse("message_delta", {
                "type": "message_delta",
                "delta": {"stop_reason": STOP_REASON.get(finish, "end_turn"), "stop_sequence": None},
                "usage": {"output_tokens": 1},
            })
            yield sse("message_stop", {"type": "message_stop"})
            return

    # 流意外结束，兜底关闭
    if text_block_index is not None:
        yield sse("content_block_stop", {"type": "content_block_stop", "index": text_block_index})
    for idx in sorted(tool_block_map):
        yield sse("content_block_stop", {"type": "content_block_stop", "index": tool_block_map[idx]})
    if started:
        yield sse("message_delta", {
            "type": "message_delta",
            "delta": {"stop_reason": "end_turn", "stop_sequence": None},
            "usage": {"output_tokens": 1},
        })
        yield sse("message_stop", {"type": "message_stop"})


# ─── Routes ────────────────────────────────────────────

@app.post("/v1/messages")
async def handle_messages(request: Request):
    verify_token(request)

    try:
        body = await request.json()
    except Exception:
        raise HTTPException(status_code=400, detail={"type": "error", "error": {
            "type": "invalid_request_error", "message": "Invalid JSON body",
        }})

    model = body.get("model", "claude-sonnet-4-20250514")
    openai_req = build_openai_request(body)

    headers = {
        "Authorization": f"Bearer {REAL_ZHIPU_KEY}",
        "Content-Type": "application/json",
    }
    url = f"{ZHIPU_API_BASE}/chat/completions"

    try:
        if openai_req.get("stream"):
            req = http_client.build_request("POST", url, json=openai_req, headers=headers)
            resp = await http_client.send(req, stream=True)

            if resp.status_code != 200:
                err = await resp.aread()
                await resp.aclose()
                return JSONResponse(status_code=resp.status_code, content={
                    "type": "error", "error": {
                        "type": "api_error",
                        "message": f"Upstream error: {err.decode(errors='replace')}",
                    },
                })

            async def generate():
                try:
                    async for chunk in translate_stream(resp, model):
                        yield chunk
                finally:
                    await resp.aclose()

            return StreamingResponse(
                generate(),
                media_type="text/event-stream",
                headers={"cache-control": "no-cache", "connection": "keep-alive"},
            )
        else:
            resp = await http_client.post(url, json=openai_req, headers=headers)
            if resp.status_code != 200:
                return JSONResponse(status_code=resp.status_code, content={
                    "type": "error", "error": {
                        "type": "api_error",
                        "message": f"Upstream error: {resp.text}",
                    },
                })
            return JSONResponse(content=build_anthropic_response(resp.json(), model))

    except httpx.ConnectError:
        raise HTTPException(status_code=502, detail={"type": "error", "error": {
            "type": "api_error", "message": "Cannot connect to ZhiPu API",
        }})


@app.get("/v1/models")
async def handle_models(request: Request):
    verify_token(request)
    return JSONResponse({"object": "list", "data": [
        {"id": k, "object": "model", "owned_by": "zhipu"} for k in MODEL_MAP
    ]})


@app.api_route("/{path:path}", methods=["GET", "HEAD", "OPTIONS"])
async def catch_all(request: Request):
    return JSONResponse({"status": "ok"})


# ─── Entry ─────────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn

    print()
    print("  ZhiPu AI <-> Anthropic API Proxy")
    print(f"  Listening:  http://{HOST}:{PORT}")
    print(f"  Token:      {ACCESS_TOKEN}")
    print()
    print("  Client config (Claude Code settings.json):")
    print(f'    "ANTHROPIC_BASE_URL": "http://<your-ip>:{PORT}"')
    print(f'    "ANTHROPIC_API_KEY":  "{ACCESS_TOKEN}"')
    print()

    uvicorn.run(app, host=HOST, port=PORT)
