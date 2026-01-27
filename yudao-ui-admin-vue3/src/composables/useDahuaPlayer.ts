/**
 * 大华无插件播放器 Composable
 * 统一实时预览和录像回放的播放逻辑
 * 
 * 依赖：
 * - 在 index.html 中引入 <script src="/dahua/PlayerControl.js"></script>
 * - 大华 SDK 文件放在 public/dahua/ 目录下
 */

import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

// ==================== 类型定义 ====================

/** 大华播放器配置 */
export interface DahuaPlayerConfig {
  /** NVR/IPC 设备 IP */
  ip: string
  /** 设备 HTTP 端口，默认 80（用于 WebSocket） */
  port: number
  /** RTSP 端口，默认 554 */
  rtspPort?: number
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** NVR 通道号（1-16） */
  channelNo: number
  /** 码流类型：0=主码流，1=辅码流 */
  subtype?: number
}

/** 回放配置 */
export interface DahuaPlaybackConfig extends DahuaPlayerConfig {
  /** 录像文件路径（从录像查询结果获取） */
  filePath?: string
  /** 回放开始时间 */
  startTime?: string
  /** 回放结束时间 */
  endTime?: string
  /** 回放速度倍率 */
  speed?: number
}

/** 播放窗格状态 */
export interface DahuaPlayerPane {
  /** 窗格索引 */
  index: number
  /** 播放器实例 */
  player: any | null
  /** 录像播放器实例（用于本地录像） */
  recordPlayer: any | null
  /** canvas 元素 */
  canvas: HTMLCanvasElement | null
  /** video 元素 */
  video: HTMLVideoElement | null
  /** 播放器容器元素 */
  container: HTMLElement | null
  /** 当前配置 */
  config: DahuaPlayerConfig | null
  /** 通道名称 */
  channelName: string
  /** 是否正在播放 */
  isPlaying: boolean
  /** 是否正在加载 */
  isLoading: boolean
  /** 是否暂停 */
  isPaused: boolean
  /** 是否回放模式 */
  isPlayback: boolean
  /** 是否正在录像 */
  isRecording: boolean
  /** 是否静音 */
  muted: boolean
  /** 错误信息 */
  error: string | null
  /** 码流类型 */
  streamType: 'main' | 'sub'
}

/** 录像文件信息 */
export interface DahuaRecordFile {
  /** 文件路径 */
  FilePath: string
  /** 开始时间 */
  StartTime: string
  /** 结束时间 */
  EndTime: string
  /** 文件大小（字节） */
  Length: number
  /** 通道号 */
  Channel?: number
}

/** 云台方向命令 */
export type PtzDirection = 'Up' | 'Down' | 'Left' | 'Right' | 'LeftUp' | 'RightUp' | 'LeftDown' | 'RightDown' | 'Auto'

/** 云台变焦命令 */
export type PtzZoom = 'ZoomTele' | 'ZoomWide' | 'FocusNear' | 'FocusFar' | 'IrisLarge' | 'IrisSmall'

// ==================== 默认配置 ====================

/** NVR 默认配置 */
export const DEFAULT_NVR_CONFIG = {
  ip: '192.168.1.200',
  port: 80,
  rtspPort: 554,  // RTSP 默认端口
  username: 'admin',
  password: 'admin123'
}

// ==================== Composable ====================

/**
 * 大华播放器 Composable
 */
export function useDahuaPlayer() {
  // 播放器实例映射
  const playerInstances = ref<Map<number, any>>(new Map())
  
  // RPC 登录状态
  const isLoggedIn = ref(false)
  const loginTarget = ref('')

  /**
   * 检查 SDK 是否加载
   */
  const checkSDK = (): boolean => {
    // @ts-ignore
    return typeof window.PlayerControl !== 'undefined'
  }

  /**
   * 登录设备（用于云台控制、录像查询等 RPC 操作）
   */
  const loginDevice = async (config: Pick<DahuaPlayerConfig, 'ip' | 'port' | 'username' | 'password'>): Promise<boolean> => {
    const target = `${config.ip}:${config.port}`
    
    // 如果已登录同一设备，跳过
    if (isLoggedIn.value && loginTarget.value === target) {
      return true
    }

    try {
      // @ts-ignore
      if (typeof window.RPC === 'undefined') {
        console.warn('[大华] RPC 未加载，跳过登录')
        return false
      }

      // @ts-ignore
      window.setIP(target)
      
      // @ts-ignore
      await window.RPC.login(config.username, config.password, false)
      
      isLoggedIn.value = true
      loginTarget.value = target
      
      // 启动保活
      // @ts-ignore
      window.RPC.keepAlive(300, 60000, window._getSession?.(), target)
      
      console.log('[大华] 设备登录成功:', target)
      return true
    } catch (e: any) {
      console.error('[大华] 设备登录失败:', e)
      isLoggedIn.value = false
      return false
    }
  }

  /**
   * 创建空的播放窗格
   * 注意：默认使用子码流(sub)以获得更好的实时性和快速响应
   */
  const createEmptyPane = (index: number): DahuaPlayerPane => ({
    index,
    player: null,
    recordPlayer: null,
    canvas: null,
    video: null,
    container: null,
    config: null,
    channelName: '',
    isPlaying: false,
    isLoading: false,
    isPaused: false,
    isPlayback: false,
    isRecording: false,
    muted: true,
    error: null,
    streamType: 'sub'  // 默认子码流，实时性更好
  })

  /**
   * 创建多个播放窗格
   */
  const createPanes = (count: number): DahuaPlayerPane[] => {
    return Array.from({ length: count }, (_, i) => createEmptyPane(i))
  }

  /**
   * 准备播放器容器（创建 canvas 和 video 元素）
   */
  const prepareContainer = (container: HTMLElement): { canvas: HTMLCanvasElement; video: HTMLVideoElement } => {
    // 清空容器
    container.innerHTML = ''
    
    // 创建 canvas（软解码）
    const canvas = document.createElement('canvas')
    canvas.style.width = '100%'
    canvas.style.height = '100%'
    container.appendChild(canvas)
    
    // 创建 video（硬解码，H.265 加速）
    const video = document.createElement('video')
    video.style.width = '100%'
    video.style.height = '100%'
    video.style.display = 'none'
    video.style.position = 'absolute'
    video.style.top = '0'
    video.style.left = '0'
    video.muted = true
    video.autoplay = true
    video.playsInline = true
    container.appendChild(video)
    
    return { canvas, video }
  }

  /**
   * 开始实时预览
   */
  const startPreview = async (
    pane: DahuaPlayerPane,
    config: DahuaPlayerConfig,
    channelName?: string
  ): Promise<boolean> => {
    if (!checkSDK()) {
      pane.error = '大华播放器 SDK 未加载'
      ElMessage.error('大华播放器 SDK 未加载，请刷新页面')
      return false
    }

    if (!pane.container) {
      pane.error = '播放器容器不存在'
      return false
    }

    // 如果正在播放，先停止
    if (pane.isPlaying || pane.player) {
      await stopPlayer(pane)
    }

    pane.isLoading = true
    pane.error = null
    pane.config = config
    pane.channelName = channelName || `通道${config.channelNo}`
    pane.isPlayback = false

    try {
      // 准备容器
      const { canvas, video } = prepareContainer(pane.container)
      pane.canvas = canvas
      pane.video = video

      // 构建 URL
      const wsScheme = location.protocol === 'https:' ? 'wss' : 'ws'
      const wsURL = `${wsScheme}://${config.ip}:${config.port}/rtspoverwebsocket`
      const subtype = config.subtype ?? (pane.streamType === 'sub' ? 1 : 0)
      const rtspPort = config.rtspPort || 554  // RTSP 默认端口 554
      const rtspURL = `rtsp://${config.ip}:${rtspPort}/cam/realmonitor?channel=${config.channelNo}&subtype=${subtype}&proto=Private3`

      console.log(`[大华] 窗格${pane.index + 1} 开始预览:`, {
        wsURL,
        rtspURL,
        channel: config.channelNo,
        subtype
      })

      // 创建播放器
      // @ts-ignore
      const player = new window.PlayerControl({
        wsURL,
        rtspURL,
        username: config.username,
        password: config.password,
        lessRateCanvas: true,
        playback: false,
        isPrivateProtocol: false,
        wndIndex: pane.index
      })

      // 事件监听
      player.on('WorkerReady', () => {
        console.log(`[大华] 窗格${pane.index + 1} Worker 就绪`)
        player.connect()
      })

      player.on('DecodeStart', (e: any) => {
        console.log(`[大华] 窗格${pane.index + 1} 开始解码:`, e)
        if (e.decodeMode === 'video') {
          canvas.style.display = 'none'
          video.style.display = 'block'
        } else {
          video.style.display = 'none'
          canvas.style.display = 'block'
        }
      })

      player.on('PlayStart', () => {
        console.log(`[大华] 窗格${pane.index + 1} 播放开始`)
        nextTick(() => {
          pane.isLoading = false
          pane.isPlaying = true
          pane.error = null
        })
      })

      player.on('Error', (e: any) => {
        console.error(`[大华] 窗格${pane.index + 1} 错误:`, e)
        nextTick(() => {
          pane.isLoading = false
          if (!pane.isPlaying) {
            pane.error = e?.description || e?.message || `错误码: ${e?.errorCode || 'unknown'}`
          }
        })
      })

      player.on('MSEResolutionChanged', (e: any) => {
        console.log(`[大华] 窗格${pane.index + 1} 分辨率变化:`, e)
      })

      // 初始化
      player.init(canvas, video)
      
      // 保存实例
      pane.player = player
      playerInstances.value.set(pane.index, player)

      // 超时检测
      setTimeout(() => {
        if (pane.isLoading && !pane.isPlaying) {
          console.warn(`[大华] 窗格${pane.index + 1} 连接超时`)
          pane.isLoading = false
          pane.error = '连接超时，请检查设备网络'
        }
      }, 15000)

      return true
    } catch (e: any) {
      console.error(`[大华] 窗格${pane.index + 1} 创建播放器失败:`, e)
      pane.isLoading = false
      pane.error = e?.message || '创建播放器失败'
      return false
    }
  }

  /**
   * 开始录像回放
   */
  const startPlayback = async (
    pane: DahuaPlayerPane,
    config: DahuaPlaybackConfig,
    channelName?: string
  ): Promise<boolean> => {
    if (!checkSDK()) {
      pane.error = '大华播放器 SDK 未加载'
      ElMessage.error('大华播放器 SDK 未加载，请刷新页面')
      return false
    }

    if (!pane.container) {
      pane.error = '播放器容器不存在'
      return false
    }

    if (!config.filePath) {
      pane.error = '未指定录像文件'
      return false
    }

    // 如果正在播放，先停止
    if (pane.isPlaying || pane.player) {
      await stopPlayer(pane)
    }

    pane.isLoading = true
    pane.error = null
    pane.config = config
    pane.channelName = channelName || `通道${config.channelNo}`
    pane.isPlayback = true
    pane.isPaused = false

    try {
      // 准备容器
      const { canvas, video } = prepareContainer(pane.container)
      pane.canvas = canvas
      pane.video = video

      // 构建 URL
      const wsScheme = location.protocol === 'https:' ? 'wss' : 'ws'
      const wsURL = `${wsScheme}://${config.ip}:${config.port}/rtspoverwebsocket`
      const rtspPort = config.rtspPort || 554  // RTSP 默认端口 554
      const rtspURL = `rtsp://${config.ip}:${rtspPort}/${config.filePath}`

      console.log(`[大华] 窗格${pane.index + 1} 开始回放:`, {
        wsURL,
        rtspURL,
        filePath: config.filePath
      })

      // 创建播放器
      // @ts-ignore
      const player = new window.PlayerControl({
        wsURL,
        rtspURL,
        username: config.username,
        password: config.password,
        lessRateCanvas: true,
        playback: true,
        isPrivateProtocol: false,
        speed: config.speed || 1,
        wndIndex: pane.index
      })

      // 事件监听
      player.on('WorkerReady', () => {
        console.log(`[大华] 窗格${pane.index + 1} Worker 就绪`)
        player.connect()
      })

      player.on('DecodeStart', (e: any) => {
        console.log(`[大华] 窗格${pane.index + 1} 开始解码:`, e)
        if (e.decodeMode === 'video') {
          canvas.style.display = 'none'
          video.style.display = 'block'
        } else {
          video.style.display = 'none'
          canvas.style.display = 'block'
        }
      })

      player.on('PlayStart', () => {
        console.log(`[大华] 窗格${pane.index + 1} 回放开始`)
        nextTick(() => {
          pane.isLoading = false
          pane.isPlaying = true
          pane.error = null
        })
      })

      player.on('Error', (e: any) => {
        console.error(`[大华] 窗格${pane.index + 1} 回放错误:`, e)
        nextTick(() => {
          pane.isLoading = false
          if (!pane.isPlaying) {
            pane.error = e?.description || e?.message || `错误码: ${e?.errorCode || 'unknown'}`
          }
        })
      })

      player.on('FileOver', () => {
        console.log(`[大华] 窗格${pane.index + 1} 录像播放完毕`)
        nextTick(() => {
          pane.isPlaying = false
        })
      })

      player.on('UpdateCanvas', (e: any) => {
        // 可以用于更新时间轴进度
        // e.timestamp 是当前播放的时间戳（秒）
      })

      player.on('GetTotalTime', (e: any) => {
        // 录像总时长（秒）
        console.log(`[大华] 窗格${pane.index + 1} 录像总时长:`, e)
      })

      // 初始化
      player.init(canvas, video)
      
      // 保存实例
      pane.player = player
      playerInstances.value.set(pane.index, player)

      // 超时检测
      setTimeout(() => {
        if (pane.isLoading && !pane.isPlaying) {
          console.warn(`[大华] 窗格${pane.index + 1} 回放连接超时`)
          pane.isLoading = false
          pane.error = '连接超时，请检查设备网络'
        }
      }, 15000)

      return true
    } catch (e: any) {
      console.error(`[大华] 窗格${pane.index + 1} 创建回放播放器失败:`, e)
      pane.isLoading = false
      pane.error = e?.message || '创建回放播放器失败'
      return false
    }
  }

  /**
   * 停止播放器
   */
  const stopPlayer = async (pane: DahuaPlayerPane): Promise<void> => {
    // 如果正在录像，先停止录像
    if (pane.isRecording && pane.recordPlayer) {
      try {
        pane.recordPlayer.startRecord(false)
        pane.recordPlayer = null
        pane.isRecording = false
        console.log(`[大华] 窗格${pane.index + 1} 录像已自动停止`)
      } catch (e) {
        console.warn(`[大华] 窗格${pane.index + 1} 停止录像异常:`, e)
      }
    }

    if (pane.player) {
      try {
        pane.player.stop?.()
        pane.player.close?.()
      } catch (e) {
        console.warn(`[大华] 窗格${pane.index + 1} 停止异常:`, e)
      }
      pane.player = null
      playerInstances.value.delete(pane.index)
    }

    // 清理容器
    if (pane.container) {
      pane.container.innerHTML = ''
    }

    // 重置状态
    pane.canvas = null
    pane.video = null
    pane.isPlaying = false
    pane.isLoading = false
    pane.isPaused = false
    pane.isRecording = false
    pane.recordPlayer = null
    pane.error = null
  }

  /**
   * 停止所有播放器
   */
  const stopAllPlayers = async (panes: DahuaPlayerPane[]): Promise<void> => {
    for (const pane of panes) {
      await stopPlayer(pane)
    }
  }

  /**
   * 暂停播放（仅回放模式）
   */
  const pausePlayback = (pane: DahuaPlayerPane): void => {
    if (pane.player && pane.isPlayback && pane.isPlaying) {
      pane.player.pause?.()
      pane.isPaused = true
      console.log(`[大华] 窗格${pane.index + 1} 已暂停`)
    }
  }

  /**
   * 恢复播放（仅回放模式）
   */
  const resumePlayback = (pane: DahuaPlayerPane): void => {
    if (pane.player && pane.isPlayback && pane.isPaused) {
      pane.player.play?.()
      pane.isPaused = false
      console.log(`[大华] 窗格${pane.index + 1} 已恢复播放`)
    }
  }

  /**
   * 设置回放速度
   */
  const setPlaybackSpeed = (pane: DahuaPlayerPane, speed: number): void => {
    if (pane.player && pane.isPlayback) {
      pane.player.playFF?.(speed)
      console.log(`[大华] 窗格${pane.index + 1} 播放速度: ${speed}x`)
    }
  }

  /**
   * 跳转到指定时间（秒）
   */
  const seekTo = (pane: DahuaPlayerPane, seconds: number): void => {
    if (pane.player && pane.isPlayback) {
      pane.player.playByTime?.(seconds)
      console.log(`[大华] 窗格${pane.index + 1} 跳转到: ${seconds}s`)
    }
  }

  /**
   * 截图（仅保存到本地）
   */
  const capture = (pane: DahuaPlayerPane, fileName?: string): void => {
    if (pane.player && pane.isPlaying) {
      const name = fileName || `snapshot_${pane.channelName}_${Date.now()}`
      pane.player.capture?.(name)
      ElMessage.success('截图已保存到本地')
    } else {
      ElMessage.warning('当前窗口没有正在播放的视频')
    }
  }

  /**
   * 获取截图数据（Base64）
   * @param pane 播放窗格
   * @param type 图片类型：jpg/png
   * @param quality 图片质量：0-1.0
   * @returns Base64 字符串（带 data:image/xxx;base64, 前缀）
   */
  const getCapture = (pane: DahuaPlayerPane, type: 'jpg' | 'png' = 'jpg', quality: number = 1.0): string | null => {
    if (pane.player && pane.isPlaying) {
      return pane.player.getCapture?.(type, quality) || null
    }
    return null
  }

  /**
   * 截图并获取数据（用于上传）
   * @param pane 播放窗格
   * @param type 图片类型
   * @param quality 图片质量
   * @returns 包含 base64 数据和文件名的对象
   */
  const captureWithData = (pane: DahuaPlayerPane, type: 'jpg' | 'png' = 'jpg', quality: number = 1.0): { 
    base64: string | null
    fileName: string
    mimeType: string
  } => {
    const base64 = getCapture(pane, type, quality)
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)
    const channelName = pane.channelName || `channel_${pane.config?.channelNo || 'unknown'}`
    const fileName = `snapshot_${channelName}_${timestamp}.${type}`
    const mimeType = type === 'png' ? 'image/png' : 'image/jpeg'
    
    return { base64, fileName, mimeType }
  }

  /**
   * 将 Base64 转换为 File 对象
   */
  const base64ToFile = (base64: string, fileName: string, mimeType: string): File => {
    // 去掉 data:image/xxx;base64, 前缀
    const base64Data = base64.replace(/^data:image\/\w+;base64,/, '')
    const byteCharacters = atob(base64Data)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray], { type: mimeType })
    return new File([blob], fileName, { type: mimeType })
  }

  /**
   * 开始本地录像
   */
  const startRecord = async (pane: DahuaPlayerPane): Promise<boolean> => {
    if (!pane.isPlaying || !pane.config) {
      ElMessage.warning('当前窗口没有正在播放的视频')
      return false
    }

    if (pane.isRecording) {
      ElMessage.warning('当前窗口已在录像中')
      return false
    }

    if (!checkSDK()) {
      ElMessage.error('大华播放器 SDK 未加载')
      return false
    }

    try {
      const config = pane.config
      const wsScheme = location.protocol === 'https:' ? 'wss' : 'ws'
      const wsURL = `${wsScheme}://${config.ip}:${config.port}/rtspoverwebsocket`
      const subtype = config.subtype ?? (pane.streamType === 'sub' ? 1 : 0)
      const rtspPort = config.rtspPort || 554
      const rtspURL = `rtsp://${config.ip}:${rtspPort}/cam/realmonitor?channel=${config.channelNo}&subtype=${subtype}&proto=Private3`

      // 创建录像播放器实例
      // @ts-ignore
      const recordPlayer = new window.PlayerControl({
        wsURL,
        rtspURL,
        username: config.username,
        password: config.password,
        isPrivateProtocol: false
      })

      // 开始录像，带文件名配置
      const fileName = `${pane.channelName || 'record'}_${config.channelNo}`
      recordPlayer.startRecord(true, {
        nameOptions: {
          nameFormat: ['{fileName}_ymd_his', {
            fileName: fileName
          }]
        }
      })

      pane.recordPlayer = recordPlayer
      pane.isRecording = true

      console.log(`[大华] 窗格${pane.index + 1} 开始录像:`, fileName)
      ElMessage.success('开始录像')
      return true
    } catch (e: any) {
      console.error(`[大华] 窗格${pane.index + 1} 开始录像失败:`, e)
      ElMessage.error('开始录像失败: ' + (e?.message || '未知错误'))
      return false
    }
  }

  /**
   * 停止本地录像
   */
  const stopRecord = async (pane: DahuaPlayerPane): Promise<boolean> => {
    if (!pane.isRecording || !pane.recordPlayer) {
      ElMessage.warning('当前窗口没有在录像')
      return false
    }

    try {
      // 停止录像
      pane.recordPlayer.startRecord(false)
      pane.recordPlayer = null
      pane.isRecording = false

      console.log(`[大华] 窗格${pane.index + 1} 停止录像`)
      ElMessage.success('录像已保存')
      return true
    } catch (e: any) {
      console.error(`[大华] 窗格${pane.index + 1} 停止录像失败:`, e)
      ElMessage.error('停止录像失败: ' + (e?.message || '未知错误'))
      return false
    }
  }

  /**
   * 切换录像状态
   */
  const toggleRecord = async (pane: DahuaPlayerPane): Promise<boolean> => {
    if (pane.isRecording) {
      return await stopRecord(pane)
    } else {
      return await startRecord(pane)
    }
  }

  /**
   * 设置音量
   */
  const setVolume = (pane: DahuaPlayerPane, volume: number): void => {
    if (pane.player) {
      pane.player.setAudioVolume?.(volume)
      pane.muted = volume === 0
    }
  }

  /**
   * 静音/取消静音
   */
  const toggleMute = (pane: DahuaPlayerPane): void => {
    if (pane.player) {
      if (pane.muted) {
        pane.player.setAudioVolume?.(0.5)
        pane.muted = false
      } else {
        pane.player.setAudioVolume?.(0)
        pane.muted = true
      }
    }
  }

  /**
   * 切换码流
   */
  const switchStream = async (pane: DahuaPlayerPane, streamType: 'main' | 'sub'): Promise<boolean> => {
    if (!pane.config) return false
    
    pane.streamType = streamType
    const newConfig = {
      ...pane.config,
      subtype: streamType === 'sub' ? 1 : 0
    }
    
    return await startPreview(pane, newConfig, pane.channelName)
  }

  // ==================== 云台控制 ====================

  /**
   * 云台方向控制
   * @param channelNo NVR 通道号（0 开始，即界面显示的通道号减 1）
   * @param direction 方向
   * @param step 步长 1-8
   * @param isStop 是否停止
   */
  const ptzMove = async (
    channelNo: number,
    direction: PtzDirection,
    step: number = 5,
    isStop: boolean = false
  ): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') {
      console.warn('[大华] RPC 未加载，无法控制云台')
      return
    }

    const channel = channelNo - 1 // RPC 通道从 0 开始
    const arg2 = ['LeftUp', 'RightUp', 'LeftDown', 'RightDown'].includes(direction) ? step : 0

    try {
      // @ts-ignore
      await window.RPC.PTZManager(
        isStop ? 'stop' : 'start',
        channel,
        { code: direction, arg1: step, arg2, arg3: 0 }
      )
      console.log(`[大华] 云台控制: ${direction} ${isStop ? '停止' : '开始'}`)
    } catch (e) {
      console.error('[大华] 云台控制失败:', e)
    }
  }

  /**
   * 云台变焦控制
   */
  const ptzZoom = async (
    channelNo: number,
    command: PtzZoom,
    step: number = 5,
    isStop: boolean = false
  ): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') {
      console.warn('[大华] RPC 未加载，无法控制云台')
      return
    }

    const channel = channelNo - 1

    try {
      // @ts-ignore
      await window.RPC.PTZManager(
        isStop ? 'stop' : 'start',
        channel,
        { code: command, arg1: step, arg2: 0, arg3: 0 }
      )
      console.log(`[大华] 变焦控制: ${command} ${isStop ? '停止' : '开始'}`)
    } catch (e) {
      console.error('[大华] 变焦控制失败:', e)
    }
  }

  /**
   * 调用预设位
   */
  const gotoPreset = async (channelNo: number, presetId: number): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') return

    const channel = channelNo - 1
    try {
      // @ts-ignore
      await window.RPC.PTZManager('start', channel, {
        code: 'GotoPreset',
        arg1: presetId,
        arg2: 0,
        arg3: 0
      })
      ElMessage.success(`正在转到预设位 ${presetId}`)
    } catch (e) {
      console.error('[大华] 调用预设位失败:', e)
      ElMessage.error('调用预设位失败')
    }
  }

  /**
   * 设置预设位
   */
  const setPreset = async (channelNo: number, presetId: number): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') return

    const channel = channelNo - 1
    try {
      // @ts-ignore
      await window.RPC.PTZManager('start', channel, {
        code: 'SetPreset',
        arg1: presetId,
        arg2: 0,
        arg3: 0
      })
      ElMessage.success(`预设位 ${presetId} 已保存`)
    } catch (e) {
      console.error('[大华] 设置预设位失败:', e)
      ElMessage.error('设置预设位失败')
    }
  }

  /**
   * 删除预设位
   */
  const clearPreset = async (channelNo: number, presetId: number): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') return

    const channel = channelNo - 1
    try {
      // @ts-ignore
      await window.RPC.PTZManager('start', channel, {
        code: 'ClearPreset',
        arg1: presetId,
        arg2: 0,
        arg3: 0
      })
      ElMessage.success(`预设位 ${presetId} 已删除`)
    } catch (e) {
      console.error('[大华] 删除预设位失败:', e)
      ElMessage.error('删除预设位失败')
    }
  }

  /**
   * 3D定位/区域放大
   * 用户在视频画面上框选区域后，摄像头会调整到使该区域居中并放大
   * 
   * @param channelNo NVR 通道号（1开始）
   * @param startX 框选起始点 X（归一化坐标 0-8191）
   * @param startY 框选起始点 Y（归一化坐标 0-8191）
   * @param endX 框选结束点 X（归一化坐标 0-8191）
   * @param endY 框选结束点 Y（归一化坐标 0-8191）
   */
  const ptz3DPosition = async (
    channelNo: number,
    startX: number,
    startY: number,
    endX: number,
    endY: number
  ): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') {
      console.warn('[大华] RPC 未加载，无法执行3D定位')
      return
    }

    const channel = channelNo - 1 // RPC 通道从 0 开始

    try {
      // 大华 RPC 3D定位命令：
      // arg1: 起始点 X 坐标（0-8191）
      // arg2: 起始点 Y 坐标（0-8191）
      // arg3: 结束点 X 坐标（0-8191）
      // arg4: 结束点 Y 坐标（0-8191）
      // @ts-ignore
      await window.RPC.PTZManager('start', channel, {
        code: 'PositionABS', // 3D定位命令
        arg1: Math.round(startX),
        arg2: Math.round(startY),
        arg3: Math.round(endX),
        arg4: Math.round(endY)
      })
      console.log(`[大华] 3D定位: (${startX},${startY}) -> (${endX},${endY})`)
    } catch (e) {
      console.error('[大华] 3D定位失败:', e)
      throw e
    }
  }

  /**
   * 区域放大（简化版：使用 DirectionTour 3D 定位）
   * 
   * @param channelNo NVR 通道号（1开始）
   * @param rect 框选区域（归一化坐标 0-1）
   * @param containerWidth 容器宽度（像素）
   * @param containerHeight 容器高度（像素）
   */
  const areaZoom = async (
    channelNo: number,
    rect: { startX: number; startY: number; endX: number; endY: number },
    containerWidth: number,
    containerHeight: number
  ): Promise<void> => {
    // 将归一化坐标（0-1）转换为大华坐标（0-8191）
    const scale = 8191
    const startX = Math.round(rect.startX * scale)
    const startY = Math.round(rect.startY * scale)
    const endX = Math.round(rect.endX * scale)
    const endY = Math.round(rect.endY * scale)

    console.log(`[大华] 区域放大: 原始 (${rect.startX.toFixed(3)},${rect.startY.toFixed(3)}) -> (${rect.endX.toFixed(3)},${rect.endY.toFixed(3)})`)
    console.log(`[大华] 区域放大: 转换后 (${startX},${startY}) -> (${endX},${endY})`)

    await ptz3DPosition(channelNo, startX, startY, endX, endY)
  }

  /**
   * 云台复位（缩小到最小倍率）
   * 通过持续执行 ZoomWide（广角/缩小）命令使镜头恢复到最大视野
   * 
   * @param channelNo NVR 通道号（1开始）
   * @param duration 执行时长（毫秒），默认 3000ms
   */
  const ptzReset = async (channelNo: number, duration: number = 3000): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') {
      console.warn('[大华] RPC 未加载，无法执行复位')
      return
    }

    const channel = channelNo - 1

    try {
      // 开始缩小（ZoomWide = 广角）
      // @ts-ignore
      await window.RPC.PTZManager('start', channel, {
        code: 'ZoomWide',
        arg1: 8,  // 最大速度
        arg2: 0,
        arg3: 0
      })

      console.log(`[大华] 云台复位开始: channel=${channelNo}, duration=${duration}ms`)

      // 等待指定时间
      await new Promise(resolve => setTimeout(resolve, duration))

      // 停止缩小
      // @ts-ignore
      await window.RPC.PTZManager('stop', channel, {
        code: 'ZoomWide',
        arg1: 0,
        arg2: 0,
        arg3: 0
      })

      console.log(`[大华] 云台复位完成`)
    } catch (e) {
      console.error('[大华] 云台复位失败:', e)
      throw e
    }
  }

  /**
   * 调用 Home 位（预设点 0 或 1，通常是出厂默认位置）
   * 
   * @param channelNo NVR 通道号（1开始）
   */
  const ptzGoHome = async (channelNo: number): Promise<void> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') {
      console.warn('[大华] RPC 未加载，无法调用Home位')
      return
    }

    const channel = channelNo - 1

    try {
      // 尝试调用预设点 0（部分设备的 Home 位）
      // @ts-ignore
      await window.RPC.PTZManager('start', channel, {
        code: 'GotoPreset',
        arg1: 0,
        arg2: 0,
        arg3: 0
      })
      console.log(`[大华] 调用 Home 位: channel=${channelNo}`)
    } catch (e) {
      console.error('[大华] 调用 Home 位失败:', e)
      throw e
    }
  }

  // ==================== 录像查询 ====================

  /**
   * 查询录像文件
   */
  const queryRecords = async (
    channelNo: number,
    startTime: string,
    endTime: string
  ): Promise<DahuaRecordFile[]> => {
    // @ts-ignore
    if (typeof window.RPC === 'undefined') {
      console.warn('[大华] RPC 未加载，无法查询录像')
      return []
    }

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

      console.log(`[大华] 查询到 ${allRecords.length} 条录像`)
      return allRecords
    } catch (e) {
      console.error('[大华] 查询录像失败:', e)
      return []
    }
  }

  return {
    // 状态
    isLoggedIn,
    playerInstances,
    
    // 工具函数
    checkSDK,
    createEmptyPane,
    createPanes,
    
    // 设备登录
    loginDevice,
    
    // 播放控制
    startPreview,
    startPlayback,
    stopPlayer,
    stopAllPlayers,
    
    // 回放控制
    pausePlayback,
    resumePlayback,
    setPlaybackSpeed,
    seekTo,
    
    // 其他功能
    capture,
    getCapture,
    captureWithData,
    base64ToFile,
    setVolume,
    toggleMute,
    switchStream,
    
    // 本地录像
    startRecord,
    stopRecord,
    toggleRecord,
    
    // 云台控制
    ptzMove,
    ptzZoom,
    gotoPreset,
    setPreset,
    clearPreset,
    ptz3DPosition,
    areaZoom,
    ptzReset,
    ptzGoHome,
    
    // 录像查询
    queryRecords
  }
}

export default useDahuaPlayer
