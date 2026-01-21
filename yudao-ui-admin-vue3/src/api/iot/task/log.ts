import request from '@/config/axios'

export interface TaskLogVO {
  id: number
  configId: number
  jobTypeCode: string
  jobName: string
  entityType: string
  entityId: number
  entityName: string
  startTime: Date
  endTime?: Date
  durationMs?: number
  executionStatus: string
  affectedCount?: number
  resultSummary?: string
  executorInfo?: string
}

export interface TaskLogDetailVO extends TaskLogVO {
  resultDetail?: string
  errorMessage?: string
  errorStack?: string
}

// 获取日志分页
export const getLogPage = (params: any) => {
  return request.get({ url: '/iot/task-log/page', params })
}

// 获取日志详情
export const getLogDetail = (id: number) => {
  return request.get<TaskLogDetailVO>({ url: `/iot/task-log/get/${id}` })
}

// 获取任务执行历史
export const getTaskHistory = (configId: number, params: any) => {
  return request.get({ url: `/iot/task-log/history/${configId}`, params })
}

// 清理旧日志
export const cleanOldLogs = (retentionDays: number) => {
  return request.delete({ url: '/iot/task-log/clean', params: { retentionDays } })
}

export const TaskLogApi = {
  getLogPage,
  getLogDetail,
  getTaskHistory,
  cleanOldLogs
}
