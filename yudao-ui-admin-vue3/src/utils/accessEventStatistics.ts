/**
 * 门禁事件统计服务
 * 提供事件统计计算和增量更新功能
 * 
 * **Feature: access-event-full-integration**
 * **Validates: Requirements 7.1, 7.2**
 */

import { getEventCategory, EventCategory } from './accessEventTypes'

// ==================== 统计数据接口 ====================

/**
 * 事件统计数据结构
 * 满足 Property 7: total = alarmCount + abnormalCount + normalCount
 */
export interface EventStatistics {
  /** 总事件数 */
  total: number
  /** 报警事件数 */
  alarmCount: number
  /** 异常事件数（验证失败） */
  abnormalCount: number
  /** 正常事件数 */
  normalCount: number
}

/**
 * 事件数据接口（用于统计计算）
 */
export interface EventData {
  /** 事件类型代码（字符串，如 'CARD_SWIPE', 'BREAK_IN'） */
  eventType: string
  /** 验证结果（1-成功，0-失败，可选） */
  verifyResult?: number
}

// ==================== 统计函数 ====================

/**
 * 计算事件统计数据
 * 
 * 分类规则：
 * 1. 报警事件：eventType 属于报警类型（DOOR_NOT_CLOSED, BREAK_IN 等）
 * 2. 异常事件：verifyResult === 0（验证失败）
 * 3. 正常事件：其他所有事件
 * 
 * @param events 事件列表
 * @returns 统计数据，满足 total = alarmCount + abnormalCount + normalCount
 * 
 * **Property 7: Statistics Calculation Accuracy**
 * **Validates: Requirements 7.1**
 */
export function calculateStatistics(events: EventData[]): EventStatistics {
  const stats: EventStatistics = {
    total: events.length,
    alarmCount: 0,
    abnormalCount: 0,
    normalCount: 0
  }
  
  for (const event of events) {
    const category = getEventCategory(event.eventType, event.verifyResult)
    switch (category) {
      case EventCategory.ALARM:
        stats.alarmCount++
        break
      case EventCategory.ABNORMAL:
        stats.abnormalCount++
        break
      case EventCategory.NORMAL:
        stats.normalCount++
        break
    }
  }
  
  return stats
}

/**
 * 更新统计数据（增量更新）
 * 当收到新的实时事件时，使用此函数更新统计数据
 * 
 * @param currentStats 当前统计数据
 * @param newEvent 新事件
 * @returns 更新后的统计数据（不可变更新）
 * 
 * **Property 7: Statistics Calculation Accuracy**
 * **Validates: Requirements 7.2**
 */
export function updateStatistics(
  currentStats: EventStatistics,
  newEvent: EventData
): EventStatistics {
  const category = getEventCategory(newEvent.eventType, newEvent.verifyResult)
  const updated = { ...currentStats, total: currentStats.total + 1 }
  
  switch (category) {
    case EventCategory.ALARM:
      updated.alarmCount++
      break
    case EventCategory.ABNORMAL:
      updated.abnormalCount++
      break
    case EventCategory.NORMAL:
      updated.normalCount++
      break
  }
  
  return updated
}

/**
 * 创建空的统计数据
 * @returns 空统计数据（所有计数为0）
 */
export function createEmptyStatistics(): EventStatistics {
  return {
    total: 0,
    alarmCount: 0,
    abnormalCount: 0,
    normalCount: 0
  }
}

/**
 * 合并两个统计数据
 * 用于合并不同时间段或不同来源的统计数据
 * 
 * @param stats1 统计数据1
 * @param stats2 统计数据2
 * @returns 合并后的统计数据
 */
export function mergeStatistics(stats1: EventStatistics, stats2: EventStatistics): EventStatistics {
  return {
    total: stats1.total + stats2.total,
    alarmCount: stats1.alarmCount + stats2.alarmCount,
    abnormalCount: stats1.abnormalCount + stats2.abnormalCount,
    normalCount: stats1.normalCount + stats2.normalCount
  }
}

/**
 * 验证统计数据的一致性
 * 检查 total === alarmCount + abnormalCount + normalCount
 * 
 * @param stats 统计数据
 * @returns 是否一致
 */
export function validateStatistics(stats: EventStatistics): boolean {
  return stats.total === stats.alarmCount + stats.abnormalCount + stats.normalCount
}

/**
 * 从统计数据中减去一个事件（用于删除事件时更新统计）
 * 
 * @param currentStats 当前统计数据
 * @param event 要减去的事件
 * @returns 更新后的统计数据
 */
export function decrementStatistics(
  currentStats: EventStatistics,
  event: EventData
): EventStatistics {
  const category = getEventCategory(event.eventType, event.verifyResult)
  const updated = { ...currentStats, total: Math.max(0, currentStats.total - 1) }
  
  switch (category) {
    case EventCategory.ALARM:
      updated.alarmCount = Math.max(0, updated.alarmCount - 1)
      break
    case EventCategory.ABNORMAL:
      updated.abnormalCount = Math.max(0, updated.abnormalCount - 1)
      break
    case EventCategory.NORMAL:
      updated.normalCount = Math.max(0, updated.normalCount - 1)
      break
  }
  
  return updated
}

/**
 * 计算统计数据的百分比
 * 
 * @param stats 统计数据
 * @returns 各类别的百分比
 */
export function calculatePercentages(stats: EventStatistics): {
  alarmPercent: number
  abnormalPercent: number
  normalPercent: number
} {
  if (stats.total === 0) {
    return { alarmPercent: 0, abnormalPercent: 0, normalPercent: 0 }
  }
  
  return {
    alarmPercent: Math.round((stats.alarmCount / stats.total) * 100),
    abnormalPercent: Math.round((stats.abnormalCount / stats.total) * 100),
    normalPercent: Math.round((stats.normalCount / stats.total) * 100)
  }
}
