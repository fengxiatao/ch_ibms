import request from '@/config/axios'

export interface SubsystemVO {
  id: number
  code: string
  name: string
  parentCode?: string
  level: number
  menuId?: number
  menuPath?: string
  icon?: string
  description?: string
  sort: number
  enabled: boolean
  createTime?: Date
  productCount?: number
  deviceCount?: number
  children?: SubsystemVO[]
}

/**
 * 获取所有子系统列表（扁平）
 */
export const getSubsystemList = () => {
  return request.get({ url: '/iot/subsystem/list' })
}

/**
 * 获取子系统树形结构
 */
export const getSubsystemTree = () => {
  return request.get({ url: '/iot/subsystem/tree' })
}

/**
 * 根据代码获取子系统
 */
export const getSubsystemByCode = (code: string) => {
  return request.get({ url: '/iot/subsystem/get-by-code', params: { code } })
}

/**
 * 获取指定父系统的子系统列表
 */
export const getSubsystemListByParent = (parentCode: string) => {
  return request.get({ url: '/iot/subsystem/list-by-parent', params: { parentCode } })
}

/**
 * 获取子系统统计信息（含产品数、设备数）
 */
export const getSubsystemStatistics = () => {
  return request.get({ url: '/iot/subsystem/statistics' })
}


















































