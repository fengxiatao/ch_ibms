import request from '@/config/axios'

// ======================= 暖通设备相关接口 =======================

export interface IbmsHvacDeviceVO {
  id?: number
  deviceCode?: string
  deviceName?: string
  deviceType?: number
  model?: string
  brand?: string
  areaId?: number
  areaName?: string
  installLocation?: string
  ratedPower?: number
  currentPower?: number
  status?: number
  runningStatus?: number
  runMode?: number
  setTemperature?: number
  returnTemperature?: number
  supplyTemperature?: number
  fanSpeed?: number
  valveOpening?: number
  runningHours?: number
  lastCommunicateTime?: Date
  createTime?: Date
}

export interface IbmsHvacDevicePageReqVO extends PageParam {
  deviceCode?: string
  deviceName?: string
  deviceType?: number
  areaId?: number
  status?: number
  runningStatus?: number
}

// 获得暖通设备分页
export const getHvacDevicePage = (params: IbmsHvacDevicePageReqVO) => {
  return request.get({ url: '/iot/building/bac/hvac/page', params })
}

// 获得暖通设备详情
export const getHvacDevice = (id: number) => {
  return request.get({ url: '/iot/building/bac/hvac/get?id=' + id })
}

// 控制暖通设备启停
export const controlHvacDevice = (id: number, runningStatus: number, operator: string) => {
  return request.put({ url: '/iot/building/bac/hvac/control', params: { id, runningStatus, operator } })
}

// 设置暖通设备参数
export const setHvacDeviceParams = (id: number, params: { runMode?: number, setTemperature?: number, fanSpeed?: number, operator: string }) => {
  return request.put({ url: '/iot/building/bac/hvac/set-params', params: { id, ...params } })
}

// ======================= 给排水设备相关接口 =======================

export interface IbmsWaterDeviceVO {
  id?: number
  deviceCode?: string
  deviceName?: string
  deviceType?: number
  model?: string
  brand?: string
  areaId?: number
  areaName?: string
  installLocation?: string
  ratedPower?: number
  currentPower?: number
  status?: number
  runningStatus?: number
  waterLevel?: number
  waterLevelMax?: number
  waterLevelMin?: number
  pressure?: number
  flowRate?: number
  runningHours?: number
  lastCommunicateTime?: Date
  createTime?: Date
}

export interface IbmsWaterDevicePageReqVO extends PageParam {
  deviceCode?: string
  deviceName?: string
  deviceType?: number
  areaId?: number
  status?: number
  runningStatus?: number
}

// 获得给排水设备分页
export const getWaterDevicePage = (params: IbmsWaterDevicePageReqVO) => {
  return request.get({ url: '/iot/building/bac/water/page', params })
}

// 获得给排水设备详情
export const getWaterDevice = (id: number) => {
  return request.get({ url: '/iot/building/bac/water/get?id=' + id })
}

// 控制给排水设备启停
export const controlWaterDevice = (id: number, runningStatus: number, operator: string) => {
  return request.put({ url: '/iot/building/bac/water/control', params: { id, runningStatus, operator } })
}

// ======================= 告警相关接口 =======================

export interface IbmsBacAlarmVO {
  id?: number
  deviceType?: number
  deviceId?: number
  deviceCode?: string
  deviceName?: string
  alarmType?: number
  alarmLevel?: number
  alarmContent?: string
  alarmTime?: Date
  status?: number
  handleTime?: Date
  handler?: string
  handleRemark?: string
}

export interface IbmsBacAlarmPageReqVO extends PageParam {
  deviceType?: number
  deviceName?: string
  alarmType?: number
  alarmLevel?: number
  status?: number
  startTime?: Date
  endTime?: Date
}

// 获得楼宇自控告警分页
export const getAlarmPage = (params: IbmsBacAlarmPageReqVO) => {
  return request.get({ url: '/iot/building/bac/alarm/page', params })
}

// 处理告警
export const handleAlarm = (id: number, handler: string, handleRemark?: string) => {
  return request.put({ url: '/iot/building/bac/alarm/handle', params: { id, handler, handleRemark } })
}

// ======================= 系统日志相关接口 =======================

export interface IbmsBacSystemLogVO {
  id?: number
  logType?: number
  logLevel?: number
  module?: string
  content?: string
  operator?: string
  ipAddress?: string
  logTime?: Date
}

export interface IbmsBacSystemLogPageReqVO extends PageParam {
  logType?: number
  logLevel?: number
  module?: string
  operator?: string
  startTime?: Date
  endTime?: Date
}

// 获得系统日志分页
export const getSystemLogPage = (params: IbmsBacSystemLogPageReqVO) => {
  return request.get({ url: '/iot/building/bac/system-log/page', params })
}

// ======================= 统计相关接口 =======================

export interface IbmsBacStatisticsVO {
  hvacTotalCount?: number
  hvacOnlineCount?: number
  hvacRunningCount?: number
  hvacFaultCount?: number
  airConditionerCount?: number
  freshAirCount?: number
  supplyFanCount?: number
  exhaustFanCount?: number
  waterTotalCount?: number
  waterOnlineCount?: number
  waterRunningCount?: number
  waterFaultCount?: number
  domesticPumpCount?: number
  firePumpCount?: number
  sewagePumpCount?: number
  waterTankCount?: number
  todayAlarmCount?: number
  unhandledAlarmCount?: number
  urgentAlarmCount?: number
  hvacCurrentPower?: number
  waterCurrentPower?: number
}

// 获得楼宇自控统计数据
export const getStatistics = () => {
  return request.get<IbmsBacStatisticsVO>({ url: '/iot/building/bac/statistics' })
}
