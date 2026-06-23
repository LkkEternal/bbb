import request from '../request'

export function getUserList(params: { page?: number; size?: number; keyword?: string }) {
  return request.get('/admin/users', { params })
}

export function getUserDetail(id: number) {
  return request.get(`/admin/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return request.put(`/admin/users/${id}/status`, { status })
}

export function assignRole(id: number, roleId: number) {
  return request.put(`/admin/users/${id}/roles`, { roleId })
}

export function getOperationLogs(params: { page?: number; size?: number; action?: string; userId?: number }) {
  return request.get('/admin/logs', { params })
}

export function getProfile() {
  return request.get('/user/profile')
}

export function updatePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }) {
  return request.put('/user/password', data)
}

export function updateNotificationSettings(data: { smsEnabled?: boolean; pushEnabled?: boolean }) {
  return request.put('/user/notification-settings', data)
}
