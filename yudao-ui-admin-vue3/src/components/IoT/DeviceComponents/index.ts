// IoT设备动态组件注册中心
import { App } from 'vue'

// 导入设备组件
import VideoCamera from './VideoCamera.vue'
import VideoPlayback from './VideoPlayback.vue'
import NVRManager from './NVRManager.vue'
import CameraCard from './CameraCard.vue'
import NVRCard from './NVRCard.vue'

// 组件映射表
export const deviceComponentMap = {
  VideoCamera,
  VideoPlayback,
  NVRManager,
  CameraCard,
  NVRCard
}

// 组件类型定义
export type DeviceComponentType = keyof typeof deviceComponentMap

// 注册所有设备组件
export const registerDeviceComponents = (app: App) => {
  Object.entries(deviceComponentMap).forEach(([name, component]) => {
    app.component(`IoT${name}`, component)
  })
}

// 动态获取组件
export const getDeviceComponent = (componentType: DeviceComponentType) => {
  return deviceComponentMap[componentType]
}

// 检查组件是否存在
export const hasDeviceComponent = (componentType: string): componentType is DeviceComponentType => {
  return componentType in deviceComponentMap
}





















