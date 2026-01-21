<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="environment-alarm-records dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>环境监测</el-breadcrumb-item>
        <el-breadcrumb-item>环境告警记录</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="120px" :inline="true">
        <el-form-item label="请选择空间位置">
          <el-select v-model="filterForm.location" placeholder="请选择空间位置" clearable style="width: 200px;">
            <el-option label="全部位置" value="" />
            <el-option label="南区_科技研发楼_9F" value="south_tech_9f" />
            <el-option label="北区_办公楼_5F" value="north_office_5f" />
            <el-option label="东区_会议中心_3F" value="east_meeting_3f" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="filterForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="filterForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="请选择处理状态">
          <el-select v-model="filterForm.status" placeholder="请选择处理状态" clearable style="width: 150px;">
            <el-option label="全部状态" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="resolved" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 环境告警记录表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>环境告警记录</span>
            <div class="header-actions">
              <el-button type="success" @click="handleBatchProcess">
                <el-icon><CircleCheck /></el-icon>
                批量处理
              </el-button>
              <el-button type="warning" @click="handleExport">
                <el-icon><Download /></el-icon>
                导出记录
              </el-button>
              <el-button @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </template>

        <el-table v-loading="loading" :data="alarmList" stripe border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="serialNumber" label="序号" width="80" />
          <el-table-column prop="eventCode" label="事件编号" width="180" show-overflow-tooltip />
          <el-table-column prop="deviceName" label="设备名称" width="180" show-overflow-tooltip />
          <el-table-column prop="location" label="空间位置" width="200" show-overflow-tooltip />
          <el-table-column prop="alarmType" label="告警事件" width="120">
            <template #default="{ row }">
              <el-tag :type="getAlarmTypeColor(row.alarmType)">
                {{ getAlarmTypeText(row.alarmType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmLevel" label="告警等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getAlarmLevelColor(row.alarmLevel)">
                {{ getAlarmLevelText(row.alarmLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmTime" label="告警时间" width="160" />
          <el-table-column prop="status" label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button v-if="row.status === 'pending'" link type="success" @click="handleProcess(row)">
                <el-icon><Tools /></el-icon>
                处理
              </el-button>
              <el-button link type="warning" @click="handleAnalyze(row)">
                <el-icon><DataAnalysis /></el-icon>
                活动
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

    <!-- 告警详情弹框 -->
    <el-dialog v-model="detailDialogVisible" title="告警记录详情" width="70%" draggable>
      <div v-if="currentAlarm" class="alarm-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事件编号">{{ currentAlarm.eventCode }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ currentAlarm.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="空间位置">{{ currentAlarm.location }}</el-descriptions-item>
          <el-descriptions-item label="告警事件">
            <el-tag :type="getAlarmTypeColor(currentAlarm.alarmType)">
              {{ getAlarmTypeText(currentAlarm.alarmType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警等级">
            <el-tag :type="getAlarmLevelColor(currentAlarm.alarmLevel)">
              {{ getAlarmLevelText(currentAlarm.alarmLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警时间">{{ currentAlarm.alarmTime }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusColor(currentAlarm.status)">
              {{ getStatusText(currentAlarm.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警值">{{ currentAlarm.alarmValue || '-' }}</el-descriptions-item>
          <el-descriptions-item label="告警描述" :span="2">
            {{ currentAlarm.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentAlarm.processTime" label="处理时间">
            {{ currentAlarm.processTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentAlarm.processUser" label="处理人员">
            {{ currentAlarm.processUser }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentAlarm.processNote" label="处理备注" :span="2">
            {{ currentAlarm.processNote }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 环境数据趋势图 -->
        <div class="trend-section" v-if="currentAlarm.trendData">
          <h4>环境数据趋势</h4>
          <div ref="trendChartRef" class="trend-chart"></div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="currentAlarm?.status === 'pending'" type="primary" @click="handleProcessFromDetail">
          立即处理
        </el-button>
      </template>
    </el-dialog>

    <!-- 处理告警弹框 -->
    <el-dialog v-model="processDialogVisible" title="处理告警" width="50%" draggable>
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理结果" prop="result">
          <el-radio-group v-model="processForm.result">
            <el-radio label="resolved">已解决</el-radio>
            <el-radio label="monitoring">持续监控</el-radio>
            <el-radio label="escalated">上报处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理措施" prop="measures">
          <el-checkbox-group v-model="processForm.measures">
            <el-checkbox label="调整设备参数">调整设备参数</el-checkbox>
            <el-checkbox label="更换传感器">更换传感器</el-checkbox>
            <el-checkbox label="环境改善">环境改善</el-checkbox>
            <el-checkbox label="设备维修">设备维修</el-checkbox>
          </el-checkbox-group>
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

    <!-- 数据分析弹框 -->
    <el-dialog v-model="analyzeDialogVisible" title="环境数据分析" width="80%" draggable>
      <div v-if="currentAlarm" class="analyze-content">
        <el-tabs v-model="analyzeTab">
          <el-tab-pane label="数据趋势" name="trend">
            <div ref="analyzeTrendChartRef" class="analyze-chart"></div>
          </el-tab-pane>
          <el-tab-pane label="统计分析" name="statistics">
            <div class="statistics-content">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div ref="statisticsChartRef" class="statistics-chart"></div>
                </el-col>
                <el-col :span="12">
                  <div class="statistics-info">
                    <el-descriptions :column="1" border>
                      <el-descriptions-item label="最大值">{{ currentAlarm.statistics?.max || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="最小值">{{ currentAlarm.statistics?.min || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="平均值">{{ currentAlarm.statistics?.avg || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="标准差">{{ currentAlarm.statistics?.std || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="超标次数">{{ currentAlarm.statistics?.exceedCount || 0 }}</el-descriptions-item>
                      <el-descriptions-item label="超标率">{{ currentAlarm.statistics?.exceedRate || '0%' }}</el-descriptions-item>
                    </el-descriptions>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          <el-tab-pane label="相关告警" name="related">
            <el-table :data="currentAlarm.relatedAlarms || []" border>
              <el-table-column prop="time" label="时间" width="160" />
              <el-table-column prop="device" label="设备" width="150" />
              <el-table-column prop="type" label="告警类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="getAlarmTypeColor(row.type)">
                    {{ getAlarmTypeText(row.type) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="value" label="告警值" width="100" />
              <el-table-column prop="description" label="描述" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <template #footer>
        <el-button @click="analyzeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, CircleCheck, Download, Refresh, View, Tools, DataAnalysis
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 接口定义
interface AlarmRecord {
  eventCode: string
  deviceName: string
  location: string
  alarmType: string
  alarmLevel: string
  alarmTime: string
  status: string
  alarmValue: number
  description: string
  processTime?: string
  processUser?: string
  processNote?: string
  trendData?: number[]
  statistics?: any
  relatedAlarms?: AlarmRecord[]
}

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const analyzeDialogVisible = ref(false)
const currentAlarm = ref<AlarmRecord | null>(null)
const selectedAlarms = ref<AlarmRecord[]>([])
const analyzeTab = ref('trend')
const trendChartRef = ref()
const analyzeTrendChartRef = ref()
const statisticsChartRef = ref()
const processFormRef = ref<any>(null)

const filterForm = reactive({
  location: '',
  startTime: '',
  endTime: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const processForm = reactive({
  result: '',
  measures: [],
  note: ''
})

const processRules = {
  result: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  note: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

// 告警记录数据
const alarmList = ref([
  {
    serialNumber: 1,
    eventCode: '20AWSD20240618001',
    deviceName: '云物-温湿度传感器',
    location: '南区_科技研发楼_9F_办公区',
    alarmType: 'high_temperature',
    alarmLevel: 'level1',
    alarmTime: '2024-06-18 08:29:42',
    status: 'resolved',
    alarmValue: '35.2°C',
    description: '环境温度超过正常范围上限',
    processTime: '2024-06-18 09:15:00',
    processUser: '系统管理员',
    processNote: '已调整空调设置，温度恢复正常',
    trendData: true,
    statistics: {
      max: '36.5°C',
      min: '22.1°C',
      avg: '26.8°C',
      std: '2.3°C',
      exceedCount: 12,
      exceedRate: '8.5%'
    },
    relatedAlarms: [
      {
        time: '2024-06-18 08:25:15',
        device: '温湿度传感器002',
        type: 'high_temperature',
        value: '34.8°C',
        description: '相邻区域温度异常'
      },
      {
        time: '2024-06-18 08:30:22',
        device: '空调控制器A1',
        type: 'system_fault',
        value: '-',
        description: '空调系统响应异常'
      }
    ]
  },
  {
    serialNumber: 2,
    eventCode: '20AWSD20240618002',
    deviceName: '温湿度传感器003',
    location: '南区_科技研发楼_8F_会议室',
    alarmType: 'high_humidity',
    alarmLevel: 'level2',
    alarmTime: '2024-06-18 10:15:30',
    status: 'processing',
    alarmValue: '75%RH',
    description: '环境湿度过高，可能影响设备正常运行',
    processTime: '2024-06-18 10:30:00',
    processUser: '维护人员',
    processNote: '正在检查通风系统'
  },
  {
    serialNumber: 3,
    eventCode: '20AWSD20240618003',
    deviceName: '空气质量传感器001',
    location: '南区_科技研发楼_9F_办公区',
    alarmType: 'poor_air_quality',
    alarmLevel: 'level2',
    alarmTime: '2024-06-18 14:22:18',
    status: 'pending',
    alarmValue: 'PM2.5: 85μg/m³',
    description: 'PM2.5浓度超标，空气质量较差'
  },
  {
    serialNumber: 4,
    eventCode: '20AWSD20240618004',
    deviceName: '噪音传感器005',
    location: '南区_科技研发楼_7F_开放办公区',
    alarmType: 'high_noise',
    alarmLevel: 'level3',
    alarmTime: '2024-06-18 16:45:12',
    status: 'ignored',
    alarmValue: '68dB',
    description: '环境噪音超过舒适范围',
    processTime: '2024-06-18 17:00:00',
    processUser: '管理员',
    processNote: '施工期间正常噪音，已协调降低'
  }
])

// 方法
const getAlarmTypeColor = (type: string) => {
  const colors = {
    high_temperature: 'danger',
    low_temperature: 'primary',
    high_humidity: 'warning',
    low_humidity: 'info',
    poor_air_quality: 'danger',
    high_noise: 'warning',
    low_light: 'info',
    system_fault: 'danger'
  }
  return colors[type] || 'info'
}

const getAlarmTypeText = (type: string) => {
  const texts = {
    high_temperature: '高温报警',
    low_temperature: '低温报警',
    high_humidity: '高湿报警',
    low_humidity: '低湿报警',
    poor_air_quality: '空气质量',
    high_noise: '噪音超标',
    low_light: '光照不足',
    system_fault: '系统故障'
  }
  return texts[type] || type
}

const getAlarmLevelColor = (level: string) => {
  const colors = {
    level1: 'danger',
    level2: 'warning',
    level3: 'info'
  }
  return colors[level] || 'info'
}

const getAlarmLevelText = (level: string) => {
  const texts = {
    level1: '一级II',
    level2: '二级',
    level3: '三级'
  }
  return texts[level] || level
}

const getStatusColor = (status: string) => {
  const colors = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success',
    ignored: 'info'
  }
  return colors[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已处理',
    ignored: '已忽略'
  }
  return texts[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('刷新成功')
}

const handleView = (alarm: any) => {
  currentAlarm.value = alarm
  detailDialogVisible.value = true
  if (alarm.trendData) {
    nextTick(() => {
      initTrendChart()
    })
  }
}

const handleProcess = (alarm: any) => {
  currentAlarm.value = alarm
  resetProcessForm()
  processDialogVisible.value = true
}

const handleProcessFromDetail = () => {
  detailDialogVisible.value = false
  resetProcessForm()
  processDialogVisible.value = true
}

const handleAnalyze = (alarm: any) => {
  currentAlarm.value = alarm
  analyzeTab.value = 'trend'
  analyzeDialogVisible.value = true
  nextTick(() => {
    initAnalyzeCharts()
  })
}

const handleSelectionChange = (selection: any[]) => {
  selectedAlarms.value = selection
}

const handleBatchProcess = () => {
  if (selectedAlarms.value.length === 0) {
    ElMessage.warning('请选择要处理的告警记录')
    return
  }
  ElMessage.success('批量处理功能开发中...')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleProcessSubmit = async () => {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    
    if (currentAlarm.value) {
      currentAlarm.value.status = processForm.result === 'resolved' ? 'resolved' : 'processing'
      currentAlarm.value.processTime = new Date().toLocaleString('zh-CN')
      currentAlarm.value.processUser = '当前用户'
      currentAlarm.value.processNote = processForm.note
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
    measures: [],
    note: ''
  })
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  const chart = echarts.init(trendChartRef.value)
  
  const option = {
    title: {
      text: '环境参数趋势',
      textStyle: { color: '#ffffff' }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['温度', '湿度'],
      textStyle: { color: '#ffffff' }
    },
    xAxis: {
      type: 'category',
      data: ['06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00'],
      axisLabel: { color: '#ffffff' }
    },
    yAxis: [
      {
        type: 'value',
        name: '温度(°C)',
        axisLabel: { color: '#ffffff' }
      },
      {
        type: 'value',
        name: '湿度(%RH)',
        axisLabel: { color: '#ffffff' }
      }
    ],
    series: [
      {
        name: '温度',
        type: 'line',
        data: [22, 35.2, 32, 28, 26, 24, 23],
        smooth: true,
        itemStyle: { color: '#f56c6c' },
        markLine: {
          data: [{ yAxis: 30, name: '上限' }]
        }
      },
      {
        name: '湿度',
        type: 'line',
        yAxisIndex: 1,
        data: [45, 48, 52, 55, 58, 54, 50],
        smooth: true,
        itemStyle: { color: '#409eff' }
      }
    ]
  }
  
  chart.setOption(option)
}

const initAnalyzeCharts = () => {
  // 趋势图
  if (analyzeTrendChartRef.value) {
    const trendChart = echarts.init(analyzeTrendChartRef.value)
    const trendOption = {
      title: {
        text: '24小时数据趋势',
        textStyle: { color: '#ffffff' }
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: Array.from({length: 24}, (_, i) => `${i.toString().padStart(2, '0')}:00`),
        axisLabel: { color: '#ffffff' }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#ffffff' }
      },
      series: [{
        data: Array.from({length: 24}, () => Math.floor(Math.random() * 40) + 20),
        type: 'line',
        smooth: true,
        itemStyle: { color: '#67c23a' }
      }]
    }
    trendChart.setOption(trendOption)
  }
  
  // 统计图
  if (statisticsChartRef.value) {
    const statsChart = echarts.init(statisticsChartRef.value)
    const statsOption = {
      title: {
        text: '告警分布',
        textStyle: { color: '#ffffff' }
      },
      tooltip: {
        trigger: 'item'
      },
      series: [{
        name: '告警类型',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 35, name: '高温告警' },
          { value: 25, name: '高湿告警' },
          { value: 20, name: '空气质量' },
          { value: 20, name: '其他' }
        ]
      }]
    }
    statsChart.setOption(statsOption)
  }
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = alarmList.value.length
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.environment-alarm-records {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .filter-section {
    background: #2d2d2d;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    border: 1px solid #404040;

    .el-form-item {
      margin-bottom: 16px;
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
  }

  .alarm-detail {
    .el-descriptions {
      margin-bottom: 20px;
    }

    .trend-section {
      margin-top: 30px;

      h4 {
        margin: 0 0 16px 0;
        color: #ffffff;
        font-size: 16px;
        border-bottom: 1px solid #404040;
        padding-bottom: 8px;
      }

      .trend-chart {
        height: 300px;
      }
    }
  }

  .analyze-content {
    .analyze-chart {
      height: 400px;
    }

    .statistics-content {
      .statistics-chart {
        height: 300px;
      }

      .statistics-info {
        padding: 20px;
      }
    }
  }
}
</style>
