import request from '@/config/axios'
import dayjs from 'dayjs'

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

type BackendParkingPresentPageReq = {
  pageNo: number
  pageSize: number
  lotId?: number
  plateNumber?: string
  vehicleType?: number
  vehicleCategory?: string
  longTermFlag?: number
  entryTime?: [string, string]
}

type BackendParkingPresentResp = {
  id: number
  lotId: number
  entryLaneId?: number
  plateNumber?: string
  vehicleType?: number
  vehicleCategory?: string
  entryTime?: string
  entryPhotoUrl?: string
  parkingDuration?: number
  longTermFlag?: number
  status?: number
  remark?: string
}

type PageResult<T> = { list: T[]; total: number }

const mapVehicleCategory = (v?: string) => {
  if (!v) return undefined
  if (v === 'temp') return 'temporary'
  if (v === 'monthly') return 'monthly'
  if (v === 'noplate') return undefined
  return undefined
}

const mapVehicleTypeForView = (category?: string) => {
  if (category === 'temporary') return 'temp'
  if (category === 'monthly') return 'monthly'
  if (category === 'free') return 'inner'
  return 'temp'
}

const normalizeDateTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return undefined
  const raw = String(value).trim()
  if (/^\d+$/.test(raw)) {
    const num = Number(raw)
    const ms = raw.length <= 10 ? num * 1000 : num
    return dayjs(ms).isValid() ? dayjs(ms).format('YYYY-MM-DD HH:mm:ss') : raw
  }
  return dayjs(raw).isValid() ? dayjs(raw).format('YYYY-MM-DD HH:mm:ss') : raw
}

const mapPresentRow = (row: BackendParkingPresentResp): ParkingPresentRespVO => {
  const plateNo = row.plateNumber || ''
  const warningType = !plateNo ? 3 : row.longTermFlag === 1 ? 2 : undefined
  return {
    id: row.id,
    lotId: row.lotId,
    entryLaneId: row.entryLaneId,
    plateNo: plateNo || undefined,
    vehicleType: mapVehicleTypeForView(row.vehicleCategory),
    inTime: normalizeDateTime(row.entryTime),
    durationMinutes: row.parkingDuration,
    warningType,
    snapshotUrl: row.entryPhotoUrl || undefined,
    remark: row.remark
  }
}

export const ParkingPresentApi = {
  getPage: async (params: ParkingPresentPageReqVO) => {
    const backendParams: BackendParkingPresentPageReq = {
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      lotId: params.lotId,
      plateNumber: params.plateNo || undefined,
      vehicleCategory: mapVehicleCategory(params.vehicleType),
      entryTime:
        params.beginInTime && params.endInTime ? [params.beginInTime, params.endInTime] : undefined
    }
    const page = await request.get<PageResult<BackendParkingPresentResp>>({
      url: '/iot/parking/record/present/page',
      params: backendParams
    })
    return {
      list: (page.list || []).map(mapPresentRow),
      total: page.total || 0
    }
  },

  forceOut: async (ids: number[]) => {
    return await request.post({ url: '/iot/parking/record/present/force-exit', data: ids })
  }
}

