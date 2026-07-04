import { request } from '@/utils/request'

/** 设备信息 */
export interface DeviceItem {
  id: number
  /** 设备名称 */
  name: string
  /** 设备编号 */
  deviceCode: string
  /** 设备类型 */
  type: string
  /** 状态：0=离线 1=在线 */
  status: number
  /** 安装时间 */
  installDate: string
  /** 所属果园ID */
  orchardId: number
  /** 备注 */
  remark?: string
  /** 创建时间 */
  createdAt?: string
  /** 更新时间 */
  updatedAt?: string
}

/** 设备列表查询参数 */
export interface DeviceListParams {
  page: number
  pageSize: number
  orchardId: number
  keyword?: string
  type?: string
  status?: number | ''
}

/** 设备列表响应 */
export interface DeviceListResult {
  list: DeviceItem[]
  total: number
}

/** 新增/编辑设备参数 */
export interface DeviceFormData {
  name: string
  deviceCode: string
  type: string
  status: number
  installDate: string
  orchardId: number
  remark?: string
}

/** 获取设备列表 */
export function getDeviceList(params: DeviceListParams) {
  return request<DeviceListResult>({
    url: '/device/list',
    method: 'GET',
    params,
  })
}

/** 获取设备详情 */
export function getDeviceDetail(id: number) {
  return request<DeviceItem>({
    url: `/device/${id}`,
    method: 'GET',
  })
}

/** 新增设备 */
export function createDevice(data: DeviceFormData) {
  return request<void>({
    url: '/device',
    method: 'POST',
    data,
  })
}

/** 编辑设备 */
export function updateDevice(id: number, data: DeviceFormData) {
  return request<void>({
    url: `/device/${id}`,
    method: 'PUT',
    data,
  })
}

/** 删除设备 */
export function deleteDevice(id: number) {
  return request<void>({
    url: `/device/${id}`,
    method: 'DELETE',
  })
}
