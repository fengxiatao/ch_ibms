<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="supply-exhaust-fans dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>送排风</el-breadcrumb-item>
        <el-breadcrumb-item>送排风机</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="120px" :inline="true">
        <el-form-item label="请选择专业系统">
          <el-select v-model="filterForm.system" placeholder="请选择专业系统" clearable style="width: 200px;">
            <el-option label="建筑专业_送排风系统_排烟机" value="building_exhaust_smoke" />
            <el-option label="建筑专业_送排风系统_送风机" value="building_supply_air" />
            <el-option label="建筑专业_送排风系统_排风机" value="building_exhaust_air" />
          </el-select>
        </el-form-item>
        <el-form-item label="请选择运行状态">
          <el-select v-model="filterForm.status" placeholder="请选择运行状态" clearable style="width: 150px;">
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="告警" value="alarm" />
            <el-option label="故障" value="fault" />
            <el-option label="停机" value="stopped" />
          </el-select>
        </el-form-item>
        <el-form-item label="请输入设备名称">
          <el-input 
            v-model="filterForm.deviceName" 
            placeholder="请输入设备名称" 
            clearable 
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计概览区域 -->
    <div class="statistics-overview">
      <el-row :gutter="20">
        <!-- 风机实时状态统计 -->
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-header">
              <el-icon><Monitor /></el-icon>
              <span>风机实时状态统计</span>
            </div>
            <div class="stat-content">
              <div class="stat-row">
                <div class="stat-item normal">
                  <div class="stat-icon">
                    <el-icon><Cpu /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.realtime.normal }}</div>
                    <div class="stat-label">正常</div>
                  </div>
                </div>
                <div class="stat-item running">
                  <div class="stat-icon">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.realtime.running }}</div>
                    <div class="stat-label">在线</div>
                  </div>
                </div>
                <div class="stat-item offline">
                  <div class="stat-icon">
                    <el-icon><VideoPause /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.realtime.offline }}</div>
                    <div class="stat-label">离线</div>
                  </div>
                </div>
                <div class="stat-item alarm">
                  <div class="stat-icon">
                    <el-icon><Warning /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.realtime.alarm }}</div>
                    <div class="stat-label">告警</div>
                  </div>
                </div>
                <div class="stat-item fault">
                  <div class="stat-icon">
                    <el-icon><Close /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.realtime.fault }}</div>
                    <div class="stat-label">故障</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 风机运行状态统计 -->
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>风机运行状态统计</span>
            </div>
            <div class="stat-content">
              <div class="stat-row">
                <div class="stat-item running">
                  <div class="stat-icon">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.operation.running }}</div>
                    <div class="stat-label">运行</div>
                  </div>
                </div>
                <div class="stat-item stopped">
                  <div class="stat-icon">
                    <el-icon><VideoPause /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.operation.stopped }}</div>
                    <div class="stat-label">停止</div>
                  </div>
                </div>
                <div class="stat-item alarm">
                  <div class="stat-icon">
                    <el-icon><Warning /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.operation.alarm }}</div>
                    <div class="stat-label">告警</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 风机故障告警次数统计 -->
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-header">
              <el-icon><Warning /></el-icon>
              <span>风机故障告警次数统计</span>
            </div>
            <div class="stat-tabs">
              <el-radio-group v-model="alarmPeriod" size="small">
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
            <div class="stat-content">
              <div class="alarm-stats">
                <div class="alarm-item">
                  <span class="alarm-label">排烟</span>
                  <span class="alarm-count">{{ getAlarmCount('smoke') }}</span>
                </div>
                <div class="alarm-item">
                  <span class="alarm-label">故障间</span>
                  <span class="alarm-count">{{ getAlarmCount('fault_room') }}</span>
                </div>
                <div class="alarm-item">
                  <span class="alarm-label">故障长</span>
                  <span class="alarm-count">{{ getAlarmCount('fault_duration') }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 风机故障告警备忘表 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="memo-card">
            <div class="card-header">
              <el-icon><DocumentChecked /></el-icon>
              <span>风机故障告警备忘表</span>
            </div>
            <div class="memo-content">
              <div class="memo-item">
                <span class="memo-label">设备名称</span>
                <span class="memo-label">故障时间</span>
                <span class="memo-label">故障长度</span>
              </div>
              <el-empty v-if="memoData.length === 0" description="暂无数据" />
              <div v-else v-for="item in memoData" :key="item.id" class="memo-item">
                <span class="memo-value">{{ item.deviceName }}</span>
                <span class="memo-value">{{ item.faultTime }}</span>
                <span class="memo-value">{{ item.faultDuration }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 风机告警记录 -->
        <el-col :span="12">
          <el-card class="alarm-record-card">
            <div class="card-header">
              <el-icon><Bell /></el-icon>
              <span>风机告警记录</span>
            </div>
            <div class="record-table-header">
              <span>事件编号</span>
              <span>设备名称</span>
              <span>空间位置</span>
              <span>告警类型</span>
              <span>告警等级</span>
              <span>告警时间</span>
              <span>处理状态</span>
            </div>
            <el-empty v-if="recentAlarms.length === 0" description="暂无数据" />
            <div v-else v-for="alarm in recentAlarms" :key="alarm.id" class="record-item">
              <span>{{ alarm.eventCode }}</span>
              <span>{{ alarm.deviceName }}</span>
              <span>{{ alarm.location }}</span>
              <span>{{ alarm.alarmType }}</span>
              <span>{{ alarm.alarmLevel }}</span>
              <span>{{ alarm.alarmTime }}</span>
              <span>{{ alarm.status }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 送排风机列表 -->
    <div class="fans-grid">
      <h3>送排风机</h3>
      <el-row :gutter="20">
        <el-col v-for="fan in pagedFans" :key="fan.id" :span="8">
          <el-card class="fan-card" shadow="hover" @click="handleViewDetail(fan)">
            <div class="fan-header">
              <div class="fan-icon" :class="getStatusClass(fan.status)">
                <el-icon><component :is="getStatusIcon(fan.status)" /></el-icon>
              </div>
              <div class="fan-info">
                <div class="fan-name">{{ fan.name }}</div>
                <div class="fan-code">{{ fan.code }}</div>
              </div>
              <div class="fan-status">
                <el-tag :type="getStatusTagType(fan.status)" size="small">
                  {{ getStatusText(fan.status) }}
                </el-tag>
              </div>
            </div>
            <div class="fan-body">
              <div class="fan-location">
                <el-icon><Location /></el-icon>
                <span>{{ fan.location }}</span>
              </div>
              <div class="fan-params">
                <div class="param-item">
                  <span class="param-label">频率反馈：</span>
                  <span class="param-value">{{ fan.frequency }}Hz</span>
                </div>
                <div class="param-item">
                  <span class="param-label">风机手自状态：</span>
                  <span class="param-value">{{ fan.controlMode }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[9, 18, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        background
        class="pagination-container"
      />
    </div>

    <!-- 设备详情对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="currentFan?.name" width="80%" draggable>
      <div v-if="currentFan" class="fan-detail">
        <!-- 基本信息 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="设备名称">{{ currentFan.name }}</el-descriptions-item>
              <el-descriptions-item label="专业系统">{{ currentFan.system }}</el-descriptions-item>
              <el-descriptions-item label="设备编码">{{ currentFan.code }}</el-descriptions-item>
              <el-descriptions-item label="空间位置">{{ currentFan.location }}</el-descriptions-item>
              <el-descriptions-item label="物联设备">{{ currentFan.iotDevice ? '是' : '否' }}</el-descriptions-item>
              <el-descriptions-item label="运行状态">
                <el-tag :type="getStatusTagType(currentFan.status)">{{ getStatusText(currentFan.status) }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <div class="control-panel">
              <h4>启停控制</h4>
              <div class="control-buttons">
                <el-radio-group v-model="currentFan.controlMode">
                  <el-radio-button label="停止">停止</el-radio-button>
                  <el-radio-button label="启动">启动</el-radio-button>
                </el-radio-group>
              </div>
              <div class="control-actions">
                <el-button type="primary" @click="handleControl">更多信息</el-button>
                <el-button type="success" @click="handleSaveControl">关闭</el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 运行参数图表 -->
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="运行日志" name="logs">
            <div class="chart-container">
              <div ref="logsChartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="operations">
            <div class="chart-container">
              <div ref="operationsChartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="历史数据" name="history">
            <div class="chart-container">
              <div ref="historyChartRef" style="width: 100%; height: 300px;"></div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <!-- 详细参数 -->
        <div class="detailed-params">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="param-group">
                <h4>运行状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentFan.runningStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>频率反馈(Hz)</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentFan.frequencyStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>在线状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentFan.onlineStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="8">
              <div class="param-group">
                <h4>风机故障状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentFan.faultStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>风机手自状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentFan.manualAutoStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, Monitor, Cpu, VideoPlay, VideoPause, Warning, Close, DataAnalysis,
  DocumentChecked, Bell, Location
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 接口定义
interface SupplyExhaustFan {
  id: number
  name: string
  code: string
  system: string
  location: string
  status: 'normal' | 'alarm' | 'fault' | 'stopped'
  iotDevice: boolean
  frequency: number
  controlMode: string
  runningStatus: string
  frequencyStatus: string
  onlineStatus: string
  faultStatus: string
  manualAutoStatus: string
}

interface AlarmRecord {
  id: number
  eventCode: string
  deviceName: string
  location: string
  alarmType: string
  alarmLevel: string
  alarmTime: string
  status: string
}

interface MemoItem {
  id: number
  deviceName: string
  faultTime: string
  faultDuration: string
}

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentFan = ref<SupplyExhaustFan | null>(null)
const activeTab = ref('logs')
const alarmPeriod = ref('day')
const logsChartRef = ref()
const operationsChartRef = ref()
const historyChartRef = ref()

const filterForm = reactive({
  system: '',
  status: '',
  deviceName: ''
})

const statistics = reactive({
  realtime: {
    normal: 7,
    running: 4,
    offline: 1,
    alarm: 1,
    fault: 1
  },
  operation: {
    running: 10,
    stopped: 9,
    alarm: 2
  },
  alarms: {
    day: { smoke: 0, fault_room: 0, fault_duration: 0 },
    week: { smoke: 2, fault_room: 1, fault_duration: 1 },
    month: { smoke: 5, fault_room: 3, fault_duration: 2 }
  }
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 9,
  total: 0
})

// 风机数据
const supplyExhaustFans = ref<SupplyExhaustFan[]>([
  {
    id: 1,
    name: '排烟机006',
    code: 'JZ_SFF_PYFJ_0006',
    system: '建筑专业_送排风系统_排烟机',
    location: '南区_科技研发楼_9F_办公区',
    status: 'normal',
    iotDevice: true,
    frequency: 50,
    controlMode: '自动',
    runningStatus: 'normal',
    frequencyStatus: 'normal',
    onlineStatus: 'online',
    faultStatus: 'normal',
    manualAutoStatus: 'auto'
  },
  {
    id: 2,
    name: '排烟机007',
    code: 'JZ_SFF_PYFJ_0007',
    system: '建筑专业_送排风系统_排烟机',
    location: '南区_科技研发楼_8F_办公区',
    status: 'normal',
    iotDevice: true,
    frequency: 48,
    controlMode: '自动',
    runningStatus: 'normal',
    frequencyStatus: 'normal',
    onlineStatus: 'online',
    faultStatus: 'normal',
    manualAutoStatus: 'auto'
  },
  {
    id: 3,
    name: '排烟机001',
    code: 'JZ_SFF_PYFJ_0001',
    system: '建筑专业_送排风系统_排烟机',
    location: '南区_科技研发楼_7F_会议室',
    status: 'normal',
    iotDevice: true,
    frequency: 52,
    controlMode: '自动',
    runningStatus: 'normal',
    frequencyStatus: 'normal',
    onlineStatus: 'online',
    faultStatus: 'normal',
    manualAutoStatus: 'auto'
  },
  {
    id: 4,
    name: '排烟机002',
    code: 'JZ_SFF_PYFJ_0002',
    system: '建筑专业_送排风系统_排烟机',
    location: '北区_办公楼_5F_休息区',
    status: 'alarm',
    iotDevice: true,
    frequency: 45,
    controlMode: '手动',
    runningStatus: 'alarm',
    frequencyStatus: 'low',
    onlineStatus: 'online',
    faultStatus: 'normal',
    manualAutoStatus: 'manual'
  },
  {
    id: 5,
    name: '排烟机005',
    code: 'JZ_SFF_PYFJ_0005',
    system: '建筑专业_送排风系统_排烟机',
    location: '东区_会议中心_3F_大厅',
    status: 'fault',
    iotDevice: false,
    frequency: 0,
    controlMode: '停机',
    runningStatus: 'fault',
    frequencyStatus: 'fault',
    onlineStatus: 'offline',
    faultStatus: 'fault',
    manualAutoStatus: 'manual'
  }
])

// 备忘数据
const memoData = ref<MemoItem[]>([])

// 告警记录
const recentAlarms = ref<AlarmRecord[]>([])

// 计算属性
const filteredFans = computed(() => {
  let filtered = supplyExhaustFans.value
  if (filterForm.system) {
    filtered = filtered.filter(fan => fan.system.includes(filterForm.system))
  }
  if (filterForm.status) {
    filtered = filtered.filter(fan => fan.status === filterForm.status)
  }
  if (filterForm.deviceName) {
    filtered = filtered.filter(fan => 
      fan.name.includes(filterForm.deviceName) || 
      fan.code.includes(filterForm.deviceName)
    )
  }
  return filtered
})

// 使用 watch 来更新 pagination.total
watch(filteredFans, (newValue) => {
  pagination.total = newValue.length
}, { immediate: true })

const pagedFans = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredFans.value.slice(start, end)
})

// 方法
const getStatusClass = (status: string) => {
  return `status-${status}`
}

const getStatusIcon = (status: string) => {
  const icons = {
    normal: Cpu,
    alarm: Warning,
    fault: Close,
    stopped: VideoPause
  }
  return icons[status] || Cpu
}

const getStatusTagType = (status: string) => {
  const types = {
    normal: 'success',
    alarm: 'warning',
    fault: 'danger',
    stopped: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    normal: '正常',
    alarm: '告警',
    fault: '故障',
    stopped: '停机'
  }
  return texts[status] || '未知'
}

const getAlarmCount = (type: string) => {
  return statistics.alarms[alarmPeriod.value][type] || 0
}

const handleSearch = () => {
  pagination.currentPage = 1
}

const handleSizeChange = (val: number) => {
  pagination.pageSize = val
}

const handleCurrentChange = (val: number) => {
  pagination.currentPage = val
}

const handleViewDetail = (fan: SupplyExhaustFan) => {
  currentFan.value = { ...fan }
  detailDialogVisible.value = true
  nextTick(() => {
    initCharts()
  })
}

const handleControl = () => {
  ElMessage.success('控制指令已发送')
}

const handleSaveControl = () => {
  detailDialogVisible.value = false
}

const initCharts = () => {
  // 运行日志图表
  if (logsChartRef.value) {
    const logsChart = echarts.init(logsChartRef.value)
    logsChart.setOption({
      title: { text: '24小时风机运行日志', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
      },
      yAxis: { type: 'value' },
      series: [{
        data: [45, 48, 50, 52, 49, 47, 46],
        type: 'line',
        smooth: true,
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  // 操作日志图表
  if (operationsChartRef.value) {
    const operationsChart = echarts.init(operationsChartRef.value)
    operationsChart.setOption({
      title: { text: '操作统计', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 40, name: '自动运行' },
          { value: 30, name: '手动控制' },
          { value: 20, name: '定时启停' },
          { value: 10, name: '其他操作' }
        ]
      }]
    })
  }

  // 历史数据图表
  if (historyChartRef.value) {
    const historyChart = echarts.init(historyChartRef.value)
    historyChart.setOption({
      title: { text: '历史运行数据', left: 'center' },
      tooltip: { trigger: 'axis' },
      legend: { top: 30 },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '频率',
          data: [48, 50, 49, 52, 51, 47, 49],
          type: 'line',
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '运行时长',
          data: [8, 10, 9, 12, 11, 6, 8],
          type: 'line',
          itemStyle: { color: '#e6a23c' }
        }
      ]
    })
  }
}

onMounted(() => {
  // 初始化统计数据
  pagination.total = supplyExhaustFans.value.length
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.supply-exhaust-fans {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .filter-section {
    background: #2d2d2d;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
  }

  .statistics-overview {
    margin-bottom: 30px;

    .stat-card {
      .stat-header {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;

        .el-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }

      .stat-content {
        .stat-row {
          display: flex;
          gap: 15px;
          flex-wrap: wrap;

          .stat-item {
            display: flex;
            align-items: center;
            padding: 10px;
            border-radius: 6px;
            min-width: 80px;

            &.normal {
              background: rgba(103, 194, 58, 0.1);
              border: 1px solid #67c23a;
            }

            &.running {
              background: rgba(64, 158, 255, 0.1);
              border: 1px solid #409eff;
            }

            &.offline, &.stopped {
              background: rgba(144, 147, 153, 0.1);
              border: 1px solid #909399;
            }

            &.alarm {
              background: rgba(230, 162, 60, 0.1);
              border: 1px solid #e6a23c;
            }

            &.fault {
              background: rgba(245, 108, 108, 0.1);
              border: 1px solid #f56c6c;
            }

            .stat-icon {
              margin-right: 8px;
              font-size: 20px;
            }

            .stat-info {
              .stat-number {
                font-size: 20px;
                font-weight: bold;
                color: #ffffff;
                line-height: 1;
              }

              .stat-label {
                font-size: 12px;
                color: #b0b0b0;
                margin-top: 2px;
              }
            }
          }
        }
      }

      .stat-tabs {
        margin-bottom: 15px;
        text-align: center;
      }

      .alarm-stats {
        .alarm-item {
          display: flex;
          justify-content: space-between;
          padding: 8px 0;
          border-bottom: 1px solid #404040;

          &:last-child {
            border-bottom: none;
          }

          .alarm-label {
            color: #b0b0b0;
          }

          .alarm-count {
            color: #ffffff;
            font-weight: bold;
          }
        }
      }
    }

    .memo-card, .alarm-record-card {
      .card-header {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;

        .el-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }

      .memo-content {
        .memo-item {
          display: flex;
          justify-content: space-between;
          padding: 8px 0;
          border-bottom: 1px solid #404040;
          font-size: 14px;

          &:first-child {
            font-weight: 600;
            color: #ffffff;
          }

          &:not(:first-child) {
            color: #b0b0b0;
          }

          &:last-child {
            border-bottom: none;
          }
        }
      }

      .record-table-header {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 2px solid #404040;
        font-weight: 600;
        color: #ffffff;
        font-size: 14px;
        margin-bottom: 10px;
      }

      .record-item {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid #404040;
        font-size: 12px;
        color: #b0b0b0;

        &:last-child {
          border-bottom: none;
        }
      }
    }
  }

  .fans-grid {
    h3 {
      color: #ffffff;
      margin: 0 0 20px 0;
      font-size: 18px;
    }

    .fan-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
      }

      .fan-header {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        .fan-icon {
          width: 50px;
          height: 50px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          font-size: 24px;
          color: white;

          &.status-normal {
            background: #67c23a;
          }

          &.status-alarm {
            background: #e6a23c;
          }

          &.status-fault {
            background: #f56c6c;
          }

          &.status-stopped {
            background: #909399;
          }
        }

        .fan-info {
          flex: 1;

          .fan-name {
            font-size: 16px;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 4px;
          }

          .fan-code {
            font-size: 12px;
            color: #b0b0b0;
          }
        }

        .fan-status {
          margin-left: 12px;
        }
      }

      .fan-body {
        .fan-location {
          display: flex;
          align-items: center;
          color: #b0b0b0;
          font-size: 14px;
          margin-bottom: 12px;

          .el-icon {
            margin-right: 6px;
          }
        }

        .fan-params {
          .param-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            font-size: 14px;

            .param-label {
              color: #b0b0b0;
            }

            .param-value {
              color: #ffffff;
              font-weight: 500;
            }
          }
        }
      }
    }

    .pagination-container {
      margin-top: 30px;
      text-align: center;
    }
  }

  .fan-detail {
    .control-panel {
      background: #2d2d2d;
      padding: 20px;
      border-radius: 8px;

      h4 {
        color: #ffffff;
        margin: 0 0 20px 0;
        font-size: 16px;
      }

      .control-buttons {
        margin-bottom: 20px;
      }

      .control-actions {
        text-align: center;
        margin-top: 30px;

        .el-button {
          margin: 0 10px;
        }
      }
    }

    .detail-tabs {
      margin: 30px 0;
    }

    .chart-container {
      background: #2d2d2d;
      border-radius: 8px;
      padding: 20px;
    }

    .detailed-params {
      margin-top: 30px;

      .param-group {
        background: #2d2d2d;
        padding: 20px;
        border-radius: 8px;
        text-align: center;

        h4 {
          color: #ffffff;
          margin: 0 0 15px 0;
          font-size: 14px;
        }

        .param-chart {
          .status-indicator {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            margin: 0 auto;
            display: flex;
            align-items: center;
            justify-content: center;

            &.normal, &.online, &.auto {
              background: #67c23a;
            }

            &.alarm, &.low, &.manual {
              background: #e6a23c;
            }

            &.fault, &.offline {
              background: #f56c6c;
            }

            .status-dot {
              width: 20px;
              height: 20px;
              border-radius: 50%;
              background: white;
            }
          }
        }
      }
    }
  }
}
</style>


