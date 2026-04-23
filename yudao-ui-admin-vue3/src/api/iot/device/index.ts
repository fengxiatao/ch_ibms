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
  return request.get({
    url: '/iot/ibms/device/page',
    params: {
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      keyword: params.name,
      systemCode: params.deviceType
    }
  })
}

/**
 * 查询设备详情（IBMS 台账，并兼容旧字段 deviceName / ipAddress）
 */
export const getDevice = async (id: number) => {
  const data: any = await request.get({ url: `/iot/ibms/device/get`, params: { id } })
  if (!data) {
    return data
  }
  return {
    ...data,
    deviceName: data.name ?? data.deviceName,
    ipAddress: data.ip ?? data.ipAddress,
    deviceKey: data.deviceCode ?? data.deviceKey
  }
}

/**
 * 新增设备（IBMS 台账 `/iot/ibms/device/create`；请求体字段与 `api/iot/ibms/device` 中 IbmsDeviceSaveReqVO 一致）
 */
export const createDevice = (data: Record<string, any>) => {
  return request.post({ url: '/iot/ibms/device/create', data })
}

/**
 * 修改设备（IBMS）
 */
export const updateDevice = (id: number, data: Record<string, any>) => {
  return request.put({ url: '/iot/ibms/device/update', data: { id, ...data } })
}

/**
 * 删除设备（IBMS）
 */
export const deleteDevice = (id: number) => {
  return request.delete({ url: `/iot/ibms/device/delete?id=${id}` })
}

/**
 * 导出设备 Excel（IBMS 台账，与列表筛选字段映射一致）
 */
export const exportDevice = (params: DevicePageReqVO) => {
  return request.download({
    url: '/iot/ibms/device/export-excel',
    params: {
      pageNo: params.pageNo ?? 1,
      pageSize: -1,
      keyword: params.name,
      systemCode: params.deviceType,
      ibmsProductId: params.productId
    }
  })
}


















