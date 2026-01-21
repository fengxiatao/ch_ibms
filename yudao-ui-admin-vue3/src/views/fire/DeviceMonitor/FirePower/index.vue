<template>
  <ContentWrap style="margin-top: 70px;">

  <div class="fire-power-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧消防</el-breadcrumb-item>
        <el-breadcrumb-item>设备监测</el-breadcrumb-item>
        <el-breadcrumb-item>消防电源</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon power-total">
              <el-icon><Lightning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.total }}</div>
              <div class="stat-label">电源设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon power-normal">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.normal }}</div>
              <div class="stat-label">正常运行</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon power-fault">
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
            <div class="stat-icon power-offline">
              <el-icon><CircleCloseFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.offline }}</div>
              <div class="stat-label">离线设备</div>
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
          <el-select v-model="filterParams.status" placeholder="设备状态" style="width: 150px; margin-right: 10px;">
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="故障" value="fault" />
            <el-option label="离线" value="offline" />
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
                  <Lightning />
                </el-icon>
                <span class="device-name">{{ device.name }}</span>
              </div>
              <el-tag :type="getStatusTagType(device.status)" size="small">
                {{ getStatusText(device.status) }}
              </el-tag>
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
                <span class="info-label">电压值:</span>
                <span class="info-value voltage-value" :class="getVoltageClass(device.voltage)">
                  {{ device.voltage }}V
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">电流值:</span>
                <span class="info-value current-value" :class="getCurrentClass(device.current)">
                  {{ device.current }}A
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">功率:</span>
                <span class="info-value">{{ device.power }}W</span>
              </div>
              <div class="info-row">
                <span class="info-label">最后更新:</span>
                <span class="info-value">{{ device.lastUpdate }}</span>
              </div>
            </div>

            <div class="device-actions">
              <el-button size="small" @click="handleViewDetail(device)" :icon="View">详情</el-button>
              <el-button size="small" @click="handleViewHistory(device)" :icon="TrendCharts">历史</el-button>
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
            <p class="map-desc">此处将显示消防电源设备的实时位置和状态</p>
          </div>
          
          <!-- 地图图例 -->
          <div class="map-legend">
            <h4>设备状态图例</h4>
            <div class="legend-items">
              <div class="legend-item">
                <span class="legend-dot normal"></span>
                <span>正常运行</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot fault"></span>
                <span>设备故障</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot offline"></span>
                <span>设备离线</span>
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
        <el-descriptions-item label="电压值">
          <span :class="getVoltageClass(currentDevice.voltage)">{{ currentDevice.voltage }}V</span>
        </el-descriptions-item>
        <el-descriptions-item label="电流值">
          <span :class="getCurrentClass(currentDevice.current)">{{ currentDevice.current }}A</span>
        </el-descriptions-item>
        <el-descriptions-item label="功率">{{ currentDevice.power }}W</el-descriptions-item>
        <el-descriptions-item label="频率">{{ currentDevice.frequency }}Hz</el-descriptions-item>
        <el-descriptions-item label="安装日期">{{ currentDevice.installDate }}</el-descriptions-item>
        <el-descriptions-item label="最后维护">{{ currentDevice.lastMaintenance }}</el-descriptions-item>
        <el-descriptions-item label="最后更新" span="2">{{ currentDevice.lastUpdate }}</el-descriptions-item>
        <el-descriptions-item label="设备描述" span="2">
          {{ currentDevice.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Lightning, CircleCheckFilled, WarningFilled, CircleCloseFilled,
  Search, View, TrendCharts, Tools, Location
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const viewMode = ref('card')
const detailVisible = ref(false)
const currentDevice = ref(null)
const hasMore = ref(true)

// 筛选参数
const filterParams = reactive({
  building: '',
  floor: '',
  status: ''
})

// 设备统计
const deviceStats = reactive({
  total: 45,
  normal: 38,
  fault: 5,
  offline: 2
})

// 模拟设备数据
const mockDevices = [
  {
    id: 1,
    code: 'FP-A101-001',
    name: '消防电源监控器001',
    type: 'monitor',
    location: 'A座1楼配电室',
    status: 'normal',
    voltage: 220.5,
    current: 15.2,
    power: 3351,
    frequency: 50.0,
    installDate: '2024-01-15',
    lastMaintenance: '2024-08-20',
    lastUpdate: '2024-09-27 15:30:25',
    description: '主配电室消防电源监控设备'
  },
  {
    id: 2,
    code: 'FP-A201-002',
    name: '应急电源EPS-02',
    type: 'eps',
    location: 'A座2楼应急配电间',
    status: 'normal',
    voltage: 380.0,
    current: 25.8,
    power: 9804,
    frequency: 50.0,
    installDate: '2024-01-20',
    lastMaintenance: '2024-08-25',
    lastUpdate: '2024-09-27 15:29:45',
    description: '应急照明系统电源'
  },
  {
    id: 3,
    code: 'FP-B101-003',
    name: 'UPS不间断电源03',
    type: 'ups',
    location: 'B座1楼机房',
    status: 'fault',
    voltage: 0,
    current: 0,
    power: 0,
    frequency: 0,
    installDate: '2024-02-01',
    lastMaintenance: '2024-07-15',
    lastUpdate: '2024-09-27 14:45:12',
    description: '消防控制室UPS电源，当前故障'
  },
  {
    id: 4,
    code: 'FP-B201-004',
    name: '消防电源模块04',
    type: 'module',
    location: 'B座2楼弱电间',
    status: 'normal',
    voltage: 24.0,
    current: 8.5,
    power: 204,
    frequency: 0,
    installDate: '2024-02-10',
    lastMaintenance: '2024-09-01',
    lastUpdate: '2024-09-27 15:28:30',
    description: '消防报警系统电源模块'
  },
  {
    id: 5,
    code: 'FP-C101-005',
    name: '柴油发电机组05',
    type: 'generator',
    location: 'C座地下1楼发电机房',
    status: 'offline',
    voltage: 0,
    current: 0,
    power: 0,
    frequency: 0,
    installDate: '2024-01-10',
    lastMaintenance: '2024-06-20',
    lastUpdate: '2024-09-26 18:20:15',
    description: '备用发电机组，当前离线'
  },
  {
    id: 6,
    code: 'FP-A301-006',
    name: '消防电源监控器006',
    type: 'monitor',
    location: 'A座3楼配电间',
    status: 'normal',
    voltage: 220.8,
    current: 12.3,
    power: 2716,
    frequency: 50.0,
    installDate: '2024-01-25',
    lastMaintenance: '2024-08-30',
    lastUpdate: '2024-09-27 15:31:10',
    description: '楼层配电监控设备'
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
  
  if (filterParams.status) {
    devices = devices.filter(device => device.status === filterParams.status)
  }
  
  return devices
})

// 方法
const getDeviceIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    'monitor': 'icon-monitor',
    'eps': 'icon-eps',
    'ups': 'icon-ups',
    'module': 'icon-module',
    'generator': 'icon-generator'
  }
  return classMap[type] || 'icon-default'
}

const getDeviceTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'monitor': '电源监控器',
    'eps': '应急电源EPS',
    'ups': 'UPS不间断电源',
    'module': '电源模块',
    'generator': '发电机组'
  }
  return textMap[type] || type
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'normal': 'success',
    'fault': 'danger',
    'offline': 'info'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'normal': '正常',
    'fault': '故障',
    'offline': '离线'
  }
  return textMap[status] || status
}

const getVoltageClass = (voltage: number) => {
  if (voltage === 0) return 'voltage-zero'
  if (voltage < 200 || voltage > 250) return 'voltage-abnormal'
  return 'voltage-normal'
}

const getCurrentClass = (current: number) => {
  if (current === 0) return 'current-zero'
  if (current > 30) return 'current-high'
  return 'current-normal'
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

const handleViewHistory = (device: any) => {
  ElMessage.info(`查看设备 ${device.name} 的历史数据`)
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

.fire-power-container {
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

          &.power-total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.power-normal {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.power-fault {
            background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
          }

          &.power-offline {
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

            &.icon-monitor { color: #409eff; }
            &.icon-eps { color: #67c23a; }
            &.icon-ups { color: #e6a23c; }
            &.icon-module { color: #f56c6c; }
            &.icon-generator { color: #909399; }
          }

          .device-name {
            font-size: 16px;
            font-weight: bold;
            color: #303133;
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

            &.voltage-normal { color: #67c23a; }
            &.voltage-abnormal { color: #f56c6c; }
            &.voltage-zero { color: #909399; }

            &.current-normal { color: #67c23a; }
            &.current-high { color: #e6a23c; }
            &.current-zero { color: #909399; }
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
                &.offline { background: #909399; }
              }
            }
          }
        }
      }
    }
  }
}
</style>