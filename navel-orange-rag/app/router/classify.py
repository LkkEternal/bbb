"""病害分类路由（预留）"""

from fastapi import APIRouter

router = APIRouter(tags=["classify"])


@router.post("/classify")
async def classify():
    """病害分类接口（待实现）"""
    return {"message": "Classify endpoint - not yet implemented"}
