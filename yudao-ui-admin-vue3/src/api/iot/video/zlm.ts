import request from '@/config/axios'

/**
 * ZLMediaKit 流媒体播放地址
 */
export interface PlayUrlVO {
  /** WebSocket-FLV 地址（推荐，低延迟 ~500ms） */
  wsFlvUrl: string
  /** HTTP-FLV 地址 */
  flvUrl: string
  /** HLS 地址（兼容性好，延迟 5-15秒） */
  hlsUrl: string
  /** WebSocket-FMP4 地址 */
  wsFmp4Url: string
  /** FMP4 地址 */
  fmp4Url: string
  /** RTMP 地址 */
  rtmpUrl: string
  /** WebRTC 地址（极低延迟 ~200ms） */
  webrtcUrl: string
  /** 流标识 */
  streamKey: string
}

/**
 * 获取通道实时播放地址
 * 
 * 后端会自动从摄像头拉流到 ZLMediaKit，返回多协议播放地址
 * 推荐使用 wsFlvUrl（低延迟）或 webrtcUrl（极低延迟）
 * 
 * @param channelId 通道ID
 * @param subtype 码流类型: 0=主码流/高清(默认), 1=子码流/标清
 */
export const getLivePlayUrl = (channelId: number, subtype: number = 0): Promise<PlayUrlVO> => {
  return request.get({ url: `/iot/video/zlm/live/${channelId}`, params: { subtype } })
}

/**
 * 停止通道流
 * 
 * @param channelId 通道ID
 */
export const stopStream = (channelId: number): Promise<boolean> => {
  return request.post({ url: `/iot/video/zlm/stop/${channelId}` })
}

/**
 * 检查通道流状态
 * 
 * @param channelId 通道ID
 */
export const isStreamOnline = (channelId: number): Promise<boolean> => {
  return request.get({ url: `/iot/video/zlm/status/${channelId}` })
}

/**
 * 获取录像回放播放地址
 * 
 * 从 NVR 拉取指定时间段的录像流
 * 
 * @param channelId 通道ID
 * @param startTime 开始时间（ISO格式或时间戳）
 * @param endTime 结束时间（ISO格式或时间戳）
 */
export const getPlaybackUrl = (channelId: number, startTime: string, endTime: string): Promise<PlayUrlVO> => {
  return request.get({ url: `/iot/video/zlm/playback/${channelId}`, params: { startTime, endTime } })
}
