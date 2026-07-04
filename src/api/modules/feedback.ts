import { request } from '@/utils/request'

/** 用户反馈 */
export interface FeedbackItem {
  id: number
  title: string
  username: string
  phone?: string
  orchardName?: string
  description: string
  imageUrl?: string
  imageUrls?: string[]
  status: number
  statusName?: string
  createdAt: string
  updatedAt?: string
  datasetTag?: string
  reviewComment?: string
  reviewedAt?: string
}

/** 反馈列表查询参数 */
export interface FeedbackListParams {
  page: number
  pageSize: number
  keyword?: string
  status?: number
}

/** 反馈列表响应 */
export interface FeedbackListResult {
  list: FeedbackItem[]
  total: number
}

/** 获取反馈列表 */
export function getFeedbackList(params: FeedbackListParams) {
  return request<FeedbackListResult>({
    url: '/feedback/list',
    method: 'GET',
    params,
  })
}

/** 获取反馈详情 */
export function getFeedbackDetail(id: number) {
  return request<FeedbackItem>({
    url: `/feedback/${id}`,
    method: 'GET',
  })
}

/** 审核反馈 */
export function reviewFeedback(
  id: number,
  data: {
    action: 'accept' | 'revise' | 'reject'
    comment?: string
    correctedDisease?: string
  },
) {
  return request<void>({
    url: `/feedback/${id}/review`,
    method: 'POST',
    data,
  })
}

/** 删除反馈 */
export function deleteFeedback(id: number) {
  return request<void>({
    url: `/feedback/${id}`,
    method: 'DELETE',
  })
}
