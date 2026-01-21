import { formatTime } from '@/utils'
import dayjs from 'dayjs'

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param date 日期
 */
export const formatDate = (date: Date | number | string | undefined): string => {
  if (!date) return ''
  return formatTime(date, 'yyyy-MM-dd')
}

/**
 * 日期格式化器2 - 用于列表显示
 * @param row 行数据
 * @param column 列配置
 * @param cellValue 单元格值
 */
export const dateFormatter2 = (row: any, column: any, cellValue: any) => {
  if (!cellValue) return ''
  return formatTime(cellValue, 'yyyy-MM-dd HH:mm:ss')
}

/**
 * 格式化日期时间
 * @param dateTime 日期时间
 * @param format 格式
 */
export const formatDateTime = (dateTime: Date | number | string | undefined, format = 'yyyy-MM-dd HH:mm:ss'): string => {
  if (!dateTime) return ''
  return formatTime(dateTime, format)
}

/**
 * 日期格式化器 - 用于列表显示
 * @param row 行数据
 * @param column 列配置
 * @param cellValue 单元格值
 */
export const dateFormatter = (row: any, column: any, cellValue: any) => {
  return formatDate(cellValue)
}

/**
 * 获取某天的开始时间（00:00:00）
 * @param date 日期
 */
export const beginOfDay = (date: Date): Date => {
  const newDate = new Date(date)
  newDate.setHours(0, 0, 0, 0)
  return newDate
}

/**
 * 获取某天的结束时间（23:59:59）
 * @param date 日期
 */
export const endOfDay = (date: Date): Date => {
  const newDate = new Date(date)
  newDate.setHours(23, 59, 59, 999)
  return newDate
}

/**
 * 增加时间
 * @param date 日期
 * @param days 天数
 */
export const addTime = (date: Date, days: number): Date => {
  return new Date(date.getTime() + days * 24 * 60 * 60 * 1000)
}

/**
 * 计算两个日期之间的天数
 * @param date1 日期1
 * @param date2 日期2
 */
export const betweenDay = (date1: Date, date2: Date): number => {
  const diff = Math.abs(date1.getTime() - date2.getTime())
  return Math.floor(diff / (24 * 60 * 60 * 1000))
}

/**
 * 格式化过去的时间
 * @param date 日期
 */
export const formatPast = (date: Date | string | number): string => {
  if (!date) return ''
  
  const dateObj = new Date(date)
  const now = new Date()
  const diff = now.getTime() - dateObj.getTime()
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

/**
 * 格式化耗时（毫秒）为人类可读字符串
 * 用途：流程任务“耗时”字段显示，如 1天2小时30分钟
 * @param durationInMillis 耗时毫秒数
 * @returns 格式化后的字符串，如“1天2小时30分钟”/“45分钟12秒”等；当入参为空或非正数时返回“-”
 * @throws 无显式抛出异常；若入参不是数字将返回“-”
 */
export const formatPast2 = (durationInMillis: number): string => {
  if (!durationInMillis || typeof durationInMillis !== 'number' || durationInMillis <= 0) {
    return '-'
  }
  const totalSeconds = Math.floor(durationInMillis / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  const secondsR = totalSeconds % 60
  const minutesR = minutes % 60
  const hoursR = hours % 24

  if (days > 0) {
    return `${days}天${hoursR}小时${minutesR}分钟`
  }
  if (hours > 0) {
    return `${hours}小时${minutesR}分钟`
  }
  if (minutes > 0) {
    return `${minutes}分钟${secondsR}秒`
  }
  return `${secondsR}秒`
}

/**
 * 默认快捷选项
 */
export const defaultShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

/**
 * 判断两个时间是否同一天（以本地时区为准）
 * 用途：统计页时间范围选择的边界判断，当开始与结束同一天需要向前扩展一天
 * @param start 开始时间，支持 `Date | string | number`（字符串如 `YYYY-MM-DD`）
 * @param end 结束时间，支持 `Date | string | number`（字符串如 `YYYY-MM-DD`）
 * @returns 布尔值，`true` 表示两者同一天，`false` 表示不同天
 * @throws 无显式抛出异常；若任一入参为空或不可解析，返回 `false`
 */
export const isSameDay = (
  start: Date | string | number,
  end: Date | string | number
): boolean => {
  if (!start || !end) {
    return false
  }
  try {
    const s = formatTime(start, 'yyyy-MM-dd')
    const e = formatTime(end, 'yyyy-MM-dd')
    return s === e
  } catch {
    return false
  }
}

/**
 * 获取指定日期的整日时间范围（开始 00:00:00，结束 23:59:59）
 * @param date 基准日期，支持 `Date | string | number`
 * @param offsetDays 相对偏移天数（可为负），例如 -1 表示昨天
 * @returns 二元组：`[开始时间字符串, 结束时间字符串]`，格式 `YYYY-MM-DD HH:mm:ss`
 * @throws 无显式抛出异常；若入参不可解析，将返回当前日期的整日范围
 */
export const getDayRange = (
  date: Date | string | number,
  offsetDays = 0
): [string, string] => {
  try {
    const d = dayjs(date).add(offsetDays, 'day')
    const begin = d.startOf('day').format('YYYY-MM-DD HH:mm:ss')
    const end = d.endOf('day').format('YYYY-MM-DD HH:mm:ss')
    return [begin, end]
  } catch {
    const today = dayjs()
    return [
      today.startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      today.endOf('day').format('YYYY-MM-DD HH:mm:ss')
    ]
  }
}

/**
 * 获取最近 7 天范围（开始为 7 天前 00:00:00，结束为昨天 23:59:59）
 * @returns 二元组：`[开始时间字符串, 结束时间字符串]`
 */
export const getLast7Days = (): [string, string] => {
  const begin = dayjs().subtract(7, 'day')
  const end = dayjs().subtract(1, 'day')
  return getDateRange(begin, end)
}

/**
 * 获取最近 30 天范围（开始为 30 天前 00:00:00，结束为昨天 23:59:59）
 * @returns 二元组：`[开始时间字符串, 结束时间字符串]`
 */
export const getLast30Days = (): [string, string] => {
  const begin = dayjs().subtract(30, 'day')
  const end = dayjs().subtract(1, 'day')
  return getDateRange(begin, end)
}

/**
 * 获取最近 1 年范围（开始为 365 天前 00:00:00，结束为昨天 23:59:59）
 * @returns 二元组：`[开始时间字符串, 结束时间字符串]`
 */
export const getLast1Year = (): [string, string] => {
  const begin = dayjs().subtract(365, 'day')
  const end = dayjs().subtract(1, 'day')
  return getDateRange(begin, end)
}

/**
 * 将两个时间归整为日期范围（开始 00:00:00，结束 23:59:59）
 * @param begin 开始时间，支持 dayjs 可解析的任意类型（Date、字符串、时间戳等）
 * @param end 结束时间，支持 dayjs 可解析的任意类型（Date、字符串、时间戳等）
 * @returns 二元组：`[开始时间字符串, 结束时间字符串]`
 */
export const getDateRange = (
  begin: dayjs.ConfigType,
  end: dayjs.ConfigType
): [string, string] => {
  const b = dayjs(begin).startOf('day').format('YYYY-MM-DD HH:mm:ss')
  const e = dayjs(end).endOf('day').format('YYYY-MM-DD HH:mm:ss')
  return [b, e]
}

