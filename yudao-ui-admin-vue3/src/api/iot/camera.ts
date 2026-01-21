import request from '@/config/axios'

export interface CameraVO {
  id?: number
  name: string
  code: string
  location?: string
  status: number
  streamUrl?: string
  manufacturer?: string
  model?: string
  ip?: string
  port?: number
  username?: string
  password?: string
  protocol?: string
  channelNo?: number
  creator?: string
  createTime?: string
  updater?: string
  updateTime?: string
}

export interface CameraPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  code?: string
  location?: string
  status?: number
}

// 获取摄像头分页
export const getCameraPage = (params: CameraPageReqVO) => {
  return request.get({ url: '/iot/camera/page', params })
}

// 获取摄像头详情
export const getCamera = (id: number) => {
  return request.get({ url: '/iot/camera/get?id=' + id })
}

// 创建摄像头
export const createCamera = (data: CameraVO) => {
  return request.post({ url: '/iot/camera/create', data })
}

// 更新摄像头
export const updateCamera = (data: CameraVO) => {
  return request.put({ url: '/iot/camera/update', data })
}

// 删除摄像头
export const deleteCamera = (id: number) => {
  return request.delete({ url: '/iot/camera/delete?id=' + id })
}

// 获取所有摄像头列表
export const getCameraList = () => {
  return request.get({ url: '/iot/camera/list' })
}

// 测试摄像头连接
export const testCameraConnection = (id: number) => {
  return request.post({ url: '/iot/camera/test-connection?id=' + id })
}
