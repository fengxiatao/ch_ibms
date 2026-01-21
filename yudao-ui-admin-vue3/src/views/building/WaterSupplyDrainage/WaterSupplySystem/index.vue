<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="water-supply-system dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>给排水</el-breadcrumb-item>
        <el-breadcrumb-item>给水系统</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="120px" :inline="true">
        <el-form-item label="请选择专业系统">
          <el-select v-model="filterForm.system" placeholder="请选择专业系统" clearable style="width: 200px;">
            <el-option label="给排水专业_给水系统_生活水泵" value="water_supply_domestic" />
            <el-option label="给排水专业_给水系统_消防水泵" value="water_supply_fire" />
            <el-option label="给排水专业_给水系统_冷却水泵" value="water_supply_cooling" />
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
        <!-- 给排水设备实时状态统计 -->
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-header">
              <el-icon><Monitor /></el-icon>
              <span>给排水设备实时状态统计</span>
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

        <!-- 水泵实时运行状态 -->
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-header">
              <el-icon><House /></el-icon>
              <span>水泵实时运行状态</span>
            </div>
            <div class="stat-content">
              <div class="stat-row">
                <div class="stat-item running">
                  <div class="stat-icon">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.pump.running }}</div>
                    <div class="stat-label">运行</div>
                  </div>
                </div>
                <div class="stat-item stopped">
                  <div class="stat-icon">
                    <el-icon><VideoPause /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.pump.stopped }}</div>
                    <div class="stat-label">在线</div>
                  </div>
                </div>
                <div class="stat-item offline">
                  <div class="stat-icon">
                    <el-icon><Close /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.pump.offline }}</div>
                    <div class="stat-label">离线</div>
                  </div>
                </div>
                <div class="stat-item alarm">
                  <div class="stat-icon">
                    <el-icon><Warning /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.pump.alarm }}</div>
                    <div class="stat-label">告警</div>
                  </div>
                </div>
                <div class="stat-item fault">
                  <div class="stat-icon">
                    <el-icon><Close /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-number">{{ statistics.pump.fault }}</div>
                    <div class="stat-label">故障</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 给排水告警类型统计 -->
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-header">
              <el-icon><Warning /></el-icon>
              <span>给排水告警类型统计</span>
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
                  <span class="alarm-label">压力异常</span>
                  <span class="alarm-count">{{ getAlarmCount('pressure') }}</span>
                </div>
                <div class="alarm-item">
                  <span class="alarm-label">流量异常</span>
                  <span class="alarm-count">{{ getAlarmCount('flow') }}</span>
                </div>
                <div class="alarm-item">
                  <span class="alarm-label">设备故障</span>
                  <span class="alarm-count">{{ getAlarmCount('device_fault') }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 生活水泵总管压力图表 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="chart-card">
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>生活水泵总管压力</span>
            </div>
            <div class="chart-legend">
              <span class="legend-item supply">— 供区</span>
              <span class="legend-item return">— 回区</span>
            </div>
            <div class="chart-container">
              <div ref="pressureChartRef" style="width: 100%; height: 250px;"></div>
            </div>
          </el-card>
        </el-col>

        <!-- 给排水告警记录 -->
        <el-col :span="12">
          <el-card class="alarm-record-card">
            <div class="card-header">
              <el-icon><Bell /></el-icon>
              <span>给排水告警记录</span>
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

    <!-- 给水系统设备列表 -->
    <div class="water-system-grid">
      <h3>给水系统</h3>
      <el-row :gutter="20">
        <el-col v-for="device in pagedDevices" :key="device.id" :span="8">
          <el-card class="device-card" shadow="hover" @click="handleViewDetail(device)">
            <div class="device-header">
              <div class="device-icon" :class="getStatusClass(device.status)">
                <el-icon><component :is="getStatusIcon(device.status)" /></el-icon>
              </div>
              <div class="device-info">
                <div class="device-name">{{ device.name }}</div>
                <div class="device-code">{{ device.code }}</div>
              </div>
              <div class="device-status">
                <el-tag :type="getStatusTagType(device.status)" size="small">
                  {{ getStatusText(device.status) }}
                </el-tag>
              </div>
            </div>
            <div class="device-body">
              <div class="device-location">
                <el-icon><Location /></el-icon>
                <span>{{ device.location }}</span>
              </div>
              <div class="device-params">
                <div class="param-item">
                  <span class="param-label">启停控制：</span>
                  <span class="param-value">{{ device.controlMode }}</span>
                </div>
                <div class="param-item">
                  <span class="param-label">{{ device.type === 'pump' ? '水泵手自状态' : '1号泵水泵故障状态' }}：</span>
                  <span class="param-value">{{ device.operationMode }}</span>
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
    <el-dialog v-model="detailDialogVisible" :title="currentDevice?.name" width="80%" draggable>
      <div v-if="currentDevice" class="device-detail">
        <!-- 基本信息 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="基本信息" :column="1" border>
              <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
              <el-descriptions-item label="专业系统">{{ currentDevice.system }}</el-descriptions-item>
              <el-descriptions-item label="设备编码">{{ currentDevice.code }}</el-descriptions-item>
              <el-descriptions-item label="空间位置">{{ currentDevice.location }}</el-descriptions-item>
              <el-descriptions-item label="物联设备">{{ currentDevice.iotDevice ? '是' : '否' }}</el-descriptions-item>
              <el-descriptions-item label="运行状态">
                <el-tag :type="getStatusTagType(currentDevice.status)">{{ getStatusText(currentDevice.status) }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <div class="control-panel">
              <h4>暂无控制信息</h4>
              <div class="control-buttons">
                <el-radio-group v-model="currentDevice.controlMode">
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
                <h4>启停控制</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.startStopStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>运行状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.runningStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>水泵故障状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.faultStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="8">
              <div class="param-group">
                <h4>1号泵水泵故障状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.pump1FaultStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>频率反馈(Hz)</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.frequencyStatus">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>在线状态</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.onlineStatus">
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
  House, Bell, Location
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 接口定义
interface WaterSupplyDevice {
  id: number
  name: string
  code: string
  system: string
  location: string
  status: 'normal' | 'alarm' | 'fault' | 'stopped'
  iotDevice: boolean
  type: 'pump' | 'tank' | 'valve'
  controlMode: string
  operationMode: string
  startStopStatus: string
  runningStatus: string
  faultStatus: string
  pump1FaultStatus: string
  frequencyStatus: string
  onlineStatus: string
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

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentDevice = ref<WaterSupplyDevice | null>(null)
const activeTab = ref('logs')
const alarmPeriod = ref('day')
const pressureChartRef = ref()
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
    normal: 28,
    running: 15,
    offline: 6,
    alarm: 3,
    fault: 4
  },
  pump: {
    running: 7,
    stopped: 4,
    offline: 2,
    alarm: 0,
    fault: 1
  },
  alarms: {
    day: { pressure: 0, flow: 0, device_fault: 0 },
    week: { pressure: 2, flow: 1, device_fault: 1 },
    month: { pressure: 5, flow: 3, device_fault: 2 }
  }
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 9,
  total: 0
})

// 给水系统设备数据
const waterSupplyDevices = ref<WaterSupplyDevice[]>([
  {
    id: 1,
    name: '生活水泵006',
    code: 'GP_GS_SHSB_0006',
    system: '给排水专业_给水系统_生活水泵',
    location: '南区_科技研发楼_9F_办公区',
    status: 'normal',
    iotDevice: true,
    type: 'pump',
    controlMode: '启动',
    operationMode: '自动',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    faultStatus: 'normal',
    pump1FaultStatus: 'normal',
    frequencyStatus: 'normal',
    onlineStatus: 'online'
  },
  {
    id: 2,
    name: '生活水泵007',
    code: 'GP_GS_SHSB_0007',
    system: '给排水专业_给水系统_生活水泵',
    location: '南区_科技研发楼_8F_办公区',
    status: 'normal',
    iotDevice: true,
    type: 'pump',
    controlMode: '启动',
    operationMode: '自动',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    faultStatus: 'normal',
    pump1FaultStatus: 'normal',
    frequencyStatus: 'normal',
    onlineStatus: 'online'
  },
  {
    id: 3,
    name: '生活水泵003',
    code: 'GP_GS_SHSB_0003',
    system: '给排水专业_给水系统_生活水泵',
    location: '南区_科技研发楼_7F_会议室',
    status: 'fault',
    iotDevice: true,
    type: 'pump',
    controlMode: '停止',
    operationMode: '手动',
    startStopStatus: 'fault',
    runningStatus: 'fault',
    faultStatus: 'fault',
    pump1FaultStatus: 'fault',
    frequencyStatus: 'fault',
    onlineStatus: 'offline'
  },
  {
    id: 4,
    name: '生活水泵002',
    code: 'GP_GS_SHSB_0002',
    system: '给排水专业_给水系统_生活水泵',
    location: '北区_办公楼_5F_休息区',
    status: 'offline',
    iotDevice: false,
    type: 'pump',
    controlMode: '停止',
    operationMode: '离线',
    startStopStatus: 'offline',
    runningStatus: 'offline',
    faultStatus: 'normal',
    pump1FaultStatus: 'normal',
    frequencyStatus: 'offline',
    onlineStatus: 'offline'
  },
  {
    id: 5,
    name: '生活水泵004',
    code: 'GP_GS_SHSB_0004',
    system: '给排水专业_给水系统_生活水泵',
    location: '东区_会议中心_3F_大厅',
    status: 'offline',
    iotDevice: true,
    type: 'pump',
    controlMode: '停止',
    operationMode: '离线',
    startStopStatus: 'offline',
    runningStatus: 'offline',
    faultStatus: 'normal',
    pump1FaultStatus: 'normal',
    frequencyStatus: 'offline',
    onlineStatus: 'offline'
  }
])

// 告警记录
const recentAlarms = ref<AlarmRecord[]>([])

// 计算属性
const filteredDevices = computed(() => {
  let filtered = waterSupplyDevices.value
  if (filterForm.system) {
    filtered = filtered.filter(device => device.system.includes(filterForm.system))
  }
  if (filterForm.status) {
    filtered = filtered.filter(device => device.status === filterForm.status)
  }
  if (filterForm.deviceName) {
    filtered = filtered.filter(device => 
      device.name.includes(filterForm.deviceName) || 
      device.code.includes(filterForm.deviceName)
    )
  }
  return filtered
})

// 使用 watch 来更新 pagination.total
watch(filteredDevices, (newValue) => {
  pagination.total = newValue.length
}, { immediate: true })

const pagedDevices = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredDevices.value.slice(start, end)
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
    stopped: VideoPause,
    offline: Close
  }
  return icons[status] || Cpu
}

const getStatusTagType = (status: string) => {
  const types = {
    normal: 'success',
    alarm: 'warning',
    fault: 'danger',
    stopped: 'info',
    offline: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    normal: '正常',
    alarm: '告警',
    fault: '故障',
    stopped: '停机',
    offline: '离线'
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

const handleViewDetail = (device: WaterSupplyDevice) => {
  currentDevice.value = { ...device }
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
  // 压力图表
  if (pressureChartRef.value) {
    const pressureChart = echarts.init(pressureChartRef.value)
    pressureChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { show: false },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#b0b0b0' }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 0.8,
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#b0b0b0' },
        splitLine: { lineStyle: { color: '#404040' } }
      },
      series: [
        {
          name: '供区',
          data: [0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1],
          type: 'line',
          smooth: true,
          itemStyle: { color: '#e6a23c' }
        },
        {
          name: '回区',
          data: [0.3, 0.25, 0.2, 0.15, 0.1, 0.05, 0.02],
          type: 'line',
          smooth: true,
          itemStyle: { color: '#409eff' }
        }
      ]
    })
  }

  // 运行日志图表
  if (logsChartRef.value) {
    const logsChart = echarts.init(logsChartRef.value)
    logsChart.setOption({
      title: { text: '24小时运行日志', left: 'center', textStyle: { color: '#ffffff' } },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#b0b0b0' }
      },
      yAxis: { 
        type: 'value',
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#b0b0b0' },
        splitLine: { lineStyle: { color: '#404040' } }
      },
      series: [{
        data: [0.4, 0.45, 0.5, 0.55, 0.52, 0.48, 0.42],
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
      title: { text: '操作统计', left: 'center', textStyle: { color: '#ffffff' } },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 50, name: '自动运行' },
          { value: 30, name: '手动控制' },
          { value: 15, name: '维护模式' },
          { value: 5, name: '其他操作' }
        ]
      }]
    })
  }

  // 历史数据图表
  if (historyChartRef.value) {
    const historyChart = echarts.init(historyChartRef.value)
    historyChart.setOption({
      title: { text: '历史运行数据', left: 'center', textStyle: { color: '#ffffff' } },
      tooltip: { trigger: 'axis' },
      legend: { top: 30, textStyle: { color: '#ffffff' } },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#b0b0b0' }
      },
      yAxis: { 
        type: 'value',
        axisLine: { lineStyle: { color: '#404040' } },
        axisLabel: { color: '#b0b0b0' },
        splitLine: { lineStyle: { color: '#404040' } }
      },
      series: [
        {
          name: '压力',
          data: [0.45, 0.48, 0.46, 0.52, 0.49, 0.44, 0.47],
          type: 'line',
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '流量',
          data: [120, 130, 125, 140, 135, 115, 128],
          type: 'line',
          itemStyle: { color: '#e6a23c' }
        }
      ]
    })
  }
}

onMounted(() => {
  // 初始化统计数据
  pagination.total = waterSupplyDevices.value.length
  
  // 初始化压力图表
  nextTick(() => {
    if (pressureChartRef.value) {
      const pressureChart = echarts.init(pressureChartRef.value)
      pressureChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { show: false },
        xAxis: {
          type: 'category',
          data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
          axisLine: { lineStyle: { color: '#404040' } },
          axisLabel: { color: '#b0b0b0' }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 0.8,
          axisLine: { lineStyle: { color: '#404040' } },
          axisLabel: { color: '#b0b0b0' },
          splitLine: { lineStyle: { color: '#404040' } }
        },
        series: [
          {
            name: '供区',
            data: [0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1],
            type: 'line',
            smooth: true,
            itemStyle: { color: '#e6a23c' }
          },
          {
            name: '回区',
            data: [0.3, 0.25, 0.2, 0.15, 0.1, 0.05, 0.02],
            type: 'line',
            smooth: true,
            itemStyle: { color: '#409eff' }
          }
        ]
      })
    }
  })
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.water-supply-system {
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

    .chart-card {
      .card-header {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;

        .el-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }

      .chart-legend {
        margin-bottom: 10px;
        text-align: center;

        .legend-item {
          margin: 0 20px;
          color: #b0b0b0;

          &.supply {
            color: #e6a23c;
          }

          &.return {
            color: #409eff;
          }
        }
      }
    }

    .alarm-record-card {
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

  .water-system-grid {
    h3 {
      color: #ffffff;
      margin: 0 0 20px 0;
      font-size: 18px;
    }

    .device-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
      }

      .device-header {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        .device-icon {
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

          &.status-stopped, &.status-offline {
            background: #909399;
          }
        }

        .device-info {
          flex: 1;

          .device-name {
            font-size: 16px;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 4px;
          }

          .device-code {
            font-size: 12px;
            color: #b0b0b0;
          }
        }

        .device-status {
          margin-left: 12px;
        }
      }

      .device-body {
        .device-location {
          display: flex;
          align-items: center;
          color: #b0b0b0;
          font-size: 14px;
          margin-bottom: 12px;

          .el-icon {
            margin-right: 6px;
          }
        }

        .device-params {
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

  .device-detail {
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

            &.normal, &.online {
              background: #67c23a;
            }

            &.alarm {
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


