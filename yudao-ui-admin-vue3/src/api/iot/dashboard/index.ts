import request from '@/config/axios'

export interface DeviceStatisticsVO {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  faultDevices: number
  onlineRate: number
  devicesByProduct: Record<string, number>
  devicesByStatus: Record<string, number>
  todayNewDevices: number
  weekNewDevices: number
}

export interface AlertStatisticsVO {
  totalAlerts: number
  todayAlerts: number
  weekAlerts: number
  monthAlerts: number
  unhandledAlerts: number
  handledAlerts: number
  alertsByLevel: Record<string, number>
  alertsByType: Record<string, number>
  alertTrend: Array<{
    date: string
    count: number
  }>
  handledRate: number
}

export interface RealTimeMonitorVO {
  latestAlerts: Array<{
    id: number
    alertName: string
    deviceName: string
    level: string
    alertTime: string
    status: number
  }>
  deviceStatusChanges: Array<{
    deviceId: number
    deviceName: string
    oldStatus: string
    newStatus: string
    changeTime: string
  }>
  latestEvents: Array<{
    id: number
    eventType: string
    deviceName: string
    eventTime: string
    eventData: string
  }>
  systemLoad: {
    cpuUsage: number
    memoryUsage: number
    diskUsage: number
    messageQueueBacklog: number
    databaseConnections: number
  }
}

// 获取设备统计数据
export const getDeviceStatistics = () => {
  return request.get<DeviceStatisticsVO>({ url: '/iot/dashboard/device-statistics' })
}

// 获取告警统计数据
export const getAlertStatistics = () => {
  return request.get<AlertStatisticsVO>({ url: '/iot/dashboard/alert-statistics' })
}

// 获取实时监控数据
export const getRealTimeMonitor = () => {
  return request.get<RealTimeMonitorVO>({ url: '/iot/dashboard/real-time-monitor' })
}












