import request from '@/config/axios'

// 报警事件记录 VO
export interface IotAlarmEventVO {
  id?: number
  hostId?: number
  hostName?: string
  eventCode: string
  eventName: string
  eventLevel: string
  area: string
  point: string
  sequence: string
  timestamp: number
  eventTime?: string
  paramDesc?: string
  isAlarm?: boolean
  isRestore?: boolean
  needRecord?: boolean
  status?: number // 0-未处理，1-已处理，2-已忽略
  processor?: string
  processTime?: string
  processRemark?: string
  createTime?: Date
}

// 报警事件分页查询 VO
export interface IotAlarmEventPageReqVO {
  pageNo: number
  pageSize: number
  // 后端使用 createTime[] 过滤（LocalDateTime[]）
  startTime?: string // 前端便捷字段：会在请求前转换为 createTime[0]
  endTime?: string   // 前端便捷字段：会在请求前转换为 createTime[1]
  createTime?: string[]
  eventLevel?: string
  hostId?: number
  // 处理状态：0-未处理，1-已处理，2-已忽略
  status?: number
  eventCode?: string
}

// 报警事件统计 VO
export interface IotAlarmEventStatsVO {
  urgentCount: number
  todayCount: number
  activeHosts: number
  processedRate: number
  // 顶部统计条字段
  total: number
  alarm: number
  restore: number
  other: number
}

// 查询报警事件记录分页
export const getAlarmEventPage = (params: IotAlarmEventPageReqVO) => {
  const req: any = { ...params }
  // 兼容后端字段：createTime
  if (!req.createTime && (req.startTime || req.endTime)) {
    req.createTime = [req.startTime, req.endTime].filter(Boolean)
  }
  // 清理前端便捷字段
  delete req.startTime
  delete req.endTime
  return request.get({ url: '/iot/alarm/event/page', params: req })
}

// 获取报警事件详情
export const getAlarmEvent = (id: number) => {
  return request.get({ url: '/iot/alarm/event/get?id=' + id })
}

// 处理报警事件
export const processAlarmEvent = (data: {
  id: number
  result: string
  actions: string[]
  remark: string
}) => {
  return request.put({ url: '/iot/alarm/event/process', data })
}

// 忽略报警事件
export const ignoreAlarmEvent = (id: number) => {
  return request.put({ url: '/iot/alarm/event/ignore?id=' + id })
}

// 获取报警事件统计
export const getAlarmEventStats = () => {
  return request.get({ url: '/iot/alarm/event/stats' })
}

// 导出报警事件记录
export const exportAlarmEvent = (params: IotAlarmEventPageReqVO) => {
  const req: any = { ...params }
  if (!req.createTime && (req.startTime || req.endTime)) {
    req.createTime = [req.startTime, req.endTime].filter(Boolean)
  }
  delete req.startTime
  delete req.endTime
  return request.download({ url: '/iot/alarm/event/export', params: req })
}
