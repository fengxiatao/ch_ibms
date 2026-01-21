import request from '@/config/axios'

// 巡更记录VO
export interface PatrolRecordVO {
  id?: number
  taskId: number
  pointId: number
  pointName?: string
  recordTime?: Date
  executorId?: number
  executorName?: string
  status: number // 1-正常，2-异常
  remark?: string
  imageUrls?: string[]
  longitude?: number
  latitude?: number
  altitude?: number
  isTimeout?: boolean
  timeoutDuration?: number
  createTime?: Date
}

// 分页请求VO
export interface PatrolRecordPageReqVO extends PageParam {
  taskId?: number
  pointId?: number
  executorId?: number
  status?: number
  isTimeout?: boolean
  createTime?: Date[]
}

// 提交请求VO
export interface PatrolRecordSubmitReqVO {
  taskId: number
  pointId: number
  executorId: number
  remark?: string
  imageUrls?: string[]
  longitude?: number
  latitude?: number
  altitude?: number
}

// 获取巡更记录分页
export const getPatrolRecordPage = (params: PatrolRecordPageReqVO) => {
  return request.get<PageResult<PatrolRecordVO>>({
    url: '/iot/patrol-record/page',
    params
  })
}

// 提交巡更记录
export const submitPatrolRecord = (data: PatrolRecordSubmitReqVO) => {
  return request.post({
    url: '/iot/patrol-record/submit',
    data
  })
}


























