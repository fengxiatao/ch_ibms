import request from '@/config/axios'

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

export interface ParkingRecordUpdateReqVO {
  id: number
  inTime?: string
  outTime?: string
  feePaid?: number
  payStatus?: number
  payChannel?: string
  remark?: string
}

export const ParkingRecordApi = {
  getPage: (params: ParkingRecordPageReqVO) => {
    return request.get<{ list: ParkingRecordRespVO[]; total: number }>({
      url: '/iot/parking/record/page',
      params
    })
  },
  update: (data: ParkingRecordUpdateReqVO) => {
    return request.put({
      url: '/iot/parking/record/update',
      data
    })
  }
}

