<script setup lang="ts">
/**
 * 文件说明：智慧工厂 - 报表中心页面
 *
 * <p>说明：本页面严格对齐产品原型中的“报表中心”布局，
 * 所有统计、模板卡片、预览与下载动作均来自真实后端接口与数据库。</p>
 */
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@/components/Icon'
import FactoryDashboardShell from '@/views/factory/dashboard/components/FactoryDashboardShell.vue'
import FactoryDashboardHeader from '@/views/factory/dashboard/components/FactoryDashboardHeader.vue'
import FactoryMetricCard from '@/views/factory/dashboard/components/FactoryMetricCard.vue'
import FactoryPanel from '@/views/factory/dashboard/components/FactoryPanel.vue'
import {
  downloadFactoryReport,
  generateFactoryReport,
  getFactoryReportPreview,
  getReportDashboard,
  type ReportCategoryOption,
  type ReportDashboardData,
  type ReportMetricItem,
  type ReportPreviewData,
  type ReportRecordRowItem,
  type ReportTemplateCardItem
} from '@/api/factory/report'
import { formatDateTime } from '@/utils/formatTime'

defineOptions({ name: 'FactoryReport' })

/**
 * 页面加载态
 */
const loading = ref(false)

/**
 * 当前筛选分类
 */
const activeCategory = ref('')

/**
 * 当前搜索关键字
 */
const keyword = ref('')

/**
 * 当前工作台数据
 */
const dashboardData = ref<ReportDashboardData | null>(null)

/**
 * 当前生成中的模板主键
 */
const generatingTemplateId = ref<number | null>(null)

/**
 * 预览弹窗开关
 */
const previewVisible = ref(false)

/**
 * 预览加载态
 */
const previewLoading = ref(false)

/**
 * 当前预览数据
 */
const previewData = ref<ReportPreviewData | null>(null)

/**
 * 顶部指标卡
 */
const metrics = computed<ReportMetricItem[]>(() => dashboardData.value?.metrics || [])

/**
 * 分类列表
 */
const categories = computed<ReportCategoryOption[]>(() => dashboardData.value?.categories || [])

/**
 * 模板卡片
 */
const templateCards = computed<ReportTemplateCardItem[]>(() => dashboardData.value?.templates || [])

/**
 * 最近记录
 */
const recentRecords = computed<ReportRecordRowItem[]>(() => dashboardData.value?.recentRecords || [])

/**
 * 更新时间文本
 */
const updatedAtText = computed(() => {
  if (!dashboardData.value?.updatedAt) {
    return '--'
  }
  return formatDateTime(dashboardData.value.updatedAt)
})

/**
 * 加载工作台数据
 *
 * @returns Promise
 */
async function loadDashboard() {
  loading.value = true
  try {
    dashboardData.value = await getReportDashboard({
      category: activeCategory.value || undefined,
      keyword: keyword.value.trim() || undefined
    })
  } finally {
    loading.value = false
  }
}

/**
 * 切换分类
 *
 * @param category 分类值
 * @returns Promise
 */
async function handleCategoryChange(category: string) {
  activeCategory.value = category
  await loadDashboard()
}

/**
 * 执行搜索
 *
 * @returns Promise
 */
async function handleSearch() {
  await loadDashboard()
}

/**
 * 重置搜索条件
 *
 * @returns Promise
 */
async function resetFilters() {
  keyword.value = ''
  activeCategory.value = ''
  await loadDashboard()
}

/**
 * 生成报表
 *
 * @param templateId 模板主键
 * @returns Promise
 */
async function handleGenerate(templateId: number) {
  generatingTemplateId.value = templateId
  try {
    const result = await generateFactoryReport({ templateId })
    ElMessage.success(`${result.reportName} 已生成`)
    await loadDashboard()
  } finally {
    generatingTemplateId.value = null
  }
}

/**
 * 打开预览
 *
 * @param recordId 记录主键
 * @returns Promise
 */
async function openPreview(recordId: number) {
  previewVisible.value = true
  previewLoading.value = true
  try {
    previewData.value = await getFactoryReportPreview(recordId)
  } finally {
    previewLoading.value = false
  }
}

/**
 * 下载报表
 *
 * @param recordId 记录主键
 * @param fallbackName 兜底文件名
 * @returns Promise
 */
async function handleDownload(recordId: number, fallbackName: string) {
  const response = await downloadFactoryReport(recordId)
  const blob = new Blob([response.data], {
    type: response.headers?.['content-type'] || 'application/octet-stream'
  })
  const downloadUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = resolveDownloadFileName(response.headers?.['content-disposition'], fallbackName)
  link.click()
  window.URL.revokeObjectURL(downloadUrl)
}

/**
 * 解析下载文件名
 *
 * @param disposition 响应头
 * @param fallbackName 兜底文件名
 * @returns 文件名
 */
function resolveDownloadFileName(disposition?: string, fallbackName = '报表中心.xls') {
  if (!disposition) {
    return fallbackName
  }
  const match = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^"]+)"?/)
  const fileName = match?.[1] || match?.[2]
  if (!fileName) {
    return fallbackName
  }
  try {
    return decodeURIComponent(fileName)
  } catch {
    return fileName
  }
}

/**
 * 解析卡片主题色
 *
 * @param theme 主题
 * @returns 合法主题
 */
function resolveMetricTheme(theme: string): 'cyan' | 'emerald' | 'amber' | 'violet' {
  if (theme === 'emerald' || theme === 'amber' || theme === 'violet') {
    return theme
  }
  return 'cyan'
}

/**
 * 解析状态胶囊主题
 *
 * @param status 状态
 * @returns 主题类
 */
function resolveStatusTone(status: string) {
  if (status.includes('失败')) {
    return 'danger'
  }
  if (status.includes('待')) {
    return 'warning'
  }
  return 'success'
}

onMounted(() => {
  loadDashboard()
})
</script>

<template>
  <FactoryDashboardShell
    hide-hero
    title="报表中心"
    subtitle="统一承接智慧工厂统计分析、专题报表与经营汇总入口。"
    status-text="当前阶段：页面已完成真实工作台接入，模板、记录与动作链路均由后端驱动。"
  >
    <FactoryDashboardHeader title="报表中心" subtitle="统一承接智慧工厂统计分析、专题报表与经营汇总入口。">
      <template #actions>
        <div class="report-updated-at">最近更新：{{ updatedAtText }}</div>
        <button type="button" class="tool-button" @click="loadDashboard">
          <Icon icon="ep:refresh-right" />
          刷新数据
        </button>
      </template>
    </FactoryDashboardHeader>

    <div class="metric-scroll">
      <div class="metric-grid">
        <FactoryMetricCard
          v-for="metric in metrics"
          :key="metric.key"
          :title="metric.title"
          :value="metric.value"
          :unit="metric.unit"
          :icon="metric.icon"
          :theme="resolveMetricTheme(metric.theme)"
        />
      </div>
    </div>

    <FactoryPanel title="报表检索" subtitle="按原型固定分类与关键字筛选当前模板和最近生成记录" accent="emerald">
      <div class="report-filter">
        <div class="report-filter__tabs">
          <button
            v-for="item in categories"
            :key="item.value || 'all'"
            type="button"
            class="filter-tab"
            :class="{ 'filter-tab--active': activeCategory === item.value }"
            @click="handleCategoryChange(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
        <div class="report-filter__actions">
          <input
            v-model="keyword"
            class="report-search-input"
            type="text"
            placeholder="搜索报表名..."
            @keyup.enter="handleSearch"
          />
          <button type="button" class="tool-button" @click="resetFilters">
            <Icon icon="ep:refresh-left" />
            重置
          </button>
          <button type="button" class="tool-button tool-button--primary" @click="handleSearch">
            <Icon icon="ep:search" />
            搜索
          </button>
        </div>
      </div>
    </FactoryPanel>

    <div class="template-grid" v-loading="loading">
      <article v-for="item in templateCards" :key="item.id" class="template-card">
        <div class="template-card__header">
          <div>
            <div class="template-card__title">{{ item.templateName }}</div>
            <div class="template-card__category">{{ item.category }}</div>
          </div>
          <span class="status-pill" :class="`status-pill--${resolveStatusTone(item.status)}`">
            {{ item.status }}
          </span>
        </div>
        <div class="template-card__desc">{{ item.description }}</div>
        <div class="template-card__meta">
          <div>最近生成：{{ item.lastGeneratedAt ? formatDateTime(item.lastGeneratedAt) : '--' }}</div>
          <div>最近状态：{{ item.lastStatus || '--' }}</div>
        </div>
        <div class="template-card__actions">
          <button
            type="button"
            class="action-button action-button--preview"
            :disabled="!item.previewAvailable || !item.latestSuccessRecordId"
            @click="item.latestSuccessRecordId && openPreview(item.latestSuccessRecordId)"
          >
            <Icon icon="ep:view" />
            预览
          </button>
          <button
            type="button"
            class="action-button action-button--generate"
            :disabled="!item.generateAvailable || generatingTemplateId === item.id"
            @click="handleGenerate(item.id)"
          >
            <Icon icon="ep:plus" />
            {{ generatingTemplateId === item.id ? '生成中...' : '生成' }}
          </button>
          <button
            type="button"
            class="action-button action-button--download"
            :disabled="!item.downloadAvailable || !item.latestSuccessRecordId"
            @click="item.latestSuccessRecordId && handleDownload(item.latestSuccessRecordId, `${item.templateName}.xls`)"
          >
            <Icon icon="ep:download" />
            下载
          </button>
        </div>
      </article>
    </div>

    <FactoryPanel title="最近生成的报表" subtitle="展示当前筛选条件下的真实生成记录" accent="violet">
      <ElTable :data="recentRecords" class="dark-table" empty-text="暂无符合条件的报表记录">
        <ElTableColumn prop="reportName" label="报表名称" min-width="220" />
        <ElTableColumn prop="category" label="类型" min-width="120" />
        <ElTableColumn prop="generatedAt" label="生成时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.generatedAt) }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="status" label="状态" min-width="110">
          <template #default="{ row }">
            <span class="status-pill" :class="`status-pill--${resolveStatusTone(row.status)}`">
              {{ row.status }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <button type="button" class="table-link" @click="openPreview(row.id)">预览</button>
              <button type="button" class="table-link" @click="handleDownload(row.id, `${row.reportName}.xls`)">下载</button>
            </div>
          </template>
        </ElTableColumn>
      </ElTable>
    </FactoryPanel>

    <ElDialog v-model="previewVisible" title="报表预览" width="560px">
      <div v-loading="previewLoading" class="preview-wrap">
        <template v-if="previewData">
          <div class="preview-title">{{ previewData.reportName }}</div>
          <div class="preview-desc">{{ previewData.templateDesc }}</div>
          <div class="preview-grid">
            <div class="preview-item">
              <span class="preview-item__label">报表类型</span>
              <span class="preview-item__value">{{ previewData.category }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-item__label">业务日期</span>
              <span class="preview-item__value">{{ previewData.bizDate || '--' }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-item__label">生成时间</span>
              <span class="preview-item__value">{{ formatDateTime(previewData.generatedAt) }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-item__label">生成状态</span>
              <span class="preview-item__value">{{ previewData.status }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-item__label">模板名称</span>
              <span class="preview-item__value">{{ previewData.templateName }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-item__label">操作人</span>
              <span class="preview-item__value">{{ previewData.operatorName }}</span>
            </div>
          </div>
        </template>
        <ElEmpty v-else description="暂无可预览数据" />
      </div>
    </ElDialog>
  </FactoryDashboardShell>
</template>

<style scoped lang="scss">
.report-updated-at {
  font-size: 12px;
  color: rgba(205, 229, 246, 0.7);
}

.metric-scroll {
  overflow-x: auto;
  padding-bottom: 2px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 16px;
  min-width: 960px;
}

.report-filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.report-filter__tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 9px 16px;
  font-size: 13px;
  color: rgba(205, 229, 246, 0.72);
  background: rgba(11, 24, 40, 0.9);
  border: 1px solid rgba(81, 141, 194, 0.22);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-tab--active {
  color: #f6fcff;
  background: linear-gradient(180deg, rgba(41, 95, 196, 0.9), rgba(25, 65, 145, 0.92));
  border-color: rgba(110, 171, 255, 0.46);
  box-shadow: 0 10px 18px rgba(9, 30, 76, 0.34);
}

.report-filter__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.report-search-input {
  width: 220px;
  padding: 10px 12px;
  font-size: 13px;
  color: #f3fbff;
  background: rgba(7, 19, 33, 0.92);
  border: 1px solid rgba(76, 141, 198, 0.24);
  border-radius: 10px;
  outline: none;
}

.report-search-input::placeholder {
  color: rgba(186, 208, 226, 0.46);
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.template-card {
  padding: 18px;
  background: linear-gradient(180deg, rgba(10, 22, 36, 0.96), rgba(7, 15, 27, 0.94));
  border: 1px solid rgba(78, 170, 235, 0.2);
  border-radius: 18px;
  box-shadow:
    inset 0 1px 0 rgba(160, 223, 255, 0.08),
    0 12px 28px rgba(0, 0, 0, 0.18);
}

.template-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.template-card__title {
  font-size: 16px;
  font-weight: 700;
  color: #f3fbff;
}

.template-card__category {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(103, 179, 255, 0.86);
}

.template-card__desc {
  min-height: 42px;
  margin-top: 14px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(212, 233, 249, 0.78);
}

.template-card__meta {
  display: grid;
  gap: 8px;
  margin-top: 14px;
  font-size: 12px;
  color: rgba(186, 208, 226, 0.68);
}

.template-card__actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
}

.status-pill--success {
  color: #8ff4c9;
  background: rgba(10, 83, 57, 0.24);
  border: 1px solid rgba(62, 214, 157, 0.2);
}

.status-pill--warning {
  color: #f7cb6a;
  background: rgba(120, 72, 15, 0.22);
  border: 1px solid rgba(247, 186, 42, 0.2);
}

.status-pill--danger {
  color: #fca5a5;
  background: rgba(127, 29, 29, 0.24);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.tool-button,
.action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 14px;
  font-size: 13px;
  color: rgba(223, 239, 252, 0.9);
  background: rgba(11, 26, 42, 0.92);
  border: 1px solid rgba(79, 140, 191, 0.24);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tool-button:disabled,
.action-button:disabled {
  opacity: 0.42;
  cursor: not-allowed;
}

.tool-button--primary,
.action-button--generate {
  color: #f8fcff;
  background: linear-gradient(180deg, #2d8b55, #1f6d43);
  border-color: rgba(96, 227, 160, 0.28);
}

.action-button--preview {
  color: #82b8ff;
}

.action-button--download {
  color: #6ee7b7;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-link {
  padding: 0;
  font-size: 13px;
  color: #7ab8ff;
  background: transparent;
  border: none;
  cursor: pointer;
}

.table-link:last-child {
  color: #68e0a4;
}

.preview-wrap {
  min-height: 180px;
}

.preview-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.preview-desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #475569;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.preview-item {
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
}

.preview-item__label {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.preview-item__value {
  display: block;
  margin-top: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.dark-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(16, 30, 49, 0.92);
  --el-table-row-hover-bg-color: rgba(44, 94, 155, 0.14);
  --el-table-border-color: rgba(79, 120, 160, 0.18);
  --el-table-text-color: #dceeff;
  --el-table-header-text-color: rgba(200, 224, 244, 0.8);
}

:deep(.dark-table .el-table__inner-wrapper::before) {
  display: none;
}

@media (max-width: 1280px) {
  .template-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .template-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .preview-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
