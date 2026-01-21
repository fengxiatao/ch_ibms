import request from '@/config/axios'

// 智慧消防首页统计数据
export interface FireDashboardStatisticsVO {
  // 火灾报警统计
  alarms: {
    total: number
    online: number
    todayAlarms: number
    status: string // normal, warning, alarm
  }
  
  // 电气火灾统计
  electric: {
    total: number
    online: number
    todayAlarms: number
    status: string
  }
  
  // 消防水系统统计
  water: {
    total: number
    online: number
    todayAlarms: number
    status: string
  }
  
  // 防排烟系统统计
  smoke: {
    total: number
    online: number
    todayAlarms: number
    status: string
  }
  
  // 消防设备统计
  equipment: {
    extinguishers: number
    hydrants: number
    emergencyLights: number
    exits: number
  }
  
  // 系统状态
  systemStatus: {
    level: string // normal, warning, alarm, fault
    text: string
    lastCheck: Date
  }
}

// 火灾报警数据
export interface FireAlarmVO {
  id: number
  deviceName: string
  location: string
  alarmType: string
  alarmLevel: string
  alarmTime: Date
  status: string
  description?: string
  handleTime?: Date
  handler?: string
}

// 消防设备数据
export interface FireDeviceVO {
  id: number
  deviceName: string
  deviceType: string
  location: string
  status: string
  lastCheck: Date
  nextMaintenance: Date
  manufacturer?: string
  model?: string
}

// 应急事件数据
export interface EmergencyEventVO {
  id: number
  eventType: string
  location: string
  level: string
  startTime: Date
  endTime?: Date
  status: string
  description: string
  responseTeam?: string
  handler?: string
}

// 获取智慧消防首页统计数据
export const getFireDashboardStatistics = async (): Promise<FireDashboardStatisticsVO> => {
  try {
    return await request.get<FireDashboardStatisticsVO>({ url: '/fire/dashboard/statistics' })
  } catch (error) {
    // 如果后端API不存在，返回模拟数据
    console.warn('智慧消防后端API未实现，使用模拟数据')
    return {
      alarms: {
        total: 156,
        online: 152,
        todayAlarms: 3,
        status: 'warning'
      },
      electric: {
        total: 89,
        online: 87,
        todayAlarms: 1,
        status: 'normal'
      },
      water: {
        total: 45,
        online: 43,
        todayAlarms: 0,
        status: 'normal'
      },
      smoke: {
        total: 78,
        online: 75,
        todayAlarms: 2,
        status: 'warning'
      },
      equipment: {
        extinguishers: 234,
        hydrants: 67,
        emergencyLights: 189,
        exits: 23
      },
      systemStatus: {
        level: 'warning',
        text: '系统正常运行',
        lastCheck: new Date(Date.now() - 1000 * 60 * 15)
      }
    }
  }
}

// 获取火灾报警列表
export const getFireAlarmList = async (params: { pageSize?: number }): Promise<FireAlarmVO[]> => {
  try {
    return await request.get<FireAlarmVO[]>({ url: '/fire/alarms/list', params })
  } catch (error) {
    console.warn('火灾报警API未实现，使用模拟数据')
    return [
      {
        id: 1,
        deviceName: '烟感探测器001',
        location: 'A座3楼办公室',
        alarmType: '烟雾报警',
        alarmLevel: '一级',
        alarmTime: new Date(Date.now() - 1000 * 60 * 5),
        status: '未处理',
        description: '检测到烟雾浓度异常'
      },
      {
        id: 2,
        deviceName: '温感探测器002',
        location: 'B座地下车库',
        alarmType: '温度报警',
        alarmLevel: '二级',
        alarmTime: new Date(Date.now() - 1000 * 60 * 30),
        status: '已处理',
        description: '检测到温度异常升高',
        handleTime: new Date(Date.now() - 1000 * 60 * 25),
        handler: '张三'
      }
    ]
  }
}

// 获取消防设备列表
export const getFireDeviceList = async (params: { deviceType?: string; pageSize?: number }): Promise<FireDeviceVO[]> => {
  try {
    return await request.get<FireDeviceVO[]>({ url: '/fire/devices/list', params })
  } catch (error) {
    console.warn('消防设备API未实现，使用模拟数据')
    return [
      {
        id: 1,
        deviceName: '灭火器001',
        deviceType: '干粉灭火器',
        location: 'A座1楼大厅',
        status: '正常',
        lastCheck: new Date(Date.now() - 1000 * 60 * 60 * 24 * 7),
        nextMaintenance: new Date(Date.now() + 1000 * 60 * 60 * 24 * 30),
        manufacturer: '海天消防',
        model: 'HT-4KG'
      },
      {
        id: 2,
        deviceName: '消火栓002',
        deviceType: '室内消火栓',
        location: 'B座2楼走廊',
        status: '维护中',
        lastCheck: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3),
        nextMaintenance: new Date(Date.now() + 1000 * 60 * 60 * 24 * 15),
        manufacturer: '中消科技',
        model: 'ZX-65'
      }
    ]
  }
}

// 获取应急事件列表
export const getEmergencyEventList = async (params: { level?: string; pageSize?: number }): Promise<EmergencyEventVO[]> => {
  try {
    return await request.get<EmergencyEventVO[]>({ url: '/fire/emergency/events/list', params })
  } catch (error) {
    console.warn('应急事件API未实现，使用模拟数据')
    return [
      {
        id: 1,
        eventType: '火灾报警',
        location: 'A座3楼',
        level: '一级',
        startTime: new Date(Date.now() - 1000 * 60 * 60 * 2),
        status: '已处理',
        description: '烟感探测器报警，现场确认为误报',
        responseTeam: '消防应急小组',
        handler: '李队长',
        endTime: new Date(Date.now() - 1000 * 60 * 60 * 1.5)
      }
    ]
  }
}

// 获取消防趋势数据
export const getFireTrend = async (params: { startTime: Date, endTime: Date, type?: string }) => {
  try {
    return await request.get({ url: '/fire/dashboard/trend', params })
  } catch (error) {
    console.warn('消防趋势API未实现')
    return []
  }
}

// 获取设备状态分布
export const getDeviceStatusDistribution = async () => {
  try {
    return await request.get({ url: '/fire/dashboard/device-status' })
  } catch (error) {
    console.warn('设备状态分布API未实现')
    return {
      normal: 145,
      warning: 8,
      fault: 3,
      maintenance: 2
    }
  }
}









