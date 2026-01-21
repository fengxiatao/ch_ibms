import request from '@/config/axios'

// 巡更点位VO
export interface PatrolPointVO {
  id?: number
  name: string
  code: string
  description?: string
  type: number // 1-普通，2-RFID，3-二维码
  longitude?: number
  latitude?: number
  altitude?: number
  qrCodeUrl?: string
  rfidTag?: string
  floorId?: number
  floorName?: string
  buildingId?: number
  buildingName?: string
  campusId?: number
  campusName?: string
  createTime?: Date
}

// 分页请求VO
export interface PatrolPointPageReqVO extends PageParam {
  name?: string
  code?: string
  type?: number
  floorId?: number
  buildingId?: number
  campusId?: number
  createTime?: Date[]
}

// 获取巡更点位分页
export const getPatrolPointPage = (params: PatrolPointPageReqVO) => {
  return request.get<PageResult<PatrolPointVO>>({
    url: '/iot/patrol-point/page',
    params
  })
}

// 获取巡更点位详情
export const getPatrolPoint = (id: number) => {
  return request.get<PatrolPointVO>({
    url: '/iot/patrol-point/get',
    params: { id }
  })
}

// 创建巡更点位
export const createPatrolPoint = (data: PatrolPointVO) => {
  return request.post({
    url: '/iot/patrol-point/create',
    data
  })
}

// 更新巡更点位
export const updatePatrolPoint = (data: PatrolPointVO) => {
  return request.put({
    url: '/iot/patrol-point/update',
    data
  })
}

// 删除巡更点位
export const deletePatrolPoint = (id: number) => {
  return request.delete({
    url: '/iot/patrol-point/delete',
    params: { id }
  })
}

// 生成二维码
export const generateQrCode = (id: number) => {
  return request.post({
    url: '/iot/patrol-point/generate-qrcode',
    params: { id }
  })
}


























