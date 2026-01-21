import { useUserStoreWithOut } from '@/store/modules/user'

/**
 * IoT WebSocket 客户端
 * 
 * 功能：
 * - 连接 IoT WebSocket 服务
 * - 接收设备状态更新
 * - 接收告警事件
 * - 接收设备统计数据
 */
class IotWebSocketClient {
  private ws: WebSocket | null = null
  private url: string = ''
  private reconnectTimer: any = null
  private heartbeatTimer: any = null
  private listeners: Map<string, Set<Function>> = new Map()
  private isManualClose: boolean = false

  /**
   * 连接 WebSocket
   */
  connect() {
    // 如果已经连接或正在连接中，跳过
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      console.log('[IoT WebSocket] 已连接或正在连接中，跳过重复连接')
      return
    }

    const userStore = useUserStoreWithOut()
    const userId = userStore.getUser?.id

    if (!userId) {
      console.warn('[IoT WebSocket] 用户未登录，跳过连接')
      return
    }

    // 构建 WebSocket URL（支持环境变量覆盖，默认跟随当前站点）
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    // 1) 若提供完整的 WS URL，则直接使用（需包含 ws:// 或 wss://）
    const fullWsUrl = (import.meta as any).env?.VITE_IOT_WS_URL as string | undefined
    if (fullWsUrl && /^wss?:\/\//i.test(fullWsUrl)) {
      const sep = fullWsUrl.includes('?') ? '&' : '?'
      this.url = `${fullWsUrl}${sep}userId=${userId}`
    } else {
      // 2) 组合 host + path（无则使用当前 host 与默认路径）
      const envHost = (import.meta as any).env?.VITE_IOT_WS_HOST as string | undefined
      const baseUrl = (import.meta as any).env?.VITE_BASE_URL as string | undefined
      let baseHost = ''
      try {
        baseHost = baseUrl ? new URL(baseUrl).host : ''
      } catch {}
      const host = envHost || baseHost || window.location.host
      // ✅ WebSocket 路径不需要 /admin-api 前缀
      const path = (import.meta as any).env?.VITE_IOT_WS_PATH || '/ws/iot'
      this.url = `${protocol}//${host}${path}?userId=${userId}`
    }

    console.log('[IoT WebSocket] 开始连接:', this.url)

    try {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        console.log('[IoT WebSocket] ✅ 连接成功')
        this.isManualClose = false
        this.startHeartbeat()
        this.emit('connected', null)
      }

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          console.log('[IoT WebSocket] 📨 收到消息:', message.type, message)
          
          // 分发消息给监听器
          this.emit(message.type, message.data)
          
          // 特别标记 alarm_event
          if (message.type === 'alarm_event') {
            console.log('[IoT WebSocket] 🚨 收到报警事件:', message.data)
          }
        } catch (e) {
          console.error('[IoT WebSocket] ❌ 解析消息失败:', e, event.data)
        }
      }

      this.ws.onerror = (error) => {
        console.error('[IoT WebSocket] ❌ 连接错误:', error)
      }

      this.ws.onclose = (event) => {
        console.log('[IoT WebSocket] 🔌 连接关闭, code:', event.code, ', reason:', event.reason)
        this.stopHeartbeat()
        
        // 如果是服务器因新连接建立而关闭旧连接，不重连
        if (event.code === 1000 && event.reason === 'New connection established') {
          console.log('[IoT WebSocket] ℹ️ 服务器因新连接建立而关闭旧连接，不进行重连')
          this.ws = null
          return
        }
        
        // 如果不是手动关闭，则自动重连
        if (!this.isManualClose) {
          this.reconnect()
        }
      }
    } catch (e) {
      console.error('[IoT WebSocket] ❌ 创建连接失败:', e)
      this.reconnect()
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    console.log('[IoT WebSocket] 手动断开连接')
    this.isManualClose = true
    this.stopHeartbeat()
    this.stopReconnect()
    
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 重新连接
   */
  private reconnect() {
    this.stopReconnect()
    
    console.log('[IoT WebSocket] 🔄 5秒后重新连接...')
    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, 5000)
  }

  /**
   * 停止重连
   */
  private stopReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  /**
   * 启动心跳
   */
  private startHeartbeat() {
    this.stopHeartbeat()
    
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send('ping', null)
      }
    }, 30000) // 每30秒发送一次心跳
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 发送消息
   */
  send(type: string, data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message = { type, data }
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('[IoT WebSocket] ⚠️ 连接未就绪，无法发送消息')
    }
  }

  /**
   * 监听消息
   */
  on(type: string, callback: Function) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, new Set())
    }
    this.listeners.get(type)!.add(callback)
  }

  /**
   * 取消监听
   */
  off(type: string, callback: Function) {
    const listeners = this.listeners.get(type)
    if (listeners) {
      listeners.delete(callback)
    }
  }

  /**
   * 触发事件
   */
  private emit(type: string, data: any) {
    const listeners = this.listeners.get(type)
    if (listeners) {
      listeners.forEach(callback => {
        try {
          callback(data)
        } catch (e) {
          console.error('[IoT WebSocket] ❌ 事件回调执行失败:', e)
        }
      })
    }
  }

  /**
   * 获取连接状态
   */
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN
  }
}

// 导出单例
export const iotWebSocket = new IotWebSocketClient()

// 自动连接（在用户登录后）
export function initIotWebSocket() {
  iotWebSocket.connect()
}

// 断开连接（在用户登出时）
export function closeIotWebSocket() {
  iotWebSocket.disconnect()
}
