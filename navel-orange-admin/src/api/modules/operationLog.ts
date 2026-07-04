import request from '../request'

// ─── 类型定义 ───

/** 操作日志 VO */
export interface OperationLogVO {
  /** 日志 ID */
  id: number
  /** 操作人 */
  operator: string
  /** 操作模块 */
  module: string
  /** 操作类型（create / update / delete / login / logout / export / upload 等） */
  operationType: string
  /** 操作详情 */
  detail: string
  /** IP 地址 */
  ipAddress: string
  /** 操作时间 */
  operateTime: string
}

/** 操作日志查询参数 */
export interface GetOperationLogsParams {
  /** 页码 */
  page?: number
  /** 每页条数 */
  size?: number
  /** 关键词（搜索操作人/模块） */
  keyword?: string
  /** 操作类型筛选 */
  operationType?: string
}

/** 分页结果 VO */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/**
 * 分页查询操作日志
 * @param params 查询参数
 */
export function getOperationLogs(params?: GetOperationLogsParams): Promise<PageResult<OperationLogVO>> {
  return request.get('/admin/logs', { params })
}

/**
 * 删除操作日志
 * @param id 日志 ID
 */
export function deleteOperationLog(id: number): Promise<void> {
  return request.delete(`/admin/logs/${id}`)
}

/**
 * 批量删除操作日志
 * @param ids 日志 ID 列表
 */
export function deleteOperationLogs(ids: number[]): Promise<void> {
  return request.delete('/admin/logs/batch', { data: { ids } })
}
