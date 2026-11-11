#!/usr/bin/env python3
"""
FER2013 数据集预处理脚本
========================

【功能说明】
将FER2013数据集从CSV格式转换为YOLO检测格式。

FER2013数据集特点：
- 包含35,887张48x48像素的灰度人脸图像
- 7种基本情绪分类：生气、厌恶、恐惧、开心、悲伤、惊讶、平静
- 原始格式为CSV，每行包含：情绪标签、像素值、数据集划分

YOLO格式要求：
- 每张图片对应一个.txt标注文件
- 标注格式：class_id x_center y_center width height（归一化到0-1）
- 目录结构：images/train, images/val, labels/train, labels/val

【使用方法】
    python scripts/preprocess_fer2013.py

【数据集下载】
    https://www.kaggle.com/datasets/msambare/fer2013
    
    下载后将 fer2013.csv 放入 data/raw/ 目录

【输出目录结构】
    data/processed/fer2013_yolo/
    ├── images/
    │   ├── train/    # 训练集图像 (约28,709张)
    │   └── val/      # 验证集图像 (约7,178张)
    ├── labels/
    │   ├── train/    # 训练集标注
    │   └── val/      # 验证集标注
    └── dataset.yaml  # YOLO数据集配置文件
"""

import os  # 操作系统接口，用于文件路径操作
import csv  # CSV文件读写模块
import numpy as np  # 数值计算库，用于处理像素数组
from PIL import Image  # 图像处理库，用于创建和保存图像
from pathlib import Path  # 面向对象的文件路径处理

# ============================================================
# 常量定义
# ============================================================

# 情绪标签映射：将数字ID映射为英文名称
# FER2013数据集中emotion字段的含义：
#   0=angry(生气), 1=disgust(厌恶), 2=fear(恐惧),
#   3=happy(开心), 4=sad(悲伤), 5=surprise(惊讶), 6=neutral(平静)
EMOTIONS = {
    0: 'angry',      # 生气
    1: 'disgust',    # 厌恶
    2: 'fear',       # 恐惧
    3: 'happy',      # 开心
    4: 'sad',        # 悲伤
    5: 'surprise',   # 惊讶
    6: 'neutral'     # 平静
}

# 情绪中文名称映射（用于显示）
EMOTIONS_CN = {
    0: '生气',
    1: '厌恶',
    2: '恐惧',
    3: '开心',
    4: '悲伤',
    5: '惊讶',
    6: '平静'
}


def create_yolo_structure(output_dir):
    """
    创建YOLO格式的目录结构
    
    【参数】
        output_dir: 输出目录路径
    
    【功能】
    创建以下目录结构：
        output_dir/
        ├── images/
        │   ├── train/    # 训练集图像
        │   └── val/      # 验证集图像
        └── labels/
            ├── train/    # 训练集标注
            └── val/      # 验证集标注
    
    【YOLO格式说明】
    YOLO要求图像和标注文件分开存放在images/和labels/目录中，
    并且需要划分训练集(train)和验证集(val)。
    """
    # 定义需要创建的目录列表
    dirs = [
        f'{output_dir}/images/train',   # 训练集图像目录
        f'{output_dir}/images/val',     # 验证集图像目录
        f'{output_dir}/labels/train',   # 训练集标注目录
        f'{output_dir}/labels/val'      # 验证集标注目录
    ]
    
    # 遍历目录列表，逐个创建
    for d in dirs:
        # exist_ok=True 表示如果目录已存在则不报错
        os.makedirs(d, exist_ok=True)
    
    print(f"✅ 创建目录结构: {output_dir}")


def convert_csv_to_yolo(csv_path, output_dir):
    """
    将FER2013 CSV文件转换为YOLO格式
    
    【参数】
        csv_path: FER2013 CSV文件路径
        output_dir: 输出目录路径
    
    【返回】
        dict: 各情绪类别的统计信息
    
    【CSV格式说明】
    FER2013 CSV文件每行格式：
        emotion,pixels,usage
        0,70 80 82 72 58...,Training
    
    - emotion: 情绪类别ID (0-6)
    - pixels: 2304个像素值(48x48=2304)，空格分隔
    - usage: 数据集划分 (Training/PublicTest/PrivateTest)
    
    【转换逻辑】
    1. 读取CSV文件，解析每行数据
    2. 将像素值重塑为48x48的二维数组
    3. 将灰度图转换为RGB格式（YOLO要求）
    4. 根据usage字段划分训练集/验证集
    5. 保存图像文件和对应的YOLO标注文件
    
    【YOLO标注格式】
    每个.txt文件包含一行：
        class_id x_center y_center width height
    
    - class_id: 类别ID (0-6)
    - x_center: 边界框中心X坐标（归一化到0-1）
    - y_center: 边界框中心Y坐标（归一化到0-1）
    - width: 边界框宽度（归一化到0-1）
    - height: 边界框高度（归一化到0-1）
    
    由于FER2013已经是裁剪好的人脸图像，bbox覆盖整图：
        class_id 0.5 0.5 1.0 1.0
    """
    print(f"📥 读取CSV文件: {csv_path}")
    
    # 初始化统计字典，记录每个情绪类别在训练集/验证集中的数量
    stats = {emotion: {'train': 0, 'val': 0} for emotion in EMOTIONS.values()}
    
    # 打开CSV文件并逐行读取
    with open(csv_path, 'r') as f:
        reader = csv.reader(f)  # 创建CSV读取器
        next(reader)  # 跳过表头行 (emotion,pixels,usage)
        
        # 遍历每一行数据
        for idx, row in enumerate(reader):
            # 每处理5000张图片打印一次进度
            if idx % 5000 == 0:
                print(f"  处理中... {idx}/35887")
            
            # 解析CSV行数据
            emotion_id = int(row[0])           # 情绪类别ID (0-6)
            pixels = list(map(int, row[1].split()))  # 像素值字符串转为整数列表
            usage = row[2]                      # 数据集划分 (Training/PublicTest/PrivateTest)
            
            # 【图像转换】
            # 将2304个像素值重塑为48x48的二维数组
            img_array = np.array(pixels, dtype=np.uint8).reshape(48, 48)
            
            # 将灰度图(L模式)转换为RGB模式
            # YOLO要求输入为RGB图像，所以需要转换
            img = Image.fromarray(img_array, mode='L').convert('RGB')
            
            # 【数据集划分】
            # 根据usage字段决定放入训练集还是验证集
            if 'Training' in usage:
                split = 'train'
            else:  # PublicTest 或 PrivateTest 都作为验证集
                split = 'val'
            
            # 【保存图像文件】
            emotion_name = EMOTIONS[emotion_id]  # 获取情绪英文名称
            img_name = f'{idx:06d}.png'          # 文件名：6位数字补零
            img_path = f'{output_dir}/images/{split}/{img_name}'
            img.save(img_path)  # 保存为PNG格式
            
            # 【生成YOLO标注文件】
            # 格式：class_id x_center y_center width height
            # 由于FER2013已裁剪为人脸，bbox覆盖整图
            label_path = f'{output_dir}/labels/{split}/{idx:06d}.txt'
            with open(label_path, 'w') as lf:
                # 写入YOLO格式标注
                # 0.5 0.5 表示中心点在图像正中央
                # 1.0 1.0 表示宽度和高度都占满整图
                lf.write(f'{emotion_id} 0.5 0.5 1.0 1.0\n')
            
            # 更新统计信息
            stats[emotion_name][split] += 1
    
    # 打印统计信息表格
    print("\n" + "=" * 60)
    print("📊 数据集统计")
    print("=" * 60)
    print(f"{'表情':<12} {'中文':<8} {'训练集':<10} {'验证集':<10} {'总计':<10}")
    print("-" * 60)
    
    total_train = 0  # 训练集总计数
    total_val = 0    # 验证集总计数
    
    # 遍历每个情绪类别，打印统计信息
    for emotion_id, emotion_name in EMOTIONS.items():
        counts = stats[emotion_name]
        total = counts['train'] + counts['val']
        cn_name = EMOTIONS_CN[emotion_id]
        print(f"{emotion_name:<12} {cn_name:<8} {counts['train']:<10} {counts['val']:<10} {total:<10}")
        total_train += counts['train']
        total_val += counts['val']
    
    print("-" * 60)
    print(f"{'总计':<12} {'':<8} {total_train:<10} {total_val:<10} {total_train + total_val:<10}")
    
    return stats


def create_dataset_yaml(output_dir, output_path='dataset.yaml'):
    """
    创建YOLO数据集配置文件
    
    【参数】
        output_dir: 输出目录路径
        output_path: 配置文件名（相对于output_dir）
    
    【功能】
    生成dataset.yaml文件，这是YOLO训练必需的配置文件，包含：
    - 数据集根路径
    - 训练集/验证集相对路径
    - 类别数量
    - 类别名称映射
    
    【YAML配置说明】
    path: 数据集根目录的绝对路径
    train: 训练集图像目录（相对于path）
    val: 验证集图像目录（相对于path）
    nc: 类别数量 (number of classes)
    names: 类别名称字典 {class_id: class_name}
    """
    # 获取输出目录的绝对路径
    abs_path = os.path.abspath(output_dir)
    
    # 生成YAML配置内容
    yaml_content = f"""# FER2013 YOLO Dataset Configuration
# 表情检测数据集配置文件
# 自动生成于预处理脚本

# 数据集路径（绝对路径）
path: {abs_path}

# 训练集和验证集的相对路径
train: images/train
val: images/val

# 类别数量
nc: 7

# 类别名称映射
# 格式: class_id: class_name
names:
  0: angry      # 生气
  1: disgust    # 厌恶
  2: fear       # 恐惧
  3: happy      # 开心
  4: sad        # 悲伤
  5: surprise   # 惊讶
  6: neutral    # 平静
"""
    
    # 保存配置文件
    yaml_path = f'{output_dir}/{output_path}'
    with open(yaml_path, 'w', encoding='utf-8') as f:
        f.write(yaml_content)
    
    print(f"\n✅ 数据集配置已保存: {yaml_path}")


def create_dataset_yaml_template(output_path='data/dataset.yaml'):
    """
    创建数据集配置模板文件
    
    【参数】
        output_path: 模板文件保存路径
    
    【功能】
    当FER2013数据集尚未下载时，创建一个配置模板供用户参考。
    """
    yaml_content = """# FER2013 YOLO Dataset Configuration Template
# 表情检测数据集配置文件模板

# 数据集路径 (请根据实际路径修改)
path: ./data/processed/fer2013_yolo

# 训练集和验证集的相对路径
train: images/train
val: images/val

# 类别数量
nc: 7

# 类别名称映射
names:
  0: angry      # 生气
  1: disgust    # 厌恶
  2: fear       # 恐惧
  3: happy      # 开心
  4: sad        # 悲伤
  5: surprise   # 惊讶
  6: neutral    # 平静
"""
    
    # 创建目录（如果不存在）
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    # 保存模板文件
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(yaml_content)
    
    print(f"✅ 配置模板已保存: {output_path}")


def main():
    """
    主函数：协调整个预处理流程
    
    【执行流程】
    1. 定义文件路径
    2. 检查CSV文件是否存在
    3. 创建YOLO目录结构
    4. 将CSV转换为YOLO格式
    5. 生成数据集配置文件
    """
    # ============================================================
    # 路径配置
    # ============================================================
    
    # 获取脚本所在目录的父目录（项目根目录）
    script_dir = Path(__file__).parent.parent
    
    # 定义输入输出路径
    csv_path = script_dir / 'data' / 'raw' / 'fer2013.csv'        # 输入：原始CSV
    output_dir = script_dir / 'data' / 'processed' / 'fer2013_yolo'  # 输出：YOLO格式
    
    # 打印项目信息
    print("=" * 60)
    print("FER2013 数据集预处理工具")
    print("=" * 60)
    print(f"工作目录: {script_dir}")
    print(f"CSV文件: {csv_path}")
    print(f"输出目录: {output_dir}")
    print()
    
    # ============================================================
    # 检查数据集是否存在
    # ============================================================
    
    if not csv_path.exists():
        print(f"❌ 错误: 找不到CSV文件 {csv_path}")
        print()
        print("请按以下步骤操作:")
        print("1. 访问 https://www.kaggle.com/datasets/msambare/fer2013")
        print("2. 下载 fer2013.csv.zip")
        print("3. 解压得到 fer2013.csv")
        print(f"4. 将 fer2013.csv 放入 {script_dir / 'data' / 'raw' /} 目录")
        print()
        
        # 创建配置模板文件
        create_dataset_yaml_template(str(script_dir / 'data' / 'dataset.yaml'))
        return
    
    # ============================================================
    # 执行预处理
    # ============================================================
    
    # 步骤1：创建目录结构
    create_yolo_structure(str(output_dir))
    
    # 步骤2：转换CSV为YOLO格式
    stats = convert_csv_to_yolo(str(csv_path), str(output_dir))
    
    # 步骤3：生成数据集配置文件
    create_dataset_yaml(str(output_dir))
    
    # 打印完成信息
    print("\n" + "=" * 60)
    print("✅ 预处理完成!")
    print("=" * 60)
    print(f"输出目录: {output_dir}")
    print()
    print("下一步:")
    print("1. 运行训练脚本: python scripts/train_yolo.py")
    print("2. 或查看训练指南: YOLO模型训练.md")


# ============================================================
# 脚本入口点
# ============================================================
# 当直接运行此脚本时（不是被import），调用main函数
# 这是Python的标准做法，确保模块被导入时不会自动执行
if __name__ == '__main__':
    main()
