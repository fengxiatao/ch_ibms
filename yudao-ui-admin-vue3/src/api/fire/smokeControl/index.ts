/**
 * 请求封装
 * 说明：统一替换为 '@/config/axios'
 */
import request from '@/config/axios'

export interface SmokeControlSystemVO {
  id?: number
  systemName: string
  systemType: string
  location: string
  coverage: string
  status: number
  installDate: Date
  lastInspection?: Date
  nextInspection?: Date
  manufacturer?: string
  model?: string
  capacity?: number
  description?: string
}

export interface SmokeDetectionVO {
  id?: number
  deviceName: string
  deviceCode: string
  deviceType: string
  location: string
  floor: string
  area: string
  status: number
  smokeLevel?: number
  threshold: number
  installDate: Date
  lastCalibration?: Date
  nextCalibration?: Date
  manufacturer?: string
  model?: string
  description?: string
}

export interface VentilationControlVO {
  id?: number
  deviceName: string
  deviceCode: string
  controlType: string
  location: string
  status: number
  currentSpeed?: number
  targetSpeed?: number
  airFlow?: number
  power?: number
  installDate: Date
  lastMaintenance?: Date
  nextMaintenance?: Date
  manufacturer?: string
  model?: string
  description?: string
}

export interface SmokeControlRecordVO {
  id?: number
  systemId: number
  systemName?: string
  recordType: string
  triggerTime: Date
  endTime?: Date
  triggerReason: string
  controlAction: string
  result: string
  smokeLevel?: number
  airFlow?: number
  operator?: string
  description?: string
  images?: string[]
}

export interface SmokeControlPageReqVO extends PageParam {
  systemName?: string
  systemType?: string
  location?: string
  status?: number
  recordType?: string
  triggerTime?: [Date, Date]
}

// 防排烟 API
export const SmokeControlApi = {
  // 查询防排烟系统分页
  getSmokeControlSystemPage: async (params: SmokeControlPageReqVO) => {
    return await request.get({ url: '/fire/smoke/system/page', params })
  },

  // 查询防排烟系统详情
  getSmokeControlSystem: async (id: number) => {
    return await request.get({ url: '/fire/smoke/system/get?id=' + id })
  },

  // 新增防排烟系统
  createSmokeControlSystem: async (data: SmokeControlSystemVO) => {
    return await request.post({ url: '/fire/smoke/system/create', data })
  },

  // 修改防排烟系统
  updateSmokeControlSystem: async (data: SmokeControlSystemVO) => {
    return await request.put({ url: '/fire/smoke/system/update', data })
  },

  // 删除防排烟系统
  deleteSmokeControlSystem: async (id: number) => {
    return await request.delete({ url: '/fire/smoke/system/delete?id=' + id })
  },

  // 查询烟雾检测分页
  getSmokeDetectionPage: async (params: SmokeControlPageReqVO) => {
    return await request.get({ url: '/fire/smoke/detection/page', params })
  },

  // 查询烟雾检测详情
  getSmokeDetection: async (id: number) => {
    return await request.get({ url: '/fire/smoke/detection/get?id=' + id })
  },

  // 新增烟雾检测
  createSmokeDetection: async (data: SmokeDetectionVO) => {
    return await request.post({ url: '/fire/smoke/detection/create', data })
  },

  // 修改烟雾检测
  updateSmokeDetection: async (data: SmokeDetectionVO) => {
    return await request.put({ url: '/fire/smoke/detection/update', data })
  },

  // 删除烟雾检测
  deleteSmokeDetection: async (id: number) => {
    return await request.delete({ url: '/fire/smoke/detection/delete?id=' + id })
  },

  // 查询通风控制分页
  getVentilationControlPage: async (params: SmokeControlPageReqVO) => {
    return await request.get({ url: '/fire/smoke/ventilation/page', params })
  },

  // 查询通风控制详情
  getVentilationControl: async (id: number) => {
    return await request.get({ url: '/fire/smoke/ventilation/get?id=' + id })
  },

  // 新增通风控制
  createVentilationControl: async (data: VentilationControlVO) => {
    return await request.post({ url: '/fire/smoke/ventilation/create', data })
  },

  // 修改通风控制
  updateVentilationControl: async (data: VentilationControlVO) => {
    return await request.put({ url: '/fire/smoke/ventilation/update', data })
  },

  // 删除通风控制
  deleteVentilationControl: async (id: number) => {
    return await request.delete({ url: '/fire/smoke/ventilation/delete?id=' + id })
  },

  // 控制通风设备
  controlVentilation: async (data: { id: number; action: string; speed?: number }) => {
    return await request.post({ url: '/fire/smoke/ventilation/control', data })
  },

  // 查询控制记录分页
  getSmokeControlRecordPage: async (params: SmokeControlPageReqVO) => {
    return await request.get({ url: '/fire/smoke/record/page', params })
  },

  // 查询控制记录详情
  getSmokeControlRecord: async (id: number) => {
    return await request.get({ url: '/fire/smoke/record/get?id=' + id })
  },

  // 新增控制记录
  createSmokeControlRecord: async (data: SmokeControlRecordVO) => {
    return await request.post({ url: '/fire/smoke/record/create', data })
  },

  // 批量删除
  deleteSmokeControlSystemBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/smoke/system/delete-batch', data: { ids } })
  },

  deleteSmokeDetectionBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/smoke/detection/delete-batch', data: { ids } })
  },

  deleteVentilationControlBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/smoke/ventilation/delete-batch', data: { ids } })
  },

  // 导出Excel
  exportSmokeControlSystem: async (params: SmokeControlPageReqVO) => {
    return await request.download({ url: '/fire/smoke/system/export-excel', params })
  },

  exportSmokeDetection: async (params: SmokeControlPageReqVO) => {
    return await request.download({ url: '/fire/smoke/detection/export-excel', params })
  },

  exportVentilationControl: async (params: SmokeControlPageReqVO) => {
    return await request.download({ url: '/fire/smoke/ventilation/export-excel', params })
  },

  exportSmokeControlRecord: async (params: SmokeControlPageReqVO) => {
    return await request.download({ url: '/fire/smoke/record/export-excel', params })
  }
}