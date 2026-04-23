import request from '@/config/axios'

export interface FactoryDashboardMetricItem {
  value: number
  unit: string
  trend: number | null
  total?: number
  online?: number
  offline?: number
  inactive?: number
  unhandled?: number
  handled?: number
  qualified?: number
  electricity?: number
  water?: number
  gas?: number
  statDate?: string | null
}

export interface FactoryDashboardOverviewData {
  kpis: {
    deviceOnlineRate: FactoryDashboardMetricItem
    alarmCount: FactoryDashboardMetricItem
    environmentComplianceRate: FactoryDashboardMetricItem
    todayEnergy: FactoryDashboardMetricItem
  }
  floors: Array<{
    id: number
    code: string
    name: string
    sort: number
  }>
  deviceStatusList: Array<{
    id: number
    name: string
    nickname?: string | null
    location: string
    floorId?: number | null
    floorCode?: string | null
    floorName?: string | null
    status: string
    online: boolean
    systemCode?: string | null
    deviceTypeCode?: string | null
  }>
  latestAlerts: Array<{
    id: number
    title: string
    level?: number | null
    levelLabel: string
    deviceId?: number | null
    deviceName: string
    location: string
    floorId?: number | null
    floorCode?: string | null
    floorName?: string | null
    handled: boolean
    alarmTime?: string | null
  }>
  scene: {
    currentFloorId?: number | null
    currentFloorName?: string | null
    title: string
    description: string
    actions: Array<{
      key: string
      label: string
      enabled: boolean
      actionType: 'route' | 'message'
      target: string
    }>
  }
  videoSnapshot: {
    total: number
    online: number
    primarySource?: {
      id: number
      deviceId?: number | null
      name: string
      location: string
      floorId?: number | null
      floorCode?: string | null
      floorName?: string | null
      status?: string | null
    } | null
    sources: Array<{
      id: number
      deviceId?: number | null
      name: string
      location: string
      floorId?: number | null
      floorCode?: string | null
      floorName?: string | null
      status?: string | null
    }>
  }
  energyTrend: Array<{
    date: string
    electricity: number
    water: number
    gas: number
  }>
  environmentSnapshot: {
    temperature?: number | null
    humidity?: number | null
    pm25?: number | null
    co2?: number | null
    differentialPressure?: number | null
    cleanliness?: string | null
    qualified: number
    total: number
    collectedAt?: string | null
    location?: string | null
  }
}

export const getFactoryDashboardOverviewData = () => {
  return request.get<FactoryDashboardOverviewData>({ url: '/iot/factory/overview' })
}
