import request from '@/config/axios'

// IoT 设备展示配置 VO
export interface DeviceDisplayConfigVO {
  id: number
  productId: number
  moduleCode: string
  moduleName: string
  pagePath: string
  componentType: string
  componentConfig: any
  sort: number
  status: number
}

// UI组件模板 VO
export interface UIComponentTemplateVO {
  id: number
  name: string
  componentType: string
  description: string
  templateConfig: any
  previewImage: string
  supportedModules: string[]
  status: number
}

// 设备展示信息 VO
export interface DeviceDisplayInfoVO {
  deviceId: number
  deviceName: string
  productId: number
  productName: string
  componentType: string
  componentConfig: any
  moduleConfigs: DeviceDisplayConfigVO[]
  streamUrl?: string
  status: number
  config: any
}

// 获取设备展示配置列表
export const getDeviceDisplayConfigList = (productId?: number) => {
  return request.get({
    url: '/iot/device-display-config/list',
    params: { productId }
  })
}

// 获取UI组件模板列表
export const getUIComponentTemplateList = () => {
  return request.get({
    url: '/iot/ui-component-template/list'
  })
}

// 获取指定模块的设备列表（带展示配置）
export const getDevicesForModule = (moduleCode: string) => {
  return request.get({
    url: '/iot/device-display/module-devices',
    params: { moduleCode }
  })
}

// 获取设备的完整展示信息
export const getDeviceDisplayInfo = (deviceId: number) => {
  return request.get({
    url: `/iot/device-display/info/${deviceId}`
  })
}

// 动态渲染设备组件配置
export const getDeviceComponentConfig = (deviceId: number, componentType: string) => {
  return request.get({
    url: '/iot/device-display/component-config',
    params: { deviceId, componentType }
  })
}

// 保存设备展示配置
export const saveDeviceDisplayConfig = (data: Partial<DeviceDisplayConfigVO>) => {
  return request.post({
    url: '/iot/device-display-config/save',
    data
  })
}

// 更新产品展示配置
export const updateProductDisplayConfig = (productId: number, data: any) => {
  return request.put({
    url: `/iot/product/display-config/${productId}`,
    data
  })
}





















