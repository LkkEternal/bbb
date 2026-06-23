"""
Qwen3-32B 问答演示脚本
=======================
连接华为 MindIE 推理服务，支持单轮问答、多轮对话、流式输出三种模式。

用法:
  python qwen3_demo.py                  # 交互式多轮对话（默认）
  python qwen3_demo.py -m single       # 单次问答
  python qwen3_demo.py -m single -q "你好"  # 直接指定问题
  python qwen3_demo.py -m stream -q "你好"  # 流式输出
  python qwen3_demo.py -m batch         # 批量预置问题
  python qwen3_demo.py -m batch -f questions.txt   # 从文件读取问题
"""

import argparse
import json
import os
import sys
import urllib3
from typing import Optional

import requests

# ──────────────────────────── 配置（按需修改） ────────────────────────────
API_URL = os.environ.get(
    "QWEN_API_URL",
    "https://10.168.89.186:5443/open/router/v1/chat/completions",
)
API_KEY = os.environ.get(
    "QWEN_API_KEY",
    "ME-sk-ZtsDePRbkExKhLnv7I+DtQ==-ot85ZPQLS1h37vz7kXLZgHlnHVB7WlcI",
)
MODEL = os.environ.get("QWEN_MODEL", "qwen3-32b-user")
TIMEOUT = 120

# 抑制自签名证书警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# ──────────────────────────── 核心函数 ────────────────────────────


def call_qwen(
    messages: list,
    *,
    temperature: float = 0.7,
    max_tokens: int = 1024,
    top_p: float = 0.9,
    stream: bool = False,
) -> Optional[requests.Response]:
    """调用 Qwen3-32B 模型，成功返回 Response，失败返回 None。"""
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}",
    }
    payload = {
        "model": MODEL,
        "messages": messages,
        "temperature": temperature,
        "max_tokens": max_tokens,
        "top_p": top_p,
        "stream": stream,
    }

    try:
        resp = requests.post(
            API_URL,
            headers=headers,
            json=payload,
            verify=False,
            timeout=TIMEOUT,
            stream=stream,
        )
        resp.raise_for_status()
        return resp
    except requests.exceptions.Timeout:
        print("  [超时] 请求超时，请稍后重试。")
    except requests.exceptions.HTTPError as e:
        print(f"  [HTTP错误] {e.response.status_code}: {e.response.text}")
    except requests.exceptions.ConnectionError:
        print("  [连接失败] 请检查网络和服务状态。")
    except Exception as e:
        print(f"  [错误] {e}")
    return None


def print_usage(usage: dict):
    """打印 token 用量。"""
    if usage:
        print(f"\n  -- Token: prompt={usage.get('prompt_tokens','?')}"
              f" | completion={usage.get('completion_tokens','?')}"
              f" | total={usage.get('total_tokens','?')}")


def banner(title: str):
    """打印模式标题。"""
    width = 56
    print("=" * width)
    print(f"  Qwen3-32B 问答演示 -- {title}")
    print("=" * width)
    print(f"  模型: {MODEL}")
    print(f"  地址: {API_URL}")
    print("-" * width)


# ──────────────────────────── 模式实现 ────────────────────────────


def mode_single(question: str = None):
    """单次问答模式。"""
    banner("单次问答")

    if not question:
        question = input("[用户] 请输入你的问题: ").strip()
        if not question:
            print("已取消。")
            return

    messages = [
        {"role": "system", "content": "你是一个有帮助、知识渊博的助手。回答简洁准确。"},
        {"role": "user", "content": question},
    ]

    print(f"[用户] {question}")
    print("[模型] ", end="", flush=True)
    resp = call_qwen(messages)
    if resp is None:
        return

    result = resp.json()
    answer = result["choices"][0]["message"]["content"]
    print(answer)
    print_usage(result.get("usage"))


def mode_stream(question: str = None):
    """流式输出模式。"""
    banner("流式输出")

    if not question:
        question = input("[用户] 请输入你的问题: ").strip()
        if not question:
            print("已取消。")
            return

    messages = [
        {"role": "system", "content": "你是一个有帮助、知识渊博的助手。"},
        {"role": "user", "content": question},
    ]

    print(f"[用户] {question}")
    print("[模型] ", end="", flush=True)
    resp = call_qwen(messages, stream=True)
    if resp is None:
        return

    collected = []
    for line in resp.iter_lines(decode_unicode=True):
        if not line:
            continue
        if line.startswith("data: "):
            data_str = line[6:]
            if data_str.strip() == "[DONE]":
                break
            try:
                chunk = json.loads(data_str)
                delta = chunk.get("choices", [{}])[0].get("delta", {})
                content = delta.get("content", "")
                if content:
                    print(content, end="", flush=True)
                    collected.append(content)
            except json.JSONDecodeError:
                pass

    print()
    usage = chunk.get("usage") if "chunk" in dir() else None
    if usage:
        print_usage(usage)


def mode_interactive():
    """多轮对话模式。"""
    banner("多轮对话")
    print("  命令:")
    print("    /clear       清空对话历史")
    print("    /temp <N>    设置温度 (0.0-2.0)")
    print("    /save <file> 保存对话记录（默认对话记录会在退出时询问）")
    print("    /exit        退出")
    print("-" * 56)

    messages = [
        {"role": "system", "content": "你是一个有帮助、知识渊博的助手。回答简洁准确。"},
    ]
    temperature = 0.7
    history_log = []

    def show_prompt():
        return f"\n[用户] [temp={temperature}] "

    while True:
        try:
            user_input = input(show_prompt()).strip()
        except (EOFError, KeyboardInterrupt):
            print()
            break

        if not user_input:
            continue

        if user_input == "/exit":
            break
        if user_input == "/clear":
            messages = messages[:1]
            print("  [OK] 对话历史已清空")
            continue
        if user_input.startswith("/temp"):
            parts = user_input.split()
            if len(parts) == 2:
                try:
                    temperature = max(0.0, min(2.0, float(parts[1])))
                    print(f"  [OK] 温度已设为 {temperature}")
                except ValueError:
                    print("  [用法] /temp <0.0-2.0>")
            continue
        if user_input.startswith("/save"):
            parts = user_input.split(maxsplit=1)
            filename = parts[1] if len(parts) > 1 else "chat_history.json"
            try:
                with open(filename, "w", encoding="utf-8") as f:
                    json.dump(history_log, f, ensure_ascii=False, indent=2)
                print(f"  [OK] 对话已保存至 {filename}")
            except Exception as e:
                print(f"  [错误] 保存失败: {e}")
            continue

        # 添加用户消息
        messages.append({"role": "user", "content": user_input})
        history_log.append({"role": "user", "content": user_input})

        print("[模型] 思考中...", end="", flush=True)
        resp = call_qwen(messages, temperature=temperature)
        if resp is None:
            messages.pop()
            history_log.pop()
            continue

        result = resp.json()
        answer = result["choices"][0]["message"]["content"]
        print(f"\r[模型] {answer}")
        print_usage(result.get("usage"))

        messages.append({"role": "assistant", "content": answer})
        history_log.append({"role": "assistant", "content": answer})

    print("\n再见！")
    if history_log:
        save = input("是否保存对话记录？(y/N): ").strip().lower()
        if save == "y":
            filename = "chat_history.json"
            with open(filename, "w", encoding="utf-8") as f:
                json.dump(history_log, f, ensure_ascii=False, indent=2)
            print(f"已保存至 {filename}")


def mode_batch(questions: list = None):
    """批量问答模式。"""
    banner("批量问答")

    if not questions:
        questions = [
            "请用一句话介绍大语言模型。",
            "华为昇腾910芯片的主要特点是什么？",
            "解释一下什么是思维链（Chain-of-Thought）提示。",
            "Python中列表和元组的区别是什么？",
        ]

    print(f"  共 {len(questions)} 个问题\n")

    results = []
    for i, q in enumerate(questions, 1):
        print(f"[{i}/{len(questions)}] [Q] {q}")

        messages = [
            {"role": "system", "content": "你是一个有帮助的助手。回答简洁。"},
            {"role": "user", "content": q},
        ]

        print("  [模型] ", end="", flush=True)
        resp = call_qwen(messages)
        if resp is None:
            print()
            continue

        result = resp.json()
        answer = result["choices"][0]["message"]["content"]
        print(answer)
        print_usage(result.get("usage"))
        print()

        results.append({"question": q, "answer": answer})

    # 保存结果
    output_file = "batch_results.json"
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"[完成] 批量问答结果已保存至 {output_file}")


# ──────────────────────────── 入口 ────────────────────────────


def main():
    parser = argparse.ArgumentParser(
        description="Qwen3-32B 问答演示脚本",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument(
        "--mode", "-m",
        choices=["chat", "single", "stream", "batch"],
        default="chat",
        help="运行模式 (默认: chat)",
    )
    parser.add_argument(
        "--question", "-q",
        help="直接指定问题（仅 single/stream 模式）",
    )
    parser.add_argument(
        "--file", "-f",
        help="从文件读取问题列表（每行一个，仅 batch 模式）",
    )

    args = parser.parse_args()

    try:
        if args.mode == "single":
            mode_single(args.question)
        elif args.mode == "stream":
            mode_stream(args.question)
        elif args.mode == "batch":
            questions = None
            if args.file:
                with open(args.file, "r", encoding="utf-8") as f:
                    questions = [line.strip() for line in f if line.strip()]
                if not questions:
                    print(f"[警告] 文件 {args.file} 为空，使用默认问题。")
                    questions = None
            mode_batch(questions)
        else:
            mode_interactive()
    except KeyboardInterrupt:
        print("\n[用户中断] 再见！")
        sys.exit(0)


if __name__ == "__main__":
    main()
