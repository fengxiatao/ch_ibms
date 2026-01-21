import { ref, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'

interface WebSocketOptions {
  onAlert?: (data: any) => void
  onConnected?: () => void
  onDisconnected?: () => void
  onError?: (error: Event) => void
}

/** 事件回调类型 */
type EventCallback<T = any> = (data: T) => void

/**
 * ✅ 告警 WebSocket 单例管理器
 * 
 * 解决多个组件重复创建连接的问题：
 * - 所有组件共享同一个WebSocket连接
 * - 支持多个组件订阅告警事件
 * - 组件卸载时自动取消订阅
 */
class AlertWebSocketManager {
  private ws: WebSocket | null = null
  private isConnected = ref(false)
  private reconnectTimer: number | undefined
  private heartbeatTimer: number | undefined
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private currentUserId: number | null = null
  private subscribers = 0
  private heartbeatInterval = 30000

  // 事件监听器映射
  private listeners: {
    alert: Set<EventCallback<any>>
    connected: Set<EventCallback<void>>
    disconnected: Set<EventCallback<void>>
    error: Set<EventCallback<Event>>
  } = {
    alert: new Set(),
    connected: new Set(),
    disconnected: new Set(),
    error: new Set()
  }

  /**
   * 获取 WebSocket URL
   */
  private getWsUrl(): string {
    const env = (import.meta as any).env || {}
    const fullWsUrl = env?.VITE_ALERT_WS_URL as string | undefined
    if (fullWsUrl && /^wss?:\/\//i.test(fullWsUrl)) {
      return fullWsUrl
    }
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const envHost = env?.VITE_ALERT_WS_HOST as string | undefined
    const baseUrl = env?.VITE_BASE_URL as string | undefined
    let baseHost = ''
    try {
      baseHost = baseUrl ? new URL(baseUrl).host : ''
    } catch {
      // ignore
    }
    const host = envHost || baseHost || window.location.host
    const path = (env?.VITE_ALERT_WS_PATH as string | undefined) || '/ws/iot/alarm/event'
    return `${protocol}//${host}${path}`
  }

  /**
   * 订阅 WebSocket 连接
   */
  subscribe(userId: number) {
    this.subscribers++
    console.log(`[Alert WebSocket Manager] 📝 新订阅者，当前订阅数: ${this.subscribers}`)

    if (this.ws?.readyState === WebSocket.OPEN && this.currentUserId === userId) {
      console.log('[Alert WebSocket Manager] ✅ 使用已有连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.log('[Alert WebSocket Manager] ⏳ 正在连接中，等待连接完成...')
      return
    }

    if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
      this.connect(userId)
    }
  }

  /**
   * 取消订阅
   */
  unsubscribe() {
    this.subscribers = Math.max(0, this.subscribers - 1)
    console.log(`[Alert WebSocket Manager] 📝 订阅者离开，当前订阅数: ${this.subscribers}`)

    if (this.subscribers === 0) {
      console.log('[Alert WebSocket Manager] 🔌 5秒后没有新订阅者将断开连接')
      setTimeout(() => {
        if (this.subscribers === 0) {
          console.log('[Alert WebSocket Manager] 🔌 确认断开连接')
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
      console.warn('[Alert WebSocket Manager] 已经连接，跳过重复连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.warn('[Alert WebSocket Manager] 正在连接中，跳过重复连接')
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
      const baseUrl = this.getWsUrl()
      const wsUrl = `${baseUrl}?userId=${userId}`
      console.log('[Alert WebSocket Manager] 🔗 正在连接:', wsUrl)

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('[Alert WebSocket Manager] ✅ 连接成功')
        this.isConnected.value = true
        this.reconnectAttempts = 0
        this.notifyListeners('connected')
        this.startHeartbeat()
      }

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          this.handleMessage(message)
        } catch (error) {
          console.error('[Alert WebSocket Manager] ❌ 消息解析失败:', error)
        }
      }

      this.ws.onerror = (event) => {
        console.error('[Alert WebSocket Manager] ❌ 连接错误:', event)
        this.isConnected.value = false
        this.notifyListeners('error', event)
      }

      this.ws.onclose = (event) => {
        console.log('[Alert WebSocket Manager] 🔌 连接关闭, code:', event.code, ', reason:', event.reason)
        this.isConnected.value = false
        this.stopHeartbeat()
        this.notifyListeners('disconnected')

        if (event.code === 1000 && event.reason === 'User disconnected') {
          return
        }

        if (event.code === 1000 && event.reason === 'New connection established') {
          this.ws = null
          this.reconnectAttempts = 0
          return
        }

        if (this.subscribers > 0 && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect()
        } else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
          console.error('[Alert WebSocket Manager] ❌ 达到最大重连次数，停止重连')
          ElNotification({
            title: 'WebSocket连接失败',
            message: '实时告警通知功能不可用，请刷新页面重试',
            type: 'error',
            duration: 0
          })
        }
      }
    } catch (error) {
      console.error('[Alert WebSocket Manager] ❌ 创建连接失败:', error)
      this.isConnected.value = false
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) return
    if (this.subscribers === 0) return

    this.reconnectAttempts++
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)

    console.log(`[Alert WebSocket Manager] 🔄 ${delay / 1000}秒后尝试第${this.reconnectAttempts}次重连...`)
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = undefined
      if (this.currentUserId !== null && this.subscribers > 0) {
        this.connect(this.currentUserId)
      }
    }, delay)
  }

  private disconnect() {
    console.log('[Alert WebSocket Manager] 🔌 主动断开连接')

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = undefined
    }

    this.stopHeartbeat()

    if (this.ws && this.ws.readyState !== WebSocket.CLOSED) {
      try {
        this.ws.close(1000, 'User disconnected')
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
    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send('ping')
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
        console.log('[Alert WebSocket Manager] 🤝 服务器确认连接')
        break
      case 'pong':
        break
      case 'alert':
      case 'alarm_event':
        console.log('[Alert WebSocket Manager] 🚨 收到告警事件:', message.data)
        this.notifyListeners('alert', message.data)
        break
      default:
        console.warn('[Alert WebSocket Manager] 未知消息类型:', message.type)
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
        console.error(`[Alert WebSocket Manager] 回调执行错误 (${event}):`, error)
      }
    })
  }

  getIsConnected() {
    return this.isConnected
  }

  send(message: string | object) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      const data = typeof message === 'string' ? message : JSON.stringify(message)
      this.ws.send(data)
    } else {
      console.warn('[Alert WebSocket Manager] 连接未打开，无法发送消息')
    }
  }
}

// ✅ 全局单例实例
const manager = new AlertWebSocketManager()

/**
 * ✅ 告警 WebSocket Hook（单例版本）
 *
 * 所有组件共享同一个WebSocket连接，避免重复连接问题
 *
 * @example
 * const { connect, disconnect, isConnected } = useAlertWebSocket({
 *   onAlert: (data) => {
 *     ElNotification({ title: '新告警', message: data.alertType })
 *   }
 * })
 *
 * onMounted(() => connect(userId))
 * onUnmounted(() => disconnect())
 */
export const useAlertWebSocket = (options: WebSocketOptions = {}) => {
  const {
    onAlert,
    onConnected,
    onDisconnected,
    onError
  } = options

  if (onAlert) manager.on('alert', onAlert)
  if (onConnected) manager.on('connected', onConnected)
  if (onDisconnected) manager.on('disconnected', onDisconnected)
  if (onError) manager.on('error', onError)

  const connect = (userId: number) => {
    manager.subscribe(userId)
  }

  const disconnect = () => {
    if (onAlert) manager.off('alert', onAlert)
    if (onConnected) manager.off('connected', onConnected)
    if (onDisconnected) manager.off('disconnected', onDisconnected)
    if (onError) manager.off('error', onError)
    manager.unsubscribe()
  }

  const send = (message: string | object) => {
    manager.send(message)
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connect,
    disconnect,
    send,
    isConnected: manager.getIsConnected()
  }
}
