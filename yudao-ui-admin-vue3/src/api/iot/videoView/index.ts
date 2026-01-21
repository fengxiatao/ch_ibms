import request from '@/config/axios'

// ========== 视图分组管理 ==========

export interface VideoViewGroupVO {
  id: number
  name: string
  icon: string
  sort: number
  createTime: string
  updateTime: string
}

// 获取分组列表
export const getVideoViewGroupList = () => {
  return request.get({ url: '/iot/video-view-group/list' })
}

// 创建分组
export const createVideoViewGroup = (data: VideoViewGroupVO) => {
  return request.post({ url: '/iot/video-view-group/create', data })
}

// 更新分组
export const updateVideoViewGroup = (data: VideoViewGroupVO) => {
  return request.put({ url: '/iot/video-view-group/update', data })
}

// 删除分组
export const deleteVideoViewGroup = (id: number) => {
  return request.delete({ url: '/iot/video-view-group/delete', params: { id } })
}

// ========== 视图管理 ==========

export interface VideoViewPaneVO {
  id?: number
  viewId?: number
  paneIndex: number
  channelId?: number
  deviceId?: number
  channelNo?: number
  channelName?: string
  targetIp?: string
  targetChannelNo?: number
  streamUrlMain?: string
  streamUrlSub?: string
  config?: any
}

export interface VideoViewVO {
  id?: number
  name: string
  groupIds?: number[]  // 改为数组，支持多对多
  gridLayout: number
  description?: string
  isDefault?: boolean
  sort?: number
  panes?: VideoViewPaneVO[]
  createTime?: string
  updateTime?: string
}

export interface VideoViewPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  groupId?: number
}

export interface VideoViewTreeVO {
  id: number
  name: string
  icon: string
  type: 'group' | 'view'
  gridLayout?: number
  paneCount?: number
  children?: VideoViewTreeVO[]
}

// 获取视图分页列表
export const getVideoViewPage = (params: VideoViewPageReqVO) => {
  return request.get({ url: '/iot/video-view/page', params })
}

// 获取视图详情
export const getVideoView = (id: number) => {
  return request.get({ url: '/iot/video-view/get', params: { id } })
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
  return request.delete({ url: '/iot/video-view/delete', params: { id } })
}

// 获取视图树（分组+视图）
export const getVideoViewTree = () => {
  return request.get({ url: '/iot/video-view/tree' })
}

// 设置默认视图
export const setDefaultVideoView = (id: number) => {
  return request.put({ url: '/iot/video-view/set-default', params: { id } })
}
