import request from '@/config/axios'

export interface ChannelVO {
  id?: number
  deviceId: number
  deviceType: string
  productId?: number
  channelNo: number
  channelName: string
  channelCode?: string
  channelType: string
  channelSubType?: string
  location?: string
  buildingId?: number
  floorId?: number
  areaId?: number
  spaceId?: number
  targetDeviceId?: number
  targetIp?: string
  targetPort?: number
  targetChannelNo?: number
  protocol?: string
  username?: string
  password?: string
  streamUrlMain?: string
  streamUrlSub?: string
  snapshotUrl?: string
  ptzSupport?: boolean
  audioSupport?: boolean
  resolution?: string
  frameRate?: number
  bitRate?: number
  doorName?: string
  doorDirection?: string
  cardReaderType?: string
  lockType?: string
  detectorType?: string
  alarmLevel?: number
  meterType?: string
  circuitName?: string
  measurementUnit?: string
  capabilities?: Record<string, any>
  onlineStatus?: number
  enableStatus?: number
  alarmStatus?: number
  lastOnlineTime?: Date
  lastSyncTime?: Date
  isRecording?: boolean
  isPatrol?: boolean
  isMonitor?: boolean
  patrolDuration?: number
  monitorPosition?: number
  config?: Record<string, any>
  description?: string
  sort?: number
  tags?: string
  deviceName?: string
  targetDeviceName?: string
  useCustomPosition?: boolean
  xCoordinate?: number
  yCoordinate?: number
  zCoordinate?: number
  createTime?: Date
  updateTime?: Date
}

export interface ChannelPageReqVO extends PageParam {
  deviceId?: number
  deviceType?: string
  channelType?: string
  channelName?: string
  onlineStatus?: number
  enableStatus?: number
  buildingId?: number
  floorId?: number
  areaId?: number
  createTime?: Date[]
}

// 查询设备通道分页
export const getChannelPage = (params: ChannelPageReqVO) => {
  return request.get({ url: '/iot/channel/page', params })
}

// 查询设备通道详情
export const getChannel = (id: number) => {
  return request.get({ url: '/iot/channel/get?id=' + id })
}

// 新增设备通道
export const createChannel = (data: ChannelVO) => {
  return request.post({ url: '/iot/channel/create', data })
}

// 修改设备通道
export const updateChannel = (data: ChannelVO) => {
  return request.put({ url: '/iot/channel/update', data })
}

// 删除设备通道
export const deleteChannel = (id: number) => {
  return request.delete({ url: '/iot/channel/delete?id=' + id })
}

// 获取设备的所有通道
export const getDeviceChannels = (deviceId: number) => {
  return request.get({ url: `/iot/channel/device/${deviceId}` })
}

// 获取视频通道列表
export const getVideoChannels = (params?: {
  deviceType?: string
  onlineStatus?: number
  isPatrol?: boolean
  isMonitor?: boolean
}) => {
  return request.get({ url: '/iot/channel/video/list', params })
}

// 获取巡更通道列表
export const getPatrolChannels = () => {
  return request.get({ url: '/iot/channel/video/patrol' })
}

// 获取监控墙通道列表
export const getMonitorChannels = () => {
  return request.get({ url: '/iot/channel/video/monitor' })
}

// 同步设备通道
export const syncDeviceChannels = (deviceId: number) => {
  return request.post({ url: `/iot/channel/sync/${deviceId}` })
}

// 批量同步所有NVR通道（更新通道名称）
export const syncAllNvrChannels = () => {
  return request.post<{
    nvrCount: number
    successCount: number
    failCount: number
    duration: number
  }>({ url: '/iot/channel/sync-all-nvr' })
}

// 批量启用通道
export const batchEnableChannels = (channelIds: number[]) => {
  return request.post({ url: '/iot/channel/batch/enable', data: channelIds })
}

// 批量禁用通道
export const batchDisableChannels = (channelIds: number[]) => {
  return request.post({ url: '/iot/channel/batch/disable', data: channelIds })
}

// 批量设置巡更
export const batchSetPatrol = (channelIds: number[], isPatrol: boolean) => {
  return request.post({
    url: '/iot/channel/batch/patrol',
    params: { channelIds: channelIds.join(','), isPatrol }
  })
}

// 批量设置监控墙
export const batchSetMonitor = (channelIds: number[], isMonitor: boolean) => {
  return request.post({
    url: '/iot/channel/batch/monitor',
    params: { channelIds: channelIds.join(','), isMonitor }
  })
}

export const batchAssignSpatial = (data: {
  channelIds: number[]
  campusId: number
  buildingId: number
  floorId: number
}) => {
  return request.post({ url: '/iot/channel/batch/assign-spatial', data })
}

// 设备树节点类型
export interface DeviceTreeNode {
  id: string | number
  label: string
  type: 'device' | 'channel'
  deviceId?: number
  channelId?: number
  channelNo?: number
  deviceType?: string
  channelType?: string
  onlineStatus?: number
  children?: DeviceTreeNode[]
  // 原始数据
  raw?: any
}

// 获取设备树（设备+通道）
export const getDeviceTree = (params?: {
  deviceType?: string
  channelType?: string
  onlineStatus?: number
  keyword?: string
}) => {
  return request.get<DeviceTreeNode[]>({ url: '/iot/channel/device-tree', params })
}
