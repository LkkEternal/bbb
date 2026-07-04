import { request } from '@/utils/request'

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  avatar?: string
  role: string
  roleName?: string
  status: number
  createdAt: string
  updatedAt?: string
}

/** 修改个人资料参数 */
export interface UpdateProfileParams {
  realName?: string
  phone?: string
  email?: string
  avatar?: string
}

/** 修改密码参数 */
export interface UpdatePasswordParams {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/** 获取当前用户信息 */
export function getUserProfile() {
  return request<UserInfo>({
    url: '/user/profile',
    method: 'GET',
  })
}

/** 修改个人资料 */
export function updateProfile(data: UpdateProfileParams) {
  return request<void>({
    url: '/user/profile',
    method: 'PUT',
    data,
  })
}

/** 修改密码 */
export function updatePassword(data: UpdatePasswordParams) {
  return request<void>({
    url: '/user/password',
    method: 'PUT',
    data,
  })
}
