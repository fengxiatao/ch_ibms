/**
 * 视频巡检任务相关类型定义（新版）
 * 与原有的"视频巡更任务"（VideoPatrolTask）区分
 * 命名规范：InspectionTask（巡检任务）vs PatrolTask（巡更任务）
 */

// 巡检任务状态
export type InspectionTaskStatus = 'draft' | 'active' | 'paused'

// 巡检通道配置
export interface InspectionChannelConfig {
  // 设备基本信息
  deviceId: number               // 设备ID
  channelId: number              // 通道ID
  channelName: string            // 通道名称
  
  // 播放配置
  duration: number               // 播放时长（秒），0表示持续播放
  
  // 设备详细信息（用于播放）
  ipAddress?: string             // 设备IP（从 config 中提取）
  productId?: string             // 产品ID
  config?: any                   // 设备配置（包含channelNo等）
  streamUrl?: string             // 流地址
  
  // NVR相关
  nvrId?: number                 // NVR设备ID（如果是NVR通道）
  channelNo?: number             // NVR通道号（0, 1, 2...）
  
  // 其他
  location?: string              // 位置信息
  snapshot?: string              // 快照URL
}

// 巡检场景（每个格子的配置）
export interface InspectionScene {
  cellIndex: number                    // 格子索引（0, 1, 2...）
  channels: InspectionChannelConfig[]  // 通道配置列表
}

// 巡检任务（新版）
export interface InspectionTask {
  id?: number                          // 任务ID（后端生成）
  taskName: string                     // 任务名称
  layout: string                       // 分屏布局：'1x1', '2x2', '3x3', '4x4'
  scenes: InspectionScene[]            // 场景列表（每个格子的配置）
  status?: InspectionTaskStatus        // 任务状态
  createTime?: string                  // 创建时间
  updateTime?: string                  // 更新时间
  description?: string                 // 任务描述
}

// 创建巡检任务请求
export interface CreateInspectionTaskRequest {
  taskName: string
  layout: string
  scenes: InspectionScene[]
  description?: string
}

// 更新巡检任务请求
export interface UpdateInspectionTaskRequest {
  taskName?: string
  layout?: string
  scenes?: InspectionScene[]
  status?: InspectionTaskStatus
  description?: string
}

// 巡检任务列表查询参数
export interface InspectionTaskListQuery {
  pageNo?: number
  pageSize?: number
  taskName?: string
  status?: InspectionTaskStatus
}

// 巡检任务列表响应
export interface InspectionTaskListResponse {
  list: InspectionTask[]
  total: number
}
