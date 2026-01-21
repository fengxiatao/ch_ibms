/**
 * 请求封装
 * 说明：统一替换为 '@/config/axios'
 */
import request from '@/config/axios'

export interface SafetyInspectionVO {
  id?: number
  inspectionName: string
  inspectionType: string
  inspectionArea: string
  inspectionDate: Date
  inspector: string
  inspectionItems: string[]
  result: string
  problems?: string[]
  improvements?: string[]
  nextInspectionDate?: Date
  status: number
  description?: string
  images?: string[]
  reportFile?: string
}

export interface SafetyTrainingVO {
  id?: number
  trainingName: string
  trainingType: string
  trainingDate: Date
  trainer: string
  trainees: string[]
  trainingContent: string
  duration: number
  location: string
  result: string
  passRate?: number
  certificate?: boolean
  nextTrainingDate?: Date
  status: number
  materials?: string[]
  images?: string[]
}

export interface EvacuationManagementVO {
  id?: number
  planName: string
  planType: string
  area: string
  evacuationRoutes: string[]
  assemblyPoints: string[]
  responsiblePerson: string
  createDate: Date
  lastReview?: Date
  nextReview?: Date
  status: number
  drillRecords?: EvacuationDrillVO[]
  description?: string
  planFile?: string
}

export interface EvacuationDrillVO {
  id?: number
  planId: number
  drillName: string
  drillDate: Date
  drillType: string
  participants: number
  evacuationTime: number
  targetTime: number
  result: string
  problems?: string[]
  improvements?: string[]
  organizer: string
  description?: string
  images?: string[]
  reportFile?: string
}

export interface SafetyReportVO {
  id?: number
  reportName: string
  reportType: string
  reportPeriod: string
  generateDate: Date
  generator: string
  content: string
  statistics: any
  conclusions: string[]
  recommendations: string[]
  status: number
  reportFile?: string
  attachments?: string[]
}

export interface FireSafetyPageReqVO extends PageParam {
  inspectionName?: string
  inspectionType?: string
  inspector?: string
  inspectionDate?: [Date, Date]
  status?: number
}

// 消防安全 API
export const FireSafetyApi = {
  // 查询安全检查分页
  getSafetyInspectionPage: async (params: FireSafetyPageReqVO) => {
    return await request.get({ url: '/fire/safety/inspection/page', params })
  },

  // 查询安全检查详情
  getSafetyInspection: async (id: number) => {
    return await request.get({ url: '/fire/safety/inspection/get?id=' + id })
  },

  // 新增安全检查
  createSafetyInspection: async (data: SafetyInspectionVO) => {
    return await request.post({ url: '/fire/safety/inspection/create', data })
  },

  // 修改安全检查
  updateSafetyInspection: async (data: SafetyInspectionVO) => {
    return await request.put({ url: '/fire/safety/inspection/update', data })
  },

  // 删除安全检查
  deleteSafetyInspection: async (id: number) => {
    return await request.delete({ url: '/fire/safety/inspection/delete?id=' + id })
  },

  // 查询安全培训分页
  getSafetyTrainingPage: async (params: FireSafetyPageReqVO) => {
    return await request.get({ url: '/fire/safety/training/page', params })
  },

  // 查询安全培训详情
  getSafetyTraining: async (id: number) => {
    return await request.get({ url: '/fire/safety/training/get?id=' + id })
  },

  // 新增安全培训
  createSafetyTraining: async (data: SafetyTrainingVO) => {
    return await request.post({ url: '/fire/safety/training/create', data })
  },

  // 修改安全培训
  updateSafetyTraining: async (data: SafetyTrainingVO) => {
    return await request.put({ url: '/fire/safety/training/update', data })
  },

  // 删除安全培训
  deleteSafetyTraining: async (id: number) => {
    return await request.delete({ url: '/fire/safety/training/delete?id=' + id })
  },

  // 查询疏散管理分页
  getEvacuationManagementPage: async (params: FireSafetyPageReqVO) => {
    return await request.get({ url: '/fire/safety/evacuation/page', params })
  },

  // 查询疏散管理详情
  getEvacuationManagement: async (id: number) => {
    return await request.get({ url: '/fire/safety/evacuation/get?id=' + id })
  },

  // 新增疏散管理
  createEvacuationManagement: async (data: EvacuationManagementVO) => {
    return await request.post({ url: '/fire/safety/evacuation/create', data })
  },

  // 修改疏散管理
  updateEvacuationManagement: async (data: EvacuationManagementVO) => {
    return await request.put({ url: '/fire/safety/evacuation/update', data })
  },

  // 删除疏散管理
  deleteEvacuationManagement: async (id: number) => {
    return await request.delete({ url: '/fire/safety/evacuation/delete?id=' + id })
  },

  // 查询疏散演练分页
  getEvacuationDrillPage: async (params: FireSafetyPageReqVO) => {
    return await request.get({ url: '/fire/safety/drill/page', params })
  },

  // 新增疏散演练
  createEvacuationDrill: async (data: EvacuationDrillVO) => {
    return await request.post({ url: '/fire/safety/drill/create', data })
  },

  // 查询安全报告分页
  getSafetyReportPage: async (params: FireSafetyPageReqVO) => {
    return await request.get({ url: '/fire/safety/report/page', params })
  },

  // 查询安全报告详情
  getSafetyReport: async (id: number) => {
    return await request.get({ url: '/fire/safety/report/get?id=' + id })
  },

  // 生成安全报告
  generateSafetyReport: async (data: { reportType: string; startDate: Date; endDate: Date }) => {
    return await request.post({ url: '/fire/safety/report/generate', data })
  },

  // 删除安全报告
  deleteSafetyReport: async (id: number) => {
    return await request.delete({ url: '/fire/safety/report/delete?id=' + id })
  },

  // 批量删除
  deleteSafetyInspectionBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/safety/inspection/delete-batch', data: { ids } })
  },

  deleteSafetyTrainingBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/safety/training/delete-batch', data: { ids } })
  },

  // 导出Excel
  exportSafetyInspection: async (params: FireSafetyPageReqVO) => {
    return await request.download({ url: '/fire/safety/inspection/export-excel', params })
  },

  exportSafetyTraining: async (params: FireSafetyPageReqVO) => {
    return await request.download({ url: '/fire/safety/training/export-excel', params })
  },

  exportSafetyReport: async (params: FireSafetyPageReqVO) => {
    return await request.download({ url: '/fire/safety/report/export-excel', params })
  }
}









