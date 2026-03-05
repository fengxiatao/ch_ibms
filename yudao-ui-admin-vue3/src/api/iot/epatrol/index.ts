import request from '@/config/axios'

// ===================== 巡更人员 =====================

export interface EpatrolPersonVO {
  id?: number
  name: string
  phone: string
  patrolStickNo?: string
  personCardNo?: string
  status?: number
  remark?: string
  createTime?: Date
}

export interface EpatrolPersonPageReqVO extends PageParam {
  name?: string
  phone?: string
  patrolStickNo?: string
  status?: number
}

// 创建巡更人员
export const createEpatrolPerson = (data: EpatrolPersonVO) => {
  return request.post({ url: '/iot/epatrol/person/create', data })
}

// 更新巡更人员
export const updateEpatrolPerson = (data: EpatrolPersonVO) => {
  return request.put({ url: '/iot/epatrol/person/update', data })
}

// 删除巡更人员
export const deleteEpatrolPerson = (id: number) => {
  return request.delete({ url: '/iot/epatrol/person/delete?id=' + id })
}

// 获得巡更人员
export const getEpatrolPerson = (id: number) => {
  return request.get({ url: '/iot/epatrol/person/get?id=' + id })
}

// 获得巡更人员分页
export const getEpatrolPersonPage = (params: EpatrolPersonPageReqVO) => {
  return request.get({ url: '/iot/epatrol/person/page', params })
}

// 获得所有启用的巡更人员
export const getEnabledEpatrolPersonList = () => {
  return request.get({ url: '/iot/epatrol/person/list-all-enabled' })
}

// 更新巡更人员状态
export const updateEpatrolPersonStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/epatrol/person/update-status?id=${id}&status=${status}` })
}

// ===================== 巡更点 =====================

export interface EpatrolPointVO {
  id?: number
  pointNo: string
  pointName: string
  pointLocation?: string
  status?: number
  remark?: string
  createTime?: Date
}

export interface EpatrolPointPageReqVO extends PageParam {
  pointNo?: string
  pointName?: string
  pointLocation?: string
  status?: number
}

// 创建巡更点
export const createEpatrolPoint = (data: EpatrolPointVO) => {
  return request.post({ url: '/iot/epatrol/point/create', data })
}

// 更新巡更点
export const updateEpatrolPoint = (data: EpatrolPointVO) => {
  return request.put({ url: '/iot/epatrol/point/update', data })
}

// 删除巡更点
export const deleteEpatrolPoint = (id: number) => {
  return request.delete({ url: '/iot/epatrol/point/delete?id=' + id })
}

// 获得巡更点
export const getEpatrolPoint = (id: number) => {
  return request.get({ url: '/iot/epatrol/point/get?id=' + id })
}

// 获得巡更点分页
export const getEpatrolPointPage = (params: EpatrolPointPageReqVO) => {
  return request.get({ url: '/iot/epatrol/point/page', params })
}

// 获得所有启用的巡更点
export const getEnabledEpatrolPointList = () => {
  return request.get({ url: '/iot/epatrol/point/list-all-enabled' })
}

// 更新巡更点状态
export const updateEpatrolPointStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/epatrol/point/update-status?id=${id}&status=${status}` })
}

// ===================== 巡更路线 =====================

export interface RoutePointItem {
  id?: number
  pointId: number
  pointNo?: string
  pointName?: string
  pointLocation?: string
  sort: number
  intervalMinutes: number
}

export interface EpatrolRouteVO {
  id?: number
  routeName: string
  pointCount?: number
  totalDuration?: number
  status?: number
  remark?: string
  createTime?: Date
  points?: RoutePointItem[]
}

export interface EpatrolRoutePageReqVO extends PageParam {
  routeName?: string
  pointName?: string
  status?: number
}

// 创建巡更路线
export const createEpatrolRoute = (data: EpatrolRouteVO) => {
  return request.post({ url: '/iot/epatrol/route/create', data })
}

// 更新巡更路线
export const updateEpatrolRoute = (data: EpatrolRouteVO) => {
  return request.put({ url: '/iot/epatrol/route/update', data })
}

// 删除巡更路线
export const deleteEpatrolRoute = (id: number) => {
  return request.delete({ url: '/iot/epatrol/route/delete?id=' + id })
}

// 获得巡更路线详情
export const getEpatrolRoute = (id: number) => {
  return request.get({ url: '/iot/epatrol/route/get?id=' + id })
}

// 获得巡更路线分页
export const getEpatrolRoutePage = (params: EpatrolRoutePageReqVO) => {
  return request.get({ url: '/iot/epatrol/route/page', params })
}

// 获得所有启用的巡更路线
export const getEnabledEpatrolRouteList = () => {
  return request.get({ url: '/iot/epatrol/route/list-all-enabled' })
}

// 更新巡更路线状态
export const updateEpatrolRouteStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/epatrol/route/update-status?id=${id}&status=${status}` })
}

// ===================== 巡更计划 =====================

export interface PlanPeriodItem {
  id?: number
  startTime: string
  durationMinutes: number
  routeId?: number
  routeName?: string
  personIds: number[]
  personNames?: string[]
  personName?: string
  endTime?: string
}

export interface EpatrolPlanVO {
  id?: number
  planCode?: string
  planName: string
  routeId?: number
  routeName?: string
  routeNames?: string[]
  startDate: string
  endDate?: string
  weekdays: number[]
  status?: number
  remark?: string
  createTime?: Date
  periods?: PlanPeriodItem[]
  personNames?: string[]
  timeRangeDesc?: string
}

export interface EpatrolPlanPageReqVO extends PageParam {
  planCode?: string
  planName?: string
  personId?: number
  personName?: string
  routeId?: number
  routeName?: string
  status?: number
}

// 创建巡更计划
export const createEpatrolPlan = (data: EpatrolPlanVO) => {
  return request.post({ url: '/iot/epatrol/plan/create', data })
}

// 更新巡更计划
export const updateEpatrolPlan = (data: EpatrolPlanVO) => {
  return request.put({ url: '/iot/epatrol/plan/update', data })
}

// 删除巡更计划
export const deleteEpatrolPlan = (id: number) => {
  return request.delete({ url: '/iot/epatrol/plan/delete?id=' + id })
}

// 获得巡更计划详情
export const getEpatrolPlan = (id: number) => {
  return request.get({ url: '/iot/epatrol/plan/get?id=' + id })
}

// 获得巡更计划分页
export const getEpatrolPlanPage = (params: EpatrolPlanPageReqVO) => {
  return request.get({ url: '/iot/epatrol/plan/page', params })
}

// 更新巡更计划状态
export const updateEpatrolPlanStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/epatrol/plan/update-status?id=${id}&status=${status}` })
}

// 手动生成每日任务
export const generateDailyTasks = () => {
  return request.post({ url: '/iot/epatrol/plan/generate-tasks' })
}

// ===================== 巡更任务 =====================

export interface TaskRecordItem {
  id?: number
  pointId?: number
  pointNo?: string
  pointName?: string
  personId?: number
  personName?: string
  expectedSort?: number
  actualSort?: number
  plannedTime?: Date
  actualTime?: Date
  patrolStatus?: number
  patrolStatusDesc?: string
  timeDiffSeconds?: number
}

export interface EpatrolTaskVO {
  id?: number
  taskCode?: string
  planId?: number
  planCode?: string
  planName?: string
  routeId?: number
  routeName?: string
  taskDate?: string
  plannedStartTime?: Date | string
  plannedEndTime?: Date | string
  plannedTimeDesc?: string
  personId?: number
  personName?: string
  personIds?: number[]
  personNames?: string[]
  status?: number
  submitTime?: Date
  remark?: string
  createTime?: Date
  records?: TaskRecordItem[]
}

export interface EpatrolTaskPageReqVO extends PageParam {
  taskCode?: string
  planId?: number
  personId?: number
  routeId?: number
  status?: number
  taskDate?: string
  taskDateStart?: string
  taskDateEnd?: string
}

export interface PatrolRecordItem {
  pointNo: string
  personCardNo?: string
  actualTime: string
  actualSort?: number
}

export interface EpatrolTaskSubmitReqVO {
  taskId: number
  records: PatrolRecordItem[]
  clearStickData?: boolean
}

// 获得巡更任务详情
export const getEpatrolTask = (id: number) => {
  return request.get({ url: '/iot/epatrol/task/get?id=' + id })
}

// 获得巡更任务分页
export const getEpatrolTaskPage = (params: EpatrolTaskPageReqVO) => {
  return request.get({ url: '/iot/epatrol/task/page', params })
}

// 提交巡更结果
export const submitEpatrolTask = (data: EpatrolTaskSubmitReqVO) => {
  return request.post({ url: '/iot/epatrol/task/submit', data })
}

// 人员统计
export interface PersonStatistics {
  personId: number
  personName: string
  total: number
  completed: number
  pending: number
  rate: number
  onTimeCount: number
  lateCount: number
  earlyCount: number
}

// 任务统计
export interface EpatrolTaskStatisticsVO {
  total: number
  completed: number
  pending: number
  rate: number
  todayTotal: number
  todayCompleted: number
  todayRate: number
  personStatistics: PersonStatistics[]
}

// 获取巡更任务统计
export const getEpatrolTaskStatistics = (params: EpatrolTaskPageReqVO) => {
  return request.get<EpatrolTaskStatisticsVO>({ url: '/iot/epatrol/task/statistics', params })
}

// ===================== 巡更棒数据 =====================

export interface PatrolStickRecordVO {
  pointNo: string
  pointName?: string
  personCardNo?: string
  personName?: string
  checkTime: string
}

export interface PatrolStickDataVO {
  stickNo: string
  records: PatrolStickRecordVO[]
}

// 读取巡更棒数据
export const readPatrolStickData = (stickNo: string) => {
  return request.get<PatrolStickDataVO>({ url: '/iot/epatrol/stick/read?stickNo=' + stickNo })
}

// 清空巡更棒数据
export const clearPatrolStickData = (stickNo: string) => {
  return request.post({ url: '/iot/epatrol/stick/clear?stickNo=' + stickNo })
}

// 提交巡更棒数据接口
export interface SubmitPatrolStickDataReqVO {
  stickNo: string
  submitDate: string
  records: PatrolRecordItem[]
}

// 提交巡更棒数据（独立提交，系统自动匹配任务）
export const submitPatrolStickData = (data: SubmitPatrolStickDataReqVO) => {
  return request.post({ url: '/iot/epatrol/stick/submit', data })
}
