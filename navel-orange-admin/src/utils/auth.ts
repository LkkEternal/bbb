const TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token_id'
const USER_KEY = 'user_info'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRefreshTokenId(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshTokenId(id: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, id)
}

export function removeRefreshTokenId(): void {
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function getUserInfo(): any {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setUserInfo(info: any): void {
  localStorage.setItem(USER_KEY, JSON.stringify(info))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_KEY)
}

export function clearAuth(): void {
  removeToken()
  removeRefreshTokenId()
  removeUserInfo()
}
