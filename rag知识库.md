# RAG 知识库检索 + LLM 智能建议生成 — 实现方案

## 一、当前状态

### 已实现
- YOLO 表情检测（MTCNN + YOLOv8-cls，8类表情）→ Python 服务完成
- Java 后端接收检测结果 → 返回给前端

### 当前建议来源（静态字典）

```
DetectionServiceImpl 第102-110行：
EmotionDict dict = emotionDictMapper.selectByLabel(emotionLabel);
adviceText = dict.getDefaultAdvice();   // 从MySQL emotion_dict表查静态文本
adviceSourceType = "dict";              // 来源标记为"字典型"
```

→ 纯数据库静态查询，无智能生成。

### 需要实现
- FAISS 向量检索 + LLM 总结 → `adviceSourceType = "llm"`

---

## 二、知识库来源

### 文档来源
- **来源**：中国知网（CNKI）学术文献
- **格式**：PDF 文件
- **数量**：5-15 篇综合心理学期刊论文
- **主题**：涵盖多种情绪的心理学研究（非按情绪分类）

### 知识库构建流水线

```
知网 PDF 文档（5-15篇）
    ↓
[第0步] PDF 解析（pdfplumber）
    ↓
文本清洗（去页眉页脚/参考文献/致谢/英文摘要）
    ↓
中文句感知分块（每块 200-400 字）
    ↓
关键词情绪分类（自动标注 emotion 标签）
    ↓
Embedding 向量化（BAAI/bge-small-zh-v1.5）
    ↓
FAISS 索引构建
```

### 情绪关键词分类规则

```python
EMOTION_KEYWORDS = {
    "sad":     ["悲伤", "抑郁", "失落", "哀伤", "沮丧", "痛苦", "绝望", "无助"],
    "angry":   ["愤怒", "生气", "暴怒", "恼怒", "敌意", "攻击", "不满"],
    "fear":    ["恐惧", "焦虑", "害怕", "惊恐", "不安", "担忧", "紧张"],
    "disgust": ["厌恶", "排斥", "反感", "抵触", "恶心"],
    "surprise":["惊讶", "震惊", "意外", "出乎意料", "意想不到"],
    "neutral": ["平静", "放松", "满足", "稳定", "安宁"]
}
```

分类逻辑：统计每个 chunk 中各类关键词出现频次，取最高频者作为该条目的 emotion 标签。

---

## 三、整体架构

```
用户上传图片
    ↓
[Java后端] 上传图片 → 调用 Python 检测服务
    ↓
[Python] MTCNN + YOLO 检测表情 → 返回 emotionLabel + confidence
    ↓
[Java后端] 接收检测结果
    ↓ if happy → 直接返回鼓励语（emotion_dict兜底）
    ↓ if 非happy → 调用 Python /api/advice 接口
    ↓
[Python] RAG 流程：
    1. Embedding 向量化查询（BAAI/bge-small-zh-v1.5）
    2. FAISS 向量检索知识库（top_k=3）
    3. 拼接 System Prompt + 检索结果 + 用户表情信息
    4. 调用 LLM 生成建议（DeepSeek API 优先，Qwen 本地降级）
    5. 返回 advice + sources
    ↓
[Java后端] 存储建议到 detection_record，返回给前端
```

### 三级降级保证

```
LLM 智能建议（最优）
  ↓ 失败
FAISS 检索结果直接拼接（可用）
  ↓ 失败
emotion_dict 静态兜底（保底）
```

---

## 四、具体实现步骤

### 第0步：PDF 文档处理流水线（新增）

#### 0a. PDF 解析与清洗服务

**文件**：`python-service/app/service/document_parser.py`

职责：
- 使用 `pdfplumber` 解析知网 PDF
- 文本清洗规则：
  - 去除页眉页脚（页码、期刊名、年份）
  - 去除参考文献部分（"参考文献" / "References" 之后的内容）
  - 去除致谢、基金项目、作者简介
  - 去除英文摘要（"Abstract" / "Key words" 部分）
  - 去除多余空白行和特殊字符
- 返回清洗后的纯文本段落列表

```python
import pdfplumber
import re
from typing import List

class DocumentParser:
    """知网 PDF 文档解析器"""

    def parse_pdf(self, pdf_path: str) -> List[str]:
        """解析 PDF 并返回清洗后的段落列表"""
        paragraphs = []
        with pdfplumber.open(pdf_path) as pdf:
            full_text = ""
            for page in pdf.pages:
                text = page.extract_text()
                if text:
                    full_text += text + "\n"

        # 清洗
        full_text = self._clean_text(full_text)

        # 按段落分割
        paragraphs = [p.strip() for p in full_text.split('\n\n') if p.strip()]
        return paragraphs

    def _clean_text(self, text: str) -> str:
        """清洗文本：去除页眉页脚、参考文献等"""
        # 去除参考文献及之后内容
        text = re.split(r'(?:参考文献|References|致谢|ACKNOWLEDGEMENTS)', text)[0]
        # 去除英文摘要
        text = re.split(r'(?:Abstract|Key\s*words)', text, flags=re.IGNORECASE)[0]
        # 去除页码行
        text = re.sub(r'^\d+\s*$', '', text, flags=re.MULTILINE)
        # 去除多余空白
        text = re.sub(r'\n{3,}', '\n\n', text)
        return text.strip()
```

#### 0b. 中文文本分块

**文件**：`python-service/app/service/document_parser.py`（同文件，追加方法）

分块策略：
- 按段落分割 → 过短的段落合并
- 按句子分割（中文句号/问号/感叹号）
- 每块 200-400 字，保留上下文

```python
class DocumentParser:
    # ... 上面的代码 ...

    def chunk_text(self, paragraphs: List[str],
                   min_chunk_size: int = 200,
                   max_chunk_size: int = 400) -> List[dict]:
        """将段落分块，返回带元数据的 chunk 列表"""
        chunks = []
        buffer = ""

        for para in paragraphs:
            if len(buffer) + len(para) < min_chunk_size:
                buffer += para
                continue
            if len(buffer) >= min_chunk_size:
                chunks.append({"text": buffer, "length": len(buffer)})
                buffer = para

        if buffer and len(buffer) >= min_chunk_size // 2:
            chunks.append({"text": buffer, "length": len(buffer)})

        return chunks
```

#### 0c. 关键词情绪分类

**文件**：`python-service/app/service/document_parser.py`（同文件，追加方法）

```python
EMOTION_KEYWORDS = {
    "sad":     ["悲伤", "抑郁", "失落", "哀伤", "沮丧", "痛苦", "绝望", "无助"],
    "angry":   ["愤怒", "生气", "暴怒", "恼怒", "敌意", "攻击", "不满"],
    "fear":    ["恐惧", "焦虑", "害怕", "惊恐", "不安", "担忧", "紧张"],
    "disgust": ["厌恶", "排斥", "反感", "抵触", "恶心"],
    "surprise":["惊讶", "震惊", "意外", "出乎意料", "意想不到"],
    "neutral": ["平静", "放松", "满足", "稳定", "安宁"]
}

EMOTION_CN = {
    "sad": "悲伤", "angry": "生气", "fear": "恐惧",
    "disgust": "厌恶", "surprise": "惊讶", "neutral": "平静"
}

class DocumentParser:
    # ... 上面的代码 ...

    def classify_emotion(self, text: str) -> tuple:
        """根据关键词频率判断情绪类别"""
        scores = {}
        for emotion, keywords in EMOTION_KEYWORDS.items():
            scores[emotion] = sum(text.count(kw) for kw in keywords)

        if max(scores.values()) == 0:
            return "neutral", 0.5

        best = max(scores, key=scores.get)
        total = sum(scores.values())
        confidence = scores[best] / total if total > 0 else 0.5
        return best, confidence

    def process_pdf(self, pdf_path: str) -> List[dict]:
        """完整流程：解析 → 分块 → 分类"""
        paragraphs = self.parse_pdf(pdf_path)
        chunks = self.chunk_text(paragraphs)

        results = []
        for chunk in chunks:
            emotion, conf = self.classify_emotion(chunk["text"])
            results.append({
                "text": chunk["text"],
                "emotion": emotion,
                "emotion_cn": EMOTION_CN[emotion],
                "confidence": conf,
                "source_file": pdf_path
            })
        return results
```

#### 0d. 知网 PDF 转知识条目脚本

**文件**：`python-service/scripts/pdf_to_knowledge.py`

功能：
1. 扫描 `data/knowledge_raw/` 目录下所有 PDF
2. 调用 `DocumentParser.process_pdf()` 解析每个 PDF
3. 按 emotion 分组，生成 6 个 JSON 文件到 `data/knowledge/`
4. 自动提取论文标题（PDF 第一页首行）作为 source 字段

```python
"""
用法：python scripts/pdf_to_knowledge.py
输入：data/knowledge_raw/*.pdf
输出：data/knowledge/{sad,angry,fear,disgust,surprise,neutral}.json
"""
import json
from pathlib import Path
from app.service.document_parser import DocumentParser, EMOTION_CN

RAW_DIR = Path("data/knowledge_raw")
OUTPUT_DIR = Path("data/knowledge")

def extract_title(pdf_path: str) -> str:
    """提取 PDF 第一页首行作为论文标题"""
    import pdfplumber
    with pdfplumber.open(pdf_path) as pdf:
        if pdf.pages:
            text = pdf.pages[0].extract_text()
            if text:
                first_line = text.strip().split('\n')[0]
                return first_line[:80]  # 截断过长标题
    return Path(pdf_path).stem

def main():
    parser = DocumentParser()
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # 按 emotion 分组收集
    grouped = {e: [] for e in EMOTION_CN}
    item_id = {e: 0 for e in EMOTION_CN}

    for pdf_file in RAW_DIR.glob("*.pdf"):
        print(f"处理: {pdf_file.name}")
        title = extract_title(str(pdf_file))
        chunks = parser.process_pdf(str(pdf_file))

        for chunk in chunks:
            emotion = chunk["emotion"]
            item_id[emotion] += 1
            grouped[emotion].append({
                "id": item_id[emotion],
                "title": f"第{item_id[emotion]}条建议",
                "content": chunk["text"],
                "category": "学术研究",
                "source": f"《{title}》",
                "tags": [emotion, chunk["emotion_cn"]],
                "confidence": chunk["confidence"]
            })

    # 输出 JSON
    for emotion, items in grouped.items():
        output = {
            "emotion": emotion,
            "emotion_cn": EMOTION_CN[emotion],
            "description": f"来自知网学术文献的情绪相关内容",
            "suggestions": items
        }
        out_path = OUTPUT_DIR / f"{emotion}.json"
        out_path.write_text(json.dumps(output, ensure_ascii=False, indent=2))
        print(f"  {emotion}: {len(items)} 条 → {out_path}")

if __name__ == "__main__":
    main()
```

---

### 第1步：知识库索引构建

**文件**：`python-service/scripts/build_knowledge.py`（更新）

与原方案基本一致，但支持 PDF 来源的 JSON：

```python
"""
用法：python scripts/build_knowledge.py
输入：data/knowledge/*.json（可来自手写或 PDF 提取）
输出：data/vectors/knowledge.faiss + knowledge_mapping.pkl
"""
import json, faiss, pickle
import numpy as np
from sentence_transformers import SentenceTransformer
from pathlib import Path

EMBEDDING_MODEL = 'BAAI/bge-small-zh-v1.5'
KNOWLEDGE_DIR = Path('data/knowledge')
VECTOR_DIR = Path('data/vectors')

def build():
    encoder = SentenceTransformer(EMBEDDING_MODEL)
    all_items = []

    for json_file in KNOWLEDGE_DIR.glob('*.json'):
        data = json.loads(json_file.read_text(encoding='utf-8'))
        for item in data.get('suggestions', []):
            all_items.append({
                'emotion': data['emotion'],
                'emotion_cn': data['emotion_cn'],
                **item
            })

    if not all_items:
        print("错误：没有找到任何知识条目，请先运行 pdf_to_knowledge.py 或手动创建 JSON")
        return

    texts = [item['content'] for item in all_items]
    print(f"共 {len(texts)} 条知识，开始向量化...")
    embeddings = encoder.encode(texts, normalize_embeddings=True, show_progress_bar=True)

    dimension = embeddings.shape[1]
    index = faiss.IndexFlatIP(dimension)
    index.add(np.array(embeddings, dtype='float32'))

    VECTOR_DIR.mkdir(parents=True, exist_ok=True)
    faiss.write_index(index, str(VECTOR_DIR / 'knowledge.faiss'))
    with open(VECTOR_DIR / 'knowledge_mapping.pkl', 'wb') as f:
        pickle.dump(all_items, f)

    print(f"索引构建完成：{len(texts)} 条，维度 {dimension}")
    print(f"  → {VECTOR_DIR / 'knowledge.faiss'}")
    print(f"  → {VECTOR_DIR / 'knowledge_mapping.pkl'}")

if __name__ == "__main__":
    build()
```

---

### 第2步：Python 服务新增模块

#### 2a. Embedding 向量化服务

**文件**：`python-service/app/service/embedding_service.py`

```python
from sentence_transformers import SentenceTransformer
import numpy as np
from app.config import EMBEDDING_MODEL

_model = None

def get_model() -> SentenceTransformer:
    global _model
    if _model is None:
        _model = SentenceTransformer(EMBEDDING_MODEL)
    return _model

def encode(texts: list[str]) -> np.ndarray:
    model = get_model()
    return model.encode(texts, normalize_embeddings=True)
```

#### 2b. 知识库检索服务

**文件**：`python-service/app/service/retriever.py`

```python
import faiss, pickle, numpy as np
from pathlib import Path
from app.config import VECTOR_DIR, RETRIEVAL_TOP_K
from app.service.embedding_service import encode

_index = None
_mapping = None

def load():
    global _index, _mapping
    index_path = Path(VECTOR_DIR) / 'knowledge.faiss'
    mapping_path = Path(VECTOR_DIR) / 'knowledge_mapping.pkl'
    if index_path.exists() and mapping_path.exists():
        _index = faiss.read_index(str(index_path))
        with open(mapping_path, 'rb') as f:
            _mapping = pickle.load(f)
        print(f"检索器加载完成：{_index.ntotal} 条向量")
    else:
        print("警告：知识库索引不存在，检索功能不可用")

def is_ready() -> bool:
    return _index is not None and _mapping is not None

def retrieve(emotion: str, query: str = None, top_k: int = RETRIEVAL_TOP_K) -> list[dict]:
    if not is_ready():
        return []

    search_text = f"{emotion}的情绪相关建议"
    if query:
        search_text = f"{emotion} {query}"

    embedding = encode([search_text])
    scores, indices = _index.search(np.array(embedding, dtype='float32'), top_k)

    results = []
    for score, idx in zip(scores[0], indices[0]):
        if idx < len(_mapping):
            item = _mapping[idx].copy()
            item['score'] = float(score)
            results.append(item)
    return results
```

#### 2c. LLM 总结服务

**文件**：`python-service/app/service/llm_service.py`

```python
from openai import OpenAI
from app.config import (
    DEEPSEEK_API_KEY, DEEPSEEK_API_BASE, DEEPSEEK_MODEL,
    LLM_TEMPERATURE, LLM_MAX_TOKENS
)

_client = None

def get_client() -> OpenAI:
    global _client
    if _client is None:
        _client = OpenAI(api_key=DEEPSEEK_API_KEY, base_url=DEEPSEEK_API_BASE)
    return _client

def is_ready() -> bool:
    return bool(DEEPSEEK_API_KEY)

SYSTEM_PROMPT = """你是一位专业的心理健康顾问。根据以下检测到的情绪和知识库检索结果，
为用户提供简洁、专业、温暖的心理健康建议。

检测到的情绪：{emotion_cn}（置信度：{confidence}）

知识库参考内容：
{retrieval_results}

请给出一条简洁的建议（100-200字），要求：
1. 先简要说明这种情绪的正常性
2. 给出2-3条具体可操作的建议
3. 语气温暖专业，不要说教
4. 如情况严重建议寻求专业帮助"""

def generate_advice(emotion_cn: str, confidence: float,
                    retrieval_results: list[dict]) -> str | None:
    if not is_ready():
        return None

    context = "\n".join(
        f"- [{r.get('source', '未知')}] {r['content'][:150]}..."
        for r in retrieval_results
    )

    prompt = SYSTEM_PROMPT.format(
        emotion_cn=emotion_cn,
        confidence=f"{confidence:.0%}",
        retrieval_results=context or "暂无参考内容"
    )

    try:
        response = get_client().chat.completions.create(
            model=DEEPSEEK_MODEL,
            messages=[
                {"role": "system", "content": prompt},
                {"role": "user", "content": f"请为检测到{emotion_cn}情绪的用户提供心理健康建议。"}
            ],
            temperature=LLM_TEMPERATURE,
            max_tokens=LLM_MAX_TOKENS
        )
        return response.choices[0].message.content
    except Exception as e:
        print(f"LLM 调用失败: {e}")
        return None
```

#### 2d. 主流程编排

**文件**：`python-service/app/service/pipeline.py`

```python
from app.service import retriever, llm_service
from app.service.document_parser import EMOTION_CN

HAPPY_ADVICE = "看到你开心我也很开心！保持积极心态，享受当下的美好时光。继续做让自己快乐的事情吧！"

def get_advice(emotion: str, confidence: float) -> dict:
    """
    RAG 主流程：
    1. happy → 直接返回鼓励语
    2. 非 happy → 检索 → LLM 总结
    3. LLM 失败 → 检索结果直接拼接
    4. 检索也失败 → 返回 None（Java 端降级到 emotion_dict）
    """
    if emotion == "happy":
        return {
            "advice": HAPPY_ADVICE,
            "sources": ["系统内置"],
            "advice_source_type": "builtin"
        }

    emotion_cn = EMOTION_CN.get(emotion, emotion)

    # 检索
    results = retriever.retrieve(emotion)

    # LLM 总结
    llm_text = llm_service.generate_advice(emotion_cn, confidence, results)
    if llm_text:
        sources = list(set(r.get("source", "") for r in results if r.get("source")))
        return {
            "advice": llm_text,
            "sources": sources,
            "advice_source_type": "llm"
        }

    # 降级：检索结果直接拼接
    if results:
        combined = "\n".join(f"• {r['content'][:100]}" for r in results[:3])
        sources = list(set(r.get("source", "") for r in results if r.get("source")))
        return {
            "advice": combined,
            "sources": sources,
            "advice_source_type": "retrieval"
        }

    # 全部失败，返回 None，Java 端降级到 dict
    return None
```

#### 2e. 新增 API 路由

**文件**：`python-service/app/router/advice.py`

```python
from fastapi import APIRouter
from pydantic import BaseModel
from app.service.pipeline import get_advice

router = APIRouter(prefix="/api", tags=["advice"])

class AdviceRequest(BaseModel):
    emotion: str
    confidence: float

class AdviceResponse(BaseModel):
    success: bool
    advice: str | None = None
    sources: list[str] = []
    advice_source_type: str = "dict"

@router.post("/advice", response_model=AdviceResponse)
async def post_advice(req: AdviceRequest):
    result = get_advice(req.emotion, req.confidence)
    if result is None:
        return AdviceResponse(success=False)
    return AdviceResponse(success=True, **result)
```

---

### 第3步：修改 Python 服务入口

**文件**：`python-service/app/main.py`

```python
# 在现有 main.py 中增加：
from app.router.advice import router as advice_router
from app.service import retriever, llm_service
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    # ... 现有代码 ...
    # 新增：加载检索器和 LLM
    retriever.load()
    llm_service.get_client()  # 预热
    yield

# 注册路由
app.include_router(advice_router)
```

---

### 第4步：修改配置文件

**文件**：`python-service/app/config.py`

```python
# 新增配置项
KNOWLEDGE_DIR = os.getenv('KNOWLEDGE_DIR', str(PROJECT_ROOT / 'data' / 'knowledge'))
RAW_DIR = os.getenv('RAW_DIR', str(PROJECT_ROOT / 'data' / 'knowledge_raw'))
VECTOR_DIR = os.getenv('VECTOR_DIR', str(PROJECT_ROOT / 'data' / 'vectors'))

EMBEDDING_MODEL = os.getenv('EMBEDDING_MODEL', 'BAAI/bge-small-zh-v1.5')

DEFAULT_LLM_PROVIDER = os.getenv('DEFAULT_LLM_PROVIDER', 'deepseek')
DEEPSEEK_API_KEY = os.getenv('DEEPSEEK_API_KEY', '')
DEEPSEEK_API_BASE = os.getenv('DEEPSEEK_API_BASE', 'https://api.deepseek.com')
DEEPSEEK_MODEL = os.getenv('DEEPSEEK_MODEL', 'deepseek-chat')
LLM_TEMPERATURE = float(os.getenv('LLM_TEMPERATURE', '0.3'))
LLM_MAX_TOKENS = int(os.getenv('LLM_MAX_TOKENS', '1024'))
RETRIEVAL_TOP_K = int(os.getenv('RETRIEVAL_TOP_K', '3'))
```

**文件**：`python-service/.env`

```
DEEPSEEK_API_KEY=你的API密钥
DEEPSEEK_API_BASE=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-chat
```

---

### 第5步：修改 Java 后端

#### 5a. `PythonServiceClient.java`

```java
public Map<String, Object> getAdvice(String emotion, double confidence) {
    String url = pythonServiceUrl + "/api/advice";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, Object> body = Map.of("emotion", emotion, "confidence", confidence);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.POST, request, Map.class);
    return response.getBody();
}
```

#### 5b. `DetectionServiceImpl.java`（第100-110行替换）

```java
// happy → 直接使用 emotion_dict 的鼓励语
if ("happy".equals(emotionLabel)) {
    if (dict != null) {
        adviceText = dict.getDefaultAdvice();
        adviceSource = dict.getAdviceSource();
    }
    adviceSourceType = "dict";
} else {
    // 非happy → 调用 Python /api/advice 获取 LLM 建议
    try {
        Map<String, Object> adviceResult =
            pythonServiceClient.getAdvice(emotionLabel, confidence);
        if (adviceResult != null && Boolean.TRUE.equals(adviceResult.get("success"))) {
            adviceText = (String) adviceResult.get("advice");
            List<String> sources = (List<String>) adviceResult.get("sources");
            adviceSource = sources != null ? String.join(", ", sources) : null;
            adviceSourceType = (String) adviceResult.get("advice_source_type");
        }
    } catch (Exception e) {
        log.warn("LLM建议生成失败，降级到字典兜底: {}", e.getMessage());
    }
    // LLM 失败时降级到 emotion_dict
    if (adviceText == null && dict != null) {
        adviceText = dict.getDefaultAdvice();
        adviceSource = dict.getAdviceSource();
        adviceSourceType = "dict";
    }
}
```

---

### 第6步：修改健康检查

**Python 端** `app/router/inference.py`：

```python
# 健康检查增加 retriever/llm 状态
@router.get("/health")
async def health():
    return {
        "status": "healthy",
        "detector": True,
        "retriever": retriever.is_ready(),
        "llm": llm_service.is_ready()
    }
```

---

## 五、关键设计决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| RAG 在哪端执行 | **Python 端** | Embedding + FAISS + LLM 均为 Python 生态，避免序列化开销 |
| Java 端角色 | 调用方 + 结果存储 | Java 负责业务编排，Python 负责 AI 能力 |
| LLM 降级策略 | DeepSeek → 检索拼接 → emotion_dict 兜底 | 三级降级，保证任何情况下都有建议返回 |
| 向量维度 | 512（BAAI/bge-small-zh-v1.5） | 中文 embedding 效果好，模型体积小 |
| 知识库来源 | 知网 PDF 学术文献 | 权威可靠，内容专业 |
| 文本分块 | 200-400 字/块 | 平衡语义完整性与检索精度 |
| 情绪分类 | 关键词频率匹配 | 无需训练，自动标注，可人工修正 |

---

## 六、准备工作清单

- [ ] 从知网下载 PDF 文档 → 放入 `python-service/data/knowledge_raw/`
- [ ] 安装依赖：`pip install pdfplumber`（取消 requirements.txt 注释）
- [ ] 配置 LLM：在 `.env` 中填入 DeepSeek API Key
- [ ] 运行 PDF 提取：`python scripts/pdf_to_knowledge.py`
- [ ] 运行索引构建：`python scripts/build_knowledge.py`
- [ ] 验证检索效果：检查 `data/knowledge/` 下 6 个 JSON 文件内容
- [ ] 启动 Python 服务，测试 `/api/advice` 接口
- [ ] 启动 Java 服务，验证完整检测流程

---

## 七、相关文件清单

### Python 端新增文件

| 文件 | 职责 |
|------|------|
| `app/service/document_parser.py` | PDF 解析、清洗、分块、情绪分类 |
| `scripts/pdf_to_knowledge.py` | 知网 PDF → JSON 知识条目 |
| `scripts/build_knowledge.py` | 知识库索引构建 |
| `app/service/embedding_service.py` | Embedding 向量化 |
| `app/service/retriever.py` | FAISS 知识库检索 |
| `app/service/llm_service.py` | LLM 总结（DeepSeek API） |
| `app/service/pipeline.py` | RAG 主流程编排 |
| `app/router/advice.py` | `POST /api/advice` 路由 |

### Python 端修改文件

| 文件 | 修改内容 |
|------|----------|
| `requirements.txt` | 启用 `pdfplumber`，新增 `nltk` |
| `app/main.py` | 注册 advice_router，lifespan 加载模型和索引 |
| `app/config.py` | 新增知识库/Embedding/LLM 配置项 |
| `app/router/inference.py` | 健康检查增加 retriever/llm 状态 |
| `.env` | 填入 LLM API Key |

### Java 端修改文件

| 文件 | 修改内容 |
|------|----------|
| `PythonServiceClient.java` | 新增 `getAdvice()` 方法 |
| `DetectionServiceImpl.java` | 替换静态字典查询为 RAG + LLM 逻辑 |

### 目录结构（新增）

```
python-service/
├── data/
│   ├── knowledge_raw/     ← 知网 PDF 放这里
│   │   ├── paper1.pdf
│   │   ├── paper2.pdf
│   │   └── ...
│   ├── knowledge/         ← 自动生成的 JSON
│   │   ├── sad.json
│   │   ├── angry.json
│   │   ├── fear.json
│   │   ├── disgust.json
│   │   ├── surprise.json
│   │   └── neutral.json
│   └── vectors/           ← 自动生成的索引
│       ├── knowledge.faiss
│       └── knowledge_mapping.pkl
└── ...
```

---

**文档版本**：v2.0
**更新日期**：2026-07-27
**变更说明**：新增知网 PDF 文档处理流水线、中文文本分块、关键词情绪分类
