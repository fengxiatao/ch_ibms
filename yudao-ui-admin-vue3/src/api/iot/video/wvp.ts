import request from '@/config/axios'

/**
 * WVP 播放地址返回结构
 */
export interface WvpPlayResult {
  code: number
  msg?: string
  data?: {
    // FLV 播放地址
    flv?: string
    ws_flv?: string
    wss_flv?: string
    // HLS 播放地址
    hls?: string
    ws_hls?: string
    wss_hls?: string
    // RTSP 播放地址
    rtsp?: string
    rtsps?: string
    // RTMP 播放地址
    rtmp?: string
    rtmps?: string
    // WebRTC 播放地址
    rtc?: string
    rtcs?: string
    // 流信息
    streamId?: string
    app?: string
    stream?: string
    mediaServerId?: string
    // 设备信息
    deviceId?: string
    channelId?: string
  }
}

/**
 * WVP 设备信息
 */
export interface WvpDevice {
  deviceId: string
  name: string
  manufacturer?: string
  model?: string
  firmware?: string
  transport?: string
  streamMode?: string
  online?: boolean
  registerTime?: string
  keepaliveTime?: string
  ip?: string
  port?: number
  hostAddress?: string
  charset?: string
  ssrcCheck?: boolean
  geoCoordSys?: string
  treeType?: string
  mediaServerId?: string
  channelCount?: number
}

/**
 * WVP 通道信息
 */
export interface WvpChannel {
  channelId: string
  deviceId: string
  name: string
  manufacturer?: string
  model?: string
  owner?: string
  civilCode?: string
  block?: string
  address?: string
  parental?: number
  parentId?: string
  safetyWay?: number
  registerWay?: number
  certNum?: string
  certifiable?: number
  errCode?: number
  endTime?: string
  secrecy?: number
  ipAddress?: string
  port?: number
  password?: string
  status?: number
  longitude?: number
  latitude?: number
  ptzType?: number
  hasAudio?: boolean
  createTime?: string
  updateTime?: string
  streamId?: string
  hasGps?: boolean
}

/**
 * 开始播放 - 点播
 * WVP API: GET /api/play/start/{deviceId}/{channelId}
 */
export const wvpPlayStart = (deviceId: string, channelId: string) => {
  return request.get<WvpPlayResult>({
    url: `/wvp-api/play/start/${deviceId}/${channelId}`
  })
}

/**
 * 停止播放
 * WVP API: GET /api/play/stop/{deviceId}/{channelId}
 */
export const wvpPlayStop = (deviceId: string, channelId: string) => {
  return request.get({
    url: `/wvp-api/play/stop/${deviceId}/${channelId}`
  })
}

/**
 * 获取设备列表
 * WVP API: GET /api/device/query/devices
 */
export const wvpGetDevices = (params?: { page?: number; count?: number; query?: string; online?: boolean }) => {
  return request.get({
    url: '/wvp-api/device/query/devices',
    params
  })
}

/**
 * 获取设备通道列表
 * WVP API: GET /api/device/query/devices/{deviceId}/channels
 */
export const wvpGetChannels = (deviceId: string, params?: { page?: number; count?: number; query?: string; online?: boolean; channelType?: boolean }) => {
  return request.get({
    url: `/wvp-api/device/query/devices/${deviceId}/channels`,
    params
  })
}

/**
 * 获取通道播放地址（已在播放中的流）
 * WVP API: GET /api/play/info/{deviceId}/{channelId}
 */
export const wvpGetPlayInfo = (deviceId: string, channelId: string) => {
  return request.get<WvpPlayResult>({
    url: `/wvp-api/play/info/${deviceId}/${channelId}`
  })
}

/**
 * 云台控制
 * WVP API: POST /api/ptz/control/{deviceId}/{channelId}
 */
export const wvpPtzControl = (deviceId: string, channelId: string, command: string, horizonSpeed?: number, verticalSpeed?: number, zoomSpeed?: number) => {
  return request.post({
    url: `/wvp-api/ptz/control/${deviceId}/${channelId}`,
    params: {
      command,
      horizonSpeed,
      verticalSpeed,
      zoomSpeed
    }
  })
}

/**
 * 录像查询
 * WVP API: GET /api/gb_record/query/{deviceId}/{channelId}
 */
export const wvpQueryRecord = (deviceId: string, channelId: string, startTime: string, endTime: string, sn?: number, type?: string) => {
  return request.get({
    url: `/wvp-api/gb_record/query/${deviceId}/${channelId}`,
    params: {
      startTime,
      endTime,
      sn,
      type
    }
  })
}

/**
 * 录像回放
 * WVP API: GET /api/playback/start/{deviceId}/{channelId}
 */
export const wvpPlaybackStart = (deviceId: string, channelId: string, startTime: string, endTime: string) => {
  return request.get<WvpPlayResult>({
    url: `/wvp-api/playback/start/${deviceId}/${channelId}`,
    params: {
      startTime,
      endTime
    }
  })
}

/**
 * 停止录像回放
 * WVP API: GET /api/playback/stop/{deviceId}/{channelId}/{streamId}
 */
export const wvpPlaybackStop = (deviceId: string, channelId: string, streamId: string) => {
  return request.get({
    url: `/wvp-api/playback/stop/${deviceId}/${channelId}/${streamId}`
  })
}

// ==================== 通用通道 API（推荐使用）====================

/**
 * 获取 WVP 通用通道列表
 * WVP API: GET /api/common/channel/list
 */
export const wvpGetCommonChannelList = (params?: {
  page?: number
  count?: number
  query?: string
  online?: boolean
  channelType?: string
}) => {
  return request.get({
    url: '/wvp-api/common/channel/list',
    params: {
      page: params?.page || 1,
      count: params?.count || 100,
      query: params?.query || '',
      online: params?.online,
      channelType: params?.channelType || ''
    }
  })
}

/**
 * 播放通道（通用 API）
 * WVP API: GET /api/common/channel/play
 */
export const wvpCommonPlay = (channelId: number | string) => {
  return request.get({
    url: '/wvp-api/common/channel/play',
    params: { channelId }
  })
}

/**
 * 停止播放通道（通用 API）
 * WVP API: GET /api/common/channel/play/stop
 */
export const wvpCommonStop = (channelId: number | string) => {
  return request.get({
    url: '/wvp-api/common/channel/play/stop',
    params: { channelId }
  })
}

/**
 * 云台控制（通用 API）
 * command: up/down/left/right/upleft/upright/downleft/downright/zoomin/zoomout/stop
 */
export const wvpCommonPtz = (
  channelId: number | string,
  command: string,
  speed: number = 50
) => {
  return request.get({
    url: '/wvp-api/common/channel/front-end/ptz',
    params: {
      channelId,
      command,
      panSpeed: speed,
      tiltSpeed: speed,
      zoomSpeed: speed
    }
  })
}

/**
 * 查询预置位
 */
export const wvpQueryPreset = (channelId: number | string) => {
  return request.get({
    url: '/wvp-api/common/channel/front-end/preset/query',
    params: { channelId }
  })
}

/**
 * 调用预置位
 */
export const wvpCallPreset = (channelId: number | string, presetId: number) => {
  return request.get({
    url: '/wvp-api/common/channel/front-end/preset/call',
    params: { channelId, presetId }
  })
}

/**
 * 添加预置位
 */
export const wvpAddPreset = (channelId: number | string, presetId: number, presetName: string) => {
  return request.get({
    url: '/wvp-api/common/channel/front-end/preset/add',
    params: { channelId, presetId, presetName }
  })
}

/**
 * 删除预置位
 */
export const wvpDeletePreset = (channelId: number | string, presetId: number) => {
  return request.get({
    url: '/wvp-api/common/channel/front-end/preset/delete',
    params: { channelId, presetId }
  })
}

/**
 * 查询录像列表（通用 API）
 */
export const wvpCommonQueryRecord = (channelId: number | string, startTime: string, endTime: string) => {
  return request.get({
    url: '/wvp-api/common/channel/playback/query',
    params: { channelId, startTime, endTime }
  })
}

/**
 * 开始回放（通用 API）
 */
export const wvpCommonPlayback = (channelId: number | string, startTime: string, endTime: string) => {
  return request.get({
    url: '/wvp-api/common/channel/playback',
    params: { channelId, startTime, endTime }
  })
}

/**
 * 停止回放（通用 API）
 */
export const wvpCommonStopPlayback = (channelId: number | string, stream: string) => {
  return request.get({
    url: '/wvp-api/common/channel/playback/stop',
    params: { channelId, stream }
  })
}

/**
 * 回放倍速（通用 API）
 */
export const wvpCommonPlaybackSpeed = (channelId: number | string, stream: string, speed: number) => {
  return request.get({
    url: '/wvp-api/common/channel/playback/speed',
    params: { channelId, stream, speed }
  })
}

/**
 * 回放暂停/恢复
 */
export const wvpCommonPlaybackPause = (channelId: number | string, stream: string) => {
  return request.get({
    url: '/wvp-api/common/channel/playback/pause',
    params: { channelId, stream }
  })
}

export const wvpCommonPlaybackResume = (channelId: number | string, stream: string) => {
  return request.get({
    url: '/wvp-api/common/channel/playback/resume',
    params: { channelId, stream }
  })
}
