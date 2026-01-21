import request from '@/config/axios'

// ===================== 视频巡更任务 =====================

// 场景配置接口
export interface PatrolSceneVO {
  sceneId?: number
  sceneName: string
  sceneOrder: number
  duration: number
  gridLayout: '1x1' | '2x2' | '3x3' | '4x4'
  gridCount: number
  channels: ChannelConfigVO[]
}

// 通道配置接口
export interface ChannelConfigVO {
  gridPosition: number
  cameraId?: number | null
  cameraName?: string
  cameraCode?: string
  streamUrl?: string
  streamType?: number
  isEmpty?: boolean
  // ✅ 播放所需的完整信息
  nvrId?: number
  channelNo?: number
  ipAddress?: string  // 从 config 中提取的 IP 地址
  nvrIp?: string
  nvrPort?: number
  ptzSupport?: boolean
}

export interface VideoPatrolTaskVO {
  id?: number
  taskName: string
  taskCode: string
  
  // 新增字段：场景配置
  startTime?: string
  endTime?: string
  loopMode?: number
  executor?: number  // 负责人ID（用户ID）
  executorName?: string  // 负责人姓名（冗余字段）
  patrolScenes?: PatrolSceneVO[]
  
  // 运行状态
  runningStatus?: 'stopped' | 'running' | 'paused' | 'trial'  // 运行状态：未启动/运行中/已暂停/试运行
  
  // 旧字段（兼容）
  pointIds?: number[]
  scheduleType?: number
  scheduleConfig?: any
  timeSlots?: any[]
  intervalMinutes?: number
  autoSnapshot?: boolean
  autoRecording?: boolean
  recordingDuration?: number
  aiAnalysis?: boolean
  alertOnAbnormal?: boolean
  alertUserIds?: number[]
  startDate?: string
  endDate?: string
  description?: string
  status?: number
}

export interface VideoPatrolTaskPageReqVO extends PageParam {
  taskName?: string
  taskCode?: string
  scheduleType?: number
  status?: number
  startDate?: string[]
  endDate?: string[]
}

// 创建视频巡更任务
export const createVideoPatrolTask = (data: VideoPatrolTaskVO) => {
  return request.post({ url: '/iot/video-patrol/task/create', data })
}

// 更新视频巡更任务
export const updateVideoPatrolTask = (data: VideoPatrolTaskVO) => {
  return request.put({ url: '/iot/video-patrol/task/update', data })
}

// 删除视频巡更任务
export const deleteVideoPatrolTask = (id: number) => {
  return request.delete({ url: '/iot/video-patrol/task/delete?id=' + id })
}

// 获得视频巡更任务
export const getVideoPatrolTask = (id: number) => {
  return request.get({ url: '/iot/video-patrol/task/get?id=' + id })
}

// 获得视频巡更任务分页
export const getVideoPatrolTaskPage = (params: VideoPatrolTaskPageReqVO) => {
  return request.get({ url: '/iot/video-patrol/task/page', params })
}

// 更新视频巡更任务状态
export const updateVideoPatrolTaskStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/video-patrol/task/update-status?id=${id}&status=${status}` })
}

// 启动视频巡更任务
export const startVideoPatrolTask = (id: number, params?: { trial?: boolean }) => {
  const trial = params?.trial ? '&trial=true' : ''
  return request.post({ url: `/iot/video-patrol/task/start?id=${id}${trial}` })
}

// 暂停视频巡更任务
export const pauseVideoPatrolTask = (id: number) => {
  return request.post({ url: `/iot/video-patrol/task/pause?id=${id}` })
}

// ===================== 视频巡更记录 =====================

export interface VideoPatrolRecordPageReqVO extends PageParam {
  taskId?: number
  pointId?: number
  cameraId?: number
  aiStatus?: number
  manualStatus?: number
  isAbnormal?: boolean
  handled?: boolean
  startTime?: string
  endTime?: string
}

// 获得视频巡更记录
export const getVideoPatrolRecord = (id: number) => {
  return request.get({ url: '/iot/video-patrol/record/get?id=' + id })
}

// 获得视频巡更记录分页
export const getVideoPatrolRecordPage = (params: VideoPatrolRecordPageReqVO) => {
  return request.get({ url: '/iot/video-patrol/record/page', params })
}

// 人工确认视频巡更记录
export const manualConfirm = (id: number, status: number, remark?: string) => {
  return request.put({ 
    url: `/iot/video-patrol/record/manual-confirm`,
    params: { id, status, remark }
  })
}

// 标记为已处理
export const markAsHandled = (id: number, remark?: string) => {
  return request.put({ 
    url: `/iot/video-patrol/record/mark-handled`,
    params: { id, remark }
  })
}

// ===================== 视频巡更点位 =====================

export interface VideoPatrolPointVO {
  id?: number
  pointName: string
  pointCode: string
  cameraId: number
  cameraName?: string
  buildingId?: number
  floorId?: number
  areaId?: number
  location?: string
  checkItems?: string[]
  aiRules?: any
  snapshotDuration?: number
  description?: string
  status?: number
  sort?: number
}

export interface VideoPatrolPointPageReqVO extends PageParam {
  pointName?: string
  pointCode?: string
  cameraId?: number
  buildingId?: number
  floorId?: number
  areaId?: number
  status?: number
}

// 创建视频巡更点位
export const createVideoPatrolPoint = (data: VideoPatrolPointVO) => {
  return request.post({ url: '/iot/video-patrol/point/create', data })
}

// 更新视频巡更点位
export const updateVideoPatrolPoint = (data: VideoPatrolPointVO) => {
  return request.put({ url: '/iot/video-patrol/point/update', data })
}

// 删除视频巡更点位
export const deleteVideoPatrolPoint = (id: number) => {
  return request.delete({ url: '/iot/video-patrol/point/delete?id=' + id })
}

// 获得视频巡更点位
export const getVideoPatrolPoint = (id: number) => {
  return request.get({ url: '/iot/video-patrol/point/get?id=' + id })
}

// 获得视频巡更点位分页
export const getVideoPatrolPointPage = (params: VideoPatrolPointPageReqVO) => {
  return request.get({ url: '/iot/video-patrol/point/page', params })
}

// 获得所有启用的视频巡更点位
export const getAllEnabledVideoPatrolPoints = () => {
  return request.get({ url: '/iot/video-patrol/point/list-all-enabled' })
}

// 更新视频巡更点位状态
export const updateVideoPatrolPointStatus = (id: number, status: number) => {
  return request.put({ url: `/iot/video-patrol/point/update-status?id=${id}&status=${status}` })
}

// ===================== 视频巡检任务 API（新版）=====================
// 与原有的"视频巡更任务"（VideoPatrolTask）区分
// 命名规范：InspectionTask（巡检任务）vs PatrolTask（巡更任务）
// API路径：/iot/video-inspection/* vs /iot/video-patrol/*

// 导入类型定义
import type { InspectionTask, InspectionTaskListQuery } from './types'

// 获取巡检任务列表
export const getInspectionTaskList = (params?: InspectionTaskListQuery) => {
  return request.get({ url: '/iot/video-inspection/task/list', params })
}

// 创建巡检任务
export const createInspectionTask = (data: InspectionTask) => {
  return request.post({ url: '/iot/video-inspection/task/create', data })
}

// 更新巡检任务
export const updateInspectionTask = (id: number, data: Partial<InspectionTask>) => {
  return request.put({ url: `/iot/video-inspection/task/update/${id}`, data })
}

// 删除巡检任务
export const deleteInspectionTask = (id: number) => {
  return request.delete({ url: `/iot/video-inspection/task/delete/${id}` })
}

// 获取单个巡检任务
export const getInspectionTask = (id: number) => {
  return request.get({ url: `/iot/video-inspection/task/get/${id}` })
}

