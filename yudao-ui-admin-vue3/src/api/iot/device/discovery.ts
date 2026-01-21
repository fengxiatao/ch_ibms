import request from '@/config/axios'

export interface DeviceScanReqVO {
  scanType?: string
  timeout?: number
  ipRange?: string
}

export interface DiscoveredDevice {
  id?: number  // 记录ID（来自数据库）
  ip: string
  vendor: string
  model?: string
  serialNumber?: string
  firmwareVersion?: string
  deviceType: string
  httpPort?: number
  rtspPort?: number
  onvifPort?: number
  discoveryMethod: string
  discoveryTime: string
  online: boolean
  onvifSupported?: boolean
}

export interface DeviceScanResultVO {
  scanId: string
  status: string // scanning, completed, failed, not_found
  devices?: DiscoveredDevice[]
}

/**
 * 启动设备扫描
 * @param data 扫描参数
 */
export const startScan = (data: DeviceScanReqVO) => {
  return request.post<{ scanId: string; status: string }>({
    url: '/iot/device/discovery/scan',
    data
  })
}

/**
 * 获取扫描结果
 * @param scanId 扫描ID
 */
export const getScanResult = (scanId: string) => {
  return request.get<DeviceScanResultVO>({
    url: `/iot/device/discovery/result/${scanId}`
  })
}

/**
 * 获取扫描状态
 * @param scanId 扫描ID
 */
export const getScanStatus = (scanId: string) => {
  return request.get<{ scanId: string; status: string }>({
    url: `/iot/device/discovery/status/${scanId}`
  })
}

/**
 * 获取最近发现的设备（从数据库）
 * @param hours 时间范围（小时），默认24小时
 */
export const getRecentDiscoveredDevices = (hours: number = 24) => {
  return request.get<DiscoveredDevice[]>({
    url: '/iot/device/discovery/recent',
    params: { hours }
  })
}

/**
 * 获取未添加的发现设备（从数据库）
 */
export const getUnaddedDevices = () => {
  return request.get<DiscoveredDevice[]>({
    url: '/iot/device/discovery/unadded'
  })
}

/**
 * 忽略发现的设备
 * @param id 设备记录ID
 * @param ignoreDays 忽略天数（可选）
 * @param reason 忽略原因（可选）
 */
export const ignoreDevice = (id: number, ignoreDays?: number, reason?: string) => {
  return request.post<boolean>({
    url: `/iot/device/discovery/ignore/${id}`,
    params: { ignoreDays, reason }
  })
}

/**
 * 取消忽略设备
 * @param id 设备记录ID
 */
export const unignoreDevice = (id: number) => {
  return request.post<boolean>({
    url: `/iot/device/discovery/unignore/${id}`
  })
}

/**
 * 标记为待处理
 * @param id 设备记录ID
 */
export const markAsPending = (id: number) => {
  return request.post<boolean>({
    url: `/iot/device/discovery/pending/${id}`
  })
}

















