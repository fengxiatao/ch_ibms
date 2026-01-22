import request from '@/config/axios'

/** 巡航线路 VO */
export interface CameraCruiseVO {
  id?: number
  channelId: number
  cruiseName: string
  cruiseType?: number
  status?: number
  dwellTime?: number
  speed?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 巡航线路点 VO */
export interface CameraCruisePointVO {
  id?: number
  cruiseId: number
  presetId: number
  sortOrder: number
  dwellTime: number
  speed?: number
  presetNo?: number
  presetName?: string
}

/** 巡航线路分页请求 */
export interface CameraCruisePageReqVO {
  pageNo: number
  pageSize: number
  channelId?: number
  cruiseName?: string
  status?: number
}

// ========== 巡航线路管理 ==========

/** 获取巡航线路分页列表 */
export const getCruisePage = (params: CameraCruisePageReqVO) => {
  return request.get({ url: '/iot/camera/cruise/page', params })
}

/** 获取通道的所有巡航线路 */
export const getCruiseListByChannelId = (channelId: number) => {
  return request.get({ url: '/iot/camera/cruise/list-by-channel?channelId=' + channelId })
}

/** 获取巡航线路详情 */
export const getCruise = (id: number) => {
  return request.get({ url: '/iot/camera/cruise/get?id=' + id })
}

/** 创建巡航线路 */
export const createCruise = (data: CameraCruiseVO) => {
  return request.post({ url: '/iot/camera/cruise/create', data })
}

/** 更新巡航线路 */
export const updateCruise = (data: CameraCruiseVO) => {
  return request.put({ url: '/iot/camera/cruise/update', data })
}

/** 删除巡航线路 */
export const deleteCruise = (id: number) => {
  return request.delete({ url: '/iot/camera/cruise/delete?id=' + id })
}

// ========== 巡航线路点管理 ==========

/** 获取巡航线路的所有点 */
export const getCruisePointList = (cruiseId: number) => {
  return request.get({ url: '/iot/camera/cruise/point/list?cruiseId=' + cruiseId })
}

/** 添加巡航线路点 */
export const addCruisePoint = (data: CameraCruisePointVO) => {
  return request.post({ url: '/iot/camera/cruise/point/add', data })
}

/** 更新巡航线路点 */
export const updateCruisePoint = (data: CameraCruisePointVO) => {
  return request.put({ url: '/iot/camera/cruise/point/update', data })
}

/** 删除巡航线路点 */
export const deleteCruisePoint = (id: number) => {
  return request.delete({ url: '/iot/camera/cruise/point/delete?id=' + id })
}

/** 批量更新巡航线路点顺序 */
export const updateCruisePointOrders = (data: { cruiseId: number; pointIds: number[] }) => {
  return request.put({ url: '/iot/camera/cruise/point/update-orders', data })
}

// ========== 巡航控制 ==========

/** 开始巡航 */
export const startCruise = (id: number) => {
  return request.post({ url: '/iot/camera/cruise/start?id=' + id })
}

/** 停止巡航 */
export const stopCruise = (id: number) => {
  return request.post({ url: '/iot/camera/cruise/stop?id=' + id })
}

/** 同步巡航线路到设备 */
export const syncCruiseToDevice = (id: number, tourNo?: number) => {
  return request.post({ url: '/iot/camera/cruise/sync-to-device', data: { id, tourNo: tourNo || 1 } })
}

/** 启动设备巡航 */
export const startDeviceCruise = (id: number, tourNo?: number) => {
  return request.post({ url: '/iot/camera/cruise/start-device-cruise', data: { id, tourNo: tourNo || 1 } })
}

/** 停止设备巡航 */
export const stopDeviceCruise = (id: number, tourNo?: number) => {
  return request.post({ url: '/iot/camera/cruise/stop-device-cruise', data: { id, tourNo: tourNo || 1 } })
}
