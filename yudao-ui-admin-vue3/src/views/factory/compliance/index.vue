<script setup lang="ts">
/**
 * 文件说明：智慧工厂 - 合规管理页面
 *
 * <p>说明：本页面严格对齐产品原型的布局与视觉层级，
 * 通过真实后端接口展示 GMP 合规、环保监测、批次追溯三类数据。</p>
 */
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import FactoryDashboardShell from '@/views/factory/dashboard/components/FactoryDashboardShell.vue'
import {
  exportComplianceReport,
  getComplianceDashboard,
  getComplianceHistory,
  type ComplianceBatchTraceRowItem,
  type ComplianceDashboardData,
  type ComplianceHistoryItem,
  type ComplianceMetricItem,
  type ComplianceSummaryCardItem,
  type ComplianceTab,
  type EnvironmentalMonitorRowItem,
  type GmpInspectionRowItem
} from '@/api/factory/compliance'

defineOptions({ name: 'FactoryCompliance' })

/**
 * 当前页面加载态
 */
const loading = ref(false)

/**
 * 当前导出态
 */
const exportLoading = ref(false)

/**
 * 当前激活的 Tab
 */
const activeTab = ref<ComplianceTab>('gmp')

/**
 * 搜索关键字
 */
const searchKeyword = ref('')

/**
 * 工作台响应数据
 */
const dashboardData = ref<ComplianceDashboardData | null>(null)

/**
 * 历史记录抽屉开关
 */
const historyVisible = ref(false)

/**
 * 历史记录数据
 */
const historyList = ref<ComplianceHistoryItem[]>([])

/**
 * 详情弹窗开关
 */
const detailVisible = ref(false)

/**
 * 当前选中详情行
 */
const selectedDetailRow = ref<Record<string, any> | null>(null)

/**
 * 页面时钟
 */
const pageClock = ref('')

let timer: ReturnType<typeof setInterval> | undefined

/**
 * Tab 定义
 */
const TAB_OPTIONS: Array<{ value: ComplianceTab; label: string; icon: string }> = [
  { value: 'gmp', label: 'GMP合规', icon: 'ep:shield' },
  { value: 'environment', label: '环保监测', icon: 'ep:wind-power' },
  { value: 'batch-trace', label: '批次追溯', icon: 'ep:collection-tag' }
]

/**
 * 当前顶部指标卡
 */
const metricList = computed<ComplianceMetricItem[]>(() => dashboardData.value?.metrics || [])

/**
 * 当前概览卡
 */
const summaryCards = computed<ComplianceSummaryCardItem[]>(() => {
  if (!dashboardData.value) {
    return []
  }
  if (activeTab.value === 'environment') {
    return dashboardData.value.environmentalOverview.regionCards
  }
  if (activeTab.value === 'batch-trace') {
    return dashboardData.value.batchOverview.batchCards
  }
  return dashboardData.value.gmpOverview.regionCards
})

/**
 * 当前表格行
 */
const tableRows = computed<Array<GmpInspectionRowItem | EnvironmentalMonitorRowItem | ComplianceBatchTraceRowItem>>(() => {
  if (!dashboardData.value) {
    return []
  }
  if (activeTab.value === 'environment') {
    return dashboardData.value.environmentalOverview.detailList
  }
  if (activeTab.value === 'batch-trace') {
    return dashboardData.value.batchOverview.detailList
  }
  return dashboardData.value.gmpOverview.detailList
})

/**
 * 环保监测明细
 */
const environmentRows = computed<EnvironmentalMonitorRowItem[]>(() => dashboardData.value?.environmentalOverview.detailList || [])

/**
 * 批次追溯明细
 */
const batchTraceRows = computed<ComplianceBatchTraceRowItem[]>(() => dashboardData.value?.batchOverview.detailList || [])

/**
 * 环保原型展示点位集合
 */
const ENVIRONMENT_PROTOTYPE_KEYS = {
  air: ['VOCs', '颗粒物', '二氧化硫', '氮氧化物'],
  wastewater: ['COD', '氨氮', 'pH值', '流量'],
  noise: ['昼间', '夜间', '限值']
} as const

/**
 * 从列表中提取原型优先项
 *
 * @param rows 环保行
 * @param names 目标名称
 * @returns 结果
 */
function pickEnvironmentRowsByNames(rows: EnvironmentalMonitorRowItem[], names: readonly string[]) {
  const result = names
    .map((name) => rows.find((row) => row.pointName === name))
    .filter((row): row is EnvironmentalMonitorRowItem => Boolean(row))
  return result
}

/**
 * 废气监测数据
 */
const airMonitorRows = computed<EnvironmentalMonitorRowItem[]>(() => {
  const preferredRows = pickEnvironmentRowsByNames(environmentRows.value, ENVIRONMENT_PROTOTYPE_KEYS.air)
  if (preferredRows.length) {
    return preferredRows
  }
  return environmentRows.value.filter((row) => row.unit === 'mg/m3' || row.pointName.includes('废气') || row.pointName.includes('锅炉'))
})

/**
 * 废水监测数据
 */
const wastewaterMonitorRows = computed<EnvironmentalMonitorRowItem[]>(() => {
  const preferredRows = pickEnvironmentRowsByNames(environmentRows.value, ENVIRONMENT_PROTOTYPE_KEYS.wastewater)
  if (preferredRows.length) {
    return preferredRows
  }
  return environmentRows.value.filter((row) => row.unit === 'mg/L' || row.pointName.includes('废水'))
})

/**
 * 噪声监测数据
 */
const noiseMonitorRows = computed<EnvironmentalMonitorRowItem[]>(() => {
  const preferredRows = pickEnvironmentRowsByNames(environmentRows.value, ENVIRONMENT_PROTOTYPE_KEYS.noise)
  if (preferredRows.length) {
    return preferredRows
  }
  return environmentRows.value.filter((row) => row.unit.toLowerCase().includes('db') || row.pointName.includes('噪'))
})

/**
 * 环保预警列表
 */
const environmentalAlertItems = computed(() => {
  const warningRows = environmentRows.value
    .filter((row) => row.exceedCount > 0 || ['注意', '异常'].includes(row.status))
    .map((row) => ({
      id: `warning-${row.id}`,
      title: `${row.pointName}预警`,
      description: `${row.pointName}当前值 ${formatNumber(row.currentValue)} ${row.unit}，标准值 ${formatNumber(row.standardValue)} ${row.unit}`,
      time: formatTime(row.lastCheckTime, 'YYYY-MM-DD HH:mm').slice(11),
      tone: 'warning'
    }))
  const normalRows = environmentRows.value
    .filter((row) => row.exceedCount === 0 && !['注意', '异常'].includes(row.status))
    .map((row) => ({
      id: `success-${row.id}`,
      title: `${row.pointName}正常`,
      description: `${row.pointName}监测正常，当前值 ${formatNumber(row.currentValue)} ${row.unit}`,
      time: formatTime(row.lastCheckTime, 'YYYY-MM-DD HH:mm').slice(11),
      tone: 'success'
    }))
  return [...warningRows, ...normalRows].slice(0, 3)
})

/**
 * 批次追溯输入值
 */
const batchTraceQuery = ref('')

/**
 * 批次追溯已提交查询值
 */
const batchTraceAppliedQuery = ref('')

/**
 * 批次追溯查询结果
 */
const batchTraceQueryResults = computed<ComplianceBatchTraceRowItem[]>(() => {
  const keyword = batchTraceAppliedQuery.value.trim().toLowerCase()
  if (!keyword) {
    return []
  }
  return batchTraceRows.value.filter((row) => {
    return row.batchCode.toLowerCase().includes(keyword) || row.productName.toLowerCase().includes(keyword)
  })
})

/**
 * 是否已执行批次查询
 */
const hasBatchTraceQuery = computed(() => batchTraceAppliedQuery.value.trim().length > 0)

/**
 * 当前表格标题
 */
const detailPanelTitle = computed(() => {
  if (activeTab.value === 'environment') {
    return '环保监测详情'
  }
  if (activeTab.value === 'batch-trace') {
    return '合规批次追溯详情'
  }
  return 'GMP合规监测详情'
})

/**
 * 当前详情字段
 */
const detailEntries = computed(() => {
  const row = selectedDetailRow.value
  if (!row) {
    return []
  }
  if (activeTab.value === 'environment') {
    return [
      { label: '监测点位', value: row.pointName },
      { label: '当前值', value: `${formatNumber(row.currentValue)} ${row.unit}` },
      { label: '标准值', value: `${formatNumber(row.standardValue)} ${row.unit}` },
      { label: '超标次数', value: `${row.exceedCount} 次` },
      { label: '状态', value: row.status },
      { label: '最后检查', value: formatTime(row.lastCheckTime) }
    ]
  }
  if (activeTab.value === 'batch-trace') {
    return [
      { label: '批次编号', value: row.batchCode },
      { label: '产品名称', value: row.productName },
      { label: '检查点数', value: `${row.checkpointCount} 项` },
      { label: '异常数', value: `${row.issueCount} 条` },
      { label: '状态', value: row.status },
      { label: '最后检查', value: formatTime(row.lastCheckTime) }
    ]
  }
  return [
    { label: '监测点位', value: row.pointName },
    { label: '合规点数', value: `${row.compliantCount} 点` },
    { label: '超标次数', value: `${row.exceedCount} 次` },
    { label: '状态', value: row.status },
    { label: '最后检查', value: formatTime(row.lastCheckTime) }
  ]
})

/**
 * 生命周期：页面挂载
 */
onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  loadDashboard()
})

/**
 * 生命周期：页面卸载
 */
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})

/**
 * 加载工作台数据
 */
async function loadDashboard() {
  loading.value = true
  try {
    dashboardData.value = await getComplianceDashboard({
      tab: activeTab.value,
      keyword: searchKeyword.value || undefined
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载历史记录
 */
async function loadHistory() {
  const data = await getComplianceHistory({ tab: activeTab.value })
  historyList.value = data.historyList || []
}

/**
 * 切换 Tab
 *
 * @param tab 当前 Tab
 */
async function switchTab(tab: ComplianceTab) {
  if (activeTab.value === tab) {
    return
  }
  activeTab.value = tab
  await loadDashboard()
}

/**
 * 执行搜索
 */
async function handleSearch() {
  await loadDashboard()
}

/**
 * 执行批次追溯查询
 */
function handleBatchTraceQuery() {
  batchTraceAppliedQuery.value = batchTraceQuery.value.trim()
}

/**
 * 打开历史记录
 */
async function openHistory() {
  historyVisible.value = true
  await loadHistory()
}

/**
 * 打开详情
 *
 * @param row 当前行
 */
function openDetail(row: Record<string, any>) {
  selectedDetailRow.value = row
  detailVisible.value = true
}

/**
 * 导出当前 Tab 报告
 */
async function exportReport() {
  exportLoading.value = true
  try {
    const response = await exportComplianceReport({
      tab: activeTab.value,
      keyword: searchKeyword.value || undefined
    })
    const blob = response.data instanceof Blob ? response.data : new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.href = url
    link.download = resolveExportFileName()
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出合规报告失败', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

/**
 * 刷新页面数据
 */
async function refreshData() {
  await loadDashboard()
  ElMessage.success('数据已刷新')
}

/**
 * 更新时间显示
 */
function updateClock() {
  pageClock.value = formatTime(new Date().toISOString(), 'YYYY/MM/DD HH:mm:ss')
}

/**
 * 获取卡片主题类
 *
 * @param theme 主题
 * @returns 类名
 */
function getMetricThemeClass(theme: string) {
  return `is-${theme || 'cyan'}`
}

/**
 * 获取状态主题类
 *
 * @param status 状态
 * @returns 类名
 */
function getStatusThemeClass(status: string) {
  if (['注意', '整改中', '待复核', '待处理'].includes(status)) {
    return 'is-warning'
  }
  if (['异常', '故障'].includes(status)) {
    return 'is-danger'
  }
  return 'is-success'
}

/**
 * 格式化时间
 *
 * @param value 时间值
 * @param mode 输出模式
 * @returns 文本
 */
function formatTime(value?: string, mode: 'YYYY/MM/DD HH:mm:ss' | 'YYYY-MM-DD HH:mm' = 'YYYY-MM-DD HH:mm') {
  if (!value) {
    return '--'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  const second = `${date.getSeconds()}`.padStart(2, '0')
  if (mode === 'YYYY/MM/DD HH:mm:ss') {
    return `${year}/${month}/${day} ${hour}:${minute}:${second}`
  }
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/**
 * 格式化数字
 *
 * @param value 数值
 * @returns 文本
 */
function formatNumber(value?: number | string) {
  if (value === undefined || value === null || value === '') {
    return '--'
  }
  return `${value}`
}

/**
 * 解析导出文件名
 *
 * @returns 文件名
 */
function resolveExportFileName() {
  const tabLabelMap: Record<ComplianceTab, string> = {
    gmp: 'GMP合规',
    environment: '环保监测',
    'batch-trace': '批次追溯'
  }
  return `合规管理-${tabLabelMap[activeTab.value]}-${Date.now()}.xls`
}
</script>

<template>
  <FactoryDashboardShell
    title="合规管理"
    subtitle="批次追溯 / GMP合规 / 环保监测"
    status-text="严格按原型布局克隆，所有数据来自真实后端接口与数据库"
    :hide-hero="true"
  >
    <div v-loading="loading" class="compliance-page">
      <header class="compliance-header">
        <div class="compliance-header__meta">
          <h1 class="compliance-header__title">合规管理</h1>
          <p class="compliance-header__subtitle">批次追溯 / GMP合规 / 环保监测</p>
        </div>
        <div class="compliance-header__actions">
          <ElInput
            v-model="searchKeyword"
            class="compliance-search"
            placeholder="搜索设备、告警、区域..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
          </ElInput>
          <div class="compliance-clock">{{ pageClock }}</div>
          <button type="button" class="icon-button" @click="refreshData">
            <Icon icon="ep:refresh-right" />
          </button>
        </div>
      </header>

      <section class="metric-grid">
        <article
          v-for="item in metricList"
          :key="item.key"
          class="metric-card"
          :class="getMetricThemeClass(item.theme)"
        >
          <div class="metric-card__icon">
            <Icon :icon="item.icon" />
          </div>
          <div class="metric-card__content">
            <p>{{ item.title }}</p>
            <strong>{{ item.value }}<span v-if="item.unit">{{ item.unit }}</span></strong>
          </div>
        </article>
      </section>

      <section class="tab-toolbar">
        <div class="tab-toolbar__tabs">
          <button
            v-for="tab in TAB_OPTIONS"
            :key="tab.value"
            type="button"
            class="tab-button"
            :class="{ 'tab-button--active': tab.value === activeTab }"
            @click="switchTab(tab.value)"
          >
            <Icon :icon="tab.icon" />
            {{ tab.label }}
          </button>
        </div>

        <div class="tab-toolbar__actions">
          <button type="button" class="tool-button" @click="openHistory">
            <Icon icon="ep:calendar" />
            历史记录
          </button>
          <button type="button" class="tool-button tool-button--primary" :disabled="exportLoading" @click="exportReport">
            <Icon icon="ep:download" />
            {{ exportLoading ? '导出中...' : '导出报告' }}
          </button>
        </div>
      </section>

      <template v-if="activeTab === 'gmp'">
        <section class="summary-grid">
          <article
            v-for="item in summaryCards.slice(0, 3)"
            :key="item.id"
            class="summary-card"
            :class="{ 'summary-card--warning': item.tone === 'warning' }"
          >
            <div class="summary-card__title">
              <Icon icon="ep:opportunity" />
              <span>{{ item.title }}</span>
            </div>
            <div class="summary-card__subtitle">{{ item.subtitle }}</div>
            <div class="summary-card__progress">
              <div class="summary-card__track">
                <div class="summary-card__bar" :style="{ width: `${item.progressRate || 0}%` }"></div>
              </div>
              <span>{{ item.progressText }}</span>
            </div>
          </article>
        </section>

        <section class="detail-panel">
          <header class="detail-panel__header">{{ detailPanelTitle }}</header>

          <div class="detail-table">
            <div class="detail-table__head detail-table__row detail-table__row--gmp">
              <span>监测点位</span>
              <span>合规点数</span>
              <span>超标次数</span>
              <span>状态</span>
              <span>最后检查</span>
              <span>详情</span>
            </div>
            <div
              v-for="row in tableRows as GmpInspectionRowItem[]"
              :key="row.id"
              class="detail-table__row detail-table__row--gmp"
            >
              <span class="detail-table__primary">{{ row.pointName }}</span>
              <span>{{ row.compliantCount }} 点</span>
              <span :class="{ 'text-warning': row.exceedCount > 0 }">{{ row.exceedCount }} 次</span>
              <span><em class="status-pill" :class="getStatusThemeClass(row.status)">{{ row.status }}</em></span>
              <span>{{ formatTime(row.lastCheckTime) }}</span>
              <span>
                <button type="button" class="view-button" @click="openDetail(row as unknown as Record<string, any>)">
                  查看
                </button>
              </span>
            </div>
            <div v-if="!tableRows.length" class="detail-table__empty">暂无记录</div>
          </div>
        </section>
      </template>

      <template v-else-if="activeTab === 'environment'">
        <section class="environment-layout">
          <article class="prototype-card">
            <h4 class="prototype-card__title">废气监测</h4>
            <div v-if="airMonitorRows.length" class="monitor-list">
              <div v-for="row in airMonitorRows" :key="row.id" class="monitor-item">
                <div class="monitor-item__header">
                  <span>{{ row.pointName }}</span>
                  <span>{{ formatNumber(row.currentValue) }} / {{ formatNumber(row.standardValue) }} {{ row.unit }}</span>
                </div>
                <div class="monitor-item__track">
                  <div
                    class="monitor-item__bar"
                    :class="{ 'monitor-item__bar--warning': row.exceedCount > 0 || row.status === '注意' }"
                    :style="{ width: `${Math.min((Number(row.currentValue) / Number(row.standardValue || 1)) * 100, 100)}%` }"
                  ></div>
                </div>
              </div>
            </div>
            <div v-else class="prototype-empty prototype-empty--compact">暂无废气监测数据</div>
          </article>

          <article class="prototype-card">
            <h4 class="prototype-card__title">废水监测</h4>
            <div v-if="wastewaterMonitorRows.length" class="monitor-list">
              <div v-for="row in wastewaterMonitorRows" :key="row.id" class="monitor-item">
                <div class="monitor-item__header">
                  <span>{{ row.pointName }}</span>
                  <span>{{ formatNumber(row.currentValue) }} / {{ formatNumber(row.standardValue) }} {{ row.unit }}</span>
                </div>
                <div class="monitor-item__track">
                  <div
                    class="monitor-item__bar"
                    :class="{ 'monitor-item__bar--warning': row.exceedCount > 0 || row.status === '注意' }"
                    :style="{ width: `${Math.min((Number(row.currentValue) / Number(row.standardValue || 1)) * 100, 100)}%` }"
                  ></div>
                </div>
              </div>
            </div>
            <div v-else class="prototype-empty prototype-empty--compact">暂无废水监测数据</div>
          </article>

          <article class="prototype-card">
            <h4 class="prototype-card__title">噪声监测</h4>
            <div v-if="noiseMonitorRows.length" class="noise-board">
              <div v-for="row in noiseMonitorRows.slice(0, 3)" :key="row.id" class="noise-board__item">
                <p>{{ formatNumber(row.currentValue) }}</p>
                <span>{{ row.pointName }}</span>
                <em>{{ row.unit }}</em>
              </div>
            </div>
            <div v-else class="prototype-empty prototype-empty--compact">暂无噪声监测数据</div>
          </article>

          <article class="prototype-card">
            <h4 class="prototype-card__title">环保预警</h4>
            <div v-if="environmentalAlertItems.length" class="alert-list">
              <div
                v-for="item in environmentalAlertItems"
                :key="item.id"
                class="alert-item"
                :class="{ 'alert-item--warning': item.tone === 'warning', 'alert-item--success': item.tone === 'success' }"
              >
                <div class="alert-item__header">
                  <span>{{ item.title }}</span>
                  <span>{{ item.time }}</span>
                </div>
                <p>{{ item.description }}</p>
              </div>
            </div>
            <div v-else class="prototype-empty prototype-empty--compact">暂无环保预警数据</div>
          </article>
        </section>
      </template>

      <template v-else>
        <section class="trace-panel">
          <article class="prototype-card prototype-card--trace">
            <h4 class="prototype-card__title">批次追溯查询</h4>
            <div class="trace-panel__toolbar">
              <input
                v-model="batchTraceQuery"
                type="text"
                class="trace-panel__input"
                placeholder="输入批次号..."
                @keyup.enter="handleBatchTraceQuery"
              />
              <button type="button" class="trace-panel__button" @click="handleBatchTraceQuery">查询</button>
            </div>

            <div v-if="!hasBatchTraceQuery" class="trace-panel__empty">
              <Icon icon="ep:collection-tag" class="trace-panel__empty-icon" />
              <p>输入批次号进行追溯查询</p>
            </div>

            <div v-else-if="!batchTraceQueryResults.length" class="trace-panel__empty">
              <Icon icon="ep:warning" class="trace-panel__empty-icon" />
              <p>未查询到匹配批次</p>
            </div>

            <div v-else class="trace-result-list">
              <article v-for="row in batchTraceQueryResults" :key="row.id" class="trace-result-card">
                <div class="trace-result-card__head">
                  <div>
                    <strong>{{ row.batchCode }}</strong>
                    <p>{{ row.productName }}</p>
                  </div>
                  <em class="status-pill" :class="getStatusThemeClass(row.status)">{{ row.status }}</em>
                </div>
                <div class="trace-result-card__meta">
                  <span>检查点 {{ row.checkpointCount }} 项</span>
                  <span :class="{ 'text-warning': row.issueCount > 0 }">异常 {{ row.issueCount }} 条</span>
                  <span>{{ formatTime(row.lastCheckTime) }}</span>
                </div>
                <div class="trace-result-card__actions">
                  <button type="button" class="view-button" @click="openDetail(row as unknown as Record<string, any>)">
                    查看详情
                  </button>
                </div>
              </article>
            </div>
          </article>
        </section>
      </template>
    </div>

    <ElDrawer v-model="historyVisible" title="历史记录" size="420px">
      <div class="history-drawer">
        <div v-for="item in historyList" :key="item.id" class="history-item">
          <div class="history-item__title">
            <span>{{ item.title }}</span>
            <em class="status-pill" :class="getStatusThemeClass(item.status)">{{ item.status }}</em>
          </div>
          <div class="history-item__meta">{{ item.eventType }} · {{ item.operatorName || '--' }}</div>
          <div class="history-item__desc">{{ item.description || '--' }}</div>
          <div class="history-item__time">{{ formatTime(item.happenedAt) }}</div>
        </div>
        <div v-if="!historyList.length" class="history-empty">暂无历史记录</div>
      </div>
    </ElDrawer>

    <ElDialog v-model="detailVisible" title="查看详情" width="520px">
      <div class="detail-dialog">
        <div v-for="item in detailEntries" :key="item.label" class="detail-dialog__item">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </ElDialog>
  </FactoryDashboardShell>
</template>

<style scoped lang="scss">
.compliance-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 4px 4px 18px;
  color: #fff;
}

.compliance-header,
.tab-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.compliance-header__title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #f8fbff;
}

.compliance-header__subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: rgba(170, 198, 224, 0.76);
}

.compliance-header__actions,
.tab-toolbar__tabs,
.tab-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.compliance-search {
  width: 260px;
}

:deep(.compliance-search .el-input__wrapper) {
  background: rgba(9, 21, 38, 0.9);
  border-radius: 10px;
  box-shadow: inset 0 0 0 1px rgba(63, 101, 155, 0.36);
}

.compliance-clock {
  min-width: 176px;
  padding: 10px 12px;
  font-size: 14px;
  color: #d3e9ff;
  text-align: center;
  background: rgba(10, 22, 40, 0.92);
  border: 1px solid rgba(66, 121, 183, 0.24);
  border-radius: 10px;
}

.icon-button,
.tool-button,
.tab-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-button {
  width: 40px;
  height: 40px;
  color: #9cc7ff;
  background: rgba(11, 27, 45, 0.88);
  border-radius: 10px;
}

.metric-grid,
.summary-grid {
  display: grid;
  gap: 14px;
}

.metric-grid {
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 4px;
  grid-template-columns: repeat(4, minmax(190px, 1fr));
  scrollbar-width: thin;
  scrollbar-color: rgba(96, 165, 250, 0.45) transparent;
}

.summary-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metric-card,
.summary-card,
.detail-panel {
  background: rgba(6, 17, 34, 0.88);
  border: 1px solid rgba(55, 97, 152, 0.22);
  border-radius: 14px;
  box-shadow: inset 0 1px 0 rgba(122, 165, 217, 0.06);
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 76px;
  padding: 16px 18px;
  white-space: nowrap;
}

.metric-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  font-size: 24px;
  border-radius: 12px;
}

.metric-card__content p {
  margin: 0;
  font-size: 12px;
  color: rgba(170, 198, 224, 0.76);
}

.metric-card__content {
  min-width: 0;
}

.metric-card__content strong {
  display: block;
  margin-top: 4px;
  font-size: 34px;
  line-height: 1;
  font-weight: 700;
}

.metric-card__content strong span {
  margin-left: 2px;
  font-size: 14px;
}

.metric-card.is-emerald .metric-card__icon {
  color: #34d399;
  background: rgba(16, 185, 129, 0.16);
}

.metric-card.is-emerald .metric-card__content strong {
  color: #34d399;
}

.metric-card.is-cyan .metric-card__icon {
  color: #60a5fa;
  background: rgba(59, 130, 246, 0.16);
}

.metric-card.is-cyan .metric-card__content strong {
  color: #60a5fa;
}

.metric-card.is-amber .metric-card__icon {
  color: #fbbf24;
  background: rgba(234, 179, 8, 0.16);
}

.metric-card.is-amber .metric-card__content strong {
  color: #fbbf24;
}

.metric-card.is-violet .metric-card__icon {
  color: #c084fc;
  background: rgba(168, 85, 247, 0.16);
}

.metric-card.is-violet .metric-card__content strong {
  color: #c084fc;
}

.tab-button,
.tool-button {
  height: 38px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 500;
  color: #92a8c2;
  background: rgba(31, 41, 55, 0.8);
  border-radius: 10px;
}

.tab-button--active,
.tool-button--primary {
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.summary-card {
  min-height: 94px;
  padding: 16px 18px;
}

.summary-card--warning {
  border-color: rgba(234, 179, 8, 0.28);
}

.summary-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: 600;
}

.summary-card__title .iconify {
  color: #34d399;
}

.summary-card--warning .summary-card__title .iconify {
  color: #fbbf24;
}

.summary-card__subtitle {
  margin-bottom: 12px;
  font-size: 12px;
  color: rgba(170, 198, 224, 0.76);
}

.summary-card__progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-card__track {
  flex: 1;
  height: 6px;
  overflow: hidden;
  background: rgba(62, 79, 103, 0.64);
  border-radius: 999px;
}

.summary-card__bar {
  height: 100%;
  background: linear-gradient(90deg, #22c55e, #33d399);
  border-radius: inherit;
}

.summary-card--warning .summary-card__bar {
  background: linear-gradient(90deg, #fbbf24, #eab308);
}

.summary-card__progress span {
  min-width: 46px;
  font-size: 12px;
  font-weight: 600;
  color: #34d399;
  text-align: right;
}

.summary-card--warning .summary-card__progress span {
  color: #fbbf24;
}

.environment-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.prototype-card {
  min-height: 230px;
  padding: 18px;
  background: linear-gradient(180deg, rgba(7, 19, 39, 0.96), rgba(8, 24, 52, 0.92));
  border: 1px solid rgba(53, 91, 148, 0.28);
  border-radius: 16px;
  box-shadow: inset 0 1px 0 rgba(122, 165, 217, 0.08);
}

.prototype-card--trace {
  min-height: 520px;
}

.prototype-card__title {
  margin: 0 0 16px;
  font-size: 22px;
  font-weight: 600;
  color: #f5f9ff;
}

.monitor-list,
.alert-list,
.trace-result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.monitor-item {
  padding: 14px 12px;
  background: rgba(31, 41, 55, 0.5);
  border-radius: 12px;
}

.monitor-item__header,
.alert-item__header,
.trace-result-card__head,
.trace-result-card__meta,
.trace-result-card__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.monitor-item__header span:first-child {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.monitor-item__header span:last-child {
  color: rgba(203, 213, 225, 0.78);
  font-size: 13px;
}

.monitor-item__track {
  width: 100%;
  height: 8px;
  margin-top: 10px;
  overflow: hidden;
  background: rgba(55, 65, 81, 0.95);
  border-radius: 999px;
}

.monitor-item__bar {
  height: 100%;
  background: linear-gradient(90deg, #22c55e, #22c55e);
  border-radius: inherit;
}

.monitor-item__bar--warning {
  background: linear-gradient(90deg, #fbbf24, #eab308);
}

.noise-board {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: center;
  min-height: 138px;
  padding: 18px 10px;
  background: rgba(31, 41, 55, 0.5);
  border-radius: 12px;
}

.noise-board__item {
  text-align: center;
}

.noise-board__item + .noise-board__item {
  border-left: 1px solid rgba(55, 65, 81, 0.95);
}

.noise-board__item p {
  margin: 0;
  font-size: 40px;
  font-weight: 700;
  line-height: 1;
  color: #4ade80;
}

.noise-board__item:nth-child(2) p {
  color: #facc15;
}

.noise-board__item:nth-child(3) p {
  color: #ffffff;
}

.noise-board__item span,
.noise-board__item em {
  display: block;
  margin-top: 8px;
  font-style: normal;
  color: rgba(203, 213, 225, 0.7);
}

.alert-item {
  padding: 14px 16px;
  border-radius: 12px;
}

.alert-item--warning {
  background: rgba(234, 179, 8, 0.1);
  border: 1px solid rgba(234, 179, 8, 0.3);
}

.alert-item--success {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.alert-item__header span:first-child {
  font-weight: 600;
}

.alert-item--warning .alert-item__header span:first-child {
  color: #facc15;
}

.alert-item--success .alert-item__header span:first-child {
  color: #34d399;
}

.alert-item__header span:last-child,
.alert-item p {
  color: rgba(203, 213, 225, 0.7);
  font-size: 12px;
}

.alert-item p {
  margin: 8px 0 0;
}

.prototype-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 160px;
  color: rgba(148, 163, 184, 0.72);
}

.prototype-empty--compact {
  min-height: 138px;
}

.trace-panel__toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.trace-panel__input {
  flex: 1;
  height: 40px;
  padding: 0 14px;
  color: #fff;
  font-size: 14px;
  background: rgba(31, 41, 55, 0.85);
  border: 1px solid rgba(55, 65, 81, 1);
  border-radius: 10px;
  outline: none;
}

.trace-panel__input::placeholder {
  color: rgba(148, 163, 184, 0.62);
}

.trace-panel__input:focus {
  border-color: rgba(59, 130, 246, 0.9);
}

.trace-panel__button {
  min-width: 72px;
  height: 40px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.trace-panel__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 340px;
  color: rgba(148, 163, 184, 0.76);
  text-align: center;
}

.trace-panel__empty-icon {
  margin-bottom: 12px;
  font-size: 56px;
  color: rgba(148, 163, 184, 0.7);
}

.trace-result-card {
  padding: 16px;
  background: rgba(31, 41, 55, 0.5);
  border: 1px solid rgba(53, 91, 148, 0.2);
  border-radius: 12px;
}

.trace-result-card__head strong {
  display: block;
  color: #fff;
  font-size: 16px;
}

.trace-result-card__head p {
  margin: 6px 0 0;
  color: rgba(203, 213, 225, 0.72);
  font-size: 13px;
}

.trace-result-card__meta {
  margin-top: 14px;
  color: rgba(203, 213, 225, 0.78);
  font-size: 13px;
}

.trace-result-card__actions {
  justify-content: flex-end;
  margin-top: 14px;
}

.detail-panel {
  padding: 16px 18px 12px;
}

.detail-panel__header {
  margin-bottom: 14px;
  font-size: 16px;
  font-weight: 600;
}

.detail-table {
  overflow: auto;
}

.detail-table__row {
  display: grid;
  align-items: center;
  min-height: 54px;
  border-bottom: 1px solid rgba(42, 62, 88, 0.52);
}

.detail-table__row--gmp {
  grid-template-columns: 1.6fr 1fr 1fr 1fr 1.5fr 0.8fr;
}

.detail-table__row--environment {
  grid-template-columns: 1.5fr 1fr 1fr 1fr 1fr 1.5fr 0.8fr;
}

.detail-table__row--batch {
  grid-template-columns: 1.2fr 1.5fr 1fr 1fr 1fr 1.5fr 0.8fr;
}

.detail-table__row span {
  padding: 0 12px;
  font-size: 13px;
  color: rgba(222, 234, 247, 0.9);
}

.detail-table__head {
  min-height: 42px;
  color: rgba(163, 184, 208, 0.72);
}

.detail-table__head span {
  font-size: 12px;
  color: rgba(163, 184, 208, 0.72);
}

.detail-table__primary {
  font-weight: 600;
}

.detail-table__empty,
.history-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: rgba(163, 184, 208, 0.62);
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  padding: 4px 10px;
  font-style: normal;
  font-size: 12px;
  border-radius: 8px;
}

.status-pill.is-success {
  color: #34d399;
  background: rgba(16, 185, 129, 0.16);
}

.status-pill.is-warning {
  color: #fbbf24;
  background: rgba(234, 179, 8, 0.16);
}

.status-pill.is-danger {
  color: #fb7185;
  background: rgba(244, 63, 94, 0.16);
}

.text-warning {
  color: #fbbf24 !important;
}

.view-button {
  padding: 6px 12px;
  color: #93c5fd;
  font-size: 12px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  background: rgba(59, 130, 246, 0.18);
}

.history-drawer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item,
.detail-dialog__item {
  padding: 14px;
  background: rgba(7, 18, 34, 0.92);
  border: 1px solid rgba(55, 97, 152, 0.2);
  border-radius: 12px;
}

.history-item__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
}

.history-item__meta,
.history-item__desc,
.history-item__time {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(163, 184, 208, 0.72);
}

.detail-dialog {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-dialog__item span {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: rgba(163, 184, 208, 0.72);
}

.detail-dialog__item strong {
  color: #fff;
  font-size: 14px;
}

@media (max-width: 1200px) {
  .environment-layout,
  .summary-grid,
  .detail-dialog {
    grid-template-columns: 1fr;
  }

  .compliance-header,
  .tab-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .compliance-search,
  .compliance-clock {
    width: 100%;
  }
}

@media (max-width: 960px) {
  .detail-table__row--gmp,
  .detail-table__row--environment,
  .detail-table__row--batch {
    min-width: 860px;
  }

  .trace-panel__toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .trace-panel__button {
    width: 100%;
  }

  .noise-board {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .noise-board__item + .noise-board__item {
    padding-top: 12px;
    border-top: 1px solid rgba(55, 65, 81, 0.95);
    border-left: none;
  }
}
</style>
