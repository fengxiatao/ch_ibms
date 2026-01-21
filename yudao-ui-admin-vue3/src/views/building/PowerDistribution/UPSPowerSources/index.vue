<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>变配电</el-breadcrumb-item>
        <el-breadcrumb-item>UPS电源</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ upsStats.total }}</div>
                <div class="stats-label">UPS总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon online">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ upsStats.online }}</div>
                <div class="stats-label">在线运行</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon battery">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ upsStats.avgBattery }}%</div>
                <div class="stats-label">平均电量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon load">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ upsStats.avgLoad }}%</div>
                <div class="stats-label">平均负载</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchText"
          placeholder="搜索UPS设备..."
          style="width: 300px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px; margin-left: 10px;">
          <el-option label="全部" value="" />
          <el-option label="在线" value="online" />
          <el-option label="离线" value="offline" />
          <el-option label="告警" value="alarm" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加UPS
        </el-button>
      </div>
    </div>

    <!-- UPS设备卡片列表 -->
    <div class="ups-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="ups in filteredUpsList" :key="ups.id">
          <el-card class="ups-card" :class="ups.status">
            <template #header>
              <div class="card-header">
                <div class="ups-name">
                  <el-icon><Lightning /></el-icon>
                  <span>{{ ups.name }}</span>
                </div>
                <div class="ups-status">
                  <el-tag :type="getStatusType(ups.status)">{{ getStatusText(ups.status) }}</el-tag>
                </div>
              </div>
            </template>
            
            <div class="ups-content">
              <!-- 基本信息 -->
              <div class="info-section">
                <div class="info-item">
                  <span class="label">型号:</span>
                  <span class="value">{{ ups.model }}</span>
                </div>
                <div class="info-item">
                  <span class="label">位置:</span>
                  <span class="value">{{ ups.location }}</span>
                </div>
                <div class="info-item">
                  <span class="label">额定功率:</span>
                  <span class="value">{{ ups.ratedPower }}kVA</span>
                </div>
              </div>
              
              <!-- 运行参数 -->
              <div class="params-section">
                <div class="param-item">
                  <div class="param-header">
                    <span>输入电压</span>
                    <span class="param-value">{{ ups.inputVoltage }}V</span>
                  </div>
                  <el-progress :percentage="getVoltagePercentage(ups.inputVoltage)" :stroke-width="8" />
                </div>
                
                <div class="param-item">
                  <div class="param-header">
                    <span>输出电压</span>
                    <span class="param-value">{{ ups.outputVoltage }}V</span>
                  </div>
                  <el-progress :percentage="getVoltagePercentage(ups.outputVoltage)" :stroke-width="8" />
                </div>
                
                <div class="param-item">
                  <div class="param-header">
                    <span>负载率</span>
                    <span class="param-value">{{ ups.loadPercentage }}%</span>
                  </div>
                  <el-progress 
                    :percentage="ups.loadPercentage" 
                    :stroke-width="8"
                    :color="getLoadColor(ups.loadPercentage)"
                  />
                </div>
                
                <div class="param-item">
                  <div class="param-header">
                    <span>电池电量</span>
                    <span class="param-value">{{ ups.batteryLevel }}%</span>
                  </div>
                  <el-progress 
                    :percentage="ups.batteryLevel" 
                    :stroke-width="8"
                    :color="getBatteryColor(ups.batteryLevel)"
                  />
                </div>
              </div>
              
              <!-- 状态指示器 -->
              <div class="status-indicators">
                <div class="indicator" :class="{ active: ups.mainsPower }">
                  <el-icon><Connection /></el-icon>
                  <span>市电</span>
                </div>
                <div class="indicator" :class="{ active: ups.batteryMode }">
                  <el-icon><Cpu /></el-icon>
                  <span>电池</span>
                </div>
                <div class="indicator" :class="{ active: ups.bypass }">
                  <el-icon><Switch /></el-icon>
                  <span>旁路</span>
                </div>
                <div class="indicator" :class="{ active: ups.alarm }">
                  <el-icon><Warning /></el-icon>
                  <span>告警</span>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button size="small" @click="handleViewDetail(ups)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button size="small" type="warning" @click="handleMonitor(ups)">
                  <el-icon><Monitor /></el-icon>
                  监控
                </el-button>
                <el-button size="small" type="primary" @click="handleEdit(ups)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- UPS详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="UPS设备详情" width="900px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="设备名称">{{ currentUps?.name }}</el-descriptions-item>
            <el-descriptions-item label="设备型号">{{ currentUps?.model }}</el-descriptions-item>
            <el-descriptions-item label="制造商">{{ currentUps?.manufacturer }}</el-descriptions-item>
            <el-descriptions-item label="额定功率">{{ currentUps?.ratedPower }}kVA</el-descriptions-item>
            <el-descriptions-item label="安装位置">{{ currentUps?.location }}</el-descriptions-item>
            <el-descriptions-item label="投运日期">{{ currentUps?.commissionDate }}</el-descriptions-item>
            <el-descriptions-item label="运行状态">
              <el-tag :type="getStatusType(currentUps?.status)">{{ getStatusText(currentUps?.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ currentUps?.lastUpdate }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <el-tab-pane label="运行参数" name="params">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="param-card">
                <template #header>输入参数</template>
                <div class="param-list">
                  <div class="param-row">
                    <span>输入电压:</span>
                    <span>{{ currentUps?.inputVoltage }}V</span>
                  </div>
                  <div class="param-row">
                    <span>输入频率:</span>
                    <span>{{ currentUps?.inputFrequency }}Hz</span>
                  </div>
                  <div class="param-row">
                    <span>输入电流:</span>
                    <span>{{ currentUps?.inputCurrent }}A</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="param-card">
                <template #header>输出参数</template>
                <div class="param-list">
                  <div class="param-row">
                    <span>输出电压:</span>
                    <span>{{ currentUps?.outputVoltage }}V</span>
                  </div>
                  <div class="param-row">
                    <span>输出频率:</span>
                    <span>{{ currentUps?.outputFrequency }}Hz</span>
                  </div>
                  <div class="param-row">
                    <span>输出电流:</span>
                    <span>{{ currentUps?.outputCurrent }}A</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-card class="param-card" style="margin-top: 20px;">
            <template #header>电池参数</template>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="param-row">
                  <span>电池电量:</span>
                  <span>{{ currentUps?.batteryLevel }}%</span>
                </div>
                <div class="param-row">
                  <span>电池电压:</span>
                  <span>{{ currentUps?.batteryVoltage }}V</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="param-row">
                  <span>剩余时间:</span>
                  <span>{{ currentUps?.remainingTime }}分钟</span>
                </div>
                <div class="param-row">
                  <span>电池温度:</span>
                  <span>{{ currentUps?.batteryTemp }}°C</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="param-row">
                  <span>负载功率:</span>
                  <span>{{ currentUps?.loadPower }}kW</span>
                </div>
                <div class="param-row">
                  <span>负载率:</span>
                  <span>{{ currentUps?.loadPercentage }}%</span>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="历史数据" name="history">
          <div id="historyChart" style="height: 400px;"></div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 编辑UPS对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑UPS' : '添加UPS'" width="600px">
      <el-form :model="upsForm" :rules="upsRules" ref="upsFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="upsForm.name" placeholder="请输入设备名称" />
            </el-form-item>
            <el-form-item label="设备型号" prop="model">
              <el-input v-model="upsForm.model" placeholder="请输入设备型号" />
            </el-form-item>
            <el-form-item label="制造商">
              <el-input v-model="upsForm.manufacturer" placeholder="请输入制造商" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="额定功率" prop="ratedPower">
              <el-input v-model="upsForm.ratedPower" placeholder="请输入额定功率(kVA)" />
            </el-form-item>
            <el-form-item label="安装位置" prop="location">
              <el-select v-model="upsForm.location" placeholder="请选择安装位置" style="width: 100%;">
                <el-option label="主机房" value="main_room" />
                <el-option label="副机房" value="sub_room" />
                <el-option label="配电室" value="power_room" />
                <el-option label="数据中心" value="data_center" />
              </el-select>
            </el-form-item>
            <el-form-item label="投运日期">
              <el-date-picker
                v-model="upsForm.commissionDate"
                type="date"
                placeholder="选择投运日期"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确定</el-button>
        </span>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lightning, VideoPlay, Cpu, DataAnalysis, Search, Refresh, Plus,
  Connection, Switch, Warning, View, Monitor, Edit
} from '@element-plus/icons-vue'
import '@/styles/dark-theme.scss'

// 接口定义
interface UPSDevice {
  id: string
  name: string
  model: string
  manufacturer: string
  ratedPower: number
  location: string
  status: 'online' | 'offline' | 'alarm'
  inputVoltage: number
  outputVoltage: number
  inputFrequency: number
  outputFrequency: number
  inputCurrent: number
  outputCurrent: number
  loadPercentage: number
  loadPower: number
  batteryLevel: number
  batteryVoltage: number
  batteryTemp: number
  remainingTime: number
  mainsPower: boolean
  batteryMode: boolean
  bypass: boolean
  alarm: boolean
  commissionDate: string
  lastUpdate: string
}

// 响应式数据
const searchText = ref('')
const statusFilter = ref('')
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)
const activeTab = ref('basic')
const currentUps = ref<UPSDevice | null>(null)

// UPS统计
const upsStats = reactive({
  total: 6,
  online: 5,
  avgBattery: 85,
  avgLoad: 68
})

// UPS表单
const upsForm = reactive({
  name: '',
  model: '',
  manufacturer: '',
  ratedPower: '',
  location: '',
  commissionDate: ''
})

const upsRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
  ratedPower: [{ required: true, message: '请输入额定功率', trigger: 'blur' }],
  location: [{ required: true, message: '请选择安装位置', trigger: 'change' }]
}

const upsFormRef = ref()

// UPS设备列表数据
const upsList = ref<UPSDevice[]>([
  {
    id: '1',
    name: 'UPS-主机房-01',
    model: 'APC Smart-UPS SRT 10kVA',
    manufacturer: 'APC',
    ratedPower: 10,
    location: '主机房',
    status: 'online',
    inputVoltage: 220,
    outputVoltage: 220,
    inputFrequency: 50,
    outputFrequency: 50,
    inputCurrent: 28.5,
    outputCurrent: 25.2,
    loadPercentage: 65,
    loadPower: 6.5,
    batteryLevel: 95,
    batteryVoltage: 240,
    batteryTemp: 28,
    remainingTime: 45,
    mainsPower: true,
    batteryMode: false,
    bypass: false,
    alarm: false,
    commissionDate: '2023-01-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '2',
    name: 'UPS-主机房-02',
    model: 'APC Smart-UPS SRT 10kVA',
    manufacturer: 'APC',
    ratedPower: 10,
    location: '主机房',
    status: 'online',
    inputVoltage: 218,
    outputVoltage: 220,
    inputFrequency: 50,
    outputFrequency: 50,
    inputCurrent: 32.1,
    outputCurrent: 28.8,
    loadPercentage: 72,
    loadPower: 7.2,
    batteryLevel: 88,
    batteryVoltage: 238,
    batteryTemp: 30,
    remainingTime: 38,
    mainsPower: true,
    batteryMode: false,
    bypass: false,
    alarm: false,
    commissionDate: '2023-01-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '3',
    name: 'UPS-副机房-01',
    model: 'Eaton 9PX 5kVA',
    manufacturer: 'Eaton',
    ratedPower: 5,
    location: '副机房',
    status: 'online',
    inputVoltage: 222,
    outputVoltage: 220,
    inputFrequency: 50,
    outputFrequency: 50,
    inputCurrent: 18.2,
    outputCurrent: 15.8,
    loadPercentage: 58,
    loadPower: 2.9,
    batteryLevel: 92,
    batteryVoltage: 120,
    batteryTemp: 26,
    remainingTime: 52,
    mainsPower: true,
    batteryMode: false,
    bypass: false,
    alarm: false,
    commissionDate: '2023-02-01',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '4',
    name: 'UPS-配电室-01',
    model: 'Schneider Galaxy VS 15kVA',
    manufacturer: 'Schneider',
    ratedPower: 15,
    location: '配电室',
    status: 'alarm',
    inputVoltage: 215,
    outputVoltage: 220,
    inputFrequency: 49.8,
    outputFrequency: 50,
    inputCurrent: 42.5,
    outputCurrent: 38.2,
    loadPercentage: 85,
    loadPower: 12.8,
    batteryLevel: 65,
    batteryVoltage: 235,
    batteryTemp: 35,
    remainingTime: 25,
    mainsPower: true,
    batteryMode: false,
    bypass: false,
    alarm: true,
    commissionDate: '2023-01-20',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '5',
    name: 'UPS-数据中心-01',
    model: 'Huawei UPS2000-G 20kVA',
    manufacturer: 'Huawei',
    ratedPower: 20,
    location: '数据中心',
    status: 'online',
    inputVoltage: 220,
    outputVoltage: 220,
    inputFrequency: 50,
    outputFrequency: 50,
    inputCurrent: 55.8,
    outputCurrent: 48.6,
    loadPercentage: 60,
    loadPower: 12.0,
    batteryLevel: 78,
    batteryVoltage: 480,
    batteryTemp: 32,
    remainingTime: 35,
    mainsPower: true,
    batteryMode: false,
    bypass: false,
    alarm: false,
    commissionDate: '2023-03-01',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '6',
    name: 'UPS-办公区-01',
    model: 'Delta Ultron DPS 3kVA',
    manufacturer: 'Delta',
    ratedPower: 3,
    location: '办公区',
    status: 'offline',
    inputVoltage: 0,
    outputVoltage: 0,
    inputFrequency: 0,
    outputFrequency: 0,
    inputCurrent: 0,
    outputCurrent: 0,
    loadPercentage: 0,
    loadPower: 0,
    batteryLevel: 0,
    batteryVoltage: 0,
    batteryTemp: 0,
    remainingTime: 0,
    mainsPower: false,
    batteryMode: false,
    bypass: false,
    alarm: true,
    commissionDate: '2023-04-01',
    lastUpdate: '2024-01-22 12:15:00'
  }
])

// 计算属性
const filteredUpsList = computed(() => {
  return upsList.value.filter(ups => {
    const matchText = ups.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
                     ups.model.toLowerCase().includes(searchText.value.toLowerCase()) ||
                     ups.location.toLowerCase().includes(searchText.value.toLowerCase())
    const matchStatus = !statusFilter.value || ups.status === statusFilter.value
    return matchText && matchStatus
  })
})

// 方法
const getStatusType = (status: string) => {
  const types = {
    online: 'success',
    offline: 'info',
    alarm: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    online: '在线',
    offline: '离线',
    alarm: '告警'
  }
  return texts[status] || '未知'
}

const getVoltagePercentage = (voltage: number) => {
  return Math.min(100, Math.max(0, (voltage / 220) * 100))
}

const getLoadColor = (percentage: number) => {
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

const getBatteryColor = (percentage: number) => {
  if (percentage <= 20) return '#f56c6c'
  if (percentage <= 50) return '#e6a23c'
  return '#67c23a'
}

const handleRefresh = () => {
  ElMessage.success('数据刷新成功')
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(upsForm, {
    name: '',
    model: '',
    manufacturer: '',
    ratedPower: '',
    location: '',
    commissionDate: ''
  })
  editDialogVisible.value = true
}

const handleViewDetail = (ups: UPSDevice) => {
  currentUps.value = ups
  activeTab.value = 'basic'
  detailDialogVisible.value = true
}

const handleMonitor = (ups: UPSDevice) => {
  ElMessage.info(`开始监控UPS设备：${ups.name}`)
}

const handleEdit = (ups: UPSDevice) => {
  isEdit.value = true
  Object.assign(upsForm, {
    name: ups.name,
    model: ups.model,
    manufacturer: ups.manufacturer,
    ratedPower: ups.ratedPower.toString(),
    location: ups.location,
    commissionDate: ups.commissionDate
  })
  editDialogVisible.value = true
}

const handleSave = async () => {
  try {
    await upsFormRef.value?.validate()
    ElMessage.success(isEdit.value ? 'UPS设备更新成功' : 'UPS设备添加成功')
    editDialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

onMounted(() => {
  // 组件挂载后的初始化操作
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.breadcrumb-container {
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
  
  .stats-card {
    .stats-content {
      display: flex;
      align-items: center;
      
      .stats-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        font-size: 24px;
        
        &.total {
          background: linear-gradient(45deg, #409eff, #66b3ff);
          color: white;
        }
        
        &.online {
          background: linear-gradient(45deg, #67c23a, #85ce61);
          color: white;
        }
        
        &.battery {
          background: linear-gradient(45deg, #e6a23c, #ebb563);
          color: white;
        }
        
        &.load {
          background: linear-gradient(45deg, #909399, #b3b6bb);
          color: white;
        }
      }
      
      .stats-info {
        .stats-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          line-height: 1;
        }
        
        .stats-label {
          font-size: 14px;
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.ups-grid {
  .ups-card {
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
    
    &.online {
      border-left: 4px solid #67c23a;
    }
    
    &.offline {
      border-left: 4px solid #909399;
    }
    
    &.alarm {
      border-left: 4px solid #f56c6c;
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .ups-name {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        font-size: 16px;
      }
    }
    
    .ups-content {
      .info-section {
        margin-bottom: 20px;
        
        .info-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          
          .label {
            color: #909399;
            font-size: 14px;
          }
          
          .value {
            font-weight: 500;
            color: #303133;
          }
        }
      }
      
      .params-section {
        margin-bottom: 20px;
        
        .param-item {
          margin-bottom: 15px;
          
          .param-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 5px;
            font-size: 14px;
            
            .param-value {
              font-weight: 600;
              color: #409eff;
            }
          }
        }
      }
      
      .status-indicators {
        display: flex;
        justify-content: space-around;
        margin-bottom: 20px;
        padding: 15px;
        background: #f8f9fa;
        border-radius: 8px;
        
        .indicator {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 5px;
          font-size: 12px;
          color: #909399;
          transition: all 0.3s ease;
          
          &.active {
            color: #409eff;
            
            .el-icon {
              color: #409eff;
            }
          }
        }
      }
      
      .action-buttons {
        display: flex;
        gap: 8px;
        
        .el-button {
          flex: 1;
        }
      }
    }
  }
}

.param-card {
  .param-list {
    .param-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
        margin-bottom: 0;
      }
    }
  }
}
</style>

