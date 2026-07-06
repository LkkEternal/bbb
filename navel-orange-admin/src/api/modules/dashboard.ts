import request from '../request'

export function getDashboard(orchardId?: number) {
  return request.get('/miniapp/dashboard', { params: { orchardId } })
}

export function getOrchardMiniData(orchardId: number) {
  return request.get(`/miniapp/orchards/${orchardId}`)
}

export function getOrchardDetections(orchardId: number, params?: { page?: number; size?: number }) {
  return request.get(`/miniapp/orchards/${orchardId}/detections`, { params })
}

export function getOrchardEnvTrend(orchardId: number) {
  return request.get(`/miniapp/orchards/${orchardId}/environment/trend`)
}

export function getAlertsList(params?: { page?: number; size?: number }) {
  return request.get('/miniapp/alerts', { params })
}

/**
 * 获取消息详情
 * @param messageId 消息ID
 */
export function getMessageDetail(messageId: number) {
  return request.get(`/messages/${messageId}`)
}
