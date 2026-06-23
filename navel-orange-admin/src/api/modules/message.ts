import request from '../request'

export function getMessageList(params: { page?: number; size?: number; read?: boolean }) {
  return request.get('/messages', { params })
}

export function getUnreadCount() {
  return request.get('/messages/unread-count')
}

export function markAsRead(id: number) {
  return request.put(`/messages/${id}/read`)
}

export function markAllAsRead() {
  return request.put('/messages/read-all')
}

export function getMessageDetail(id: number) {
  return request.get(`/messages/${id}`)
}
