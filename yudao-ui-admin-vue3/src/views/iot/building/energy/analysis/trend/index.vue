<template>
  <div class="energy-trend-page dark-theme-page">
    <!-- 趋势图表 -->
    <div class="analysis-card">
      <div class="card-header">
        <div class="card-title">
          <Icon icon="mdi:chart-line" class="mr-8px" /> 近30天能耗趋势
        </div>
        <div class="trend-tabs">
          <button
            v-for="tab in trendTabs"
            :key="tab.value"
            class="trend-tab"
            :class="{ active: activeTab === tab.value }"
            @click="switchTab(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
      
      <div ref="trendChartRef" class="trend-chart" v-loading="chartLoading"></div>
      
      <!-- 统计数据 -->
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-num">{{ formatNumber(monthStats.total) }}</div>
          <div class="stat-label">本月累计({{ getUnit() }})</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ formatNumber(monthStats.avgDaily) }}</div>
          <div class="stat-label">日均用量({{ getUnit() }})</div>
        </div>
        <div class="stat-item">
          <div class="stat-num" :class="{ danger: monthStats.mom > 0, success: monthStats.mom < 0 }">
            {{ monthStats.mom > 0 ? '+' : '' }}{{ monthStats.mom }}%
          </div>
          <div class="stat-label">环比增长</div>
        </div>
        <div class="stat-item">
          <div class="stat-num" :class="{ danger: monthStats.yoy > 0, success: monthStats.yoy < 0 }">
            {{ monthStats.yoy > 0 ? '+' : '' }}{{ monthStats.yoy }}%
          </div>
          <div class="stat-label">同比变化</div>
        </div>
      </div>
    </div>

    <!-- 分项对比 -->
    <div class="comparison-grid">
      <div class="comparison-card electric">
        <div class="comparison-header">
          <Icon icon="mdi:flash" class="icon" />
          <span>用电分析</span>
        </div>
        <div class="comparison-body">
          <div class="comparison-item">
            <span class="label">今日用量</span>
            <span class="value">{{ formatNumber(compareData.electric.today) }} kWh</span>
          </div>
          <div class="comparison-item">
            <span class="label">昨日用量</span>
            <span class="value">{{ formatNumber(compareData.electric.yesterday) }} kWh</span>
          </div>
          <div class="comparison-item">
            <span class="label">本月累计</span>
            <span class="value highlight">{{ formatNumber(compareData.electric.month) }} kWh</span>
          </div>
          <div class="comparison-item">
            <span class="label">环比变化</span>
            <span class="value" :class="{ up: compareData.electric.change > 0, down: compareData.electric.change < 0 }">
              {{ compareData.electric.change > 0 ? '↑' : '↓' }}{{ Math.abs(compareData.electric.change) }}%
            </span>
          </div>
        </div>
      </div>
      
      <div class="comparison-card water">
        <div class="comparison-header">
          <Icon icon="mdi:water" class="icon" />
          <span>用水分析</span>
        </div>
        <div class="comparison-body">
          <div class="comparison-item">
            <span class="label">今日用量</span>
            <span class="value">{{ formatNumber(compareData.water.today) }} m³</span>
          </div>
          <div class="comparison-item">
            <span class="label">昨日用量</span>
            <span class="value">{{ formatNumber(compareData.water.yesterday) }} m³</span>
          </div>
          <div class="comparison-item">
            <span class="label">本月累计</span>
            <span class="value highlight">{{ formatNumber(compareData.water.month) }} m³</span>
          </div>
          <div class="comparison-item">
            <span class="label">环比变化</span>
            <span class="value" :class="{ up: compareData.water.change > 0, down: compareData.water.change < 0 }">
              {{ compareData.water.change > 0 ? '↑' : '↓' }}{{ Math.abs(compareData.water.change) }}%
            </span>
          </div>
        </div>
      </div>
      
      <div class="comparison-card gas">
        <div class="comparison-header">
          <Icon icon="mdi:fire" class="icon" />
          <span>用气分析</span>
        </div>
        <div class="comparison-body">
          <div class="comparison-item">
            <span class="label">今日用量</span>
            <span class="value">{{ formatNumber(compareData.gas.today) }} m³</span>
          </div>
          <div class="comparison-item">
            <span class="label">昨日用量</span>
            <span class="value">{{ formatNumber(compareData.gas.yesterday) }} m³</span>
          </div>
          <div class="comparison-item">
            <span class="label">本月累计</span>
            <span class="value highlight">{{ formatNumber(compareData.gas.month) }} m³</span>
          </div>
          <div class="comparison-item">
            <span class="label">环比变化</span>
            <span class="value" :class="{ up: compareData.gas.change > 0, down: compareData.gas.change < 0 }">
              {{ compareData.gas.change > 0 ? '↑' : '↓' }}{{ Math.abs(compareData.gas.change) }}%
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import echarts from '@/plugins/echarts'

defineOptions({ name: 'BuildingEnergyTrendAnalysis' })

// 数据
const chartLoading = ref(false)
const trendChartRef = ref<HTMLElement>()
let trendChartInstance: echarts.ECharts | null = null

// Tab切换
const activeTab = ref('total')
const trendTabs = [
  { label: '综合能耗', value: 'total' },
  { label: '用电', value: 'electric' },
  { label: '用水', value: 'water' },
  { label: '用气', value: 'gas' }
]

// 统计数据
const monthStats = reactive({
  total: 68520,
  avgDaily: 2284,
  mom: 8.5,
  yoy: -2.3
})

// 分项对比数据
const compareData = reactive({
  electric: { today: 2458, yesterday: 2186, month: 52400, change: 12.5 },
  water: { today: 18.6, yesterday: 19.6, month: 580, change: -5.2 },
  gas: { today: 45.2, yesterday: 41.8, month: 1420, change: 8.3 }
})

// 图表数据
const chartData = ref<{ dates: string[]; values: number[] }>({ dates: [], values: [] })

// 工具函数
const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '0'
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

const getUnit = () => {
  const map: Record<string, string> = {
    total: 'tce',
    electric: 'kWh',
    water: 'm³',
    gas: 'm³'
  }
  return map[activeTab.value] || ''
}

// 切换Tab
const switchTab = (tab: string) => {
  activeTab.value = tab
  loadChartData()
}

// 加载图表数据
const loadChartData = async () => {
  chartLoading.value = true
  try {
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    
    let meterType: number | undefined
    if (activeTab.value === 'electric') meterType = 1
    else if (activeTab.value === 'water') meterType = 2
    else if (activeTab.value === 'gas') meterType = 3
    
    let data: any[]
    if (meterType) {
      data = await EnergyApi.getStatisticsByTypeAndDateRange(meterType, startDate, endDate)
    } else {
      data = await EnergyApi.getStatisticsByDateRange(startDate, endDate)
    }
    
    // 按日期分组汇总
    const dateMap = new Map<string, number>()
    data.forEach(item => {
      const date = item.statisticsDate
      const current = dateMap.get(date) || 0
      dateMap.set(date, current + (item.dailyUsage || 0))
    })
    
    const dates = Array.from(dateMap.keys()).sort()
    const values = dates.map(d => dateMap.get(d) || 0)
    
    chartData.value = { dates, values }
    renderChart()
  } catch (e) {
    console.error('加载图表数据失败', e)
  } finally {
    chartLoading.value = false
  }
}

// 渲染图表
const renderChart = () => {
  if (!trendChartRef.value) return
  
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value)
  }
  
  const colors: Record<string, string> = {
    total: '#10b981',
    electric: '#f59e0b',
    water: '#06b6d4',
    gas: '#f97316'
  }
  
  const color = colors[activeTab.value] || '#10b981'
  
  trendChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 30, 0.9)',
      borderColor: '#404040',
      textStyle: { color: '#fff' },
      axisPointer: {
        type: 'shadow',
        shadowStyle: { color: 'rgba(16, 185, 129, 0.1)' }
      }
    },
    grid: {
      top: 20,
      right: 20,
      bottom: 40,
      left: 60
    },
    xAxis: {
      type: 'category',
      data: chartData.value.dates,
      axisLine: { lineStyle: { color: '#3a3a4a' } },
      axisLabel: { color: '#a0a0b0', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#a0a0b0' },
      splitLine: { lineStyle: { color: '#2d2d3a' } }
    },
    series: [{
      name: '用量',
      type: 'bar',
      data: chartData.value.values,
      barWidth: '60%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: color },
          { offset: 1, color: color + '33' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: {
        itemStyle: { color: color }
      }
    }]
  })
}

// 加载总览数据
const loadOverview = async () => {
  try {
    const overview = await EnergyApi.getOverview()
    
    compareData.electric.today = overview.todayElectricity || 0
    compareData.electric.month = overview.monthElectricity || 0
    compareData.electric.change = overview.electricityMom || 0
    
    compareData.water.today = overview.todayWater || 0
    compareData.water.month = overview.monthWater || 0
    compareData.water.change = overview.waterMom || 0
    
    compareData.gas.today = overview.todayGas || 0
    compareData.gas.month = overview.monthGas || 0
    
    // 计算综合能耗统计
    monthStats.total = compareData.electric.month + compareData.water.month * 10 + compareData.gas.month * 12
    monthStats.avgDaily = Math.round(monthStats.total / 30)
  } catch (e) {
    console.error('加载总览数据失败', e)
  }
}

// 生命周期
onMounted(() => {
  loadOverview()
  loadChartData()
  window.addEventListener('resize', () => trendChartInstance?.resize())
})

onUnmounted(() => {
  trendChartInstance?.dispose()
  window.removeEventListener('resize', () => trendChartInstance?.resize())
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-trend-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.analysis-card {
  background: #252532;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid #3a3a4a;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #ffffff;
      display: flex;
      align-items: center;
    }
    
    .trend-tabs {
      display: flex;
      gap: 8px;
      
      .trend-tab {
        padding: 8px 20px;
        border: 1px solid #3a3a4a;
        background: transparent;
        border-radius: 20px;
        cursor: pointer;
        font-size: 13px;
        font-weight: 500;
        color: #a0a0b0;
        transition: all 0.3s;
        
        &:hover {
          border-color: #10b981;
          color: #10b981;
        }
        
        &.active {
          background: #10b981;
          border-color: #10b981;
          color: #ffffff;
        }
      }
    }
  }
  
  .trend-chart {
    height: 320px;
  }
  
  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #3a3a4a;
    
    .stat-item {
      text-align: center;
      
      .stat-num {
        font-size: 28px;
        font-weight: 700;
        color: #ffffff;
        font-family: 'Courier New', monospace;
        
        &.danger { color: #f87171; }
        &.success { color: #4ade80; }
      }
      
      .stat-label {
        font-size: 12px;
        color: #6b7280;
        margin-top: 4px;
      }
    }
  }
}

.comparison-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.comparison-card {
  background: #252532;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #3a3a4a;
  
  .comparison-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #3a3a4a;
    font-size: 15px;
    font-weight: 600;
    color: #ffffff;
    
    .icon {
      font-size: 20px;
    }
  }
  
  &.electric .comparison-header .icon { color: #fbbf24; }
  &.water .comparison-header .icon { color: #22d3ee; }
  &.gas .comparison-header .icon { color: #fb923c; }
  
  .comparison-body {
    .comparison-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px dashed #3a3a4a;
      
      &:last-child {
        border-bottom: none;
      }
      
      .label {
        font-size: 13px;
        color: #a0a0b0;
      }
      
      .value {
        font-size: 14px;
        font-weight: 600;
        color: #ffffff;
        font-family: 'Courier New', monospace;
        
        &.highlight {
          color: #10b981;
        }
        
        &.up {
          color: #f87171;
        }
        
        &.down {
          color: #4ade80;
        }
      }
    }
  }
}
</style>
