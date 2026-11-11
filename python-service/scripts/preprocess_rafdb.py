#!/usr/bin/env python3
"""
RAF-DB 数据集预处理脚本
========================

【功能说明】
从RAF-DB数据集中筛选12000张图片，转换为YOLOv8-cls分类目录格式。

【采样策略】
少数类全保留 + 多数类等量补足:
  fear:      355张 (全保留)
  disgust:   877张 (全保留)
  angry:     867张 (全保留)
  surprise: 1619张 (全保留)
  sad:      2460张 (全保留)
  neutral:  2911张 (降采样)
  happy:    2911张 (降采样)
  总计:    12000张

【输出目录结构】(YOLOv8-cls分类格式)
data/processed/rafdb/
├── train/
│   ├── angry/
│   ├── disgust/
│   ├── fear/
│   ├── happy/
│   ├── sad/
│   ├── surprise/
│   └── neutral/
├── val/
│   ├── angry/
│   ├── disgust/
│   ├── fear/
│   ├── happy/
│   ├── sad/
│   ├── surprise/
│   └── neutral/
└── dataset.yaml

【使用方法】
    python scripts/preprocess_rafdb.py
"""

import os
import random
import shutil
from pathlib import Path
from collections import defaultdict

# ============================================================
# 配置
# ============================================================

# RAF-DB 标签映射 (RAF-DB标签1-7 -> 项目统一标签0-6)
# RAF-DB: 1=surprise, 2=fear, 3=disgust, 4=happy, 5=sad, 6=angry, 7=neutral
# 项目:   0=angry, 1=disgust, 2=fear, 3=happy, 4=sad, 5=surprise, 6=neutral
RAFDB_TO_PROJECT = {
    1: 5,   # surprise -> 5
    2: 2,   # fear     -> 2
    3: 1,   # disgust  -> 1
    4: 3,   # happy    -> 3
    5: 4,   # sad      -> 4
    6: 0,   # angry    -> 0
    7: 6,   # neutral  -> 6
}

# 项目统一情绪标签
EMOTIONS = {
    0: 'angry',
    1: 'disgust',
    2: 'fear',
    3: 'happy',
    4: 'sad',
    5: 'surprise',
    6: 'neutral'
}

EMOTIONS_CN = {
    0: '生气',
    1: '厌恶',
    2: '恐惧',
    3: '开心',
    4: '悲伤',
    5: '惊讶',
    6: '平静'
}

# 各类别筛选数量 (方案A: 少数类全保留+多数类等量补足)
FILTER_CONFIG = {
    0: 867,    # angry:    867张 (全保留)
    1: 877,    # disgust:  877张 (全保留)
    2: 355,    # fear:     355张 (全保留)
    3: 2911,   # happy:    2911张 (从5957降采样)
    4: 2460,   # sad:      2460张 (全保留)
    5: 1619,   # surprise: 1619张 (全保留)
    6: 2911,   # neutral:  2911张 (从3204降采样)
}

# 训练集/验证集比例
TRAIN_RATIO = 0.8

# 随机种子
RANDOM_SEED = 42


def load_labels(label_file):
    """
    加载RAF-DB标签文件

    【参数】
        label_file: 标签文件路径 (list_patition_label.txt)

    【返回】
        dict: {project_label: [filename, ...]}
    """
    print(f"[INFO] 加载标签文件: {label_file}")

    data_by_class = defaultdict(list)
    total = 0

    with open(label_file, 'r') as f:
        for line in f:
            parts = line.strip().split()
            if len(parts) == 2:
                filename = parts[0]
                rafdb_label = int(parts[1])
                project_label = RAFDB_TO_PROJECT[rafdb_label]
                data_by_class[project_label].append(filename)
                total += 1

    print(f"\n[STAT] RAF-DB 原始数据统计 (共 {total} 张):")
    for label_id in sorted(data_by_class.keys()):
        name = EMOTIONS[label_id]
        cn = EMOTIONS_CN[label_id]
        count = len(data_by_class[label_id])
        print(f"  {name:>10} ({cn}): {count:>6} 张")

    return dict(data_by_class)


def filter_data(data_by_class):
    """
    按配置筛选数据

    【参数】
        data_by_class: 原始数据

    【返回】
        dict: {project_label: [filename, ...]}
    """
    print("\n[STEP] 开始筛选数据...")

    filtered = {}

    for label_id, target_count in FILTER_CONFIG.items():
        class_data = data_by_class.get(label_id, [])

        random.seed(RANDOM_SEED + label_id)
        random.shuffle(class_data)

        selected = class_data[:target_count]
        filtered[label_id] = selected

        status = "全保留" if target_count >= len(class_data) else f"降采样 {len(class_data)}->{target_count}"
        print(f"  {EMOTIONS[label_id]:>10}: {len(class_data):>5} -> {len(selected):>4} 张 ({status})")

    total = sum(len(v) for v in filtered.values())
    print(f"\n[DONE] 筛选完成，共 {total} 张")

    return filtered


def create_cls_structure(output_dir):
    """创建YOLOv8-cls分类目录结构"""
    for split in ['train', 'val']:
        for emotion_name in EMOTIONS.values():
            dir_path = os.path.join(output_dir, split, emotion_name)
            os.makedirs(dir_path, exist_ok=True)
    print(f"[DONE] 创建分类目录结构: {output_dir}")


def split_and_copy(filtered_data, src_dir, output_dir):
    """
    按80/20划分train/val并复制图片

    【参数】
        filtered_data: 筛选后的数据 {label_id: [filename, ...]}
        src_dir: 源图片目录 (aligned/)
        output_dir: 输出目录
    """
    print("\n[STEP] 划分数据集并复制图片...")

    stats = {
        'train': {i: 0 for i in range(7)},
        'val': {i: 0 for i in range(7)}
    }

    missing = 0
    copied = 0

    for label_id, filenames in filtered_data.items():
        emotion_name = EMOTIONS[label_id]

        random.seed(RANDOM_SEED + label_id)
        random.shuffle(filenames)

        split_idx = int(len(filenames) * TRAIN_RATIO)
        train_files = filenames[:split_idx]
        val_files = filenames[split_idx:]

        for f in train_files:
            src = os.path.join(src_dir, f)
            if not os.path.exists(src):
                aligned_name = f.replace('.jpg', '_aligned.jpg')
                src = os.path.join(src_dir, aligned_name)
            dst = os.path.join(output_dir, 'train', emotion_name, os.path.basename(src))
            if os.path.exists(src):
                shutil.copy2(src, dst)
                stats['train'][label_id] += 1
                copied += 1
            else:
                missing += 1

        for f in val_files:
            src = os.path.join(src_dir, f)
            if not os.path.exists(src):
                aligned_name = f.replace('.jpg', '_aligned.jpg')
                src = os.path.join(src_dir, aligned_name)
            dst = os.path.join(output_dir, 'val', emotion_name, os.path.basename(src))
            if os.path.exists(src):
                shutil.copy2(src, dst)
                stats['val'][label_id] += 1
                copied += 1
            else:
                missing += 1

        print(f"  {emotion_name:>10}: train={stats['train'][label_id]:>4}, val={stats['val'][label_id]:>4}")

    if missing > 0:
        print(f"\n  WARNING: {missing} files not found!")

    print("\n" + "=" * 60)
    print("[STAT] 最终数据集统计")
    print("=" * 60)
    print(f"\n{'类别':<12} {'中文':<8} {'训练集':<10} {'验证集':<10} {'总计':<10}")
    print("-" * 60)

    for label_id in range(7):
        name = EMOTIONS[label_id]
        cn = EMOTIONS_CN[label_id]
        train_count = stats['train'][label_id]
        val_count = stats['val'][label_id]
        total_count = train_count + val_count
        print(f"{name:<12} {cn:<8} {train_count:<10} {val_count:<10} {total_count:<10}")

    print("-" * 60)
    total_train = sum(stats['train'].values())
    total_val = sum(stats['val'].values())
    print(f"{'总计':<12} {'':<8} {total_train:<10} {total_val:<10} {total_train + total_val:<10}")


def create_dataset_yaml(output_dir):
    """创建YOLOv8-cls数据集配置文件"""
    abs_path = os.path.abspath(output_dir)

    yaml_content = f"""# RAF-DB 分类数据集配置
# 12000张图片，7类表情
# 采样策略: 少数类全保留 + 多数类等量补足

# 数据集路径
path: {abs_path}
train: train
val: val

# 类别数量
nc: 7

# 类别名称 (项目统一编号)
names:
  0: angry      # 生气
  1: disgust    # 厌恶
  2: fear       # 恐惧
  3: happy      # 开心
  4: sad        # 悲伤
  5: surprise   # 惊讶
  6: neutral    # 平静
"""

    yaml_path = os.path.join(output_dir, 'dataset.yaml')
    with open(yaml_path, 'w', encoding='utf-8') as f:
        f.write(yaml_content)

    print(f"\n[DONE] 数据集配置已保存: {yaml_path}")


def main():
    print()
    print("=" * 60)
    print("RAF-DB 数据集预处理工具")
    print("=" * 60)

    script_dir = Path(__file__).parent
    rafdb_base = script_dir.parent.parent / 'RAF-DB'
    label_file = rafdb_base / 'basic' / 'EmoLabel' / 'list_patition_label.txt'
    src_img_dir = rafdb_base / 'basic' / 'Image' / 'aligned'
    output_dir = script_dir.parent / 'data' / 'processed' / 'rafdb'

    print(f"标签文件: {label_file}")
    print(f"源图片目录: {src_img_dir}")
    print(f"输出目录: {output_dir}")
    print()

    if not label_file.exists():
        print(f"[ERROR] 找不到标签文件: {label_file}")
        return
    if not src_img_dir.exists():
        print(f"[ERROR] 找不到图片目录: {src_img_dir}")
        return

    data_by_class = load_labels(label_file)
    filtered_data = filter_data(data_by_class)
    create_cls_structure(str(output_dir))
    split_and_copy(filtered_data, str(src_img_dir), str(output_dir))
    create_dataset_yaml(str(output_dir))

    print("\n" + "=" * 60)
    print("[DONE] RAF-DB 预处理完成!")
    print("=" * 60)
    print(f"输出目录: {output_dir}")
    print()
    print("下一步:")
    print("  python scripts/train_yolo.py --mode cls")


if __name__ == '__main__':
    main()
