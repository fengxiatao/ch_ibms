import request from '@/config/axios'

export interface DeviceScanReqVO {
  scanType?: string
  timeout?: number
  ipRange?: string
}

export interface DiscoveredDevice {
  id?: number
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
  status: string
  devices?: DiscoveredDevice[]
}

/** IBMS 设备发现（后端表 ibms_discovered_device，路径 /iot/ibms/discovery） */
export const startScan = (data: DeviceScanReqVO) => {
  return request.post<{ scanId: string; status: string }>({
    url: '/iot/ibms/discovery/scan',
    data
  })
}

export const getScanResult = (scanId: string) => {
  return request.get<DeviceScanResultVO>({
    url: `/iot/ibms/discovery/result/${scanId}`
  })
}

export const getScanStatus = (scanId: string) => {
  return request.get<{ scanId: string; status: string }>({
    url: `/iot/ibms/discovery/status/${scanId}`
  })
}

export const getRecentDiscoveredDevices = (hours: number = 24) => {
  return request.get<DiscoveredDevice[]>({
    url: '/iot/ibms/discovery/recent',
    params: { hours }
  })
}

export const getUnaddedDevices = () => {
  return request.get<DiscoveredDevice[]>({
    url: '/iot/ibms/discovery/unadded',
    timeout: 20000
  })
}

export const ignoreDevice = (id: number, ignoreDays?: number, reason?: string) => {
  return request.post<boolean>({
    url: `/iot/ibms/discovery/ignore/${id}`,
    params: { ignoreDays, reason }
  })
}

export const unignoreDevice = (id: number) => {
  return request.post<boolean>({
    url: `/iot/ibms/discovery/unignore/${id}`
  })
}

export const markAsPending = (id: number) => {
  return request.post<boolean>({
    url: `/iot/ibms/discovery/pending/${id}`
  })
}
