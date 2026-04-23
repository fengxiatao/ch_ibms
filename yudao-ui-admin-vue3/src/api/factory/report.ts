/**
 * 文件说明：智慧工厂报表中心 API
 *
 * <p>说明：统一维护报表中心工作台、生成、预览与下载相关请求。</p>
 */
import request from '@/config/axios'

/**
 * 顶部统计卡
 */
export interface ReportMetricItem {
  key: string
  title: string
  value: string
  unit?: string
  icon: string
  theme: 'cyan' | 'emerald' | 'amber' | 'violet'
}

/**
 * 分类选项
 */
export interface ReportCategoryOption {
  value: string
  label: string
}

/**
 * 模板卡片
 */
export interface ReportTemplateCardItem {
  id: number
  templateName: string
  category: string
  description: string
  status: string
  latestSuccessRecordId?: number
  lastGeneratedAt?: string
  lastStatus: string
  previewAvailable: boolean
  downloadAvailable: boolean
  generateAvailable: boolean
}

/**
 * 最近记录行
 */
export interface ReportRecordRowItem {
  id: number
  reportName: string
  category: string
  generatedAt: string
  status: string
  operatorName: string
}

/**
 * 工作台响应
 */
export interface ReportDashboardData {
  updatedAt: string
  metrics: ReportMetricItem[]
  categories: ReportCategoryOption[]
  templates: ReportTemplateCardItem[]
  recentRecords: ReportRecordRowItem[]
}

/**
 * 生成响应
 */
export interface ReportGenerateData {
  recordId: number
  status: string
  reportName: string
  generatedAt: string
}

/**
 * 预览响应
 */
export interface ReportPreviewData {
  recordId: number
  reportName: string
  category: string
  bizDate: string
  generatedAt: string
  status: string
  operatorName: string
  templateId: number
  templateName: string
  templateDesc: string
}

/**
 * 获取报表中心工作台
 *
 * @param params 查询参数
 * @returns 工作台数据
 */
export const getReportDashboard = (params: { category?: string; keyword?: string }) => {
  return request.get<ReportDashboardData>({
    url: '/iot/factory/report/dashboard',
    params
  })
}

/**
 * 生成报表
 *
 * @param data 生成参数
 * @returns 生成结果
 */
export const generateFactoryReport = (data: { templateId: number; bizDate?: string }) => {
  return request.post<ReportGenerateData>({
    url: '/iot/factory/report/generate',
    data
  })
}

/**
 * 获取报表预览
 *
 * @param id 记录主键
 * @returns 预览数据
 */
export const getFactoryReportPreview = (id: number) => {
  return request.get<ReportPreviewData>({
    url: '/iot/factory/report/preview',
    params: { id }
  })
}

/**
 * 下载报表
 *
 * @param id 记录主键
 * @returns 二进制响应
 */
export const downloadFactoryReport = (id: number) => {
  return request.download({
    url: '/iot/factory/report/download',
    params: { id }
  })
}

