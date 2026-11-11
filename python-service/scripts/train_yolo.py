#!/usr/bin/env python3
"""
YOLOv8 表情检测训练脚本
=======================

【功能说明】
支持两种模式:
  detect - YOLOv8n检测模型 (旧FER2013方案)
  cls    - YOLOv8n-cls分类模型 (新RAF-DB方案)

【硬件要求】
- GPU: RTX 4060 8GB (必须使用独显)
- 内存: 16GB
- 存储: 50GB SSD

【使用方法】
    python scripts/train_yolo.py --mode cls      # RAF-DB分类模型 (推荐)
    python scripts/train_yolo.py --mode detect    # FER2013检测模型 (旧)

【输出】
- 分类模型: runs/classify/emotion_*/weights/best.pt
- 检测模型: runs/detect/emotion_*/weights/best.pt
"""

import os
os.environ["CUDA_VISIBLE_DEVICES"] = "0"
os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"

import sys
import torch
import argparse
from pathlib import Path
from datetime import datetime

script_dir = Path(__file__).parent
project_root = script_dir.parent
os.chdir(project_root)


def check_gpu():
    """检查GPU状态，确保独显可用"""
    print("=" * 60)
    print("GPU 状态检查")
    print("=" * 60)

    if not torch.cuda.is_available():
        print("FATAL: 未检测到CUDA GPU!")
        print("请确保:")
        print("  1. 已安装NVIDIA显卡驱动")
        print("  2. 已安装CUDA Toolkit")
        print("  3. PyTorch已安装CUDA版本: pip install torch --index-url https://download.pytorch.org/whl/cu121")
        print("  4. 设置环境变量 CUDA_VISIBLE_DEVICES=0")
        sys.exit(1)

    gpu_name = torch.cuda.get_device_name(0)
    gpu_memory = torch.cuda.get_device_properties(0).total_memory / 1024**3

    print(f"  GPU: {gpu_name}")
    print(f"  显存: {gpu_memory:.1f} GB")
    print(f"  CUDA: {torch.version.cuda}")
    print(f"  cuDNN: {torch.backends.cudnn.version()}")

    if "RTX" in gpu_name or "GTX" in gpu_name or "RTX" in gpu_name:
        print(f"  状态: NVIDIA独显已激活")
    else:
        print(f"  警告: 当前GPU为 {gpu_name}，可能不是独显!")

    return True, gpu_memory


def check_dataset_cls():
    """检查RAF-DB分类数据集 (支持7类和8类)"""
    print("\n" + "=" * 60)
    print("数据集检查 (YOLOv8-cls 分类模式)")
    print("=" * 60)

    dataset_8class = Path('data/processed/rafdb_8class/dataset.yaml')
    dataset_7class = Path('data/processed/rafdb/dataset.yaml')

    if dataset_8class.exists():
        dataset_yaml = dataset_8class
        data_dir = Path('data/processed/rafdb_8class')
        emotions = ['angry', 'disgust', 'fear', 'happy', 'sad', 'surprise', 'neutral', 'unknown']
        print(f"  模式: 8类 (含unknown)")
    elif dataset_7class.exists():
        dataset_yaml = dataset_7class
        data_dir = Path('data/processed/rafdb')
        emotions = ['angry', 'disgust', 'fear', 'happy', 'sad', 'surprise', 'neutral']
        print(f"  模式: 7类 (原始)")
    else:
        print(f"数据集配置文件不存在")
        print("请先运行: python scripts/preprocess_rafdb.py 或 python scripts/assemble_8class.py")
        return False

    print(f"  数据集配置: {dataset_yaml}")

    train_dir = data_dir / 'train'
    val_dir = data_dir / 'val'

    if train_dir.exists():
        total_train = 0
        for emo in emotions:
            emo_dir = train_dir / emo
            if emo_dir.exists():
                count = len(list(emo_dir.glob('*')))
                total_train += count
                print(f"  train/{emo}: {count}")
            else:
                print(f"  train/{emo}: NOT FOUND")
                return False
        print(f"  训练集总计: {total_train}")
    else:
        print(f"训练集目录不存在: {train_dir}")
        return False

    if val_dir.exists():
        total_val = 0
        for emo in emotions:
            emo_dir = val_dir / emo
            if emo_dir.exists():
                count = len(list(emo_dir.glob('*')))
                total_val += count
            else:
                print(f"  val/{emo}: NOT FOUND")
                return False
        print(f"  验证集总计: {total_val}")
    else:
        print(f"验证集目录不存在: {val_dir}")
        return False

    print(f"  数据集总计: {total_train + total_val}")
    return True


def check_dataset_detect():
    """检查FER2013检测数据集"""
    print("\n" + "=" * 60)
    print("数据集检查 (YOLOv8 detect 模式)")
    print("=" * 60)

    dataset_yaml = Path('data/processed/fer2013_filtered/dataset.yaml')

    if not dataset_yaml.exists():
        print(f"数据集配置文件不存在: {dataset_yaml}")
        print("请先运行: python scripts/preprocess_fer2013_filtered.py")
        return False

    print(f"  数据集配置: {dataset_yaml}")

    train_images = Path('data/processed/fer2013_filtered/images/train')
    val_images = Path('processed/fer2013_filtered/images/val')

    if train_images.exists():
        count = len(list(train_images.glob('*.png')))
        print(f"  训练集: {count} 张")
    else:
        print(f"  训练集目录不存在: {train_images}")
        return False

    if val_images.exists():
        count = len(list(val_images.glob('*.png')))
        print(f"  验证集: {count} 张")
    else:
        print(f"  验证集目录不存在: {val_images}")
        return False

    return True


def train_cls(gpu_memory_gb):
    """YOLOv8-cls 分类模型训练"""
    from ultralytics import YOLO

    print("=" * 60)
    print("YOLOv8n-cls 表情分类训练 (RAF-DB)")
    print(f"开始时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    if not check_dataset_cls():
        return None, None

    if gpu_memory_gb >= 8:
        batch_size = 32
    elif gpu_memory_gb >= 6:
        batch_size = 16
    else:
        batch_size = 8

    print(f"\n  Batch Size: {batch_size}")

    dataset_8class = Path('data/processed/rafdb_8class/dataset.yaml')
    data_path = 'data/processed/rafdb_8class' if dataset_8class.exists() else 'data/processed/rafdb'
    n_classes = 8 if dataset_8class.exists() else 7
    print(f"\n  数据集路径: {data_path} ({n_classes}类)")

    print("\n  加载 YOLOv8n-cls 预训练模型...")
    model = YOLO('yolov8n-cls.pt')

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    run_name = f'emotion_cls_8class_{timestamp}'

    print("\n" + "=" * 60)
    print("训练配置")
    print("=" * 60)
    print(f"  模型: YOLOv8n-cls (分类模型, 3.2M参数)")
    print(f"  数据集: RAF-DB 8类 (含unknown, 17,500张)")
    print(f"  Epochs: 100")
    print(f"  Batch Size: {batch_size}")
    print(f"  图像尺寸: 224x224")
    print(f"  设备: GPU (RTX 4060)")
    print(f"  运行名称: {run_name}")
    print()

    print("  开始训练...")
    print()

    try:
        results = model.train(
            data=data_path,
            epochs=100,
            batch=batch_size,
            imgsz=224,
            device='0',
            project='runs/classify',
            name=run_name,
            exist_ok=True,
            save_period=10,
            patience=20,
            optimizer='AdamW',
            lr0=0.001,
            lrf=0.01,
            momentum=0.937,
            weight_decay=0.0005,
            cos_lr=True,
            warmup_epochs=3.0,
            warmup_momentum=0.8,
            hsv_h=0.01,
            hsv_s=0.5,
            hsv_v=0.4,
            fliplr=0.5,
            flipud=0.0,
            degrees=15.0,
            translate=0.1,
            scale=0.5,
            erasing=0.2,
            label_smoothing=0.1,
            amp=True,
            workers=4,
            verbose=True,
        )

        print()
        print("=" * 60)
        print("训练完成!")
        print("=" * 60)
        print(f"结束时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

        print("\n  评估模型...")
        metrics = model.val(device='0')

        print(f"\n  Top-1 Accuracy: {metrics.top1:.4f}")
        print(f"  Top-5 Accuracy: {metrics.top5:.4f}")

        best_model_path = f'runs/classify/{run_name}/weights/best.pt'
        print(f"\n  最佳模型: {best_model_path}")

        return model, metrics

    except Exception as e:
        print(f"\n训练失败: {e}")
        print("如果是显存不足(OOM)，尝试减小batch size:")
        print("  编辑 train_yolo.py 中的 batch_size 变量")
        return None, None


def train_detect(gpu_memory_gb):
    """YOLOv8n detect 模型训练"""
    from ultralytics import YOLO

    print("=" * 60)
    print("YOLOv8n 表情检测训练 (FER2013)")
    print(f"开始时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    if not check_dataset_detect():
        return None, None

    if gpu_memory_gb >= 8:
        batch_size = 16
    elif gpu_memory_gb >= 6:
        batch_size = 8
    else:
        batch_size = 4

    print(f"\n  Batch Size: {batch_size}")

    print("\n  加载 YOLOv8n 预训练模型...")
    last_checkpoint = Path('runs/detect/runs/detect/emotion_20260723_153050/weights/last.pt')
    if last_checkpoint.exists():
        print(f"  找到checkpoint: {last_checkpoint}")
        model = YOLO(str(last_checkpoint))
        resume = True
        run_name = 'emotion_20260723_153050'
    else:
        model = YOLO('yolov8n.pt')
        resume = False
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        run_name = f'emotion_{timestamp}'

    print("\n" + "=" * 60)
    print("训练配置")
    print("=" * 60)
    print(f"  模型: YOLOv8n (检测模型)")
    print(f"  数据集: FER2013")
    print(f"  Epochs: 100")
    print(f"  Batch Size: {batch_size}")
    print(f"  图像尺寸: 640x640")
    print(f"  设备: GPU (NVIDIA独显)")
    print()

    try:
        results = model.train(
            data='data/processed/fer2013_filtered/dataset.yaml',
            epochs=100,
            batch=batch_size,
            imgsz=640,
            device='0',
            project='runs/detect',
            name=run_name,
            exist_ok=True,
            save_period=10,
            patience=20,
            resume=resume,
            optimizer='AdamW',
            lr0=0.001,
            lrf=0.01,
            momentum=0.937,
            weight_decay=0.0005,
            cos_lr=True,
            warmup_epochs=3.0,
            warmup_momentum=0.8,
            hsv_h=0.0,
            hsv_s=0.0,
            hsv_v=0.4,
            fliplr=0.5,
            flipud=0.0,
            degrees=15.0,
            translate=0.1,
            scale=0.5,
            mosaic=1.0,
            close_mosaic=10,
            label_smoothing=0.1,
            amp=True,
            workers=4,
            verbose=True,
        )

        print()
        print("=" * 60)
        print("训练完成!")
        print("=" * 60)

        print("\n  评估模型...")
        metrics = model.val(device='0')

        print(f"\n  mAP50: {metrics.box.map50:.4f}")
        print(f"  mAP50-95: {metrics.box.map:.4f}")
        print(f"  Precision: {metrics.box.mp:.4f}")
        print(f"  Recall: {metrics.box.mr:.4f}")

        best_model_path = f'runs/detect/{run_name}/weights/best.pt'
        print(f"\n  最佳模型: {best_model_path}")

        return model, metrics

    except Exception as e:
        print(f"\n训练失败: {e}")
        return None, None


def export_model(model, mode):
    """导出模型为ONNX格式"""
    print("\n" + "=" * 60)
    print("模型导出")
    print("=" * 60)

    if model is None:
        print("  无可用模型，跳过导出")
        return

    print("\n  导出ONNX格式...")
    try:
        onnx_path = model.export(format='onnx', device='0')
        print(f"  ONNX模型: {onnx_path}")

        import shutil
        models_dir = Path('models')
        models_dir.mkdir(exist_ok=True)

        if mode == 'cls':
            src = Path(f'runs/classify/*/weights/best.pt')
            import glob
            candidates = sorted(glob.glob(str(src)))
            if candidates:
                shutil.copy2(candidates[-1], models_dir / 'yolov8n_emotion_cls.pt')
                print(f"  PyTorch模型已复制: models/yolov8n_emotion_cls.pt")
        else:
            import glob
            candidates = sorted(glob.glob('runs/detect/*/weights/best.pt'))
            if candidates:
                shutil.copy2(candidates[-1], models_dir / 'yolov8n_emotion_detect.pt')
                print(f"  PyTorch模型已复制: models/yolov8n_emotion_detect.pt")

    except Exception as e:
        print(f"  导出失败: {e}")


def main():
    parser = argparse.ArgumentParser(description='YOLOv8 表情检测训练')
    parser.add_argument('--mode', type=str, default='cls', choices=['cls', 'detect'],
                        help='训练模式: cls=分类模型(RAF-DB), detect=检测模型(FER2013)')
    args = parser.parse_args()

    print()
    print("=" * 62)
    print("  YOLOv8 表情检测训练系统  (必须使用NVIDIA独显)")
    print("=" * 62)
    print()

    has_gpu, gpu_memory = check_gpu()

    if args.mode == 'cls':
        model, metrics = train_cls(gpu_memory)
    else:
        model, metrics = train_detect(gpu_memory)

    if model is not None:
        export_model(model, args.mode)

    print()
    print("=" * 60)
    print("所有任务完成!")
    print("=" * 60)


if __name__ == '__main__':
    main()
