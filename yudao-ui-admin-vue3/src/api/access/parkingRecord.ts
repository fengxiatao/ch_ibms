import request from '@/config/axios'
import dayjs from 'dayjs'

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
  inPhotoUrl?: string
  outPhotoUrl?: string
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

type BackendParkingRecordPageReq = {
  pageNo: number
  pageSize: number
  plateNumber?: string
  paymentStatus?: number
  entryTime?: [string, string]
}

type BackendParkingRecordResp = {
  id: number
  lotId: number
  plateNumber: string
  vehicleType?: number
  vehicleCategory?: string
  entryTime?: string
  entryPhotoUrl?: string
  entryOperator?: string
  exitTime?: string
  exitPhotoUrl?: string
  exitOperator?: string
  parkingDuration?: number
  paidAmount?: number
  paymentMethod?: string
  paymentTime?: string
  paymentStatus?: number
  recordStatus?: number
  remark?: string
}

type PageResult<T> = { list: T[]; total: number }

const normalizeDateTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return ''
  const raw = String(value).trim()
  if (/^\d+$/.test(raw)) {
    const num = Number(raw)
    const ms = raw.length <= 10 ? num * 1000 : num
    return dayjs(ms).isValid() ? dayjs(ms).format('YYYY-MM-DD HH:mm:ss') : raw
  }
  return dayjs(raw).isValid() ? dayjs(raw).format('YYYY-MM-DD HH:mm:ss') : raw
}

const mapRecordRow = (row: BackendParkingRecordResp): ParkingRecordRespVO => {
  const ioType: 'in' | 'out' = row.exitTime ? 'out' : 'in'
  const operator = row.exitTime ? row.exitOperator : row.entryOperator
  return {
    id: row.id,
    lotId: row.lotId,
    plateNo: row.plateNumber,
    // 这里页面使用 temp/monthly/... 作为车辆类型展示；后端暂存的是 vehicleCategory（temporary/monthly/free）
    vehicleType:
      row.vehicleCategory === 'temporary'
        ? 'temp'
        : row.vehicleCategory === 'monthly'
          ? 'monthly'
          : row.vehicleCategory === 'free'
            ? 'inner'
            : 'temp',
    ioType,
    inTime: normalizeDateTime(row.entryTime),
    outTime: row.exitTime ? normalizeDateTime(row.exitTime) : undefined,
    durationMinutes: row.parkingDuration || 0,
    gateName: '-', // 后端 VO 未返回 gateName，先用占位，避免页面空列
    feePaid: Number(row.paidAmount || 0),
    payStatus: row.paymentStatus ?? 0,
    payChannel: row.paymentMethod || undefined,
    payTime: row.paymentTime ? normalizeDateTime(row.paymentTime) : undefined,
    operator: operator || undefined,
    inPhotoUrl: row.entryPhotoUrl || undefined,
    outPhotoUrl: row.exitPhotoUrl || undefined,
    abnormal: false,
    remark: row.remark || undefined
  }
}

export const ParkingRecordApi = {
  getPage: (params: ParkingRecordPageReqVO) => {
    const backendParams: BackendParkingRecordPageReq = {
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      plateNumber: params.plateNo || undefined,
      paymentStatus: params.payStatus,
      entryTime:
        params.beginInTime && params.endInTime ? [params.beginInTime, params.endInTime] : undefined
    }
    return request
      .get<PageResult<BackendParkingRecordResp>>({
        url: '/iot/parking/record/page',
        params: backendParams
      })
      .then((page) => ({
        list: (page.list || []).map(mapRecordRow),
        total: page.total || 0
      }))
  },
  update: (data: ParkingRecordUpdateReqVO) => {
    return request.put({
      url: '/iot/parking/record/update',
      data
    })
  }
}

