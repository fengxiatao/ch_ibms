<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="emergency-light-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧消防</el-breadcrumb-item>
        <el-breadcrumb-item>设备监测</el-breadcrumb-item>
        <el-breadcrumb-item>应急照明</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon light-total">
              <el-icon><Sunny /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.total }}</div>
              <div class="stat-label">应急照明总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon light-normal">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.normal }}</div>
              <div class="stat-label">正常工作</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon light-fault">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.fault }}</div>
              <div class="stat-label">故障设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon light-maintenance">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.maintenance }}</div>
              <div class="stat-label">维护中</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选和地图切换 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-header">
        <div class="filter-left">
          <el-select v-model="filterParams.building" placeholder="选择建筑" style="width: 150px; margin-right: 10px;">
            <el-option label="全部建筑" value="" />
            <el-option label="A座" value="A" />
            <el-option label="B座" value="B" />
            <el-option label="C座" value="C" />
          </el-select>
          <el-select v-model="filterParams.floor" placeholder="选择楼层" style="width: 150px; margin-right: 10px;">
            <el-option label="全部楼层" value="" />
            <el-option label="1楼" value="1" />
            <el-option label="2楼" value="2" />
            <el-option label="3楼" value="3" />
          </el-select>
          <el-select v-model="filterParams.type" placeholder="设备类型" style="width: 150px; margin-right: 10px;">
            <el-option label="全部类型" value="" />
            <el-option label="应急照明灯" value="emergency_light" />
            <el-option label="疏散指示灯" value="evacuation_sign" />
            <el-option label="安全出口灯" value="exit_sign" />
            <el-option label="楼层指示灯" value="floor_sign" />
          </el-select>
          <el-select v-model="filterParams.status" placeholder="设备状态" style="width: 150px; margin-right: 10px;">
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="故障" value="fault" />
            <el-option label="维护" value="maintenance" />
          </el-select>
          <el-button type="primary" @click="handleFilter" :icon="Search">筛选</el-button>
        </div>
        <div class="filter-right">
          <el-radio-group v-model="viewMode" @change="handleViewModeChange">
            <el-radio-button label="card">卡片视图</el-radio-button>
            <el-radio-button label="map">地图视图</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="card-view">
      <el-row :gutter="20">
        <el-col :span="8" v-for="device in filteredDevices" :key="device.id" class="device-card-col">
          <el-card class="device-card" shadow="hover">
            <div class="device-header">
              <div class="device-title">
                <el-icon class="device-icon" :class="getDeviceIconClass(device.type)">
                  <Sunny />
                </el-icon>
                <span class="device-name">{{ device.name }}</span>
              </div>
              <div class="status-indicators">
                <el-tag :type="getStatusTagType(device.status)" size="small">
                  {{ getStatusText(device.status) }}
                </el-tag>
                <div class="battery-indicator" v-if="device.batteryLevel !== null">
                  <el-icon :class="getBatteryIconClass(device.batteryLevel)">
                    <Lightning />
                  </el-icon>
                  <span class="battery-level">{{ device.batteryLevel }}%</span>
                </div>
              </div>
            </div>
            
            <div class="device-info">
              <div class="info-row">
                <span class="info-label">设备类型:</span>
                <span class="info-value">{{ getDeviceTypeText(device.type) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">安装位置:</span>
                <span class="info-value">{{ device.location }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">亮度等级:</span>
                <span class="info-value brightness-value" :class="getBrightnessClass(device.brightness)">
                  {{ device.brightness }}%
                </span>
              </div>
              <div class="info-row" v-if="device.batteryLevel !== null">
                <span class="info-label">电池电量:</span>
                <span class="info-value battery-value" :class="getBatteryClass(device.batteryLevel)">
                  {{ device.batteryLevel }}%
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">工作模式:</span>
                <span class="info-value">{{ getWorkModeText(device.workMode) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">最后检测:</span>
                <span class="info-value">{{ device.lastCheck }}</span>
              </div>
            </div>

            <div class="device-actions">
              <el-button size="small" @click="handleViewDetail(device)" :icon="View">详情</el-button>
              <el-button size="small" @click="handleTest(device)" :icon="VideoPlay">测试</el-button>
              <el-button 
                v-if="device.status === 'fault'" 
                type="warning" 
                size="small" 
                @click="handleRepair(device)"
                :icon="Tools"
              >
                维修
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button @click="loadMore" :loading="loading">加载更多</el-button>
      </div>
    </div>

    <!-- 地图视图 -->
    <div v-else class="map-view">
      <el-card class="map-card" shadow="never">
        <div class="map-container">
          <div class="map-placeholder">
            <el-icon class="map-icon"><Location /></el-icon>
            <p>电子地图加载中...</p>
            <p class="map-desc">此处将显示应急照明设备的实时位置和状态</p>
          </div>
          
          <!-- 地图图例 -->
          <div class="map-legend">
            <h4>设备状态图例</h4>
            <div class="legend-items">
              <div class="legend-item">
                <span class="legend-dot normal"></span>
                <span>正常工作</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot fault"></span>
                <span>设备故障</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot maintenance"></span>
                <span>维护中</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 设备详情弹窗 -->
    <el-dialog v-model="detailVisible" title="设备详情" width="600px" append-to-body>
      <el-descriptions v-if="currentDevice" :column="2" border>
        <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ currentDevice.code }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ getDeviceTypeText(currentDevice.type) }}</el-descriptions-item>
        <el-descriptions-item label="设备状态">
          <el-tag :type="getStatusTagType(currentDevice.status)" size="small">
            {{ getStatusText(currentDevice.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="安装位置" span="2">{{ currentDevice.location }}</el-descriptions-item>
        <el-descriptions-item label="亮度等级">
          <span :class="getBrightnessClass(currentDevice.brightness)">{{ currentDevice.brightness }}%</span>
        </el-descriptions-item>
        <el-descriptions-item label="电池电量" v-if="currentDevice.batteryLevel !== null">
          <span :class="getBatteryClass(currentDevice.batteryLevel)">{{ currentDevice.batteryLevel }}%</span>
        </el-descriptions-item>
        <el-descriptions-item label="工作模式">{{ getWorkModeText(currentDevice.workMode) }}</el-descriptions-item>
        <el-descriptions-item label="额定功率">{{ currentDevice.power }}W</el-descriptions-item>
        <el-descriptions-item label="安装日期">{{ currentDevice.installDate }}</el-descriptions-item>
        <el-descriptions-item label="最后维护">{{ currentDevice.lastMaintenance }}</el-descriptions-item>
        <el-descriptions-item label="最后检测" span="2">{{ currentDevice.lastCheck }}</el-descriptions-item>
        <el-descriptions-item label="设备描述" span="2">
          {{ currentDevice.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 测试弹窗 -->
    <el-dialog v-model="testVisible" title="设备测试" width="500px" append-to-body>
      <div v-if="currentDevice" class="test-content">
        <el-alert
          title="设备测试说明"
          type="info"
          :closable="false"
          show-icon
        >
          <p>点击开始测试将进行应急照明设备的功能测试，包括：</p>
          <ul>
            <li>照明功能测试</li>
            <li>电池备电测试</li>
            <li>指示功能测试</li>
          </ul>
        </el-alert>
        
        <div class="test-info">
          <p><strong>设备名称：</strong>{{ currentDevice.name }}</p>
          <p><strong>设备位置：</strong>{{ currentDevice.location }}</p>
        </div>

        <div class="test-progress" v-if="testInProgress">
          <el-progress 
            :percentage="testProgress" 
            :status="testProgress === 100 ? 'success' : undefined"
          />
          <p class="progress-text">{{ testProgressText }}</p>
        </div>

        <div class="test-result" v-if="testCompleted">
          <el-result
            :icon="testResult.success ? 'success' : 'error'"
            :title="testResult.success ? '测试通过' : '测试失败'"
            :sub-title="testResult.message"
          />
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="testVisible = false" :disabled="testInProgress">
            {{ testCompleted ? '关闭' : '取消' }}
          </el-button>
          <el-button 
            type="primary" 
            @click="startTest" 
            :loading="testInProgress"
            v-if="!testCompleted"
          >
            开始测试
          </el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Sunny, CircleCheckFilled, WarningFilled, Tools, Lightning,
  Search, View, VideoPlay, Location
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const viewMode = ref('card')
const detailVisible = ref(false)
const testVisible = ref(false)
const currentDevice = ref(null)
const hasMore = ref(true)
const testInProgress = ref(false)
const testCompleted = ref(false)
const testProgress = ref(0)
const testProgressText = ref('')
const testResult = ref({ success: false, message: '' })

// 筛选参数
const filterParams = reactive({
  building: '',
  floor: '',
  type: '',
  status: ''
})

// 设备统计
const deviceStats = reactive({
  total: 128,
  normal: 115,
  fault: 8,
  maintenance: 5
})

// 模拟设备数据
const mockDevices = [
  {
    id: 1,
    code: 'EL-A101-001',
    name: '应急照明灯001',
    type: 'emergency_light',
    location: 'A座1楼走廊东侧',
    status: 'normal',
    brightness: 85,
    batteryLevel: 92,
    workMode: 'auto',
    power: 12,
    installDate: '2024-01-15',
    lastMaintenance: '2024-08-20',
    lastCheck: '2024-09-27 15:30:25',
    description: '走廊应急照明设备'
  },
  {
    id: 2,
    code: 'ES-A102-002',
    name: '疏散指示灯002',
    type: 'evacuation_sign',
    location: 'A座1楼安全出口',
    status: 'normal',
    brightness: 100,
    batteryLevel: 88,
    workMode: 'always_on',
    power: 8,
    installDate: '2024-01-20',
    lastMaintenance: '2024-08-25',
    lastCheck: '2024-09-27 15:29:45',
    description: '安全出口疏散指示'
  },
  {
    id: 3,
    code: 'EXIT-B201-003',
    name: '安全出口灯003',
    type: 'exit_sign',
    location: 'B座2楼安全出口',
    status: 'fault',
    brightness: 0,
    batteryLevel: 15,
    workMode: 'emergency',
    power: 10,
    installDate: '2024-02-01',
    lastMaintenance: '2024-07-15',
    lastCheck: '2024-09-27 14:45:12',
    description: '安全出口标识灯，当前故障'
  },
  {
    id: 4,
    code: 'FL-B301-004',
    name: '楼层指示灯004',
    type: 'floor_sign',
    location: 'B座3楼楼梯间',
    status: 'normal',
    brightness: 90,
    batteryLevel: null, // 市电供电，无电池
    workMode: 'always_on',
    power: 15,
    installDate: '2024-02-10',
    lastMaintenance: '2024-09-01',
    lastCheck: '2024-09-27 15:28:30',
    description: '楼层标识指示灯'
  },
  {
    id: 5,
    code: 'EL-C101-005',
    name: '应急照明灯005',
    type: 'emergency_light',
    location: 'C座1楼大厅',
    status: 'maintenance',
    brightness: 0,
    batteryLevel: 0,
    workMode: 'maintenance',
    power: 20,
    installDate: '2024-01-10',
    lastMaintenance: '2024-09-25',
    lastCheck: '2024-09-26 18:20:15',
    description: '大厅应急照明，维护中'
  },
  {
    id: 6,
    code: 'ES-A201-006',
    name: '疏散指示灯006',
    type: 'evacuation_sign',
    location: 'A座2楼走廊中间',
    status: 'normal',
    brightness: 95,
    batteryLevel: 85,
    workMode: 'auto',
    power: 8,
    installDate: '2024-01-25',
    lastMaintenance: '2024-08-30',
    lastCheck: '2024-09-27 15:31:10',
    description: '走廊疏散指示设备'
  }
]

const allDevices = ref([...mockDevices])

// 计算属性
const filteredDevices = computed(() => {
  let devices = allDevices.value
  
  if (filterParams.building) {
    devices = devices.filter(device => 
      device.location.includes(filterParams.building + '座')
    )
  }
  
  if (filterParams.floor) {
    devices = devices.filter(device => 
      device.location.includes(filterParams.floor + '楼')
    )
  }
  
  if (filterParams.type) {
    devices = devices.filter(device => device.type === filterParams.type)
  }
  
  if (filterParams.status) {
    devices = devices.filter(device => device.status === filterParams.status)
  }
  
  return devices
})

// 方法
const getDeviceIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    'emergency_light': 'icon-emergency-light',
    'evacuation_sign': 'icon-evacuation-sign',
    'exit_sign': 'icon-exit-sign',
    'floor_sign': 'icon-floor-sign'
  }
  return classMap[type] || 'icon-default'
}

const getDeviceTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'emergency_light': '应急照明灯',
    'evacuation_sign': '疏散指示灯',
    'exit_sign': '安全出口灯',
    'floor_sign': '楼层指示灯'
  }
  return textMap[type] || type
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'normal': 'success',
    'fault': 'danger',
    'maintenance': 'warning'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'normal': '正常',
    'fault': '故障',
    'maintenance': '维护'
  }
  return textMap[status] || status
}

const getBrightnessClass = (brightness: number) => {
  if (brightness === 0) return 'brightness-zero'
  if (brightness < 50) return 'brightness-low'
  if (brightness < 80) return 'brightness-medium'
  return 'brightness-high'
}

const getBatteryClass = (batteryLevel: number) => {
  if (batteryLevel < 20) return 'battery-low'
  if (batteryLevel < 50) return 'battery-medium'
  return 'battery-high'
}

const getBatteryIconClass = (batteryLevel: number) => {
  if (batteryLevel < 20) return 'battery-icon-low'
  if (batteryLevel < 50) return 'battery-icon-medium'
  return 'battery-icon-high'
}

const getWorkModeText = (mode: string) => {
  const textMap: Record<string, string> = {
    'auto': '自动模式',
    'always_on': '常亮模式',
    'emergency': '应急模式',
    'maintenance': '维护模式'
  }
  return textMap[mode] || mode
}

const handleFilter = () => {
  ElMessage.success('筛选完成')
}

const handleViewModeChange = (mode: string) => {
  ElMessage.info(`切换到${mode === 'card' ? '卡片' : '地图'}视图`)
}

const handleViewDetail = (device: any) => {
  currentDevice.value = device
  detailVisible.value = true
}

const handleTest = (device: any) => {
  currentDevice.value = device
  testInProgress.value = false
  testCompleted.value = false
  testProgress.value = 0
  testProgressText.value = ''
  testVisible.value = true
}

const startTest = () => {
  testInProgress.value = true
  testCompleted.value = false
  testProgress.value = 0
  
  // 模拟测试过程
  const testSteps = [
    { progress: 20, text: '正在检测设备连接...' },
    { progress: 40, text: '正在测试照明功能...' },
    { progress: 60, text: '正在测试电池备电...' },
    { progress: 80, text: '正在测试指示功能...' },
    { progress: 100, text: '测试完成' }
  ]
  
  let stepIndex = 0
  const testInterval = setInterval(() => {
    if (stepIndex < testSteps.length) {
      const step = testSteps[stepIndex]
      testProgress.value = step.progress
      testProgressText.value = step.text
      stepIndex++
    } else {
      clearInterval(testInterval)
      testInProgress.value = false
      testCompleted.value = true
      
      // 随机生成测试结果
      const success = Math.random() > 0.3
      testResult.value = {
        success,
        message: success 
          ? '所有功能测试正常，设备工作状态良好' 
          : '发现设备异常，建议进行维护检查'
      }
    }
  }, 1000)
}

const handleRepair = (device: any) => {
  ElMessage.info(`发起设备 ${device.name} 的维修工单`)
}

const loadMore = () => {
  loading.value = true
  
  // 模拟加载更多数据
  setTimeout(() => {
    // 这里可以添加更多设备数据
    loading.value = false
    hasMore.value = false
    ElMessage.success('已加载全部数据')
  }, 1000)
}

onMounted(() => {
  // 初始化数据
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.emergency-light-container {
  padding: 20px;
  background: #0a1628 !important;
  min-height: 100vh;
  
  :deep(.el-card) {
    background: rgba(255, 255, 255, 0.05) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-card__body) {
    background: transparent !important;
    color: #ffffff !important;
  }
  
  :deep(.el-breadcrumb) {
    color: #ffffff !important;
  }
  
  :deep(.el-breadcrumb__item) {
    color: #ffffff !important;
  }
  
  :deep(.el-table) {
    background: rgba(255, 255, 255, 0.05) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table__header) {
    background: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table__body) {
    background: transparent !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table td) {
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-table th) {
    border-color: rgba(255, 255, 255, 0.1) !important;
    color: #ffffff !important;
    background: rgba(255, 255, 255, 0.1) !important;
  }
  
  :deep(.el-form-item__label) {
    color: #ffffff !important;
  }
  
  :deep(.el-input__inner) {
    background: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.2) !important;
    color: #ffffff !important;
  }
  
  :deep(.el-select) {
    color: #ffffff !important;
  }
  
  :deep(.el-pagination) {
    color: #ffffff !important;
  }

  .breadcrumb-container {
    margin-bottom: 20px;
  }

  .stats-cards {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          font-size: 24px;
          color: white;

          &.light-total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.light-normal {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.light-fault {
            background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
          }

          &.light-maintenance {
            background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
          }
        }

        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-top: 5px;
          }
        }
      }
    }
  }

  .filter-card {
    margin-bottom: 20px;

    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .filter-left {
        display: flex;
        align-items: center;
      }
    }
  }

  .card-view {
    .device-card-col {
      margin-bottom: 20px;
    }

    .device-card {
      height: 100%;

      .device-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .device-title {
          display: flex;
          align-items: center;

          .device-icon {
            font-size: 20px;
            margin-right: 8px;
            color: #409eff;

            &.icon-emergency-light { color: #f39c12; }
            &.icon-evacuation-sign { color: #27ae60; }
            &.icon-exit-sign { color: #e74c3c; }
            &.icon-floor-sign { color: #3498db; }
          }

          .device-name {
            font-size: 16px;
            font-weight: bold;
            color: #303133;
          }
        }

        .status-indicators {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 5px;

          .battery-indicator {
            display: flex;
            align-items: center;
            font-size: 12px;

            .el-icon {
              margin-right: 4px;
              
              &.battery-icon-high { color: #67c23a; }
              &.battery-icon-medium { color: #e6a23c; }
              &.battery-icon-low { color: #f56c6c; }
            }

            .battery-level {
              font-weight: 500;
            }
          }
        }
      }

      .device-info {
        margin-bottom: 15px;

        .info-row {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 14px;

          .info-label {
            color: #909399;
            min-width: 80px;
          }

          .info-value {
            color: #303133;
            font-weight: 500;

            &.brightness-high { color: #67c23a; }
            &.brightness-medium { color: #e6a23c; }
            &.brightness-low { color: #f56c6c; }
            &.brightness-zero { color: #909399; }

            &.battery-high { color: #67c23a; }
            &.battery-medium { color: #e6a23c; }
            &.battery-low { color: #f56c6c; }
          }
        }
      }

      .device-actions {
        display: flex;
        gap: 8px;
      }
    }

    .load-more {
      text-align: center;
      margin-top: 20px;
    }
  }

  .map-view {
    .map-card {
      .map-container {
        position: relative;
        height: 600px;
        background: #f5f7fa;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;

        .map-placeholder {
          text-align: center;
          color: #909399;

          .map-icon {
            font-size: 64px;
            margin-bottom: 20px;
          }

          p {
            margin: 5px 0;
            font-size: 16px;

            &.map-desc {
              font-size: 14px;
              color: #c0c4cc;
            }
          }
        }

        .map-legend {
          position: absolute;
          top: 20px;
          right: 20px;
          background: #1a1a1a;
          padding: 15px;
          border-radius: 4px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

          h4 {
            margin: 0 0 10px 0;
            font-size: 14px;
            color: #303133;
          }

          .legend-items {
            .legend-item {
              display: flex;
              align-items: center;
              margin-bottom: 8px;
              font-size: 12px;

              .legend-dot {
                width: 12px;
                height: 12px;
                border-radius: 50%;
                margin-right: 8px;

                &.normal { background: #67c23a; }
                &.fault { background: #f56c6c; }
                &.maintenance { background: #e6a23c; }
              }
            }
          }
        }
      }
    }
  }

  .test-content {
    .test-info {
      margin: 20px 0;
      
      p {
        margin: 8px 0;
        font-size: 14px;
      }
    }

    .test-progress {
      margin: 20px 0;
      
      .progress-text {
        text-align: center;
        margin-top: 10px;
        font-size: 14px;
        color: #606266;
      }
    }

    .test-result {
      margin: 20px 0;
    }
  }

  .dialog-footer {
    text-align: right;
  }
}
</style>
