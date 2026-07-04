import { request } from '@/utils/request'

/** 农事日历事件 */
export interface CalendarEvent {
  id: number
  eventTitle: string
  phenology: string
  solarTerm: string
  startTime: string
  endTime: string
  farmingAdvice: string
  relatedDisease: string
  createdAt: string
  updatedAt: string
}

/** 日历事件列表查询参数 */
export interface CalendarListParams {
  page: number
  pageSize: number
  keyword?: string
}

/** 日历事件列表响应 */
export interface CalendarListResult {
  list: CalendarEvent[]
  total: number
}

/** 创建 / 更新日历事件参数 */
export interface CalendarEventForm {
  eventTitle: string
  phenology?: string
  solarTerm?: string
  startTime?: string
  endTime?: string
  farmingAdvice?: string
  relatedDisease?: string
}

/** 获取日历事件列表 */
export function getCalendarList(params: CalendarListParams) {
  return request<CalendarListResult>({
    url: '/calendar/list',
    method: 'GET',
    params,
  })
}

/** 创建日历事件 */
export function createCalendarEvent(data: CalendarEventForm) {
  return request<CalendarEvent>({
    url: '/calendar',
    method: 'POST',
    data,
  })
}

/** 更新日历事件 */
export function updateCalendarEvent(id: number, data: CalendarEventForm) {
  return request<CalendarEvent>({
    url: `/calendar/${id}`,
    method: 'PUT',
    data,
  })
}

/** 删除日历事件 */
export function deleteCalendarEvent(id: number) {
  return request<void>({
    url: `/calendar/${id}`,
    method: 'DELETE',
  })
}
