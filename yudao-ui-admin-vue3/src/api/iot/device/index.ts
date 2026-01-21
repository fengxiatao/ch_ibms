/**
 * 设备管理 API
 */

import request from '@/config/axios'

export interface DeviceVO {
  id?: number
  name: string
  deviceKey?: string
  deviceType: string
  productId: number
  gatewayId?: number
  status?: string
  
  // 位置相关（扩展字段）
  buildingId?: number    // 所属建筑ID
  floorId?: number       // 所属楼层ID
  floorName?: string
  roomId?: number        // 所属区域ID（房间），对应前端的areaId
  areaId?: number        // 旧的地区编码（Integer），与roomId不同
  areaName?: string
  localX?: number
  localY?: number
  localZ?: number
  
  // 其他
  realtimeData?: Record<string, any>
  creator?: string
  createTime?: Date
}

export interface DevicePageReqVO {
  pageNo?: number
  pageSize?: number
  name?: string
  deviceType?: string
  productId?: number
  status?: string
  buildingId?: number  // 所属建筑ID
  floorId?: number     // 所属楼层ID
  areaId?: number      // 所属区域ID（前端使用areaId，后端映射到roomId字段）
}

/**
 * 查询设备列表
 */
export const getDeviceList = (params: DevicePageReqVO) => {
  return request.get({ url: '/iot/device/page', params })
}

/**
 * 查询设备详情
 */
export const getDevice = (id: number) => {
  return request.get({ url: `/iot/device/get?id=${id}` })
}

/**
 * 新增设备
 */
export const createDevice = (data: DeviceVO) => {
  return request.post({ url: '/iot/device/create', data })
}

/**
 * 修改设备
 */
export const updateDevice = (id: number, data: Partial<DeviceVO>) => {
  return request.put({ url: '/iot/device/update', data: { id, ...data } })
}

/**
 * 删除设备
 */
export const deleteDevice = (id: number) => {
  return request.delete({ url: `/iot/device/delete?id=${id}` })
}

/**
 * 导出设备 Excel
 */
export const exportDevice = (params: DevicePageReqVO) => {
  return request.download({ url: '/iot/device/export-excel', params })
}


















