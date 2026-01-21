/**
 * IoT 设备相关字典工具
 * 
 * 使用说明：
 * 1. 数据库使用英文状态值（online, offline, fault）
 * 2. 前端通过此字典工具将英文转换为中文显示
 * 3. 符合国际化（i18n）规范，便于多语言扩展
 * 
 * @author IBMS Team
 */

/**
 * IoT 设备状态字典
 */
export const IOT_DEVICE_STATUS_DICT = [
  { 
    value: 'online', 
    label: '在线', 
    type: 'success' as const,
    color: '#67C23A',
    icon: 'CircleCheck'
  },
  { 
    value: 'offline', 
    label: '离线', 
    type: 'info' as const,
    color: '#909399',
    icon: 'CircleClose'
  },
  { 
    value: 'fault', 
    label: '故障', 
    type: 'danger' as const,
    color: '#F56C6C',
    icon: 'WarningFilled'
  }
] as const

/**
 * IoT 设备类型字典
 */
export const IOT_DEVICE_TYPE_DICT = [
  { value: '传感器', label: '传感器', type: 'primary' as const },
  { value: '摄像头', label: '摄像头', type: 'success' as const },
  { value: '控制器', label: '控制器', type: 'warning' as const },
  { value: '照明', label: '照明', type: 'info' as const },
  { value: '空调', label: '空调', type: 'info' as const },
  { value: '门禁', label: '门禁', type: 'warning' as const },
  { value: '监控', label: '监控', type: 'success' as const },
  { value: '消防', label: '消防', type: 'danger' as const }
] as const

/**
 * IoT 设备健康状态字典
 */
export const IOT_DEVICE_HEALTH_DICT = [
  { value: '正常', label: '正常', type: 'success' as const },
  { value: '警告', label: '警告', type: 'warning' as const },
  { value: '异常', label: '异常', type: 'danger' as const }
] as const

/**
 * 通用：获取字典项
 */
export const getDictItem = (dict: readonly any[], value: string) => {
  return dict.find(item => item.value === value)
}

/**
 * 通用：获取字典标签
 */
export const getDictItemLabel = (dict: readonly any[], value: string): string => {
  return getDictItem(dict, value)?.label || value
}

/**
 * 通用：获取字典类型
 */
export const getDictItemType = (dict: readonly any[], value: string) => {
  return getDictItem(dict, value)?.type || 'info'
}

/**
 * 通用：获取字典颜色
 */
export const getDictItemColor = (dict: readonly any[], value: string) => {
  return getDictItem(dict, value)?.color
}

/**
 * 通用：获取字典图标
 */
export const getDictItemIcon = (dict: readonly any[], value: string) => {
  return getDictItem(dict, value)?.icon
}

// ============= 设备状态便捷函数 =============

/**
 * 获取设备状态标签（online → 在线）
 */
export const getDeviceStatusLabel = (value: string): string => {
  return getDictItemLabel(IOT_DEVICE_STATUS_DICT, value)
}

/**
 * 获取设备状态类型（online → success）
 */
export const getDeviceStatusType = (value: string) => {
  return getDictItemType(IOT_DEVICE_STATUS_DICT, value)
}

/**
 * 获取设备状态颜色（online → #67C23A）
 */
export const getDeviceStatusColor = (value: string) => {
  return getDictItemColor(IOT_DEVICE_STATUS_DICT, value)
}

/**
 * 获取设备状态图标（online → CircleCheck）
 */
export const getDeviceStatusIcon = (value: string) => {
  return getDictItemIcon(IOT_DEVICE_STATUS_DICT, value)
}

// ============= 设备类型便捷函数 =============

/**
 * 获取设备类型类型（用于 el-tag）
 */
export const getDeviceTypeLabel = (value: string) => {
  return getDictItemType(IOT_DEVICE_TYPE_DICT, value)
}

// ============= 设备健康状态便捷函数 =============

/**
 * 获取设备健康状态类型（用于 el-tag）
 */
export const getDeviceHealthType = (value: string) => {
  return getDictItemType(IOT_DEVICE_HEALTH_DICT, value)
}












