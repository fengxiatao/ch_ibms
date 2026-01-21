/**
 * 楼层管理 API
 */

import request from '@/config/axios'

export interface FloorVO {
  id?: number
  buildingId: number
  name: string
  code: string
  floorNumber: number
  floorType?: string
  floorHeight?: number
  floorArea?: number
  usableArea?: number
  primaryFunction?: string
  geom?: string
  localGeom?: string
  zBase?: number
  zTop?: number
  areaCount?: number  // 区域数量（统计字段）
}

export interface FloorPageReqVO {
  pageNo?: number
  pageSize?: number
  buildingId?: number
  name?: string
  floorNumber?: number
}

/**
 * 查询楼层列表
 * 注意：使用空间管理模块的API，而不是GIS模块
 */
export const getFloorList = (params: FloorPageReqVO) => {
  return request.get({ url: '/iot/floor/page', params })
}

/**
 * 查询楼层详情
 * 注意：使用空间管理模块的API
 */
export const getFloor = (id: number) => {
  return request.get({ url: `/iot/floor/get?id=${id}` })
}

/**
 * 新增楼层
 * 注意：使用空间管理模块的API
 */
export const createFloor = (data: FloorVO) => {
  return request.post({ url: '/iot/floor/create', data })
}

/**
 * 修改楼层
 * 注意：使用空间管理模块的API
 */
export const updateFloor = (data: FloorVO) => {
  return request.put({ url: '/iot/floor/update', data })
}

/**
 * 删除楼层
 * 注意：使用空间管理模块的API
 */
export const deleteFloor = (id: number) => {
  return request.delete({ url: `/iot/floor/delete?id=${id}` })
}

/**
 * 获取建筑的所有楼层
 * 注意：使用空间管理模块的API
 */
export const getBuildingFloors = (buildingId: number) => {
  return request.get({ url: `/iot/floor/list-by-building?buildingId=${buildingId}` })
}




