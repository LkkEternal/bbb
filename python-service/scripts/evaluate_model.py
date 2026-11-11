#!/usr/bin/env python3
"""
YOLO模型评估脚本
================

【功能说明】
评估训练好的YOLO表情检测模型的性能，计算各类别和总体的评估指标。

【评估指标说明】

1. mAP50 (Mean Average Precision at IoU=0.5):
   - 在IoU阈值为0.5时的平均精度
   - IoU(Intersection over Union)是预测框和真实框的交并比
   - mAP50是目标检测最常用的指标之一
   - 范围: 0-1，越高越好

2. mAP50-95 (Mean Average Precision at IoU=0.5:0.95):
   - 在IoU阈值从0.5到0.95（步长0.5）的平均mAP
   - 更严格的评估标准，要求更精确的定位
   - 范围: 0-1，越高越好

3. 精确率 (Precision):
   - 预测为正样本中，实际为正样本的比例
   - Precision = TP / (TP + FP)
   - TP: True Positive (真正例)
   - FP: False Positive (假正例)

4. 召回率 (Recall):
   - 实际正样本中，被正确预测的比例
   - Recall = TP / (TP + FN)
   - FN: False Negative (假负例)

5. AP (Average Precision):
   - 单个类别的平均精度
   - 是P-R曲线下的面积

【使用方法】
    python scripts/evaluate_model.py

【输入】
    - 训练好的模型权重 (best.pt)
    - 数据集配置文件 (dataset.yaml)

【输出】
    - 总体评估指标
    - 各类别的详细评估
    - 评估报告文件
"""

import os  # 操作系统接口
from pathlib import Path  # 面向对象的文件路径处理

# 将工作目录切换到项目根目录
script_dir = Path(__file__).parent
project_root = script_dir.parent
os.chdir(project_root)

# ============================================================
# 情绪标签定义
# ============================================================

# 情绪标签映射：(英文名称, 中文名称)
EMOTIONS = {
    0: ('angry', '生气'),      # 生气
    1: ('disgust', '厌恶'),    # 厌恶
    2: ('fear', '恐惧'),       # 恐惧
    3: ('happy', '开心'),      # 开心
    4: ('sad', '悲伤'),        # 悲伤
    5: ('surprise', '惊讶'),   # 惊讶
    6: ('neutral', '平静')     # 平静
}


def find_best_model():
    """
    查找最佳训练模型
    
    【功能】
    自动查找训练好的最佳模型权重文件。
    
    【搜索顺序】
    1. 首先检查 models/ 目录
    2. 然后检查 runs/detect/ 目录下最新的训练结果
    
    【返回】
        str: 模型文件路径，如果未找到则返回None
    
    【模型文件说明】
    - best.pt: 训练过程中验证集表现最好的模型
    - last.pt: 最后一个epoch的模型
    """
    print("=" * 60)
    print("查找训练模型")
    print("=" * 60)
    
    # 首先检查models目录
    models_dir = Path('models')
    if models_dir.exists():
        # 查找所有.pt文件
        for model_file in models_dir.glob('*.pt'):
            print(f"✅ 找到模型: {model_file}")
            return str(model_file)
    
    # 然后检查runs目录
    runs_dir = Path('runs/detect')
    if runs_dir.exists():
        # 查找最新的emotion训练目录（按时间排序）
        emotion_dirs = sorted(
            [d for d in runs_dir.iterdir() if d.is_dir() and 'emotion_' in d.name],
            reverse=True  # 降序，最新的在前
        )
        
        # 遍历查找best.pt
        for emotion_dir in emotion_dirs:
            best_model = emotion_dir / 'weights' / 'best.pt'
            if best_model.exists():
                print(f"✅ 找到模型: {best_model}")
                return str(best_model)
    
    # 未找到模型
    print("❌ 未找到训练模型")
    print("请先运行训练脚本: python scripts/train_yolo.py")
    return None


def evaluate_model(model_path, data_yaml=None):
    """
    评估模型性能
    
    【参数】
        model_path: 模型文件路径
        data_yaml: 数据集配置文件路径（可选，默认使用FER2013配置）
    
    【返回】
        metrics: 评估指标对象，包含所有评估结果
    
    【评估流程】
    1. 加载训练好的模型
    2. 在验证集上进行推理
    3. 计算各类别的AP、mAP等指标
    4. 生成混淆矩阵
    5. 输出评估结果
    """
    from ultralytics import YOLO  # 导入YOLOv8
    
    print()
    print("=" * 60)
    print("模型评估")
    print("=" * 60)
    
    # 加载模型
    print(f"\n📥 加载模型: {model_path}")
    model = YOLO(model_path)
    
    # 数据集配置
    if data_yaml is None:
        data_yaml = 'data/processed/fer2013_yolo/dataset.yaml'
    
    print(f"📊 数据集配置: {data_yaml}")
    
    # 执行评估
    print("\n🔄 开始评估...")
    # model.val() 会在验证集上进行推理并计算评估指标
    metrics = model.val(data=data_yaml)
    
    # ============================================================
    # 打印总体评估结果
    # ============================================================
    
    print()
    print("=" * 60)
    print("📈 评估结果")
    print("=" * 60)
    
    # mAP50: IoU=0.5时的平均精度
    print(f"  mAP50:      {metrics.box.map50:.4f}")
    
    # mAP50-95: IoU从0.5到0.95的平均mAP
    print(f"  mAP50-95:   {metrics.box.map:.4f}")
    
    # 精确率: 预测为正样本中实际为正的比例
    print(f"  精确率:     {metrics.box.mp:.4f}")
    
    # 召回率: 实际正样本中被正确预测的比例
    print(f"  召回率:     {metrics.box.mr:.4f}")
    
    print()
    
    # ============================================================
    # 打印各类别详细性能
    # ============================================================
    
    print("=" * 60)
    print("📊 各类别性能")
    print("=" * 60)
    print(f"{'类别':<12} {'中文':<8} {'AP50':<10} {'AP':<10}")
    print("-" * 60)
    
    # 遍历7个类别，打印每个类别的AP50和AP
    for i in range(7):
        emotion_en, emotion_cn = EMOTIONS[i]
        ap50 = metrics.box.ap50[i]  # 该类别的AP50
        ap = metrics.box.ap[i]      # 该类别的AP
        print(f"{emotion_en:<12} {emotion_cn:<8} {ap50:<10.4f} {ap:<10.4f}")
    
    print("-" * 60)
    # 打印平均值
    print(f"{'平均':<12} {'':<8} {metrics.box.map50:<10.4f} {metrics.box.map:<10.4f}")
    
    return metrics


def generate_report(metrics, output_path='evaluation_report.txt'):
    """
    生成评估报告文件
    
    【参数】
        metrics: 评估指标对象
        output_path: 报告输出路径
    
    【功能】
    将评估结果保存为文本文件，便于后续查看和分析。
    
    【报告内容】
    - 总体评估指标
    - 各类别的详细性能
    - 评估时间戳
    """
    # 打开文件并写入报告
    with open(output_path, 'w', encoding='utf-8') as f:
        # 写入报告标题
        f.write("YOLO表情检测模型评估报告\n")
        f.write("=" * 60 + "\n\n")
        
        # 写入总体指标
        f.write("总体指标:\n")
        f.write(f"  mAP50:      {metrics.box.map50:.4f}\n")
        f.write(f"  mAP50-95:   {metrics.box.map:.4f}\n")
        f.write(f"  精确率:     {metrics.box.mp:.4f}\n")
        f.write(f"  召回率:     {metrics.box.mr:.4f}\n\n")
        
        # 写入各类别性能
        f.write("各类别性能:\n")
        for i in range(7):
            emotion_en, emotion_cn = EMOTIONS[i]
            ap50 = metrics.box.ap50[i]
            ap = metrics.box.ap[i]
            f.write(f"  {emotion_en} ({emotion_cn}): AP50={ap50:.4f}, AP={ap:.4f}\n")
    
    print(f"\n📄 评估报告已保存: {output_path}")


def main():
    """
    主函数：协调整个评估流程
    
    【执行流程】
    1. 查找训练好的模型
    2. 评估模型性能
    3. 生成评估报告
    """
    # 打印项目标题
    print()
    print("╔" + "═" * 58 + "╗")
    print("║" + " " * 15 + "YOLO模型评估工具" + " " * 15 + "║")
    print("╚" + "═" * 58 + "╝")
    print()
    
    # 查找模型
    model_path = find_best_model()
    
    if model_path is None:
        return
    
    # 评估模型
    metrics = evaluate_model(model_path)
    
    # 生成报告
    if metrics is not None:
        generate_report(metrics)
    
    # 打印完成信息
    print()
    print("=" * 60)
    print("✅ 评估完成!")
    print("=" * 60)


# ============================================================
# 脚本入口点
# ============================================================
if __name__ == '__main__':
    main()
