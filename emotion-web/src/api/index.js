import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.request.use(config => {
  const authStore = useAuthStore()
  if (authStore.accessToken.value) {
    config.headers.Authorization = `Bearer ${authStore.accessToken.value}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res && res.code !== 200) {
      const err = new Error(res.message || '请求失败')
      err.response = response
      err.code = res.code
      return Promise.reject(err)
    }
    return response
  },
  async error => {
    const authStore = useAuthStore()
    const originalRequest = error.config
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      if (!authStore.refreshToken.value) {
        authStore.logout()
        router.push('/login')
        return Promise.reject(error)
      }
      try {
        const res = await api.post('/auth/refresh', {
          refreshToken: authStore.refreshToken.value
        })
        const newToken = res.data.data.accessToken
        authStore.refreshTokenFn(newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch {
        authStore.logout()
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  register: data => api.post('/auth/register', data),
  login: data => api.post('/auth/login', data),
  refresh: data => api.post('/auth/refresh', data),
  logout: () => api.post('/auth/logout')
}

export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  changePassword: data => api.put('/user/password', data),
  updateNotificationSettings: data => api.put('/user/notification-settings', data),
  uploadAvatar: file => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const detectionAPI = {
  detect: (file, extra = {}) => {
    const formData = new FormData()
    formData.append('file', file)
    if (extra.modelGender != null) formData.append('modelGender', extra.modelGender)
    if (extra.modelAge != null) formData.append('modelAge', extra.modelAge)
    if (extra.modelRange != null) formData.append('modelRange', extra.modelRange)
    return api.post('/detection/detect', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  getHistory: params => api.get('/detection/history', { params }),
  getDetail: id => api.get(`/detection/${id}`),
  submitFeedback: (recordId, data) => api.post(`/detection/${recordId}/feedback`, data),
  getFeedbackStatus: recordId => api.get(`/detection/${recordId}/feedback/status`),
  getStats: () => api.get('/detection/stats')
}

export const weatherAPI = {
  getCurrent: location => {
    const params = location ? { location } : {}
    return api.get('/weather/current', { params })
  },
  getLatest: () => api.get('/weather/latest'),
  getForecast: location => api.get('/weather/forecast', { params: { location } })
}

export const alertAPI = {
  getMessages: params => api.get('/alert/messages', { params }),
  getSummary: () => api.get('/alert/messages/summary'),
  markRead: id => api.put(`/alert/messages/${id}/read`)
}

export const adminAPI = {
  getUsers: params => api.get('/admin/users', { params }),
  getUserDetail: id => api.get(`/admin/users/${id}`),
  updateUserStatus: (id, data) => api.put(`/admin/users/${id}/status`, data),
  getLogs: params => api.get('/admin/logs', { params }),
  getAlertRules: () => api.get('/admin/alert-rules'),
  updateAlertRule: (id, data) => api.put(`/admin/alert-rules/${id}`, data),
  getEmotionDict: () => api.get('/admin/emotion-dict'),
  updateEmotionDict: (id, data) => api.put(`/admin/emotion-dict/${id}`, data),
  getFeedbacks: params => api.get('/admin/feedbacks', { params }),
  reviewFeedback: (id, data) => api.put(`/admin/feedbacks/${id}/review`, data),
  exportFeedbacks: () => api.get('/admin/feedbacks/export', { responseType: 'blob' })
}

export const chatAPI = {
  send: data => api.post('/chat/send', data),
  getHistory: params => api.get('/chat/history', { params })
}

export const assessmentAPI = {
  getList: () => api.get('/assessment/list'),
  start: data => api.post('/assessment/start', data),
  answer: data => api.post('/assessment/answer', data),
  prev: data => api.post('/assessment/prev', data),
  getHistory: params => api.get('/assessment/history', { params }),
  submit: data => api.post('/assessment/submit', data),
  getDetail: id => api.get(`/assessment/detail/${id}`)
}

export const documentAPI = {
  parse: file => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/assessment/document/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 310000
    })
  },
  confirm: data => api.post('/assessment/document/confirm', data)
}

export const assessmentAdminAPI = {
  getList: params => api.get('/assessment/admin/questionnaires', { params }),
  getDetail: id => api.get(`/assessment/admin/questionnaire/${id}`),
  toggleActive: id => api.put(`/assessment/admin/questionnaire/${id}/toggle-active`)
}

export default api