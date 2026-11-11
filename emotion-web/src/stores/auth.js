import { ref, computed } from 'vue'

const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const accessToken = ref(localStorage.getItem('accessToken'))
const refreshToken = ref(localStorage.getItem('refreshToken'))

export function useAuthStore() {
  const isLoggedIn = computed(() => !!accessToken.value && !!user.value)
  
  function login(data) {
    user.value = data.user
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('user', JSON.stringify(data.user))
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
  }
  
  function logout() {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }
  
  function setUser(data) {
    user.value = data
    localStorage.setItem('user', JSON.stringify(data))
  }
  
  function refreshTokenFn(newToken) {
    accessToken.value = newToken
    localStorage.setItem('accessToken', newToken)
  }
  
  return {
    user,
    accessToken,
    refreshToken,
    isLoggedIn,
    login,
    logout,
    setUser,
    refreshTokenFn
  }
}