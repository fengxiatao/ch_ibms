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

export const getParkingDashboardOverview = (lotId?: number) => {
  return request.get<ParkingDashboardOverviewRespVO>({
    url: '/iot/parking/statistics/overview',
    params: { lotId }
  })
}

