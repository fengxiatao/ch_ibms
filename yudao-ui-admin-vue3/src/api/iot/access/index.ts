import request from '@/config/axios'

// ==================== 组织架构管理 ====================

export interface AccessDepartmentVO {
  id: number
  parentId: number
  deptName: string
  deptCode: string
  sort: number
  leader: string
  phone: string
  status: number
  children?: AccessDepartmentVO[]
  createTime?: Date
}

export interface AccessDepartmentCreateReqVO {
  parentId: number
  deptName: string
  deptCode?: string
  sort?: number
  leader?: string
  phone?: string
  status?: number
}

export interface AccessDepartmentUpdateReqVO extends AccessDepartmentCreateReqVO {
  id: number
}

export const AccessDepartmentApi = {
  // 创建部门
  createDepartment: async (data: AccessDepartmentCreateReqVO) => {
    return await request.post({ url: '/iot/access/department/create', data })
  },
  // 更新部门
  updateDepartment: async (data: AccessDepartmentUpdateReqVO) => {
    return await request.put({ url: '/iot/access/department/update', data })
  },
  // 删除部门
  deleteDepartment: async (id: number) => {
    return await request.delete({ url: '/iot/access/department/delete', params: { id } })
  },
  // 获取部门详情
  getDepartment: async (id: number) => {
    return await request.get({ url: '/iot/access/department/get', params: { id } })
  },
  // 获取组织树
  getDepartmentTree: async () => {
    return await request.get({ url: '/iot/access/department/tree' })
  },
  // 获取部门列表
  getDepartmentList: async (name?: string) => {
    return await request.get({ url: '/iot/access/department/list', params: { name } })
  }
}

// ==================== 人员管理 ====================

export interface AccessPersonVO {
  id: number
  personCode: string
  personName: string
  personType: number
  deptId: number
  deptName?: string
  idCard: string
  phone: string
  email: string
  faceUrl: string
  validStart: Date
  validEnd: Date
  status: number
  fingerprintCount?: number  // 指纹数量
  createTime?: Date
}

export interface AccessPersonCreateReqVO {
  personCode: string
  personName: string
  personType?: number
  deptId: number
  idCard?: string
  phone?: string
  email?: string
  faceUrl?: string
  validStart?: Date
  validEnd?: Date
  status?: number
}

// 批量创建人员请求VO - Requirements 6.1
export interface BatchCreatePersonItemVO {
  personCode: string
  personName: string
  personType?: number
  deptId: number
  validStart?: Date
  validEnd?: Date
  cardNo?: string
}

export interface BatchCreatePersonReqVO {
  persons: BatchCreatePersonItemVO[]
}

// 批量创建人员响应VO - Requirements 6.3
export interface BatchCreatePersonErrorVO {
  personCode: string
  message: string
}

export interface BatchCreatePersonRespVO {
  successCount: number
  failCount: number
  createdIds: number[]
  errors?: BatchCreatePersonErrorVO[]
}


export interface AccessPersonUpdateReqVO extends AccessPersonCreateReqVO {
  id: number
}

export interface AccessPersonPageReqVO {
  pageNo: number
  pageSize: number
  personCode?: string
  personName?: string
  deptId?: number
  status?: number
}

// 人员凭证VO - SmartPSS认证Tab
export interface AccessPersonCredentialVO {
  id: number
  personId: number
  credentialType: string  // PASSWORD, CARD, FINGERPRINT, FACE
  credentialData?: string
  cardNo?: string
  issueTime?: Date        // 发卡时间
  replaceTime?: Date      // 换卡时间
  cardStatus?: number     // 0-正常, 1-挂失, 2-注销
  oldCardNo?: string      // 旧卡号
  fingerIndex?: number    // 指纹序号
  fingerName?: string     // 指纹名称
  deviceSynced?: boolean
  syncTime?: Date
  status?: number
  createTime?: Date
}

// 卡状态选项 - SmartPSS认证Tab (使用下方 AccessCardStatusOptions)

// 人员类型选项
export const AccessPersonTypeOptions = [
  { value: 1, label: '员工' },
  { value: 2, label: '访客' },
  { value: 3, label: '临时人员' },
  { value: 4, label: 'VIP' }
]

// 人员状态选项
export const AccessPersonStatusOptions = [
  { value: 0, label: '禁用', type: 'danger' },
  { value: 1, label: '正常', type: 'success' }
]

export const AccessPersonApi = {
  // 创建人员
  createPerson: async (data: AccessPersonCreateReqVO) => {
    return await request.post({ url: '/iot/access/person/create', data })
  },
  // 更新人员
  updatePerson: async (data: AccessPersonUpdateReqVO) => {
    return await request.put({ url: '/iot/access/person/update', data })
  },
  // 删除人员
  deletePerson: async (id: number) => {
    return await request.delete({ url: '/iot/access/person/delete', params: { id } })
  },
  // 获取人员详情
  getPerson: async (id: number) => {
    return await request.get({ url: '/iot/access/person/get', params: { id } })
  },
  // 获取人员分页
  getPersonPage: async (params: AccessPersonPageReqVO) => {
    return await request.get({ url: '/iot/access/person/page', params })
  },
  // 批量导入人员
  importPersons: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return await request.post({ url: '/iot/access/person/import', data: formData })
  },
  // 导出模板
  exportTemplate: async () => {
    return await request.download({ url: '/iot/access/person/export-template' })
  },
  // 设置密码
  setPassword: async (personId: number, password: string) => {
    return await request.post({ url: '/iot/access/person/set-password', data: { personId, password } })
  },
  // 添加卡片
  addCard: async (personId: number, cardNo: string) => {
    return await request.post({ url: '/iot/access/person/add-card', data: { personId, cardNo } })
  },
  // 录入指纹
  addFingerprint: async (personId: number, fingerprintData: string) => {
    return await request.post({ url: '/iot/access/person/add-fingerprint', data: { personId, fingerprintData } })
  },
  // 录入人脸
  addFace: async (personId: number, faceData: string) => {
    return await request.post({ url: '/iot/access/person/add-face', data: { personId, faceData } })
  },
  // 删除凭证
  removeCredential: async (personId: number, credentialType: string, credentialData?: string, fingerIndex?: number) => {
    return await request.delete({ 
      url: '/iot/access/person/remove-credential', 
      data: { personId, credentialType, credentialData, fingerIndex } 
    })
  },
  // 批量创建人员 - Requirements 6.1
  batchCreate: async (data: BatchCreatePersonReqVO): Promise<BatchCreatePersonRespVO> => {
    return await request.post({ url: '/iot/access/person/batch-create', data })
  },
  // 冻结人员 - SmartPSS功能对齐
  freezePerson: async (id: number) => {
    return await request.put({ url: '/iot/access/person/freeze', params: { id } })
  },
  // 解冻人员 - SmartPSS功能对齐
  unfreezePerson: async (id: number) => {
    return await request.put({ url: '/iot/access/person/unfreeze', params: { id } })
  },
  // 获取人员凭证列表 - SmartPSS认证Tab
  getCredentials: async (personId: number) => {
    return await request.get({ url: `/iot/access/person/credentials/${personId}` })
  },
  // 卡片挂失 - SmartPSS认证Tab
  reportCardLost: async (credentialId: number) => {
    return await request.put({ url: '/iot/access/person/card/lost', params: { credentialId } })
  },
  // 卡片解挂 - SmartPSS认证Tab
  cancelCardLost: async (credentialId: number) => {
    return await request.put({ url: '/iot/access/person/card/unlost', params: { credentialId } })
  },
  // 换卡 - SmartPSS认证Tab
  replaceCard: async (credentialId: number, newCardNo: string) => {
    return await request.put({ url: '/iot/access/person/card/replace', params: { credentialId, newCardNo } })
  }
}

// ==================== 权限组管理 ====================

export interface AccessPermissionGroupVO {
  id: number
  groupName: string
  timeTemplateId: number
  timeTemplateName?: string
  authMode: string  // 逗号分隔的认证方式字符串
  authModes?: string[]  // 认证方式数组（前端使用）
  description: string
  status: number
  deviceCount?: number
  personCount?: number
  createTime?: Date
  devices?: {
    deviceId: number
    deviceName: string
    deviceIp?: string
    channelId: number
    channelNo?: number
    channelName: string
  }[]
}

export interface AccessPermissionGroupCreateReqVO {
  groupName: string
  timeTemplateId?: number
  authMode?: string  // 逗号分隔的认证方式字符串
  authModes?: string[]  // 认证方式数组（前端使用，会自动转换为authMode）
  description?: string
  status?: number
  deviceIds?: number[]
  channelIds?: number[]
}

export interface AccessPermissionGroupUpdateReqVO extends AccessPermissionGroupCreateReqVO {
  id: number
}

export interface AccessPermissionGroupPageReqVO {
  pageNo: number
  pageSize: number
  groupName?: string
  status?: number
}

// 认证方式选项
export const AccessAuthModeOptions = [
  { value: 1, label: '刷卡' },
  { value: 2, label: '密码' },
  { value: 3, label: '指纹' },
  { value: 4, label: '人脸' },
  { value: 5, label: '刷卡+密码' },
  { value: 6, label: '刷卡+指纹' },
  { value: 7, label: '刷卡+人脸' }
]

export const AccessPermissionGroupApi = {
  // 创建权限组
  createGroup: async (data: AccessPermissionGroupCreateReqVO) => {
    return await request.post({ url: '/iot/access/permission-group/create', data })
  },
  // 更新权限组
  updateGroup: async (data: AccessPermissionGroupUpdateReqVO) => {
    return await request.put({ url: '/iot/access/permission-group/update', data })
  },
  // 删除权限组
  deleteGroup: async (id: number) => {
    return await request.delete({ url: '/iot/access/permission-group/delete', params: { id } })
  },
  // 获取权限组详情
  getGroup: async (id: number) => {
    return await request.get({ url: '/iot/access/permission-group/get', params: { id } })
  },
  // 获取权限组分页
  getGroupPage: async (params: AccessPermissionGroupPageReqVO) => {
    return await request.get({ url: '/iot/access/permission-group/page', params })
  },
  // 获取权限组关联的设备列表
  getGroupDevices: async (groupId: number) => {
    return await request.get({ url: '/iot/access/permission-group/devices', params: { groupId } })
  },
  // 获取权限组关联的人员列表
  getGroupPersons: async (groupId: number) => {
    return await request.get({ url: '/iot/access/permission-group/persons', params: { groupId } })
  },
  // 添加人员到权限组（不触发下发）
  addPersons: async (groupId: number, personIds: number[]) => {
    return await request.post({ url: '/iot/access/permission-group/add-persons', params: { groupId }, data: personIds })
  },
  // 添加人员到权限组并触发授权下发 - Requirements 1.1, 1.4
  addPersonsWithDispatch: async (groupId: number, personIds: number[]): Promise<number> => {
    return await request.post({ url: '/iot/access/permission-group/add-persons-with-dispatch', params: { groupId }, data: personIds })
  },
  // 从权限组移除人员（不触发撤销）
  removePersons: async (groupId: number, personIds: number[]) => {
    return await request.delete({ url: '/iot/access/permission-group/remove-persons', params: { groupId }, data: personIds })
  },
  // 从权限组移除人员并触发权限撤销 - Requirements 2.1
  removePersonsWithRevoke: async (groupId: number, personIds: number[]): Promise<number> => {
    return await request.delete({ url: '/iot/access/permission-group/remove-persons-with-revoke', params: { groupId }, data: personIds })
  }
}


// ==================== 人员权限配置 ====================

export interface AccessPersonPermissionVO {
  personId: number
  personCode: string
  personName: string
  groups: {
    groupId: number
    groupName: string
    deviceCount: number
  }[]
  devices: {
    deviceId: number
    deviceName: string
    channelId: number
    channelName: string
  }[]
}

export const AccessPersonPermissionApi = {
  // 按权限组分配权限（不触发下发）
  assignByGroup: async (personId: number, groupIds: number[]) => {
    return await request.post({ url: '/iot/access/person-permission/assign-by-group', data: { personId, groupIds } })
  },
  // 按权限组分配权限并触发授权下发
  assignByGroupWithDispatch: async (personId: number, groupIds: number[]): Promise<number> => {
    return await request.post({ url: '/iot/access/person-permission/assign-by-group-with-dispatch', data: { personId, groupIds } })
  },
  // 按设备分配权限
  assignByDevice: async (personId: number, deviceIds: number[]) => {
    return await request.post({ url: '/iot/access/person-permission/assign-by-device', data: { personId, deviceIds } })
  },
  // 批量按权限组分配
  batchAssignByGroup: async (personIds: number[], groupIds: number[]) => {
    return await request.post({ url: '/iot/access/person-permission/batch-assign-by-group', data: { personIds, groupIds } })
  },
  // 获取人员权限
  getPersonPermission: async (personId: number) => {
    return await request.get({ url: '/iot/access/person-permission/get', params: { personId } })
  },
  // 移除权限组（不触发撤销）
  removeGroups: async (personId: number, groupIds: number[]) => {
    return await request.delete({ url: '/iot/access/person-permission/remove-groups', data: { personId, groupIds } })
  },
  // 移除权限组并触发权限撤销
  removeGroupsWithRevoke: async (personId: number, groupIds: number[]): Promise<number> => {
    return await request.delete({ url: '/iot/access/person-permission/remove-groups-with-revoke', data: { personId, groupIds } })
  }
}

// ==================== 授权任务管理 ====================

export interface AccessAuthTaskVO {
  id: number
  taskType: string  // group_dispatch/person_dispatch/revoke/incremental
  groupId?: number
  groupName?: string
  deviceId?: number
  deviceName?: string
  totalCount: number
  successCount: number
  failCount: number
  status: number
  progress?: number
  startTime?: Date
  endTime?: Date
  createTime?: Date
  // 实时更新字段（WebSocket推送）
  currentPersonName?: string
  currentDeviceName?: string
}

export interface AccessAuthTaskDetailVO {
  id: number
  taskId: number
  personId: number
  personCode?: string
  personName?: string
  deviceId: number
  deviceName?: string
  channelId?: number
  channelName?: string
  status: number  // 0-待执行 1-成功 2-失败
  errorMessage?: string
  credentialTypes?: string  // 逗号分隔的凭证类型
  executeTime?: Date
  createTime?: Date
}

export interface AccessAuthTaskPageReqVO {
  pageNo: number
  pageSize: number
  groupId?: number
  deviceId?: number
  status?: number
}

// 任务状态选项
export const AccessAuthTaskStatusOptions = [
  { value: 0, label: '待执行', type: 'info' },
  { value: 1, label: '执行中', type: 'primary' },
  { value: 2, label: '成功', type: 'success' },
  { value: 3, label: '部分成功', type: 'warning' },
  { value: 4, label: '失败', type: 'danger' }
]

// 任务类型选项
export const AccessAuthTaskTypeOptions = [
  { value: 'group_dispatch', label: '权限组下发' },
  { value: 'person_dispatch', label: '单人下发' },
  { value: 'revoke', label: '权限撤销' },
  { value: 'incremental', label: '增量下发' }
]

// WebSocket 授权任务进度消息
export interface AuthTaskProgressMessage {
  taskId: number
  taskType: string
  groupId?: number
  groupName?: string
  totalCount: number
  successCount: number
  failCount: number
  progress: number
  status: number
  statusDesc: string
  currentPersonName?: string
  currentDeviceName?: string
  latestError?: string
  startTime?: Date
  endTime?: Date
}

export const AccessAuthTaskApi = {
  // 获取任务分页
  getTaskPage: async (params: AccessAuthTaskPageReqVO) => {
    return await request.get({ url: '/iot/access/auth-task/page', params })
  },
  // 获取任务详情
  getTask: async (taskId: number) => {
    return await request.get({ url: '/iot/access/auth-task/get', params: { id: taskId } })
  },
  // 获取任务明细列表
  getTaskDetails: async (taskId: number) => {
    return await request.get({ url: `/iot/access/auth-task/${taskId}/details` })
  },
  // 获取任务明细列表（别名，向后兼容）
  getTaskDetail: async (taskId: number) => {
    return await request.get({ url: `/iot/access/auth-task/${taskId}/details` })
  },
  // 获取失败的任务明细列表
  getFailedTaskDetails: async (taskId: number) => {
    return await request.get({ url: `/iot/access/auth-task/${taskId}/failed-details` })
  },
  // 重新下发
  retryTask: async (taskId: number) => {
    return await request.post({ url: `/iot/access/auth-task/${taskId}/retry` })
  },
  // 取消任务
  cancelTask: async (taskId: number) => {
    return await request.post({ url: `/iot/access/auth-task/cancel/${taskId}` })
  },
  // 权限组授权下发
  dispatchByGroup: async (groupId: number) => {
    return await request.post({ url: `/iot/access/auth-task/dispatch/group/${groupId}` })
  },
  // 单人授权下发
  dispatchByPerson: async (personId: number) => {
    return await request.post({ url: `/iot/access/auth-task/dispatch/person/${personId}` })
  },
  // 权限组增量下发
  dispatchByGroupIncremental: async (groupId: number) => {
    return await request.post({ url: `/iot/access/auth-task/dispatch/group/${groupId}/incremental` })
  },
  // 单人增量下发
  dispatchByPersonIncremental: async (personId: number) => {
    return await request.post({ url: `/iot/access/auth-task/dispatch/person/${personId}/incremental` })
  },
  // 撤销人员权限
  revokeByPerson: async (personId: number) => {
    return await request.post({ url: `/iot/access/auth-task/revoke/person/${personId}` })
  },
  // 批量撤销人员权限
  revokeByPersons: async (personIds: number[]) => {
    return await request.post({ url: '/iot/access/auth-task/revoke/persons', data: personIds })
  }
}

// ==================== 门禁设备管理 ====================

export interface AccessDeviceVO {
  id: number
  deviceName: string
  deviceCode: string
  productId: number
  productName?: string
  ip: string
  port: number
  username: string
  state: number
  config: {
    generation?: number
    loginHandle?: number
    supportFingerprint?: boolean
    supportFace?: boolean
    supportCard?: boolean
  }
  createTime?: Date
}

/**
 * 设备状态选项（与后端 IotDeviceStateEnum 保持一致）
 * 0: 未激活 - 设备已添加但从未连接
 * 1: 在线 - 设备当前在线
 * 2: 离线 - 设备连接断开
 * 3: 已激活 - 被动连接设备首次心跳后的状态
 */
export const AccessDeviceStateOptions = [
  { value: 0, label: '未激活', type: 'info' },
  { value: 1, label: '在线', type: 'success' },
  { value: 2, label: '离线', type: 'danger' },
  { value: 3, label: '已激活', type: 'primary' }
]

export const AccessDeviceApi = {
  // 激活设备
  activateDevice: async (deviceId: number) => {
    return await request.post({ url: '/iot/access/device/activate', data: { deviceId } })
  },
  // 停用设备
  deactivateDevice: async (deviceId: number) => {
    return await request.post({ url: '/iot/access/device/deactivate', params: { deviceId } })
  },
  // 获取门禁设备列表
  getDeviceList: async () => {
    return await request.get({ url: '/iot/access/device/list' })
  },
  // 获取在线设备列表
  getOnlineDevices: async () => {
    return await request.get({ url: '/iot/access/device/online' })
  }
}


// ==================== 门禁通道管理 ====================

export interface AccessChannelVO {
  id: number
  deviceId: number
  deviceName?: string
  channelNo: number
  channelName: string
  config: {
    openDuration?: number
    alarmDuration?: number
    doorStatus?: string
    lockStatus?: string
    alwaysOpen?: boolean
    alwaysClosed?: boolean
  }
  createTime?: Date
}

// 门状态选项
export const AccessDoorStatusOptions = [
  { value: 'open', label: '开启', type: 'success' },
  { value: 'closed', label: '关闭', type: 'info' }
]

// 锁状态选项
export const AccessLockStatusOptions = [
  { value: 'locked', label: '锁定', type: 'danger' },
  { value: 'unlocked', label: '解锁', type: 'success' }
]

export const AccessChannelApi = {
  // 获取设备通道列表
  getChannelsByDevice: async (deviceId: number) => {
    return await request.get({ url: '/iot/access/channel/list-by-device', params: { deviceId } })
  },
  // 发现通道
  discoverChannels: async (deviceId: number) => {
    return await request.post({ url: '/iot/access/channel/discover', params: { deviceId } })
  },
  // 远程开门
  openDoor: async (channelId: number) => {
    return await request.post({ url: '/iot/access/channel/open-door', data: { channelId } })
  },
  // 远程关门
  closeDoor: async (channelId: number) => {
    return await request.post({ url: '/iot/access/channel/close-door', data: { channelId } })
  },
  // 设置常开
  setAlwaysOpen: async (channelId: number) => {
    return await request.post({ url: '/iot/access/channel/always-open', data: { channelId } })
  },
  // 设置常闭
  setAlwaysClosed: async (channelId: number) => {
    return await request.post({ url: '/iot/access/channel/always-closed', data: { channelId } })
  },
  // 取消常开/常闭
  cancelAlwaysState: async (channelId: number) => {
    return await request.post({ url: '/iot/access/channel/cancel-always', data: { channelId } })
  }
}

// ==================== 门禁事件日志 ====================

export interface AccessEventLogVO {
  id: number
  deviceId: number
  deviceName?: string
  channelId?: number
  channelName?: string
  personId?: number
  personName?: string
  personCode?: string
  eventType: string | number
  eventTime: string | Date
  verifyMode?: string | number
  verifyResult?: number
  success?: boolean
  verifyResultDesc?: string
  failReason?: string
  cardNo?: string
  captureUrl?: string
  snapshotUrl?: string
  temperature?: number
  maskStatus?: number
  createTime?: Date
  // 实时事件标记
  isRealtime?: boolean
  _highlight?: boolean
}

export interface AccessEventLogPageReqVO {
  pageNo: number
  pageSize: number
  deviceId?: number
  personId?: number
  eventCategory?: string  // 事件类别（NORMAL/ALARM/ABNORMAL）
  eventType?: string      // 事件类型代码（字符串，如 CARD_SWIPE）
  verifyResult?: number
  startTime?: string      // 开始时间
  endTime?: string        // 结束时间
}

// 事件类型选项 - 使用新的事件类型常量
import {
  EventCategory,
  EventCategoryConfig,
  AllEventTypes,
  NormalEventTypes,
  AlarmEventTypes,
  getEventCategory,
  getEventTypeName,
  getCategoryStyle,
  getEventCategoryOptions,
  getEventTypeOptionsByCategory
} from '@/utils/accessEventTypes'

// 重新导出事件类型相关内容
export {
  EventCategory,
  EventCategoryConfig,
  AllEventTypes,
  NormalEventTypes,
  AlarmEventTypes,
  getEventCategory,
  getEventTypeName,
  getCategoryStyle,
  getEventCategoryOptions,
  getEventTypeOptionsByCategory
}

// 事件类别选项
export const AccessEventCategoryOptions = getEventCategoryOptions()

// 事件类型选项（完整列表）- 使用字符串代码
export const AccessEventTypeOptions = AllEventTypes.map(t => ({
  value: t.code,  // 字符串代码，如 'CARD_SWIPE'
  label: t.name,
  type: t.category === EventCategory.ALARM ? 'danger' :
        t.category === EventCategory.ABNORMAL ? 'warning' : 'success'
}))

// 验证结果选项
export const AccessVerifyResultOptions = [
  { value: 0, label: '失败', type: 'danger' },
  { value: 1, label: '成功', type: 'success' }
]

export const AccessEventLogApi = {
  // 获取事件分页
  getEventPage: async (params: AccessEventLogPageReqVO) => {
    return await request.get({ url: '/iot/access/event/page', params })
  },
  // 获取最近事件
  getRecentEvents: async (limit?: number) => {
    return await request.get({ url: '/iot/access/event/recent', params: { limit: limit || 10 } })
  },
  // 获取今日事件统计 - Requirements 7.1
  getTodayStatistics: async (): Promise<{ total: number; alarmCount: number; normalCount: number; abnormalCount: number }> => {
    return await request.get({ url: '/iot/access/event/today-statistics' })
  }
}

// ==================== 操作日志 ====================

export interface AccessOperationLogVO {
  id: number
  operationType: string
  operationTypeName?: string
  deviceId?: number
  deviceName?: string
  channelId?: number
  channelName?: string
  operatorId?: number
  operatorName?: string
  result: number
  resultDesc?: string
  errorMessage?: string
  operationTime: string  // 后端返回格式化后的字符串
  createTime?: string
  // 授权操作扩展字段
  targetPersonId?: number
  targetPersonCode?: string
  targetPersonName?: string
  permissionGroupId?: number
  permissionGroupName?: string
  authTaskId?: number
  credentialTypes?: string
  successCredentialCount?: number
  failedCredentialCount?: number
  sdkErrorCode?: number
}

export interface AccessOperationLogPageReqVO {
  pageNo: number
  pageSize: number
  operationType?: string
  operatorId?: number
  result?: number
  startTime?: string  // 修复：与后端字段名保持一致
  endTime?: string    // 修复：与后端字段名保持一致
}

// 操作类型选项
export const AccessOperationTypeOptions = [
  { value: 'open_door', label: '远程开门' },
  { value: 'close_door', label: '远程关门' },
  { value: 'always_open', label: '设置常开' },
  { value: 'always_closed', label: '设置常闭' },
  { value: 'cancel_always', label: '取消常开/常闭' },
  { value: 'auth_dispatch_group', label: '权限组下发' },
  { value: 'auth_dispatch_person', label: '单人下发' },
  { value: 'auth_revoke', label: '授权撤销' },
  { value: 'auth_retry', label: '授权重试' },
  { value: 'add_user', label: '添加用户' },
  { value: 'delete_user', label: '删除用户' },
  { value: 'add_card', label: '添加卡片' },
  { value: 'delete_card', label: '删除卡片' },
  { value: 'add_face', label: '录入人脸' },
  { value: 'delete_face', label: '删除人脸' },
  { value: 'add_fingerprint', label: '录入指纹' },
  { value: 'delete_fingerprint', label: '删除指纹' }
]

export const AccessOperationLogApi = {
  // 获取操作日志分页
  getLogPage: async (params: AccessOperationLogPageReqVO) => {
    return await request.get({ url: '/iot/access/operation-log/page', params })
  }
}

// ==================== 时间模板 ====================

export interface AccessTimeTemplateVO {
  id: number
  templateName: string
  weekConfig: string // JSON格式的周配置
  holidayConfig: string
  status: number
  createTime?: Date
}

export const AccessTimeTemplateApi = {
  // 获取时间模板列表
  getTemplateList: async () => {
    return await request.get({ url: '/iot/access/time-template/list' })
  }
}

// ==================== 测试工具 - 设备配置查询 ====================

export interface AccessDeviceConfigVO {
  // 基本信息
  id: number
  deviceName: string
  deviceCode: string
  productId: number
  productName?: string
  ip: string
  port: number
  username: string
  state: number
  
  // 连接配置
  config: {
    generation?: number
    loginHandle?: number
    supportFingerprint?: boolean
    supportFace?: boolean
    supportCard?: boolean
  }
  
  // 能力集
  capabilities: {
    maxUserCount?: number
    maxCardCount?: number
    maxFaceCount?: number
    maxFingerprintCount?: number
    supportedAuthModes?: string[]
  }
  
  // 运行时信息
  runtime: {
    isOnline: boolean
    loginHandle?: number
    onlineDuration?: number
    lastHeartbeatTime?: Date
    lastOfflineTime?: Date
    offlineReason?: string
  }
  
  // 通道列表
  channels: AccessChannelDetailVO[]
}

export interface AccessChannelDetailVO {
  id: number
  deviceId: number
  channelNo: number
  channelName: string
  
  // 配置参数
  config: {
    openDuration?: number
    alarmDuration?: number
    doorStatus?: string
    lockStatus?: string
    alwaysOpen?: boolean
    alwaysClosed?: boolean
  }
  
  // 实时状态
  status: {
    doorStatus?: string
    lockStatus?: string
    isAlwaysOpen?: boolean
    isAlwaysClosed?: boolean
    lastUpdateTime?: Date
  }
}

export const AccessTestApi = {
  // 获取设备完整配置
  getDeviceConfig: async (deviceId: number) => {
    return await request.get({ url: `/iot/access/device/config/${deviceId}` })
  },
  
  // 获取通道详细信息
  getChannelDetail: async (channelId: number) => {
    return await request.get({ url: `/iot/access/channel/detail/${channelId}` })
  }
}

// ==================== 测试工具 - 凭证验证开门 ====================

export interface AccessCredentialVerifyReqVO {
  channelId: number
  credentialType: 'CARD' | 'PASSWORD' | 'FACE' | 'FINGERPRINT'
  credentialValue: string
}

export interface AccessCredentialVerifyRespVO {
  success: boolean
  message: string
  openTime?: Date
  personId?: number
  personName?: string
  verifyResult?: {
    credentialValid: boolean
    permissionValid: boolean
    deviceOnline: boolean
  }
}

export const AccessCredentialApi = {
  // 凭证验证开门
  verifyAndOpen: async (data: AccessCredentialVerifyReqVO) => {
    return await request.post({ url: '/iot/access/credential/verify-and-open', data })
  }
}

// ==================== 测试工具 - 卡信息管理 ====================

export interface AccessCardAddReqVO {
  deviceId: number
  recordNo?: number
  cardNo: string
  cardName?: string
  userId?: string
  password?: string
  status?: number
  type?: number
  isValid?: boolean
  validStartTime?: string
  validEndTime?: string
  doorList?: number[]
  timeSectionList?: number[]
}

export interface AccessCardRespVO {
  recordNo: number
  cardNo: string
  cardName: string
  userId: string
  password: string
  status: number
  type: number
  isValid: boolean
  validStartTime: string
  validEndTime: string
  doorList: number[]
  timeSectionList: number[]
}

// 卡状态选项
export const AccessCardStatusOptions = [
  { value: 0, label: '正常', type: 'success' },
  { value: 1, label: '挂失', type: 'warning' },
  { value: 2, label: '注销', type: 'danger' },
  { value: 3, label: '冻结', type: 'info' }
]

// 卡类型选项
export const AccessCardTypeOptions = [
  { value: 0, label: '普通卡' },
  { value: 1, label: '巡更卡' },
  { value: 2, label: '胁迫卡' },
  { value: 3, label: '超级卡' },
  { value: 4, label: '来宾卡' }
]

export const AccessCardApi = {
  // 添加卡
  addCard: async (data: AccessCardAddReqVO) => {
    return await request.post({ url: '/iot/access/card/add', data })
  },
  
  // 更新卡
  updateCard: async (data: AccessCardAddReqVO) => {
    return await request.put({ url: '/iot/access/card/update', data })
  },
  
  // 删除卡
  deleteCard: async (deviceId: number, recordNo: number) => {
    return await request.delete({ url: '/iot/access/card/delete', params: { deviceId, recordNo } })
  },
  
  // 查询卡列表
  listCards: async (deviceId: number) => {
    return await request.get({ url: '/iot/access/card/list', params: { deviceId } })
  },
  
  // 清空所有卡
  clearAllCards: async (deviceId: number) => {
    return await request.post({ url: '/iot/access/card/clear', params: { deviceId } })
  }
}

// ==================== 门禁管理（控制器+门通道组合视图）====================

export interface AccessControllerTreeVO {
  deviceId: number
  deviceName: string
  deviceCode?: string
  ip?: string
  port?: number
  online: boolean
  state: number
  stateDesc?: string
  lastOnlineTime?: Date
  channelCount?: number
  supportVideo?: boolean
  deviceType?: string
  channels: DoorChannelVO[]
}

export interface DoorChannelVO {
  channelId: number
  channelNo: number
  channelName?: string
  doorStatus: number
  doorStatusDesc?: string
  lockStatus: number
  lockStatusDesc?: string
  alwaysMode: number
  alwaysModeDesc?: string
  operable: boolean
}

export interface AccessControllerDetailVO {
  deviceId: number
  deviceName: string
  deviceCode?: string
  productId?: number
  productName?: string
  ip: string
  port?: number
  username?: string
  online: boolean
  state: number
  stateDesc?: string
  lastOnlineTime?: Date
  lastOfflineTime?: Date
  activeTime?: Date
  onlineDuration?: number
  deviceType?: string
  capabilities?: {
    supportPassword?: boolean
    supportCard?: boolean
    supportFingerprint?: boolean
    supportFace?: boolean
    supportQrCode?: boolean
    supportRemoteOpen?: boolean
    supportAlwaysMode?: boolean
    maxUsers?: number
    maxCards?: number
    maxFingerprints?: number
    maxFaces?: number
  }
  channelCount?: number
  channels: DoorChannelDetailVO[]
  createTime?: Date
}

export interface DoorChannelDetailVO extends DoorChannelVO {
  openDuration?: number
  alarmDuration?: number
  location?: string
}

export interface DoorControlReqVO {
  deviceId: number
  channelId: number
  channelNo?: number
  action: 'OPEN_DOOR' | 'CLOSE_DOOR' | 'ALWAYS_OPEN' | 'ALWAYS_CLOSED' | 'CANCEL_ALWAYS'
}

export interface DoorControlRespVO {
  success: boolean
  deviceId?: number
  channelId?: number
  action?: string
  errorMessage?: string
  duration?: number
}

// 门状态选项
export const DoorStatusOptions = [
  { value: 0, label: '关闭', type: 'info' },
  { value: 1, label: '打开', type: 'success' },
  { value: 2, label: '未知', type: 'warning' }
]

// 锁状态选项
export const LockStatusOptions = [
  { value: 0, label: '锁定', type: 'danger' },
  { value: 1, label: '解锁', type: 'success' },
  { value: 2, label: '未知', type: 'warning' }
]

// 常开常闭模式选项
export const AlwaysModeOptions = [
  { value: 0, label: '正常', type: 'info' },
  { value: 1, label: '常开', type: 'success' },
  { value: 2, label: '常闭', type: 'danger' }
]

// ==================== 授权记录 VO (Requirements: 7.1, 7.2) ====================

export interface PersonDeviceAuthVO {
  id: number
  personId: number
  personName: string
  personCode?: string
  deviceId: number
  deviceName: string
  channelId?: number
  channelName?: string
  authStatus: number
  authStatusName: string
  result?: string
  lastDispatchTime?: Date
  createTime?: Date
  updateTime?: Date
}

export interface AuthRecordPageReqVO {
  pageNo: number
  pageSize: number
  personId?: number
  personName?: string
  deviceId?: number
  deviceName?: string
  channelId?: number
  authStatus?: number
  createTimeStart?: Date
  createTimeEnd?: Date
}

// 授权状态选项
export const AuthStatusOptions = [
  { value: 0, label: '未授权', type: 'info' },
  { value: 1, label: '已授权', type: 'success' },
  { value: 2, label: '授权中', type: 'primary' },
  { value: 3, label: '授权失败', type: 'danger' },
  { value: 4, label: '待撤销', type: 'warning' },
  { value: 5, label: '撤销中', type: 'primary' }
]

export const AccessManagementApi = {
  // 获取门禁控制器树形结构
  getControllerTree: async (): Promise<AccessControllerTreeVO[]> => {
    return await request.get({ url: '/iot/access/management/tree' })
  },
  
  // 获取在线门禁控制器树形结构
  getOnlineControllerTree: async (): Promise<AccessControllerTreeVO[]> => {
    return await request.get({ url: '/iot/access/management/online-tree' })
  },
  
  // 获取门禁控制器详情
  getControllerDetail: async (deviceId: number): Promise<AccessControllerDetailVO> => {
    return await request.get({ url: `/iot/access/management/detail/${deviceId}` })
  },
  
  // 门控操作
  doorControl: async (data: DoorControlReqVO): Promise<DoorControlRespVO> => {
    return await request.post({ url: '/iot/access/management/door-control', data })
  },
  
  // 刷新控制器状态
  refreshControllerStatus: async (deviceId: number): Promise<boolean> => {
    return await request.post({ url: `/iot/access/management/refresh/${deviceId}` })
  },

  // ========== 授权记录接口 (Requirements: 7.1, 7.2, 7.3) ==========
  
  // 获取授权记录分页
  getAuthRecordPage: async (params: AuthRecordPageReqVO) => {
    return await request.get({ url: '/iot/access/management/auth-record/page', params })
  },
  
  // 重试失败的授权
  retryFailedAuth: async (authId: number): Promise<boolean> => {
    return await request.post({ url: `/iot/access/management/auth-record/retry/${authId}` })
  }
}

// ==================== 设备人员同步 ====================

export interface DeviceSyncCheckResultVO {
  deviceId: number
  deviceName: string
  deviceIp?: string
  success: boolean
  errorMessage?: string
  systemUsers?: DeviceSyncPersonInfoVO[]
  deviceUsers?: DeviceSyncDeviceUserInfoVO[]
  syncedUsers?: DeviceSyncSyncedUserVO[]
  systemOnlyUsers?: DeviceSyncPersonInfoVO[]
  deviceOnlyUsers?: DeviceSyncDeviceUserInfoVO[]
  statistics?: DeviceSyncStatisticsVO
}

export interface DeviceSyncPersonInfoVO {
  personId: number
  personCode: string
  personName: string
  credentialTypes?: string[]
  cardNo?: string
  groupName?: string
}

export interface DeviceSyncDeviceUserInfoVO {
  userId: string
  userName?: string
  cardNo?: string
  recNo?: number
  validStart?: string
  validEnd?: string
  hasFace?: boolean
  hasFingerprint?: boolean
}

export interface DeviceSyncSyncedUserVO {
  systemUser: DeviceSyncPersonInfoVO
  deviceUser: DeviceSyncDeviceUserInfoVO
  consistent: boolean
  difference?: string
}

export interface DeviceSyncStatisticsVO {
  systemTotal: number
  deviceTotal: number
  syncedCount: number
  systemOnlyCount: number
  deviceOnlyCount: number
  syncRate: number
}

// 同步状态选项
export const DeviceSyncStatusOptions = [
  { value: 'synced', label: '已同步', type: 'success' },
  { value: 'system_only', label: '待下发', type: 'warning' },
  { value: 'device_only', label: '野生用户', type: 'danger' }
]

export const AccessDeviceSyncApi = {
  // 对账检查单个设备
  checkDevice: async (deviceId: number): Promise<DeviceSyncCheckResultVO> => {
    return await request.get({ url: '/iot/access/device-sync/check', params: { deviceId } })
  },
  
  // 批量对账检查
  checkDevices: async (deviceIds: number[]): Promise<DeviceSyncCheckResultVO[]> => {
    return await request.get({ url: '/iot/access/device-sync/check-batch', params: { deviceIds: deviceIds.join(',') } })
  },
  
  // 对账权限组关联的所有设备
  checkByGroup: async (groupId: number): Promise<DeviceSyncCheckResultVO[]> => {
    return await request.get({ url: '/iot/access/device-sync/check-group', params: { groupId } })
  },
  
  // 清理设备多余用户
  cleanExtraUsers: async (deviceId: number): Promise<DeviceSyncCheckResultVO> => {
    return await request.post({ url: '/iot/access/device-sync/clean', params: { deviceId } })
  },
  
  // 补发缺失用户
  repairMissingUsers: async (deviceId: number): Promise<DeviceSyncCheckResultVO> => {
    return await request.post({ url: '/iot/access/device-sync/repair', params: { deviceId } })
  },
  
  // 全量同步
  fullSync: async (deviceId: number): Promise<DeviceSyncCheckResultVO> => {
    return await request.post({ url: '/iot/access/device-sync/full-sync', params: { deviceId } })
  },
  
  // 清理指定用户
  cleanSpecificUsers: async (deviceId: number, userIds: string[]): Promise<DeviceSyncCheckResultVO> => {
    return await request.post({ url: '/iot/access/device-sync/clean-users', params: { deviceId, userIds: userIds.join(',') } })
  },
  
  // 查询设备用户列表
  queryDeviceUsers: async (deviceId: number): Promise<DeviceSyncDeviceUserInfoVO[]> => {
    return await request.get({ url: '/iot/access/device-sync/device-users', params: { deviceId } })
  },
  
  // 查询系统应授权人员
  getSystemUsers: async (deviceId: number): Promise<DeviceSyncPersonInfoVO[]> => {
    return await request.get({ url: '/iot/access/device-sync/system-users', params: { deviceId } })
  }
}

// ==================== 门禁视频预览 ====================

export interface AccessVideoPlayParamsVO {
  wsURL: string
  rtspURL: string
  username: string
  password: string
  target: string
  deviceName: string
  channelName: string
  online: boolean
}

export interface AccessVideoSupportCheckVO {
  deviceId: number
  supportVideo: boolean
  httpPort?: number
  rtspPort?: number
  online: boolean
}

export const AccessVideoApi = {
  // 获取视频播放参数
  getPlayParams: async (deviceId: number, channelNo?: number): Promise<AccessVideoPlayParamsVO> => {
    return await request.get({ 
      url: '/iot/access/video/play-params', 
      params: { deviceId, channelNo: channelNo ?? 0 } 
    })
  },
  
  // 检查设备是否支持视频
  checkVideoSupport: async (deviceId: number): Promise<AccessVideoSupportCheckVO> => {
    return await request.get({ 
      url: '/iot/access/video/support-check', 
      params: { deviceId } 
    })
  }
}
