import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, clearAuth, getRefreshTokenId, setRefreshTokenId, setUserInfo, getUserInfo } from '@/utils/auth'
import { loginByPassword, loginBySms, logout as logoutApi } from '@/api/modules/auth'

interface UserInfo {
  id: number
  phone: string
  username: string
  avatarUrl?: string
  roles: Array<{ id: number; code: string; name: string }>
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const refreshTokenId = ref<string | null>(getRefreshTokenId())
  const userInfo = ref<UserInfo | null>(getUserInfo())

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const avatarUrl = computed(() => userInfo.value?.avatarUrl || '')
  const roles = computed(() => userInfo.value?.roles?.map(r => r.code) || [])
  const isAdmin = computed(() => roles.value.includes('ROLE_ADMIN'))

  async function loginByPwd(phone: string, password: string) {
    const data: any = await loginByPassword({ phone, password })
    token.value = data.accessToken
    refreshTokenId.value = data.refreshTokenId
    userInfo.value = {
      id: data.id,
      phone: data.phone,
      username: data.username,
      avatarUrl: data.avatarUrl,
      roles: data.roles,
    }
    setToken(data.accessToken)
    setRefreshTokenId(data.refreshTokenId)
    setUserInfo(userInfo.value)
    return data
  }

  async function loginBySmsCode(phone: string, smsCode: string) {
    const data: any = await loginBySms({ phone, smsCode })
    token.value = data.accessToken
    refreshTokenId.value = data.refreshTokenId
    userInfo.value = {
      id: data.id,
      phone: data.phone,
      username: data.username,
      avatarUrl: data.avatarUrl,
      roles: data.roles,
    }
    setToken(data.accessToken)
    setRefreshTokenId(data.refreshTokenId)
    setUserInfo(userInfo.value)
    return data
  }

  async function doLogout() {
    try {
      if (refreshTokenId.value) {
        await logoutApi(refreshTokenId.value)
      }
    } catch {}
    token.value = null
    refreshTokenId.value = null
    userInfo.value = null
    clearAuth()
  }

  return {
    token,
    refreshTokenId,
    userInfo,
    isLoggedIn,
    username,
    avatarUrl,
    roles,
    isAdmin,
    loginByPwd,
    loginBySmsCode,
    doLogout,
  }
})
