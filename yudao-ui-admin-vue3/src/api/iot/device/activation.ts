import request from '@/config/axios'

export interface DeviceActivationReqVO {
  productId: number
  ipAddress: string
  username: string
  password: string
  vendor?: string
  model?: string
  serialNumber?: string
  firmwareVersion?: string
  deviceType?: string
  httpPort?: number
  rtspPort?: number
  onvifPort?: number
}

export interface DeviceActivationRespVO {
  activationId: string
  status: string // activating, completed, failed, not_found
  deviceId?: number
}

/**
 * 激活设备
 * @param data 激活参数
 */
export const activateDevice = (data: DeviceActivationReqVO) => {
  return request.post<{ activationId: string; status: string }>({
    url: '/iot/device/activation/activate',
    data
  })
}

/**
 * 获取激活结果
 * @param activationId 激活ID
 */
export const getActivationResult = (activationId: string) => {
  return request.get<DeviceActivationRespVO>({
    url: `/iot/device/activation/result/${activationId}`
  })
}

/**
 * 获取激活状态
 * @param activationId 激活ID
 */
export const getActivationStatus = (activationId: string) => {
  return request.get<{ activationId: string; status: string }>({
    url: `/iot/device/activation/status/${activationId}`
  })
}

/**
 * 断开设备连接
 * @param deviceId 设备ID
 */
export const disconnectDevice = (deviceId: number) => {
  return request.post<boolean>({
    url: `/iot/device/activation/disconnect/${deviceId}`
  })
}

















