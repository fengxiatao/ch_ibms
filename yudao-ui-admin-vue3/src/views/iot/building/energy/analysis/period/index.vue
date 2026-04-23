<template>
  <div class="energy-period-page dark-theme-page">
    <!-- 用电分析 -->
    <div class="analysis-row">
      <div class="analysis-card chart-card">
        <div class="card-header">
          <div class="card-title">
            <Icon icon="mdi:flash" class="icon electric" /> 用电负荷曲线（24小时）
          </div>
          <div class="legend">
            <span class="legend-item"><span class="dot today"></span> 今日</span>
            <span class="legend-item"><span class="dot yesterday"></span> 昨日</span>
          </div>
        </div>
        <div ref="electricChartRef" class="period-chart"></div>
      </div>
      
      <div class="analysis-card table-card">
        <div class="card-header">
          <div class="card-title">
            <Icon icon="mdi:flash" class="icon electric" /> 峰谷平用电统计
          </div>
        </div>
        <el-table :data="electricPeriodData" stripe class="dark-table period-table">
          <el-table-column label="时段类型" prop="type" width="120">
            <template #default="{ row }">
              <span class="period-type" :class="row.typeClass">● {{ row.type }}</span>
            </template>
          </el-table-column>
          <el-table-column label="时间范围" prop="range" width="140" />
          <el-table-column label="用电量(kWh)" prop="usage" align="right">
            <template #default="{ row }">
              <span class="usage-value">{{ formatNumber(row.usage) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" prop="ratio" width="100" align="right">
            <template #default="{ row }">{{ row.ratio }}%</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 用水分析 -->
    <div class="analysis-row">
      <div class="analysis-card chart-card">
        <div class="card-header">
          <div class="card-title">
            <Icon icon="mdi:water" class="icon water" /> 用水量时段分布（24小时）
          </div>
          <div class="legend">
            <span class="legend-item"><span class="dot today water"></span> 今日</span>
            <span class="legend-item"><span class="dot yesterday"></span> 昨日</span>
          </div>
        </div>
        <div ref="waterChartRef" class="period-chart"></div>
      </div>
      
      <div class="analysis-card table-card">
        <div class="card-header">
          <div class="card-title">
            <Icon icon="mdi:water" class="icon water" /> 用水高峰统计
          </div>
        </div>
        <el-table :data="waterPeriodData" stripe class="dark-table period-table">
          <el-table-column label="时段" prop="type" width="100">
            <template #default="{ row }">
              <span class="period-type water">{{ row.type }}</span>
            </template>
          </el-table-column>
          <el-table-column label="时间范围" prop="range" width="120" />
          <el-table-column label="用水量(m³)" prop="usage" align="right">
            <template #default="{ row }">
              <span class="usage-value">{{ formatNumber(row.usage) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" prop="ratio" width="80" align="right">
            <template #default="{ row }">{{ row.ratio }}%</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 燃气分析 -->
    <div class="analysis-row">
      <div class="analysis-card chart-card">
        <div class="card-header">
          <div class="card-title">
            <Icon icon="mdi:fire" class="icon gas" /> 燃气用量时段分布（24小时）
          </div>
          <div class="legend">
            <span class="legend-item"><span class="dot today gas"></span> 今日</span>
            <span class="legend-item"><span class="dot yesterday"></span> 昨日</span>
          </div>
        </div>
        <div ref="gasChartRef" class="period-chart"></div>
      </div>
      
      <div class="analysis-card table-card">
        <div class="card-header">
          <div class="card-title">
            <Icon icon="mdi:fire" class="icon gas" /> 燃气使用高峰统计
          </div>
        </div>
        <el-table :data="gasPeriodData" stripe class="dark-table period-table">
          <el-table-column label="时段" prop="type" width="100" />
          <el-table-column label="时间范围" prop="range" width="120" />
          <el-table-column label="用量(m³)" prop="usage" align="right">
            <template #default="{ row }">
              <span class="usage-value">{{ formatNumber(row.usage) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" prop="ratio" width="80" align="right">
            <template #default="{ row }">{{ row.ratio }}%</template>
          </el-table-column>
          <el-table-column label="用途" prop="purpose" width="100">
            <template #default="{ row }">
              <span class="purpose-tag">{{ row.purpose }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import echarts from '@/plugins/echarts'

defineOptions({ name: 'BuildingEnergyPeriodAnalysis' })

// 图表引用
const electricChartRef = ref<HTMLElement>()
const waterChartRef = ref<HTMLElement>()
const gasChartRef = ref<HTMLElement>()

let electricChartInstance: echarts.ECharts | null = null
let waterChartInstance: echarts.ECharts | null = null
let gasChartInstance: echarts.ECharts | null = null

// 用电时段数据
const electricPeriodData = reactive([
  { type: '峰时', typeClass: 'peak', range: '08:00-22:00', usage: 1890, ratio: 76.9 },
  { type: '谷时', typeClass: 'valley', range: '22:00-08:00', usage: 568, ratio: 23.1 },
  { type: '合计', typeClass: 'total', range: '24小时', usage: 2458, ratio: 100 }
])

// 用水时段数据
const waterPeriodData = reactive([
  { type: '早高峰', range: '07:00-09:00', usage: 5.2, ratio: 28 },
  { type: '午高峰', range: '11:00-13:00', usage: 6.8, ratio: 37 },
  { type: '晚高峰', range: '17:00-19:00', usage: 4.1, ratio: 22 },
  { type: '夜间', range: '22:00-06:00', usage: 2.5, ratio: 13 },
  { type: '合计', range: '24小时', usage: 18.6, ratio: 100 }
])

// 燃气时段数据
const gasPeriodData = reactive([
  { type: '早餐准备', range: '06:00-09:00', usage: 12.5, ratio: 28, purpose: '厨房烹饪' },
  { type: '午餐高峰', range: '10:00-14:00', usage: 18.2, ratio: 40, purpose: '厨房烹饪' },
  { type: '晚餐高峰', range: '16:00-20:00', usage: 11.8, ratio: 26, purpose: '厨房烹饪' },
  { type: '锅炉供暖', range: '全天', usage: 2.7, ratio: 6, purpose: '供暖保温' },
  { type: '合计', range: '24小时', usage: 45.2, ratio: 100, purpose: '-' }
])

// 工具函数
const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '0'
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

// 生成24小时数据
const generateHourlyData = (baseValue: number, variance: number = 0.3) => {
  return Array.from({ length: 24 }, (_, i) => {
    // 模拟日常负荷曲线
    let factor = 0.4
    if (i >= 8 && i <= 11) factor = 1.2 // 早高峰
    else if (i >= 12 && i <= 14) factor = 0.9 // 午休
    else if (i >= 15 && i <= 18) factor = 1.1 // 下午
    else if (i >= 19 && i <= 22) factor = 0.7 // 晚间
    else factor = 0.3 // 夜间
    
    return Math.round(baseValue * factor * (1 + (Math.random() - 0.5) * variance))
  })
}

// 渲染用电图表
const renderElectricChart = () => {
  if (!electricChartRef.value) return
  
  if (!electricChartInstance) {
    electricChartInstance = echarts.init(electricChartRef.value)
  }
  
  const hours = Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`)
  const todayData = generateHourlyData(100)
  const yesterdayData = generateHourlyData(95)
  
  electricChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 30, 0.9)',
      borderColor: '#404040',
      textStyle: { color: '#fff' }
    },
    grid: { top: 10, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#3a3a4a' } },
      axisLabel: { color: '#6b7280', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#6b7280' },
      splitLine: { lineStyle: { color: '#2d2d3a' } }
    },
    series: [
      {
        name: '今日',
        type: 'bar',
        data: todayData,
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f59e0b' },
            { offset: 1, color: '#f59e0b33' }
          ])
        }
      },
      {
        name: '昨日',
        type: 'line',
        data: yesterdayData,
        smooth: true,
        lineStyle: { color: '#6b7280', width: 2 },
        symbol: 'none'
      }
    ]
  })
}

// 渲染用水图表
const renderWaterChart = () => {
  if (!waterChartRef.value) return
  
  if (!waterChartInstance) {
    waterChartInstance = echarts.init(waterChartRef.value)
  }
  
  const hours = Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`)
  const todayData = generateHourlyData(0.8)
  const yesterdayData = generateHourlyData(0.75)
  
  waterChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 30, 0.9)',
      borderColor: '#404040',
      textStyle: { color: '#fff' }
    },
    grid: { top: 10, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#3a3a4a' } },
      axisLabel: { color: '#6b7280', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#6b7280' },
      splitLine: { lineStyle: { color: '#2d2d3a' } }
    },
    series: [
      {
        name: '今日',
        type: 'bar',
        data: todayData,
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#06b6d4' },
            { offset: 1, color: '#06b6d433' }
          ])
        }
      },
      {
        name: '昨日',
        type: 'line',
        data: yesterdayData,
        smooth: true,
        lineStyle: { color: '#6b7280', width: 2 },
        symbol: 'none'
      }
    ]
  })
}

// 渲染燃气图表
const renderGasChart = () => {
  if (!gasChartRef.value) return
  
  if (!gasChartInstance) {
    gasChartInstance = echarts.init(gasChartRef.value)
  }
  
  const hours = Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`)
  
  // 燃气曲线特点：早中晚餐时段高峰
  const generateGasData = () => {
    return Array.from({ length: 24 }, (_, i) => {
      let factor = 0.1
      if (i >= 6 && i <= 8) factor = 0.8 // 早餐
      else if (i >= 11 && i <= 13) factor = 1.2 // 午餐
      else if (i >= 17 && i <= 19) factor = 1.0 // 晚餐
      else factor = 0.1
      return Math.round(2 * factor * (1 + (Math.random() - 0.5) * 0.3) * 10) / 10
    })
  }
  
  const todayData = generateGasData()
  const yesterdayData = generateGasData()
  
  gasChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 30, 0.9)',
      borderColor: '#404040',
      textStyle: { color: '#fff' }
    },
    grid: { top: 10, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#3a3a4a' } },
      axisLabel: { color: '#6b7280', fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#6b7280' },
      splitLine: { lineStyle: { color: '#2d2d3a' } }
    },
    series: [
      {
        name: '今日',
        type: 'bar',
        data: todayData,
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f97316' },
            { offset: 1, color: '#f9731633' }
          ])
        }
      },
      {
        name: '昨日',
        type: 'line',
        data: yesterdayData,
        smooth: true,
        lineStyle: { color: '#6b7280', width: 2 },
        symbol: 'none'
      }
    ]
  })
}

// 窗口resize
const handleResize = () => {
  electricChartInstance?.resize()
  waterChartInstance?.resize()
  gasChartInstance?.resize()
}

// 生命周期
onMounted(() => {
  renderElectricChart()
  renderWaterChart()
  renderGasChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  electricChartInstance?.dispose()
  waterChartInstance?.dispose()
  gasChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-period-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.analysis-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.analysis-card {
  background: #252532;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #3a3a4a;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #ffffff;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .icon {
        font-size: 18px;
        
        &.electric { color: #fbbf24; }
        &.water { color: #22d3ee; }
        &.gas { color: #fb923c; }
      }
    }
    
    .legend {
      display: flex;
      gap: 16px;
      
      .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #a0a0b0;
        
        .dot {
          width: 12px;
          height: 12px;
          border-radius: 2px;
          
          &.today {
            background: #f59e0b;
            
            &.water { background: #06b6d4; }
            &.gas { background: #f97316; }
          }
          
          &.yesterday {
            background: #6b7280;
          }
        }
      }
    }
  }
  
  &.chart-card {
    .period-chart {
      height: 280px;
    }
  }
  
  &.table-card {
    .period-table {
      --el-table-bg-color: transparent;
      --el-table-header-bg-color: #1a1a2e;
      --el-table-tr-bg-color: transparent;
      --el-table-row-hover-bg-color: #2d2d3a;
      --el-table-border-color: #3a3a4a;
      --el-table-text-color: #ffffff;
      --el-table-header-text-color: #a0a0b0;
      
      :deep(.el-table__row:last-child) {
        background: #1a1a2e !important;
        font-weight: 700;
      }
    }
    
    .period-type {
      font-weight: 600;
      
      &.peak { color: #f87171; }
      &.valley { color: #60a5fa; }
      &.total { color: #ffffff; }
      &.water { color: #22d3ee; }
    }
    
    .usage-value {
      font-weight: 600;
      font-family: 'Courier New', monospace;
    }
    
    .purpose-tag {
      font-size: 12px;
      color: #a0a0b0;
    }
  }
}
</style>
