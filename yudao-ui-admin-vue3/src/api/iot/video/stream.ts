/**
 * 请求封装
 * 说明：统一使用 '@/config/axios'
 */
import request from '@/config/axios'

export interface VideoStreamCreateReqVO {
  deviceId: number
  streamType: string // 'live' | 'playback'
  startTime?: string
  endTime?: string
}

export interface VideoStreamRespVO {
  streamId: string
  deviceId: number
  streamType: string
  rtspUrl: string
  hlsUrl: string
  wsFlvUrl?: string
  webrtcUrl?: string
  status: string
  createTime: string
  startTime?: string
  endTime?: string
}

// 创建视频流
export const createVideoStream = (data: VideoStreamCreateReqVO) => {
  return request.post<VideoStreamRespVO>({ url: '/iot/video/stream/create', data })
}

// 停止视频流
export const stopVideoStream = (streamId: string) => {
  return request.delete({ url: '/iot/video/stream/stop', params: { streamId } })
}

// 获取视频流信息
export const getVideoStream = (streamId: string) => {
  return request.get<VideoStreamRespVO>({ url: '/iot/video/stream/get', params: { streamId } })
}

// 刷新视频流
export const refreshVideoStream = (streamId: string) => {
  return request.post<VideoStreamRespVO>({ url: '/iot/video/stream/refresh', params: { streamId } })
}

// 获取实时预览流
export const getLiveStream = (deviceId: number) => {
  return request.get<VideoStreamRespVO>({ url: '/iot/video/stream/live', params: { deviceId } })
}

// 获取录像回放流
export const getPlaybackStream = (deviceId: number, startTime: string, endTime: string) => {
  return request.get<VideoStreamRespVO>({
    url: '/iot/video/stream/playback',
    params: { deviceId, startTime, endTime }
  })
}












