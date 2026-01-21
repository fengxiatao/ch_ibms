/**
 * 门禁事件 WebSocket 连接管理工具
 * 提供 WebSocket 连接、断开、指数退避重连和心跳机制
 * 
 * **Feature: access-event-full-integration**
 * **Validates: Requirements 4.4, 4.5**
 * **Property 10: WebSocket Reconnection Backoff**
 */

// ==================== 类型定义 ====================

/**
 * WebSocket 连接配置
 */
export interface WebSocketConfig {
  /** WebSocket URL */
  url: string
  /** 基础重连延迟（毫秒），默认 5000 */
  baseDelay?: number
  /** 最大重连延迟（毫秒），默认 30000 */
  maxDelay?: number
  /** 最大重连次数，默认 10 */
  maxReconnectAttempts?: number
  /** 心跳间隔（毫秒），默认 30000 */
  heartbeatInterval?: number
  /** 心跳超时（毫秒），默认 10000 */
  heartbeatTimeout?: number
}

/**
 * WebSocket 连接状态
 */
export enum ConnectionState {
  DISCONNECTED = 'DISCONNECTED',
  CONNECTING = 'CONNECTING',
  CONNECTED = 'CONNECTED',
  RECONNECTING = 'RECONNECTING'
}

/**
 * WebSocket 事件回调
 */
export interface WebSocketCallbacks {
  /** 连接成功回调 */
  onOpen?: () => void
  /** 收到消息回调 */
  onMessage?: (data: any) => void
  /** 连接关闭回调 */
  onClose?: (event: CloseEvent) => void
  /** 连接错误回调 */
  onError?: (error: Event) => void
  /** 连接状态变化回调 */
  onStateChange?: (state: ConnectionState) => void
  /** 重连尝试回调 */
  onReconnectAttempt?: (attempt: number, delay: number) => void
}

/**
 * WebSocket 连接管理器接口
 */
export interface WebSocketManager {
  /** 当前连接状态 */
  state: ConnectionState
  /** 重连尝试次数 */
  reconnectAttempts: number
  /** 建立连接 */
  connect: () => void
  /** 断开连接 */
  disconnect: () => void
  /** 发送消息 */
  send: (data: any) => boolean
  /** 手动重连 */
  reconnect: () => void
  /** 是否已连接 */
  isConnected: () => boolean
}

// ==================== 核心函数 ====================

/**
 * 计算重连退避延迟
 * 使用指数退避算法: delay(n) = min(baseDelay * 2^(n-1), maxDelay)
 * 
 * @param attemptNumber 重连尝试次数（从1开始）
 * @param baseDelay 基础延迟（毫秒）
 * @param maxDelay 最大延迟（毫秒）
 * @returns 计算后的延迟时间（毫秒）
 * 
 * **Property 10: WebSocket Reconnection Backoff**
 * **Validates: Requirements 4.4**
 */
export function calculateReconnectDelay(
  attemptNumber: number,
  baseDelay: number = 5000,
  maxDelay: number = 30000
): number {
  if (attemptNumber < 1) {
    return baseDelay
  }
  // 指数退避: baseDelay * 2^(attempts-1), 最大 maxDelay
  const delay = baseDelay * Math.pow(2, attemptNumber - 1)
  return Math.min(delay, maxDelay)
}

/**
 * 创建 WebSocket 连接管理器
 * 
 * @param config WebSocket 配置
 * @param callbacks 事件回调
 * @returns WebSocket 管理器实例
 * 
 * **Validates: Requirements 4.4, 4.5**
 */
export function createWebSocketManager(
  config: WebSocketConfig,
  callbacks: WebSocketCallbacks = {}
): WebSocketManager {
  const {
    url,
    baseDelay = 5000,
    maxDelay = 30000,
    maxReconnectAttempts = 10,
    heartbeatInterval = 30000,
    heartbeatTimeout = 10000
  } = config

  let ws: WebSocket | null = null
  let state: ConnectionState = ConnectionState.DISCONNECTED
  let reconnectAttempts = 0
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  let heartbeatTimeoutTimer: ReturnType<typeof setTimeout> | null = null
  let isManualDisconnect = false

  /**
   * 更新连接状态
   */
  const setState = (newState: ConnectionState) => {
    state = newState
    callbacks.onStateChange?.(state)
  }

  /**
   * 清除所有定时器
   */
  const clearTimers = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
    if (heartbeatTimeoutTimer) {
      clearTimeout(heartbeatTimeoutTimer)
      heartbeatTimeoutTimer = null
    }
  }

  /**
   * 启动心跳机制
   * **Validates: Requirements 4.5**
   */
  const startHeartbeat = () => {
    stopHeartbeat()
    
    heartbeatTimer = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        // 发送 ping 消息
        ws.send(JSON.stringify({ type: 'ping' }))
        
        // 设置心跳超时检测
        heartbeatTimeoutTimer = setTimeout(() => {
          console.warn('[WebSocket] 心跳超时，准备重连')
          // 心跳超时，关闭连接触发重连
          if (ws) {
            ws.close(4000, 'Heartbeat timeout')
          }
        }, heartbeatTimeout)
      }
    }, heartbeatInterval)
  }

  /**
   * 停止心跳机制
   */
  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
    if (heartbeatTimeoutTimer) {
      clearTimeout(heartbeatTimeoutTimer)
      heartbeatTimeoutTimer = null
    }
  }

  /**
   * 处理收到的 pong 响应
   */
  const handlePong = () => {
    // 收到 pong，清除超时定时器
    if (heartbeatTimeoutTimer) {
      clearTimeout(heartbeatTimeoutTimer)
      heartbeatTimeoutTimer = null
    }
  }

  /**
   * 安排重连
   * **Property 10: WebSocket Reconnection Backoff**
   * **Validates: Requirements 4.4**
   */
  const scheduleReconnect = () => {
    if (isManualDisconnect) {
      return
    }
    
    if (reconnectTimer) {
      return
    }
    
    if (reconnectAttempts >= maxReconnectAttempts) {
      console.warn('[WebSocket] 达到最大重连次数，停止重连')
      setState(ConnectionState.DISCONNECTED)
      return
    }
    
    reconnectAttempts++
    const delay = calculateReconnectDelay(reconnectAttempts, baseDelay, maxDelay)
    
    console.log(`[WebSocket] ${delay / 1000}秒后尝试第${reconnectAttempts}次重连...`)
    callbacks.onReconnectAttempt?.(reconnectAttempts, delay)
    
    setState(ConnectionState.RECONNECTING)
    
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, delay)
  }

  /**
   * 建立 WebSocket 连接
   */
  const connect = () => {
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
      console.log('[WebSocket] 已连接或正在连接，跳过')
      return
    }

    isManualDisconnect = false
    setState(ConnectionState.CONNECTING)

    try {
      ws = new WebSocket(url)

      ws.onopen = () => {
        console.log('[WebSocket] ✅ 连接成功')
        setState(ConnectionState.CONNECTED)
        reconnectAttempts = 0
        
        // 清除重连定时器
        if (reconnectTimer) {
          clearTimeout(reconnectTimer)
          reconnectTimer = null
        }
        
        // 启动心跳
        startHeartbeat()
        
        callbacks.onOpen?.()
      }

      ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          
          // 处理 pong 响应
          if (message.type === 'pong') {
            handlePong()
            return
          }
          
          callbacks.onMessage?.(message)
        } catch (error) {
          // 如果不是 JSON，直接传递原始数据
          callbacks.onMessage?.(event.data)
        }
      }

      ws.onclose = (event) => {
        console.log('[WebSocket] 🔌 连接关闭:', event.code, event.reason)
        ws = null
        stopHeartbeat()
        
        if (!isManualDisconnect) {
          scheduleReconnect()
        } else {
          setState(ConnectionState.DISCONNECTED)
        }
        
        callbacks.onClose?.(event)
      }

      ws.onerror = (error) => {
        console.error('[WebSocket] ❌ 连接错误:', error)
        callbacks.onError?.(error)
      }
    } catch (error) {
      console.error('[WebSocket] 创建连接失败:', error)
      scheduleReconnect()
    }
  }

  /**
   * 断开 WebSocket 连接
   */
  const disconnect = () => {
    isManualDisconnect = true
    clearTimers()
    
    if (ws) {
      ws.close(1000, 'User disconnect')
      ws = null
    }
    
    reconnectAttempts = 0
    setState(ConnectionState.DISCONNECTED)
  }

  /**
   * 发送消息
   * @param data 要发送的数据
   * @returns 是否发送成功
   */
  const send = (data: any): boolean => {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.warn('[WebSocket] 连接未就绪，无法发送消息')
      return false
    }
    
    try {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      ws.send(message)
      return true
    } catch (error) {
      console.error('[WebSocket] 发送消息失败:', error)
      return false
    }
  }

  /**
   * 手动重连
   */
  const reconnect = () => {
    disconnect()
    reconnectAttempts = 0
    isManualDisconnect = false
    connect()
  }

  /**
   * 检查是否已连接
   */
  const isConnected = (): boolean => {
    return ws !== null && ws.readyState === WebSocket.OPEN
  }

  return {
    get state() {
      return state
    },
    get reconnectAttempts() {
      return reconnectAttempts
    },
    connect,
    disconnect,
    send,
    reconnect,
    isConnected
  }
}

// ==================== 门禁事件专用工具 ====================

/**
 * 门禁事件 WebSocket 消息类型
 */
export const AccessEventMessageTypes = {
  CONNECTED: 'connected',
  PING: 'ping',
  PONG: 'pong',
  ACCESS_EVENT: 'access_event',
  DEVICE_STATUS: 'access_device_status'
} as const

/**
 * 创建门禁事件 WebSocket URL
 * 
 * @param userId 用户ID
 * @param baseUrl 基础URL（可选，默认使用当前页面的 host）
 * @returns WebSocket URL
 */
export function createAccessEventWebSocketUrl(userId: number | string, baseUrl?: string): string {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = baseUrl || window.location.host
  // 统一使用 /ws/iot 前缀
  return `${protocol}//${host}/ws/iot/access/event?userId=${userId}`
}

/**
 * 创建门禁事件 WebSocket 管理器
 * 
 * @param userId 用户ID
 * @param callbacks 事件回调
 * @param config 额外配置（可选）
 * @returns WebSocket 管理器实例
 */
export function createAccessEventWebSocket(
  userId: number | string,
  callbacks: WebSocketCallbacks = {},
  config: Partial<Omit<WebSocketConfig, 'url'>> = {}
): WebSocketManager {
  const url = createAccessEventWebSocketUrl(userId)
  
  return createWebSocketManager(
    {
      url,
      baseDelay: config.baseDelay ?? 5000,
      maxDelay: config.maxDelay ?? 30000,
      maxReconnectAttempts: config.maxReconnectAttempts ?? 10,
      heartbeatInterval: config.heartbeatInterval ?? 30000,
      heartbeatTimeout: config.heartbeatTimeout ?? 10000
    },
    callbacks
  )
}

// ==================== 导出默认配置 ====================

/**
 * 默认 WebSocket 配置
 */
export const DEFAULT_WEBSOCKET_CONFIG: Omit<WebSocketConfig, 'url'> = {
  baseDelay: 5000,
  maxDelay: 30000,
  maxReconnectAttempts: 10,
  heartbeatInterval: 30000,
  heartbeatTimeout: 10000
}
