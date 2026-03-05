<template>
  <div class="energy-report-page dark-theme-page">
    <!-- 报表Tab -->
    <div class="report-tabs">
      <button
        v-for="tab in reportTabs"
        :key="tab.value"
        class="report-tab"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 日报 -->
    <div v-show="activeTab === 'daily'" class="report-content">
      <div class="stats-grid">
        <div class="stat-card electric">
          <div class="stat-label">今日用电</div>
          <div class="stat-value">{{ formatNumber(dailyStats.electricity) }}<span class="stat-unit">kWh</span></div>
        </div>
        <div class="stat-card water">
          <div class="stat-label">今日用水</div>
          <div class="stat-value">{{ formatNumber(dailyStats.water) }}<span class="stat-unit">m³</span></div>
        </div>
        <div class="stat-card gas">
          <div class="stat-label">今日用气</div>
          <div class="stat-value">{{ formatNumber(dailyStats.gas) }}<span class="stat-unit">m³</span></div>
        </div>
        <div class="stat-card cost">
          <div class="stat-label">今日费用</div>
          <div class="stat-value">¥{{ formatNumber(dailyStats.cost) }}</div>
        </div>
      </div>
      
      <div class="table-section">
        <div class="table-header">
          <div class="table-title">
            <Icon icon="mdi:chart-bar" class="mr-8px" /> {{ currentDate }} 日用能明细
          </div>
          <el-button type="success" size="small" @click="handleExport('daily')">
            <Icon icon="ep:download" class="mr-5px" /> 导出日报
          </el-button>
        </div>
        <el-table :data="dailyDetailData" stripe border class="dark-table">
          <el-table-column label="时间段" prop="period" width="140" align="center" />
          <el-table-column label="用电量(kWh)" prop="electricity" align="right">
            <template #default="{ row }">{{ formatNumber(row.electricity) }}</template>
          </el-table-column>
          <el-table-column label="用水量(m³)" prop="water" align="right">
            <template #default="{ row }">{{ formatNumber(row.water) }}</template>
          </el-table-column>
          <el-table-column label="燃气量(m³)" prop="gas" align="right">
            <template #default="{ row }">{{ formatNumber(row.gas) }}</template>
          </el-table-column>
          <el-table-column label="费用(元)" prop="cost" align="right">
            <template #default="{ row }">¥{{ formatNumber(row.cost) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 周报 -->
    <div v-show="activeTab === 'weekly'" class="report-content">
      <div class="stats-grid">
        <div class="stat-card electric">
          <div class="stat-label">本周用电</div>
          <div class="stat-value">{{ formatNumber(weeklyStats.electricity) }}<span class="stat-unit">kWh</span></div>
        </div>
        <div class="stat-card water">
          <div class="stat-label">本周用水</div>
          <div class="stat-value">{{ formatNumber(weeklyStats.water) }}<span class="stat-unit">m³</span></div>
        </div>
        <div class="stat-card gas">
          <div class="stat-label">本周用气</div>
          <div class="stat-value">{{ formatNumber(weeklyStats.gas) }}<span class="stat-unit">m³</span></div>
        </div>
        <div class="stat-card cost">
          <div class="stat-label">本周费用</div>
          <div class="stat-value">¥{{ formatNumber(weeklyStats.cost) }}</div>
        </div>
      </div>
      
      <div class="chart-section">
        <div class="chart-header">
          <div class="chart-title">本周能耗趋势</div>
        </div>
        <div ref="weeklyChartRef" class="report-chart"></div>
      </div>
    </div>

    <!-- 月报 -->
    <div v-show="activeTab === 'monthly'" class="report-content">
      <div class="stats-grid">
        <div class="stat-card electric">
          <div class="stat-label">本月用电</div>
          <div class="stat-value">{{ formatNumber(monthlyStats.electricity) }}<span class="stat-unit">kWh</span></div>
          <div class="stat-change" :class="{ up: monthlyStats.electricityChange > 0, down: monthlyStats.electricityChange < 0 }">
            环比 {{ monthlyStats.electricityChange > 0 ? '+' : '' }}{{ monthlyStats.electricityChange }}%
          </div>
        </div>
        <div class="stat-card water">
          <div class="stat-label">本月用水</div>
          <div class="stat-value">{{ formatNumber(monthlyStats.water) }}<span class="stat-unit">m³</span></div>
          <div class="stat-change" :class="{ up: monthlyStats.waterChange > 0, down: monthlyStats.waterChange < 0 }">
            环比 {{ monthlyStats.waterChange > 0 ? '+' : '' }}{{ monthlyStats.waterChange }}%
          </div>
        </div>
        <div class="stat-card gas">
          <div class="stat-label">本月用气</div>
          <div class="stat-value">{{ formatNumber(monthlyStats.gas) }}<span class="stat-unit">m³</span></div>
          <div class="stat-change" :class="{ up: monthlyStats.gasChange > 0, down: monthlyStats.gasChange < 0 }">
            环比 {{ monthlyStats.gasChange > 0 ? '+' : '' }}{{ monthlyStats.gasChange }}%
          </div>
        </div>
        <div class="stat-card cost">
          <div class="stat-label">本月费用</div>
          <div class="stat-value">¥{{ formatNumber(monthlyStats.cost) }}</div>
          <div class="stat-change" :class="{ up: monthlyStats.costChange > 0, down: monthlyStats.costChange < 0 }">
            环比 {{ monthlyStats.costChange > 0 ? '+' : '' }}{{ monthlyStats.costChange }}%
          </div>
        </div>
      </div>
      
      <div class="chart-section">
        <div class="chart-header">
          <div class="chart-title">本月能耗趋势</div>
          <el-button type="success" size="small" @click="handleExport('monthly')">
            <Icon icon="ep:download" class="mr-5px" /> 导出月报
          </el-button>
        </div>
        <div ref="monthlyChartRef" class="report-chart"></div>
      </div>
    </div>

    <!-- 费用报表 -->
    <div v-show="activeTab === 'cost'" class="report-content">
      <div class="filter-section">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="costDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="!w-280px"
            />
          </el-form-item>
          <el-form-item label="能源类型">
            <el-select v-model="costEnergyType" placeholder="全部类型" clearable class="!w-120px">
              <el-option label="全部类型" :value="undefined" />
              <el-option label="电费" :value="1" />
              <el-option label="水费" :value="2" />
              <el-option label="燃气费" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadCostData">查询</el-button>
            <el-button type="success" @click="handleExport('cost')">
              <Icon icon="ep:download" class="mr-5px" /> 导出报表
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="cost-summary">
        <div class="summary-item">
          <span class="summary-label">总费用</span>
          <span class="summary-value">¥{{ formatNumber(costSummary.total) }}</span>
        </div>
        <div class="summary-item electric">
          <span class="summary-label">电费</span>
          <span class="summary-value">¥{{ formatNumber(costSummary.electricity) }}</span>
        </div>
        <div class="summary-item water">
          <span class="summary-label">水费</span>
          <span class="summary-value">¥{{ formatNumber(costSummary.water) }}</span>
        </div>
        <div class="summary-item gas">
          <span class="summary-label">燃气费</span>
          <span class="summary-value">¥{{ formatNumber(costSummary.gas) }}</span>
        </div>
      </div>
      
      <div class="table-section">
        <el-table :data="costDetailData" stripe border class="dark-table">
          <el-table-column label="日期" prop="date" width="140" align="center" />
          <el-table-column label="电费(元)" prop="electricityCost" align="right">
            <template #default="{ row }">¥{{ formatNumber(row.electricityCost) }}</template>
          </el-table-column>
          <el-table-column label="水费(元)" prop="waterCost" align="right">
            <template #default="{ row }">¥{{ formatNumber(row.waterCost) }}</template>
          </el-table-column>
          <el-table-column label="燃气费(元)" prop="gasCost" align="right">
            <template #default="{ row }">¥{{ formatNumber(row.gasCost) }}</template>
          </el-table-column>
          <el-table-column label="合计(元)" prop="totalCost" align="right">
            <template #default="{ row }">
              <span class="total-cost">¥{{ formatNumber(row.totalCost) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as EnergyApi from '@/api/iot/building/energy'
import * as echarts from 'echarts'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'BuildingEnergyReport' })

const message = useMessage()

// Tab
const activeTab = ref('daily')
const reportTabs = [
  { label: '日报', value: 'daily' },
  { label: '周报', value: 'weekly' },
  { label: '月报', value: 'monthly' },
  { label: '费用报表', value: 'cost' }
]

// 图表引用
const weeklyChartRef = ref<HTMLElement>()
const monthlyChartRef = ref<HTMLElement>()
let weeklyChartInstance: echarts.ECharts | null = null
let monthlyChartInstance: echarts.ECharts | null = null

// 当前日期
const currentDate = new Date().toISOString().split('T')[0]

// 日报数据
const dailyStats = reactive({
  electricity: 2458,
  water: 18.6,
  gas: 45.2,
  cost: 3240
})

const dailyDetailData = reactive([
  { period: '00:00-04:00', electricity: 180, water: 0.5, gas: 2.1, cost: 216 },
  { period: '04:00-08:00', electricity: 220, water: 2.1, gas: 8.5, cost: 298 },
  { period: '08:00-12:00', electricity: 680, water: 6.2, gas: 12.4, cost: 856 },
  { period: '12:00-16:00', electricity: 620, water: 4.8, gas: 10.2, cost: 768 },
  { period: '16:00-20:00', electricity: 580, water: 3.5, gas: 9.8, cost: 712 },
  { period: '20:00-24:00', electricity: 178, water: 1.5, gas: 2.2, cost: 390 }
])

// 周报数据
const weeklyStats = reactive({
  electricity: 17206,
  water: 130.2,
  gas: 316.4,
  cost: 22680
})

// 月报数据
const monthlyStats = reactive({
  electricity: 52400,
  water: 580,
  gas: 1420,
  cost: 68520,
  electricityChange: 12,
  waterChange: -3,
  gasChange: 5,
  costChange: 8.5
})

// 费用报表数据
const costDateRange = ref<[string, string] | null>(null)
const costEnergyType = ref<number | undefined>()
const costSummary = reactive({
  total: 68520,
  electricity: 52400,
  water: 5800,
  gas: 10320
})
const costDetailData = reactive([
  { date: '2026-01-30', electricityCost: 1720, waterCost: 186, gasCost: 342, totalCost: 2248 },
  { date: '2026-01-29', electricityCost: 1650, waterCost: 178, gasCost: 325, totalCost: 2153 },
  { date: '2026-01-28', electricityCost: 1580, waterCost: 192, gasCost: 318, totalCost: 2090 },
  { date: '2026-01-27', electricityCost: 1690, waterCost: 168, gasCost: 335, totalCost: 2193 },
  { date: '2026-01-26', electricityCost: 1720, waterCost: 182, gasCost: 348, totalCost: 2250 }
])

// 工具函数
const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '0'
  return value.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
}

// 切换Tab
const switchTab = async (tab: string) => {
  activeTab.value = tab
  await nextTick()
  if (tab === 'weekly') renderWeeklyChart()
  if (tab === 'monthly') renderMonthlyChart()
}

// 渲染周报图表
const renderWeeklyChart = () => {
  if (!weeklyChartRef.value) return
  
  if (!weeklyChartInstance) {
    weeklyChartInstance = echarts.init(weeklyChartRef.value)
  }
  
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  weeklyChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 30, 0.9)',
      borderColor: '#404040',
      textStyle: { color: '#fff' }
    },
    legend: {
      data: ['用电量(kWh)', '用水量(m³)', '燃气量(m³)'],
      textStyle: { color: '#a0a0b0' },
      top: 0
    },
    grid: { top: 40, right: 60, bottom: 30, left: 60 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#3a3a4a' } },
      axisLabel: { color: '#a0a0b0' }
    },
    yAxis: [
      { type: 'value', name: 'kWh', axisLabel: { color: '#a0a0b0' }, splitLine: { lineStyle: { color: '#2d2d3a' } } },
      { type: 'value', name: 'm³', axisLabel: { color: '#a0a0b0' }, splitLine: { show: false } }
    ],
    series: [
      { name: '用电量(kWh)', type: 'bar', data: [2458, 2350, 2480, 2520, 2380, 2100, 1918], itemStyle: { color: '#f59e0b' } },
      { name: '用水量(m³)', type: 'line', yAxisIndex: 1, data: [18.6, 17.8, 19.2, 18.5, 18.9, 16.2, 21], itemStyle: { color: '#06b6d4' } },
      { name: '燃气量(m³)', type: 'line', yAxisIndex: 1, data: [45.2, 43.8, 46.5, 44.2, 45.8, 42.1, 48.8], itemStyle: { color: '#f97316' } }
    ]
  })
}

// 渲染月报图表
const renderMonthlyChart = () => {
  if (!monthlyChartRef.value) return
  
  if (!monthlyChartInstance) {
    monthlyChartInstance = echarts.init(monthlyChartRef.value)
  }
  
  const days = Array.from({ length: 30 }, (_, i) => `${i + 1}日`)
  const electricityData = Array.from({ length: 30 }, () => Math.round(2000 + Math.random() * 800))
  const waterData = Array.from({ length: 30 }, () => Math.round((15 + Math.random() * 8) * 10) / 10)
  
  monthlyChartInstance.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 30, 30, 0.9)',
      borderColor: '#404040',
      textStyle: { color: '#fff' }
    },
    legend: {
      data: ['用电量(kWh)', '用水量(m³)'],
      textStyle: { color: '#a0a0b0' },
      top: 0
    },
    grid: { top: 40, right: 60, bottom: 30, left: 60 },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#3a3a4a' } },
      axisLabel: { color: '#a0a0b0', fontSize: 10 }
    },
    yAxis: [
      { type: 'value', name: 'kWh', axisLabel: { color: '#a0a0b0' }, splitLine: { lineStyle: { color: '#2d2d3a' } } },
      { type: 'value', name: 'm³', axisLabel: { color: '#a0a0b0' }, splitLine: { show: false } }
    ],
    series: [
      { name: '用电量(kWh)', type: 'bar', data: electricityData, itemStyle: { color: '#f59e0b' } },
      { name: '用水量(m³)', type: 'line', yAxisIndex: 1, data: waterData, itemStyle: { color: '#06b6d4' }, smooth: true }
    ]
  })
}

// 加载费用数据
const loadCostData = () => {
  message.success('查询成功')
}

// 加载总览数据
const loadOverview = async () => {
  try {
    const overview = await EnergyApi.getOverview()
    
    dailyStats.electricity = overview.todayElectricity || 0
    dailyStats.water = overview.todayWater || 0
    dailyStats.gas = overview.todayGas || 0
    
    monthlyStats.electricity = overview.monthElectricity || 0
    monthlyStats.water = overview.monthWater || 0
    monthlyStats.gas = overview.monthGas || 0
    monthlyStats.electricityChange = overview.electricityMom || 0
    monthlyStats.waterChange = overview.waterMom || 0
  } catch (e) {
    console.error('加载总览数据失败', e)
  }
}

// 导出
const handleExport = (type: string) => {
  message.success(`${type}报表导出功能开发中...`)
}

// 窗口resize
const handleResize = () => {
  weeklyChartInstance?.resize()
  monthlyChartInstance?.resize()
}

// 生命周期
onMounted(() => {
  loadOverview()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  weeklyChartInstance?.dispose()
  monthlyChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss' as *;

.energy-report-page {
  padding: 20px;
  padding-top: calc(
    20px + max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 20px)))
  );
  height: 100%;
  box-sizing: border-box;
  overflow: auto;
  background: #1a1a2e;
}

.report-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  background: #252532;
  padding: 4px;
  border-radius: 8px;
  border: 1px solid #3a3a4a;
  
  .report-tab {
    flex: 1;
    padding: 12px 24px;
    border: none;
    background: transparent;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    color: #a0a0b0;
    border-radius: 6px;
    transition: all 0.3s;
    
    &:hover:not(.active) {
      background: #2d2d3a;
    }
    
    &.active {
      background: #10b981;
      color: #ffffff;
    }
  }
}

.report-content {
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 20px;
    
    .stat-card {
      background: #252532;
      border-radius: 12px;
      padding: 20px;
      border: 1px solid #3a3a4a;
      border-left: 4px solid #10b981;
      
      &.electric { border-left-color: #f59e0b; }
      &.water { border-left-color: #06b6d4; }
      &.gas { border-left-color: #f97316; }
      &.cost { border-left-color: #10b981; }
      
      .stat-label {
        font-size: 13px;
        color: #a0a0b0;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 26px;
        font-weight: 700;
        color: #ffffff;
        font-family: 'Courier New', monospace;
        
        .stat-unit {
          font-size: 13px;
          font-weight: normal;
          color: #6b7280;
          margin-left: 4px;
        }
      }
      
      .stat-change {
        font-size: 12px;
        margin-top: 8px;
        
        &.up { color: #f87171; }
        &.down { color: #4ade80; }
      }
    }
  }
  
  .table-section, .chart-section {
    background: #252532;
    border-radius: 12px;
    padding: 20px;
    border: 1px solid #3a3a4a;
    margin-bottom: 20px;
    
    .table-header, .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #3a3a4a;
      
      .table-title, .chart-title {
        font-size: 15px;
        font-weight: 600;
        color: #ffffff;
        display: flex;
        align-items: center;
      }
    }
    
    .dark-table {
      --el-table-bg-color: transparent;
      --el-table-header-bg-color: #1a1a2e;
      --el-table-tr-bg-color: transparent;
      --el-table-row-hover-bg-color: #2d2d3a;
      --el-table-border-color: #3a3a4a;
      --el-table-text-color: #ffffff;
      --el-table-header-text-color: #a0a0b0;
    }
    
    .report-chart {
      height: 320px;
    }
  }
  
  .filter-section {
    background: #252532;
    border-radius: 8px;
    padding: 16px 20px;
    margin-bottom: 16px;
    border: 1px solid #3a3a4a;
    
    .filter-form {
      :deep(.el-form-item__label) {
        color: #a0a0b0;
      }
    }
  }
  
  .cost-summary {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;
    
    .summary-item {
      flex: 1;
      background: #252532;
      border-radius: 8px;
      padding: 16px;
      border: 1px solid #3a3a4a;
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .summary-label {
        font-size: 13px;
        color: #a0a0b0;
        margin-bottom: 8px;
      }
      
      .summary-value {
        font-size: 20px;
        font-weight: 700;
        color: #ffffff;
        font-family: 'Courier New', monospace;
      }
      
      &.electric .summary-value { color: #fbbf24; }
      &.water .summary-value { color: #22d3ee; }
      &.gas .summary-value { color: #fb923c; }
    }
  }
  
  .total-cost {
    font-weight: 700;
    color: #10b981;
  }
}

// 深色表单控件
:deep(.el-select), :deep(.el-date-picker) {
  .el-select__wrapper, .el-input__wrapper {
    background: #1a1a2e !important;
    border-color: #3a3a4a !important;
    box-shadow: none !important;
  }
}
</style>
