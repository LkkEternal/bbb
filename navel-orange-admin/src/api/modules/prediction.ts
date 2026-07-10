import request from '../request'

// ─── 响应 VO 类型 ───

/** 风险因子项 */
export interface RiskFactorVO {
  feature: string
  value: number
  contribution: number
  direction: string
}

/** 风险预测结果 VO */
export interface RiskPredictionVO {
  id: number
  orchardId: number
  riskLevel: number
  riskScore: number
  probabilities: string
  llmAnalysis: string
  suggestions: string
  topFactors: string
  triggerType: string
  triggerDetail: string
  createdAt: string
}

/** 预测详情（解析后的） */
export interface PredictionDetail {
  id: number
  orchardId: number
  riskLevel: number
  riskScore: number
  probabilities: number[]
  llmAnalysis: string
  suggestions: string
  topFactors: RiskFactorVO[] | null
  triggerType: string
  triggerDetail: string
  createdAt: string
  /** 友好标签 */
  riskLabel: string
  riskColor: string
}

// 风险等级映射
export const RISK_LEVEL_MAP: Record<number, { label: string; color: string; icon: string }> = {
  0: { label: '无风险', color: '#67c23a', icon: 'CircleCheckFilled' },
  1: { label: '低风险', color: '#e6a23c', icon: 'WarningFilled' },
  2: { label: '中风险', color: '#f56c6c', icon: 'WarningFilled' },
  3: { label: '高风险', color: '#f56c6c', icon: 'CircleCloseFilled' },
}

/**
 * 解析预测 JSON 为详情对象
 */
export function parsePrediction(row: RiskPredictionVO): PredictionDetail {
  let probs: number[] = [1, 0, 0, 0]
  let factors: RiskFactorVO[] | null = null
  try { probs = JSON.parse(row.probabilities || '[1,0,0,0]') } catch { /* ignore */ }
  try { factors = JSON.parse(row.topFactors || 'null') } catch { /* ignore */ }

  const level = row.riskLevel ?? 0
  const info = RISK_LEVEL_MAP[level] || RISK_LEVEL_MAP[0]

  return {
    ...row,
    probabilities: probs,
    topFactors: factors,
    riskLabel: info.label,
    riskColor: info.color,
  }
}

// ─── API 函数 ───

/**
 * 获取某个果园的最新预测结果
 */
export function getLatestPrediction(orchardId: number) {
  return request.get('/prediction/latest', { params: { orchardId } })
}

/**
 * 获取某个果园的历史预测记录
 */
export function getPredictionHistory(params: {
  orchardId: number
  limit?: number
}) {
  return request.get('/prediction/history', { params })
}

/**
 * 手动触发某个果园的预测
 */
export function triggerPrediction(orchardId: number, triggerType?: string) {
  return request.post('/prediction/trigger', null, {
    params: { orchardId, triggerType: triggerType || 'manual' },
  })
}
