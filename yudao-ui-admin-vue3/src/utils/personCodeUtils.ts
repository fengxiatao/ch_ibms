/**
 * 人员编号生成工具函数
 * 用于批量添加人员功能中的人员编号生成
 */

/**
 * 生成人员编号列表
 * **Feature: batch-person-add, Property 1: 人员编号生成正确性**
 * **Validates: Requirements 1.3**
 * 
 * @param startCode 起始编号
 * @param count 数量
 * @returns 人员编号列表
 */
export const generatePersonCodes = (startCode: string, count: number): string[] => {
  if (!startCode || count <= 0) {
    return []
  }
  
  const codes: string[] = []
  const startNum = parseInt(startCode, 10)
  const isNumeric = !isNaN(startNum)
  
  for (let i = 0; i < count; i++) {
    let personCode: string
    if (isNumeric) {
      // 数字编号，保持位数
      personCode = String(startNum + i).padStart(startCode.length, '0')
    } else {
      // 非数字编号，直接追加序号
      personCode = startCode + String(i + 1).padStart(3, '0')
    }
    codes.push(personCode)
  }
  
  return codes
}

/**
 * 验证人员编号列表是否连续递增
 * @param codes 人员编号列表
 * @param startCode 起始编号
 * @returns 是否连续递增
 */
export const isSequentialCodes = (codes: string[], startCode: string): boolean => {
  if (codes.length === 0) {
    return true
  }
  
  const startNum = parseInt(startCode, 10)
  const isNumeric = !isNaN(startNum)
  
  if (isNumeric) {
    // 数字编号，检查是否连续递增
    for (let i = 0; i < codes.length; i++) {
      const expectedNum = startNum + i
      const actualNum = parseInt(codes[i], 10)
      if (actualNum !== expectedNum) {
        return false
      }
    }
  } else {
    // 非数字编号，检查后缀是否连续递增
    for (let i = 0; i < codes.length; i++) {
      const expectedSuffix = String(i + 1).padStart(3, '0')
      const expectedCode = startCode + expectedSuffix
      if (codes[i] !== expectedCode) {
        return false
      }
    }
  }
  
  return true
}
