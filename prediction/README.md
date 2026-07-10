# 脐橙病虫害风险预测 — 预测模型 Demo

## 项目背景

项目现有的风险预测功能使用**硬编码规则引擎**（if-else条件判断）加 RAG 防治建议，缺乏真正的**预测能力**。

本 Demo 证明：**可以使用历史数据（环境、土壤、病害检测、天气预报、物候期）训练逻辑回归模型，实现对未来7天果园整体病害风险的预测。**

## 统一有序多分类模型

不再为每种病害分别训练模型，而是**一个模型预测果园的整体风险等级**：

| 等级 | 含义 | 响应策略 |
|------|------|---------|
| **0 — 无风险** | 各病害爆发概率均很低 | 保持正常管理 |
| **1 — 低风险** | 个别病害有低概率爆发可能 | 常规监测 |
| **2 — 中风险** | 存在明显病害爆发条件 | 加强监测频率 |
| **3 — 高风险** | 多种病害爆发概率高 | 立即检查并准备防治 |

## 架构概览

```
                    ┌─────────────────┐
                    │ data_generator.py│   ← 模拟数据生成器（--unified 模式）
                    └───────┬─────────┘
                            │ CSV (800条, 4类标签)
                    ┌───────▼─────────┐
                    │    train.py     │   ← 训练管线 (multinomial LR)
                    └───────┬─────────┘
                            │ model_overall.pkl
                    ┌───────▼─────────┐
                    │ predict_demo.py │   ← 预测 Demo
                    └─────────────────┘
```

## 特征工程（6大特征组，45+ 维）

| 特征组 | 原始数据源 | 特征数 | 特征示例 |
|--------|-----------|--------|---------|
| **A. 环境统计量** | `environment_data` | 13 | 7天/14天温度均值、湿度均值、累积降雨、持续天数 |
| **B. 土壤数据** | `extra_data` (JSONB) | 4 | 土壤温度、含水率、PH值、电导率 |
| **C. 病害压力** | `disease_detection` | 12+1 | S14/S10/S3_S4 各4个特征（DPI/距上次检测/检测频次）+ 病害多样性 |
| **D. 交互特征** | 派生计算 | 5 | THI温湿指数、病害三角风险指数、高温高湿持续天数 |
| **E. 天气预报** | `weather_forecast` | 5 | 未来3天预报温度/湿度/降雨量 |
| **F. 物候期** | `farming_calendar` | 5 | one-hot编码（春梢/开花/膨大/着色/采收） |

> **核心公式**: 病害三角风险指数 = max(T-25,0) × max(RH-80,0) × max(DPI_S14, DPI_S10, DPI_S3_S4)

## 使用方式

```bash
# 1. 生成统一训练数据（800条，4个风险等级）
python data_generator.py --unified --n-samples 800 --seed 42

# 2. 训练统一有序多分类模型
python train.py --data disease_training_data_unified.csv --C 1.0

# 3. 预测 Demo（生成一条模拟样本）
python predict_demo.py --model models/model_overall.pkl

# 4. 对已有 CSV 批量预测
python predict_demo.py --model models/model_overall.pkl --data sample.csv
```

## 模型表现（使用模拟数据）

| 指标 | 值 | 说明 |
|------|-----|------|
| **准确率 (Accuracy)** | **0.7625** | 整体正确率 |
| **Macro F1** | **0.6902** | 各类F1的均值 |
| **Weighted F1** | **0.7701** | 按样本量加权 |
| **Quadratic Weighted Kappa** | **0.8749** | 有序分类核心指标 |

**混淆矩阵**（行=真实, 列=预测）:
```
           pred0  pred1  pred2  pred3
true0(无)   44      6      0      0
true1(低)    6     27      7      0
true2(中)    0      4     46     11
true3(高)    0      0      4      5
```

> 模型很好地学到了有序分类特性：预测很少偏离超过1个等级。

**Top-5 特征权重**（对高风险等级的正向影响）:
1. 果实膨大期物候: +2.06
2. 高温高湿持续≥3天: +2.55
3. 溃疡病检测频次: +0.63
4. 距上次检测天数(短=高风险): +0.73
5. 湿度最小值: +0.61

## 文件结构

```
prediction/
├── FEATURES.md                  # 特征设计文档
├── data_generator.py            # 模拟数据生成器
├── train.py                     # 模型训练管线
├── predict_demo.py              # 预测 Demo 脚本
├── disease_training_data_unified.csv  # 统一训练数据
├── feature_columns.json         # 特征名列表
└── models/
    ├── model_overall.pkl            # 统一预测模型
    ├── features_overall.json        # 特征列名
    ├── confusion_matrix.csv         # 混淆矩阵
    ├── feature_weights_class*.csv   # 每个风险等级的特征权重
    └── model_metrics.json           # 评估指标
```

## 后续整合路径

### 方案 A: Python 微服务（推荐）
```
Spring Boot --HTTP--> FastAPI 推理服务 --> 加载 model_overall.pkl --> 返回风险等级 0-3
```

### 方案 B: Java PMML 加载
```
Spring Boot --> jpmml-evaluator --> 加载模型.pmml
```

**推荐方案 A**：
- 项目已有 Python RAG 服务，可复用部署经验
- Python 特征工程生态丰富，便于特征提取
- 模型可热更新，无需重启 Java 服务

## 注意事项

1. **数据范围限制**: 只能用**自身果园**的历史数据预测自身果园，不可跨果园
2. **模拟数据局限**: 当前使用随机生成的数据，模型表现不代表真实水平
3. **模型选择**: 逻辑回归适合小样本快速验证，后续可升级为 XGBoost/LightGBM
4. **特征更新**: 随着数据积累，特征权重需要定期重新训练
5. **类别不平衡**: 高风险样本占比较小（~6%），真实数据中建议使用 SMOTE 或过采样
