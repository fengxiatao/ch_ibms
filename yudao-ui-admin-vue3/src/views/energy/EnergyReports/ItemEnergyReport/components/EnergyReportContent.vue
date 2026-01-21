<template>
  <div class="energy-report-content dark-theme-page">
    <!-- 查询条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="80px" :inline="true">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="统计粒度">
          <el-select v-model="filterForm.granularity" placeholder="请选择">
            <el-option label="小时" value="hour" />
            <el-option label="日" value="day" />
            <el-option label="月" value="month" />
            <el-option label="年" value="year" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRefresh">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 概览卡片 -->
    <div class="summary-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="summary-card total">
            <div class="summary-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-title">总{{ getUnitName() }}耗</div>
              <div class="summary-value">{{ formatNumber(defaultData.summary.totalConsumption) }}</div>
              <div class="summary-unit">{{ defaultData.summary.unit }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-card average">
            <div class="summary-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-title">平均单价</div>
              <div class="summary-value">{{ formatNumber(defaultData.summary.averagePrice) }}</div>
              <div class="summary-unit">元/{{ defaultData.summary.unit }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-card cost">
            <div class="summary-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-title">总费用</div>
              <div class="summary-value">{{ formatNumber(defaultData.summary.totalCost) }}</div>
              <div class="summary-unit">元</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-card trend">
            <div class="summary-icon">
              <el-icon><Top /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-title">较上期</div>
              <div class="summary-value" :class="defaultData.summary.trendType">
                {{ defaultData.summary.trendValue }}%
              </div>
              <div class="summary-unit">
                <el-icon v-if="defaultData.summary.trendType === 'up'"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="chart-header">
                <span>{{ getUnitName() }}耗分布</span>
                <el-button link type="primary" @click="toggleChartType('pie')">
                  <el-icon><View /></el-icon>
                  饼图
                </el-button>
              </div>
            </template>
            <div ref="pieChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="chart-header">
                <span>{{ getUnitName() }}耗趋势</span>
                <el-button link type="primary" @click="toggleChartType('line')">
                  <el-icon><DataAnalysis /></el-icon>
                  趋势图
                </el-button>
              </div>
            </template>
            <div ref="lineChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>{{ getUnitName() }}耗详细数据</span>
            <div class="table-actions">
              <el-button size="small" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增
              </el-button>
              <el-button size="small" type="success" @click="handleBatchExport">
                <el-icon><Document /></el-icon>
                批量导出
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table v-loading="loading" :data="defaultData.details" stripe>
          <el-table-column type="selection" width="50" />
          <el-table-column prop="time" label="时间" width="160" />
          <el-table-column prop="consumption" :label="`${getUnitName()}耗量`" width="120">
            <template #default="{ row }">
              {{ formatNumber(row.consumption) }} {{ defaultData.summary.unit }}
            </template>
          </el-table-column>
          <el-table-column prop="cost" label="费用(元)" width="120">
            <template #default="{ row }">
              ¥{{ formatNumber(row.cost) }}
            </template>
          </el-table-column>
          <el-table-column prop="devices" label="主要设备" min-width="200">
            <template #default="{ row }">
              <el-tag v-for="device in row.devices.slice(0, 3)" :key="device" size="small" style="margin-right: 5px;">
                {{ device }}
              </el-tag>
              <el-tag v-if="row.devices.length > 3" size="small" type="info">
                +{{ row.devices.length - 3 }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="warning" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right;"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Download, 
  Document,
  DataAnalysis,
  Money,
  Top,
  Plus,
  View,
  Edit,
  CaretTop,
  CaretBottom
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// Props
interface Props {
  reportType: string
  data?: any
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ({
    summary: {
      totalConsumption: 0,
      averagePrice: 0,
      totalCost: 0,
      trendValue: 0,
      trendType: 'up',
      unit: 'kWh'
    },
    details: []
  }),
  loading: false
})

// Emits
interface Emits {
  refresh: []
  export: [type: string]
}

const emit = defineEmits<Emits>()

// 响应式数据
const pieChartRef = ref()
const lineChartRef = ref()
let pieChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null

const filterForm = reactive({
  dateRange: [],
  granularity: 'day'
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 默认数据
const defaultData = reactive({
  summary: {
    totalConsumption: 125680,
    averagePrice: 1.35,
    totalCost: 169668,
    trendValue: 8.5,
    trendType: 'up',
    unit: getUnitByType(props.reportType)
  },
  details: [
    {
      time: '2024-01-22 00:00',
      consumption: 12500,
      cost: 16875,
      devices: ['空调系统-A1', '照明系统-L1', '电梯-E1', 'UPS-U1']
    },
    {
      time: '2024-01-21 00:00', 
      consumption: 11800,
      cost: 15930,
      devices: ['空调系统-A2', '照明系统-L2', '电梯-E2']
    },
    {
      time: '2024-01-20 00:00',
      consumption: 13200,
      cost: 17820,
      devices: ['空调系统-A3', '照明系统-L3']
    }
  ]
})

// 方法
function getUnitByType(type: string) {
  const units = {
    electricity: 'kWh',
    water: 'm³',
    gas: 'm³',
    heat: 'GJ',
    cooling: 'GJ'
  }
  return units[type] || 'kWh'
}

function getUnitName() {
  const names = {
    electricity: '电',
    water: '水',
    gas: '燃气',
    heat: '热量',
    cooling: '冷量'
  }
  return names[props.reportType] || '能'
}

function formatNumber(num: number | undefined | null) {
  if (num === undefined || num === null) {
    return '0'
  }
  return num.toLocaleString()
}

function handleRefresh() {
  emit('refresh')
}

function handleExport() {
  emit('export', 'excel')
}

function handleBatchExport() {
  emit('export', 'batch')
}

function handleAdd() {
  ElMessage.info('新增功能开发中...')
}

function handleView(row: any) {
  ElMessage.info(`查看详情: ${row.time}`)
}

function handleEdit(row: any) {
  ElMessage.info(`编辑: ${row.time}`)
}

function toggleChartType(type: string) {
  if (type === 'pie') {
    initPieChart()
  } else if (type === 'line') {
    initLineChart()
  }
}

function initPieChart() {
  if (!pieChartRef.value) return
  
  if (pieChart) {
    pieChart.dispose()
  }
  
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: `${getUnitName()}耗分布`,
        type: 'pie',
        radius: '50%',
        data: [
          { value: 35, name: '空调系统' },
          { value: 25, name: '照明系统' },
          { value: 20, name: '电梯系统' },
          { value: 12, name: 'UPS系统' },
          { value: 8, name: '其他设备' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  pieChart.setOption(option)
}

function initLineChart() {
  if (!lineChartRef.value) return
  
  if (lineChart) {
    lineChart.dispose()
  }
  
  lineChart = echarts.init(lineChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: [`${getUnitName()}耗量`, '费用']
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    },
    yAxis: [
      {
        type: 'value',
        name: `${getUnitName()}耗量`,
        position: 'left'
      },
      {
        type: 'value',
        name: '费用(元)',
        position: 'right'
      }
    ],
    series: [
      {
        name: `${getUnitName()}耗量`,
        type: 'line',
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        smooth: true
      },
      {
        name: '费用',
        type: 'line',
        yAxisIndex: 1,
        data: [1107, 1258, 1216, 1261, 1742, 1795, 1782],
        smooth: true
      }
    ]
  }
  
  lineChart.setOption(option)
}

// 监听数据变化
watch(() => props.data, (newData) => {
  if (newData && newData.summary) {
    Object.assign(defaultData, newData)
    pagination.total = newData.details?.length || defaultData.details.length
  }
}, { immediate: true })

// 生命周期
onMounted(async () => {
  await nextTick()
  initPieChart()
  initLineChart()
  
  // 响应式调整
  window.addEventListener('resize', () => {
    pieChart?.resize()
    lineChart?.resize()
  })
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.energy-report-content {
  .filter-section {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .summary-section {
    margin-bottom: 20px;

    .summary-card {
      background: #1a1a1a;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      display: flex;
      align-items: center;
      height: 100px;

      .summary-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        font-size: 24px;
        color: #fff;

        &.total {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
      }

      .summary-info {
        flex: 1;

        .summary-title {
          font-size: 14px;
          color: #909399;
          margin-bottom: 5px;
        }

        .summary-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          line-height: 1;

          &.up {
            color: #f56c6c;
          }

          &.down {
            color: #67c23a;
          }
        }

        .summary-unit {
          font-size: 12px;
          color: #909399;
          margin-top: 2px;
        }
      }

      &.total .summary-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.average .summary-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.cost .summary-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.trend .summary-icon {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }
  }

  .chart-section {
    margin-bottom: 20px;

    .chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
      }

      .chart-container {
        height: 300px;
      }
    }
  }

  .table-section {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;

      .table-actions {
        display: flex;
        gap: 10px;
      }
    }
  }
}
</style>
