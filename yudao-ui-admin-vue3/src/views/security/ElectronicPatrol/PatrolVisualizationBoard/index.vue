<template>
  <div class="patrol-board-page">
    <section ref="hostRef" class="fit-host">
      <div class="fit-canvas" :style="canvasStyle">
        <div class="board-surface">
          <header class="board-header glass-panel">
            <div class="header-left">
              <div class="header-icon">
                <svg class="header-icon-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
                  />
                </svg>
              </div>
              <div>
                <h1 class="header-title">离线巡更可视化看板</h1>
              </div>
            </div>
            <div class="header-right">
              <div class="time-range">
                <button class="range-btn" :class="{ active: rangeMode === 'today' }" @click="setPresetRange('today')">
                  今日
                </button>
                <button class="range-btn" :class="{ active: rangeMode === 'week' }" @click="setPresetRange('week')">
                  近一周
                </button>
                <button class="range-btn" :class="{ active: rangeMode === 'month' }" @click="setPresetRange('month')">
                  近一月
                </button>
                <div class="custom-date" :class="{ visible: rangeMode === 'custom' }">
                  <span class="custom-label">从</span>
                  <ElDatePicker
                    v-model="customStart"
                    type="date"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    :clearable="false"
                    class="custom-picker"
                    @change="handleCustomDateChange"
                  />
                  <span class="custom-sep">—</span>
                  <ElDatePicker
                    v-model="customEnd"
                    type="date"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    :clearable="false"
                    class="custom-picker"
                    @change="handleCustomDateChange"
                  />
                </div>
              </div>
            </div>
          </header>
 
          <div class="grid-layout">
            <div class="col-span-3">
              <div class="glass-panel metric-card border-blue">
                <div class="metric-head">
                  <div>
                    <p class="metric-label">巡更任务 <span class="time-label">{{ currentRangeLabel }}</span></p>
                    <h3 class="metric-total">{{ dashboard.totalTasks }}</h3>
                  </div>
                  <span class="status-indicator status-normal"></span>
                </div>
                <div class="stat-grid">
                  <div class="stat-item">
                    <div class="stat-value text-green">{{ dashboard.completedTasks }}</div>
                    <div class="stat-label">已巡任务</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value text-yellow">{{ dashboard.pendingTasks }}</div>
                    <div class="stat-label">未巡任务</div>
                  </div>
                </div>
              </div>
            </div>
 
            <div class="col-span-3">
              <div class="glass-panel metric-card border-purple">
                <div class="metric-head">
                  <div>
                    <p class="metric-label">涉及巡更点位次数 <span class="time-label">{{ currentRangeLabel }}</span></p>
                    <h3 class="metric-total">{{ dashboard.totalPoints }}</h3>
                  </div>
                </div>
                <div class="stat-grid">
                  <div class="stat-item">
                    <div class="stat-value text-green">{{ dashboard.checkedPoints }}</div>
                    <div class="stat-label">已巡点位次数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-value text-red">{{ dashboard.missedPoints }}</div>
                    <div class="stat-label">漏巡点位次数</div>
                  </div>
                </div>
              </div>
            </div>
 
            <div class="col-span-3">
              <div class="glass-panel metric-card border-pink">
                <div class="metric-head">
                  <div>
                    <p class="metric-label">巡更路线 <span class="time-label">{{ currentRangeLabel }}</span></p>
                    <h3 class="metric-total">{{ dashboard.totalRoutes }}</h3>
                  </div>
                </div>
                <div class="metric-kv">
                  <div class="kv-row">
                    <span class="kv-k">平均点位/路线</span>
                    <span class="kv-v">{{ dashboard.avgPointsPerRoute }}</span>
                  </div>
                  <div class="kv-row">
                    <span class="kv-k">点位巡更完成率</span>
                    <span class="kv-v text-green">{{ dashboard.completionRate }}%</span>
                  </div>
                </div>
              </div>
            </div>
 
            <div class="col-span-3">
              <div class="glass-panel metric-card border-orange">
                <div class="metric-head">
                  <div>
                    <p class="metric-label">巡更人员 <span class="time-label">{{ currentRangeLabel }}</span></p>
                    <h3 class="metric-total">{{ dashboard.totalStaff }}</h3>
                  </div>
                </div>
                <div class="avatar-row">
                  <div
                    v-for="staff in dashboard.staff"
                    :key="staff.name"
                    class="avatar"
                    :style="{ background: staff.avatarBg }"
                  >
                    {{ staff.avatar }}
                  </div>
                </div>
              </div>
            </div>
 
            <div class="col-span-5">
              <div class="glass-panel panel">
                <h3 class="panel-title">
                  <svg class="panel-icon text-yellow" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z"
                    />
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z"
                    />
                  </svg>
                  任务完成率分布 <span class="time-label">{{ currentRangeLabel }}</span>
                </h3>
                <div ref="completionChartRef" class="chart-container completion-chart"></div>
                <div class="legend-row">
                  <div class="legend-item">
                    <span class="legend-dot dot-green"></span>
                    <span class="legend-text">100%完成 ({{ dashboard.pieData.full }}个)</span>
                  </div>
                  <div class="legend-item">
                    <span class="legend-dot dot-yellow"></span>
                    <span class="legend-text">1-99%完成 ({{ dashboard.pieData.partial }}个)</span>
                  </div>
                  <div class="legend-item">
                    <span class="legend-dot dot-red"></span>
                    <span class="legend-text">0%完成 ({{ dashboard.pieData.zero }}个)</span>
                  </div>
                </div>
                <div class="legend-desc">
                  <p class="desc-title">📋 完成率说明：</p>
                  <p class="desc-item">• <span class="text-green">100%完成</span>：该任务所有点位均已巡更，无漏巡</p>
                  <p class="desc-item">• <span class="text-yellow">1-99%完成</span>：该任务部分点位漏巡，完成率介于1%-99%之间</p>
                  <p class="desc-item">• <span class="text-red">0%完成</span>：该任务所有点位均未巡更，全部漏巡</p>
                </div>
              </div>
            </div>
 
            <div class="col-span-7">
              <div class="glass-panel panel">
                <h3 class="panel-title">
                  <svg class="panel-icon text-green" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                  </svg>
                  人员绩效排行榜 <span class="time-label">{{ currentRangeLabel }}</span>
                </h3>
                <div class="staff-rank">
                  <div v-for="staff in dashboard.staff" :key="staff.name" class="staff-rank-item">
                    <div class="staff-avatar">{{ staff.avatar }}</div>
                    <div class="staff-info">
                      <div class="staff-name">{{ staff.name }}</div>
                      <div class="staff-stats">
                        <span>✅ 已巡点位数: {{ staff.done }}</span>
                        <span>❌ 漏巡点位数: {{ staff.miss }}</span>
                        <span>📊 漏巡率 {{ staff.missRate }}%</span>
                      </div>
                    </div>
                    <div class="staff-score">{{ staff.score }}</div>
                  </div>
                </div>
                <div class="score-card">
                  <div class="score-head">
                    <span class="score-title">综合评分 (100分制)</span>
                    <span class="score-rule">(1 - 漏巡/巡检总数) × 100</span>
                  </div>
                  <div class="score-value-wrap">
                    <span class="score-value">{{ dashboard.totalScore }}</span>
                    <span class="score-unit">分</span>
                  </div>
                </div>
              </div>
            </div>
 
            <div class="col-span-5">
              <div class="glass-panel panel">
                <h3 class="panel-title mb-lg">
                  <svg class="panel-icon text-red" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                    />
                  </svg>
                  点位漏巡排行榜 <span class="time-label">{{ currentRangeLabel }}</span>
                </h3>
                <div class="missed-rank">
                  <div v-for="(item, idx) in dashboard.rankPoints" :key="item.name" class="rank-item">
                    <span class="rank-number">{{ idx + 1 }}</span>
                    <span class="rank-name">{{ item.name }}</span>
                    <div class="rank-bar">
                      <div class="rank-bar-fill" :style="{ width: `${item.widthPercent}%` }"></div>
                    </div>
                    <span class="rank-count">{{ item.missCount }}次</span>
                  </div>
                </div>
                <div class="missed-summary">
                  基于{{ dashboard.totalTasks }}个巡更任务，总计{{ dashboard.totalPoints }}个点位次数，其中{{ dashboard.missedPoints }}次漏巡
                </div>
              </div>
            </div>
 
            <div class="col-span-7">
              <div class="glass-panel panel">
                <h3 class="panel-title">
                  <svg class="panel-icon text-purple" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  巡更打卡时段热力
                </h3>
                <div ref="heatmapChartRef" class="chart-container heatmap-chart"></div>
                <div class="heat-legend">
                  <span class="heat-item"><span class="heat-box heat-low"></span>低频 (0~1次)</span>
                  <span class="heat-item"><span class="heat-box heat-mid"></span>中频 (2次)</span>
                  <span class="heat-item"><span class="heat-box heat-high"></span>高频 (3次+)</span>
                </div>
                <div class="function-hint">
                  <div class="hint-row">
                    <svg
                      class="hint-icon"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                    <div>
                      <p class="hint-title">📌 辅助巡更计划优化</p>
                      <p class="hint-desc">
                        展示不同时段/周几的打卡密集程度。颜色越深(紫)表示打卡次数越多，帮助识别高峰与低峰时段。
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
 
<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useElementSize } from '@vueuse/core'
import echarts from '@/plugins/echarts'
import type { EChartsType } from 'echarts'
 
type RangeMode = 'today' | 'week' | 'month' | 'custom'
 
type StaffRank = {
  name: string
  done: number
  miss: number
  score: number
  missRate: number
  avatar: string
  avatarBg: string
}
 
type RankPoint = {
  name: string
  missCount: number
  widthPercent: number
}
 
type DashboardData = {
  totalTasks: number
  completedTasks: number
  pendingTasks: number
  totalPoints: number
  checkedPoints: number
  missedPoints: number
  totalRoutes: number
  avgPointsPerRoute: string
  completionRate: string
  totalStaff: number
  staff: StaffRank[]
  rankPoints: RankPoint[]
  pieData: { full: number; partial: number; zero: number }
  totalScore: number
}
 
const DESIGN_WIDTH = 1920
const DESIGN_HEIGHT = 1080
 
const hostRef = ref<HTMLElement>()
const { width: hostWidth, height: hostHeight } = useElementSize(hostRef)
 
const scale = computed(() => {
  const w = hostWidth.value || 0
  const h = hostHeight.value || 0
  if (w <= 0 || h <= 0) return 1
  // 采用 cover 策略：尽量减少左右空白；底部可能被“视觉裁切”时，允许在本组件滚动查看
  return Math.max(w / DESIGN_WIDTH, h / DESIGN_HEIGHT)
})
 
const canvasStyle = computed(() => {
  const w = hostWidth.value || DESIGN_WIDTH
  const h = hostHeight.value || DESIGN_HEIGHT
  const s = scale.value
  // 左上对齐，避免顶部留白/裁切在不同窗口下抖动
  const x = 0
  const y = 0
  return {
    width: `${DESIGN_WIDTH}px`,
    height: `${DESIGN_HEIGHT}px`,
    transform: `translate(${x}px, ${y}px) scale(${s})`,
    transformOrigin: 'top left'
  }
})
 
const rangeMode = ref<RangeMode>('today')
const currentRangeLabel = computed(() => {
  if (rangeMode.value === 'today') return '今日'
  if (rangeMode.value === 'week') return '近一周'
  if (rangeMode.value === 'month') return '近一月'
  return '自定义'
})
 
const customStart = ref<string>('')
const customEnd = ref<string>('')
 
const currentStart = ref<Date>(new Date())
const currentEnd = ref<Date>(new Date())
 
const completionChartRef = ref<HTMLDivElement>()
const heatmapChartRef = ref<HTMLDivElement>()
let completionChart: EChartsType | null = null
let heatmapChart: EChartsType | null = null
let themeObserver: MutationObserver | null = null
 
const dashboard = ref<DashboardData>({
  totalTasks: 0,
  completedTasks: 0,
  pendingTasks: 0,
  totalPoints: 0,
  checkedPoints: 0,
  missedPoints: 0,
  totalRoutes: 0,
  avgPointsPerRoute: '0.0',
  completionRate: '0.0',
  totalStaff: 0,
  staff: [],
  rankPoints: [],
  pieData: { full: 0, partial: 0, zero: 0 },
  totalScore: 0
})
 
const isDark = () => document.documentElement.classList.contains('dark')
 
const formatDateYmd = (date: Date) => {
  const y = date.getFullYear()
  const m = `${date.getMonth() + 1}`.padStart(2, '0')
  const d = `${date.getDate()}`.padStart(2, '0')
  return `${y}-${m}-${d}`
}
 
const normalizeToDayStart = (d: Date) => {
  const nd = new Date(d)
  nd.setHours(0, 0, 0, 0)
  return nd
}
 
const setPresetRange = (mode: Exclude<RangeMode, 'custom'>) => {
  rangeMode.value = mode
  const end = normalizeToDayStart(new Date())
  const start = new Date(end)
  if (mode === 'week') start.setDate(end.getDate() - 6)
  if (mode === 'month') start.setDate(end.getDate() - 29)
  currentStart.value = start
  currentEnd.value = end
  refreshDashboard()
}
 
const setCustomMode = () => {
  rangeMode.value = 'custom'
  if (!customStart.value || !customEnd.value) {
    const end = normalizeToDayStart(new Date())
    const start = normalizeToDayStart(new Date())
    customStart.value = formatDateYmd(start)
    customEnd.value = formatDateYmd(end)
  }
  handleCustomDateChange()
}
 
const handleCustomDateChange = () => {
  if (rangeMode.value !== 'custom') return
  if (!customStart.value || !customEnd.value) return
  const start = normalizeToDayStart(new Date(`${customStart.value}T00:00:00`))
  const end = normalizeToDayStart(new Date(`${customEnd.value}T00:00:00`))
  if (start.getTime() > end.getTime()) return
  currentStart.value = start
  currentEnd.value = end
  refreshDashboard()
}
 
const generateDataByRange = (start: Date, end: Date): DashboardData => {
  const days = Math.ceil((end.getTime() - start.getTime()) / (1000 * 3600 * 24)) + 1
  let factor = days / 3.5
  if (factor < 0.6) factor = 0.6
  if (factor > 3.2) factor = 3.2
 
  const baseTasks = 10
  const baseCompleted = 3
  const baseTotalPoints = 56
  const baseChecked = 32
  const baseRoutes = 5
 
  const totalTasks = Math.round(baseTasks * factor * 0.7)
  const completedTasks = Math.round(baseCompleted * Math.sqrt(factor) * 0.9)
  const pendingTasks = totalTasks - completedTasks
 
  const totalPoints = Math.round(baseTotalPoints * factor * 0.8)
  const checkedPoints = Math.round(baseChecked * Math.pow(factor, 0.7) * 0.9)
  const missedPoints = totalPoints - checkedPoints
 
  const totalRoutes = Math.round(baseRoutes * (factor > 1 ? Math.min(1.6, factor) : factor))
  const avgPointsPerRoute = (totalPoints / totalRoutes).toFixed(1)
  const completionRate = ((checkedPoints / Math.max(1, totalPoints)) * 100).toFixed(1)
 
  const rawStaff = [
    { name: '王小军', done: Math.round(9 * factor * 0.5), miss: Math.round(1 * factor), avatar: '王', bg: 'linear-gradient(145deg, #2563eb, #7c3aed)' },
    { name: '李芳', done: Math.round(7 * factor * 0.5), miss: Math.round(2 * factor), avatar: '李', bg: 'linear-gradient(145deg, #8b5cf6, #ec4899)' },
    { name: '张强', done: Math.round(6 * factor * 0.5), miss: Math.round(3 * factor), avatar: '张', bg: 'linear-gradient(145deg, #10b981, #22c55e)' },
    { name: '赵丽', done: Math.round(4 * factor * 0.5), miss: Math.round(4 * factor), avatar: '赵', bg: 'linear-gradient(145deg, #f59e0b, #ef4444)' }
  ]
 
  const staff: StaffRank[] = rawStaff.map((s) => {
    const total = s.done + s.miss
    const score = total > 0 ? Math.round((1 - s.miss / total) * 100) : 0
    const missRate = total > 0 ? Math.round((s.miss / total) * 100) : 0
    return { name: s.name, done: s.done, miss: s.miss, score, missRate, avatar: s.avatar, avatarBg: s.bg }
  })
 
  const rankPointsBase = [
    { name: 'C栋主入口', missCount: Math.round(4 * factor) },
    { name: 'A栋3楼', missCount: Math.round(3 * factor) },
    { name: '园区后门', missCount: Math.round(3 * factor) },
    { name: 'A栋4楼', missCount: Math.round(2 * factor) },
    { name: 'B栋天面', missCount: Math.round(2 * factor) }
  ]
  const maxMiss = Math.max(1, rankPointsBase[0]?.missCount || 1)
  const rankPoints: RankPoint[] = rankPointsBase.map((p) => ({
    name: p.name,
    missCount: p.missCount,
    widthPercent: Math.min(100, (p.missCount / maxMiss) * 100)
  }))
 
  const full = Math.max(0, Math.round(2 * factor * 0.6))
  const partial = Math.max(0, Math.round(1.5 * factor * 0.6))
  const zero = Math.max(0, totalTasks - full - partial)
 
  const totalDoneSum = staff.reduce((sum, s) => sum + s.done, 0)
  const totalMissSum = staff.reduce((sum, s) => sum + s.miss, 0)
  const totalAll = totalDoneSum + totalMissSum
  const totalScore = totalAll > 0 ? Math.round((1 - totalMissSum / totalAll) * 100) : 0
 
  return {
    totalTasks,
    completedTasks,
    pendingTasks,
    totalPoints,
    checkedPoints,
    missedPoints,
    totalRoutes,
    avgPointsPerRoute,
    completionRate,
    totalStaff: staff.length,
    staff,
    rankPoints,
    pieData: { full, partial, zero },
    totalScore
  }
}
 
const buildHeatData = () => {
  const data: Array<[number, number, number]> = []
  for (let i = 0; i < 7; i++) {
    for (let j = 0; j < 12; j++) {
      let baseVal = 0
      if (i === 4 && j === 1) baseVal = 1
      else if (i === 0 && j === 4) baseVal = 2
      else if (i === 4 && j === 6) baseVal = 2
      else if (Math.random() < 0.15) baseVal = 1
      if (baseVal > 4) baseVal = 4
      data.push([j, i, baseVal])
    }
  }
  return data
}
 
const getChartTextColor = () => (isDark() ? '#e2e8f0' : '#303133')
const getAxisLabelColor = () => (isDark() ? '#94a3b8' : '#606266')
const getGridSplitAreaColor = () => (isDark() ? ['rgba(255,255,255,0.02)', 'rgba(255,255,255,0.00)'] : ['rgba(0,0,0,0.02)', 'rgba(0,0,0,0.00)'])
 
const initCharts = () => {
  if (completionChartRef.value && !completionChart) {
    completionChart = echarts.init(completionChartRef.value)
  }
  if (heatmapChartRef.value && !heatmapChart) {
    heatmapChart = echarts.init(heatmapChartRef.value)
  }
  renderCharts()
}
 
const renderCharts = () => {
  if (!completionChart || !heatmapChart) return
  const textColor = getChartTextColor()
  const axisLabelColor = getAxisLabelColor()
  const splitAreaColors = getGridSplitAreaColor()
 
  completionChart.setOption({
    textStyle: { color: textColor },
    tooltip: { trigger: 'item', formatter: '{b}: {c}个 ({d}%)' },
    legend: { show: false },
    series: [
      {
        type: 'pie',
        radius: ['55%', '75%'],
        label: { show: true, position: 'outside', color: textColor, fontSize: 11 },
        data: [
          { value: dashboard.value.pieData.full, name: '100%完成', itemStyle: { color: '#10b981' } },
          { value: dashboard.value.pieData.partial, name: '1-99%完成', itemStyle: { color: '#f59e0b' } },
          { value: dashboard.value.pieData.zero, name: '0%完成', itemStyle: { color: '#ef4444' } }
        ]
      }
    ]
  })
 
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const hours = ['00-02', '02-04', '04-06', '06-08', '08-10', '10-12', '12-14', '14-16', '16-18', '18-20', '20-22', '22-24']
 
  heatmapChart.setOption({
    textStyle: { color: textColor },
    tooltip: {
      position: 'top',
      formatter: (params: any) => `${days[params.value[1]]} ${hours[params.value[0]]}<br/>打卡次数: ${params.value[2]}`
    },
    grid: { left: '8%', right: '5%', top: '8%', bottom: '10%' },
    xAxis: {
      type: 'category',
      data: hours,
      splitArea: { show: true, areaStyle: { color: splitAreaColors } },
      axisLabel: { rotate: 30, fontSize: 10, color: axisLabelColor }
    },
    yAxis: {
      type: 'category',
      data: days,
      splitArea: { show: true, areaStyle: { color: splitAreaColors } },
      axisLabel: { color: axisLabelColor }
    },
    visualMap: {
      min: 0,
      max: 4,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '0%',
      textStyle: { color: axisLabelColor },
      inRange: { color: ['#1e293b', '#3b82f6', '#8b5cf6'] }
    },
    series: [{ type: 'heatmap', data: buildHeatData() }]
  })
}
 
const refreshDashboard = () => {
  dashboard.value = generateDataByRange(currentStart.value, currentEnd.value)
  nextTick(() => {
    renderCharts()
  })
}
 
const observeThemeChange = () => {
  themeObserver?.disconnect()
  themeObserver = new MutationObserver(() => {
    renderCharts()
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
}
 
watch(
  () => [hostWidth.value, hostHeight.value],
  () => {
    completionChart?.resize()
    heatmapChart?.resize()
  }
)
 
onMounted(async () => {
  const today = normalizeToDayStart(new Date())
  currentStart.value = today
  currentEnd.value = today
  customStart.value = formatDateYmd(today)
  customEnd.value = formatDateYmd(today)
  refreshDashboard()
 
  await nextTick()
  initCharts()
  observeThemeChange()
})
 
onBeforeUnmount(() => {
  themeObserver?.disconnect()
  themeObserver = null
  completionChart?.dispose()
  heatmapChart?.dispose()
  completionChart = null
  heatmapChart = null
})
</script>
 
<style scoped lang="scss">
.patrol-board-page {
  height: 100%;
  width: 100%;
  /* 让布局层的 ElScrollbar 统一负责滚动，避免出现双滚动条体验差 */
  overflow: hidden;
}
 
.fit-host {
  height: calc(var(--viewport-height, 100vh) - var(--page-top-gap, 0px));
  margin-top: var(--page-top-gap, 0px);
  width: 100%;
  /* 让本看板内部滚动，避免外层再出现第二个滚动条 */
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

:global(.v-layout__fullscreen) .fit-host {
  height: var(--viewport-height, 100vh);
  margin-top: 0;
}
 
.fit-canvas {
  /* 让内容参与布局，避免外层 ElScrollbar 认为“没有更多高度可滚动”。 */
  position: relative;
}
 
.board-surface {
  width: 100%;
  height: 100%;
  background: var(--board-bg);
  color: var(--board-text);
  /* 本组件自身滚动时，需要让内容不被再次裁切 */
  overflow: visible;
}
 
.glass-panel {
  background: var(--board-panel-bg);
  backdrop-filter: blur(12px);
  border: 1px solid var(--board-panel-border);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}
 
.board-header {
  margin: 12px;
  padding: 12px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
 
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
 
.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}
 
.header-icon-svg {
  width: 22px;
  height: 22px;
}
 
.header-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0.5px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #ec4899);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
 
.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}
 
.time-range {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
 
.range-btn {
  background: var(--board-btn-bg);
  color: var(--board-text-muted);
  border: 1px solid var(--board-btn-border);
  border-radius: 30px;
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}
 
.range-btn:hover {
  border-color: rgba(59, 130, 246, 0.6);
  color: var(--board-text);
}
 
.range-btn.active {
  background: #3b82f6;
  color: #ffffff;
  border-color: #3b82f6;
}
 
.custom-date {
  display: none;
  gap: 8px;
  align-items: center;
  background: var(--board-btn-bg);
  padding: 4px 12px 4px 16px;
  border-radius: 40px;
  border: 1px solid var(--board-btn-border);
}
 
.custom-date.visible {
  display: flex;
}
 
.custom-label {
  font-size: 12px;
  color: var(--board-text-muted);
}
 
.custom-sep {
  color: var(--board-text-muted);
}
 
.custom-picker {
  width: 130px;
}
 
.now {
  text-align: right;
}
 
.now-time {
  font-size: 24px;
  font-weight: 800;
}
 
.now-date {
  font-size: 12px;
  color: var(--board-text-muted);
}
 
.grid-layout {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 20px;
  padding: 0 12px 16px 12px;
}
 
.col-span-3 {
  grid-column: span 3;
}
 
.col-span-5 {
  grid-column: span 5;
}
 
.col-span-7 {
  grid-column: span 7;
}
 
.metric-card {
  padding: 20px 22px;
  height: 100%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(147, 51, 234, 0.1));
  border-left: 4px solid #3b82f6;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
 
.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.18);
}
 
.border-blue {
  border-left-color: #3b82f6;
}
 
.border-purple {
  border-left-color: #8b5cf6;
}
 
.border-pink {
  border-left-color: #ec4899;
}
 
.border-orange {
  border-left-color: #f59e0b;
}
 
.metric-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}
 
.metric-label {
  font-size: 13px;
  color: var(--board-text-muted);
}
 
.time-label {
  font-size: 12px;
  display: inline-block;
  background: rgba(59, 130, 246, 0.18);
  padding: 2px 8px;
  border-radius: 20px;
  margin-left: 6px;
  color: #3b82f6;
}
 
.metric-total {
  font-size: 34px;
  font-weight: 800;
  margin-top: 4px;
  color: var(--board-text);
}
 
.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  margin-top: 8px;
  animation: pulse 2s infinite;
}
 
.status-normal {
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
}
 
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
 
.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 12px;
}
 
.stat-item {
  background: rgba(0, 0, 0, 0.18);
  border-radius: 12px;
  padding: 12px;
  text-align: center;
}
 
.stat-value {
  font-size: 26px;
  font-weight: 800;
}
 
.stat-label {
  font-size: 12px;
  color: var(--board-text-muted);
}
 
.metric-kv {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}
 
.kv-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}
 
.kv-k {
  color: var(--board-text-muted);
}
 
.kv-v {
  color: var(--board-text);
  font-weight: 700;
}
 
.avatar-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
}
 
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid var(--board-avatar-border);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: #ffffff;
}
 
.panel {
  padding: 20px 22px;
  height: 100%;
}
 
.panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}
 
.panel-icon {
  width: 20px;
  height: 20px;
}
 
.chart-container {
  width: 100%;
  height: 300px;
}
 
.completion-chart {
  height: 280px;
}
 
.heatmap-chart {
  height: 260px;
}
 
.legend-row {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 8px;
}
 
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
 
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
 
.dot-green {
  background: #10b981;
}
 
.dot-yellow {
  background: #f59e0b;
}
 
.dot-red {
  background: #ef4444;
}
 
.legend-desc {
  font-size: 12px;
  color: var(--board-text-muted);
  margin-top: 12px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.18);
  border-radius: 10px;
  line-height: 1.7;
}
 
.desc-title {
  color: var(--board-text);
  margin-bottom: 4px;
}
 
.desc-item {
  margin-top: 2px;
}
 
.staff-rank {
  margin-top: 6px;
  max-height: 360px;
  overflow-y: auto;
  padding-right: 6px;
}
 
.staff-rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.45);
  border-radius: 14px;
  margin-bottom: 10px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  transition: 0.2s;
}
 
.staff-rank-item:hover {
  background: rgba(59, 130, 246, 0.14);
  border-color: rgba(59, 130, 246, 0.7);
}
 
.staff-avatar {
  width: 40px;
  height: 40px;
  border-radius: 40%;
  background: linear-gradient(145deg, #2563eb, #7c3aed);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
  color: #ffffff;
}
 
.staff-info {
  flex: 1;
  min-width: 0;
}
 
.staff-name {
  font-weight: 700;
  font-size: 16px;
  color: var(--board-text);
}
 
.staff-stats {
  display: flex;
  gap: 16px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--board-text-muted);
  flex-wrap: wrap;
}
 
.staff-score {
  font-size: 20px;
  font-weight: 900;
  color: var(--board-text);
}
 
.score-card {
  margin-top: 12px;
  background: rgba(15, 23, 42, 0.55);
  border-radius: 12px;
  padding: 12px;
  border: 1px solid rgba(148, 163, 184, 0.16);
}
 
.score-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}
 
.score-title {
  font-size: 13px;
  color: var(--board-text-muted);
}
 
.score-rule {
  font-size: 11px;
  color: #3b82f6;
}
 
.score-value-wrap {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 2px;
  margin-top: 4px;
}
 
.score-value {
  font-size: 26px;
  font-weight: 900;
  color: var(--board-text);
}
 
.score-unit {
  font-size: 14px;
  color: var(--board-text-muted);
}
 
.missed-rank {
  margin-top: 8px;
}
 
.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(15, 23, 42, 0.36);
  border-radius: 10px;
  margin-bottom: 10px;
}
 
.rank-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
}
 
.rank-name {
  flex: 1;
  font-size: 13px;
  font-weight: 700;
  color: var(--board-text);
}
 
.rank-bar {
  width: 96px;
  height: 8px;
  background: rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  overflow: hidden;
}
 
.rank-bar-fill {
  height: 100%;
  background: #ef4444;
}
 
.rank-count {
  font-size: 13px;
  font-weight: 800;
  color: #ef4444;
}
 
.missed-summary {
  margin-top: 14px;
  padding: 10px 12px;
  background: rgba(148, 163, 184, 0.12);
  border-radius: 10px;
  font-size: 12px;
  color: var(--board-text-muted);
}
 
.heat-legend {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  color: var(--board-text-muted);
  margin-top: 10px;
  padding: 0 10px;
}
 
.heat-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
 
.heat-box {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}
 
.heat-low {
  background: #1e293b;
}
 
.heat-mid {
  background: #3b82f6;
}
 
.heat-high {
  background: #8b5cf6;
}
 
.function-hint {
  background: rgba(0, 212, 255, 0.07);
  border-left: 4px solid #38bdf8;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  margin-top: 14px;
}
 
.hint-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
 
.hint-icon {
  width: 20px;
  height: 20px;
  color: #38bdf8;
  margin-top: 2px;
  flex-shrink: 0;
}
 
.hint-title {
  font-size: 14px;
  font-weight: 700;
  color: rgba(56, 189, 248, 0.95);
}
 
.hint-desc {
  font-size: 12px;
  color: var(--board-text);
  opacity: 0.9;
  line-height: 1.6;
  margin-top: 4px;
}
 
.text-green {
  color: #10b981;
}
 
.text-yellow {
  color: #f59e0b;
}
 
.text-red {
  color: #ef4444;
}
 
.text-purple {
  color: #8b5cf6;
}
 
:global(.dark) .patrol-board-page {
  --board-bg: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  --board-panel-bg: rgba(30, 41, 59, 0.72);
  --board-panel-border: rgba(148, 163, 184, 0.12);
  --board-text: #e2e8f0;
  --board-text-muted: #94a3b8;
  --board-btn-bg: rgba(15, 23, 42, 0.75);
  --board-btn-border: rgba(51, 65, 85, 0.95);
  --board-avatar-border: rgba(15, 23, 42, 0.9);
}
 
:global(.light) .patrol-board-page,
:global(:root:not(.dark)) .patrol-board-page {
  --board-bg: linear-gradient(135deg, #eef2ff 0%, #f8fafc 100%);
  --board-panel-bg: rgba(255, 255, 255, 0.82);
  --board-panel-border: rgba(2, 6, 23, 0.08);
  --board-text: var(--el-text-color-primary);
  --board-text-muted: var(--el-text-color-secondary);
  --board-btn-bg: rgba(255, 255, 255, 0.7);
  --board-btn-border: rgba(2, 6, 23, 0.12);
  --board-avatar-border: rgba(255, 255, 255, 0.9);
}
 
.staff-rank::-webkit-scrollbar {
  width: 6px;
}
 
.staff-rank::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
}
 
.staff-rank::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.35);
  border-radius: 6px;
}
</style>
