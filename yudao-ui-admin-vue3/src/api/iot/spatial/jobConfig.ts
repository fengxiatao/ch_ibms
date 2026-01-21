import request from '@/config/axios'

// ==================== 园区定时任务配置 ====================

export const getCampusJobConfig = (campusId: number) => {
  return request.get({ url: `/iot/campus-job-config/get/${campusId}` })
}

export const saveCampusJobConfig = (campusId: number, jobConfig: string) => {
  return request.put({ url: `/iot/campus-job-config/save/${campusId}`, data: jobConfig })
}

export const deleteCampusJobConfig = (campusId: number) => {
  return request.delete({ url: `/iot/campus-job-config/delete/${campusId}` })
}

// ==================== 建筑定时任务配置 ====================

export const getBuildingJobConfig = (buildingId: number) => {
  return request.get({ url: `/iot/building-job-config/get/${buildingId}` })
}

export const saveBuildingJobConfig = (buildingId: number, jobConfig: string) => {
  return request.put({ url: `/iot/building-job-config/save/${buildingId}`, data: jobConfig })
}

export const deleteBuildingJobConfig = (buildingId: number) => {
  return request.delete({ url: `/iot/building-job-config/delete/${buildingId}` })
}

// ==================== 楼层定时任务配置 ====================

export const getFloorJobConfig = (floorId: number) => {
  return request.get({ url: `/iot/floor-job-config/get/${floorId}` })
}

export const saveFloorJobConfig = (floorId: number, jobConfig: string) => {
  return request.put({ url: `/iot/floor-job-config/save/${floorId}`, data: jobConfig })
}

export const deleteFloorJobConfig = (floorId: number) => {
  return request.delete({ url: `/iot/floor-job-config/delete/${floorId}` })
}

// ==================== 区域定时任务配置 ====================

export const getAreaJobConfig = (areaId: number) => {
  return request.get({ url: `/iot/area-job-config/get/${areaId}` })
}

export const saveAreaJobConfig = (areaId: number, jobConfig: string) => {
  return request.put({ url: `/iot/area-job-config/save/${areaId}`, data: jobConfig })
}

export const deleteAreaJobConfig = (areaId: number) => {
  return request.delete({ url: `/iot/area-job-config/delete/${areaId}` })
}






