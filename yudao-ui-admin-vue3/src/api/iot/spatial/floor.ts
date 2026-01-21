import request from '@/config/axios'

// 楼层 VO
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
  occupancyRate?: number
  maxOccupancy?: number
  hasSprinkler?: boolean
  hasSmokeDetector?: boolean
  hasEmergencyExit?: boolean
  emergencyExitCount?: number
  acType?: string
  designTempSummer?: number
  designTempWinter?: number
  description?: string
  createTime?: Date
  // 平面图相关字段
  floorPlanUrl?: string          // 平面图URL
  floorPlanWidth?: number         // 建筑宽度（米）
  floorPlanHeight?: number        // 建筑长度（米）
}

// 楼层分页 Request VO
export interface FloorPageReqVO extends PageParam {
  name?: string
  code?: string
  buildingId?: number
  floorNumber?: number
  createTime?: Date[]
}

// 查询楼层分页
export const getFloorPage = async (params: FloorPageReqVO) => {
  return await request.get({ url: '/iot/floor/page', params })
}

// 查询楼层列表
export const getFloorList = async () => {
  return await request.get({ url: '/iot/floor/list' })
}

// 根据建筑ID查询楼层列表
export const getFloorListByBuildingId = async (buildingId: number) => {
  return await request.get({ url: '/iot/floor/list-by-building', params: { buildingId } })
}

// 查询楼层详情
export const getFloor = async (id: number) => {
  return await request.get({ url: '/iot/floor/get', params: { id } })
}

// 新增楼层
export const createFloor = async (data: FloorVO) => {
  return await request.post({ url: '/iot/floor/create', data })
}

// 修改楼层
export const updateFloor = async (data: FloorVO) => {
  return await request.put({ url: '/iot/floor/update', data })
}

// 删除楼层
export const deleteFloor = async (id: number) => {
  return await request.delete({ url: '/iot/floor/delete', params: { id } })
}

