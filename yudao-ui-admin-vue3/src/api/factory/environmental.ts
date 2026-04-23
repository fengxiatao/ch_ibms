/**
 * 文件说明：智慧工厂环保监测 API
 *
 * <p>说明：独立页 `环保监测` 与合规管理中的环保监测 Tab 完全解耦，
 * 当前文件仅维护 `/iot/factory/environmental/dashboard` 聚合接口与类型定义。</p>
 */
import request from '@/config/axios'

/**
 * 顶部 KPI 项
 */
export interface EnvironmentalKpiCard {
  key: string
  title: string
  value?: number | null
  valueText: string
  unit?: string | null
  icon: string
  theme: string
  status?: string | null
}

/**
 * 废气明细项
 */
export interface EnvironmentalAirEmissionItem {
  pointCode: string
  pointName: string
  value?: number | null
  valueText: string
  unit?: string | null
  limitValue?: number | null
  limitValueText: string
  status: string
  tone: string
  progressPercent?: number | null
  limitMarkerPercent?: number | null
}

/**
 * 废气卡
 */
export interface EnvironmentalAirEmissionCard {
  title: string
  overallStatusText: string
  items: EnvironmentalAirEmissionItem[]
}

/**
 * 废水明细项
 */
export interface EnvironmentalWastewaterItem {
  pointCode: string
  pointName: string
  value?: number | null
  valueText: string
  displayUnitText?: string | null
  status: string
  tone: string
  theme: string
  progressPercent?: number | null
}

/**
 * 废水卡
 */
export interface EnvironmentalWastewaterCard {
  title: string
  overallStatusText: string
  items: EnvironmentalWastewaterItem[]
}

/**
 * 噪声仪表项
 */
export interface EnvironmentalNoiseGauge {
  label: string
  value?: number | null
  valueText: string
  unit?: string | null
  tone: string
  percent?: number | null
}

/**
 * 噪声限值项
 */
export interface EnvironmentalNoiseLimit {
  label: string
  value?: number | null
  valueText: string
  unit?: string | null
}

/**
 * 噪声卡
 */
export interface EnvironmentalNoiseCard {
  title: string
  day: EnvironmentalNoiseGauge
  night: EnvironmentalNoiseGauge
  limit: EnvironmentalNoiseLimit
}

/**
 * 预警项
 */
export interface EnvironmentalAlertItem {
  id: number
  title: string
  happenedAt?: string | null
  currentValue?: number | null
  currentValueText: string
  limitValue?: number | null
  limitValueText: string
  unit?: string | null
  level: string
  tone: string
  description?: string | null
}

/**
 * 工作台响应
 */
export interface EnvironmentalDashboardData {
  updatedAt?: string | null
  kpiCards: EnvironmentalKpiCard[]
  airEmission: EnvironmentalAirEmissionCard
  wastewater: EnvironmentalWastewaterCard
  noise: EnvironmentalNoiseCard
  alerts: EnvironmentalAlertItem[]
}

/**
 * 获取环保监测工作台数据
 *
 * @returns 工作台数据
 */
export const getEnvironmentalDashboard = () => {
  return request.get<EnvironmentalDashboardData>({
    url: '/iot/factory/environmental/dashboard'
  })
}
