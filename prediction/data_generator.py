"""
脐橙病虫害风险预测 — 模拟数据生成器
========================================
基于农业科学规律，生成带标签的模拟训练数据。

支持两种模式:
  1. 单病害二分类（旧）: --n-samples 500  (默认)
  2. 统一有序多分类（新）: --unified --n-samples 800

统一模式输出整体风险等级 0-3（无/低/中/高），
基于 S14/S10/S3_S4 三种病害概率的 max 聚合。

使用方式:
  python data_generator.py --unified --n-samples 800 --seed 42
  python data_generator.py --n-samples 500  (单病害)

输出: CSV 文件 + 特征名列表 (feature_columns.json)
"""

import argparse
import json
import os
import warnings
from pathlib import Path

import numpy as np
import pandas as pd

warnings.filterwarnings("ignore")

# ─── 全局随机种子 ───
RNG = np.random.default_rng(42)


def _rng(seed_offset: int = 0):
    """返回一个带偏移的独立随机数生成器（保证可重复性）"""
    return np.random.default_rng(42 + seed_offset)


# ===================================================================
# 1. 病害配置
# ===================================================================

DISEASE_CONFIG = {
    "S14": {
        "name": "溃疡病",
        "name_en": "canker",
        "prevalence_mean": 1.2,  # Poisson λ — 每周平均检测次数
        "severity": 3,
        "seasonal_peak": [4, 5, 6, 7],  # 春夏高发
        # 标签生成系数 — 特征已 Min-Max 标准化到 [0,1]，系数直接反映影响大小
        "coef": {
            "intercept": -2.5,
            "temp_mean_7d": 0.10,
            "humidity_mean_7d": 0.06,
            "precip_cumul_7d": 0.03,
            "heat_humid_days_7d_ge3": 1.20,
            "phenology_果实膨大期": 0.50,
            "soil_moisture_mean_7d": 0.08,
        },
    },
    "S10": {
        "name": "炭疽病",
        "name_en": "anthracnose",
        "prevalence_mean": 0.8,
        "severity": 2,
        "seasonal_peak": [5, 6, 7, 8],
        "coef": {
            "intercept": -2.0,
            "temp_mean_7d": 0.08,
            "humidity_mean_7d": 0.07,
            "precip_cumul_7d": 0.04,
            "heat_humid_days_7d_ge3": 0.90,
            "phenology_果实膨大期": 0.30,
            "soil_moisture_mean_7d": 0.06,
        },
    },
    "S3_S4": {
        "name": "黄龙病",
        "name_en": "HLB",
        "prevalence_mean": 0.3,
        "severity": 3,
        "seasonal_peak": [5, 6, 7, 8, 9],
        "coef": {
            "intercept": -3.0,
            "temp_mean_7d": 0.06,
            "humidity_mean_7d": 0.04,
            "precip_cumul_7d": 0.01,
            "heat_humid_days_7d_ge3": 0.50,
            "phenology_果实膨大期": 0.20,
            "soil_moisture_mean_7d": 0.03,
        },
    },
}

# 物候期列表
PHENOLOGY_LIST = ["春梢萌发期", "开花期", "果实膨大期", "果实着色期", "成熟采收期"]

# 有序多分类阈值 — 将 max_prob 映射到 0/1/2/3
RISK_THRESHOLDS = [0.15, 0.25, 0.40]
RISK_LABELS = ["无风险", "低风险", "中风险", "高风险"]

# ===================================================================
# 2. 环境数据生成
# ===================================================================


def generate_environment(n_samples: int, seed: int = 0) -> pd.DataFrame:
    """
    生成模拟环境数据（7天统计量窗口）。
    参数参考赣南柑橘产区气候特征。
    """
    rng = _rng(seed)

    df = pd.DataFrame()

    # ── 温度 (℃) ──
    # 赣南年均温18-19°C，春夏月均20-28°C
    df["temp_mean_7d"] = rng.normal(24, 6, n_samples).clip(8, 38)
    df["temp_max_7d"] = df["temp_mean_7d"] + rng.exponential(3, n_samples)
    df["temp_min_7d"] = df["temp_mean_7d"] - rng.exponential(2.5, n_samples)
    df["temp_std_7d"] = rng.exponential(2, n_samples).clip(0.5, 6)

    # ── 湿度 (%) ──
    df["humidity_mean_7d"] = rng.normal(75, 15, n_samples).clip(30, 100)
    df["humidity_max_7d"] = (df["humidity_mean_7d"] + rng.exponential(8, n_samples)).clip(0, 100)
    df["humidity_min_7d"] = (df["humidity_mean_7d"] - rng.exponential(8, n_samples)).clip(0, 100)

    # ── 降雨 (mm) ──
    df["precip_cumul_7d"] = rng.exponential(50, n_samples)
    df["precip_days_7d"] = rng.binomial(7, 0.35, n_samples).clip(0, 7)

    # ── 风速 (km/h) ──
    df["wind_mean_7d"] = rng.exponential(5, n_samples).clip(0.5, 30)

    # ── 14天扩展窗口 ──
    df["temp_mean_14d"] = df["temp_mean_7d"] + rng.normal(0, 2, n_samples)
    df["humidity_mean_14d"] = df["humidity_mean_7d"] + rng.normal(0, 5, n_samples).clip(-15, 15)
    df["precip_cumul_14d"] = df["precip_cumul_7d"] + rng.exponential(40, n_samples)

    return df


# ===================================================================
# 3. 土壤数据生成
# ===================================================================


def generate_soil(n_samples: int, seed: int = 0) -> pd.DataFrame:
    """
    生成模拟土壤数据（来自 environment_data.extra_data）。
    """
    rng = _rng(seed + 100)

    df = pd.DataFrame()
    df["soil_temp_mean_7d"] = rng.normal(20, 4, n_samples).clip(8, 35)
    df["soil_moisture_mean_7d"] = rng.normal(35, 10, n_samples).clip(10, 60)
    df["soil_ph_mean_7d"] = rng.normal(6.0, 0.8, n_samples).clip(4.5, 8.0)
    df["soil_conductivity_mean_7d"] = rng.normal(200, 80, n_samples).clip(30, 500)

    return df


# ===================================================================
# 4. 病害压力数据生成
# ===================================================================


def generate_disease_pressure(
    n_samples: int, disease_code: str, config: dict, seed: int = 0
) -> pd.DataFrame:
    """
    生成某病害的检测历史压力特征。

    时间衰减: DPI = sum(count_i * exp(-0.3 * (t-i)))
    - 近1天检测权重最高 ~1.0
    - 近7天检测权重最低 ~0.12
    """
    rng = _rng(seed + 200 + sum(ord(c) for c in disease_code))
    λ = 0.3  # 衰减因子

    # 过去7天每天的检测次数 (Poisson)
    daily_counts = rng.poisson(config["prevalence_mean"] / 7, (n_samples, 7))

    # 时间衰减权重 [最近->最远]
    weights = np.exp(-λ * np.arange(7))
    dpi_7d = daily_counts @ weights

    # 14天扩展
    daily_counts_14 = rng.poisson(config["prevalence_mean"] / 14, (n_samples, 14))
    weights_14 = np.exp(-λ * np.arange(14))
    dpi_14d = daily_counts_14 @ weights_14

    # 距上次检测天数
    # 如果 DPI 高则天数短，DPI 低则天数长
    days_since = rng.exponential(15 / (dpi_7d + 0.5), n_samples).clip(1, 60).astype(int)

    # 近7天总检测频次
    detection_freq_7d = daily_counts.sum(axis=1)

    df = pd.DataFrame()
    df[f"dpi_{disease_code}_7d"] = dpi_7d.round(4)
    df[f"dpi_{disease_code}_14d"] = dpi_14d.round(4)
    df[f"days_since_last_{disease_code}"] = days_since
    df[f"detection_freq_{disease_code}_7d"] = detection_freq_7d

    return df


def generate_disease_diversity(n_samples: int, seed: int = 0) -> pd.DataFrame:
    """生成病害多样性指数 (Shannon)。"""
    rng = _rng(seed + 300)

    # 假设有6种非健康病害会被检测到
    n_disease_types = 6
    # 每种病害的检测比例
    proportions = rng.dirichlet(np.ones(n_disease_types), n_samples)
    # Shannon 指数
    diversity = -np.sum(proportions * np.log(proportions + 1e-10), axis=1)
    # 归一化到 0-1 范围 (max Shannon for 6 classes = ln(6) ≈ 1.79)
    diversity = diversity / np.log(n_disease_types)

    df = pd.DataFrame({"disease_diversity_14d": diversity.round(4)})
    return df


# ===================================================================
# 5. 交互特征生成
# ===================================================================


def generate_interaction_features(df_env: pd.DataFrame) -> pd.DataFrame:
    """
    生成交互特征:
      - THI (温湿度综合指数)
      - 高温高湿持续天数
      - 病害三角风险指数 (温度×湿度交互)
      - 冷却效应
    """
    df = pd.DataFrame()

    T = df_env["temp_mean_7d"].values
    RH = df_env["humidity_mean_7d"].values

    # THI 指数: T - 0.55 * (1 - RH/100) * (T - 14.5)
    df["th_index_7d"] = (T - 0.55 * (1 - RH / 100) * (T - 14.5)).round(2)

    # 高温高湿持续天数 (模拟, 基于温度和湿度的联合分布)
    # 每天高温高湿的概率 ~ sigmoid(0.2*(T-25) + 0.1*(RH-80))
    prob_heat_humid = 1 / (1 + np.exp(-(0.2 * (T - 25) + 0.1 * (RH - 80))))
    heat_humid_days = np.random.default_rng(42 + 400).binomial(7, prob_heat_humid)
    df["heat_humid_days_7d"] = heat_humid_days
    df["heat_humid_days_7d_ge3"] = (heat_humid_days >= 3).astype(int)

    # 温度×湿度交互项
    df["temp_humidity_interact"] = (T * RH / 100).round(2)

    # 病害三角风险指数 (需要dpi, 此处留空，在组装时计算)
    # 在 assemble_features 中补全

    return df


# ===================================================================
# 6. 天气预报特征生成
# ===================================================================


def generate_forecast(n_samples: int, seed: int = 0) -> pd.DataFrame:
    """
    生成未来3天天气预报特征。
    与当前环境数据有一定相关性但带有噪声。
    """
    rng = _rng(seed + 500)

    df = pd.DataFrame()
    df["forecast_temp_mean_3d"] = rng.normal(24, 5, n_samples).clip(5, 38)
    df["forecast_humidity_mean_3d"] = rng.normal(72, 12, n_samples).clip(20, 100)
    df["forecast_pop_mean_3d"] = rng.beta(2, 3, n_samples).round(3)  # 降水概率
    df["forecast_precip_cumul_3d"] = rng.exponential(30, n_samples)
    df["forecast_precip_days_3d"] = rng.binomial(3, 0.3, n_samples).clip(0, 3)

    return df


# ===================================================================
# 7. 物候期生成
# ===================================================================


def generate_phenology(n_samples: int, seed: int = 0) -> pd.DataFrame:
    """
    生成物候期 one-hot 编码。
    根据月份分布: 赣南脐橙物候期
      - 春梢萌发期: 3-4月
      - 开花期: 4-5月
      - 果实膨大期: 6-9月
      - 果实着色期: 10-11月
      - 成熟采收期: 11-12月
    """
    rng = _rng(seed + 600)

    # 随机生成月份（模拟全年分布）
    months = rng.integers(1, 13, n_samples)

    def _get_phenology(month: int) -> str:
        if month in [3, 4]:
            return "春梢萌发期"
        elif month in [4, 5]:
            return "开花期"
        elif 6 <= month <= 9:
            return "果实膨大期"
        elif month in [10, 11]:
            return "果实着色期"
        else:  # 11, 12, 1, 2
            return "成熟采收期"

    phenology_names = np.array([_get_phenology(m) for m in months])

    # one-hot 编码
    df = pd.DataFrame()
    for p in PHENOLOGY_LIST:
        df[f"phenology_{p}"] = (phenology_names == p).astype(int)

    df["month"] = months  # 保留月份用于调试
    return df


# ===================================================================
# 8. 标签生成
# ===================================================================


def _standardize(series: pd.Series) -> np.ndarray:
    """Min-Max 标准化到 [0, 1] 范围，使系数具有可解释性。"""
    min_val, max_val = series.min(), series.max()
    if max_val - min_val < 1e-6:
        return np.zeros(len(series))
    return ((series.values - min_val) / (max_val - min_val)).clip(0, 1)


def generate_labels(
    df_features: pd.DataFrame,
    disease_code: str,
    config: dict,
    n_samples: int,
    seed: int = 0,
) -> np.ndarray:
    """
    使用逻辑回归公式生成标签。

    特征先 Min-Max 标准化到 [0,1] 再乘以系数，使系数大小反映影响强度。
    截距控制基准爆发率。
    """
    rng = _rng(seed + 700 + sum(ord(c) for c in disease_code))
    coef = config["coef"]

    # 计算线性组合：特征先标准化再乘系数
    logit = coef["intercept"]

    for col in df_features.columns:
        if col in coef:
            scaled = _standardize(df_features[col])
            logit += coef[col] * scaled

    # 添加随机噪声 (模拟不可观测因素)
    noise = rng.normal(0, 0.2, n_samples)
    logit += noise

    # sigmoid 转换
    prob = 1 / (1 + np.exp(-logit))
    prob = prob.clip(0.01, 0.99)

    # 从伯努利分布采样
    labels = rng.binomial(1, prob)

    return labels, prob


def generate_unified_labels(
    df_features: pd.DataFrame,
    n_samples: int,
    seed: int = 0,
) -> tuple:
    """
    统一标签生成：同时计算 S14/S10/S3_S4 的爆发概率，
    取最大值映射到有序等级 0/1/2/3。

    返回: (ordinal_labels, max_probs, per_disease_probs)
      - ordinal_labels: 0/1/2/3 数组
      - max_probs: 各样本的最大概率
      - per_disease_probs: dict {dcode: prob_array}
    """
    rng_base = _rng(seed + 999)

    all_probs = {}
    for i, (dcode, config) in enumerate(DISEASE_CONFIG.items()):
        # 用与原来一致的种子偏移计算各病害概率
        rng = _rng(seed + 700 + sum(ord(c) for c in dcode))
        coef = config["coef"]

        logit = coef["intercept"]
        for col in df_features.columns:
            if col in coef:
                scaled = _standardize(df_features[col])
                logit += coef[col] * scaled

        # 添加独立噪声 (各病害噪声不同)
        noise = rng.normal(0, 0.2, n_samples)
        logit += noise

        prob = 1 / (1 + np.exp(-logit))
        prob = prob.clip(0.01, 0.99)
        all_probs[dcode] = prob

    # 聚合：取最大概率作为整体风险分数
    probs_stack = np.column_stack([all_probs[d] for d in DISEASE_CONFIG])
    max_probs = probs_stack.max(axis=1)

    # 映射到有序等级
    ordinal_labels = np.zeros(n_samples, dtype=int)
    ordinal_labels[max_probs >= RISK_THRESHOLDS[0]] = 1
    ordinal_labels[max_probs >= RISK_THRESHOLDS[1]] = 2
    ordinal_labels[max_probs >= RISK_THRESHOLDS[2]] = 3

    return ordinal_labels, max_probs, all_probs


# ===================================================================
# 9. 主组装函数
# ===================================================================


def generate_dataset(
    n_samples: int = 1000,
    disease_code: str = "S14",
    seed: int = 42,
) -> pd.DataFrame:
    """
    生成完整的训练数据集。
    """
    # 重置全局种子
    np.random.default_rng(seed)

    disease_config = DISEASE_CONFIG[disease_code]

    # 1. 环境数据
    df_env = generate_environment(n_samples, seed)

    # 2. 土壤数据
    df_soil = generate_soil(n_samples, seed)

    # 3. 病害压力
    df_pressure = generate_disease_pressure(n_samples, disease_code, disease_config, seed)

    # 4. 病害多样性
    df_diversity = generate_disease_diversity(n_samples, seed)

    # 5. 交互特征
    df_interact = generate_interaction_features(df_env)

    # 补充: 病害三角风险指数 = (T-25)+ * (RH-80)+ * DPI
    T = df_env["temp_mean_7d"].values
    RH = df_env["humidity_mean_7d"].values
    dpi = df_pressure[f"dpi_{disease_code}_7d"].values
    df_interact["disease_risk_index"] = (
        np.maximum(T - 25, 0) * np.maximum(RH - 80, 0) * dpi / 100
    ).round(4)

    # 6. 天气预报
    df_forecast = generate_forecast(n_samples, seed)

    # 7. 物候期
    df_phenology = generate_phenology(n_samples, seed)

    # ── 合并所有特征 ──
    df = pd.concat(
        [
            df_env,
            df_soil,
            df_pressure,
            df_diversity,
            df_interact,
            df_forecast,
            df_phenology,
        ],
        axis=1,
    )

    # 丢弃临时列
    df.drop(columns=["month"], inplace=True, errors="ignore")

    # ── 生成标签 ──
    # 只使用与病害无关的特征（避免数据泄露）
    label_features = [c for c in df.columns if not c.startswith(("dpi_", "days_since_last_", "detection_freq_", "disease_risk_index"))]
    labels, probs = generate_labels(df, disease_code, disease_config, n_samples, seed)

    df["target_disease_code"] = disease_code
    df["target_disease_name"] = disease_config["name"]
    df["target_probability"] = probs.round(4)
    df["target"] = labels

    return df


def generate_multi_disease_dataset(
    n_samples_per_disease: int = 500,
    seed: int = 42,
) -> pd.DataFrame:
    """
    生成多病害数据集。

    所有病害共享同一套基础特征（环境/土壤/物候/预报），
    但各自的病害压力特征独立生成，最后合并为多标签格式。
    """
    rng = _rng(seed + 999)

    # ── 生成共享基础特征 ──
    df_env = generate_environment(n_samples_per_disease, seed)
    df_soil = generate_soil(n_samples_per_disease, seed)
    df_diversity = generate_disease_diversity(n_samples_per_disease, seed)
    df_interact = generate_interaction_features(df_env)
    df_forecast = generate_forecast(n_samples_per_disease, seed)
    df_phenology = generate_phenology(n_samples_per_disease, seed)

    # 病害三角风险指数（留空，后面按病害补全）
    df_interact["disease_risk_index"] = 0.0

    base_df = pd.concat(
        [df_env, df_soil, df_diversity, df_interact, df_forecast, df_phenology],
        axis=1,
    )
    base_df.drop(columns=["month"], inplace=True, errors="ignore")

    # ── 对每种病害生成压力特征和标签 ──
    all_rows = []
    for i, (dcode, config) in enumerate(DISEASE_CONFIG.items()):
        # 病害压力特征
        df_pressure = generate_disease_pressure(
            n_samples_per_disease, dcode, config, seed + i * 100
        )
        # 病害三角风险指数（用当前病害的DPI）
        T = df_env["temp_mean_7d"].values
        RH = df_env["humidity_mean_7d"].values
        dpi = df_pressure[f"dpi_{dcode}_7d"].values
        disease_risk = (
            np.maximum(T - 25, 0) * np.maximum(RH - 80, 0) * dpi / 100
        ).round(4)

        # 合并基础特征 + 当前病害压力
        df_combined = pd.concat([base_df, df_pressure], axis=1)
        df_combined["disease_risk_index"] = disease_risk

        # 生成标签
        labels, probs = generate_labels(
            df_combined, dcode, config, n_samples_per_disease, seed + i * 100
        )

        df_combined["target_disease_code"] = dcode
        df_combined["target_disease_name"] = config["name"]
        df_combined["target_probability"] = probs.round(4)
        df_combined["target"] = labels

        all_rows.append(df_combined)

    return pd.concat(all_rows, ignore_index=True)


def generate_unified_dataset(
    n_samples: int = 800,
    seed: int = 42,
) -> pd.DataFrame:
    """
    生成统一有序多分类数据集。

    每条样本包含所有3种病害的压力特征，
    标签为整体风险等级 0/1/2/3。
    """
    rng = _rng(seed + 999)

    # ── 基础特征（共享） ──
    df_env = generate_environment(n_samples, seed)
    df_soil = generate_soil(n_samples, seed)
    df_diversity = generate_disease_diversity(n_samples, seed)
    df_interact = generate_interaction_features(df_env)
    df_forecast = generate_forecast(n_samples, seed)
    df_phenology = generate_phenology(n_samples, seed)

    # ── 各病害压力特征（全部保留） ──
    all_pressure_dfs = []
    all_dpi_cols = {}  # dcode -> dpi_7d column name
    for i, (dcode, config) in enumerate(DISEASE_CONFIG.items()):
        df_p = generate_disease_pressure(n_samples, dcode, config, seed + i * 100)
        all_pressure_dfs.append(df_p)
        all_dpi_cols[dcode] = f"dpi_{dcode}_7d"

    df_pressure_all = pd.concat(all_pressure_dfs, axis=1)

    # ── 病害三角风险指数（用 S14 的 DPI，但保留最大风险） ──
    T = df_env["temp_mean_7d"].values
    RH = df_env["humidity_mean_7d"].values
    risk_indices = []
    for dcode in DISEASE_CONFIG:
        dpi_col = all_dpi_cols[dcode]
        ri = np.maximum(T - 25, 0) * np.maximum(RH - 80, 0) * df_pressure_all[dpi_col].values / 100
        risk_indices.append(ri)
    df_interact["disease_risk_index"] = np.maximum.reduce(risk_indices).round(4)

    # ── 合并所有特征 ──
    df = pd.concat(
        [df_env, df_soil, df_pressure_all, df_diversity, df_interact,
         df_forecast, df_phenology],
        axis=1,
    )
    df.drop(columns=["month"], inplace=True, errors="ignore")

    # ── 生成统一标签 ──
    ordinal_labels, max_probs, per_disease_probs = generate_unified_labels(
        df, n_samples, seed
    )

    df["target"] = ordinal_labels
    df["target_probability"] = max_probs.round(4)
    # 同时保存各病害概率（用于分析）
    for dcode, probs in per_disease_probs.items():
        df[f"prob_{dcode}"] = probs.round(4)

    return df


# ===================================================================
# 10. CLI 入口
# ===================================================================


def main():
    parser = argparse.ArgumentParser(
        description="脐橙病虫害风险预测 — 模拟数据生成器"
    )
    parser.add_argument(
        "--n-samples",
        type=int,
        default=1000,
        help="每个病害的样本数 (default: 1000)",
    )
    parser.add_argument(
        "--output-dir",
        type=str,
        default=".",
        help="输出目录 (default: current)",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=42,
        help="随机种子 (default: 42)",
    )
    parser.add_argument(
        "--unified",
        action="store_true",
        help="生成统一有序多分类数据集（所有病害综合风险等级 0-3）",
    )
    parser.add_argument(
        "--multi-disease",
        action="store_true",
        help="生成多病害数据集（每种病害独立生成，已弃用，推荐 --unified）",
    )
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    if args.multi_disease:
        df = generate_multi_disease_dataset(args.n_samples, args.seed)
        output_path = output_dir / "disease_training_data_multi.csv"
    elif args.unified:
        df = generate_unified_dataset(args.n_samples, args.seed)
        output_path = output_dir / "disease_training_data_unified.csv"
    else:
        df = generate_dataset(args.n_samples, "S14", args.seed)
        output_path = output_dir / "disease_training_data_S14.csv"

    df.to_csv(output_path, index=False)
    print(f"[OK] 数据已生成: {output_path}")
    print(f"   样本数: {len(df)}")
    print(f"   特征数: {len(df.columns) - 1}")  # 减去 target 列
    if "target" in df.columns:
        # 打印分布
        val_counts = df["target"].value_counts(normalize=True).sort_index()
        dist_str = ", ".join([f"{lvl}={val_counts.get(i,0):.1%}" for i, lvl in enumerate(RISK_LABELS)])
        print(f"   等级分布: {dist_str}")
    print(f"   特征列: {list(df.columns[:8])}...")

    # 同时保存特征名列表供训练使用
    feature_cols = [c for c in df.columns if c != "target"]
    feat_path = output_dir / "feature_columns.json"
    with open(feat_path, "w", encoding="utf-8") as f:
        json.dump(feature_cols, f, ensure_ascii=False, indent=2)
    print(f"[OK] 特征名列表已保存: {feat_path}")


if __name__ == "__main__":
    main()
