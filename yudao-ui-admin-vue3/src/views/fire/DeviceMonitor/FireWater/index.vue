<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="fire-water-container dark-theme-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb>
        <el-breadcrumb-item>智慧消防</el-breadcrumb-item>
        <el-breadcrumb-item>设备监测</el-breadcrumb-item>
        <el-breadcrumb-item>消防水</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon water-total">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.total }}</div>
              <div class="stat-label">消防水设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon water-normal">
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
            <div class="stat-icon water-pressure">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ deviceStats.pressureAlarm }}</div>
              <div class="stat-label">压力报警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon water-offline">
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
            <el-option label="地下1楼" value="-1" />
            <el-option label="1楼" value="1" />
            <el-option label="2楼" value="2" />
            <el-option label="3楼" value="3" />
          </el-select>
          <el-select v-model="filterParams.type" placeholder="设备类型" style="width: 150px; margin-right: 10px;">
            <el-option label="全部类型" value="" />
            <el-option label="消火栓" value="hydrant" />
            <el-option label="喷淋系统" value="sprinkler" />
            <el-option label="水泵" value="pump" />
            <el-option label="水箱" value="tank" />
            <el-option label="阀门" value="valve" />
          </el-select>
          <el-select v-model="filterParams.status" placeholder="设备状态" style="width: 150px; margin-right: 10px;">
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="压力异常" value="pressure_alarm" />
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
                  <Setting />
                </el-icon>
                <span class="device-name">{{ device.name }}</span>
              </div>
              <div class="status-indicators">
                <el-tag :type="getStatusTagType(device.status)" size="small">
                  {{ getStatusText(device.status) }}
                </el-tag>
                <div class="pressure-indicator" v-if="device.pressure !== null">
                  <el-icon :class="getPressureIconClass(device.pressure, device.pressureRange)">
                    <Stopwatch />
                  </el-icon>
                  <span class="pressure-value">{{ device.pressure }}MPa</span>
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
              <div class="info-row" v-if="device.pressure !== null">
                <span class="info-label">水压值:</span>
                <span class="info-value pressure-value" :class="getPressureClass(device.pressure, device.pressureRange)">
                  {{ device.pressure }}MPa
                </span>
              </div>
              <div class="info-row" v-if="device.flow !== null">
                <span class="info-label">流量:</span>
                <span class="info-value">{{ device.flow }}L/min</span>
              </div>
              <div class="info-row" v-if="device.level !== null">
                <span class="info-label">水位:</span>
                <span class="info-value level-value" :class="getLevelClass(device.level)">
                  {{ device.level }}%
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">运行状态:</span>
                <span class="info-value">{{ getRunStatusText(device.runStatus) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">最后更新:</span>
                <span class="info-value">{{ device.lastUpdate }}</span>
              </div>
            </div>

            <div class="device-actions">
              <el-button size="small" @click="handleViewDetail(device)" :icon="View">详情</el-button>
              <el-button size="small" @click="handleViewTrend(device)" :icon="TrendCharts">趋势</el-button>
              <el-button 
                v-if="device.type === 'pump' && device.runStatus === 'stopped'" 
                type="success" 
                size="small" 
                @click="handleStartPump(device)"
                :icon="VideoPlay"
              >
                启动
              </el-button>
              <el-button 
                v-if="device.status === 'pressure_alarm'" 
                type="warning" 
                size="small" 
                @click="handleMaintenance(device)"
                :icon="Tools"
              >
                维护
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
            <p class="map-desc">此处将显示消防水系统设备的实时位置和状态</p>
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
                <span class="legend-dot pressure-alarm"></span>
                <span>压力报警</span>
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
        <el-descriptions-item label="水压值" v-if="currentDevice.pressure !== null">
          <span :class="getPressureClass(currentDevice.pressure, currentDevice.pressureRange)">
            {{ currentDevice.pressure }}MPa
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="压力范围" v-if="currentDevice.pressureRange">
          {{ currentDevice.pressureRange.min }}-{{ currentDevice.pressureRange.max }}MPa
        </el-descriptions-item>
        <el-descriptions-item label="流量" v-if="currentDevice.flow !== null">
          {{ currentDevice.flow }}L/min
        </el-descriptions-item>
        <el-descriptions-item label="水位" v-if="currentDevice.level !== null">
          <span :class="getLevelClass(currentDevice.level)">{{ currentDevice.level }}%</span>
        </el-descriptions-item>
        <el-descriptions-item label="运行状态">{{ getRunStatusText(currentDevice.runStatus) }}</el-descriptions-item>
        <el-descriptions-item label="额定功率" v-if="currentDevice.power">
          {{ currentDevice.power }}kW
        </el-descriptions-item>
        <el-descriptions-item label="安装日期">{{ currentDevice.installDate }}</el-descriptions-item>
        <el-descriptions-item label="最后维护">{{ currentDevice.lastMaintenance }}</el-descriptions-item>
        <el-descriptions-item label="最后更新" span="2">{{ currentDevice.lastUpdate }}</el-descriptions-item>
        <el-descriptions-item label="设备描述" span="2">
          {{ currentDevice.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 趋势图弹窗 -->
    <el-dialog v-model="trendVisible" title="数据趋势" width="800px" append-to-body>
      <div v-if="currentDevice" class="trend-content">
        <div class="trend-header">
          <h4>{{ currentDevice.name }} - 数据趋势图</h4>
          <el-radio-group v-model="trendTimeRange" size="small">
            <el-radio-button label="1h">1小时</el-radio-button>
            <el-radio-button label="24h">24小时</el-radio-button>
            <el-radio-button label="7d">7天</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="trend-chart">
          <div class="chart-placeholder">
            <el-icon class="chart-icon"><TrendCharts /></el-icon>
            <p>趋势图表加载中...</p>
            <p class="chart-desc">此处将显示设备的历史数据趋势</p>
          </div>
        </div>
      </div>
    </el-dialog>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Setting, CircleCheckFilled, WarningFilled, CircleCloseFilled, Stopwatch,
  Search, View, TrendCharts, Tools, VideoPlay, Location
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const viewMode = ref('card')
const detailVisible = ref(false)
const trendVisible = ref(false)
const currentDevice = ref(null)
const hasMore = ref(true)
const trendTimeRange = ref('24h')

// 筛选参数
const filterParams = reactive({
  building: '',
  floor: '',
  type: '',
  status: ''
})

// 设备统计
const deviceStats = reactive({
  total: 32,
  normal: 28,
  pressureAlarm: 3,
  offline: 1
})

// 模拟设备数据
const mockDevices = [
  {
    id: 1,
    code: 'FW-A101-001',
    name: '室内消火栓001',
    type: 'hydrant',
    location: 'A座1楼东侧走廊',
    status: 'normal',
    pressure: 0.35,
    pressureRange: { min: 0.3, max: 0.5 },
    flow: null,
    level: null,
    runStatus: 'standby',
    power: null,
    installDate: '2024-01-15',
    lastMaintenance: '2024-08-20',
    lastUpdate: '2024-09-27 15:30:25',
    description: '室内消火栓设备'
  },
  {
    id: 2,
    code: 'FW-B201-002',
    name: '自动喷淋系统002',
    type: 'sprinkler',
    location: 'B座2楼办公区',
    status: 'normal',
    pressure: 0.42,
    pressureRange: { min: 0.4, max: 0.6 },
    flow: 150,
    level: null,
    runStatus: 'running',
    power: null,
    installDate: '2024-01-20',
    lastMaintenance: '2024-08-25',
    lastUpdate: '2024-09-27 15:29:45',
    description: '自动喷淋灭火系统'
  },
  {
    id: 3,
    code: 'FW-PUMP-003',
    name: '消防水泵003',
    type: 'pump',
    location: '地下1楼泵房',
    status: 'normal',
    pressure: 0.65,
    pressureRange: { min: 0.6, max: 0.8 },
    flow: 2500,
    level: null,
    runStatus: 'running',
    power: 55,
    installDate: '2024-02-01',
    lastMaintenance: '2024-07-15',
    lastUpdate: '2024-09-27 15:28:30',
    description: '主消防水泵'
  },
  {
    id: 4,
    code: 'FW-TANK-004',
    name: '消防水箱004',
    type: 'tank',
    location: 'A座屋顶',
    status: 'normal',
    pressure: null,
    pressureRange: null,
    flow: null,
    level: 85,
    runStatus: 'normal',
    power: null,
    installDate: '2024-02-10',
    lastMaintenance: '2024-09-01',
    lastUpdate: '2024-09-27 15:31:10',
    description: '屋顶消防水箱'
  },
  {
    id: 5,
    code: 'FW-VALVE-005',
    name: '消防阀门005',
    type: 'valve',
    location: 'C座1楼管道井',
    status: 'pressure_alarm',
    pressure: 0.25,
    pressureRange: { min: 0.3, max: 0.5 },
    flow: null,
    level: null,
    runStatus: 'closed',
    power: null,
    installDate: '2024-01-10',
    lastMaintenance: '2024-06-20',
    lastUpdate: '2024-09-27 14:45:12',
    description: '主管道控制阀门，压力异常'
  },
  {
    id: 6,
    code: 'FW-A301-006',
    name: '室内消火栓006',
    type: 'hydrant',
    location: 'A座3楼西侧走廊',
    status: 'offline',
    pressure: 0,
    pressureRange: { min: 0.3, max: 0.5 },
    flow: null,
    level: null,
    runStatus: 'offline',
    power: null,
    installDate: '2024-01-25',
    lastMaintenance: '2024-08-30',
    lastUpdate: '2024-09-26 18:20:15',
    description: '室内消火栓设备，当前离线'
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
    if (filterParams.floor === '-1') {
      devices = devices.filter(device => device.location.includes('地下'))
    } else {
      devices = devices.filter(device => 
        device.location.includes(filterParams.floor + '楼')
      )
    }
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
    'hydrant': 'icon-hydrant',
    'sprinkler': 'icon-sprinkler',
    'pump': 'icon-pump',
    'tank': 'icon-tank',
    'valve': 'icon-valve'
  }
  return classMap[type] || 'icon-default'
}

const getDeviceTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'hydrant': '消火栓',
    'sprinkler': '喷淋系统',
    'pump': '消防水泵',
    'tank': '消防水箱',
    'valve': '消防阀门'
  }
  return textMap[type] || type
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'normal': 'success',
    'pressure_alarm': 'warning',
    'offline': 'info'
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'normal': '正常',
    'pressure_alarm': '压力异常',
    'offline': '离线'
  }
  return textMap[status] || status
}

const getPressureClass = (pressure: number, range: any) => {
  if (!range || pressure === 0) return 'pressure-zero'
  if (pressure < range.min) return 'pressure-low'
  if (pressure > range.max) return 'pressure-high'
  return 'pressure-normal'
}

const getPressureIconClass = (pressure: number, range: any) => {
  if (!range || pressure === 0) return 'pressure-icon-zero'
  if (pressure < range.min || pressure > range.max) return 'pressure-icon-abnormal'
  return 'pressure-icon-normal'
}

const getLevelClass = (level: number) => {
  if (level < 20) return 'level-low'
  if (level < 50) return 'level-medium'
  return 'level-high'
}

const getRunStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'running': '运行中',
    'stopped': '已停止',
    'standby': '待机',
    'closed': '关闭',
    'open': '开启',
    'normal': '正常',
    'offline': '离线'
  }
  return textMap[status] || status
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

const handleViewTrend = (device: any) => {
  currentDevice.value = device
  trendVisible.value = true
}

const handleStartPump = (device: any) => {
  ElMessageBox.confirm(
    `确定要启动水泵"${device.name}"吗？`,
    '系统提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    // 更新运行状态
    const index = allDevices.value.findIndex(item => item.id === device.id)
    if (index !== -1) {
      allDevices.value[index].runStatus = 'running'
      ElMessage.success('水泵启动成功')
    }
  })
}

const handleMaintenance = (device: any) => {
  ElMessage.info(`发起设备 ${device.name} 的维护工单`)
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

.fire-water-container {
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

          &.water-total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.water-normal {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.water-pressure {
            background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
          }

          &.water-offline {
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

            &.icon-hydrant { color: #e74c3c; }
            &.icon-sprinkler { color: #3498db; }
            &.icon-pump { color: #f39c12; }
            &.icon-tank { color: #27ae60; }
            &.icon-valve { color: #9b59b6; }
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

          .pressure-indicator {
            display: flex;
            align-items: center;
            font-size: 12px;

            .el-icon {
              margin-right: 4px;
              
              &.pressure-icon-normal { color: #67c23a; }
              &.pressure-icon-abnormal { color: #f56c6c; }
              &.pressure-icon-zero { color: #909399; }
            }

            .pressure-value {
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

            &.pressure-normal { color: #67c23a; }
            &.pressure-high, &.pressure-low { color: #f56c6c; }
            &.pressure-zero { color: #909399; }

            &.level-high { color: #67c23a; }
            &.level-medium { color: #e6a23c; }
            &.level-low { color: #f56c6c; }
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
                &.pressure-alarm { background: #e6a23c; }
                &.offline { background: #909399; }
              }
            }
          }
        }
      }
    }
  }

  .trend-content {
    .trend-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h4 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }

    .trend-chart {
      height: 400px;
      background: #f5f7fa;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;

      .chart-placeholder {
        text-align: center;
        color: #909399;

        .chart-icon {
          font-size: 64px;
          margin-bottom: 20px;
        }

        p {
          margin: 5px 0;
          font-size: 16px;

          &.chart-desc {
            font-size: 14px;
            color: #c0c4cc;
          }
        }
      }
    }
  }

  .dialog-footer {
    text-align: right;
  }
}
</style>
