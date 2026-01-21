/**
 * 请求封装
 * 说明：统一使用 '@/config/axios' 中的 Axios 封装，避免找不到 '@/utils/request'
 */
import request from '@/config/axios'

// ========== 设备配置数据结构 ==========

export interface NetworkConfig {
  ipAddress?: string
  subnetMask?: string
  gateway?: string
  dns?: string
  dhcpEnabled?: boolean
  httpPort?: number
  rtspPort?: number
  onvifPort?: number
}

export interface EventConfig {
  motionDetectionEnabled?: boolean
  motionSensitivity?: number
  videoLossEnabled?: boolean
  tamperDetectionEnabled?: boolean
  audioDetectionEnabled?: boolean
  audioThreshold?: number
}

export interface VideoConfig {
  resolution?: string
  frameRate?: number
  bitrate?: number
  codecType?: string
  quality?: number
  gopLength?: number
}

export interface DeviceConfigVO {
  deviceId: number
  deviceName: string
  networkConfig?: NetworkConfig
  eventConfig?: EventConfig
  videoConfig?: VideoConfig
}

export interface DeviceCapabilitiesVO {
  deviceId: number
  manufacturer: string
  model: string
  firmwareVersion: string
  supportedResolutions: string[]
  supportedCodecs: string[]
  maxFrameRate: number
  ptzSupported: boolean
  audioSupported: boolean
  motionDetectionSupported: boolean
  tamperDetectionSupported: boolean
  dhcpSupported: boolean
  recordingSupported: boolean
  mediaProfiles: MediaProfile[]
}

export interface MediaProfile {
  name: string
  token: string
  videoEncoding: string
  resolution: string
  frameRate: number
  bitrate: number
}

// ========== 设备配置 API ==========

// 获取设备配置
export const getDeviceConfig = (deviceId: number) => {
  return request.get<DeviceConfigVO>({ url: '/iot/device/config/get', params: { deviceId } })
}

// 更新网络配置
export const updateNetworkConfig = (deviceId: number, data: NetworkConfig) => {
  return request.put({ url: '/iot/device/config/network', params: { deviceId }, data })
}

// 更新事件配置
export const updateEventConfig = (deviceId: number, data: EventConfig) => {
  return request.put({ url: '/iot/device/config/event', params: { deviceId }, data })
}

// 更新视频配置
export const updateVideoConfig = (deviceId: number, data: VideoConfig) => {
  return request.put({ url: '/iot/device/config/video', params: { deviceId }, data })
}

// 从设备同步配置
export const syncConfigFromDevice = (deviceId: number) => {
  return request.post<DeviceConfigVO>({
    url: '/iot/device/config/sync-from-device',
    params: { deviceId }
  })
}

// 应用配置到设备
export const applyConfigToDevice = (deviceId: number) => {
  return request.post({ url: '/iot/device/config/apply-to-device', params: { deviceId } })
}

// 重置设备配置
export const resetDeviceConfig = (deviceId: number) => {
  return request.post({ url: '/iot/device/config/reset', params: { deviceId } })
}

// 获取设备能力集
export const getDeviceCapabilities = (deviceId: number) => {
  return request.get<DeviceCapabilitiesVO>({
    url: '/iot/device/config/capabilities',
    params: { deviceId }
  })
}












