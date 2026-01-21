/**
 * 门禁事件实时插入属性测试
 * **Feature: access-event-full-integration, Property 6: Real-time Event Insertion Order**
 * **Validates: Requirements 5.5**
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import {
  EventLogItem,
  RealtimeEventManager,
  createRealtimeEventManager,
  insertRealtimeEvent,
  insertRealtimeEventsBatch,
  mergeEventLists,
  getEventIndex,
  removeHighlight,
  clearRealtimeEvents,
  validateInsertionOrder
} from './accessEventInsertion'
import { NormalEventTypes, AlarmEventTypes } from './accessEventTypes'

// ==================== 测试数据生成器 ====================

// 生成唯一ID
let idCounter = 0
const generateUniqueId = () => ++idCounter

// 事件类型代码
const normalEventCodes = NormalEventTypes.map(t => t.code)
const alarmEventCodes = AlarmEventTypes.map(t => t.code)
const allEventCodes = [...normalEventCodes, ...alarmEventCodes]
const eventTypeArb = fc.constantFrom(...allEventCodes)

// 验证结果
const verifyResultArb = fc.constantFrom(0, 1, undefined)

// 生成事件日志项
const eventLogItemArb: fc.Arbitrary<EventLogItem> = fc.record({
  id: fc.integer({ min: 1, max: 1000000 }),
  eventType: eventTypeArb,
  verifyResult: verifyResultArb,
  eventTime: fc.date().map(d => d.toISOString()),
  deviceName: fc.option(fc.string({ minLength: 1, maxLength: 20 })),
  channelName: fc.option(fc.string({ minLength: 1, maxLength: 20 })),
  personName: fc.option(fc.string({ minLength: 1, maxLength: 10 })),
  isRealtime: fc.boolean(),
  _highlight: fc.boolean()
})

// 生成具有唯一ID的事件列表
const uniqueEventListArb = (minLength: number = 0, maxLength: number = 20): fc.Arbitrary<EventLogItem[]> => {
  return fc.array(eventLogItemArb, { minLength, maxLength }).map(events => {
    // 确保ID唯一
    return events.map((event, index) => ({
      ...event,
      id: index + 1
    }))
  })
}

// 生成事件管理器
const managerArb = (maxEvents: number = 50): fc.Arbitrary<RealtimeEventManager> => {
  return uniqueEventListArb(0, Math.min(maxEvents, 30)).map(events => ({
    events,
    maxEvents
  }))
}

describe('accessEventInsertion', () => {
  // 重置ID计数器
  beforeEach(() => {
    idCounter = 0
  })

  // ==================== 基础单元测试 ====================

  describe('createRealtimeEventManager', () => {
    it('should create empty manager with default maxEvents', () => {
      const manager = createRealtimeEventManager()
      expect(manager.events).toEqual([])
      expect(manager.maxEvents).toBe(50)
    })

    it('should create empty manager with custom maxEvents', () => {
      const manager = createRealtimeEventManager(100)
      expect(manager.events).toEqual([])
      expect(manager.maxEvents).toBe(100)
    })
  })

  describe('insertRealtimeEvent', () => {
    it('should insert event at index 0', () => {
      const manager = createRealtimeEventManager()
      const event: EventLogItem = {
        id: 1,
        eventType: 'CARD_SWIPE',
        verifyResult: 1,
        eventTime: new Date().toISOString()
      }

      const updated = insertRealtimeEvent(manager, event)
      expect(updated.events.length).toBe(1)
      expect(updated.events[0].id).toBe(1)
    })

    it('should insert new event before existing events', () => {
      let manager = createRealtimeEventManager()
      
      // 插入第一个事件
      const event1: EventLogItem = { id: 1, eventType: 'CARD_SWIPE', eventTime: '2024-01-01T10:00:00Z' }
      manager = insertRealtimeEvent(manager, event1)
      
      // 插入第二个事件
      const event2: EventLogItem = { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '2024-01-01T10:01:00Z' }
      manager = insertRealtimeEvent(manager, event2)

      expect(manager.events.length).toBe(2)
      expect(manager.events[0].id).toBe(2) // 新事件在顶部
      expect(manager.events[1].id).toBe(1) // 旧事件在后面
    })

    it('should mark event as realtime and highlight', () => {
      const manager = createRealtimeEventManager()
      const event: EventLogItem = {
        id: 1,
        eventType: 'CARD_SWIPE',
        eventTime: new Date().toISOString(),
        isRealtime: false,
        _highlight: false
      }

      const updated = insertRealtimeEvent(manager, event)
      expect(updated.events[0].isRealtime).toBe(true)
      expect(updated.events[0]._highlight).toBe(true)
    })

    it('should limit events to maxEvents', () => {
      let manager = createRealtimeEventManager(3)
      
      for (let i = 1; i <= 5; i++) {
        const event: EventLogItem = { id: i, eventType: 'CARD_SWIPE', eventTime: new Date().toISOString() }
        manager = insertRealtimeEvent(manager, event)
      }

      expect(manager.events.length).toBe(3)
      // 最新的3个事件应该保留（id: 5, 4, 3）
      expect(manager.events[0].id).toBe(5)
      expect(manager.events[1].id).toBe(4)
      expect(manager.events[2].id).toBe(3)
    })

    it('should not mutate original manager', () => {
      const manager = createRealtimeEventManager()
      const event: EventLogItem = { id: 1, eventType: 'CARD_SWIPE', eventTime: new Date().toISOString() }

      const updated = insertRealtimeEvent(manager, event)
      
      expect(manager.events.length).toBe(0)
      expect(updated.events.length).toBe(1)
    })
  })

  describe('insertRealtimeEventsBatch', () => {
    it('should insert events in order, last event at top', () => {
      const manager = createRealtimeEventManager()
      const events: EventLogItem[] = [
        { id: 1, eventType: 'CARD_SWIPE', eventTime: '2024-01-01T10:00:00Z' },
        { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '2024-01-01T10:01:00Z' },
        { id: 3, eventType: 'FINGERPRINT', eventTime: '2024-01-01T10:02:00Z' }
      ]

      const updated = insertRealtimeEventsBatch(manager, events)
      
      expect(updated.events.length).toBe(3)
      expect(updated.events[0].id).toBe(3) // 最后插入的在顶部
      expect(updated.events[1].id).toBe(2)
      expect(updated.events[2].id).toBe(1)
    })
  })

  describe('mergeEventLists', () => {
    it('should put realtime events before history events', () => {
      const realtimeEvents: EventLogItem[] = [
        { id: 3, eventType: 'CARD_SWIPE', eventTime: '2024-01-01T10:02:00Z', isRealtime: true },
        { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '2024-01-01T10:01:00Z', isRealtime: true }
      ]
      const historyEvents: EventLogItem[] = [
        { id: 1, eventType: 'FINGERPRINT', eventTime: '2024-01-01T10:00:00Z', isRealtime: false }
      ]

      const merged = mergeEventLists(realtimeEvents, historyEvents)
      
      expect(merged.length).toBe(3)
      expect(merged[0].id).toBe(3)
      expect(merged[1].id).toBe(2)
      expect(merged[2].id).toBe(1)
    })
  })

  describe('getEventIndex', () => {
    it('should return correct index', () => {
      const events: EventLogItem[] = [
        { id: 3, eventType: 'CARD_SWIPE', eventTime: '' },
        { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '' },
        { id: 1, eventType: 'FINGERPRINT', eventTime: '' }
      ]

      expect(getEventIndex(events, 3)).toBe(0)
      expect(getEventIndex(events, 2)).toBe(1)
      expect(getEventIndex(events, 1)).toBe(2)
      expect(getEventIndex(events, 999)).toBe(-1)
    })
  })

  describe('removeHighlight', () => {
    it('should remove highlight from specific event', () => {
      const manager: RealtimeEventManager = {
        events: [
          { id: 1, eventType: 'CARD_SWIPE', eventTime: '', _highlight: true },
          { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '', _highlight: true }
        ],
        maxEvents: 50
      }

      const updated = removeHighlight(manager, 1)
      
      expect(updated.events[0]._highlight).toBe(false)
      expect(updated.events[1]._highlight).toBe(true)
    })
  })

  describe('clearRealtimeEvents', () => {
    it('should clear all events', () => {
      const manager: RealtimeEventManager = {
        events: [
          { id: 1, eventType: 'CARD_SWIPE', eventTime: '' },
          { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '' }
        ],
        maxEvents: 50
      }

      const cleared = clearRealtimeEvents(manager)
      
      expect(cleared.events.length).toBe(0)
      expect(cleared.maxEvents).toBe(50)
    })
  })

  describe('validateInsertionOrder', () => {
    it('should return true for correct insertion', () => {
      const before: EventLogItem[] = [
        { id: 1, eventType: 'CARD_SWIPE', eventTime: '' }
      ]
      const newEvent: EventLogItem = { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '' }
      const after: EventLogItem[] = [
        { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '' },
        { id: 1, eventType: 'CARD_SWIPE', eventTime: '' }
      ]

      expect(validateInsertionOrder(before, after, newEvent)).toBe(true)
    })

    it('should return false for incorrect insertion', () => {
      const before: EventLogItem[] = [
        { id: 1, eventType: 'CARD_SWIPE', eventTime: '' }
      ]
      const newEvent: EventLogItem = { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '' }
      const after: EventLogItem[] = [
        { id: 1, eventType: 'CARD_SWIPE', eventTime: '' }, // 新事件不在顶部
        { id: 2, eventType: 'FACE_RECOGNIZE', eventTime: '' }
      ]

      expect(validateInsertionOrder(before, after, newEvent)).toBe(false)
    })
  })

  // ==================== 属性测试 ====================

  /**
   * Property 6: Real-time Event Insertion Order
   * *For any* sequence of real-time events pushed via WebSocket, 
   * new events SHALL be displayed at the top of the event list (index 0).
   * 
   * **Feature: access-event-full-integration, Property 6: Real-time Event Insertion Order**
   * **Validates: Requirements 5.5**
   */
  describe('Property 6: Real-time Event Insertion Order', () => {
    it('should always insert new event at index 0', () => {
      fc.assert(
        fc.property(
          managerArb(50),
          eventLogItemArb.map(e => ({ ...e, id: 999999 })), // 使用唯一ID
          (manager, newEvent) => {
            const updated = insertRealtimeEvent(manager, newEvent)
            
            // 新事件应该在索引0
            expect(updated.events[0].id).toBe(newEvent.id)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should preserve relative order of existing events', () => {
      fc.assert(
        fc.property(
          managerArb(50),
          eventLogItemArb.map(e => ({ ...e, id: 999999 })),
          (manager, newEvent) => {
            const updated = insertRealtimeEvent(manager, newEvent)
            
            // 原有事件应该保持相对顺序（在新事件之后）
            const originalIds = manager.events.map(e => e.id)
            const updatedIdsAfterNew = updated.events.slice(1).map(e => e.id)
            
            // 检查原有事件的相对顺序是否保持
            for (let i = 0; i < Math.min(originalIds.length, updatedIdsAfterNew.length); i++) {
              expect(updatedIdsAfterNew[i]).toBe(originalIds[i])
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should increase list length by 1 (unless at max)', () => {
      fc.assert(
        fc.property(
          managerArb(50),
          eventLogItemArb.map(e => ({ ...e, id: 999999 })),
          (manager, newEvent) => {
            const updated = insertRealtimeEvent(manager, newEvent)
            
            if (manager.events.length < manager.maxEvents) {
              // 未达到最大值时，长度增加1
              expect(updated.events.length).toBe(manager.events.length + 1)
            } else {
              // 达到最大值时，长度保持不变
              expect(updated.events.length).toBe(manager.maxEvents)
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should never exceed maxEvents', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 1, max: 100 }),
          fc.array(eventLogItemArb, { minLength: 1, maxLength: 200 }),
          (maxEvents, events) => {
            let manager = createRealtimeEventManager(maxEvents)
            
            // 插入所有事件
            for (let i = 0; i < events.length; i++) {
              const event = { ...events[i], id: i + 1 }
              manager = insertRealtimeEvent(manager, event)
            }
            
            // 列表长度不应超过maxEvents
            expect(manager.events.length).toBeLessThanOrEqual(maxEvents)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should mark all inserted events as realtime', () => {
      fc.assert(
        fc.property(
          eventLogItemArb,
          (event) => {
            const manager = createRealtimeEventManager()
            const updated = insertRealtimeEvent(manager, event)
            
            expect(updated.events[0].isRealtime).toBe(true)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should mark all inserted events with highlight', () => {
      fc.assert(
        fc.property(
          eventLogItemArb,
          (event) => {
            const manager = createRealtimeEventManager()
            const updated = insertRealtimeEvent(manager, event)
            
            expect(updated.events[0]._highlight).toBe(true)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should be consistent - same event always inserted at same position', () => {
      fc.assert(
        fc.property(
          managerArb(50),
          eventLogItemArb.map(e => ({ ...e, id: 999999 })),
          (manager, newEvent) => {
            const updated1 = insertRealtimeEvent(manager, newEvent)
            const updated2 = insertRealtimeEvent(manager, newEvent)
            
            // 两次插入应该产生相同的结果
            expect(updated1.events[0].id).toBe(updated2.events[0].id)
            expect(updated1.events.length).toBe(updated2.events.length)
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Property: Batch Insertion Order
   * *For any* sequence of events inserted in batch, the last event in the input
   * should be at index 0 in the result.
   */
  describe('Property: Batch Insertion Order', () => {
    it('should have last input event at index 0', () => {
      fc.assert(
        fc.property(
          fc.array(eventLogItemArb, { minLength: 1, maxLength: 20 }).map(events => 
            events.map((e, i) => ({ ...e, id: i + 1 }))
          ),
          (events) => {
            const manager = createRealtimeEventManager(100)
            const updated = insertRealtimeEventsBatch(manager, events)
            
            // 最后一个输入事件应该在顶部
            const lastEvent = events[events.length - 1]
            expect(updated.events[0].id).toBe(lastEvent.id)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should reverse the order of input events', () => {
      fc.assert(
        fc.property(
          fc.array(eventLogItemArb, { minLength: 1, maxLength: 20 }).map(events => 
            events.map((e, i) => ({ ...e, id: i + 1 }))
          ),
          (events) => {
            const manager = createRealtimeEventManager(100)
            const updated = insertRealtimeEventsBatch(manager, events)
            
            // 输出顺序应该是输入顺序的逆序
            const expectedOrder = [...events].reverse().map(e => e.id)
            const actualOrder = updated.events.map(e => e.id)
            
            expect(actualOrder).toEqual(expectedOrder)
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Property: Merge Lists Order
   * *For any* realtime and history event lists, merged list should have
   * all realtime events before all history events.
   */
  describe('Property: Merge Lists Order', () => {
    it('should have all realtime events before history events', () => {
      fc.assert(
        fc.property(
          uniqueEventListArb(0, 10).map(events => 
            events.map(e => ({ ...e, isRealtime: true }))
          ),
          uniqueEventListArb(0, 10).map(events => 
            events.map((e, i) => ({ ...e, id: e.id + 1000, isRealtime: false }))
          ),
          (realtimeEvents, historyEvents) => {
            const merged = mergeEventLists(realtimeEvents, historyEvents)
            
            // 找到第一个历史事件的位置
            const firstHistoryIndex = merged.findIndex(e => e.isRealtime === false)
            
            if (firstHistoryIndex === -1) {
              // 没有历史事件，所有都是实时事件
              expect(merged.every(e => e.isRealtime === true)).toBe(true)
            } else {
              // 第一个历史事件之前的所有事件都应该是实时事件
              for (let i = 0; i < firstHistoryIndex; i++) {
                expect(merged[i].isRealtime).toBe(true)
              }
              // 第一个历史事件之后的所有事件都应该是历史事件
              for (let i = firstHistoryIndex; i < merged.length; i++) {
                expect(merged[i].isRealtime).toBe(false)
              }
            }
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should preserve order within each list', () => {
      fc.assert(
        fc.property(
          uniqueEventListArb(0, 10),
          uniqueEventListArb(0, 10).map(events => 
            events.map((e, i) => ({ ...e, id: e.id + 1000 }))
          ),
          (realtimeEvents, historyEvents) => {
            const merged = mergeEventLists(realtimeEvents, historyEvents)
            
            // 实时事件的相对顺序应该保持
            const realtimeIds = realtimeEvents.map(e => e.id)
            const mergedRealtimeIds = merged.slice(0, realtimeEvents.length).map(e => e.id)
            expect(mergedRealtimeIds).toEqual(realtimeIds)
            
            // 历史事件的相对顺序应该保持
            const historyIds = historyEvents.map(e => e.id)
            const mergedHistoryIds = merged.slice(realtimeEvents.length).map(e => e.id)
            expect(mergedHistoryIds).toEqual(historyIds)
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Property: Immutability
   * *For any* operation on the event manager, the original manager should not be mutated.
   */
  describe('Property: Immutability', () => {
    it('should not mutate original manager on insert', () => {
      fc.assert(
        fc.property(
          managerArb(50),
          eventLogItemArb,
          (manager, event) => {
            const originalLength = manager.events.length
            const originalEvents = [...manager.events]
            
            insertRealtimeEvent(manager, event)
            
            // 原始管理器不应该被修改
            expect(manager.events.length).toBe(originalLength)
            expect(manager.events).toEqual(originalEvents)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not mutate original manager on clear', () => {
      fc.assert(
        fc.property(
          managerArb(50),
          (manager) => {
            const originalLength = manager.events.length
            const originalEvents = [...manager.events]
            
            clearRealtimeEvents(manager)
            
            // 原始管理器不应该被修改
            expect(manager.events.length).toBe(originalLength)
            expect(manager.events).toEqual(originalEvents)
          }
        ),
        { numRuns: 100 }
      )
    })

    it('should not mutate original manager on removeHighlight', () => {
      fc.assert(
        fc.property(
          managerArb(50).filter(m => m.events.length > 0),
          (manager) => {
            const originalHighlights = manager.events.map(e => e._highlight)
            const eventId = manager.events[0].id
            
            removeHighlight(manager, eventId)
            
            // 原始管理器不应该被修改
            const currentHighlights = manager.events.map(e => e._highlight)
            expect(currentHighlights).toEqual(originalHighlights)
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
