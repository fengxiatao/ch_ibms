/**
 * 文件说明：智慧工厂合规管理 API
 *
 * <p>说明：合规管理页面包含 GMP 合规、环保监测、批次追溯三个独立 Tab，
 * 此文件统一维护其前端请求方法与类型定义。</p>
 */
import request from '@/config/axios'

/**
 * 当前 Tab 类型
 */
export type ComplianceTab = 'gmp' | 'environment' | 'batch-trace'

/**
 * 顶部指标卡
 */
export interface ComplianceMetricItem {
  key: string
  title: string
  value: string
  unit?: string
  icon: string
  theme: string
}

/**
 * 概览卡
 */
export interface ComplianceSummaryCardItem {
  id: number
  title: string
  subtitle: string
  progressRate: number
  progressText: string
  tone: string
}

/**
 * GMP 明细行
 */
export interface GmpInspectionRowItem {
  id: number
  pointName: string
  compliantCount: number
  exceedCount: number
  status: string
  lastCheckTime: string
  detailText: string
}

/**
 * 环保监测明细行
 */
export interface EnvironmentalMonitorRowItem {
  id: number
  pointName: string
  currentValue: number
  standardValue: number
  exceedCount: number
  status: string
  lastCheckTime: string
  detailText: string
  unit: string
}

/**
 * 合规批次追溯明细行
 */
export interface ComplianceBatchTraceRowItem {
  id: number
  batchCode: string
  productName: string
  checkpointCount: number
  issueCount: number
  status: string
  lastCheckTime: string
  detailText: string
}

/**
 * GMP 总览
 */
export interface ComplianceGmpOverview {
  regionCards: ComplianceSummaryCardItem[]
  detailList: GmpInspectionRowItem[]
}

/**
 * 环保监测总览
 */
export interface ComplianceEnvironmentalOverview {
  regionCards: ComplianceSummaryCardItem[]
  detailList: EnvironmentalMonitorRowItem[]
}

/**
 * 合规批次追溯总览
 */
export interface ComplianceBatchOverview {
  batchCards: ComplianceSummaryCardItem[]
  detailList: ComplianceBatchTraceRowItem[]
}

/**
 * 工作台响应
 */
export interface ComplianceDashboardData {
  updatedAt: string
  tab: ComplianceTab
  metrics: ComplianceMetricItem[]
  gmpOverview: ComplianceGmpOverview
  environmentalOverview: ComplianceEnvironmentalOverview
  batchOverview: ComplianceBatchOverview
}

/**
 * 历史项
 */
export interface ComplianceHistoryItem {
  id: number
  eventType: string
  title: string
  status: string
  operatorName: string
  description: string
  happenedAt: string
}

/**
 * 历史记录响应
 */
export interface ComplianceHistoryData {
  updatedAt: string
  tab: ComplianceTab
  historyList: ComplianceHistoryItem[]
}

/**
 * 获取合规管理工作台
 *
 * @param params 查询参数
 * @returns 工作台数据
 */
export const getComplianceDashboard = (params: { tab: ComplianceTab; keyword?: string }) => {
  return request.get<ComplianceDashboardData>({
    url: '/iot/factory/collaboration/compliance/dashboard',
    params
  })
}

/**
 * 获取合规历史记录
 *
 * @param params 查询参数
 * @returns 历史记录
 */
export const getComplianceHistory = (params: { tab: ComplianceTab }) => {
  return request.get<ComplianceHistoryData>({
    url: '/iot/factory/collaboration/compliance/history',
    params
  })
}

/**
 * 导出合规管理报告
 *
 * @param data 查询参数
 * @returns 原始响应
 */
export const exportComplianceReport = (data: { tab: ComplianceTab; keyword?: string }) => {
  return request.postOriginal({
    url: '/iot/factory/collaboration/compliance/report/export',
    data,
    responseType: 'blob'
  })
}
