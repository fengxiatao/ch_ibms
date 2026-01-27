/**
 * 录像回放页面类型定义
 */

// 播放器窗格类型
export interface PlaybackPane {
  // 通道信息
  channel: { name: string, channelNo?: number } | null
  // 流信息
  streamInfo: any | null
  // 播放器实例（大华 SDK PlayerControl）
  jessibucaPlayer: any | null
  // video 元素（用于截图等）
  videoEl: HTMLVideoElement | null
  // 状态
  isPlaying: boolean
  isLoading: boolean
  isPaused: boolean
  // 错误信息
  error: string | null
  // 静音
  muted: boolean
  // 通道号
  channelNo: number | null
  // 流 ID（用于控制回放）
  streamId?: string | null
  // 回放时间范围
  playbackStartTime: string | null
  playbackEndTime: string | null
  // 当前播放时间（秒）
  currentPlaySeconds: number
  // 录像文件列表
  recordFiles: any[]
  // 是否有录像
  hasRecording: boolean
}

// 设备树节点类型
export interface DeviceTreeNode {
  id: string
  name: string
  type: 'building' | 'floor' | 'area' | 'channels' | 'channel'
  // 关联 ID
  buildingId?: number
  floorId?: number
  areaId?: number
  channelId?: number
  // IBMS 通道数据
  ibmsChannel?: IbmsChannel
  // 子节点
  children?: DeviceTreeNode[]
  // 是否叶子节点
  isLeaf?: boolean
}

// IBMS 通道类型
export interface IbmsChannel {
  id: number
  channelName: string
  channelNo: number
  targetIp?: string
  targetPort?: number
  username?: string
  password?: string
  onlineStatus?: number
}

// 录像时间段
export interface RecordingSegment {
  startTime: string
  endTime: string
  hasRecording: boolean
  // 扩展字段
  fileName?: string
  fileSize?: number
  filePath?: string
}

// 通道录像信息
export interface ChannelRecordingInfo {
  channelId: number | string
  channelName: string
  segments: RecordingSegment[]
}

// 时间轴片段（用于显示）
export interface TimelineSegment {
  left: number  // 百分比位置
  width: number  // 百分比宽度
  label: string  // 提示文本
  startTime: number  // 开始时间戳
  endTime: number  // 结束时间戳
}

// 剪切任务状态
export type CutTaskStatus = 'pending' | 'cutting' | 'success' | 'error'

// 剪切任务
export interface CutTask {
  paneIndex: number
  channelId: number | string | null
  channelName: string | null
  startTime: string
  endTime: string
  durationSec: number
  progress: number
  status: CutTaskStatus
  statusText: string
  createdAt: string
}

// 分屏布局类型
export type GridLayoutType = 1 | 4 | 6 | 9 | 12 | 16

// 分屏布局 CSS 类映射
export const GRID_LAYOUT_CLASS: Record<GridLayoutType, string> = {
  1: 'grid-1x1',
  4: 'grid-2x2',
  6: 'grid-2x3',
  9: 'grid-3x3',
  12: 'grid-3x4',
  16: 'grid-4x4'
}

// 时间格式化工具
export function formatDateTime(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
}

// 解析时间字符串为时间戳
export function parseTimeString(timeStr: string): number {
  if (!timeStr) return 0
  const normalizedStr = timeStr.replace(' ', 'T')
  const timestamp = new Date(normalizedStr).getTime()
  return isNaN(timestamp) ? 0 : timestamp
}
