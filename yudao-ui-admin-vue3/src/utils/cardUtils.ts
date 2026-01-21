/**
 * 卡号工具函数
 * 用于批量添加人员功能中的卡号处理
 */

/**
 * 验证卡号格式是否正确
 * 卡号需要是8-16位十六进制字符串
 * @param cardNo 卡号
 * @returns 是否有效
 */
export const isValidCardNo = (cardNo: string): boolean => {
  const hexRegex = /^[0-9A-Fa-f]{8,16}$/
  return hexRegex.test(cardNo)
}

/**
 * 添加卡号到列表（带去重）
 * **Feature: batch-person-add, Property 3: 卡号去重**
 * **Validates: Requirements 2.2**
 * 
 * @param cardNos 现有卡号列表
 * @param newCard 要添加的新卡号
 * @returns 添加结果 { success: boolean, cardNos: string[], message?: string }
 */
export const addCardToList = (
  cardNos: string[],
  newCard: string
): { success: boolean; cardNos: string[]; message?: string } => {
  const card = newCard.trim().toUpperCase()
  
  if (!card) {
    return { success: false, cardNos, message: '请输入卡号' }
  }
  
  if (!isValidCardNo(card)) {
    return { success: false, cardNos, message: '卡号格式错误，需要8-16位十六进制' }
  }
  
  // 去重检查 - Requirements 2.2
  if (cardNos.includes(card)) {
    return { success: false, cardNos, message: '该卡号已添加' }
  }
  
  // 返回新数组，不修改原数组
  return { success: true, cardNos: [...cardNos, card] }
}

/**
 * 从列表中移除卡号
 * @param cardNos 现有卡号列表
 * @param index 要移除的索引
 * @returns 新的卡号列表
 */
export const removeCardFromList = (cardNos: string[], index: number): string[] => {
  if (index < 0 || index >= cardNos.length) {
    return cardNos
  }
  return [...cardNos.slice(0, index), ...cardNos.slice(index + 1)]
}
