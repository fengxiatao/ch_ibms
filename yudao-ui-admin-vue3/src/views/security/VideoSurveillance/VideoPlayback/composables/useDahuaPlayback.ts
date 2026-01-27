/**
 * 大华录像回放 Composable
 * 使用大华 H5 SDK 进行录像查询和回放
 */

import { ref, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { DEFAULT_NVR_CONFIG } from '@/composables/useDahuaPlayer'
import type { PlaybackPane, ChannelRecordingInfo } from '../types'

/** 录像文件信息 */
export interface DahuaRecordFile {
  FilePath: string
  StartTime: string
  EndTime: string
  Length: number
  Channel?: number
}

/**
 * 大华录像回放 Composable
 */
export function useDahuaPlayback() {
  const recordsLoading = ref(false)
  const isLoggedIn = ref(false)

  /**
   * 登录设备
   */
  const loginDevice = async (): Promise<boolean> => {
    if (isLoggedIn.value) return true

    // @ts-ignore
    if (typeof window.RPC === 'undefined') {
      console.warn('[大华回放] RPC 未加载')
      return false
    }

    const { ip, port, username, password } = DEFAULT_NVR_CONFIG
    const target = `${ip}:${port}`

    try {
      // @ts-ignore
      window.setIP(target)
      // @ts-ignore
      await window.RPC.login(username, password, false)
      isLoggedIn.value = true
      // @ts-ignore
      window.RPC.keepAlive(300, 60000, window._getSession?.(), target)
      console.log('[大华回放] 登录成功')
      return true
    } catch (e) {
      console.error('[大华回放] 登录失败:', e)
      return false
    }
  }

  /**
   * 查询通道录像
   */
  const queryChannelRecords = async (
    channelNo: number,
    startTime: string,
    endTime: string
  ): Promise<DahuaRecordFile[]> => {
    // 确保已登录
    const loggedIn = await loginDevice()
    if (!loggedIn) {
      console.warn('[大华回放] 未登录，无法查询录像')
      return []
    }

    // @ts-ignore
    if (typeof window.RPC === 'undefined') return []

    const channel = channelNo - 1 // RPC 通道从 0 开始
    const allRecords: DahuaRecordFile[] = []

    try {
      // 获取存储信息
      // @ts-ignore
      const tmpDir = await window.RPC.getDeviceAllInfo('getDeviceAllInfo').catch(() => ({ info: [] }))
      let dirs = 'All'
      if (tmpDir.info && tmpDir.info.length === 1 && tmpDir.info[0]?.Detail?.[0]?.Path) {
        dirs = tmpDir.info[0].Detail[0].Path
      }

      // 创建查询实例
      // @ts-ignore
      const queryRes = await window.RPC.MediaFileFind.instance()
      const queryId = queryRes.result

      // 设置查询条件
      const params = {
        condition: {
          Channel: channel,
          Dirs: [dirs],
          StartTime: startTime,
          EndTime: endTime,
          Flags: null,
          Events: ['*'],
          Types: ['dav']
        }
      }

      // @ts-ignore
      await window.RPC.MediaFileFind.findFile(queryId, params)

      // 循环获取结果
      const findNext = async (): Promise<void> => {
        // @ts-ignore
        const data = await window.RPC.MediaFileFind.findNextFile(queryId, { count: 100 })
        if (data.found && data.found > 0) {
          allRecords.push(...data.infos)
          if (data.found === 100) {
            await findNext()
          }
        }
      }

      await findNext()

      // 关闭查询
      // @ts-ignore
      await window.RPC.MediaFileFind.close(queryId)
      // @ts-ignore
      await window.RPC.MediaFileFind.destroy(queryId)

      console.log(`[大华回放] 通道${channelNo} 查询到 ${allRecords.length} 条录像`)
      return allRecords
    } catch (e) {
      console.error('[大华回放] 查询录像失败:', e)
      return []
    }
  }

  /**
   * 批量查询多个通道的录像
   */
  const queryMultipleChannelRecords = async (
    channelNos: number[],
    startTime: string,
    endTime: string
  ): Promise<ChannelRecordingInfo[]> => {
    recordsLoading.value = true
    const results: ChannelRecordingInfo[] = []

    try {
      for (const channelNo of channelNos) {
        const records = await queryChannelRecords(channelNo, startTime, endTime)
        results.push({
          channelId: channelNo,
          channelName: `通道${channelNo}`,
          segments: records.map(r => ({
            startTime: r.StartTime,
            endTime: r.EndTime,
            hasRecording: true,
            filePath: r.FilePath
          }))
        })
      }
      console.log('[大华回放] 批量查询完成:', results.length, '个通道')
    } catch (e) {
      console.error('[大华回放] 批量查询失败:', e)
    } finally {
      recordsLoading.value = false
    }

    return results
  }

  /**
   * 开始回放
   * @param pane 播放窗格
   * @param paneIndex 窗格索引
   * @param channelNo 通道号
   * @param filePath 录像文件路径
   * @param channelName 通道名称
   * @param seekSeconds 跳转到指定秒数（相对于录像文件开始时间的偏移）
   */
  const startPlayback = async (
    pane: PlaybackPane,
    paneIndex: number,
    channelNo: number,
    filePath: string,
    channelName?: string,
    seekSeconds?: number
  ): Promise<boolean> => {
    // @ts-ignore
    if (typeof window.PlayerControl === 'undefined') {
      pane.error = '大华播放器 SDK 未加载'
      ElMessage.error('大华播放器 SDK 未加载')
      return false
    }

    // 确保已登录
    await loginDevice()

    pane.isLoading = true
    pane.error = null

    try {
      // 获取容器
      const containerId = `playback-player-${paneIndex}`
      const container = document.getElementById(containerId)
      if (!container) {
        throw new Error('播放器容器不存在')
      }

      // 清理容器
      container.innerHTML = ''

      // 创建 canvas 和 video
      const canvas = document.createElement('canvas')
      canvas.style.width = '100%'
      canvas.style.height = '100%'
      container.appendChild(canvas)

      const video = document.createElement('video')
      video.style.width = '100%'
      video.style.height = '100%'
      video.style.display = 'none'
      video.style.position = 'absolute'
      video.style.top = '0'
      video.style.left = '0'
      container.appendChild(video)

      // 构建 URL
      const { ip, port, username, password } = DEFAULT_NVR_CONFIG
      const wsScheme = location.protocol === 'https:' ? 'wss' : 'ws'
      const wsURL = `${wsScheme}://${ip}:${port}/rtspoverwebsocket`
      const rtspURL = `rtsp://${ip}:${port}/${filePath}`

      console.log('[大华回放] 开始回放:', {
        wsURL,
        rtspURL,
        filePath,
        paneIndex,
        seekSeconds
      })

      // 创建播放器
      // @ts-ignore
      const player = new window.PlayerControl({
        wsURL,
        rtspURL,
        username,
        password,
        lessRateCanvas: true,
        playback: true,
        isPrivateProtocol: false,
        speed: 1,
        wndIndex: paneIndex
      })

      // 事件监听
      player.on('WorkerReady', () => {
        console.log(`[大华回放] 窗格${paneIndex + 1} Worker 就绪`)
        player.connect()
      })

      player.on('DecodeStart', (e: any) => {
        console.log(`[大华回放] 窗格${paneIndex + 1} 开始解码:`, e)
        if (e.decodeMode === 'video') {
          canvas.style.display = 'none'
          video.style.display = 'block'
        } else {
          video.style.display = 'none'
          canvas.style.display = 'block'
        }
      })

      player.on('PlayStart', () => {
        console.log(`[大华回放] 窗格${paneIndex + 1} 回放开始`)
        nextTick(() => {
          pane.isLoading = false
          pane.isPlaying = true
          pane.error = null
          pane.channel = { name: channelName || `通道${channelNo}` } as any
          
          // 如果指定了跳转时间，则跳转到指定位置
          if (seekSeconds && seekSeconds > 0) {
            console.log(`[大华回放] 窗格${paneIndex + 1} 跳转到 ${seekSeconds} 秒`)
            setTimeout(() => {
              player.playByTime?.(seekSeconds)
            }, 500) // 稍微延迟确保播放稳定后再跳转
          }
        })
      })

      player.on('Error', (e: any) => {
        console.error(`[大华回放] 窗格${paneIndex + 1} 错误:`, e)
        nextTick(() => {
          pane.isLoading = false
          if (!pane.isPlaying) {
            pane.error = e?.description || `错误码: ${e?.errorCode}`
          }
        })
      })

      player.on('FileOver', () => {
        console.log(`[大华回放] 窗格${paneIndex + 1} 录像播放完毕`)
        nextTick(() => {
          pane.isPlaying = false
        })
      })

      // 初始化
      player.init(canvas, video)

      // 保存实例
      pane.jessibucaPlayer = player

      // 超时检测
      setTimeout(() => {
        if (pane.isLoading && !pane.isPlaying) {
          pane.isLoading = false
          pane.error = '连接超时'
        }
      }, 15000)

      return true
    } catch (e: any) {
      console.error('[大华回放] 创建播放器失败:', e)
      pane.isLoading = false
      pane.error = e?.message || '创建播放器失败'
      return false
    }
  }

  /**
   * 停止回放
   */
  const stopPlayback = async (pane: PlaybackPane): Promise<void> => {
    if (pane.jessibucaPlayer) {
      try {
        pane.jessibucaPlayer.stop?.()
        pane.jessibucaPlayer.close?.()
      } catch (e) {
        console.warn('[大华回放] 停止异常:', e)
      }
      pane.jessibucaPlayer = null
    }

    pane.channel = null
    pane.isPlaying = false
    pane.isLoading = false
    pane.isPaused = false
    pane.error = null
  }

  /**
   * 暂停回放
   */
  const pausePlayback = (pane: PlaybackPane): void => {
    if (pane.jessibucaPlayer && pane.isPlaying) {
      pane.jessibucaPlayer.pause?.()
      pane.isPaused = true
    }
  }

  /**
   * 恢复回放
   */
  const resumePlayback = (pane: PlaybackPane): void => {
    if (pane.jessibucaPlayer && pane.isPaused) {
      pane.jessibucaPlayer.play?.()
      pane.isPaused = false
    }
  }

  /**
   * 设置回放速度
   */
  const setSpeed = (pane: PlaybackPane, speed: number): void => {
    if (pane.jessibucaPlayer) {
      pane.jessibucaPlayer.playFF?.(speed)
    }
  }

  /**
   * 跳转到指定时间（秒）
   */
  const seekTo = (pane: PlaybackPane, seconds: number): void => {
    if (pane.jessibucaPlayer) {
      pane.jessibucaPlayer.playByTime?.(seconds)
    }
  }

  /**
   * 快进/快退（相对于当前位置）
   * @param pane 播放窗格
   * @param offsetSeconds 偏移秒数（正数快进，负数快退）
   */
  const seekRelative = (pane: PlaybackPane, offsetSeconds: number): void => {
    if (!pane.jessibucaPlayer || !pane.isPlaying) return
    
    const currentTime = pane.currentPlaySeconds || 0
    const newTime = Math.max(0, currentTime + offsetSeconds)
    console.log(`[大华回放] 跳转: ${currentTime}s -> ${newTime}s (偏移 ${offsetSeconds}s)`)
    pane.jessibucaPlayer.playByTime?.(newTime)
    pane.currentPlaySeconds = newTime
  }

  /**
   * 获取当前播放时间（秒）
   */
  const getCurrentTime = (pane: PlaybackPane): number => {
    return pane.currentPlaySeconds || 0
  }

  /**
   * 截图
   */
  const downloadSnapshot = (pane: PlaybackPane, paneIndex: number): void => {
    if (!pane.isPlaying || !pane.jessibucaPlayer) {
      ElMessage.warning('当前窗口没有正在播放的视频')
      return
    }

    const channelName = pane.channel?.name || `窗口${paneIndex + 1}`
    const fileName = `playback_${channelName}_${Date.now()}`
    pane.jessibucaPlayer.capture?.(fileName)
    ElMessage.success('截图已保存')
  }

  // 录像裁剪相关状态（支持多通道同时裁剪）
  interface CutTask {
    player: any
    isCutting: boolean
    progress: number
    channelName: string
  }
  const cutTasks = ref<Map<number, CutTask>>(new Map())
  
  // 兼容性：当前活动窗口的裁剪状态
  const isCutting = computed(() => {
    for (const task of cutTasks.value.values()) {
      if (task.isCutting || task.progress > 0 && task.progress < 100) {
        return true
      }
    }
    return false
  })
  
  const cutProgress = computed(() => {
    // 返回第一个正在进行的任务的进度
    for (const task of cutTasks.value.values()) {
      if (task.progress > 0 && task.progress < 100) {
        return task.progress
      }
    }
    return 0
  })

  /**
   * 获取指定窗口的裁剪状态
   */
  const getCutTaskState = (paneIndex: number) => {
    const task = cutTasks.value.get(paneIndex)
    return {
      isCutting: task?.isCutting || false,
      progress: task?.progress || 0
    }
  }

  /**
   * 开始录像裁剪下载
   * @param paneIndex 窗口索引（用于多通道同时裁剪）
   * @param filePath 录像文件路径
   * @param startTime 裁剪开始时间 (如: 2026-01-27 04:00:00)
   * @param endTime 裁剪结束时间 (如: 2026-01-27 05:00:00)
   * @param recordStartTime 录像文件开始时间 (用于计算偏移)
   * @param channelName 通道名称 (用于文件命名)
   * @param onProgress 进度回调
   * @param onComplete 完成回调
   */
  const startRecordCut = async (
    paneIndex: number,
    filePath: string,
    startTime: string,
    endTime: string,
    recordStartTime: string,
    channelName: string,
    onProgress?: (progress: number) => void,
    onComplete?: () => void
  ): Promise<boolean> => {
    // @ts-ignore
    if (typeof window.PlayerControl === 'undefined') {
      ElMessage.error('大华播放器 SDK 未加载')
      return false
    }

    // 确保已登录
    await loginDevice()

    // 如果该窗口正在裁剪，先停止
    if (cutTasks.value.has(paneIndex)) {
      stopRecordCut(paneIndex)
    }

    const { ip, port, username, password } = DEFAULT_NVR_CONFIG
    const wsScheme = location.protocol === 'https:' ? 'wss' : 'ws'
    const wsURL = `${wsScheme}://${ip}:${port}/rtspoverwebsocket`
    const rtspURL = `rtsp://${ip}:${port}/${filePath}`

    // 计算裁剪起始时间相对于录像文件开始时间的偏移（秒）
    const cutStartTs = new Date(startTime.replace('T', ' ')).getTime()
    const recordStartTs = new Date(recordStartTime.replace('T', ' ')).getTime()
    const range = Math.max(0, (cutStartTs - recordStartTs) / 1000)

    // 裁剪结束时间戳（秒）
    const cutEndTs = new Date(endTime.replace('T', ' ')).getTime() / 1000
    const cutStartSec = cutStartTs / 1000

    // 裁剪时长（秒）
    const cutDurationSec = (cutEndTs - cutStartSec)
    
    console.log(`[大华回放] 窗口${paneIndex + 1} 开始录像裁剪:`, {
      filePath,
      startTime,
      endTime,
      recordStartTime,
      range,
      cutStartSec,
      cutEndTs,
      cutDurationSec
    })

    // 创建任务状态
    const task: CutTask = {
      player: null,
      isCutting: false,
      progress: 0,
      channelName
    }
    cutTasks.value.set(paneIndex, task)

    // 记录裁剪开始的实际时间戳
    let cutActualStartTs = 0

    try {
      // @ts-ignore
      const player = new window.PlayerControl({
        wsURL,
        rtspURL,
        username,
        password,
        isPrivateProtocol: false,
        // @ts-ignore
        realm: window.RPC?.realm,
        speed: 16, // 16倍速拉流加速下载
        playback: true,
        isDownLoad: true, // 下载模式
        range: range // 从指定偏移开始
      })

      task.player = player

      // 文件播放结束
      player.on('FileOver', () => {
        console.log(`[大华回放] 窗口${paneIndex + 1} 录像文件播放结束`)
        if (task.isCutting) {
          player.startCut(false)
          task.isCutting = false
          task.progress = 100
          onProgress?.(100)
          onComplete?.()
          ElMessage.success(`${channelName} 录像裁剪完成，文件已下载`)
        }
        // 清理
        stopRecordCut(paneIndex)
      })

      // 时间戳更新（用于判断何时开始/停止裁剪）
      player.on('UpdateTimeStamp', (e: any) => {
        const currentTs = e.timestamp // 当前播放时间戳（秒）

        // 到达起始时间，开始录制
        if (currentTs >= cutStartSec && !task.isCutting) {
          console.log(`[大华回放] 窗口${paneIndex + 1} 到达起始时间 ${cutStartSec}，开始录制，当前时间戳: ${currentTs}`)
          player.startCut(true)
          task.isCutting = true
          cutActualStartTs = currentTs // 记录实际开始时间
        }

        // 计算进度（基于裁剪开始后的实际时长）
        if (task.isCutting && cutActualStartTs > 0) {
          const elapsed = currentTs - cutActualStartTs // 已裁剪时长
          const progress = Math.min(100, Math.max(0, Math.floor((elapsed / cutDurationSec) * 100)))
          task.progress = progress
          onProgress?.(progress)
        }

        // 到达结束时间，停止录制
        if (currentTs >= cutEndTs && task.isCutting) {
          console.log(`[大华回放] 窗口${paneIndex + 1} 到达结束时间 ${cutEndTs}，停止录制，当前时间戳: ${currentTs}`)
          player.startCut(false)
          task.isCutting = false
          task.progress = 100
          onProgress?.(100)
          onComplete?.()
          ElMessage.success(`${channelName} 录像裁剪完成，文件已下载`)
          // 清理
          stopRecordCut(paneIndex)
        }
      })

      player.on('Error', (e: any) => {
        console.error(`[大华回放] 窗口${paneIndex + 1} 裁剪出错:`, e)
        ElMessage.error(`${channelName} 裁剪失败: ${e?.description || e?.errorCode || '未知错误'}`)
        stopRecordCut(paneIndex)
      })

      // 获取容器（每个窗口独立的容器）
      const containerId = `dahua-cut-container-${paneIndex}`
      let container = document.getElementById(containerId)
      if (!container) {
        container = document.createElement('div')
        container.id = containerId
        container.style.cssText = 'position:fixed;width:1px;height:1px;overflow:hidden;opacity:0;pointer-events:none;left:-9999px;'
        document.body.appendChild(container)
      }
      container.innerHTML = ''

      const canvas = document.createElement('canvas')
      canvas.width = 320
      canvas.height = 180
      container.appendChild(canvas)

      const video = document.createElement('video')
      video.style.display = 'none'
      container.appendChild(video)

      // 初始化并连接
      player.init(canvas, video)
      player.connect(true)
      
      ElMessage.info(`${channelName} 开始裁剪: ${startTime} ~ ${endTime}`)
      return true
    } catch (e: any) {
      console.error(`[大华回放] 窗口${paneIndex + 1} 创建裁剪播放器失败:`, e)
      ElMessage.error(`${channelName} 创建裁剪任务失败: ` + (e?.message || '未知错误'))
      cutTasks.value.delete(paneIndex)
      return false
    }
  }

  /**
   * 停止指定窗口的录像裁剪
   */
  const stopRecordCut = (paneIndex?: number): void => {
    if (paneIndex !== undefined) {
      // 停止指定窗口
      const task = cutTasks.value.get(paneIndex)
      if (task?.player) {
        try {
          if (task.isCutting) {
            task.player.startCut(false, true) // 取消并不保存
          }
          task.player.stop?.()
          task.player.close?.()
        } catch (e) {
          console.warn(`[大华回放] 窗口${paneIndex + 1} 停止裁剪异常:`, e)
        }
      }
      cutTasks.value.delete(paneIndex)
    } else {
      // 停止所有裁剪任务
      for (const [idx, task] of cutTasks.value.entries()) {
        if (task?.player) {
          try {
            if (task.isCutting) {
              task.player.startCut(false, true)
            }
            task.player.stop?.()
            task.player.close?.()
          } catch (e) {
            console.warn(`[大华回放] 窗口${idx + 1} 停止裁剪异常:`, e)
          }
        }
      }
      cutTasks.value.clear()
    }
  }

  return {
    recordsLoading,
    queryChannelRecords,
    queryMultipleChannelRecords,
    startPlayback,
    stopPlayback,
    pausePlayback,
    resumePlayback,
    setSpeed,
    seekTo,
    seekRelative,
    getCurrentTime,
    downloadSnapshot,
    // 录像裁剪下载（支持多通道）
    startRecordCut,
    stopRecordCut,
    getCutTaskState,
    isCutting,
    cutProgress,
    cutTasks
  }
}

/**
 * 创建空的回放窗格
 */
export function createEmptyPlaybackPane(): PlaybackPane {
  return {
    channel: null,
    streamInfo: null,
    jessibucaPlayer: null,
    videoEl: null,
    isPlaying: false,
    isLoading: false,
    isPaused: false,
    error: null,
    muted: true,
    channelNo: null,
    playbackStartTime: null,
    playbackEndTime: null,
    currentPlaySeconds: 0,
    recordFiles: [],
    hasRecording: false
  }
}

/**
 * 创建多个回放窗格
 */
export function createPlaybackPanes(count: number): PlaybackPane[] {
  return Array.from({ length: count }, () => createEmptyPlaybackPane())
}
