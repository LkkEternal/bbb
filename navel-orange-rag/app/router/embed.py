"""Embedding 路由 —— 文本向量化"""

from fastapi import APIRouter, HTTPException

from app.models import EmbedRequest, EmbedResponse
from app.service.embedding_service import embedding_service

router = APIRouter(tags=["embedding"])


@router.post("/embed", response_model=EmbedResponse)
async def embed(req: EmbedRequest):
    """批量文本向量化"""
    try:
        embeddings = embedding_service.embed_batch(req.texts)
        return EmbedResponse(
            embeddings=embeddings,
            dim=embedding_service.dim,
            model=embedding_service.model_name,
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
