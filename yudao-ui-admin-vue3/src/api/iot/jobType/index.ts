import request from '@/config/axios'

// ==================== 任务类型定义 API ====================

/**
 * 获取适用于指定实体类型的任务类型列表
 * @param entityType 实体类型 (PRODUCT, DEVICE, CAMPUS, BUILDING, FLOOR, AREA)
 */
export const getApplicableJobTypes = (entityType: string) => {
  return request.get({
    url: `/iot/job-type/applicable`,
    params: { entityType }
  })
}

/**
 * 获取所有任务类型列表
 */
export const getAllJobTypes = () => {
  return request.get({
    url: '/iot/job-type/list'
  })
}

/**
 * 根据业务类型获取任务类型
 * @param businessType 业务类型 (IOT_DEVICE, SPATIAL, ENERGY, SECURITY, HVAC, SYSTEM)
 */
export const getJobTypesByBusinessType = (businessType: string) => {
  return request.get({
    url: '/iot/job-type/business',
    params: { businessType }
  })
}

/**
 * 根据编码获取任务类型
 * @param code 任务编码
 */
export const getJobTypeByCode = (code: string) => {
  return request.get({
    url: `/iot/job-type/code/${code}`
  })
}

