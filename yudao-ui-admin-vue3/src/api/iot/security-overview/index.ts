import request from '@/config/axios'

/**
 * 安防概览 - 摄像头VO
 */
export interface SecurityOverviewCameraVO {
  id: number
  deviceName: string
  nickname: string
  location: string
  online: boolean
  status: string
  statusText: string
  snapshotUrl?: string
  lastOnlineTime: number
  deviceKey: string
  deviceInfo?: {
    vendor: string
    httpPort: number
    rtspPort: number
    onvifPort: number
  }
}

/**
 * 安防概览 - 摄像头分页请求
 */
export interface SecurityOverviewCameraPageReqVO {
  pageNo: number
  pageSize: number
  includeSnapshot?: boolean
  onlineOnly?: boolean
}

/**
 * 获取安防概览摄像头列表
 * 
 * ⚠️ 超时设置：120秒
 * 原因：如果 includeSnapshot=true，后端需要通过物模型逐个获取快照
 * - 100个设备 * 0.5秒/快照 = 50秒+
 * - 需要足够的时间避免超时
 */
export const getSecurityOverviewCameras = (params: SecurityOverviewCameraPageReqVO) => {
  return request.get<{
    total: number
    list: SecurityOverviewCameraVO[]
  }>({ 
    url: '/iot/security-overview/cameras', 
    params,
    timeout: 120000  // 🆕 120秒超时（批量获取快照需要较长时间）
  })
}

/**
 * 获取单个设备的实时抓图
 */
export const getDeviceSnapshot = (deviceId: number) => {
  return request.get<string>({ 
    url: `/iot/security-overview/snapshot/${deviceId}` 
  })
}

/**
 * 播放地址响应VO
 */
export interface PlayUrlRespVO {
  wsFlvUrl?: string      // WebSocket-FLV 播放地址（超低延时 < 500ms）
  webrtcUrl?: string     // WebRTC 播放地址（超低延时 < 500ms）
  wsFmp4Url?: string     // WebSocket-FMP4 播放地址（低延时 ~1-2秒）
  fmp4Url?: string       // HTTP-FMP4 播放地址（低延时 ~1-2秒）
  flvUrl?: string        // HTTP-FLV 播放地址（低延时 ~1秒）
  hlsUrl?: string        // HLS 播放地址（延时 5-10秒）
  rtmpUrl?: string       // RTMP 播放地址（需专用播放器）
  streamKey?: string     // 流标识
}

/**
 * 获取设备播放地址
 * 
 * ⚠️ 超时设置：60秒
 * 原因：首次拉流需要时间（RTSP连接、ZLMediaKit转码等）
 */
export const getPlayUrl = (deviceId: number) => {
  return request.get<PlayUrlRespVO>({ 
    url: `/iot/security-overview/play-url/${deviceId}`,
    timeout: 60000  // 🆕 60秒超时（首次拉流可能需要较长时间）
  })
}

// 导出默认对象
export default {
  getSecurityOverviewCameras,
  getDeviceSnapshot,
  getPlayUrl
}

