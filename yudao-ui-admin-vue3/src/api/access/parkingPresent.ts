import request from '@/config/axios'

export interface ParkingPresentPageReqVO {
  pageNo: number
  pageSize: number
  lotId?: number
  plateNo?: string
  vehicleType?: string
  status?: number
  beginInTime?: string
  endInTime?: string
}

export interface ParkingPresentRespVO {
  id: number
  lotId: number
  entryLaneId?: number
  plateNo?: string
  vehicleType?: string
  inTime?: string
  spaceNo?: string
  durationMinutes?: number
  expectedFee?: number
  feeStatus?: number
  status?: number
  warningType?: number
  snapshotUrl?: string
  remark?: string
}

export const ParkingPresentApi = {
  getPage: async (params: ParkingPresentPageReqVO) => {
    return await request.get({ url: '/iot/parking/record/present/page', params })
  },

  forceOut: async (ids: number[]) => {
    return await request.post({ url: '/iot/parking/record/present/force-exit', data: ids })
  }
}

