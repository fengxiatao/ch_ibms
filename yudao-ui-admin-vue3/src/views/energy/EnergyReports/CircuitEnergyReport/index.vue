<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="circuit-energy-report dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗报表</el-breadcrumb-item>
        <el-breadcrumb-item>回路能耗报表</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
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
        <el-form-item label="能源类型">
          <el-select v-model="filterForm.energyType" placeholder="请选择">
            <el-option label="电力" value="electricity" />
            <el-option label="水量" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="热量" value="heat" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属楼宇">
          <el-select v-model="selectedBuilding" placeholder="选择楼宇" style="width: 150px">
            <el-option label="全部楼宇" value="" />
            <el-option label="A栋" value="building_a" />
            <el-option label="B栋" value="building_b" />
            <el-option label="C栋" value="building_c" />
          </el-select>
        </el-form-item>
        <el-form-item label="回路状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="异常" value="abnormal" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 概览统计 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in overviewData" :key="item.title">
          <div class="overview-card">
            <div class="card-icon" :style="{ backgroundColor: item.color }">
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">{{ item.title }}</div>
              <div class="card-value">{{ item.value }}</div>
              <div class="card-unit">{{ item.unit }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表分析 -->
    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="chart-header">
                <span>回路能耗趋势</span>
                <el-button link type="primary" @click="refreshChart">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            <div ref="lineChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="chart-header">
                <span>回路能耗分布</span>
                <el-button link type="primary" @click="refreshPieChart">
                  <el-icon><DataAnalysis /></el-icon>
                  分析
                </el-button>
              </div>
            </template>
            <div ref="pieChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>回路能耗详细数据</span>
            <div class="header-actions">
              <el-button size="small" type="primary">
                <el-icon><Plus /></el-icon>
                新增回路
              </el-button>
              <el-button size="small" type="warning">
                <el-icon><Monitor /></el-icon>
                实时监控
              </el-button>
            </div>
          </div>
        </template>

        <el-table v-loading="loading" :data="tableData" stripe border>
          <el-table-column type="selection" width="50" />
          <el-table-column prop="circuitName" label="回路名称" min-width="150">
            <template #default="{ row }">
              <div class="circuit-info">
                <el-icon class="circuit-icon"><Connection /></el-icon>
                <span>{{ row.circuitName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="circuitCode" label="回路编号" width="120" />
          <el-table-column prop="energyType" label="能源类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getEnergyTypeColor(row.energyType)">
                {{ getEnergyTypeName(row.energyType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="building" label="所属楼宇" width="100" />
          <el-table-column prop="consumption" label="能耗量" width="120">
            <template #default="{ row }">
              {{ row.consumption.toLocaleString() }} {{ getUnit(row.energyType) }}
            </template>
          </el-table-column>
          <el-table-column prop="cost" label="费用(元)" width="120">
            <template #default="{ row }">
              ¥{{ row.cost.toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column prop="load" label="负载率" width="100">
            <template #default="{ row }">
              <div class="load-info">
                <span>{{ row.load }}%</span>
                <el-progress :percentage="row.load" :color="getLoadColor(row.load)" size="small" :show-text="false" />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button link type="success" @click="handleChart(row)">
                <el-icon><DataAnalysis /></el-icon>
                图表
              </el-button>
              <el-button link type="warning" @click="handleControl(row)">
                <el-icon><Setting /></el-icon>
                控制
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
          @size-change="loadData"
          @current-change="loadData"
        />
      </el-card>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Download,
  Monitor,
  View,
  DataAnalysis,
  Setting,
  Warning,
  CircleCheck,
  Connection,
  Lightning,
  Plus,
  Stopwatch
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const lineChartRef = ref()
const pieChartRef = ref()
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const selectedBuilding = ref('building_a')

const filterForm = reactive({
  dateRange: [],
  energyType: 'electricity',
  building: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 概览数据
const overviewData = ref([
  {
    title: '总回路数',
    value: '156',
    unit: '个',
    icon: Connection,
    color: '#3b82f6'
  },
  {
    title: '正常回路',
    value: '142',
    unit: '个',
    icon: CircleCheck,
    color: '#10b981'
  },
  {
    title: '异常回路',
    value: '8',
    unit: '个',
    icon: Warning,
    color: '#f59e0b'
  },
  {
    title: '平均负载率',
    value: '68.5',
    unit: '%',
    icon: Stopwatch,
    color: '#8b5cf6'
  }
])

// 表格数据
const tableData = ref([
  {
    circuitName: '主配电室-A栋总路',
    circuitCode: 'CIR001',
    energyType: 'electricity',
    building: 'A栋',
    consumption: 45200,
    cost: 61020,
    load: 75,
    status: 'normal'
  },
  {
    circuitName: 'A栋1楼照明回路',
    circuitCode: 'CIR002', 
    energyType: 'electricity',
    building: 'A栋',
    consumption: 12800,
    cost: 17280,
    load: 62,
    status: 'normal'
  },
  {
    circuitName: 'A栋空调系统回路',
    circuitCode: 'CIR003',
    energyType: 'electricity',
    building: 'A栋',
    consumption: 28900,
    cost: 39015,
    load: 88,
    status: 'abnormal'
  },
  {
    circuitName: 'B栋给水总管',
    circuitCode: 'CIR004',
    energyType: 'water',
    building: 'B栋',
    consumption: 1580,
    cost: 6478,
    load: 45,
    status: 'normal'
  },
  {
    circuitName: 'C栋燃气主管路',
    circuitCode: 'CIR005',
    energyType: 'gas',
    building: 'C栋',
    consumption: 890,
    cost: 3560,
    load: 32,
    status: 'disabled'
  }
])

// 方法
const getUnit = (energyType: string) => {
  const units = {
    electricity: 'kWh',
    water: 'm³',
    gas: 'm³',
    heat: 'GJ'
  }
  return units[energyType] || 'kWh'
}

const getEnergyTypeColor = (type: string) => {
  const colors = {
    electricity: 'primary',
    water: 'success',
    gas: 'warning',
    heat: 'danger'
  }
  return colors[type] || 'info'
}

const getEnergyTypeName = (type: string) => {
  const names = {
    electricity: '电力',
    water: '水量',
    gas: '燃气',
    heat: '热量'
  }
  return names[type] || type
}

const getLoadColor = (load: number) => {
  if (load < 50) return '#67c23a'
  if (load < 80) return '#e6a23c'
  return '#f56c6c'
}

const getStatusTagType = (status: string) => {
  const types = {
    normal: 'success',
    abnormal: 'danger',
    disabled: 'info'
  }
  return types[status] || 'info'
}

const getStatusName = (status: string) => {
  const names = {
    normal: '正常',
    abnormal: '异常',
    disabled: '停用'
  }
  return names[status] || status
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loadData()
    loading.value = false
  }, 500)
}

const handleReset = () => {
  Object.assign(filterForm, {
    dateRange: [],
    energyType: 'electricity',
    building: '',
    status: ''
  })
  selectedBuilding.value = ''
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleView = (row: any) => {
  ElMessage.info(`查看回路详情: ${row.circuitName}`)
}

const handleChart = (row: any) => {
  ElMessage.info(`查看回路图表: ${row.circuitName}`)
}

const handleControl = (row: any) => {
  ElMessage.info(`控制回路: ${row.circuitName}`)
}

const refreshChart = () => {
  initLineChart()
}

const refreshPieChart = () => {
  initPieChart()
}

const initLineChart = () => {
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
      data: ['电力回路', '水量回路', '燃气回路']
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    },
    yAxis: {
      type: 'value',
      name: '能耗量'
    },
    series: [
      {
        name: '电力回路',
        type: 'line',
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        smooth: true,
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '水量回路',
        type: 'line',
        data: [220, 182, 191, 234, 290, 330, 310],
        smooth: true,
        itemStyle: { color: '#10b981' }
      },
      {
        name: '燃气回路',
        type: 'line',
        data: [150, 232, 201, 154, 190, 330, 410],
        smooth: true,
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }
  
  lineChart.setOption(option)
}

const initPieChart = () => {
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
        name: '回路能耗分布',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 45, name: '照明回路' },
          { value: 35, name: '空调回路' },
          { value: 15, name: '动力回路' },
          { value: 5, name: '其他回路' }
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

const loadData = () => {
  pagination.total = tableData.value.length
}

// 监听楼宇选择
watch(selectedBuilding, () => {
  handleSearch()
})

// 生命周期
onMounted(async () => {
  loadData()
  await nextTick()
  initLineChart()
  initPieChart()
  
  // 响应式调整
  window.addEventListener('resize', () => {
    lineChart?.resize()
    pieChart?.resize()
  })
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.circuit-energy-report {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .filter-section {
    background: #1a1a1a;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .el-form-item {
      margin-bottom: 16px;
    }
  }

  .overview-section {
    margin-bottom: 20px;

    .overview-card {
      background: #1a1a1a;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      display: flex;
      align-items: center;
      height: 100px;

      .card-icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        color: #fff;
        font-size: 20px;
      }

      .card-content {
        flex: 1;

        .card-title {
          font-size: 14px;
          color: #909399;
          margin-bottom: 5px;
        }

        .card-value {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
        }

        .card-unit {
          font-size: 12px;
          color: #909399;
        }
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

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }

    .circuit-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .circuit-icon {
        color: #409eff;
      }
    }

    .load-info {
      display: flex;
      flex-direction: column;
      gap: 4px;

      span {
        font-size: 12px;
        font-weight: 500;
      }
    }
  }
}
</style>
