import request from '@/config/axios'

// ==================== 门禁可视化 Dashboard API ====================
//
// 后端：cn.iocoder.yudao.module.iot.controller.admin.access.AccessDashboardController
//   GET /admin-api/iot/access/dashboard/{statistics,real-time,trend,device-status,heatmap,abnormal-events}
//
// 实现成熟度（M2-D 落地后，2026-05-06）：
//   ✅ statistics       基于 iot_access_event_log + ibms_device(_runtime) 真实数据
//   ✅ real-time        基于 iot_access_event_log 倒序 LIMIT N 真实数据
//   ✅ trend            按日/小时聚合（同日返 24 槽，跨日返每日槽），返结构化对象
//   ✅ device-status    设备总数/在线/离线 + 按 systemCode 分布
//   ✅ heatmap          按 device × hour 分组（指定日期）
//   ✅ abnormal-events  ALARM+ABNORMAL event_type 联合最新 N 条
//
// 历史 GAP（已修复）：原实现注入 AccessRecordMapper/AccessAlarmMapper，DO 标注的
//   iot_access_record / iot_access_alarm 表不存在，所有查询运行时抛 SQLException。
//   已迁移到真实表 iot_access_event_log（IotAccessEventLogDO/Mapper），56448 行真数据。
//   详见 docs/ibms-bidirectional-gap.md GAP-001。

// ---- statistics ----------------------------------------------------------

export interface AccessDashboardDeviceStatusDistribution {
  online: number
  offline: number
  maintenance: number
  fault: number
}

export interface AccessDashboardAccessTypeDistribution {
  employee: number
  visitor: number
  vehicle: number
  elevator: number
}

export interface AccessDashboardStatisticsRespVO {
  todayAccessCount: number
  todayVisitorCount: number
  todayVehicleCount: number
  todayAlarmCount: number
  onlineDeviceCount: number
  totalDeviceCount: number
  currentVisitorCount: number
  occupiedParkingSpaces: number
  totalParkingSpaces: number
  accessCountGrowth: number
  visitorCountGrowth: number
  vehicleCountGrowth: number
  deviceStatusDistribution: AccessDashboardDeviceStatusDistribution
  accessTypeDistribution: AccessDashboardAccessTypeDistribution
}

export const getAccessDashboardStatistics = () => {
  return request.get<AccessDashboardStatisticsRespVO>({
    url: '/iot/access/dashboard/statistics'
  })
}

// ---- real-time -----------------------------------------------------------

export interface RealTimeAccessItem {
  id: number
  type: string
  userName?: string
  deviceName?: string
  location?: string
  time?: string
  result?: string
  photo?: string
}

export interface RealTimeAccessRespVO {
  records: RealTimeAccessItem[]
}

export const getRealTimeAccess = (pageSize: number = 10) => {
  return request.get<RealTimeAccessRespVO>({
    url: '/iot/access/dashboard/real-time',
    params: { pageSize }
  })
}

// ---- trend ---------------------------------------------------------------

export interface AccessTrendRespVO {
  /** 'hour' = 同日返 24 槽位；'day' = 跨日返逐日槽位 */
  granularity: 'hour' | 'day'
  /** 槽位标签：hour 模式如 '0:00'..'23:00'；day 模式如 '2026-04-15'.. */
  labels: string[]
  /** 每槽位通行总次数（含正常+告警+异常） */
  accessData: number[]
  /** 每槽位告警/异常事件次数 */
  rejectData: number[]
  /** 进门 (direction=1) 次数；DB direction 全空时回退到 accessData */
  inData: number[]
  /** 出门 (direction=2) 次数 */
  outData: number[]
}

export const getAccessTrend = (params: {
  startTime: string
  endTime: string
  type?: string
}) => {
  return request.get<AccessTrendRespVO>({
    url: '/iot/access/dashboard/trend',
    params
  })
}

// ---- device-status -------------------------------------------------------

export interface AccessDeviceStatusOverviewRespVO {
  total: number
  online: number
  offline: number
  maintenance: number
  fault: number
  bySystemCode: Record<string, number>
}

export const getAccessDeviceStatusOverview = () => {
  return request.get<AccessDeviceStatusOverviewRespVO>({
    url: '/iot/access/dashboard/device-status'
  })
}

// ---- heatmap -------------------------------------------------------------

export interface AccessHeatmapItem {
  deviceId: number
  deviceName: string
  hour: number
  cnt: number
}

export const getAccessHeatmap = (params: { date: string; type?: string }) => {
  return request.get<AccessHeatmapItem[]>({
    url: '/iot/access/dashboard/heatmap',
    params
  })
}

// ---- abnormal-events -----------------------------------------------------

export interface AccessAbnormalEventItem {
  id: number
  deviceId?: number
  deviceName?: string
  channelName?: string
  eventType: string
  eventDesc?: string
  eventTime: string
  failReason?: string
  personName?: string
  level: 'high' | 'medium' | 'low'
}

export const getAccessAbnormalEvents = (params: { pageSize?: number; level?: string } = {}) => {
  return request.get<AccessAbnormalEventItem[]>({
    url: '/iot/access/dashboard/abnormal-events',
    params
  })
}
