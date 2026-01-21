import request from '@/config/axios'

// ==================== 门禁授权 API ====================

export interface AccessAuthorizationVO {
  id?: number
  authType: number
  orgId?: number
  personId?: number
  deviceId?: number
  doorGroupId?: number
  startTime?: Date
  endTime?: Date
  weekDays?: string
  timeSlots?: string
  authStatus: number
  remark?: string
  createTime?: Date
}

export interface AccessAuthorizationPageReqVO extends PageParam {
  authType?: number
  orgId?: number
  personId?: number
  deviceId?: number
  doorGroupId?: number
  authStatus?: number
  createTime?: [Date, Date]
}

export interface AccessAuthorizationCreateReqVO {
  authType: number
  orgId?: number
  personId?: number
  deviceId?: number
  doorGroupId?: number
  startTime?: Date
  endTime?: Date
  weekDays?: string
  timeSlots?: string
  authStatus: number
  remark?: string
}

export interface AccessAuthorizationUpdateReqVO extends AccessAuthorizationCreateReqVO {
  id: number
}

// 分页查询门禁授权
export const getAccessAuthorizationPage = (params: AccessAuthorizationPageReqVO) => {
  return request.get<{
    total: number
    list: AccessAuthorizationVO[]
  }>({
    url: '/iot/access-authorization/page',
    params
  })
}

// 获取门禁授权详情
export const getAccessAuthorization = (id: number) => {
  return request.get<AccessAuthorizationVO>({
    url: `/iot/access-authorization/get?id=${id}`
  })
}

// 新增门禁授权
export const createAccessAuthorization = (data: AccessAuthorizationCreateReqVO) => {
  return request.post({
    url: '/iot/access-authorization/create',
    data
  })
}

// 修改门禁授权
export const updateAccessAuthorization = (data: AccessAuthorizationUpdateReqVO) => {
  return request.put({
    url: '/iot/access-authorization/update',
    data
  })
}

// 删除门禁授权
export const deleteAccessAuthorization = (id: number) => {
  return request.delete({
    url: `/iot/access-authorization/delete?id=${id}`
  })
}

// 批量删除门禁授权
export const deleteAccessAuthorizationList = (ids: number[]) => {
  return request.delete({
    url: '/iot/access-authorization/delete-list',
    data: ids
  })
}

// 更新授权状态
export const updateAuthStatus = (id: number, status: number) => {
  return request.put({
    url: '/iot/access-authorization/status',
    data: { id, status }
  })
}

// 导出门禁授权 Excel
export const exportAccessAuthorizationExcel = (params: AccessAuthorizationPageReqVO) => {
  return request.download({
    url: '/iot/access-authorization/export-excel',
    params
  })
}







