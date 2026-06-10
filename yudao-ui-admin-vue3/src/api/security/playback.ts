import request from '@/config/axios'

/** 录像片段 */
export interface RecordingSegmentVO {
  startTime: string
  endTime: string
  hasRecording: boolean
}

/** 通道录像信息 */
export interface ChannelRecordingVO {
  channelId: number
  channelName: string
  segments: RecordingSegmentVO[]
}

/** 查询录像请求 */
export interface PlaybackQueryRecordingsReqVO {
  cameraId: number
  startTime: string
  endTime: string
  recordType?: number
}

/** 批量查询录像请求 */
export interface PlaybackQueryRecordingsBatchReqVO {
  cameraIds: number[]
  startTime: string
  endTime: string
  recordType?: number
}

/** 回放地址请求 */
export interface PlaybackUrlReqVO {
  cameraId: number
  startTime: string
  endTime: string
  streamType?: number
}

/** 回放地址响应 */
export interface PlaybackUrlRespVO {
  cameraId: number
  cameraName: string
  rtspUrl?: string
  wsFlvUrl?: string
  flvUrl: string
  webrtcUrl?: string
  streamId?: string
  app?: string
  stream?: string
}

/** 剪切录像请求 */
export interface PlaybackClipReqVO {
  cameraId: number
  startTime: string
  endTime: string
}

/** 剪切录像响应 */
export interface PlaybackClipRespVO {
  cameraId: number
  cameraName: string
  startTime: string
  endTime: string
  fileUrl: string
  filePath: string
}

export const PlaybackApi = {
  /** 查询录像片段 */
  queryRecordings: (data: PlaybackQueryRecordingsReqVO) =>
    request.post<ChannelRecordingVO[]>({
      url: '/security/playback/query-recordings',
      data
    }),

  /** 批量查询录像片段 */
  queryRecordingsBatch: (data: PlaybackQueryRecordingsBatchReqVO) =>
    request.post<ChannelRecordingVO[]>({
      url: '/security/playback/query-recordings-batch',
      data
    }),

  /** 获取回放地址（ZLM 流） */
  getPlaybackUrl: (data: PlaybackUrlReqVO) =>
    request.post<PlaybackUrlRespVO>({
      url: '/security/playback/get-url',
      data
    }),

  /** 关闭回放流 */
  closeStream: (streamId: string) =>
    request.post({
      url: '/security/playback/close-stream',
      params: { streamId }
    }),

  /** 按时间剪切录像并生成可下载文件 */
  clipRecording: (data: PlaybackClipReqVO) =>
    request.post<PlaybackClipRespVO>({
      url: '/security/playback/clip',
      data
    })
}
