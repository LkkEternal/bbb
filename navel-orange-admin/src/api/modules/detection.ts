import request from '../request'

export function getDetectionList(params: {
  page?: number
  size?: number
  orchardId?: number
  diseaseCode?: string
  startDate?: string
  endDate?: string
}) {
  return request.get('/detections', { params })
}

export function getDetectionStats(params: { orchardId?: number; startDate?: string; endDate?: string }) {
  return request.get('/detections/stats', { params })
}

export function getEnvironmentList(params: { orchardId?: number; page?: number; size?: number }) {
  return request.get('/environment', { params })
}

export function getEnvironmentTrend(params: { orchardId?: number; days?: number }) {
  return request.get('/environment/trend', { params })
}

export function getWeatherNow(orchardId: number) {
  return request.get('/weather/now', { params: { orchardId } })
}

export function getWeatherForecast(orchardId: number) {
  return request.get('/weather/forecast', { params: { orchardId } })
}
