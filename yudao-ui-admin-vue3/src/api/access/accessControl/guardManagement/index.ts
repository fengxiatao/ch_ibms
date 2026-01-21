/**
 * 请求封装
 * 说明：统一改为 '@/config/axios'
 */
import request from '@/config/axios'

export interface GuardVO {
  id?: number
  guardName: string
  guardCode: string
  location: string
  description?: string
  status: number
  deviceCount?: number
  onlineDeviceCount?: number
  createTime?: Date
}

export interface GuardPageReqVO extends PageParam {
  guardName?: string
  guardCode?: string
  location?: string
  status?: number
  createTime?: [Date, Date]
}

export interface GuardDeviceVO {
  id?: number
  guardId: number
  deviceId: number
  deviceName: string
  deviceType: string
  status: number
  createTime?: Date
}

// 门岗管理 API
export const GuardManagementApi = {
  // 查询门岗分页
  getGuardPage: async (params: GuardPageReqVO) => {
    return await request.get({ url: '/access/guard/page', params })
  },

  // 查询门岗详情
  getGuard: async (id: number) => {
    return await request.get({ url: '/access/guard/get?id=' + id })
  },

  // 新增门岗
  createGuard: async (data: GuardVO) => {
    return await request.post({ url: '/access/guard/create', data })
  },

  // 修改门岗
  updateGuard: async (data: GuardVO) => {
    return await request.put({ url: '/access/guard/update', data })
  },

  // 删除门岗
  deleteGuard: async (id: number) => {
    return await request.delete({ url: '/access/guard/delete?id=' + id })
  },

  // 批量删除门岗
  deleteGuardBatch: async (ids: number[]) => {
    return await request.delete({ url: '/access/guard/delete-batch', data: { ids } })
  },

  // 查询门岗设备列表
  getGuardDeviceList: async (guardId: number) => {
    return await request.get({ url: '/access/guard-device/list?guardId=' + guardId })
  },

  // 为门岗分配设备
  assignDeviceToGuard: async (data: { guardId: number; deviceIds: number[] }) => {
    return await request.post({ url: '/access/guard-device/assign', data })
  },

  // 移除门岗设备
  removeDeviceFromGuard: async (guardId: number, deviceId: number) => {
    return await request.delete({ url: `/access/guard-device/remove?guardId=${guardId}&deviceId=${deviceId}` })
  },

  // 查询所有门岗列表（用于下拉选择）
  getGuardSimpleList: async () => {
    return await request.get({ url: '/access/guard/simple-list' })
  },

  // 导出门岗 Excel
  exportGuard: async (params: GuardPageReqVO) => {
    return await request.download({ url: '/access/guard/export-excel', params })
  }
}









