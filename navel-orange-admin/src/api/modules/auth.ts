import request from '../request'

export function loginByPassword(data: { phone: string; password: string }) {
  return request.post('/auth/login/password', data)
}

export function loginBySms(data: { phone: string; smsCode: string }) {
  return request.post('/auth/login/sms', data)
}

export function register(data: { phone: string; username: string; password: string; smsCode: string }) {
  return request.post('/auth/register', data)
}

export function sendSmsCode(data: { phone: string; scene: string }) {
  return request.post('/auth/sms/send', data)
}

export function refreshToken(refreshTokenId: string) {
  return request.post('/auth/refresh', { refreshTokenId })
}

export function logout(refreshTokenId: string) {
  return request.post('/auth/logout', { refreshTokenId })
}
