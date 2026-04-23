import request from '@/config/axios'

export interface IbmsDeviceRespVO {
  id: number
  deviceCode: string
  name: string
  groupCode: string
  systemCode: string
  deviceTypeCode: string
  productModel: string
  brand: string
  accessType: string
  ip?: string
  protocol?: string
  sn?: string
  productKey?: string
  pointCount: number
  pointsOnline: number
  pointsAlarm: number
  space?: string
  extra?: string
  createTime?: string
}

export interface IbmsDeviceSaveReqVO {
  id?: number
  name: string
  groupCode: string
  systemCode: string
  deviceTypeCode: string
  brand: string
  accessType: string
  productModel: string
  ip?: string
  protocol?: string
  space?: string
  /** 设备编码序号（3 位），如 1/2/3，会在后端补零 */
  seq: number
  /** 扩展配置 JSON 字符串，键与产品属性 propName 一致 */
  extra?: string
}

export interface IbmsDevicePageReqVO {
  pageNo: number
  pageSize: number
  keyword?: string
  groupCode?: string
  systemCode?: string
  deviceTypeCode?: string
  brand?: string
  accessType?: string
  /** 对齐 ibms_device.ibms_product_id */
  ibmsProductId?: number
}

export const getDevicePage = (params: IbmsDevicePageReqVO) => {
  return request.get<any>({ url: '/iot/ibms/device/page', params })
}

export const getDevice = (id: number) => {
  return request.get<IbmsDeviceRespVO>({ url: '/iot/ibms/device/get', params: { id } })
}

export const createDevice = (data: IbmsDeviceSaveReqVO) => {
  return request.post<number>({ url: '/iot/ibms/device/create', data })
}

export const updateDevice = (data: IbmsDeviceSaveReqVO) => {
  return request.put<boolean>({ url: '/iot/ibms/device/update', data })
}

export const deleteDevice = (id: number) => {
  return request.delete<boolean>({ url: '/iot/ibms/device/delete', params: { id } })
}

/** 导出当前筛选条件下的 IBMS 设备台账（须 iot:ibms-device:export） */
export const exportDeviceExcel = (params: IbmsDevicePageReqVO) => {
  return request.download({ url: '/iot/ibms/device/export-excel', params })
}

