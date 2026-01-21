import request from '@/config/axios'

export interface IotAlarmHostVO {
  id?: number
  hostName: string
  hostModel?: string
  hostSn?: string
  deviceId: number
  zoneCount?: number
  location?: string
  onlineStatus?: number
  armStatus?: string
  alarmStatus?: number
  // 连接配置（后端 IotAlarmHostBaseVO）
  ipAddress?: string
  port?: number
  account?: string
  password?: string
  remark?: string
  createTime?: Date
}

export interface IotAlarmHostPageReqVO extends PageParam {
  hostName?: string
  deviceId?: number
  onlineStatus?: number
  armStatus?: string
  alarmStatus?: number
  createTime?: Date[]
}

// 查询报警主机分页
export const getAlarmHostPage = (params: IotAlarmHostPageReqVO) => {
  return request.get({ url: '/iot/alarm/host/page', params })
}

/**
 * 获取全部报警主机（自动按后端 pageSize 最大 100 进行分页拉取）
 *
 * 注意：后端对 pageSize 有 @Max(100) 校验，前端不得传 > 100，否则会触发 400。
 */
export const getAllAlarmHosts = async (): Promise<IotAlarmHostVO[]> => {
  const pageSize = 100
  let pageNo = 1
  const result: IotAlarmHostVO[] = []

  // 安全阈值：避免异常情况下无限循环
  const maxPages = 50

  while (pageNo <= maxPages) {
    const page = await getAlarmHostPage({ pageNo, pageSize } as any)
    const list = (page?.list || []) as IotAlarmHostVO[]
    result.push(...list)
    const total = Number(page?.total || 0)

    if (!list.length) break
    if (total && result.length >= total) break
    pageNo += 1
  }

  return result
}

// 查询报警主机详情
export const getAlarmHost = (id: number) => {
  return request.get({ url: '/iot/alarm/host/get?id=' + id })
}

// 新增报警主机
export const createAlarmHost = (data: IotAlarmHostVO) => {
  return request.post({ url: '/iot/alarm/host/create', data })
}

// 修改报警主机
export const updateAlarmHost = (data: IotAlarmHostVO) => {
  return request.put({ url: '/iot/alarm/host/update', data })
}

// 删除报警主机
export const deleteAlarmHost = (id: number) => {
  return request.delete({ url: '/iot/alarm/host/delete?id=' + id })
}

// 新增：带完整状态的响应接口（主机+分区+防区）
export interface IotAlarmHostWithDetailsVO extends IotAlarmHostVO {
  partitions?: IotAlarmPartitionVO[]
  zones?: IotAlarmZoneVO[]
}

// 全部布防（返回主机+分区+防区完整状态）
export const armAll = (id: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/arm-all?id=' + id })
}

// 紧急布防（返回主机+分区+防区完整状态）
export const armEmergency = (id: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/arm-emergency?id=' + id })
}

// 撤防（返回主机+分区+防区完整状态）
export const disarm = (id: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/disarm?id=' + id })
}

// 消警（返回主机+分区+防区完整状态）
export const clearAlarm = (id: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/clear-alarm?id=' + id })
}

// ==================== 分区和防区查询接口 ====================

// 分区VO
export interface IotAlarmPartitionVO {
  id: number
  hostId: number
  partitionNo: number
  partitionName?: string
  status: number
  description?: string
  zoneCount?: number
  createTime?: Date
}

// 防区VO
export interface IotAlarmZoneVO {
  id: number
  hostId: number
  zoneNo: number
  zoneName?: string
  zoneType?: string
  zoneStatus?: string
  partitionId?: number
  areaLocation?: string
  // 实时/枚举字段（后端 IotAlarmZoneRespVO）
  zoneStatusEnum?: number
  onlineStatus?: number
  statusChar?: string
  statusName?: string
  armStatus?: number
  alarmStatus?: number
  zoneStatusCode?: string
  alarmCount?: number
  lastAlarmTime?: Date
  createTime?: Date
}

// 主机状态VO
export interface IotAlarmHostStatusVO {
  hostId?: number
  account: string
  systemStatus: number
  partitions?: Array<{
    partitionNo: number
    status: number
  }>
  zones?: Array<{
    zoneNo: number
    zoneName?: string
    status: string
    statusName?: string
    isArmed?: boolean
    isAlarming?: boolean
    lastAlarmTime?: Date
  }>
  queryTime?: Date
  lastQueryTime?: Date
  rawData?: string
}

// 查询主机分区列表
export const getPartitionList = (hostId: number) => {
  return request.get({ url: `/iot/alarm/host/${hostId}/partitions` })
}

// 查询主机防区列表
export const getZoneList = (hostId: number) => {
  return request.get({ url: `/iot/alarm/host/${hostId}/zones` })
}

// 查询主机实时状态
export const queryHostStatus = (hostId: number) => {
  return request.post({ url: `/iot/alarm/host/${hostId}/query-status` })
}

// 更新主机名称
export const updateHostName = (id: number, hostName: string) => {
  return request.put({ url: '/iot/alarm/host/update-name', data: { id, hostName } })
}

// 更新分区名称
export const updatePartitionName = (id: number, partitionName: string) => {
  return request.put({ url: '/iot/alarm/host/partition/update-name', data: { id, partitionName } })
}

// 更新防区名称
export const updateZoneName = (id: number, zoneName: string) => {
  return request.put({ url: '/iot/alarm/host/zone/update-name', data: { id, zoneName } })
}

// ==================== 新增：异步状态查询接口 ====================

/**
 * 快速查询主机状态（指令码0，无参数）
 * 用于快速检测主机在线状态和基本信息
 * 
 * @param account 主机账号
 * @returns Promise<boolean> 是否成功触发查询
 */
export const quickQueryHostStatus = (account: string) => {
  return request.post({ url: '/iot/alarm/host/quick-query', params: { account } })
}

/**
 * 触发主机状态查询（异步）- 指令码10，查询分区和防区详细状态
 * 调用Gateway层触发查询，结果通过消息总线异步返回并更新数据库
 * 
 * @param account 主机账号
 * @returns Promise<boolean> 是否成功触发查询
 */
export const triggerQueryHostStatus = (account: string) => {
  return request.post({ url: '/iot/alarm/host/trigger-query', params: { account } })
}

/**
 * 获取主机状态（从数据库）
 * 返回最后一次查询并持久化的状态数据
 * 
 * @param account 主机账号
 * @returns Promise<IotAlarmHostStatusVO> 主机状态信息
 */
export const getHostStatus = (account: string) => {
  return request.get({ url: '/iot/alarm/host/status', params: { account } })
}

// ==================== 分区操作接口 ====================

/**
 * 分区外出布防
 * @param partitionId 分区ID
 */
export const armPartitionAll = (partitionId: number) => {
  return request.put({ url: '/iot/alarm/host/partition/arm-all', params: { partitionId } })
}

/**
 * 分区居家布防
 * @param partitionId 分区ID
 */
export const armPartitionEmergency = (partitionId: number) => {
  return request.put({ url: '/iot/alarm/host/partition/arm-emergency', params: { partitionId } })
}

/**
 * 分区撤防
 * @param partitionId 分区ID
 */
export const disarmPartition = (partitionId: number) => {
  return request.put({ url: '/iot/alarm/host/partition/disarm', params: { partitionId } })
}

/**
 * 分区消警
 * @param partitionId 分区ID
 */
export const clearPartitionAlarm = (partitionId: number) => {
  return request.put({ url: '/iot/alarm/host/partition/clear-alarm', params: { partitionId } })
}

// ==================== 防区操作接口 ====================

/**
 * 单防区布防
 * @param hostId 主机ID
 * @param zoneNo 防区号
 */
export const armZone = (hostId: number, zoneNo: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/zone/arm', params: { hostId, zoneNo } })
}

/**
 * 单防区撤防
 * @param hostId 主机ID
 * @param zoneNo 防区号
 */
export const disarmZone = (hostId: number, zoneNo: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/zone/disarm', params: { hostId, zoneNo } })
}

/**
 * 防区旁路
 * @param hostId 主机ID
 * @param zoneNo 防区号
 */
export const bypassZone = (hostId: number, zoneNo: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/zone/bypass', params: { hostId, zoneNo } })
}

/**
 * 撤销防区旁路
 * @param hostId 主机ID
 * @param zoneNo 防区号
 */
export const unbypassZone = (hostId: number, zoneNo: number): Promise<IotAlarmHostWithDetailsVO> => {
  return request.put({ url: '/iot/alarm/host/zone/unbypass', params: { hostId, zoneNo } })
}
