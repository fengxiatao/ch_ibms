import request from '@/config/axios'

export interface IbmsSpaceRespVO {
  id: number
  parentId: number
  spaceCode: string
  code: string
  subCode?: string
  name: string
  type: string
  sort?: number
  extra?: string
  createTime?: string
}

export interface IbmsSpaceTreeNodeRespVO {
  id: number
  parentId: number
  name: string
  spaceCode: string
  type: string
  children?: IbmsSpaceTreeNodeRespVO[]
}

export interface IbmsSpaceSaveReqVO {
  id?: number
  parentId: number
  spaceCode: string
  code: string
  subCode?: string
  name: string
  type: string
  sort?: number
  extra?: string
}

export const getSpaceTree = () => {
  return request.get<IbmsSpaceTreeNodeRespVO[]>({ url: '/iot/ibms/space/tree' })
}

export const getSpace = (id: number) => {
  return request.get<IbmsSpaceRespVO>({ url: '/iot/ibms/space/get', params: { id } })
}

export const createSpace = (data: IbmsSpaceSaveReqVO) => {
  return request.post<number>({ url: '/iot/ibms/space/create', data })
}

export const updateSpace = (data: IbmsSpaceSaveReqVO) => {
  return request.put<boolean>({ url: '/iot/ibms/space/update', data })
}

export const deleteSpace = (id: number) => {
  return request.delete<boolean>({ url: '/iot/ibms/space/delete', params: { id } })
}

