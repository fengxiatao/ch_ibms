import request from '@/config/axios'

// 巡更任务VO
export interface PatrolTaskVO {
  id?: number
  planId?: number
  planName?: string
  routeId?: number
  routeName?: string
  startTime?: Date
  endTime?: Date
  actualStartTime?: Date
  actualEndTime?: Date
  status: number // 1-待执行，2-执行中，3-已完成，4-已逾期，5-已取消
  executorId?: number
  executorName?: string
  timeoutDuration?: number
  remark?: string
  createTime?: Date
}

// 分页请求VO
export interface PatrolTaskPageReqVO extends PageParam {
  planId?: number
  routeId?: number
  status?: number
  executorId?: number
  createTime?: Date[]
}

// 获取巡更任务分页
export const getPatrolTaskPage = (params: PatrolTaskPageReqVO) => {
  return request.get<PageResult<PatrolTaskVO>>({
    url: '/iot/patrol-task/page',
    params
  })
}

// 获取巡更任务详情
export const getPatrolTask = (id: number) => {
  return request.get<PatrolTaskVO>({
    url: '/iot/patrol-task/get',
    params: { id }
  })
}

// 开始任务
export const startPatrolTask = (id: number) => {
  return request.put({
    url: '/iot/patrol-task/start',
    params: { id }
  })
}

// 完成任务
export const completePatrolTask = (id: number) => {
  return request.put({
    url: '/iot/patrol-task/complete',
    params: { id }
  })
}


























