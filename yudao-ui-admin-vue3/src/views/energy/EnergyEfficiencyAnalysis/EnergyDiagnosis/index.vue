<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="energy-diagnosis-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>智慧能源</el-breadcrumb-item>
        <el-breadcrumb-item>能效分析</el-breadcrumb-item>
        <el-breadcrumb-item>能耗诊断</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 诊断概览 -->
    <div class="diagnosis-overview">
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in overviewData" :key="index">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon">
                <el-icon :size="32" :color="item.color">
                  <component :is="item.icon" />
                </el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-title">{{ item.title }}</div>
                <div class="overview-value">{{ item.value }}</div>
                <div class="overview-desc">{{ item.desc }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterForm" label-width="80px" :inline="true">
        <el-form-item label="诊断时间">
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
        <el-form-item label="问题等级">
          <el-select v-model="filterForm.level" placeholder="请选择问题等级" style="width: 150px">
            <el-option label="全部" value="all" />
            <el-option label="严重" value="severe" />
            <el-option label="中等" value="medium" />
            <el-option label="轻微" value="minor" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="filterForm.deviceType" placeholder="请选择设备类型" style="width: 150px">
            <el-option label="全部" value="all" />
            <el-option label="空调系统" value="hvac" />
            <el-option label="照明系统" value="lighting" />
            <el-option label="动力设备" value="power" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" style="width: 120px">
            <el-option label="全部" value="all" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
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
          <el-button type="success" @click="handleDiagnose">
            <el-icon><Operation /></el-icon>
            开始诊断
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 诊断结果 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span class="table-title">诊断结果</span>
          <div class="table-actions">
            <el-button type="primary" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="diagnosisList" v-loading="loading" stripe>
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="deviceType" label="设备类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getDeviceTypeTagType(row.deviceType)">
              {{ getDeviceTypeName(row.deviceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="problemDesc" label="问题描述" min-width="200" />
        <el-table-column prop="level" label="问题等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)">
              {{ getLevelName(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="energyLoss" label="能耗损失" width="120" align="right">
          <template #default="{ row }">
            <span class="energy-loss">{{ row.energyLoss }} kWh</span>
          </template>
        </el-table-column>
        <el-table-column prop="costLoss" label="费用损失" width="120" align="right">
          <template #default="{ row }">
            <span class="cost-loss">¥{{ row.costLoss }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosisTime" label="诊断时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="success" @click="handleSolution(row)" v-if="row.status === 'pending'">
              <el-icon><Tools /></el-icon>
              解决方案
            </el-button>
            <el-button link type="warning" @click="handleMark(row)" v-if="row.status === 'processing'">
              <el-icon><CircleCheck /></el-icon>
              标记完成
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="诊断详情" width="1000px" destroy-on-close>
      <div v-if="currentDiagnosis" class="diagnosis-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="设备名称">{{ currentDiagnosis.deviceName }}</el-descriptions-item>
              <el-descriptions-item label="设备类型">{{ getDeviceTypeName(currentDiagnosis.deviceType) }}</el-descriptions-item>
              <el-descriptions-item label="问题等级">
                <el-tag :type="getLevelTagType(currentDiagnosis.level)">
                  {{ getLevelName(currentDiagnosis.level) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="能耗损失">{{ currentDiagnosis.energyLoss }} kWh/天</el-descriptions-item>
              <el-descriptions-item label="费用损失">¥{{ currentDiagnosis.costLoss }}/天</el-descriptions-item>
              <el-descriptions-item label="诊断时间">{{ currentDiagnosis.diagnosisTime }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <div class="problem-analysis">
              <h4>问题分析</h4>
              <p class="problem-desc">{{ currentDiagnosis.problemDesc }}</p>
              <h4>可能原因</h4>
              <ul class="cause-list">
                <li v-for="(cause, index) in currentDiagnosis.causes" :key="index">{{ cause }}</li>
              </ul>
              <h4>建议措施</h4>
              <ul class="solution-list">
                <li v-for="(solution, index) in currentDiagnosis.solutions" :key="index">{{ solution }}</li>
              </ul>
            </div>
          </el-col>
        </el-row>

        <!-- 能耗趋势图 -->
        <div class="trend-chart" style="margin-top: 20px;">
          <h4>能耗趋势对比</h4>
          <div ref="trendChart" style="width: 100%; height: 400px;"></div>
        </div>
      </div>
    </el-dialog>

    <!-- 解决方案对话框 -->
    <el-dialog v-model="solutionVisible" title="解决方案" width="600px">
      <el-form :model="solutionForm" label-width="100px">
        <el-form-item label="解决方案">
          <el-input
            v-model="solutionForm.solution"
            type="textarea"
            :rows="4"
            placeholder="请输入解决方案..."
          />
        </el-form-item>
        <el-form-item label="预期效果">
          <el-input
            v-model="solutionForm.expectedEffect"
            type="textarea"
            :rows="3"
            placeholder="请输入预期效果..."
          />
        </el-form-item>
        <el-form-item label="执行人员">
          <el-input v-model="solutionForm.executor" placeholder="请输入执行人员" />
        </el-form-item>
        <el-form-item label="计划完成时间">
          <el-date-picker
            v-model="solutionForm.planTime"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="solutionVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSolution">确定</el-button>
        </span>
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
  View, 
  Operation,
  Tools,
  CircleCheck,
  Warning,
  InfoFilled,
  QuestionFilled,
  SuccessFilled
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const detailVisible = ref(false)
const solutionVisible = ref(false)
const currentDiagnosis = ref(null)
const trendChart = ref(null)

// 筛选表单
const filterForm = reactive({
  dateRange: [],
  level: 'all',
  deviceType: 'all',
  status: 'all'
})

// 解决方案表单
const solutionForm = reactive({
  solution: '',
  expectedEffect: '',
  executor: '',
  planTime: ''
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
    title: '待处理问题',
    value: '12',
    desc: '需要立即处理',
    icon: Warning,
    color: '#F56C6C'
  },
  {
    title: '严重问题',
    value: '3',
    desc: '影响系统运行',
    icon: InfoFilled,
    color: '#E6A23C'
  },
  {
    title: '能耗损失',
    value: '1,245',
    desc: 'kWh/天',
    icon: QuestionFilled,
    color: '#409EFF'
  },
  {
    title: '已完成',
    value: '28',
    desc: '本月处理完成',
    icon: SuccessFilled,
    color: '#67C23A'
  }
])

// 诊断列表
const diagnosisList = ref([
  {
    id: 1,
    deviceName: '中央空调主机1号',
    deviceType: 'hvac',
    problemDesc: '制冷效率下降，能耗比正常值高出25%',
    level: 'severe',
    energyLoss: 156,
    costLoss: 125,
    status: 'pending',
    diagnosisTime: '2025-09-27 09:30:00',
    causes: [
      '冷凝器换热面结垢严重',
      '制冷剂充注量不足',
      '过滤网堵塞导致风量不足'
    ],
    solutions: [
      '清洗冷凝器换热面',
      '检查并补充制冷剂',
      '更换或清洗空气过滤网',
      '调整运行参数优化能效'
    ]
  },
  {
    id: 2,
    deviceName: '照明回路B区',
    deviceType: 'lighting',
    problemDesc: '部分LED灯具老化，光效衰减导致能耗增加',
    level: 'medium',
    energyLoss: 45,
    costLoss: 36,
    status: 'processing',
    diagnosisTime: '2025-09-27 08:15:00',
    causes: [
      'LED灯具使用时间过长',
      '驱动电源老化',
      '灯具散热不良'
    ],
    solutions: [
      '更换老化的LED灯具',
      '优化照明控制策略',
      '改善散热条件'
    ]
  },
  {
    id: 3,
    deviceName: '给水泵2号',
    deviceType: 'power',
    problemDesc: '泵效率低下，运行功率超出设计值15%',
    level: 'minor',
    energyLoss: 28,
    costLoss: 22,
    status: 'completed',
    diagnosisTime: '2025-09-26 16:45:00',
    causes: [
      '叶轮磨损',
      '管道阻力增大',
      '运行点偏离高效区'
    ],
    solutions: [
      '检修或更换叶轮',
      '清理管道，降低阻力',
      '调整运行参数'
    ]
  },
  {
    id: 4,
    deviceName: '新风机组3号',
    deviceType: 'hvac',
    problemDesc: '风机运行效率偏低，电流偏大',
    level: 'medium',
    energyLoss: 72,
    costLoss: 58,
    status: 'pending',
    diagnosisTime: '2025-09-27 11:20:00',
    causes: [
      '风机叶片积尘',
      '轴承润滑不良',
      '皮带松动'
    ],
    solutions: [
      '清洁风机叶片',
      '更换轴承润滑油',
      '调整皮带张紧度'
    ]
  },
  {
    id: 5,
    deviceName: '电梯1号',
    deviceType: 'power',
    problemDesc: '待机能耗异常，比正常值高出30%',
    level: 'severe',
    energyLoss: 89,
    costLoss: 71,
    status: 'processing',
    diagnosisTime: '2025-09-27 07:30:00',
    causes: [
      '变频器参数设置不当',
      '制动电阻发热异常',
      '控制系统故障'
    ],
    solutions: [
      '重新设置变频器参数',
      '检修制动电阻',
      '检查控制系统'
    ]
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
  filterForm.level = 'all'
  filterForm.deviceType = 'all'
  filterForm.status = 'all'
  handleSearch()
}

const handleDiagnose = () => {
  loading.value = true
  ElMessage.info('正在进行能耗诊断...')
  // 模拟诊断过程
  setTimeout(() => {
    ElMessage.success('诊断完成，发现2个新问题')
    loading.value = false
    handleSearch()
  }, 3000)
}

const handleExport = () => {
  ElMessage.success('正在导出诊断报告...')
}

const handleView = (row: any) => {
  currentDiagnosis.value = row
  detailVisible.value = true
  nextTick(() => {
    initTrendChart()
  })
}

const handleSolution = (row: any) => {
  currentDiagnosis.value = row
  solutionForm.solution = ''
  solutionForm.expectedEffect = ''
  solutionForm.executor = ''
  solutionForm.planTime = ''
  solutionVisible.value = true
}

const handleMark = (row: any) => {
  ElMessageBox.confirm('确认标记该问题为已完成？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = 'completed'
    ElMessage.success('标记成功')
  })
}

const handleSubmitSolution = () => {
  if (!solutionForm.solution.trim()) {
    ElMessage.warning('请输入解决方案')
    return
  }
  
  // 更新状态为处理中
  if (currentDiagnosis.value) {
    currentDiagnosis.value.status = 'processing'
  }
  
  solutionVisible.value = false
  ElMessage.success('解决方案已提交')
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
const getDeviceTypeTagType = (type: string) => {
  const typeMap = {
    hvac: 'primary',
    lighting: 'success',
    power: 'warning'
  }
  return typeMap[type] || 'info'
}

const getDeviceTypeName = (type: string) => {
  const nameMap = {
    hvac: '暖通空调',
    lighting: '照明系统',
    power: '动力设备'
  }
  return nameMap[type] || '其他'
}

const getLevelTagType = (level: string) => {
  const typeMap = {
    severe: 'danger',
    medium: 'warning',
    minor: 'info'
  }
  return typeMap[level] || 'info'
}

const getLevelName = (level: string) => {
  const nameMap = {
    severe: '严重',
    medium: '中等',
    minor: '轻微'
  }
  return nameMap[level] || '未知'
}

const getStatusTagType = (status: string) => {
  const typeMap = {
    pending: 'danger',
    processing: 'warning',
    completed: 'success'
  }
  return typeMap[status] || 'info'
}

const getStatusName = (status: string) => {
  const nameMap = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成'
  }
  return nameMap[status] || '未知'
}

const initTrendChart = () => {
  if (!trendChart.value) return
  
  const chart = echarts.init(trendChart.value)
  const option = {
    title: {
      text: '能耗对比分析',
      textStyle: {
        color: '#e5eaf3'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['正常能耗', '实际能耗', '节能后预期'],
      textStyle: {
        color: '#e5eaf3'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
      axisLine: {
        lineStyle: { color: '#6b7485' }
      },
      axisLabel: {
        color: '#e5eaf3'
      }
    },
    yAxis: {
      type: 'value',
      name: '功率(kW)',
      axisLine: {
        lineStyle: { color: '#6b7485' }
      },
      axisLabel: {
        color: '#e5eaf3'
      }
    },
    series: [
      {
        name: '正常能耗',
        type: 'line',
        data: [120, 132, 101, 134, 90, 230, 210],
        smooth: true,
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '实际能耗',
        type: 'line',
        data: [150, 165, 126, 167, 113, 287, 262],
        smooth: true,
        itemStyle: { color: '#F56C6C' }
      },
      {
        name: '节能后预期',
        type: 'line',
        data: [115, 127, 97, 129, 87, 221, 202],
        smooth: true,
        lineStyle: { type: 'dashed' },
        itemStyle: { color: '#409EFF' }
      }
    ]
  }
  chart.setOption(option)
}

// 初始化
onMounted(() => {
  pagination.total = diagnosisList.value.length
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.energy-diagnosis-container {
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

  .diagnosis-overview {
    margin-bottom: 20px;

    .overview-card {
      background: #1e293b !important;
      border: 1px solid #334155 !important;

      :deep(.el-card__body) {
        background: #1e293b !important;
        padding: 20px;
      }

      .overview-content {
        display: flex;
        align-items: center;
        gap: 15px;

        .overview-icon {
          flex-shrink: 0;
        }

        .overview-info {
          flex: 1;

          .overview-title {
            font-size: 14px;
            color: #94a3b8;
            margin-bottom: 8px;
          }

          .overview-value {
            font-size: 24px;
            font-weight: bold;
            color: #e5eaf3;
            margin-bottom: 4px;
          }

          .overview-desc {
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

  .energy-loss {
    color: #F56C6C;
    font-weight: 600;
  }

  .cost-loss {
    color: #E6A23C;
    font-weight: 600;
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

  .problem-analysis {
    h4 {
      color: #e5eaf3;
      margin: 15px 0 10px 0;
    }

    .problem-desc {
      color: #94a3b8;
      line-height: 1.5;
      margin-bottom: 15px;
    }

    .cause-list,
    .solution-list {
      color: #94a3b8;
      
      li {
        margin-bottom: 8px;
        line-height: 1.4;
      }
    }
  }

  :deep(.el-textarea__inner) {
    background: #334155 !important;
    border-color: #475569 !important;
    color: #e5eaf3 !important;
  }
}
</style>








