"""FastAPI应用入口"""
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import HOST, PORT
from app.service import inference_service
from app.router.inference import router as inference_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    inference_service.load_models()
    yield


app = FastAPI(
    title='表情检测服务',
    version='1.0.0',
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=['*'],
    allow_credentials=True,
    allow_methods=['*'],
    allow_headers=['*'],
)

app.include_router(inference_router)


@app.get('/')
def root():
    return {'message': '表情检测服务', 'docs': '/docs'}


if __name__ == '__main__':
    import uvicorn
    uvicorn.run('app.main:app', host=HOST, port=PORT, reload=True)
