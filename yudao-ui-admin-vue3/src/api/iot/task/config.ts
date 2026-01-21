import request from '@/config/axios'

export interface TaskConfigVO {
  id?: number
  entityType: string
  entityId: number
  entityName: string
  jobTypeCode: string
  name: string
  cronExpression?: string
  intervalSeconds?: number
  taskConfig?: any
  priority: number
  status: number
  lastExecuteTime?: Date
  nextExecuteTime?: Date
  executeCount?: number
  successCount?: number
  failureCount?: number
  avgDuration?: number
}

export interface TaskMonitorVO {
  id: number
  entityType: string
  entityId: number
  entityName: string
  jobTypeCode: string
  jobTypeName: string
  name: string
  cronExpression?: string
  intervalSeconds?: number
  priority: number
  status: number
  lastExecuteTime?: Date
  nextExecuteTime?: Date
  executeCount: number
  successCount: number
  failureCount: number
  avgDuration: number
}

export interface TaskStatisticsVO {
  totalTasks: number
  enabledTasks: number
  disabledTasks: number
  runningTasks: number
  recentSuccessCount: number
  recentFailureCount: number
}

// 创建任务
export const addTask = (data: TaskConfigVO) => {
  return request.post({ url: '/iot/task-config/add', data })
}

// 更新任务
export const updateTask = (data: TaskConfigVO) => {
  return request.put({ url: '/iot/task-config/update', data })
}

// 删除任务
export const deleteTask = (id: number) => {
  return request.delete({ url: `/iot/task-config/delete/${id}` })
}

// 获取任务详情
export const getTask = (id: number) => {
  return request.get({ url: `/iot/task-config/get/${id}` })
}

// 获取实体的任务列表
export const getTaskList = (params: { entityType: string; entityId: number }) => {
  return request.get({ url: '/iot/task-config/list', params })
}

// 获取监控列表（分页）
export const getMonitorPage = (params: any) => {
  return request.get({ url: '/iot/task-config/monitor/list', params })
}

// 获取统计数据
export const getStatistics = () => {
  return request.get<TaskStatisticsVO>({ url: '/iot/task-config/monitor/statistics' })
}

// 启用/禁用任务
export const toggleTask = (id: number) => {
  return request.put({ url: `/iot/task-config/toggle/${id}` })
}

// 立即执行任务
export const executeTask = (id: number) => {
  return request.post({ url: `/iot/task-config/execute/${id}` })
}

export const TaskConfigApi = {
  addTask,
  updateTask,
  deleteTask,
  getTask,
  getTaskList,
  getMonitorPage,
  getStatistics,
  toggleTask,
  executeTask
}
