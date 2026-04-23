import request from '@/config/axios'

export interface ParkingDashboardOverviewRespVO {
  totalSpaces: number
  usedSpaces: number
  freeSpaces: number
  tempVehicleCount: number
  monthlyVehicleCount: number
  usageRate: number
  todayInCount: number
  todayOutCount: number
  todayIncome: number
  alertCount: number
}

type BackendParkingOverview = {
  lotCount: number
  totalSpaces: number
  presentVehicleCount: number
  availableSpaces: number
  overallOccupancyRate: number
  todayEntryCount: number
  todayExitCount: number
  todayIncome: number
  monthlyVehicleCount: number
  freeVehicleCount: number
  lotStatisticsList?: Array<{
    lotId: number
    lotName: string
    totalSpaces: number
    presentCount: number
    availableSpaces: number
    occupancyRate: number
  }>
}

type BackendPresentStats = {
  totalCount: number
  temporaryCount: number
  monthlyCount: number
  freeCount: number
  totalSpaces: number
  availableSpaces: number
  occupancyRate: number
  longTermCount: number
}

export const getParkingDashboardOverview = async (lotId?: number) => {
  const [overview, present] = await Promise.all([
    request.get<BackendParkingOverview>({
      url: '/iot/parking/statistics/overview',
      params: { lotId }
    }),
    request.get<BackendPresentStats>({
      url: '/iot/parking/statistics/present',
      params: { lotId }
    })
  ])

  const lot = lotId
    ? (overview.lotStatisticsList || []).find((item) => Number(item.lotId) === Number(lotId))
    : undefined

  const totalSpaces = lot?.totalSpaces ?? present.totalSpaces ?? overview.totalSpaces ?? 0
  const usedSpaces = lot?.presentCount ?? present.totalCount ?? overview.presentVehicleCount ?? 0
  const freeSpaces = lot?.availableSpaces ?? present.availableSpaces ?? overview.availableSpaces ?? 0
  const usageRateRaw = lot?.occupancyRate ?? present.occupancyRate ?? overview.overallOccupancyRate ?? 0

  const result: ParkingDashboardOverviewRespVO = {
    totalSpaces: Number(totalSpaces || 0),
    usedSpaces: Number(usedSpaces || 0),
    freeSpaces: Number(freeSpaces || 0),
    tempVehicleCount: Number(present.temporaryCount || 0),
    monthlyVehicleCount: Number(present.monthlyCount || overview.monthlyVehicleCount || 0),
    usageRate: Number(usageRateRaw || 0),
    todayInCount: Number(overview.todayEntryCount || 0),
    todayOutCount: Number(overview.todayExitCount || 0),
    todayIncome: Number(overview.todayIncome || 0),
    // 当前页面“异常记录”优先使用可解释且可对账的长停预警数
    alertCount: Number(present.longTermCount || 0)
  }
  return result
}

