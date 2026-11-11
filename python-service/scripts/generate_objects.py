"""从RAF-DB生成非人脸物品图片 500张（重度变换）"""
import random
from pathlib import Path
from PIL import Image, ImageFilter, ImageEnhance, ImageOps
import numpy as np

RAFDB_ALIGNED = Path(r'D:\表情检测\RAF-DB\basic\Image\aligned')
RAFDB_LABELS = Path(r'D:\表情检测\RAF-DB\basic\EmoLabel\list_patition_label.txt')
OUT_DIR = Path(r'D:\表情检测\python-service\data\raw\unknown\object')
TARGET = 500

OUT_DIR.mkdir(parents=True, exist_ok=True)

label_map = {}
with open(RAFDB_LABELS, 'r') as f:
    for line in f:
        parts = line.strip().split()
        if len(parts) == 2:
            label_map[parts[0]] = int(parts[1])

all_imgs = list(RAFDB_LABELS.parent.parent.parent.joinpath('Image', 'aligned').glob('*.jpg'))
if not all_imgs:
    all_imgs = list(RAFDB_ALIGNED.glob('*.jpg'))

print(f'Available images: {len(all_imgs)}')

random.seed(42)
selected = random.sample(all_imgs, min(TARGET * 5, len(all_imgs)))


def heavy_blur(img):
    return img.filter(ImageFilter.GaussianBlur(radius=random.uniform(5, 15)))


def pixelate(img):
    w, h = img.size
    small = img.resize((random.randint(4, 12), random.randint(4, 12)), Image.BILINEAR)
    return small.resize((w, h), Image.NEAREST)


def invert_colors(img):
    return ImageOps.invert(img)


def extreme_color_shift(img):
    img = ImageEnhance.Color(img).enhance(random.uniform(0.0, 0.1))
    img = ImageEnhance.Brightness(img).enhance(random.uniform(0.3, 2.0))
    img = ImageEnhance.Contrast(img).enhance(random.uniform(2.0, 5.0))
    return img


def add_heavy_noise(img):
    arr = np.array(img)
    noise = np.random.randint(0, 255, arr.shape, dtype=np.uint8)
    alpha = random.uniform(0.3, 0.7)
    arr = (arr * (1 - alpha) + noise * alpha).astype(np.uint8)
    return Image.fromarray(arr)


def mirror_flip(img):
    return ImageOps.mirror(img)


def rotate_extreme(img):
    angle = random.uniform(45, 180)
    return img.rotate(angle, fillcolor=tuple(random.randint(0, 255) for _ in range(3)))


def solarize(img):
    return ImageOps.solarize(img, threshold=random.randint(64, 192))


def posterize(img):
    return ImageOps.posterize(img, bits=random.randint(1, 3))


def edge_only(img):
    return img.filter(ImageFilter.FIND_EDGES).convert('RGB')


def channel_shuffle(img):
    arr = np.array(img)
    order = [0, 1, 2]
    random.shuffle(order)
    arr = arr[:, :, order]
    return Image.fromarray(arr)


transforms = [
    heavy_blur, pixelate, invert_colors, extreme_color_shift,
    add_heavy_noise, mirror_flip, rotate_extreme, solarize,
    posterize, edge_only, channel_shuffle,
]

count = 0
for img_path in selected:
    if count >= TARGET:
        break
    try:
        img = Image.open(str(img_path)).convert('RGB')

        n_transforms = random.randint(2, 4)
        chosen = random.sample(transforms, min(n_transforms, len(transforms)))
        for t in chosen:
            img = t(img)

        img = img.resize((256, 256), Image.LANCZOS)
        save_path = OUT_DIR / f'{count:05d}.jpg'
        img.save(str(save_path), 'JPEG', quality=85)
        count += 1

        if count % 100 == 0:
            print(f'  Progress: {count}/{TARGET}', flush=True)
    except Exception:
        continue

print(f'Done! {count} non-face object images saved to {OUT_DIR}')
