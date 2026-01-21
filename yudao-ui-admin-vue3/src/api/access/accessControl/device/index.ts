import request from '@/config/axios'

export interface AccessDeviceVO {
  id: number
  deviceCode: string
  deviceName: string
  deviceType: string // gate, turnstile, door, elevator
  deviceModel: string
  deviceLocation: string
  ipAddress: string
  port: number
  status: string // online, offline, maintenance
  installTime: Date
  lastHeartbeat: Date
  features: string[] // face, card, fingerprint, qrcode
  remark: string
  createTime: Date
  updateTime: Date
}

export interface AccessDevicePageReqVO extends PageParam {
  deviceName?: string
  deviceType?: string
  status?: string
  deviceLocation?: string
}

export interface AccessDeviceSaveReqVO {
  id?: number
  deviceCode: string
  deviceName: string
  deviceType: string
  deviceModel: string
  deviceLocation: string
  ipAddress: string
  port: number
  features: string[]
  remark?: string
}

// 查询门禁设备列表
export const getAccessDevicePage = (params: AccessDevicePageReqVO) => {
  return request.get({ url: '/access/device/page', params })
}

// 查询门禁设备详情
export const getAccessDevice = (id: number) => {
  return request.get({ url: '/access/device/get?id=' + id })
}

// 新增门禁设备
export const createAccessDevice = (data: AccessDeviceSaveReqVO) => {
  return request.post({ url: '/access/device/create', data })
}

// 修改门禁设备
export const updateAccessDevice = (data: AccessDeviceSaveReqVO) => {
  return request.put({ url: '/access/device/update', data })
}

// 删除门禁设备
export const deleteAccessDevice = (id: number) => {
  return request.delete({ url: '/access/device/delete?id=' + id })
}

// 设备控制 - 开门
export const openDoor = (id: number) => {
  return request.post({ url: '/access/device/open-door?id=' + id })
}

// 设备控制 - 关门
export const closeDoor = (id: number) => {
  return request.post({ url: '/access/device/close-door?id=' + id })
}

// 设备重启
export const restartDevice = (id: number) => {
  return request.post({ url: '/access/device/restart?id=' + id })
}

// 获取设备状态
export const getDeviceStatus = (id: number) => {
  return request.get({ url: '/access/device/status?id=' + id })
}

// 同步设备时间
export const syncDeviceTime = (id: number) => {
  return request.post({ url: '/access/device/sync-time?id=' + id })
}

// 获取设备简单列表（用于选择器）
export const getAccessDeviceSimpleList = () => {
  return request.get({ url: '/access/device/simple-list' })
}











