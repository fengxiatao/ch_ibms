<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="drainage-system dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>给排水</el-breadcrumb-item>
        <el-breadcrumb-item>排水系统</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="120px" :inline="true">
        <el-form-item label="请选择专业系统">
          <el-select v-model="filterForm.system" placeholder="请选择专业系统" clearable style="width: 200px;">
            <el-option label="给排水专业_排水系统_双泵" value="drainage_double_pump" />
            <el-option label="给排水专业_排水系统_单泵" value="drainage_single_pump" />
            <el-option label="给排水专业_排水系统_集水井" value="drainage_sump" />
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

    <!-- 排水系统设备列表 -->
    <div class="drainage-system-grid">
      <h3>排水系统</h3>
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
                  <span class="param-label">{{ getSecondParamLabel(device.type) }}：</span>
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
                <h4>{{ getFirstParamTitle(currentDevice.type) }}</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.param1Status">
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
          <el-row :gutter="20" style="margin-top: 20px;">
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
                <h4>{{ getSecondParamTitle(currentDevice.type) }}</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.param2Status">
                    <div class="status-dot"></div>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="param-group">
                <h4>水位</h4>
                <div class="param-chart">
                  <div class="status-indicator" :class="currentDevice.waterLevelStatus">
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
  Search, Cpu, VideoPlay, VideoPause, Warning, Close, Location
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 接口定义
interface DrainageDevice {
  id: number
  name: string
  code: string
  system: string
  location: string
  status: 'normal' | 'alarm' | 'fault' | 'stopped'
  iotDevice: boolean
  type: 'double_pump' | 'single_pump' | 'sump'
  controlMode: string
  operationMode: string
  startStopStatus: string
  runningStatus: string
  param1Status: string
  param2Status: string
  onlineStatus: string
  waterLevelStatus: string
}

// 响应式数据
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentDevice = ref<DrainageDevice | null>(null)
const activeTab = ref('logs')
const logsChartRef = ref()
const operationsChartRef = ref()
const historyChartRef = ref()

const filterForm = reactive({
  system: '',
  status: '',
  deviceName: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 9,
  total: 0
})

// 排水系统设备数据
const drainageDevices = ref<DrainageDevice[]>([
  {
    id: 1,
    name: '双泵006',
    code: 'GP_PS_PSSB_0006',
    system: '给排水专业_排水系统_双泵',
    location: '南区_科技研发楼_9F_办公区',
    status: 'normal',
    iotDevice: true,
    type: 'double_pump',
    controlMode: '启动',
    operationMode: '1号泵水泵故障状态',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  },
  {
    id: 2,
    name: '双泵007',
    code: 'GP_PS_PSSB_0007',
    system: '给排水专业_排水系统_双泵',
    location: '南区_科技研发楼_8F_办公区',
    status: 'fault',
    iotDevice: true,
    type: 'double_pump',
    controlMode: '停止',
    operationMode: '1号泵水泵故障状态',
    startStopStatus: 'fault',
    runningStatus: 'fault',
    param1Status: 'fault',
    param2Status: 'fault',
    onlineStatus: 'online',
    waterLevelStatus: 'alarm'
  },
  {
    id: 3,
    name: '双泵002',
    code: 'GP_PS_PSSB_0002',
    system: '给排水专业_排水系统_双泵',
    location: '南区_科技研发楼_7F_会议室',
    status: 'offline',
    iotDevice: false,
    type: 'double_pump',
    controlMode: '停止',
    operationMode: '1号泵水泵故障状态',
    startStopStatus: 'offline',
    runningStatus: 'offline',
    param1Status: 'offline',
    param2Status: 'offline',
    onlineStatus: 'offline',
    waterLevelStatus: 'offline'
  },
  {
    id: 4,
    name: '双泵003',
    code: 'GP_PS_PSSB_0003',
    system: '给排水专业_排水系统_双泵',
    location: '北区_办公楼_5F_休息区',
    status: 'normal',
    iotDevice: true,
    type: 'double_pump',
    controlMode: '启动',
    operationMode: '1号泵水泵故障状态',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  },
  {
    id: 5,
    name: '双泵001',
    code: 'GP_PS_PSSB_0001',
    system: '给排水专业_排水系统_双泵',
    location: '东区_会议中心_3F_大厅',
    status: 'alarm',
    iotDevice: true,
    type: 'double_pump',
    controlMode: '启动',
    operationMode: '1号泵水泵故障状态',
    startStopStatus: 'alarm',
    runningStatus: 'alarm',
    param1Status: 'alarm',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'alarm'
  },
  {
    id: 6,
    name: '双泵005',
    code: 'GP_PS_PSSB_0005',
    system: '给排水专业_排水系统_双泵',
    location: '南区_科技研发楼_6F_办公区',
    status: 'normal',
    iotDevice: true,
    type: 'double_pump',
    controlMode: '启动',
    operationMode: '1号泵水泵故障状态',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  },
  {
    id: 7,
    name: '双泵004',
    code: 'GP_PS_PSSB_0004',
    system: '给排水专业_排水系统_双泵',
    location: '北区_办公楼_4F_会议室',
    status: 'offline',
    iotDevice: false,
    type: 'double_pump',
    controlMode: '停止',
    operationMode: '1号泵水泵故障状态',
    startStopStatus: 'offline',
    runningStatus: 'offline',
    param1Status: 'offline',
    param2Status: 'offline',
    onlineStatus: 'offline',
    waterLevelStatus: 'offline'
  },
  {
    id: 8,
    name: '单泵001',
    code: 'GP_PS_DB_0001',
    system: '给排水专业_排水系统_单泵',
    location: '南区_科技研发楼_5F_办公区',
    status: 'normal',
    iotDevice: true,
    type: 'single_pump',
    controlMode: '启动',
    operationMode: '水泵故障状态',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  },
  {
    id: 9,
    name: '单泵007',
    code: 'GP_PS_DB_0007',
    system: '给排水专业_排水系统_单泵',
    location: '东区_会议中心_2F_休息区',
    status: 'normal',
    iotDevice: true,
    type: 'single_pump',
    controlMode: '启动',
    operationMode: '水泵故障状态',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  },
  {
    id: 10,
    name: '单泵004',
    code: 'GP_PS_DB_0004',
    system: '给排水专业_排水系统_单泵',
    location: '北区_办公楼_3F_大厅',
    status: 'normal',
    iotDevice: true,
    type: 'single_pump',
    controlMode: '启动',
    operationMode: '水泵故障状态',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  },
  {
    id: 11,
    name: '单泵005',
    code: 'GP_PS_DB_0005',
    system: '给排水专业_排水系统_单泵',
    location: '南区_科技研发楼_4F_会议室',
    status: 'alarm',
    iotDevice: true,
    type: 'single_pump',
    controlMode: '启动',
    operationMode: '水泵故障状态',
    startStopStatus: 'alarm',
    runningStatus: 'alarm',
    param1Status: 'alarm',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'alarm'
  },
  {
    id: 12,
    name: '单泵006',
    code: 'GP_PS_DB_0006',
    system: '给排水专业_排水系统_单泵',
    location: '东区_会议中心_1F_大厅',
    status: 'offline',
    iotDevice: false,
    type: 'single_pump',
    controlMode: '停止',
    operationMode: '水泵故障状态',
    startStopStatus: 'offline',
    runningStatus: 'offline',
    param1Status: 'offline',
    param2Status: 'offline',
    onlineStatus: 'offline',
    waterLevelStatus: 'offline'
  },
  {
    id: 13,
    name: '单泵003',
    code: 'GP_PS_DB_0003',
    system: '给排水专业_排水系统_单泵',
    location: '北区_办公楼_2F_办公区',
    status: 'fault',
    iotDevice: true,
    type: 'single_pump',
    controlMode: '停止',
    operationMode: '水泵故障状态',
    startStopStatus: 'fault',
    runningStatus: 'fault',
    param1Status: 'fault',
    param2Status: 'fault',
    onlineStatus: 'online',
    waterLevelStatus: 'fault'
  },
  {
    id: 14,
    name: '单泵002',
    code: 'GP_PS_DB_0002',
    system: '给排水专业_排水系统_单泵',
    location: '南区_科技研发楼_3F_休息区',
    status: 'normal',
    iotDevice: true,
    type: 'single_pump',
    controlMode: '启动',
    operationMode: '水泵故障状态',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  },
  {
    id: 15,
    name: '集水井007',
    code: 'GP_PS_JSJ_0007',
    system: '给排水专业_排水系统_集水井',
    location: '东区_会议中心_B1_地下室',
    status: 'normal',
    iotDevice: true,
    type: 'sump',
    controlMode: '启动',
    operationMode: '水位',
    startStopStatus: 'normal',
    runningStatus: 'normal',
    param1Status: 'normal',
    param2Status: 'normal',
    onlineStatus: 'online',
    waterLevelStatus: 'normal'
  }
])

// 计算属性
const filteredDevices = computed(() => {
  let filtered = drainageDevices.value
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

const getSecondParamLabel = (type: string) => {
  const labels = {
    double_pump: '1号泵水泵故障状态',
    single_pump: '水泵故障状态',
    sump: '水位'
  }
  return labels[type] || '参数状态'
}

const getFirstParamTitle = (type: string) => {
  const titles = {
    double_pump: '1号泵水泵故障状态',
    single_pump: '水泵故障状态',
    sump: '水位控制'
  }
  return titles[type] || '参数1'
}

const getSecondParamTitle = (type: string) => {
  const titles = {
    double_pump: '2号泵水泵故障状态',
    single_pump: '水泵手自状态',
    sump: '水位状态'
  }
  return titles[type] || '参数2'
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

const handleViewDetail = (device: DrainageDevice) => {
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
        data: [1.2, 1.5, 1.8, 2.1, 1.9, 1.6, 1.3],
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
          { value: 45, name: '自动运行' },
          { value: 25, name: '手动控制' },
          { value: 20, name: '维护模式' },
          { value: 10, name: '其他操作' }
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
          name: '水位',
          data: [1.2, 1.5, 1.3, 1.8, 1.6, 1.1, 1.4],
          type: 'line',
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '流量',
          data: [80, 95, 85, 110, 100, 75, 90],
          type: 'line',
          itemStyle: { color: '#e6a23c' }
        }
      ]
    })
  }
}

onMounted(() => {
  // 初始化统计数据
  pagination.total = drainageDevices.value.length
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.drainage-system {
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

  .drainage-system-grid {
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


