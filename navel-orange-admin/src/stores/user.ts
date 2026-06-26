import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, clearAuth, getRefreshTokenId, setRefreshTokenId, setUserInfo, getUserInfo } from '@/utils/auth'
import { loginByPassword, loginBySms, logout as logoutApi } from '@/api/modules/auth'
import { getOrchardList } from '@/api/modules/orchard'

interface UserInfo {
  id: number
  phone: string
  username: string
  avatarUrl?: string
  roles: Array<{ id: number; code: string; name: string }>
  orchardRole?: string // 果园角色：owner / manager / viewer
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
  const orchardRole = computed(() => userInfo.value?.orchardRole || '')
  const isOrchardOwner = computed(() => orchardRole.value === 'owner')
  const isOrchardManager = computed(() => orchardRole.value === 'manager')
  const isOrchardViewer = computed(() => orchardRole.value === 'viewer')
  const isFarmer = computed(() => roles.value.includes('ROLE_FARMER'))

  const hasOrchard = ref(false)
  const orchardChecked = ref(false)

  async function checkOrchard() {
    try {
      const data: any = await getOrchardList()
      const list = Array.isArray(data) ? data : data?.records ?? []
      hasOrchard.value = list.length > 0
      orchardChecked.value = true
    } catch {
      hasOrchard.value = false
      orchardChecked.value = true
    }
  }

  function setHasOrchard(val: boolean) {
    hasOrchard.value = val
    orchardChecked.value = true
  }

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
      orchardRole: data.orchardRole,
    }
    setToken(data.accessToken)
    setRefreshTokenId(data.refreshTokenId)
    setUserInfo(userInfo.value)
    await checkOrchard()
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
      orchardRole: data.orchardRole,
    }
    setToken(data.accessToken)
    setRefreshTokenId(data.refreshTokenId)
    setUserInfo(userInfo.value)
    await checkOrchard()
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
    hasOrchard.value = false
    orchardChecked.value = false
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
    orchardRole,
    isOrchardOwner,
    isOrchardManager,
    isOrchardViewer,
    isFarmer,
    hasOrchard,
    orchardChecked,
    checkOrchard,
    setHasOrchard,
    loginByPwd,
    loginBySmsCode,
    doLogout,
  }
})
