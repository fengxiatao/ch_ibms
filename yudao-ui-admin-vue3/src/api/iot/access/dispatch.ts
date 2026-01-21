import request from '@/config/axios'

// ==================== 门禁下发 API ====================

export interface AccessDispatchVO {
  id?: number
  authorizationId: number
  deviceId: number
  deviceName?: string
  dispatchType: number
  dispatchStatus: number
  dispatchTime?: Date
  responseTime?: Date
  errorMsg?: string
  createTime?: Date
}

export interface AccessDispatchPageReqVO extends PageParam {
  authorizationId?: number
  deviceId?: number
  dispatchType?: number
  dispatchStatus?: number
  dispatchTime?: [Date, Date]
}

// 分页查询门禁下发记录
export const getAccessDispatchPage = (params: AccessDispatchPageReqVO) => {
  return request.get<{
    total: number
    list: AccessDispatchVO[]
  }>({
    url: '/iot/access-dispatch/page',
    params
  })
}

// 获取门禁下发详情
export const getAccessDispatch = (id: number) => {
  return request.get<AccessDispatchVO>({
    url: `/iot/access-dispatch/get?id=${id}`
  })
}

// 重新下发
export const retryDispatch = (id: number) => {
  return request.post({
    url: `/iot/access-dispatch/retry?id=${id}`
  })
}

// 导出门禁下发记录 Excel
export const exportAccessDispatchExcel = (params: AccessDispatchPageReqVO) => {
  return request.download({
    url: '/iot/access-dispatch/export-excel',
    params
  })
}


























