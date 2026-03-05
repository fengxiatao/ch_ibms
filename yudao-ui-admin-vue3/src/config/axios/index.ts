import { service } from './service'

import { config } from './config'

const { default_headers } = config

const request = (option: any) => {
  const { headersType, headers, ...otherOption } = option
  
  // 如果 data 是 FormData，不设置 Content-Type，让浏览器自动处理
  const isFormData = otherOption.data instanceof FormData
  const finalHeaders: Record<string, any> = { ...headers }
  
  if (!isFormData) {
    finalHeaders['Content-Type'] = headersType || default_headers
  }
  // 如果是 FormData 且明确指定了 headersType，则使用指定的值
  else if (headersType) {
    finalHeaders['Content-Type'] = headersType
  }
  
  return service({
    ...otherOption,
    headers: finalHeaders
  })
}
export default {
  get: async <T = any>(option: any) => {
    const res = await request({ method: 'GET', ...option })
    return res.data as unknown as T
  },
  post: async <T = any>(option: any) => {
    const res = await request({ method: 'POST', ...option })
    return res.data as unknown as T
  },
  postOriginal: async (option: any) => {
    const res = await request({ method: 'POST', ...option })
    return res
  },
  delete: async <T = any>(option: any) => {
    const res = await request({ method: 'DELETE', ...option })
    return res.data as unknown as T
  },
  put: async <T = any>(option: any) => {
    const res = await request({ method: 'PUT', ...option })
    return res.data as unknown as T
  },
  download: async <T = any>(option: any) => {
    const res = await request({ method: 'GET', responseType: 'blob', ...option })
    return res as unknown as Promise<T>
  },
  upload: async <T = any>(option: any) => {
    option.headersType = 'multipart/form-data'
    const res = await request({ method: 'POST', ...option })
    return res as unknown as Promise<T>
  }
}
