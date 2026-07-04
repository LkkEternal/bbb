import request from '../request'

// ─── 响应 VO 类型 ───

/** 识别记录列表项 VO */
export interface DetectionListVO {
  /** 记录 ID */
  id: number
  /** 设备编码 */
  deviceCode: string
  /** 设备名称 */
  deviceName: string
  /** 果园 ID */
  orchardId: number
  /** 果园名称 */
  orchardName: string
  /** 识别图片 URL */
  imageUrl: string
  /** 病害编码（如 S4） */
  topClassCode: string
  /** 病害名称（如"红鼻果"） */
  topClassName: string
  /** 置信度（如 0.94） */
  topProbability: number
  /** 所有病害概率（JSON 字符串） */
  allProbabilities: string
  /** 设备识别时间 */
  deviceTimestamp: string
  /** 服务端接收时间 */
  createdAt: string
}

/** 病害分布项 */
export interface DiseaseDistribution {
  /** 病害编码 */
  diseaseCode: string
  /** 病害名称 */
  diseaseName: string
  /** 数量 */
  count: number
  /** 百分比 */
  percentage: number
}

/** 每日趋势项 */
export interface DailyTrendItem {
  /** 日期（如 2026-06-01） */
  date: string
  /** 识别数量 */
  count: number
  /** 预警数量 */
  alertCount: number
}

/** 识别统计数据 VO */
export interface DetectionStatsVO {
  /** 识别总数 */
  totalCount: number
  /** 预警数量 */
  alertCount: number
  /** 病害分布 */
  diseaseDistribution: DiseaseDistribution[]
  /** 每日趋势 */
  dailyTrend: DailyTrendItem[]
}

// ─── API 函数 ───

/**
 * 分页查询识别记录
 * @param params.orchardId 可选，按果园筛选
 * @param params.diseaseCode 可选，按病害编码筛选
 * @param params.startDate 可选，起始日期（含）
 * @param params.endDate 可选，结束日期（含）
 * @param params.page 页码，默认 1
 * @param params.size 每页条数，默认 20
 */
export function getDetectionList(params?: {
  page?: number
  size?: number
  orchardId?: number
  diseaseCode?: string
  startDate?: string
  endDate?: string
}) {
  return request.get('/detections', { params })
}

/**
 * 获取识别统计
 * @param params.orchardId 可选，按果园筛选
 * @param params.startDate 起始日期（必填）
 * @param params.endDate 结束日期（必填）
 */
export function getDetectionStats(params: {
  orchardId?: number
  startDate: string
  endDate: string
}) {
  return request.get('/detections/stats', { params })
}

/**
 * 删除识别记录
 */
export function deleteDetection(id: number) {
  return request.delete(`/detections/${id}`)
}
