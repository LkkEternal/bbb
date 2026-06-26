import request from '../request'

/** 视频流配置 */
export interface VideoStreamConfig {
  host: string
  wsPort: number
  httpPort: number
}

/**
 * 获取视频流服务器配置
 */
export function getVideoConfig() {
  return request.get('/config/video')
}
