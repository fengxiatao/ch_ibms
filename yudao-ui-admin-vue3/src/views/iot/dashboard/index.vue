<template>
  <div class="dashboard-container">
    <!-- 统计卡片行 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="设备总数"
          :value="deviceStats.totalDevices"
          :sub-title="`今日新增 ${deviceStats.todayNewDevices}`"
          icon="mdi:devices"
          icon-bg-color="#409EFF"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="在线设备"
          :value="deviceStats.onlineDevices"
          :sub-title="`在线率 ${deviceStats.onlineRate?.toFixed(1)}%`"
          icon="mdi:check-circle"
          icon-bg-color="#67C23A"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="今日告警"
          :value="alertStats.todayAlerts"
          :sub-title="`未处理 ${alertStats.unhandledAlerts}`"
          icon="mdi:alert"
          icon-bg-color="#E6A23C"
        />
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <StatCard
          title="告警总数"
          :value="alertStats.totalAlerts"
          :sub-title="`处理率 ${alertStats.handledRate?.toFixed(1)}%`"
          icon="mdi:chart-line"
          icon-bg-color="#F56C6C"
        />
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :xs="24" :md="12">
        <el-card>
          <DeviceStatusChart :data="deviceStats.devicesByStatus" height="350px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <AlertLevelChart :data="alertStats.alertsByLevel" height="350px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :xs="24" :md="12">
        <el-card>
          <AlertTrendChart :data="alertStats.alertTrend" height="350px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <SystemLoadChart :data="realTimeMonitor.systemLoad" height="350px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时监控数据 -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新告警</span>
              <el-button text type="primary" @click="refreshData">
                <Icon icon="mdi:refresh" />
                刷新
              </el-button>
            </div>
          </template>
          <el-table :data="realTimeMonitor.latestAlerts" height="300">
            <el-table-column prop="alertName" label="告警名称" min-width="120" />
            <el-table-column prop="deviceName" label="设备名称" min-width="100" />
            <el-table-column prop="level" label="级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertTime" label="时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.alertTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新事件</span>
              <el-button text type="primary" @click="refreshData">
                <Icon icon="mdi:refresh" />
                刷新
              </el-button>
            </div>
          </template>
          <el-table :data="realTimeMonitor.latestEvents" height="300">
            <el-table-column prop="eventType" label="事件类型" min-width="120" />
            <el-table-column prop="deviceName" label="设备名称" min-width="100" />
            <el-table-column prop="eventTime" label="时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.eventTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import * as DashboardApi from '@/api/iot/dashboard'
import StatCard from './components/StatCard.vue'
import DeviceStatusChart from './components/DeviceStatusChart.vue'
import AlertLevelChart from './components/AlertLevelChart.vue'
import AlertTrendChart from './components/AlertTrendChart.vue'
import SystemLoadChart from './components/SystemLoadChart.vue'
import { formatDate } from '@/utils/formatTime'

const deviceStats = ref<DashboardApi.DeviceStatisticsVO>({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  faultDevices: 0,
  onlineRate: 0,
  devicesByProduct: {},
  devicesByStatus: {},
  todayNewDevices: 0,
  weekNewDevices: 0
})

const alertStats = ref<DashboardApi.AlertStatisticsVO>({
  totalAlerts: 0,
  todayAlerts: 0,
  weekAlerts: 0,
  monthAlerts: 0,
  unhandledAlerts: 0,
  handledAlerts: 0,
  alertsByLevel: {},
  alertsByType: {},
  alertTrend: [],
  handledRate: 0
})

const realTimeMonitor = ref<DashboardApi.RealTimeMonitorVO>({
  latestAlerts: [],
  deviceStatusChanges: [],
  latestEvents: [],
  systemLoad: {
    cpuUsage: 0,
    memoryUsage: 0,
    diskUsage: 0,
    messageQueueBacklog: 0,
    databaseConnections: 0
  }
})

const loadDeviceStatistics = async () => {
  try {
    const data = await DashboardApi.getDeviceStatistics()
    deviceStats.value = data
  } catch (error) {
    console.error('加载设备统计数据失败:', error)
  }
}

const loadAlertStatistics = async () => {
  try {
    const data = await DashboardApi.getAlertStatistics()
    alertStats.value = data
  } catch (error) {
    console.error('加载告警统计数据失败:', error)
  }
}

const loadRealTimeMonitor = async () => {
  try {
    const data = await DashboardApi.getRealTimeMonitor()
    realTimeMonitor.value = data
  } catch (error) {
    console.error('加载实时监控数据失败:', error)
  }
}

const refreshData = async () => {
  await Promise.all([loadDeviceStatistics(), loadAlertStatistics(), loadRealTimeMonitor()])
}

const getLevelType = (level: string) => {
  const typeMap: Record<string, any> = {
    CRITICAL: 'danger',
    ERROR: 'danger',
    WARNING: 'warning',
    INFO: 'info'
  }
  return typeMap[level] || 'info'
}

let timer: NodeJS.Timeout | null = null

onMounted(async () => {
  await refreshData()
  
  // 每30秒自动刷新数据
  timer = setInterval(() => {
    refreshData()
  }, 30000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 16px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>












