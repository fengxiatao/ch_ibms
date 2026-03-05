import request from '@/config/axios'

// ======================= 回路相关接口 =======================

export interface IbmsLightingCircuitVO {
  id?: number
  circuitCode?: string
  circuitName?: string
  circuitType?: number
  areaId?: number
  areaName?: string
  controllerId?: number
  controllerName?: string
  ratedPower?: number
  currentPower?: number
  brightness?: number
  lightCount?: number
  status?: number
  lastOperateTime?: Date
  createTime?: Date
}

export interface IbmsLightingCircuitPageReqVO extends PageParam {
  circuitCode?: string
  circuitName?: string
  circuitType?: number
  areaId?: number
  status?: number
}

// 获得照明回路分页
export const getCircuitPage = (params: IbmsLightingCircuitPageReqVO) => {
  return request.get({ url: '/iot/building/lighting/circuit/page', params })
}

// 获得照明回路详情
export const getCircuit = (id: number) => {
  return request.get({ url: '/iot/building/lighting/circuit/get?id=' + id })
}

// 控制回路开关
export const controlCircuit = (id: number, status: number, operator: string) => {
  return request.put({ url: '/iot/building/lighting/circuit/control', params: { id, status, operator } })
}

// 调节回路亮度
export const dimCircuit = (id: number, brightness: number, operator: string) => {
  return request.put({ url: '/iot/building/lighting/circuit/dim', params: { id, brightness, operator } })
}

// ======================= 场景相关接口 =======================

export interface IbmsLightingSceneVO {
  id?: number
  sceneCode?: string
  sceneName?: string
  sceneIcon?: string
  areaId?: number
  areaName?: string
  circuitConfig?: string
  description?: string
  sort?: number
  createTime?: Date
}

export interface IbmsLightingScenePageReqVO extends PageParam {
  sceneCode?: string
  sceneName?: string
  areaId?: number
}

// 获得照明场景分页
export const getScenePage = (params: IbmsLightingScenePageReqVO) => {
  return request.get({ url: '/iot/building/lighting/scene/page', params })
}

// 获得照明场景列表
export const getSceneSimpleList = () => {
  return request.get({ url: '/iot/building/lighting/scene/list' })
}

// 执行场景
export const executeScene = (id: number, operator: string) => {
  return request.post({ url: '/iot/building/lighting/scene/execute', params: { id, operator } })
}

// ======================= 定时任务相关接口 =======================

export interface IbmsLightingScheduleVO {
  id?: number
  scheduleName?: string
  sceneId?: number
  sceneName?: string
  executeType?: number
  executeTime?: string
  customCycle?: string
  enabled?: boolean
  lastExecuteTime?: Date
  createTime?: Date
}

export interface IbmsLightingSchedulePageReqVO extends PageParam {
  scheduleName?: string
  sceneId?: number
  enabled?: boolean
}

// 获得定时任务分页
export const getSchedulePage = (params: IbmsLightingSchedulePageReqVO) => {
  return request.get({ url: '/iot/building/lighting/schedule/page', params })
}

// 启用/禁用定时任务
export const updateScheduleEnabled = (id: number, enabled: boolean) => {
  return request.put({ url: '/iot/building/lighting/schedule/enable', params: { id, enabled } })
}

// ======================= 设备相关接口 =======================

export interface IbmsLightingDevicePageReqVO extends PageParam {
  deviceCode?: string
  deviceName?: string
  status?: number
}

export interface IbmsLightingGatewayVO {
  id?: number
  gatewayCode?: string
  gatewayName?: string
  model?: string
  brand?: string
  ipAddress?: string
  port?: number
  protocolType?: string
  installLocation?: string
  status?: number
  lastCommunicateTime?: Date
  createTime?: Date
}

export interface IbmsLightingControllerVO {
  id?: number
  controllerCode?: string
  controllerName?: string
  model?: string
  brand?: string
  gatewayId?: number
  gatewayName?: string
  channelCount?: number
  dimmable?: boolean
  installLocation?: string
  status?: number
  lastCommunicateTime?: Date
  createTime?: Date
}

// 获得照明网关分页
export const getGatewayPage = (params: IbmsLightingDevicePageReqVO) => {
  return request.get({ url: '/iot/building/lighting/gateway/page', params })
}

// 获得照明控制器分页
export const getControllerPage = (params: IbmsLightingDevicePageReqVO) => {
  return request.get({ url: '/iot/building/lighting/controller/page', params })
}

// ======================= 操作日志相关接口 =======================

export interface IbmsLightingOperationLogVO {
  id?: number
  operationType?: number
  targetType?: number
  targetId?: number
  targetName?: string
  beforeStatus?: string
  afterStatus?: string
  operator?: string
  operateMethod?: number
  operateTime?: Date
  remark?: string
}

export interface IbmsLightingOperationLogPageReqVO extends PageParam {
  operationType?: number
  targetType?: number
  targetName?: string
  operator?: string
  startTime?: Date
  endTime?: Date
}

// 获得操作日志分页
export const getOperationLogPage = (params: IbmsLightingOperationLogPageReqVO) => {
  return request.get({ url: '/iot/building/lighting/operation-log/page', params })
}

// ======================= 告警相关接口 =======================

export interface IbmsLightingAlarmVO {
  id?: number
  deviceType?: number
  deviceId?: number
  deviceName?: string
  alarmType?: string
  alarmLevel?: number
  alarmContent?: string
  alarmTime?: Date
  status?: number
  handleTime?: Date
  handler?: string
  handleRemark?: string
}

export interface IbmsLightingAlarmPageReqVO extends PageParam {
  deviceType?: number
  deviceName?: string
  alarmLevel?: number
  status?: number
  startTime?: Date
  endTime?: Date
}

// 获得照明告警分页
export const getAlarmPage = (params: IbmsLightingAlarmPageReqVO) => {
  return request.get({ url: '/iot/building/lighting/alarm/page', params })
}

// 处理告警
export const handleAlarm = (id: number, handler: string, handleRemark?: string) => {
  return request.put({ url: '/iot/building/lighting/alarm/handle', params: { id, handler, handleRemark } })
}

// ======================= 统计相关接口 =======================

export interface IbmsLightingStatisticsVO {
  circuitTotalCount?: number
  circuitOnCount?: number
  circuitOffCount?: number
  circuitFaultCount?: number
  sceneTotalCount?: number
  gatewayTotalCount?: number
  gatewayOnlineCount?: number
  controllerTotalCount?: number
  controllerOnlineCount?: number
  lightTotalCount?: number
  totalPower?: number
  currentPower?: number
  todayAlarmCount?: number
  unhandledAlarmCount?: number
}

// 获得智能照明统计数据
export const getStatistics = () => {
  return request.get<IbmsLightingStatisticsVO>({ url: '/iot/building/lighting/statistics' })
}
