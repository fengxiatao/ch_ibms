<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="energy-indicators-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能效分析</el-breadcrumb-item>
        <el-breadcrumb-item>能效指标</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 指标概览卡片 -->
    <div class="indicators-overview">
      <el-row :gutter="20">
        <el-col :span="6" v-for="(indicator, index) in overviewData" :key="index">
          <el-card class="indicator-card" shadow="hover">
            <div class="indicator-content">
              <div class="indicator-icon">
                <el-icon :size="32" :color="indicator.color">
                  <component :is="indicator.icon" />
                </el-icon>
              </div>
              <div class="indicator-info">
                <div class="indicator-title">{{ indicator.title }}</div>
                <div class="indicator-value">{{ indicator.value }}</div>
                <div class="indicator-unit">{{ indicator.unit }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
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
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="分项类型">
          <el-select v-model="filterForm.itemType" placeholder="请选择分项类型" style="width: 150px">
            <el-option label="全部" value="all" />
            <el-option label="照明插座用电" value="lighting" />
            <el-option label="空调用电" value="air_conditioning" />
            <el-option label="动力用电" value="power" />
            <el-option label="特殊用电" value="special" />
          </el-select>
        </el-form-item>
        <el-form-item label="分区">
          <el-select v-model="filterForm.zone" placeholder="请选择分区" style="width: 150px">
            <el-option label="全部" value="all" />
            <el-option label="办公区" value="office" />
            <el-option label="会议区" value="meeting" />
            <el-option label="机房" value="server_room" />
            <el-option label="公共区域" value="public" />
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
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 指标详情表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span class="table-title">能效指标详情</span>
          <div class="table-actions">
            <el-button type="primary" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="indicatorsList" v-loading="loading" stripe>
        <el-table-column prop="name" label="指标名称" width="200" />
        <el-table-column prop="category" label="指标类别" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)">
              {{ getCategoryName(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentValue" label="当前值" width="120" align="right">
          <template #default="{ row }">
            <span class="indicator-value-text">{{ row.currentValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="targetValue" label="目标值" width="120" align="right">
          <template #default="{ row }">
            <span class="target-value">{{ row.targetValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="achievement" label="达成率" width="100" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.achievement" 
              :color="getProgressColor(row.achievement)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="trend" label="趋势" width="100" align="center">
          <template #default="{ row }">
            <el-icon :size="16" :color="getTrendColor(row.trend)">
              <component :is="getTrendIcon(row.trend)" />
            </el-icon>
            <span class="trend-text" :style="{ color: getTrendColor(row.trend) }">
              {{ getTrendText(row.trend) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="warning" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 指标详情对话框 -->
    <el-dialog v-model="dialogVisible" title="指标详情" width="800px" destroy-on-close>
      <div v-if="currentIndicator" class="indicator-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="指标名称">{{ currentIndicator.name }}</el-descriptions-item>
          <el-descriptions-item label="指标类别">{{ getCategoryName(currentIndicator.category) }}</el-descriptions-item>
          <el-descriptions-item label="当前值">{{ currentIndicator.currentValue }} {{ currentIndicator.unit }}</el-descriptions-item>
          <el-descriptions-item label="目标值">{{ currentIndicator.targetValue }} {{ currentIndicator.unit }}</el-descriptions-item>
          <el-descriptions-item label="达成率">{{ currentIndicator.achievement }}%</el-descriptions-item>
          <el-descriptions-item label="趋势">{{ getTrendText(currentIndicator.trend) }}</el-descriptions-item>
          <el-descriptions-item label="计算公式" :span="2">{{ currentIndicator.formula }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentIndicator.remark }}</el-descriptions-item>
        </el-descriptions>

        <!-- 历史趋势图 -->
        <div class="trend-chart" style="margin-top: 20px;">
          <h4>历史趋势</h4>
          <div ref="trendChart" style="width: 100%; height: 300px;"></div>
        </div>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Download, 
  View, 
  Edit,
  DataAnalysis,
  Stopwatch,
  Lightning,
  CaretTop,
  CaretBottom,
  Minus
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const currentIndicator = ref(null)
const trendChart = ref(null)

// 筛选表单
const filterForm = reactive({
  dateRange: [],
  itemType: 'all',
  zone: 'all'
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 概览数据
const overviewData = ref([
  {
    title: '总体能效比',
    value: '3.25',
    unit: 'COP',
    icon: DataAnalysis,
    color: '#409EFF'
  },
  {
    title: '单位面积能耗',
    value: '45.8',
    unit: 'kWh/m²',
    icon: Stopwatch,
    color: '#67C23A'
  },
  {
    title: '节能率',
    value: '15.6',
    unit: '%',
    icon: DataAnalysis,
    color: '#E6A23C'
  },
  {
    title: '碳排放强度',
    value: '28.3',
    unit: 'kg/m²',
    icon: Lightning,
    color: '#F56C6C'
  }
])

// 指标列表
const indicatorsList = ref([
  {
    id: 1,
    name: '制冷系统COP',
    category: 'efficiency',
    currentValue: 3.25,
    targetValue: 3.5,
    unit: 'COP',
    achievement: 92.9,
    trend: 'up',
    updateTime: '2025-09-27 14:30:00',
    formula: '制冷量 / 耗电量',
    remark: '反映制冷系统能效水平'
  },
  {
    id: 2,
    name: '照明功率密度',
    category: 'lighting',
    currentValue: 8.5,
    targetValue: 9.0,
    unit: 'W/m²',
    achievement: 105.9,
    trend: 'down',
    updateTime: '2025-09-27 14:25:00',
    formula: '照明总功率 / 照明面积',
    remark: '单位面积照明用电功率'
  },
  {
    id: 3,
    name: '空调负荷率',
    category: 'hvac',
    currentValue: 78.6,
    targetValue: 80.0,
    unit: '%',
    achievement: 98.3,
    trend: 'stable',
    updateTime: '2025-09-27 14:20:00',
    formula: '实际负荷 / 设计负荷 × 100%',
    remark: '空调系统实际运行负荷比例'
  },
  {
    id: 4,
    name: '能耗强度',
    category: 'consumption',
    currentValue: 45.8,
    targetValue: 42.0,
    unit: 'kWh/m²',
    achievement: 91.7,
    trend: 'up',
    updateTime: '2025-09-27 14:15:00',
    formula: '总能耗 / 建筑面积',
    remark: '单位面积年度能耗'
  },
  {
    id: 5,
    name: '设备利用率',
    category: 'equipment',
    currentValue: 85.2,
    targetValue: 85.0,
    unit: '%',
    achievement: 100.2,
    trend: 'up',
    updateTime: '2025-09-27 14:10:00',
    formula: '设备运行时间 / 计划运行时间 × 100%',
    remark: '设备实际运行效率'
  }
])

// 方法
const handleSearch = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    ElMessage.success('查询完成')
    loading.value = false
  }, 1000)
}

const handleReset = () => {
  filterForm.dateRange = []
  filterForm.itemType = 'all'
  filterForm.zone = 'all'
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleView = (row: any) => {
  currentIndicator.value = row
  dialogVisible.value = true
  nextTick(() => {
    initTrendChart()
  })
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑功能开发中...')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  handleSearch()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  handleSearch()
}

// 工具方法
const getCategoryTagType = (category: string) => {
  const typeMap = {
    efficiency: 'primary',
    lighting: 'success',
    hvac: 'warning',
    consumption: 'danger',
    equipment: 'info'
  }
  return typeMap[category] || 'info'
}

const getCategoryName = (category: string) => {
  const nameMap = {
    efficiency: '能效指标',
    lighting: '照明指标',
    hvac: '暖通指标',
    consumption: '能耗指标',
    equipment: '设备指标'
  }
  return nameMap[category] || '其他'
}

const getProgressColor = (percentage: number) => {
  if (percentage >= 100) return '#67C23A'
  if (percentage >= 80) return '#E6A23C'
  return '#F56C6C'
}

const getTrendColor = (trend: string) => {
  const colorMap = {
    up: '#67C23A',
    down: '#F56C6C',
    stable: '#909399'
  }
  return colorMap[trend] || '#909399'
}

const getTrendIcon = (trend: string) => {
  const iconMap = {
    up: CaretTop,
    down: CaretBottom,
    stable: Minus
  }
  return iconMap[trend] || Minus
}

const getTrendText = (trend: string) => {
  const textMap = {
    up: '上升',
    down: '下降',
    stable: '平稳'
  }
  return textMap[trend] || '平稳'
}

const initTrendChart = () => {
  if (!trendChart.value) return
  
  const chart = echarts.init(trendChart.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '指标值',
        type: 'line',
        data: [3.1, 3.15, 3.2, 3.18, 3.22, 3.26, 3.24, 3.23, 3.25],
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '目标值',
        type: 'line',
        data: [3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5],
        lineStyle: {
          type: 'dashed',
          color: '#E6A23C'
        },
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }
  chart.setOption(option)
}

// 初始化
onMounted(() => {
  pagination.total = indicatorsList.value.length
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.energy-indicators-container {
  background: #0a1628 !important;
  min-height: 100vh;
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
    
    :deep(.el-breadcrumb__inner) {
      color: #e5eaf3 !important;
    }
    
    :deep(.el-breadcrumb__separator) {
      color: #6b7485 !important;
    }
  }

  .indicators-overview {
    margin-bottom: 20px;

    .indicator-card {
      background: #1e293b !important;
      border: 1px solid #334155 !important;

      :deep(.el-card__body) {
        background: #1e293b !important;
        padding: 20px;
      }

      .indicator-content {
        display: flex;
        align-items: center;
        gap: 15px;

        .indicator-icon {
          flex-shrink: 0;
        }

        .indicator-info {
          flex: 1;

          .indicator-title {
            font-size: 14px;
            color: #94a3b8;
            margin-bottom: 8px;
          }

          .indicator-value {
            font-size: 24px;
            font-weight: bold;
            color: #e5eaf3;
            margin-bottom: 4px;
          }

          .indicator-unit {
            font-size: 12px;
            color: #6b7485;
          }
        }
      }
    }
  }

  .filter-card,
  .table-card {
    background: #1e293b !important;
    border: 1px solid #334155 !important;
    margin-bottom: 20px;

    :deep(.el-card__header) {
      background: #1e293b !important;
      border-bottom: 1px solid #334155 !important;
      color: #e5eaf3 !important;
    }

    :deep(.el-card__body) {
      background: #1e293b !important;
    }
  }

  :deep(.el-form-item__label) {
    color: #e5eaf3 !important;
  }

  :deep(.el-input__inner) {
    background: #334155 !important;
    border-color: #475569 !important;
    color: #e5eaf3 !important;
  }

  :deep(.el-select) {
    .el-input__inner {
      background: #334155 !important;
      border-color: #475569 !important;
      color: #e5eaf3 !important;
    }
  }

  :deep(.el-date-editor) {
    background: #334155 !important;
    border-color: #475569 !important;
    
    .el-input__inner {
      background: transparent !important;
      color: #e5eaf3 !important;
    }
  }

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .table-title {
      font-size: 16px;
      font-weight: 500;
      color: #e5eaf3;
    }
  }

  :deep(.el-table) {
    background: #1e293b !important;
    color: #e5eaf3 !important;

    th {
      background: #334155 !important;
      color: #e5eaf3 !important;
      border-color: #475569 !important;
    }

    td {
      background: #1e293b !important;
      border-color: #475569 !important;
    }

    .el-table__row:hover td {
      background: #334155 !important;
    }
  }

  .indicator-value-text {
    font-weight: 600;
    color: #3b82f6;
  }

  .target-value {
    color: #10b981;
  }

  .trend-text {
    margin-left: 4px;
    font-size: 12px;
  }

  .pagination-container {
    margin-top: 20px;
    text-align: right;

    :deep(.el-pagination) {
      .btn-next,
      .btn-prev,
      .el-pager li {
        background: #334155 !important;
        color: #e5eaf3 !important;
        border-color: #475569 !important;
      }

      .el-pager li.is-active {
        background: #3b82f6 !important;
        color: white !important;
      }
    }
  }

  :deep(.el-dialog) {
    background: #1e293b !important;
    border: 1px solid #334155 !important;

    .el-dialog__header {
      background: #1e293b !important;
      border-bottom: 1px solid #334155 !important;
    }

    .el-dialog__title {
      color: #e5eaf3 !important;
    }

    .el-dialog__body {
      background: #1e293b !important;
      color: #e5eaf3 !important;
    }
  }

  :deep(.el-descriptions) {
    .el-descriptions__label {
      background: #334155 !important;
      color: #e5eaf3 !important;
    }

    .el-descriptions__content {
      background: #1e293b !important;
      color: #e5eaf3 !important;
    }
  }
}
</style>





