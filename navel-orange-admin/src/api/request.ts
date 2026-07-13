import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, setToken, getRefreshTokenId, clearAuth } from '@/utils/auth'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

let isRefreshing = false
let pendingRequests: Array<(token: string) => void> = []

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 如果是 blob 类型响应（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }

    // 401 未认证 → 清除登录态并跳转登录页
    if (code === 401) {
      const userStore = useUserStore()
      userStore.token = null
      userStore.userInfo = null
      clearAuth()
      ElMessage.error('未认证，请先登录')
      router.push('/login')
      return Promise.reject(new Error('未认证，请先登录'))
    }

    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  async (error) => {
    const { response, config } = error
    if (!response) {
      ElMessage.error('网络错误，请检查连接')
      return Promise.reject(error)
    }

    if (response.status === 401 && !config._retry) {
      config._retry = true

      if (!isRefreshing) {
        isRefreshing = true
        const refreshTokenId = getRefreshTokenId()
        if (refreshTokenId) {
          try {
            const res = await axios.post(`${import.meta.env.VITE_API_BASE}/auth/refresh`, {
              refreshTokenId,
            })
            const { accessToken, refreshTokenId: newId } = res.data.data
            setToken(accessToken)
            if (newId) localStorage.setItem('refresh_token_id', newId)
            pendingRequests.forEach(cb => cb(accessToken))
            pendingRequests = []
            config.headers.Authorization = `Bearer ${accessToken}`
            return service(config)
          } catch {
            const userStore = useUserStore()
            userStore.token = null
            userStore.userInfo = null
            clearAuth()
            ElMessage.error('登录已过期，请重新登录')
            router.push('/login')
            return Promise.reject(error)
          } finally {
            isRefreshing = false
          }
        } else {
          const userStore = useUserStore()
          userStore.token = null
          userStore.userInfo = null
          clearAuth()
          ElMessage.error('未认证，请先登录')
          router.push('/login')
          return Promise.reject(error)
        }
      }

      return new Promise((resolve) => {
        pendingRequests.push((token: string) => {
          config.headers.Authorization = `Bearer ${token}`
          resolve(service(config))
        })
      })
    }

    // 401 重试也失败 → 明确身份过期，强制跳转登录页
    if (response.status === 401) {
      const userStore = useUserStore()
      userStore.token = null
      userStore.userInfo = null
      clearAuth()
      ElMessage.error('未认证，请先登录')
      router.push('/login')
      return Promise.reject(error)
    }

    if (response.status === 403) {
      ElMessage.warning('无权限访问')
    } else if (response.status === 429) {
      ElMessage.warning('请求过于频繁，请稍后再试')
    } else if (response.data?.message) {
      ElMessage.error(response.data.message)
    }

    return Promise.reject(error)
  },
)

export default service
