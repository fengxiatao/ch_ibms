import { ref } from 'vue'

/**
 * IoT 实时消息类型
 */
export interface IotMessage {
  type: 'device_status' | 'alarm_event' | 'device_stats' | 'snapshot_update' | 'device_discovered' | 'recording_event' | 'START_PATROL' | 'STOP_PATROL' | 'connected' | 'pong'  // ✅ 添加轮巡消息类型
  data?: any
  timestamp?: number
}

/**
 * 设备状态更新消息
 */
export interface DeviceStatusMessage {
  deviceId: number
  deviceName: string
  status: 'online' | 'offline'
  timestamp: number
}

/**
 * 告警事件消息
 */
export interface AlarmEventMessage {
  id: number
  type: 'personnel' | 'device' | 'behavior' | 'fire' | 'intrusion'
  level: 'high' | 'warning' | 'info'
  title: string
  content?: string
  deviceId?: number
  deviceName?: string
  location?: string
  timestamp: number
  time?: string  // 格式化后的相对时间（如："23分钟前"）
}

/**
 * 设备统计数据消息
 */
export interface DeviceStatsMessage {
  online: number
  offline: number
  alarm: number
  total: number
  rate: number
}

/**
 * 事件回调类型
 */
type EventCallback<T = any> = (data: T) => void

/**
 * ✅ 全局单例WebSocket管理器
 * 
 * 解决多个组件重复创建连接的问题：
 * - 所有组件共享同一个WebSocket连接
 * - 支持多个组件订阅同一个事件
 * - 组件卸载时自动取消订阅
 */
class IotWebSocketManager {
  private ws: WebSocket | null = null
  private isConnected = ref(false)
  private reconnectTimer: number | undefined
  private heartbeatTimer: number | undefined
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private currentUserId: number | null = null
  private subscribers = 0 // 订阅者计数
  
  // 事件监听器映射（支持多个回调）
  private listeners: {
    deviceStatus: Set<EventCallback<DeviceStatusMessage>>
    alarmEvent: Set<EventCallback<AlarmEventMessage>>
    deviceStats: Set<EventCallback<DeviceStatsMessage>>
    snapshotUpdate: Set<EventCallback<any>>
    deviceDiscovered: Set<EventCallback<any>>  // ✅ 添加设备发现事件
    recordingEvent: Set<EventCallback<any>>   // ✅ 添加录像事件
    startPatrol: Set<EventCallback<any>>      // ✅ 添加启动轮巡事件
    stopPatrol: Set<EventCallback<any>>       // ✅ 添加停止轮巡事件
    connected: Set<EventCallback<void>>
    disconnected: Set<EventCallback<void>>
    error: Set<EventCallback<Event>>
  } = {
    deviceStatus: new Set(),
    alarmEvent: new Set(),
    deviceStats: new Set(),
    snapshotUpdate: new Set(),
    deviceDiscovered: new Set(),  // ✅ 初始化
    recordingEvent: new Set(),    // ✅ 初始化
    startPatrol: new Set(),       // ✅ 初始化
    stopPatrol: new Set(),        // ✅ 初始化
    connected: new Set(),
    disconnected: new Set(),
    error: new Set()
  }

  // WebSocket URL和配置（动态生成，避免硬编码端口导致部署/代理环境无法连接）
  private heartbeatInterval = 30000
  private autoReconnect = true

  /**
   * 构建 WebSocket 基础地址（不包含 userId 参数）
   *
   * 优先级：
   * 1) VITE_IOT_WS_URL（完整 ws/wss URL，可包含查询参数）
   * 2) VITE_IOT_WS_HOST / VITE_BASE_URL / window.location.host + VITE_IOT_WS_PATH(/ws/iot)
   */
  private getWsBaseUrl(): string {
    const env = (import.meta as any).env || {}
    const fullWsUrl = env?.VITE_IOT_WS_URL as string | undefined
    if (fullWsUrl && /^wss?:\/\//i.test(fullWsUrl)) {
      return fullWsUrl
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const envHost = env?.VITE_IOT_WS_HOST as string | undefined
    const baseUrl = env?.VITE_BASE_URL as string | undefined
    let baseHost = ''
    try {
      baseHost = baseUrl ? new URL(baseUrl).host : ''
    } catch {
      // ignore
    }
    const host = envHost || baseHost || window.location.host
    const path = (env?.VITE_IOT_WS_PATH as string | undefined) || '/ws/iot'
    return `${protocol}//${host}${path}`
  }

  /**
   * 订阅WebSocket连接
   */
  subscribe(userId: number) {
    this.subscribers++
    console.log(`[IoT WebSocket Manager] 📝 新订阅者，当前订阅数: ${this.subscribers}`)

    // 如果已经连接且连接状态正常，直接返回
    if (this.ws?.readyState === WebSocket.OPEN && this.currentUserId === userId) {
      console.log('[IoT WebSocket Manager] ✅ 使用已有连接')
      return
    }

    // 如果正在连接中，等待连接完成
    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.log('[IoT WebSocket Manager] ⏳ 正在连接中，等待连接完成...')
      return
    }

    // 如果没有连接或连接已关闭，创建新连接
    if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
      this.connect(userId)
    }
  }

  /**
   * 取消订阅
   */
  unsubscribe() {
    this.subscribers = Math.max(0, this.subscribers - 1)
    console.log(`[IoT WebSocket Manager] 📝 订阅者离开，当前订阅数: ${this.subscribers}`)

    // 如果没有订阅者了，延迟断开连接（避免页面切换时频繁断开）
    if (this.subscribers === 0) {
      console.log('[IoT WebSocket Manager] 🔌 5秒后没有新订阅者将断开连接')
      setTimeout(() => {
        if (this.subscribers === 0) {
          console.log('[IoT WebSocket Manager] 🔌 确认断开连接')
          this.disconnect()
        }
      }, 5000)
    }
  }

  /**
   * 连接WebSocket
   */
  private connect(userId: number) {
    // 检查连接状态，避免重复连接
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.warn('[IoT WebSocket Manager] 已经连接，跳过重复连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.warn('[IoT WebSocket Manager] 正在连接中，跳过重复连接')
      return
    }

    // 如果有旧连接（CLOSING 状态），先完全关闭
    if (this.ws && this.ws.readyState === WebSocket.CLOSING) {
      console.log('[IoT WebSocket Manager] ⏳ 等待旧连接完全关闭...')
      // 强制关闭并清理
      try {
        this.ws.close()
      } catch (e) {
        // 忽略关闭错误
      }
      this.ws = null
      this.stopHeartbeat()
    }

    this.currentUserId = userId

    try {
      const base = this.getWsBaseUrl()
      const sep = base.includes('?') ? '&' : '?'
      const wsUrl = `${base}${sep}userId=${userId}`
      console.log('[IoT WebSocket Manager] 🔗 正在连接:', wsUrl)

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('[IoT WebSocket Manager] ✅ 连接成功')
        this.isConnected.value = true
        this.reconnectAttempts = 0
        this.notifyListeners('connected')
        this.startHeartbeat()
      }

      this.ws.onmessage = (event) => {
        try {
          const message: IotMessage = JSON.parse(event.data)
          console.log('[IoT WebSocket Manager] 📨 收到消息:', message.type)

          switch (message.type) {
            case 'connected':
              console.log('[IoT WebSocket Manager] 🤝 服务器确认连接')
              break

            case 'pong':
              // 心跳响应
              break

            case 'device_status':
              this.notifyListeners('deviceStatus', message.data)
              break

            case 'alarm_event':
              this.notifyListeners('alarmEvent', message.data)
              break

            case 'device_stats':
              this.notifyListeners('deviceStats', message.data)
              break

            case 'snapshot_update':
              this.notifyListeners('snapshotUpdate', message.data)
              break

            case 'device_discovered':  // ✅ 添加设备发现事件处理
              this.notifyListeners('deviceDiscovered', message.data)
              break

            case 'recording_event':   // ✅ 录像事件
              this.notifyListeners('recordingEvent', message.data)
              break

            case 'START_PATROL':      // ✅ 启动轮巡
              this.notifyListeners('startPatrol', message.data)
              break

            case 'STOP_PATROL':       // ✅ 停止轮巡
              this.notifyListeners('stopPatrol', message.data)
              break

            default:
              console.warn('[IoT WebSocket Manager] ⚠️ 未知消息类型:', message.type)
          }
        } catch (error) {
          console.error('[IoT WebSocket Manager] ❌ 消息解析失败:', error)
        }
      }

      this.ws.onerror = (event) => {
        console.error('[IoT WebSocket Manager] ❌ 连接错误:', event)
        this.isConnected.value = false
        this.notifyListeners('error', event)
      }

      this.ws.onclose = (event) => {
        console.log('[IoT WebSocket Manager] 🔌 连接关闭, code:', event.code, ', reason:', event.reason)
        this.isConnected.value = false
        this.stopHeartbeat()
        this.notifyListeners('disconnected')

        // 如果是正常关闭（用户主动断开），不重连
        if (event.code === 1000 && event.reason === 'User disconnected') {
          console.log('[IoT WebSocket Manager] 用户主动断开，不重连')
          return
        }

        // 如果是因为新连接建立而关闭旧连接，不重连（避免无限循环）
        if (event.code === 1000 && event.reason === 'New connection established') {
          console.log('[IoT WebSocket Manager] ℹ️ 服务器因新连接建立而关闭旧连接，不进行重连')
          // 清理状态，但不触发重连
          this.ws = null
          this.reconnectAttempts = 0
          return
        }

        // 只有在有订阅者时才自动重连
        if (this.autoReconnect && this.subscribers > 0 && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          // 使用指数退避算法，最小2秒，最大30秒
          const delay = Math.min(2000 * Math.pow(2, this.reconnectAttempts - 1), 30000)
          console.log(
            `[IoT WebSocket Manager] 🔄 ${delay / 1000}秒后尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`
          )
          this.reconnectTimer = window.setTimeout(() => {
            if (this.currentUserId !== null && this.subscribers > 0) {
              this.connect(this.currentUserId)
            }
          }, delay)
        } else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
          console.error('[IoT WebSocket Manager] ❌ 达到最大重连次数，停止重连')
          // ✅ 临时屏蔽错误通知（WebSocket 服务未启动时避免频繁提示）
          // ElNotification({
          //   title: 'IoT 实时通知连接失败',
          //   message: '实时设备状态更新功能不可用，请刷新页面重试',
          //   type: 'error',
          //   duration: 0
          // })
        } else if (this.subscribers === 0) {
          console.log('[IoT WebSocket Manager] 没有订阅者，不重连')
        }
      }
    } catch (error) {
      console.error('[IoT WebSocket Manager] ❌ 创建连接失败:', error)
      this.isConnected.value = false
    }
  }

  /**
   * 断开连接
   */
  private disconnect() {
    console.log('[IoT WebSocket Manager] 🔌 主动断开连接')

    // 取消重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = undefined
    }

    // 停止心跳
    this.stopHeartbeat()

    // 关闭连接（使用特殊的关闭码和原因，避免触发重连）
    if (this.ws && this.ws.readyState !== WebSocket.CLOSED) {
      try {
        this.ws.close(1000, 'User disconnected')
      } catch (e) {
        console.error('[IoT WebSocket Manager] 关闭连接失败:', e)
      }
      this.ws = null
    }

    this.isConnected.value = false
    this.reconnectAttempts = 0
    this.currentUserId = null
  }

  /**
   * 发送消息
   */
  send(message: string | object) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const data = typeof message === 'string' ? message : JSON.stringify(message)
      this.ws.send(data)
      console.log('[IoT WebSocket Manager] 📤 发送消息:', message)
    } else {
      console.warn('[IoT WebSocket Manager] ⚠️ 连接未打开，无法发送消息')
    }
  }

  /**
   * 启动心跳
   */
  private startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = undefined
    }
  }

  /**
   * 添加事件监听器
   */
  on<T = any>(event: keyof typeof this.listeners, callback: EventCallback<T>) {
    ;(this.listeners[event] as Set<EventCallback<T>>).add(callback)
  }

  /**
   * 移除事件监听器
   */
  off<T = any>(event: keyof typeof this.listeners, callback: EventCallback<T>) {
    ;(this.listeners[event] as Set<EventCallback<T>>).delete(callback)
  }

  /**
   * 通知所有监听器
   */
  private notifyListeners<T = any>(event: keyof typeof this.listeners, data?: T) {
    const callbacks = this.listeners[event] as Set<EventCallback<T>>
    callbacks.forEach(callback => {
      try {
        callback(data as T)
      } catch (error) {
        console.error(`[IoT WebSocket Manager] 回调执行错误 (${event}):`, error)
      }
    })
  }

  /**
   * 获取连接状态
   */
  getIsConnected() {
    return this.isConnected
  }
}

// ✅ 全局单例实例
const manager = new IotWebSocketManager()

/**
 * WebSocket 配置选项
 */
export interface IotWebSocketOptions {
  url?: string
  onDeviceStatus?: (data: DeviceStatusMessage) => void
  onAlarmEvent?: (data: AlarmEventMessage) => void
  onDeviceStats?: (data: DeviceStatsMessage) => void
  onSnapshotUpdate?: (data: any) => void
  onDeviceDiscovered?: (data: any) => void  // ✅ 添加设备发现回调
  onRecordingEvent?: (data: any) => void    // ✅ 添加录像事件回调
  onStartPatrol?: (data: any) => void       // ✅ 添加启动轮巡回调
  onStopPatrol?: (data: any) => void        // ✅ 添加停止轮巡回调
  onConnected?: () => void
  onDisconnected?: () => void
  onError?: (error: Event) => void
  heartbeatInterval?: number
  autoReconnect?: boolean
}

/**
 * ✅ IoT WebSocket Hook（全局单例版本）
 *
 * 所有组件共享同一个WebSocket连接，避免重复连接问题
 *
 * @example
 * const { connect, disconnect, isConnected, send } = useIotWebSocket({
 *   onDeviceStatus: (data) => {
 *     console.log('设备状态更新:', data)
 *   },
 *   onAlarmEvent: (data) => {
 *     ElNotification({ title: '新告警', message: data.title, type: 'warning' })
 *   },
 *   onDeviceStats: (data) => {
 *     deviceStats.value = data
 *   }
 * })
 *
 * onMounted(() => connect(userId))
 * onUnmounted(() => disconnect())
 */
export const useIotWebSocket = (options: IotWebSocketOptions = {}) => {
  const {
    onDeviceStatus,
    onAlarmEvent,
    onDeviceStats,
    onSnapshotUpdate,
    onDeviceDiscovered,  // ✅ 添加
    onRecordingEvent,
    onStartPatrol,       // ✅ 添加
    onStopPatrol,        // ✅ 添加
    onConnected,
    onDisconnected,
    onError
  } = options

  // 注册事件监听器
  if (onDeviceStatus) manager.on('deviceStatus', onDeviceStatus)
  if (onAlarmEvent) manager.on('alarmEvent', onAlarmEvent)
  if (onDeviceStats) manager.on('deviceStats', onDeviceStats)
  if (onSnapshotUpdate) manager.on('snapshotUpdate', onSnapshotUpdate)
  if (onDeviceDiscovered) manager.on('deviceDiscovered', onDeviceDiscovered)  // ✅ 添加
  if (onRecordingEvent) manager.on('recordingEvent', onRecordingEvent)        // ✅ 添加
  if (onStartPatrol) manager.on('startPatrol', onStartPatrol)                 // ✅ 添加
  if (onStopPatrol) manager.on('stopPatrol', onStopPatrol)                    // ✅ 添加
  if (onConnected) manager.on('connected', onConnected)
  if (onDisconnected) manager.on('disconnected', onDisconnected)
  if (onError) manager.on('error', onError)

  /**
   * 连接（订阅）
   */
  const connect = (userId: number) => {
    manager.subscribe(userId)
  }

  /**
   * 断开（取消订阅）
   */
  const disconnect = () => {
    // 移除事件监听器
    if (onDeviceStatus) manager.off('deviceStatus', onDeviceStatus)
    if (onAlarmEvent) manager.off('alarmEvent', onAlarmEvent)
    if (onDeviceStats) manager.off('deviceStats', onDeviceStats)
    if (onSnapshotUpdate) manager.off('snapshotUpdate', onSnapshotUpdate)
    if (onDeviceDiscovered) manager.off('deviceDiscovered', onDeviceDiscovered)  // ✅ 添加
    if (onRecordingEvent) manager.off('recordingEvent', onRecordingEvent)        // ✅ 添加
    if (onStartPatrol) manager.off('startPatrol', onStartPatrol)                 // ✅ 添加
    if (onStopPatrol) manager.off('stopPatrol', onStopPatrol)                    // ✅ 添加
    if (onConnected) manager.off('connected', onConnected)
    if (onDisconnected) manager.off('disconnected', onDisconnected)
    if (onError) manager.off('error', onError)

    // 取消订阅
    manager.unsubscribe()
  }

  /**
   * 发送消息
   */
  const send = (message: string | object) => {
    manager.send(message)
  }

  return {
    connect,
    disconnect,
    send,
    isConnected: manager.getIsConnected()
  }
}

