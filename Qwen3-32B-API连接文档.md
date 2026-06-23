# Qwen3-32B 模型 API 连接文档

## 1. 环境概览

| 项目 | 值 |
|------|-----|
| **模型** | Qwen3-32B（资源组: 000） |
| **部署名称** | `qwen3-32b-user` |
| **API 地址** | `https://10.168.89.186:5443/open/router/v1/chat/completions` |
| **模型列表地址** | `https://10.168.89.186:5443/open/router/v1/models` |
| **API Key** | `ME-sk-ZtsDePRbkExKhLnv7I+DtQ==-ot85ZPQLS1h37vz7kXLZgHlnHVB7WlcI` |
| **推理框架** | MindIE 2.1.RC1（华为昇腾 910B NPU） |
| **协议** | OpenAI API 兼容格式 |

> ⚠️ 注意：API 使用自签名 HTTPS 证书，所有请求需跳过证书验证（`-k` 参数）。

---

## 2. 快速测试（命令行）

### 2.1 检查服务是否在线

```bash
curl -sk \
  -H "Authorization: Bearer ME-sk-ZtsDePRbkExKhLnv7I+DtQ==-ot85ZPQLS1h37vz7kXLZgHlnHVB7WlcI" \
  "https://10.168.89.186:5443/open/router/v1/models"
```

**预期返回：**

```json
{"dataType":"list","data":[{"id":"qwen3-32b-user","type":"chat","object":"model"}]}
```

### 2.2 发送聊天请求

```bash
curl -sk --max-time 60 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ME-sk-ZtsDePRbkExKhLnv7I+DtQ==-ot85ZPQLS1h37vz7kXLZgHlnHVB7WlcI" \
  -d '{
    "model": "qwen3-32b-user",
    "messages": [
      {"role": "system", "content": "你是一个有用的助手。"},
      {"role": "user", "content": "你好，请介绍一下自己。"}
    ],
    "max_tokens": 500,
    "temperature": 0.7,
    "stream": false
  }' \
  "https://10.168.89.186:5443/open/router/v1/chat/completions"
```

> ⚠️ **重要：** 必须显式设置 `"stream": false`，否则请求可能返回 400 Bad Request。

**成功返回示例：**

```json
{
  "id": "endpoint_common_xxx",
  "object": "chat.completion",
  "created": 1782197155,
  "model": "qwen3-32b-user",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "你好！我是Qwen3-32B模型..."
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 15,
    "completion_tokens": 50,
    "total_tokens": 65
  }
}
```

---

## 3. Python 调用示例

```python
import requests
import json

API_URL = "https://10.168.89.186:5443/open/router/v1/chat/completions"
API_KEY = "ME-sk-ZtsDePRbkExKhLnv7I+DtQ==-ot85ZPQLS1h37vz7kXLZgHlnHVB7WlcI"

headers = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {API_KEY}"
}

payload = {
    "model": "qwen3-32b-user",
    "messages": [
        {"role": "system", "content": "你是一个有用的助手。"},
        {"role": "user", "content": "请用三句话介绍华为昇腾AI生态。"}
    ],
    "max_tokens": 500,
    "temperature": 0.7,
    "stream": False  # 必须显式关闭流式
}

response = requests.post(
    API_URL,
    headers=headers,
    json=payload,
    verify=False,       # 自签名证书，跳过验证
    timeout=60
)

if response.status_code == 200:
    result = response.json()
    print(result["choices"][0]["message"]["content"])
    print(f"\n--- Token 使用情况 ---")
    print(f"输入: {result['usage']['prompt_tokens']} tokens")
    print(f"输出: {result['usage']['completion_tokens']} tokens")
    print(f"总计: {result['usage']['total_tokens']} tokens")
else:
    print(f"请求失败: {response.status_code}")
    print(response.text)
```

> ⚠️ Python 中 `verify=False` 会触发 `InsecureRequestWarning`，可用以下方式抑制：
> ```python
> import urllib3
> urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
> ```

---

## 4. 关键参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `model` | string | 是 | 模型部署名称，固定为 `qwen3-32b-user` |
| `messages` | array | 是 | 对话消息列表，支持 `system` / `user` / `assistant` 角色 |
| `max_tokens` | int | 否 | 最大生成 token 数（默认值依服务端配置） |
| `temperature` | float | 否 | 采样温度，范围 0~2，默认 1.0 |
| `top_p` | float | 否 | 核采样参数，默认 1.0 |
| `stream` | bool | **建议显式设置** | 是否流式返回，**必须设为 `false`** 否则可能报 400 |

---

## 5. 常见问题

### 5.1 返回 `400 Bad Request`

**可能原因：** 未设置 `"stream": false`，或 `model` 名称不正确。

**解决方法：**
- 确认 `model` 参数值为 `qwen3-32b-user`（不是 `Qwen3-32B`）
- 显式添加 `"stream": false`

### 5.2 返回 `Model is not available or does not exist`

**原因：** `model` 字段使用了错误的名称。

**解决方法：** 使用 `qwen3-32b-user`。可通过查询模型列表接口确认：
```bash
curl -sk -H "Authorization: Bearer <API_KEY>" \
  "https://10.168.89.186:5443/open/router/v1/models"
```

### 5.3 SSL 证书错误

**原因：** 服务使用自签名证书。

**解决方法：**
- curl：加 `-k` 参数
- Python requests：加 `verify=False`
- 或将该证书添加到系统信任链中

---

## 6. 连接测试记录

测试时间：2026-06-23

| 测试项 | 结果 |
|--------|------|
| HTTPS 可达（端口 5443） | ✅ 成功 |
| 模型列表查询 | ✅ 成功 |
| 聊天补全（非流式） | ✅ 成功 |
| 响应格式 | ✅ OpenAI 兼容 |

---

*文档版本: v1.0*
