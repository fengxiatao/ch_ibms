/**
 * 实时预览页面类型定义
 * 使用大华无插件 SDK
 */

// IBMS 本地通道信息
export interface IbmsChannel {
  id: number
  deviceId: number
  channelNo: number           // NVR 通道号（1-16）
  channelName: string
  channelCode?: string
  // 大华直连所需字段
  targetIp?: string           // NVR/IPC 设备 IP
  targetPort?: number         // HTTP 端口（默认 80，用于 WebSocket）
  rtspPort?: number           // RTSP 端口（默认 554）
  targetChannelNo?: number    // 摄像机本身的通道号（如双目摄像机）
  username?: string
  password?: string
  streamUrlMain?: string
  streamUrlSub?: string
  // 空间位置
  buildingId?: number
  floorId?: number
  areaId?: number
  spaceId?: number
  // 能力
  ptzSupport?: boolean
  hasAudio?: boolean
  // 状态
  onlineStatus?: number
  enableStatus?: number
  // WVP 关联字段（兼容旧数据）
  gbChannelId?: string
  gbDeviceId?: string
}

// 播放器窗格类型（大华 SDK）
export interface PlayerPane {
  // 基础信息
  ibmsChannel?: IbmsChannel | null
  channelName?: string
  // 播放器实例（大华 SDK PlayerControl）
  player: any | null
  // 容器元素
  container: HTMLElement | null
  // 状态
  isPlaying: boolean
  isLoading: boolean
  isPaused: boolean
  isRecording: boolean
  isPlayback: boolean
  // 错误信息
  error: string | null
  // 码流类型
  streamType: 'main' | 'sub'
  // 静音
  muted: boolean
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
  channelNo?: number
  // IBMS 通道数据
  ibmsChannel?: IbmsChannel
  // 子节点
  children?: DeviceTreeNode[]
  // 是否叶子节点
  isLeaf?: boolean
}

// 视图配置
export interface VideoView {
  id: number
  name: string
  groupIds: number[]
  gridLayout: number
  layout?: number
  panes: VideoViewPane[]
}

export interface VideoViewPane {
  paneIndex: number
  channelId?: number | null  // NVR 通道号
  channelName?: string
  channelNo?: number         // NVR 通道号（别名）
}

// 视图分组
export interface VideoViewGroup {
  id: number
  name: string
  icon?: string
  children?: VideoView[]
}

// 轮巡计划
export interface PatrolPlan {
  id: number
  name: string
  planName?: string
  loopMode?: number
  status?: number
}

// 轮巡任务
export interface PatrolTask {
  id: number
  taskName: string
  taskOrder: number
  duration: number
  status: number
}

// 轮巡场景
export interface PatrolScene {
  id: number
  sceneName: string
  gridLayout: string | number
  gridCount?: number
  duration?: number
  channels: PatrolSceneChannel[]
}

export interface PatrolSceneChannel {
  channelId: number
  channelName: string
  gridPosition: number
  duration?: number
  channelNo?: number  // NVR 通道号
}

// 云台预设点
export interface CameraPreset {
  presetId: number
  presetIndex?: number
  presetName?: string
  name?: string
  description?: string
}

// 巡航路线
export interface CameraCruise {
  id: number
  channelId: number
  cruiseName: string
  description?: string
  dwellTime: number
  loopEnabled: boolean
  status: number
  points: CameraCruisePoint[]
}

export interface CameraCruisePoint {
  presetId: number
  sortOrder: number
  dwellTime?: number
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
