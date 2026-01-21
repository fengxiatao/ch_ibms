/**
 * 卡号工具函数属性测试
 * **Feature: batch-person-add, Property 3: 卡号去重**
 * **Validates: Requirements 2.2**
 */
import { describe, it, expect } from 'vitest'
import * as fc from 'fast-check'
import { addCardToList, isValidCardNo } from './cardUtils'

/**
 * 生成有效的卡号（8-16位十六进制字符串）
 */
const validCardNoArbitrary = fc.integer({ min: 8, max: 16 }).chain((length) =>
  fc.stringOf(
    fc.constantFrom(...'0123456789ABCDEF'.split('')),
    { minLength: length, maxLength: length }
  )
)

/**
 * 生成有效的卡号列表（无重复）
 */
const uniqueCardListArbitrary = fc.uniqueArray(validCardNoArbitrary, { minLength: 0, maxLength: 20 })

describe('卡号工具函数', () => {
  describe('isValidCardNo - 卡号格式验证', () => {
    it('应该接受8-16位十六进制字符串', () => {
      fc.assert(
        fc.property(validCardNoArbitrary, (cardNo) => {
          return isValidCardNo(cardNo) === true
        }),
        { numRuns: 100 }
      )
    })

    it('应该拒绝长度小于8的字符串', () => {
      fc.assert(
        fc.property(
          fc.stringOf(fc.constantFrom(...'0123456789ABCDEF'.split('')), { minLength: 1, maxLength: 7 }),
          (cardNo) => {
            return isValidCardNo(cardNo) === false
          }
        ),
        { numRuns: 100 }
      )
    })

    it('应该拒绝长度大于16的字符串', () => {
      fc.assert(
        fc.property(
          fc.stringOf(fc.constantFrom(...'0123456789ABCDEF'.split('')), { minLength: 17, maxLength: 30 }),
          (cardNo) => {
            return isValidCardNo(cardNo) === false
          }
        ),
        { numRuns: 100 }
      )
    })

    it('应该拒绝包含非十六进制字符的字符串', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 8, max: 16 }).chain((length) =>
            fc.tuple(
              fc.stringOf(fc.constantFrom(...'0123456789ABCDEF'.split('')), { minLength: 0, maxLength: length - 1 }),
              fc.constantFrom(...'GHIJKLMNOPQRSTUVWXYZ!@#$%^&*()'.split('')),
              fc.stringOf(fc.constantFrom(...'0123456789ABCDEF'.split('')), { minLength: 0, maxLength: length - 1 })
            ).map(([prefix, invalid, suffix]) => prefix + invalid + suffix)
          ),
          (cardNo) => {
            // 只测试长度在8-16之间的情况
            if (cardNo.length >= 8 && cardNo.length <= 16) {
              return isValidCardNo(cardNo) === false
            }
            return true
          }
        ),
        { numRuns: 100 }
      )
    })
  })

  describe('addCardToList - 卡号去重属性测试', () => {
    /**
     * **Feature: batch-person-add, Property 3: 卡号去重**
     * **Validates: Requirements 2.2**
     * 
     * 属性：对于任意卡号列表，添加已存在的卡号后，列表长度不应增加
     */
    it('Property 3: 添加已存在的卡号后，列表长度不应增加', () => {
      fc.assert(
        fc.property(
          uniqueCardListArbitrary.filter(list => list.length > 0),
          fc.nat({ max: 100 }),
          (cardList, indexSeed) => {
            // 从列表中选择一个已存在的卡号
            const existingCard = cardList[indexSeed % cardList.length]
            const originalLength = cardList.length
            
            // 尝试添加已存在的卡号
            const result = addCardToList(cardList, existingCard)
            
            // 验证：添加失败，列表长度不变
            expect(result.success).toBe(false)
            expect(result.cardNos.length).toBe(originalLength)
            expect(result.message).toBe('该卡号已添加')
            
            return true
          }
        ),
        { numRuns: 100 }
      )
    })

    /**
     * 属性：添加新卡号后，列表长度应该增加1
     */
    it('添加新的有效卡号后，列表长度应该增加1', () => {
      fc.assert(
        fc.property(
          uniqueCardListArbitrary,
          validCardNoArbitrary,
          (cardList, newCard) => {
            // 确保新卡号不在列表中
            fc.pre(!cardList.includes(newCard))
            
            const originalLength = cardList.length
            const result = addCardToList(cardList, newCard)
            
            // 验证：添加成功，列表长度增加1
            expect(result.success).toBe(true)
            expect(result.cardNos.length).toBe(originalLength + 1)
            expect(result.cardNos).toContain(newCard)
            
            return true
          }
        ),
        { numRuns: 100 }
      )
    })

    /**
     * 属性：添加卡号不应修改原列表（不可变性）
     */
    it('添加卡号不应修改原列表', () => {
      fc.assert(
        fc.property(
          uniqueCardListArbitrary,
          validCardNoArbitrary,
          (cardList, newCard) => {
            const originalList = [...cardList]
            addCardToList(cardList, newCard)
            
            // 验证：原列表未被修改
            expect(cardList).toEqual(originalList)
            
            return true
          }
        ),
        { numRuns: 100 }
      )
    })

    /**
     * 属性：卡号大小写不敏感（统一转为大写）
     */
    it('卡号应该大小写不敏感', () => {
      fc.assert(
        fc.property(
          uniqueCardListArbitrary.filter(list => list.length > 0),
          fc.nat({ max: 100 }),
          (cardList, indexSeed) => {
            // 从列表中选择一个已存在的卡号，转为小写
            const existingCard = cardList[indexSeed % cardList.length]
            const lowerCaseCard = existingCard.toLowerCase()
            
            // 尝试添加小写版本
            const result = addCardToList(cardList, lowerCaseCard)
            
            // 验证：应该被识别为重复
            expect(result.success).toBe(false)
            expect(result.message).toBe('该卡号已添加')
            
            return true
          }
        ),
        { numRuns: 100 }
      )
    })

    /**
     * 属性：空字符串或空白字符串应该被拒绝
     */
    it('空字符串或空白字符串应该被拒绝', () => {
      fc.assert(
        fc.property(
          uniqueCardListArbitrary,
          fc.constantFrom('', ' ', '  ', '\t', '\n', '   '),
          (cardList, emptyCard) => {
            const result = addCardToList(cardList, emptyCard)
            
            // 验证：添加失败
            expect(result.success).toBe(false)
            expect(result.message).toBe('请输入卡号')
            
            return true
          }
        ),
        { numRuns: 50 }
      )
    })

    /**
     * 属性：无效格式的卡号应该被拒绝
     */
    it('无效格式的卡号应该被拒绝', () => {
      fc.assert(
        fc.property(
          uniqueCardListArbitrary,
          fc.stringOf(fc.constantFrom(...'GHIJKLMNOPQRSTUVWXYZ!@#$%^&*()'.split('')), { minLength: 8, maxLength: 16 }),
          (cardList, invalidCard) => {
            const result = addCardToList(cardList, invalidCard)
            
            // 验证：添加失败
            expect(result.success).toBe(false)
            expect(result.message).toBe('卡号格式错误，需要8-16位十六进制')
            
            return true
          }
        ),
        { numRuns: 100 }
      )
    })
  })
})
