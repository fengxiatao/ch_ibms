import request from '@/config/axios'

// 园区 VO
export interface CampusVO {
  id?: number
  name: string
  code: string
  type?: string
  areaSqm?: number
  greenCoverageRate?: number
  floorAreaRatio?: number
  province?: string
  city?: string
  district?: string
  address?: string
  postcode?: string
  contactPerson?: string
  contactPhone?: string
  email?: string
  propertyCompany?: string
  managementMode?: string
  operationStatus?: string
  geom?: string
  centerPoint?: string
  altitude?: number
  remark?: string
  createTime?: Date
}

// 园区分页 Request VO
export interface CampusPageReqVO extends PageParam {
  name?: string
  code?: string
  type?: string
  province?: string
  city?: string
  district?: string
  operationStatus?: string
  createTime?: Date[]
}

// 查询园区分页
export const getCampusPage = async (params: CampusPageReqVO) => {
  return await request.get({ url: '/iot/campus/page', params })
}

// 查询园区列表
export const getCampusList = async () => {
  return await request.get({ url: '/iot/campus/list' })
}

// 查询园区详情
export const getCampus = async (id: number) => {
  return await request.get({ url: '/iot/campus/get', params: { id } })
}

// 新增园区
export const createCampus = async (data: CampusVO) => {
  return await request.post({ url: '/iot/campus/create', data })
}

// 修改园区
export const updateCampus = async (data: CampusVO) => {
  return await request.put({ url: '/iot/campus/update', data })
}

// 删除园区
export const deleteCampus = async (id: number) => {
  return await request.delete({ url: '/iot/campus/delete', params: { id } })
}

// 导出园区 Excel
export const exportCampus = async (params: CampusPageReqVO) => {
  return await request.download({ url: '/iot/campus/export-excel', params })
}

