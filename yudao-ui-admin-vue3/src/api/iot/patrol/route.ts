import request from '@/config/axios'

// 巡更线路点位VO
export interface PatrolRoutePointVO {
  pointId: number
  pointName?: string
  pointOrder: number
  expectedDuration?: number
}

// 巡更线路VO
export interface PatrolRouteVO {
  id?: number
  name: string
  description?: string
  rule: number // 1-顺序，2-无序
  duration?: number
  pointCount?: number
  points?: PatrolRoutePointVO[]
  createTime?: Date
}

// 获取巡更线路列表
export const getPatrolRouteList = () => {
  return request.get<PatrolRouteVO[]>({
    url: '/iot/patrol-route/list'
  })
}

// 获取巡更线路详情
export const getPatrolRoute = (id: number) => {
  return request.get<PatrolRouteVO>({
    url: '/iot/patrol-route/get',
    params: { id }
  })
}

// 创建巡更线路
export const createPatrolRoute = (data: PatrolRouteVO) => {
  return request.post({
    url: '/iot/patrol-route/create',
    data
  })
}

// 更新巡更线路
export const updatePatrolRoute = (data: PatrolRouteVO) => {
  return request.put({
    url: '/iot/patrol-route/update',
    data
  })
}

// 删除巡更线路
export const deletePatrolRoute = (id: number) => {
  return request.delete({
    url: '/iot/patrol-route/delete',
    params: { id }
  })
}


























