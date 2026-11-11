#!/usr/bin/env python3
"""
FER2013 数据集筛选预处理脚本
============================

【功能说明】
从FER2013 CSV数据集中筛选12000张图片，转换为YOLO格式。

【筛选策略】
- 生气、悲伤、恐惧：各2000张（增加数量）
- 开心：2500张
- 平静：1500张
- 惊讶：1453张
- 厌恶：547张（全部保留）

【输出目录结构】
data/processed/fer2013_filtered/
├── images/
│   ├── train/    # 9600张 (80%)
│   └── val/      # 2400张 (20%)
├── labels/
│   ├── train/
│   └── val/
└── dataset.yaml

【使用方法】
    python scripts/preprocess_fer2013_filtered.py
"""

import os
import csv
import random
import numpy as np
from PIL import Image
from pathlib import Path

# ============================================================
# 筛选配置
# ============================================================

# 各类别筛选数量（总共12000张）
FILTER_CONFIG = {
    0: 2000,   # angry (生气): 从4953中选2000
    1: 547,    # disgust (厌恶): 全部保留
    2: 2000,   # fear (恐惧): 从5121中选2000
    3: 2500,   # happy (开心): 从8989中选2500
    4: 2000,   # sad (悲伤): 从6077中选2000
    5: 1453,   # surprise (惊讶): 从4002中选1453
    6: 1500,   # neutral (平静): 从6198中选1500
}

# 情绪标签映射
EMOTIONS = {
    0: 'angry',
    1: 'disgust',
    2: 'fear',
    3: 'happy',
    4: 'sad',
    5: 'surprise',
    6: 'neutral'
}

# 中文名称
EMOTIONS_CN = {
    0: '生气',
    1: '厌恶',
    2: '恐惧',
    3: '开心',
    4: '悲伤',
    5: '惊讶',
    6: '平静'
}

# 训练集/验证集比例
TRAIN_RATIO = 0.8  # 80%训练集，20%验证集

# 随机种子（保证可复现）
RANDOM_SEED = 42


def load_csv_data(csv_path):
    """
    加载CSV数据
    
    【参数】
        csv_path: CSV文件路径
    
    【返回】
        dict: {emotion_id: [(pixels, usage), ...]}
    """
    print(f"[INFO] 加载CSV数据: {csv_path}")
    
    # 按类别存储数据
    data_by_class = {i: [] for i in range(7)}
    
    with open(csv_path, 'r') as f:
        reader = csv.reader(f)
        next(reader)  # 跳过表头
        
        for row in reader:
            emotion_id = int(row[0])
            pixels = row[1]
            usage = row[2]
            
            data_by_class[emotion_id].append((pixels, usage))
    
    # 打印统计信息
    print("\n[STAT] 原始数据统计:")
    for emotion_id in sorted(data_by_class.keys()):
        count = len(data_by_class[emotion_id])
        print(f"  {EMOTIONS[emotion_id]:>10} ({EMOTIONS_CN[emotion_id]}): {count:>6} 张")
    
    return data_by_class


def filter_data(data_by_class, filter_config):
    """
    按配置筛选数据
    
    【参数】
        data_by_class: 原始数据
        filter_config: 筛选配置
    
    【返回】
        list: [(emotion_id, pixels, usage), ...]
    """
    print("\n[STEP] 开始筛选数据...")
    
    filtered_data = []
    
    for emotion_id, target_count in filter_config.items():
        # 获取该类别的所有数据
        class_data = data_by_class[emotion_id]
        
        # 随机打乱（使用固定种子保证可复现）
        random.seed(RANDOM_SEED + emotion_id)
        random.shuffle(class_data)
        
        # 筛选指定数量
        selected = class_data[:target_count]
        
        # 添加到结果
        for pixels, usage in selected:
            filtered_data.append((emotion_id, pixels, usage))
        
        print(f"  {EMOTIONS[emotion_id]:>10}: 从 {len(class_data):>5} 张中筛选 {len(selected):>4} 张")
    
    # 再次打乱，混合所有类别
    random.seed(RANDOM_SEED)
    random.shuffle(filtered_data)
    
    total = len(filtered_data)
    print(f"\n[DONE] 筛选完成，共 {total} 张")
    
    return filtered_data


def create_yolo_structure(output_dir):
    """创建YOLO目录结构"""
    dirs = [
        f'{output_dir}/images/train',
        f'{output_dir}/images/val',
        f'{output_dir}/labels/train',
        f'{output_dir}/labels/val'
    ]
    for d in dirs:
        os.makedirs(d, exist_ok=True)
    print(f"[DONE] 创建目录结构: {output_dir}")


def convert_to_yolo(filtered_data, output_dir):
    """
    将筛选后的数据转换为YOLO格式
    
    【参数】
        filtered_data: 筛选后的数据
        output_dir: 输出目录
    """
    print("\n[STEP] 转换为YOLO格式...")
    
    # 计算划分边界
    total = len(filtered_data)
    train_end = int(total * TRAIN_RATIO)
    
    print(f"  训练集: {train_end} 张 (80%)")
    print(f"  验证集: {total - train_end} 张 (20%)")
    
    # 统计信息
    stats = {
        'train': {i: 0 for i in range(7)},
        'val': {i: 0 for i in range(7)}
    }
    
    # 转换并保存
    for idx, (emotion_id, pixels, usage) in enumerate(filtered_data):
        # 确定划分
        if idx < train_end:
            split = 'train'
        else:
            split = 'val'
        
        # 转换像素为图像
        pixel_values = list(map(int, pixels.split()))
        img_array = np.array(pixel_values, dtype=np.uint8).reshape(48, 48)
        img = Image.fromarray(img_array, mode='L').convert('RGB')
        
        # 保存图像
        img_name = f'{idx:06d}.png'
        img_path = f'{output_dir}/images/{split}/{img_name}'
        img.save(img_path)
        
        # 生成YOLO标注
        label_path = f'{output_dir}/labels/{split}/{idx:06d}.txt'
        with open(label_path, 'w') as lf:
            lf.write(f'{emotion_id} 0.5 0.5 1.0 1.0\n')
        
        # 更新统计
        stats[split][emotion_id] += 1
        
        # 打印进度
        if idx % 2000 == 0:
            print(f"  处理中... {idx}/{total}")
    
    # 打印统计信息
    print("\n" + "=" * 60)
    print("[STAT] 筛选后数据集统计")
    print("=" * 60)
    
    print(f"\n{'类别':<12} {'中文':<8} {'训练集':<10} {'验证集':<10} {'总计':<10}")
    print("-" * 60)
    
    for emotion_id in range(7):
        name = EMOTIONS[emotion_id]
        cn_name = EMOTIONS_CN[emotion_id]
        train_count = stats['train'][emotion_id]
        val_count = stats['val'][emotion_id]
        total_count = train_count + val_count
        print(f"{name:<12} {cn_name:<8} {train_count:<10} {val_count:<10} {total_count:<10}")
    
    print("-" * 60)
    total_train = sum(stats['train'].values())
    total_val = sum(stats['val'].values())
    print(f"{'总计':<12} {'':<8} {total_train:<10} {total_val:<10} {total_train + total_val:<10}")


def create_dataset_yaml(output_dir):
    """创建数据集配置文件"""
    abs_path = os.path.abspath(output_dir)
    
    yaml_content = f"""# FER2013 筛选数据集配置
# 12000张图片，7类表情

# 数据集路径
path: {abs_path}
train: images/train
val: images/val

# 类别数量
nc: 7

# 类别名称
names:
  0: angry      # 生气
  1: disgust    # 厌恶
  2: fear       # 恐惧
  3: happy      # 开心
  4: sad        # 悲伤
  5: surprise   # 惊讶
  6: neutral    # 平静
"""
    
    yaml_path = f'{output_dir}/dataset.yaml'
    with open(yaml_path, 'w', encoding='utf-8') as f:
        f.write(yaml_content)
    
    print(f"\n[DONE] 数据集配置已保存: {yaml_path}")


def main():
    """主函数"""
    print()
    print("=" * 60)
    print("FER2013 数据集筛选预处理工具")
    print("=" * 60)
    
    # 路径配置
    script_dir = Path(__file__).parent.parent
    csv_path = script_dir.parent / 'fer2013' / 'fer2013.csv'
    output_dir = script_dir / 'data' / 'processed' / 'fer2013_filtered'
    
    print(f"CSV文件: {csv_path}")
    print(f"输出目录: {output_dir}")
    print()
    
    # 检查CSV文件
    if not csv_path.exists():
        print(f"[ERROR] 错误: 找不到CSV文件 {csv_path}")
        print("请确保FER2013数据集已下载并放置在正确位置")
        return
    
    # 加载数据
    data_by_class = load_csv_data(csv_path)
    
    # 筛选数据
    filtered_data = filter_data(data_by_class, FILTER_CONFIG)
    
    # 创建目录结构
    create_yolo_structure(str(output_dir))
    
    # 转换为YOLO格式
    convert_to_yolo(filtered_data, str(output_dir))
    
    # 创建配置文件
    create_dataset_yaml(str(output_dir))
    
    # 打印完成信息
    print("\n" + "=" * 60)
    print("[DONE] 预处理完成!")
    print("=" * 60)
    print(f"输出目录: {output_dir}")
    print()
    print("下一步:")
    print("1. 运行训练脚本: python scripts/train_yolo.py")
    print("2. 或查看训练指南: YOLO模型训练.md")


if __name__ == '__main__':
    main()
