"""用MTCNN置信度筛选RAF-DB中的极端角度真人脸 500张"""
import random
from pathlib import Path
from PIL import Image

RAFDB_ALIGNED = Path(r'D:\表情检测\RAF-DB\basic\Image\aligned')
OUT_DIR = Path(r'D:\表情检测\python-service\data\raw\unknown\extreme_face')
TARGET = 500

OUT_DIR.mkdir(parents=True, exist_ok=True)

import torch
from facenet_pytorch import MTCNN

device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
mtcnn = MTCNN(keep_all=True, device=device, min_face_size=20,
              thresholds=[0.5, 0.6, 0.6], post_process=True)
print(f'MTCNN on {device}')

all_imgs = sorted(RAFDB_ALIGNED.glob('*.jpg'))
print(f'Total RAF-DB images: {len(all_imgs)}')

candidates = []
batch_size = 64

for batch_start in range(0, len(all_imgs), batch_size):
    batch_paths = all_imgs[batch_start:batch_start + batch_size]
    batch_pil = []
    for p in batch_paths:
        try:
            batch_pil.append(Image.open(str(p)).convert('RGB'))
        except Exception:
            batch_pil.append(None)

    valid = [(i, img) for i, img in enumerate(batch_pil) if img is not None]
    if not valid:
        continue

    for orig_idx, img in valid:
        try:
            boxes, probs, _ = mtcnn.detect(img, landmarks=False)
            if probs is not None:
                min_conf = min(float(p) for p in probs)
                candidates.append((batch_paths[orig_idx], min_conf))
        except Exception:
            continue

    if batch_start % 1000 == 0:
        print(f'  Scanned {batch_start}/{len(all_imgs)}, candidates: {len(candidates)}', flush=True)

    if len(candidates) >= TARGET * 10:
        break

candidates.sort(key=lambda x: x[1])
print(f'Total candidates with MTCNN: {len(candidates)}')
print(f'Confidence range: {candidates[0][1]:.4f} ~ {candidates[-1][1]:.4f}')

selected = candidates[:TARGET]

count = 0
for img_path, conf in selected:
    if count >= TARGET:
        break
    try:
        img = Image.open(str(img_path)).convert('RGB')
        save_path = OUT_DIR / f'{count:05d}.jpg'
        img.save(str(save_path), 'JPEG', quality=85)
        count += 1
    except Exception:
        continue

print(f'Done! {count} extreme face images saved (MTCNN conf: {selected[0][1]:.4f} ~ {selected[-1][1]:.4f})')
