import request from '@/config/axios'

// ==================== 门禁告警 API ====================

export interface AccessAlarmVO {
  id?: number
  deviceId: number
  deviceName?: string
  alarmType: number
  alarmLevel: number
  alarmContent?: string
  alarmTime?: Date
  handleStatus: number
  handleUserId?: number
  handleUserName?: string
  handleTime?: Date
  handleRemark?: string
  createTime?: Date
}

export interface AccessAlarmPageReqVO extends PageParam {
  deviceId?: number
  alarmType?: number
  alarmLevel?: number
  handleStatus?: number
  alarmTime?: [Date, Date]
}

export interface AccessAlarmHandleReqVO {
  id: number
  handleRemark?: string
}

// 分页查询门禁告警
export const getAccessAlarmPage = (params: AccessAlarmPageReqVO) => {
  return request.get<{
    total: number
    list: AccessAlarmVO[]
  }>({
    url: '/iot/access-alarm/page',
    params
  })
}

// 获取门禁告警详情
export const getAccessAlarm = (id: number) => {
  return request.get<AccessAlarmVO>({
    url: `/iot/access-alarm/get?id=${id}`
  })
}

// 处理门禁告警
export const handleAccessAlarm = (data: AccessAlarmHandleReqVO) => {
  return request.put({
    url: '/iot/access-alarm/handle',
    data
  })
}

// 批量处理门禁告警
export const handleAccessAlarmBatch = (ids: number[], remark?: string) => {
  return request.put({
    url: '/iot/access-alarm/handle-batch',
    data: { ids, handleRemark: remark }
  })
}

// 导出门禁告警 Excel
export const exportAccessAlarmExcel = (params: AccessAlarmPageReqVO) => {
  return request.download({
    url: '/iot/access-alarm/export-excel',
    params
  })
}

// 🆕 获取告警类型统计
export const getAlarmTypeStatistics = (params: {
  startTime?: Date
  endTime?: Date
}) => {
  return request.get<{
    alarmType: number
    count: number
  }[]>({
    url: '/iot/access-alarm/statistics/type',
    params
  })
}

