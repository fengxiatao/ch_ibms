<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { EChartsOption } from 'echarts'
import { ElMessage } from 'element-plus'
import { Echart } from '@/components/Echart'
import dayjs from 'dayjs'
import {
  getParkingDashboardOverview,
  type ParkingDashboardOverviewRespVO
} from '@/api/access/parkingDashboard'
import { ParkingReportApi, type ParkingReportOverviewRespVO } from '@/api/access/parkingReport'
import { ParkingLotApi } from '@/api/iot/parking'
import { ParkingPresentApi } from '@/api/access/parkingPresent'
import { StatisticsApi, type IotStatisticsSummaryRespVO } from '@/api/iot/statistics'
import { AlertRecordApi } from '@/api/iot/alert/record'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/store/modules/app'

defineOptions({ name: 'ParkingDashboard' })

const appStore = useAppStore()
const router = useRouter()
const loading = ref(false)
const overview = ref<ParkingDashboardOverviewRespVO>()
const trafficReport = ref<ParkingReportOverviewRespVO>()
const revenueReport = ref<ParkingReportOverviewRespVO>()
const todayCompare = ref<{ inTotal: number; outTotal: number }>()
const yesterdayCompare = ref<{ inTotal: number; outTotal: number }>()
const deviceSummary = ref<IotStatisticsSummaryRespVO>()
const deviceLastSyncAt = ref<number>()
const warningCounts = ref<{ space: number; longtime: number; noplate: number; device: number }>()
const warningLastSyncAt = ref<number>()
const alertCountToday = ref<number>()
const alertCountYesterday = ref<number>()
const lotId = ref<number>()
const updateTime = ref('')
const updateTimer = ref<number>()
const trafficRange = ref<'today' | 'yesterday' | 'week'>('today')
const isDark = computed(
  () => appStore.getIsDark || document.documentElement.classList.contains('dark')
)

const fetchOverview = async () => {
  try {
    overview.value = await getParkingDashboardOverview(lotId.value)
  } catch (error) {
    overview.value = undefined
    ElMessage.warning('获取停车看板数据失败')
  }
}

const fetchTrafficReport = async () => {
  const today = dayjs()
  const { start, end } =
    trafficRange.value === 'yesterday'
      ? { start: today.subtract(1, 'day'), end: today.subtract(1, 'day') }
      : trafficRange.value === 'week'
        ? { start: today.subtract(6, 'day'), end: today }
        : { start: today, end: today }

  try {
    trafficReport.value = await ParkingReportApi.getOverview({
      lotId: lotId.value,
      startDate: start.format('YYYY-MM-DD'),
      endDate: end.format('YYYY-MM-DD'),
      granularity: 'day'
    })
  } catch (error) {
    ElMessage.warning('获取停车报表数据失败')
    trafficReport.value = undefined
  }
}

const fetchRevenueReport = async () => {
  const today = dayjs()
  try {
    revenueReport.value = await ParkingReportApi.getOverview({
      lotId: lotId.value,
      startDate: today.subtract(6, 'day').format('YYYY-MM-DD'),
      endDate: today.format('YYYY-MM-DD'),
      granularity: 'day'
    })
  } catch (error) {
    revenueReport.value = undefined
  }
}

const initLot = async () => {
  try {
    const list = await ParkingLotApi.getSimpleList()
    if (Array.isArray(list) && list.length > 0) {
      const first = list[0] as any
      if (typeof first?.id === 'number') lotId.value = first.id
    }
  } catch (error) {
    lotId.value = undefined
  }
}

const fetchDeviceSummary = async () => {
  try {
    deviceSummary.value = await StatisticsApi.getStatisticsSummary()
    deviceLastSyncAt.value = Date.now()
  } catch (error) {
    deviceSummary.value = undefined
    deviceLastSyncAt.value = undefined
  }
}

const fetchWarningCounts = async () => {
  try {
    const baseParams: any = { pageNo: 1, pageSize: 1, lotId: lotId.value }
    const [spaceRes, longtimeRes, noplateRes, deviceRes] = await Promise.all([
      ParkingPresentApi.getPage({ ...baseParams, warningType: 1 }),
      ParkingPresentApi.getPage({ ...baseParams, warningType: 2 }),
      ParkingPresentApi.getPage({ ...baseParams, warningType: 3 }),
      ParkingPresentApi.getPage({ ...baseParams, warningType: 4 })
    ])
    warningCounts.value = {
      space: Number(spaceRes?.total || 0),
      longtime: Number(longtimeRes?.total || 0),
      noplate: Number(noplateRes?.total || 0),
      device: Number(deviceRes?.total || 0)
    }
    warningLastSyncAt.value = Date.now()
  } catch (error) {
    warningCounts.value = undefined
    warningLastSyncAt.value = undefined
  }
}

const fetchAlertCounts = async () => {
  try {
    const now = dayjs()
    const todayRange = [
      now.startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      now.endOf('day').format('YYYY-MM-DD HH:mm:ss')
    ]
    const yesterdayRange = [
      now.subtract(1, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      now.subtract(1, 'day').endOf('day').format('YYYY-MM-DD HH:mm:ss')
    ]
    const [todayRes, yesterdayRes] = await Promise.all([
      AlertRecordApi.getAlertRecordPage({ pageNo: 1, pageSize: 1, createTime: todayRange }),
      AlertRecordApi.getAlertRecordPage({ pageNo: 1, pageSize: 1, createTime: yesterdayRange })
    ])
    alertCountToday.value = typeof todayRes?.total === 'number' ? todayRes.total : Number(todayRes?.total || 0)
    alertCountYesterday.value =
      typeof yesterdayRes?.total === 'number' ? yesterdayRes.total : Number(yesterdayRes?.total || 0)
  } catch (error) {
    alertCountToday.value = undefined
    alertCountYesterday.value = undefined
  }
}

const refreshAll = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchOverview(),
      fetchTrafficReport(),
      fetchRevenueReport(),
      fetchCompare(),
      fetchDeviceSummary(),
      fetchWarningCounts(),
      fetchAlertCounts()
    ])
  } finally {
    loading.value = false
  }
}

const sumPeak = (data?: ParkingReportOverviewRespVO) => {
  const rows = data?.peakRows ?? []
  return rows.reduce(
    (acc, cur) => {
      acc.inTotal += cur.inCount
      acc.outTotal += cur.outCount
      return acc
    },
    { inTotal: 0, outTotal: 0 }
  )
}

const fetchCompare = async () => {
  const today = dayjs()
  try {
    const [todayData, yesterdayData] = await Promise.all([
      ParkingReportApi.getOverview({
        lotId: lotId.value,
        startDate: today.format('YYYY-MM-DD'),
        endDate: today.format('YYYY-MM-DD'),
        granularity: 'day'
      }),
      ParkingReportApi.getOverview({
        lotId: lotId.value,
        startDate: today.subtract(1, 'day').format('YYYY-MM-DD'),
        endDate: today.subtract(1, 'day').format('YYYY-MM-DD'),
        granularity: 'day'
      })
    ])
    todayCompare.value = sumPeak(todayData)
    yesterdayCompare.value = sumPeak(yesterdayData)
  } catch (error) {
    todayCompare.value = undefined
    yesterdayCompare.value = undefined
  }
}

onMounted(async () => {
  await initLot()
  await refreshAll()
  const tick = () => {
    const now = new Date()
    const hh = String(now.getHours()).padStart(2, '0')
    const mm = String(now.getMinutes()).padStart(2, '0')
    const ss = String(now.getSeconds()).padStart(2, '0')
    updateTime.value = `${hh}:${mm}:${ss}`
  }
  tick()
  updateTimer.value = window.setInterval(tick, 1000)
})

onBeforeUnmount(() => {
  if (updateTimer.value) window.clearInterval(updateTimer.value)
})

watch(trafficRange, () => {
  fetchTrafficReport()
})

const usageRatePercent = computed(() => Math.round((overview.value?.usageRate ?? 0) * 100))

const formatMom = (todayValue: number, yesterdayValue: number) => {
  if (!yesterdayValue) return '--'
  const rate = ((todayValue - yesterdayValue) / yesterdayValue) * 100
  const fixed = Math.abs(rate) >= 100 ? rate.toFixed(0) : rate.toFixed(1)
  return `${rate >= 0 ? '+' : ''}${fixed}%`
}

const inMomText = computed(() => {
  const todayTotal = todayCompare.value?.inTotal
  const yesterdayTotal = yesterdayCompare.value?.inTotal
  if (typeof todayTotal !== 'number' || typeof yesterdayTotal !== 'number') return '--'
  return formatMom(todayTotal, yesterdayTotal)
})

const outMomText = computed(() => {
  const todayTotal = todayCompare.value?.outTotal
  const yesterdayTotal = yesterdayCompare.value?.outTotal
  if (typeof todayTotal !== 'number' || typeof yesterdayTotal !== 'number') return '--'
  return formatMom(todayTotal, yesterdayTotal)
})

const agoText = (ts?: number) => {
  if (!ts) return '--'
  const diff = Math.max(0, Date.now() - ts)
  if (diff < 10_000) return '刚刚'
  const minutes = Math.floor(diff / 60_000)
  if (minutes < 1) return '1分钟前'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  return `${hours}小时前`
}

const deviceOnlineText = computed(() => {
  const v = deviceSummary.value?.deviceOnlineCount
  return typeof v === 'number' ? String(v) : '--'
})

const deviceOfflineText = computed(() => {
  const v = deviceSummary.value?.deviceOfflineCount
  return typeof v === 'number' ? String(v) : '--'
})

const deviceLastUpdateText = computed(() => agoText(deviceLastSyncAt.value))

const warningTotal = computed(() => {
  const c = warningCounts.value
  if (!c) return undefined
  return c.space + c.longtime + c.noplate + c.device
})

const warningTotalText = computed(() => {
  const v = warningTotal.value
  if (typeof v === 'number') return String(v)
  const fallback = overview.value?.alertCount
  return typeof fallback === 'number' ? String(fallback) : '--'
})

const warningLastUpdateText = computed(() => agoText(warningLastSyncAt.value))

const alertDiffText = computed(() => {
  const t = alertCountToday.value
  const y = alertCountYesterday.value
  if (typeof t !== 'number' || typeof y !== 'number') return '--'
  const diff = t - y
  return `${diff >= 0 ? '+' : ''}${diff}`
})

const warningCardValueText = computed(() => {
  const t = alertCountToday.value
  if (typeof t === 'number') return String(t)
  const fallback = overview.value?.alertCount
  return typeof fallback === 'number' ? String(fallback) : '--'
})

const warningDistribution = computed(() => {
  const c = warningCounts.value
  const total = c ? c.space + c.longtime + c.noplate + c.device : 0
  const toPercent = (count: number) => (total ? Math.round((count / total) * 100) : 0)
  return [
    { label: '余位不足预警', count: c?.space ?? 0, percent: toPercent(c?.space ?? 0), color: '#fbbf24' },
    { label: '长时间停车预警', count: c?.longtime ?? 0, percent: toPercent(c?.longtime ?? 0), color: '#fb923c' },
    { label: '无牌车预警', count: c?.noplate ?? 0, percent: toPercent(c?.noplate ?? 0), color: '#a78bfa' },
    { label: '设备异常预警', count: c?.device ?? 0, percent: toPercent(c?.device ?? 0), color: '#f87171' }
  ]
})

const trafficOption = computed<EChartsOption>(() => {
  const rows = trafficReport.value?.peakRows ?? []
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 24, right: 24, top: 48, bottom: 24, containLabel: true },
    xAxis: { type: 'category', data: rows.map((r) => r.period) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '入场车辆',
        type: 'bar',
        data: rows.map((r) => r.inCount),
        barMaxWidth: 26,
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '出场车辆',
        type: 'bar',
        data: rows.map((r) => r.outCount),
        barMaxWidth: 26,
        itemStyle: { color: '#10b981' }
      }
    ]
  }
})

const incomeTrendOption = computed<EChartsOption>(() => {
  const rows = revenueReport.value?.revenueRows ?? []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 24, right: 24, top: 24, bottom: 24, containLabel: true },
    xAxis: { type: 'category', data: rows.map((r) => r.period) },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        name: '收入（元）',
        data: rows.map((r) => r.total),
        barMaxWidth: 30,
        itemStyle: { color: '#f97316' }
      }
    ]
  }
})

const goPresent = async () => {
  await router.push('/smart-access/parking/present').catch(() => {
    ElMessage.warning('未找到在场车辆监控路由，请检查后端菜单 path 配置')
  })
}
</script>

<template>
  <div class="parking-dashboard parking-page" :class="[{ 'is-loading': loading }, { dark: isDark }]">
    <section class="dashboard-section dashboard-section--overview">
      <div class="dashboard-section__header">
        <div class="overview-title">
          <div class="overview-title__icon">
            <Icon icon="ep:histogram" :size="18" />
          </div>
          <div>
            <div class="overview-title__name">停车场管理</div>
            <div class="overview-title__sub">实时更新于 {{ updateTime }}</div>
          </div>
        </div>
        <div class="overview-actions">
          <el-tag type="success" effect="dark" class="overview-actions__tag">数据实时同步中</el-tag>
          <el-button size="small" type="primary" @click="refreshAll">
            <Icon icon="ep:refresh" class="btn-icon" />
            刷新
          </el-button>
        </div>
      </div>

      <div class="overview-grid">
        <el-card shadow="never" class="panel panel-capacity">
          <div class="panel-title">
            <span>车场容量监控</span>
            <div class="panel-title-icon is-blue">
              <span class="capacity-badge">P</span>
            </div>
          </div>
          <div class="capacity-main">
            <div class="capacity-value">
              <span class="capacity-used">{{ overview?.usedSpaces ?? '--' }}</span>
              <span class="capacity-split">/</span>
              <span class="capacity-total">{{ overview?.totalSpaces ?? '--' }}</span>
              <span class="capacity-unit">车位</span>
            </div>
            <div class="capacity-progress">
              <div class="capacity-progress-meta">
                <span>使用率</span>
                <span class="capacity-progress-rate">{{ usageRatePercent }}%</span>
              </div>
              <el-progress :percentage="usageRatePercent" :stroke-width="12" :show-text="false" />
            </div>
            <div class="capacity-metrics">
              <div class="metric metric-green">
                <div class="metric-value">{{ overview?.freeSpaces ?? '--' }}</div>
                <div class="metric-label">剩余空位</div>
              </div>
              <div class="metric metric-blue">
                <div class="metric-value">{{ overview?.tempVehicleCount ?? '--' }}</div>
                <div class="metric-label">临时车辆</div>
              </div>
              <div class="metric metric-purple">
                <div class="metric-value">{{ overview?.monthlyVehicleCount ?? '--' }}</div>
                <div class="metric-label">月卡车辆</div>
              </div>
            </div>
          </div>
        </el-card>

        <div class="overview-flow">
          <div class="overview-flow__stats">
            <el-card shadow="never" class="panel panel-stat panel-stat--in">
              <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--in">
                  <Icon icon="ri:login-box-line" :size="20" />
                </div>
                <div class="stat-card__label">今日进场车辆</div>
                <div class="stat-card__value">{{ overview?.todayInCount ?? '--' }}</div>
                <div class="stat-card__meta">
                  <span class="stat-card__meta-label">较昨日</span>
                  <span class="stat-card__meta-rate stat-card__meta-rate--in">{{ inMomText }}</span>
                </div>
              </div>
            </el-card>
            <el-card shadow="never" class="panel panel-stat panel-stat--out">
              <div class="stat-card">
                <div class="stat-card__icon stat-card__icon--out">
                  <Icon icon="ri:logout-box-line" :size="20" />
                </div>
                <div class="stat-card__label">今日出场车辆</div>
                <div class="stat-card__value">{{ overview?.todayOutCount ?? '--' }}</div>
                <div class="stat-card__meta">
                  <span class="stat-card__meta-label">较昨日</span>
                  <span class="stat-card__meta-rate stat-card__meta-rate--out">{{ outMomText }}</span>
                </div>
              </div>
            </el-card>
          </div>

          <el-card shadow="never" class="panel panel-action">
            <el-button type="default" class="action-btn" @click="goPresent">
              <Icon icon="ep:location" class="btn-icon" />
              查看在场车辆
            </el-button>
          </el-card>
        </div>
      </div>
    </section>

    <section class="dashboard-mid">
      <el-card shadow="never" class="panel panel-stat panel-stat--income dashboard-mid__income">
        <div class="stat-head">
          <div class="stat-label">今日收入</div>
          <div class="panel-title-icon is-orange">
            <Icon icon="ep:money" :size="16" />
          </div>
        </div>
        <div class="stat-value">
          <span v-if="typeof overview?.todayIncome === 'number'">¥{{ overview.todayIncome.toFixed(0) }}</span>
          <span v-else>--</span>
        </div>
        <div class="stat-foot">
          较昨日 {{ revenueReport?.revenueRows?.length ? revenueReport.revenueRows.at(-1)?.momText || '--' : '--' }}
        </div>
      </el-card>

      <el-card shadow="never" class="panel panel-stat panel-stat--warning dashboard-mid__warning">
        <div class="stat-head">
          <div class="stat-label">预警数量</div>
          <div class="panel-title-icon is-yellow">
            <Icon icon="ep:warning" :size="16" />
          </div>
        </div>
        <div class="stat-value">{{ warningCardValueText }}</div>
        <div class="stat-foot">较昨日 {{ alertDiffText }}</div>
      </el-card>

      <el-card shadow="never" class="panel panel-device dashboard-mid__device">
        <div class="panel-title">
          <span>设备运行状态</span>
          <span class="panel-subtitle">最后更新: {{ deviceLastUpdateText }}</span>
        </div>
        <div class="device-metrics">
          <div class="device-metric is-online">
            <div class="device-metric-icon is-online">
              <Icon icon="ep:connection" :size="16" />
            </div>
            <div class="device-metric-content">
              <div class="device-metric-value">{{ deviceOnlineText }}</div>
              <div class="device-metric-label">在线设备</div>
            </div>
          </div>
          <div class="device-metric is-offline">
            <div class="device-metric-icon is-offline">
              <Icon icon="ep:close" :size="16" />
            </div>
            <div class="device-metric-content">
              <div class="device-metric-value">{{ deviceOfflineText }}</div>
              <div class="device-metric-label">离线设备</div>
            </div>
          </div>
        </div>
      </el-card>
    </section>

    <section class="dashboard-bottom">
      <el-card shadow="never" class="panel panel-chart panel-traffic dashboard-bottom__traffic">
        <div class="panel-title">
          <div>
            <div class="panel-title-text">今日时段流量分析</div>
            <div class="panel-subtitle">每小时车辆进出统计</div>
          </div>
          <el-select v-model="trafficRange" size="small" class="traffic-range">
            <el-option label="今日" value="today" />
            <el-option label="昨日" value="yesterday" />
            <el-option label="近7天" value="week" />
          </el-select>
        </div>
        <div class="chart-body chart-body--lg">
          <Echart :options="trafficOption" height="100%" />
        </div>
      </el-card>

      <div class="dashboard-aside">
        <el-card shadow="never" class="panel panel-chart panel-income">
          <div class="panel-title">
            <div class="panel-title-text">近7日收入趋势</div>
          </div>
          <div class="chart-body chart-body--sm">
            <Echart :options="incomeTrendOption" height="100%" />
          </div>
        </el-card>

        <el-card shadow="never" class="panel panel-warning">
          <div class="panel-title">
            <div class="panel-title-text">预警类型分布</div>
          </div>
          <div class="warning-body">
            <div v-for="item in warningDistribution" :key="item.label" class="warning-row">
              <div class="warning-label">{{ item.label }}</div>
              <div class="warning-bar">
                <el-progress
                  :percentage="item.percent"
                  :stroke-width="10"
                  :show-text="false"
                  :color="item.color"
                />
              </div>
              <div class="warning-percent">{{ item.percent }}%</div>
            </div>
          </div>
        </el-card>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.parking-page {
  padding-top: 0;
  box-sizing: border-box;
}

.parking-dashboard {
  background: var(--el-bg-color-page);
  padding: 24px;
  padding-top: calc(
    24px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)))
  );
  display: flex;
  flex-direction: column;
  gap: 24px;
  box-sizing: border-box;
  min-height: calc(
    100vh - var(--top-tool-height) - var(--tags-view-height) - var(--app-footer-height) -
      var(--app-content-padding) - var(--app-content-padding)
  );
}

.btn-icon {
  margin-right: 6px;
}

.dashboard-section {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  padding: 24px;
}

.dark .dashboard-section {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.35);
}

.dashboard-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.overview-title {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.overview-title__icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1677ff;
  background: rgba(22, 119, 255, 0.12);
}

.overview-title__name {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.overview-title__sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.overview-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.overview-actions__tag {
  border-radius: 999px;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 2fr);
  gap: 24px;
}

.overview-flow {
  display: grid;
  grid-template-rows: 4fr 1fr;
  gap: 24px;
  min-height: 0;
}

.overview-flow__stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  min-height: 0;
  height: 100%;
}

.panel {
  height: 100%;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.dark .panel {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.35);
}

.panel :deep(.el-card__body) {
  height: 100%;
  padding: 20px;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.panel-title-text {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.panel-subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.panel-title-icon {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
}

.panel-title-icon.is-blue {
  color: #1677ff;
  background: rgba(22, 119, 255, 0.12);
  border-color: rgba(22, 119, 255, 0.18);
}

.panel-title-icon.is-green {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.12);
  border-color: rgba(82, 196, 26, 0.18);
}

.panel-title-icon.is-purple {
  color: #722ed1;
  background: rgba(114, 46, 209, 0.12);
  border-color: rgba(114, 46, 209, 0.18);
}

.panel-title-icon.is-orange {
  color: #fa8c16;
  background: rgba(250, 140, 22, 0.12);
  border-color: rgba(250, 140, 22, 0.18);
}

.panel-title-icon.is-yellow {
  color: #faad14;
  background: rgba(250, 173, 20, 0.12);
  border-color: rgba(250, 173, 20, 0.18);
}

.traffic-range {
  width: 110px;
}

.panel-capacity {
  border: none;
  background: linear-gradient(135deg, #1f2937 0%, #0f172a 100%);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.12);
}

.dark .panel-capacity {
  background: linear-gradient(135deg, #111827 0%, #020617 100%);
}

.panel-capacity .panel-title,
.panel-capacity .panel-title-text {
  color: rgba(255, 255, 255, 0.92);
}

.panel-capacity .panel-title-icon.is-blue {
  color: #60a5fa;
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  font-weight: 800;
  font-size: 14px;
}

.capacity-badge {
  line-height: 1;
}

.capacity-main {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(100% - 34px);
  min-height: 0;
}

.panel-capacity .capacity-main {
  padding: 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.panel-capacity .capacity-progress {
  padding: 12px;
  border-radius: 12px;
  background: rgba(2, 6, 23, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.capacity-value {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.capacity-used {
  font-size: clamp(26px, 2.2vw, 44px);
  font-weight: 800;
  color: var(--el-text-color-primary);
  letter-spacing: -0.02em;
}

.capacity-split,
.capacity-total {
  color: var(--el-text-color-secondary);
  font-size: 18px;
}

.capacity-unit {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-left: 4px;
}

.capacity-progress-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.capacity-progress-rate {
  color: #fa8c16;
  font-weight: 700;
}

.panel-capacity .capacity-used {
  color: rgba(255, 255, 255, 0.92);
}

.panel-capacity .capacity-split,
.panel-capacity .capacity-total,
.panel-capacity .capacity-unit,
.panel-capacity .capacity-progress-meta {
  color: rgba(226, 232, 240, 0.75);
}

.panel-capacity .capacity-progress-rate {
  color: #fbbf24;
}

.panel-capacity :deep(.el-progress-bar__outer) {
  background: #334155;
}

.panel-capacity :deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #fbbf24 0%, #f97316 100%);
}

.capacity-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: auto;
}

.metric {
  border-radius: 12px;
  padding: 10px 10px;
  border: 1px solid var(--el-border-color-lighter);
  background: rgba(255, 255, 255, 0.7);
}

.panel-capacity .metric {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

::global(.dark) .metric,
.dark .metric {
  background: rgba(18, 18, 18, 0.7);
}

.metric-value {
  font-size: 20px;
  font-weight: 800;
}

.metric-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.panel-capacity .metric-label {
  color: rgba(226, 232, 240, 0.7);
}

.metric-green .metric-value {
  color: #52c41a;
}

.metric-blue .metric-value {
  color: #1677ff;
}

.metric-purple .metric-value {
  color: #722ed1;
}

.panel-stat :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.panel-stat--in {
  position: relative;
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.14) 0%, rgba(255, 255, 255, 1) 68%);
  border-color: rgba(34, 197, 94, 0.22);
}

.panel-stat--out {
  position: relative;
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.14) 0%, rgba(255, 255, 255, 1) 68%);
  border-color: rgba(168, 85, 247, 0.22);
}

.dark .panel-stat--in {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.18) 0%, rgba(18, 18, 18, 0.92) 70%);
  border-color: rgba(34, 197, 94, 0.22);
}

.dark .panel-stat--out {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.2) 0%, rgba(18, 18, 18, 0.92) 70%);
  border-color: rgba(168, 85, 247, 0.24);
}

.panel-stat--in::before,
.panel-stat--in::after,
.panel-stat--out::before,
.panel-stat--out::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.panel-stat--in::before {
  width: 140px;
  height: 140px;
  right: -72px;
  top: -84px;
  background: rgba(34, 197, 94, 0.14);
}

.panel-stat--in::after {
  width: 90px;
  height: 90px;
  right: -38px;
  top: -40px;
  border: 1px solid rgba(34, 197, 94, 0.22);
  background: transparent;
}

.panel-stat--out::before {
  width: 140px;
  height: 140px;
  right: -72px;
  top: -84px;
  background: rgba(168, 85, 247, 0.14);
}

.panel-stat--out::after {
  width: 90px;
  height: 90px;
  right: -38px;
  top: -40px;
  border: 1px solid rgba(168, 85, 247, 0.22);
  background: transparent;
}

.panel-stat--in :deep(.el-card__body),
.panel-stat--out :deep(.el-card__body) {
  position: relative;
  z-index: 1;
  padding: 24px;
}

.stat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-card__icon--in {
  background: #22c55e;
  box-shadow: 0 10px 18px rgba(34, 197, 94, 0.28);
}

.stat-card__icon--out {
  background: #a855f7;
  box-shadow: 0 10px 18px rgba(168, 85, 247, 0.28);
}

.dark .stat-card__icon--in,
.dark .stat-card__icon--out {
  box-shadow: 0 10px 18px rgba(0, 0, 0, 0.35);
}

.stat-card__label {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.stat-card__value {
  font-size: clamp(32px, 2.6vw, 44px);
  font-weight: 800;
  color: var(--el-text-color-primary);
  letter-spacing: -0.02em;
}

.stat-card__meta {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.stat-card__meta-label {
  color: var(--el-text-color-secondary);
}

.stat-card__meta-rate {
  font-weight: 700;
}

.stat-card__meta-rate--in {
  color: #16a34a;
}

.stat-card__meta-rate--out {
  color: #7c3aed;
}

.panel-stat--income {
  border: none;
  background: linear-gradient(135deg, #f97316 0%, #ef4444 100%);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.12);
}

.panel-stat--warning {
  border: none;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.12);
}

.stat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 600;
}

.stat-value {
  font-size: clamp(22px, 1.8vw, 38px);
  font-weight: 800;
  color: var(--el-text-color-primary);
  letter-spacing: -0.02em;
}

.stat-foot {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.panel-stat--income .stat-label,
.panel-stat--warning .stat-label,
.panel-stat--income .stat-value,
.panel-stat--warning .stat-value,
.panel-stat--income .stat-foot,
.panel-stat--warning .stat-foot {
  color: rgba(255, 255, 255, 0.92);
}

.panel-stat--income .stat-label,
.panel-stat--warning .stat-label,
.panel-stat--income .stat-foot,
.panel-stat--warning .stat-foot {
  opacity: 0.88;
}

.panel-stat--in .panel-title-icon,
.panel-stat--out .panel-title-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: none;
  color: #fff;
}

.panel-stat--in .panel-title-icon {
  background: #22c55e;
  box-shadow: 0 10px 18px rgba(34, 197, 94, 0.28);
}

.panel-stat--out .panel-title-icon {
  background: #a855f7;
  box-shadow: 0 10px 18px rgba(168, 85, 247, 0.28);
}

.panel-stat--income .panel-title-icon,
.panel-stat--warning .panel-title-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.92);
}

.panel-action :deep(.el-card__body) {
  height: 100%;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn {
  width: 25%;
  min-width: 180px;
  max-width: 260px;
  height: 44px;
  border-radius: 12px;
  font-weight: 700;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  color: #374151;
}

.action-btn:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}

.panel-device :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.device-metrics {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: auto;
}

.device-metric {
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  gap: 12px;
}

.device-metric.is-online {
  background: rgba(34, 197, 94, 0.08);
  border-color: rgba(34, 197, 94, 0.18);
}

.device-metric.is-offline {
  background: rgba(239, 68, 68, 0.08);
  border-color: rgba(239, 68, 68, 0.18);
}

.device-metric-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.device-metric-icon.is-online {
  background: #22c55e;
}

.device-metric-icon.is-offline {
  background: #ef4444;
}

.device-metric-value {
  font-size: 22px;
  font-weight: 800;
}

.device-metric.is-online .device-metric-value {
  color: #52c41a;
}

.device-metric.is-offline .device-metric-value {
  color: #f5222d;
}

.device-metric-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.panel-chart :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
}

.chart-body {
  flex: 1;
  min-height: 0;
}

.chart-body--lg {
  height: 320px;
}

.chart-body--sm {
  height: 200px;
}

.panel-warning :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
}

.warning-body {
  display: grid;
  grid-auto-rows: minmax(0, 1fr);
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.warning-row {
  display: grid;
  grid-template-columns: 110px 1fr 44px;
  align-items: center;
  gap: 10px;
}

.warning-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 600;
}

.warning-percent {
  font-size: 12px;
  text-align: right;
  color: var(--el-text-color-primary);
  font-weight: 700;
}

.dashboard-mid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 24px;
}

.dashboard-mid__income {
  grid-column: span 12;
}

.dashboard-mid__warning {
  grid-column: span 12;
}

.dashboard-mid__device {
  grid-column: span 12;
}

.dashboard-bottom {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 24px;
  min-height: 0;
}

.dashboard-bottom__traffic {
  grid-column: span 12;
}

.dashboard-aside {
  grid-column: span 12;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 0;
}

@media (min-width: 992px) {
  .overview-grid {
    grid-template-columns: minmax(0, 1fr) minmax(0, 2fr);
  }

  .dashboard-mid__income {
    grid-column: span 3;
  }

  .dashboard-mid__warning {
    grid-column: span 3;
  }

  .dashboard-mid__device {
    grid-column: span 6;
  }

  .dashboard-bottom__traffic {
    grid-column: span 8;
  }

  .dashboard-aside {
    grid-column: span 4;
  }
}

@media (max-width: 991px) {
  .dashboard-section__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .overview-flow__stats {
    grid-template-columns: 1fr;
  }

  .overview-flow {
    grid-template-rows: auto auto;
  }

  .action-btn {
    width: 100%;
    min-width: 0;
    max-width: none;
  }
}
</style>

