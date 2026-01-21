/**
 * 门禁事件 WebSocket Hook（单例模式）
 * 
 * 用于接收门禁事件的实时推送
 * 所有组件共享同一个 WebSocket 连接，避免重复连接问题
 * 
 * 端点：/ws/iot/access/event
 * Requirements: 5.2, 5.3, 5.4
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { calculateReconnectDelay, createAccessEventWebSocketUrl } from '@/utils/accessEventWebSocket'

/** 门禁事件推送消息类型 */
export interface AccessEventPushMessage {
  /** 事件ID */
  eventId: number
  /** 事件时间 */
  eventTime: string
  /** 事件类型 */
  eventType: string
  /** 事件类型名称 */
  eventTypeName: string
  /** 事件描述 */
  eventDesc?: string
  /** 人员ID */
  personId?: number
  /** 人员姓名 */
  personName?: string
  /** 人员编号 */
  personCode?: string
  /** 卡号 */
  cardNo?: string
  /** 设备ID */
  deviceId: number
  /** 设备名称 */
  deviceName: string
  /** 通道ID */
  channelId?: number
  /** 通道名称 */
  channelName?: string
  /** 验证结果 */
  success?: boolean
  /** 验证结果：0-失败，1-成功 */
  verifyResult?: number
  /** 验证结果描述 */
  verifyResultDesc?: string
  /** 验证方式 */
  verifyMode?: string
  /** 失败原因 */
  failReason?: string
  /** 抓拍图片URL */
  captureUrl?: string
  /** 体温 */
  temperature?: number
  /** 口罩状态 */
  maskStatus?: number
  /** 凭证类型 */
  credentialType?: string
  /** 凭证数据 */
  credentialData?: string
  /** 推送时间戳 */
  timestamp: number
}

export interface UseAccessEventWebSocketOptions {
  /** 是否自动连接 */
  autoConnect?: boolean
  /** 事件回调 */
  onEvent?: (event: AccessEventPushMessage) => void
  /** 连接状态变化回调 */
  onConnectionChange?: (connected: boolean) => void
  /** 错误回调 */
  onError?: (error: Error) => void
}

/** 事件回调类型 */
type EventCallback<T = any> = (data: T) => void

/**
 * ✅ 门禁事件 WebSocket 单例管理器
 */
class AccessEventWebSocketManager {
  private ws: WebSocket | null = null
  private isConnected = ref(false)
  private reconnectTimer: ReturnType<typeof setTimeout> | undefined
  private heartbeatTimer: ReturnType<typeof setInterval> | undefined
  private reconnectAttempts = ref(0)
  private maxReconnectAttempts = 10
  private heartbeatInterval = 30000
  private reconnectInterval = 5000
  private currentUserId: number | null = null
  private subscribers = 0

  // 事件监听器映射
  private listeners: {
    event: Set<EventCallback<AccessEventPushMessage>>
    connectionChange: Set<EventCallback<boolean>>
    error: Set<EventCallback<Error>>
  } = {
    event: new Set(),
    connectionChange: new Set(),
    error: new Set()
  }

  subscribe(userId: number) {
    this.subscribers++
    console.log(`[AccessEvent WebSocket Manager] 📝 新订阅者，当前订阅数: ${this.subscribers}`)

    if (this.ws?.readyState === WebSocket.OPEN && this.currentUserId === userId) {
      console.log('[AccessEvent WebSocket Manager] ✅ 使用已有连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.log('[AccessEvent WebSocket Manager] ⏳ 正在连接中，等待连接完成...')
      return
    }

    if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
      this.connect(userId)
    }
  }

  unsubscribe() {
    this.subscribers = Math.max(0, this.subscribers - 1)
    console.log(`[AccessEvent WebSocket Manager] 📝 订阅者离开，当前订阅数: ${this.subscribers}`)

    if (this.subscribers === 0) {
      console.log('[AccessEvent WebSocket Manager] 🔌 5秒后没有新订阅者将断开连接')
      setTimeout(() => {
        if (this.subscribers === 0) {
          console.log('[AccessEvent WebSocket Manager] 🔌 确认断开连接')
          this.disconnect()
        }
      }, 5000)
    }
  }

  private connect(userId: number) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.warn('[AccessEvent WebSocket Manager] 已经连接，跳过重复连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.warn('[AccessEvent WebSocket Manager] 正在连接中，跳过重复连接')
      return
    }

    if (this.ws && this.ws.readyState === WebSocket.CLOSING) {
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
      const wsUrl = createAccessEventWebSocketUrl(userId)
      console.log('[AccessEvent WebSocket Manager] 🔗 正在连接:', wsUrl)

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('[AccessEvent WebSocket Manager] ✅ 连接成功')
        this.isConnected.value = true
        this.reconnectAttempts.value = 0
        this.notifyListeners('connectionChange', true)
        this.startHeartbeat()
      }

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          this.handleMessage(message)
        } catch (error) {
          console.error('[AccessEvent WebSocket Manager] ❌ 消息解析失败:', error)
        }
      }

      this.ws.onerror = (event) => {
        console.error('[AccessEvent WebSocket Manager] ❌ 连接错误:', event)
        this.isConnected.value = false
        this.notifyListeners('connectionChange', false)
        this.notifyListeners('error', new Error('WebSocket connection error'))
      }

      this.ws.onclose = (event) => {
        console.log('[AccessEvent WebSocket Manager] 🔌 连接关闭, code:', event.code, ', reason:', event.reason)
        this.isConnected.value = false
        this.stopHeartbeat()
        this.notifyListeners('connectionChange', false)

        if (event.code === 1000 && event.reason === 'User disconnect') {
          return
        }

        if (event.code === 1000 && event.reason === 'New connection established') {
          this.ws = null
          this.reconnectAttempts.value = 0
          return
        }

        if (this.subscribers > 0 && this.reconnectAttempts.value < this.maxReconnectAttempts) {
          this.scheduleReconnect()
        }
      }
    } catch (error) {
      console.error('[AccessEvent WebSocket Manager] ❌ 创建连接失败:', error)
      this.isConnected.value = false
      this.notifyListeners('error', error as Error)
      this.scheduleReconnect()
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) return
    if (this.subscribers === 0) return

    this.reconnectAttempts.value++
    const delay = calculateReconnectDelay(this.reconnectAttempts.value, this.reconnectInterval, 30000)

    console.log(`[AccessEvent WebSocket Manager] 🔄 ${delay / 1000}秒后尝试第${this.reconnectAttempts.value}次重连...`)
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = undefined
      if (this.currentUserId !== null && this.subscribers > 0) {
        this.connect(this.currentUserId)
      }
    }, delay)
  }

  private disconnect() {
    console.log('[AccessEvent WebSocket Manager] 🔌 主动断开连接')

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
    this.reconnectAttempts.value = 0
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
        console.log('[AccessEvent WebSocket Manager] 🤝 服务器确认连接')
        break
      case 'pong':
        break
      case 'access_event':
        if (message.data) {
          console.log('[AccessEvent WebSocket Manager] 🚪 收到门禁事件:', message.data.eventTypeName)
          this.notifyListeners('event', message.data as AccessEventPushMessage)
        }
        break
      case 'DEVICE_EVENT': {
        const data = message?.data
        const deviceType = String(data?.deviceType || '').toUpperCase()
        if (!deviceType.startsWith('ACCESS')) {
          break
        }
        const eventData = data?.eventData || {}
        const mapped: AccessEventPushMessage = {
          eventId: Number(eventData.eventId || 0),
          eventTime: String(eventData.eventTime || ''),
          eventType: String(eventData.eventType || data?.eventType || ''),
          eventTypeName: String(eventData.eventTypeName || ''),
          eventDesc: eventData.eventDesc != null ? String(eventData.eventDesc) : undefined,
          personId: eventData.personId != null ? Number(eventData.personId) : undefined,
          personName: eventData.personName != null ? String(eventData.personName) : undefined,
          personCode: eventData.personCode != null ? String(eventData.personCode) : undefined,
          cardNo: eventData.cardNo != null ? String(eventData.cardNo) : undefined,
          deviceId: Number(eventData.deviceId || data?.deviceId || 0),
          deviceName: eventData.deviceName != null ? String(eventData.deviceName) : '',
          channelId: eventData.channelId != null ? Number(eventData.channelId) : undefined,
          channelName: eventData.channelName != null ? String(eventData.channelName) : undefined,
          success: eventData.success != null ? Boolean(eventData.success) : undefined,
          verifyResult: eventData.verifyResult != null ? Number(eventData.verifyResult) : undefined,
          verifyResultDesc: eventData.verifyResultDesc != null ? String(eventData.verifyResultDesc) : undefined,
          verifyMode: eventData.verifyMode != null ? String(eventData.verifyMode) : undefined,
          failReason: eventData.failReason != null ? String(eventData.failReason) : undefined,
          captureUrl: eventData.captureUrl != null ? String(eventData.captureUrl) : undefined,
          temperature: eventData.temperature != null ? Number(eventData.temperature) : undefined,
          maskStatus: eventData.maskStatus != null ? Number(eventData.maskStatus) : undefined,
          credentialType: eventData.credentialType != null ? String(eventData.credentialType) : undefined,
          credentialData: eventData.credentialData != null ? String(eventData.credentialData) : undefined,
          timestamp: Number(data?.timestamp || Date.now())
        }
        console.log('[AccessEvent WebSocket Manager] 📨 收到统一门禁事件:', mapped.eventType)
        this.notifyListeners('event', mapped)
        break
      }
      case 'access_device_status':
        console.log('[AccessEvent WebSocket Manager] 📡 设备状态更新:', message.data?.deviceName)
        break
      default:
        console.log('[AccessEvent WebSocket Manager] 未知消息类型:', message.type)
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
        console.error(`[AccessEvent WebSocket Manager] 回调执行错误 (${event}):`, error)
      }
    })
  }

  getIsConnected() {
    return this.isConnected
  }

  getReconnectAttempts() {
    return this.reconnectAttempts
  }

  forceReconnect() {
    if (this.currentUserId === null) {
      console.warn('[AccessEvent WebSocket Manager] 无法重连：没有用户ID')
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

    this.reconnectAttempts.value = 0
    this.connect(this.currentUserId)
  }
}

// ✅ 全局单例实例
const manager = new AccessEventWebSocketManager()

/**
 * ✅ 门禁事件 WebSocket Hook（单例版本）
 */
export function useAccessEventWebSocket(options: UseAccessEventWebSocketOptions = {}) {
  const {
    autoConnect = true,
    onEvent,
    onConnectionChange,
    onError
  } = options

  const userStore = useUserStore()

  if (onEvent) manager.on('event', onEvent)
  if (onConnectionChange) manager.on('connectionChange', onConnectionChange)
  if (onError) manager.on('error', onError)

  const connect = () => {
    const userId = userStore.getUser?.id
    if (!userId) {
      console.warn('[AccessEvent WebSocket] 用户未登录，无法建立连接')
      return
    }
    manager.subscribe(userId)
  }

  const disconnect = () => {
    if (onEvent) manager.off('event', onEvent)
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
    reconnectAttempts: manager.getReconnectAttempts(),
    connect,
    disconnect,
    reconnect
  }
}
