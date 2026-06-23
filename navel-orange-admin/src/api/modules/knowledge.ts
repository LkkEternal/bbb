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

export function deleteDocument(id: number) {
  return request.delete(`/admin/knowledge/documents/${id}`)
}

export function processDocument(id: number) {
  return request.post(`/admin/knowledge/documents/${id}/process`)
}

export function searchKnowledge(query: string) {
  return request.post('/knowledge/search', { query })
}

export function askQuestion(question: string, orchardId?: number) {
  return request.post('/knowledge/qa', { question, orchardId })
}
