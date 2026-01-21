import request from '@/config/axios'

export interface JobTypeDefinitionVO {
  id: number
  name: string
  code: string
  description: string
  businessType: string
  applicableEntities: string
  defaultConfigTemplate: any
  status: number
  createTime: Date
}

// 获取适用于指定实体类型的任务类型列表
export const getApplicableJobTypes = (entityType: string) => {
  return request.get({
    url: '/iot/job-type-definition/applicable',
    params: { entityType }
  })
}

// 获取任务类型详情
export const getJobTypeDefinition = (id: number) => {
  return request.get({
    url: `/iot/job-type-definition/get/${id}`
  })
}

// 根据code获取任务类型
export const getJobTypeByCode = (code: string) => {
  return request.get({
    url: '/iot/job-type-definition/get-by-code',
    params: { code }
  })
}




