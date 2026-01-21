import request from '@/config/axios'

export interface VehicleVO {
  id: number
  plateNumber: string
  vehicleType: string // car, truck, motorcycle, bicycle
  vehicleColor: string
  vehicleBrand: string
  vehicleModel: string
  ownerId: number
  ownerName: string
  ownerPhone: string
  ownerType: string // employee, visitor, resident
  status: string // active, inactive, blacklist
  registrationTime: Date
  expiryTime?: Date
  parkingPermission: string[] // area codes
  remark: string
  createTime: Date
  updateTime: Date
}

export interface VehiclePageReqVO extends PageParam {
  plateNumber?: string
  ownerName?: string
  vehicleType?: string
  status?: string
  ownerType?: string
}

export interface VehicleSaveReqVO {
  id?: number
  plateNumber: string
  vehicleType: string
  vehicleColor: string
  vehicleBrand: string
  vehicleModel: string
  ownerId: number
  ownerType: string
  expiryTime?: Date
  parkingPermission: string[]
  remark?: string
}

export interface VehicleAccessRecordVO {
  id: number
  plateNumber: string
  vehicleId?: number
  ownerName?: string
  accessType: string // entry, exit
  accessTime: Date
  accessGate: string
  accessResult: string // success, failed, denied
  vehiclePhoto?: string
  platePhoto?: string
  createTime: Date
}

export interface VehicleAccessRecordPageReqVO extends PageParam {
  plateNumber?: string
  accessType?: string
  accessResult?: string
  accessTime?: Date[]
}

// 查询车辆列表
export const getVehiclePage = (params: VehiclePageReqVO) => {
  return request.get({ url: '/access/vehicle/page', params })
}

// 查询车辆详情
export const getVehicle = (id: number) => {
  return request.get({ url: '/access/vehicle/get?id=' + id })
}

// 新增车辆
export const createVehicle = (data: VehicleSaveReqVO) => {
  return request.post({ url: '/access/vehicle/create', data })
}

// 修改车辆
export const updateVehicle = (data: VehicleSaveReqVO) => {
  return request.put({ url: '/access/vehicle/update', data })
}

// 删除车辆
export const deleteVehicle = (id: number) => {
  return request.delete({ url: '/access/vehicle/delete?id=' + id })
}

// 加入黑名单
export const addToBlacklist = (id: number, remark: string) => {
  return request.post({ url: '/access/vehicle/add-blacklist', data: { id, remark } })
}

// 移出黑名单
export const removeFromBlacklist = (id: number) => {
  return request.post({ url: '/access/vehicle/remove-blacklist?id=' + id })
}

// 查询车辆通行记录
export const getVehicleAccessRecordPage = (params: VehicleAccessRecordPageReqVO) => {
  return request.get({ url: '/access/vehicle/access-record/page', params })
}

// 导出车辆信息
export const exportVehicle = (params: VehiclePageReqVO) => {
  return request.download({ url: '/access/vehicle/export-excel', params })
}

// 导出车辆通行记录
export const exportVehicleAccessRecord = (params: VehicleAccessRecordPageReqVO) => {
  return request.download({ url: '/access/vehicle/access-record/export-excel', params })
}

// 获取车辆统计数据
export const getVehicleStatistics = (params: { startTime: Date, endTime: Date }) => {
  return request.get({ url: '/access/vehicle/statistics', params })
}











