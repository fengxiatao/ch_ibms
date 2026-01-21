/**
 * 请求封装
 * 说明：统一使用 '@/config/axios'，替换错误的 '@/utils/request' 引用
 */
import request from '@/config/axios'

export interface SuppressionSystemVO {
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
  pressure?: number
  description?: string
}

export interface SuppressionDeviceVO {
  id?: number
  deviceName: string
  deviceCode: string
  deviceType: string
  systemId?: number
  systemName?: string
  location: string
  status: number
  installDate: Date
  lastMaintenance?: Date
  nextMaintenance?: Date
  manufacturer?: string
  model?: string
  specifications?: string
  description?: string
}

export interface SuppressionRecordVO {
  id?: number
  systemId: number
  systemName?: string
  recordType: string
  triggerTime: Date
  endTime?: Date
  triggerReason: string
  suppressionResult: string
  damageAssessment?: string
  operator?: string
  description?: string
  images?: string[]
}

export interface MaintenanceRecordVO {
  id?: number
  deviceId: number
  deviceName?: string
  maintenanceType: string
  maintenanceDate: Date
  nextMaintenanceDate?: Date
  maintainer: string
  maintenanceContent: string
  result: string
  cost?: number
  parts?: string
  description?: string
  images?: string[]
}

export interface SuppressionPageReqVO extends PageParam {
  systemName?: string
  systemType?: string
  location?: string
  status?: number
  installDate?: [Date, Date]
}

// 消防灭火 API
export const FireSuppressionApi = {
  // 查询灭火系统分页
  getSuppressionSystemPage: async (params: SuppressionPageReqVO) => {
    return await request.get({ url: '/fire/suppression/system/page', params })
  },

  // 查询灭火系统详情
  getSuppressionSystem: async (id: number) => {
    return await request.get({ url: '/fire/suppression/system/get?id=' + id })
  },

  // 新增灭火系统
  createSuppressionSystem: async (data: SuppressionSystemVO) => {
    return await request.post({ url: '/fire/suppression/system/create', data })
  },

  // 修改灭火系统
  updateSuppressionSystem: async (data: SuppressionSystemVO) => {
    return await request.put({ url: '/fire/suppression/system/update', data })
  },

  // 删除灭火系统
  deleteSuppressionSystem: async (id: number) => {
    return await request.delete({ url: '/fire/suppression/system/delete?id=' + id })
  },

  // 查询灭火设备分页
  getSuppressionDevicePage: async (params: SuppressionPageReqVO) => {
    return await request.get({ url: '/fire/suppression/device/page', params })
  },

  // 查询灭火设备详情
  getSuppressionDevice: async (id: number) => {
    return await request.get({ url: '/fire/suppression/device/get?id=' + id })
  },

  // 新增灭火设备
  createSuppressionDevice: async (data: SuppressionDeviceVO) => {
    return await request.post({ url: '/fire/suppression/device/create', data })
  },

  // 修改灭火设备
  updateSuppressionDevice: async (data: SuppressionDeviceVO) => {
    return await request.put({ url: '/fire/suppression/device/update', data })
  },

  // 删除灭火设备
  deleteSuppressionDevice: async (id: number) => {
    return await request.delete({ url: '/fire/suppression/device/delete?id=' + id })
  },

  // 查询灭火记录分页
  getSuppressionRecordPage: async (params: SuppressionPageReqVO) => {
    return await request.get({ url: '/fire/suppression/record/page', params })
  },

  // 查询灭火记录详情
  getSuppressionRecord: async (id: number) => {
    return await request.get({ url: '/fire/suppression/record/get?id=' + id })
  },

  // 新增灭火记录
  createSuppressionRecord: async (data: SuppressionRecordVO) => {
    return await request.post({ url: '/fire/suppression/record/create', data })
  },

  // 查询维护保养分页
  getMaintenanceRecordPage: async (params: SuppressionPageReqVO) => {
    return await request.get({ url: '/fire/suppression/maintenance/page', params })
  },

  // 查询维护保养详情
  getMaintenanceRecord: async (id: number) => {
    return await request.get({ url: '/fire/suppression/maintenance/get?id=' + id })
  },

  // 新增维护保养
  createMaintenanceRecord: async (data: MaintenanceRecordVO) => {
    return await request.post({ url: '/fire/suppression/maintenance/create', data })
  },

  // 修改维护保养
  updateMaintenanceRecord: async (data: MaintenanceRecordVO) => {
    return await request.put({ url: '/fire/suppression/maintenance/update', data })
  },

  // 删除维护保养
  deleteMaintenanceRecord: async (id: number) => {
    return await request.delete({ url: '/fire/suppression/maintenance/delete?id=' + id })
  },

  // 批量删除
  deleteSuppressionSystemBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/suppression/system/delete-batch', data: { ids } })
  },

  deleteSuppressionDeviceBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/suppression/device/delete-batch', data: { ids } })
  },

  // 导出Excel
  exportSuppressionSystem: async (params: SuppressionPageReqVO) => {
    return await request.download({ url: '/fire/suppression/system/export-excel', params })
  },

  exportSuppressionDevice: async (params: SuppressionPageReqVO) => {
    return await request.download({ url: '/fire/suppression/device/export-excel', params })
  },

  exportMaintenanceRecord: async (params: SuppressionPageReqVO) => {
    return await request.download({ url: '/fire/suppression/maintenance/export-excel', params })
  }
}









