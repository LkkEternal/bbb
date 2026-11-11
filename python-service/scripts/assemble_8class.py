"""组装YOLO-cls 8类数据集"""
import shutil
import random
from pathlib import Path

SRC_7CLASS = Path(r'D:\表情检测\python-service\data\processed\rafdb')
SRC_UNKNOWN = Path(r'D:\表情检测\python-service\data\raw\unknown')
OUT_DIR = Path(r'D:\表情检测\python-service\data\processed\rafdb_8class')

CLASSES = ['angry', 'disgust', 'fear', 'happy', 'sad', 'surprise', 'neutral', 'unknown']
UNKNOWN_TRAIN_RATIO = 0.8

if OUT_DIR.exists():
    shutil.rmtree(str(OUT_DIR))

for split in ['train', 'val']:
    for cls in CLASSES:
        (OUT_DIR / split / cls).mkdir(parents=True, exist_ok=True)

print('Copying 7 original classes...')
for split in ['train', 'val']:
    for cls in CLASSES[:7]:
        src = SRC_7CLASS / split / cls
        dst = OUT_DIR / split / cls
        if src.exists():
            for f in src.glob('*.jpg'):
                shutil.copy2(str(f), str(dst / f.name))

for cls in CLASSES[:7]:
    n_train = len(list((OUT_DIR / 'train' / cls).glob('*.jpg')))
    n_val = len(list((OUT_DIR / 'val' / cls).glob('*.jpg')))
    print(f'  {cls}: train={n_train}, val={n_val}')

print('\nCollecting unknown images...')
unknown_all = []

anime_dir = SRC_UNKNOWN / 'anime'
for f in anime_dir.glob('*.jpg'):
    unknown_all.append(f)

for sub in ['cat', 'dog', 'wild', 'monkey']:
    animal_dir = SRC_UNKNOWN / 'animal' / sub
    for f in animal_dir.glob('*.jpg'):
        unknown_all.append(f)

object_dir = SRC_UNKNOWN / 'object'
for f in object_dir.glob('*.jpg'):
    unknown_all.append(f)

print(f'  Total unknown: {len(unknown_all)}')

random.seed(42)
random.shuffle(unknown_all)

split_idx = int(len(unknown_all) * UNKNOWN_TRAIN_RATIO)
train_unknown = unknown_all[:split_idx]
val_unknown = unknown_all[split_idx:]

print(f'  Train: {len(train_unknown)}, Val: {len(val_unknown)}')

for idx, f in enumerate(train_unknown):
    shutil.copy2(str(f), str(OUT_DIR / 'train' / 'unknown' / f'{idx:05d}.jpg'))

for idx, f in enumerate(val_unknown):
    shutil.copy2(str(f), str(OUT_DIR / 'val' / 'unknown' / f'{idx:05d}.jpg'))

yaml_content = f"""# RAF-DB 8-class 分类数据集
# 7类表情 + 1类unknown (动漫/动物/物品)

path: {str(OUT_DIR).replace(chr(92), '/')}
train: train
val: val

nc: 8

names:
  0: angry
  1: disgust
  2: fear
  3: happy
  4: sad
  5: surprise
  6: neutral
  7: unknown
"""

with open(OUT_DIR / 'dataset.yaml', 'w', encoding='utf-8') as f:
    f.write(yaml_content)

print('\n=== Final Dataset ===')
total = 0
for split in ['train', 'val']:
    split_total = 0
    for cls in CLASSES:
        n = len(list((OUT_DIR / split / cls).glob('*.jpg')))
        split_total += n
    total += split_total
    print(f'{split}: {split_total} images')

print(f'Total: {total} images')
print(f'\nSaved to: {OUT_DIR}')
print(f'Config: {OUT_DIR / "dataset.yaml"}')
