/**
 * 流媒体录像回放（ZLM + WebRTC/FLV）
 * 用于外网演示，与 useDahuaPlayback（内网 RPC2）并列使用
 */
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { PlaybackApi } from '@/api/security/playback'
import type { PlaybackPane } from '../types'
import { formatDateTime, parseTimeString } from '../types'

export interface StreamPaneState {
  cameraId?: number
  cameraName?: string
  streamId?: string
  app?: string
  stream?: string
  flvUrl?: string
  webrtcUrl?: string
  streamStartTime?: number
  streamEndTime?: number
}

const fixUrl = (url?: string) => {
  if (!url) return url
  return url
}

export function useStreamPlayback() {
  const paneStates = ref<Map<number, StreamPaneState>>(new Map())

  const getState = (idx: number): StreamPaneState => {
    if (!paneStates.value.has(idx)) {
      paneStates.value.set(idx, {})
    }
    return paneStates.value.get(idx)!
  }

  /** 关闭单个窗口当前流 */
  const stopPlayback = async (pane: PlaybackPane, paneIndex: number): Promise<void> => {
    const st = getState(paneIndex)
    if (st.streamId) {
      try {
        await PlaybackApi.closeStream(st.streamId)
      } catch (e) {
        console.error('[流媒体回放] 关闭回放流失败:', e)
      }
      st.streamId = undefined
    }
    pane.isPlaying = false
    pane.isLoading = false
    pane.isPaused = false
    pane.error = null
  }

  /** 判断时间是否在当前窗口的流片段范围内（允许 5 秒容差） */
  const isTimeInSegment = (paneIndex: number, timeMs: number): boolean => {
    const st = getState(paneIndex)
    if (st.streamStartTime == null || st.streamEndTime == null) return false
    const tol = 5000
    return timeMs >= st.streamStartTime - tol && timeMs <= st.streamEndTime + tol
  }

  /** 从指定时间拉一条 ZLM 回放流 */
  const startStreamFromTime = async (
    pane: PlaybackPane,
    paneIndex: number,
    cameraId: number,
    cameraName: string,
    startMs: number,
    endMs: number
  ): Promise<boolean> => {
    try {
      pane.isLoading = true
      pane.error = null

      const resp: any = await PlaybackApi.getPlaybackUrl({
        cameraId,
        startTime: formatDateTime(new Date(startMs)),
        endTime: formatDateTime(new Date(endMs)),
        streamType: 1
      })
      // 兼容 CommonResult 包装：{ code, data, msg }
      const vo = resp?.data ?? resp

      const st = getState(paneIndex)
      st.cameraId = cameraId
      st.cameraName = cameraName
      st.flvUrl = fixUrl(vo?.flvUrl)
      st.webrtcUrl = fixUrl(vo?.webrtcUrl)
      st.streamId = vo?.streamId ?? vo?.stream
      st.app = vo?.app
      st.stream = vo?.stream ?? vo?.streamId
      st.streamStartTime = startMs
      st.streamEndTime = endMs

      pane.channel = { name: cameraName, channelNo: cameraId }
      pane.isPlaying = true
      pane.isPaused = false
      pane.error = null
      pane.currentPlaySeconds = 0

      await nextTick()
      pane.isLoading = false
      ElMessage.success(`正在回放: ${vo?.cameraName ?? cameraName}`)
      return true
    } catch (e: any) {
      console.error('[流媒体回放] 获取回放地址失败:', e)
      pane.isLoading = false
      pane.error = e?.message || '获取回放地址失败'
      ElMessage.error(pane.error)
      return false
    }
  }

  /** 从 currentPlayTime 开始播放（最多 1 小时） */
  const startPlaybackByCurrentTime = async (
    pane: PlaybackPane,
    paneIndex: number,
    cameraId: number,
    cameraName: string,
    currentPlayTimeMs: number,
    rangeEndStr: string
  ): Promise<boolean> => {
    const rangeEnd = parseTimeString(rangeEndStr)
    const endMs = Math.min(currentPlayTimeMs + 3600_000, rangeEnd)
    if (endMs <= currentPlayTimeMs) {
      ElMessage.warning('当前时间范围无效')
      return false
    }
    return startStreamFromTime(pane, paneIndex, cameraId, cameraName, currentPlayTimeMs, endMs)
  }

  /** 时间轴/快进/快退：不在当前流片段内则重新拉流 */
  const seekByRestart = async (
    pane: PlaybackPane,
    paneIndex: number,
    cameraId: number,
    cameraName: string,
    targetTimeMs: number,
    rangeEndStr: string
  ): Promise<void> => {
    const rangeEnd = parseTimeString(rangeEndStr)
    const endMs = Math.min(targetTimeMs + 3600_000, rangeEnd)
    if (endMs <= targetTimeMs) return
    await stopPlayback(pane, paneIndex)
    await startStreamFromTime(pane, paneIndex, cameraId, cameraName, targetTimeMs, endMs)
  }

  return {
    paneStates,
    getState,
    stopPlayback,
    isTimeInSegment,
    startPlaybackByCurrentTime,
    seekByRestart
  }
}
