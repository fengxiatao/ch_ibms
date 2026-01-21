/**
 * 设备状态存储 Hook
 * 
 * 结合 WebSocket 实时更新和本地状态管理
 * 提供设备状态的响应式访问
 * Requirements: 2.1, 2.2
 */
import { ref, computed, onMounted } from 'vue'
import { 
  useDeviceStatusWebSocket, 
  DeviceStatusPushMessage,
  DeviceStateEnum,
  type UseDeviceStatusWebSocketOptions 
} from './useDeviceStatusWebSocket'

export interface DeviceStatusInfo {
  /** 设备ID */
  deviceId: number
  /** 设备名称 */
  deviceName: string
  /** 当前状态 */
  state: number
  /** 状态名称 */
  stateName: string
  /** 设备类型 */
  deviceType?: string
  /** 最后更新时间 */
  lastUpdateTime: number
}

export interface UseDeviceStatusStoreOptions extends Omit<UseDeviceStatusWebSocketOptions, 'onStatusChange'> {
  /** 初始设备列表（用于初始化状态） */
  initialDevices?: Array<{ id: number; deviceName: string; state: number; deviceType?: string }>
  /** 状态变更回调（在更新本地状态后调用） */
  onStatusChange?: (message: DeviceStatusPushMessage, deviceStatus: DeviceStatusInfo) => void
}

export function useDeviceStatusStore(options: UseDeviceStatusStoreOptions = {}) {
  const {
    initialDevices = [],
    onStatusChange,
    ...wsOptions
  } = options

  // 设备状态映射 (deviceId -> DeviceStatusInfo)
  const deviceStatusMap = ref<Map<number, DeviceStatusInfo>>(new Map())

  // 初始化设备状态
  const initializeDevices = (devices: Array<{ id: number; deviceName: string; state: number; deviceType?: string }>) => {
    devices.forEach(device => {
      deviceStatusMap.value.set(device.id, {
        deviceId: device.id,
        deviceName: device.deviceName,
        state: device.state,
        stateName: getStateName(device.state),
        deviceType: device.deviceType,
        lastUpdateTime: Date.now()
      })
    })
  }

  // 获取状态名称
  const getStateName = (state: number): string => {
    switch (state) {
      case DeviceStateEnum.ONLINE:
        return '在线'
      case DeviceStateEnum.OFFLINE:
        return '离线'
      case DeviceStateEnum.INACTIVE:
      default:
        return '未激活'
    }
  }

  // 处理状态变更
  const handleStatusChange = (message: DeviceStatusPushMessage) => {
    const deviceStatus: DeviceStatusInfo = {
      deviceId: message.deviceId,
      deviceName: message.deviceName,
      state: message.newState,
      stateName: message.newStateName,
      deviceType: message.deviceType,
      lastUpdateTime: message.timestamp
    }

    // 更新本地状态
    deviceStatusMap.value.set(message.deviceId, deviceStatus)

    // 触发回调
    onStatusChange?.(message, deviceStatus)
  }

  // 使用 WebSocket
  const {
    connected,
    reconnectAttempts,
    connect,
    disconnect,
    reconnect,
    subscribe,
    unsubscribe
  } = useDeviceStatusWebSocket({
    ...wsOptions,
    onStatusChange: handleStatusChange
  })

  // 获取设备状态
  const getDeviceStatus = (deviceId: number): DeviceStatusInfo | undefined => {
    return deviceStatusMap.value.get(deviceId)
  }

  // 获取设备状态（响应式）
  const getDeviceState = (deviceId: number): number => {
    return deviceStatusMap.value.get(deviceId)?.state ?? DeviceStateEnum.INACTIVE
  }

  // 判断设备是否在线
  const isDeviceOnline = (deviceId: number): boolean => {
    return getDeviceState(deviceId) === DeviceStateEnum.ONLINE
  }

  // 获取所有在线设备数量
  const onlineCount = computed(() => {
    let count = 0
    deviceStatusMap.value.forEach(status => {
      if (status.state === DeviceStateEnum.ONLINE) {
        count++
      }
    })
    return count
  })

  // 获取所有离线设备数量
  const offlineCount = computed(() => {
    let count = 0
    deviceStatusMap.value.forEach(status => {
      if (status.state === DeviceStateEnum.OFFLINE) {
        count++
      }
    })
    return count
  })

  // 获取所有设备数量
  const totalCount = computed(() => deviceStatusMap.value.size)

  // 更新设备状态（手动更新，用于初始化或刷新）
  const updateDeviceStatus = (deviceId: number, state: number, deviceName?: string, deviceType?: string) => {
    const existing = deviceStatusMap.value.get(deviceId)
    deviceStatusMap.value.set(deviceId, {
      deviceId,
      deviceName: deviceName ?? existing?.deviceName ?? '',
      state,
      stateName: getStateName(state),
      deviceType: deviceType ?? existing?.deviceType,
      lastUpdateTime: Date.now()
    })
  }

  // 批量更新设备状态
  const updateDeviceStatusBatch = (devices: Array<{ id: number; deviceName: string; state: number; deviceType?: string }>) => {
    devices.forEach(device => {
      updateDeviceStatus(device.id, device.state, device.deviceName, device.deviceType)
    })
  }

  // 清除设备状态
  const clearDeviceStatus = (deviceId: number) => {
    deviceStatusMap.value.delete(deviceId)
  }

  // 清除所有设备状态
  const clearAllDeviceStatus = () => {
    deviceStatusMap.value.clear()
  }

  // 初始化
  onMounted(() => {
    if (initialDevices.length > 0) {
      initializeDevices(initialDevices)
    }
  })

  return {
    // WebSocket 状态
    connected,
    reconnectAttempts,
    
    // WebSocket 方法
    connect,
    disconnect,
    reconnect,
    subscribe,
    unsubscribe,
    
    // 设备状态
    deviceStatusMap,
    onlineCount,
    offlineCount,
    totalCount,
    
    // 设备状态方法
    getDeviceStatus,
    getDeviceState,
    isDeviceOnline,
    updateDeviceStatus,
    updateDeviceStatusBatch,
    clearDeviceStatus,
    clearAllDeviceStatus,
    initializeDevices
  }
}
