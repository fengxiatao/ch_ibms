import request from '@/config/axios'

export interface CloudDefenseOverviewData {
  updatedAt?: string | null
  metrics: {
    armedAreaCount: number
    totalAreaCount: number
    onlineDeviceCount: number
    totalDeviceCount: number
    todayAlertCount: number
    safetyScore: number
    safetyLevel: string
  }
  modes: Array<{
    id: number
    code: string
    name: string
    icon?: string | null
    statusText?: string | null
    enabled: boolean
  }>
  activeModeCode: string
  topology: {
    title: string
    legends: Array<{
      key: string
      label: string
      color: string
    }>
    areas: Array<{
      id: number
      code: string
      name: string
      type?: string | null
      x: number
      y: number
      width: number
      height: number
      armed: boolean
      alarming: boolean
      deviceCount: number
      zoneCount: number
      detailText?: string | null
    }>
    points: Array<{
      id: number
      areaId: number
      deviceId?: number | null
      channelId?: number | null
      code: string
      name: string
      type?: string | null
      x: number
      y: number
      armed: boolean
      alarming: boolean
      online: boolean
    }>
  }
  deviceList: Array<{
    id: number
    areaId: number
    areaName: string
    name: string
    typeLabel: string
    location: string
    online: boolean
    capabilityTags: string[]
  }>
  zoneList: Array<{
    id: number
    areaId: number
    name: string
    deviceCount: number
    onlineDeviceCount: number
    zoneCount: number
    armedZoneCount: number
    alarmingZoneCount: number
    statusText: string
    healthText: string
    actionText: string
  }>
}

export const getCloudDefenseOverview = () => {
  return request.get<CloudDefenseOverviewData>({ url: '/iot/factory/cloud-defense/overview' })
}
