#!/usr/bin/env python3
"""
YOLO模型导出脚本
================

【功能说明】
将训练好的YOLO模型导出为不同的格式，便于部署和推理。

【支持的导出格式】

1. ONNX (Open Neural Network Exchange):
   - 跨平台标准格式，支持多种推理引擎
   - 可以使用TensorRT、OpenVINO、ONNX Runtime等加速
   - 推荐用于生产环境部署
   - 文件扩展名: .onnx

2. TorchScript:
   - PyTorch原生的序列化格式
   - 无需Python环境即可运行
   - 适合PyTorch生态内的部署
   - 文件扩展名: .torchscript

3. TensorRT:
   - NVIDIA GPU专用的高性能推理引擎
   - 支持FP16/INT8量化，推理速度最快
   - 仅支持NVIDIA GPU
   - 文件扩展名: .engine

【使用方法】
    python scripts/export_model.py

【输出】
    - models/yolov8_emotion.pt    (PyTorch格式)
    - models/yolov8_emotion.onnx  (ONNX格式)
    - models/yolov8_emotion.engine (TensorRT格式，可选)
"""

import os  # 操作系统接口
import shutil  # 高级文件操作（复制、移动等）
from pathlib import Path  # 面向对象的文件路径处理

# 将工作目录切换到项目根目录
script_dir = Path(__file__).parent
project_root = script_dir.parent
os.chdir(project_root)


def find_best_model():
    """
    查找最佳训练模型
    
    【功能】
    自动查找训练好的最佳模型权重文件。
    
    【搜索策略】
    1. 优先查找 models/ 目录（已导出的模型）
    2. 然后查找 runs/detect/ 目录下最新的训练结果
    
    【返回】
        str: 模型文件路径，如果未找到则返回None
    """
    print("=" * 60)
    print("查找训练模型")
    print("=" * 60)
    
    # 首先检查models目录
    models_dir = Path('models')
    if models_dir.exists():
        # 遍历查找所有.pt文件
        for model_file in models_dir.glob('*.pt'):
            print(f"✅ 找到模型: {model_file}")
            return str(model_file)
    
    # 然后检查runs目录
    runs_dir = Path('runs/detect')
    if runs_dir.exists():
        # 查找最新的emotion训练目录（按时间戳排序）
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


def export_model(model_path, formats=None):
    """
    导出模型为不同格式
    
    【参数】
        model_path: 模型文件路径
        formats: 导出格式列表，默认['onnx']
    
    【返回】
        list: 导出的文件路径列表
    
    【导出流程】
    1. 加载PyTorch模型
    2. 根据指定格式逐个导出
    3. 将导出的文件复制到models目录
    4. 返回导出的文件列表
    
    【注意事项】
    - ONNX导出通常都能成功
    - TensorRT导出需要CUDA支持
    - 某些格式可能需要额外的依赖
    """
    from ultralytics import YOLO  # 导入YOLOv8
    
    # 默认导出ONNX格式
    if formats is None:
        formats = ['onnx']
    
    print()
    print("=" * 60)
    print("模型导出")
    print("=" * 60)
    
    # 加载模型
    print(f"\n📥 加载模型: {model_path}")
    model = YOLO(model_path)
    
    # 确保models目录存在
    models_dir = Path('models')
    models_dir.mkdir(exist_ok=True)
    
    # 存储导出的文件路径
    exported_files = []
    
    # 遍历导出格式列表
    for fmt in formats:
        print(f"\n📦 导出{fmt.upper()}格式...")
        
        try:
            if fmt == 'onnx':
                # ====================================================
                # ONNX格式导出
                # ====================================================
                # ONNX是跨平台的模型格式，支持多种推理引擎
                exported_path = model.export(format='onnx')
                
                # 复制到models目录
                target_path = models_dir / 'yolov8_emotion.onnx'
                shutil.copy2(exported_path, target_path)
                print(f"✅ ONNX模型: {target_path}")
                exported_files.append(target_path)
                
            elif fmt == 'torchscript':
                # ====================================================
                # TorchScript格式导出
                # ====================================================
                # TorchScript是PyTorch的序列化格式
                exported_path = model.export(format='torchscript')
                
                # 复制到models目录
                target_path = models_dir / 'yolov8_emotion.torchscript'
                shutil.copy2(exported_path, target_path)
                print(f"✅ TorchScript模型: {target_path}")
                exported_files.append(target_path)
                
            elif fmt == 'engine':
                # ====================================================
                # TensorRT格式导出
                # ====================================================
                # TensorRT需要CUDA支持
                print("  ⚠️ TensorRT导出需要CUDA支持")
                
                # half=True 表示使用FP16半精度，可以减小模型大小并加速推理
                exported_path = model.export(format='engine', half=True)
                
                # 复制到models目录
                target_path = models_dir / 'yolov8_emotion.engine'
                shutil.copy2(exported_path, target_path)
                print(f"✅ TensorRT模型: {target_path}")
                exported_files.append(target_path)
                
            else:
                # 不支持的格式
                print(f"  ⚠️ 不支持的格式: {fmt}")
                
        except Exception as e:
            # 导出失败
            print(f"  ❌ 导出{fmt.upper()}失败: {e}")
    
    # ============================================================
    # 复制原始PyTorch模型
    # ============================================================
    
    print("\n📦 复制PyTorch模型...")
    target_pt = models_dir / 'yolov8_emotion.pt'
    shutil.copy2(model_path, target_pt)
    print(f"✅ PyTorch模型: {target_pt}")
    exported_files.append(target_pt)
    
    return exported_files


def print_model_info(models_dir):
    """
    打印模型目录内容信息
    
    【参数】
        models_dir: 模型目录路径
    
    【功能】
    列出models目录中的所有模型文件及其大小。
    """
    print()
    print("=" * 60)
    print("模型目录内容")
    print("=" * 60)
    
    if models_dir.exists():
        # 遍历目录中的所有文件
        for model_file in models_dir.iterdir():
            if model_file.is_file():
                # 计算文件大小（MB）
                size_mb = model_file.stat().st_size / 1024 / 1024
                print(f"  {model_file.name:<30} {size_mb:.2f} MB")
    else:
        print("  目录为空或不存在")


def main():
    """
    主函数：协调整个导出流程
    
    【执行流程】
    1. 查找训练好的模型
    2. 导出为ONNX格式
    3. 显示模型目录内容
    4. 打印完成信息
    """
    # 打印项目标题
    print()
    print("╔" + "═" * 58 + "╗")
    print("║" + " " * 15 + "YOLO模型导出工具" + " " * 15 + "║")
    print("╚" + "═" * 58 + "╝")
    print()
    
    # 查找模型
    model_path = find_best_model()
    
    if model_path is None:
        return
    
    # 导出模型（默认导出ONNX格式）
    formats = ['onnx']
    exported_files = export_model(model_path, formats)
    
    # 显示模型目录内容
    print_model_info(Path('models'))
    
    # 打印完成信息
    print()
    print("=" * 60)
    print("✅ 导出完成!")
    print("=" * 60)
    
    if exported_files:
        print("\n导出的模型文件:")
        for f in exported_files:
            print(f"  - {f}")
    
    print("\n下一步:")
    print("1. 测试推理: python scripts/test_inference.py")
    print("2. 部署到Python服务: 将模型放入 python-service/models/")


# ============================================================
# 脚本入口点
# ============================================================
if __name__ == '__main__':
    main()
