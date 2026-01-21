/**
 * 门禁事件类型工具函数属性测试
 * **Feature: access-event-full-integration, Property 5: Event Category Classification**
 * **Validates: Requirements 5.1, 5.2, 5.3, 5.4, 8.3**
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import {
  EventCategory,
  EventCategoryConfig,
  NormalEventTypes,
  AlarmEventTypes,
  AllEventTypes,
  ALARM_TYPE_CODES,
  getEventCategory,
  getEventTypeName,
  getCategoryStyle,
  getEventCategoryOptions,
  getEventTypeOptionsByCategory,
  isAlarmEvent,
  isAbnormalEvent,
  isNormalEvent,
  convertLegacyCode,
  LegacyCodeMapping
} from './accessEventTypes'

describe('accessEventTypes', () => {
  // ==================== 基础单元测试 ====================
  
  describe('getEventCategory', () => {
    it('should return ALARM for alarm event types', () => {
      expect(getEventCategory('DOOR_NOT_CLOSED')).toBe(EventCategory.ALARM)
      expect(getEventCategory('BREAK_IN')).toBe(EventCategory.ALARM)
      expect(getEventCategory('DURESS')).toBe(EventCategory.ALARM)
      expect(getEventCategory('TAMPER_ALARM')).toBe(EventCategory.ALARM)
      expect(getEventCategory('MALICIOUS_OPEN')).toBe(EventCategory.ALARM)
      expect(getEventCategory('REPEAT_ENTER')).toBe(EventCategory.ALARM)
      expect(getEventCategory('LOCAL_ALARM')).toBe(EventCategory.ALARM)
    })

    it('should return ABNORMAL when verifyResult is 0', () => {
      expect(getEventCategory('CARD_SWIPE', 0)).toBe(EventCategory.ABNORMAL)
      expect(getEventCategory('FINGERPRINT', 0)).toBe(EventCategory.ABNORMAL)
      expect(getEventCategory('FACE_RECOGNIZE', 0)).toBe(EventCategory.ABNORMAL)
    })

    it('should return NORMAL for normal event types with successful verification', () => {
      expect(getEventCategory('CARD_SWIPE', 1)).toBe(EventCategory.NORMAL)
      expect(getEventCategory('FINGERPRINT', 1)).toBe(EventCategory.NORMAL)
      expect(getEventCategory('FACE_RECOGNIZE', 1)).toBe(EventCategory.NORMAL)
      expect(getEventCategory('REMOTE_OPEN', 1)).toBe(EventCategory.NORMAL)
      expect(getEventCategory('BUTTON_OPEN', 1)).toBe(EventCategory.NORMAL)
    })

    it('should return NORMAL for normal event types without verifyResult', () => {
      expect(getEventCategory('CARD_SWIPE')).toBe(EventCategory.NORMAL)
      expect(getEventCategory('REMOTE_OPEN')).toBe(EventCategory.NORMAL)
    })

    it('should prioritize ALARM over ABNORMAL', () => {
      // Even with verifyResult=0, alarm types should return ALARM
      expect(getEventCategory('BREAK_IN', 0)).toBe(EventCategory.ALARM)
      expect(getEventCategory('DURESS', 0)).toBe(EventCategory.ALARM)
    })

    it('should return NORMAL for unknown event types', () => {
      expect(getEventCategory('UNKNOWN_TYPE')).toBe(EventCategory.NORMAL)
      expect(getEventCategory('')).toBe(EventCategory.NORMAL)
    })
  })

  describe('getEventTypeName', () => {
    it('should return correct name for normal events', () => {
      expect(getEventTypeName('CARD_SWIPE')).toBe('刷卡开门')
      expect(getEventTypeName('FINGERPRINT')).toBe('指纹开门')
      expect(getEventTypeName('FACE_RECOGNIZE')).toBe('人脸开门')
      expect(getEventTypeName('REMOTE_OPEN')).toBe('远程开门')
    })

    it('should return correct name for alarm events', () => {
      expect(getEventTypeName('DOOR_NOT_CLOSED')).toBe('门未关报警')
      expect(getEventTypeName('BREAK_IN')).toBe('闯入报警')
      expect(getEventTypeName('DURESS')).toBe('胁迫报警')
      expect(getEventTypeName('TAMPER_ALARM')).toBe('防拆报警')
    })

    it('should return unknown event string for undefined codes', () => {
      expect(getEventTypeName('UNKNOWN_TYPE')).toBe('未知事件(UNKNOWN_TYPE)')
      expect(getEventTypeName('')).toBe('未知事件()')
    })
  })

  describe('getCategoryStyle', () => {
    it('should return danger style for ALARM', () => {
      const style = getCategoryStyle(EventCategory.ALARM)
      expect(style.color).toBe('danger')
      expect(style.icon).toBe('Warning')
      expect(style.label).toBe('报警事件')
    })

    it('should return warning style for ABNORMAL', () => {
      const style = getCategoryStyle(EventCategory.ABNORMAL)
      expect(style.color).toBe('warning')
      expect(style.icon).toBe('CircleClose')
      expect(style.label).toBe('异常事件')
    })

    it('should return success style for NORMAL', () => {
      const style = getCategoryStyle(EventCategory.NORMAL)
      expect(style.color).toBe('success')
      expect(style.icon).toBe('CircleCheck')
      expect(style.label).toBe('正常事件')
    })
  })

  describe('getEventCategoryOptions', () => {
    it('should return all category options including empty option', () => {
      const options = getEventCategoryOptions()
      expect(options).toHaveLength(4)
      expect(options[0].value).toBe('')
      expect(options[0].label).toBe('全部')
    })
  })

  describe('getEventTypeOptionsByCategory', () => {
    it('should return all types when no category specified', () => {
      const options = getEventTypeOptionsByCategory()
      expect(options.length).toBe(AllEventTypes.length)
    })

    it('should return only alarm types for ALARM category', () => {
      const options = getEventTypeOptionsByCategory(EventCategory.ALARM)
      expect(options.length).toBe(AlarmEventTypes.length)
    })

    it('should return only normal types for NORMAL category', () => {
      const options = getEventTypeOptionsByCategory(EventCategory.NORMAL)
      expect(options.length).toBe(NormalEventTypes.length)
    })
  })

  describe('helper functions', () => {
    it('isAlarmEvent should correctly identify alarm events', () => {
      expect(isAlarmEvent('BREAK_IN')).toBe(true)
      expect(isAlarmEvent('DURESS')).toBe(true)
      expect(isAlarmEvent('CARD_SWIPE')).toBe(false)
      expect(isAlarmEvent('REMOTE_OPEN')).toBe(false)
    })

    it('isAbnormalEvent should correctly identify abnormal events', () => {
      expect(isAbnormalEvent(0)).toBe(true)
      expect(isAbnormalEvent(1)).toBe(false)
    })

    it('isNormalEvent should correctly identify normal events', () => {
      expect(isNormalEvent('CARD_SWIPE', 1)).toBe(true)
      expect(isNormalEvent('BREAK_IN', 1)).toBe(false)
      expect(isNormalEvent('CARD_SWIPE', 0)).toBe(false)
    })
  })

  describe('convertLegacyCode', () => {
    it('should convert legacy numeric codes to string codes', () => {
      expect(convertLegacyCode(1)).toBe('CARD_SWIPE')
      expect(convertLegacyCode(3)).toBe('FINGERPRINT')
      expect(convertLegacyCode(4)).toBe('FACE_RECOGNIZE')
      expect(convertLegacyCode(100)).toBe('BREAK_IN')
      expect(convertLegacyCode(104)).toBe('DURESS')
    })

    it('should return UNKNOWN for unmapped codes', () => {
      expect(convertLegacyCode(999)).toBe('UNKNOWN')
      expect(convertLegacyCode(-1)).toBe('UNKNOWN')
    })
  })

  // ==================== 属性测试 ====================

  /**
   * Property 5: Event Category Classification
   * *For any* event, the category SHALL be determined as: 
   * ALARM if eventType is in alarm types list, 
   * ABNORMAL if verifyResult=0, 
   * otherwise NORMAL.
   * 
   * **Feature: access-event-full-integration, Property 5: Event Category Classification**
   * **Validates: Requirements 5.1, 5.2, 5.3, 5.4**
   */
  describe('Property 5: Event Category Classification', () => {
    // 生成报警事件类型代码
    const alarmTypeArb = fc.constantFrom(...Array.from(ALARM_TYPE_CODES))
    
    // 生成正常事件类型代码
    const normalTypeCodes = NormalEventTypes.map(t => t.code)
    const normalTypeArb = fc.constantFrom(...normalTypeCodes)
    
    // 生成任意已定义的事件类型代码
    const allTypeCodes = AllEventTypes.map(t => t.code)
    const anyDefinedTypeArb = fc.constantFrom(...allTypeCodes)
    
    // 生成验证结果
    const verifyResultArb = fc.constantFrom(0, 1)

    it('should always return ALARM for alarm event types regardless of verifyResult', () => {
      fc.assert(
        fc.property(alarmTypeArb, verifyResultArb, (eventType, verifyResult) => {
          const category = getEventCategory(eventType, verifyResult)
          expect(category).toBe(EventCategory.ALARM)
        }),
        { numRuns: 100 }
      )
    })

    it('should return ABNORMAL for non-alarm types when verifyResult is 0', () => {
      fc.assert(
        fc.property(normalTypeArb, (eventType) => {
          const category = getEventCategory(eventType, 0)
          expect(category).toBe(EventCategory.ABNORMAL)
        }),
        { numRuns: 100 }
      )
    })

    it('should return NORMAL for non-alarm types when verifyResult is 1', () => {
      fc.assert(
        fc.property(normalTypeArb, (eventType) => {
          const category = getEventCategory(eventType, 1)
          expect(category).toBe(EventCategory.NORMAL)
        }),
        { numRuns: 100 }
      )
    })

    it('should always return a valid EventCategory for any defined type', () => {
      fc.assert(
        fc.property(anyDefinedTypeArb, fc.option(verifyResultArb), (eventType, verifyResult) => {
          const category = getEventCategory(eventType, verifyResult ?? undefined)
          expect([EventCategory.NORMAL, EventCategory.ALARM, EventCategory.ABNORMAL]).toContain(category)
        }),
        { numRuns: 100 }
      )
    })

    it('should be consistent - same inputs always return same category', () => {
      fc.assert(
        fc.property(anyDefinedTypeArb, verifyResultArb, (eventType, verifyResult) => {
          const category1 = getEventCategory(eventType, verifyResult)
          const category2 = getEventCategory(eventType, verifyResult)
          expect(category1).toBe(category2)
        }),
        { numRuns: 100 }
      )
    })
  })

  /**
   * Property: Event Type Name Mapping Completeness
   * *For any* defined event type code, getEventTypeName SHALL return a non-empty Chinese name.
   * 
   * **Validates: Requirements 8.3**
   */
  describe('Property: Event Type Name Mapping Completeness', () => {
    const allTypeCodes = AllEventTypes.map(t => t.code)
    const definedCodeArb = fc.constantFrom(...allTypeCodes)

    it('should return non-empty Chinese name for all defined event codes', () => {
      fc.assert(
        fc.property(definedCodeArb, (code) => {
          const name = getEventTypeName(code)
          // 名称不应该是 "未知事件(xxx)" 格式
          expect(name).not.toMatch(/^未知事件\(.+\)$/)
          // 名称应该非空
          expect(name.length).toBeGreaterThan(0)
        }),
        { numRuns: 100 }
      )
    })

    it('should return "未知事件(code)" for undefined codes', () => {
      const undefinedCodeArb = fc.string({ minLength: 1, maxLength: 20 }).filter(
        code => !allTypeCodes.includes(code)
      )

      fc.assert(
        fc.property(undefinedCodeArb, (code) => {
          const name = getEventTypeName(code)
          expect(name).toBe(`未知事件(${code})`)
        }),
        { numRuns: 50 }
      )
    })

    it('should have unique names for all defined event types', () => {
      const names = AllEventTypes.map(t => t.name)
      const uniqueNames = new Set(names)
      expect(uniqueNames.size).toBe(names.length)
    })
  })

  /**
   * Property: Category Style Mapping Consistency
   * *For any* event category, getCategoryStyle SHALL return consistent style config.
   * 
   * **Validates: Requirements 5.2, 5.3, 5.4**
   */
  describe('Property: Category Style Mapping Consistency', () => {
    const categoryArb = fc.constantFrom(EventCategory.ALARM, EventCategory.ABNORMAL, EventCategory.NORMAL)

    it('should return consistent style for each category', () => {
      fc.assert(
        fc.property(categoryArb, (category) => {
          const style = getCategoryStyle(category)
          
          // 验证返回的样式对象包含必要的属性
          expect(style).toHaveProperty('label')
          expect(style).toHaveProperty('color')
          expect(style).toHaveProperty('icon')
          
          // 验证属性值非空
          expect(style.label.length).toBeGreaterThan(0)
          expect(style.color.length).toBeGreaterThan(0)
          expect(style.icon.length).toBeGreaterThan(0)
        }),
        { numRuns: 100 }
      )
    })

    it('should return danger color for ALARM category', () => {
      fc.assert(
        fc.property(fc.constant(EventCategory.ALARM), (category) => {
          const style = getCategoryStyle(category)
          expect(style.color).toBe('danger')
          expect(style.icon).toBe('Warning')
        }),
        { numRuns: 10 }
      )
    })

    it('should return warning color for ABNORMAL category', () => {
      fc.assert(
        fc.property(fc.constant(EventCategory.ABNORMAL), (category) => {
          const style = getCategoryStyle(category)
          expect(style.color).toBe('warning')
          expect(style.icon).toBe('CircleClose')
        }),
        { numRuns: 10 }
      )
    })

    it('should return success color for NORMAL category', () => {
      fc.assert(
        fc.property(fc.constant(EventCategory.NORMAL), (category) => {
          const style = getCategoryStyle(category)
          expect(style.color).toBe('success')
          expect(style.icon).toBe('CircleCheck')
        }),
        { numRuns: 10 }
      )
    })

    it('should have matching config in EventCategoryConfig', () => {
      fc.assert(
        fc.property(categoryArb, (category) => {
          const style = getCategoryStyle(category)
          const config = EventCategoryConfig[category]
          
          expect(style).toEqual(config)
        }),
        { numRuns: 100 }
      )
    })
  })

  // ==================== 边界条件测试 ====================

  describe('Boundary conditions', () => {
    it('should handle empty string event type', () => {
      expect(getEventCategory('')).toBe(EventCategory.NORMAL)
      expect(getEventTypeName('')).toBe('未知事件()')
    })

    it('should handle undefined verifyResult', () => {
      expect(getEventCategory('CARD_SWIPE', undefined)).toBe(EventCategory.NORMAL)
      expect(getEventCategory('BREAK_IN', undefined)).toBe(EventCategory.ALARM)
    })

    it('should handle all alarm types in ALARM_TYPE_CODES set', () => {
      AlarmEventTypes.forEach(type => {
        expect(ALARM_TYPE_CODES.has(type.code)).toBe(true)
      })
    })
  })
})
