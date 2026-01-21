import request from '@/config/axios'

export interface ParkingLotVO {
  id: number
  lotCode: string
  lotName: string
  lotType: string // outdoor, indoor, underground
  totalSpaces: number
  occupiedSpaces: number
  availableSpaces: number
  location: string
  description: string
  hourlyRate: number
  dailyRate: number
  monthlyRate: number
  status: string // active, inactive, maintenance
  features: string[] // camera, sensor, charging
  createTime: Date
  updateTime: Date
}

export interface ParkingSpaceVO {
  id: number
  spaceCode: string
  lotId: number
  lotName: string
  spaceType: string // normal, vip, disabled, electric
  status: string // vacant, occupied, reserved, maintenance
  currentVehicle?: string
  occupiedTime?: Date
  reservedBy?: string
  reservedTime?: Date
  location: string
  remark: string
  createTime: Date
  updateTime: Date
}

export interface ParkingRecordVO {
  id: number
  plateNumber: string
  vehicleId?: number
  ownerName?: string
  lotId: number
  lotName: string
  spaceId: number
  spaceCode: string
  entryTime: Date
  exitTime?: Date
  duration?: number // minutes
  fee?: number
  paymentStatus: string // unpaid, paid, free
  paymentTime?: Date
  createTime: Date
}

export interface ParkingLotPageReqVO extends PageParam {
  lotName?: string
  lotType?: string
  status?: string
}

export interface ParkingSpacePageReqVO extends PageParam {
  spaceCode?: string
  lotId?: number
  spaceType?: string
  status?: string
}

export interface ParkingRecordPageReqVO extends PageParam {
  plateNumber?: string
  lotId?: number
  paymentStatus?: string
  entryTime?: Date[]
}

// 停车场管理
export const getParkingLotPage = (params: ParkingLotPageReqVO) => {
  return request.get({ url: '/access/parking/lot/page', params })
}

export const getParkingLot = (id: number) => {
  return request.get({ url: '/access/parking/lot/get?id=' + id })
}

export const createParkingLot = (data: Partial<ParkingLotVO>) => {
  return request.post({ url: '/access/parking/lot/create', data })
}

export const updateParkingLot = (data: Partial<ParkingLotVO>) => {
  return request.put({ url: '/access/parking/lot/update', data })
}

export const deleteParkingLot = (id: number) => {
  return request.delete({ url: '/access/parking/lot/delete?id=' + id })
}

// 车位管理
export const getParkingSpacePage = (params: ParkingSpacePageReqVO) => {
  return request.get({ url: '/access/parking/space/page', params })
}

export const getParkingSpace = (id: number) => {
  return request.get({ url: '/access/parking/space/get?id=' + id })
}

export const createParkingSpace = (data: Partial<ParkingSpaceVO>) => {
  return request.post({ url: '/access/parking/space/create', data })
}

export const updateParkingSpace = (data: Partial<ParkingSpaceVO>) => {
  return request.put({ url: '/access/parking/space/update', data })
}

export const deleteParkingSpace = (id: number) => {
  return request.delete({ url: '/access/parking/space/delete?id=' + id })
}

// 批量创建车位
export const batchCreateParkingSpaces = (data: { lotId: number, startCode: string, endCode: string, spaceType: string }) => {
  return request.post({ url: '/access/parking/space/batch-create', data })
}

// 停车记录管理
export const getParkingRecordPage = (params: ParkingRecordPageReqVO) => {
  return request.get({ url: '/access/parking/record/page', params })
}

export const getParkingRecord = (id: number) => {
  return request.get({ url: '/access/parking/record/get?id=' + id })
}

// 车辆入场
export const vehicleEntry = (data: { plateNumber: string, lotId: number, spaceId?: number }) => {
  return request.post({ url: '/access/parking/record/entry', data })
}

// 车辆出场
export const vehicleExit = (data: { plateNumber: string, exitTime?: Date }) => {
  return request.post({ url: '/access/parking/record/exit', data })
}

// 缴费
export const payParkingFee = (id: number, data: { paymentMethod: string, amount: number }) => {
  return request.post({ url: '/access/parking/record/pay?id=' + id, data })
}

// 获取停车统计数据
export const getParkingStatistics = (params: { startTime: Date, endTime: Date, lotId?: number }) => {
  return request.get({ url: '/access/parking/statistics', params })
}

// 获取实时停车数据
export const getRealTimeParkingData = () => {
  return request.get({ url: '/access/parking/real-time' })
}

// 导出停车记录
export const exportParkingRecord = (params: ParkingRecordPageReqVO) => {
  return request.download({ url: '/access/parking/record/export-excel', params })
}











