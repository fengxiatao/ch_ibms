import request from '@/config/axios'

// ======================= 仪表相关接口 =======================

export interface IbmsEnergyMeterVO {
  id?: number
  /** 关联的 IBMS 设备台账 ID（ibms_device.id） */
  ibmsDeviceId?: number
  /** 关联的 IBMS 设备名称（仅展示，后端联表填充） */
  ibmsDeviceName?: string
  /** 关联的 IBMS 设备编码（仅展示） */
  ibmsDeviceCode?: string
  /** 关联的 IBMS 设备 IP（仅展示） */
  ibmsDeviceIp?: string
  /** 关联的 IBMS 设备空间（仅展示） */
  ibmsDeviceSpace?: string
  meterCode?: string
  meterName?: string
  meterType?: number
  model?: string
  brand?: string
  areaId?: number
  areaName?: string
  installLocation?: string
  ratio?: number
  currentReading?: number
  lastReading?: number
  todayUsage?: number
  monthUsage?: number
  status?: number
  lastCommunicateTime?: Date
  createTime?: Date
}

export interface IbmsEnergyMeterPageReqVO extends PageParam {
  meterCode?: string
  meterName?: string
  meterType?: number
  areaId?: number
  status?: number
}

export interface IbmsEnergyMeterSaveReqVO {
  id?: number
  meterCode?: string
  meterName?: string
  meterType?: number
  model?: string
  brand?: string
  areaId?: number
  areaName?: string
  installLocation?: string
  ratio?: number
  status?: number
}

// 获得能耗仪表分页
export const getMeterPage = (params: IbmsEnergyMeterPageReqVO) => {
  return request.get({ url: '/iot/building/energy/meter/page', params })
}

// 获得能耗仪表详情
export const getMeter = (id: number) => {
  return request.get({ url: '/iot/building/energy/meter/get?id=' + id })
}

// 获得仪表列表（按类型）
export const getMeterListByType = (meterType: number) => {
  return request.get({ url: '/iot/building/energy/meter/list-by-type', params: { meterType } })
}

// 获得全部仪表列表
export const getMeterList = () => {
  return request.get({ url: '/iot/building/energy/meter/list' })
}

// 创建能耗仪表
export const createMeter = (data: IbmsEnergyMeterSaveReqVO) => {
  return request.post({ url: '/iot/building/energy/meter/create', data })
}

// 更新能耗仪表
export const updateMeter = (data: IbmsEnergyMeterSaveReqVO) => {
  return request.put({ url: '/iot/building/energy/meter/update', data })
}

// 删除能耗仪表
export const deleteMeter = (id: number) => {
  return request.delete({ url: '/iot/building/energy/meter/delete?id=' + id })
}

// 绑定/解绑能耗仪表到 IBMS 设备台账（ibmsDeviceId 传 null 表示解绑；幂等）
export const bindMeterDevice = (meterId: number, ibmsDeviceId: number | null) => {
  const params: Record<string, number | undefined> = { meterId }
  if (ibmsDeviceId != null) {
    params.ibmsDeviceId = ibmsDeviceId
  }
  return request.post({ url: '/iot/building/energy/meter/bind-device', params })
}

// ======================= 采集记录相关接口 =======================

export interface IbmsEnergyRecordVO {
  id?: number
  meterId?: number
  meterCode?: string
  meterName?: string
  reading?: number
  usage?: number
  collectTime?: Date
}

export interface IbmsEnergyRecordPageReqVO extends PageParam {
  meterId?: number
  meterCode?: string
  startTime?: Date
  endTime?: Date
}

// 获得能耗采集记录分页
export const getRecordPage = (params: IbmsEnergyRecordPageReqVO) => {
  return request.get({ url: '/iot/building/energy/record/page', params })
}

// 获得仪表最新采集记录
export const getLatestRecord = (meterId: number) => {
  return request.get({ url: '/iot/building/energy/record/latest?meterId=' + meterId })
}

// ======================= 统计相关接口 =======================

export interface IbmsEnergyStatisticsVO {
  id?: number
  statisticsDate?: string
  meterId?: number
  meterCode?: string
  meterName?: string
  meterType?: number
  areaId?: number
  areaName?: string
  startReading?: number
  endReading?: number
  dailyUsage?: number
  peakUsage?: number
  valleyUsage?: number
  flatUsage?: number
  yoyGrowthRate?: number
  momGrowthRate?: number
}

export interface IbmsEnergyStatisticsPageReqVO extends PageParam {
  meterId?: number
  meterType?: number
  areaId?: number
  startDate?: string
  endDate?: string
}

// 获得能耗日统计分页
export const getStatisticsPage = (params: IbmsEnergyStatisticsPageReqVO) => {
  return request.get({ url: '/iot/building/energy/statistics/page', params })
}

// 获得指定日期范围的能耗统计
export const getStatisticsByDateRange = (startDate: string, endDate: string) => {
  return request.get({ url: '/iot/building/energy/statistics/by-date-range', params: { startDate, endDate } })
}

// 获得仪表指定日期范围的能耗统计
export const getMeterStatisticsByDateRange = (meterId: number, startDate: string, endDate: string) => {
  return request.get({ url: '/iot/building/energy/statistics/meter-by-date-range', params: { meterId, startDate, endDate } })
}

// 按类型获得指定日期范围的能耗统计
export const getStatisticsByTypeAndDateRange = (meterType: number, startDate: string, endDate: string) => {
  return request.get({ url: '/iot/building/energy/statistics/by-type-and-date-range', params: { meterType, startDate, endDate } })
}

// 类型别名
export type IbmsEnergyStatisticsDailyVO = IbmsEnergyStatisticsVO

// ======================= 告警相关接口 =======================

export interface IbmsEnergyAlarmVO {
  id?: number
  meterId?: number
  meterCode?: string
  meterName?: string
  alarmType?: number
  alarmLevel?: number
  alarmContent?: string
  alarmValue?: number
  threshold?: number
  alarmTime?: Date
  status?: number
  handleTime?: Date
  handler?: string
  handleRemark?: string
}

export interface IbmsEnergyAlarmPageReqVO extends PageParam {
  meterId?: number
  meterName?: string
  alarmType?: number
  alarmLevel?: number
  status?: number
  startTime?: Date
  endTime?: Date
}

// 获得能耗告警分页
export const getAlarmPage = (params: IbmsEnergyAlarmPageReqVO) => {
  return request.get({ url: '/iot/building/energy/alarm/page', params })
}

// 获得告警详情
export const getAlarm = (id: number) => {
  return request.get({ url: '/iot/building/energy/alarm/get?id=' + id })
}

// 处理告警
export const handleAlarm = (id: number, handler: string, handleRemark?: string) => {
  return request.put({ url: '/iot/building/energy/alarm/handle', params: { id, handler, handleRemark } })
}

// 忽略告警
export const ignoreAlarm = (id: number, handler: string, handleRemark?: string) => {
  return request.put({ url: '/iot/building/energy/alarm/ignore', params: { id, handler, handleRemark } })
}

// 获得今日告警数量
export const getTodayAlarmCount = () => {
  return request.get({ url: '/iot/building/energy/alarm/today-count' })
}

// 获得未处理告警数量
export const getUnhandledAlarmCount = () => {
  return request.get({ url: '/iot/building/energy/alarm/unhandled-count' })
}

// ======================= 费率设置相关接口 =======================

export interface IbmsEnergyRateVO {
  id?: number
  rateName?: string
  energyType?: number
  rateLevel?: number
  unitPrice?: number
  startTime?: string
  endTime?: string
  effectiveDate?: string
  expiryDate?: string
  status?: number
  remark?: string
  createTime?: Date
}

export interface IbmsEnergyRatePageReqVO extends PageParam {
  rateName?: string
  energyType?: number
  rateLevel?: number
  status?: number
}

export interface IbmsEnergyRateSaveReqVO {
  id?: number
  rateName?: string
  energyType?: number
  rateLevel?: number
  unitPrice?: number
  startTime?: string
  endTime?: string
  effectiveDate?: string
  expiryDate?: string
  status?: number
  remark?: string
}

// 获得费率设置分页
export const getRatePage = (params: IbmsEnergyRatePageReqVO) => {
  return request.get({ url: '/iot/building/energy/rate/page', params })
}

// 获得费率设置详情
export const getRate = (id: number) => {
  return request.get({ url: '/iot/building/energy/rate/get?id=' + id })
}

// 获得费率列表（按能源类型）
export const getRateListByEnergyType = (energyType: number) => {
  return request.get({ url: '/iot/building/energy/rate/list-by-energy-type', params: { energyType } })
}

// 创建费率设置
export const createRate = (data: IbmsEnergyRateSaveReqVO) => {
  return request.post({ url: '/iot/building/energy/rate/create', data })
}

// 更新费率设置
export const updateRate = (data: IbmsEnergyRateSaveReqVO) => {
  return request.put({ url: '/iot/building/energy/rate/update', data })
}

// 删除费率设置
export const deleteRate = (id: number) => {
  return request.delete({ url: '/iot/building/energy/rate/delete?id=' + id })
}

// ======================= 人工抄表相关接口 =======================

export interface IbmsEnergyManualReadingVO {
  id?: number
  meterId?: number
  meterCode?: string
  meterName?: string
  meterType?: number
  areaName?: string
  readingValue?: number
  lastReadingValue?: number
  usageValue?: number
  readingTime?: Date
  reader?: string
  readingImage?: string
  status?: number
  reviewer?: string
  reviewTime?: Date
  remark?: string
  createTime?: Date
}

export interface IbmsEnergyManualReadingPageReqVO extends PageParam {
  meterId?: number
  meterCode?: string
  meterType?: number
  reader?: string
  status?: number
  startTime?: Date
  endTime?: Date
}

export interface IbmsEnergyManualReadingSaveReqVO {
  id?: number
  meterId?: number
  readingValue?: number
  readingTime?: Date
  reader?: string
  readingImage?: string
  remark?: string
}

// 获得人工抄表记录分页
export const getManualReadingPage = (params: IbmsEnergyManualReadingPageReqVO) => {
  return request.get({ url: '/iot/building/energy/manual-reading/page', params })
}

// 获得今日人工抄表记录
export const getTodayManualReadings = () => {
  return request.get({ url: '/iot/building/energy/manual-reading/today' })
}

// 获得仪表最新人工抄表记录
export const getLatestManualReading = (meterId: number) => {
  return request.get({ url: '/iot/building/energy/manual-reading/latest?meterId=' + meterId })
}

// 创建人工抄表记录
export const createManualReading = (data: IbmsEnergyManualReadingSaveReqVO) => {
  return request.post({ url: '/iot/building/energy/manual-reading/create', data })
}

// 复核人工抄表记录
export const reviewManualReading = (id: number, reviewer: string) => {
  return request.put({ url: '/iot/building/energy/manual-reading/review', params: { id, reviewer } })
}

// 作废人工抄表记录
export const voidManualReading = (id: number) => {
  return request.put({ url: '/iot/building/energy/manual-reading/void?id=' + id })
}

// ======================= 总览相关接口 =======================

export interface IbmsEnergyOverviewVO {
  meterTotalCount?: number
  meterOnlineCount?: number
  meterOfflineCount?: number
  meterFaultCount?: number
  electricMeterCount?: number
  waterMeterCount?: number
  gasMeterCount?: number
  coldMeterCount?: number
  heatMeterCount?: number
  todayElectricity?: number
  todayWater?: number
  todayGas?: number
  todayCold?: number
  todayHeat?: number
  monthElectricity?: number
  monthWater?: number
  monthGas?: number
  monthCold?: number
  monthHeat?: number
  electricityYoy?: number
  electricityMom?: number
  waterYoy?: number
  waterMom?: number
  todayAlarmCount?: number
  unhandledAlarmCount?: number
}

// 获得能耗总览数据
export const getOverview = () => {
  return request.get<IbmsEnergyOverviewVO>({ url: '/iot/building/energy/overview' })
}

// 按日期范围获得能耗总览数据
export const getOverviewByRange = (startDate: string, endDate: string) => {
  return request.get<IbmsEnergyOverviewVO>({
    url: '/iot/building/energy/overview-by-range',
    params: { startDate, endDate }
  })
}
