import request from '@/config/axios'

// ==================== 设备管理 ====================

export interface ChanghuiDeviceVO {
  id: number
  stationCode: string
  deviceName: string
  deviceType: number
  provinceCode: string
  managementCode: string
  stationCodePart: string
  pileFront: string
  pileBack: string
  manufacturer: string
  sequenceNo: string
  teaKey: string
  password: string
  status: number
  lastHeartbeat: Date
  createTime: Date
}

export interface ChanghuiDevicePageReqVO {
  pageNo: number
  pageSize: number
  stationCode?: string
  deviceName?: string
  deviceType?: number
  status?: number
}

// 长辉设备类型枚举
export const ChanghuiDeviceTypeEnum = {
  INTEGRATED_GATE: 1,    // 测控一体化闸门
  SEPARATE_GATE: 2,      // 测控分体式闸门
  DRAIN_GATE: 3,         // 退水闸
  CONTROL_GATE: 4,       // 节制闸
  INLET_GATE: 5,         // 进水闸
  WATER_LEVEL_METER: 6,  // 水位计
  FLOW_METER: 7,         // 流量计
  VELOCITY_METER: 8,     // 流速仪
  SEEPAGE_METER: 9       // 渗压计
}

// 长辉设备类型选项
/**
 * @deprecated 已改用数据字典 DICT_TYPE.CHANGHUI_DEVICE_TYPE
 * 请使用 getIntDictOptions(DICT_TYPE.CHANGHUI_DEVICE_TYPE) 获取设备类型选项
 * 此数组仅作为后备兼容，新代码请勿使用
 */
export const ChanghuiDeviceTypeOptions = [
  { value: 1, label: '测控一体化闸门' },
  { value: 2, label: '测控分体式闸门' },
  { value: 3, label: '退水闸' },
  { value: 4, label: '节制闸' },
  { value: 5, label: '进水闸' },
  { value: 6, label: '水位计' },
  { value: 7, label: '流量计' },
  { value: 8, label: '流速仪' },
  { value: 9, label: '渗压计' },
  { value: 10, label: '荷载计' },
  { value: 11, label: '温度计' },
  { value: 12, label: '启闭机' }
]

// 设备状态选项
export const ChanghuiDeviceStatusOptions = [
  { value: 0, label: '离线', type: 'danger' },
  { value: 1, label: '在线', type: 'success' }
]


export const ChanghuiDeviceApi = {
  // 注册设备
  registerDevice: async (data: ChanghuiDeviceVO) => {
    return await request.post({ url: '/iot/changhui/device/register', data })
  },
  // 更新设备
  updateDevice: async (data: ChanghuiDeviceVO) => {
    return await request.put({ url: '/iot/changhui/device/update', data })
  },
  // 删除设备
  deleteDevice: async (id: number) => {
    return await request.delete({ url: '/iot/changhui/device/delete', params: { id } })
  },
  // 获取设备
  getDevice: async (id: number) => {
    return await request.get({ url: '/iot/changhui/device/get', params: { id } })
  },
  // 根据测站编码获取设备
  getDeviceByStationCode: async (stationCode: string) => {
    return await request.get({ url: '/iot/changhui/device/get-by-station-code', params: { stationCode } })
  },
  // 获取设备分页
  getDevicePage: async (params: ChanghuiDevicePageReqVO) => {
    return await request.get({ url: '/iot/changhui/device/page', params })
  },
  // 获取在线设备列表
  getOnlineDevices: async () => {
    return await request.get({ url: '/iot/changhui/device/online' })
  }
}

// ==================== 报警管理 ====================

export interface ChanghuiAlarmVO {
  id: number
  deviceId: number
  stationCode: string
  alarmType: string
  alarmValue: string
  alarmTime: Date
  status: number
  ackTime: Date
  ackUser: string
  createTime: Date
}

export interface ChanghuiAlarmPageReqVO {
  pageNo: number
  pageSize: number
  stationCode?: string
  alarmType?: string
  status?: number
  alarmTimeStart?: Date
  alarmTimeEnd?: Date
}

// 报警类型选项
export const ChanghuiAlarmTypeOptions = [
  { value: 'OVER_TORQUE', label: '过力矩报警' },
  { value: 'OVER_CURRENT', label: '过电流报警' },
  { value: 'OVER_VOLTAGE', label: '过电压报警' },
  { value: 'LOW_VOLTAGE', label: '低电压报警' },
  { value: 'WATER_LEVEL', label: '水位超限报警' },
  { value: 'GATE_POSITION', label: '闸位超限报警' }
]

// 报警状态选项
export const ChanghuiAlarmStatusOptions = [
  { value: 0, label: '未确认', type: 'warning' },
  { value: 1, label: '已确认', type: 'success' }
]

export const ChanghuiAlarmApi = {
  // 获取报警分页
  getAlarmPage: async (params: ChanghuiAlarmPageReqVO) => {
    return await request.get({ url: '/iot/changhui/alarm/page', params })
  },
  // 确认报警
  acknowledgeAlarm: async (alarmId: number, ackUser?: string) => {
    return await request.post({ 
      url: '/iot/changhui/alarm/acknowledge', 
      params: { alarmId, ackUser: ackUser || 'admin' } 
    })
  },
  // 获取未确认报警数量
  getUnacknowledgedCount: async () => {
    return await request.get({ url: '/iot/changhui/alarm/unacknowledged-count' })
  }
}


// ==================== 控制管理 ====================

export interface ChanghuiControlLogVO {
  id: number
  deviceId: number
  stationCode: string
  controlType: string
  controlParams: string
  result: number
  errorMessage: string
  operator: string
  operateTime: Date
  createTime: Date
}

export interface ChanghuiControlLogPageReqVO {
  pageNo: number
  pageSize: number
  stationCode?: string
  controlType?: string
  result?: number
  operateTimeStart?: Date
  operateTimeEnd?: Date
}

// 控制类型选项
export const ChanghuiControlTypeOptions = [
  { value: 'MODE_SWITCH', label: '模式切换' },
  { value: 'MANUAL_RISE', label: '手动上升' },
  { value: 'MANUAL_FALL', label: '手动下降' },
  { value: 'MANUAL_STOP', label: '手动停止' },
  { value: 'AUTO_FLOW', label: '自动流量控制' },
  { value: 'AUTO_OPENING', label: '自动开度控制' },
  { value: 'AUTO_LEVEL', label: '自动水位控制' },
  { value: 'AUTO_VOLUME', label: '自动水量控制' }
]

export const ChanghuiControlApi = {
  // 模式切换
  switchMode: async (stationCode: string, mode: string) => {
    return await request.post({ url: '/iot/changhui/control/mode-switch', params: { stationCode, mode } })
  },
  // 手动控制
  manualControl: async (stationCode: string, action: string) => {
    return await request.post({ url: '/iot/changhui/control/manual', params: { stationCode, action } })
  },
  // 自动控制
  autoControl: async (data: { stationCode: string; command: string; value: number }) => {
    return await request.post({ 
      url: '/iot/changhui/control/auto', 
      params: { stationCode: data.stationCode, controlType: data.command, targetValue: data.value } 
    })
  },
  // 获取控制记录
  getControlLogs: async (params: ChanghuiControlLogPageReqVO) => {
    return await request.get({ url: '/iot/changhui/control/logs', params })
  }
}


// ==================== 升级管理 ====================

export interface ChanghuiFirmwareVO {
  id: number
  name: string
  version: string
  deviceType: number
  filePath: string
  fileSize: number
  fileMd5: string
  description: string
  createTime: Date
}

export interface ChanghuiUpgradeTaskVO {
  id: number
  deviceId: number
  stationCode: string
  firmwareId: number
  firmwareVersion: string
  upgradeMode: number
  firmwareUrl?: string
  status: number
  progress: number
  totalFrames: number
  sentFrames: number
  retryCount: number
  startTime: Date
  endTime: Date
  errorMessage: string
  createTime: Date
}

// 升级模式选项
// 注意：TCP帧传输模式(value=0)暂未完全实现，已隐藏
export const ChanghuiUpgradeModeOptions = [
  // { value: 0, label: 'TCP帧传输', description: '平台通过TCP直接推送固件数据帧' },
  { value: 1, label: 'HTTP URL下载', description: '平台发送URL，设备自行HTTP下载' }
]

export interface ChanghuiUpgradeTaskPageReqVO {
  pageNo: number
  pageSize: number
  stationCode?: string
  status?: number
}

// 升级任务状态选项
export const ChanghuiUpgradeStatusOptions = [
  { value: 0, label: '待执行', type: 'info' },
  { value: 1, label: '进行中', type: 'primary' },
  { value: 2, label: '成功', type: 'success' },
  { value: 3, label: '失败', type: 'danger' },
  { value: 4, label: '已取消', type: 'warning' },
  { value: 5, label: '已拒绝', type: 'danger' }
]

// 批量升级请求VO
export interface ChanghuiBatchUpgradeCreateReqVO {
  stationCodes: string[]  // 测站编码列表（后端要求）
  firmwareId: number
  upgradeMode: number
}

// 批量升级结果VO
export interface ChanghuiBatchUpgradeResultVO {
  totalDevices: number
  successCount: number
  failedCount: number
  taskIds: number[]
  failedDetails: Record<number, string>
}

// 批量升级进度VO
export interface ChanghuiBatchUpgradeProgressVO {
  totalTasks: number
  completedCount: number
  inProgressCount: number
  pendingCount: number
  failedCount: number
  overallProgress: number
}

export const ChanghuiUpgradeApi = {
  // 上传固件（支持文件上传）
  uploadFirmware: async (data: FormData) => {
    return await request.upload({ url: '/iot/changhui/upgrade/firmware/upload', data })
  },
  // 获取固件列表
  getFirmwareList: async (deviceType?: number) => {
    return await request.get({ url: '/iot/changhui/upgrade/firmware/list', params: { deviceType } })
  },
  // 创建升级任务（支持双模式）
  createUpgradeTask: async (stationCode: string, firmwareId: number, upgradeMode: number = 0) => {
    return await request.post({ url: '/iot/changhui/upgrade/task/create', data: { stationCode, firmwareId, upgradeMode } })
  },
  // 批量创建升级任务
  createBatchUpgradeTasks: async (data: ChanghuiBatchUpgradeCreateReqVO): Promise<ChanghuiBatchUpgradeResultVO> => {
    return await request.post({ url: '/iot/changhui/upgrade/task/batch-create', data })
  },
  // 获取批量升级进度
  getBatchUpgradeProgress: async (taskIds: number[]): Promise<ChanghuiBatchUpgradeProgressVO> => {
    return await request.get({ url: '/iot/changhui/upgrade/task/batch-progress', params: { taskIds: taskIds.join(',') } })
  },
  // 取消升级任务
  cancelUpgradeTask: async (taskId: number) => {
    return await request.put({ url: '/iot/changhui/upgrade/task/cancel', params: { id: taskId } })
  },
  // 重试升级任务
  retryUpgradeTask: async (taskId: number) => {
    return await request.put({ url: '/iot/changhui/upgrade/task/retry', params: { id: taskId } })
  },
  // 清理超时任务
  cleanupTimeoutTasks: async (): Promise<number> => {
    return await request.post({ url: '/iot/changhui/upgrade/task/cleanup-timeout' })
  },
  // 获取升级任务
  getUpgradeTask: async (taskId: number) => {
    return await request.get({ url: '/iot/changhui/upgrade/task/get', params: { id: taskId } })
  },
  // 获取升级任务分页
  getUpgradeTaskPage: async (params: ChanghuiUpgradeTaskPageReqVO) => {
    return await request.get({ url: '/iot/changhui/upgrade/task/page', params })
  },
  // 获取固件下载URL
  getFirmwareDownloadUrl: async (firmwareId: number) => {
    return await request.get({ url: `/iot/changhui/upgrade/firmware/download-url/${firmwareId}` })
  }
}


// ==================== 数据管理 ====================

// 数据响应VO
export interface ChanghuiDataRespVO {
  id?: number
  stationCode: string
  indicator: string
  value: number | string
  timestamp: Date | string
  isNew?: boolean
}

// 历史数据查询参数
export interface ChanghuiDataHistoryReqVO {
  stationCode: string
  indicator?: string
  startTime?: string
  endTime?: string
  pageNo?: number
  pageSize?: number
}

// 数据查询参数
export interface ChanghuiDataQueryParams {
  stationCode: string
  indicator?: string
  indicators?: string[]
  startTime?: string | number
  endTime?: string | number
  pageNo?: number
  pageSize?: number
}

// 指标类型枚举 - 对应 Requirements 11.1-11.9
export const ChanghuiIndicatorEnum = {
  WATER_LEVEL: 'waterLevel',           // 水位 (AFN=0x06) - Req 11.1
  INSTANT_FLOW: 'instantFlow',         // 瞬时流量 (AFN=0x07) - Req 11.2
  INSTANT_VELOCITY: 'instantVelocity', // 瞬时流速 (AFN=0x08) - Req 11.3
  CUMULATIVE_FLOW: 'cumulativeFlow',   // 累计流量 (AFN=0x0A) - Req 11.4
  GATE_POSITION: 'gatePosition',       // 闸位 (AFN=0x0B) - Req 11.5
  TEMPERATURE: 'temperature',          // 温度 (AFN=0x0D) - Req 11.6
  SEEPAGE_PRESSURE: 'seepagePressure', // 渗透水压力 (AFN=0x0C) - Req 11.7
  LOAD: 'load',                        // 荷重 (AFN=0x0E) - Req 11.8
  CONDUCTIVITY: 'conductivity',        // 流体电导比 (AFN=0x09)
  REMAINING_4G: 'remaining4g'          // 4G卡剩余流量 (AFN=0x0F)
}

// 指标选项列表
export const ChanghuiIndicatorOptions = [
  { value: 'waterLevel', label: '水位', unit: 'm', afn: '0x06' },
  { value: 'instantFlow', label: '瞬时流量', unit: 'L/s', afn: '0x07' },
  { value: 'instantVelocity', label: '瞬时流速', unit: 'm/s', afn: '0x08' },
  { value: 'cumulativeFlow', label: '累计流量', unit: 'm³', afn: '0x0A' },
  { value: 'gatePosition', label: '闸位', unit: 'mm', afn: '0x0B' },
  { value: 'temperature', label: '温度', unit: '°C', afn: '0x0D' },
  { value: 'seepagePressure', label: '渗透水压力', unit: 'kPa', afn: '0x0C' },
  { value: 'load', label: '荷重', unit: 'kN', afn: '0x0E' },
  { value: 'conductivity', label: '流体电导比', unit: '', afn: '0x09' },
  { value: 'remaining4g', label: '4G卡剩余流量', unit: 'MB', afn: '0x0F' }
]

// 获取指标标签
export const getChanghuiIndicatorLabel = (value: string): string => {
  const item = ChanghuiIndicatorOptions.find(i => i.value === value)
  return item ? item.label : value
}

// 获取指标单位
export const getChanghuiIndicatorUnit = (value: string): string => {
  const item = ChanghuiIndicatorOptions.find(i => i.value === value)
  return item ? item.unit : ''
}

/**
 * 长辉设备数据 API
 * 实现 Requirements 11.1-11.9 (数据采集) 和 12.1-12.3 (数据存储和历史查询)
 */
export const ChanghuiDataApi = {
  /**
   * 查询单个指标
   * @param stationCode 测站编码
   * @param indicator 指标类型
   * @returns 指标数据
   * Requirements: 11.1-11.8
   */
  queryData: async (stationCode: string, indicator: string): Promise<ChanghuiDataRespVO> => {
    return await request.get({ url: '/iot/changhui/data/query', params: { stationCode, indicator } })
  },

  /**
   * 查询多个指标 (动态指标查询)
   * @param stationCode 测站编码
   * @param indicators 指标类型列表
   * @returns 多个指标数据
   * Requirements: 11.9 (动态指标查询 AFN=0x83)
   */
  queryMultipleData: async (stationCode: string, indicators: string[]): Promise<ChanghuiDataRespVO[]> => {
    return await request.get({ 
      url: '/iot/changhui/data/query-multiple', 
      params: { stationCode, indicators: indicators.join(',') } 
    })
  },

  /**
   * 获取历史数据
   * @param params 查询参数
   * @returns 分页历史数据
   * Requirements: 12.1, 12.2 (数据存储和历史查询)
   */
  getHistoryData: async (params: ChanghuiDataHistoryReqVO) => {
    return await request.get({ url: '/iot/changhui/data/history', params })
  },

  /**
   * 获取最新数据
   * @param stationCode 测站编码
   * @returns 所有指标的最新数据
   * Requirements: 12.3
   */
  getLatestData: async (stationCode: string): Promise<Record<string, any>> => {
    return await request.get({ url: '/iot/changhui/data/latest', params: { stationCode } })
  },

  /**
   * 导出历史数据
   * @param params 查询参数
   * @returns Blob数据
   * Requirements: 12.2
   */
  exportHistoryData: async (params: ChanghuiDataHistoryReqVO) => {
    return await request.download({ url: '/iot/changhui/data/export', params })
  }
}

// 导出便捷方法
export const getChanghuiDevicePage = ChanghuiDeviceApi.getDevicePage
export const getChanghuiLatestData = ChanghuiDataApi.getLatestData
export const getChanghuiHistoryData = ChanghuiDataApi.getHistoryData
export const queryChanghuiData = ChanghuiDataApi.queryData
export const queryChanghuiMultipleData = ChanghuiDataApi.queryMultipleData
