import request from '@/config/axios'

export interface VideoPatrolScheduleVO {
  id?: number
  name: string
  patrolPlanId: number
  patrolPlanName?: string
  scheduleType: number
  startTime: string
  endTime: string
  weekDays?: string
  status: number
  remark?: string
  createTime?: Date
}

export interface VideoPatrolSchedulePageReqVO extends PageParam {
  name?: string
  patrolPlanId?: number
  scheduleType?: number
  status?: number
  createTime?: Date[]
}

// 分页查询定时轮巡计划
export const getVideoPatrolSchedulePage = (params: VideoPatrolSchedulePageReqVO) => {
  return request.get({ url: '/iot/video-patrol-schedule/page', params })
}

// 获取定时轮巡计划详情
export const getVideoPatrolSchedule = (id: number) => {
  return request.get({ url: `/iot/video-patrol-schedule/get?id=${id}` })
}

// 创建定时轮巡计划
export const createVideoPatrolSchedule = (data: VideoPatrolScheduleVO) => {
  return request.post({ url: '/iot/video-patrol-schedule/create', data })
}

// 更新定时轮巡计划
export const updateVideoPatrolSchedule = (data: VideoPatrolScheduleVO) => {
  return request.put({ url: '/iot/video-patrol-schedule/update', data })
}

// 删除定时轮巡计划
export const deleteVideoPatrolSchedule = (id: number) => {
  return request.delete({ url: `/iot/video-patrol-schedule/delete?id=${id}` })
}

// 更新定时轮巡计划状态
export const updateVideoPatrolScheduleStatus = (id: number, status: number) => {
  return request.put({ 
    url: '/iot/video-patrol-schedule/update-status', 
    data: { id, status } 
  })
}
