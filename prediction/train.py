"""
脐橙病虫害风险预测 — 模型训练管线
======================================
训练统一有序多分类模型（风险等级 0/1/2/3）。

使用方式:
  python train.py --data disease_training_data_unified.csv [--output-dir models]
  python train.py --data disease_training_data_unified.csv --C 0.5

输出:
  - model_overall.pkl         (joblib 序列化的完整 Pipeline)
  - model_metrics.json         (评估指标)
  - feature_weights_*.csv      (每个风险等级的特征权重)
  - confusion_matrix.csv       (混淆矩阵)
"""

import argparse
import json
import warnings
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

warnings.filterwarnings("ignore")

RISK_LABELS = ["无风险(0)", "低风险(1)", "中风险(2)", "高风险(3)"]

# ─── 排除列（不用于训练） ───
EXCLUDED_COLUMNS = {"target", "target_probability"}
# 也排除 prob_S14 / prob_S10 / prob_S3_S4（这些是标签生成的中间结果，不是特征）
EXCLUDED_PREFIXES = ("prob_",)


def get_feature_columns(df: pd.DataFrame) -> list:
    """获取训练特征列名：排除 target 列和 prob_* 列。"""
    return [
        c for c in df.columns
        if c not in EXCLUDED_COLUMNS
        and not any(c.startswith(p) for p in EXCLUDED_PREFIXES)
    ]


def train_overall_model(
    df: pd.DataFrame,
    model_dir: Path,
    C: float = 1.0,
) -> dict:
    """
    训练统一有序多分类逻辑回归模型（4 类：0/1/2/3）。

    LogisticRegression(multi_class='multinomial') 输出每个类别的概率。
    """
    print(f"\n{'='*60}")
    print(f"训练统一风险预测模型（有序多分类）")
    print(f"{'='*60}")

    # ── 准备特征和目标 ──
    feature_cols = get_feature_columns(df)
    X = df[feature_cols].values
    y = df["target"].values

    n_classes = len(np.unique(y))

    print(f"  样本数: {len(df)}")
    print(f"  特征数: {len(feature_cols)}")
    print(f"  类别数: {n_classes}")

    # 打印类别分布
    val_counts = pd.Series(y).value_counts(normalize=True).sort_index()
    for i in range(n_classes):
        label = RISK_LABELS[i] if i < len(RISK_LABELS) else f"类别{i}"
        print(f"    {label}: {val_counts.get(i, 0):.1%}")

    # ── 训练/测试集划分 ──
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    print(f"\n  训练集: {len(X_train)}, 测试集: {len(X_test)}")

    # ── Pipeline: 标准化 → 多分类逻辑回归 ──
    pipeline = Pipeline([
        ("scaler", StandardScaler()),
        (
            "classifier",
            LogisticRegression(
                class_weight="balanced",     # 自动处理类别不平衡
                C=C,
                max_iter=2000,
                random_state=42,
                solver="lbfgs",
            ),
        ),
    ])

    # ── 训练 ──
    pipeline.fit(X_train, y_train)

    # ── 评估 ──
    y_pred = pipeline.predict(X_test)
    y_prob = pipeline.predict_proba(X_test)  # shape: (n_samples, n_classes)

    acc = accuracy_score(y_test, y_pred)
    f1_macro = f1_score(y_test, y_pred, average="macro", zero_division=0)
    f1_weighted = f1_score(y_test, y_pred, average="weighted", zero_division=0)
    cm = confusion_matrix(y_test, y_pred)

    # Quadratic Weighted Kappa (有序分类专用)
    kappa = _quadratic_weighted_kappa(y_test, y_pred, n_classes)

    metrics = {
        "model": "overall",
        "n_samples": len(df),
        "n_features": len(feature_cols),
        "n_classes": int(n_classes),
        "accuracy": float(acc),
        "f1_macro": float(f1_macro),
        "f1_weighted": float(f1_weighted),
        "quadratic_weighted_kappa": float(kappa),
    }

    print(f"\n  ── 评估结果 ──")
    print(f"  准确率 (Accuracy):             {acc:.4f}")
    print(f"  Macro F1:                     {f1_macro:.4f}")
    print(f"  Weighted F1:                  {f1_weighted:.4f}")
    print(f"  Quadratic Weighted Kappa:      {kappa:.4f}")

    # ── 混淆矩阵 ──
    print(f"\n  混淆矩阵 (行=真实, 列=预测):")
    header = "        " + "  ".join([f" pred{j}" for j in range(n_classes)])
    print(f"  {header}")
    for i in range(n_classes):
        row_str = "  ".join([f"{cm[i][j]:5d}" for j in range(n_classes)])
        label = RISK_LABELS[i] if i < len(RISK_LABELS) else f"类别{i}"
        print(f"  true{i}  {row_str}  ({label})")

    # ── 每类精确率/召回率/F1 ──
    print(f"\n  每类指标:")
    report = classification_report(y_test, y_pred, zero_division=0, output_dict=True)
    for i in range(n_classes):
        r = report.get(str(i), {})
        label = RISK_LABELS[i] if i < len(RISK_LABELS) else f"类别{i}"
        print(f"    {label}: precision={r.get('precision', 0):.4f}, "
              f"recall={r.get('recall', 0):.4f}, "
              f"f1={r.get('f1-score', 0):.4f}")

    # ── 特征权重分析 ──
    coef = pipeline.named_steps["classifier"].coef_  # shape: (n_classes, n_features)
    print(f"\n  ── Top-10 特征权重（按平均绝对权重） ──")
    mean_abs_weights = np.mean(np.abs(coef), axis=0)
    top_idx = np.argsort(mean_abs_weights)[-10:][::-1]
    for idx in top_idx:
        weights_str = ", ".join([f"class{j}={coef[j][idx]:+.4f}" for j in range(n_classes)])
        print(f"    {feature_cols[idx]}: {weights_str}")

    # ── 保存全部特征权重（每个类别一份） ──
    for j in range(n_classes):
        weights_df = pd.DataFrame({
            "feature": feature_cols,
            "weight": coef[j],
            "abs_weight": np.abs(coef[j]),
        }).sort_values("abs_weight", ascending=False)
        label_short = RISK_LABELS[j].split("(")[0]
        weights_path = model_dir / f"feature_weights_class{j}_{label_short}.csv"
        weights_df.to_csv(weights_path, index=False, encoding="utf-8-sig")

    # ── 保存模型 ──
    model_path = model_dir / "model_overall.pkl"
    joblib.dump(pipeline, model_path)
    print(f"\n  [OK] 模型已保存: {model_path}")

    # ── 保存混淆矩阵 ──
    cm_df = pd.DataFrame(
        cm,
        index=[f"true_{i}" for i in range(n_classes)],
        columns=[f"pred_{i}" for i in range(n_classes)],
    )
    cm_path = model_dir / "confusion_matrix.csv"
    cm_df.to_csv(cm_path, encoding="utf-8-sig")
    print(f"  [OK] 混淆矩阵已保存: {cm_path}")

    # ── 保存特征列名 ──
    feat_path = model_dir / "features_overall.json"
    with open(feat_path, "w", encoding="utf-8") as f:
        json.dump(feature_cols, f, ensure_ascii=False, indent=2)
    print(f"  [OK] 特征列名已保存: {feat_path}")

    return metrics


def _quadratic_weighted_kappa(y_true, y_pred, n_classes: int) -> float:
    """
    计算 Quadratic Weighted Kappa。
    用于有序分类的评估，跨级误差惩罚更大。
    """
    cm = confusion_matrix(y_true, y_pred, labels=range(n_classes))
    n = cm.sum()
    # 期望矩阵（行×列/总数）
    row_sums = cm.sum(axis=1)
    col_sums = cm.sum(axis=0)
    expected = np.outer(row_sums, col_sums) / n

    # 权重矩阵：w[i,j] = (i-j)^2 / (n_classes-1)^2
    w = np.zeros((n_classes, n_classes))
    for i in range(n_classes):
        for j in range(n_classes):
            w[i, j] = (i - j) ** 2 / (n_classes - 1) ** 2

    # Kappa = 1 - (sum(w * obs) / sum(w * exp))
    obs_weighted = np.sum(w * cm)
    exp_weighted = np.sum(w * expected)

    if exp_weighted == 0:
        return 1.0
    return 1.0 - obs_weighted / exp_weighted


# ===================================================================
# CLI
# ===================================================================


def main():
    parser = argparse.ArgumentParser(
        description="脐橙病虫害风险预测 — 模型训练管线（统一有序多分类）"
    )
    parser.add_argument(
        "--data",
        type=str,
        default="disease_training_data_unified.csv",
        help="训练数据 CSV 文件路径",
    )
    parser.add_argument(
        "--output-dir",
        type=str,
        default="models",
        help="模型输出目录 (default: models)",
    )
    parser.add_argument(
        "--C",
        type=float,
        default=1.0,
        help="逻辑回归正则化强度 (default: 1.0)",
    )
    args = parser.parse_args()

    # ── 加载数据 ──
    data_path = Path(args.data)
    if not data_path.exists():
        print(f"[错误] 数据文件不存在: {data_path}")
        print("请先运行 data_generator.py --unified 生成数据")
        return

    df = pd.read_csv(data_path)
    print(f"加载数据: {data_path}")
    print(f"  样本数: {len(df)}, 列数: {len(df.columns)}")

    # ── 输出目录 ──
    model_dir = Path(args.output_dir)
    model_dir.mkdir(parents=True, exist_ok=True)

    # ── 训练统一模型 ──
    metrics = train_overall_model(df, model_dir, args.C)

    # ── 保存评估指标 ──
    summary_path = model_dir / "model_metrics.json"
    with open(summary_path, "w", encoding="utf-8") as f:
        json.dump([metrics], f, ensure_ascii=False, indent=2)
    print(f"\n{'='*60}")
    print(f"评估指标已保存: {summary_path}")
    print(f"{'='*60}")

    # 打印汇总
    print(f"\n{'='*60}")
    print(f"训练完成汇总")
    print(f"{'='*60}")
    print(f"  准确率: {metrics['accuracy']:.4f}")
    print(f"  Macro F1: {metrics['f1_macro']:.4f}")
    print(f"  Weighted F1: {metrics['f1_weighted']:.4f}")
    print(f"  QWK: {metrics['quadratic_weighted_kappa']:.4f}")
    print(f"  模型文件: {model_dir.resolve() / 'model_overall.pkl'}")


if __name__ == "__main__":
    main()
