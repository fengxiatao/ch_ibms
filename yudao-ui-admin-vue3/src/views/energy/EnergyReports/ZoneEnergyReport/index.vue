<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="zone-energy-report dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗报表</el-breadcrumb-item>
        <el-breadcrumb-item>分区能耗报表</el-breadcrumb-item>
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
        <el-form-item label="分区楼宇">
          <el-select v-model="selectedBuilding" placeholder="选择楼宇" style="width: 150px">
            <el-option label="全部楼宇" value="" />
            <el-option label="A栋" value="building_a" />
            <el-option label="B栋" value="building_b" />
            <el-option label="C栋" value="building_c" />
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
              <div class="card-trend" :class="item.trend > 0 ? 'trend-up' : 'trend-down'">
                <el-icon v-if="item.trend > 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
                {{ Math.abs(item.trend) }}%
              </div>
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
                <span>各分区能耗对比</span>
                <el-button link type="primary" @click="refreshChart">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            <div ref="barChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="chart-header">
                <span>分区能耗热力图</span>
                <el-button link type="primary" @click="refreshHeatmap">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-button>
              </div>
            </template>
            <div ref="heatmapChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>分区能耗详细数据</span>
            <div class="header-actions">
              <el-button size="small" type="primary">
                <el-icon><Plus /></el-icon>
                新增分区
              </el-button>
              <el-button size="small" type="success">
                <el-icon><DataAnalysis /></el-icon>
                分析报告
              </el-button>
            </div>
          </div>
        </template>

        <el-table v-loading="loading" :data="tableData" stripe border>
          <el-table-column type="selection" width="50" />
          <el-table-column prop="zoneName" label="分区名称" min-width="140">
            <template #default="{ row }">
              <div class="zone-info">
                <el-icon class="zone-icon"><Location /></el-icon>
                <span>{{ row.zoneName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="building" label="所属楼宇" width="100">
            <template #default="{ row }">
              <el-tag :type="getBuildingTagType(row.building)">
                {{ getBuildingName(row.building) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="area" label="面积(m²)" width="100" />
          <el-table-column prop="deviceCount" label="设备数量" width="100" />
          <el-table-column prop="consumption" label="能耗量" width="120">
            <template #default="{ row }">
              {{ row.consumption.toLocaleString() }} {{ getUnit() }}
            </template>
          </el-table-column>
          <el-table-column prop="cost" label="费用(元)" width="120">
            <template #default="{ row }">
              ¥{{ row.cost.toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column prop="intensity" label="能耗强度" width="120">
            <template #default="{ row }">
              {{ row.intensity }} {{ getUnit() }}/m²
            </template>
          </el-table-column>
          <el-table-column prop="efficiency" label="能效等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getEfficiencyTagType(row.efficiency)">
                {{ row.efficiency }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="warning" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="success" @click="handleAnalyze(row)">
                <el-icon><DataAnalysis /></el-icon>
                分析
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Download, 
  Plus, 
  Edit,
  View,
  Setting,
  Location,
  ArrowUp,
  ArrowDown,
  DataAnalysis,
  Money
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const barChartRef = ref()
const heatmapChartRef = ref()
let barChart: echarts.ECharts | null = null
let heatmapChart: echarts.ECharts | null = null

const selectedBuilding = ref('building_a')

const filterForm = reactive({
  dateRange: [],
  energyType: 'electricity',
  building: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 概览数据
const overviewData = ref([
  {
    title: '总能耗',
    value: '245,678',
    unit: 'kWh',
    icon: DataAnalysis,
    color: '#3b82f6',
    trend: 8.5
  },
  {
    title: '平均能耗强度',
    value: '45.8',
    unit: 'kWh/m²',
    icon: DataAnalysis,
    color: '#10b981',
    trend: -3.2
  },
  {
    title: '最高能耗区域',
    value: 'A栋办公区',
    unit: '',
    icon: Location,
    color: '#f59e0b',
    trend: 12.8
  },
  {
    title: '总费用',
    value: '331,665',
    unit: '元',
    icon: Money,
    color: '#ef4444',
    trend: 6.7
  }
])

// 表格数据
const tableData = ref([
  {
    zoneName: 'A栋1楼办公区',
    building: 'building_a',
    area: 2500,
    deviceCount: 45,
    consumption: 18500,
    cost: 24975,
    intensity: 7.4,
    efficiency: 'A'
  },
  {
    zoneName: 'A栋2楼会议区',
    building: 'building_a',
    area: 1800,
    deviceCount: 25,
    consumption: 12800,
    cost: 17280,
    intensity: 7.1,
    efficiency: 'A'
  },
  {
    zoneName: 'B栋餐厅区域',
    building: 'building_b',
    area: 3200,
    deviceCount: 65,
    consumption: 28900,
    cost: 39015,
    intensity: 9.0,
    efficiency: 'B'
  },
  {
    zoneName: 'C栋机房区域',
    building: 'building_c',
    area: 800,
    deviceCount: 85,
    consumption: 45200,
    cost: 61020,
    intensity: 56.5,
    efficiency: 'C'
  }
])

// 方法
const getUnit = () => {
  const units = {
    electricity: 'kWh',
    water: 'm³',
    gas: 'm³',
    heat: 'GJ'
  }
  return units[filterForm.energyType] || 'kWh'
}

const getBuildingTagType = (building: string) => {
  const types = {
    building_a: 'primary',
    building_b: 'success',
    building_c: 'warning'
  }
  return types[building] || 'info'
}

const getBuildingName = (building: string) => {
  const names = {
    building_a: 'A栋',
    building_b: 'B栋',
    building_c: 'C栋'
  }
  return names[building] || building
}

const getEfficiencyTagType = (efficiency: string) => {
  const types = {
    'A': 'success',
    'B': 'warning', 
    'C': 'danger',
    'D': 'info'
  }
  return types[efficiency] || 'info'
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
    building: ''
  })
  selectedBuilding.value = ''
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleView = (row: any) => {
  ElMessage.info(`查看分区详情: ${row.zoneName}`)
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑分区: ${row.zoneName}`)
}

const handleAnalyze = (row: any) => {
  ElMessage.info(`分析分区能耗: ${row.zoneName}`)
}

const refreshChart = () => {
  initBarChart()
}

const refreshHeatmap = () => {
  initHeatmapChart()
}

const initBarChart = () => {
  if (!barChartRef.value) return
  
  if (barChart) {
    barChart.dispose()
  }
  
  barChart = echarts.init(barChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['能耗量', '费用']
    },
    xAxis: {
      type: 'category',
      data: ['A栋1楼', 'A栋2楼', 'B栋餐厅', 'C栋机房', 'D栋仓库']
    },
    yAxis: [
      {
        type: 'value',
        name: '能耗量(kWh)',
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
        name: '能耗量',
        type: 'bar',
        data: [18500, 12800, 28900, 45200, 32100],
        itemStyle: {
          color: '#3b82f6'
        }
      },
      {
        name: '费用',
        type: 'bar',
        yAxisIndex: 1,
        data: [24975, 17280, 39015, 61020, 43335],
        itemStyle: {
          color: '#10b981'
        }
      }
    ]
  }
  
  barChart.setOption(option)
}

const initHeatmapChart = () => {
  if (!heatmapChartRef.value) return
  
  if (heatmapChart) {
    heatmapChart.dispose()
  }
  
  heatmapChart = echarts.init(heatmapChartRef.value)
  
  // 生成热力图数据
  const hours = ['00', '02', '04', '06', '08', '10', '12', '14', '16', '18', '20', '22']
  const zones = ['A栋1楼', 'A栋2楼', 'B栋餐厅', 'C栋机房', 'D栋仓库']
  
  const data: number[][] = []
  for (let i = 0; i < zones.length; i++) {
    for (let j = 0; j < hours.length; j++) {
      data.push([j, i, Math.round(Math.random() * 100)])
    }
  }
  
  const option = {
    tooltip: {
      position: 'top',
      formatter: function (params: any) {
        return `${zones[params.data[1]]}<br/>${hours[params.data[0]]}:00<br/>能耗: ${params.data[2]} kWh`
      }
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: hours,
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: zones,
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%',
      inRange: {
        color: ['#e6f7ff', '#1890ff', '#0050b3']
      }
    },
    series: [
      {
        name: '能耗热力图',
        type: 'heatmap',
        data: data,
        label: {
          show: true
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  heatmapChart.setOption(option)
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
  initBarChart()
  initHeatmapChart()
  
  // 响应式调整
  window.addEventListener('resize', () => {
    barChart?.resize()
    heatmapChart?.resize()
  })
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.zone-energy-report {
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
          line-height: 1.2;
        }

        .card-unit {
          font-size: 12px;
          color: #909399;
        }

        .card-trend {
          font-size: 12px;
          margin-top: 5px;
          display: flex;
          align-items: center;
          gap: 2px;

          &.trend-up {
            color: #f56c6c;
          }

          &.trend-down {
            color: #67c23a;
          }
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
        height: 350px;
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

    .zone-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .zone-icon {
        color: #409eff;
      }
    }
  }
}
</style>



