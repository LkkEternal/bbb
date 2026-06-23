import request from '../request'

// ─── TypeScript 类型定义 ───

/** 消息列表查询参数 */
export interface MessageListParams {
  /** 消息级别：1-提示 / 2-警告 / 3-严重 */
  level?: number
  /** 已读状态：0-未读 / 1-已读 */
  readStatus?: number
  /** 页码（默认 1） */
  page?: number
  /** 每页条数（默认 20） */
  size?: number
}

/** 消息列表项 */
export interface MessageListVO {
  id: number
  /** 消息类型：alert-预警 / calendar-农事日历 / system-系统通知 */
  messageType: string
  title: string
  summary: string
  /** 消息级别：1-提示 / 2-警告 / 3-严重 */
  level: number
  orchardId: number
  orchardName: string
  ruleCode: string
  /** 已读状态：0-未读 / 1-已读 */
  readStatus: number
  createdAt: string
}

/** 消息详情 */
export interface MessageDetailVO {
  id: number
  /** 消息类型：alert-预警 / calendar-农事日历 / system-系统通知 */
  messageType: string
  title: string
  content: string
  /** 消息级别：1-提示 / 2-警告 / 3-严重 */
  level: number
  orchardId: number
  orchardName: string
  ruleCode: string
  /** 已读状态：0-未读 / 1-已读 */
  readStatus: number
  /** 防控建议 */
  controlSuggestion: string
  createdAt: string
}

/** 未读消息数量统计 */
export interface UnreadCountVO {
  total: number
  l1Count: number
  l2Count: number
  l3Count: number
}

/** 标记全部已读返回结果 */
export interface MarkAllAsReadVO {
  markedCount: number
}

// ─── API 函数 ───

/**
 * 分页查询消息列表
 * @param params 查询参数（支持 level / readStatus 筛选，page 默认 1，size 默认 20）
 */
export function getMessageList(params?: MessageListParams) {
  return request.get('/messages', { params })
}

/**
 * 获取未读消息数量统计
 * @returns UnreadCountVO { total, l1Count, l2Count, l3Count }
 */
export function getUnreadCount() {
  return request.get('/messages/unread-count')
}

/**
 * 标记单条消息已读
 * @param id 消息 ID
 */
export function markAsRead(id: number) {
  return request.put(`/messages/${id}/read`)
}

/**
 * 标记所有消息已读
 * @returns MarkAllAsReadVO { markedCount: number }
 */
export function markAllAsRead() {
  return request.put('/messages/read-all')
}

/**
 * 获取消息详情（查看时自动标记已读）
 * @param id 消息 ID
 * @returns MessageDetailVO
 */
export function getMessageDetail(id: number) {
  return request.get(`/messages/${id}`)
}
