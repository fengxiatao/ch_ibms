<!--
  原型复刻：01 智能照明数据总览（2026.03.09）.html
  说明：不包含原型侧边栏（项目统一侧边栏已提供）；页面使用等比缩放容器适配 main 区域。
-->
<template>
  <div class="newlight-page wh-full">
    <ProtoScaleContainer :design-width="1440" :design-height="900" scale-by="width" class="wh-full">
      <div class="proto-root min-h-screen p-6 overflow-x-hidden">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
          <div class="fused-card">
            <div class="flex justify-between items-start mb-4">
              <div>
                <p class="text-slate-500 text-sm mb-1">设备运行状态</p>
                <h3 class="text-3xl font-bold text-slate-800">{{ deviceOnlineRate }}%</h3>
                <p class="text-green-600 text-xs mt-2 flex items-center gap-2">
                  <Icon icon="fa6-solid:arrow-trend-up" class="mr-1" /> 设备在线率（网关 + 控制器）
                  <span class="bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full ml-2">
                    {{ deviceOnline }}在线 / {{ deviceOffline }}离线
                  </span>
                </p>
              </div>
              <div class="w-12 h-12 bg-blue-500/10 rounded-lg flex items-center justify-center">
                <Icon icon="fa6-solid:chart-pie" class="text-blue-600 text-xl" />
              </div>
            </div>

            <div class="mt-2">
              <div class="flex items-center gap-2 mb-3">
                <Icon icon="fa6-solid:cubes" class="text-blue-500 text-sm" />
                <span class="font-medium text-slate-700">设备类型分布</span>
                <span class="bg-blue-50 text-blue-700 text-xs px-2 py-0.5 rounded-full ml-auto">总计 {{ deviceTypeTotal }} 台</span>
              </div>

              <div class="chart-ring-container">
                <div style="width: 120px; height: 120px" class="shrink-0">
                  <EChart :options="deviceDonutOpt" width="120px" height="120px" />
                </div>
                <div class="grid grid-cols-2 gap-x-3 gap-y-1 flex-1">
                  <div class="flex items-center">
                    <span class="legend-dot" style="background: #6366f1"></span>
                    <span class="text-xs">智能网关</span>
                    <span class="font-bold ml-auto">{{ stats.gatewayTotalCount ?? 0 }}</span>
                  </div>
                  <div class="flex items-center">
                    <span class="legend-dot" style="background: #eab308"></span>
                    <span class="text-xs">执行控制器</span>
                    <span class="font-bold ml-auto">{{ stats.controllerTotalCount ?? 0 }}</span>
                  </div>
                  <div class="flex items-center">
                    <span class="legend-dot" style="background: #22c55e"></span>
                    <span class="text-xs">灯具</span>
                    <span class="font-bold ml-auto">{{ stats.lightTotalCount ?? 0 }}</span>
                  </div>
                </div>
              </div>

              <div class="flex gap-3 mt-3 text-xs text-slate-500 border-t border-slate-100 pt-3">
                <span><Icon icon="fa6-solid:circle" class="text-green-500 mr-1" /> 在线: {{ deviceOnline }}台</span>
                <span><Icon icon="fa6-solid:circle" class="text-red-500 mr-1" /> 离线: {{ deviceOffline }}台</span>
              </div>
            </div>
          </div>

          <div class="glass-panel rounded-2xl p-6">
            <div class="flex items-center gap-2 mb-4">
              <Icon icon="fa6-solid:chart-pie" class="text-blue-600" />
              <h2 class="text-lg font-semibold text-slate-800">照明回路状态统计</h2>
            </div>

            <div class="status-donut-container mb-4">
              <div style="width: 130px; height: 130px" class="shrink-0">
                <EChart :options="circuitStatusDonutOpt" width="130px" height="130px" />
              </div>

              <div class="flex-1 grid grid-cols-2 gap-3">
                <div class="status-stat-card">
                  <div class="status-stat-value">{{ stats.circuitTotalCount ?? 0 }}</div>
                  <div class="status-stat-label">回路总数</div>
                </div>
                <div class="status-stat-card">
                  <div class="status-stat-value text-green-600">{{ stats.circuitOnCount ?? 0 }}</div>
                  <div class="status-stat-label">开启回路</div>
                </div>
                <div class="status-stat-card">
                  <div class="status-stat-value text-slate-500">{{ stats.circuitOffCount ?? 0 }}</div>
                  <div class="status-stat-label">关闭回路</div>
                </div>
                <div class="status-stat-card">
                  <div class="status-stat-value text-blue-600">{{ circuitOnRate }}%</div>
                  <div class="status-stat-label">开启率</div>
                </div>
              </div>
            </div>

            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：基于今日实时状态（更新于 {{ refreshAtLabel }}）</span>
            </div>
          </div>

          <div class="glass-panel rounded-2xl p-6">
            <div class="flex items-center gap-2 mb-4">
              <Icon icon="fa6-solid:triangle-exclamation" class="text-amber-500" />
              <h2 class="text-lg font-semibold text-slate-800">告警状态统计</h2>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div class="status-stat-card">
                <div class="status-stat-value text-amber-500">{{ alarmPending }}</div>
                <div class="status-stat-label">待处理</div>
              </div>
              <div class="status-stat-card">
                <div class="status-stat-value text-green-600">{{ alarmHandled }}</div>
                <div class="status-stat-label">已处理</div>
              </div>
              <div class="status-stat-card">
                <div class="status-stat-value text-red-500">{{ alarmCritical }}</div>
                <div class="status-stat-label">紧急</div>
              </div>
              <div class="status-stat-card">
                <div class="status-stat-value text-slate-700">{{ alarmTodayTotal }}</div>
                <div class="status-stat-label">今日总计</div>
              </div>
            </div>
            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：基于告警明细聚合（更新于 {{ refreshAtLabel }}）</span>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
          <div class="glass-panel rounded-2xl p-6 lg:col-span-2">
            <div class="flex items-center gap-2 mb-4">
              <Icon icon="fa6-solid:bolt" class="text-blue-600" />
              <h2 class="text-lg font-semibold text-slate-800">能耗趋势</h2>
              <span class="text-xs text-slate-500 ml-auto">单位：kWh</span>
            </div>
            <div class="h-64 flex flex-col items-center justify-center text-slate-400">
              <Icon icon="fa6-solid:chart-line" class="text-4xl mb-3" />
              <p class="text-sm">能耗采集端点开发中</p>
              <p class="text-xs mt-1">当前总功率：{{ stats.totalPower ?? 0 }} kW · 实时功率：{{ stats.currentPower ?? 0 }} kW</p>
            </div>
            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：能耗历史采集端点未启用，待后端补</span>
            </div>
          </div>

          <div class="glass-panel rounded-2xl p-6">
            <div class="flex items-center gap-2 mb-4">
              <Icon icon="fa6-solid:trophy" class="text-amber-500" />
              <h2 class="text-lg font-semibold text-slate-800">节能排名</h2>
            </div>
            <div class="scrollable-content flex flex-col items-center justify-center text-slate-400 py-12">
              <Icon icon="fa6-solid:ranking-star" class="text-4xl mb-3" />
              <p class="text-sm">区域能耗排名端点开发中</p>
            </div>
            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：区域能耗聚合端点未启用，待后端补</span>
            </div>
          </div>
        </div>
      </div>
    </ProtoScaleContainer>
  </div>
</template>

<script lang="ts" setup>
import type { EChartsOption } from 'echarts'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ProtoScaleContainer from '../ProtoScaleContainer.vue'
import * as LightingApi from '@/api/iot/building/lighting'

defineOptions({ name: 'NewLightOverview' })

let refreshTimer: number | null = null

const stats = ref<LightingApi.IbmsLightingStatisticsVO>({})
const alarms = ref<LightingApi.IbmsLightingAlarmVO[]>([])
const loading = ref(false)
const lastRefreshAt = ref<Date | null>(null)

// 拉取统计 + 告警列表（用于本地按 level/status 分组）
const refreshData = async () => {
  loading.value = true
  try {
    const [s, a] = await Promise.all([
      LightingApi.getStatistics(),
      LightingApi.getAlarmPage({ pageNo: 1, pageSize: 100 } as LightingApi.IbmsLightingAlarmPageReqVO)
    ])
    stats.value = (s ?? {}) as LightingApi.IbmsLightingStatisticsVO
    alarms.value = ((a as { list?: LightingApi.IbmsLightingAlarmVO[] })?.list ?? [])
    lastRefreshAt.value = new Date()
  } finally {
    loading.value = false
  }
}

// 设备运行状态聚合（gateway + controller，灯具不参与在线状态）
const deviceTotal = computed(
  () => (stats.value.gatewayTotalCount ?? 0) + (stats.value.controllerTotalCount ?? 0)
)
const deviceOnline = computed(
  () => (stats.value.gatewayOnlineCount ?? 0) + (stats.value.controllerOnlineCount ?? 0)
)
const deviceOffline = computed(() => Math.max(0, deviceTotal.value - deviceOnline.value))
const deviceOnlineRate = computed(() => {
  if (deviceTotal.value === 0) return '0.0'
  return ((deviceOnline.value / deviceTotal.value) * 100).toFixed(1)
})

// 设备类型分布饼图总数（含灯具，对齐饼图三段）
const deviceTypeTotal = computed(() => deviceTotal.value + (stats.value.lightTotalCount ?? 0))

// 回路开启率
const circuitOnRate = computed(() => {
  const total = stats.value.circuitTotalCount ?? 0
  const on = stats.value.circuitOnCount ?? 0
  if (total === 0) return 0
  return Math.round((on / total) * 100)
})

// 告警分组（基于 alarms 列表）
const todayStartTs = computed(() => {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d.getTime()
})
const alarmTodayTotal = computed(() =>
  alarms.value.filter((a) => {
    if (!a.alarmTime) return false
    return new Date(a.alarmTime as unknown as string).getTime() >= todayStartTs.value
  }).length
)
const alarmPending = computed(() => alarms.value.filter((a) => a.status === 0).length)
const alarmHandled = computed(() => alarms.value.filter((a) => a.status === 2).length)
const alarmCritical = computed(() => alarms.value.filter((a) => a.alarmLevel === 3).length)

const refreshAtLabel = computed(() => {
  const d = lastRefreshAt.value
  if (!d) return '加载中…'
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
})

const donutBase = {
  tooltip: { show: false },
  legend: { show: false },
  series: []
} satisfies EChartsOption

const deviceDonutOpt = computed<EChartsOption>(() => {
  return {
    ...donutBase,
    series: [
      {
        type: 'pie',
        radius: ['58%', '86%'],
        avoidLabelOverlap: true,
        label: { show: false },
        labelLine: { show: false },
        data: [
          { value: stats.value.gatewayTotalCount ?? 0, name: '智能网关', itemStyle: { color: '#6366f1' } },
          { value: stats.value.controllerTotalCount ?? 0, name: '执行控制器', itemStyle: { color: '#eab308' } },
          { value: stats.value.lightTotalCount ?? 0, name: '灯具', itemStyle: { color: '#22c55e' } }
        ]
      }
    ]
  }
})

const circuitStatusDonutOpt = computed<EChartsOption>(() => {
  return {
    ...donutBase,
    series: [
      {
        type: 'pie',
        radius: ['60%', '88%'],
        label: { show: false },
        labelLine: { show: false },
        data: [
          { value: stats.value.circuitOnCount ?? 0, name: '开启', itemStyle: { color: '#10b981' } },
          { value: stats.value.circuitOffCount ?? 0, name: '关闭', itemStyle: { color: '#94a3b8' } }
        ]
      }
    ]
  }
})

onMounted(() => {
  // 初次加载 + 30s 自动刷新
  refreshData()
  refreshTimer = window.setInterval(refreshData, 30_000)
})

onBeforeUnmount(() => {
  if (refreshTimer) window.clearInterval(refreshTimer)
  refreshTimer = null
})
</script>

<style scoped>
.proto-root {
  font-family: 'Inter', 'Noto Sans SC', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Microsoft YaHei', Arial, sans-serif;
  background: var(--app-content-bg-color);
  color: var(--el-text-color-primary);
}

.glass-panel {
  background: var(--el-bg-color-overlay);
  backdrop-filter: blur(12px);
  border: 1px solid var(--el-border-color);
  box-shadow: var(--el-box-shadow-light);
}

.neon-glow {
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.15);
}

.gradient-text {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.live-indicator::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  margin-right: 8px;
  animation: blink 2s infinite;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.scrollable-content {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: var(--el-border-color) var(--el-fill-color-light);
}

.scrollable-content::-webkit-scrollbar {
  width: 6px;
}

.scrollable-content::-webkit-scrollbar-track {
  background: var(--el-fill-color-light);
  border-radius: 10px;
}

.scrollable-content::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 10px;
}

.scrollable-content::-webkit-scrollbar-thumb:hover {
  background: var(--el-text-color-placeholder);
}

.status-donut-container {
  display: flex;
  align-items: center;
  gap: 2rem;
  flex-wrap: wrap;
  justify-content: center;
}

.status-stat-card {
  background: var(--el-bg-color-overlay);
  border-radius: 1rem;
  padding: 1rem;
  text-align: center;
  flex: 1;
  min-width: 100px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid var(--el-border-color);
}

.status-stat-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.2;
}

.status-stat-label {
  font-size: 0.8rem;
  color: var(--el-text-color-regular);
  margin-top: 0.25rem;
}

.rank-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--el-border-color);
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-number {
  width: 24px;
  height: 24px;
  border-radius: 9999px;
  background: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--el-text-color-regular);
  margin-right: 0.5rem;
  flex-shrink: 0;
}

.rank-number.top-1 {
  background: #fbbf24;
  color: #92400e;
}

.rank-number.top-2 {
  background: #94a3b8;
  color: #1e293b;
}

.rank-number.top-3 {
  background: #b45309;
  color: #fffbeb;
}

.rank-value {
  font-weight: 600;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.rank-unit {
  font-size: 0.7rem;
  color: var(--el-text-color-regular);
  margin-left: 0.25rem;
}

.progress-bar {
  height: 6px;
  background: var(--el-fill-color-light);
  border-radius: 20px;
  overflow: hidden;
  flex: 1;
  margin: 0 0.5rem;
  min-width: 60px;
}

.progress-fill {
  height: 100%;
  border-radius: 20px;
  background: var(--el-color-primary);
}

.data-basis {
  font-size: 0.7rem;
  color: var(--el-text-color-placeholder);
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px dashed var(--el-border-color);
}

.chart-ring-container {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 4px;
  display: inline-block;
  margin-right: 8px;
}

.fused-card {
  background: linear-gradient(135deg, var(--el-bg-color-overlay), var(--el-fill-color-light));
  border: 1px solid var(--el-border-color);
  border-radius: 1.5rem;
  padding: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
}

.proto-root .text-slate-800,
.proto-root .text-slate-700 {
  color: var(--el-text-color-primary) !important;
}

.proto-root .text-slate-600,
.proto-root .text-slate-500 {
  color: var(--el-text-color-regular) !important;
}

.proto-root .text-slate-400,
.proto-root .text-slate-300 {
  color: var(--el-text-color-placeholder) !important;
}

.proto-root .border-slate-100,
.proto-root .border-slate-200 {
  border-color: var(--el-border-color-lighter) !important;
}

.proto-root .hover\:bg-slate-100:hover {
  background-color: var(--el-fill-color-light) !important;
}
</style>
