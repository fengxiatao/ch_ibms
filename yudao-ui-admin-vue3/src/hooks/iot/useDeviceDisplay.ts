import { ref, computed } from 'vue'
import * as DeviceApi from '@/api/iot/device/device'
import * as DeviceDisplayApi from '@/api/iot/device/display'
import { getDeviceComponent, hasDeviceComponent } from '@/components/IoT/DeviceComponents'

/**
 * IoT设备动态展示Hook
 * 根据产品配置自动加载和渲染设备组件
 */
export const useDeviceDisplay = () => {
  const devices = ref<any[]>([])
  const displayConfigs = ref<any[]>([])
  const componentTemplates = ref<any[]>([])
  const loading = ref(false)

  /**
   * 获取指定模块的设备列表
   * @param moduleCode 模块代码，如 'security.video.preview'
   */
  const getModuleDevices = async (moduleCode: string) => {
    try {
      loading.value = true
      
      // 获取该模块支持的产品展示配置
      const configRes = await DeviceDisplayApi.getDeviceDisplayConfigList()
      const moduleConfigs = configRes.data.filter((config: any) => 
        config.moduleCode === moduleCode && config.status === 1
      )
      
      if (moduleConfigs.length === 0) {
        console.warn(`模块 ${moduleCode} 没有配置任何设备展示`)
        return []
      }
      
      // 获取这些产品的设备列表
      const productIds = [...new Set(moduleConfigs.map((config: any) => config.productId))]
      const deviceRes = await DeviceApi.getDevicePage({
        pageNo: 1,
        pageSize: 1000,
        productId: null
      })
      
      // 过滤出属于指定产品的设备
      const moduleDevices = deviceRes.data.list.filter((device: any) => 
        productIds.includes(device.productId) && device.state === 1
      )
      
      // 为每个设备添加展示配置信息
      const devicesWithConfig = moduleDevices.map((device: any) => {
        const config = moduleConfigs.find((c: any) => c.productId === device.productId)
        return {
          ...device,
          displayConfig: config,
          componentType: config?.componentType,
          componentConfig: config?.componentConfig
        }
      })
      
      devices.value = devicesWithConfig
      displayConfigs.value = moduleConfigs
      
      console.log(`模块 ${moduleCode} 加载了 ${devicesWithConfig.length} 个设备`, devicesWithConfig)
      return devicesWithConfig
      
    } catch (error) {
      console.error('获取模块设备失败:', error)
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取设备的动态组件
   * @param device 设备信息
   */
  const getDeviceComponentInfo = (device: any) => {
    const componentType = device.componentType || device.displayConfig?.componentType
    
    if (!componentType) {
      console.warn('设备缺少组件类型配置:', device)
      return null
    }
    
    if (!hasDeviceComponent(componentType)) {
      console.warn('未找到组件类型:', componentType)
      return null
    }
    
    return {
      component: getDeviceComponent(componentType),
      componentType,
      config: device.componentConfig || device.displayConfig?.componentConfig || {}
    }
  }

  /**
   * 生成设备的RTSP流地址
   */
  const generateStreamUrl = (device: any) => {
    const config = device.config ? JSON.parse(device.config) : {}
    const { ip, username, password } = config
    
    if (!ip || !username || !password) {
      return null
    }
    
    // 根据产品类型生成对应的RTSP地址
    if (device.productId === 61 || ip === '192.168.1.201') {
      // 海康威视
      return `rtsp://${username}:${password}@${ip}:554/Streaming/Channels/101`
    } else if (device.productId === 62 || ip === '192.168.1.202') {
      // 大华摄像机
      return `rtsp://${username}:${password}@${ip}:554/cam/realmonitor?channel=1&subtype=0`
    }
    
    // 通用格式
    return `rtsp://${username}:${password}@${ip}:554/stream1`
  }

  /**
   * 获取设备支持的功能列表
   */
  const getDeviceCapabilities = (device: any) => {
    const displayConfig = device.displayConfig || {}
    const componentConfig = displayConfig.componentConfig || {}
    
    // 从组件配置中提取功能列表
    const capabilities = []
    
    if (componentConfig.controls?.includes('ptz')) {
      capabilities.push('云台控制')
    }
    if (componentConfig.controls?.includes('record')) {
      capabilities.push('录像')
    }
    if (componentConfig.controls?.includes('snapshot')) {
      capabilities.push('截图')
    }
    if (componentConfig.streamConfig) {
      capabilities.push('视频流')
    }
    
    return capabilities
  }

  /**
   * 按组件类型分组设备
   */
  const devicesByComponent = computed(() => {
    const groups: Record<string, any[]> = {}
    
    devices.value.forEach(device => {
      const componentType = device.componentType || 'default'
      if (!groups[componentType]) {
        groups[componentType] = []
      }
      groups[componentType].push(device)
    })
    
    return groups
  })

  /**
   * 获取在线设备数量
   */
  const onlineDeviceCount = computed(() => {
    return devices.value.filter(device => device.state === 1).length
  })

  /**
   * 获取支持指定功能的设备
   */
  const getDevicesByCapability = (capability: string) => {
    return devices.value.filter(device => {
      const capabilities = getDeviceCapabilities(device)
      return capabilities.includes(capability)
    })
  }

  return {
    devices,
    displayConfigs,
    componentTemplates,
    loading,
    devicesByComponent,
    onlineDeviceCount,
    
    // 方法
    getModuleDevices,
    getDeviceComponentInfo,
    generateStreamUrl,
    getDeviceCapabilities,
    getDevicesByCapability
  }
}





















