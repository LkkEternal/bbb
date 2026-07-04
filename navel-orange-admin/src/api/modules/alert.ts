import request from '../request'

export function getAlertRules(params?: { page?: number; size?: number }) {
  return request.get('/admin/rules', { params })
}

export function updateAlertRule(id: number, data: any) {
  return request.put(`/admin/rules/${id}`, data)
}

export function toggleAlertRule(id: number) {
  return request.put(`/admin/rules/${id}/toggle`)
}

export function getRiskAssessment(orchardId: number) {
  return request.get('/alert/risk', { params: { orchardId } })
}

/**
 * 创建预警规则
 */
export function createAlertRule(data: any) {
  return request.post('/alert/rules', data)
}

/**
 * 删除预警规则
 */
export function deleteAlertRule(id: number) {
  return request.delete(`/alert/rules/${id}`)
}
