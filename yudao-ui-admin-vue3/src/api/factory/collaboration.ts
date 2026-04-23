import request from '@/config/axios'

/**
 * 文件说明：智慧工厂业务协同 API
 */

/**
 * 业务协同指标卡
 */
export interface CollaborationMetricCard {
  key: string
  title: string
  value: string
  unit?: string
  hint?: string
  trend?: string
  icon?: string
  theme?: 'cyan' | 'emerald' | 'amber' | 'violet'
}

/**
 * 生产协同工作台响应
 */
export interface ProductionDashboardData {
  updatedAt?: string
  metrics: CollaborationMetricCard[]
  planList: Array<{
    id: number
    planCode: string
    productName: string
    batchCode: string
    plannedQuantity: number
    operatorName: string
    lineName: string
    plannedStartTime: string
    status: string
    progress: number
  }>
  batchList: Array<{
    id: number
    planId: number
    batchCode: string
    productName: string
    currentProcess: string
    currentLocation: string
    completedQuantity: number
    yieldRate: number
    status: string
    updatedAt: string
  }>
  alertList: Array<{
    id: number
    alertTitle: string
    levelLabel: string
    lineName: string
    status: string
    handlerName: string
    happenedAt: string
  }>
}

/**
 * 批次追溯详情响应
 */
export interface ProductionBatchTraceDetailData {
  summary?: {
    id: number
    planId: number
    planCode: string
    batchCode: string
    productName: string
    lineName: string
    operatorName: string
    currentProcess: string
    currentLocation: string
    plannedQuantity: number
    completedQuantity: number
    yieldRate: number
    status: string
    plannedStartTime: string
    updatedAt: string
  } | null
  environmentRecords: Array<{
    id: number
    recordTime: string
    temperatureValue: number
    humidityValue: number
    pressureValue: number
    phValue: number
    cleanLevel: string
    recorderName: string
  }>
  qualityRecords: Array<{
    id: number
    sampleName: string
    inspectionItem: string
    standardValue: string
    measuredValue: string
    resultStatus: string
    recordTime: string
    inspectorName: string
  }>
  personnelRecords: Array<{
    id: number
    roleName: string
    staffName: string
    operationName: string
    workstationName: string
    recordTime: string
    durationMinutes: number
    remark?: string
  }>
  deviceRecords: Array<{
    id: number
    deviceCode: string
    deviceName: string
    operationName: string
    runningStatus: string
    parameterSummary?: string
    recordTime: string
    operatorName?: string
  }>
  materialRecords: Array<{
    id: number
    materialCode: string
    materialName: string
    materialType: string
    materialBatchNo: string
    plannedQuantity: number
    actualQuantity: number
    unit: string
    feederName?: string
    recordTime: string
  }>
}

/**
 * 能源工作台响应
 */
export interface EnergyDashboardData {
  updatedAt?: string
  subTab: string
  metrics: CollaborationMetricCard[]
  trendList: Array<{
    label: string
    electricityValue: number
    waterValue: number
    gasValue: number
    currentValue: number
  }>
  areaRanking: Array<{
    name: string
    value: number
    unit: string
    extraText: string
  }>
  deviceRanking: Array<{
    name: string
    value: number
    unit: string
    extraText: string
  }>
  suggestionList: Array<{
    id: number
    title: string
    content: string
    levelLabel: string
    status: string
  }>
}

/**
 * 设备工作台响应
 */
export interface DeviceDashboardData {
  updatedAt?: string
  subTab: string
  metrics: CollaborationMetricCard[]
  deviceList: Array<{
    id: number
    deviceCode: string
    deviceName: string
    categoryName: string
    areaName: string
    online: boolean
    runningStatus: string
    healthStatus: string
    efficiencyRate: number
    statusText: string
  }>
  maintenancePlanList: Array<{
    id: number
    deviceId: number
    planName: string
    deviceName: string
    cycleType: string
    nextExecuteDate: string
    ownerName: string
    status: string
    pendingOrderCount: number
    latestOrderId?: number
  }>
  detail?: {
    id: number
    deviceCode: string
    deviceName: string
    categoryName: string
    modelName: string
    areaName: string
    online: boolean
    runningStatus: string
    healthStatus: string
    efficiencyRate: number
    ownerName?: string
    lastMaintenanceDate?: string
    nextMaintenanceDate?: string
    remark?: string
  } | null
}

/**
 * 碳资产工作台响应
 */
export interface CarbonDashboardData {
  updatedAt?: string
  subTab: string
  metrics: CollaborationMetricCard[]
  trendList: Array<{
    label: string
    emissionValue: number
    targetValue: number
  }>
  sourceList: Array<{
    id: number
    sourceName: string
    sourceType: string
    emissionValue: number
    proportion: number
  }>
  tradeList: Array<{
    id: number
    tradeCode: string
    tradeType: string
    quantity: number
    unitPrice: number
    amount: number
    balanceAfter: number
    tradeDate: string
    counterparty: string
    status: string
  }>
  targetCard: {
    annualTargetValue: number
    emittedValue: number
    remainingValue: number
    completionRate: number
  }
}

/**
 * 获取生产协同工作台
 *
 * @param params 查询参数
 * @returns 工作台数据
 */
export const getProductionDashboard = (params: { subTab: string; keyword?: string }) => {
  return request.get<ProductionDashboardData>({ url: '/iot/factory/collaboration/production/dashboard', params })
}

/**
 * 获取批次追溯详情
 *
 * @param params 查询参数
 * @returns 批次详情
 */
export const getProductionBatchTraceDetail = (params: { batchId: number }) => {
  return request.get<ProductionBatchTraceDetailData>({
    url: '/iot/factory/collaboration/production/batch-trace/detail',
    params
  })
}

/**
 * 创建生产计划
 *
 * @param data 请求体
 * @returns 计划主键
 */
export const createProductionPlan = (data: {
  planCode: string
  productName: string
  batchCode: string
  lineName: string
  plannedQuantity: number
  operatorName: string
  plannedStartTime: string
}) => {
  return request.post<number>({ url: '/iot/factory/collaboration/production/plan/create', data })
}

/**
 * 更新生产计划状态
 *
 * @param data 请求体
 * @returns 是否成功
 */
export const updateProductionPlanStatus = (data: {
  id: number
  status: string
  progress: number
  remark?: string
}) => {
  return request.put<boolean>({
    url: '/iot/factory/collaboration/production/plan/update-status',
    data
  })
}

/**
 * 获取能源工作台
 *
 * @param params 查询参数
 * @returns 工作台数据
 */
export const getEnergyDashboard = (params: { subTab: string; dateMode?: string; statDate?: string }) => {
  return request.get<EnergyDashboardData>({ url: '/iot/factory/collaboration/energy/dashboard', params })
}

/**
 * 处理节能建议
 *
 * @param data 请求体
 * @returns 是否成功
 */
export const handleEnergySuggestion = (data: { id: number; status: string }) => {
  return request.put<boolean>({ url: '/iot/factory/collaboration/energy/suggestion/handle', data })
}

/**
 * 获取设备工作台
 *
 * @param params 查询参数
 * @returns 工作台数据
 */
export const getDeviceDashboard = (params: {
  subTab: string
  keyword?: string
  selectedDeviceId?: number
}) => {
  return request.get<DeviceDashboardData>({ url: '/iot/factory/collaboration/device/dashboard', params })
}

/**
 * 创建设备
 *
 * @param data 请求体
 * @returns 设备主键
 */
export const createDevice = (data: {
  deviceCode: string
  deviceName: string
  categoryName: string
  modelName: string
  areaName: string
  online?: boolean
  runningStatus?: string
  efficiencyRate?: number
}) => {
  return request.post<number>({ url: '/iot/factory/collaboration/device/create', data })
}

/**
 * 创建维保计划
 *
 * @param data 请求体
 * @returns 计划主键
 */
export const createMaintenancePlan = (data: {
  deviceId: number
  planName: string
  cycleType: string
  nextExecuteDate: string
  ownerName: string
}) => {
  return request.post<number>({
    url: '/iot/factory/collaboration/device/maintenance-plan/create',
    data
  })
}

/**
 * 完成维保工单
 *
 * @param data 请求体
 * @returns 是否成功
 */
export const completeMaintenanceOrder = (data: {
  orderId: number
  result: string
  remark?: string
}) => {
  return request.put<boolean>({
    url: '/iot/factory/collaboration/device/maintenance-order/complete',
    data
  })
}

/**
 * 获取碳资产工作台
 *
 * @param params 查询参数
 * @returns 工作台数据
 */
export const getCarbonDashboard = (params: { subTab: string; statDate?: string }) => {
  return request.get<CarbonDashboardData>({ url: '/iot/factory/collaboration/carbon/dashboard', params })
}

/**
 * 新增碳交易
 *
 * @param data 请求体
 * @returns 交易主键
 */
export const createCarbonTrade = (data: {
  tradeCode: string
  tradeType: string
  quantity: number
  unitPrice: number
  tradeDate: string
  counterparty: string
}) => {
  return request.post<number>({ url: '/iot/factory/collaboration/carbon/trade/create', data })
}
