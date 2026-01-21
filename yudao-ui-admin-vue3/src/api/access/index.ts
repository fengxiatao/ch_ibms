import request from '@/config/axios'

// 智慧通行首页统计数据
export interface AccessDashboardStatisticsVO {
  // 今日数据
  todayAccessCount: number
  todayVisitorCount: number
  todayVehicleCount: number
  todayAlarmCount: number
  
  // 实时数据
  onlineDeviceCount: number
  totalDeviceCount: number
  currentVisitorCount: number
  occupiedParkingSpaces: number
  totalParkingSpaces: number
  
  // 同比数据
  accessCountGrowth: number // 百分比
  visitorCountGrowth: number
  vehicleCountGrowth: number
  
  // 设备状态分布
  deviceStatusDistribution: {
    online: number
    offline: number
    maintenance: number
    fault: number
  }
  
  // 访问类型分布
  accessTypeDistribution: {
    employee: number
    visitor: number
    vehicle: number
    elevator: number
  }
}

// 通行趋势数据
export interface AccessTrendVO {
  date: string
  accessCount: number
  visitorCount: number
  vehicleCount: number
  alarmCount: number
}

// 实时通行数据
export interface RealTimeAccessVO {
  id: number
  type: string // access, visitor, vehicle, elevator
  userName: string
  deviceName: string
  location: string
  time: Date
  result: string // success, failed
  photo?: string
}

// 获取智慧通行首页统计数据
export const getAccessDashboardStatistics = async (): Promise<AccessDashboardStatisticsVO> => {
  try {
    return await request.get<AccessDashboardStatisticsVO>({ url: '/iot/access/dashboard/statistics' })
  } catch (error) {
    // 如果后端API不存在，返回模拟数据
    console.warn('智慧通行后端API未实现，使用模拟数据')
    return {
      todayAccessCount: 1256,
      todayVisitorCount: 89,
      todayVehicleCount: 456,
      todayAlarmCount: 3,
      onlineDeviceCount: 15,
      totalDeviceCount: 18,
      currentVisitorCount: 23,
      occupiedParkingSpaces: 45,
      totalParkingSpaces: 100,
      accessCountGrowth: 12.5,
      visitorCountGrowth: 8.3,
      vehicleCountGrowth: -2.1,
      deviceStatusDistribution: { online: 15, offline: 2, maintenance: 1, fault: 0 },
      accessTypeDistribution: { employee: 65, visitor: 20, vehicle: 12, elevator: 3 }
    }
  }
}

// 获取通行趋势数据
export const getAccessTrend = (params: { startTime: Date, endTime: Date, type?: string }) => {
  return request.get({ url: '/access/dashboard/trend', params })
}

// 获取实时通行数据
export const getRealTimeAccessList = async (params: { pageSize?: number }): Promise<RealTimeAccessVO[]> => {
  try {
    const result = await request.get<{ records: RealTimeAccessVO[] }>({ url: '/iot/access/dashboard/real-time', params })
    return result.records || []
  } catch (error) {
    // 如果后端API不存在，返回模拟数据
    console.warn('智慧通行实时数据API未实现，使用模拟数据')
    return [
      {
        id: 1,
        type: 'access',
        userName: '张三',
        deviceName: '大门闸机01',
        location: '主入口',
        time: new Date(Date.now() - 1000 * 60 * 2),
        result: 'success',
        photo: '/avatar/default.jpg'
      },
      {
        id: 2,
        type: 'visitor',
        userName: '李四',
        deviceName: '访客通道',
        location: '前台',
        time: new Date(Date.now() - 1000 * 60 * 5),
        result: 'success',
        photo: '/avatar/default.jpg'
      },
      {
        id: 3,
        type: 'vehicle',
        userName: '王五',
        deviceName: '车库入口',
        location: '地下车库',
        time: new Date(Date.now() - 1000 * 60 * 8),
        result: 'success'
      }
    ]
  }
}

// 获取设备状态概览
export const getDeviceStatusOverview = () => {
  return request.get({ url: '/iot/access/dashboard/device-status' })
}

// 获取热力图数据
export const getAccessHeatmapData = (params: { date: Date, type?: string }) => {
  return request.get({ url: '/iot/access/dashboard/heatmap', params })
}

// 获取异常事件列表
export const getAbnormalEventList = (params: { pageSize?: number, level?: string }) => {
  return request.get({ url: '/iot/access/dashboard/abnormal-events', params })
}


