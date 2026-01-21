import request from '@/config/axios'

// ==================== 访客信息 ====================

export interface VisitorVO {
  id: number
  visitorCode: string
  visitorName: string
  gender: number
  phone: string
  idCard: string
  company: string
  faceUrl: string
  remark: string
  createTime?: Date
}

// ==================== 访客申请 ====================

export interface VisitorApplyVO {
  id: number
  applyCode: string
  visitorId: number
  visitorCode: string
  visitorName: string
  visitorPhone: string
  idCard: string
  company: string
  faceUrl: string
  visiteeId: number
  visiteeName: string
  visiteeDeptId: number
  visiteeDeptName: string
  visitReason: string
  visitStatus: number
  visitStatusName: string
  planVisitTime: Date
  planLeaveTime: Date
  actualVisitTime: Date
  actualLeaveTime: Date
  applyTime: Date
  approveStatus: number
  approveStatusName: string
  approveTime: Date
  approverName: string
  approveRemark: string
  // 授权信息
  authStatus: number
  authStatusName: string
  cardNo: string
  timeTemplateId: number
  timeTemplateName: string
  authType: number
  authTypeName: string
  authStartTime: Date
  authEndTime: Date
  maxAccessCount: number
  usedAccessCount: number
  dailyAccessLimit: number
  dailyUsedCount: number
  authDevices: AuthDeviceVO[]
  remark: string
  createTime: Date
}

export interface AuthDeviceVO {
  deviceId: number
  deviceName: string
  channelId: number
  channelName: string
  dispatchStatus: number
  dispatchResult: string
}

export interface VisitorApplyCreateReqVO {
  // 访客信息
  visitorId?: number
  visitorName: string
  gender?: number
  visitorPhone: string
  idCard?: string
  company?: string
  // 被访人信息
  visiteeId: number
  // 来访信息
  visitReason: string
  planVisitTime: Date
  planLeaveTime?: Date
  // 授权信息
  cardNo?: string
  faceUrl?: string
  timeTemplateId?: number
  authType?: number
  authStartTime?: Date
  authEndTime?: Date
  maxAccessCount?: number
  dailyAccessLimit?: number
  deviceIds?: number[]
  channelIds?: number[]
  remark?: string
}

export interface VisitorApplyPageReqVO {
  pageNo: number
  pageSize: number
  visitorName?: string
  visiteeName?: string
  visitReason?: string
  visitStatus?: number
  approveStatus?: number
  visitTimeStart?: Date
  visitTimeEnd?: Date
}

export interface VisitorAuthDispatchReqVO {
  applyId: number
  cardNo?: string
  faceUrl?: string
  timeTemplateId?: number
  authType?: number
  authStartTime?: Date
  authEndTime?: Date
  maxAccessCount?: number
  dailyAccessLimit?: number
  deviceIds: number[]
  channelIds?: number[]
}

export interface VisitorStatisticsVO {
  currentVisitingCount: number
  todayVisitorCount: number
  totalVisitorCount: number
  pendingApproveCount: number
  pendingDispatchCount: number
}

// ==================== 选项常量 ====================

// 来访状态选项
export const VisitStatusOptions = [
  { value: 0, label: '待访', type: 'info' },
  { value: 1, label: '在访', type: 'success' },
  { value: 2, label: '离访', type: 'default' },
  { value: 3, label: '已取消', type: 'danger' }
]

// 审批状态选项
export const ApproveStatusOptions = [
  { value: 0, label: '待审批', type: 'warning' },
  { value: 1, label: '已通过', type: 'success' },
  { value: 2, label: '已拒绝', type: 'danger' }
]

// 授权状态选项
export const VisitorAuthStatusOptions = [
  { value: 0, label: '待下发', type: 'info' },
  { value: 1, label: '下发中', type: 'primary' },
  { value: 2, label: '已下发', type: 'success' },
  { value: 3, label: '已回收', type: 'default' },
  { value: 4, label: '下发失败', type: 'danger' }
]

// 授权类型选项
export const AuthTypeOptions = [
  { value: 1, label: '按时间段', type: 'primary' },
  { value: 2, label: '按次数', type: 'success' },
  { value: 3, label: '时间+次数', type: 'warning' }
]

// 性别选项
export const GenderOptions = [
  { value: 0, label: '未知' },
  { value: 1, label: '男' },
  { value: 2, label: '女' }
]

// ==================== API ====================

export const VisitorApplyApi = {
  // 创建访客申请
  createApply: async (data: VisitorApplyCreateReqVO) => {
    return await request.post({ url: '/iot/visitor/apply/create', data })
  },

  // 更新访客申请
  updateApply: async (id: number, data: VisitorApplyCreateReqVO) => {
    return await request.put({ url: '/iot/visitor/apply/update', params: { id }, data })
  },

  // 删除访客申请
  deleteApply: async (id: number) => {
    return await request.delete({ url: '/iot/visitor/apply/delete', params: { id } })
  },

  // 获取访客申请详情
  getApply: async (id: number): Promise<VisitorApplyVO> => {
    return await request.get({ url: '/iot/visitor/apply/get', params: { id } })
  },

  // 获取访客申请分页
  getApplyPage: async (params: VisitorApplyPageReqVO) => {
    return await request.get({ url: '/iot/visitor/apply/page', params })
  },

  // 审批通过
  approve: async (id: number, remark?: string) => {
    return await request.post({ url: '/iot/visitor/apply/approve', params: { id, remark } })
  },

  // 审批拒绝
  reject: async (id: number, remark?: string) => {
    return await request.post({ url: '/iot/visitor/apply/reject', params: { id, remark } })
  },

  // 取消申请
  cancel: async (id: number, reason?: string) => {
    return await request.post({ url: '/iot/visitor/apply/cancel', params: { id, reason } })
  },

  // 访客签到
  checkIn: async (id: number) => {
    return await request.post({ url: '/iot/visitor/apply/check-in', params: { id } })
  },

  // 访客签离
  checkOut: async (id: number) => {
    return await request.post({ url: '/iot/visitor/apply/check-out', params: { id } })
  },

  // 下发权限
  dispatchAuth: async (data: VisitorAuthDispatchReqVO) => {
    return await request.post({ url: '/iot/visitor/apply/dispatch-auth', data })
  },

  // 回收权限
  revokeAuth: async (applyId: number) => {
    return await request.post({ url: '/iot/visitor/apply/revoke-auth', params: { applyId } })
  },

  // 获取访客统计
  getStatistics: async (): Promise<VisitorStatisticsVO> => {
    return await request.get({ url: '/iot/visitor/apply/statistics' })
  }
}
