/**
 * 请求封装
 * 说明：替换为 '@/config/axios'
 */
import request from '@/config/axios'

export interface EmergencyPlanVO {
  id?: number
  planName: string
  planType: string
  planLevel: string
  applicableArea: string
  responsibleDepartment: string
  responsiblePerson: string
  createDate: Date
  lastUpdate?: Date
  nextReview?: Date
  status: number
  planContent: string
  procedures: EmergencyProcedureVO[]
  resources: string[]
  contacts: EmergencyContactVO[]
  planFile?: string
  attachments?: string[]
}

export interface EmergencyProcedureVO {
  id?: number
  stepNumber: number
  stepName: string
  description: string
  responsibleRole: string
  timeLimit?: number
  resources?: string[]
  nextStep?: number
}

export interface EmergencyContactVO {
  id?: number
  name: string
  role: string
  department: string
  phone: string
  email?: string
  alternatePhone?: string
  priority: number
}

export interface EmergencyDrillVO {
  id?: number
  drillName: string
  drillType: string
  planId?: number
  planName?: string
  drillDate: Date
  location: string
  organizer: string
  participants: number
  duration: number
  objectives: string[]
  scenarios: string
  result: string
  evaluation: string
  problems?: string[]
  improvements?: string[]
  nextDrillDate?: Date
  status: number
  images?: string[]
  reportFile?: string
}

export interface ResponseTeamVO {
  id?: number
  teamName: string
  teamType: string
  teamLeader: string
  members: TeamMemberVO[]
  responsibilities: string[]
  equipment: string[]
  trainingRecords: string[]
  contactInfo: string
  status: number
  createDate: Date
  lastTraining?: Date
  nextTraining?: Date
  description?: string
}

export interface TeamMemberVO {
  id?: number
  name: string
  role: string
  phone: string
  email?: string
  specialties?: string[]
  certifications?: string[]
  joinDate: Date
  status: number
}

export interface IncidentManagementVO {
  id?: number
  incidentName: string
  incidentType: string
  incidentLevel: string
  location: string
  startTime: Date
  endTime?: Date
  reportPerson: string
  responsePerson?: string
  responseTeam?: string
  description: string
  cause?: string
  impact?: string
  actions: IncidentActionVO[]
  casualties?: number
  propertyLoss?: number
  status: number
  lessons?: string[]
  improvements?: string[]
  reportFile?: string
  images?: string[]
}

export interface IncidentActionVO {
  id?: number
  actionTime: Date
  actionType: string
  actionPerson: string
  description: string
  result?: string
}

export interface EmergencyPageReqVO extends PageParam {
  planName?: string
  planType?: string
  planLevel?: string
  responsiblePerson?: string
  createDate?: [Date, Date]
  status?: number
}

// 应急响应 API
export const EmergencyResponseApi = {
  // 查询应急预案分页
  getEmergencyPlanPage: async (params: EmergencyPageReqVO) => {
    return await request.get({ url: '/fire/emergency/plan/page', params })
  },

  // 查询应急预案详情
  getEmergencyPlan: async (id: number) => {
    return await request.get({ url: '/fire/emergency/plan/get?id=' + id })
  },

  // 新增应急预案
  createEmergencyPlan: async (data: EmergencyPlanVO) => {
    return await request.post({ url: '/fire/emergency/plan/create', data })
  },

  // 修改应急预案
  updateEmergencyPlan: async (data: EmergencyPlanVO) => {
    return await request.put({ url: '/fire/emergency/plan/update', data })
  },

  // 删除应急预案
  deleteEmergencyPlan: async (id: number) => {
    return await request.delete({ url: '/fire/emergency/plan/delete?id=' + id })
  },

  // 激活应急预案
  activateEmergencyPlan: async (id: number) => {
    return await request.post({ url: '/fire/emergency/plan/activate?id=' + id })
  },

  // 查询应急演练分页
  getEmergencyDrillPage: async (params: EmergencyPageReqVO) => {
    return await request.get({ url: '/fire/emergency/drill/page', params })
  },

  // 查询应急演练详情
  getEmergencyDrill: async (id: number) => {
    return await request.get({ url: '/fire/emergency/drill/get?id=' + id })
  },

  // 新增应急演练
  createEmergencyDrill: async (data: EmergencyDrillVO) => {
    return await request.post({ url: '/fire/emergency/drill/create', data })
  },

  // 修改应急演练
  updateEmergencyDrill: async (data: EmergencyDrillVO) => {
    return await request.put({ url: '/fire/emergency/drill/update', data })
  },

  // 删除应急演练
  deleteEmergencyDrill: async (id: number) => {
    return await request.delete({ url: '/fire/emergency/drill/delete?id=' + id })
  },

  // 查询响应团队分页
  getResponseTeamPage: async (params: EmergencyPageReqVO) => {
    return await request.get({ url: '/fire/emergency/team/page', params })
  },

  // 查询响应团队详情
  getResponseTeam: async (id: number) => {
    return await request.get({ url: '/fire/emergency/team/get?id=' + id })
  },

  // 新增响应团队
  createResponseTeam: async (data: ResponseTeamVO) => {
    return await request.post({ url: '/fire/emergency/team/create', data })
  },

  // 修改响应团队
  updateResponseTeam: async (data: ResponseTeamVO) => {
    return await request.put({ url: '/fire/emergency/team/update', data })
  },

  // 删除响应团队
  deleteResponseTeam: async (id: number) => {
    return await request.delete({ url: '/fire/emergency/team/delete?id=' + id })
  },

  // 查询事件管理分页
  getIncidentManagementPage: async (params: EmergencyPageReqVO) => {
    return await request.get({ url: '/fire/emergency/incident/page', params })
  },

  // 查询事件管理详情
  getIncidentManagement: async (id: number) => {
    return await request.get({ url: '/fire/emergency/incident/get?id=' + id })
  },

  // 新增事件管理
  createIncidentManagement: async (data: IncidentManagementVO) => {
    return await request.post({ url: '/fire/emergency/incident/create', data })
  },

  // 修改事件管理
  updateIncidentManagement: async (data: IncidentManagementVO) => {
    return await request.put({ url: '/fire/emergency/incident/update', data })
  },

  // 删除事件管理
  deleteIncidentManagement: async (id: number) => {
    return await request.delete({ url: '/fire/emergency/incident/delete?id=' + id })
  },

  // 添加事件处理记录
  addIncidentAction: async (data: IncidentActionVO) => {
    return await request.post({ url: '/fire/emergency/incident/add-action', data })
  },

  // 结束事件处理
  closeIncident: async (data: { id: number; summary: string; lessons: string[] }) => {
    return await request.post({ url: '/fire/emergency/incident/close', data })
  },

  // 批量删除
  deleteEmergencyPlanBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/emergency/plan/delete-batch', data: { ids } })
  },

  deleteEmergencyDrillBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/emergency/drill/delete-batch', data: { ids } })
  },

  deleteResponseTeamBatch: async (ids: number[]) => {
    return await request.delete({ url: '/fire/emergency/team/delete-batch', data: { ids } })
  },

  // 导出Excel
  exportEmergencyPlan: async (params: EmergencyPageReqVO) => {
    return await request.download({ url: '/fire/emergency/plan/export-excel', params })
  },

  exportEmergencyDrill: async (params: EmergencyPageReqVO) => {
    return await request.download({ url: '/fire/emergency/drill/export-excel', params })
  },

  exportResponseTeam: async (params: EmergencyPageReqVO) => {
    return await request.download({ url: '/fire/emergency/team/export-excel', params })
  },

  exportIncidentManagement: async (params: EmergencyPageReqVO) => {
    return await request.download({ url: '/fire/emergency/incident/export-excel', params })
  }
}









