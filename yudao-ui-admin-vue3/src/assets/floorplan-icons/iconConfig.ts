/**
 * 平面图设备图标配置
 * 统一管理所有设备图标，确保产品管理、设备模板、画布图标的一致性
 * 
 * 图标来源：F:\work\ch_ibms\平台开发平面图常用矢量图标
 */

/**
 * 图标配置类型
 */
export interface IconConfig {
  /** 图标键名（用于 deviceIconMap） */
  key: string
  /** 显示名称（用于产品名称匹配） */
  label: string
  /** 图标颜色 */
  color: string
  /** 分类 */
  category: string
  /** 别名（用于模糊匹配） */
  aliases?: string[]
}

/**
 * 标准化的图标配置列表
 * 🔑 这是唯一的图标配置源，所有页面都从这里获取
 */
export const ICON_CONFIGS: IconConfig[] = [
  // ==================== 摄像机类 ====================
  {
    key: '枪型摄像机',
    label: '枪型摄像机',
    color: '#1296db',
    category: '视频监控',
    aliases: ['枪机', '网络摄像机', 'bullet', 'camera']
  },
  {
    key: '半球摄像机',
    label: '半球摄像机',
    color: '#1296db',
    category: '视频监控',
    aliases: ['半球', 'dome']
  },
  {
    key: '球形摄像机',
    label: '球形摄像机',
    color: '#1296db',
    category: '视频监控',
    aliases: ['球机', 'ptz', 'ball']
  },
  
  // ==================== 门禁类 ====================
  {
    key: '车辆道闸',
    label: '车辆道闸',
    color: '#67c23a',
    category: '门禁管理',
    aliases: ['道闸', 'gate', 'barrier']
  },
  {
    key: '车辆识别一体机',
    label: '车辆识别一体机',
    color: '#409eff',
    category: '门禁管理',
    aliases: ['车牌识别', 'lpr', 'anpr']
  },
  {
    key: '人行闸机',
    label: '人行闸机',
    color: '#e6a23c',
    category: '门禁管理',
    aliases: ['闸机', 'turnstile', 'tripod']
  },
  {
    key: '人脸识别一体机',
    label: '人脸识别一体机',
    color: '#409eff',
    category: '门禁管理',
    aliases: ['人脸识别', '人脸', 'face recognition']
  },
  
  // ==================== 巡更类 ====================
  {
    key: '巡更点',
    label: '巡更点',
    color: '#f56c6c',
    category: '安防巡更',
    aliases: ['巡检点', 'patrol', 'checkpoint']
  },
  
  // ==================== 计量类 ====================
  {
    key: '水表',
    label: '水表',
    color: '#5dade2',
    category: '能源计量',
    aliases: ['水计量', 'water meter']
  },
  {
    key: '电表',
    label: '电表',
    color: '#f39c12',
    category: '能源计量',
    aliases: ['电计量', 'electric meter']
  },
  {
    key: '燃气表',
    label: '燃气表',
    color: '#e74c3c',
    category: '能源计量',
    aliases: ['气表', 'gas meter']
  },
  
  // ==================== 考勤类 ====================
  {
    key: '考勤机',
    label: '考勤机',
    color: '#9b59b6',
    category: '考勤管理',
    aliases: ['打卡机', 'attendance']
  }
]

/**
 * 根据产品名称获取图标配置
 * 
 * @param productName 产品名称
 * @returns 图标配置对象
 */
export function getIconConfigByProductName(productName: string): IconConfig | undefined {
  if (!productName) return undefined
  
  const lowerName = productName.toLowerCase()
  
  // 1. 精确匹配标签
  const exactMatch = ICON_CONFIGS.find(config => config.label === productName)
  if (exactMatch) return exactMatch
  
  // 2. 模糊匹配（包含关键字）
  for (const config of ICON_CONFIGS) {
    // 检查标签
    if (productName.includes(config.label) || config.label.includes(productName)) {
      return config
    }
    
    // 检查别名
    if (config.aliases) {
      for (const alias of config.aliases) {
        if (lowerName.includes(alias.toLowerCase())) {
          return config
        }
      }
    }
  }
  
  return undefined
}

/**
 * 获取图标键名（用于 deviceIconMap 查找）
 * 
 * @param productName 产品名称
 * @returns 图标键名
 */
export function getIconKeyByProductName(productName: string): string {
  const config = getIconConfigByProductName(productName)
  return config?.key || '枪型摄像机'  // 默认使用枪型摄像机
}

/**
 * 获取图标颜色
 * 
 * @param productName 产品名称
 * @returns 颜色值
 */
export function getIconColorByProductName(productName: string): string {
  const config = getIconConfigByProductName(productName)
  return config?.color || '#1296db'  // 默认蓝色
}

/**
 * 获取所有图标选项（用于产品管理的图标选择器）
 * 
 * @returns 图标选项列表
 */
export function getAllIconOptions() {
  return ICON_CONFIGS.map(config => ({
    value: config.key,
    label: config.label,
    color: config.color,
    category: config.category
  }))
}

/**
 * 按分类分组的图标选项
 */
export function getIconOptionsByCategory() {
  const grouped: Record<string, typeof ICON_CONFIGS> = {}
  
  for (const config of ICON_CONFIGS) {
    if (!grouped[config.category]) {
      grouped[config.category] = []
    }
    grouped[config.category].push(config)
  }
  
  return grouped
}






































