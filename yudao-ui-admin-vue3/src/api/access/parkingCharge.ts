import request from '@/config/axios'

export interface ParkingChargeRuleVO {
  id?: number
  lotId: number
  vehicleType: string // small/medium/large
  firstHourFee: number
  extraHourFee: number
  dailyCap: number
  enabled?: number
  remark?: string
}

export interface ParkingSystemConfigVO {
  id: number
  parkingName: string
  address?: string
  phone?: string
  totalSpaces: number
  businessHours?: string
  parkingType?: string
  remark?: string
}

export interface ParkingRecordPageReqVO {
  pageNo: number
  pageSize: number
  plateNo?: string
  vehicleType?: string
  payStatus?: number
  beginInTime?: string
  endInTime?: string
}

export interface ParkingRecordRespVO {
  id: number
  lotId: number
  plateNo: string
  vehicleType: string
  ioType: 'in' | 'out'
  inTime: string
  outTime?: string
  durationMinutes: number
  gateName: string
  feePaid: number
  payStatus: number
  payChannel?: string
  payTime?: string
  operator?: string
  abnormal?: boolean
  remark?: string
}

export interface ParkingRecordManualPayReqVO {
  lotId?: number
  plateNo: string
  vehicleType?: string
  inTime: string
  outTime: string
  durationMinutes?: number
  amount: number
  discountType?: string
  discountRate?: number
  payChannel?: string
  remark?: string
}

export const ParkingChargeApi = {
  // 收费规则
  getRules: (lotId?: number) => {
    const params = lotId ? { lotId } : {}
    return request.get<ParkingChargeRuleVO[]>({
      url: '/iot/parking/charge-rule/simple-list',
      params
    })
  },
  saveRules: (data: ParkingChargeRuleVO[]) => {
    return request.post({
      url: '/iot/parking/charge-rule/save-batch',
      data
    })
  },

  // 系统设置
  getSystemConfig: (lotId?: number) => {
    const params = lotId ? { lotId } : {}
    return request.get<ParkingSystemConfigVO>({
      url: '/iot/parking/system/get',
      params
    })
  },
  saveSystemConfig: (data: ParkingSystemConfigVO) => {
    return request.put({
      url: '/iot/parking/system/save',
      data
    })
  },

  // 出入场记录（用于缴费记录查询）
  getRecordPage: (params: ParkingRecordPageReqVO) => {
    return request.get<{ list: ParkingRecordRespVO[]; total: number }>({
      url: '/iot/parking/record/page',
      params
    })
  },
  manualPay: (data: ParkingRecordManualPayReqVO) => {
    return request.post<number>({
      url: '/iot/parking/record/manual-pay',
      data
    })
  }
}

