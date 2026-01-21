import request from '@/config/axios'

// IoT 设备 VO
export interface DeviceVO {
  id: number // 设备 ID，主键，自增
  deviceName: string // 设备名称
  productId: number // 产品编号
  productKey: string // 产品标识
  deviceType: number // 设备类型
  nickname: string // 设备备注名称
  gatewayId: number // 网关设备 ID
  state: number // 设备状态
  onlineTime: Date // 最后上线时间
  offlineTime: Date // 最后离线时间
  activeTime: Date // 设备激活时间
  createTime: Date // 创建时间
  ipAddress: string // 设备的 IP 地址（从 config 中提取）
  firmwareVersion: string // 设备的固件版本
  deviceSecret: string // 设备密钥，用于设备认证，需安全存储
  mqttClientId: string // MQTT 客户端 ID
  mqttUsername: string // MQTT 用户名
  mqttPassword: string // MQTT 密码
  authType: string // 认证类型
  locationType: number // 定位类型
  latitude?: number // 设备位置的纬度
  longitude?: number // 设备位置的经度
  areaId: number // 地区编码
  address: string // 设备详细地址
  serialNumber: string // 设备序列号
  dxfEntityId?: string // DXF实体唯一标识（用于关联DXF图纸中的设备）
  config: string // 设备配置
  groupIds?: number[] // 添加分组 ID
  
  // 空间定位字段（用于平面图编辑器）
  campusId?: number // 所属园区ID
  buildingId?: number // 所属建筑ID
  floorId?: number // 所属楼层ID
  roomId?: number // 所属房间ID
  localX?: number // 本地X坐标（米）
  localY?: number // 本地Y坐标（米）
  localZ?: number // 本地Z坐标（安装高度，米）
  installHeightType?: string // 安装高度类型
  installLocation?: string // 安装位置描述
  
  // 菜单配置字段
  menuIds?: string // 关联的菜单ID列表（JSON数组）
  primaryMenuId?: number // 主要菜单ID
  menuOverride?: boolean // 是否手动覆盖菜单配置
  
  // 实际生效的配置（只读）
  effectiveMenuIds?: string // 实际生效的菜单ID列表
  effectivePrimaryMenuId?: number // 实际生效的主要菜单ID
  menuConfigSource?: 'product' | 'device' // 配置来源
}

// 设备菜单配置 VO
export interface DeviceMenuConfigVO {
  menuIds: string // 菜单ID列表（JSON数组）
  primaryMenuId: number // 主要菜单ID
  source: 'product' | 'device' // 配置来源
}

// IoT 设备属性详细 VO
export interface IotDevicePropertyDetailRespVO {
  identifier: string // 属性标识符
  value: string // 最新值
  updateTime: Date // 更新时间
  name: string // 属性名称
  dataType: string // 数据类型
  dataSpecs: any // 数据定义
  dataSpecsList: any[] // 数据定义列表
}

// IoT 设备属性 VO
export interface IotDevicePropertyRespVO {
  identifier: string // 属性标识符
  value: string // 最新值
  updateTime: Date // 更新时间
}

// TODO @长辉开发团队：调整到 constants
// IoT 设备状态枚举
export enum DeviceStateEnum {
  INACTIVE = 0, // 未激活
  ONLINE = 1, // 在线
  OFFLINE = 2, // 离线
  ACTIVATED = 3 // 已激活（被动连接设备首次连接）
}

// 设备认证参数 VO
export interface IotDeviceAuthInfoVO {
  clientId: string // 客户端 ID
  username: string // 用户名
  password: string // 密码
}

// IoT 设备发送消息 Request VO
export interface IotDeviceMessageSendReqVO {
  deviceId: number // 设备编号
  method: string // 请求方法
  params?: any // 请求参数
}

// 设备 API
export const DeviceApi = {
  // 查询设备分页
  getDevicePage: async (params: any) => {
    return await request.get({ url: `/iot/device/page`, params })
  },

  // 查询设备详情
  getDevice: async (id: number) => {
    return await request.get({ url: `/iot/device/get?id=` + id })
  },

  // 新增设备
  createDevice: async (data: DeviceVO) => {
    return await request.post({ url: `/iot/device/create`, data })
  },

  // 修改设备
  updateDevice: async (data: DeviceVO) => {
    return await request.put({ url: `/iot/device/update`, data })
  },

  // 修改设备分组
  updateDeviceGroup: async (data: { ids: number[]; groupIds: number[] }) => {
    return await request.put({ url: `/iot/device/update-group`, data })
  },

  // 删除单个设备
  deleteDevice: async (id: number) => {
    return await request.delete({ url: `/iot/device/delete?id=` + id })
  },

  // 删除多个设备
  deleteDeviceList: async (ids: number[]) => {
    return await request.delete({ url: `/iot/device/delete-list`, params: { ids: ids.join(',') } })
  },

  // 导出设备
  exportDeviceExcel: async (params: any) => {
    return await request.download({ url: `/iot/device/export-excel`, params })
  },

  // 获取设备数量
  getDeviceCount: async (productId: number) => {
    return await request.get({ url: `/iot/device/count?productId=` + productId })
  },

  // 获取设备的精简信息列表
  getSimpleDeviceList: async (deviceType?: number, productId?: number) => {
    return await request.get({ url: `/iot/device/simple-list?`, params: { deviceType, productId } })
  },

  // 根据产品编号，获取设备的精简信息列表
  getDeviceListByProductId: async (productId: number) => {
    return await request.get({ url: `/iot/device/simple-list?`, params: { productId } })
  },

  // 获取导入模板
  importDeviceTemplate: async () => {
    return await request.download({ url: `/iot/device/get-import-template` })
  },

  // 获取设备属性最新数据
  getLatestDeviceProperties: async (params: any) => {
    return await request.get({ url: `/iot/device/property/get-latest`, params })
  },

  // 获取设备属性历史数据
  getHistoryDevicePropertyList: async (params: any) => {
    return await request.get({ url: `/iot/device/property/history-list`, params })
  },

  // 获取设备认证信息
  getDeviceAuthInfo: async (id: number) => {
    return await request.get({ url: `/iot/device/get-auth-info`, params: { id } })
  },

  // 查询设备消息分页
  getDeviceMessagePage: async (params: any) => {
    return await request.get({ url: `/iot/device/message/page`, params })
  },

  // 查询设备消息配对分页
  getDeviceMessagePairPage: async (params: any) => {
    return await request.get({ url: `/iot/device/message/pair-page`, params })
  },

  // 发送设备消息
  sendDeviceMessage: async (params: IotDeviceMessageSendReqVO) => {
    return await request.post({ url: `/iot/device/message/send`, data: params })
  }
}

// 兼容命名导出：部分代码可能使用 { getDevicePage } 或 { devicePage } 的方式导入。
// 为避免 Rollup 构建阶段出现“未导出”错误，这里提供同名的函数别名导出。
/**
 * 设备分页查询（命名导出别名）
 *
 * 用途：兼容以 `{ getDevicePage }` 方式的命名导入，调用设备分页查询接口。
 * @param params 任意查询参数对象；例如包含分页信息、筛选条件等。
 * @returns Promise<any> 返回接口响应的分页数据对象。
 * @throws 请求失败时抛出网络或后端异常（由 axios 封装抛出）。
 */
export const getDevicePage = DeviceApi.getDevicePage

/**
 * 设备分页查询（命名导出别名：devicePage）
 *
 * 用途：兼容历史代码中以 `{ devicePage }` 命名导入的使用方式。
 * @param params 任意查询参数对象；例如包含分页信息、筛选条件等。
 * @returns Promise<any> 返回接口响应的分页数据对象。
 * @throws 请求失败时抛出网络或后端异常（由 axios 封装抛出）。
 */
export const devicePage = DeviceApi.getDevicePage
