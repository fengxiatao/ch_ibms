/**
 * 授权任务 WebSocket Hook（单例模式）
 * 
 * 用于接收授权任务的实时进度更新
 * 所有组件共享同一个 WebSocket 连接，避免重复连接问题
 * 
 * 端点：/ws/iot/access/auth-task/progress
 * Requirements: 13.5
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import type { AuthTaskProgressMessage } from '@/api/iot/access'

export interface UseAuthTaskWebSocketOptions {
  /** 是否自动连接 */
  autoConnect?: boolean
  /** 进度更新回调 */
  onProgress?: (progress: AuthTaskProgressMessage) => void
  /** 任务完成回调 */
  onCompleted?: (progress: AuthTaskProgressMessage) => void
  /** 连接状态变化回调 */
  onConnectionChange?: (connected: boolean) => void
}

/** 事件回调类型 */
type EventCallback<T = any> = (data: T) => void

/**
 * ✅ 授权任务 WebSocket 单例管理器
 */
class AuthTaskWebSocketManager {
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
    progress: Set<EventCallback<AuthTaskProgressMessage>>
    completed: Set<EventCallback<AuthTaskProgressMessage>>
    connectionChange: Set<EventCallback<boolean>>
  } = {
    progress: new Set(),
    completed: new Set(),
    connectionChange: new Set()
  }

  subscribe(userId: number) {
    this.subscribers++
    console.log(`[AuthTask WebSocket Manager] 📝 新订阅者，当前订阅数: ${this.subscribers}`)

    if (this.ws?.readyState === WebSocket.OPEN && this.currentUserId === userId) {
      console.log('[AuthTask WebSocket Manager] ✅ 使用已有连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.log('[AuthTask WebSocket Manager] ⏳ 正在连接中，等待连接完成...')
      return
    }

    if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
      this.connect(userId)
    }
  }

  unsubscribe() {
    this.subscribers = Math.max(0, this.subscribers - 1)
    console.log(`[AuthTask WebSocket Manager] 📝 订阅者离开，当前订阅数: ${this.subscribers}`)

    if (this.subscribers === 0) {
      console.log('[AuthTask WebSocket Manager] 🔌 5秒后没有新订阅者将断开连接')
      setTimeout(() => {
        if (this.subscribers === 0) {
          console.log('[AuthTask WebSocket Manager] 🔌 确认断开连接')
          this.disconnect()
        }
      }, 5000)
    }
  }

  private connect(userId: number) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.warn('[AuthTask WebSocket Manager] 已经连接，跳过重复连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.warn('[AuthTask WebSocket Manager] 正在连接中，跳过重复连接')
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
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.host
      const wsUrl = `${protocol}//${host}/ws/iot/access/auth-task/progress?userId=${userId}`

      console.log('[AuthTask WebSocket Manager] 🔗 正在连接:', wsUrl)

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('[AuthTask WebSocket Manager] ✅ 连接成功')
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
          console.error('[AuthTask WebSocket Manager] ❌ 消息解析失败:', error)
        }
      }

      this.ws.onerror = (event) => {
        console.error('[AuthTask WebSocket Manager] ❌ 连接错误:', event)
        this.isConnected.value = false
        this.notifyListeners('connectionChange', false)
      }

      this.ws.onclose = (event) => {
        console.log('[AuthTask WebSocket Manager] 🔌 连接关闭, code:', event.code, ', reason:', event.reason)
        this.isConnected.value = false
        this.stopHeartbeat()
        this.notifyListeners('connectionChange', false)

        if (event.code === 1000 && event.reason === 'User disconnect') {
          return
        }

        if (event.code === 1000 && event.reason === 'New connection established') {
          this.ws = null
          this.reconnectAttempts = 0
          return
        }

        if (this.subscribers > 0 && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect()
        }
      }
    } catch (error) {
      console.error('[AuthTask WebSocket Manager] ❌ 创建连接失败:', error)
      this.isConnected.value = false
      this.scheduleReconnect()
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimer) return
    if (this.subscribers === 0) return

    this.reconnectAttempts++
    const delay = Math.min(this.reconnectInterval * this.reconnectAttempts, 30000)

    console.log(`[AuthTask WebSocket Manager] 🔄 ${delay / 1000}秒后尝试第${this.reconnectAttempts}次重连...`)
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = undefined
      if (this.currentUserId !== null && this.subscribers > 0) {
        this.connect(this.currentUserId)
      }
    }, delay)
  }

  private disconnect() {
    console.log('[AuthTask WebSocket Manager] 🔌 主动断开连接')

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
    console.log('[AuthTask WebSocket Manager] 📨 收到消息:', message.type)

    switch (message.type) {
      case 'connected':
        console.log('[AuthTask WebSocket Manager] 🤝 服务器确认连接')
        break
      case 'pong':
        break
      case 'auth_task_progress':
        this.notifyListeners('progress', message.data as AuthTaskProgressMessage)
        break
      case 'auth_task_completed':
        this.notifyListeners('completed', message.data as AuthTaskProgressMessage)
        break
      default:
        console.log('[AuthTask WebSocket Manager] 未知消息类型:', message.type)
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
        console.error(`[AuthTask WebSocket Manager] 回调执行错误 (${event}):`, error)
      }
    })
  }

  getIsConnected() {
    return this.isConnected
  }

  forceReconnect() {
    if (this.currentUserId === null) {
      console.warn('[AuthTask WebSocket Manager] 无法重连：没有用户ID')
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
const manager = new AuthTaskWebSocketManager()

/**
 * ✅ 授权任务 WebSocket Hook（单例版本）
 */
export function useAuthTaskWebSocket(options: UseAuthTaskWebSocketOptions = {}) {
  const {
    autoConnect = true,
    onProgress,
    onCompleted,
    onConnectionChange
  } = options

  const userStore = useUserStore()

  if (onProgress) manager.on('progress', onProgress)
  if (onCompleted) manager.on('completed', onCompleted)
  if (onConnectionChange) manager.on('connectionChange', onConnectionChange)

  const connect = () => {
    const userId = userStore.getUser?.id
    if (!userId) {
      console.warn('[AuthTask WebSocket] 用户未登录，无法建立连接')
      return
    }
    manager.subscribe(userId)
  }

  const disconnect = () => {
    if (onProgress) manager.off('progress', onProgress)
    if (onCompleted) manager.off('completed', onCompleted)
    if (onConnectionChange) manager.off('connectionChange', onConnectionChange)
    manager.unsubscribe()
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
    disconnect
  }
}


