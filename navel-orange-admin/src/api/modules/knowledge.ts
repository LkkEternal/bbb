import request from '../request'

export function uploadDocument(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/knowledge/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getDocumentList(params: { page?: number; size?: number }) {
  return request.get('/admin/knowledge/documents', { params })
}

export function getPendingDocuments(size: number = 5) {
  return request.get('/admin/knowledge/documents', {
    params: { page: 1, size, status: 0 }
  })
}

export function getFailedDocuments(size: number = 5) {
  return request.get('/admin/knowledge/documents', {
    params: { page: 1, size, status: 3 }
  })
}

export function deleteDocument(id: number) {
  return request.delete(`/admin/knowledge/documents/${id}`)
}

export function processDocument(id: number) {
  return request.post(`/admin/knowledge/documents/${id}/process`, null, {
    timeout: 180000,  // 文档处理可能较慢，3 分钟超时
  })
}

export function searchKnowledge(question: string) {
  return request.post('/knowledge/search', { question })
}

export function askQuestion(question: string, orchardId?: number) {
  return request.post('/knowledge/qa', { question, orchardId })
}
