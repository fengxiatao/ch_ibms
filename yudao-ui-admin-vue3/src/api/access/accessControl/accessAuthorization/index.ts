import request from '@/config/axios'

export interface AccessAuthorizationVO {
  id: number
  userId: number
  userName: string
  userCard: string
  department: string
  deviceId: number
  deviceName: string
  authStatus: string // authorized, unauthorized, expired
  authStartTime: Date
  authEndTime: Date
  createTime: Date
  updateTime: Date
  remark: string
}

export interface AccessAuthorizationPageReqVO extends PageParam {
  userName?: string
  department?: string
  authStatus?: string
  deviceName?: string
}

export interface AccessAuthorizationSaveReqVO {
  id?: number
  userId: number
  deviceId: number
  authStartTime: Date
  authEndTime: Date
  remark?: string
}

// 查询通行授权列表
export const getAccessAuthorizationPage = (params: AccessAuthorizationPageReqVO) => {
  return request.get({ url: '/access/access-authorization/page', params })
}

// 查询通行授权详情
export const getAccessAuthorization = (id: number) => {
  return request.get({ url: '/access/access-authorization/get?id=' + id })
}

// 新增通行授权
export const createAccessAuthorization = (data: AccessAuthorizationSaveReqVO) => {
  return request.post({ url: '/access/access-authorization/create', data })
}

// 修改通行授权
export const updateAccessAuthorization = (data: AccessAuthorizationSaveReqVO) => {
  return request.put({ url: '/access/access-authorization/update', data })
}

// 删除通行授权
export const deleteAccessAuthorization = (id: number) => {
  return request.delete({ url: '/access/access-authorization/delete?id=' + id })
}

// 批量删除通行授权
export const deleteAccessAuthorizationList = (ids: number[]) => {
  return request.delete({ url: '/access/access-authorization/delete-list', params: { ids: ids.join(',') } })
}

// 批量授权
export const batchAuthorize = (data: { userIds: number[], deviceIds: number[], authStartTime: Date, authEndTime: Date }) => {
  return request.post({ url: '/access/access-authorization/batch-authorize', data })
}

// 撤销授权
export const revokeAuthorization = (id: number) => {
  return request.post({ url: '/access/access-authorization/revoke?id=' + id })
}

// 导出通行授权
export const exportAccessAuthorization = (params: AccessAuthorizationPageReqVO) => {
  return request.download({ url: '/access/access-authorization/export-excel', params })
}



