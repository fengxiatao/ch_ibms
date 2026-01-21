import request from '@/config/axios'

// ==================== 轮巡计划 ====================

export interface PatrolPlanVO {
  id?: number
  planName: string
  planCode: string
  description?: string
  status: number
  loopMode?: number // 循环模式：1-循环执行，2-执行一次
  executor?: string
  executorName?: string
  startDate?: string
  endDate?: string
  sort?: number
  createTime?: Date
  tasks?: PatrolTaskVO[]
}

export interface PatrolPlanPageReqVO extends PageParam {
  planName?: string
  planCode?: string
  status?: number
  createTime?: Date[]
}

// 获取轮巡计划分页
export const getPatrolPlanPage = (params: PatrolPlanPageReqVO) => {
  return request.get<PageResult<PatrolPlanVO>>({
    url: '/iot/patrol-plan/page',
    params
  })
}

// 获取轮巡计划详情
export const getPatrolPlan = (id: number) => {
  return request.get<PatrolPlanVO>({
    url: '/iot/patrol-plan/get',
    params: { id }
  })
}

// 创建轮巡计划
export const createPatrolPlan = (data: PatrolPlanVO) => {
  return request.post({
    url: '/iot/patrol-plan/create',
    data
  })
}

// 更新轮巡计划
export const updatePatrolPlan = (data: PatrolPlanVO) => {
  return request.put({
    url: '/iot/patrol-plan/update',
    data
  })
}

// 删除轮巡计划
export const deletePatrolPlan = (id: number) => {
  return request.delete({
    url: '/iot/patrol-plan/delete',
    params: { id }
  })
}

// 启动轮巡计划
export const startPatrolPlan = (id: number) => {
  return request.post({
    url: '/iot/patrol-plan/start',
    params: { id }
  })
}

// 停止轮巡计划
export const stopPatrolPlan = (id: number) => {
  return request.post({
    url: '/iot/patrol-plan/stop',
    params: { id }
  })
}

// ==================== 轮巡任务 ====================

export interface PatrolTaskVO {
  id?: number
  planId: number
  taskName: string
  taskCode: string
  description?: string
  taskOrder?: number // 任务执行顺序
  duration?: number // 任务总时长（秒）
  scheduleType: number
  scheduleConfig?: any
  timeSlots?: any[]
  loopMode: number
  intervalMinutes: number
  autoSnapshot?: boolean
  autoRecording?: boolean
  recordingDuration?: number
  aiAnalysis?: boolean
  alertOnAbnormal?: boolean
  alertUserIds?: string
  status: number
  sort?: number
  createTime?: Date
  scene?: PatrolSceneVO
}

export interface PatrolTaskPageReqVO extends PageParam {
  planId?: number
  taskName?: string
  status?: number
}

// 获取轮巡任务分页
export const getPatrolTaskPage = (params: PatrolTaskPageReqVO) => {
  return request.get<PageResult<PatrolTaskVO>>({
    url: '/iot/patrol-task/page',
    params
  })
}

// 获取轮巡任务列表（不分页）
export const getPatrolTaskList = (planId: number) => {
  return request.get<PatrolTaskVO[]>({
    url: '/iot/patrol-task/list',
    params: { planId }
  })
}

// 获取轮巡任务详情
export const getPatrolTask = (id: number) => {
  return request.get<PatrolTaskVO>({
    url: '/iot/patrol-task/get',
    params: { id }
  })
}

// 创建轮巡任务
export const createPatrolTask = (data: PatrolTaskVO) => {
  return request.post({
    url: '/iot/patrol-task/create',
    data
  })
}

// 更新轮巡任务
export const updatePatrolTask = (data: PatrolTaskVO) => {
  return request.put({
    url: '/iot/patrol-task/update',
    data
  })
}

// 删除轮巡任务
export const deletePatrolTask = (id: number) => {
  return request.delete({
    url: '/iot/patrol-task/delete',
    params: { id }
  })
}

// 更新任务顺序
export const updatePatrolTaskOrder = (data: { planId: number; taskIds: number[] }) => {
  return request.put({
    url: '/iot/patrol-task/update-order',
    data
  })
}

// ==================== 轮巡场景 ====================

export interface PatrolSceneVO {
  id?: number
  taskId: number
  sceneName: string
  sceneOrder?: number
  duration: number // 场景停留时长（秒），必填
  gridLayout: string
  gridCount: number
  description?: string
  status: number
  createTime?: Date
  channels?: PatrolSceneChannelVO[]
}

export interface PatrolSceneChannelVO {
  id?: number
  sceneId?: number
  gridPosition: number
  duration: number // 通道播放时长（秒）
  channelId?: number
  deviceId?: string
  channelNo?: number
  channelName?: string
  targetIp?: string
  targetChannelNo?: number
  streamUrlMain?: string
  streamUrlSub?: string
  config?: any
}

// 获取轮巡场景详情
export const getPatrolScene = (id: number) => {
  return request.get<PatrolSceneVO>({
    url: '/iot/patrol-scene/get',
    params: { id }
  })
}

// 获取任务的场景
export const getPatrolSceneByTaskId = (taskId: number) => {
  return request.get<PatrolSceneVO>({
    url: '/iot/patrol-scene/get-by-task',
    params: { taskId }
  })
}

// 创建轮巡场景
export const createPatrolScene = (data: PatrolSceneVO) => {
  return request.post({
    url: '/iot/patrol-scene/create',
    data
  })
}

// 更新轮巡场景
export const updatePatrolScene = (data: PatrolSceneVO) => {
  return request.put({
    url: '/iot/patrol-scene/update',
    data
  })
}

// 删除轮巡场景
export const deletePatrolScene = (id: number) => {
  return request.delete({
    url: '/iot/patrol-scene/delete',
    params: { id }
  })
}

// 保存场景（包含通道）
export const savePatrolSceneWithChannels = (data: PatrolSceneVO) => {
  return request.post({
    url: '/iot/patrol-scene/save-with-channels',
    data
  })
}
