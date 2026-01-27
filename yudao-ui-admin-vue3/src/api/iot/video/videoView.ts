import request from '@/config/axios'

// 视图 VO
export interface VideoViewVO {
  id?: number
  name: string
  groupId?: number
  groupIds?: number[]
  groupName?: string
  layout?: number
  gridLayout?: number  // 兼容前端字段名
  panes?: VideoViewPaneVO[]
  isDefault?: boolean
  sortOrder?: number
  remark?: string
  createTime?: Date
}

// 视图窗格 VO
export interface VideoViewPaneVO {
  paneIndex: number
  channelId?: number | string | null  // WVP 通道 ID (commonGbChannelId)
  wvpPlayId?: number | string | null  // 兼容前端字段名
  channelName?: string
  deviceId?: number
  channelNo?: number
  config?: string
}

// 视图分组 VO
export interface VideoViewGroupVO {
  id?: number
  name: string
  icon?: string
  sort?: number
  sortOrder?: number
  children?: VideoViewVO[]
}

// 获取视图树（分组+视图）
export const getVideoViewTree = () => {
  return request.get({ url: '/iot/video-view/tree' })
}

// 获取视图分组列表
export const getVideoViewGroupList = () => {
  return request.get({ url: '/iot/video-view-group/list' })
}

// 获取视图详情
export const getVideoView = (id: number) => {
  return request.get({ url: `/iot/video-view/get?id=${id}` })
}

// 创建视图
export const createVideoView = (data: VideoViewVO) => {
  return request.post({ url: '/iot/video-view/create', data })
}

// 更新视图
export const updateVideoView = (data: VideoViewVO) => {
  return request.put({ url: '/iot/video-view/update', data })
}

// 删除视图
export const deleteVideoView = (id: number) => {
  return request.delete({ url: `/iot/video-view/delete?id=${id}` })
}

// 创建视图分组
export const createVideoViewGroup = (data: VideoViewGroupVO) => {
  return request.post({ url: '/iot/video-view-group/create', data })
}

// 更新视图分组
export const updateVideoViewGroup = (data: VideoViewGroupVO) => {
  return request.put({ url: '/iot/video-view-group/update', data })
}

// 删除视图分组
export const deleteVideoViewGroup = (id: number) => {
  return request.delete({ url: `/iot/video-view-group/delete?id=${id}` })
}
