import request from '@/config/axios'

// ==================== 门禁可视化 Dashboard API ====================
//
// 后端：cn.iocoder.yudao.module.iot.controller.admin.access.AccessDashboardController
//   GET /admin-api/iot/access/dashboard/{statistics,real-time,trend,device-status,heatmap,abnormal-events}
//
// 实现成熟度（截至 v26 / M2-A 接入时点，详见 docs/ibms-bidirectional-gap.md GAP-001）：
//   ✅ statistics       基于 access_record + access_alarm + ibms_device(_runtime) 真实数据
//   ✅ real-time        基于 access_record 倒序 LIMIT N 真实数据
//   ⚪ trend            后端 stub 返 []，需在 M2 后期补聚合实现
//   ⚪ device-status    后端 stub 返 {}
//   ⚪ heatmap          后端 stub 返 []
//   ⚪ abnormal-events  后端 stub 返 []
//
// 真实可用的"24h 进入趋势"现走 access-record/statistics/hourly（见 record.ts），不走 dashboard/trend。

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

// ---- trend / device-status / heatmap / abnormal-events (M2 stub) ---------

export const getAccessTrend = (params: { startTime: string; endTime: string; type?: string }) => {
  return request.get<unknown>({
    url: '/iot/access/dashboard/trend',
    params
  })
}

export const getAccessDeviceStatusOverview = () => {
  return request.get<Record<string, unknown>>({
    url: '/iot/access/dashboard/device-status'
  })
}

export const getAccessHeatmap = (params: { date: string; type?: string }) => {
  return request.get<unknown>({
    url: '/iot/access/dashboard/heatmap',
    params
  })
}

export const getAccessAbnormalEvents = (params: { pageSize?: number; level?: string } = {}) => {
  return request.get<unknown>({
    url: '/iot/access/dashboard/abnormal-events',
    params
  })
}
