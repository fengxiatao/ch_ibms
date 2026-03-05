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

// ==================== 首页大屏数据 ====================

/** 设备状态统计 */
export interface DeviceStatusStats {
  total: number
  online: number
  offline: number
  alarm: number
  fault: number
  onlineRate: number
}

/** 告警项 */
export interface AlarmItem {
  title: string
  location: string
  time: string
  level: 'danger' | 'warning'
}

/** 安防数据 */
export interface SecurityData {
  channelOnlineRate: number
  channelOnline: number
  channelOffline: number
  storageOnlineRate: number
  storageOnline: number
  storageOffline: number
  serverOnlineRate: number | null
  serverOnline: number
  serverOffline: number
  unhandledAlarms: number
  alarmTrend: number[]
  recentAlarms: AlarmItem[]
  patrolRate: number
  patrolCompleted: number
  patrolTotal: number
  patrolAbnormal: number
  patrolMissed: number
}

/** 通行趋势 */
export interface AccessTrend {
  time: string
  entry: number
  exit: number
}

/** 通行数据 */
export interface AccessData {
  doorStatus: string
  todayEntry: number
  todayExit: number
  accessTrend: AccessTrend[]
  visitorBooked: number
  visitorVisiting: number
  visitorLeft: number
  parkingRate: number
  parkingUsed: number
  parkingRemaining: number
  parkingTotal: number
}

/** 能源数据 */
export interface EnergyData {
  todayElectricity: number
  electricityChange: number
  electricityTrend: number[]
  todayWater: number
  waterChange: number
  electricityCost: number
  waterCost: number
  gasCost: number
}

/** 楼宇环境数据 */
export interface BuildingEnvData {
  temperature: number
  humidity: number
  airQuality: string
  pm25: number
  co2: number
  deviceOnlineRate: number
  deviceLoadRate: number
}

/** 最新告警 */
export interface LatestAlarm {
  id: number
  title: string
  deviceName: string
  location: string
  level: 'critical' | 'warning' | 'info'
  time: string
}

/** 首页大屏数据 */
export interface HomeScreenVO {
  // 顶部统计
  deviceTotal: number
  monthlyEnergy: number
  abnormalDevices: number
  todayAlerts: number
  // 设备状态统计
  deviceStatusStats: DeviceStatusStats
  // 安防数据
  securityData: SecurityData
  // 通行数据
  accessData: AccessData
  // 能源数据
  energyData: EnergyData
  // 楼宇环境
  buildingEnvData: BuildingEnvData
  // 最新告警
  latestAlarms: LatestAlarm[]
}

// 获取首页大屏数据
export const getHomeScreenData = () => {
  return request.get<HomeScreenVO>({ url: '/iot/dashboard/home-screen' })
}












