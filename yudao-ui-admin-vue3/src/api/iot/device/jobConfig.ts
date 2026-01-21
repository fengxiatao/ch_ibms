import request from '@/config/axios'

export const getDeviceJobConfig = (deviceId: number) => {
  return request.get({ url: `/iot/device-job-config/get/${deviceId}` })
}

export const saveDeviceJobConfig = (deviceId: number, jobConfig: string) => {
  return request.put({ url: `/iot/device-job-config/save/${deviceId}`, data: jobConfig })
}

export const deleteDeviceJobConfig = (deviceId: number) => {
  return request.delete({ url: `/iot/device-job-config/delete/${deviceId}` })
}






