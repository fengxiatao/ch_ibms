import request from '@/config/axios'

/** 将 IBMS 通道行映射为旧 ChannelVO，便于页面少改 */
function mapIbmsChannelRow(row: Record<string, any>): ChannelVO {
  if (!row) {
    return row as any
  }
  const st = row.status === 'online' ? 1 : row.status === 'offline' ? 0 : undefined
  return {
    id: row.id,
    deviceId: row.deviceId,
    deviceType: row.systemType || '',
    channelNo: row.channelNo,
    channelName: row.name,
    channelCode: row.code,
    channelType: row.typeCode,
    location: row.space,
    deviceName: row.deviceName,
    streamUrlMain: undefined,
    onlineStatus: st,
    ip: row.ip,
    createTime: row.createTime,
    updateTime: row.updateTime
  } as ChannelVO
}

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

// 查询设备通道分页（IBMS）
export const getChannelPage = async (params: ChannelPageReqVO) => {
  const res: any = await request.get({
    url: '/iot/ibms/channel/page',
    params: {
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      deviceId: params.deviceId,
      typeCode: params.channelType,
      keyword: params.channelName,
      status:
        params.onlineStatus === 1 ? 'online' : params.onlineStatus === 0 ? 'offline' : undefined,
      createTime: params.createTime
    }
  })
  if (res?.list) {
    res.list = res.list.map(mapIbmsChannelRow)
  }
  return res
}

// 查询设备通道详情（IBMS）
export const getChannel = async (id: number) => {
  const row = await request.get({ url: '/iot/ibms/channel/get', params: { id } })
  return mapIbmsChannelRow(row as any)
}

/** ChannelVO → IBMS 保存载荷（兼容视频等业务页少改字段名） */
function channelVoToIbmsSavePayload(data: ChannelVO): Record<string, any> {
  const deviceId = data.deviceId
  const channelNo = data.channelNo ?? 1
  const code =
    data.channelCode ||
    (deviceId != null ? `CH-${deviceId}-${String(channelNo).padStart(2, '0')}` : `CH-TEMP-${channelNo}`)
  const st =
    data.onlineStatus === 0 ? 'offline' : data.onlineStatus === 1 ? 'online' : 'online'
  const payload: Record<string, any> = {
    id: data.id,
    spaceId: data.spaceId,
    deviceId: data.deviceId,
    code,
    channelNo,
    name: data.channelName || code,
    business: 'security',
    typeCode: data.channelType || 'VT',
    category: data.channelSubType,
    systemType: data.deviceType || 'VI',
    dataSource: 'NVR',
    ip: data.ip,
    deviceName: data.deviceName,
    space: data.location,
    status: st
  }
  if (data.config && Object.keys(data.config).length > 0) {
    payload.extra = JSON.stringify(data.config)
  }
  return payload
}

// 新增设备通道（IBMS）
export const createChannel = (data: ChannelVO) => {
  return request.post({ url: '/iot/ibms/channel/create', data: channelVoToIbmsSavePayload(data) })
}

// 修改设备通道（IBMS）
export const updateChannel = (data: ChannelVO) => {
  return request.put({ url: '/iot/ibms/channel/update', data: channelVoToIbmsSavePayload(data) })
}

// 删除设备通道（IBMS）
export const deleteChannel = (id: number) => {
  return request.delete({ url: '/iot/ibms/channel/delete?id=' + id })
}

// 获取设备的所有通道（IBMS）
export const getDeviceChannels = async (deviceId: number) => {
  const list = await request.get<any[]>({
    url: '/iot/ibms/channel/list-by-device',
    params: { deviceId }
  })
  return (list || []).map(mapIbmsChannelRow)
}

// 获取视频通道列表（IBMS）
export const getVideoChannels = async (params?: {
  deviceType?: string
  onlineStatus?: number
  isPatrol?: boolean
  isMonitor?: boolean
}) => {
  const list = await request.get<any[]>({ url: '/iot/ibms/channel/video/list', params })
  return (list || []).map(mapIbmsChannelRow)
}

// 获取巡更通道列表（IBMS）
export const getPatrolChannels = async () => {
  const list = await request.get<any[]>({ url: '/iot/ibms/channel/video/patrol' })
  return (list || []).map(mapIbmsChannelRow)
}

// 获取监控墙通道列表（IBMS）
export const getMonitorChannels = async () => {
  const list = await request.get<any[]>({ url: '/iot/ibms/channel/video/monitor' })
  return (list || []).map(mapIbmsChannelRow)
}

// 同步设备通道（IBMS / NVR）
export const syncDeviceChannels = (deviceId: number) => {
  return request.post({
    url: '/iot/ibms/channel/sync-from-device',
    params: { deviceId }
  })
}

// 批量同步所有NVR通道（IBMS，委托既有同步逻辑）
export const syncAllNvrChannels = () => {
  return request.post<{
    nvrCount: number
    successCount: number
    failCount: number
    duration: number
  }>({ url: '/iot/ibms/channel/sync-all-nvr' })
}

// 批量启用通道（IBMS extra.enableStatus）
export const batchEnableChannels = (channelIds: number[]) => {
  return request.post({ url: '/iot/ibms/channel/batch/enable', data: channelIds })
}

// 批量禁用通道（IBMS）
export const batchDisableChannels = (channelIds: number[]) => {
  return request.post({ url: '/iot/ibms/channel/batch/disable', data: channelIds })
}

// 批量设置巡更（IBMS extra.isPatrol）
export const batchSetPatrol = (channelIds: number[], isPatrol: boolean) => {
  return request.post({
    url: '/iot/ibms/channel/batch/patrol',
    params: { channelIds: channelIds.join(','), isPatrol }
  })
}

// 批量设置监控墙（IBMS extra.isMonitor）
export const batchSetMonitor = (channelIds: number[], isMonitor: boolean) => {
  return request.post({
    url: '/iot/ibms/channel/batch/monitor',
    params: { channelIds: channelIds.join(','), isMonitor }
  })
}

export const batchAssignSpatial = (data: {
  channelIds: number[]
  campusId: number
  buildingId: number
  floorId: number
  areaId?: number
}) => {
  return request.post({ url: '/iot/ibms/channel/batch/assign-spatial', data })
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

// 获取设备树（设备+通道，IBMS，默认 VI 设备）
export const getDeviceTree = (params?: {
  deviceType?: string
  channelType?: string
  onlineStatus?: number
  keyword?: string
}) => {
  return request.get<DeviceTreeNode[]>({ url: '/iot/ibms/channel/device-tree', params })
}
