import request from '@/config/axios'

// ==================== 门岗管理 API ====================

export interface DoorPostVO {
  id?: number
  name: string
  code?: string
  description?: string
  buildingId?: number
  buildingName?: string
  floorId?: number
  floorName?: string
  longitude?: number
  latitude?: number
  altitude?: number
  deviceCount?: number
  devices?: number[]
  createTime?: Date
}

export interface DoorPostPageReqVO extends PageParam {
  name?: string
  code?: string
  buildingId?: number
  floorId?: number
  createTime?: [Date, Date]
}

export interface DoorPostCreateReqVO {
  name: string
  code?: string
  description?: string
  buildingId?: number
  floorId?: number
  longitude?: number
  latitude?: number
  altitude?: number
  devices?: number[]
}

export interface DoorPostUpdateReqVO extends DoorPostCreateReqVO {
  id: number
}

// 分页查询门岗
export const getDoorPostPage = (params: DoorPostPageReqVO) => {
  return request.get<{
    total: number
    list: DoorPostVO[]
  }>({
    url: '/iot/door-post/page',
    params
  })
}

// 获取门岗列表（不分页）
export const getDoorPostList = () => {
  return request.get<DoorPostVO[]>({
    url: '/iot/door-post/list'
  })
}

// 获取门岗详情
export const getDoorPost = (id: number) => {
  return request.get<DoorPostVO>({
    url: `/iot/door-post/get?id=${id}`
  })
}

// 新增门岗
export const createDoorPost = (data: DoorPostCreateReqVO) => {
  return request.post({
    url: '/iot/door-post/create',
    data
  })
}

// 修改门岗
export const updateDoorPost = (data: DoorPostUpdateReqVO) => {
  return request.put({
    url: '/iot/door-post/update',
    data
  })
}

// 删除门岗
export const deleteDoorPost = (id: number) => {
  return request.delete({
    url: `/iot/door-post/delete?id=${id}`
  })
}

// 导出门岗 Excel
export const exportDoorPostExcel = (params: DoorPostPageReqVO) => {
  return request.download({
    url: '/iot/door-post/export-excel',
    params
  })
}


























