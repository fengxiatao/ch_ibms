/**
 * 请求封装
 * 说明：使用 '@/config/axios' 作为统一请求入口
 */
import request from '@/config/axios'

export interface FireAlarmDeviceVO {
  id?: number
  deviceName: string
  deviceCode: string
  deviceType: string
  location: string
  floor: string
  area: string
  status: number
  installDate: Date
  lastMaintenance?: Date
  nextMaintenance?: Date
  manufacturer?: string
  model?: string
  description?: string
}

export interface FireAlarmRecordVO {
  id?: number
  deviceId: number
  deviceName?: string
  alarmType: string
  alarmLevel: number
  alarmTime: Date
  alarmValue?: number
  threshold?: number
  status: number
  handleTime?: Date
  handler?: string
  handleMethod?: string
  description?: string
  images?: string[]
}

export interface FireAlarmSettingsVO {
  id?: number
  deviceId: number
  deviceName?: string
  alarmType: string
  threshold: number
  warningThreshold: number
  enabled: boolean
  notificationEnabled: boolean
  emailNotification: boolean
  smsNotification: boolean
  description?: string
}

export interface FireAlarmPageReqVO extends PageParam {
  deviceName?: string
  deviceType?: string
  location?: string
  status?: number
  alarmTime?: [Date, Date]
}

// 火灾报警监控 API
export const FireAlarmApi = {
  // 获取报警监控数据
  getAlarmMonitoringData: async () => {
    return await request.get({ url: '/fire/alarm/monitoring' })
  },

  // 查询报警设备分页
  getAlarmDevicePage: async (params: FireAlarmPageReqVO) => {
    return await request.get({ url: '/fire/alarm/device/page', params })
  },

  // 查询报警设备详情
  getAlarmDevice: async (id: number) => {
    return await request.get({ url: '/fire/alarm/device/get?id=' + id })
  },

  // 新增报警设备
  createAlarmDevice: async (data: FireAlarmDeviceVO) => {
    return await request.post({ url: '/fire/alarm/device/create', data })
  },

  // 修改报警设备
  updateAlarmDevice: async (data: FireAlarmDeviceVO) => {
    return await request.put({ url: '/fire/alarm/device/update', data })
  },

  // 删除报警设备
  deleteAlarmDevice: async (id: number) => {
    return await request.delete({ url: '/fire/alarm/device/delete?id=' + id })
  },

  // 查询报警记录分页
  getAlarmRecordPage: async (params: FireAlarmPageReqVO) => {
    return await request.get({ url: '/fire/alarm/record/page', params })
  },

  // 查询报警记录详情
  getAlarmRecord: async (id: number) => {
    return await request.get({ url: '/fire/alarm/record/get?id=' + id })
  },

  // 处理报警记录
  handleAlarmRecord: async (data: { id: number; handleMethod: string; description: string }) => {
    return await request.post({ url: '/fire/alarm/record/handle', data })
  },

  // 查询报警设置分页
  getAlarmSettingsPage: async (params: FireAlarmPageReqVO) => {
    return await request.get({ url: '/fire/alarm/settings/page', params })
  },

  // 查询报警设置详情
  getAlarmSettings: async (id: number) => {
    return await request.get({ url: '/fire/alarm/settings/get?id=' + id })
  },

  // 新增报警设置
  createAlarmSettings: async (data: FireAlarmSettingsVO) => {
    return await request.post({ url: '/fire/alarm/settings/create', data })
  },

  // 修改报警设置
  updateAlarmSettings: async (data: FireAlarmSettingsVO) => {
    return await request.put({ url: '/fire/alarm/settings/update', data })
  },

  // 删除报警设置
  deleteAlarmSettings: async (id: number) => {
    return await request.delete({ url: '/fire/alarm/settings/delete?id=' + id })
  },

  // 批量删除
  deleteAlarmDeviceBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/alarm/device/delete-batch', data: { ids } })
  },

  // 导出Excel
  exportAlarmDevice: async (params: FireAlarmPageReqVO) => {
    return await request.download({ url: '/fire/alarm/device/export-excel', params })
  },

  exportAlarmRecord: async (params: FireAlarmPageReqVO) => {
    return await request.download({ url: '/fire/alarm/record/export-excel', params })
  }
}









