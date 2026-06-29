import request from '../request'

export function getFeedbackList(params: { page?: number; size?: number; status?: number }) {
  return request.get('/admin/feedbacks', { params })
}

export function getFeedbackDetail(id: number) {
  return request.get(`/admin/feedbacks/${id}`)
}

export function reviewFeedback(id: number, data: { action: 'accept' | 'revise' | 'reject'; reason?: string; correctedDisease?: string }) {
  return request.post(`/admin/feedbacks/${id}/review`, data)
}

export function exportDataset() {
  return request.get('/admin/feedbacks/export-dataset', { responseType: 'blob' })
}
