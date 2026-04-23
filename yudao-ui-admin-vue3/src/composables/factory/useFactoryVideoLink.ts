import dayjs from 'dayjs'
import type { LocationQuery } from 'vue-router'

export type FactoryVideoLinkMode = 'live' | 'playback'

export interface FactoryVideoLinkQuery {
  mode: FactoryVideoLinkMode
  deviceId?: number
  alarmId?: number
  sourceId?: string
  sourceName?: string
  location?: string
  alarmTime?: string
  startTime?: string
  endTime?: string
}

const normalizeString = (value: unknown) => {
  if (Array.isArray(value)) {
    return value[0] ? String(value[0]) : undefined
  }
  if (value === null || value === undefined || value === '') {
    return undefined
  }
  return String(value)
}

const normalizeNumber = (value: unknown) => {
  const str = normalizeString(value)
  if (!str) {
    return undefined
  }
  const parsed = Number(str)
  return Number.isNaN(parsed) ? undefined : parsed
}

export const buildPlaybackTimeRange = (alarmTime?: string, windowMinutes = 5) => {
  if (!alarmTime) {
    return {
      startTime: undefined,
      endTime: undefined
    }
  }

  const baseTime = dayjs(alarmTime)
  if (!baseTime.isValid()) {
    return {
      startTime: undefined,
      endTime: undefined
    }
  }

  return {
    startTime: baseTime.subtract(windowMinutes, 'minute').format('YYYY-MM-DDTHH:mm:ss'),
    endTime: baseTime.add(windowMinutes, 'minute').format('YYYY-MM-DDTHH:mm:ss')
  }
}

export const buildFactoryVideoLinkQuery = (query: Partial<FactoryVideoLinkQuery>) => {
  const nextQuery: Record<string, string> = {
    mode: query.mode || 'live'
  }

  if (query.deviceId !== undefined) {
    nextQuery.deviceId = String(query.deviceId)
  }
  if (query.alarmId !== undefined) {
    nextQuery.alarmId = String(query.alarmId)
  }
  if (query.sourceId) {
    nextQuery.sourceId = query.sourceId
  }
  if (query.sourceName) {
    nextQuery.sourceName = query.sourceName
  }
  if (query.location) {
    nextQuery.location = query.location
  }
  if (query.alarmTime) {
    nextQuery.alarmTime = query.alarmTime
  }
  if (query.startTime) {
    nextQuery.startTime = query.startTime
  }
  if (query.endTime) {
    nextQuery.endTime = query.endTime
  }

  return nextQuery
}

export const parseFactoryVideoLinkQuery = (query: LocationQuery): FactoryVideoLinkQuery => {
  const mode = normalizeString(query.mode) === 'playback' ? 'playback' : 'live'

  return {
    mode,
    deviceId: normalizeNumber(query.deviceId),
    alarmId: normalizeNumber(query.alarmId),
    sourceId: normalizeString(query.sourceId),
    sourceName: normalizeString(query.sourceName),
    location: normalizeString(query.location),
    alarmTime: normalizeString(query.alarmTime),
    startTime: normalizeString(query.startTime),
    endTime: normalizeString(query.endTime)
  }
}
