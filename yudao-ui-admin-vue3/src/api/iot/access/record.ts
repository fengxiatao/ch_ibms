import request from '@/config/axios'

// ==================== 门禁记录 API ====================

export interface AccessRecordVO {
  id?: number
  deviceId: number
  deviceName?: string
  personId?: number
  personName?: string
  cardNo?: string
  openType: number
  openResult: number
  openTime?: Date
  temperature?: number
  imageUrl?: string
  remark?: string
  createTime?: Date
}

export interface AccessRecordPageReqVO extends PageParam {
  deviceId?: number
  personId?: number
  cardNo?: string
  openType?: number
  openResult?: number
  openTime?: [Date, Date]
}

// 分页查询门禁记录
export const getAccessRecordPage = (params: AccessRecordPageReqVO) => {
  return request.get<{
    total: number
    list: AccessRecordVO[]
  }>({
    url: '/iot/access-record/page',
    params
  })
}

// 获取门禁记录详情
export const getAccessRecord = (id: number) => {
  return request.get<AccessRecordVO>({
    url: `/iot/access-record/get?id=${id}`
  })
}

// 导出门禁记录 Excel
export const exportAccessRecordExcel = (params: AccessRecordPageReqVO) => {
  return request.download({
    url: '/iot/access-record/export-excel',
    params
  })
}

// 获取门禁统计数据
export const getAccessStatistics = (params: {
  deviceId?: number
  startTime?: Date
  endTime?: Date
}) => {
  return request.get<{
    totalCount: number
    successCount: number
    failCount: number
    todayCount: number
  }>({
    url: '/iot/access-record/statistics',
    params
  })
}

// 🆕 获取通行方式统计（按开门类型分组）
export const getAccessMethodStatistics = (params: {
  startTime?: Date
  endTime?: Date
}) => {
  return request.get<{
    methodName: string
    count: number
  }[]>({
    url: '/iot/access-record/statistics/method',
    params
  })
}

// 🆕 获取24小时人员流量统计（按小时分组）
export const getHourlyTrafficStatistics = (params: {
  date?: Date  // 指定日期，默认今天
}) => {
  return request.get<{
    hour: number
    inCount: number
    outCount: number
  }[]>({
    url: '/iot/access-record/statistics/hourly',
    params
  })
}

