"""风险预测路由 — 评估果园的整体病害风险"""

from __future__ import annotations

import logging

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

from app.config import settings
from app.service.prediction_service import prediction_service

logger = logging.getLogger(__name__)

router = APIRouter(tags=["prediction"])


# ── 响应模型 ──

class RiskFactorItem(BaseModel):
    feature: str
    value: float
    contribution: float
    direction: str  # "positive" | "negative"


class PredictionResponse(BaseModel):
    orchard_id: int
    risk_level: int       # 0/1/2/3
    risk_label: str       # 中文标签
    probabilities: list[float]  # [p0, p1, p2, p3]
    suggestion: str       # 简短建议
    top_factors: list[RiskFactorItem]
    llm_analysis: str     # LLM 生成的详细分析


class PredictAllItem(BaseModel):
    orchard_id: int
    risk_level: int
    risk_label: str
    error: str | None = None


class PredictAllResponse(BaseModel):
    total: int
    success: int
    failed: int
    results: list[PredictAllItem]


# ── 路由 ──

@router.post("/api/prediction/evaluate", response_model=PredictionResponse)
async def evaluate(orchard_id: int):
    """
    评估单个果园的整体病害风险。

    流程: 查询环境/病害/预报/物候数据 → 特征组装 → 模型预测 → LLM分析
    """
    if not prediction_service.ready:
        raise HTTPException(status_code=503, detail="Prediction model not loaded")

    try:
        result = await prediction_service.evaluate_orchard(orchard_id)
        return PredictionResponse(**result)
    except Exception as e:
        logger.error("Prediction failed for orchard %d: %s", orchard_id, e)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/api/prediction/evaluate-all", response_model=PredictAllResponse)
async def evaluate_all():
    """
    评估所有果园的风险（遍历 + 逐個预测）。
    生产环境建议通过定时任务触发，避免超时。
    """
    if not prediction_service.ready:
        raise HTTPException(status_code=503, detail="Prediction model not loaded")

    import asyncpg
    conn = await asyncpg.connect(
        host=prediction_service._pool._pool[0]._host  # type: ignore
        if prediction_service._pool else "localhost",
        database=settings.prediction_db_name,
        user=settings.prediction_db_user,
        password=settings.prediction_db_password,
    )
    try:
        rows = await conn.fetch(
            "SELECT id FROM orchard WHERE deleted = FALSE AND status = 1"
        )
        orchard_ids = [r["id"] for r in rows]
    finally:
        await conn.close()

    results = []
    success = 0
    failed = 0
    for oid in orchard_ids:
        try:
            r = await prediction_service.evaluate_orchard(oid)
            results.append(PredictAllItem(
                orchard_id=oid,
                risk_level=r["risk_level"],
                risk_label=r["risk_label"],
            ))
            success += 1
        except Exception as e:
            results.append(PredictAllItem(
                orchard_id=oid,
                risk_level=0,
                risk_label="评估失败",
                error=str(e),
            ))
            failed += 1

    return PredictAllResponse(
        total=len(orchard_ids),
        success=success,
        failed=failed,
        results=results,
    )
