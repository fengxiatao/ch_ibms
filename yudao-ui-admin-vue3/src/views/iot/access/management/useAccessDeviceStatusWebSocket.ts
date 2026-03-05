/**
 * 门禁设备状态 WebSocket Hook（单例模式）
 * 
 * 用于接收门禁设备在线状态的实时推送
 * 所有组件共享同一个 WebSocket 连接，避免重复连接问题
 * 
 * 端点：/ws/iot/access/device/status
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/user'

/** 门禁设备状态消息（后端：AccessDeviceStatusMessage） */
export interface AccessDeviceStatusMessage {
  deviceId: number
  deviceName?: string
  onlineStatus: number // 0-离线, 1-在线
  statusType?: string
  activationStatus?: number
}

/** 统一设备状态消息（后端：UnifiedDeviceStatusMessage） */
export interface UnifiedDeviceStatusMessage {
  deviceId: number
  deviceType: string
  status: string // ONLINE/OFFLINE/INACTIVE
  timestamp: number
  deviceName?: string
}

/** 门状态变化消息（后端：DOOR_STATE_CHANGE 事件） */
export interface DoorStateChangeMessage {
  deviceId: number
  deviceType: string
  eventType: string // DOOR_STATE_CHANGE
  eventData: {
    channelId: number
    channelNo: number
    doorStatus: number       // 0-关闭, 1-打开, 2-未知
    doorStatusDesc: string
    lockStatus: number       // 0-已锁, 1-已解锁, 2-未知
    lockStatusDesc: string
    alwaysMode: number       // 0-正常, 1-常开, 2-常闭
    alwaysModeDesc: string
    action: string           // 操作类型
    timestamp: number
  }
  timestamp: number
}

export interface UseAccessDeviceStatusWebSocketOptions {
  /** 是否自动连接 */
  autoConnect?: boolean
  /** 门禁设备状态变更回调 */
  onAccessDeviceStatus?: (data: AccessDeviceStatusMessage) => void
  /** 统一设备状态变更回调 */
  onUnifiedDeviceStatus?: (data: UnifiedDeviceStatusMessage) => void
  /** 门状态变化回调（门控操作后的实时状态更新） */
  onDoorStateChange?: (data: DoorStateChangeMessage) => void
  /** 连接状态变化回调 */
  onConnectionChange?: (connected: boolean) => void
  /** 错误回调 */
  onError?: (error: Error) => void
}

/** 事件回调类型 */
type EventCallback<T = any> = (data: T) => void

/**
 * ✅ 门禁设备状态 WebSocket 单例管理器
 */
class AccessDeviceStatusWebSocketManager {
  private ws: WebSocket | null = null
  private isConnected = ref(false)
  private reconnectTimer: ReturnType<typeof setTimeout> | undefined
  private heartbeatTimer: ReturnType<typeof setInterval> | undefined
  private reconnectAttempts = 0
  private maxReconnectAttempts = 10
  private heartbeatInterval = 30000
  private reconnectInterval = 5000
  private currentUserId: number | null = null
  private subscribers = 0

  // 事件监听器映射
  private listeners: {
    accessDeviceStatus: Set<EventCallback<AccessDeviceStatusMessage>>
    unifiedDeviceStatus: Set<EventCallback<UnifiedDeviceStatusMessage>>
    doorStateChange: Set<EventCallback<DoorStateChangeMessage>>
    connectionChange: Set<EventCallback<boolean>>
    error: Set<EventCallback<Error>>
  } = {
    accessDeviceStatus: new Set(),
    unifiedDeviceStatus: new Set(),
    doorStateChange: new Set(),
    connectionChange: new Set(),
    error: new Set()
  }

  /**
   * 订阅 WebSocket 连接
   */
  subscribe(userId: number) {
    this.subscribers++
    console.log(`[Access Device WebSocket Manager] 📝 新订阅者，当前订阅数: ${this.subscribers}`)

    // 如果已经连接且连接状态正常，直接返回
    if (this.ws?.readyState === WebSocket.OPEN && this.currentUserId === userId) {
      console.log('[Access Device WebSocket Manager] ✅ 使用已有连接')
      return
    }

    // 如果正在连接中，等待连接完成
    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.log('[Access Device WebSocket Manager] ⏳ 正在连接中，等待连接完成...')
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
    console.log(`[Access Device WebSocket Manager] 📝 订阅者离开，当前订阅数: ${this.subscribers}`)

    // 如果没有订阅者了，延迟断开连接
    if (this.subscribers === 0) {
      console.log('[Access Device WebSocket Manager] 🔌 5秒后没有新订阅者将断开连接')
      setTimeout(() => {
        if (this.subscribers === 0) {
          console.log('[Access Device WebSocket Manager] 🔌 确认断开连接')
          this.disconnect()
        }
      }, 5000)
    }
  }

  /**
   * 连接 WebSocket
   */
  private connect(userId: number) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.warn('[Access Device WebSocket Manager] 已经连接，跳过重复连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.warn('[Access Device WebSocket Manager] 正在连接中，跳过重复连接')
      return
    }

    if (this.ws && this.ws.readyState === WebSocket.CLOSING) {
      console.log('[Access Device WebSocket Manager] ⏳ 等待旧连接完全关闭...')
      try {
        this.ws.close()
      } catch (e) {
        // 忽略
      }
      this.ws = null
      this.stopHeartbeat()
    }

    this.currentUserId = userId

    try {
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsUrl = `${wsProtocol}//${window.location.host}/ws/iot/access/device/status?userId=${userId}`

      console.log('[Access Device WebSocket Manager] 🔗 正在连接:', wsUrl)

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('[Access Device WebSocket Manager] ✅ 连接成功')
        this.isConnected.value = true
        this.reconnectAttempts = 0
        this.notifyListeners('connectionChange', true)
        this.startHeartbeat()
      }

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          this.handleMessage(message)
        } catch (error) {
          console.error('[Access Device WebSocket Manager] ❌ 消息解析失败:', error)
        }
      }

      this.ws.onerror = (event) => {
        console.error('[Access Device WebSocket Manager] ❌ 连接错误:', event)
        this.isConnected.value = false
        this.notifyListeners('connectionChange', false)
        this.notifyListeners('error', new Error('WebSocket connection error'))
      }

      this.ws.onclose = (event) => {
        console.log('[Access Device WebSocket Manager] 🔌 连接关闭, code:', event.code, ', reason:', event.reason)
        this.isConnected.value = false
        this.stopHeartbeat()
        this.notifyListeners('connectionChange', false)

        // 如果是正常关闭，不重连
        if (event.code === 1000 && event.reason === 'User disconnect') {
          console.log('[Access Device WebSocket Manager] 用户主动断开，不重连')
          return
        }

        // 如果是因为新连接建立而关闭旧连接，不重连
        if (event.code === 1000 && event.reason === 'New connection established') {
          console.log('[Access Device WebSocket Manager] ℹ️ 服务器因新连接建立而关闭旧连接，不进行重连')
          this.ws = null
          this.reconnectAttempts = 0
          return
        }

        // 只有在有订阅者时才自动重连
        if (this.subscribers > 0 && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect()
        }
      }
    } catch (error) {
      console.error('[Access Device WebSocket Manager] ❌ 创建连接失败:', error)
      this.isConnected.value = false
      this.notifyListeners('error', error as Error)
      this.scheduleReconnect()
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) return
    if (this.subscribers === 0) return

    this.reconnectAttempts++
    const delay = Math.min(this.reconnectInterval * this.reconnectAttempts, 30000)

    console.log(`[Access Device WebSocket Manager] 🔄 ${delay / 1000}秒后尝试第${this.reconnectAttempts}次重连...`)
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = undefined
      if (this.currentUserId !== null && this.subscribers > 0) {
        this.connect(this.currentUserId)
      }
    }, delay)
  }

  private disconnect() {
    console.log('[Access Device WebSocket Manager] 🔌 主动断开连接')

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = undefined
    }

    this.stopHeartbeat()

    if (this.ws && this.ws.readyState !== WebSocket.CLOSED) {
      try {
        this.ws.close(1000, 'User disconnect')
      } catch (e) {
        // 忽略
      }
      this.ws = null
    }

    this.isConnected.value = false
    this.reconnectAttempts = 0
    this.currentUserId = null
  }

  private startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, this.heartbeatInterval)
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = undefined
    }
  }

  private handleMessage(message: any) {
    switch (message.type) {
      case 'connected':
        console.log('[Access Device WebSocket Manager] 🤝 服务器确认连接')
        break
      case 'pong':
        // 心跳响应
        break
      case 'access_device_status':
        if (message.data) {
          console.log('[Access Device WebSocket Manager] 📡 门禁设备状态变更:', message.data)
          this.notifyListeners('accessDeviceStatus', message.data as AccessDeviceStatusMessage)
        }
        break
      case 'DEVICE_STATUS':
        if (message.data) {
          console.log('[Access Device WebSocket Manager] 📡 统一设备状态变更:', message.data)
          this.notifyListeners('unifiedDeviceStatus', message.data as UnifiedDeviceStatusMessage)
        }
        break
      // ========== 设备事件：处理门状态变化 ==========
      case 'DEVICE_EVENT':
        if (message.data?.eventType === 'DOOR_STATE_CHANGE') {
          console.log('[Access Device WebSocket Manager] 📡 门状态变化:', message.data)
          this.notifyListeners('doorStateChange', message.data as DoorStateChangeMessage)
        }
        break
      case 'COMMAND_RESULT':
        // 命令结果由专门的处理器处理，这里不做处理
        break
      case 'auth_task_progress':
      case 'auth_task_completed':
        // 授权任务消息由 AuthTask WebSocket Manager 处理
        break
      default:
        // 只有真正未知的消息类型才记录日志
        console.log('[Access Device WebSocket Manager] 未知消息类型:', message.type)
    }
  }

  on<T = any>(event: keyof typeof this.listeners, callback: EventCallback<T>) {
    ;(this.listeners[event] as Set<EventCallback<T>>).add(callback)
  }

  off<T = any>(event: keyof typeof this.listeners, callback: EventCallback<T>) {
    ;(this.listeners[event] as Set<EventCallback<T>>).delete(callback)
  }

  private notifyListeners<T = any>(event: keyof typeof this.listeners, data?: T) {
    const callbacks = this.listeners[event] as Set<EventCallback<T>>
    callbacks.forEach(callback => {
      try {
        callback(data as T)
      } catch (error) {
        console.error(`[Access Device WebSocket Manager] 回调执行错误 (${event}):`, error)
      }
    })
  }

  getIsConnected() {
    return this.isConnected
  }

  forceReconnect() {
    if (this.currentUserId === null) {
      console.warn('[Access Device WebSocket Manager] 无法重连：没有用户ID')
      return
    }

    if (this.ws) {
      try {
        this.ws.close()
      } catch (e) {
        // 忽略
      }
      this.ws = null
    }
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = undefined
    }

    this.reconnectAttempts = 0
    this.connect(this.currentUserId)
  }
}

// ✅ 全局单例实例
const manager = new AccessDeviceStatusWebSocketManager()

/**
 * ✅ 门禁设备状态 WebSocket Hook（单例版本）
 * 
 * @example
 * const { connect, disconnect, connected } = useAccessDeviceStatusWebSocket({
 *   onAccessDeviceStatus: (data) => {
 *     console.log('门禁设备状态:', data)
 *   },
 *   onUnifiedDeviceStatus: (data) => {
 *     console.log('统一设备状态:', data)
 *   }
 * })
 */
export function useAccessDeviceStatusWebSocket(options: UseAccessDeviceStatusWebSocketOptions = {}) {
  const {
    autoConnect = true,
    onAccessDeviceStatus,
    onUnifiedDeviceStatus,
    onDoorStateChange,
    onConnectionChange,
    onError
  } = options

  const userStore = useUserStore()

  // 注册事件监听器
  if (onAccessDeviceStatus) manager.on('accessDeviceStatus', onAccessDeviceStatus)
  if (onUnifiedDeviceStatus) manager.on('unifiedDeviceStatus', onUnifiedDeviceStatus)
  if (onDoorStateChange) manager.on('doorStateChange', onDoorStateChange)
  if (onConnectionChange) manager.on('connectionChange', onConnectionChange)
  if (onError) manager.on('error', onError)

  const connect = () => {
    const userId = userStore.getUser?.id
    if (!userId) {
      console.warn('[Access Device WebSocket] 用户未登录，无法建立连接')
      return
    }
    manager.subscribe(userId)
  }

  const disconnect = () => {
    // 移除事件监听器
    if (onAccessDeviceStatus) manager.off('accessDeviceStatus', onAccessDeviceStatus)
    if (onUnifiedDeviceStatus) manager.off('unifiedDeviceStatus', onUnifiedDeviceStatus)
    if (onDoorStateChange) manager.off('doorStateChange', onDoorStateChange)
    if (onConnectionChange) manager.off('connectionChange', onConnectionChange)
    if (onError) manager.off('error', onError)

    manager.unsubscribe()
  }

  const reconnect = () => {
    manager.forceReconnect()
  }

  onMounted(() => {
    if (autoConnect && userStore.getUser?.id) {
      connect()
    }
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected: manager.getIsConnected(),
    connect,
    disconnect,
    reconnect
  }
}


