import request from '../request'

// ─── 响应 VO 类型 ───

/** 果园列表项 VO */
export interface OrchardListVO {
  /** 果园 ID */
  id: number
  /** 果园名称 */
  name: string
  /** 详细地址（JSON 中为 address） */
  formattedAddress: string
  /** 经纬度 */
  location: string
  /** 面积（亩，JSON 中为 area） */
  area: number
  /** 树龄（年） */
  treeAge: number
  /** 品种 */
  variety: string
  /** 当前用户角色：owner / manager / viewer */
  roleType: string
  /** 设备数量 */
  deviceCount: number
  /** 创建时间 */
  createdAt: string
}

/** 果园成员 VO */
export interface OrchardMemberVO {
  /** 用户 ID */
  userId: number
  /** 用户名 */
  username: string
  /** 角色类型：owner / manager / viewer */
  roleType: string
  /** 加入时间 */
  joinedAt: string
}

/** 设备列表项 VO */
export interface DeviceListVO {
  /** 设备 ID */
  id: number
  /** 设备编码 */
  deviceCode: string
  /** 设备名称 */
  name: string
  /** 设备类型：camera / sensor */
  type: string
  /** 设备型号 */
  model: string
  /** 安装位置描述 */
  locationDesc: string
  /** 状态：0=离线 1=在线 2=故障 */
  status: number
  /** 最后心跳时间 */
  lastHeartbeat: string
  /** 创建时间 */
  createdAt: string
}

/** 果园详情 VO */
export interface OrchardDetailVO {
  /** 果园 ID */
  id: number
  /** 果园名称 */
  name: string
  /** 详细地址 */
  formattedAddress: string
  /** 经纬度 */
  location: string
  /** 面积（亩） */
  areaMu: number
  /** 树龄（年） */
  treeAge: number
  /** 品种 */
  variety: string
  /** 当前用户角色：owner / manager / viewer */
  roleType: string
  /** 设备数量 */
  deviceCount: number
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
  /** 成员列表 */
  members: OrchardMemberVO[]
  /** 设备列表 */
  devices: DeviceListVO[]
}

// ─── 请求 DTO 类型 ───

/** 创建果园请求参数 */
export interface CreateOrchardDTO {
  /** 果园名称（必填） */
  name: string
  /** 详细地址（必填） */
  formattedAddress: string
  /** 经纬度 */
  location?: string
  /** 面积（亩） */
  areaMu?: number
  /** 树龄（年） */
  treeAge?: number
  /** 品种 */
  variety?: string
}

/** 更新果园请求参数 */
export interface UpdateOrchardDTO {
  /** 果园名称 */
  name?: string
  /** 详细地址 */
  formattedAddress?: string
  /** 经纬度 */
  location?: string
  /** 面积（亩） */
  areaMu?: number
  /** 树龄（年） */
  treeAge?: number
  /** 品种 */
  variety?: string
}

/** 添加果园成员请求参数 */
export interface AddMemberDTO {
  /** 被添加用户 ID（必填） */
  userId: number
  /** 角色类型：manager / viewer（必填） */
  roleType: string
}

// ─── API 函数 ───

/**
 * 获取果园列表
 * 普通用户：只返回关联的果园；管理员：返回所有果园
 * 返回非分页的 List<OrchardListVO>
 */
export function getOrchardList() {
  return request.get('/orchards')
}

/**
 * 获取果园详情
 * @param id 果园 ID
 */
export function getOrchardDetail(id: number) {
  return request.get(`/orchards/${id}`)
}

/**
 * 创建果园
 * 创建者自动成为园主，系统管理员不能创建果园
 * @param data 创建果园参数
 */
export function createOrchard(data: CreateOrchardDTO) {
  return request.post('/orchards', data)
}

/**
 * 更新果园信息
 * 园主和管理员可更新
 * @param id 果园 ID
 * @param data 更新参数
 */
export function updateOrchard(id: number, data: UpdateOrchardDTO) {
  return request.put(`/orchards/${id}`, data)
}

/**
 * 删除果园（软删除）
 * 只有园主可以删除
 * @param id 果园 ID
 */
export function deleteOrchard(id: number) {
  return request.delete(`/orchards/${id}`)
}

/**
 * 添加果园成员
 * 园主可添加管理员和查看者；管理员只能添加查看者
 * @param orchardId 果园 ID
 * @param data 添加成员参数
 */
export function addMember(orchardId: number, data: AddMemberDTO) {
  return request.post(`/orchards/${orchardId}/members`, data)
}

/**
 * 移除果园成员
 * 园主可移除管理员和查看者；管理员只能移除查看者
 * @param orchardId 果园 ID
 * @param userId 被移除用户 ID
 */
export function removeMember(orchardId: number, userId: number) {
  return request.delete(`/orchards/${orchardId}/members/${userId}`)
}

// ═══════════════════════════════════════════════════════════
// 设备相关
// ═══════════════════════════════════════════════════════════

/**
 * 获取果园设备列表
 * @param orchardId 果园 ID
 */
export function getDeviceList(orchardId: number) {
  return request.get(`/orchards/${orchardId}/devices`)
}

/**
 * 获取IoTDA平台上的所有设备
 * 包含设备是否已注册到本地系统的标记
 */
export function getIotdaDevices() {
  return request.get('/devices/iotda')
}

/**
 * 注册设备（绑定到果园）
 * 果园园主和管理员可以注册设备
 * @param data 注册设备参数
 */
export function registerDevice(data: {
  orchardId: number
  deviceCode: string
  name: string
  type: string
  model?: string
  locationDesc?: string
}) {
  return request.post('/devices', data)
}

/**
 * 更新设备
 * 果果园园主和管理员可操作
 * @param id 设备 ID
 * @param data 更新参数
 */
export function updateDevice(id: number, data: {
  name?: string
  model?: string
  locationDesc?: string
}) {
  return request.put(`/devices/${id}`, data)
}

/**
 * 同步所有设备状态
 * 从华为云IoTDA获取最新设备状态并更新到本地
 */
export function syncAllDevices() {
  return request.post('/devices/sync')
}

/**
 * 同步单个设备状态
 * @param id 设备 ID
 */
export function syncDevice(id: number) {
  return request.post(`/devices/${id}/sync`)
}

/**
 * 获取设备状态（包含IoTDA状态）
 * @param id 设备 ID
 */
export function getDeviceStatus(id: number) {
  return request.get(`/devices/${id}/status`)
}

/**
 * 获取设备传感器数据
 * @param id 设备 ID
 */
export function getDeviceSensors(id: number) {
  return request.get(`/devices/${id}/sensors`)
}

/**
 * 注销设备（删除）
 * 只有果园园主可以操作
 * @param id 设备 ID
 */
export function deleteDevice(id: number) {
  return request.delete(`/devices/${id}`)
}

/**
 * 下发设备命令（如云台控制）
 * @param id 设备 ID
 * @param data 命令参数
 */
export function sendCommand(id: number, data: { commandName: string; paras?: Record<string, number> }) {
  return request.post(`/devices/${id}/command`, data)
}
