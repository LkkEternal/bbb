import request from '../request'

export function getCalendarList(params?: { page?: number; size?: number }) {
  return request.get('/admin/calendar', { params })
}

export function createCalendar(data: any) {
  return request.post('/admin/calendar', data)
}

export function updateCalendar(id: number, data: any) {
  return request.put(`/admin/calendar/${id}`, data)
}

export function deleteCalendar(id: number) {
  return request.delete(`/admin/calendar/${id}`)
}

export function getCurrentReminder() {
  return request.get('/miniapp/calendar/current')
}
