import request from '@/config/axios'

export interface IbmsChannelRespVO {
  id: number
  spaceId?: number
  deviceId?: number
  code: string
  channelNo: number
  name: string
  business: string
  typeCode: string
  category?: string
  systemType?: string
  dataSource?: string
  ip?: string
  mac?: string
  deviceSn?: string
  deviceName?: string
  space?: string
  currentValue?: string
  status: string
  extra?: string
  createTime?: string
}

export interface IbmsChannelSaveReqVO {
  id?: number
  spaceId?: number
  deviceId?: number
  code: string
  channelNo: number
  name: string
  business: string
  typeCode: string
  category?: string
  systemType?: string
  dataSource?: string
  ip?: string
  mac?: string
  deviceSn?: string
  deviceName?: string
  space?: string
  currentValue?: string
  status: string
  extra?: string
}

export interface IbmsChannelPageReqVO {
  pageNo: number
  pageSize: number
  keyword?: string
  business?: string
  spaceId?: number
  deviceId?: number
  typeCode?: string
  systemType?: string
  status?: string
}

export const getChannelPage = (params: IbmsChannelPageReqVO) => {
  return request.get<any>({ url: '/iot/ibms/channel/page', params })
}

export const getChannel = (id: number) => {
  return request.get<IbmsChannelRespVO>({ url: '/iot/ibms/channel/get', params: { id } })
}

export const createChannel = (data: IbmsChannelSaveReqVO) => {
  return request.post<number>({ url: '/iot/ibms/channel/create', data })
}

export const updateChannel = (data: IbmsChannelSaveReqVO) => {
  return request.put<boolean>({ url: '/iot/ibms/channel/update', data })
}

export const deleteChannel = (id: number) => {
  return request.delete<boolean>({ url: '/iot/ibms/channel/delete', params: { id } })
}

export const listChannelsByDevice = (deviceId: number) => {
  return request.get<IbmsChannelRespVO[]>({ url: '/iot/ibms/channel/list-by-device', params: { deviceId } })
}

export const syncChannelsFromDevice = (deviceId: number) => {
  return request.post<IbmsChannelRespVO[]>({ url: '/iot/ibms/channel/sync-from-device', params: { deviceId } })
}

