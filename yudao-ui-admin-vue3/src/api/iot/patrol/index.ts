import request from '@/config/axios'

// ===================== 巡更计划 =====================

export interface PatrolPlanVO {
  id?: number
  planName: string
  planCode: string
  planType: number
  routeId: number
  patrolMode?: number
  scheduleType: number
  scheduleConfig?: any
  timeSlots: any[]
  patrolUserIds: number[]
  startDate: string
  endDate?: string
  allowEarlyCheckin?: boolean
  earlyCheckinMinutes?: number
  allowLateCheckin?: boolean
  lateCheckinMinutes?: number
  missAlert?: boolean
  timeoutAlert?: boolean
  timeoutMinutes?: number
  description?: string
  status?: number
}

export interface PatrolPlanPageReqVO extends PageParam {
  planName?: string
  planCode?: string
  planType?: number
  routeId?: number
  scheduleType?: number
  status?: number
  startDate?: string[]
  endDate?: string[]
}

// 创建巡更计划
export const createPatrolPlan = (data: PatrolPlanVO) => {
  return request.post({ url: '/iot/patrol/plan/create', data })
}

// 更新巡更计划
export const updatePatrolPlan = (data: PatrolPlanVO) => {
  return request.put({ url: '/iot/patrol/plan/update', data })
}

// 删除巡更计划
export const deletePatrolPlan = (id: number) => {
  return request.delete({ url: '/iot/patrol/plan/delete?id=' + id })
}

// 获得巡更计划
export const getPatrolPlan = (id: number) => {
  return request.get({ url: '/iot/patrol/plan/get?id=' + id })
}

// 获得巡更计划分页
export const getPatrolPlanPage = (params: PatrolPlanPageReqVO) => {
  return request.get({ url: '/iot/patrol/plan/page', params })
}

// 更新巡更计划状态
export const updatePatrolPlanStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/patrol/plan/update-status?id=${id}&status=${status}` })
}

// ===================== 巡更记录 =====================

export interface PatrolRecordPageReqVO extends PageParam {
  planId?: number
  routeId?: number
  pointId?: number
  userId?: number
  checkinType?: number
  isAbnormal?: boolean
  startTime?: string
  endTime?: string
}

// 获得巡更记录
export const getPatrolRecord = (id: number) => {
  return request.get({ url: '/iot/patrol/record/get?id=' + id })
}

// 获得巡更记录分页
export const getPatrolRecordPage = (params: PatrolRecordPageReqVO) => {
  return request.get({ url: '/iot/patrol/record/page', params })
}

// ===================== 巡更点位 =====================

export interface PatrolPointVO {
  id?: number
  pointName: string
  pointCode: string
  pointType: number
  buildingId?: number
  floorId?: number
  areaId?: number
  location?: string
  longitude?: string
  latitude?: string
  description?: string
  status?: number
  sort?: number
}

export interface PatrolPointPageReqVO extends PageParam {
  pointName?: string
  pointCode?: string
  pointType?: number
  buildingId?: number
  floorId?: number
  areaId?: number
  status?: number
}

// 创建巡更点位
export const createPatrolPoint = (data: PatrolPointVO) => {
  return request.post({ url: '/iot/patrol/point/create', data })
}

// 更新巡更点位
export const updatePatrolPoint = (data: PatrolPointVO) => {
  return request.put({ url: '/iot/patrol/point/update', data })
}

// 删除巡更点位
export const deletePatrolPoint = (id: number) => {
  return request.delete({ url: '/iot/patrol/point/delete?id=' + id })
}

// 获得巡更点位
export const getPatrolPoint = (id: number) => {
  return request.get({ url: '/iot/patrol/point/get?id=' + id })
}

// 获得巡更点位分页
export const getPatrolPointPage = (params: PatrolPointPageReqVO) => {
  return request.get({ url: '/iot/patrol/point/page', params })
}

// 获得所有启用的巡更点位
export const getAllEnabledPatrolPoints = () => {
  return request.get({ url: '/iot/patrol/point/list-all-enabled' })
}

// 更新巡更点位状态
export const updatePatrolPointStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/patrol/point/update-status?id=${id}&status=${status}` })
}

// ===================== 巡更路线 =====================

export interface PatrolRouteVO {
  id?: number
  routeName: string
  routeCode: string
  pointIds: number[]
  pointCount?: number
  estimatedDuration?: number
  description?: string
  status?: number
}

export interface PatrolRoutePageReqVO extends PageParam {
  routeName?: string
  routeCode?: string
  status?: number
}

// 创建巡更路线
export const createPatrolRoute = (data: PatrolRouteVO) => {
  return request.post({ url: '/iot/patrol/route/create', data })
}

// 更新巡更路线
export const updatePatrolRoute = (data: PatrolRouteVO) => {
  return request.put({ url: '/iot/patrol/route/update', data })
}

// 删除巡更路线
export const deletePatrolRoute = (id: number) => {
  return request.delete({ url: '/iot/patrol/route/delete?id=' + id })
}

// 获得巡更路线
export const getPatrolRoute = (id: number) => {
  return request.get({ url: '/iot/patrol/route/get?id=' + id })
}

// 获得巡更路线分页
export const getPatrolRoutePage = (params: PatrolRoutePageReqVO) => {
  return request.get({ url: '/iot/patrol/route/page', params })
}

// 获得所有启用的巡更路线
export const getAllEnabledPatrolRoutes = () => {
  return request.get({ url: '/iot/patrol/route/list-all-enabled' })
}

// 更新巡更路线状态
export const updatePatrolRouteStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/patrol/route/update-status?id=${id}&status=${status}` })
}

