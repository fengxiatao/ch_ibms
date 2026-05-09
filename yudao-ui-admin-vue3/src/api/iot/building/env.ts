import request from '@/config/axios'

// ======================= 传感器相关接口 =======================

export interface IbmsEnvSensorVO {
  id?: number
  sensorCode: string
  sensorName: string
  sensorType: number
  model?: string
  brand?: string
  areaId?: number
  areaName?: string
  installLocation?: string
  installTime?: Date
  collectInterval?: number
  status?: number
  temperature?: number
  humidity?: number
  pm25?: number
  co2?: number
  noise?: number
  illumination?: number
  pressure?: number
  lastCommunicateTime?: Date
  createTime?: Date
}

export interface IbmsEnvSensorPageReqVO extends PageParam {
  sensorCode?: string
  sensorName?: string
  sensorType?: number
  areaId?: number
  status?: number
}

// 获得环境传感器分页
export const getEnvSensorPage = (params: IbmsEnvSensorPageReqVO) => {
  return request.get({ url: '/iot/building/env/sensor/page', params })
}

// 获得环境传感器列表
export const getEnvSensorList = (params: IbmsEnvSensorPageReqVO) => {
  return request.get({ url: '/iot/building/env/sensor/list', params })
}

// 获得环境传感器详情
export const getEnvSensor = (id: number) => {
  return request.get({ url: '/iot/building/env/sensor/get?id=' + id })
}

// 创建环境传感器
export const createEnvSensor = (data: IbmsEnvSensorVO) => {
  return request.post({ url: '/iot/building/env/sensor/create', data })
}

// 更新环境传感器
export const updateEnvSensor = (data: IbmsEnvSensorVO) => {
  return request.put({ url: '/iot/building/env/sensor/update', data })
}

// 删除环境传感器
export const deleteEnvSensor = (id: number) => {
  return request.delete({ url: '/iot/building/env/sensor/delete?id=' + id })
}

// ======================= 数据记录相关接口 =======================

export interface IbmsEnvDataRecordVO {
  id?: number
  sensorId?: number
  sensorCode?: string
  sensorName?: string
  temperature?: number
  humidity?: number
  pm25?: number
  co2?: number
  noise?: number
  illumination?: number
  pressure?: number
  collectTime?: Date
}

export interface IbmsEnvDataRecordPageReqVO extends PageParam {
  sensorId?: number
  sensorCode?: string
  startTime?: Date
  endTime?: Date
}

// 获得环境数据记录分页
export const getEnvDataRecordPage = (params: IbmsEnvDataRecordPageReqVO) => {
  return request.get({ url: '/iot/building/env/data/page', params })
}

// 获得传感器最新数据
export const getLatestEnvDataRecord = (sensorId: number) => {
  return request.get({ url: '/iot/building/env/data/latest?sensorId=' + sensorId })
}

// 获得传感器历史数据
export const getEnvDataRecordHistory = (sensorId: number, limit: number = 100) => {
  return request.get({ url: '/iot/building/env/data/history', params: { sensorId, limit } })
}

// ======================= 告警相关接口 =======================

export interface IbmsEnvAlarmVO {
  id?: number
  sensorId?: number
  sensorCode?: string
  sensorName?: string
  alarmType?: number
  alarmLevel?: number
  alarmContent?: string
  alarmValue?: string
  thresholdValue?: string
  alarmTime?: Date
  status?: number
  handleTime?: Date
  handler?: string
  handleRemark?: string
  areaName?: string
}

export interface IbmsEnvAlarmPageReqVO extends PageParam {
  sensorId?: number
  sensorName?: string
  alarmType?: number
  alarmLevel?: number
  status?: number
  startTime?: Date
  endTime?: Date
}

// 获得环境告警分页
export const getEnvAlarmPage = (params: IbmsEnvAlarmPageReqVO) => {
  return request.get({ url: '/iot/building/env/alarm/page', params })
}

// 处理环境告警
export const handleEnvAlarm = (id: number, handler: string, handleRemark?: string) => {
  return request.put({ url: '/iot/building/env/alarm/handle', params: { id, handler, handleRemark } })
}

// 忽略环境告警
export const ignoreEnvAlarm = (id: number, handler: string, handleRemark?: string) => {
  return request.put({ url: '/iot/building/env/alarm/ignore', params: { id, handler, handleRemark } })
}

// ======================= 统计相关接口 =======================

export interface IbmsEnvStatisticsVO {
  totalCount?: number
  onlineCount?: number
  offlineCount?: number
  faultCount?: number
  tempHumidityCount?: number
  pm25Count?: number
  co2Count?: number
  noiseCount?: number
  illuminationCount?: number
  pressureCount?: number
  todayAlarmCount?: number
  unhandledAlarmCount?: number
  outdoorTemperature?: number
  outdoorHumidity?: number
  outdoorPm25?: number
  windDirection?: string
  windSpeed?: number
}

// 获得环境监测统计数据
export const getEnvStatistics = () => {
  return request.get<IbmsEnvStatisticsVO>({ url: '/iot/building/env/statistics' })
}
