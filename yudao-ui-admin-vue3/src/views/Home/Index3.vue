<template>
  <div class="ibms-screen">
    <!-- 顶部标题栏 -->
    <ScreenHeader :weather="weatherData" />
    
    <!-- 主内容区 -->
    <div class="ibms-screen__content">
      <!-- 左侧面板 -->
      <div class="ibms-screen__left">
        <!-- 统计卡片 -->
        <div class="ibms-screen__stats">
          <StatCard
            label="设备总数"
            :value="dashboardData.deviceTotal"
            unit="台"
            icon="ep:cpu"
            theme="blue"
            icon-color="#00b4ff"
            icon-bg="rgba(0, 180, 255, 0.15)"
            :trend="5.2"
          />
          <StatCard
            label="在线设备"
            :value="dashboardData.deviceOnline"
            unit="台"
            icon="ep:connection"
            theme="green"
            icon-color="#10b981"
            icon-bg="rgba(16, 185, 129, 0.15)"
            sub-label="在线率"
            :sub-value="onlineRate + '%'"
          />
          <StatCard
            label="今日告警"
            :value="dashboardData.alarmToday"
            unit="条"
            icon="ep:warning-filled"
            theme="red"
            icon-color="#ef4444"
            icon-bg="rgba(239, 68, 68, 0.15)"
            :trend="-12.5"
          />
          <StatCard
            label="今日能耗"
            :value="dashboardData.energyToday"
            unit="kWh"
            icon="ep:lightning"
            theme="orange"
            icon-color="#f59e0b"
            icon-bg="rgba(245, 158, 11, 0.15)"
            :trend="3.8"
          />
        </div>
        
        <!-- 设备状态饼图 -->
        <PanelCard title="设备状态" class="ibms-screen__panel" size="small">
          <DeviceStatusChart :data="deviceStatusData" height="160px" />
        </PanelCard>
        
        <!-- 子系统状态 - 只显示4个 -->
        <PanelCard title="子系统监控" class="ibms-screen__panel ibms-screen__subsystem-panel" size="small">
          <div class="ibms-screen__subsystems">
            <SubsystemCard
              v-for="system in subsystems.slice(0, 4)"
              :key="system.key"
              :name="system.name"
              :icon="system.icon"
              :icon-color="system.iconColor"
              :icon-bg="system.iconBg"
              :online-count="system.onlineCount"
              :offline-count="system.offlineCount"
              :error-count="system.errorCount"
              @click="handleSubsystemClick(system)"
            />
          </div>
        </PanelCard>
      </div>
      
      <!-- 中心区域 -->
      <div class="ibms-screen__center">
        <PanelCard title="楼宇可视化" class="ibms-screen__building-panel" size="small">
          <template #headerRight>
            <el-select v-model="selectedBuilding" placeholder="选择楼宇" size="small" style="width: 120px">
              <el-option label="全部楼宇" value="" />
              <el-option label="1号楼" value="1" />
              <el-option label="2号楼" value="2" />
            </el-select>
          </template>
          <BuildingModel :floors="buildingFloors" :env-data="envData" />
        </PanelCard>
        
        <!-- 能耗趋势 -->
        <PanelCard title="能耗趋势" class="ibms-screen__energy-panel" size="small">
          <template #headerRight>
            <el-radio-group v-model="energyTimeRange" size="small">
              <el-radio-button value="day">日</el-radio-button>
              <el-radio-button value="week">周</el-radio-button>
              <el-radio-button value="month">月</el-radio-button>
            </el-radio-group>
          </template>
          <EnergyChart :data="energyData" height="180px" />
        </PanelCard>
      </div>
      
      <!-- 右侧面板 -->
      <div class="ibms-screen__right">
        <!-- 天气信息 -->
        <WeatherWidget
          :temperature="weatherData.temperature"
          :description="weatherData.description"
          :humidity="58"
          :wind-speed="3.2"
          :aqi="65"
          weather-type="sunny"
        />
        
        <!-- 实时告警 -->
        <PanelCard title="实时告警" class="ibms-screen__panel ibms-screen__alarm-panel" size="small">
          <template #headerRight>
            <el-badge :value="alarmList.length" :max="99" type="danger">
              <el-button text type="primary" size="small" @click="handleViewAllAlarms">
                全部
              </el-button>
            </el-badge>
          </template>
          <RealTimeAlarm
            :list="alarmList.slice(0, 4)"
            :loading="alarmLoading"
            @view-all="handleViewAllAlarms"
            @item-click="handleAlarmClick"
          />
        </PanelCard>
        
        <!-- 告警排行 -->
        <PanelCard title="告警TOP5" class="ibms-screen__panel" size="small">
          <AlarmRankChart :data="alarmRankData" height="140px" />
        </PanelCard>
      </div>
    </div>
    
    <!-- 底部状态栏 -->
    <div class="ibms-screen__footer">
      <div class="ibms-screen__footer-left">
        <span class="ibms-screen__status">
          <span class="ibms-screen__status-dot"></span>
          系统正常
        </span>
        <span class="ibms-screen__footer-divider">|</span>
        <span>响应: 12ms</span>
      </div>
      <div class="ibms-screen__footer-center">
        长辉信息科技 · IBMS智慧楼宇管理平台 V1.0
      </div>
      <div class="ibms-screen__footer-right">
        <span>更新: {{ lastUpdateTime }}</span>
        <el-button text type="primary" size="small" @click="refreshData">
          <Icon icon="ep:refresh" :size="14" />
        </el-button>
        <el-button text type="primary" size="small" @click="toggleFullscreen">
          <Icon :icon="isFullscreen ? 'ep:close' : 'ep:full-screen'" :size="14" />
        </el-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { Icon } from '@/components/Icon'
import {
  ScreenHeader,
  StatCard,
  PanelCard,
  DeviceStatusChart,
  EnergyChart,
  SubsystemCard,
  AlarmRankChart,
  RealTimeAlarm,
  BuildingModel,
  WeatherWidget
} from './components'

defineOptions({ name: 'Index3' })

const router = useRouter()

// ==================== 数据状态 ====================

// 天气数据
const weatherData = ref({
  temperature: 26,
  description: '晴'
})

// 仪表盘统计数据
const dashboardData = ref({
  deviceTotal: 1337,
  deviceOnline: 1280,
  deviceOffline: 45,
  deviceAlarm: 12,
  alarmToday: 28,
  energyToday: 4520
})

// 在线率
const onlineRate = computed(() => {
  const { deviceTotal, deviceOnline } = dashboardData.value
  return deviceTotal > 0 ? Math.round((deviceOnline / deviceTotal) * 100) : 0
})

// 设备状态分布
const deviceStatusData = computed(() => ({
  online: dashboardData.value.deviceOnline,
  offline: dashboardData.value.deviceOffline,
  alarm: dashboardData.value.deviceAlarm
}))

// 子系统数据
const subsystems = ref([
  {
    key: 'hvac',
    name: '暖通空调',
    icon: 'mdi:air-conditioner',
    iconColor: '#3b82f6',
    iconBg: 'rgba(59, 130, 246, 0.15)',
    onlineCount: 256,
    offlineCount: 8,
    errorCount: 2
  },
  {
    key: 'lighting',
    name: '智能照明',
    icon: 'mdi:lightbulb-outline',
    iconColor: '#f59e0b',
    iconBg: 'rgba(245, 158, 11, 0.15)',
    onlineCount: 420,
    offlineCount: 12,
    errorCount: 0
  },
  {
    key: 'fire',
    name: '消防报警',
    icon: 'mdi:fire-extinguisher',
    iconColor: '#ef4444',
    iconBg: 'rgba(239, 68, 68, 0.15)',
    onlineCount: 180,
    offlineCount: 5,
    errorCount: 3
  },
  {
    key: 'security',
    name: '安防监控',
    icon: 'mdi:cctv',
    iconColor: '#10b981',
    iconBg: 'rgba(16, 185, 129, 0.15)',
    onlineCount: 156,
    offlineCount: 8,
    errorCount: 0
  }
])

// 楼宇选择
const selectedBuilding = ref('')

// 楼层数据 - 减少楼层
const buildingFloors = ref([
  { id: 1, name: 'B1', height: 28, color: '#374151', hasAlert: false, rooms: 15, devices: 45, temperature: 22, power: 32 },
  { id: 2, name: '1F', height: 32, color: '#0891b2', hasAlert: false, rooms: 20, devices: 68, temperature: 24, power: 56 },
  { id: 3, name: '2F', height: 32, color: '#0ea5e9', hasAlert: true, rooms: 25, devices: 82, temperature: 25, power: 78 },
  { id: 4, name: '3F', height: 32, color: '#38bdf8', hasAlert: false, rooms: 25, devices: 75, temperature: 24, power: 65 },
  { id: 5, name: '4F', height: 28, color: '#7dd3fc', hasAlert: false, rooms: 22, devices: 70, temperature: 23, power: 58 }
])

// 环境数据
const envData = ref([
  { label: '温度', value: 24.5, unit: '°C', percent: 65, color: '#f59e0b' },
  { label: '湿度', value: 58, unit: '%', percent: 58, color: '#3b82f6' },
  { label: 'PM2.5', value: 35, unit: 'μg', percent: 35, color: '#10b981' }
])

// 能耗时间范围
const energyTimeRange = ref('month')

// 能耗数据
const energyData = ref({
  dates: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
  electricity: [320, 302, 341, 374, 390, 450, 520, 510, 420, 350, 310, 290],
  water: [120, 132, 101, 134, 90, 130, 140, 152, 135, 120, 110, 100],
  gas: [220, 182, 191, 234, 290, 330, 310, 298, 265, 230, 210, 195]
})

// 告警列表
const alarmLoading = ref(false)
const alarmList = ref([
  {
    id: 1,
    title: '消防烟感探测器离线',
    deviceName: 'SM-03-042',
    location: '3层 东区走廊',
    level: 'critical' as const,
    time: new Date(Date.now() - 5 * 60 * 1000)
  },
  {
    id: 2,
    title: '空调机组温度异常',
    deviceName: 'AHU-02-001',
    location: '2层 机房',
    level: 'warning' as const,
    time: new Date(Date.now() - 15 * 60 * 1000)
  },
  {
    id: 3,
    title: '电梯运行状态异常',
    deviceName: 'ELV-01-003',
    location: '1号楼 A座',
    level: 'critical' as const,
    time: new Date(Date.now() - 32 * 60 * 1000)
  },
  {
    id: 4,
    title: '照明回路通信中断',
    deviceName: 'LT-04-012',
    location: '4层 办公区',
    level: 'info' as const,
    time: new Date(Date.now() - 45 * 60 * 1000)
  }
])

// 告警排行
const alarmRankData = ref([
  { name: '消防系统', value: 28 },
  { name: '空调系统', value: 22 },
  { name: '电梯系统', value: 18 },
  { name: '照明系统', value: 15 },
  { name: '安防系统', value: 12 }
])

// 更新时间
const lastUpdateTime = ref(dayjs().format('HH:mm:ss'))

// ==================== 方法 ====================

const handleSubsystemClick = (system: typeof subsystems.value[0]) => {
  console.log('点击子系统:', system.name)
}

const handleViewAllAlarms = () => {
  router.push('/iot/alert/record')
}

const handleAlarmClick = (alarm: typeof alarmList.value[0]) => {
  console.log('点击告警:', alarm)
}

const refreshData = () => {
  lastUpdateTime.value = dayjs().format('HH:mm:ss')
}

// 全屏功能
const isFullscreen = ref(false)

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

// 自动刷新
let refreshTimer: ReturnType<typeof setInterval>

onMounted(() => {
  refreshTimer = setInterval(() => {
    refreshData()
  }, 30000)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style lang="scss" scoped>
.ibms-screen {
  display: flex;
  flex-direction: column;
  height: var(--page-content-height);
  margin: -20px -20px 0 -20px;
  background: linear-gradient(135deg, #030a1a 0%, #0a1628 50%, #051020 100%);
  overflow: hidden;
  
  // 背景网格
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      linear-gradient(rgba(0, 180, 255, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 180, 255, 0.03) 1px, transparent 1px);
    background-size: 50px 50px;
    pointer-events: none;
    z-index: 0;
  }
  
  &__content {
    flex: 1;
    display: grid;
    grid-template-columns: 300px 1fr 300px;
    gap: 12px;
    padding: 12px;
    overflow: hidden;
    position: relative;
    z-index: 1;
    min-height: 0; // 重要：让flex子元素可以收缩
  }
  
  &__left,
  &__right {
    display: flex;
    flex-direction: column;
    gap: 10px;
    overflow: hidden;
    min-height: 0;
  }
  
  &__center {
    display: flex;
    flex-direction: column;
    gap: 10px;
    overflow: hidden;
    min-height: 0;
  }
  
  &__stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    flex-shrink: 0;
  }
  
  &__panel {
    flex-shrink: 0;
  }
  
  &__subsystem-panel {
    flex: 1;
    min-height: 0;
    
    :deep(.panel-card__body) {
      height: 100%;
      overflow: hidden;
    }
  }
  
  &__subsystems {
    display: flex;
    flex-direction: column;
    gap: 6px;
    height: 100%;
  }
  
  &__building-panel {
    flex: 1;
    min-height: 0;
    
    :deep(.panel-card__body) {
      height: calc(100% - 40px);
      overflow: hidden;
    }
  }
  
  &__energy-panel {
    flex-shrink: 0;
    height: 240px;
  }
  
  &__alarm-panel {
    flex: 1;
    min-height: 0;
    
    :deep(.panel-card__body) {
      height: calc(100% - 40px);
      overflow: hidden;
    }
  }
  
  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 32px;
    padding: 0 16px;
    background: rgba(5, 20, 45, 0.95);
    border-top: 1px solid rgba(0, 180, 255, 0.2);
    font-size: 11px;
    color: rgba(255, 255, 255, 0.6);
    position: relative;
    z-index: 1;
    flex-shrink: 0;
    
    &-left,
    &-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    &-center {
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      color: rgba(0, 180, 255, 0.8);
    }
    
    &-divider {
      color: rgba(0, 180, 255, 0.3);
    }
  }
  
  &__status {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #10b981;
    
    &-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #10b981;
      animation: pulse 2s ease-in-out infinite;
    }
  }
}

// 自定义Element Plus组件样式
:deep(.el-select) {
  .el-input__wrapper {
    background: rgba(5, 20, 45, 0.8);
    border-color: rgba(0, 180, 255, 0.3);
    box-shadow: none;
    
    &:hover {
      border-color: rgba(0, 180, 255, 0.5);
    }
  }
  
  .el-input__inner {
    color: #fff;
    font-size: 12px;
  }
}

:deep(.el-radio-group) {
  .el-radio-button__inner {
    background: rgba(5, 20, 45, 0.8);
    border-color: rgba(0, 180, 255, 0.3);
    color: rgba(255, 255, 255, 0.7);
    padding: 4px 10px;
    font-size: 12px;
    
    &:hover {
      color: #00b4ff;
    }
  }
  
  .el-radio-button__original-radio:checked + .el-radio-button__inner {
    background: rgba(0, 180, 255, 0.2);
    border-color: #00b4ff;
    color: #00b4ff;
    box-shadow: -1px 0 0 0 #00b4ff;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4);
  }
  50% {
    opacity: 0.7;
    box-shadow: 0 0 0 4px rgba(16, 185, 129, 0);
  }
}
</style>
