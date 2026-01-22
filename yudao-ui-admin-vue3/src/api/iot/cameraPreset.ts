import request from '@/config/axios'

/** 预设点 VO */
export interface CameraPresetVO {
  id?: number
  channelId: number
  presetNo: number
  presetName: string
  description?: string
  createTime?: string
  updateTime?: string
}

/** 预设点保存请求 */
export interface CameraPresetSaveReqVO {
  id?: number
  channelId: number
  presetNo: number
  presetName: string
  description?: string
}

// ========== 预设点管理 ==========

/** 创建预设点 */
export const createPreset = (data: CameraPresetSaveReqVO) => {
  return request.post({ url: '/iot/camera/preset/create', data })
}

/** 更新预设点 */
export const updatePreset = (data: CameraPresetSaveReqVO) => {
  return request.put({ url: '/iot/camera/preset/update', data })
}

/** 删除预设点 */
export const deletePreset = (id: number) => {
  return request.delete({ url: '/iot/camera/preset/delete?id=' + id })
}

/** 获取预设点详情 */
export const getPreset = (id: number) => {
  return request.get({ url: '/iot/camera/preset/get?id=' + id })
}

/** 获取通道的所有预设点 */
export const getPresetListByChannelId = (channelId: number) => {
  return request.get({ url: '/iot/camera/preset/list-by-channel?channelId=' + channelId })
}
