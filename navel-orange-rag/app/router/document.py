"""文档处理路由 —— 上传 + 分块 + 向量化"""

from fastapi import APIRouter, HTTPException, UploadFile, File

from app.config import settings
from app.models import ProcessDocumentRequest, ProcessDocumentResponse
from app.service.document_service import process_document, DocumentProcessingError

router = APIRouter(tags=["document"])


@router.post("/process-document", response_model=ProcessDocumentResponse)
async def process_document_endpoint(req: ProcessDocumentRequest):
    """
    处理文档：从 URL 下载 → 文本提取 → 关键词验证 → 分块 → 去重 → 向量化

    file_type: pdf / docx / txt
    """
    try:
        result = await process_document(file_url=req.file_url, file_type=req.file_type)
        result.document_id = req.document_id
        return result
    except DocumentProcessingError as e:
        raise HTTPException(status_code=422, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/process-document/upload", response_model=ProcessDocumentResponse)
async def process_document_upload(
    file: UploadFile = File(...),
    document_id: int | None = None,
):
    """
    直接上传文件处理（替代 URL 方式）。

    支持: pdf, docx, txt  (最大 20MB)
    """
    # 校验类型
    filename = file.filename or ""
    if filename.endswith(".pdf"):
        file_type = "pdf"
    elif filename.endswith(".docx"):
        file_type = "docx"
    elif filename.endswith(".txt"):
        file_type = "txt"
    else:
        raise HTTPException(status_code=400, detail=f"Unsupported file type: {filename}")

    # 保存到临时目录
    import aiofiles
    import os
    import tempfile
    from pathlib import Path

    tmp_dir = Path(settings.temp_dir)
    tmp_dir.mkdir(parents=True, exist_ok=True)

    suffix = f".{file_type}"
    fd, local_path = tempfile.mkstemp(suffix=suffix, dir=str(tmp_dir))
    os.close(fd)

    content = await file.read()
    if len(content) > 20 * 1024 * 1024:
        raise HTTPException(status_code=400, detail="File too large (max 20MB)")

    async with aiofiles.open(local_path, "wb") as f:
        await f.write(content)

    # 构建结果时从本地读
    from app.service.document_service import _extract_text, _domain_keyword_check, chunk_text, _deduplicate_chunks
    from app.service.embedding_service import embedding_service
    from app.models import ChunkResult
    from numpy import ndarray

    try:
        text = _extract_text(local_path, file_type)
        if not _domain_keyword_check(text):
            raise DocumentProcessingError("Content not relevant to domain")

        raw_chunks = chunk_text(text, chunk_size=settings.chunk_size_sentences, overlap=settings.chunk_overlap_sentences)
        deduped = _deduplicate_chunks(raw_chunks)
        texts = [c["content"] for c in deduped]
        embeddings = embedding_service.embed_batch(texts)

        chunk_results = [
            ChunkResult(
                content=c["content"],
                chunk_index=c["index"],
                embedding=emb.tolist() if isinstance(emb, ndarray) else emb,
                token_count=len(c["content"]),
                metadata=c.get("metadata", {}),
            )
            for c, emb in zip(deduped, embeddings)
        ]

        return ProcessDocumentResponse(
            document_id=document_id,
            chunks=chunk_results,
            total_chunks=len(chunk_results),
            model=settings.embedding_model_name,
        )
    except Exception as e:
        raise HTTPException(status_code=422, detail=str(e))
    finally:
        try:
            Path(local_path).unlink(missing_ok=True)
        except Exception:
            pass
