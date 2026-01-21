/**
 * 设备位置管理 API
 * 用于室内2D地图编辑器的设备位置CRUD操作
 */

import request from '@/config/axios'

// ========== 设备位置类型定义 ==========

export interface DeviceLocationVO {
  id?: number
  deviceId: number
  floorId: number
  buildingId?: number
  areaId?: number
  localX: number  // 本地X坐标（米）
  localY: number  // 本地Y坐标（米）
  localZ: number  // 本地Z坐标（米）
  globalLongitude?: number  // 全局经度（可选）
  globalLatitude?: number   // 全局纬度（可选）
  globalAltitude?: number   // 全局海拔（可选）
  creator?: string
  createTime?: Date
  updater?: string
  updateTime?: Date
}

export interface DeviceWithLocationVO {
  // 设备基本信息
  id: number
  name: string
  deviceKey: string
  deviceType: string
  productId: number
  gatewayId?: number
  status: string
  
  // 位置信息
  floorId?: number
  floorName?: string
  buildingId?: number
  buildingName?: string
  areaId?: number
  areaName?: string
  localX?: number
  localY?: number
  localZ?: number
  
  // 实时数据
  realtimeData?: Record<string, any>
}

export interface BatchUpdateLocationReqVO {
  devices: Array<{
    deviceId: number
    localX: number
    localY: number
    localZ: number
    areaId?: number
  }>
}

// ========== API 方法 ==========

/**
 * 创建设备位置
 */
export const createDeviceLocation = (data: DeviceLocationVO) => {
  return request.post({ url: '/iot/device-location/create', data })
}

/**
 * 更新设备位置
 */
export const updateDeviceLocation = (data: DeviceLocationVO) => {
  return request.put({ url: '/iot/device-location/update', data })
}

/**
 * 删除设备位置
 */
export const deleteDeviceLocation = (id: number) => {
  return request.delete({ url: `/iot/device-location/delete?id=${id}` })
}

/**
 * 获取设备位置详情
 */
export const getDeviceLocation = (id: number) => {
  return request.get({ url: `/iot/device-location/get?id=${id}` })
}

/**
 * 根据设备ID获取位置信息
 */
export const getDeviceLocationByDeviceId = (deviceId: number) => {
  return request.get({ url: `/iot/device-location/by-device?deviceId=${deviceId}` })
}

/**
 * 批量更新设备位置
 * 用于拖拽多个设备后批量保存
 */
export const batchUpdateDeviceLocation = (data: BatchUpdateLocationReqVO) => {
  return request.post({ url: '/iot/device-location/batch-update', data })
}

/**
 * 获取楼层内的所有设备（包含位置信息）
 */
export const getDevicesInFloor = (floorId: number) => {
  return request.get<DeviceWithLocationVO[]>({ 
    url: `/iot/device-location/floor/${floorId}/devices` 
  })
}

/**
 * 获取区域内的所有设备（包含位置信息）
 */
export const getDevicesInArea = (areaId: number) => {
  return request.get<DeviceWithLocationVO[]>({ 
    url: `/iot/device-location/area/${areaId}/devices` 
  })
}

/**
 * 获取建筑内的所有设备（包含位置信息）
 */
export const getDevicesInBuilding = (buildingId: number) => {
  return request.get<DeviceWithLocationVO[]>({ 
    url: `/iot/device-location/building/${buildingId}/devices` 
  })
}

/**
 * 自动分配设备到区域
 * 根据设备的本地坐标自动判断所属区域
 */
export const autoAssignDeviceToArea = (deviceId: number) => {
  return request.post({ url: `/iot/device-location/auto-assign-area?deviceId=${deviceId}` })
}

/**
 * 批量自动分配设备到区域
 */
export const batchAutoAssignDeviceToArea = (floorId: number) => {
  return request.post({ url: `/iot/device-location/batch-auto-assign-area?floorId=${floorId}` })
}

/**
 * 获取设备的全局坐标（经纬度）
 * 根据建筑信息将本地坐标转换为全局坐标
 */
export const getDeviceGlobalCoordinate = (deviceId: number) => {
  return request.get({ url: `/iot/device-location/global-coordinate?deviceId=${deviceId}` })
}

/**
 * 在指定位置添加新设备
 * 一步完成设备创建和位置设置
 */
export interface CreateDeviceWithLocationReqVO {
  // 设备信息
  name: string
  deviceKey?: string
  deviceType: string
  productId: number
  gatewayId?: number
  
  // 位置信息
  floorId: number
  areaId?: number
  localX: number
  localY: number
  localZ: number
}

export const createDeviceWithLocation = (data: CreateDeviceWithLocationReqVO) => {
  return request.post({ url: '/iot/device-location/create-device-with-location', data })
}

/**
 * 移动设备到新位置
 */
export interface MoveDeviceReqVO {
  deviceId: number
  targetFloorId?: number
  targetAreaId?: number
  localX: number
  localY: number
  localZ?: number
}

export const moveDevice = (data: MoveDeviceReqVO) => {
  return request.post({ url: '/iot/device-location/move-device', data })
}

/**
 * 验证设备位置是否有效
 * 检查是否在楼层范围内、是否在正确的区域内等
 */
export const validateDeviceLocation = (data: {
  floorId: number
  localX: number
  localY: number
}) => {
  return request.post({ url: '/iot/device-location/validate', data })
}


















