import { request } from '@/utils/request'

/** 消息 */
export interface MessageItem {
  id: number
  title: string
  type: number
  typeName: string
  content: string
  sendTime: string
  isRead: boolean
  createdAt: string
}

/** 消息列表查询参数 */
export interface MessageListParams {
  page: number
  pageSize: number
  keyword?: string
  type?: number
  isRead?: boolean
}

/** 消息列表响应 */
export interface MessageListResult {
  list: MessageItem[]
  total: number
}

/** 获取消息列表 */
export function getMessageList(params: MessageListParams) {
  return request<MessageListResult>({
    url: '/message/list',
    method: 'GET',
    params,
  })
}

/** 标记已读 */
export function markRead(ids: number[]) {
  return request<void>({
    url: '/message/markRead',
    method: 'PUT',
    data: { ids },
  })
}

/** 删除消息 */
export function deleteMessage(id: number) {
  return request<void>({
    url: `/message/${id}`,
    method: 'DELETE',
  })
}

/** 批量删除消息 */
export function batchDeleteMessage(ids: number[]) {
  return request<void>({
    url: '/message/batchDelete',
    method: 'DELETE',
    data: { ids },
  })
}
