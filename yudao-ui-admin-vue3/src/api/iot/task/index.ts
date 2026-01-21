import request from '@/config/axios'

// ==================== 任务管理 API ====================

/**
 * 获取实体的任务列表
 * @param entityType 实体类型 (PRODUCT, DEVICE, CAMPUS, BUILDING, FLOOR, AREA)
 * @param entityId 实体ID
 */
export const getEntityTasks = (entityType: string, entityId: number) => {
  return request.get({
    url: `/iot/task/entity/${entityType}/${entityId}`
  })
}

/**
 * 创建任务
 * @param data 任务数据
 */
export const createTask = (data: any) => {
  return request.post({
    url: '/iot/task',
    data
  })
}

/**
 * 更新任务
 * @param data 任务数据
 */
export const updateTask = (data: any) => {
  return request.put({
    url: '/iot/task',
    data
  })
}

/**
 * 删除任务
 * @param id 任务ID
 */
export const deleteTask = (id: number) => {
  return request.delete({
    url: `/iot/task/${id}`
  })
}

/**
 * 启用/禁用任务
 * @param id 任务ID
 * @param enabled 是否启用
 */
export const toggleTask = (id: number, enabled: boolean) => {
  return request.put({
    url: `/iot/task/${id}/toggle`,
    data: { enabled }
  })
}

/**
 * 立即执行任务
 * @param id 任务ID
 */
export const executeTask = (id: number) => {
  return request.post({
    url: `/iot/task/${id}/execute`
  })
}

/**
 * 获取任务日志
 * @param params 查询参数
 */
export const getTaskLogs = (params: any) => {
  return request.get({
    url: '/iot/task/logs',
    params
  })
}

/**
 * 获取任务统计信息
 * @param taskId 任务ID
 */
export const getTaskStats = (taskId: number) => {
  return request.get({
    url: `/iot/task/${taskId}/stats`
  })
}

/**
 * 批量启用/禁用任务
 * @param ids 任务ID数组
 * @param enabled 是否启用
 */
export const batchToggleTasks = (ids: number[], enabled: boolean) => {
  return request.put({
    url: '/iot/task/batch/toggle',
    data: { ids, enabled }
  })
}

/**
 * 批量删除任务
 * @param ids 任务ID数组
 */
export const batchDeleteTasks = (ids: number[]) => {
  return request.delete({
    url: '/iot/task/batch',
    data: { ids }
  })
}

