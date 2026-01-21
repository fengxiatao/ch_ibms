import request from '@/config/axios'

export interface DeviceEventLogVO {
  id: number
  deviceId: number
  productId: number
  productKey: string
  deviceName: string
  eventIdentifier: string
  eventName: string
  eventType: string
  eventData: string
  eventTime: Date
  onvifTopic?: string
  processed: boolean
  triggeredSceneRuleIds?: string
  generatedAlertRecordIds?: string
  createTime: Date
}

export interface DeviceEventLogPageReqVO extends PageParam {
  deviceId?: number
  productId?: number
  eventIdentifier?: string
  eventType?: string
  eventTime?: Date[]
}

/**
 * 获取设备事件日志分页
 */
export const getDeviceEventLogPage = (params: DeviceEventLogPageReqVO) => {
  return request.get<PageResult<DeviceEventLogVO>>({
    url: '/iot/device/event-log/page',
    params
  })
}

/**
 * 获取设备事件日志详情
 */
export const getDeviceEventLog = (id: number) => {
  return request.get<DeviceEventLogVO>({
    url: '/iot/device/event-log/get',
    params: { id }
  })
}












