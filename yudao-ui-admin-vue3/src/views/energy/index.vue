<template>
  <ContentWrap style="margin-top: 70px;">
    <div class="ibms-energy dark-theme-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <Icon icon="ep:lightning" :size="24" />
        <h1>智慧能源</h1>
      </div>
      <div class="energy-status">
        <div class="status-item">
          <span class="label">总功率:</span>
          <span class="value">{{ energyStatus.totalPower }}kW</span>
        </div>
        <div class="status-item">
          <span class="label">今日用电:</span>
          <span class="value">{{ energyStatus.todayUsage }}kWh</span>
        </div>
        <div class="status-item">
          <span class="label">碳排放:</span>
          <span class="value">{{ energyStatus.carbonEmission }}kg CO₂</span>
        </div>
      </div>
    </div>

    <!-- 能源概览 -->
    <div class="energy-overview">
      <div class="overview-card electric">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:lightning" />
          </div>
          <div class="card-title">电力系统</div>
          <div class="card-status normal">正常</div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">实时功率</span>
            <span class="metric-value">{{ energyData.electric.power }}kW</span>
          </div>
          <div class="metric">
            <span class="metric-label">电压</span>
            <span class="metric-value">{{ energyData.electric.voltage }}V</span>
          </div>
          <div class="metric">
            <span class="metric-label">电流</span>
            <span class="metric-value">{{ energyData.electric.current }}A</span>
          </div>
        </div>
      </div>

      <div class="overview-card water">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:water-cup" />
          </div>
          <div class="card-title">给排水</div>
          <div class="card-status normal">正常</div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">水压</span>
            <span class="metric-value">{{ energyData.water.pressure }}MPa</span>
          </div>
          <div class="metric">
            <span class="metric-label">流量</span>
            <span class="metric-value">{{ energyData.water.flow }}L/min</span>
          </div>
          <div class="metric">
            <span class="metric-label">用水量</span>
            <span class="metric-value">{{ energyData.water.usage }}m³</span>
          </div>
        </div>
      </div>

      <div class="overview-card hvac">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:wind-power" />
          </div>
          <div class="card-title">暖通空调</div>
          <div class="card-status normal">正常</div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">室内温度</span>
            <span class="metric-value">{{ energyData.hvac.temperature }}°C</span>
          </div>
          <div class="metric">
            <span class="metric-label">湿度</span>
            <span class="metric-value">{{ energyData.hvac.humidity }}%</span>
          </div>
          <div class="metric">
            <span class="metric-label">能耗</span>
            <span class="metric-value">{{ energyData.hvac.consumption }}kWh</span>
          </div>
        </div>
      </div>

      <div class="overview-card lighting">
        <div class="card-header">
          <div class="card-icon">
            <Icon icon="ep:sunny" />
          </div>
          <div class="card-title">照明系统</div>
          <div class="card-status normal">正常</div>
        </div>
        <div class="card-content">
          <div class="metric">
            <span class="metric-label">开启率</span>
            <span class="metric-value">{{ energyData.lighting.onRate }}%</span>
          </div>
          <div class="metric">
            <span class="metric-label">亮度</span>
            <span class="metric-value">{{ energyData.lighting.brightness }}lx</span>
          </div>
          <div class="metric">
            <span class="metric-label">能耗</span>
            <span class="metric-value">{{ energyData.lighting.consumption }}kWh</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧监控面板 -->
      <div class="monitor-panel">
        <div class="panel-header">
          <h3>能源监控</h3>
          <div class="time-range">
            <el-radio-group v-model="timeRange" @change="changeTimeRange">
              <el-radio-button label="realtime">实时</el-radio-button>
              <el-radio-button label="hour">小时</el-radio-button>
              <el-radio-button label="day">日</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 实时监控图表 -->
        <div class="monitor-charts">
          <!-- 电力消耗图表 -->
          <div class="chart-container">
            <div class="chart-title">电力消耗趋势</div>
            <PowerConsumptionChart 
              :data="chartData.powerConsumption" 
              :timeRange="timeRange"
            />
          </div>

          <!-- 能源分布图表 -->
          <div class="chart-container">
            <div class="chart-title">能源分布</div>
            <EnergyDistributionChart :data="chartData.energyDistribution" />
          </div>

          <!-- 碳排放图表 -->
          <div class="chart-container">
            <div class="chart-title">碳排放监控</div>
            <CarbonEmissionChart 
              :data="chartData.carbonEmission" 
              :timeRange="timeRange"
            />
          </div>
        </div>
      </div>

      <!-- 右侧管理面板 -->
      <div class="management-panel">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 设备监控 -->
          <el-tab-pane label="设备监控" name="devices">
            <div class="device-monitor">
              <div class="device-filter">
                <el-select v-model="deviceFilter" placeholder="设备类型" size="small">
                  <el-option label="全部设备" value="" />
                  <el-option label="配电设备" value="electric" />
                  <el-option label="给排水" value="water" />
                  <el-option label="空调系统" value="hvac" />
                  <el-option label="照明设备" value="lighting" />
                </el-select>
                <el-input 
                  v-model="deviceSearch" 
                  placeholder="搜索设备"
                  :prefix-icon="Search"
                  size="small"
                  style="width: 150px"
                />
              </div>

              <div class="device-list">
                <div 
                  v-for="device in filteredDevices" 
                  :key="device.id"
                  :class="['device-item', device.status]"
                >
                  <div class="device-header">
                    <div class="device-icon">
                      <Icon :icon="getDeviceIcon(device.type)" />
                    </div>
                    <div class="device-info">
                      <div class="device-name">{{ device.name }}</div>
                      <div class="device-location">{{ device.location }}</div>
                    </div>
                    <div class="device-status" :class="device.status">
                      {{ getDeviceStatusText(device.status) }}
                    </div>
                  </div>
                  <div class="device-metrics">
                    <div 
                      v-for="metric in device.metrics" 
                      :key="metric.name"
                      class="metric-item"
                    >
                      <span class="metric-name">{{ metric.name }}</span>
                      <span class="metric-value" :class="{ warning: metric.warning, danger: metric.danger }">
                        {{ metric.value }}{{ metric.unit }}
                      </span>
                    </div>
                  </div>
                  <div class="device-actions">
                    <el-button size="small" @click="viewDevice(device)">查看</el-button>
                    <el-button size="small" @click="controlDevice(device)">控制</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 告警管理 -->
          <el-tab-pane label="告警管理" name="alarms">
            <div class="alarm-management">
              <div class="alarm-summary">
                <div class="summary-card high">
                  <div class="summary-count">{{ alarmStats.high }}</div>
                  <div class="summary-label">高级告警</div>
                </div>
                <div class="summary-card medium">
                  <div class="summary-count">{{ alarmStats.medium }}</div>
                  <div class="summary-label">中级告警</div>
                </div>
                <div class="summary-card low">
                  <div class="summary-count">{{ alarmStats.low }}</div>
                  <div class="summary-label">低级告警</div>
                </div>
              </div>

              <div class="alarm-filter">
                <el-select v-model="alarmFilter" placeholder="告警级别" size="small">
                  <el-option label="全部级别" value="" />
                  <el-option label="高级告警" value="high" />
                  <el-option label="中级告警" value="medium" />
                  <el-option label="低级告警" value="low" />
                </el-select>
                <el-select v-model="alarmTypeFilter" placeholder="告警类型" size="small">
                  <el-option label="全部类型" value="" />
                  <el-option label="电力异常" value="electric" />
                  <el-option label="水压异常" value="water" />
                  <el-option label="温度异常" value="temperature" />
                  <el-option label="设备故障" value="device" />
                </el-select>
              </div>

              <div class="alarm-list">
                <div 
                  v-for="alarm in filteredAlarms" 
                  :key="alarm.id"
                  :class="['alarm-item', alarm.level]"
                >
                  <div class="alarm-icon">
                    <Icon :icon="getAlarmIcon(alarm.type)" />
                  </div>
                  <div class="alarm-content">
                    <div class="alarm-title">{{ alarm.title }}</div>
                    <div class="alarm-desc">{{ alarm.description }}</div>
                    <div class="alarm-meta">
                      <span class="alarm-location">{{ alarm.location }}</span>
                      <span class="alarm-time">{{ formatTime(alarm.time) }}</span>
                    </div>
                  </div>
                  <div class="alarm-level" :class="alarm.level">
                    {{ getAlarmLevelText(alarm.level) }}
                  </div>
                  <div class="alarm-actions">
                    <el-button 
                      v-if="!alarm.handled"
                      size="small" 
                      type="primary" 
                      @click="handleAlarm(alarm)"
                    >
                      处理
                    </el-button>
                    <el-button size="small" @click="viewAlarmDetail(alarm)">详情</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 节能管理 -->
          <el-tab-pane label="节能管理" name="saving">
            <div class="energy-saving">
              <div class="saving-summary">
                <div class="summary-item">
                  <div class="summary-icon">
                    <Icon icon="ep:trend-charts" />
                  </div>
                  <div class="summary-content">
                    <div class="summary-value">{{ savingData.totalSaved }}kWh</div>
                    <div class="summary-label">总节能量</div>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-icon">
                    <Icon icon="ep:money" />
                  </div>
                  <div class="summary-content">
                    <div class="summary-value">¥{{ savingData.costSaved.toLocaleString() }}</div>
                    <div class="summary-label">节约费用</div>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-icon">
                    <Icon icon="ep:wind-power" />
                  </div>
                  <div class="summary-content">
                    <div class="summary-value">{{ savingData.carbonReduced }}kg</div>
                    <div class="summary-label">减少碳排</div>
                  </div>
                </div>
              </div>

              <div class="saving-strategies">
                <div class="strategies-header">
                  <h4>节能策略</h4>
                  <el-button size="small" type="primary" @click="addStrategy">
                    新增策略
                  </el-button>
                </div>
                <div class="strategies-list">
                  <div 
                    v-for="strategy in savingStrategies" 
                    :key="strategy.id"
                    class="strategy-item"
                  >
                    <div class="strategy-header">
                      <div class="strategy-name">{{ strategy.name }}</div>
                      <div class="strategy-status" :class="strategy.status">
                        {{ getStrategyStatusText(strategy.status) }}
                      </div>
                    </div>
                    <div class="strategy-desc">{{ strategy.description }}</div>
                    <div class="strategy-metrics">
                      <div class="metric">
                        <span class="metric-label">预计节能:</span>
                        <span class="metric-value">{{ strategy.expectedSaving }}kWh/月</span>
                      </div>
                      <div class="metric">
                        <span class="metric-label">实际节能:</span>
                        <span class="metric-value">{{ strategy.actualSaving }}kWh/月</span>
                      </div>
                    </div>
                    <div class="strategy-actions">
                      <el-button size="small" @click="editStrategy(strategy)">编辑</el-button>
                      <el-button 
                        size="small" 
                        :type="strategy.status === 'active' ? 'danger' : 'success'"
                        @click="toggleStrategy(strategy)"
                      >
                        {{ strategy.status === 'active' ? '停用' : '启用' }}
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 报表分析 -->
          <el-tab-pane label="报表分析" name="reports">
            <div class="report-analysis">
              <div class="report-controls">
                <el-date-picker
                  v-model="reportDate"
                  type="monthrange"
                  range-separator="至"
                  start-placeholder="开始月份"
                  end-placeholder="结束月份"
                  size="small"
                  @change="generateReport"
                />
                <el-button size="small" type="primary" @click="exportReport">
                  导出报表
                </el-button>
              </div>

              <div class="report-content">
                <div class="report-section">
                  <h4>用电分析</h4>
                  <div class="report-chart">
                    <ElectricityReportChart :data="reportData.electricity" />
                  </div>
                </div>

                <div class="report-section">
                  <h4>能效对比</h4>
                  <div class="efficiency-comparison">
                    <div class="comparison-item">
                      <div class="comparison-title">本月能效</div>
                      <div class="comparison-value">{{ reportData.thisMonthEfficiency }}%</div>
                    </div>
                    <div class="comparison-item">
                      <div class="comparison-title">上月能效</div>
                      <div class="comparison-value">{{ reportData.lastMonthEfficiency }}%</div>
                    </div>
                    <div class="comparison-item">
                      <div class="comparison-title">同比变化</div>
                      <div
class="comparison-value" :class="{ 
                        positive: reportData.efficiencyChange > 0,
                        negative: reportData.efficiencyChange < 0
                      }">
                        {{ reportData.efficiencyChange > 0 ? '+' : '' }}{{ reportData.efficiencyChange }}%
                      </div>
                    </div>
                  </div>
                </div>

                <div class="report-section">
                  <h4>成本分析</h4>
                  <div class="cost-breakdown">
                    <div class="cost-item">
                      <span class="cost-label">电费:</span>
                      <span class="cost-value">¥{{ reportData.electricCost.toLocaleString() }}</span>
                    </div>
                    <div class="cost-item">
                      <span class="cost-label">水费:</span>
                      <span class="cost-value">¥{{ reportData.waterCost.toLocaleString() }}</span>
                    </div>
                    <div class="cost-item">
                      <span class="cost-label">燃气费:</span>
                      <span class="cost-value">¥{{ reportData.gasCost.toLocaleString() }}</span>
                    </div>
                    <div class="cost-item total">
                      <span class="cost-label">总计:</span>
                      <span class="cost-value">¥{{ (reportData.electricCost + reportData.waterCost + reportData.gasCost).toLocaleString() }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ContentWrap } from '@/components/ContentWrap'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import PowerConsumptionChart from './components/PowerConsumptionChart.vue'
import EnergyDistributionChart from './components/EnergyDistributionChart.vue'
import CarbonEmissionChart from './components/CarbonEmissionChart.vue'
import ElectricityReportChart from './components/ElectricityReportChart.vue'

// 响应式数据
const timeRange = ref('realtime')
const activeTab = ref('devices')
const deviceFilter = ref('')
const deviceSearch = ref('')
const alarmFilter = ref('')
const alarmTypeFilter = ref('')
const reportDate = ref<[Date, Date]>([new Date(), new Date()])

// 能源状态
const energyStatus = ref({
  totalPower: 1245.6,
  todayUsage: 2847.3,
  carbonEmission: 1423.5
})

// 能源数据
const energyData = ref({
  electric: {
    power: 1245.6,
    voltage: 380,
    current: 1876.5
  },
  water: {
    pressure: 0.45,
    flow: 125.8,
    usage: 234.7
  },
  hvac: {
    temperature: 24.5,
    humidity: 55,
    consumption: 456.2
  },
  lighting: {
    onRate: 78,
    brightness: 450,
    consumption: 123.4
  }
})

// 图表数据
const chartData = ref({
  powerConsumption: [],
  energyDistribution: [],
  carbonEmission: []
})

// 设备列表
const deviceList = ref([
  {
    id: 1,
    name: '主配电柜A',
    type: 'electric',
    location: 'A座地下室',
    status: 'normal',
    metrics: [
      { name: '电压', value: 380, unit: 'V', warning: false, danger: false },
      { name: '电流', value: 245, unit: 'A', warning: false, danger: false },
      { name: '功率', value: 93.1, unit: 'kW', warning: false, danger: false }
    ]
  },
  {
    id: 2,
    name: '冷却塔1#',
    type: 'hvac',
    location: 'A座天台',
    status: 'warning',
    metrics: [
      { name: '温度', value: 28.5, unit: '°C', warning: true, danger: false },
      { name: '流量', value: 156, unit: 'L/min', warning: false, danger: false },
      { name: '功率', value: 15.6, unit: 'kW', warning: false, danger: false }
    ]
  },
  {
    id: 3,
    name: '给水泵2#',
    type: 'water',
    location: 'B座机房',
    status: 'fault',
    metrics: [
      { name: '水压', value: 0.25, unit: 'MPa', warning: false, danger: true },
      { name: '流量', value: 45, unit: 'L/min', warning: true, danger: false },
      { name: '功率', value: 8.2, unit: 'kW', warning: false, danger: false }
    ]
  }
])

// 告警统计
const alarmStats = ref({
  high: 2,
  medium: 5,
  low: 8
})

// 告警列表
const alarmList = ref([
  {
    id: 1,
    type: 'electric',
    level: 'high',
    title: '主配电柜电压异常',
    description: '电压超出正常范围，可能影响设备正常运行',
    location: 'A座地下室',
    time: new Date(Date.now() - 300000),
    handled: false
  },
  {
    id: 2,
    type: 'water',
    level: 'medium',
    title: '给水泵压力不足',
    description: '水压低于设定值，需要检查泵体状态',
    location: 'B座机房',
    time: new Date(Date.now() - 600000),
    handled: false
  },
  {
    id: 3,
    type: 'temperature',
    level: 'low',
    title: '空调温度偏高',
    description: '室内温度超过设定值2°C',
    location: 'A座3楼',
    time: new Date(Date.now() - 900000),
    handled: true
  }
])

// 节能数据
const savingData = ref({
  totalSaved: 12456,
  costSaved: 8924,
  carbonReduced: 6234
})

// 节能策略
const savingStrategies = ref([
  {
    id: 1,
    name: '智能照明控制',
    description: '根据人员活动和自然光线自动调节照明亮度',
    status: 'active',
    expectedSaving: 850,
    actualSaving: 782
  },
  {
    id: 2,
    name: '空调分时控制',
    description: '非办公时间自动降低空调运行功率',
    status: 'active',
    expectedSaving: 1200,
    actualSaving: 1156
  },
  {
    id: 3,
    name: '设备定时启停',
    description: '根据使用规律自动启停非关键设备',
    status: 'inactive',
    expectedSaving: 450,
    actualSaving: 0
  }
])

// 报表数据
const reportData = ref({
  electricity: [],
  thisMonthEfficiency: 85.6,
  lastMonthEfficiency: 82.3,
  efficiencyChange: 3.3,
  electricCost: 45678,
  waterCost: 12345,
  gasCost: 8901
})

// 计算属性
const filteredDevices = computed(() => {
  let devices = deviceList.value
  
  if (deviceFilter.value) {
    devices = devices.filter(device => device.type === deviceFilter.value)
  }
  
  if (deviceSearch.value) {
    devices = devices.filter(device => 
      device.name.includes(deviceSearch.value) || 
      device.location.includes(deviceSearch.value)
    )
  }
  
  return devices
})

const filteredAlarms = computed(() => {
  let alarms = alarmList.value
  
  if (alarmFilter.value) {
    alarms = alarms.filter(alarm => alarm.level === alarmFilter.value)
  }
  
  if (alarmTypeFilter.value) {
    alarms = alarms.filter(alarm => alarm.type === alarmTypeFilter.value)
  }
  
  return alarms
})

// 方法
const formatTime = (date: Date) => {
  return date.toLocaleString('zh-CN')
}

const changeTimeRange = (range: string) => {
  ElMessage.info(`切换到${range === 'realtime' ? '实时' : range === 'hour' ? '小时' : range === 'day' ? '日' : '月'}视图`)
  // 更新图表数据
}

const getDeviceIcon = (type: string) => {
  const iconMap = {
    electric: 'ep:lightning',
    water: 'ep:water-cup',
    hvac: 'ep:wind-power',
    lighting: 'ep:sunny'
  }
  return iconMap[type] || 'ep:monitor'
}

const getDeviceStatusText = (status: string) => {
  const statusMap = {
    normal: '正常',
    warning: '告警',
    fault: '故障',
    offline: '离线'
  }
  return statusMap[status] || '未知'
}

const getAlarmIcon = (type: string) => {
  const iconMap = {
    electric: 'ep:lightning',
    water: 'ep:water-cup',
    temperature: 'ep:thermometer',
    device: 'ep:monitor'
  }
  return iconMap[type] || 'ep:warning'
}

const getAlarmLevelText = (level: string) => {
  const levelMap = {
    high: '高级',
    medium: '中级',
    low: '低级'
  }
  return levelMap[level] || '未知'
}

const getStrategyStatusText = (status: string) => {
  const statusMap = {
    active: '运行中',
    inactive: '已停用',
    pending: '待启用'
  }
  return statusMap[status] || '未知'
}

const viewDevice = (device: any) => {
  ElMessage.info(`查看设备: ${device.name}`)
}

const controlDevice = (device: any) => {
  ElMessage.info(`控制设备: ${device.name}`)
}

const handleAlarm = (alarm: any) => {
  alarm.handled = true
  ElMessage.success(`告警 ${alarm.title} 已处理`)
}

const viewAlarmDetail = (alarm: any) => {
  ElMessage.info(`查看告警详情: ${alarm.title}`)
}

const addStrategy = () => {
  ElMessage.info('新增节能策略功能开发中...')
}

const editStrategy = (strategy: any) => {
  ElMessage.info(`编辑策略: ${strategy.name}`)
}

const toggleStrategy = (strategy: any) => {
  const newStatus = strategy.status === 'active' ? 'inactive' : 'active'
  strategy.status = newStatus
  ElMessage.success(`策略 ${strategy.name} 已${newStatus === 'active' ? '启用' : '停用'}`)
}

const generateReport = () => {
  ElMessage.success('报表已生成')
}

const exportReport = () => {
  ElMessage.info('正在导出报表...')
}

onMounted(() => {
  // 初始化数据
})
</script>

<style lang="scss" scoped>@use '@/styles/dark-theme.scss';

.ibms-energy {
  padding: 20px;
  background: linear-gradient(135deg, #0a1628 0%, #1e3a8a 50%, #0f172a 100%);
  min-height: auto;
  color: #ffffff;

  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
    padding: 16px 24px;
    background: rgba(15, 23, 42, 0.8);
    backdrop-filter: blur(15px);
    border-radius: 12px;
    border: 1px solid rgba(0, 212, 255, 0.1);

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        color: #00d4ff;
      }

      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #00d4ff;
      }
    }

    .energy-status {
      display: flex;
      gap: 24px;

      .status-item {
        display: flex;
        flex-direction: column;
        align-items: flex-end;

        .label {
          font-size: 12px;
          color: #94a3b8;
          margin-bottom: 4px;
        }

        .value {
          font-size: 18px;
          font-weight: bold;
          color: #00d4ff;
        }
      }
    }
  }

  .energy-overview {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .overview-card {
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(15px);
      border-radius: 12px;
      border: 1px solid rgba(0, 212, 255, 0.2);
      padding: 20px;

      .card-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;

        .card-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #00d4ff 0%, #0099cc 100%);

          .el-icon {
            font-size: 20px;
            color: #ffffff;
          }
        }

        .card-title {
          flex: 1;
          margin-left: 12px;
          font-size: 16px;
          font-weight: 500;
          color: #ffffff;
        }

        .card-status {
          padding: 4px 8px;
          border-radius: 12px;
          font-size: 12px;

          &.normal {
            background: #22c55e;
            color: #ffffff;
          }

          &.warning {
            background: #f59e0b;
            color: #ffffff;
          }

          &.fault {
            background: #ef4444;
            color: #ffffff;
          }
        }
      }

      .card-content {
        .metric {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 14px;

          &:last-child {
            margin-bottom: 0;
          }

          .metric-label {
            color: #94a3b8;
          }

          .metric-value {
            color: #00d4ff;
            font-weight: 500;
          }
        }
      }
    }
  }

  .main-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 380px);

    .monitor-panel {
      flex: 2;
      background: rgba(15, 23, 42, 0.8);
      backdrop-filter: blur(15px);
      border-radius: 12px;
      border: 1px solid rgba(0, 212, 255, 0.1);
      padding: 20px;

      .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 1px solid rgba(0, 212, 255, 0.1);

        h3 {
          margin: 0;
          color: #00d4ff;
        }
      }

      .monitor-charts {
        display: grid;
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr 1fr;
        gap: 20px;
        height: calc(100% - 80px);

        .chart-container {
          background: rgba(0, 212, 255, 0.02);
          border-radius: 8px;
          padding: 16px;
          border: 1px solid rgba(0, 212, 255, 0.1);

          &:first-child {
            grid-column: 1 / -1;
          }

          .chart-title {
            color: #00d4ff;
            font-size: 14px;
            margin-bottom: 12px;
            font-weight: 500;
          }
        }
      }
    }

    .management-panel {
      width: 400px;

      :deep(.el-tabs) {
        height: 100%;
        background: rgba(15, 23, 42, 0.8);
        backdrop-filter: blur(15px);
        border: 1px solid rgba(0, 212, 255, 0.1);

        .el-tabs__header {
          background: rgba(0, 212, 255, 0.05);
          margin: 0;

          .el-tabs__nav {
            background: transparent;

            .el-tabs__item {
              color: #94a3b8;
              border-color: rgba(0, 212, 255, 0.1);

              &.is-active {
                color: #00d4ff;
                background: rgba(0, 212, 255, 0.1);
              }
            }
          }
        }

        .el-tabs__content {
          height: calc(100% - 60px);
          padding: 20px;
          color: #ffffff;

          .el-tab-pane {
            height: 100%;
            display: flex;
            flex-direction: column;
          }
        }
      }

      // 设备监控样式
      .device-monitor {
        height: 100%;
        display: flex;
        flex-direction: column;

        .device-filter {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;
        }

        .device-list {
          flex: 1;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .device-item {
            padding: 16px;
            background: rgba(0, 212, 255, 0.05);
            border-radius: 8px;
            border-left: 4px solid;

            &.normal {
              border-left-color: #22c55e;
            }

            &.warning {
              border-left-color: #f59e0b;
            }

            &.fault {
              border-left-color: #ef4444;
            }

            &.offline {
              border-left-color: #64748b;
            }

            .device-header {
              display: flex;
              align-items: center;
              margin-bottom: 12px;

              .device-icon {
                width: 32px;
                height: 32px;
                background: rgba(0, 212, 255, 0.2);
                border-radius: 6px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-right: 12px;

                .el-icon {
                  color: #00d4ff;
                }
              }

              .device-info {
                flex: 1;

                .device-name {
                  font-size: 14px;
                  color: #ffffff;
                  margin-bottom: 2px;
                }

                .device-location {
                  font-size: 12px;
                  color: #94a3b8;
                }
              }

              .device-status {
                padding: 4px 8px;
                border-radius: 12px;
                font-size: 12px;

                &.normal { background: #22c55e; color: #ffffff; }
                &.warning { background: #f59e0b; color: #ffffff; }
                &.fault { background: #ef4444; color: #ffffff; }
                &.offline { background: #64748b; color: #ffffff; }
              }
            }

            .device-metrics {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 8px;
              margin-bottom: 12px;

              .metric-item {
                display: flex;
                justify-content: space-between;
                font-size: 12px;

                .metric-name {
                  color: #94a3b8;
                }

                .metric-value {
                  color: #ffffff;

                  &.warning {
                    color: #f59e0b;
                  }

                  &.danger {
                    color: #ef4444;
                  }
                }
              }
            }

            .device-actions {
              display: flex;
              gap: 8px;
            }
          }
        }
      }

      // 告警管理样式
      .alarm-management {
        height: 100%;
        display: flex;
        flex-direction: column;

        .alarm-summary {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;

          .summary-card {
            flex: 1;
            padding: 12px;
            border-radius: 8px;
            text-align: center;

            &.high { background: rgba(239, 68, 68, 0.2); }
            &.medium { background: rgba(245, 158, 11, 0.2); }
            &.low { background: rgba(34, 197, 94, 0.2); }

            .summary-count {
              font-size: 20px;
              font-weight: bold;
              color: #ffffff;
              margin-bottom: 4px;
            }

            .summary-label {
              font-size: 12px;
              color: #94a3b8;
            }
          }
        }

        .alarm-filter {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;
        }

        .alarm-list {
          flex: 1;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 12px;

          .alarm-item {
            padding: 16px;
            border-radius: 8px;
            display: flex;
            gap: 12px;
            border-left: 4px solid;

            &.high {
              background: rgba(239, 68, 68, 0.1);
              border-left-color: #ef4444;
            }

            &.medium {
              background: rgba(245, 158, 11, 0.1);
              border-left-color: #f59e0b;
            }

            &.low {
              background: rgba(34, 197, 94, 0.1);
              border-left-color: #22c55e;
            }

            .alarm-icon {
              width: 32px;
              height: 32px;
              background: rgba(239, 68, 68, 0.2);
              border-radius: 6px;
              display: flex;
              align-items: center;
              justify-content: center;

              .el-icon {
                color: #ef4444;
              }
            }

            .alarm-content {
              flex: 1;

              .alarm-title {
                font-size: 14px;
                color: #ffffff;
                margin-bottom: 4px;
              }

              .alarm-desc {
                font-size: 12px;
                color: #94a3b8;
                margin-bottom: 8px;
              }

              .alarm-meta {
                display: flex;
                gap: 12px;
                font-size: 11px;
                color: #64748b;
              }
            }

            .alarm-level {
              padding: 4px 8px;
              border-radius: 12px;
              font-size: 12px;
              height: fit-content;

              &.high { background: #ef4444; color: #ffffff; }
              &.medium { background: #f59e0b; color: #ffffff; }
              &.low { background: #22c55e; color: #ffffff; }
            }

            .alarm-actions {
              display: flex;
              flex-direction: column;
              gap: 4px;
            }
          }
        }
      }

      // 节能管理样式
      .energy-saving {
        height: 100%;
        display: flex;
        flex-direction: column;

        .saving-summary {
          display: flex;
          flex-direction: column;
          gap: 12px;
          margin-bottom: 20px;

          .summary-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            background: rgba(0, 212, 255, 0.05);
            border-radius: 8px;

            .summary-icon {
              width: 40px;
              height: 40px;
              background: rgba(0, 212, 255, 0.2);
              border-radius: 8px;
              display: flex;
              align-items: center;
              justify-content: center;

              .el-icon {
                color: #00d4ff;
                font-size: 20px;
              }
            }

            .summary-content {
              .summary-value {
                font-size: 18px;
                font-weight: bold;
                color: #00d4ff;
                margin-bottom: 2px;
              }

              .summary-label {
                font-size: 12px;
                color: #94a3b8;
              }
            }
          }
        }

        .saving-strategies {
          flex: 1;

          .strategies-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;

            h4 {
              margin: 0;
              color: #00d4ff;
            }
          }

          .strategies-list {
            display: flex;
            flex-direction: column;
            gap: 16px;

            .strategy-item {
              padding: 16px;
              background: rgba(0, 212, 255, 0.05);
              border-radius: 8px;

              .strategy-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 8px;

                .strategy-name {
                  font-size: 14px;
                  font-weight: 500;
                  color: #ffffff;
                }

                .strategy-status {
                  padding: 4px 8px;
                  border-radius: 12px;
                  font-size: 12px;

                  &.active { background: #22c55e; color: #ffffff; }
                  &.inactive { background: #64748b; color: #ffffff; }
                  &.pending { background: #f59e0b; color: #ffffff; }
                }
              }

              .strategy-desc {
                font-size: 12px;
                color: #94a3b8;
                margin-bottom: 12px;
              }

              .strategy-metrics {
                display: flex;
                gap: 16px;
                margin-bottom: 12px;

                .metric {
                  font-size: 12px;

                  .metric-label {
                    color: #94a3b8;
                  }

                  .metric-value {
                    color: #00d4ff;
                    font-weight: 500;
                  }
                }
              }

              .strategy-actions {
                display: flex;
                gap: 8px;
              }
            }
          }
        }
      }

      // 报表分析样式
      .report-analysis {
        height: 100%;
        display: flex;
        flex-direction: column;

        .report-controls {
          display: flex;
          gap: 12px;
          margin-bottom: 20px;
        }

        .report-content {
          flex: 1;
          overflow-y: auto;

          .report-section {
            margin-bottom: 24px;

            h4 {
              color: #00d4ff;
              margin-bottom: 12px;
            }

            .report-chart {
              height: 200px;
              background: rgba(0, 212, 255, 0.02);
              border-radius: 8px;
              padding: 16px;
            }

            .efficiency-comparison {
              display: flex;
              gap: 12px;

              .comparison-item {
                flex: 1;
                padding: 12px;
                background: rgba(0, 212, 255, 0.05);
                border-radius: 8px;
                text-align: center;

                .comparison-title {
                  font-size: 12px;
                  color: #94a3b8;
                  margin-bottom: 4px;
                }

                .comparison-value {
                  font-size: 16px;
                  font-weight: bold;
                  color: #00d4ff;

                  &.positive {
                    color: #22c55e;
                  }

                  &.negative {
                    color: #ef4444;
                  }
                }
              }
            }

            .cost-breakdown {
              .cost-item {
                display: flex;
                justify-content: space-between;
                margin-bottom: 8px;
                font-size: 14px;

                &.total {
                  border-top: 1px solid rgba(0, 212, 255, 0.2);
                  padding-top: 8px;
                  font-weight: bold;
                }

                .cost-label {
                  color: #94a3b8;
                }

                .cost-value {
                  color: #00d4ff;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
