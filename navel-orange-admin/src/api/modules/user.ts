import request from '../request'

// ─── 响应 VO 类型 ───

/** 角色 VO */
export interface RoleVO {
  /** 角色 ID */
  id: number
  /** 角色编码（如 ROLE_ADMIN、ROLE_FARMER） */
  code: string
  /** 角色名称（如管理员、农户） */
  name: string
}

/** 用户列表项 VO */
export interface UserVO {
  /** 用户 ID */
  id: number
  /** 手机号 */
  phone: string
  /** 用户名 / 昵称 */
  username: string
  /** 头像 URL */
  avatarUrl: string | null
  /** 角色列表 */
  roles: RoleVO[]
  /** 状态：0=禁用 1=启用 */
  status: number
  /** 注册时间 */
  createdAt: string
  /** 最后登录时间 */
  lastLoginAt: string
  /** 最后登录 IP */
  lastLoginIp: string
}

/** 用户详情 VO（目前字段与 UserVO 一致，预留扩展） */
export interface UserDetailVO extends UserVO {}

/** 分页结果 VO */
export interface PageResult<T> {
  /** 数据列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 当前页码 */
  page: number
  /** 每页条数 */
  size: number
  /** 总页数 */
  pages: number
}

// ─── 请求 DTO 类型 ───

/** 分页查询用户列表参数 */
export interface GetAdminUserListParams {
  /** 页码，默认 1 */
  page?: number
  /** 每页条数，默认 20 */
  size?: number
  /** 搜索关键字（手机号/用户名模糊匹配） */
  keyword?: string
  /** 状态筛选：0=禁用 1=启用 */
  status?: number
}

/** 更新用户状态请求参数 */
export interface UpdateUserStatusDTO {
  /** 状态：0=禁用 1=启用（必填） */
  status: number
}

/** 分配角色请求参数 */
export interface AssignRolesDTO {
  /** 角色 ID 列表 */
  roleIds: number[]
}

// ─── API 函数 ───

/**
 * 分页查询用户列表（管理员端）
 * 支持按关键字（手机号/用户名）模糊搜索，以及按状态筛选
 * @param params 查询参数
 * @returns Promise<PageResult<UserVO>>
 */
export function getAdminUserList(params?: GetAdminUserListParams) {
  return request.get('/admin/users', { params })
}

/**
 * 获取用户详情（管理员端）
 * @param id 用户 ID
 * @returns Promise<UserDetailVO>
 */
export function getAdminUserDetail(id: number) {
  return request.get(`/admin/users/${id}`)
}

/**
 * 更新用户状态（管理员端）
 * 启用或禁用指定用户的账号
 * @param id 用户 ID
 * @param data 状态参数
 */
export function updateUserStatus(id: number, data: UpdateUserStatusDTO) {
  return request.put(`/admin/users/${id}/status`, data)
}

/**
 * 为用户分配角色（管理员端）
 * 可同时分配多个角色，覆盖原有角色
 * @param id 用户 ID
 * @param data 角色 ID 列表
 */
export function assignRoles(id: number, roleIds: number[]) {
  return request.put(`/admin/users/${id}/roles`, roleIds)
}

// ─── 兼容旧调用的别名 ───

/**
 * 分页查询用户列表（等价于 getAdminUserList）
 * @deprecated 建议使用 getAdminUserList
 */
export function getUserList(params?: GetAdminUserListParams) {
  return getAdminUserList(params)
}

/**
 * 更新用户状态（兼容旧版签名 (id, status)）
 * @deprecated 建议使用 updateUserStatus(id, { status })
 */
export function updateUserStatusCompat(id: number, status: number) {
  return updateUserStatus(id, { status })
}

/**
 * 分配角色（单个角色，兼容旧版调用）
 * @deprecated 建议使用 assignRoles(id, [roleId])
 */
export function assignRole(id: number, roleId: number) {
  return assignRoles(id, [roleId])
}

// ─── 操作日志 ───

/** 操作日志查询参数 */
export interface GetOperationLogsParams {
  /** 页码 */
  page?: number
  /** 每页条数 */
  size?: number
  /** 操作类型（如 login / create / update / delete） */
  action?: string
  /** 操作用户 ID */
  userId?: number
}

/**
 * 分页查询操作日志
 * @param params 查询参数
 */
export function getOperationLogs(params?: GetOperationLogsParams) {
  return request.get('/admin/logs', { params })
}

// ─── 个人中心相关 ───

/**
 * 获取当前登录用户个人信息
 */
export function getProfile() {
  return request.get('/user/profile')
}

/** 修改密码请求参数 */
export interface UpdatePasswordDTO {
  /** 旧密码 */
  oldPassword: string
  /** 新密码 */
  newPassword: string
  /** 确认新密码 */
  confirmPassword: string
}

/**
 * 修改当前登录用户密码
 * @param data 密码参数
 */
export function updatePassword(data: UpdatePasswordDTO) {
  return request.put('/user/password', data)
}

/** 通知设置请求参数 */
export interface UpdateNotificationSettingsDTO {
  /** 是否启用邮件通知 */
  emailEnabled?: boolean
  /** 是否启用推送通知 */
  pushEnabled?: boolean
}

/**
 * 更新当前用户的通知设置
 * @param data 通知设置参数
 */
export function updateNotificationSettings(data: UpdateNotificationSettingsDTO) {
  return request.put('/user/notification-settings', data)
}

/**
 * 上传并更新用户头像
 * @param file 头像文件
 * @returns 头像URL
 */
export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 更新个人信息请求参数 */
export interface UpdateProfileDTO {
  email?: string
}

/**
 * 更新当前登录用户的个人信息
 * @param data 个人信息参数
 */
export function updateProfile(data: UpdateProfileDTO) {
  return request.put('/user/profile', data)
}
