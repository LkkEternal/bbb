"""推理API路由"""
import time
from fastapi import APIRouter, File, UploadFile, HTTPException, Query
from app.service import inference_service

router = APIRouter(prefix='/api/inference', tags=['inference'])


@router.get('/health')
def health():
    return {
        'status': 'ok' if inference_service.is_ready() else 'loading',
        'model_loaded': inference_service.is_ready(),
    }


@router.post('/image')
async def predict_image(
    file: UploadFile = File(...),
):
    if not inference_service.is_ready():
        raise HTTPException(status_code=503, detail='模型未加载完成，请稍后重试')

    if not file.content_type or not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail='请上传图片文件')

    image_bytes = await file.read()
    if len(image_bytes) == 0:
        raise HTTPException(status_code=400, detail='文件为空')

    t0 = time.time()
    result = inference_service.inference(image_bytes)
    total_ms = (time.time() - t0) * 1000

    return {
        'success': True,
        'faces_count': result['faces_count'],
        'valid_count': result['valid_count'],
        'skipped_count': result['skipped_count'],
        'faces': result['faces'],
        'latency_ms': round(total_ms, 1),
    }
