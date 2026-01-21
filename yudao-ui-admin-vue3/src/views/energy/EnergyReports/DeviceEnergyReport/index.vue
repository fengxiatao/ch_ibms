<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="device-energy-report dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗报表</el-breadcrumb-item>
        <el-breadcrumb-item>设备能耗报表</el-breadcrumb-item>
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
        <el-form-item label="设备类型">
          <el-select v-model="filterForm.deviceType" placeholder="请选择">
            <el-option label="全部类型" value="" />
            <el-option label="空调设备" value="hvac" />
            <el-option label="照明设备" value="lighting" />
            <el-option label="电梯设备" value="elevator" />
            <el-option label="UPS设备" value="ups" />
            <el-option label="其他设备" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="能源类型">
          <el-select v-model="filterForm.energyType" placeholder="请选择">
            <el-option label="电力" value="electricity" />
            <el-option label="水量" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="热量" value="heat" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable>
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="异常" value="error" />
            <el-option label="维修" value="maintenance" />
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
                <span>设备能耗排行</span>
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
                <span>设备类型分布</span>
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
            <span>设备能耗详细数据</span>
            <div class="header-actions">
              <el-button size="small" type="primary">
                <el-icon><Plus /></el-icon>
                添加设备
              </el-button>
              <el-button size="small" type="warning">
                <el-icon><Tools /></el-icon>
                批量维护
              </el-button>
            </div>
          </div>
        </template>

        <el-table v-loading="loading" :data="tableData" stripe border>
          <el-table-column type="selection" width="50" />
          <el-table-column prop="deviceName" label="设备名称" min-width="180">
            <template #default="{ row }">
              <div class="device-info">
                <el-icon class="device-icon" :style="{ color: getDeviceIconColor(row.deviceType) }">
                  <component :is="getDeviceIcon(row.deviceType)" />
                </el-icon>
                <div>
                  <div class="device-name">{{ row.deviceName }}</div>
                  <div class="device-code">{{ row.deviceCode }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="deviceType" label="设备类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getDeviceTypeColor(row.deviceType)">
                {{ getDeviceTypeName(row.deviceType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="安装位置" width="140" />
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
          <el-table-column prop="power" label="额定功率" width="120">
            <template #default="{ row }">
              {{ row.power }} kW
            </template>
          </el-table-column>
          <el-table-column prop="efficiency" label="能效等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getEfficiencyColor(row.efficiency)">
                {{ row.efficiency }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="运行状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Download,
  DataAnalysis,
  Setting,
  View,
  Tools,
  Monitor,
  Lightning,
  Cpu,
  Warning,
  Plus
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const barChartRef = ref()
const pieChartRef = ref()
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const filterForm = reactive({
  dateRange: [],
  deviceType: '',
  energyType: 'electricity',
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
    title: '设备总数',
    value: '368',
    unit: '台',
    icon: Monitor,
    color: '#3b82f6'
  },
  {
    title: '在线设备',
    value: '342',
    unit: '台',
    icon: Cpu,
    color: '#10b981'
  },
  {
    title: '异常设备',
    value: '18',
    unit: '台',
    icon: Warning,
    color: '#f59e0b'
  },
  {
    title: '平均能效',
    value: 'A级',
    unit: '',
    icon: Lightning,
    color: '#8b5cf6'
  }
])

// 表格数据
const tableData = ref([
  {
    deviceName: '中央空调主机-1号',
    deviceCode: 'HVAC001',
    deviceType: 'hvac',
    location: 'A栋地下1层机房',
    consumption: 28500,
    cost: 38475,
    power: 120,
    efficiency: 'A',
    energyType: 'electricity',
    status: 'online'
  },
  {
    deviceName: '智能照明控制器-A101',
    deviceCode: 'LED001',
    deviceType: 'lighting',
    location: 'A栋1楼大厅',
    consumption: 1250,
    cost: 1687,
    power: 5.5,
    efficiency: 'A+',
    energyType: 'electricity',
    status: 'online'
  },
  {
    deviceName: '客梯电机-E01',
    deviceCode: 'ELV001',
    deviceType: 'elevator',
    location: 'A栋1-20楼',
    consumption: 8900,
    cost: 12015,
    power: 35,
    efficiency: 'B',
    energyType: 'electricity',
    status: 'online'
  },
  {
    deviceName: 'UPS不间断电源-01',
    deviceCode: 'UPS001',
    deviceType: 'ups',
    location: 'A栋地下2层机房',
    consumption: 15600,
    cost: 21060,
    power: 50,
    efficiency: 'A',
    energyType: 'electricity',
    status: 'error'
  },
  {
    deviceName: '热水循环泵-P01',
    deviceCode: 'PMP001',
    deviceType: 'other',
    location: 'B栋地下1层',
    consumption: 3200,
    cost: 4320,
    power: 15,
    efficiency: 'B',
    energyType: 'electricity',
    status: 'maintenance'
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

const getDeviceIcon = (deviceType: string) => {
  const icons = {
    hvac: Cpu,
    lighting: Lightning,
    elevator: Monitor,
    ups: Lightning,
    other: Tools
  }
  return icons[deviceType] || Monitor
}

const getDeviceIconColor = (deviceType: string) => {
  const colors = {
    hvac: '#3b82f6',
    lighting: '#f59e0b',
    elevator: '#10b981',
    ups: '#8b5cf6',
    other: '#6b7280'
  }
  return colors[deviceType] || '#6b7280'
}

const getDeviceTypeColor = (type: string) => {
  const colors = {
    hvac: 'primary',
    lighting: 'warning',
    elevator: 'success',
    ups: '',
    other: 'info'
  }
  return colors[type] || 'info'
}

const getDeviceTypeName = (type: string) => {
  const names = {
    hvac: '空调',
    lighting: '照明',
    elevator: '电梯',
    ups: 'UPS',
    other: '其他'
  }
  return names[type] || type
}

const getEfficiencyColor = (efficiency: string) => {
  const colors = {
    'A+': 'success',
    'A': 'success',
    'B': 'warning',
    'C': 'danger',
    'D': 'info'
  }
  return colors[efficiency] || 'info'
}

const getStatusColor = (status: string) => {
  const colors = {
    online: 'success',
    offline: 'info',
    error: 'danger',
    maintenance: 'warning'
  }
  return colors[status] || 'info'
}

const getStatusName = (status: string) => {
  const names = {
    online: '在线',
    offline: '离线',
    error: '异常',
    maintenance: '维修'
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
    deviceType: '',
    energyType: 'electricity',
    status: ''
  })
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleView = (row: any) => {
  ElMessage.info(`查看设备详情: ${row.deviceName}`)
}

const handleChart = (row: any) => {
  ElMessage.info(`查看设备图表: ${row.deviceName}`)
}

const handleControl = (row: any) => {
  ElMessage.info(`控制设备: ${row.deviceName}`)
}

const refreshChart = () => {
  initBarChart()
}

const refreshPieChart = () => {
  initPieChart()
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
    xAxis: {
      type: 'category',
      data: ['中央空调-1', 'UPS电源-1', '客梯电机-1', '热水泵-1', '照明控制-1']
    },
    yAxis: {
      type: 'value',
      name: '能耗量(kWh)'
    },
    series: [
      {
        name: '能耗量',
        type: 'bar',
        data: [28500, 15600, 8900, 3200, 1250],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  
  barChart.setOption(option)
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
        name: '设备类型分布',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 45, name: '空调设备' },
          { value: 25, name: '照明设备' },
          { value: 15, name: '电梯设备' },
          { value: 10, name: 'UPS设备' },
          { value: 5, name: '其他设备' }
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

// 生命周期
onMounted(async () => {
  loadData()
  await nextTick()
  initBarChart()
  initPieChart()
  
  // 响应式调整
  window.addEventListener('resize', () => {
    barChart?.resize()
    pieChart?.resize()
  })
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.device-energy-report {
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

    .device-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .device-icon {
        font-size: 20px;
      }

      .device-name {
        font-weight: 500;
        color: #303133;
        line-height: 1.2;
      }

      .device-code {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }
  }
}
</style>



