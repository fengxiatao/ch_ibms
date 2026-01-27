import request from '@/config/axios'

export interface NvrRespVO {
  id: number
  name: string
  ipAddress?: string  // 从 config 中提取的 IP 地址
  state?: number
}

export interface NvrChannelRespVO {
  id: number
  deviceId?: number
  name: string
  state?: number
  ipAddress?: string  // 从 config 中提取的 IP 地址
  channelNo?: number
  ptzSupport?: boolean
}

export const getNvrList = () => {
  return request.get<NvrRespVO[]>({ url: '/iot/video/nvr/list' })
}

export interface NvrPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
}

export const getNvrPage = (params: NvrPageReqVO) => {
  return request.get({ url: '/iot/video/nvr/page', params })
}

export const getNvrChannels = (nvrId: number, refresh?: number) => {
  const params = refresh ? { refresh } : {}
  return request.get<NvrChannelRespVO[]>({ url: `/iot/video/nvr/${nvrId}/channels`, params })
}

// 云台控制：连续移动（旧接口，保留兼容）
export const nvrPtzMove = (
  nvrId: number,
  data: { channelNo: number; pan?: number; tilt?: number; zoom?: number; timeoutMs?: number }
) => {
  return request.post<boolean>({ url: `/iot/video/nvr/${nvrId}/ptz/move`, data })
}

// 云台控制：停止（旧接口，保留兼容）
export const nvrPtzStop = (
  nvrId: number,
  data: { channelNo: number; panTilt?: boolean; zoom?: boolean }
) => {
  return request.post<boolean>({ url: `/iot/video/nvr/${nvrId}/ptz/stop`, data })
}

/**
 * 云台控制：命令模式（推荐使用）
 * 支持的命令：UP, DOWN, LEFT, RIGHT, LEFT_UP, RIGHT_UP, LEFT_DOWN, RIGHT_DOWN,
 *           ZOOM_IN (ZoomTele), ZOOM_OUT (ZoomWide), FOCUS_NEAR (FocusNear), 
 *           FOCUS_FAR (FocusFar), IRIS_OPEN (IrisLarge), IRIS_CLOSE (IrisSmall)
 */
export interface NvrPtzControlReq {
  channelNo: number
  command: string   // PTZ 命令
  speed?: number    // 速度 1-8，默认 4
  stop?: boolean    // true=停止，false=开始
}

export const nvrPtzControl = (nvrId: number, data: NvrPtzControlReq) => {
  return request.post<string>({ url: `/iot/video/nvr/${nvrId}/ptz/control`, data })
}

/**
 * 预设点控制
 * 支持的操作：GOTO（转到预设点）、SET（设置预设点）、CLEAR（删除预设点）
 */
export interface NvrPresetControlReq {
  channelNo: number    // 通道号
  presetNo: number     // 预设点编号（1-255）
  action: string       // 操作：GOTO, SET, CLEAR
  presetName?: string  // 预设点名称（SET操作时使用）
}

export const nvrPresetControl = (nvrId: number, data: NvrPresetControlReq) => {
  return request.post<string>({ url: `/iot/video/nvr/${nvrId}/ptz/preset`, data })
}

/**
 * 3D定位/区域放大控制
 * 用于在视频画面上框选区域进行快速定位放大
 * 
 * 坐标系说明：
 * - x, y 为归一化坐标（0-8192），其中 (0,0) 为画面左上角，(8192,8192) 为画面右下角
 * - 前端传入框选区域的像素坐标，后端会进行坐标转换
 */
export interface NvrAreaZoomReq {
  channelNo: number      // 通道号
  startX: number         // 框选起始点 X（归一化坐标 0-8192）
  startY: number         // 框选起始点 Y（归一化坐标 0-8192）
  endX: number           // 框选结束点 X（归一化坐标 0-8192）
  endY: number           // 框选结束点 Y（归一化坐标 0-8192）
}

export const nvrAreaZoom = (nvrId: number, data: NvrAreaZoomReq) => {
  return request.post<string>({ url: `/iot/video/nvr/${nvrId}/ptz/area-zoom`, data })
}

/**
 * 3D定位控制（直接指定中心点和放大倍数）
 */
export interface Nvr3DPositionReq {
  channelNo: number      // 通道号
  x: number              // 中心点 X（归一化坐标 0-8192）
  y: number              // 中心点 Y（归一化坐标 0-8192）
  zoom: number           // 放大倍数（1-128）
}

export const nvr3DPosition = (nvrId: number, data: Nvr3DPositionReq) => {
  return request.post<string>({ url: `/iot/video/nvr/${nvrId}/ptz/3d-position`, data })
}

// 云台控制：旧的网关直接调用接口（已废弃，保留兼容）
export interface PtzControlReq {
  ip: string
  port?: number
  username: string
  password: string
  channel: number
  command: string
  speed?: number
  stop: boolean
}

export const ptzControl = (data: PtzControlReq) => {
  // 直接调用网关接口（已废弃）
  const gatewayUrl = import.meta.env.VITE_GATEWAY_BASE_URL || 'http://localhost:8099'
  return request.post<any>({ 
    url: `${gatewayUrl}/api/ptz/control`, 
    data,
    timeout: 5000 
  })
}
