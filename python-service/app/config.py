"""FastAPI应用配置"""
import os
from pathlib import Path

os.environ["CUDA_VISIBLE_DEVICES"] = "0"
os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"

PROJECT_ROOT = Path(__file__).parent.parent
RUNS_CLASSIFY = PROJECT_ROOT / 'runs' / 'classify'


def find_best_model() -> str | None:
    # 优先从 models/ 目录查找
    root_models = PROJECT_ROOT / 'models'
    if root_models.exists():
        for f in sorted(root_models.glob('*cls*.pt'), reverse=True):
            return str(f)

    # 回退到 runs/ 目录（训练输出）
    runs_dir = RUNS_CLASSIFY / 'runs' / 'classify'
    if not runs_dir.exists():
        runs_dir = RUNS_CLASSIFY

    for d in sorted(runs_dir.iterdir(), reverse=True):
        if d.is_dir() and d.name.startswith('emotion_cls'):
            candidate = d / 'weights' / 'best.pt'
            if candidate.exists():
                return str(candidate)

    return None


HOST = os.getenv('HOST', '0.0.0.0')
PORT = int(os.getenv('PORT', '8001'))
DEVICE = os.getenv('DEVICE', '0')
MODEL_PATH = os.getenv('MODEL_PATH', '') or find_best_model()
CONF_THRESHOLD = float(os.getenv('CONF_THRESHOLD', '0.3'))
