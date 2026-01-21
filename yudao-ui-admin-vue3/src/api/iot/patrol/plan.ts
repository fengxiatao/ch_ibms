import request from '@/config/axios'

// 巡更计划VO
export interface PatrolPlanVO {
  id?: number
  name: string
  description?: string
  triggerType: number // 1-手动，2-定时
  cronExpression?: string
  startTime?: Date
  endTime?: Date
  routeId: number
  routeName?: string
  status: number // 1-启用，2-停用
  createTime?: Date
}

// 分页请求VO
export interface PatrolPlanPageReqVO extends PageParam {
  name?: string
  status?: number
  triggerType?: number
  createTime?: Date[]
}

// 获取巡更计划分页
export const getPatrolPlanPage = (params: PatrolPlanPageReqVO) => {
  return request.get<PageResult<PatrolPlanVO>>({
    url: '/iot/patrol-plan/page',
    params
  })
}

// 获取巡更计划详情
export const getPatrolPlan = (id: number) => {
  return request.get<PatrolPlanVO>({
    url: '/iot/patrol-plan/get',
    params: { id }
  })
}

// 创建巡更计划
export const createPatrolPlan = (data: PatrolPlanVO) => {
  return request.post({
    url: '/iot/patrol-plan/create',
    data
  })
}

// 更新巡更计划
export const updatePatrolPlan = (data: PatrolPlanVO) => {
  return request.put({
    url: '/iot/patrol-plan/update',
    data
  })
}

// 删除巡更计划
export const deletePatrolPlan = (id: number) => {
  return request.delete({
    url: '/iot/patrol-plan/delete',
    params: { id }
  })
}

// 更新计划状态
export const updatePatrolPlanStatus = (id: number, status: number) => {
  return request.put({
    url: '/iot/patrol-plan/status',
    params: { id, status }
  })
}

// 手动触发巡更计划
export const triggerPatrolPlan = (id: number) => {
  return request.post({
    url: '/iot/patrol-plan/trigger',
    params: { id }
  })
}


























