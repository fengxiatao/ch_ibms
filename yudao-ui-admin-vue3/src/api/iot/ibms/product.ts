import request from '@/config/axios'

export interface IbmsProductPointTypeVO {
  id?: number
  pointTypeCode: string
  name: string
  count: number
  dataType?: string
}

export interface IbmsProductPropertyVO {
  id?: number
  propName: string
  label: string
  type: string
  options?: string
  defaultValue?: string
  unit?: string
}

/** 弹窗表单行：仅前端使用，提交接口前需剔除 */
export type IbmsProductPointTypeFormRow = IbmsProductPointTypeVO & { clientRowKey: string }
export type IbmsProductPropertyFormRow = IbmsProductPropertyVO & { clientRowKey: string }

export interface IbmsProductRespVO {
  id: number
  productCode: string
  productName: string
  groupCode: string
  systemCode: string
  modelCode: string
  deviceTypeCode: string
  manufacturer: string
  modelNumber: string
  protocol?: string
  icon?: string
  color?: string
  description?: string
  pointTypes: IbmsProductPointTypeVO[]
  properties: IbmsProductPropertyVO[]
}

export interface IbmsProductSaveReqVO {
  id?: number
  groupCode: string
  systemCode: string
  modelCode: string
  deviceTypeCode: string
  productName: string
  manufacturer: string
  modelNumber: string
  protocol?: string
  icon?: string
  description?: string
  pointTypes: IbmsProductPointTypeVO[]
  properties: IbmsProductPropertyVO[]
}

export interface IbmsProductPageReqVO {
  pageNo: number
  pageSize: number
  productName?: string
  groupCode?: string
  systemCode?: string
  modelCode?: string
  deviceTypeCode?: string
  manufacturer?: string
  /** 产品型号，精确匹配 */
  modelNumber?: string
}

/** 设备表单：按录入维度解析产品模板（含 properties） */
export interface IbmsProductResolveTemplateReq {
  groupCode: string
  systemCode: string
  deviceTypeCode: string
  modelNumber: string
}

export const getProductPage = (params: IbmsProductPageReqVO) => {
  return request.get<any>({ url: '/iot/ibms/product/page', params })
}

export const getProduct = (id: number) => {
  return request.get<IbmsProductRespVO>({ url: '/iot/ibms/product/get', params: { id } })
}

export const resolveTemplateForDevice = (params: IbmsProductResolveTemplateReq) => {
  return request.get<IbmsProductRespVO | null>({
    url: '/iot/ibms/product/resolve-template-for-device',
    params
  })
}

export const createProduct = (data: IbmsProductSaveReqVO) => {
  return request.post<number>({ url: '/iot/ibms/product/create', data })
}

export const updateProduct = (data: IbmsProductSaveReqVO) => {
  return request.put<boolean>({ url: '/iot/ibms/product/update', data })
}

export const deleteProduct = (id: number) => {
  return request.delete<boolean>({ url: '/iot/ibms/product/delete', params: { id } })
}

