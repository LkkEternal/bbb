"""
脐橙病虫害风险预测 — 预测 Demo 脚本
========================================
加载统一有序多分类模型，对新样本进行整体风险预测。

使用方式:
  # 使用单条模拟样本预测（默认）
  python predict_demo.py --model models/model_overall.pkl

  # 对 CSV 文件中的样本批量预测
  python predict_demo.py --model models/model_overall.pkl --data new_samples.csv
"""

import argparse
import json
import warnings
from pathlib import Path

import joblib
import numpy as np
import pandas as pd

warnings.filterwarnings("ignore")

RISK_LABELS = ["无风险(0)", "低风险(1)", "中风险(2)", "高风险(3)"]
RISK_SUGGESTIONS = [
    "保持正常管理",
    "常规监测，关注环境变化",
    "加强监测频率，检查果园状态",
    "立即检查果园，准备防治措施",
]


def load_model(model_path: Path):
    """加载训练好的 Pipeline 模型和特征列名。"""
    if not model_path.exists():
        print(f"[错误] 模型文件不存在: {model_path}")
        return None, None

    pipeline = joblib.load(model_path)
    print(f"[OK] 模型已加载: {model_path}")

    # 尝试加载特征列名（兼容不同命名）
    for pattern in ["features_overall.json", "features_*.json"]:
        for f in model_path.parent.glob(pattern):
            feat_path = f
            break
        else:
            continue
        break
    else:
        feat_path = model_path.parent / "features_overall.json"

    if feat_path.exists():
        with open(feat_path, "r", encoding="utf-8") as f:
            feature_cols = json.load(f)
        print(f"[OK] 特征列名已加载 ({len(feature_cols)} 个特征)")
    else:
        feature_cols = None
        print("[提示] 未找到特征列名文件，使用模型内部顺序")

    return pipeline, feature_cols


def generate_demo_sample(feature_cols: list = None, seed: int = 42) -> pd.DataFrame:
    """
    生成一条模拟的新样本（包含所有3种病害的压力特征）。
    """
    rng = np.random.default_rng(seed)

    sample = {}

    # ── 环境特征 ──
    sample["temp_mean_7d"] = rng.normal(28, 3)
    sample["temp_max_7d"] = sample["temp_mean_7d"] + rng.exponential(2)
    sample["temp_min_7d"] = sample["temp_mean_7d"] - rng.exponential(2)
    sample["temp_std_7d"] = rng.exponential(1.5)

    sample["humidity_mean_7d"] = rng.normal(85, 5)
    sample["humidity_max_7d"] = min(sample["humidity_mean_7d"] + rng.exponential(5), 100)
    sample["humidity_min_7d"] = max(sample["humidity_mean_7d"] - rng.exponential(5), 0)

    sample["precip_cumul_7d"] = rng.exponential(60)
    sample["precip_days_7d"] = int(rng.binomial(7, 0.4))
    sample["wind_mean_7d"] = rng.exponential(4)

    sample["temp_mean_14d"] = sample["temp_mean_7d"] + rng.normal(0, 1)
    sample["humidity_mean_14d"] = sample["humidity_mean_7d"] + rng.normal(0, 3)
    sample["precip_cumul_14d"] = sample["precip_cumul_7d"] + rng.exponential(30)

    # ── 土壤特征 ──
    sample["soil_temp_mean_7d"] = sample["temp_mean_7d"] - rng.normal(3, 1)
    sample["soil_moisture_mean_7d"] = rng.normal(40, 5)
    sample["soil_ph_mean_7d"] = rng.normal(6.0, 0.3)
    sample["soil_conductivity_mean_7d"] = rng.normal(200, 40)

    # ── 病害压力特征（全部3种病害） ──
    sample["dpi_S14_7d"] = rng.exponential(0.8)
    sample["dpi_S14_14d"] = rng.exponential(1.2)
    sample["days_since_last_S14"] = int(rng.exponential(5)) + 1
    sample["detection_freq_S14_7d"] = int(rng.poisson(2))

    sample["dpi_S10_7d"] = rng.exponential(0.5)
    sample["dpi_S10_14d"] = rng.exponential(0.8)
    sample["days_since_last_S10"] = int(rng.exponential(8)) + 1
    sample["detection_freq_S10_7d"] = int(rng.poisson(1))

    sample["dpi_S3_S4_7d"] = rng.exponential(0.2)
    sample["dpi_S3_S4_14d"] = rng.exponential(0.3)
    sample["days_since_last_S3_S4"] = int(rng.exponential(20)) + 1
    sample["detection_freq_S3_S4_7d"] = int(rng.poisson(0.3))

    # ── 病害多样性 ──
    sample["disease_diversity_14d"] = rng.beta(2, 3)

    # ── 交互特征 ──
    T = sample["temp_mean_7d"]
    RH = sample["humidity_mean_7d"]
    sample["th_index_7d"] = T - 0.55 * (1 - RH / 100) * (T - 14.5)
    sample["heat_humid_days_7d"] = 3
    sample["heat_humid_days_7d_ge3"] = 1
    sample["temp_humidity_interact"] = T * RH / 100
    sample["disease_risk_index"] = max(T - 25, 0) * max(RH - 80, 0) * 0.8 / 100

    # ── 天气预报 ──
    sample["forecast_temp_mean_3d"] = rng.normal(T, 2)
    sample["forecast_humidity_mean_3d"] = rng.normal(RH, 5)
    sample["forecast_pop_mean_3d"] = rng.beta(3, 2)
    sample["forecast_precip_cumul_3d"] = rng.exponential(25)
    sample["forecast_precip_days_3d"] = int(rng.binomial(3, 0.4))

    # ── 物候期 ──
    sample["phenology_春梢萌发期"] = 0
    sample["phenology_开花期"] = 0
    sample["phenology_果实膨大期"] = 1
    sample["phenology_果实着色期"] = 0
    sample["phenology_成熟采收期"] = 0

    df = pd.DataFrame([sample])

    if feature_cols is not None:
        missing = [c for c in feature_cols if c not in df.columns]
        extra = [c for c in df.columns if c not in feature_cols]
        if missing:
            for c in missing:
                df[c] = 0.0
        if extra:
            df = df.drop(columns=extra)
        df = df[feature_cols]

    return df


def predict_unified(pipeline, df: pd.DataFrame, feature_cols: list = None):
    """
    统一预测：输出4个风险等级的概率。

    返回: (pred_class, probs, risk_level, suggestion, contrib_df)
    """
    if feature_cols is not None:
        X = df[feature_cols].values
    else:
        X = df.values

    # 多分类预测
    probs = pipeline.predict_proba(X)[0]  # 4 个概率
    pred_class = int(np.argmax(probs))
    risk_level = RISK_LABELS[pred_class] if pred_class < len(RISK_LABELS) else f"类别{pred_class}"
    suggestion = RISK_SUGGESTIONS[pred_class] if pred_class < len(RISK_SUGGESTIONS) else ""

    # 特征贡献分析（针对预测类别）
    coef = pipeline.named_steps["classifier"].coef_  # (n_classes, n_features)
    scaler = pipeline.named_steps["scaler"]
    feature_names = feature_cols if feature_cols else [f"F{i}" for i in range(coef.shape[1])]

    X_scaled = scaler.transform(X)
    # 对预测类别计算贡献
    contributions = coef[pred_class] * X_scaled[0]
    contrib_df = pd.DataFrame({
        "feature": feature_names,
        "contribution": contributions,
        "abs_contribution": np.abs(contributions),
        "weight": coef[pred_class],
        "feature_value": X[0],
    }).sort_values("abs_contribution", ascending=False)

    return pred_class, probs, risk_level, suggestion, contrib_df


def main():
    parser = argparse.ArgumentParser(
        description="脐橙病虫害风险预测 Demo（统一有序多分类）"
    )
    parser.add_argument(
        "--model",
        type=str,
        default="models/model_overall.pkl",
        help="模型文件路径 (default: models/model_overall.pkl)",
    )
    parser.add_argument(
        "--data",
        type=str,
        default=None,
        help="待预测的 CSV 文件（可选，默认生成模拟样本）",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=42,
        help="模拟样本随机种子 (default: 42)",
    )
    args = parser.parse_args()

    # ── 加载模型 ──
    model_path = Path(args.model)
    pipeline, feature_cols = load_model(model_path)

    if pipeline is None:
        return

    # ── 加载或生成数据 ──
    if args.data:
        data_path = Path(args.data)
        if not data_path.exists():
            print(f"[错误] 数据文件不存在: {data_path}")
            return
        df = pd.read_csv(data_path)
        print(f"[OK] 加载预测数据: {data_path} ({len(df)} 条样本)")
        # 移除目标/概率列
        drop_cols = [c for c in ["target", "target_probability", "prob_S14", "prob_S10", "prob_S3_S4"]
                     if c in df.columns]
        if drop_cols:
            df = df.drop(columns=drop_cols)
    else:
        print("[INFO] 生成模拟样本进行演示预测...")
        df = generate_demo_sample(feature_cols, args.seed)

    # ── 显示输入特征 ──
    print(f"\n{'='*60}")
    print(f"输入样本特征（关键项）")
    print(f"{'='*60}")
    key_features = [
        "temp_mean_7d", "humidity_mean_7d", "precip_cumul_7d",
        "dpi_S14_7d", "dpi_S10_7d", "dpi_S3_S4_7d",
        "heat_humid_days_7d_ge3", "soil_moisture_mean_7d",
    ]
    for feat in key_features:
        if feat in df.columns:
            print(f"  {feat}: {df[feat].values[0]:.4f}")

    # ── 预测 ──
    print(f"\n{'='*60}")
    print(f"预测结果")
    print(f"{'='*60}")

    pred_class, probs, risk_level, suggestion, contrib_df = predict_unified(
        pipeline, df, feature_cols
    )

    # 显示各等级概率
    print(f"  风险概率分布:")
    for i, (label, prob) in enumerate(zip(RISK_LABELS, probs)):
        bar = "#" * int(prob * 30) + "." * (30 - int(prob * 30))
        print(f"    {label}: {prob:.2%}  {bar}")

    print(f"\n  预测等级:      {pred_class} — {risk_level}")
    print(f"  防治建议:      {suggestion}")

    # 特征贡献
    print(f"\n  ── Top-5 贡献特征（对预测等级 {pred_class} 的影响） ──")
    for _, row in contrib_df.head(5).iterrows():
        direction = "[+] 增加风险" if row["contribution"] > 0 else "[-] 降低风险"
        print(f"    {direction}: {row['feature']} = {row['feature_value']:.4f} "
              f"(贡献: {row['contribution']:+.4f})")

    # 保存结果
    output_path = Path("prediction_result.csv")
    result_df = df.copy()
    result_df["predicted_class"] = pred_class
    result_df["predicted_level"] = risk_level
    for i, label in enumerate(RISK_LABELS):
        result_df[f"prob_class{i}"] = round(probs[i], 4)
    result_df.to_csv(output_path, index=False, encoding="utf-8-sig")
    print(f"\n[OK] 预测结果已保存: {output_path}")


if __name__ == "__main__":
    main()
