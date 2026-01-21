import request from '@/config/axios'

export interface IotAlarmOperationLogVO {
  id?: number
  hostId?: number
  hostName?: string
  partitionId?: number
  partitionName?: string
  zoneId?: number
  zoneName?: string
  operationType: string
  operationTime: Date
  operatorId?: number
  operatorName?: string
  result?: string
  errorMessage?: string
  requestId?: string
  createTime?: Date
}

export interface IotAlarmOperationLogPageReqVO extends PageParam {
  hostId?: number
  partitionId?: number
  zoneId?: number
  operationType?: string
  operationTime?: Date[]
}

// 查询报警主机操作记录分页
export const getOperationLogPage = (params: IotAlarmOperationLogPageReqVO) => {
  return request.get({ url: '/iot/alarm/operation-log/page', params })
}
