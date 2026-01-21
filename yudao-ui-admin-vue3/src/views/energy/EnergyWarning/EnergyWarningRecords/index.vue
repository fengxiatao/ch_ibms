<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="energy-warning-records dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能耗预警</el-breadcrumb-item>
        <el-breadcrumb-item>能耗预警记录</el-breadcrumb-item>
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
        <el-form-item label="预警级别">
          <el-select v-model="filterForm.warningLevel" placeholder="请选择" clearable>
            <el-option label="一般" value="low" />
            <el-option label="重要" value="medium" />
            <el-option label="紧急" value="high" />
            <el-option label="严重" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item label="能源类型">
          <el-select v-model="filterForm.energyType" placeholder="请选择" clearable>
            <el-option label="电力" value="electricity" />
            <el-option label="水量" value="water" />
            <el-option label="燃气" value="gas" />
            <el-option label="热量" value="heat" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已忽略" value="ignored" />
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

    <!-- 统计概览 -->
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
                <span>预警趋势分析</span>
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
                <span>预警级别分布</span>
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

    <!-- 预警记录表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>预警记录列表</span>
            <div class="header-actions">
              <el-button size="small" type="success" @click="handleBatchProcess">
                <el-icon><CircleCheck /></el-icon>
                批量处理
              </el-button>
              <el-button size="small" type="info" @click="handleBatchIgnore">
                <el-icon><CircleClose /></el-icon>
                批量忽略
              </el-button>
              <el-button size="small" type="danger" @click="handleBatchDelete">
                <el-icon><Delete /></el-icon>
                批量删除
              </el-button>
            </div>
          </div>
        </template>

        <el-table v-loading="loading" :data="recordsList" stripe border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="warningTitle" label="预警内容" min-width="200">
            <template #default="{ row }">
              <div class="warning-info">
                <el-icon class="warning-icon" :style="{ color: getLevelColor(row.warningLevel) }">
                  <Warning />
                </el-icon>
                <div>
                  <div class="warning-title">{{ row.warningTitle }}</div>
                  <div class="warning-desc">{{ row.description }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="energyType" label="能源类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getEnergyTypeColor(row.energyType)">
                {{ getEnergyTypeName(row.energyType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="warningLevel" label="预警级别" width="100">
            <template #default="{ row }">
              <el-tag :type="getLevelTagType(row.warningLevel)">
                {{ getLevelName(row.warningLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="triggerValue" label="触发值" width="120">
            <template #default="{ row }">
              {{ row.triggerValue }}{{ row.unit }}
            </template>
          </el-table-column>
          <el-table-column prop="thresholdValue" label="阈值" width="120">
            <template #default="{ row }">
              {{ row.thresholdValue }}{{ row.unit }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" width="140" show-overflow-tooltip />
          <el-table-column prop="triggerTime" label="触发时间" width="160" />
          <el-table-column prop="status" label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="processingTime" label="处理时间" width="160">
            <template #default="{ row }">
              {{ row.processingTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button v-if="row.status === 'pending'" link type="success" @click="handleProcess(row)">
                <el-icon><CircleCheck /></el-icon>
                处理
              </el-button>
              <el-button v-if="row.status === 'pending'" link type="warning" @click="handleIgnore(row)">
                <el-icon><CircleClose /></el-icon>
                忽略
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
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

    <!-- 详情弹框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="预警记录详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预警标题">{{ currentRecord.warningTitle }}</el-descriptions-item>
          <el-descriptions-item label="预警级别">
            <el-tag :type="getLevelTagType(currentRecord.warningLevel)">
              {{ getLevelName(currentRecord.warningLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="能源类型">
            <el-tag :type="getEnergyTypeColor(currentRecord.energyType)">
              {{ getEnergyTypeName(currentRecord.energyType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="触发值">{{ currentRecord.triggerValue }}{{ currentRecord.unit }}</el-descriptions-item>
          <el-descriptions-item label="阈值">{{ currentRecord.thresholdValue }}{{ currentRecord.unit }}</el-descriptions-item>
          <el-descriptions-item label="超标程度">
            <span :style="{ color: getLevelColor(currentRecord.warningLevel) }">
              {{ ((currentRecord.triggerValue - currentRecord.thresholdValue) / currentRecord.thresholdValue * 100).toFixed(1) }}%
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="触发位置">{{ currentRecord.location }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusTagType(currentRecord.status)">
              {{ getStatusName(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ currentRecord.triggerTime }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ currentRecord.processingTime || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="预警描述" :span="2">{{ currentRecord.description }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.processingNote" label="处理备注" :span="2">
            {{ currentRecord.processingNote }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 处理预警弹框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理预警"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="120px">
        <el-form-item label="处理结果" prop="result">
          <el-radio-group v-model="processForm.result">
            <el-radio label="resolved">已解决</el-radio>
            <el-radio label="monitoring">持续监控</el-radio>
            <el-radio label="escalated">上报处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="note">
          <el-input
            v-model="processForm.note"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleProcessSubmit">确认处理</el-button>
      </template>
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
  Delete,
  Clock,
  View,
  CircleCheck,
  CircleClose,
  DataAnalysis,
  Warning
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentRecord = ref<any>(null)
const selectedRecords = ref<any[]>([])
const lineChartRef = ref()
const pieChartRef = ref()
let lineChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const filterForm = reactive({
  dateRange: [],
  warningLevel: '',
  energyType: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 处理表单
const processForm = reactive({
  result: '',
  note: ''
})

const processRules = {
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  note: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

// 概览数据
const overviewData = ref([
  {
    title: '今日预警',
    value: '28',
    unit: '条',
    icon: Warning,
    color: '#f59e0b'
  },
  {
    title: '待处理',
    value: '12',
    unit: '条',
    icon: Clock,
    color: '#ef4444'
  },
  {
    title: '已处理',
    value: '156',
    unit: '条',
    icon: CircleCheck,
    color: '#10b981'
  },
  {
    title: '处理率',
    value: '91.2',
    unit: '%',
    icon: DataAnalysis,
    color: '#3b82f6'
  }
])

// 预警记录列表
const recordsList = ref([
  {
    id: '1',
    warningTitle: '电力能耗超标预警',
    description: 'A栋办公楼电力消耗超过设定阈值1000kWh',
    energyType: 'electricity',
    warningLevel: 'high',
    triggerValue: 1250,
    thresholdValue: 1000,
    unit: 'kWh',
    location: 'A栋办公楼',
    triggerTime: '2024-01-22 14:30:00',
    status: 'pending',
    processingTime: null,
    processingNote: null
  },
  {
    id: '2',
    warningTitle: '水量异常消耗预警',
    description: 'B栋餐厅水量消耗异常增长，超过正常值50%',
    energyType: 'water',
    warningLevel: 'medium',
    triggerValue: 750,
    thresholdValue: 500,
    unit: 'm³',
    location: 'B栋餐厅',
    triggerTime: '2024-01-22 12:15:00',
    status: 'processing',
    processingTime: '2024-01-22 12:45:00',
    processingNote: '已联系维修人员检查管道'
  },
  {
    id: '3',
    warningTitle: '燃气泄漏风险预警',
    description: 'C栋厨房燃气消耗量异常，疑似泄漏风险',
    energyType: 'gas',
    warningLevel: 'critical',
    triggerValue: 150,
    thresholdValue: 100,
    unit: 'm³',
    location: 'C栋厨房',
    triggerTime: '2024-01-22 10:20:00',
    status: 'resolved',
    processingTime: '2024-01-22 10:35:00',
    processingNote: '已检查管道，确认无泄漏，消耗增长为正常使用'
  },
  {
    id: '4',
    warningTitle: '热量供应不足预警',
    description: 'D栋宿舍楼热量供应低于最低要求',
    energyType: 'heat',
    warningLevel: 'medium',
    triggerValue: 35,
    thresholdValue: 50,
    unit: 'GJ',
    location: 'D栋宿舍楼',
    triggerTime: '2024-01-22 08:00:00',
    status: 'ignored',
    processingTime: '2024-01-22 08:30:00',
    processingNote: '临时性供应不足，已恢复正常'
  }
])

const processFormRef = ref()

// 方法
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

const getLevelColor = (level: string) => {
  const colors = {
    low: '#10b981',
    medium: '#f59e0b',
    high: '#ef4444',
    critical: '#dc2626'
  }
  return colors[level] || '#6b7280'
}

const getLevelTagType = (level: string) => {
  const types = {
    low: 'success',
    medium: 'warning',
    high: 'danger',
    critical: 'danger'
  }
  return types[level] || 'info'
}

const getLevelName = (level: string) => {
  const names = {
    low: '一般',
    medium: '重要',
    high: '紧急',
    critical: '严重'
  }
  return names[level] || level
}

const getStatusTagType = (status: string) => {
  const types = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success',
    ignored: 'info'
  }
  return types[status] || 'info'
}

const getStatusName = (status: string) => {
  const names = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已处理',
    ignored: '已忽略'
  }
  return names[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(filterForm, {
    dateRange: [],
    warningLevel: '',
    energyType: '',
    status: ''
  })
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleView = (row: any) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row: any) => {
  currentRecord.value = row
  resetProcessForm()
  processDialogVisible.value = true
}

const handleIgnore = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认忽略该预警记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    row.status = 'ignored'
    row.processingTime = new Date().toLocaleString('zh-CN')
    row.processingNote = '用户主动忽略'
    
    ElMessage.success('忽略成功')
  } catch {
    // 用户取消
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该预警记录吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = recordsList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      recordsList.value.splice(index, 1)
    }
    
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedRecords.value = selection
}

const handleBatchProcess = () => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请选择要处理的记录')
    return
  }
  ElMessage.success('批量处理功能开发中...')
}

const handleBatchIgnore = () => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请选择要忽略的记录')
    return
  }
  ElMessage.success('批量忽略功能开发中...')
}

const handleBatchDelete = () => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  ElMessage.success('批量删除功能开发中...')
}

const handleProcessSubmit = async () => {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    
    if (currentRecord.value) {
      currentRecord.value.status = processForm.result === 'resolved' ? 'resolved' : 'processing'
      currentRecord.value.processingTime = new Date().toLocaleString('zh-CN')
      currentRecord.value.processingNote = processForm.note
    }
    
    processDialogVisible.value = false
    ElMessage.success('处理成功')
  } catch {
    // 表单验证失败
  }
}

const resetProcessForm = () => {
  Object.assign(processForm, {
    result: '',
    note: ''
  })
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
      data: ['预警数量', '处理数量']
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value',
      name: '数量(条)'
    },
    series: [
      {
        name: '预警数量',
        type: 'line',
        data: [12, 8, 15, 20, 18, 6, 9],
        smooth: true,
        itemStyle: { color: '#f59e0b' }
      },
      {
        name: '处理数量',
        type: 'line',
        data: [10, 7, 12, 18, 16, 5, 8],
        smooth: true,
        itemStyle: { color: '#10b981' }
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
        name: '预警级别分布',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 35, name: '一般预警' },
          { value: 30, name: '重要预警' },
          { value: 25, name: '紧急预警' },
          { value: 10, name: '严重预警' }
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
  loading.value = true
  setTimeout(() => {
    pagination.total = recordsList.value.length
    loading.value = false
  }, 500)
}

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

<style scoped lang="scss">@use '@/styles/dark-theme.scss';

.energy-warning-records {
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

    .warning-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .warning-icon {
        font-size: 20px;
      }

      .warning-title {
        font-weight: 500;
        color: #303133;
        line-height: 1.2;
      }

      .warning-desc {
        font-size: 12px;
        color: #909399;
        margin-top: 2px;
      }
    }
  }

  .detail-content {
    .el-descriptions {
      margin-bottom: 20px;
    }
  }
}
</style>
