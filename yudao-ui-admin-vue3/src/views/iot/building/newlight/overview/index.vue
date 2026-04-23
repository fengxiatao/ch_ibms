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
                <h3 class="text-3xl font-bold text-slate-800">98.2%</h3>
                <p class="text-green-600 text-xs mt-2 flex items-center gap-2">
                  <Icon icon="fa6-solid:arrow-trend-up" class="mr-1" /> 设备在线率 较昨日 +0.5%
                  <span class="bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full ml-2">
                    17在线 / 1离线
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
                <span class="bg-blue-50 text-blue-700 text-xs px-2 py-0.5 rounded-full ml-auto">总计 17 台</span>
              </div>

              <div class="chart-ring-container">
                <div style="width: 120px; height: 120px" class="shrink-0">
                  <EChart :options="deviceDonutOpt" width="120px" height="120px" />
                </div>
                <div class="grid grid-cols-2 gap-x-3 gap-y-1 flex-1">
                  <div class="flex items-center">
                    <span class="legend-dot" style="background: #6366f1"></span>
                    <span class="text-xs">智能网关</span>
                    <span class="font-bold ml-auto">2</span>
                  </div>
                  <div class="flex items-center">
                    <span class="legend-dot" style="background: #eab308"></span>
                    <span class="text-xs">执行控制器</span>
                    <span class="font-bold ml-auto">2</span>
                  </div>
                  <div class="flex items-center">
                    <span class="legend-dot" style="background: #22c55e"></span>
                    <span class="text-xs">传感器</span>
                    <span class="font-bold ml-auto">13</span>
                  </div>
                </div>
              </div>

              <div class="flex gap-3 mt-3 text-xs text-slate-500 border-t border-slate-100 pt-3">
                <span><Icon icon="fa6-solid:circle" class="text-green-500 mr-1" /> 在线: 17台</span>
                <span><Icon icon="fa6-solid:circle" class="text-red-500 mr-1" /> 离线: 1台</span>
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
                  <div class="status-stat-value">15</div>
                  <div class="status-stat-label">回路总数</div>
                </div>
                <div class="status-stat-card">
                  <div class="status-stat-value text-green-600">11</div>
                  <div class="status-stat-label">开启回路</div>
                </div>
                <div class="status-stat-card">
                  <div class="status-stat-value text-slate-500">4</div>
                  <div class="status-stat-label">关闭回路</div>
                </div>
                <div class="status-stat-card">
                  <div class="status-stat-value text-blue-600">73%</div>
                  <div class="status-stat-label">开启率</div>
                </div>
              </div>
            </div>

            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：基于今日实时状态 (2026-03-04 14:30 更新)</span>
            </div>
          </div>

          <div class="glass-panel rounded-2xl p-6">
            <div class="flex items-center gap-2 mb-4">
              <Icon icon="fa6-solid:triangle-exclamation" class="text-amber-500" />
              <h2 class="text-lg font-semibold text-slate-800">告警状态统计</h2>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div class="status-stat-card">
                <div class="status-stat-value text-amber-500">3</div>
                <div class="status-stat-label">待处理</div>
              </div>
              <div class="status-stat-card">
                <div class="status-stat-value text-green-600">12</div>
                <div class="status-stat-label">已处理</div>
              </div>
              <div class="status-stat-card">
                <div class="status-stat-value text-red-500">1</div>
                <div class="status-stat-label">严重</div>
              </div>
              <div class="status-stat-card">
                <div class="status-stat-value text-slate-700">16</div>
                <div class="status-stat-label">今日总计</div>
              </div>
            </div>
            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：今日累计告警 (2026-03-04 14:30 更新)</span>
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
            <div class="h-64">
              <EChart :options="energyTrendOpt" width="100%" height="260px" />
            </div>
            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：近 7 天能耗走势（模拟数据）</span>
            </div>
          </div>

          <div class="glass-panel rounded-2xl p-6">
            <div class="flex items-center gap-2 mb-4">
              <Icon icon="fa6-solid:trophy" class="text-amber-500" />
              <h2 class="text-lg font-semibold text-slate-800">节能排名</h2>
            </div>
            <div class="scrollable-content">
              <div v-for="(item, idx) in rankList" :key="item.name" class="rank-item">
                <div class="flex items-center flex-1 min-w-0">
                  <div class="rank-number" :class="idx <= 2 ? `top-${idx + 1}` : ''">{{ idx + 1 }}</div>
                  <div class="truncate text-slate-700">{{ item.name }}</div>
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: `${item.percent}%` }"></div>
                  </div>
                </div>
                <div class="rank-value">{{ item.value }}<span class="rank-unit">kWh</span></div>
              </div>
            </div>
            <div class="data-basis mt-2">
              <Icon icon="fa6-solid:circle-info" class="text-slate-400" />
              <span>数据基准：今日节能 Top 10（模拟数据）</span>
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

defineOptions({ name: 'NewLightOverview' })

const now = ref<Date>(new Date())
let timer: number | null = null

/**
 * 刷新页面展示数据（原型为静态数据，此处仅更新“当前时间/日期”）
 */
const refreshData = () => {
  now.value = new Date()
}

const currentTime = computed(() => {
  const d = now.value
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${hh}:${mm}:${ss}`
})

const currentDate = computed(() => {
  const d = now.value
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
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
          { value: 2, name: '智能网关', itemStyle: { color: '#6366f1' } },
          { value: 2, name: '执行控制器', itemStyle: { color: '#eab308' } },
          { value: 13, name: '传感器', itemStyle: { color: '#22c55e' } }
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
          { value: 11, name: '开启', itemStyle: { color: '#10b981' } },
          { value: 4, name: '关闭', itemStyle: { color: '#94a3b8' } }
        ]
      }
    ]
  }
})

const energyTrendOpt = computed<EChartsOption>(() => {
  const x = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const y = [120, 132, 101, 134, 90, 70, 110]
  return {
    grid: { left: 24, right: 18, top: 18, bottom: 28, containLabel: true },
    xAxis: { type: 'category', data: x, axisTick: { show: false }, axisLine: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(148,163,184,0.25)' } } },
    tooltip: { trigger: 'axis' },
    series: [
      {
        type: 'line',
        data: y,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#3b82f6' },
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(59,130,246,0.35)' },
              { offset: 1, color: 'rgba(59,130,246,0.02)' }
            ]
          }
        }
      }
    ]
  }
})

const rankList = ref(
  Array.from({ length: 10 }).map((_, i) => ({
    name: `区域 ${i + 1}`,
    value: [36, 34, 33, 31, 29, 26, 24, 22, 19, 16][i] ?? 0,
    percent: [100, 94, 90, 84, 78, 70, 64, 58, 50, 44][i] ?? 0
  }))
)

onMounted(() => {
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  timer = null
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
