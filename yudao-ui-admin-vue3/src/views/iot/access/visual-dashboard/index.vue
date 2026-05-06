<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import {
  getAccessDashboardStatistics,
  getAccessTrend,
  type AccessDashboardStatisticsRespVO
} from '@/api/iot/access/dashboard'

defineOptions({ name: 'AccessVisualDashboard' })

type DateRangeKey = 'today' | 'week' | 'month' | 'year' | 'custom'

interface AccessTrendData {
  labels: string[]
  accessData: number[]
  rejectData: number[]
}

interface ChartDataSet {
  accessTrend: AccessTrendData
  alertCategory: { labels: string[]; data: number[] }
  alertTrend: { labels: string[]; data: number[] }
  companyAccess: { labels: string[]; data: number[] }
}

const dateRange = ref<DateRangeKey>('today')
const customStartDate = ref('')
const customEndDate = ref('')
const customData = ref<ChartDataSet | null>(null)

const showAccess = ref(true)
const showReject = ref(true)

// === 真实数据（M2-A + M2-D：AccessDashboardController.statistics + trend）===
// statistics 接口提供 4 metric 卡片 + 设备分布 + 通行类型分布。
// trend 接口 today/week/month/year 全部走真实数据（同日 24 槽，跨日逐日槽）。
// 告警分类饰图 / 告警趋势 / 公司排行 后端尚无聚合端点，保留 mock。
// 详见 docs/ibms-bidirectional-gap.md GAP-001/GAP-007。
const liveStats = ref<AccessDashboardStatisticsRespVO | null>(null)
const liveTrendByRange = ref<Partial<Record<DateRangeKey, AccessTrendData>>>({})

const formatNumber = (value: number) => new Intl.NumberFormat('zh-CN').format(value)
const sum = (arr: number[]) => arr.reduce((acc, cur) => acc + cur, 0)

const builtinData: Record<Exclude<DateRangeKey, 'custom'>, ChartDataSet> = {
  today: {
    accessTrend: {
      labels: Array.from({ length: 24 }, (_, i) => `${i}:00`),
      accessData: [
        12, 19, 15, 8, 5, 3, 2, 5, 18, 45, 89, 120, 156, 145, 130, 110, 95, 80, 65, 78, 90, 65, 40,
        25
      ],
      rejectData: [2, 1, 3, 1, 0, 0, 1, 2, 5, 8, 12, 15, 18, 14, 10, 8, 5, 7, 4, 3, 2, 1, 1, 0]
    },
    alertCategory: {
      labels: ['无效卡', '未授权访问', '门异常开启', '超时未关门', '胁迫开门', '其他'],
      data: [18, 12, 8, 5, 3, 2]
    },
    alertTrend: {
      labels: [
        '0:00',
        '2:00',
        '4:00',
        '6:00',
        '8:00',
        '10:00',
        '12:00',
        '14:00',
        '16:00',
        '18:00',
        '20:00',
        '22:00'
      ],
      data: [0, 1, 0, 2, 5, 8, 7, 6, 5, 4, 3, 2]
    },
    companyAccess: {
      labels: [
        '科技有限公司',
        '金融服务公司',
        '文化传媒公司',
        '建筑工程公司',
        '咨询顾问公司',
        '电商平台公司',
        '物流运输公司'
      ],
      data: [856, 628, 452, 389, 278, 215, 168]
    }
  },
  week: {
    accessTrend: {
      labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      accessData: [2150, 2380, 2450, 2280, 2560, 980, 850],
      rejectData: [35, 42, 38, 45, 48, 15, 12]
    },
    alertCategory: {
      labels: ['无效卡', '未授权访问', '门异常开启', '超时未关门', '胁迫开门', '其他'],
      data: [95, 68, 42, 30, 18, 12]
    },
    alertTrend: {
      labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      data: [42, 48, 52, 45, 58, 20, 18]
    },
    companyAccess: {
      labels: [
        '科技有限公司',
        '金融服务公司',
        '文化传媒公司',
        '建筑工程公司',
        '咨询顾问公司',
        '电商平台公司',
        '物流运输公司'
      ],
      data: [5280, 3850, 2860, 2450, 1850, 1420, 1080]
    }
  },
  month: {
    accessTrend: {
      labels: Array.from({ length: 30 }, (_, i) => `${i + 1}日`),
      accessData: [
        2150, 2380, 2450, 2280, 2560, 980, 850, 2280, 2450, 2520, 2380, 2650, 1050, 920, 2350, 2520,
        2680, 2450, 2780, 1120, 980, 2420, 2580, 2750, 2620, 2850, 1250, 1050, 2380, 2550
      ],
      rejectData: [
        35, 42, 38, 45, 48, 15, 12, 38, 45, 42, 48, 52, 18, 15, 42, 48, 52, 45, 58, 20, 18, 45, 52,
        48, 55, 60, 22, 20, 40, 46
      ]
    },
    alertCategory: {
      labels: ['无效卡', '未授权访问', '门异常开启', '超时未关门', '胁迫开门', '其他'],
      data: [420, 285, 185, 125, 75, 55]
    },
    alertTrend: {
      labels: ['第1周', '第2周', '第3周', '第4周'],
      data: [185, 205, 220, 210]
    },
    companyAccess: {
      labels: [
        '科技有限公司',
        '金融服务公司',
        '文化传媒公司',
        '建筑工程公司',
        '咨询顾问公司',
        '电商平台公司',
        '物流运输公司'
      ],
      data: [21500, 15800, 11800, 9850, 7850, 6200, 4850]
    }
  },
  year: {
    accessTrend: {
      labels: [
        '1月',
        '2月',
        '3月',
        '4月',
        '5月',
        '6月',
        '7月',
        '8月',
        '9月',
        '10月',
        '11月',
        '12月'
      ],
      accessData: [
        48500, 45200, 52800, 56500, 62800, 65500, 68200, 70500, 68800, 65200, 60800, 58500
      ],
      rejectData: [850, 780, 920, 980, 1050, 1120, 1180, 1250, 1180, 1080, 950, 880]
    },
    alertCategory: {
      labels: ['无效卡', '未授权访问', '门异常开启', '超时未关门', '胁迫开门', '其他'],
      data: [4850, 3250, 2150, 1450, 850, 650]
    },
    alertTrend: {
      labels: [
        '1月',
        '2月',
        '3月',
        '4月',
        '5月',
        '6月',
        '7月',
        '8月',
        '9月',
        '10月',
        '11月',
        '12月'
      ],
      data: [850, 780, 920, 980, 1050, 1120, 1180, 1250, 1180, 1080, 950, 880]
    },
    companyAccess: {
      labels: [
        '科技有限公司',
        '金融服务公司',
        '文化传媒公司',
        '建筑工程公司',
        '咨询顾问公司',
        '电商平台公司',
        '物流运输公司'
      ],
      data: [258000, 185000, 142000, 115000, 92000, 75000, 58000]
    }
  }
}

const buildCustomData = (startDate: string, endDate: string): ChartDataSet | null => {
  if (!startDate || !endDate) return null
  const start = dayjs(startDate).startOf('day')
  const end = dayjs(endDate).startOf('day')
  if (!start.isValid() || !end.isValid()) return null
  const daysDiff = Math.max(1, end.diff(start, 'day') + 1)

  const labels = Array.from({ length: daysDiff }, (_, i) => start.add(i, 'day').format('M月D日'))
  const accessData = Array.from({ length: daysDiff }, () => Math.floor(Math.random() * 2000 + 500))
  const rejectData = Array.from({ length: daysDiff }, () => Math.floor(Math.random() * 50 + 5))
  const alertTrendData = Array.from({ length: daysDiff }, () => Math.floor(Math.random() * 60 + 5))

  return {
    accessTrend: { labels, accessData, rejectData },
    alertCategory: {
      labels: ['无效卡', '未授权访问', '门异常开启', '超时未关门', '胁迫开门', '其他'],
      data: [
        Math.floor(Math.random() * 50 + 10),
        Math.floor(Math.random() * 40 + 5),
        Math.floor(Math.random() * 30 + 3),
        Math.floor(Math.random() * 20 + 2),
        Math.floor(Math.random() * 10 + 1),
        Math.floor(Math.random() * 10 + 1)
      ]
    },
    alertTrend: { labels, data: alertTrendData },
    companyAccess: {
      labels: [
        '科技有限公司',
        '金融服务公司',
        '文化传媒公司',
        '建筑工程公司',
        '咨询顾问公司',
        '电商平台公司',
        '物流运输公司'
      ],
      data: Array.from({ length: 7 }, () => Math.floor(Math.random() * 5000 + 1000))
    }
  }
}

const effectiveData = computed<ChartDataSet>(() => {
  if (dateRange.value === 'custom') {
    return customData.value ?? (liveTrendByRange.value.today
      ? { ...builtinData.today, accessTrend: liveTrendByRange.value.today }
      : builtinData.today)
  }
  // 优先使用真实 trend 数据，没有时 fallback 到 mock
  const liveTrend = liveTrendByRange.value[dateRange.value]
  const baseMock = builtinData[dateRange.value]
  if (liveTrend) return { ...baseMock, accessTrend: liveTrend }
  return baseMock
})

const metrics = computed(() => {
  const stats = liveStats.value
  const accessTotal = stats?.todayAccessCount ?? sum(effectiveData.value.accessTrend.accessData)
  // 在岗人员通行人次：后端暂未细分员工/访客/车辆，使用 accessTypeDistribution.employee 优先；
  // 否则按 92% 估算（mock fallback）。
  const staffAccessTotal =
    stats?.accessTypeDistribution?.employee ?? Math.floor(accessTotal * 0.92)
  const alertTotal = stats?.todayAlarmCount ?? 48
  const devicesTotal = stats?.totalDeviceCount ?? 28
  const devicesOnline = stats?.onlineDeviceCount ?? 26
  const devicesOffline = Math.max(0, devicesTotal - devicesOnline)
  return {
    accessTotal,
    staffAccessTotal,
    alertTotal,
    devicesTotal,
    devicesOnline,
    devicesOffline
  }
})

const growth = computed(() => {
  const stats = liveStats.value
  return {
    total: stats?.accessCountGrowth ?? 12.8,
    staff: stats?.visitorCountGrowth ?? 8.5,
    alert: 15.3, // 后端暂无告警增长率字段
    device: stats?.vehicleCountGrowth ?? 6.2
  }
})

// === 拉取真实 statistics + trend（按当前日期范围）===
const rangeToDates = (range: DateRangeKey): { startTime: string; endTime: string } | null => {
  const today = dayjs()
  switch (range) {
    case 'today':
      return {
        startTime: today.startOf('day').format('YYYY-MM-DD'),
        endTime: today.endOf('day').format('YYYY-MM-DD')
      }
    case 'week':
      return {
        startTime: today.subtract(6, 'day').format('YYYY-MM-DD'),
        endTime: today.format('YYYY-MM-DD')
      }
    case 'month':
      return {
        startTime: today.subtract(29, 'day').format('YYYY-MM-DD'),
        endTime: today.format('YYYY-MM-DD')
      }
    case 'year':
      return {
        startTime: today.subtract(11, 'month').startOf('month').format('YYYY-MM-DD'),
        endTime: today.format('YYYY-MM-DD')
      }
    default:
      return null
  }
}

const loadStats = async () => {
  try {
    liveStats.value = await getAccessDashboardStatistics()
  } catch (err) {
    console.warn('[AccessVisualDashboard] statistics 接口失败，回退至 mock 数据', err)
  }
}

const loadTrend = async (range: DateRangeKey) => {
  const dates = rangeToDates(range)
  if (!dates) return
  try {
    const trend = await getAccessTrend(dates)
    if (trend && Array.isArray(trend.labels) && trend.labels.length > 0) {
      liveTrendByRange.value = {
        ...liveTrendByRange.value,
        [range]: {
          labels: trend.labels,
          accessData: (trend.inData ?? trend.accessData ?? []).map(Number),
          rejectData: (trend.rejectData ?? []).map(Number)
        }
      }
    }
  } catch (err) {
    console.warn(`[AccessVisualDashboard] trend(${range}) 接口失败，回退至 mock 数据`, err)
  }
}

onMounted(() => {
  loadStats()
  loadTrend('today')
})

// 切换日期范围时按需拉取（带缓存）
watch(dateRange, (range) => {
  if (range !== 'custom' && !liveTrendByRange.value[range]) {
    loadTrend(range)
  }
})

const accessTrendTitle = computed(() => {
  if (dateRange.value === 'today') return '24小时进入趋势'
  if (dateRange.value === 'week') return '7日进入趋势（本周）'
  if (dateRange.value === 'month') return '30日进入趋势（本月）'
  if (dateRange.value === 'year') return '12个月进入趋势（本年）'
  if (customStartDate.value && customEndDate.value)
    return `进入趋势（${customStartDate.value} 至 ${customEndDate.value}）`
  return '进入趋势（自定义）'
})

const alertTrendTitle = computed(() => {
  if (dateRange.value === 'today') return '告警趋势分析（今日）'
  if (dateRange.value === 'week') return '告警趋势分析（本周）'
  if (dateRange.value === 'month') return '告警趋势分析（本月）'
  if (dateRange.value === 'year') return '告警趋势分析（本年）'
  return '告警趋势分析（自定义）'
})

const companyAccessTitle = computed(() => {
  if (dateRange.value === 'today') return 'XX大厦各公司人员进入统计（今日）'
  if (dateRange.value === 'week') return 'XX大厦各公司人员进入统计（本周）'
  if (dateRange.value === 'month') return 'XX大厦各公司人员进入统计（本月）'
  if (dateRange.value === 'year') return 'XX大厦各公司人员进入统计（本年）'
  return 'XX大厦各公司人员进入统计（自定义）'
})

const accessTrendOptions = computed<EChartsOption>(() => {
  const data = effectiveData.value.accessTrend
  const series: any[] = []
  if (showAccess.value) {
    series.push({
      name: '进入',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2, color: '#3b82f6' },
      itemStyle: { color: '#3b82f6' },
      areaStyle: { color: 'rgba(59, 130, 246, 0.12)' },
      data: data.accessData
    })
  }
  if (showReject.value) {
    series.push({
      name: '拒绝',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2, color: '#ef4444' },
      itemStyle: { color: '#ef4444' },
      areaStyle: { color: 'rgba(239, 68, 68, 0.10)' },
      data: data.rejectData
    })
  }
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0 },
    grid: { left: 12, right: 12, top: 48, bottom: 12, containLabel: true },
    xAxis: { type: 'category', data: data.labels, axisTick: { show: false } },
    yAxis: { type: 'value' },
    series
  }
})

const alertCategoryOptions = computed<EChartsOption>(() => {
  const data = effectiveData.value.alertCategory
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, left: 'center' },
    series: [
      {
        name: '告警分类',
        type: 'pie',
        radius: ['35%', '65%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 600 } },
        labelLine: { show: false },
        data: data.labels.map((label, idx) => ({ name: label, value: data.data[idx] }))
      }
    ]
  }
})

const alertTrendOptions = computed<EChartsOption>(() => {
  const data = effectiveData.value.alertTrend
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 12, right: 12, top: 20, bottom: 12, containLabel: true },
    xAxis: { type: 'category', data: data.labels, axisTick: { show: false } },
    yAxis: { type: 'value' },
    series: [
      {
        name: '告警',
        type: 'line',
        smooth: true,
        symbolSize: 6,
        lineStyle: { width: 2, color: '#f59e0b' },
        itemStyle: { color: '#f59e0b' },
        areaStyle: { color: 'rgba(245, 158, 11, 0.12)' },
        data: data.data
      }
    ]
  }
})

const companyAccessOptions = computed<EChartsOption>(() => {
  const data = effectiveData.value.companyAccess
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 12, right: 12, top: 20, bottom: 12, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.labels,
      axisTick: { show: false },
      axisLabel: { interval: 0, rotate: 18 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '进入人次',
        type: 'bar',
        barWidth: 22,
        itemStyle: { borderRadius: [8, 8, 0, 0], color: '#10b981' },
        data: data.data
      }
    ]
  }
})

const setDateRange = (range: DateRangeKey) => {
  dateRange.value = range
  if (range !== 'custom') {
    customStartDate.value = ''
    customEndDate.value = ''
    customData.value = null
  }
}

const applyCustomDateRange = () => {
  const data = buildCustomData(customStartDate.value, customEndDate.value)
  if (!data) return
  customData.value = data
}

const toggleDataDisplay = (key: 'access' | 'reject') => {
  if (key === 'access') showAccess.value = !showAccess.value
  if (key === 'reject') showReject.value = !showReject.value
}
</script>

<template>
  <div
    class="ac-board-content"
    :style="{
      paddingTop: 'max(0px, calc(var(--page-top-gap,70px) - (var(--app-content-padding) + 10px)))'
    }"
  >
    <div class="content">
      <div class="metrics-grid">
        <div class="metric-card">
          <div class="metric-top">
            <div class="metric-icon-box purple">
              <Icon icon="fa6-solid:clock" />
            </div>
            <div class="metric-growth green">
              <Icon icon="fa6-solid:arrow-up" />
              <span>{{ growth.total.toFixed(1) }}%</span>
            </div>
          </div>
          <div class="metric-body">
            <p class="metric-label">今日总通行人次</p>
            <p class="metric-value">{{ formatNumber(metrics.accessTotal) }}</p>
            <p class="metric-unit">人次</p>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <div class="metric-icon-box blue">
              <Icon icon="fa6-solid:users" />
            </div>
            <div class="metric-growth green">
              <Icon icon="fa6-solid:arrow-up" />
              <span>{{ growth.staff.toFixed(1) }}%</span>
            </div>
          </div>
          <div class="metric-body">
            <p class="metric-label">今日在岗人员通行人次</p>
            <p class="metric-value">{{ formatNumber(metrics.staffAccessTotal) }}</p>
            <p class="metric-unit">人次</p>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <div class="metric-icon-box red">
              <Icon icon="fa6-solid:triangle-exclamation" />
            </div>
            <div class="metric-growth red">
              <Icon icon="fa6-solid:arrow-up" />
              <span>{{ growth.alert.toFixed(1) }}%</span>
            </div>
          </div>
          <div class="metric-body">
            <p class="metric-label">今日异常通行次数</p>
            <p class="metric-value">{{ formatNumber(metrics.alertTotal) }}</p>
            <p class="metric-unit">告警: 8 | 拒绝: 40</p>
          </div>
        </div>

        <div class="metric-card">
          <div class="metric-top">
            <div class="metric-icon-box green">
              <Icon icon="fa6-solid:door-open" />
            </div>
            <div class="metric-growth green">
              <Icon icon="fa6-solid:arrow-up" />
              <span>{{ growth.device.toFixed(1) }}%</span>
            </div>
          </div>
          <div class="metric-body">
            <p class="metric-label">门禁设备</p>
            <p class="metric-value">{{ metrics.devicesTotal }}</p>
            <p class="metric-unit"
              >在线: {{ metrics.devicesOnline }} | 离线: {{ metrics.devicesOffline }}</p
            >
          </div>
        </div>
      </div>

      <div class="section">
        <div class="date-filter">
          <div class="date-filter-tabs">
            <div
              class="date-filter-tab"
              :class="{ active: dateRange === 'today' }"
              @click="setDateRange('today')"
            >
              今日
            </div>
            <div
              class="date-filter-tab"
              :class="{ active: dateRange === 'week' }"
              @click="setDateRange('week')"
            >
              本周
            </div>
            <div
              class="date-filter-tab"
              :class="{ active: dateRange === 'month' }"
              @click="setDateRange('month')"
            >
              本月
            </div>
            <div
              class="date-filter-tab"
              :class="{ active: dateRange === 'year' }"
              @click="setDateRange('year')"
            >
              本年
            </div>
            <div
              class="date-filter-tab"
              :class="{ active: dateRange === 'custom' }"
              @click="setDateRange('custom')"
            >
              自定义
            </div>
          </div>

          <div class="date-range-picker" :class="{ hidden: dateRange !== 'custom' }">
            <input v-model="customStartDate" type="date" class="date-input" />
            <span class="date-separator">至</span>
            <input v-model="customEndDate" type="date" class="date-input" />
            <button class="apply-btn" @click="applyCustomDateRange">应用</button>
          </div>
        </div>

        <div class="chart-container">
          <div class="chart-header">
            <h3 class="chart-title">{{ accessTrendTitle }}</h3>
            <div class="data-toggle-group">
              <div
                class="data-toggle-btn access"
                :class="{ active: showAccess }"
                @click="toggleDataDisplay('access')"
              >
                <span class="dot-blue"></span>
                <span>进入</span>
              </div>
              <div
                class="data-toggle-btn reject"
                :class="{ active: showReject }"
                @click="toggleDataDisplay('reject')"
              >
                <span class="dot-red"></span>
                <span>拒绝</span>
              </div>
            </div>
          </div>
          <div class="chart-fixed-height chart-h-320">
            <Echart :options="accessTrendOptions" height="320px" />
          </div>
        </div>
      </div>

      <div class="charts-2">
        <div class="chart-container">
          <h3 class="chart-title mb-16">告警事件分类统计</h3>
          <div class="chart-fixed-height chart-h-300">
            <Echart :options="alertCategoryOptions" height="300px" />
          </div>
        </div>
        <div class="chart-container">
          <h3 class="chart-title mb-16">{{ alertTrendTitle }}</h3>
          <div class="chart-fixed-height chart-h-300">
            <Echart :options="alertTrendOptions" height="300px" />
          </div>
        </div>
      </div>

      <div class="chart-container">
        <h3 class="chart-title mb-16">{{ companyAccessTitle }}</h3>
        <div class="chart-fixed-height chart-h-350">
          <Echart :options="companyAccessOptions" height="350px" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

.ac-board-content {
  font-family:
    'Noto Sans SC',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    sans-serif;
  background-color: var(--el-bg-color-page, #f8fafc);
}

.ac-board-content .content {
  padding: 24px;
  overflow: visible;
  flex: initial;
}

.ac-board-content .metric-card,
.ac-board-content .date-filter,
.ac-board-content .chart-container {
  background: var(--el-bg-color-overlay, #fff);
  border-color: var(--el-border-color-lighter, #f1f5f9);
}

.ac-board-content .metric-label,
.ac-board-content .metric-unit,
.ac-board-content .date-separator {
  color: var(--el-text-color-secondary);
}

.ac-board-content .metric-value,
.ac-board-content .chart-title {
  color: var(--el-text-color-primary);
}

.ac-board-content .date-filter-tab {
  color: var(--el-text-color-secondary);
  background-color: var(--el-fill-color-light, #f1f5f9);
}

.ac-board-content .date-filter-tab:hover {
  color: var(--el-text-color-regular);
  background-color: var(--el-fill-color, #e2e8f0);
}

.ac-board-content .date-filter-tab.active {
  color: #fff;
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.ac-board-content .date-input {
  color: var(--el-text-color-primary);
  background-color: var(--el-bg-color-overlay, #fff);
  border-color: var(--el-border-color-lighter, #e2e8f0);
}

::global(.dark) .ac-board-content .date-input,
.dark .ac-board-content .date-input {
  box-shadow: none;
}

::global(.dark) .ac-board-content .metric-card,
::global(.dark) .ac-board-content .date-filter,
::global(.dark) .ac-board-content .chart-container,
.dark .ac-board-content .metric-card,
.dark .ac-board-content .date-filter,
.dark .ac-board-content .chart-container {
  box-shadow: 0 4px 10px rgb(0 0 0 / 35%);
}

.ac-board-page {
  position: relative;
  height: calc(100vh - var(--page-top-gap, 70px));
  overflow: hidden;
  font-family:
    'Noto Sans SC',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    sans-serif;
  background-color: #f8fafc;
}

.mobile-overlay {
  position: absolute;
  z-index: 40;
  background: rgb(0 0 0 / 50%);
  inset: 0;
}

.hidden {
  display: none !important;
}

.sidebar {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 50;
  display: flex;
  width: 260px;
  height: 100%;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  box-shadow: 2px 0 10px rgb(0 0 0 / 10%);
  transition: all 0.3s ease;
  flex-direction: column;
}

.sidebar.collapsed {
  width: 70px;
}

.sidebar-head {
  padding: 24px;
  border-bottom: 1px solid rgb(148 163 184 / 35%);
}

.logo {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-icon {
  font-size: 28px;
  color: #3b82f6;
}

.logo-text h1 {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
  color: #fff;
}

.logo-text p {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

.sidebar-nav {
  padding: 24px 0;
  flex: 1;
  overflow: auto;
}

.menu-item {
  position: relative;
  display: flex;
  padding: 14px 20px;
  color: #cbd5e1;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.2s ease;
  user-select: none;
  align-items: center;
  gap: 12px;
}

.menu-item:hover {
  color: #fff;
  background-color: #334155;
  border-left-color: #3b82f6;
}

.menu-item.active {
  color: #fff;
  background-color: #2563eb;
  border-left-color: #fff;
}

.menu-icon {
  width: 20px;
  font-size: 16px;
}

.menu-text {
  white-space: nowrap;
}

.menu-item.has-children .menu-arrow {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.3s ease;
}

.menu-item.has-children.expanded .menu-arrow {
  transform: rotate(180deg);
}

.submenu {
  display: none;
  background-color: #334155;
}

.submenu.show {
  display: block;
}

.submenu-item {
  padding-left: 48px;
}

.badge {
  position: absolute;
  top: 8px;
  right: 20px;
  padding: 2px 8px;
  font-size: 11px;
  color: #fff;
  background-color: #ef4444;
  border-radius: 10px;
}

.sidebar-footer {
  display: flex;
  padding: 24px;
  border-top: 1px solid rgb(148 163 184 / 35%);
  align-items: center;
  gap: 16px;
}

.admin-avatar {
  display: flex;
  width: 48px;
  height: 48px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  background: #3b82f6;
  border-radius: 999px;
  align-items: center;
  justify-content: center;
}

.admin-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.admin-email {
  margin-top: 2px;
  font-size: 12px;
  color: #94a3b8;
}

.sidebar-toggle {
  position: absolute;
  bottom: 20px;
  left: 275px;
  z-index: 51;
  display: flex;
  width: 36px;
  height: 36px;
  color: #fff;
  cursor: pointer;
  background: #2563eb;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgb(37 99 235 / 30%);
  transition: all 0.3s ease;
  align-items: center;
  justify-content: center;
}

.sidebar-toggle:hover {
  background: #1d4ed8;
  transform: scale(1.1);
}

.sidebar-toggle.collapsed {
  left: 85px;
}

.main-content {
  display: flex;
  height: 100%;
  margin-left: 260px;
  overflow: hidden;
  transition: all 0.3s ease;
  flex-direction: column;
}

.main-content.expanded {
  margin-left: 70px;
}

.top-header {
  display: flex;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  align-items: center;
  justify-content: space-between;
  flex: 0 0 auto;
}

.top-left,
.top-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mobile-btn {
  display: none;
  padding: 8px;
  color: #4b5563;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 10px;
}

.mobile-btn:hover {
  background: #f3f4f6;
}

.search-box {
  display: flex;
  padding: 8px 16px;
  background: #f3f4f6;
  border-radius: 10px;
  align-items: center;
  gap: 8px;
}

.search-icon {
  color: #9ca3af;
}

.search-input {
  width: 260px;
  font-size: 13px;
  background: transparent;
  border: none;
  outline: none;
}

.icon-btn {
  position: relative;
  padding: 8px;
  color: #4b5563;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 10px;
}

.icon-btn:hover {
  background: #f3f4f6;
}

.notify-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 50%;
}

.date-time {
  text-align: right;
}

.date-text {
  font-size: 13px;
  color: #4b5563;
}

.time-text {
  font-size: 12px;
  color: #9ca3af;
}

.page-title {
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  flex: 0 0 auto;
}

.breadcrumb {
  font-size: 13px;
  color: #6b7280;
}

.breadcrumb .sep {
  margin: 0 8px;
  color: #9ca3af;
}

.breadcrumb .current {
  font-weight: 600;
  color: #111827;
}

.content {
  padding: 24px;
  overflow: auto;
  flex: 1;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.metric-card {
  padding: 24px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  box-shadow: 0 4px 6px rgb(0 0 0 / 5%);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgb(0 0 0 / 10%);
}

.metric-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.metric-icon-box {
  display: inline-flex;
  padding: 12px;
  font-size: 18px;
  border-radius: 12px;
  align-items: center;
  justify-content: center;
}

.metric-icon-box.purple {
  color: #9333ea;
  background: #f3e8ff;
}

.metric-icon-box.blue {
  color: #2563eb;
  background: #dbeafe;
}

.metric-icon-box.red {
  color: #dc2626;
  background: #fee2e2;
}

.metric-icon-box.green {
  color: #16a34a;
  background: #dcfce7;
}

.metric-growth {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
}

.metric-growth.green {
  color: #16a34a;
}

.metric-growth.red {
  color: #dc2626;
}

.metric-body {
  margin-top: 16px;
}

.metric-label {
  font-size: 13px;
  color: #6b7280;
}

.metric-value {
  margin-top: 6px;
  font-size: 30px;
  font-weight: 800;
  color: #1f2937;
}

.metric-unit {
  margin-top: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.section {
  margin-bottom: 24px;
}

.date-filter {
  padding: 20px 24px;
  margin-bottom: 24px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  box-shadow: 0 4px 6px rgb(0 0 0 / 5%);
}

.date-filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.date-filter-tab {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  background-color: #f1f5f9;
  border: 2px solid transparent;
  border-radius: 8px;
  transition: all 0.2s ease;
  user-select: none;
}

.date-filter-tab:hover {
  color: #334155;
  background-color: #e2e8f0;
}

.date-filter-tab.active {
  color: #fff;
  background-color: #2563eb;
  border-color: #1d4ed8;
}

.date-range-picker {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 16px;
}

.date-input {
  min-width: 140px;
  padding: 8px 12px;
  font-size: 14px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.date-input:focus {
  border-color: #2563eb;
  outline: none;
  box-shadow: 0 0 0 3px rgb(37 99 235 / 10%);
}

.date-separator {
  font-weight: 600;
  color: #94a3b8;
}

.apply-btn {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  cursor: pointer;
  background-color: #2563eb;
  border: none;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.apply-btn:hover {
  background-color: #1d4ed8;
  transform: translateY(-1px);
}

.chart-container {
  padding: 24px;
  margin-bottom: 24px;
  background: #fff;
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  box-shadow: 0 4px 6px rgb(0 0 0 / 5%);
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.mb-16 {
  margin-bottom: 16px;
}

.data-toggle-group {
  display: flex;
  gap: 8px;
}

.data-toggle-btn {
  display: inline-flex;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 6px;
  transition: all 0.2s ease;
  user-select: none;
  align-items: center;
  gap: 6px;
}

.data-toggle-btn.access {
  color: #3b82f6;
  background-color: rgb(59 130 246 / 10%);
}

.data-toggle-btn.access.active {
  color: #fff;
  background-color: #3b82f6;
}

.data-toggle-btn.reject {
  color: #ef4444;
  background-color: rgb(239 68 68 / 10%);
}

.data-toggle-btn.reject.active {
  color: #fff;
  background-color: #ef4444;
}

.dot-blue,
.dot-red {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 999px;
}

.dot-blue {
  background: #3b82f6;
}

.dot-red {
  background: #ef4444;
}

.chart-fixed-height {
  width: 100%;
}

.chart-h-320 {
  height: 320px;
}

.chart-h-300 {
  height: 300px;
}

.chart-h-350 {
  height: 350px;
}

.charts-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

@media (width <= 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (width <= 1024px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.show {
    transform: translateX(0);
  }

  .main-content {
    margin-left: 0;
  }

  .main-content.expanded {
    margin-left: 0;
  }

  .sidebar-toggle {
    left: 20px;
  }

  .sidebar-toggle.collapsed {
    left: 20px;
  }

  .mobile-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .date-time {
    display: none;
  }
}

@media (width <= 900px) {
  .charts-2 {
    grid-template-columns: 1fr;
  }

  .search-input {
    width: 160px;
  }
}
</style>
