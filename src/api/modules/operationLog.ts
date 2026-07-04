import { request } from '@/utils/request'

/** 操作日志 */
export interface OperationLogItem {
  id: number
  /** 操作人 */
  operator: string
  /** 操作模块 */
  module: string
  /** 操作类型 */
  operationType: string
  /** 操作详情 */
  detail: string
  /** IP 地址 */
  ip: string
  /** 操作时间 */
  createTime: string
}

/** 操作日志列表查询参数 */
export interface OperationLogListParams {
  page: number
  pageSize: number
  keyword?: string
}

/** 操作日志列表响应 */
export interface OperationLogListResult {
  list: OperationLogItem[]
  total: number
}

/** 获取操作日志列表 */
export function getOperationLogList(params: OperationLogListParams) {
  return request<OperationLogListResult>({
    url: '/operation-log/list',
    method: 'GET',
    params,
  })
}
