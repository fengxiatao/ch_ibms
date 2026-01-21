import request from '@/config/axios'

export interface ElevatorVO {
  id: number
  elevatorCode: string
  elevatorName: string
  building: string
  floors: number[]
  maxLoad: number // kg
  maxPersons: number
  status: string // running, stopped, maintenance, fault
  brand: string
  model: string
  installDate: Date
  lastMaintenance: Date
  nextMaintenance: Date
  currentFloor: number
  direction: string // up, down, idle
  remark: string
  createTime: Date
  updateTime: Date
}

export interface ElevatorDispatchRecordVO {
  id: number
  elevatorId: number
  elevatorName: string
  userId: number
  userName: string
  userCard: string
  fromFloor: number
  toFloor: number
  dispatchTime: Date
  arrivalTime?: Date
  status: string // pending, completed, cancelled, failed
  accessMethod: string // card, face, qrcode
  createTime: Date
}

export interface ElevatorAlarmRecordVO {
  id: number
  elevatorId: number
  elevatorName: string
  alarmType: string // overload, door_fault, power_fault, emergency
  alarmLevel: string // low, medium, high, critical
  alarmMessage: string
  alarmTime: Date
  handleTime?: Date
  handleBy?: string
  handleResult?: string
  status: string // pending, handling, resolved, ignored
  createTime: Date
}

export interface ElevatorPageReqVO extends PageParam {
  elevatorName?: string
  building?: string
  status?: string
}

export interface ElevatorDispatchRecordPageReqVO extends PageParam {
  elevatorId?: number
  userName?: string
  status?: string
  dispatchTime?: Date[]
}

export interface ElevatorAlarmRecordPageReqVO extends PageParam {
  elevatorId?: number
  alarmType?: string
  alarmLevel?: string
  status?: string
  alarmTime?: Date[]
}

// 电梯管理
export const getElevatorPage = (params: ElevatorPageReqVO) => {
  return request.get({ url: '/access/elevator/page', params })
}

export const getElevator = (id: number) => {
  return request.get({ url: '/access/elevator/get?id=' + id })
}

export const createElevator = (data: Partial<ElevatorVO>) => {
  return request.post({ url: '/access/elevator/create', data })
}

export const updateElevator = (data: Partial<ElevatorVO>) => {
  return request.put({ url: '/access/elevator/update', data })
}

export const deleteElevator = (id: number) => {
  return request.delete({ url: '/access/elevator/delete?id=' + id })
}

// 电梯控制
export const callElevator = (data: { elevatorId: number, fromFloor: number, toFloor: number, userId: number }) => {
  return request.post({ url: '/access/elevator/call', data })
}

export const stopElevator = (id: number) => {
  return request.post({ url: '/access/elevator/stop?id=' + id })
}

export const startElevator = (id: number) => {
  return request.post({ url: '/access/elevator/start?id=' + id })
}

export const setMaintenanceMode = (id: number, maintenance: boolean) => {
  return request.post({ url: '/access/elevator/maintenance', data: { id, maintenance } })
}

// 获取电梯实时状态
export const getElevatorStatus = (id: number) => {
  return request.get({ url: '/access/elevator/status?id=' + id })
}

// 电梯调度记录
export const getElevatorDispatchRecordPage = (params: ElevatorDispatchRecordPageReqVO) => {
  return request.get({ url: '/access/elevator/dispatch-record/page', params })
}

export const getElevatorDispatchRecord = (id: number) => {
  return request.get({ url: '/access/elevator/dispatch-record/get?id=' + id })
}

// 电梯报警记录
export const getElevatorAlarmRecordPage = (params: ElevatorAlarmRecordPageReqVO) => {
  return request.get({ url: '/access/elevator/alarm-record/page', params })
}

export const getElevatorAlarmRecord = (id: number) => {
  return request.get({ url: '/access/elevator/alarm-record/get?id=' + id })
}

export const handleElevatorAlarm = (data: { id: number, handleResult: string, handleBy: string }) => {
  return request.post({ url: '/access/elevator/alarm-record/handle', data })
}

// 获取电梯统计数据
export const getElevatorStatistics = (params: { startTime: Date, endTime: Date, elevatorId?: number }) => {
  return request.get({ url: '/access/elevator/statistics', params })
}

// 导出调度记录
export const exportElevatorDispatchRecord = (params: ElevatorDispatchRecordPageReqVO) => {
  return request.download({ url: '/access/elevator/dispatch-record/export-excel', params })
}

// 导出报警记录
export const exportElevatorAlarmRecord = (params: ElevatorAlarmRecordPageReqVO) => {
  return request.download({ url: '/access/elevator/alarm-record/export-excel', params })
}











