/**
 * 认证方式工具函数
 * 用于处理门禁权限组的认证方式序列化/反序列化
 */

/** 认证方式枚举 */
export enum AuthMode {
  CARD = 'CARD',
  FACE = 'FACE',
  FINGERPRINT = 'FINGERPRINT',
  PASSWORD = 'PASSWORD'
}

/** 认证方式中文标签映射 */
export const AuthModeLabels: Record<string, string> = {
  [AuthMode.CARD]: '卡',
  [AuthMode.FACE]: '人脸',
  [AuthMode.FINGERPRINT]: '指纹',
  [AuthMode.PASSWORD]: '密码'
}

/** 所有有效的认证方式列表 */
export const ValidAuthModes = Object.values(AuthMode)

/**
 * 将认证方式数组序列化为逗号分隔的字符串
 * @param modes 认证方式数组
 * @returns 逗号分隔的字符串，如 "CARD,FACE,FINGERPRINT"
 */
export function serializeAuthModes(modes: string[]): string {
  if (!modes || modes.length === 0) {
    return ''
  }
  // 过滤无效值，去重，排序以保证一致性
  const validModes = modes
    .filter((mode) => ValidAuthModes.includes(mode as AuthMode))
    .filter((mode, index, arr) => arr.indexOf(mode) === index)
    .sort()
  return validModes.join(',')
}

/**
 * 将逗号分隔的字符串反序列化为认证方式数组
 * @param str 逗号分隔的字符串
 * @returns 认证方式数组
 */
export function deserializeAuthModes(str: string): string[] {
  if (!str || str.trim() === '') {
    return []
  }
  // 分割、过滤空值和无效值、去重、排序
  const modes = str
    .split(',')
    .map((s) => s.trim())
    .filter((s) => s !== '' && ValidAuthModes.includes(s as AuthMode))
    .filter((mode, index, arr) => arr.indexOf(mode) === index)
    .sort()
  return modes
}

/**
 * 获取认证方式的中文标签
 * @param mode 认证方式代码
 * @returns 中文标签，如果未找到则返回原始代码
 */
export function getAuthModeLabel(mode: string): string {
  return AuthModeLabels[mode] || mode
}

/**
 * 获取多个认证方式的中文标签，用逗号分隔
 * @param modes 认证方式数组
 * @returns 中文标签字符串，如 "卡,人脸,指纹"
 */
export function getAuthModeLabels(modes: string[]): string {
  if (!modes || modes.length === 0) {
    return ''
  }
  return modes.map(getAuthModeLabel).join(',')
}

/**
 * 从逗号分隔字符串获取中文标签
 * @param str 逗号分隔的认证方式字符串
 * @returns 中文标签字符串
 */
export function getAuthModeLabelsFromString(str: string): string {
  const modes = deserializeAuthModes(str)
  return getAuthModeLabels(modes)
}
