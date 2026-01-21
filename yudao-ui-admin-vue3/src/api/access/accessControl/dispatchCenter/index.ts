/**
 * 请求封装
 * 说明：统一使用 '@/config/axios'
 */
import request from '@/config/axios'

export interface DispatchTaskVO {
  id?: number
  deviceId: number
  deviceName?: string
  taskType: string
  taskContent: string
  status: number
  createTime?: Date
  executeTime?: Date
  completeTime?: Date
  errorMessage?: string
}

export interface DispatchTaskPageReqVO extends PageParam {
  deviceId?: number
  taskType?: string
  status?: number
  createTime?: [Date, Date]
}

// 下发中心 API
export const DispatchCenterApi = {
  // 查询下发任务分页
  getDispatchTaskPage: async (params: DispatchTaskPageReqVO) => {
    return await request.get({ url: '/access/dispatch-task/page', params })
  },

  // 查询下发任务详情
  getDispatchTask: async (id: number) => {
    return await request.get({ url: '/access/dispatch-task/get?id=' + id })
  },

  // 新增下发任务
  createDispatchTask: async (data: DispatchTaskVO) => {
    return await request.post({ url: '/access/dispatch-task/create', data })
  },

  // 修改下发任务
  updateDispatchTask: async (data: DispatchTaskVO) => {
    return await request.put({ url: '/access/dispatch-task/update', data })
  },

  // 删除下发任务
  deleteDispatchTask: async (id: number) => {
    return await request.delete({ url: '/access/dispatch-task/delete?id=' + id })
  },

  // 批量删除下发任务
  deleteDispatchTaskBatch: async (ids: number[]) => {
    return await request.delete({ url: '/access/dispatch-task/delete-batch', data: { ids } })
  },

  // 重新执行下发任务
  retryDispatchTask: async (id: number) => {
    return await request.post({ url: '/access/dispatch-task/retry?id=' + id })
  },

  // 取消下发任务
  cancelDispatchTask: async (id: number) => {
    return await request.post({ url: '/access/dispatch-task/cancel?id=' + id })
  },

  // 导出下发任务 Excel
  exportDispatchTask: async (params: DispatchTaskPageReqVO) => {
    return await request.download({ url: '/access/dispatch-task/export-excel', params })
  }
}










