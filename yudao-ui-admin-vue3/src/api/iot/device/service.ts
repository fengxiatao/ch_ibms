import request from '@/config/axios'

export interface DeviceServiceInvokeReqVO {
  deviceId: number
  serviceIdentifier: string
  serviceName?: string
  serviceParams?: Record<string, any>
}

export interface DeviceServiceLogVO {
  id: number
  deviceId: number
  productId: number
  productKey: string
  deviceName: string
  serviceIdentifier: string
  serviceName: string
  requestId: string
  requestParams: string
  requestTime: Date
  statusCode?: number
  responseMessage?: string
  responseData?: string
  responseTime?: Date
  executionTime?: number
  operatorId?: number
  operatorName?: string
  createTime: Date
}

export interface DeviceServiceLogPageReqVO extends PageParam {
  deviceId?: number
  productId?: number
  serviceIdentifier?: string
  statusCode?: number
  requestTime?: Date[]
}

/**
 * 调用设备服务
 */
export const invokeDeviceService = (data: DeviceServiceInvokeReqVO) => {
  return request.post<{ requestId: string; message: string }>({
    url: '/iot/device/service/invoke',
    data
  })
}

/**
 * 获取服务调用日志分页
 */
export const getDeviceServiceLogPage = (params: DeviceServiceLogPageReqVO) => {
  return request.get<PageResult<DeviceServiceLogVO>>({
    url: '/iot/device/service/log/page',
    params
  })
}

/**
 * 获取服务调用日志详情
 */
export const getDeviceServiceLog = (id: number) => {
  return request.get<DeviceServiceLogVO>({
    url: '/iot/device/service/log/get',
    params: { id }
  })
}












