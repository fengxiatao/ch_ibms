/**
 * 认证方式工具函数属性测试
 * **Feature: permission-group-ui-redesign, Property 1: 认证方式序列化round-trip**
 * **Validates: Requirements 2.3, 2.4, 5.1, 5.2**
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import {
  serializeAuthModes,
  deserializeAuthModes,
  getAuthModeLabel,
  getAuthModeLabels,
  getAuthModeLabelsFromString,
  AuthMode,
  AuthModeLabels,
  ValidAuthModes
} from './authModeUtils'

describe('authModeUtils', () => {
  describe('serializeAuthModes', () => {
    it('should return empty string for empty array', () => {
      expect(serializeAuthModes([])).toBe('')
    })

    it('should return empty string for null/undefined', () => {
      expect(serializeAuthModes(null as any)).toBe('')
      expect(serializeAuthModes(undefined as any)).toBe('')
    })

    it('should serialize single auth mode', () => {
      expect(serializeAuthModes(['CARD'])).toBe('CARD')
    })

    it('should serialize multiple auth modes sorted', () => {
      expect(serializeAuthModes(['FACE', 'CARD'])).toBe('CARD,FACE')
    })

    it('should filter invalid auth modes', () => {
      expect(serializeAuthModes(['CARD', 'INVALID', 'FACE'])).toBe('CARD,FACE')
    })

    it('should remove duplicates', () => {
      expect(serializeAuthModes(['CARD', 'CARD', 'FACE'])).toBe('CARD,FACE')
    })
  })

  describe('deserializeAuthModes', () => {
    it('should return empty array for empty string', () => {
      expect(deserializeAuthModes('')).toEqual([])
    })

    it('should return empty array for whitespace string', () => {
      expect(deserializeAuthModes('   ')).toEqual([])
    })

    it('should return empty array for null/undefined', () => {
      expect(deserializeAuthModes(null as any)).toEqual([])
      expect(deserializeAuthModes(undefined as any)).toEqual([])
    })

    it('should deserialize single auth mode', () => {
      expect(deserializeAuthModes('CARD')).toEqual(['CARD'])
    })

    it('should deserialize multiple auth modes sorted', () => {
      expect(deserializeAuthModes('FACE,CARD')).toEqual(['CARD', 'FACE'])
    })

    it('should handle whitespace around values', () => {
      expect(deserializeAuthModes(' CARD , FACE ')).toEqual(['CARD', 'FACE'])
    })

    it('should filter invalid auth modes', () => {
      expect(deserializeAuthModes('CARD,INVALID,FACE')).toEqual(['CARD', 'FACE'])
    })
  })

  describe('getAuthModeLabel', () => {
    it('should return Chinese label for valid auth mode', () => {
      expect(getAuthModeLabel('CARD')).toBe('卡')
      expect(getAuthModeLabel('FACE')).toBe('人脸')
      expect(getAuthModeLabel('FINGERPRINT')).toBe('指纹')
      expect(getAuthModeLabel('PASSWORD')).toBe('密码')
    })

    it('should return original code for unknown auth mode', () => {
      expect(getAuthModeLabel('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getAuthModeLabels', () => {
    it('should return empty string for empty array', () => {
      expect(getAuthModeLabels([])).toBe('')
    })

    it('should return comma-separated Chinese labels', () => {
      expect(getAuthModeLabels(['CARD', 'FACE'])).toBe('卡,人脸')
    })
  })

  describe('getAuthModeLabelsFromString', () => {
    it('should convert string to Chinese labels', () => {
      expect(getAuthModeLabelsFromString('CARD,FACE')).toBe('卡,人脸')
    })
  })

  /**
   * Property-Based Tests
   * **Feature: permission-group-ui-redesign, Property 1: 认证方式序列化round-trip**
   * **Validates: Requirements 2.3, 2.4, 5.1, 5.2**
   */
  describe('Property: Round-trip consistency', () => {
    // 生成有效认证方式的任意子集
    const authModeSubsetArb = fc.subarray(ValidAuthModes, { minLength: 0 })

    it('should preserve auth modes after serialize then deserialize (round-trip)', () => {
      fc.assert(
        fc.property(authModeSubsetArb, (modes) => {
          const serialized = serializeAuthModes(modes)
          const deserialized = deserializeAuthModes(serialized)
          
          // 排序后比较，因为序列化会排序
          const sortedOriginal = [...modes].sort()
          expect(deserialized).toEqual(sortedOriginal)
        }),
        { numRuns: 100 }
      )
    })

    it('should preserve auth modes after deserialize then serialize (round-trip)', () => {
      // 生成有效的逗号分隔字符串
      const validStringArb = authModeSubsetArb.map((modes) => modes.join(','))

      fc.assert(
        fc.property(validStringArb, (str) => {
          const deserialized = deserializeAuthModes(str)
          const serialized = serializeAuthModes(deserialized)
          
          // 再次反序列化应该得到相同结果
          const deserializedAgain = deserializeAuthModes(serialized)
          expect(deserializedAgain).toEqual(deserialized)
        }),
        { numRuns: 100 }
      )
    })

    it('should handle any combination of valid auth modes', () => {
      fc.assert(
        fc.property(authModeSubsetArb, (modes) => {
          const serialized = serializeAuthModes(modes)
          
          // 序列化结果应该只包含有效的认证方式
          if (serialized !== '') {
            const parts = serialized.split(',')
            parts.forEach((part) => {
              expect(ValidAuthModes).toContain(part)
            })
          }
        }),
        { numRuns: 100 }
      )
    })

    it('should produce sorted output', () => {
      fc.assert(
        fc.property(authModeSubsetArb, (modes) => {
          const serialized = serializeAuthModes(modes)
          const deserialized = deserializeAuthModes(serialized)
          
          // 结果应该是排序的
          const sorted = [...deserialized].sort()
          expect(deserialized).toEqual(sorted)
        }),
        { numRuns: 100 }
      )
    })

    it('should remove duplicates', () => {
      // 生成可能包含重复的数组
      const modesWithDuplicatesArb = fc.array(fc.constantFrom(...ValidAuthModes), { minLength: 0, maxLength: 10 })

      fc.assert(
        fc.property(modesWithDuplicatesArb, (modes) => {
          const serialized = serializeAuthModes(modes)
          const deserialized = deserializeAuthModes(serialized)
          
          // 结果不应该有重复
          const unique = [...new Set(deserialized)]
          expect(deserialized).toEqual(unique)
        }),
        { numRuns: 100 }
      )
    })

    it('should filter invalid auth modes', () => {
      // 生成混合有效和无效值的数组
      const mixedArb = fc.array(
        fc.oneof(
          fc.constantFrom(...ValidAuthModes),
          fc.string({ minLength: 1, maxLength: 10 })
        ),
        { minLength: 0, maxLength: 10 }
      )

      fc.assert(
        fc.property(mixedArb, (modes) => {
          const serialized = serializeAuthModes(modes)
          const deserialized = deserializeAuthModes(serialized)
          
          // 结果应该只包含有效的认证方式
          deserialized.forEach((mode) => {
            expect(ValidAuthModes).toContain(mode)
          })
        }),
        { numRuns: 100 }
      )
    })
  })
})
