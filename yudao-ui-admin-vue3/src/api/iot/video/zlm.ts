/**
 * ZLMediaKit 流媒体 API
 * 
 * 提供低延迟视频播放接口，替代大华 RPC2 直连方式
 */
import request from '@/config/axios'

/**
 * 播放地址响应VO
 */
export interface PlayUrlRespVO {
  wsFlvUrl?: string      // WebSocket-FLV 播放地址（超低延时 < 500ms）
  webrtcUrl?: string     // WebRTC 播放地址（超低延时 < 500ms）
  wsFmp4Url?: string     // WebSocket-FMP4 播放地址（低延时 ~1-2秒）
  fmp4Url?: string       // HTTP-FMP4 播放地址（低延时 ~1-2秒）
  flvUrl?: string        // HTTP-FLV 播放地址（低延时 ~1秒）
  hlsUrl?: string        // HLS 播放地址（延时 5-10秒）
  rtmpUrl?: string       // RTMP 播放地址（需专用播放器）
  streamKey?: string     // 流标识
}

/**
 * 获取通道实时播放地址
 * 
 * @param channelId 通道ID
 * @param subtype 码流类型: 0=主码流/高清(默认), 1=子码流/标清
 * @returns 多协议播放地址，推荐使用 wsFlvUrl（低延迟）或 webrtcUrl（极低延迟）
 */
export const getLivePlayUrl = (channelId: number, subtype: number = 0) => {
  return request.get<PlayUrlRespVO>({
    url: `/iot/video/zlm/live/${channelId}`,
    params: { subtype },
    timeout: 60000  // 60秒超时（首次拉流可能需要较长时间）
  })
}

/**
 * 停止通道流
 * 
 * @param channelId 通道ID
 * @returns 是否成功停止
 */
export const stopStream = (channelId: number) => {
  return request.post<boolean>({
    url: `/iot/video/zlm/stop/${channelId}`
  })
}

/**
 * 检查通道流状态
 * 
 * @param channelId 通道ID
 * @returns 流是否在线
 */
export const isStreamOnline = (channelId: number) => {
  return request.get<boolean>({
    url: `/iot/video/zlm/status/${channelId}`
  })
}

/**
 * 获取录像回放播放地址
 * 
 * @param channelId 通道ID
 * @param startTime 开始时间（ISO格式或时间戳）
 * @param endTime 结束时间（ISO格式或时间戳）
 * @returns 多协议播放地址
 */
export const getPlaybackUrl = (channelId: number, startTime: string, endTime: string) => {
  return request.get<PlayUrlRespVO>({
    url: `/iot/video/zlm/playback/${channelId}`,
    params: { startTime, endTime },
    timeout: 60000  // 60秒超时
  })
}

/**
 * 清除所有流代理
 * 
 * 清空 ZLMediaKit 上缓存的所有流代理，用于修复因 RTSP URL 错误导致的缓存问题
 * 
 * @returns 清除的流数量
 */
export const clearAllStreams = () => {
  return request.post<number>({
    url: '/iot/video/zlm/clear-all'
  })
}
