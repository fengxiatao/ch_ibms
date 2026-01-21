import request from '@/config/axios'

export interface VisitorVO {
  id: number
  visitorName: string
  visitorPhone: string
  visitorIdCard: string
  visitorCompany: string
  visitPurpose: string
  visiteeId: number
  visiteeName: string
  visiteePhone: string
  visiteeDept: string
  visitStartTime: Date
  visitEndTime: Date
  visitArea: string
  status: string // pending, approved, rejected, expired, completed
  approvalTime?: Date
  approvalRemark?: string
  checkInTime?: Date
  checkOutTime?: Date
  visitorPhoto?: string
  idCardPhoto?: string
  tempCardNumber?: string
  createTime: Date
  updateTime: Date
}

export interface VisitorPageReqVO extends PageParam {
  visitorName?: string
  visitorPhone?: string
  visiteeName?: string
  status?: string
  visitStartTime?: Date[]
}

export interface VisitorSaveReqVO {
  id?: number
  visitorName: string
  visitorPhone: string
  visitorIdCard: string
  visitorCompany: string
  visitPurpose: string
  visiteeId: number
  visitStartTime: Date
  visitEndTime: Date
  visitArea: string
  visitorPhoto?: string
  idCardPhoto?: string
}

// 查询访客列表
export const getVisitorPage = (params: VisitorPageReqVO) => {
  return request.get({ url: '/access/visitor/page', params })
}

// 查询访客详情
export const getVisitor = (id: number) => {
  return request.get({ url: '/access/visitor/get?id=' + id })
}

// 新增访客预约
export const createVisitor = (data: VisitorSaveReqVO) => {
  return request.post({ url: '/access/visitor/create', data })
}

// 修改访客信息
export const updateVisitor = (data: VisitorSaveReqVO) => {
  return request.put({ url: '/access/visitor/update', data })
}

// 删除访客记录
export const deleteVisitor = (id: number) => {
  return request.delete({ url: '/access/visitor/delete?id=' + id })
}

// 审批访客申请
export const approveVisitor = (data: { id: number, approved: boolean, remark?: string }) => {
  return request.post({ url: '/access/visitor/approve', data })
}

// 访客签到
export const checkInVisitor = (id: number, data: { tempCardNumber?: string }) => {
  return request.post({ url: '/access/visitor/check-in?id=' + id, data })
}

// 访客签退
export const checkOutVisitor = (id: number) => {
  return request.post({ url: '/access/visitor/check-out?id=' + id })
}

// 导出访客记录
export const exportVisitor = (params: VisitorPageReqVO) => {
  return request.download({ url: '/access/visitor/export-excel', params })
}

// 获取访客统计数据
export const getVisitorStatistics = (params: { startTime: Date, endTime: Date }) => {
  return request.get({ url: '/access/visitor/statistics', params })
}











