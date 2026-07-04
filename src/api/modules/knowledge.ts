import { request } from '@/utils/request'

/** 知识库搜索结果项 */
export interface KnowledgeItem {
  id: number
  /** 标题 */
  title: string
  /** 摘要 / 内容片段 */
  summary: string
  /** 相似度评分 (0~1) */
  score: number
  /** 来源文献 */
  source: string
  /** 创建时间 */
  createdAt?: string
}

/** 知识搜索查询参数 */
export interface KnowledgeSearchParams {
  page: number
  pageSize: number
  keyword?: string
}

/** 知识搜索响应 */
export interface KnowledgeSearchResult {
  list: KnowledgeItem[]
  total: number
}

/** 搜索知识库 */
export function searchKnowledge(params: KnowledgeSearchParams) {
  return request<KnowledgeSearchResult>({
    url: '/knowledge/search',
    method: 'GET',
    params,
  })
}
