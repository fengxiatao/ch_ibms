/**
 * 区域管理 API
 */

import request from '@/config/axios'

export interface AreaVO {
  id?: number
  floorId: number
  buildingId?: number
  campusId?: number
  name: string
  code: string
  areaType: string
  subType?: string
  areaSqm?: number
  capacity?: number
  geom?: string
  localGeom?: string
  centerPoint?: string
  fillColor?: string
  strokeColor?: string
  opacity?: number
  isVisible?: boolean
}

export interface AreaPageReqVO {
  pageNo?: number
  pageSize?: number
  floorId?: number
  buildingId?: number
  name?: string
  areaType?: string
}

/**
 * 查询区域列表
 */
export const getAreaList = (params: AreaPageReqVO) => {
  return request.get({ url: '/iot/gis/area/page', params })
}

/**
 * 查询区域详情
 */
export const getArea = (id: number) => {
  return request.get({ url: `/iot/gis/area/get?id=${id}` })
}

/**
 * 新增区域
 */
export const createArea = (data: AreaVO) => {
  return request.post({ url: '/iot/gis/area/create', data })
}

/**
 * 修改区域
 */
export const updateArea = (data: AreaVO) => {
  return request.put({ url: '/iot/gis/area/update', data })
}

/**
 * 删除区域
 */
export const deleteArea = (id: number) => {
  return request.delete({ url: `/iot/gis/area/delete?id=${id}` })
}

/**
 * 获取楼层的所有区域
 */
export const getFloorAreas = (floorId: number) => {
  return request.get({ url: `/iot/gis/area/floor/${floorId}/list` })
}


















