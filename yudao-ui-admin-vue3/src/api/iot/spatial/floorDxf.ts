import request from '@/config/axios'

/**
 * 上传楼层DXF文件
 */
export const uploadDxf = (floorId: number, file: File) => {
  const formData = new FormData()
  formData.append('floorId', floorId.toString())
  formData.append('file', file)
  
  return request.post({
    url: '/iot/floor-dxf/upload',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除楼层DXF文件
 */
export const deleteDxf = (floorId: number) => {
  return request.delete({
    url: '/iot/floor-dxf/delete',
    params: { floorId }
  })
}

/**
 * 获取楼层DXF文件信息
 */
export const getDxfInfo = (floorId: number) => {
  return request.get({
    url: '/iot/floor-dxf/info',
    params: { floorId }
  })
}

/**
 * 获取楼层平面图SVG
 */
export const getFloorPlanSvg = (floorId: number, layout?: string, layers?: string) => {
  return request.get({
    url: '/iot/floor-dxf/svg',
    params: { floorId, layout, layers }
  })
}

/**
 * 获取楼层DXF文件的布局列表
 */
export const getLayouts = (floorId: number) => {
  return request.get({
    url: '/iot/floor-dxf/layouts',
    params: { floorId }
  })
}

/**
 * 获取楼层DXF文件的图层列表
 */
export const getLayers = (floorId: number) => {
  return request.get({
    url: '/iot/floor-dxf/layers',
    params: { floorId }
  })
}

/**
 * 识别DXF文件中的区域
 */
export const recognizeAreas = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post({
    url: '/iot/floor-dxf/recognize-areas',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 识别DXF文件中的设备
 */
export const recognizeDevices = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post({
    url: '/iot/floor-dxf/recognize-devices',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 识别DXF文件中的区域和设备（一次性识别所有）
 */
export const recognizeAll = (file: File, deviceConfig?: any[]) => {
  const formData = new FormData()
  formData.append('file', file)
  
  // 如果有配置，添加到请求中
  if (deviceConfig && deviceConfig.length > 0) {
    formData.append('deviceConfig', JSON.stringify(deviceConfig))
  }
  
  return request.post({
    url: '/iot/floor-dxf/recognize-all',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 识别已上传的DXF文件（通过楼层ID）
 */
export const recognizeByFloorId = (floorId: number, deviceConfig?: any[]) => {
  return request.post({
    url: '/iot/floor-dxf/recognize-by-floor',
    params: { floorId },
    data: deviceConfig
  })
}

/**
 * 获取楼层DXF文件内容（用于前端直接解析）
 * 
 * @param floorId 楼层ID
 * @returns DXF文件文本内容
 */
export const getDxfFileContent = (floorId: number) => {
  return request.get<string>({
    url: '/iot/floor-dxf/dxf-content',
    params: { floorId }
  })
}

