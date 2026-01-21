<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="app-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧建筑</el-breadcrumb-item>
        <el-breadcrumb-item>变配电</el-breadcrumb-item>
        <el-breadcrumb-item>油机</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><SetUp /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ generatorStats.total }}</div>
                <div class="stats-label">发电机总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon running">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ generatorStats.running }}</div>
                <div class="stats-label">运行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon fuel">
                <el-icon><MagicStick /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ generatorStats.avgFuel }}%</div>
                <div class="stats-label">平均油量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon power">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-value">{{ generatorStats.totalPower }}kW</div>
                <div class="stats-label">总发电功率</div>
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
          placeholder="搜索发电机设备..."
          style="width: 300px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px; margin-left: 10px;">
          <el-option label="全部" value="" />
          <el-option label="运行" value="running" />
          <el-option label="停机" value="stopped" />
          <el-option label="待机" value="standby" />
          <el-option label="故障" value="fault" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加发电机
        </el-button>
      </div>
    </div>

    <!-- 发电机设备卡片列表 -->
    <div class="generator-grid">
      <el-row :gutter="20">
        <el-col :span="8" v-for="generator in filteredGeneratorList" :key="generator.id">
          <el-card class="generator-card" :class="generator.status">
            <template #header>
              <div class="card-header">
                <div class="generator-name">
                  <el-icon><SetUp /></el-icon>
                  <span>{{ generator.name }}</span>
                </div>
                <div class="generator-status">
                  <el-tag :type="getStatusType(generator.status)">{{ getStatusText(generator.status) }}</el-tag>
                </div>
              </div>
            </template>
            
            <div class="generator-content">
              <!-- 基本信息 -->
              <div class="info-section">
                <div class="info-item">
                  <span class="label">型号:</span>
                  <span class="value">{{ generator.model }}</span>
                </div>
                <div class="info-item">
                  <span class="label">位置:</span>
                  <span class="value">{{ generator.location }}</span>
                </div>
                <div class="info-item">
                  <span class="label">额定功率:</span>
                  <span class="value">{{ generator.ratedPower }}kW</span>
                </div>
              </div>
              
              <!-- 运行参数 -->
              <div class="params-section">
                <div class="param-item">
                  <div class="param-header">
                    <span>发电功率</span>
                    <span class="param-value">{{ generator.outputPower }}kW</span>
                  </div>
                  <el-progress 
                    :percentage="getPowerPercentage(generator.outputPower, generator.ratedPower)" 
                    :stroke-width="8"
                    :color="getPowerColor(generator.outputPower, generator.ratedPower)"
                  />
                </div>
                
                <div class="param-item">
                  <div class="param-header">
                    <span>燃油量</span>
                    <span class="param-value">{{ generator.fuelLevel }}%</span>
                  </div>
                  <el-progress 
                    :percentage="generator.fuelLevel" 
                    :stroke-width="8"
                    :color="getFuelColor(generator.fuelLevel)"
                  />
                </div>
                
                <div class="param-item">
                  <div class="param-header">
                    <span>输出电压</span>
                    <span class="param-value">{{ generator.outputVoltage }}V</span>
                  </div>
                  <el-progress :percentage="getVoltagePercentage(generator.outputVoltage)" :stroke-width="8" />
                </div>
                
                <div class="param-item">
                  <div class="param-header">
                    <span>运行温度</span>
                    <span class="param-value">{{ generator.temperature }}°C</span>
                  </div>
                  <el-progress 
                    :percentage="getTemperaturePercentage(generator.temperature)" 
                    :stroke-width="8"
                    :color="getTemperatureColor(generator.temperature)"
                  />
                </div>
              </div>
              
              <!-- 状态指示器 -->
              <div class="status-indicators">
                <div class="indicator" :class="{ active: generator.engineRunning }">
                  <el-icon><VideoPlay /></el-icon>
                  <span>引擎</span>
                </div>
                <div class="indicator" :class="{ active: generator.generatorRunning }">
                  <el-icon><Lightning /></el-icon>
                  <span>发电</span>
                </div>
                <div class="indicator" :class="{ active: generator.autoMode }">
                  <el-icon><Setting /></el-icon>
                  <span>自动</span>
                </div>
                <div class="indicator" :class="{ active: generator.alarm }">
                  <el-icon><Warning /></el-icon>
                  <span>告警</span>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button size="small" @click="handleViewDetail(generator)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button 
                  size="small" 
                  :type="generator.status === 'running' ? 'danger' : 'success'" 
                  @click="handleToggleEngine(generator)"
                >
                  <el-icon><Switch /></el-icon>
                  {{ generator.status === 'running' ? '停机' : '启动' }}
                </el-button>
                <el-button size="small" type="primary" @click="handleEdit(generator)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 发电机详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="发电机设备详情" width="900px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="设备名称">{{ currentGenerator?.name }}</el-descriptions-item>
            <el-descriptions-item label="设备型号">{{ currentGenerator?.model }}</el-descriptions-item>
            <el-descriptions-item label="制造商">{{ currentGenerator?.manufacturer }}</el-descriptions-item>
            <el-descriptions-item label="额定功率">{{ currentGenerator?.ratedPower }}kW</el-descriptions-item>
            <el-descriptions-item label="安装位置">{{ currentGenerator?.location }}</el-descriptions-item>
            <el-descriptions-item label="投运日期">{{ currentGenerator?.commissionDate }}</el-descriptions-item>
            <el-descriptions-item label="运行状态">
              <el-tag :type="getStatusType(currentGenerator?.status)">{{ getStatusText(currentGenerator?.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ currentGenerator?.lastUpdate }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <el-tab-pane label="运行参数" name="params">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="param-card">
                <template #header>发电参数</template>
                <div class="param-list">
                  <div class="param-row">
                    <span>发电功率:</span>
                    <span>{{ currentGenerator?.outputPower }}kW</span>
                  </div>
                  <div class="param-row">
                    <span>输出电压:</span>
                    <span>{{ currentGenerator?.outputVoltage }}V</span>
                  </div>
                  <div class="param-row">
                    <span>输出电流:</span>
                    <span>{{ currentGenerator?.outputCurrent }}A</span>
                  </div>
                  <div class="param-row">
                    <span>输出频率:</span>
                    <span>{{ currentGenerator?.outputFrequency }}Hz</span>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="param-card">
                <template #header>引擎参数</template>
                <div class="param-list">
                  <div class="param-row">
                    <span>引擎转速:</span>
                    <span>{{ currentGenerator?.engineRPM }}rpm</span>
                  </div>
                  <div class="param-row">
                    <span>引擎温度:</span>
                    <span>{{ currentGenerator?.temperature }}°C</span>
                  </div>
                  <div class="param-row">
                    <span>机油压力:</span>
                    <span>{{ currentGenerator?.oilPressure }}bar</span>
                  </div>
                  <div class="param-row">
                    <span>冷却液温度:</span>
                    <span>{{ currentGenerator?.coolantTemp }}°C</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-card class="param-card" style="margin-top: 20px;">
            <template #header>燃油参数</template>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="param-row">
                  <span>燃油量:</span>
                  <span>{{ currentGenerator?.fuelLevel }}%</span>
                </div>
                <div class="param-row">
                  <span>燃油消耗:</span>
                  <span>{{ currentGenerator?.fuelConsumption }}L/h</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="param-row">
                  <span>剩余运行时间:</span>
                  <span>{{ currentGenerator?.remainingTime }}小时</span>
                </div>
                <div class="param-row">
                  <span>累计运行时间:</span>
                  <span>{{ currentGenerator?.totalRunTime }}小时</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="param-row">
                  <span>启动次数:</span>
                  <span>{{ currentGenerator?.startCount }}次</span>
                </div>
                <div class="param-row">
                  <span>负载率:</span>
                  <span>{{ currentGenerator?.loadPercentage }}%</span>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="历史数据" name="history">
          <div id="generatorHistoryChart" style="height: 400px;"></div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 编辑发电机对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑发电机' : '添加发电机'" width="600px">
      <el-form :model="generatorForm" :rules="generatorRules" ref="generatorFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="generatorForm.name" placeholder="请输入设备名称" />
            </el-form-item>
            <el-form-item label="设备型号" prop="model">
              <el-input v-model="generatorForm.model" placeholder="请输入设备型号" />
            </el-form-item>
            <el-form-item label="制造商">
              <el-input v-model="generatorForm.manufacturer" placeholder="请输入制造商" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="额定功率" prop="ratedPower">
              <el-input v-model="generatorForm.ratedPower" placeholder="请输入额定功率(kW)" />
            </el-form-item>
            <el-form-item label="安装位置" prop="location">
              <el-select v-model="generatorForm.location" placeholder="请选择安装位置" style="width: 100%;">
                <el-option label="发电机房1" value="generator_room_1" />
                <el-option label="发电机房2" value="generator_room_2" />
                <el-option label="地下室" value="basement" />
                <el-option label="室外" value="outdoor" />
              </el-select>
            </el-form-item>
            <el-form-item label="投运日期">
              <el-date-picker
                v-model="generatorForm.commissionDate"
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
  SetUp, VideoPlay, MagicStick, Lightning, Search, Refresh, Plus,
  Setting, Warning, View, Switch, Edit
} from '@element-plus/icons-vue'
import '@/styles/dark-theme.scss'

// 接口定义
interface Generator {
  id: string
  name: string
  model: string
  manufacturer: string
  ratedPower: number
  location: string
  status: 'running' | 'stopped' | 'standby' | 'fault'
  outputPower: number
  outputVoltage: number
  outputCurrent: number
  outputFrequency: number
  fuelLevel: number
  fuelConsumption: number
  temperature: number
  engineRPM: number
  oilPressure: number
  coolantTemp: number
  remainingTime: number
  totalRunTime: number
  startCount: number
  loadPercentage: number
  engineRunning: boolean
  generatorRunning: boolean
  autoMode: boolean
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
const currentGenerator = ref<Generator | null>(null)

// 发电机统计
const generatorStats = reactive({
  total: 4,
  running: 1,
  avgFuel: 75,
  totalPower: 850
})

// 发电机表单
const generatorForm = reactive({
  name: '',
  model: '',
  manufacturer: '',
  ratedPower: '',
  location: '',
  commissionDate: ''
})

const generatorRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
  ratedPower: [{ required: true, message: '请输入额定功率', trigger: 'blur' }],
  location: [{ required: true, message: '请选择安装位置', trigger: 'change' }]
}

const generatorFormRef = ref()

// 发电机设备列表数据
const generatorList = ref<Generator[]>([
  {
    id: '1',
    name: '发电机组-01',
    model: 'Caterpillar C18 ACERT',
    manufacturer: 'Caterpillar',
    ratedPower: 500,
    location: '发电机房1',
    status: 'running',
    outputPower: 350,
    outputVoltage: 400,
    outputCurrent: 505,
    outputFrequency: 50,
    fuelLevel: 85,
    fuelConsumption: 120,
    temperature: 85,
    engineRPM: 1500,
    oilPressure: 4.2,
    coolantTemp: 82,
    remainingTime: 18,
    totalRunTime: 2850,
    startCount: 156,
    loadPercentage: 70,
    engineRunning: true,
    generatorRunning: true,
    autoMode: true,
    alarm: false,
    commissionDate: '2022-06-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '2',
    name: '发电机组-02',
    model: 'Cummins QSK19-G4',
    manufacturer: 'Cummins',
    ratedPower: 600,
    location: '发电机房1',
    status: 'standby',
    outputPower: 0,
    outputVoltage: 0,
    outputCurrent: 0,
    outputFrequency: 0,
    fuelLevel: 92,
    fuelConsumption: 0,
    temperature: 25,
    engineRPM: 0,
    oilPressure: 0,
    coolantTemp: 25,
    remainingTime: 24,
    totalRunTime: 1950,
    startCount: 89,
    loadPercentage: 0,
    engineRunning: false,
    generatorRunning: false,
    autoMode: true,
    alarm: false,
    commissionDate: '2022-06-15',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '3',
    name: '备用发电机-01',
    model: 'Volvo Penta TAD1341GE',
    manufacturer: 'Volvo',
    ratedPower: 350,
    location: '发电机房2',
    status: 'standby',
    outputPower: 0,
    outputVoltage: 0,
    outputCurrent: 0,
    outputFrequency: 0,
    fuelLevel: 68,
    fuelConsumption: 0,
    temperature: 28,
    engineRPM: 0,
    oilPressure: 0,
    coolantTemp: 28,
    remainingTime: 15,
    totalRunTime: 850,
    startCount: 45,
    loadPercentage: 0,
    engineRunning: false,
    generatorRunning: false,
    autoMode: true,
    alarm: false,
    commissionDate: '2022-08-20',
    lastUpdate: '2024-01-22 14:30:00'
  },
  {
    id: '4',
    name: '应急发电机-01',
    model: 'Perkins 1306C-E87TAG4',
    manufacturer: 'Perkins',
    ratedPower: 200,
    location: '地下室',
    status: 'fault',
    outputPower: 0,
    outputVoltage: 0,
    outputCurrent: 0,
    outputFrequency: 0,
    fuelLevel: 45,
    fuelConsumption: 0,
    temperature: 32,
    engineRPM: 0,
    oilPressure: 0,
    coolantTemp: 32,
    remainingTime: 8,
    totalRunTime: 650,
    startCount: 78,
    loadPercentage: 0,
    engineRunning: false,
    generatorRunning: false,
    autoMode: false,
    alarm: true,
    commissionDate: '2022-10-10',
    lastUpdate: '2024-01-22 12:45:00'
  }
])

// 计算属性
const filteredGeneratorList = computed(() => {
  return generatorList.value.filter(generator => {
    const matchText = generator.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
                     generator.model.toLowerCase().includes(searchText.value.toLowerCase()) ||
                     generator.location.toLowerCase().includes(searchText.value.toLowerCase())
    const matchStatus = !statusFilter.value || generator.status === statusFilter.value
    return matchText && matchStatus
  })
})

// 方法
const getStatusType = (status: string) => {
  const types = {
    running: 'success',
    stopped: 'info',
    standby: 'warning',
    fault: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts = {
    running: '运行',
    stopped: '停机',
    standby: '待机',
    fault: '故障'
  }
  return texts[status] || '未知'
}

const getPowerPercentage = (current: number, rated: number) => {
  return Math.min(100, (current / rated) * 100)
}

const getPowerColor = (current: number, rated: number) => {
  const percentage = (current / rated) * 100
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

const getFuelColor = (percentage: number) => {
  if (percentage <= 20) return '#f56c6c'
  if (percentage <= 50) return '#e6a23c'
  return '#67c23a'
}

const getVoltagePercentage = (voltage: number) => {
  return Math.min(100, Math.max(0, (voltage / 400) * 100))
}

const getTemperaturePercentage = (temp: number) => {
  return Math.min(100, Math.max(0, (temp / 100) * 100))
}

const getTemperatureColor = (temp: number) => {
  if (temp >= 90) return '#f56c6c'
  if (temp >= 70) return '#e6a23c'
  return '#67c23a'
}

const handleRefresh = () => {
  ElMessage.success('数据刷新成功')
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(generatorForm, {
    name: '',
    model: '',
    manufacturer: '',
    ratedPower: '',
    location: '',
    commissionDate: ''
  })
  editDialogVisible.value = true
}

const handleViewDetail = (generator: Generator) => {
  currentGenerator.value = generator
  activeTab.value = 'basic'
  detailDialogVisible.value = true
}

const handleToggleEngine = async (generator: Generator) => {
  try {
    const action = generator.status === 'running' ? '停机' : '启动'
    await ElMessageBox.confirm(
      `确定要${action}发电机 "${generator.name}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success(`发电机${action}操作成功`)
  } catch {
    ElMessage.info('操作已取消')
  }
}

const handleEdit = (generator: Generator) => {
  isEdit.value = true
  Object.assign(generatorForm, {
    name: generator.name,
    model: generator.model,
    manufacturer: generator.manufacturer,
    ratedPower: generator.ratedPower.toString(),
    location: generator.location,
    commissionDate: generator.commissionDate
  })
  editDialogVisible.value = true
}

const handleSave = async () => {
  try {
    await generatorFormRef.value?.validate()
    ElMessage.success(isEdit.value ? '发电机设备更新成功' : '发电机设备添加成功')
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
        
        &.running {
          background: linear-gradient(45deg, #67c23a, #85ce61);
          color: white;
        }
        
        &.fuel {
          background: linear-gradient(45deg, #e6a23c, #ebb563);
          color: white;
        }
        
        &.power {
          background: linear-gradient(45deg, #f56c6c, #f78989);
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

.generator-grid {
  .generator-card {
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
    
    &.running {
      border-left: 4px solid #67c23a;
    }
    
    &.stopped {
      border-left: 4px solid #909399;
    }
    
    &.standby {
      border-left: 4px solid #e6a23c;
    }
    
    &.fault {
      border-left: 4px solid #f56c6c;
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .generator-name {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        font-size: 16px;
      }
    }
    
    .generator-content {
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

