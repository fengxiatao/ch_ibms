/**
 * 人员编号生成属性测试
 * **Feature: batch-person-add, Property 1: 人员编号生成正确性**
 * **Validates: Requirements 1.3**
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import { generatePersonCodes, isSequentialCodes } from './personCodeUtils'

/**
 * 生成有效的数字起始编号（1-6位数字字符串）
 */
const numericStartCodeArbitrary = fc.integer({ min: 1, max: 999999 }).map((n) => String(n))

/**
 * 生成带前导零的数字起始编号
 */
const paddedNumericStartCodeArbitrary = fc.tuple(
  fc.integer({ min: 1, max: 999999 }),
  fc.integer({ min: 1, max: 6 })
).map(([n, minLength]) => String(n).padStart(Math.max(minLength, String(n).length), '0'))

/**
 * 生成有效的非数字起始编号（字母前缀）
 */
const alphaStartCodeArbitrary = fc.stringOf(fc.constantFrom(...'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('')), { minLength: 1, maxLength: 5 })

/**
 * 生成有效的数量（1-100）
 */
const countArbitrary = fc.integer({ min: 1, max: 100 })

describe('人员编号生成工具函数', () => {
  describe('generatePersonCodes - 人员编号生成', () => {
    /**
     * **Feature: batch-person-add, Property 1: 人员编号生成正确性**
     * **Validates: Requirements 1.3**
     * 
     * 属性：对于任意起始编号N和数量M，生成的人员编号列表应该是连续递增的序列，从N开始，共M个
     */
    it('Property 1: 数字起始编号生成的列表长度应等于数量', () => {
      fc.assert(
        fc.property(numericStartCodeArbitrary, countArbitrary, (startCode, count) => {
          const codes = generatePersonCodes(startCode, count)
          
          // 验证：列表长度等于数量
          expect(codes.length).toBe(count)
          
          return true
        }),
        { numRuns: 100 }
      )
    })

    it('Property 1: 数字起始编号生成的列表应该连续递增', () => {
      fc.assert(
        fc.property(numericStartCodeArbitrary, countArbitrary, (startCode, count) => {
          const codes = generatePersonCodes(startCode, count)
          
          // 验证：列表是连续递增的
          expect(isSequentialCodes(codes, startCode)).toBe(true)
          
          return true
        }),
        { numRuns: 100 }
      )
    })

    it('Property 1: 带前导零的数字起始编号应保持位数', () => {
      fc.assert(
        fc.property(paddedNumericStartCodeArbitrary, countArbitrary, (startCode, count) => {
          const codes = generatePersonCodes(startCode, count)
          
          // 验证：所有编号长度至少等于起始编号长度
          for (const code of codes) {
            expect(code.length).toBeGreaterThanOrEqual(startCode.length)
          }
          
          return true
        }),
        { numRuns: 100 }
      )
    })

    it('Property 1: 字母前缀起始编号生成的列表长度应等于数量', () => {
      fc.assert(
        fc.property(alphaStartCodeArbitrary, countArbitrary, (startCode, count) => {
          const codes = generatePersonCodes(startCode, count)
          
          // 验证：列表长度等于数量
          expect(codes.length).toBe(count)
          
          return true
        }),
        { numRuns: 100 }
      )
    })

    it('Property 1: 字母前缀起始编号生成的列表应该连续递增', () => {
      fc.assert(
        fc.property(alphaStartCodeArbitrary, countArbitrary, (startCode, count) => {
          const codes = generatePersonCodes(startCode, count)
          
          // 验证：列表是连续递增的
          expect(isSequentialCodes(codes, startCode)).toBe(true)
          
          return true
        }),
        { numRuns: 100 }
      )
    })

    it('Property 1: 第一个编号应该基于起始编号', () => {
      fc.assert(
        fc.property(numericStartCodeArbitrary, countArbitrary, (startCode, count) => {
          const codes = generatePersonCodes(startCode, count)
          
          // 验证：第一个编号的数值等于起始编号
          const firstNum = parseInt(codes[0], 10)
          const startNum = parseInt(startCode, 10)
          expect(firstNum).toBe(startNum)
          
          return true
        }),
        { numRuns: 100 }
      )
    })

    it('Property 1: 最后一个编号应该等于起始编号加数量减1', () => {
      fc.assert(
        fc.property(numericStartCodeArbitrary, countArbitrary, (startCode, count) => {
          const codes = generatePersonCodes(startCode, count)
          
          // 验证：最后一个编号的数值等于起始编号 + 数量 - 1
          const lastNum = parseInt(codes[codes.length - 1], 10)
          const startNum = parseInt(startCode, 10)
          expect(lastNum).toBe(startNum + count - 1)
          
          return true
        }),
        { numRuns: 100 }
      )
    })

    it('空起始编号应返回空列表', () => {
      const codes = generatePersonCodes('', 10)
      expect(codes).toEqual([])
    })

    it('数量为0应返回空列表', () => {
      const codes = generatePersonCodes('100001', 0)
      expect(codes).toEqual([])
    })

    it('负数数量应返回空列表', () => {
      const codes = generatePersonCodes('100001', -5)
      expect(codes).toEqual([])
    })
  })

  describe('isSequentialCodes - 连续性验证', () => {
    it('空列表应返回true', () => {
      expect(isSequentialCodes([], '100001')).toBe(true)
    })

    it('正确的连续数字列表应返回true', () => {
      expect(isSequentialCodes(['100001', '100002', '100003'], '100001')).toBe(true)
    })

    it('不连续的数字列表应返回false', () => {
      expect(isSequentialCodes(['100001', '100003', '100005'], '100001')).toBe(false)
    })

    it('正确的字母前缀列表应返回true', () => {
      expect(isSequentialCodes(['ABC001', 'ABC002', 'ABC003'], 'ABC')).toBe(true)
    })

    it('不连续的字母前缀列表应返回false', () => {
      expect(isSequentialCodes(['ABC001', 'ABC003', 'ABC005'], 'ABC')).toBe(false)
    })
  })
})
