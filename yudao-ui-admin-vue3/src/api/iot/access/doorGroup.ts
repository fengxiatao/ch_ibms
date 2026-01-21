import request from '@/config/axios'

// ==================== 门组管理 API ====================

export interface DoorGroupVO {
  id?: number
  name: string
  description?: string
  deviceCount?: number
  devices?: number[]
  createTime?: Date
}

export interface DoorGroupPageReqVO extends PageParam {
  name?: string
  createTime?: [Date, Date]
}

export interface DoorGroupCreateReqVO {
  name: string
  description?: string
  devices?: number[]
}

export interface DoorGroupUpdateReqVO extends DoorGroupCreateReqVO {
  id: number
}

// 分页查询门组
export const getDoorGroupPage = (params: DoorGroupPageReqVO) => {
  return request.get<{
    total: number
    list: DoorGroupVO[]
  }>({
    url: '/iot/door-group/page',
    params
  })
}

// 获取门组列表（不分页）
export const getDoorGroupList = () => {
  return request.get<DoorGroupVO[]>({
    url: '/iot/door-group/list'
  })
}

// 获取门组详情
export const getDoorGroup = (id: number) => {
  return request.get<DoorGroupVO>({
    url: `/iot/door-group/get?id=${id}`
  })
}

// 新增门组
export const createDoorGroup = (data: DoorGroupCreateReqVO) => {
  return request.post({
    url: '/iot/door-group/create',
    data
  })
}

// 修改门组
export const updateDoorGroup = (data: DoorGroupUpdateReqVO) => {
  return request.put({
    url: '/iot/door-group/update',
    data
  })
}

// 删除门组
export const deleteDoorGroup = (id: number) => {
  return request.delete({
    url: `/iot/door-group/delete?id=${id}`
  })
}

// 导出门组 Excel
export const exportDoorGroupExcel = (params: DoorGroupPageReqVO) => {
  return request.download({
    url: '/iot/door-group/export-excel',
    params
  })
}


























