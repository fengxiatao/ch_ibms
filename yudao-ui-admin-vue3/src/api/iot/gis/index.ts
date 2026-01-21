import request from '@/config/axios'

// 获取 GIS 统计数据
export const getGisStatistics = () => {
  return request.get({ url: '/iot/gis/statistics' })
}

// 搜索设备
export const searchDevices = (params: any) => {
  return request.get({ url: '/iot/gis/search', params })
}

// 获取附近设备
export const getNearbyDevices = (data: any) => {
  return request.post({ url: '/iot/gis/nearby-devices', data })
}

// 获取边界内设备
export const getDevicesInBounds = (data: any) => {
  return request.post({ url: '/iot/gis/devices-in-bounds', data })
}

// ==================== 虚拟坐标系统 API ====================

/**
 * 根据建筑编码查询建筑信息
 * @param code 建筑编码（如 BUILD_A01）
 */
export const getBuildingByCode = (code: string) => {
  return request.get({ url: `/iot/gis/building/code/${code}` })
}

/**
 * 获取建筑的所有楼层
 * @param buildingId 建筑ID
 */
export const getBuildingFloors = (buildingId: number) => {
  return request.get({ url: `/iot/gis/floor/building/${buildingId}/floors` })
}

/**
 * 获取楼层详情（包含房间和设备）
 * @param floorId 楼层ID
 */
export const getFloorDetail = (floorId: number) => {
  return request.get({ url: `/iot/gis/floor/${floorId}/detail` })
}

/**
 * 获取楼层基本信息
 * @param floorId 楼层ID
 */
export const getFloorInfo = (floorId: number) => {
  return request.get({ url: `/iot/gis/floor/${floorId}/info` })
}

/**
 * 获取房间详情（包含设备列表）
 * @param roomId 房间ID
 */
export const getRoomDetail = (roomId: number) => {
  return request.get({ url: `/iot/gis/room/${roomId}/detail` })
}

/**
 * 获取房间的设备列表
 * @param roomId 房间ID
 */
export const getRoomDevices = (roomId: number) => {
  return request.get({ url: `/iot/gis/room/${roomId}/devices` })
}

// ==================== Area区域管理 API ====================

/**
 * 获取楼层所有区域
 * @param floorId 楼层ID
 */
export const getAreasByFloorId = (floorId: number) => {
  return request.get({ url: `/iot/area/floor/${floorId}/list` })
}

/**
 * 获取建筑所有区域
 * @param buildingId 建筑ID
 */
export const getAreasByBuildingId = (buildingId: number) => {
  return request.get({ url: `/iot/area/building/${buildingId}/list` })
}

/**
 * 根据楼层和类型查询区域
 * @param floorId 楼层ID
 * @param areaType 区域类型（ROOM/CORRIDOR/ELEVATOR/STAIRCASE等）
 */
export const getAreasByFloorIdAndType = (floorId: number, areaType: string) => {
  return request.get({ url: `/iot/area/floor/${floorId}/type/${areaType}` })
}

/**
 * 获取区域详情
 * @param id 区域ID
 */
export const getAreaById = (id: number) => {
  return request.get({ url: `/iot/area/get/${id}` })
}

/**
 * 获取楼层可视化数据（JSON格式，包含区域、设备、聚类等）
 * @param floorId 楼层ID
 */
export const getFloorVisualizationData = (floorId: number) => {
  return request.get({ url: `/iot/area/floor/${floorId}/visualization` })
}

/**
 * 查找两个区域之间的最短路径
 * @param data 路径请求参数
 */
export const findShortestPath = (data: {
  fromAreaId: number
  toAreaId: number
  accessibleOnly?: boolean
}) => {
  return request.post({ url: '/iot/area/path/find', data })
}

/**
 * 查找附近可导航区域
 * @param areaId 区域ID
 * @param maxDistance 最大距离（米）
 * @param includeVertical 是否包含垂直连接（电梯、楼梯）
 */
export const findNearbyAreas = (
  areaId: number,
  maxDistance?: number,
  includeVertical?: boolean
) => {
  return request.get({
    url: `/iot/area/nearby/${areaId}`,
    params: { maxDistance, includeVertical }
  })
}

/**
 * 分析区域可达性（N跳内可达的所有区域）
 * @param areaId 区域ID
 * @param maxHops 最大跳数
 */
export const analyzeReachability = (areaId: number, maxHops?: number) => {
  return request.get({
    url: `/iot/area/reachability/${areaId}`,
    params: { maxHops }
  })
}

/**
 * 设备定位到区域（根据设备坐标自动判断所属区域）
 * @param deviceId 设备ID
 */
export const locateDeviceToArea = (deviceId: number) => {
  return request.get({ url: `/iot/area/locate/device/${deviceId}` })
}

// ==================== 建筑绘制管理 API ====================

/**
 * 保存建筑（新建或更新）
 * @param data 建筑数据
 */
export const saveBuilding = (data: any) => {
  return request.post({ url: '/iot/gis/building/save', data })
}

/**
 * 删除建筑
 * @param id 建筑ID
 */
export const deleteBuilding = (id: number) => {
  return request.delete({ url: `/iot/gis/building/delete/${id}` })
}

/**
 * 更新建筑几何形状
 * @param id 建筑ID
 * @param geom WKT格式的几何数据
 */
export const updateBuildingGeometry = (id: number, geom: string) => {
  return request.put({ url: `/iot/gis/building/${id}/geometry`, data: { geom } })
}

/**
 * 获取建筑列表（带几何数据）
 * @param campusId 园区ID（可选）
 */
export const getBuildingList = (campusId?: number) => {
  return request.get({ 
    url: '/iot/gis/building/list',
    params: { campusId }
  })
}

