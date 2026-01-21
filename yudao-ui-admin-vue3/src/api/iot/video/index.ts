import request from '@/config/axios'

/**
 * ====================================
 * 摄像头预设点 API
 * ====================================
 */

// 预设点保存请求参数
export interface CameraPresetSaveReqVO {
  id?: number
  channelId: number
  presetNo: number
  presetName: string
  description?: string
  pan?: number
  tilt?: number
  zoom?: number
}

// 预设点响应VO
export interface CameraPresetRespVO {
  id: number
  channelId: number
  presetNo: number
  presetName: string
  description?: string
  pan?: number
  tilt?: number
  zoom?: number
  createTime: string
}

// 创建预设点
export const createCameraPreset = (data: CameraPresetSaveReqVO) => {
  return request.post({ url: '/iot/camera/preset/create', data })
}

// 更新预设点
export const updateCameraPreset = (data: CameraPresetSaveReqVO) => {
  return request.put({ url: '/iot/camera/preset/update', data })
}

// 删除预设点
export const deleteCameraPreset = (id: number) => {
  return request.delete({ url: '/iot/camera/preset/delete?id=' + id })
}

// 获取预设点详情
export const getCameraPreset = (id: number) => {
  return request.get<CameraPresetRespVO>({ url: '/iot/camera/preset/get?id=' + id })
}

// 获取通道的所有预设点
export const getCameraPresetListByChannel = (channelId: number) => {
  return request.get<CameraPresetRespVO[]>({ url: '/iot/camera/preset/list-by-channel?channelId=' + channelId })
}

/**
 * ====================================
 * 摄像头巡航 API
 * ====================================
 */

// 巡航点保存请求参数
export interface CameraCruisePointSaveReqVO {
  presetId: number
  sortOrder: number
  dwellTime?: number
}

// 巡航路线保存请求参数
export interface CameraCruiseSaveReqVO {
  id?: number
  channelId: number
  cruiseName: string
  description?: string
  dwellTime: number
  loopEnabled: boolean
  points: CameraCruisePointSaveReqVO[]
}

// 巡航点响应VO
export interface CameraCruisePointRespVO {
  id: number
  presetId: number
  presetNo: number
  presetName: string
  sortOrder: number
  dwellTime?: number
}

// 巡航路线响应VO
export interface CameraCruiseRespVO {
  id: number
  channelId: number
  cruiseName: string
  description?: string
  status: number
  dwellTime: number
  loopEnabled: boolean
  createTime: string
  points: CameraCruisePointRespVO[]
}

// 创建巡航路线
export const createCameraCruise = (data: CameraCruiseSaveReqVO) => {
  return request.post({ url: '/iot/camera/cruise/create', data })
}

// 更新巡航路线
export const updateCameraCruise = (data: CameraCruiseSaveReqVO) => {
  return request.put({ url: '/iot/camera/cruise/update', data })
}

// 删除巡航路线
export const deleteCameraCruise = (id: number) => {
  return request.delete({ url: '/iot/camera/cruise/delete?id=' + id })
}

// 获取巡航路线详情
export const getCameraCruise = (id: number) => {
  return request.get<CameraCruiseRespVO>({ url: '/iot/camera/cruise/get?id=' + id })
}

// 获取通道的所有巡航路线
export const getCameraCruiseListByChannel = (channelId: number) => {
  return request.get<CameraCruiseRespVO[]>({ url: '/iot/camera/cruise/list-by-channel?channelId=' + channelId })
}

// 启动巡航
export const startCameraCruise = (id: number) => {
  return request.post({ url: '/iot/camera/cruise/start?id=' + id })
}

// 停止巡航
export const stopCameraCruise = (id: number) => {
  return request.post({ url: '/iot/camera/cruise/stop?id=' + id })
}

/**
 * ====================================
 * 摄像头录像记录 API
 * ====================================
 */

// 录像记录分页查询请求参数
export interface CameraRecordingPageReqVO {
  pageNo?: number
  pageSize?: number
  deviceId?: number
  cameraId?: number
  cameraIds?: number[] // 批量查询通道ID数组（优先级高于 cameraId）
  recordingType?: number // 1:手动 2:定时 3:报警触发 4:移动侦测
  status?: number // 0:录像中 1:已完成 2:已停止 3:异常
  startTime?: string
  endTime?: string
}

// 录像记录响应VO
export interface CameraRecordingRespVO {
  id: number
  cameraId: number
  deviceId?: number
  deviceName?: string
  startTime: string
  endTime?: string
  duration?: number // 时长(秒)
  filePath?: string
  fileSize?: number // 文件大小(字节)
  fileUrl?: string
  recordingType: number
  status: number
  errorMsg?: string
  createTime: string
}

// 获取录像记录分页
export const getCameraRecordingPage = (params: CameraRecordingPageReqVO) => {
  return request.get<PageResult<CameraRecordingRespVO>>({ url: '/iot/camera/recording/page', params })
}

// 删除录像记录
export const deleteCameraRecording = (id: number) => {
  return request.delete({ url: '/iot/camera/recording/delete?id=' + id })
}

// 上传录像文件并创建记录
export const uploadCameraRecording = (deviceId: number, file: File, recordingType?: number) => {
  const data = new FormData()
  data.append('deviceId', String(deviceId))
  if (recordingType !== undefined) data.append('recordingType', String(recordingType))
  data.append('file', file)
  return request.upload({ url: '/iot/camera/recording/upload', data })
}

// 录像时间段分布查询请求参数
export interface RecordingTimeSegmentReqVO {
  channelIds: number[]
  startTime: string
  endTime: string
  intervalMinutes?: number // 时间间隔（分钟），默认5分钟
}

// 录像片段
export interface RecordingSegment {
  startTime: string // 开始时间
  endTime: string   // 结束时间
  hasRecording: boolean // 是否有录像
}

// 录像时间段分布VO
export interface RecordingTimeSegmentVO {
  channelId: number
  channelName: string
  deviceName: string
  segments: RecordingSegment[]
}

// 查询录像时间段分布
export const getRecordingTimeSegments = (params: RecordingTimeSegmentReqVO) => {
  return request.get<RecordingTimeSegmentVO[]>({ url: '/iot/camera/recording/time-segments', params })
}

// 开始服务端录像
export const startCameraRecording = (
  deviceId: number,
  duration: number = 0,
  policy?: 'nvr' | 'server' | 'both'
) => {
  const params: any = { deviceId, duration }
  if (policy) params.policy = policy
  return request.post({
    url: '/iot/camera/recording/start',
    params
  })
}

// 停止服务端录像
export const stopCameraRecording = (recordingId: number) => {
  return request.post({
    url: '/iot/camera/recording/stop',
    params: { recordingId }
  })
}

/**
 * ====================================
 * 摄像头抓图记录 API
 * ====================================
 */

// 抓图记录分页查询请求参数
export interface CameraSnapshotPageReqVO {
  pageNo?: number
  pageSize?: number
  deviceId?: number
  channelId?: number
  channelIds?: number[] // 通道ID数组（批量查询）
  captureType?: number // 1:手动抓图 2:定时抓图 3:报警抓图 4:移动侦测抓图
  eventType?: string // motion_detected:移动侦测, alarm:报警等
  startTime?: string
  endTime?: string
  isProcessed?: boolean
}

// 抓图记录响应VO
export interface CameraSnapshotRespVO {
  id: number
  channelId: number
  deviceId: number
  channelName?: string
  snapshotUrl: string
  snapshotPath?: string
  fileSize?: number // 文件大小(字节)
  width?: number
  height?: number
  captureTime: string
  snapshotType: number
  triggerEvent?: string
  eventType?: string
  description?: string
  isProcessed?: boolean
  processor?: string
  processTime?: string
  processRemark?: string
  createTime: string
}

// 获取抓图记录分页
export const getCameraSnapshotPage = (params: CameraSnapshotPageReqVO) => {
  return request.get<PageResult<CameraSnapshotRespVO>>({ url: '/iot/camera/snapshot/page', params })
}

// 删除抓图记录
export const deleteCameraSnapshot = (id: number) => {
  return request.delete({ url: '/iot/camera/snapshot/delete?id=' + id })
}

// 获取通道最近一张抓图
export const getLatestCameraSnapshot = (channelId: number) => {
  return request.get<CameraSnapshotRespVO>({
    url: '/iot/camera/snapshot/latest',
    params: { channelId }
  })
}

// 标记抓图记录为已处理
export const markSnapshotAsProcessed = (id: number, processor: string, remark?: string) => {
  return request.put({
    url: '/iot/camera/snapshot/mark-processed',
    params: { id, processor, remark }
  })
}

// 上传抓图文件并创建记录
export const uploadCameraSnapshot = (
  channelId: number, 
  file: File, 
  snapshotType?: number
) => {
  const data = new FormData()
  data.append('channelId', String(channelId))
  if (snapshotType !== undefined) data.append('snapshotType', String(snapshotType))
  data.append('file', file)
  return request.upload({ url: '/iot/camera/snapshot/upload', data })
}

/**
 * ====================================
 * 录像剪切和导出 API
 * ====================================
 */

// 剪切录像请求参数
export interface CutRecordingReqVO {
  channelId: number
  startTime: string
  endTime: string
  description?: string
}

// 剪切录像 - 保存剪切后的录像片段到数据库
export const cutCameraRecording = (params: CutRecordingReqVO) => {
  return request.post({ url: '/iot/camera/recording/cut', data: params })
}

// 导出录像请求参数
export interface ExportRecordingReqVO {
  recordingId?: number // 录像记录ID（如果是已存在的录像）
  channelId?: number   // 通道ID（如果是实时剪切）
  startTime?: string   // 开始时间
  endTime?: string     // 结束时间
  format?: string      // 导出格式：mp4, avi, flv等
}

// 导出录像 - 下载录像文件
export const exportCameraRecording = (params: ExportRecordingReqVO) => {
  return request.download({ url: '/iot/camera/recording/export', params })
}

/**
 * ====================================
 * 辅助类型定义
 * ====================================
 */

// 分页结果类型
export interface PageResult<T> {
  list: T[]
  total: number
}







