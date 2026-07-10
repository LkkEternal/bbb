"""
风险预测服务 — 加载模型、查询特征、运行预测、LLM 后处理
==========================================================

启动时加载 model_overall.pkl，暴露 predict(orchard_id) 方法。
"""

from __future__ import annotations

import json
import logging
import os
from pathlib import Path
from typing import Any

import joblib
import numpy as np
import pandas as pd
from sklearn.pipeline import Pipeline

from app.config import settings
from app.service.llm_service import llm_service

logger = logging.getLogger(__name__)

RISK_LABELS = ["无风险", "低风险", "中风险", "高风险"]
RISK_SUGGESTIONS = [
    "保持正常管理，无需额外措施",
    "常规监测，关注环境变化和早期病害迹象",
    "加强监测频率，检查果园状态，准备防治物资",
    "立即检查果园，启动防治措施，必要时联系植保专家",
]

# ── SQL 查询模板 ──

SQL_ENV_7D = """
SELECT
    AVG(temp)           AS temp_mean_7d,
    MAX(temp)           AS temp_max_7d,
    MIN(temp)           AS temp_min_7d,
    STDDEV(temp)        AS temp_std_7d,
    AVG(humidity)       AS humidity_mean_7d,
    MAX(humidity)       AS humidity_max_7d,
    MIN(humidity)       AS humidity_min_7d,
    COALESCE(SUM(precip), 0) AS precip_cumul_7d,
    COUNT(*) FILTER (WHERE precip > 0.1) AS precip_days_7d,
    AVG(windspeed)      AS wind_mean_7d
FROM environment_data
WHERE orchard_id = $1
  AND record_time >= NOW() - INTERVAL '7 days'
"""

SQL_ENV_14D = """
SELECT
    AVG(temp)           AS temp_mean_14d,
    AVG(humidity)       AS humidity_mean_14d,
    COALESCE(SUM(precip), 0) AS precip_cumul_14d
FROM environment_data
WHERE orchard_id = $1
  AND record_time >= NOW() - INTERVAL '14 days'
"""

SQL_SOIL = """
SELECT
    AVG((extra_data->>'soil_temp')::numeric)        AS soil_temp_mean_7d,
    AVG((extra_data->>'moisture')::numeric)          AS soil_moisture_mean_7d,
    AVG((extra_data->>'ph')::numeric)                AS soil_ph_mean_7d,
    AVG((extra_data->>'conductivity')::numeric)      AS soil_conductivity_mean_7d
FROM environment_data
WHERE orchard_id = $1
  AND record_time >= NOW() - INTERVAL '7 days'
  AND extra_data IS NOT NULL
  AND extra_data != '{}'
"""

SQL_DETECTIONS_7D = """
SELECT
    dc.disease_code,
    COUNT(*) AS cnt,
    MAX(dd.device_timestamp) AS last_detected
FROM disease_detection dd
JOIN disease_catalog dc ON dd.disease_catalog_id = dc.id
WHERE dd.orchard_id = $1
  AND dd.device_timestamp >= NOW() - INTERVAL '14 days'
GROUP BY dc.disease_code
"""

SQL_FORECAST_3D = """
SELECT
    AVG((temp_max + temp_min) / 2.0) AS forecast_temp_mean_3d,
    AVG(humidity)       AS forecast_humidity_mean_3d,
    AVG(pop)            AS forecast_pop_mean_3d,
    COALESCE(SUM(precip), 0) AS forecast_precip_cumul_3d,
    COUNT(*) FILTER (WHERE precip > 0.1) AS forecast_precip_days_3d
FROM weather_forecast
WHERE orchard_id = $1
  AND fxTime >= NOW()
  AND fxTime < NOW() + INTERVAL '3 days'
"""

SQL_PHENOLOGY = """
SELECT phenology_name
FROM farming_calendar
WHERE (orchard_id = $1 OR orchard_id IS NULL)
  AND EXTRACT(MONTH FROM NOW()) BETWEEN start_month AND end_month
  AND EXTRACT(DAY FROM NOW()) BETWEEN start_day AND end_day
  AND enabled = TRUE
ORDER BY orchard_id NULLS LAST
LIMIT 1
"""

# ── 病害码映射 ──
DISEASE_CODES = ["S14", "S10", "S3_S4"]

PHENOLOGY_LIST = ["春梢萌发期", "开花期", "果实膨大期", "果实着色期", "成熟采收期"]


class PredictionService:
    """风险预测服务（单例）"""

    def __init__(self):
        self.pipeline: Pipeline | None = None
        self.feature_cols: list[str] | None = None
        self._pool = None

    # ── 生命周期 ────────────────────────────────────────

    def load(self):
        """启动时调用：加载模型和特征列名。"""
        logger.info("Loading prediction model ...")
        model_path = Path(settings.prediction_model_path)
        feat_path = Path(settings.prediction_features_path)

        if not model_path.exists():
            logger.warning("Prediction model not found: %s", model_path)
            self.pipeline = None
            return
        if not feat_path.exists():
            logger.warning("Features file not found: %s", feat_path)
            self.feature_cols = None
            self.pipeline = None
            return

        self.pipeline = joblib.load(str(model_path))
        with open(feat_path, "r", encoding="utf-8") as f:
            self.feature_cols = json.load(f)

        logger.info(
            "Prediction model loaded. Features=%d, Classes=4",
            len(self.feature_cols),
        )

    def unload(self):
        """释放资源。"""
        self.pipeline = None
        self.feature_cols = None
        logger.info("Prediction model unloaded.")

    @property
    def ready(self) -> bool:
        return self.pipeline is not None and self.feature_cols is not None

    # ── 数据库连接 ───────────────────────────────────────

    async def _get_conn(self):
        """获取 asyncpg 连接（延迟创建连接池）。"""
        if self._pool is None:
            import asyncpg
            self._pool = await asyncpg.create_pool(
                host=settings.prediction_db_host,
                port=settings.prediction_db_port,
                database=settings.prediction_db_name,
                user=settings.prediction_db_user,
                password=settings.prediction_db_password,
                min_size=1,
                max_size=3,
            )
        return await self._pool.acquire()

    async def _release_conn(self, conn):
        if self._pool:
            await self._pool.release(conn)

    # ── 特征查询 ────────────────────────────────────────

    async def _query_features(self, orchard_id: int) -> dict[str, float]:
        """直连 PostgreSQL 查询所有特征数据。"""
        features = {}

        conn = await self._get_conn()
        try:
            # 1. 环境数据 7 天
            row = await conn.fetchrow(SQL_ENV_7D, orchard_id)
            if row:
                for col in ("temp_mean_7d", "temp_max_7d", "temp_min_7d", "temp_std_7d",
                            "humidity_mean_7d", "humidity_max_7d", "humidity_min_7d",
                            "precip_cumul_7d", "precip_days_7d", "wind_mean_7d"):
                    features[col] = float(row[col]) if row[col] is not None else 0.0

            # 2. 环境数据 14 天
            row = await conn.fetchrow(SQL_ENV_14D, orchard_id)
            if row:
                for col in ("temp_mean_14d", "humidity_mean_14d", "precip_cumul_14d"):
                    features[col] = float(row[col]) if row[col] is not None else 0.0

            # 3. 土壤数据
            row = await conn.fetchrow(SQL_SOIL, orchard_id)
            if row:
                for col in ("soil_temp_mean_7d", "soil_moisture_mean_7d",
                            "soil_ph_mean_7d", "soil_conductivity_mean_7d"):
                    features[col] = float(row[col]) if row[col] is not None else 0.0
            else:
                for col in ("soil_temp_mean_7d", "soil_moisture_mean_7d",
                            "soil_ph_mean_7d", "soil_conductivity_mean_7d"):
                    features[col] = 25.0  # 默认值

            # 4. 病害检测（计算 DPI 和距上次检测天数）
            rows = await conn.fetch(SQL_DETECTIONS_7D, orchard_id)
            detections = {r["disease_code"]: {"cnt": r["cnt"], "last": r["last_detected"]}
                          for r in rows}

            now = pd.Timestamp.now()
            for dcode in DISEASE_CODES:
                info = detections.get(dcode, {"cnt": 0, "last": None})
                # DPI 时间衰减
                if dcode == "S14":
                    prevalence = 1.2
                elif dcode == "S10":
                    prevalence = 0.8
                else:
                    prevalence = 0.3

                # 用过去7天的检测次数估算 DPI
                daily_rate = prevalence / 7
                weights = np.exp(-0.3 * np.arange(7))
                dpi_7d = info["cnt"] * weights.mean()  # 简化估算
                dpi_14d = dpi_7d * 1.5  # 14天约为7天的1.5倍

                features[f"dpi_{dcode}_7d"] = dpi_7d
                features[f"dpi_{dcode}_14d"] = dpi_14d
                features[f"detection_freq_{dcode}_7d"] = info["cnt"]

                # 距上次检测天数
                if info["last"]:
                    days_since = (now - pd.Timestamp(info["last"])).days
                else:
                    days_since = 30  # 默认30天无检测
                features[f"days_since_last_{dcode}"] = days_since

            # 5. 病害多样性
            disease_types = sum(1 for d in DISEASE_CODES if detections.get(d, {}).get("cnt", 0) > 0)
            features["disease_diversity_14d"] = min(disease_types / len(DISEASE_CODES), 1.0)

            # 6. 天气预报 3 天
            row = await conn.fetchrow(SQL_FORECAST_3D, orchard_id)
            if row:
                for col in ("forecast_temp_mean_3d", "forecast_humidity_mean_3d",
                            "forecast_pop_mean_3d", "forecast_precip_cumul_3d",
                            "forecast_precip_days_3d"):
                    features[col] = float(row[col]) if row[col] is not None else 0.0
            else:
                # 无预报数据时用当前环境数据
                features["forecast_temp_mean_3d"] = features.get("temp_mean_7d", 24.0)
                features["forecast_humidity_mean_3d"] = features.get("humidity_mean_7d", 75.0)
                features["forecast_pop_mean_3d"] = 0.3
                features["forecast_precip_cumul_3d"] = 0.0
                features["forecast_precip_days_3d"] = 0

            # 7. 物候期
            row = await conn.fetchrow(SQL_PHENOLOGY, orchard_id)
            phenology = row["phenology_name"] if row else "成熟采收期"
            for p in PHENOLOGY_LIST:
                features[f"phenology_{p}"] = 1.0 if p == phenology else 0.0

        finally:
            await self._release_conn(conn)

        return features

    # ── 派生特征计算 ─────────────────────────────────────

    def _compute_derived_features(self, f: dict[str, float]) -> dict[str, float]:
        """计算交互特征和派生特征。"""
        T = f.get("temp_mean_7d", 24.0)
        RH = f.get("humidity_mean_7d", 75.0)

        # THI 指数
        f["th_index_7d"] = round(T - 0.55 * (1 - RH / 100) * (T - 14.5), 2)

        # 高温高湿持续天数（简化估算）
        heat_humid_prob = 1 / (1 + np.exp(-(0.2 * (T - 25) + 0.1 * (RH - 80))))
        f["heat_humid_days_7d"] = int(round(heat_humid_prob * 7))
        f["heat_humid_days_7d_ge3"] = 1 if f["heat_humid_days_7d"] >= 3 else 0

        # 温度×湿度交互项
        f["temp_humidity_interact"] = round(T * RH / 100, 2)

        # 病害三角风险指数
        dpi_values = [f.get(f"dpi_{d}_7d", 0) for d in DISEASE_CODES]
        max_dpi = max(dpi_values)
        f["disease_risk_index"] = round(
            max(T - 25, 0) * max(RH - 80, 0) * max_dpi / 100, 4
        )

        return f

    # ── 核心预测 ────────────────────────────────────────

    def predict(self, features: dict[str, float]) -> dict[str, Any]:
        """
        对已组装好的特征向量进行预测。

        返回:
            risk_level: 0-3
            probabilities: [p0, p1, p2, p3]
            risk_label: 中文标签
            top_factors: Top-5 风险因子
        """
        if not self.ready:
            return {
                "risk_level": 0,
                "probabilities": [1.0, 0.0, 0.0, 0.0],
                "risk_label": "无风险",
                "top_factors": [],
                "error": "Model not loaded",
            }

        # 构建特征 DataFrame
        df = pd.DataFrame([features])
        # 填充缺失特征
        for col in self.feature_cols:
            if col not in df.columns:
                df[col] = 0.0
        df = df[self.feature_cols]

        # 预测
        probs = self.pipeline.predict_proba(df.values)[0]  # [p0, p1, p2, p3]
        pred_class = int(np.argmax(probs))

        # 特征贡献分析（对预测类别）
        coef = self.pipeline.named_steps["classifier"].coef_
        scaler = self.pipeline.named_steps["scaler"]
        X_scaled = scaler.transform(df.values)
        contributions = coef[pred_class] * X_scaled[0]
        top_indices = np.argsort(np.abs(contributions))[-5:][::-1]

        top_factors = []
        for idx in top_indices:
            feat_name = self.feature_cols[idx]
            top_factors.append({
                "feature": feat_name,
                "value": float(df.values[0, idx]),
                "contribution": float(contributions[idx]),
                "direction": "positive" if contributions[idx] > 0 else "negative",
            })

        return {
            "risk_level": pred_class,
            "probabilities": [float(p) for p in probs],
            "risk_label": RISK_LABELS[pred_class],
            "suggestion": RISK_SUGGESTIONS[pred_class],
            "top_factors": top_factors,
        }

    # ── LLM 后处理 ──────────────────────────────────────

    def generate_llm_analysis(self, result: dict[str, Any], orchard_id: int) -> str:
        """
        调用 LLM 生成自然语言分析和防治方案。
        """
        if not self.ready:
            return ""

        level = result["risk_level"]
        probs = result["probabilities"]
        label = result["risk_label"]
        factors = result["top_factors"]

        # 构建提示词
        factors_text = "\n".join([
            f"- {f['feature']}: 值={f['value']:.2f}, "
            f"{'增加' if f['direction']=='positive' else '降低'}风险 (贡献={f['contribution']:+.2f})"
            for f in factors
        ])

        prompt = f"""你是一个柑橘植保专家。请根据以下风险预测结果，为果园(ID={orchard_id})生成一份详细的风险分析报告。

## 风险等级
- 当前等级: {label} (Level {level}/3)
- 各等级概率: 无风险={probs[0]:.1%}, 低风险={probs[1]:.1%}, 中风险={probs[2]:.1%}, 高风险={probs[3]:.1%}

## 主要风险因子 (Top-5)
{factors_text}

## 要求
请用中文生成以下内容（每部分2-3句话）：
1. 风险概况：当前果园的整体风险状态
2. 主要风险因素：根据风险因子分析原因
3. 防治建议：针对性的防治方案（具体药剂、措施）
4. 监测建议：后续建议的监测频率和重点

注意：不要编造具体的药剂名称，只提及防治策略方向。如果风险等级为0（无风险），只需简单说明情况。
"""

        try:
            content, usage, model = llm_service.chat_with_fallback(
                messages=[{"role": "user", "content": prompt}],
                model=settings.llm_default_model,
                temperature=0.3,
                max_tokens=1024,
            )
            return content.strip()
        except Exception as e:
            logger.warning("LLM analysis failed: %s", e)
            return f"【风险等级 {label}】{RISK_SUGGESTIONS[level]}"

    # ── 外部调用入口 ─────────────────────────────────────

    async def evaluate_orchard(self, orchard_id: int) -> dict[str, Any]:
        """
        完整评估一个果园：查询特征 → 预测 → LLM分析。
        """
        # 查询特征
        features = await self._query_features(orchard_id)
        # 计算派生特征
        features = self._compute_derived_features(features)

        # 预测
        result = self.predict(features)
        result["orchard_id"] = orchard_id
        result["features"] = features

        # LLM 分析
        llm_analysis = self.generate_llm_analysis(result, orchard_id)
        result["llm_analysis"] = llm_analysis

        return result


# ── 全局单例 ──
prediction_service = PredictionService()
