import request from '../request'

/** 固件包 VO */
export interface FirmwarePackageVO {
  id: number
  fileName: string
  version: string
  fileSize: number
  fileUrl: string
  md5?: string
  description?: string
  status: number        // 0=待发布 1=已发布 2=已废弃
  createdAt: string
  updatedAt: string
}

/** 新增固件包 DTO */
export interface FirmwarePackageDTO {
  fileName: string
  version: string
  fileSize: number
  fileUrl: string
  md5?: string
  description?: string
  status?: number
}

/** OTA 升级 DTO */
export interface OtaUpgradeDTO {
  packageId: number
}

/** 命令下发结果 */
export interface CommandResult {
  commandId: string
  resultCode: string
  response: any
  responseName: string
}

/** OBS 文件信息 */
export interface ObsFileInfo {
  fileName: string
  fileSize: number
  fileUrl: string
  etag: string
  lastModified: string
}

/** 新增固件包 */
export function createPackage(data: FirmwarePackageDTO) {
  return request.post('/ota/packages', data)
}

/** 固件包列表（分页） */
export function getPackageList(params: { page: number; size: number }) {
  return request.get<{ records: FirmwarePackageVO[]; total: number }>('/ota/packages', { params })
}

/** 向设备下发 OTA 升级命令 */
export function upgradeDevice(deviceId: number, data: OtaUpgradeDTO) {
  return request.post<CommandResult>(`/ota/devices/${deviceId}/upgrade`, data)
}
