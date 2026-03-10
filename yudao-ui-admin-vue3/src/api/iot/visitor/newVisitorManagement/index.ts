import request from '@/config/axios'

export interface VisitorAppointmentPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  phone?: string
  type?: string
  status?: string
  visitTime?: [string, string]
}

export interface VisitorDoorRecordPageReqVO {
  pageNo: number
  pageSize: number
  appointmentId?: number
}

export interface VisitorDoorRecordVO {
  id: number
  channelName: string
  method: string
  openTime: string
}

export interface VisitorAuthDeviceVO {
  id: number
  name: string
}

export interface VisitorAppointmentVO {
  id: number
  name: string
  phone: string
  type: string
  company?: string
  host: string
  hostDept?: string
  visitTime: string
  reason: string
  areas: string[]
  idCard?: string
  carNo?: string
  remark?: string
  status: string
  approvalComment?: string
  approvalTime?: string
  signInTime?: string
  signOutTime?: string
  currentLocation?: string
  rating?: number
}

export interface VisitorAppointmentCreateReqVO {
  name: string
  phone: string
  type: string
  company?: string
  host: string
  hostDept?: string
  visitTime: string
  reason: string
  areas: string[]
  idCard?: string
  carNo?: string
  remark?: string
}

export const NewVisitorManagementApi = {
  // 统计卡片
  getStats: async () => {
    return await request.get({ url: '/iot/visitor/overview/stats' })
  },
  // 仪表盘（图表/排行）
  getDashboard: async (params?: { dateFrom?: string; dateTo?: string }) => {
    return await request.get({ url: '/iot/visitor/overview/dashboard', params })
  },
  // 今日在访分页
  getTodayPage: async (params: VisitorAppointmentPageReqVO) => {
    return await request.get({ url: '/iot/visitor/overview/today/page', params })
  },
  // 预约分页
  getAppointmentPage: async (params: VisitorAppointmentPageReqVO) => {
    return await request.get({ url: '/iot/visitor/appointment/page', params })
  },
  // 来访记录分页
  getHistoryPage: async (params: VisitorAppointmentPageReqVO) => {
    return await request.get({ url: '/iot/visitor/overview/history/page', params })
  },
  // 异常监控分页
  getAbnormalPage: async (params: { pageNo: number; pageSize: number; abnormalType?: string; riskLevel?: string }) => {
    return await request.get({ url: '/iot/visitor/overview/abnormal/page', params })
  },
  // 开门记录分页
  getDoorRecordPage: async (params: VisitorDoorRecordPageReqVO) => {
    return await request.get({ url: '/iot/visitor/door-record/page', params })
  },
  // 门禁设备列表（下发权限用）
  getAuthDevices: async () => {
    return await request.get({ url: '/iot/visitor/auth-devices' })
  },
  // 创建预约
  createAppointment: async (data: VisitorAppointmentCreateReqVO) => {
    return await request.post({ url: '/iot/visitor/appointment/create', data })
  },
  // 审批
  approveAppointment: async (id: number, action: 'approve' | 'reject', comment: string) => {
    return await request.post({
      url: '/iot/visitor/appointment/approve',
      data: { id, action, comment }
    })
  },
  // 签离
  signOut: async (id: number) => {
    return await request.post({ url: '/iot/visitor/appointment/sign-out', params: { id } })
  }
}

