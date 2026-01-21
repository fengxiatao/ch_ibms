import request from '@/config/axios'

/**
 * 产品定时任务配置 API
 */

/**
 * 获取产品定时任务配置
 * 
 * @param productId 产品ID
 * @returns 任务配置JSON字符串
 */
export const getProductJobConfig = (productId: number) => {
  return request.get({
    url: `/iot/product/job-config/${productId}`
  })
}

/**
 * 保存产品定时任务配置
 * 
 * @param productId 产品ID
 * @param data 任务配置数据
 * @returns 操作结果
 */
export const saveProductJobConfig = (productId: number, data: any) => {
  return request.put({
    url: `/iot/product/job-config/${productId}`,
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 删除产品定时任务配置
 * 
 * @param productId 产品ID
 * @returns 操作结果
 */
export const deleteProductJobConfig = (productId: number) => {
  return request.delete({
    url: `/iot/product/job-config/${productId}`
  })
}






