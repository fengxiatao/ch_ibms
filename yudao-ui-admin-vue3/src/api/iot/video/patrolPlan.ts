import request from '@/config/axios'

export interface VideoPatrolPlanVO {
  id?: number
  name: string
  channelIds?: number[]
  channelNames?: string[]
  intervalSeconds?: number
  status?: number
  remark?: string
  createTime?: Date
}

export interface VideoPatrolPlanPageReqVO extends PageParam {
  name?: string
  status?: number
  createTime?: Date[]
}

// 分页查询轮巡计划
export const getVideoPatrolPlanPage = (params: VideoPatrolPlanPageReqVO) => {
  return request.get({ url: '/iot/patrol-plan/page', params })
}

// 获取轮巡计划列表（不分页，用于下拉选择）
export const getVideoPatrolPlanList = async () => {
  // 使用分页接口，pageSize最大100
  const res = await request.get({ 
    url: '/iot/patrol-plan/page', 
    params: { pageNo: 1, pageSize: 100 } 
  })
  return res.list || []
}

// 获取轮巡计划详情
export const getVideoPatrolPlan = (id: number) => {
  return request.get({ url: `/iot/patrol-plan/get?id=${id}` })
}

// 创建轮巡计划
export const createVideoPatrolPlan = (data: VideoPatrolPlanVO) => {
  return request.post({ url: '/iot/patrol-plan/create', data })
}

// 更新轮巡计划
export const updateVideoPatrolPlan = (data: VideoPatrolPlanVO) => {
  return request.put({ url: '/iot/patrol-plan/update', data })
}

// 删除轮巡计划
export const deleteVideoPatrolPlan = (id: number) => {
  return request.delete({ url: `/iot/patrol-plan/delete?id=${id}` })
}

// 启动轮巡计划
export const startVideoPatrolPlan = (id: number) => {
  return request.post({ url: `/iot/patrol-plan/start?id=${id}` })
}

// 暂停轮巡计划
export const pauseVideoPatrolPlan = (id: number) => {
  return request.post({ url: `/iot/patrol-plan/pause?id=${id}` })
}

// 停止轮巡计划
export const stopVideoPatrolPlan = (id: number) => {
  return request.post({ url: `/iot/patrol-plan/stop?id=${id}` })
}

// 分页查询轮巡计划（别名）
export const getPatrolPlanPage = getVideoPatrolPlanPage

// 获取轮巡计划详情（别名）
export const getPatrolPlan = getVideoPatrolPlan

// 获取轮巡任务列表
export const getPatrolTaskList = (planId: number) => {
  return request.get({ url: `/iot/patrol-task/list?planId=${planId}` })
}

// 根据任务ID获取轮巡场景
export const getPatrolSceneByTaskId = (taskId: number) => {
  return request.get({ url: `/iot/patrol-scene/get-by-task?taskId=${taskId}` })
}
