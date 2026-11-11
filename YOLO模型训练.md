# YOLO表情检测模型训练指南

> **训练结果与优化方案**：[训练记录与优化方案.md](训练记录与优化方案.md)

## 一、环境准备

### 1.1 硬件要求

| 组件 | 最低配置 | 推荐配置 | 说明 |
|------|---------|---------|------|
| GPU | GTX 1660 6GB | RTX 4060 8GB | 必须使用NVIDIA独显 |
| 内存 | 8GB | 16GB | 数据预处理需要 |
| 存储 | 20GB SSD | 50GB SSD | 数据集+模型+训练输出 |

**本项目配置**: RTX 4060 Laptop 8GB ✅（必须使用独显，不可用CPU+核显）

### 1.2 软件依赖

```bash
# Python环境 (推荐3.10+)
python --version  # 确认Python 3.10+

# 创建虚拟环境
python -m venv venv
venv\Scripts\activate  # Windows
# source venv/bin/activate  # Linux/Mac

# 安装PyTorch (CUDA 11.8)
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118

# 安装ultralytics (YOLOv8)
pip install ultralytics

# 安装其他依赖
pip install pillow numpy pandas matplotlib opencv-python
```

### 1.3 检查GPU可用性

```python
# 检查CUDA是否可用
import torch
print(f"CUDA可用: {torch.cuda.is_available()}")
print(f"GPU设备: {torch.cuda.get_device_name(0)}")
print(f"显存大小: {torch.cuda.get_device_properties(0).total_memory / 1024**3:.1f} GB")
```

### 1.4 项目目录结构

```
D:\表情检测\
├── RAF-DB/                              # RAF-DB原始数据集
│   └── basic/
│       ├── Image/aligned/               # 15,339张已对齐人脸图片
│       └── EmoLabel/list_patition_label.txt  # 标签文件
│
└── python-service/
    ├── data/
    │   └── processed/
    │       └── rafdb/                   # 处理后的分类目录格式（当前使用）
    │           ├── train/
    │           │   ├── angry/
    │           │   ├── disgust/
    │           │   ├── fear/
    │           │   ├── happy/
    │           │   ├── sad/
    │           │   ├── surprise/
    │           │   └── neutral/
    │           ├── val/
    │           │   ├── angry/
    │           │   ├── disgust/
    │           │   ├── fear/
    │           │   ├── happy/
    │           │   ├── sad/
    │           │   ├── surprise/
    │           │   └── neutral/
    │           └── dataset.yaml
    ├── models/                          # 训练好的模型
    ├── scripts/
    │   ├── preprocess_rafdb.py          # RAF-DB预处理脚本
    │   ├── preprocess_fer2013_filtered.py  # 旧FER2013预处理（仅供参考）
    │   ├── train_yolo.py                # 训练脚本（支持cls/detect两种模式）
    │   ├── evaluate_model.py            # 评估脚本
    │   ├── test_inference.py            # 测试脚本
    │   └── export_model.py              # 导出脚本
    └── runs/
        └── classify/                    # 分类模型训练输出
            └── emotion_cls_YYYYMMDD_HHMMSS/
```

---

## 二、数据集准备

### 2.1 RAF-DB数据集介绍

**RAF-DB** (Real-world Affective Faces Database) 是一个真实场景的人脸表情数据集。

| 属性 | 说明 |
|------|------|
| 总图片数 | 15,339张 |
| 图片尺寸 | 不固定（已对齐的高清人脸） |
| 颜色模式 | RGB彩色图 (3通道) |
| 类别数量 | 7种基本表情 |
| 数据格式 | JPG图片 + 标签文本文件 |
| 人脸对齐 | 已完成（aligned版本） |

### 2.2 下载地址

RAF-DB通过国内渠道下载，存放在 `D:\表情检测\RAF-DB\`

### 2.3 数据集分布

| 表情 | 英文标签 | 原始数量 | 筛选后 | 采样策略 |
|------|---------|---------|--------|---------|
| 生气 | angry | 867 | 867 | 全保留 |
| 厌恶 | disgust | 877 | 877 | 全保留 |
| 恐惧 | fear | 355 | 355 | 全保留 |
| 开心 | happy | 5,957 | 2,911 | 降采样 |
| 悲伤 | sad | 2,460 | 2,460 | 全保留 |
| 惊讶 | surprise | 1,619 | 1,619 | 全保留 |
| 平静 | neutral | 3,204 | 2,911 | 降采样 |
| **总计** | | **15,339** | **12,000** | |

**采样策略**: 少数类全保留 + 多数类等量补足

### 2.4 RAF-DB标签格式

标签文件 `list_patition_label.txt` 每行格式：`图片名 标签编号`

```
train_00001.jpg 5
train_00002.jpg 5
train_00003.jpg 4
test_00001.jpg 1
test_00002.jpg 6
```

标签编号映射（RAF-DB → 项目统一）：

| RAF-DB编号 | 含义 | 项目编号 | 英文名 |
|-----------|------|---------|--------|
| 1 | 惊讶 | 5 | surprise |
| 2 | 恐惧 | 2 | fear |
| 3 | 厌恶 | 1 | disgust |
| 4 | 开心 | 3 | happy |
| 5 | 悲伤 | 4 | sad |
| 6 | 生气 | 0 | angry |
| 7 | 平静 | 6 | neutral |

---

## 三、数据预处理

### 3.1 YOLOv8-cls分类目录格式

与旧的YOLO detect格式不同，YOLOv8-cls分类模型要求数据按以下格式组织：

```
rafdb/
├── train/
│   ├── angry/       (693张)
│   ├── disgust/     (701张)
│   ├── fear/        (284张)
│   ├── happy/       (2,328张)
│   ├── sad/         (1,968张)
│   ├── surprise/    (1,295张)
│   └── neutral/     (2,328张)
├── val/
│   ├── angry/       (174张)
│   ├── disgust/     (176张)
│   ├── fear/        (71张)
│   ├── happy/       (583张)
│   ├── sad/         (492张)
│   ├── surprise/    (324张)
│   └── neutral/     (583张)
└── dataset.yaml
```

**关键区别**：
- 不需要 labels/ 目录（分类任务用文件夹名作为标签）
- 图片直接放在对应类别的文件夹下
- 训练时 `data` 参数传目录路径，不是yaml文件路径

### 3.2 预处理脚本

**文件**: `scripts/preprocess_rafdb.py`

**使用方法**:
```bash
cd python-service
python scripts/preprocess_rafdb.py
```

**脚本功能**:
1. 读取 `RAF-DB/basic/EmoLabel/list_patition_label.txt` 标签文件
2. 将RAF-DB标签编号(1-7)映射为项目统一编号(0-6)
3. 按配置筛选12,000张图片（少数类全保留+多数类等量补足）
4. 从 `aligned/` 目录复制图片到分类目录格式
5. 按80/20比例划分训练集/验证集
6. 生成 `dataset.yaml` 配置文件

**输出**: `data/processed/rafdb/`

### 3.3 数据集配置文件

**文件**: `data/processed/rafdb/dataset.yaml`

```yaml
# RAF-DB 分类数据集配置
# 12000张图片，7类表情

path: D:\表情检测\python-service\data\processed\rafdb
train: train
val: val

nc: 7

names:
  0: angry      # 生气
  1: disgust    # 厌恶
  2: fear       # 恐惧
  3: happy      # 开心
  4: sad        # 悲伤
  5: surprise   # 惊讶
  6: neutral    # 平静
```

---

## 四、模型训练

### 4.1 训练脚本

**文件**: `scripts/train_yolo.py`

支持两种模式：
- `--mode cls`：YOLOv8n-cls 分类模型（当前使用，推荐）
- `--mode detect`：YOLOv8n 检测模型（旧方案，仅供参考）

**使用方法**:
```bash
cd python-service

# 分类模型训练（推荐）
python scripts/train_yolo.py --mode cls

# 检测模型训练（旧方案）
python scripts/train_yolo.py --mode detect
```

### 4.2 训练配置（YOLOv8n-cls 分类模式）

| 参数 | 值 | 说明 |
|------|-----|------|
| 模型 | YOLOv8n-cls | nano分类模型，1.4M参数 |
| 数据集 | RAF-DB | 12,000张高清彩色人脸 |
| epochs | 100 | 最大训练轮次 |
| batch | 16 | 批次大小 |
| imgsz | 224 | 输入图像尺寸（分类标准） |
| device | 0 | **强制使用RTX 4060独显** |
| optimizer | AdamW | 优化器 |
| lr0 | 0.001 | 初始学习率 |
| lrf | 0.01 | 最终学习率比例 |
| cos_lr | True | 余弦退火调度 |
| warmup_epochs | 3.0 | 学习率预热 |
| patience | 20 | 早停轮次 |
| label_smoothing | 0.1 | 标签平滑 |
| hsv_h | 0.01 | 色调增强（彩色图微调） |
| hsv_s | 0.5 | 饱和度增强 |
| hsv_v | 0.4 | 亮度增强 |
| fliplr | 0.5 | 左右翻转 |
| degrees | 15.0 | 旋转角度 |
| erasing | 0.2 | 随机擦除 |
| amp | True | 混合精度训练 |

### 4.3 GPU强制使用配置

训练脚本在启动时会强制设置以下环境变量，确保使用RTX 4060独显：

```python
import os
os.environ["CUDA_VISIBLE_DEVICES"] = "0"
os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"
```

同时在训练调用中指定 `device='0'`。

**如果检测不到GPU，脚本会直接报错退出，不会回退到CPU训练。**

### 4.4 超参数配置说明

| 参数 | 默认值 | 推荐值 | 说明 |
|------|--------|--------|------|
| epochs | 100 | 100 | 训练轮次（实际约38轮收敛） |
| batch | 16 | 16 | 批次大小（8GB显存） |
| imgsz | 224 | 224 | 图像尺寸（分类标准输入） |
| lr0 | 0.01 | 0.001 | 初始学习率 |
| lrf | 0.01 | 0.01 | 最终学习率比例 |
| optimizer | SGD | AdamW | 优化器 |
| patience | 100 | 20 | 早停轮次 |
| cos_lr | False | True | 余弦学习率调度 |
| label_smoothing | 0.0 | 0.1 | 标签平滑 |
| hsv_h | 0.015 | 0.01 | 色调（彩色图微调） |
| hsv_s | 0.7 | 0.5 | 饱和度 |
| hsv_v | 0.4 | 0.4 | 亮度 |
| fliplr | 0.5 | 0.5 | 左右翻转 |
| flipud | 0.0 | 0.0 | 不上下翻转 |
| degrees | 0.0 | 15.0 | 旋转角度 |
| erasing | 0.0 | 0.2 | 随机擦除 |

### 4.5 CLI训练命令（备选）

```bash
# 分类模型训练
yolo classify train ^
    data=data/processed/rafdb ^
    model=yolov8n-cls.pt ^
    epochs=100 ^
    imgsz=224 ^
    batch=16 ^
    device=0 ^
    patience=20 ^
    optimizer=AdamW ^
    lr0=0.001 ^
    cos_lr=True ^
    label_smoothing=0.1 ^
    amp=True ^
    project=runs/classify ^
    name=emotion_cls
```

### 4.6 显存不足时的调整方案

**如果遇到显存不足 (OOM) 错误**:

```python
# 方案1: 减小batch
batch=8  # 或 batch=4

# 方案2: 减小图像尺寸（不推荐，分类模型标准为224）
imgsz=160

# 方案3: 使用自动batch
batch=-1  # 自动选择最大batch

# 方案4: 使用更小的模型（不推荐，nano已是最小）
model='yolov8n-cls.pt'  # nano版本
```

---

## 五、模型评估

### 5.1 评估结果

**当前最佳模型**: YOLOv8n-cls + RAF-DB

#### 整体指标

| 指标 | 值 |
|------|-----|
| **Top-1 Accuracy** | **82.4%** |
| **Top-5 Accuracy** | **99.4%** |

#### 各类别精度

| 表情 | 精度 | 正确/总数 | 分析 |
|------|------|----------|------|
| happy | **92.2%** | 529/574 | 最强，特征明显 |
| angry | **84.1%** | 137/163 | 较好 |
| neutral | **82.8%** | 283/342 | 较好 |
| surprise | **82.6%** | 389/471 | 较好 |
| sad | **78.6%** | 498/634 | 中等，常与surprise混淆 |
| disgust | **66.2%** | 104/157 | 较弱，与angry/sad混淆 |
| fear | **66.1%** | 41/62 | 最弱，样本少(仅355张) |

#### 混淆矩阵

```
              angry   disgust      fear     happy       sad  surprise   neutral
     angry      137         9         6         2         0         4         5
   disgust       15       104         1         8        14        11         4
      fear        4         1        41         0         2         2        12
     happy        2        12         3       529        13        14         1
       sad        5        29         1        25       498        61        15
  surprise        3        17        13        11        34       389         4
   neutral        8         4         6         8        22        11       283
```

### 5.2 与旧模型对比

| 对比维度 | 旧模型 (FER2013) | 新模型 (RAF-DB) | 提升 |
|---------|-----------------|----------------|------|
| 数据集 | FER2013 (48x48灰度) | RAF-DB (高清彩色对齐) | + |
| 模型类型 | detect (检测) | cls (分类) | + |
| 输入尺寸 | 640x640 | 224x224 | + |
| mAP50 / Top-1 | 0.576 | **0.824** | **+24.8%** |
| 训练时间 | ~4小时 | ~13分钟 | 18x更快 |
| 模型大小 | 6.3MB | 3.0MB | 更小 |

### 5.3 训练趋势

| Epoch | Top-1 Accuracy | Train Loss |
|-------|---------------|------------|
| 1 | 61.3% | 1.409 |
| 10 | 76.5% | 0.654 |
| 20 | 79.4% | 0.494 |
| 30 | 80.8% | 0.385 |
| **38** | **82.4%** | **0.325** |
| 58 | 82.2% | 0.201 |

最佳模型在 epoch 38 达到，之后触发 early stopping。

---

## 六、模型导出与部署

### 6.1 模型导出

训练完成后自动导出ONNX格式。手动导出：

```python
from ultralytics import YOLO

model = YOLO('runs/classify/emotion_cls_*/weights/best.pt')
onnx_path = model.export(format='onnx', device='0')
```

### 6.2 导出格式对比

| 格式 | 用途 | 优点 | 缺点 |
|------|------|------|------|
| .pt | PyTorch训练 | 原生格式 | 需要PyTorch |
| .onnx | 跨平台部署 | 通用格式 | 推理略慢 |
| .engine | NVIDIA GPU | TensorRT加速 | 仅NVIDIA |
| .torchscript | PyTorch部署 | 无Python依赖 | 仅PyTorch |

**推荐**: 导出为 `.onnx` 格式，兼容性最好。

### 6.3 最终模型文件结构

```
models/
└── yolov8n_emotion_cls.pt         # PyTorch格式 (3.0MB)

runs/classify/emotion_cls_*/weights/
├── best.pt                         # PyTorch格式，最佳验证集表现 (3.0MB)
├── last.pt                         # 最后一个epoch的checkpoint
└── best.onnx                       # ONNX格式 (5.5MB)
```

---

## 七、推理测试

### 7.1 分类模型推理

```python
from ultralytics import YOLO

# 加载分类模型
model = YOLO('runs/classify/emotion_cls_*/weights/best.pt')

# 推理（传入图片路径或目录）
results = model.predict(source='test.jpg', device='0')

# 解析结果
for result in results:
    # 分类模型返回类别概率
    probs = result.probs  # 分类概率
    cls = int(probs.top1)  # 最可能的类别
    conf = float(probs.top1conf)  # 置信度

    names = ['angry', 'disgust', 'fear', 'happy', 'sad', 'surprise', 'neutral']
    print(f"预测: {names[cls]} ({conf:.2%})")
```

### 7.2 单张图片推理

```python
from ultralytics import YOLO

model = YOLO('runs/classify/emotion_cls_*/weights/best.pt')
results = model.predict(source='test.jpg', conf=0.5, device='0', save=True)

for result in results:
    probs = result.probs
    print(f"类别: {probs.top1}, 置信度: {probs.top1conf:.2%}")
```

### 7.3 批量推理

```python
from ultralytics import YOLO

model = YOLO('runs/classify/emotion_cls_*/weights/best.pt')
results = model.predict(source='test_images/', device='0', save=True)

for result in results:
    probs = result.probs
    names = ['angry', 'disgust', 'fear', 'happy', 'sad', 'surprise', 'neutral']
    print(f"{result.path}: {names[probs.top1]} ({probs.top1conf:.2%})")
```

---

## 八、训练输出文件

训练完成后，在 `runs/classify/emotion_cls_YYYYMMDD_HHMMSS/` 目录下生成以下文件:

```
runs/classify/emotion_cls_20260724_101434/
├── weights/
│   ├── best.pt              # 最佳模型权重 (3.0MB)
│   ├── last.pt              # 最后一个epoch的权重
│   └── best.onnx            # ONNX格式 (5.5MB)
├── results.csv              # 训练结果CSV
├── confusion_matrix.png     # 混淆矩阵
├── confusion_matrix_normalized.png  # 归一化混淆矩阵
├── args.yaml                # 训练参数
└── train_batch*.jpg         # 训练样本可视化
```

**关键文件说明**:
- `best.pt`: 最佳模型，用于后续部署
- `results.csv`: 可用于分析训练过程（包含每个epoch的loss、accuracy等）
- `confusion_matrix.png`: 查看各类别识别效果和混淆情况

---

## 九、常见问题与解决方案

### 9.1 显存不足 (Out of Memory)

**症状**: `RuntimeError: CUDA out of memory`

**解决方案**:
```python
# 1. 减小batch
batch=8  # 或 batch=4

# 2. 减小图像尺寸（不推荐）
imgsz=160

# 3. 使用自动batch
batch=-1

# 4. 清理GPU缓存
import torch
torch.cuda.empty_cache()
```

### 9.2 过拟合

**症状**: 训练损失下降，但验证损失上升

**解决方案**:
```python
# 1. 使用标签平滑
label_smoothing=0.1

# 2. 早停
patience=20

# 3. 增加数据增强
erasing=0.2
fliplr=0.5
degrees=15.0

# 4. 增加数据量（对少数类别进行过采样）
```

### 9.3 类别不均衡

**症状**: 少数类别 (如fear、disgust) 识别效果差

**当前状况**:
- fear: 355张（最少），精度66.1%
- disgust: 877张，精度66.2%

**解决方案**:
```python
# 1. 已采用：少数类全保留采样策略
# fear(355), disgust(877), angry(867) 全部保留

# 2. 可尝试：使用更大模型
model='yolov8s-cls.pt'  # small版本，11.2M参数

# 3. 可尝试：增加训练数据
# 从其他数据源补充fear/disgust样本
```

### 9.4 训练速度慢

**症状**: 训练时间过长

**解决方案**:
```python
# 1. 必须使用GPU训练（独显）
device='0'  # 指定RTX 4060

# 2. 启用混合精度
amp=True

# 3. 增大batch（在显存允许范围内）
batch=32  # 8GB显存下可尝试

# 4. 确保CUDA环境正确安装
import torch
print(torch.cuda.is_available())  # 必须为True
```

### 9.5 CUDA不可用

**症状**: 脚本报错 `FATAL: 未检测到CUDA GPU!`

**解决方案**:
```bash
# 1. 确认已安装NVIDIA显卡驱动
nvidia-smi

# 2. 确认已安装CUDA Toolkit
nvcc --version

# 3. 重新安装PyTorch CUDA版本
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118

# 4. 确认PyTorch CUDA版本匹配
python -c "import torch; print(torch.version.cuda)"
```

### 9.6 分类模型数据格式问题

**症状**: `Dataset error: Classification datasets must be a directory`

**原因**: YOLOv8-cls 的 `data` 参数必须传**目录路径**，不是yaml文件路径

**解决方案**:
```python
# 错误写法
model.train(data='data/processed/rafdb/dataset.yaml')  # 不行

# 正确写法
model.train(data='data/processed/rafdb')  # 传目录路径
```

---

## 十、完整执行步骤

### 步骤1：确认环境

```bash
# 检查Python
python --version

# 检查GPU
python -c "import torch; print(torch.cuda.is_available(), torch.cuda.get_device_name(0))"

# 检查ultralytics
python -c "import ultralytics; print(ultralytics.__version__)"
```

### 步骤2：预处理RAF-DB数据集

```bash
cd python-service
python scripts/preprocess_rafdb.py
```

**输出**: `data/processed/rafdb/` 目录（12,000张图片，分类目录格式）

### 步骤3：开始训练（必须使用独显）

```bash
cd python-service
python scripts/train_yolo.py --mode cls
```

**预期结果**:
- 训练时间: ~13分钟
- Top-1 Accuracy: ~82%
- 模型保存: `runs/classify/emotion_cls_*/weights/best.pt`

### 步骤4：查看训练结果

```bash
# 查看训练日志
type runs\classify\emotion_cls_*\results.csv

# 查看混淆矩阵
start runs\classify\emotion_cls_*\confusion_matrix_normalized.png
```

### 步骤5：导出模型

训练完成后自动导出ONNX。手动导出：

```bash
python scripts/export_model.py
```

### 步骤6：复制模型到部署目录

```bash
copy runs\classify\emotion_cls_*\weights\best.pt models\yolov8n_emotion_cls.pt
copy runs\classify\emotion_cls_*\weights\best.onnx models\yolov8n_emotion_cls.onnx
```

---

## 十一、参考资源

### 官方文档
- Ultralytics YOLOv8: https://docs.ultralytics.com/
- RAF-DB数据集: http://whdeng.cn/RAFDB.html

### 项目脚本

| 脚本 | 用途 | 命令 |
|------|------|------|
| `preprocess_rafdb.py` | RAF-DB数据预处理 | `python scripts/preprocess_rafdb.py` |
| `train_yolo.py --mode cls` | 分类模型训练（推荐） | `python scripts/train_yolo.py --mode cls` |
| `train_yolo.py --mode detect` | 检测模型训练（旧） | `python scripts/train_yolo.py --mode detect` |
| `evaluate_model.py` | 模型评估 | `python scripts/evaluate_model.py` |
| `test_inference.py` | 推理测试 | `python scripts/test_inference.py` |
| `export_model.py` | 模型导出ONNX | `python scripts/export_model.py` |

---

## 十二、下一步

当前状态：
1. ✅ RAF-DB数据集准备完成（12,000张）
2. ✅ YOLOv8n-cls分类模型训练完成（Top-1=82.4%）
3. ✅ ONNX模型导出完成

后续开发：
1. 搭建两阶段推理管线（MTCNN人脸检测 + YOLOv8-cls分类）
2. 搭建Python FastAPI推理服务
3. 开发知识库检索模块（FAISS）
4. 开发Java SpringBoot后端
5. 开发Vue3前端界面
6. 集成LLM生成建议（DeepSeek + Qwen）

---

**文档版本**: v2.0
**适用模型**: YOLOv8n-cls (分类模型)
**适用数据集**: RAF-DB
**适用GPU**: RTX 4060 8GB（必须使用独显）
