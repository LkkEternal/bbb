"""
下载 unknown 类训练图片（国内镜像版）
======================================
使用国内可访问的镜像站下载:
1. HuggingFace镜像: hf-mirror.com
2. GitHub加速: ghproxy.com
3. pollinations.ai (AI生图，无需翻墙)
4. RAF-DB本地筛选
"""

import os
import sys
import time
import random
from pathlib import Path
from io import BytesIO
from urllib.request import urlopen, Request
from urllib.error import URLError, HTTPError

from PIL import Image

SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
RAW_DIR = PROJECT_ROOT / 'data' / 'raw' / 'unknown'
RAFDB_ALIGNED = Path(r'D:\表情检测\RAF-DB\basic\Image\aligned')
RAFDB_LABELS = Path(r'D:\表情检测\RAF-DB\basic\EmoLabel\list_patition_label.txt')

ANIME_DIR = RAW_DIR / 'anime'
ANIMAL_DIR = RAW_DIR / 'animal'
OBJECT_DIR = RAW_DIR / 'object'
EXTREME_DIR = RAW_DIR / 'extreme_face'

TARGET_ANIME = 1000
TARGET_ANIMAL = 1000
TARGET_OBJECT = 500
TARGET_EXTREME = 500

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
}

HF_MIRROR = 'https://hf-mirror.com'
GH_PROXY = 'https://ghproxy.com'


def ensure_dirs():
    for d in [ANIME_DIR, ANIMAL_DIR, OBJECT_DIR, EXTREME_DIR]:
        d.mkdir(parents=True, exist_ok=True)


def fetch(url, timeout=15):
    try:
        req = Request(url, headers=HEADERS)
        with urlopen(req, timeout=timeout) as resp:
            return resp.read()
    except Exception:
        return None


def save_img(data, path):
    try:
        img = Image.open(BytesIO(data)).convert('RGB')
        if img.width < 40 or img.height < 40:
            return False
        img = img.resize((256, 256), Image.LANCZOS)
        img.save(str(path), 'JPEG', quality=85)
        return True
    except Exception:
        return False


def count_existing(d):
    return len(list(d.glob('*.jpg')))


def progress(label, current, target):
    pct = current / target * 100 if target > 0 else 0
    print(f"  [{label}] {current}/{target} ({pct:.0f}%)")


# ============================================================
# 方案1: pollinations.ai (AI生图，国内可访问)
# ============================================================

def download_pollinations(prompt, save_dir, count, idx, seed_start=0):
    seed = seed_start + idx
    url = f'https://image.pollinations.ai/prompt/{prompt}?width=256&height=256&seed={seed}&nologo=true'
    data = fetch(url, timeout=30)
    if data:
        save_path = save_dir / f'{idx:05d}.jpg'
        return save_img(data, save_path)
    return False


# ============================================================
# 方案2: HuggingFace镜像 (hf-mirror.com)
# ============================================================

def download_hf_file(repo_id, filename, save_path):
    url = f'{HF_MIRROR}/{repo_id}/resolve/main/{filename}'
    data = fetch(url, timeout=20)
    if data:
        return save_img(data, save_path)
    return False


# ============================================================
# 动漫脸下载
# ============================================================

def download_anime():
    print("\n=== 下载动漫脸 ===")
    current = count_existing(ANIME_DIR)
    if current >= TARGET_ANIME:
        print(f"  已有 {current} 张，跳过")
        return current

    idx = current

    print("  尝试 pollinations.ai 生成动漫脸...")
    prompts = [
        'anime%20girl%20face%20portrait%20blue%20hair%20beautiful',
        'anime%20boy%20face%20portrait%20red%20eyes',
        'anime%20face%20close%20up%20cute%20smile',
        'anime%20character%20face%20chibi%20style',
        'japanese%20anime%20face%20colorful%20hair',
        'anime%20face%20serious%20expression%20dark%20hair',
        'anime%20face%20happy%20sparkle%20eyes',
        'anime%20face%20sad%20tear%20emotional',
        'anime%20warrior%20face%20helmet%20detailed',
        'anime%20magical%20girl%20face%20glowing',
    ]

    while idx < TARGET_ANIME:
        prompt = prompts[idx % len(prompts)]
        if download_pollinations(prompt, ANIME_DIR, TARGET_ANIME, idx, seed_start=random.randint(1000, 99999)):
            idx += 1
        else:
            idx += 1

        if idx % 50 == 0:
            progress('动漫脸', idx, TARGET_ANIME)

    print(f"  [动漫脸] 完成: {idx} 张")
    return idx


# ============================================================
# 动物脸下载
# ============================================================

def download_animal():
    print("\n=== 下载动物脸 ===")
    current = count_existing(ANIMAL_DIR)
    if current >= TARGET_ANIMAL:
        print(f"  已有 {current} 张，跳过")
        return current

    idx = current

    print("  尝试 pollinations.ai 生成动物脸...")
    prompts = [
        'close%20up%20photo%20of%20cute%20cat%20face%20fluffy',
        'close%20up%20photo%20of%20golden%20retriever%20dog%20face',
        'close%20up%20photo%20of%20baby%20monkey%20face',
        'close%20up%20photo%20of%20panda%20bear%20face',
        'close%20up%20photo%20of%20tiger%20face%20stripes',
        'close%20up%20photo%20of%20lion%20face%20majestic',
        'close%20up%20photo%20of%20fox%20face%20orange%20fur',
        'close%20up%20photo%20of%20rabbit%20face%20white%20fluffy',
        'close%20up%20photo%20of%20puppy%20face%20pomeranian',
        'close%20up%20photo%20of%20kitten%20face%20persian',
        'close%20up%20photo%20of%20husky%20dog%20face%20blue%20eyes',
        'close%20up%20photo%20of%20corgi%20face%20cute',
        'close%20up%20photo%20of%20shiba%20inu%20face%20smile',
        'close%20up%20photo%20of%20black%20cat%20face%20green%20eyes',
        'close%20up%20photo%20of%20hamster%20face%20cheeks%20full',
    ]

    while idx < TARGET_ANIMAL:
        prompt = prompts[idx % len(prompts)]
        if download_pollinations(prompt, ANIMAL_DIR, TARGET_ANIMAL, idx, seed_start=random.randint(1000, 99999)):
            idx += 1
        else:
            idx += 1

        if idx % 50 == 0:
            progress('动物脸', idx, TARGET_ANIMAL)

    print(f"  [动物脸] 完成: {idx} 张")
    return idx


# ============================================================
# 非人脸物品下载
# ============================================================

def download_objects():
    print("\n=== 下载非人脸物品 ===")
    current = count_existing(OBJECT_DIR)
    if current >= TARGET_OBJECT:
        print(f"  已有 {current} 张，跳过")
        return current

    idx = current

    print("  尝试 pollinations.ai 生成物品图...")
    prompts = [
        'beautiful%20red%20rose%20flower%20garden',
        'yellow%20sunflower%20field%20sunny',
        'red%20sports%20car%20racing',
        'white%20airplane%20flying%20sky',
        'old%20wooden%20ship%20ocean',
        'grand%20piano%20concert%20hall',
        'slice%20of%20pizza%20cheese%20melting',
        'laptop%20computer%20desk%20modern',
        'mountain%20landscape%20sunset%20clouds',
        'ocean%20wave%20blue%20water%20crashing',
        'birthday%20cake%20candles%20colorful',
        'bicycle%20parked%20street%20vintage',
        'butterfly%20flower%20garden%20colorful',
        'smartphone%20screen%20glowing%20modern',
        'forest%20trees%20autumn%20leaves%20golden',
    ]

    while idx < TARGET_OBJECT:
        prompt = prompts[idx % len(prompts)]
        if download_pollinations(prompt, OBJECT_DIR, TARGET_OBJECT, idx, seed_start=random.randint(1000, 99999)):
            idx += 1
        else:
            idx += 1

        if idx % 50 == 0:
            progress('非人脸物品', idx, TARGET_OBJECT)

    print(f"  [非人脸物品] 完成: {idx} 张")
    return idx


# ============================================================
# 极端角度真人脸 - RAF-DB本地筛选
# ============================================================

def download_extreme():
    print("\n=== 筛选极端角度真人脸 ===")
    current = count_existing(EXTREME_DIR)
    if current >= TARGET_EXTREME:
        print(f"  已有 {current} 张，跳过")
        return current

    if not RAFDB_LABELS.exists():
        print("  [ERROR] RAF-DB 标签文件不存在")
        return 0

    print("  使用尺寸筛选RAF-DB中的小图/模糊图作为极端角度候选...")

    label_map = {}
    with open(RAFDB_LABELS, 'r') as f:
        for line in f:
            parts = line.strip().split()
            if len(parts) == 2:
                label_map[parts[0]] = int(parts[1])

    candidates = []
    for fname in label_map:
        img_path = RAFDB_ALIGNED / fname
        if not img_path.exists():
            continue
        try:
            img = Image.open(str(img_path))
            w, h = img.size
            area = w * h
            if area < 100 * 100:
                candidates.append((img_path, area))
        except Exception:
            continue

    candidates.sort(key=lambda x: x[1])

    count = current
    for img_path, area in candidates:
        if count >= TARGET_EXTREME:
            break
        save_path = EXTREME_DIR / f'{count:05d}.jpg'
        try:
            img = Image.open(str(img_path)).convert('RGB')
            img.save(str(save_path), 'JPEG', quality=85)
            count += 1
        except Exception:
            continue

    if count - current < TARGET_EXTREME:
        print("  小图不够，补充中等尺寸图...")
        all_imgs = []
        for fname in label_map:
            img_path = RAFDB_ALIGNED / fname
            if not img_path.exists():
                continue
            try:
                img = Image.open(str(img_path))
                w, h = img.size
                area = w * h
                if area < 120 * 120:
                    all_imgs.append((img_path, area))
            except Exception:
                continue

        all_imgs.sort(key=lambda x: x[1])
        for img_path, area in all_imgs:
            if count >= TARGET_EXTREME:
                break
            save_path = EXTREME_DIR / f'{count:05d}.jpg'
            try:
                img = Image.open(str(img_path)).convert('RGB')
                img.save(str(save_path), 'JPEG', quality=85)
                count += 1
            except Exception:
                continue

    print(f"  [极端角度] 完成: {count} 张")
    return count


# ============================================================
# 主函数
# ============================================================

def main():
    print("=" * 60)
    print("下载 unknown 类训练图片 (国内镜像版)")
    print("=" * 60)

    ensure_dirs()

    download_anime()
    download_animal()
    download_objects()
    download_extreme()

    print("\n" + "=" * 60)
    print("下载完成，统计:")
    print("=" * 60)

    total = 0
    for name, d in [('动漫脸', ANIME_DIR), ('动物脸', ANIMAL_DIR),
                     ('非人脸物品', OBJECT_DIR), ('极端角度', EXTREME_DIR)]:
        n = count_existing(d)
        total += n
        print(f"  {name}: {n} 张")

    print(f"  总计: {total} 张")
    print(f"\n  保存路径: {RAW_DIR}")


if __name__ == '__main__':
    main()
