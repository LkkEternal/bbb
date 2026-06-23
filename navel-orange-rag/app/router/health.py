"""Health check 路由"""

from fastapi import APIRouter

from app.config import settings
from app.models import HealthResponse
from app.service.embedding_service import embedding_service
from app.service.llm_service import llm_service

router = APIRouter(tags=["health"])


@router.get("/health", response_model=HealthResponse)
async def health():
    """服务健康检查"""
    return HealthResponse(
        status="ok",
        embedding_model=settings.embedding_model_name,
        embedding_loaded=embedding_service.is_loaded,
        llm_deepseek_configured=llm_service.deepseek_configured,
        llm_qwen_local_configured=llm_service.qwen_local_reachable,
        device=embedding_service.device,
    )
