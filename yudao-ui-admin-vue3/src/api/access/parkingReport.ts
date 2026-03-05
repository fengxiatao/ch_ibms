import request from '@/config/axios'

export interface ParkingDurationRow {
  bucket: string
  count: number
  rate: number
  avgFee: number
  income: number
}

export interface ParkingDurationSummary {
  shortCount: number
  midCount: number
  longCount: number
  avgText: string
}

export interface ParkingPeakRow {
  period: string
  inCount: number
  outCount: number
  net: number
  avgTime: string
  congestion: string
}

export interface ParkingPeakSummary {
  morning: string
  evening: string
  normal: string
  low: string
}

export interface ParkingCarTypeRow {
  type: string
  count: number
  rate: number
  monthlyAvgText: string
  income: number
  incomeRate: number
}

export interface ParkingCarTypeSummary {
  fixed: number
  temp: number
  free: number
  fixedIncomeRate: string
}

export interface ParkingRevenueRow {
  period: string
  total: number
  momText: string
  yoyText: string
  avgText: string
}

export interface ParkingRevenueSummary {
  avgIncome: string
  weekendVsWorkday: string
  holidayGrowth: string
  monthlyGrowth: string
}

export interface ParkingReportOverviewRespVO {
  durationRows: ParkingDurationRow[]
  durationSummary: ParkingDurationSummary
  peakRows: ParkingPeakRow[]
  peakSummary: ParkingPeakSummary
  carTypeRows: ParkingCarTypeRow[]
  carTypeSummary: ParkingCarTypeSummary
  revenueRows: ParkingRevenueRow[]
  revenueSummary: ParkingRevenueSummary
}

export interface ParkingReportQueryReqVO {
  lotId?: number
  startDate?: string
  endDate?: string
  granularity?: 'day' | 'week' | 'month'
}

export const ParkingReportApi = {
  getOverview: (params: ParkingReportQueryReqVO) => {
    return request.get<ParkingReportOverviewRespVO>({
      url: '/iot/parking/report/overview',
      params
    })
  }
}

