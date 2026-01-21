/**
 * 门禁事件统计服务属性测试
 * **Feature: access-event-full-integration, Property 7: Statistics Calculation Accuracy**
 * **Validates: Requirements 7.1, 7.2**
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import {
  EventStatistics,
  EventData,
  calculateStatistics,
  updateStatistics,
  createEmptyStatistics,
  mergeStatistics,
  validateStatistics,
  decrementStatistics,
  calculatePercentages
} from './accessEventStatistics'
import { EventCategory, getEventCategory, NormalEventTypes, AlarmEventTypes } from './accessEventTypes'

// ==================== 测试数据生成器 ====================

// 正常事件类型代码
const normalEventCodes = NormalEventTypes.map(t => t.code)
const normalEventTypeArb = fc.constantFrom(...normalEventCodes)

// 报警事件类型代码
const alarmEventCodes = AlarmEventTypes.map(t => t.code)
const alarmEventTypeArb = fc.constantFrom(...alarmEventCodes)

// 任意事件类型代码
const anyEventTypeArb = fc.oneof(normalEventTypeArb, alarmEventTypeArb)

// 验证结果（0-失败，1-成功）
const verifyResultArb = fc.constantFrom(0, 1, undefined)

// 生成事件对象
const eventArb: fc.Arbitrary<EventData> = fc.record({
  eventType: anyEventTypeArb,
  verifyResult: verifyResultArb
})

// 生成事件列表
const eventListArb = fc.array(eventArb, { minLength: 0, maxLength: 100 })

// 生成统计数据
const statsArb: fc.Arbitrary<EventStatistics> = fc.record({
  total: fc.integer({ min: 0, max: 1000 }),
  alarmCount: fc.integer({ min: 0, max: 1000 }),
  abnormalCount: fc.integer({ min: 0, max: 1000 }),
  normalCount: fc.integer({ min: 0, max: 1000 })
})

describe('accessEventStatistics', () => {
  // ==================== 基础单元测试 ====================

  describe('createEmptyStatistics', () => {
    it('should return statistics with all zeros', () => {
      const stats = createEmptyStatistics()
      expect(stats.total).toBe(0)
      expect(stats.alarmCount).toBe(0)
      expect(stats.abnormalCount).toBe(0)
      expect(stats.normalCount).toBe(0)
    })
  })

  describe('calculateStatistics', () => {
    it('should return empty statistics for empty array', () => {
      const stats = calculateStatistics([])
      expect(stats.total).toBe(0)
      expect(stats.alarmCount).toBe(0)
      expect(stats.abnormalCount).toBe(0)
      expect(stats.normalCount).toBe(0)
    })

    it('should count normal events correctly', () => {
      const events: EventData[] = [
        { eventType: 'CARD_SWIPE', verifyResult: 1 },
        { eventType: 'FACE_RECOGNIZE', verifyResult: 1 },
        { eventType: 'REMOTE_OPEN', verifyResult: 1 }
      ]
      const stats = calculateStatistics(events)
      expect(stats.total).toBe(3)
      expect(stats.normalCount).toBe(3)
      expect(stats.alarmCount).toBe(0)
      expect(stats.abnormalCount).toBe(0)
    })

    it('should count alarm events correctly', () => {
      const events: EventData[] = [
        { eventType: 'BREAK_IN' },
        { eventType: 'DURESS' },
        { eventType: 'TAMPER_ALARM' }
      ]
      const stats = calculateStatistics(events)
      expect(stats.total).toBe(3)
      expect(stats.alarmCount).toBe(3)
      expect(stats.normalCount).toBe(0)
      expect(stats.abnormalCount).toBe(0)
    })

    it('should count abnormal events correctly (verifyResult=0)', () => {
      const events: EventData[] = [
        { eventType: 'CARD_SWIPE', verifyResult: 0 },
        { eventType: 'FINGERPRINT', verifyResult: 0 },
        { eventType: 'FACE_RECOGNIZE', verifyResult: 0 }
      ]
      const stats = calculateStatistics(events)
      expect(stats.total).toBe(3)
      expect(stats.abnormalCount).toBe(3)
      expect(stats.normalCount).toBe(0)
      expect(stats.alarmCount).toBe(0)
    })

    it('should count mixed events correctly', () => {
      const events: EventData[] = [
        { eventType: 'CARD_SWIPE', verifyResult: 1 },    // normal
        { eventType: 'BREAK_IN' },                        // alarm
        { eventType: 'FINGERPRINT', verifyResult: 0 },   // abnormal
        { eventType: 'FACE_RECOGNIZE', verifyResult: 1 }, // normal
        { eventType: 'DURESS' }                           // alarm
      ]
      const stats = calculateStatistics(events)
      expect(stats.total).toBe(5)
      expect(stats.normalCount).toBe(2)
      expect(stats.alarmCount).toBe(2)
      expect(stats.abnormalCount).toBe(1)
    })

    it('should prioritize alarm over abnormal for alarm event types', () => {
      // 报警事件即使验证失败也应该归类为报警
      const events: EventData[] = [
        { eventType: 'BREAK_IN', verifyResult: 0 }
      ]
      const stats = calculateStatistics(events)
      expect(stats.alarmCount).toBe(1)
      expect(stats.abnormalCount).toBe(0)
    })
  })

  describe('updateStatistics', () => {
    it('should increment total and correct category count', () => {
      const initial = createEmptyStatistics()
      
      // 添加正常事件
      const afterNormal = updateStatistics(initial, { eventType: 'CARD_SWIPE', verifyResult: 1 })
      expect(afterNormal.total).toBe(1)
      expect(afterNormal.normalCount).toBe(1)
      
      // 添加报警事件
      const afterAlarm = updateStatistics(afterNormal, { eventType: 'BREAK_IN' })
      expect(afterAlarm.total).toBe(2)
      expect(afterAlarm.alarmCount).toBe(1)
      
      // 添加异常事件
      const afterAbnormal = updateStatistics(afterAlarm, { eventType: 'FINGERPRINT', verifyResult: 0 })
      expect(afterAbnormal.total).toBe(3)
      expect(afterAbnormal.abnormalCount).toBe(1)
    })

    it('should not mutate original statistics', () => {
      const initial = createEmptyStatistics()
      const updated = updateStatistics(initial, { eventType: 'CARD_SWIPE', verifyResult: 1 })
      
      expect(initial.total).toBe(0)
      expect(updated.total).toBe(1)
    })
  })

  describe('mergeStatistics', () => {
    it('should merge two statistics correctly', () => {
      const stats1: EventStatistics = {
        total: 10,
        alarmCount: 2,
        abnormalCount: 3,
        normalCount: 5
      }
      const stats2: EventStatistics = {
        total: 5,
        alarmCount: 1,
        abnormalCount: 1,
        normalCount: 3
      }
      
      const merged = mergeStatistics(stats1, stats2)
      expect(merged.total).toBe(15)
      expect(merged.alarmCount).toBe(3)
      expect(merged.abnormalCount).toBe(4)
      expect(merged.normalCount).toBe(8)
    })

    it('should handle merging with empty statistics', () => {
      const stats: EventStatistics = {
        total: 10,
        alarmCount: 2,
        abnormalCount: 3,
        normalCount: 5
      }
      const empty = createEmptyStatistics()
      
      const merged = mergeStatistics(stats, empty)
      expect(merged).toEqual(stats)
    })
  })

  describe('validateStatistics', () => {
    it('should return true for valid statistics', () => {
      const stats: EventStatistics = {
        total: 10,
        alarmCount: 2,
        abnormalCount: 3,
        normalCount: 5
      }
      expect(validateStatistics(stats)).toBe(true)
    })

    it('should return false for invalid statistics', () => {
      const stats: EventStatistics = {
        total: 10,
        alarmCount: 2,
        abnormalCount: 3,
        normalCount: 6 // 2+3+6=11 != 10
      }
      expect(validateStatistics(stats)).toBe(false)
    })
  })

  describe('decrementStatistics', () => {
    it('should decrement correct category', () => {
      const stats: EventStatistics = {
        total: 10,
        alarmCount: 2,
        abnormalCount: 3,
        normalCount: 5
      }
      
      const afterDecrement = decrementStatistics(stats, { eventType: 'CARD_SWIPE', verifyResult: 1 })
      expect(afterDecrement.total).toBe(9)
      expect(afterDecrement.normalCount).toBe(4)
    })

    it('should not go below zero', () => {
      const stats = createEmptyStatistics()
      const afterDecrement = decrementStatistics(stats, { eventType: 'CARD_SWIPE', verifyResult: 1 })
      expect(afterDecrement.total).toBe(0)
      expect(afterDecrement.normalCount).toBe(0)
    })
  })

  describe('calculatePercentages', () => {
    it('should calculate percentages correctly', () => {
      const stats: EventStatistics = {
        total: 100,
        alarmCount: 20,
        abnormalCount: 30,
        normalCount: 50
      }
      const percentages = calculatePercentages(stats)
      expect(percentages.alarmPercent).toBe(20)
      expect(percentages.abnormalPercent).toBe(30)
      expect(percentages.normalPercent).toBe(50)
    })

    it('should return zeros for empty statistics', () => {
      const stats = createEmptyStatistics()
      const percentages = calculatePercentages(stats)
      expect(percentages.alarmPercent).toBe(0)
      expect(percentages.abnormalPercent).toBe(0)
      expect(percentages.normalPercent).toBe(0)
    })
  })


  // ==================== 属性测试 ====================

  /**
   * Property 7: Statistics Calculation Accuracy
   * *For any* list of events, the calculateStatistics function SHALL return statistics where:
   * - total = alarmCount + abnormalCount + normalCount
   * - Each event is counted exactly once in its correct category
   * **Feature: access-event-full-integration, Property 7: Statistics Calculation Accuracy**
   * **Validates: Requirements 7.1, 7.2**
   */
  describe('Property 7: Statistics Calculation Accuracy', () => {
    it('should have total equal to sum of category counts', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          const stats = calculateStatistics(events)
          expect(stats.total).toBe(stats.alarmCount + stats.abnormalCount + stats.normalCount)
        }),
        { numRuns: 100 }
      )
    })

    it('should have total equal to event list length', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          const stats = calculateStatistics(events)
          expect(stats.total).toBe(events.length)
        }),
        { numRuns: 100 }
      )
    })

    it('should count each event exactly once', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          const stats = calculateStatistics(events)
          
          // 手动计算每个类别的数量
          let expectedNormal = 0
          let expectedAlarm = 0
          let expectedAbnormal = 0
          
          for (const event of events) {
            const category = getEventCategory(event.eventType, event.verifyResult)
            switch (category) {
              case EventCategory.NORMAL:
                expectedNormal++
                break
              case EventCategory.ALARM:
                expectedAlarm++
                break
              case EventCategory.ABNORMAL:
                expectedAbnormal++
                break
            }
          }
          
          expect(stats.normalCount).toBe(expectedNormal)
          expect(stats.alarmCount).toBe(expectedAlarm)
          expect(stats.abnormalCount).toBe(expectedAbnormal)
        }),
        { numRuns: 100 }
      )
    })

    it('should return non-negative counts', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          const stats = calculateStatistics(events)
          expect(stats.total).toBeGreaterThanOrEqual(0)
          expect(stats.alarmCount).toBeGreaterThanOrEqual(0)
          expect(stats.abnormalCount).toBeGreaterThanOrEqual(0)
          expect(stats.normalCount).toBeGreaterThanOrEqual(0)
        }),
        { numRuns: 100 }
      )
    })

    it('should be consistent - same events always produce same statistics', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          const stats1 = calculateStatistics(events)
          const stats2 = calculateStatistics(events)
          expect(stats1).toEqual(stats2)
        }),
        { numRuns: 100 }
      )
    })

    it('should always produce valid statistics', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          const stats = calculateStatistics(events)
          expect(validateStatistics(stats)).toBe(true)
        }),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Property: Incremental Update Consistency
   * *For any* sequence of events, updating statistics one by one should produce
   * the same result as calculating statistics for the entire list.
   * **Feature: access-event-full-integration, Property 7 (extended): Incremental Update Consistency**
   * **Validates: Requirements 7.2**
   */
  describe('Property: Incremental Update Consistency', () => {
    it('should produce same result as batch calculation', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          // 批量计算
          const batchStats = calculateStatistics(events)
          
          // 增量更新
          let incrementalStats = createEmptyStatistics()
          for (const event of events) {
            incrementalStats = updateStatistics(incrementalStats, event)
          }
          
          expect(incrementalStats).toEqual(batchStats)
        }),
        { numRuns: 100 }
      )
    })

    it('should be order-independent for final result', () => {
      fc.assert(
        fc.property(eventListArb, (events) => {
          // 正序计算
          const forwardStats = calculateStatistics(events)
          
          // 逆序计算
          const reversedEvents = [...events].reverse()
          const reverseStats = calculateStatistics(reversedEvents)
          
          expect(forwardStats).toEqual(reverseStats)
        }),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Property: Merge Statistics Associativity
   * *For any* three statistics objects, merging should be associative:
   * merge(merge(a, b), c) === merge(a, merge(b, c))
   * **Feature: access-event-full-integration, Property 7 (extended): Merge Associativity**
   * **Validates: Requirements 7.2**
   */
  describe('Property: Merge Statistics Associativity', () => {
    it('should be associative', () => {
      fc.assert(
        fc.property(statsArb, statsArb, statsArb, (a, b, c) => {
          const leftAssoc = mergeStatistics(mergeStatistics(a, b), c)
          const rightAssoc = mergeStatistics(a, mergeStatistics(b, c))
          expect(leftAssoc).toEqual(rightAssoc)
        }),
        { numRuns: 100 }
      )
    })

    it('should be commutative', () => {
      fc.assert(
        fc.property(statsArb, statsArb, (a, b) => {
          const ab = mergeStatistics(a, b)
          const ba = mergeStatistics(b, a)
          expect(ab).toEqual(ba)
        }),
        { numRuns: 100 }
      )
    })

    it('should have identity element (empty statistics)', () => {
      fc.assert(
        fc.property(statsArb, (stats) => {
          const empty = createEmptyStatistics()
          const merged = mergeStatistics(stats, empty)
          expect(merged).toEqual(stats)
        }),
        { numRuns: 100 }
      )
    })
  })
})
