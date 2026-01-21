import request from '@/config/axios'

// 区域 VO
export interface AreaVO {
  id?: number
  floorId: number
  name: string
  code: string
  areaType?: string
  areaSize?: number
  usableArea?: number
  maxOccupancy?: number
  currentOccupancy?: number
  primaryPurpose?: string
  responsiblePerson?: string
  contactPhone?: string
  isActive?: boolean
  hasCctv?: boolean
  hasAccessControl?: boolean
  hasFireEquipment?: boolean
  temperatureSetting?: number
  humiditySetting?: number
  lightingPower?: number
  socketPower?: number
  description?: string
  boundary?: string
  properties?: string
  createTime?: Date
}

// 区域分页 Request VO
export interface AreaPageReqVO extends PageParam {
  name?: string
  code?: string
  floorId?: number
  areaType?: string
  createTime?: Date[]
}

// 查询区域分页
export const getAreaPage = async (params: AreaPageReqVO) => {
  return await request.get({ url: '/iot/gis/area/page', params })
}

// 查询区域列表
export const getAreaList = async () => {
  return await request.get({ url: '/iot/gis/area/list' })
}

// 根据楼层ID查询区域列表
export const getAreaListByFloorId = async (floorId: number) => {
  return await request.get({ url: '/iot/gis/area/list-by-floor', params: { floorId } })
}

// 查询区域详情
export const getArea = async (id: number) => {
  return await request.get({ url: '/iot/gis/area/get', params: { id } })
}

// 新增区域
export const createArea = async (data: AreaVO) => {
  return await request.post({ url: '/iot/gis/area/create', data })
}

// 修改区域
export const updateArea = async (data: AreaVO) => {
  return await request.put({ url: '/iot/gis/area/update', data })
}

// 删除区域
export const deleteArea = async (id: number) => {
  return await request.delete({ url: '/iot/gis/area/delete', params: { id } })
}

// 批量创建区域（从DXF识别结果）
export const batchCreateAreas = async (areas: AreaVO[]) => {
  return await request.post({ url: '/iot/gis/area/batch-create', data: areas })
}

