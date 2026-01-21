<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="environment-sensors dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>环境监测</el-breadcrumb-item>
        <el-breadcrumb-item>环境传感器</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 顶部信息区域 -->
    <div class="info-section">
      <el-row :gutter="20">
        <!-- 实时室外环境 -->
        <el-col :span="8">
          <div class="weather-card">
            <div class="weather-header">
              <el-icon><Sunny /></el-icon>
              <span>实时室外环境</span>
            </div>
            <div class="weather-content">
              <div class="temperature">
                <span class="temp-value">28°</span>
                <span class="temp-desc">武汉3级 湿度: 83%RH</span>
              </div>
              <div class="weather-status">晴</div>
            </div>
          </div>
        </el-col>

        <!-- 环境传感器分类统计 -->
        <el-col :span="8">
          <div class="sensor-stats-card">
            <div class="stats-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>环境传感器分类统计</span>
            </div>
            <div class="stats-content">
              <div class="stats-chart">
                <div class="chart-item">
                  <span class="chart-label">14/14</span>
                  <div class="chart-icon">
                    <el-icon><Monitor /></el-icon>
                  </div>
                </div>
                <div class="chart-desc">温湿度传感器</div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 实时环境告警 -->
        <el-col :span="8">
          <div class="alarm-card">
            <div class="alarm-header">
              <el-icon><Warning /></el-icon>
              <span>实时环境告警</span>
            </div>
            <div class="alarm-content">
              <div class="no-data">
                <el-empty description="暂无数据" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" label-width="120px" :inline="true">
        <el-form-item label="请选择专业系统">
          <el-select v-model="filterForm.systemType" placeholder="请选择专业系统" clearable style="width: 200px;">
            <el-option label="全部系统" value="" />
            <el-option label="建筑专业_传感器系统" value="building_sensor" />
            <el-option label="暖通专业_环境监测" value="hvac_environment" />
            <el-option label="电气专业_环境监测" value="electrical_environment" />
          </el-select>
        </el-form-item>
        <el-form-item label="请选择运行状态">
          <el-select v-model="filterForm.status" placeholder="请选择运行状态" clearable style="width: 180px;">
            <el-option label="全部状态" value="" />
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="故障" value="fault" />
          </el-select>
        </el-form-item>
        <el-form-item label="请输入设备名称">
          <el-input v-model="filterForm.deviceName" placeholder="请输入设备名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            更多筛选
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 传感器列表 -->
    <div class="sensors-section">
      <div class="section-header">
        <h3>环境传感器</h3>
        <div class="header-actions">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增传感器
          </el-button>
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <div class="sensors-grid">
        <div
          v-for="sensor in filteredSensors"
          :key="sensor.id"
          class="sensor-card"
          @click="handleSensorClick(sensor)"
        >
          <div class="sensor-header">
            <div class="sensor-icon">
              <el-icon>
                <component :is="getSensorIcon(sensor.type)" />
              </el-icon>
            </div>
            <div class="sensor-status">
              <el-tag :type="getStatusColor(sensor.status)" size="small">
                {{ getStatusText(sensor.status) }}
              </el-tag>
            </div>
          </div>

          <div class="sensor-info">
            <h4 class="sensor-name">{{ sensor.name }}</h4>
            <div class="sensor-location">
              <el-icon><Location /></el-icon>
              <span>{{ sensor.location }}</span>
            </div>
            <div class="sensor-system">
              <span>{{ sensor.systemType }}</span>
            </div>
          </div>

          <div class="sensor-data">
            <div class="data-item" v-for="data in sensor.data" :key="data.type">
              <span class="data-label">{{ data.label }}</span>
              <span class="data-value" :style="{ color: getDataColor(data.status) }">
                {{ data.value }}{{ data.unit }}
              </span>
            </div>
          </div>

          <div class="sensor-actions">
            <el-button link type="primary" @click.stop="handleEdit(sensor)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="success" @click.stop="handleControl(sensor)">
              <el-icon><Operation /></el-icon>
              控制
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 传感器详情弹框 -->
    <el-dialog v-model="detailDialogVisible" :title="currentSensor?.name || '传感器详情'" width="80%" draggable>
      <div v-if="currentSensor" class="sensor-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="设备名称">{{ currentSensor.name }}</el-descriptions-item>
            <el-descriptions-item label="专业系统">{{ currentSensor.systemType }}</el-descriptions-item>
            <el-descriptions-item label="设备编号">{{ currentSensor.deviceCode }}</el-descriptions-item>
            <el-descriptions-item label="空间位置">{{ currentSensor.location }}</el-descriptions-item>
            <el-descriptions-item label="物联设备">{{ currentSensor.iotDevice ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="运行状态">
              <el-tag :type="getStatusColor(currentSensor.status)">
                {{ getStatusText(currentSensor.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 实时数据 -->
        <div class="detail-section">
          <h4>实时数据</h4>
          <div class="realtime-data">
            <div class="data-tabs">
              <el-tabs v-model="activeTab">
                <el-tab-pane label="运行信息" name="runtime">
                  <div class="runtime-info">
                    <div class="status-indicator">
                      <div class="status-circle" :class="currentSensor.status"></div>
                      <div class="status-text">{{ getStatusText(currentSensor.status) }}</div>
                      <div class="status-time">{{ currentSensor.lastUpdateTime }}</div>
                    </div>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="操作日志" name="logs">
                  <div class="operation-logs">
                    <el-table :data="currentSensor.logs || []" border>
                      <el-table-column prop="time" label="时间" width="180" />
                      <el-table-column prop="operation" label="操作" width="120" />
                      <el-table-column prop="operator" label="操作人" width="120" />
                      <el-table-column prop="result" label="结果" width="100">
                        <template #default="{ row }">
                          <el-tag :type="row.result === 'success' ? 'success' : 'danger'">
                            {{ row.result === 'success' ? '成功' : '失败' }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
                    </el-table>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="历史数据" name="history">
                  <div class="history-data">
                    <div ref="historyChartRef" class="chart-container"></div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>

            <!-- 实时数据显示 -->
            <div class="realtime-values">
              <div class="value-card" v-for="data in currentSensor.data" :key="data.type">
                <div class="value-header">
                  <span class="value-label">{{ data.label }}</span>
                  <el-tag :type="data.status === 'normal' ? 'success' : 'warning'" size="small">
                    {{ data.status === 'normal' ? '正常' : '异常' }}
                  </el-tag>
                </div>
                <div class="value-content">
                  <span class="value-number">{{ data.value }}</span>
                  <span class="value-unit">{{ data.unit }}</span>
                </div>
                <div class="value-time">{{ data.updateTime }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleControlFromDetail">远程控制</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑传感器弹框 -->
    <el-dialog v-model="editDialogVisible" :title="isEditMode ? '编辑传感器' : '新增传感器'" width="60%" draggable>
      <el-form ref="sensorFormRef" :model="sensorForm" :rules="sensorRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="sensorForm.name" placeholder="请输入设备名称" />
            </el-form-item>
            <el-form-item label="专业系统" prop="systemType">
              <el-select v-model="sensorForm.systemType" placeholder="请选择专业系统" style="width: 100%;">
                <el-option label="建筑专业_传感器系统" value="building_sensor" />
                <el-option label="暖通专业_环境监测" value="hvac_environment" />
                <el-option label="电气专业_环境监测" value="electrical_environment" />
              </el-select>
            </el-form-item>
            <el-form-item label="传感器类型" prop="type">
              <el-select v-model="sensorForm.type" placeholder="请选择传感器类型" style="width: 100%;">
                <el-option label="温湿度传感器" value="temperature_humidity" />
                <el-option label="空气质量传感器" value="air_quality" />
                <el-option label="光照传感器" value="light" />
                <el-option label="噪音传感器" value="noise" />
                <el-option label="压力传感器" value="pressure" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编号" prop="deviceCode">
              <el-input v-model="sensorForm.deviceCode" placeholder="请输入设备编号" />
            </el-form-item>
            <el-form-item label="空间位置" prop="location">
              <el-input v-model="sensorForm.location" placeholder="请输入空间位置" />
            </el-form-item>
            <el-form-item label="物联设备">
              <el-switch v-model="sensorForm.iotDevice" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="设备描述">
          <el-input
            v-model="sensorForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入设备描述"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Sunny, DataAnalysis, Monitor, Warning, Search, Plus, Refresh,
  Location, Edit, Operation
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

// 响应式数据
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEditMode = ref(false)
const currentSensor = ref(null)
const activeTab = ref('runtime')
const historyChartRef = ref()

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  systemType: '',
  status: '',
  deviceName: ''
})

// 传感器表单
const sensorForm = reactive({
  name: '',
  systemType: '',
  type: '',
  deviceCode: '',
  location: '',
  iotDevice: true,
  description: ''
})

const sensorRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  systemType: [{ required: true, message: '请选择专业系统', trigger: 'change' }],
  type: [{ required: true, message: '请选择传感器类型', trigger: 'change' }],
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  location: [{ required: true, message: '请输入空间位置', trigger: 'blur' }]
}

// 传感器数据
const sensorList = ref([
  {
    id: 1,
    name: '温湿度传感器001',
    systemType: '建筑专业_传感器系统_温湿度传感器',
    type: 'temperature_humidity',
    deviceCode: 'JZ_CGQ_WSCG_0012',
    location: '南区_科技研发楼_9F_办公区',
    iotDevice: true,
    status: 'online',
    lastUpdateTime: '2024-09-30 17:05:16',
    data: [
      { type: 'temperature', label: '温度(℃)', value: '-', unit: '', status: 'normal', updateTime: '-' },
      { type: 'humidity', label: '湿度(%RH)', value: '-', unit: '', status: 'normal', updateTime: '-' }
    ],
    logs: [
      { time: '2024-09-30 17:05:16', operation: '状态查询', operator: '系统', result: 'success', remark: '设备在线' },
      { time: '2024-09-30 16:30:00', operation: '数据采集', operator: '系统', result: 'success', remark: '数据正常' }
    ]
  },
  {
    id: 2,
    name: '温湿度传感器010',
    systemType: '建筑专业_传感器系统_温湿度传感器',
    type: 'temperature_humidity',
    deviceCode: 'JZ_CGQ_WSCG_0010',
    location: '南区_科技研发楼_9F_办公区',
    iotDevice: true,
    status: 'online',
    lastUpdateTime: '2024-09-30 17:03:22',
    data: [
      { type: 'temperature', label: '温度(℃)', value: '-', unit: '', status: 'normal', updateTime: '-' },
      { type: 'humidity', label: '湿度(%RH)', value: '-', unit: '', status: 'normal', updateTime: '-' }
    ]
  },
  {
    id: 3,
    name: '温湿度传感器009',
    systemType: '建筑专业_传感器系统_温湿度传感器',
    type: 'temperature_humidity',
    deviceCode: 'JZ_CGQ_WSCG_0009',
    location: '南区_科技研发楼_9F_办公区',
    iotDevice: true,
    status: 'online',
    lastUpdateTime: '2024-09-30 17:01:45',
    data: [
      { type: 'temperature', label: '温度(℃)', value: '-', unit: '', status: 'normal', updateTime: '-' },
      { type: 'humidity', label: '湿度(%RH)', value: '-', unit: '', status: 'normal', updateTime: '-' }
    ]
  },
  {
    id: 4,
    name: '温湿度传感器006',
    systemType: '建筑专业_传感器系统_温湿度传感器',
    type: 'temperature_humidity',
    deviceCode: 'JZ_CGQ_WSCG_0006',
    location: '南区_科技研发楼_9F_办公区',
    iotDevice: true,
    status: 'online',
    lastUpdateTime: '2024-09-30 16:58:30',
    data: [
      { type: 'temperature', label: '温度(℃)', value: '-', unit: '', status: 'normal', updateTime: '-' },
      { type: 'humidity', label: '湿度(%RH)', value: '-', unit: '', status: 'normal', updateTime: '-' }
    ]
  },
  {
    id: 5,
    name: '温湿度传感器013',
    systemType: '建筑专业_传感器系统_温湿度传感器',
    type: 'temperature_humidity',
    deviceCode: 'JZ_CGQ_WSCG_0013',
    location: '南区_科技研发楼_9F_办公区',
    iotDevice: true,
    status: 'online',
    lastUpdateTime: '2024-09-30 16:55:12',
    data: [
      { type: 'temperature', label: '温度(℃)', value: '-', unit: '', status: 'normal', updateTime: '-' },
      { type: 'humidity', label: '湿度(%RH)', value: '-', unit: '', status: 'normal', updateTime: '-' }
    ]
  },
  {
    id: 6,
    name: '云物-温湿度传感器',
    systemType: '建筑专业_传感器系统_温湿度传感器',
    type: 'temperature_humidity',
    deviceCode: 'JZ_CGQ_WSCG_YW01',
    location: '南区_科技研发楼_9F_办公区',
    iotDevice: true,
    status: 'online',
    lastUpdateTime: '2024-09-30 16:52:08',
    data: [
      { type: 'temperature', label: '温度(℃)', value: '26.7', unit: '°C', status: 'normal', updateTime: '2024-09-30 16:52:08' },
      { type: 'humidity', label: '湿度(%RH)', value: '52.2', unit: '%RH', status: 'normal', updateTime: '2024-09-30 16:52:08' }
    ]
  }
])

const sensorFormRef = ref()

// 计算属性
const filteredSensors = computed(() => {
  let filtered = sensorList.value
  
  if (filterForm.systemType) {
    filtered = filtered.filter(sensor => sensor.systemType.includes(filterForm.systemType))
  }
  
  if (filterForm.status) {
    filtered = filtered.filter(sensor => sensor.status === filterForm.status)
  }
  
  if (filterForm.deviceName) {
    filtered = filtered.filter(sensor => 
      sensor.name.toLowerCase().includes(filterForm.deviceName.toLowerCase()) ||
      sensor.deviceCode.toLowerCase().includes(filterForm.deviceName.toLowerCase())
    )
  }
  
  return filtered
})

// 方法
const getSensorIcon = (type: string) => {
  const icons = {
    temperature_humidity: Monitor,
    air_quality: DataAnalysis,
    light: Sunny,
    noise: Warning,
    pressure: Monitor
  }
  return icons[type] || Monitor
}

const getStatusColor = (status: string) => {
  const colors = {
    online: 'success',
    offline: 'danger',
    fault: 'warning'
  }
  return colors[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    online: '在线',
    offline: '离线',
    fault: '故障'
  }
  return texts[status] || status
}

const getDataColor = (status: string) => {
  const colors = {
    normal: '#67c23a',
    warning: '#e6a23c',
    danger: '#f56c6c'
  }
  return colors[status] || '#909399'
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleAdd = () => {
  isEditMode.value = false
  resetSensorForm()
  editDialogVisible.value = true
}

const handleEdit = (sensor: any) => {
  isEditMode.value = true
  Object.assign(sensorForm, {
    name: sensor.name,
    systemType: sensor.systemType.split('_')[0] + '_' + sensor.systemType.split('_')[1],
    type: sensor.type,
    deviceCode: sensor.deviceCode,
    location: sensor.location,
    iotDevice: sensor.iotDevice,
    description: sensor.description || ''
  })
  editDialogVisible.value = true
}

const handleControl = (sensor: any) => {
  ElMessage.success(`远程控制 "${sensor.name}" 成功`)
}

const handleControlFromDetail = () => {
  if (currentSensor.value) {
    ElMessage.success(`远程控制 "${currentSensor.value.name}" 成功`)
  }
  detailDialogVisible.value = false
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('刷新成功')
}

const handleSensorClick = (sensor: any) => {
  currentSensor.value = sensor
  detailDialogVisible.value = true
  nextTick(() => {
    initHistoryChart()
  })
}

const handleSave = async () => {
  if (!sensorFormRef.value) return
  
  try {
    await sensorFormRef.value.validate()
    
    if (isEditMode.value) {
      ElMessage.success('传感器修改成功')
    } else {
      const newSensor = {
        id: Date.now(),
        ...sensorForm,
        status: 'online',
        lastUpdateTime: new Date().toLocaleString('zh-CN'),
        data: [
          { type: 'temperature', label: '温度(℃)', value: '-', unit: '', status: 'normal', updateTime: '-' },
          { type: 'humidity', label: '湿度(%RH)', value: '-', unit: '', status: 'normal', updateTime: '-' }
        ]
      }
      sensorList.value.push(newSensor)
      ElMessage.success('传感器创建成功')
    }
    
    editDialogVisible.value = false
    loadData()
  } catch {
    // 表单验证失败
  }
}

const resetSensorForm = () => {
  Object.assign(sensorForm, {
    name: '',
    systemType: '',
    type: '',
    deviceCode: '',
    location: '',
    iotDevice: true,
    description: ''
  })
}

const initHistoryChart = () => {
  if (!historyChartRef.value) return
  
  const chart = echarts.init(historyChartRef.value)
  
  const option = {
    title: {
      text: '历史数据趋势',
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
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
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
        data: [22, 23, 25, 28, 26, 24, 23],
        smooth: true,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '湿度',
        type: 'line',
        yAxisIndex: 1,
        data: [45, 48, 52, 55, 58, 54, 50],
        smooth: true,
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  
  chart.setOption(option)
}

const loadData = () => {
  pagination.total = filteredSensors.value.length
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use '@/styles/dark-theme.scss';

.environment-sensors {
  padding: 20px;

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .info-section {
    margin-bottom: 20px;

    .weather-card,
    .sensor-stats-card,
    .alarm-card {
      background: #2d2d2d;
      border: 1px solid #404040;
      border-radius: 12px;
      padding: 20px;
      height: 140px;

      .weather-header,
      .stats-header,
      .alarm-header {
        display: flex;
        align-items: center;
        margin-bottom: 16px;
        color: #ffffff;
        font-weight: 600;

        .el-icon {
          margin-right: 8px;
          color: #409eff;
        }
      }

      .weather-content {
        .temperature {
          display: flex;
          flex-direction: column;

          .temp-value {
            font-size: 36px;
            font-weight: 600;
            color: #ffffff;
          }

          .temp-desc {
            font-size: 14px;
            color: #cccccc;
          }
        }

        .weather-status {
          margin-top: 8px;
          color: #409eff;
          font-size: 16px;
        }
      }

      .stats-content {
        .stats-chart {
          text-align: center;

          .chart-item {
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 8px;

            .chart-label {
              font-size: 24px;
              font-weight: 600;
              color: #409eff;
              margin-right: 12px;
            }

            .chart-icon {
              font-size: 20px;
              color: #67c23a;
            }
          }

          .chart-desc {
            color: #cccccc;
            font-size: 14px;
          }
        }
      }

      .alarm-content {
        .no-data {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 60px;
        }
      }
    }
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

  .sensors-section {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        margin: 0;
        color: #ffffff;
        font-size: 18px;
      }

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }

    .sensors-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 20px;

      .sensor-card {
        background: #2d2d2d;
        border: 1px solid #404040;
        border-radius: 12px;
        padding: 20px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
          transform: translateY(-2px);
        }

        .sensor-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;

          .sensor-icon {
            width: 40px;
            height: 40px;
            background: linear-gradient(135deg, #409eff, #67c23a);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;

            .el-icon {
              font-size: 20px;
              color: white;
            }
          }
        }

        .sensor-info {
          margin-bottom: 16px;

          .sensor-name {
            margin: 0 0 8px 0;
            font-size: 16px;
            font-weight: 600;
            color: #ffffff;
          }

          .sensor-location {
            display: flex;
            align-items: center;
            color: #cccccc;
            font-size: 14px;
            margin-bottom: 4px;

            .el-icon {
              margin-right: 4px;
              font-size: 14px;
            }
          }

          .sensor-system {
            font-size: 12px;
            color: #909399;
          }
        }

        .sensor-data {
          margin-bottom: 16px;

          .data-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;

            .data-label {
              font-size: 14px;
              color: #cccccc;
            }

            .data-value {
              font-size: 14px;
              font-weight: 600;
            }
          }
        }

        .sensor-actions {
          display: flex;
          justify-content: space-between;
        }
      }
    }

    .pagination {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .sensor-detail {
    .detail-section {
      margin-bottom: 30px;

      h4 {
        margin: 0 0 16px 0;
        color: #ffffff;
        font-size: 16px;
        border-bottom: 1px solid #404040;
        padding-bottom: 8px;
      }
    }

    .realtime-data {
      .data-tabs {
        margin-bottom: 20px;
      }

      .runtime-info {
        text-align: center;
        padding: 20px;

        .status-indicator {
          .status-circle {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            margin: 0 auto 16px;

            &.online {
              background: #67c23a;
            }

            &.offline {
              background: #f56c6c;
            }

            &.fault {
              background: #e6a23c;
            }
          }

          .status-text {
            font-size: 18px;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 8px;
          }

          .status-time {
            font-size: 14px;
            color: #cccccc;
          }
        }
      }

      .chart-container {
        height: 300px;
      }

      .realtime-values {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 16px;

        .value-card {
          background: #363636;
          border-radius: 8px;
          padding: 16px;

          .value-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;

            .value-label {
              font-size: 14px;
              color: #cccccc;
            }
          }

          .value-content {
            margin-bottom: 8px;

            .value-number {
              font-size: 24px;
              font-weight: 600;
              color: #ffffff;
              margin-right: 4px;
            }

            .value-unit {
              font-size: 14px;
              color: #cccccc;
            }
          }

          .value-time {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }
}
</style>



