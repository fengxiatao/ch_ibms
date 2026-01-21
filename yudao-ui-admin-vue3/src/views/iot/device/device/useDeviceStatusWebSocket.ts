/**
 * 设备状态 WebSocket Hook（单例模式）
 * 
 * 用于接收设备在线状态的实时推送
 * 所有组件共享同一个 WebSocket 连接，避免重复连接问题
 * 
 * Requirements: 2.2, 2.4
 */
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/store/modules/user'

/** 设备状态推送消息类型 */
export interface DeviceStatusPushMessage {
  /** 设备ID */
  deviceId: number
  /** 设备名称 */
  deviceName: string
  /** 新状态码 (0-未激活, 1-在线, 2-离线) */
  newState: number
  /** 新状态名称 */
  newStateName: string
  /** 变更前状态码 */
  previousState: number | null
  /** 变更前状态名称 */
  previousStateName: string | null
  /** 变更时间戳（毫秒） */
  timestamp: number
  /** 设备类型 */
  deviceType?: string
  /** 产品ID */
  productId?: number
}

/** 设备状态枚举 */
export const DeviceStateEnum = {
  /** 未激活 */
  INACTIVE: 0,
  /** 在线 */
  ONLINE: 1,
  /** 离线 */
  OFFLINE: 2
} as const

/** 设备状态名称映射 */
export const DeviceStateNameMap: Record<number, string> = {
  [DeviceStateEnum.INACTIVE]: '未激活',
  [DeviceStateEnum.ONLINE]: '在线',
  [DeviceStateEnum.OFFLINE]: '离线'
}

export interface UseDeviceStatusWebSocketOptions {
  /** 是否自动连接 */
  autoConnect?: boolean
  /** 重连间隔（毫秒） */
  reconnectInterval?: number
  /** 最大重连次数 */
  maxReconnectAttempts?: number
  /** 心跳间隔（毫秒） */
  heartbeatInterval?: number
  /** 订阅的设备类型列表（为空表示订阅所有） */
  deviceTypes?: string[]
  /** 状态变更回调 */
  onStatusChange?: (message: DeviceStatusPushMessage) => void
  /** 连接状态变化回调 */
  onConnectionChange?: (connected: boolean) => void
  /** 错误回调 */
  onError?: (error: Error) => void
}

/** 事件回调类型 */
type EventCallback<T = any> = (data: T) => void

/**
 * ✅ 设备状态 WebSocket 单例管理器
 * 
 * 解决多个组件重复创建连接的问题：
 * - 所有组件共享同一个 WebSocket 连接
 * - 支持多个组件订阅状态变更事件
 * - 组件卸载时自动取消订阅
 */
class DeviceStatusWebSocketManager {
  private ws: WebSocket | null = null
  private isConnected = ref(false)
  private reconnectTimer: ReturnType<typeof setTimeout> | undefined
  private heartbeatTimer: ReturnType<typeof setInterval> | undefined
  private reconnectAttempts = ref(0)
  private maxReconnectAttempts = 10
  private heartbeatInterval = 30000
  private reconnectInterval = 5000
  private currentUserId: number | null = null
  private subscribers = 0 // 订阅者计数
  private subscribedDeviceTypes: Set<string> = new Set() // 订阅的设备类型

  // 事件监听器映射（支持多个回调）
  private listeners: {
    statusChange: Set<EventCallback<DeviceStatusPushMessage>>
    connectionChange: Set<EventCallback<boolean>>
    error: Set<EventCallback<Error>>
  } = {
    statusChange: new Set(),
    connectionChange: new Set(),
    error: new Set()
  }

  /**
   * 订阅 WebSocket 连接
   */
  subscribe(userId: number, deviceTypes: string[] = []) {
    this.subscribers++
    console.log(`[DeviceStatus WebSocket Manager] 📝 新订阅者，当前订阅数: ${this.subscribers}`)

    // 添加设备类型订阅
    deviceTypes.forEach(type => this.subscribedDeviceTypes.add(type))

    // 如果已经连接且连接状态正常，直接返回
    if (this.ws?.readyState === WebSocket.OPEN && this.currentUserId === userId) {
      console.log('[DeviceStatus WebSocket Manager] ✅ 使用已有连接')
      // 如果有新的设备类型订阅，发送订阅消息
      if (deviceTypes.length > 0) {
        this.sendSubscribe(deviceTypes)
      }
      return
    }

    // 如果正在连接中，等待连接完成
    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.log('[DeviceStatus WebSocket Manager] ⏳ 正在连接中，等待连接完成...')
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
  unsubscribe(deviceTypes: string[] = []) {
    this.subscribers = Math.max(0, this.subscribers - 1)
    console.log(`[DeviceStatus WebSocket Manager] 📝 订阅者离开，当前订阅数: ${this.subscribers}`)

    // 移除设备类型订阅
    deviceTypes.forEach(type => this.subscribedDeviceTypes.delete(type))

    // 如果没有订阅者了，延迟断开连接（避免页面切换时频繁断开）
    if (this.subscribers === 0) {
      console.log('[DeviceStatus WebSocket Manager] 🔌 5秒后没有新订阅者将断开连接')
      setTimeout(() => {
        if (this.subscribers === 0) {
          console.log('[DeviceStatus WebSocket Manager] 🔌 确认断开连接')
          this.disconnect()
        }
      }, 5000)
    }
  }

  /**
   * 连接 WebSocket
   */
  private connect(userId: number) {
    // 检查连接状态，避免重复连接
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.warn('[DeviceStatus WebSocket Manager] 已经连接，跳过重复连接')
      return
    }

    if (this.ws?.readyState === WebSocket.CONNECTING) {
      console.warn('[DeviceStatus WebSocket Manager] 正在连接中，跳过重复连接')
      return
    }

    // 如果有旧连接（CLOSING 状态），先完全关闭
    if (this.ws && this.ws.readyState === WebSocket.CLOSING) {
      console.log('[DeviceStatus WebSocket Manager] ⏳ 等待旧连接完全关闭...')
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
      // 构建 WebSocket URL - 统一使用 /ws/iot 前缀
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.host
      const wsUrl = `${protocol}//${host}/ws/iot/device/status?userId=${userId}`

      console.log('[DeviceStatus WebSocket Manager] 🔗 正在连接:', wsUrl)

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('[DeviceStatus WebSocket Manager] ✅ 连接成功')
        this.isConnected.value = true
        this.reconnectAttempts.value = 0
        this.notifyListeners('connectionChange', true)
        this.startHeartbeat()

        // 如果有订阅的设备类型，发送订阅消息
        if (this.subscribedDeviceTypes.size > 0) {
          this.sendSubscribe(Array.from(this.subscribedDeviceTypes))
        }
      }

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          this.handleMessage(message)
        } catch (error) {
          console.error('[DeviceStatus WebSocket Manager] ❌ 消息解析失败:', error)
        }
      }

      this.ws.onerror = (event) => {
        console.error('[DeviceStatus WebSocket Manager] ❌ 连接错误:', event)
        this.isConnected.value = false
        this.notifyListeners('connectionChange', false)
        this.notifyListeners('error', new Error('WebSocket connection error'))
      }

      this.ws.onclose = (event) => {
        console.log('[DeviceStatus WebSocket Manager] 🔌 连接关闭, code:', event.code, ', reason:', event.reason)
        this.isConnected.value = false
        this.stopHeartbeat()
        this.notifyListeners('connectionChange', false)

        // 如果是正常关闭（用户主动断开），不重连
        if (event.code === 1000 && event.reason === 'User disconnect') {
          console.log('[DeviceStatus WebSocket Manager] 用户主动断开，不重连')
          return
        }

        // 如果是因为新连接建立而关闭旧连接，不重连（避免无限循环）
        if (event.code === 1000 && event.reason === 'New connection established') {
          console.log('[DeviceStatus WebSocket Manager] ℹ️ 服务器因新连接建立而关闭旧连接，不进行重连')
          this.ws = null
          this.reconnectAttempts.value = 0
          return
        }

        // 只有在有订阅者时才自动重连
        if (this.subscribers > 0 && this.reconnectAttempts.value < this.maxReconnectAttempts) {
          this.scheduleReconnect()
        } else if (this.reconnectAttempts.value >= this.maxReconnectAttempts) {
          console.error('[DeviceStatus WebSocket Manager] ❌ 达到最大重连次数，停止重连')
        } else if (this.subscribers === 0) {
          console.log('[DeviceStatus WebSocket Manager] 没有订阅者，不重连')
        }
      }
    } catch (error) {
      console.error('[DeviceStatus WebSocket Manager] ❌ 创建连接失败:', error)
      this.isConnected.value = false
      this.notifyListeners('error', error as Error)
      this.scheduleReconnect()
    }
  }

  /**
   * 安排重连
   */
  private scheduleReconnect() {
    if (this.reconnectTimer) return
    if (this.subscribers === 0) return

    this.reconnectAttempts.value++
    const delay = Math.min(this.reconnectInterval * this.reconnectAttempts.value, 30000) // 最大30秒

    console.log(`[DeviceStatus WebSocket Manager] 🔄 ${delay / 1000}秒后尝试第${this.reconnectAttempts.value}次重连...`)
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = undefined
      if (this.currentUserId !== null && this.subscribers > 0) {
        this.connect(this.currentUserId)
      }
    }, delay)
  }

  /**
   * 断开连接
   */
  private disconnect() {
    console.log('[DeviceStatus WebSocket Manager] 🔌 主动断开连接')

    // 取消重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = undefined
    }

    // 停止心跳
    this.stopHeartbeat()

    // 关闭连接
    if (this.ws && this.ws.readyState !== WebSocket.CLOSED) {
      try {
        this.ws.close(1000, 'User disconnect')
      } catch (e) {
        console.error('[DeviceStatus WebSocket Manager] 关闭连接失败:', e)
      }
      this.ws = null
    }

    this.isConnected.value = false
    this.reconnectAttempts.value = 0
    this.currentUserId = null
    this.subscribedDeviceTypes.clear()
  }

  /**
   * 启动心跳
   */
  private startHeartbeat() {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
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
   * 发送订阅消息
   */
  private sendSubscribe(deviceTypes: string[]) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return
    }

    const message = {
      type: 'subscribe',
      data: {
        deviceTypes: deviceTypes
      }
    }
    this.ws.send(JSON.stringify(message))
    console.log('[DeviceStatus WebSocket Manager] 📋 订阅设备类型:', deviceTypes)
  }

  /**
   * 发送取消订阅消息
   */
  sendUnsubscribe(deviceTypes: string[]) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return
    }

    const message = {
      type: 'unsubscribe',
      data: {
        deviceTypes: deviceTypes
      }
    }
    this.ws.send(JSON.stringify(message))
    console.log('[DeviceStatus WebSocket Manager] 📋 取消订阅设备类型:', deviceTypes)
  }

  /**
   * 处理 WebSocket 消息
   */
  private handleMessage(message: any) {
    switch (message.type) {
      case 'connected':
        console.log('[DeviceStatus WebSocket Manager] 🤝 服务器确认连接')
        break
      case 'pong':
        // 心跳响应，不需要处理
        break
      case 'device_status_change':
        // 设备状态变更推送 (Requirements: 2.1) - 旧格式
        if (message.data) {
          console.log('[DeviceStatus WebSocket Manager] 📡 设备状态变更:',
            message.data.deviceName,
            message.data.previousStateName, '->', message.data.newStateName)
          this.notifyListeners('statusChange', message.data as DeviceStatusPushMessage)
        }
        break
      case 'DEVICE_STATUS':
        // 设备状态变更推送 - 统一格式 (UnifiedDeviceStatusMessage)
        if (message.data) {
          console.log('[DeviceStatus WebSocket Manager] 📡 统一设备状态变更:',
            message.data.deviceId, message.data.deviceType, '->', message.data.status)
          // 转换为前端期望的格式
          const statusMessage: DeviceStatusPushMessage = {
            deviceId: message.data.deviceId,
            deviceName: message.data.deviceName || `设备${message.data.deviceId}`,
            newState: this.convertStatusToState(message.data.status),
            newStateName: message.data.status,
            previousState: null,
            previousStateName: null,
            timestamp: message.data.timestamp || Date.now(),
            deviceType: message.data.deviceType,
            // 兼容：直接传递 status 字段
            status: message.data.status
          } as DeviceStatusPushMessage & { status: string }
          this.notifyListeners('statusChange', statusMessage)
        }
        break
      default:
        console.log('[DeviceStatus WebSocket Manager] 未知消息类型:', message.type, message)
    }
  }

  /**
   * 将状态字符串转换为状态码
   */
  private convertStatusToState(status: string): number {
    const stateMap: Record<string, number> = {
      'INACTIVE': DeviceStateEnum.INACTIVE,
      'ONLINE': DeviceStateEnum.ONLINE,
      'OFFLINE': DeviceStateEnum.OFFLINE
    }
    return stateMap[status] ?? DeviceStateEnum.INACTIVE
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
        console.error(`[DeviceStatus WebSocket Manager] 回调执行错误 (${event}):`, error)
      }
    })
  }

  /**
   * 获取连接状态
   */
  getIsConnected() {
    return this.isConnected
  }

  /**
   * 获取重连次数
   */
  getReconnectAttempts() {
    return this.reconnectAttempts
  }

  /**
   * 强制重连
   */
  forceReconnect() {
    if (this.currentUserId === null) {
      console.warn('[DeviceStatus WebSocket Manager] 无法重连：没有用户ID')
      return
    }

    // 清理旧连接
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
const manager = new DeviceStatusWebSocketManager()

/**
 * ✅ 设备状态 WebSocket Hook（单例版本）
 * 
 * 所有组件共享同一个 WebSocket 连接，避免重复连接问题
 * 
 * @example
 * const { connect, disconnect, connected } = useDeviceStatusWebSocket({
 *   onStatusChange: (data) => {
 *     console.log('设备状态变更:', data)
 *   },
 *   onConnectionChange: (isConnected) => {
 *     console.log('连接状态:', isConnected)
 *   }
 * })
 * 
 * onMounted(() => connect())
 * onUnmounted(() => disconnect())
 */
export function useDeviceStatusWebSocket(options: UseDeviceStatusWebSocketOptions = {}) {
  const {
    autoConnect = true,
    deviceTypes = [],
    onStatusChange,
    onConnectionChange,
    onError
  } = options

  const userStore = useUserStore()

  // 注册事件监听器
  if (onStatusChange) manager.on('statusChange', onStatusChange)
  if (onConnectionChange) manager.on('connectionChange', onConnectionChange)
  if (onError) manager.on('error', onError)

  /**
   * 建立 WebSocket 连接（订阅）
   * Requirements: 2.2
   */
  const connect = () => {
    const userId = userStore.getUser?.id
    if (!userId) {
      console.warn('[DeviceStatus WebSocket] 用户未登录，无法建立连接')
      return
    }
    manager.subscribe(userId, deviceTypes)
  }

  /**
   * 断开 WebSocket 连接（取消订阅）
   */
  const disconnect = () => {
    // 移除事件监听器
    if (onStatusChange) manager.off('statusChange', onStatusChange)
    if (onConnectionChange) manager.off('connectionChange', onConnectionChange)
    if (onError) manager.off('error', onError)

    // 取消订阅
    manager.unsubscribe(deviceTypes)
  }

  /**
   * 订阅特定设备类型
   * Requirements: 2.4
   */
  const subscribe = (types: string[]) => {
    types.forEach(type => {
      if (!deviceTypes.includes(type)) {
        deviceTypes.push(type)
      }
    })
    manager.subscribe(userStore.getUser?.id || 0, types)
  }

  /**
   * 取消订阅特定设备类型
   */
  const unsubscribe = (types: string[]) => {
    manager.sendUnsubscribe(types)
    types.forEach(type => {
      const index = deviceTypes.indexOf(type)
      if (index > -1) {
        deviceTypes.splice(index, 1)
      }
    })
  }

  /**
   * 手动重连
   */
  const reconnect = () => {
    manager.forceReconnect()
  }

  // 监听用户登录状态变化
  watch(() => userStore.getUser?.id, (newId, oldId) => {
    if (newId && !oldId) {
      // 用户登录，自动连接
      connect()
    } else if (!newId && oldId) {
      // 用户登出，断开连接
      disconnect()
    }
  })

  // 生命周期
  onMounted(() => {
    if (autoConnect && userStore.getUser?.id) {
      connect()
    }
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    /** 连接状态 */
    connected: manager.getIsConnected(),
    /** 重连次数 */
    reconnectAttempts: manager.getReconnectAttempts(),
    /** 建立连接 */
    connect,
    /** 断开连接 */
    disconnect,
    /** 手动重连 */
    reconnect,
    /** 订阅设备类型 */
    subscribe,
    /** 取消订阅设备类型 */
    unsubscribe
  }
}
