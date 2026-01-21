/**
 * 门禁事件实时插入工具函数
 * 用于管理实时事件的插入顺序
 * 
 * **Feature: access-event-full-integration**
 * **Validates: Requirements 5.5**
 */

import type { EventData } from './accessEventStatistics'

/**
 * 事件日志接口（简化版，用于测试）
 */
export interface EventLogItem extends EventData {
  id: number | string
  eventTime: string | Date
  deviceName?: string
  channelName?: string
  personName?: string
  isRealtime?: boolean
  _highlight?: boolean
}

/**
 * 实时事件列表管理器
 * 负责管理实时事件的插入、限制和合并
 */
export interface RealtimeEventManager {
  /** 实时事件列表 */
  events: EventLogItem[]
  /** 最大事件数量 */
  maxEvents: number
}

/**
 * 创建空的实时事件管理器
 */
export function createRealtimeEventManager(maxEvents: number = 50): RealtimeEventManager {
  return {
    events: [],
    maxEvents
  }
}

/**
 * 插入新的实时事件到列表顶部
 * 
 * Property 6: Real-time Event Insertion Order
 * *For any* sequence of real-time events pushed via WebSocket, 
 * new events SHALL be displayed at the top of the event list (index 0).
 * 
 * @param manager 事件管理器
 * @param event 新事件
 * @returns 更新后的事件管理器（不可变）
 */
export function insertRealtimeEvent(
  manager: RealtimeEventManager,
  event: EventLogItem
): RealtimeEventManager {
  // 标记为实时事件
  const realtimeEvent: EventLogItem = {
    ...event,
    isRealtime: true,
    _highlight: true
  }

  // 插入到列表顶部（index 0）
  const newEvents = [realtimeEvent, ...manager.events]

  // 限制最大数量
  const limitedEvents = newEvents.slice(0, manager.maxEvents)

  return {
    ...manager,
    events: limitedEvents
  }
}

/**
 * 批量插入实时事件（保持顺序）
 * 事件按照数组顺序依次插入，最后一个事件会在列表最顶部
 * 
 * @param manager 事件管理器
 * @param events 事件数组（按时间顺序，最早的在前）
 * @returns 更新后的事件管理器
 */
export function insertRealtimeEventsBatch(
  manager: RealtimeEventManager,
  events: EventLogItem[]
): RealtimeEventManager {
  let result = manager
  for (const event of events) {
    result = insertRealtimeEvent(result, event)
  }
  return result
}

/**
 * 合并实时事件和历史事件列表
 * 实时事件显示在前，历史事件显示在后
 * 
 * @param realtimeEvents 实时事件列表
 * @param historyEvents 历史事件列表
 * @returns 合并后的列表
 */
export function mergeEventLists(
  realtimeEvents: EventLogItem[],
  historyEvents: EventLogItem[]
): EventLogItem[] {
  return [...realtimeEvents, ...historyEvents]
}

/**
 * 获取事件在列表中的位置
 * 
 * @param events 事件列表
 * @param eventId 事件ID
 * @returns 事件位置索引，未找到返回 -1
 */
export function getEventIndex(events: EventLogItem[], eventId: number | string): number {
  return events.findIndex(e => e.id === eventId)
}

/**
 * 移除事件的高亮标记
 * 
 * @param manager 事件管理器
 * @param eventId 事件ID
 * @returns 更新后的事件管理器
 */
export function removeHighlight(
  manager: RealtimeEventManager,
  eventId: number | string
): RealtimeEventManager {
  const newEvents = manager.events.map(event => {
    if (event.id === eventId) {
      return { ...event, _highlight: false }
    }
    return event
  })

  return {
    ...manager,
    events: newEvents
  }
}

/**
 * 清空实时事件列表
 * 
 * @param manager 事件管理器
 * @returns 清空后的事件管理器
 */
export function clearRealtimeEvents(manager: RealtimeEventManager): RealtimeEventManager {
  return {
    ...manager,
    events: []
  }
}

/**
 * 验证事件插入顺序是否正确
 * 新插入的事件应该在列表的第一个位置
 * 
 * @param eventsBefore 插入前的事件列表
 * @param eventsAfter 插入后的事件列表
 * @param newEvent 新插入的事件
 * @returns 是否符合插入顺序规则
 */
export function validateInsertionOrder(
  eventsBefore: EventLogItem[],
  eventsAfter: EventLogItem[],
  newEvent: EventLogItem
): boolean {
  // 新事件应该在第一个位置
  if (eventsAfter.length === 0) {
    return false
  }
  
  if (eventsAfter[0].id !== newEvent.id) {
    return false
  }

  // 原有事件应该保持相对顺序
  const originalEventsInAfter = eventsAfter.slice(1)
  for (let i = 0; i < Math.min(eventsBefore.length, originalEventsInAfter.length); i++) {
    if (eventsBefore[i].id !== originalEventsInAfter[i].id) {
      return false
    }
  }

  return true
}
