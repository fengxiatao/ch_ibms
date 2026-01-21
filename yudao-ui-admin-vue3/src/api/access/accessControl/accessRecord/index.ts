import request from '@/config/axios'

export interface AccessRecordVO {
  id: number
  userId: number
  userName: string
  userCard: string
  deviceId: number
  deviceName: string
  deviceLocation: string
  accessType: string // entry, exit
  accessTime: Date
  accessResult: string // success, failed, denied
  accessMethod: string // card, face, fingerprint, qrcode
  temperature?: number
  photo?: string
  createTime: Date
}

export interface AccessRecordPageReqVO extends PageParam {
  userName?: string
  deviceName?: string
  accessType?: string
  accessResult?: string
  accessMethod?: string
  accessTime?: Date[]
}

// 查询通行记录列表
export const getAccessRecordPage = (params: AccessRecordPageReqVO) => {
  return request.get({ url: '/access/access-record/page', params })
}

// 查询通行记录详情
export const getAccessRecord = (id: number) => {
  return request.get({ url: '/access/access-record/get?id=' + id })
}

// 导出通行记录
export const exportAccessRecord = (params: AccessRecordPageReqVO) => {
  return request.download({ url: '/access/access-record/export-excel', params })
}

// 获取通行统计数据
export const getAccessStatistics = (params: { startTime: Date, endTime: Date, deviceId?: number }) => {
  return request.get({ url: '/access/access-record/statistics', params })
}

// 获取实时通行数据
export const getRealTimeAccessData = () => {
  return request.get({ url: '/access/access-record/real-time' })
}











