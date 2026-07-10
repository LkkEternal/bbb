# 脐橙病虫害风险预测 — 特征设计文档

## 1. 问题定义

**目标**: 对每个果园，预测**未来 7 天的整体病害风险等级**（有序多分类）。

| 等级 | 标签 | 含义 |
|------|------|------|
| 0 | 无风险 | 未来7天各病害爆发概率均很低 |
| 1 | 低风险 | 个别病害有低概率爆发可能 |
| 2 | 中风险 | 存在明显病害爆发条件，需关注 |
| 3 | 高风险 | 多种病害爆发概率高，需立即行动 |

- **输出**: 4个类别的概率分布 $[p_0, p_1, p_2, p_3]$，取 $\arg\max$ 判定等级
- **约束**: 只能用**自身果园**的历史数据预测自身果园，不使用其他果园数据。

---

## 2. 特征总览（全量维度）

每条训练样本 = **某个果园 × 某个时间点** 的综合特征（不区分病害类型）。

共 **6 大特征组，约 45-50 维特征**（含各病害独立压力特征）。

### 2.1 环境统计量（A组） — 数据源: `environment_data`

| 特征名 | 说明 | 窗口 | 公式 |
|--------|------|------|------|
| `temp_mean_7d` | 过去7天平均温度 | 7天 | $\frac{1}{n}\sum_{i=t-7}^{t} T_i$ |
| `temp_max_7d` | 过去7天最高温度 | 7天 | $\max(T_{t-7}, ..., T_t)$ |
| `temp_min_7d` | 过去7天最低温度 | 7天 | $\min(T_{t-7}, ..., T_t)$ |
| `temp_std_7d` | 过去7天温度标准差 | 7天 | $\sqrt{\frac{1}{n}\sum(T_i - \bar{T})^2}$ |
| `humidity_mean_7d` | 过去7天平均湿度 | 7天 | $\frac{1}{n}\sum_{i=t-7}^{t} RH_i$ |
| `humidity_max_7d` | 过去7天最高湿度 | 7天 | — |
| `humidity_min_7d` | 过去7天最低湿度 | 7天 | — |
| `precip_cumul_7d` | 过去7天累积降雨量 | 7天 | $\sum_{i=t-7}^{t} Precip_i$ |
| `precip_days_7d` | 过去7天降雨天数 | 7天 | $\sum_{i=t-7}^{t} [Precip_i > 0.1mm]$ |
| `wind_mean_7d` | 过去7天平均风速 | 7天 | — |
| `temp_mean_14d` | 过去14天平均温度 | 14天 | 同上，窗口扩展 |
| `humidity_mean_14d` | 过去14天平均湿度 | 14天 | — |
| `precip_cumul_14d` | 过去14天累积降雨 | 14天 | — |

### 2.2 土壤数据（B组） — 数据源: `environment_data.extra_data`

| 特征名 | 说明 | 窗口 | 公式 |
|--------|------|------|------|
| `soil_temp_mean_7d` | 过去7天平均土壤温度 | 7天 | $\frac{1}{n}\sum soil\_temp_i$ |
| `soil_moisture_mean_7d` | 过去7天平均土壤含水率 | 7天 | — |
| `soil_ph_mean_7d` | 过去7天平均PH值 | 7天 | — |
| `soil_conductivity_mean_7d` | 过去7天平均电导率 | 7天 | — |

### 2.3 病害压力指数（C组） — 数据源: `disease_detection`

| 特征名 | 说明 | 窗口 | 公式 |
|--------|------|------|------|
| `dpi_{disease_code}_7d` | 某病害近7天压力指数（时间衰减） | 7天 | $DPI_d = \sum_{i=t-7}^{t} count_{d,i} \cdot e^{-0.3 \cdot (t-i)}$ |
| `dpi_{disease_code}_14d` | 某病害近14天压力指数 | 14天 | 同上，窗口扩展 |
| `disease_diversity_14d` | 近14天病害多样性 | 14天 | Shannon指数 $-\sum p_j \ln(p_j)$ |
| `days_since_last_{disease}` | 距上次检测到该病害的天数 | — | $t - \max(\{t_i \mid disease = d\})$ |
| `detection_freq_7d` | 近7天检测总频次 | 7天 | $\sum count_{all, i}$ |

### 2.4 交互与派生特征（D组）

| 特征名 | 说明 | 公式 |
|--------|------|------|
| `th_index_7d` | 温湿度综合指数 | $THI = T - 0.55 \times (1 - RH/100) \times (T - 14.5)$ |
| `heat_humid_days_7d` | 高温高湿持续天数 | $\sum_{i=t-7}^{t} [T_i > 25 \land RH_i > 80]$ |
| `disease_risk_index` | 病害三角综合风险指数 | $(T-25)_+ \cdot (RH-80)_+ \cdot DPI_d$ |
| `temp_humidity_interact` | 温度×湿度交互项 | $T_{mean} \cdot RH_{mean} / 100$ |
| `cooling_days_since_last` | 距上次检测天数×温湿条件 | $daysSinceLast \cdot (T-25)_+$ |

> **病害三角（Disease Triangle）**: 环境(Environment) + 宿主(Host) + 病原体(Pathogen) 三者叠加 = 病害爆发。本项目聚焦环境和病原体历史。

### 2.5 天气预报因子（E组） — 数据源: `weather_forecast`

| 特征名 | 说明 | 窗口 | 公式 |
|--------|------|------|------|
| `forecast_temp_mean_3d` | 未来3天预报平均温度 | 未来3天 | — |
| `forecast_humidity_mean_3d` | 未来3天预报平均湿度 | 未来3天 | — |
| `forecast_pop_mean_3d` | 未来3天平均降水概率 | 未来3天 | — |
| `forecast_precip_cumul_3d` | 未来3天预报累积雨量 | 未来3天 | $\sum_{i=t+1}^{t+3} Precip_i$ |
| `forecast_precip_days_3d` | 未来3天预报降雨天数 | 未来3天 | — |

### 2.6 物候期因子（F组） — 数据源: `farming_calendar`

| 特征名 | 说明 | 编码方式 |
|--------|------|----------|
| `phenology_春梢萌发期` | 是否处于春梢萌发期 | one-hot [0/1] |
| `phenology_开花期` | 是否处于开花期 | one-hot [0/1] |
| `phenology_果实膨大期` | 是否处于果实膨大期 | one-hot [0/1] |
| `phenology_果实着色期` | 是否处于果实着色期 | one-hot [0/1] |
| `phenology_成熟采收期` | 是否处于成熟采收期 | one-hot [0/1] |

---

## 3. 标签定义（有序多分类）

```python
# 对 (果园o, 时间点t) 二元组:
# 1) 分别计算各病害在未来7天的爆发概率
# 2) 取最大概率作为整体风险分数
# 3) 按阈值映射到 0-3 等级

max_prob = max(prob_溃疡病, prob_炭疽病, prob_黄龙病)

if max_prob < 0.2:
    label = 0  # 无风险
elif max_prob < 0.4:
    label = 1  # 低风险
elif max_prob < 0.7:
    label = 2  # 中风险
else:
    label = 3  # 高风险
```

- **预测窗口**: 未来 7 天
- **预测目标**: 果园整体病害活跃度（综合考虑溃疡病/炭疽病/黄龙病）

---

## 4. 数据源映射

| 特征组 | 项目表 | 查询方式 |
|--------|--------|----------|
| A 环境统计量 | `environment_data` | `WHERE orchard_id=? AND record_time BETWEEN ? AND ?` |
| B 土壤数据 | `environment_data.extra_data` | 同上，解析 JSONB |
| C 病害压力 | `disease_detection` | `WHERE orchard_id=? AND device_timestamp BETWEEN ? AND ?` |
| D 交互特征 | 派生自 A+B+C | 内存计算 |
| E 天气预报 | `weather_forecast` | `WHERE orchard_id=? AND fxTime BETWEEN ? AND ?` |
| F 物候期 | `farming_calendar` | 根据当前日期匹配 start_month/day ~ end_month/day |
| 标签 | `disease_detection` | 未来7天窗口 |

---

## 5. 模拟数据生成策略

由于短期内无法获取真实数据，采用**参数化随机生成**方式创建训练数据集：

### 生成原则
1. **环境数据**基于正态分布，参数参考柑橘主产区（江西赣南）气候统计值
2. **病害检测**基于 Poisson 分布，均值按病害频次高低设定
3. **标签**通过带噪声的逻辑函数生成，确保特征与标签之间有已知但非确定性的相关关系
4. **正负样本比例**控制在 1:3 ~ 1:4，模拟病害爆发的稀有性

### 随机参数依据（农业文献参考值）

| 参数 | 分布 | 依据 |
|------|------|------|
| 气温 (°C) | Normal(22, 6) | 赣南年均温 18-19°C，春夏波动范围 |
| 湿度 (%) | Normal(75, 15) | 赣南年均湿度 75-80% |
| 降雨量 (mm/天) | Exponential(3) | 赣南年降雨 1400-1700mm，春夏集中 |
| 土壤温度 (°C) | Normal(20, 4) | 比气温低 2-3°C |
| 土壤含水率 (%) | Normal(35, 10) | 红壤含水率范围 |
| 溃疡病检测次数/周 | Poisson(1.2) | 春夏高发期频次 |
| 炭疽病检测次数/周 | Poisson(0.8) | 中等频次 |
| 黄龙病检测次数/周 | Poisson(0.3) | 低频次（慢性病） |

### 标签生成策略（统一模型）

不再按单个病害生成二元标签。改为以下步骤：

1. 对 S14(溃疡病)、S10(炭疽病)、S3_S4(黄龙病) 分别用各自的逻辑回归系数计算爆发概率
2. 三种病害共享同一套基础环境特征（温度/湿度/降雨/土壤等），但各有独立的病害压力特征（DPI值）
3. 取三者的最大概率作为整体风险分数
4. 按阈值 `[0.2, 0.4, 0.7]` 映射到 0/1/2/3

```python
# 对每种病害独立计算概率
溃疡病风险概率 = sigmoid(intercept_S14 + Σ(coef_S14[i] * feature_i))
炭疽病风险概率 = sigmoid(intercept_S10 + Σ(coef_S10[i] * feature_i))
黄龙病风险概率 = sigmoid(intercept_S3_S4 + Σ(coef_S3_S4[i] * feature_i))

# 聚合为整体风险分数
overall_risk_score = max(溃疡病风险概率, 炭疽病风险概率, 黄龙病风险概率)

# 映射到有序等级
if overall_risk_score < 0.2:   label = 0
elif overall_risk_score < 0.4: label = 1
elif overall_risk_score < 0.7: label = 2
else:                          label = 3
```

---

## 6. 模型评估指标

| 指标 | 说明 |
|------|------|
| **准确率 (Accuracy)** | 总体正确率（预测等级=真实等级的比例） |
| **每类精确率/召回率/F1** | 按风险等级0/1/2/3分别评估 |
| **Macro F1** | 各类F1的均值（不受类别不平衡影响） |
| **Weighted F1** | 按各类样本量加权的F1均值 |
| **Quadratic Weighted Kappa** | 有序分类专用指标，惩罚跨级误差（预测3真实0的惩罚 > 预测1真实0） |
| **混淆矩阵** | 4×4矩阵，直观展示分类错误分布 |
| **特征权重 (Coefficients)** | 逻辑回归系数绝对值 = 特征重要性 |

> 在病害预测中，**高估风险（预测等级 > 真实等级）比低估更安全** — 宁可误报不可漏报。

---

## 7. 后续整合路径

```
当前 (Demo)                              未来 (生产)
─────────────                           ────────────
data_generator.py                       → 替换为真实数据 SQL 查询
train.py (sklearn)                      → 保留或迁移到 Java (PMML/ONNX)
predict_demo.py                         → 集成到 Spring Boot (PMML加载器) 或 Python推理微服务
```

### 部署选项对比

| 方式 | 优势 | 劣势 |
|------|------|------|
| **Python 微服务** (FastAPI) | 模型生态丰富，可热更新模型 | 需要额外的服务运维 |
| **Java PMML/ONNX 加载** | 无需额外服务，嵌入现有后端 | 特征工程需在Java重写 |
| **Python 脚本定时执行** | 最简单，写入结果到DB | 实时性差 |

**推荐**: Demo 阶段用 Python 脚本; 生产阶段用 **Python FastAPI 微服务**（现有 RAG 已是 Python，可复用部署经验）。
