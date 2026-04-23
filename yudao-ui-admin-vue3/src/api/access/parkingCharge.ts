import request from '@/config/axios'
import dayjs from 'dayjs'

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
  vehicleCategory?: string
  entryTime?: string
  entryOperator?: string
  exitTime?: string
  exitOperator?: string
  parkingDuration?: number
  paidAmount?: number
  paymentMethod?: string
  paymentTime?: string
  paymentStatus?: number
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
    gateName: '-',
    feePaid: Number(row.paidAmount || 0),
    payStatus: row.paymentStatus ?? 0,
    payChannel: row.paymentMethod || undefined,
    payTime: row.paymentTime ? normalizeDateTime(row.paymentTime) : undefined,
    operator: operator || undefined,
    abnormal: false,
    remark: row.remark || undefined
  }
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
  manualPay: (data: ParkingRecordManualPayReqVO) => {
    return request.post<number>({
      url: '/iot/parking/record/manual-pay',
      data
    })
  }
}

