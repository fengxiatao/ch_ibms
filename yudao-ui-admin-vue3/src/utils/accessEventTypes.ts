/**
 * 门禁事件类型常量和工具函数
 * 基于大华SDK编程指导手册中定义的门禁报警事件类型
 * 
 * **Feature: access-event-full-integration**
 * **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 8.3**
 */

// ==================== 事件类别枚举 ====================

export enum EventCategory {
  ALARM = 'ALARM',       // 报警事件
  ABNORMAL = 'ABNORMAL', // 异常事件（验证失败）
  NORMAL = 'NORMAL'      // 正常事件
}

// ==================== 事件类别配置 ====================

export interface EventCategoryStyle {
  label: string
  color: string
  icon: string
}

export const EventCategoryConfig: Record<EventCategory, EventCategoryStyle> = {
  [EventCategory.ALARM]: {
    label: '报警事件',
    color: 'danger',
    icon: 'Warning'
  },
  [EventCategory.ABNORMAL]: {
    label: '异常事件',
    color: 'warning',
    icon: 'CircleClose'
  },
  [EventCategory.NORMAL]: {
    label: '正常事件',
    color: 'success',
    icon: 'CircleCheck'
  }
}

// ==================== 事件类型定义接口 ====================

export interface EventTypeDefinition {
  code: string
  name: string
  category: EventCategory
}

// ==================== 正常开门事件类型 ====================
// 对应 NET_ALARM_ACCESS_CTL_EVENT 中的正常开门事件

export const NormalEventTypes: EventTypeDefinition[] = [
  { code: 'CARD_SWIPE', name: '刷卡开门', category: EventCategory.NORMAL },
  { code: 'FINGERPRINT', name: '指纹开门', category: EventCategory.NORMAL },
  { code: 'FACE_RECOGNIZE', name: '人脸开门', category: EventCategory.NORMAL },
  { code: 'PASSWORD', name: '密码开门', category: EventCategory.NORMAL },
  { code: 'QRCODE', name: '二维码开门', category: EventCategory.NORMAL },
  { code: 'REMOTE_OPEN', name: '远程开门', category: EventCategory.NORMAL },
  { code: 'BUTTON_OPEN', name: '按钮开门', category: EventCategory.NORMAL },
  { code: 'MULTI_PERSON_OPEN', name: '多人组合开门', category: EventCategory.NORMAL },
  { code: 'ACCESS_STATUS', name: '门禁状态事件', category: EventCategory.NORMAL },
  { code: 'FINGERPRINT_CAPTURE', name: '指纹采集事件', category: EventCategory.NORMAL }
]

// ==================== 报警事件类型 ====================
// 基于大华SDK编程指导手册 - 门禁报警事件类型

export const AlarmEventTypes: EventTypeDefinition[] = [
  { code: 'DOOR_NOT_CLOSED', name: '门未关报警', category: EventCategory.ALARM },     // NET_ALARM_ACCESS_CTL_NOT_CLOSE
  { code: 'BREAK_IN', name: '闯入报警', category: EventCategory.ALARM },               // NET_ALARM_ACCESS_CTL_BREAK_IN
  { code: 'REPEAT_ENTER', name: '反复进入报警', category: EventCategory.ALARM },       // NET_ALARM_ACCESS_CTL_REPEAT_ENTER
  { code: 'MALICIOUS_OPEN', name: '恶意开门报警', category: EventCategory.ALARM },     // NET_ALARM_ACCESS_CTL_MALICIOUS
  { code: 'DURESS', name: '胁迫报警', category: EventCategory.ALARM },                 // NET_ALARM_ACCESS_CTL_DURESS
  { code: 'TAMPER_ALARM', name: '防拆报警', category: EventCategory.ALARM },           // NET_ALARM_CHASSISINTRUDED
  { code: 'LOCAL_ALARM', name: '本地报警', category: EventCategory.ALARM }             // NET_ALARM_ALARM_EX2
]

// ==================== 报警事件类型代码集合 ====================

export const ALARM_TYPE_CODES: Set<string> = new Set(
  AlarmEventTypes.map(t => t.code)
)

// ==================== 所有事件类型合集 ====================

export const AllEventTypes: EventTypeDefinition[] = [
  ...NormalEventTypes,
  ...AlarmEventTypes
]

// ==================== 事件类型映射表 ====================

const EventTypeMap: Map<string, EventTypeDefinition> = new Map(
  AllEventTypes.map(t => [t.code, t])
)

// ==================== 工具函数 ====================

/**
 * 根据事件类型代码和验证结果获取事件类别
 * 
 * 分类规则（按优先级）：
 * 1. 如果事件类型属于报警类型（DOOR_NOT_CLOSED, BREAK_IN等），返回 ALARM
 * 2. 如果验证结果为失败(0)，返回 ABNORMAL
 * 3. 否则返回事件类型定义的类别（默认 NORMAL）
 * 
 * @param eventType 事件类型代码（字符串）
 * @param verifyResult 验证结果（1-成功，0-失败，可选）
 * @returns 事件类别
 * 
 * **Validates: Requirements 5.1, 5.2, 5.3, 5.4**
 */
export function getEventCategory(eventType: string, verifyResult?: number): EventCategory {
  // 报警事件优先判断
  if (ALARM_TYPE_CODES.has(eventType)) {
    return EventCategory.ALARM
  }
  
  // 验证失败为异常事件
  if (verifyResult === 0) {
    return EventCategory.ABNORMAL
  }
  
  // 返回事件类型定义的类别，默认为 NORMAL
  const typeDef = EventTypeMap.get(eventType)
  return typeDef?.category ?? EventCategory.NORMAL
}

/**
 * 根据事件类型代码获取事件名称
 * 
 * @param eventType 事件类型代码（字符串）
 * @returns 事件名称，如果未找到则返回 "未知事件(code)"
 * 
 * **Validates: Requirements 8.3**
 */
export function getEventTypeName(eventType: string): string {
  const typeDef = EventTypeMap.get(eventType)
  return typeDef?.name ?? `未知事件(${eventType})`
}

/**
 * 根据事件类别获取样式配置
 * 
 * @param category 事件类别
 * @returns 样式配置 { label, color, icon }
 * 
 * **Validates: Requirements 5.2, 5.3, 5.4**
 */
export function getCategoryStyle(category: EventCategory): EventCategoryStyle {
  return EventCategoryConfig[category]
}

/**
 * 获取事件类别选项列表（用于下拉选择器）
 * @returns 类别选项数组
 */
export function getEventCategoryOptions() {
  return [
    { value: '', label: '全部' },
    { value: EventCategory.ALARM, label: '报警事件', type: 'danger' },
    { value: EventCategory.ABNORMAL, label: '异常事件', type: 'warning' },
    { value: EventCategory.NORMAL, label: '正常事件', type: 'success' }
  ]
}

/**
 * 根据类别获取该类别下的所有事件类型选项
 * @param category 事件类别（可选，不传则返回所有类型）
 * @returns 事件类型选项数组
 */
export function getEventTypeOptionsByCategory(category?: EventCategory | '') {
  if (!category) {
    return AllEventTypes.map(t => ({ value: t.code, label: t.name }))
  }
  
  switch (category) {
    case EventCategory.ALARM:
      return AlarmEventTypes.map(t => ({ value: t.code, label: t.name }))
    case EventCategory.NORMAL:
      return NormalEventTypes.map(t => ({ value: t.code, label: t.name }))
    default:
      return AllEventTypes.map(t => ({ value: t.code, label: t.name }))
  }
}

/**
 * 根据事件类型代码获取完整的事件类型定义
 * @param eventType 事件类型代码
 * @returns 事件类型定义或 undefined
 */
export function getEventTypeDefinition(eventType: string): EventTypeDefinition | undefined {
  return EventTypeMap.get(eventType)
}

/**
 * 判断事件是否为报警事件
 * @param eventType 事件类型代码
 * @returns 是否为报警事件
 */
export function isAlarmEvent(eventType: string): boolean {
  return ALARM_TYPE_CODES.has(eventType)
}

/**
 * 判断事件是否为异常事件（基于验证结果）
 * @param verifyResult 验证结果（1-成功，0-失败）
 * @returns 是否为异常事件
 */
export function isAbnormalEvent(verifyResult: number): boolean {
  return verifyResult === 0
}

/**
 * 判断事件是否为正常事件
 * @param eventType 事件类型代码
 * @param verifyResult 验证结果（1-成功，0-失败）
 * @returns 是否为正常事件
 */
export function isNormalEvent(eventType: string, verifyResult?: number): boolean {
  return getEventCategory(eventType, verifyResult) === EventCategory.NORMAL
}

// ==================== 兼容旧版数字代码的映射（向后兼容） ====================

/**
 * 旧版数字代码到新版字符串代码的映射
 * 用于兼容旧数据
 */
export const LegacyCodeMapping: Record<number, string> = {
  1: 'CARD_SWIPE',
  2: 'PASSWORD',
  3: 'FINGERPRINT',
  4: 'FACE_RECOGNIZE',
  5: 'QRCODE',
  6: 'BUTTON_OPEN',
  9: 'BUTTON_OPEN',
  10: 'REMOTE_OPEN',
  12: 'MULTI_PERSON_OPEN',
  100: 'BREAK_IN',
  103: 'DOOR_NOT_CLOSED',
  104: 'DURESS',
  116: 'TAMPER_ALARM',
  125: 'REPEAT_ENTER',
  126: 'MALICIOUS_OPEN',
  127: 'LOCAL_ALARM'
}

/**
 * 将旧版数字代码转换为新版字符串代码
 * @param legacyCode 旧版数字代码
 * @returns 新版字符串代码
 */
export function convertLegacyCode(legacyCode: number): string {
  return LegacyCodeMapping[legacyCode] ?? 'UNKNOWN'
}
