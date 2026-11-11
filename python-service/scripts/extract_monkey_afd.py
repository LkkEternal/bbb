"""从AFD动物面部数据集提取猴子/灵长类面部图片"""
import zipfile
import random
import os
import sys
from io import BytesIO
from pathlib import Path

from facenet_pytorch import MTCNN
from PIL import Image
import torch

# ============ 配置 ============
ZIP_PATH = os.path.join("D:", os.sep, "\u8868\u60c5\u68c0\u6d4b", "z3x59pv4bz-2.zip")
OUT_DIR = Path(os.path.join("D:", os.sep, "\u8868\u60c5\u68c0\u6d4b", "python-service", "data", "raw", "unknown", "animal", "monkey"))
TARGET_SIZE = (256, 256)
MIN_FACE_SIZE = 40
MTCNN_THRESHOLDS = [0.6, 0.7, 0.7]
RED_PANDA_SPECIES = "Ailurus fulgens"
RED_PANDA_LIMIT = 200

print("=" * 60)
print("AFD 猴子/灵长类面部图片提取")
print("=" * 60)
print(f"ZIP: {ZIP_PATH}")
print(f"输出: {OUT_DIR}")
print(f"ZIP存在: {os.path.exists(ZIP_PATH)}")

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
print(f"设备: {device}")

mtcnn = MTCNN(
    keep_all=True,
    device=device,
    min_face_size=MIN_FACE_SIZE,
    thresholds=MTCNN_THRESHOLDS,
    post_process=True,
)
print("MTCNN 加载完成")

OUT_DIR.mkdir(parents=True, exist_ok=True)

z = zipfile.ZipFile(ZIP_PATH, "r")
inner_zips = [n for n in z.namelist() if n.endswith(".zip")]
print(f"\n找到 {len(inner_zips)} 个物种zip")

total_saved = 0
total_skipped = 0

for iz_name in inner_zips:
    species = iz_name.split("/")[-1].replace(".zip", "")
    print(f"\n--- {species} ---")

    iz = zipfile.ZipFile(BytesIO(z.read(iz_name)), "r")
    img_files = [n for n in iz.namelist() if not n.endswith("/") and not n.lower().endswith(".db")]
    print(f"  图片数: {len(img_files)}")

    is_red_panda = species == RED_PANDA_SPECIES
    if is_red_panda:
        random.seed(42)
        img_files = random.sample(img_files, min(RED_PANDA_LIMIT, len(img_files)))
        print(f"  小熊猫限制为 {len(img_files)} 张")

    saved = 0
    skipped = 0

    for idx, fname in enumerate(img_files):
        try:
            data = iz.read(fname)
            img = Image.open(BytesIO(data)).convert("RGB")
            boxes, probs, _ = mtcnn.detect(img, landmarks=True)

            if boxes is None or len(boxes) == 0:
                skipped += 1
                continue

            best_idx = probs.argmax()
            conf = float(probs[best_idx])
            if conf < 0.90:
                skipped += 1
                continue

            box = boxes[best_idx].astype(int)
            x1, y1, x2, y2 = box
            x1, y1 = max(0, x1), max(0, y1)
            x2, y2 = min(img.width, x2), min(img.height, y2)

            if x2 - x1 < MIN_FACE_SIZE or y2 - y1 < MIN_FACE_SIZE:
                skipped += 1
                continue

            face = img.crop((x1, y1, x2, y2))
            face = face.resize(TARGET_SIZE, Image.LANCZOS)

            safe_name = species.replace(" ", "_").lower()
            save_path = OUT_DIR / f"{safe_name}_{saved:05d}.jpg"
            face.save(str(save_path), "JPEG", quality=85)
            saved += 1

        except Exception:
            skipped += 1
            continue

        if (idx + 1) % 200 == 0:
            print(f"  进度: {idx + 1}/{len(img_files)}, 保存: {saved}, 跳过: {skipped}")

    total_saved += saved
    total_skipped += skipped
    print(f"  完成: 保存 {saved}, 跳过 {skipped}")

z.close()

print(f"\n{'=' * 60}")
print(f"全部完成! 总保存: {total_saved}, 总跳过: {total_skipped}")
print(f"输出: {OUT_DIR}")
print(f"{'=' * 60}")
