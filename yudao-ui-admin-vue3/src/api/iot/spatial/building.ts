import request from '@/config/axios'

// 建筑 VO
export interface BuildingVO {
  id?: number
  campusId: number
  name: string
  code: string
  buildingType?: string
  structureType?: string
  totalFloors?: number
  aboveGroundFloors?: number
  undergroundFloors?: number
  buildingHeight?: number
  footprintAreaSqm?: number
  totalAreaSqm?: number
  usableAreaSqm?: number
  constructionYear?: number
  designUnit?: string
  constructionUnit?: string
  fireResistanceRating?: string
  seismicIntensity?: string
  elevatorCount?: number
  hasCentralAc?: boolean
  hasFireSystem?: boolean
  hasSecuritySystem?: boolean
  hasSmartSystem?: boolean
  powerCapacityKva?: number
  waterCapacityCubic?: number
  geom?: string
  centroid?: string
  orientation?: number
  description?: string
  properties?: string
  createTime?: Date
}

// 建筑分页 Request VO
export interface BuildingPageReqVO extends PageParam {
  name?: string
  code?: string
  campusId?: number
  buildingType?: string
  createTime?: Date[]
}

// 查询建筑分页
export const getBuildingPage = async (params: BuildingPageReqVO) => {
  return await request.get({ url: '/iot/building/page', params })
}

// 查询建筑列表
export const getBuildingList = async () => {
  return await request.get({ url: '/iot/building/list' })
}

// 根据园区ID查询建筑列表
export const getBuildingListByCampusId = async (campusId: number) => {
  return await request.get({ url: '/iot/building/list-by-campus', params: { campusId } })
}

// 查询建筑详情
export const getBuilding = async (id: number) => {
  return await request.get({ url: '/iot/building/get', params: { id } })
}

// 新增建筑
export const createBuilding = async (data: BuildingVO) => {
  return await request.post({ url: '/iot/building/create', data })
}

// 修改建筑
export const updateBuilding = async (data: BuildingVO) => {
  return await request.put({ url: '/iot/building/update', data })
}

// 删除建筑
export const deleteBuilding = async (id: number) => {
  return await request.delete({ url: '/iot/building/delete', params: { id } })
}

