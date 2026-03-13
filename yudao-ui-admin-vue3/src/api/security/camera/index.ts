import request from '@/config/axios'

export interface CameraDevice {
  id: number
  name: string
  productId: number
  deviceKey: string
  online: boolean
  ip: string
  streamUrl: string
  ptzEnabled: boolean
  presetCount: number
  config: Record<string, any>
  lastOnlineTime: string
}

export interface PTZControlRequest {
  deviceId: number
  direction: string
  speed?: number
}

export interface PresetControlRequest {
  deviceId: number
  action: 'goto' | 'set' | 'delete'
  presetId: number
}

export interface RecordControlRequest {
  deviceId: number
  action: 'start' | 'stop'
  duration?: number
}

// 获取摄像机设备列表
export const getCameraDeviceList = () => {
  return request.get({
    url: '/security/camera/list'
  })
}

// 获取摄像机设备详情
export const getCameraDevice = (id: number) => {
  return request.get({
    url: `/security/camera/${id}`
  })
}

// 获取摄像机实时流地址
export const getCameraStreamUrl = (deviceId: number) => {
  return request.get({
    url: `/security/camera/${deviceId}/stream`
  })
}

// PTZ云台控制
export const controlPTZ = (data: PTZControlRequest) => {
  return request.post({
    url: '/security/camera/ptz/control',
    data
  })
}

// 预置位控制
export const controlPreset = (data: PresetControlRequest) => {
  return request.post({
    url: '/security/camera/preset/control',
    data
  })
}

// 录像控制
export const controlRecord = (data: RecordControlRequest) => {
  return request.post({
    url: '/security/camera/record/control',
    data
  })
}

// 摄像机抓拍
export const takeSnapshot = (deviceId: number) => {
  return request.post({
    url: `/security/camera/${deviceId}/snapshot`
  })
}

// 获取摄像机能力集
export const getCameraCapabilities = (deviceId: number) => {
  return request.get({
    url: `/security/camera/${deviceId}/capabilities`
  })
}

// 从IoT模块获取摄像机设备（用于集成IoT数据）
export const getCameraDevicesFromIoT = () => {
  return request.get({
    url: '/security/camera/iot/devices'
  })
}

/** Security 摄像头分页（用于录像回放等，返回 security_camera 表数据） */
export interface SecurityCameraPageReqVO {
  pageNo?: number
  pageSize?: number
  floorId?: number
  buildingId?: number
  nvrId?: number
  cameraName?: string
}

export interface SecurityCameraVO {
  id: number
  cameraName: string
  channelNo: number
  nvrId?: number
  status?: number
  buildingId?: number
  floorId?: number
  [key: string]: any
}

/** 分页查询 Security 摄像头（用于录像回放设备树，确保使用 security_camera.id） */
export const getSecurityCameraPage = (params: SecurityCameraPageReqVO) => {
  return request.get<{ list: SecurityCameraVO[]; total: number }>({
    url: '/security/camera/page',
    params
  })
}




















