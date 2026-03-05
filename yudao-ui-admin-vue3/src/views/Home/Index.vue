<template>
  <div class="ibms-screen">
    <!-- 顶部 Header -->
    <header class="ibms-screen__header glass-panel">
      <div class="header-left">
        <div class="logo-wrap">
          <img src="@/assets/imgs/logo.png" alt="Logo" class="logo-img" />
        </div>
        <div class="title-wrap">
          <h1 class="main-title">长辉科技IBMS管理平台</h1>
          <p class="sub-title">CHANGHUI TECH IBMS MANAGEMENT SYSTEM</p>
        </div>
      </div>

      <div class="header-right">
        <div class="weather-info">
          <Icon icon="ep:sunny" :size="20" color="#facc15" />
          <div class="weather-text">
            <div class="weather-temp"
              >{{ weatherData.temperature }}°C {{ weatherData.description }}</div
            >
            <div class="weather-extra">PM2.5: {{ screenData?.buildingEnvData?.pm25 || 35 }}</div>
          </div>
        </div>
        <div class="datetime-info">
          <div class="time-text">{{ currentTime }}</div>
          <div class="date-text">{{ currentDate }}</div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="ibms-screen__main">
      <!-- 左侧：智慧安防 -->
      <section class="left-panel">
        <div class="glass-panel panel-full clickable-module" @click="navigateToModule('security')">
          <div class="corner-accent corner-tl"></div>
          <div class="corner-accent corner-tr"></div>
          <div class="corner-accent corner-bl"></div>
          <div class="corner-accent corner-br"></div>
          <div class="module-title">
            <Icon icon="mdi:shield-outline" :size="18" />
            <span>智慧安防</span>
            <Icon icon="ep:arrow-right" :size="14" class="module-arrow" />
          </div>
          <div class="panel-content">
            <!-- 通道总览 -->
            <div class="data-card">
              <div class="card-header">
                <Icon icon="mdi:rhombus" :size="12" color="#00d4ff" />
                <span>通道总览</span>
              </div>
              <div class="channel-overview">
                <div class="channel-gauge">
                  <div ref="channelGaugeRef" class="chart-container"></div>
                  <div class="gauge-value"
                    >{{ screenData?.securityData?.channelOnlineRate || 0 }}%</div
                  >
                </div>
                <div class="channel-stats">
                  <div class="stat-item online">
                    <span class="stat-label"> <span class="dot green"></span>在线数量 </span>
                    <span class="stat-value green">{{
                      screenData?.securityData?.channelOnline || 0
                    }}</span>
                  </div>
                  <div class="stat-item offline">
                    <span class="stat-label"> <span class="dot red"></span>离线数量 </span>
                    <span class="stat-value red">{{
                      screenData?.securityData?.channelOffline || 0
                    }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 在线率统计 -->
            <div class="data-card">
              <div class="card-header">
                <Icon icon="mdi:rhombus" :size="12" color="#00d4ff" />
                <span>在线率统计</span>
                <div class="legend">
                  <span><span class="dot green"></span>在线</span>
                  <span><span class="dot red"></span>离线</span>
                </div>
              </div>
              <div class="rate-stat">
                <div class="rate-header">
                  <span>存储设备</span>
                  <span class="rate-value"
                    >{{ screenData?.securityData?.storageOnlineRate || 100 }}%</span
                  >
                </div>
                <div class="rate-bar">
                  <div
                    class="rate-fill"
                    :style="{ width: (screenData?.securityData?.storageOnlineRate || 100) + '%' }"
                  ></div>
                </div>
                <div class="rate-count">
                  <span class="green">{{ screenData?.securityData?.storageOnline || 0 }}</span>
                  <span class="red">{{ screenData?.securityData?.storageOffline || 0 }}</span>
                </div>
              </div>
              <div class="rate-stat">
                <div class="rate-header">
                  <span>服务器</span>
                  <span class="rate-value muted">{{
                    screenData?.securityData?.serverOnlineRate ?? '--'
                  }}</span>
                </div>
                <div class="rate-bar">
                  <div
                    class="rate-fill"
                    :style="{ width: (screenData?.securityData?.serverOnlineRate || 0) + '%' }"
                  ></div>
                </div>
                <div class="rate-count">
                  <span class="green">{{ screenData?.securityData?.serverOnline || 0 }}</span>
                  <span class="red">{{ screenData?.securityData?.serverOffline || 0 }}</span>
                </div>
              </div>
            </div>

            <!-- 入侵报警 -->
            <div class="data-card">
              <div class="card-header">
                <Icon icon="mdi:alert-circle-outline" :size="14" />
                <span>入侵报警</span>
                <span v-if="screenData?.securityData?.unhandledAlarms" class="badge danger">
                  {{ screenData.securityData.unhandledAlarms }} 条未处理
                </span>
              </div>
              <div ref="alarmChartRef" class="chart-small"></div>
              <div class="alarm-list">
                <div
                  v-for="(alarm, index) in screenData?.securityData?.recentAlarms || []"
                  :key="index"
                  class="alarm-item"
                  :class="alarm.level"
                >
                  <div class="alarm-title">{{ alarm.title }}</div>
                  <div class="alarm-meta">
                    <span>{{ alarm.location }}</span>
                    <span>{{ alarm.time }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 电子巡更 -->
            <div class="data-card">
              <div class="card-header">
                <Icon icon="mdi:map-marker-path" :size="14" />
                <span>电子巡更</span>
                <span class="rate-badge">{{ screenData?.securityData?.patrolRate || 0 }}%</span>
              </div>
              <div class="patrol-content">
                <div class="patrol-ring">
                  <svg class="ring-svg" viewBox="0 0 56 56">
                    <circle
                      cx="28"
                      cy="28"
                      r="24"
                      fill="none"
                      stroke="rgba(0,212,255,0.2)"
                      stroke-width="3"
                    />
                    <circle
                      cx="28"
                      cy="28"
                      r="24"
                      fill="none"
                      stroke="#00d4ff"
                      stroke-width="3"
                      :stroke-dasharray="150.8"
                      :stroke-dashoffset="
                        150.8 * (1 - (screenData?.securityData?.patrolRate || 0) / 100)
                      "
                      transform="rotate(-90 28 28)"
                    />
                  </svg>
                  <div class="ring-text">
                    {{ screenData?.securityData?.patrolCompleted || 0 }}/{{
                      screenData?.securityData?.patrolTotal || 0
                    }}
                  </div>
                </div>
                <div class="patrol-grid">
                  <div class="patrol-stat green"
                    >已完成 {{ screenData?.securityData?.patrolCompleted || 0 }}</div
                  >
                  <div class="patrol-stat yellow"
                    >异常 {{ screenData?.securityData?.patrolAbnormal || 0 }}</div
                  >
                  <div class="patrol-stat red"
                    >漏检 {{ screenData?.securityData?.patrolMissed || 0 }}</div
                  >
                  <div class="patrol-stat cyan">进行中</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 中间区域 -->
      <section class="center-panel">
        <!-- 顶部统计栏 -->
        <div class="stats-row">
          <div class="stats-card">
            <div class="hexagon-icon blue">
              <Icon icon="mdi:server" :size="20" />
            </div>
            <div class="stats-text">
              <div class="stats-label">校园设备接入</div>
              <div class="stats-number blue">{{ formatNumber(screenData?.deviceTotal || 0) }}</div>
            </div>
          </div>
          <div class="stats-card">
            <div class="hexagon-icon green">
              <Icon icon="mdi:leaf" :size="20" />
            </div>
            <div class="stats-text">
              <div class="stats-label">本月能耗(标煤)</div>
              <div class="stats-number green"
                >{{ screenData?.monthlyEnergy || 0 }}<span class="unit">t</span></div
              >
            </div>
          </div>
          <div class="stats-card">
            <div class="hexagon-icon red">
              <Icon icon="mdi:bell-alert" :size="20" />
            </div>
            <div class="stats-text">
              <div class="stats-label">实时异常设备</div>
              <div class="stats-number red">{{ screenData?.abnormalDevices || 0 }}</div>
            </div>
          </div>
          <div class="stats-card">
            <div class="hexagon-icon yellow">
              <Icon icon="mdi:alert-circle" :size="20" />
            </div>
            <div class="stats-text">
              <div class="stats-label">今日告警事件</div>
              <div class="stats-number yellow">{{ screenData?.todayAlerts || 0 }}</div>
            </div>
          </div>
        </div>

        <!-- 设备实时状态统计栏 -->
        <div class="glass-panel device-status-bar">
          <div class="status-title">设备实时状态统计</div>
          <div class="status-items">
            <div class="status-item">
              <div class="hexagon-icon small blue">
                <Icon icon="mdi:cube" :size="16" />
              </div>
              <div class="status-text">
                <div class="status-number blue">{{
                  screenData?.deviceStatusStats?.total || 0
                }}</div>
                <div class="status-label">总数(台)</div>
              </div>
            </div>
            <div class="status-item">
              <div class="hexagon-icon small green">
                <Icon icon="mdi:play-circle" :size="16" />
              </div>
              <div class="status-text">
                <div class="status-number green">{{
                  screenData?.deviceStatusStats?.online || 0
                }}</div>
                <div class="status-label">在线(台)</div>
              </div>
            </div>
            <div class="status-item">
              <div class="hexagon-icon small gray">
                <Icon icon="mdi:minus-circle" :size="16" />
              </div>
              <div class="status-text">
                <div class="status-number gray">{{
                  screenData?.deviceStatusStats?.offline || 0
                }}</div>
                <div class="status-label">离线(台)</div>
              </div>
            </div>
            <div class="status-item">
              <div class="hexagon-icon small yellow">
                <Icon icon="mdi:alert" :size="16" />
              </div>
              <div class="status-text">
                <div class="status-number yellow">{{
                  screenData?.deviceStatusStats?.alarm || 0
                }}</div>
                <div class="status-label">告警(台)</div>
              </div>
            </div>
            <div class="status-item">
              <div class="hexagon-icon small red">
                <Icon icon="mdi:close-circle" :size="16" />
              </div>
              <div class="status-text">
                <div class="status-number red">{{ screenData?.deviceStatusStats?.fault || 0 }}</div>
                <div class="status-label">故障(台)</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间视图区域 -->
        <div class="map-view glass-panel">
          <img src="@/assets/imgs/school.jpg" alt="校园全景鸟瞰" class="campus-panorama" />
          <div class="map-info-tag">
            <div class="info-label">当前视角</div>
            <div class="info-value">校园全景鸟瞰</div>
          </div>
          <div class="map-status-tags">
            <div class="status-tag green">
              <Icon icon="mdi:check-circle" :size="12" />
              <span>安防正常</span>
            </div>
            <div v-if="screenData?.todayAlerts" class="status-tag yellow">
              <Icon icon="mdi:alert" :size="12" />
              <span>{{ screenData.todayAlerts }}处告警</span>
            </div>
          </div>
        </div>

        <!-- 智慧楼宇 -->
        <div
          class="glass-panel building-panel clickable-module"
          @click="navigateToModule('building')"
        >
          <div class="module-title">
            <Icon icon="mdi:office-building" :size="18" />
            <span>智慧楼宇</span>
            <Icon icon="ep:arrow-right" :size="14" class="module-arrow" />
          </div>
          <div class="building-grid">
            <div class="building-card">
              <div class="building-label"><Icon icon="mdi:thermometer" :size="14" />环境温度</div>
              <div class="building-value orange"
                >{{ screenData?.buildingEnvData?.temperature || 24.5 }}°C</div
              >
              <div class="progress-bar">
                <div class="progress-fill orange" style="width: 65%"></div>
              </div>
            </div>
            <div class="building-card">
              <div class="building-label"><Icon icon="mdi:water" :size="14" />环境湿度</div>
              <div class="building-value blue"
                >{{ screenData?.buildingEnvData?.humidity || 58 }}%</div
              >
              <div class="progress-bar">
                <div
                  class="progress-fill blue"
                  :style="{ width: (screenData?.buildingEnvData?.humidity || 58) + '%' }"
                ></div>
              </div>
            </div>
            <div class="building-card">
              <div class="building-label"><Icon icon="mdi:weather-windy" :size="14" />空气质量</div>
              <div class="building-value green">{{
                screenData?.buildingEnvData?.airQuality || '优'
              }}</div>
              <div class="building-detail"
                >PM2.5: {{ screenData?.buildingEnvData?.pm25 || 35 }}μg/m³</div
              >
              <div class="building-detail"
                >CO₂: {{ screenData?.buildingEnvData?.co2 || 450 }}ppm</div
              >
            </div>
            <div class="building-card">
              <div class="building-label"><Icon icon="mdi:server" :size="14" />设备状态</div>
              <div class="building-dual">
                <div>
                  <div class="building-detail">在线率</div>
                  <div class="building-value small green"
                    >{{ screenData?.buildingEnvData?.deviceOnlineRate || 96.2 }}%</div
                  >
                </div>
                <div>
                  <div class="building-detail">负载率</div>
                  <div class="building-value small yellow"
                    >{{ screenData?.buildingEnvData?.deviceLoadRate || 68 }}%</div
                  >
                </div>
              </div>
              <div class="progress-bar mt-1">
                <div
                  class="progress-fill green"
                  :style="{ width: (screenData?.buildingEnvData?.deviceOnlineRate || 96.2) + '%' }"
                ></div>
              </div>
              <div class="progress-bar mt-1">
                <div
                  class="progress-fill yellow"
                  :style="{ width: (screenData?.buildingEnvData?.deviceLoadRate || 68) + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 右侧：智慧通行 + 智慧能源 -->
      <section class="right-panel">
        <!-- 智慧通行 -->
        <div class="glass-panel panel-half clickable-module" @click="navigateToModule('access')">
          <div class="corner-accent corner-tl"></div>
          <div class="corner-accent corner-tr"></div>
          <div class="corner-accent corner-bl"></div>
          <div class="corner-accent corner-br"></div>
          <div class="module-title">
            <Icon icon="mdi:card-account-details" :size="18" />
            <span>智慧通行</span>
            <Icon icon="ep:arrow-right" :size="14" class="module-arrow" />
          </div>
          <div class="panel-content">
            <!-- 门禁管理 -->
            <div class="data-card">
              <div class="card-header">
                <Icon icon="mdi:door-open" :size="14" />
                <span>门禁管理</span>
                <span class="badge success">{{
                  screenData?.accessData?.doorStatus || '正常'
                }}</span>
              </div>
              <div class="access-stats">
                <div class="access-stat">
                  <div class="access-value">{{
                    formatNumber(screenData?.accessData?.todayEntry || 0)
                  }}</div>
                  <div class="access-label">今日进入</div>
                </div>
                <div class="access-stat">
                  <div class="access-value">{{
                    formatNumber(screenData?.accessData?.todayExit || 0)
                  }}</div>
                  <div class="access-label">今日离开</div>
                </div>
              </div>
              <div ref="accessChartRef" class="chart-small"></div>
            </div>

            <!-- 访客预约 -->
            <div class="data-card">
              <div class="card-header">
                <Icon icon="mdi:account-clock" :size="14" />
                <span>访客预约</span>
              </div>
              <div class="visitor-stats">
                <div class="visitor-stat">
                  <div class="visitor-value blue">{{
                    screenData?.accessData?.visitorBooked || 0
                  }}</div>
                  <div class="visitor-label">预约</div>
                </div>
                <div class="visitor-stat">
                  <div class="visitor-value green">{{
                    screenData?.accessData?.visitorVisiting || 0
                  }}</div>
                  <div class="visitor-label">在访</div>
                </div>
                <div class="visitor-stat">
                  <div class="visitor-value gray">{{
                    screenData?.accessData?.visitorLeft || 0
                  }}</div>
                  <div class="visitor-label">已离</div>
                </div>
              </div>
            </div>

            <!-- 停车场 -->
            <div class="data-card">
              <div class="card-header">
                <Icon icon="mdi:car" :size="14" />
                <span>停车场</span>
              </div>
              <div class="parking-info">
                <div class="parking-header">
                  <span>车位占用</span>
                  <span class="parking-rate">{{ screenData?.accessData?.parkingRate || 0 }}%</span>
                </div>
                <div class="progress-bar">
                  <div
                    class="progress-fill cyan"
                    :style="{ width: (screenData?.accessData?.parkingRate || 0) + '%' }"
                  ></div>
                </div>
                <div class="parking-detail">
                  <span>已用: {{ screenData?.accessData?.parkingUsed || 0 }}</span>
                  <span>剩余: {{ screenData?.accessData?.parkingRemaining || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 智慧能源 -->
        <div class="glass-panel panel-half clickable-module" @click="navigateToModule('energy')">
          <div class="corner-accent corner-tl"></div>
          <div class="corner-accent corner-tr"></div>
          <div class="corner-accent corner-bl"></div>
          <div class="corner-accent corner-br"></div>
          <div class="module-title">
            <Icon icon="mdi:lightning-bolt" :size="18" />
            <span>智慧能源</span>
            <Icon icon="ep:arrow-right" :size="14" class="module-arrow" />
          </div>
          <div class="panel-content">
            <!-- 用电量 -->
            <div class="data-card energy-card yellow">
              <div class="energy-header">
                <span class="energy-title"
                  ><Icon icon="mdi:lightning-bolt" :size="14" />用电量</span
                >
                <span class="energy-period">今日</span>
              </div>
              <div class="energy-value"
                >{{ formatNumber(screenData?.energyData?.todayElectricity || 0)
                }}<span class="unit">kWh</span></div
              >
              <div ref="electricChartRef" class="chart-small"></div>
            </div>

            <!-- 用水量 -->
            <div class="data-card energy-card blue">
              <div class="energy-header">
                <span class="energy-title"><Icon icon="mdi:water" :size="14" />用水量</span>
                <span class="energy-period">今日</span>
              </div>
              <div class="energy-value"
                >{{ formatNumber(screenData?.energyData?.todayWater || 0)
                }}<span class="unit">m³</span></div
              >
              <div
                class="energy-change"
                :class="(screenData?.energyData?.waterChange || 0) < 0 ? 'down' : 'up'"
              >
                {{ (screenData?.energyData?.waterChange || 0) < 0 ? '↓' : '↑' }} 比昨日{{
                  Math.abs(screenData?.energyData?.waterChange || 0)
                }}%
              </div>
            </div>

            <!-- 费用统计 -->
            <div class="cost-grid">
              <div class="cost-item yellow">
                <div class="cost-label">电费</div>
                <div class="cost-value"
                  >¥{{ formatNumber(screenData?.energyData?.electricityCost || 0) }}</div
                >
              </div>
              <div class="cost-item blue">
                <div class="cost-label">水费</div>
                <div class="cost-value"
                  >¥{{ formatNumber(screenData?.energyData?.waterCost || 0) }}</div
                >
              </div>
              <div class="cost-item orange">
                <div class="cost-label">燃气</div>
                <div class="cost-value"
                  >¥{{ formatNumber(screenData?.energyData?.gasCost || 0) }}</div
                >
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部状态栏 -->
    <footer class="ibms-screen__footer glass-panel">
      <div class="footer-left">
        <span class="status-indicator">
          <span class="pulse-dot"></span>
          系统运行正常
        </span>
      </div>
      <div class="footer-center">长辉科技IBMS管理平台 V1.0</div>
      <div class="footer-right">
        <span>更新: {{ lastUpdateTime }}</span>
        <el-button text type="primary" size="small" @click="refreshData">
          <Icon icon="ep:refresh" :size="14" />
        </el-button>
        <el-button text type="primary" size="small" @click="toggleFullscreen">
          <Icon :icon="isFullscreen ? 'ep:close' : 'ep:full-screen'" :size="14" />
        </el-button>
      </div>
    </footer>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Icon } from '@/components/Icon'
import { getHomeScreenData, type HomeScreenVO } from '@/api/iot/dashboard'

dayjs.locale('zh-cn')

defineOptions({ name: 'Index' })

const router = useRouter()

// ==================== 数据状态 ====================

const screenData = ref<HomeScreenVO | null>(null)
const loading = ref(false)

// 天气数据
const weatherData = ref({
  temperature: 26,
  description: '晴'
})

// 时间
const currentDate = ref('')
const currentTime = ref('')
const lastUpdateTime = ref(dayjs().format('HH:mm:ss'))

// 全屏
const isFullscreen = ref(false)

// 图表refs
const channelGaugeRef = ref<HTMLElement>()
const alarmChartRef = ref<HTMLElement>()
const accessChartRef = ref<HTMLElement>()
const electricChartRef = ref<HTMLElement>()

// 图表实例
let channelGaugeChart: echarts.ECharts | null = null
let alarmChart: echarts.ECharts | null = null
let accessChart: echarts.ECharts | null = null
let electricChart: echarts.ECharts | null = null

// ==================== 计算属性 ====================

const formatNumber = (num: number) => {
  if (num >= 1000) {
    return num.toLocaleString()
  }
  return num
}

// ==================== 方法 ====================

const updateDateTime = () => {
  currentDate.value = dayjs().format('YYYY-MM-DD')
  currentTime.value = dayjs().format('HH:mm:ss')
}

const fetchData = async () => {
  try {
    loading.value = true
    const data = await getHomeScreenData()
    screenData.value = data
    lastUpdateTime.value = dayjs().format('HH:mm:ss')

    // 更新图表
    nextTick(() => {
      updateCharts()
    })
  } catch (error) {
    console.error('获取首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  fetchData()
}

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

// ==================== 模块点击跳转 ====================

// 模块路由配置
// 模块路由配置
const moduleRoutes = {
  security: '/security/video-surveillance/realtime-preview', // 智慧安防 - 实时预览
  access: '/smart-access/door/management', // 智慧通行 - 门禁管理
  energy: '/energy', // 智慧能源 - 数据总览
  building: '/building/bac/monitor' // 智慧楼宇 - 建筑设备监控
}

// 跳转到对应模块
const navigateToModule = (module: keyof typeof moduleRoutes) => {
  const route = moduleRoutes[module]
  
  // 智慧能源模块暂未开发
  if (!route) {
    ElMessage.info('该模块正在开发中，敬请期待！')
    return
  }
  
  // 跳转到对应路由（全屏模式会在路由守卫中自动关闭）
  router.push(route)
}

// ==================== 图表初始化 ====================

const initCharts = () => {
  // 通道仪表盘
  if (channelGaugeRef.value) {
    channelGaugeChart = echarts.init(channelGaugeRef.value)
  }

  // 报警趋势
  if (alarmChartRef.value) {
    alarmChart = echarts.init(alarmChartRef.value)
  }

  // 进出趋势
  if (accessChartRef.value) {
    accessChart = echarts.init(accessChartRef.value)
  }

  // 用电趋势
  if (electricChartRef.value) {
    electricChart = echarts.init(electricChartRef.value)
  }

  updateCharts()
}

const updateCharts = () => {
  const rate = screenData.value?.securityData?.channelOnlineRate || 98

  // 通道仪表盘
  if (channelGaugeChart) {
    channelGaugeChart.setOption({
      series: [
        {
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          radius: '100%',
          center: ['50%', '70%'],
          itemStyle: { color: '#52c41a', shadowColor: 'rgba(82,196,26,0.45)', shadowBlur: 10 },
          progress: { show: true, roundCap: true, width: 8 },
          pointer: { show: false },
          axisLine: {
            roundCap: true,
            lineStyle: { width: 8, color: [[1, 'rgba(255,255,255,0.1)']] }
          },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          title: { show: false },
          detail: { show: false },
          data: [{ value: rate }]
        }
      ]
    })
  }

  // 报警趋势柱状图
  if (alarmChart) {
    const trend = screenData.value?.securityData?.alarmTrend || [0, 0, 1, 0, 2, 1]
    alarmChart.setOption({
      grid: { top: 5, bottom: 5, left: 0, right: 0 },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
        show: false
      },
      yAxis: { type: 'value', show: false },
      series: [
        {
          data: trend,
          type: 'bar',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#ff4d4f' },
              { offset: 1, color: 'rgba(255,77,79,0.1)' }
            ])
          },
          barWidth: '50%'
        }
      ]
    })
  }

  // 进出趋势折线图
  if (accessChart) {
    accessChart.setOption({
      grid: { top: 5, bottom: 5, left: 0, right: 0 },
      xAxis: { type: 'category', data: ['8h', '10h', '12h', '14h', '16h'], show: false },
      yAxis: { type: 'value', show: false },
      series: [
        {
          name: '进入',
          type: 'line',
          smooth: true,
          data: [200, 450, 380, 420, 350],
          lineStyle: { color: '#22d3ee', width: 2 },
          showSymbol: false
        },
        {
          name: '离开',
          type: 'line',
          smooth: true,
          data: [150, 300, 480, 390, 380],
          lineStyle: { color: '#f472b6', width: 2 },
          showSymbol: false
        }
      ]
    })
  }

  // 用电趋势
  if (electricChart) {
    const trend = screenData.value?.energyData?.electricityTrend || [8200, 7800, 8500, 8100, 8650]
    electricChart.setOption({
      grid: { top: 5, bottom: 5, left: 0, right: 0 },
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月'], show: false },
      yAxis: { type: 'value', show: false },
      series: [
        {
          type: 'line',
          smooth: true,
          data: trend,
          lineStyle: { color: '#facc15', width: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(250,204,21,0.3)' },
              { offset: 1, color: 'rgba(250,204,21,0)' }
            ])
          },
          symbol: 'none'
        }
      ]
    })
  }
}

const handleResize = () => {
  channelGaugeChart?.resize()
  alarmChart?.resize()
  accessChart?.resize()
  electricChart?.resize()
}

// ==================== 生命周期 ====================

let refreshTimer: ReturnType<typeof setInterval>
let clockTimer: ReturnType<typeof setInterval>

onMounted(() => {
  updateDateTime()
  clockTimer = setInterval(updateDateTime, 1000)

  // 获取数据
  fetchData()

  // 自动刷新（30秒）
  refreshTimer = setInterval(refreshData, 30000)

  // 初始化图表
  nextTick(() => {
    initCharts()
  })

  document.addEventListener('fullscreenchange', handleFullscreenChange)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (clockTimer) clearInterval(clockTimer)

  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  window.removeEventListener('resize', handleResize)

  // 销毁图表
  channelGaugeChart?.dispose()
  alarmChart?.dispose()
  accessChart?.dispose()
  electricChart?.dispose()

  // 全屏模式和 Footer 状态会在路由守卫中自动处理
})
</script>

<style lang="scss" scoped>
// ==================== 变量 ====================
$primary-blue: #00d4ff;
$deep-blue: #0a1929;
$glass-bg: rgba(16, 30, 50, 0.9);
$alert-red: #ff4d4f;
$success-green: #52c41a;
$warning-gold: #faad14;

// ==================== 主布局 ====================
.ibms-screen {
  display: flex;
  flex-direction: column;
  height: calc(100% + var(--app-content-padding));
  margin: 0 calc(-1 * var(--app-content-padding)) calc(-1 * var(--app-content-padding))
    calc(-1 * var(--app-content-padding));
  padding-top: 0;
  background: linear-gradient(135deg, #030a1a 0%, #0a1628 50%, #051020 100%);
  overflow: hidden;
  position: relative;
}

// ==================== 公共样式 ====================
.glass-panel {
  background: rgba(16, 30, 50, 0.85);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(0, 212, 255, 0.25);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.6),
    inset 0 0 20px rgba(0, 212, 255, 0.05);
  border-radius: 12px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, $primary-blue, transparent);
    opacity: 0.8;
  }
}

.corner-accent {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid $primary-blue;
  z-index: 10;
}
.corner-tl {
  top: -1px;
  left: -1px;
  border-right: 0;
  border-bottom: 0;
}
.corner-tr {
  top: -1px;
  right: -1px;
  border-left: 0;
  border-bottom: 0;
}
.corner-bl {
  bottom: -1px;
  left: -1px;
  border-right: 0;
  border-top: 0;
}
.corner-br {
  bottom: -1px;
  right: -1px;
  border-left: 0;
  border-top: 0;
}

.module-title {
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.3) 0%, transparent 100%);
  border-left: 4px solid $primary-blue;
  padding: 10px 16px;
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: $primary-blue;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);

  .module-arrow {
    margin-left: auto;
    opacity: 0.6;
    transition: all 0.3s ease;
  }
}

// 可点击模块样式
.clickable-module {
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow:
      0 12px 40px rgba(0, 212, 255, 0.3),
      inset 0 0 30px rgba(0, 212, 255, 0.1);
    border-color: rgba(0, 212, 255, 0.5);

    .module-title {
      background: linear-gradient(90deg, rgba(0, 212, 255, 0.5) 0%, transparent 100%);
    }

    .module-arrow {
      opacity: 1;
      transform: translateX(4px);
    }
  }

  &:active {
    transform: translateY(0);
  }
}

.data-card {
  background: rgba(0, 20, 40, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.15);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;

  .legend {
    margin-left: auto;
    display: flex;
    gap: 12px;
    font-size: 10px;
    color: rgba(255, 255, 255, 0.6);
  }
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;

  &.green {
    background: $success-green;
  }
  &.red {
    background: $alert-red;
  }
  &.yellow {
    background: $warning-gold;
  }
}

.badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  margin-left: auto;

  &.danger {
    background: rgba(255, 77, 79, 0.2);
    color: $alert-red;
    animation: pulse 2s infinite;
  }

  &.success {
    background: rgba(82, 196, 26, 0.2);
    color: $success-green;
  }
}

.rate-badge {
  margin-left: auto;
  color: $primary-blue;
  font-size: 12px;
}

.chart-container {
  width: 100%;
  height: 100%;
}

.chart-small {
  height: 50px;
}

// ==================== Header ====================
.ibms-screen__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  margin: 12px 12px 0;
  padding: 0 24px;
  flex-shrink: 0;
  z-index: 10;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d4ff, #0066ff);
  display: flex;
  align-items: center;
  justify-content: center;

  .logo-img {
    width: 28px;
    height: 28px;
    object-fit: contain;
  }
}

.title-wrap {
  .main-title {
    font-size: 1.5rem;
    font-weight: 700;
    letter-spacing: 2px;
    background: linear-gradient(90deg, #00d4ff, #60a5fa);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin: 0;
  }

  .sub-title {
    font-size: 10px;
    color: rgba(0, 212, 255, 0.7);
    letter-spacing: 2px;
    margin: 0;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.weather-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: $primary-blue;

  .weather-text {
    text-align: right;
  }

  .weather-temp {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
  }

  .weather-extra {
    font-size: 10px;
    color: rgba(0, 212, 255, 0.7);
  }
}

.datetime-info {
  text-align: right;
  font-family: 'Orbitron', monospace;

  .time-text {
    font-size: 1.25rem;
    color: $primary-blue;
    font-weight: 600;
  }

  .date-text {
    font-size: 10px;
    color: rgba(0, 212, 255, 0.7);
  }
}

// ==================== Main ====================
.ibms-screen__main {
  flex: 1;
  display: grid;
  grid-template-columns: 280px 1fr 280px;
  gap: 12px;
  padding: 12px;
  overflow: hidden;
  z-index: 1;
  position: relative;
  min-height: 0;
}

// ==================== 左侧面板 ====================
.left-panel,
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
  min-height: 0;
}

.panel-full {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-half {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-content {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

// 通道总览
.channel-overview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.channel-gauge {
  width: 100px;
  height: 80px;
  position: relative;

  .gauge-value {
    position: absolute;
    top: 55%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 18px;
    font-weight: bold;
    font-family: 'Orbitron', monospace;
    color: $success-green;
  }
}

.channel-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  border-left: 3px solid;

  &.online {
    border-left-color: $success-green;
  }
  &.offline {
    border-left-color: $alert-red;
  }
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  font-family: 'Orbitron', monospace;

  &.green {
    color: $success-green;
  }
  &.red {
    color: $alert-red;
  }
}

// 在线率统计
.rate-stat {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  padding: 8px;
  margin-bottom: 8px;
}

.rate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
  color: $primary-blue;
}

.rate-value {
  font-weight: 600;

  &.muted {
    color: #6b7280;
  }
}

.rate-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 4px;
}

.rate-fill {
  height: 100%;
  border-radius: 2px;
  background: linear-gradient(90deg, $primary-blue, $success-green);
  transition: width 0.5s ease;
}

.rate-count {
  display: flex;
  gap: 8px;
  font-size: 10px;

  .green {
    color: $success-green;
  }
  .red {
    color: $alert-red;
  }
}

// 入侵报警
.alarm-list {
  margin-top: 8px;
}

.alarm-item {
  border-left: 3px solid transparent;
  background: rgba(0, 0, 0, 0.2);
  margin-bottom: 6px;
  border-radius: 0 4px 4px 0;
  padding: 8px;

  &.danger {
    border-left-color: $alert-red;
    background: rgba(255, 77, 79, 0.15);
  }

  &.warning {
    border-left-color: $warning-gold;
    background: rgba(250, 173, 20, 0.15);
  }
}

.alarm-title {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.alarm-meta {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #6b7280;
}

// 电子巡更
.patrol-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.patrol-ring {
  position: relative;
  width: 56px;
  height: 56px;

  .ring-svg {
    width: 100%;
    height: 100%;
  }

  .ring-text {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: bold;
    font-family: 'Orbitron', monospace;
    color: #fff;
  }
}

.patrol-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 4px;
}

.patrol-stat {
  padding: 4px 8px;
  border-radius: 4px;
  text-align: center;
  font-size: 10px;

  &.green {
    background: rgba(82, 196, 26, 0.1);
    color: $success-green;
  }
  &.yellow {
    background: rgba(250, 173, 20, 0.1);
    color: $warning-gold;
  }
  &.red {
    background: rgba(255, 77, 79, 0.1);
    color: $alert-red;
  }
  &.cyan {
    background: rgba(0, 212, 255, 0.1);
    color: $primary-blue;
  }
}

// ==================== 中间面板 ====================
.center-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
  min-height: 0;
}

// 顶部统计栏
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  flex-shrink: 0;
}

.stats-card {
  background: linear-gradient(135deg, rgba(0, 50, 100, 0.7), rgba(0, 20, 40, 0.85));
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 10px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  backdrop-filter: blur(10px);
}

.hexagon-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(0, 100, 200, 0.4));
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary-blue;

  &.small {
    width: 36px;
    height: 36px;
  }

  &.blue {
    color: $primary-blue;
  }
  &.green {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.3), rgba(21, 128, 61, 0.5));
    color: #86efac;
  }
  &.red {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(185, 28, 28, 0.5));
    color: #fca5a5;
  }
  &.yellow {
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.3), rgba(180, 83, 9, 0.5));
    color: #fcd34d;
  }
  &.gray {
    background: linear-gradient(135deg, rgba(156, 163, 175, 0.2), rgba(75, 85, 99, 0.4));
    color: #d1d5db;
  }
}

.stats-text {
  .stats-label {
    font-size: 11px;
    color: #94a3b8;
    margin-bottom: 4px;
  }

  .stats-number {
    font-size: 1.5rem;
    font-weight: 700;
    font-family: 'Orbitron', monospace;

    &.blue {
      background: linear-gradient(180deg, #fff, #7ecfff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    &.green {
      background: linear-gradient(180deg, #86efac, #22c55e);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    &.red {
      background: linear-gradient(180deg, #fca5a5, #ef4444);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    &.yellow {
      background: linear-gradient(180deg, #fcd34d, #f59e0b);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    .unit {
      font-size: 12px;
      color: #6b7280;
    }
  }
}

// 设备状态栏
.device-status-bar {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.status-title {
  font-size: 14px;
  font-weight: 600;
  color: $primary-blue;
  border-right: 1px solid rgba(0, 212, 255, 0.3);
  padding-right: 16px;
  white-space: nowrap;
}

.status-items {
  flex: 1;
  display: flex;
  justify-content: space-around;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-text {
  .status-number {
    font-size: 1rem;
    font-weight: 700;
    font-family: 'Orbitron', monospace;

    &.blue {
      background: linear-gradient(180deg, #fff, #7ecfff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    &.green {
      background: linear-gradient(180deg, #86efac, #22c55e);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    &.gray {
      background: linear-gradient(180deg, #d1d5db, #9ca3af);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    &.yellow {
      background: linear-gradient(180deg, #fcd34d, #f59e0b);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    &.red {
      background: linear-gradient(180deg, #fca5a5, #ef4444);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }

  .status-label {
    font-size: 10px;
    color: #6b7280;
  }
}

// 地图视图
.map-view {
  flex: 1;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 1px solid rgba(0, 212, 255, 0.2);
  background: linear-gradient(180deg, transparent, rgba(0, 150, 180, 0.1));
  overflow: hidden;
}

.campus-panorama {
  width: 100%;
  height: 100%;
  object-fit: contain;
  position: absolute;
  top: 0;
  left: 0;
}

.map-placeholder {
  text-align: center;
  opacity: 0.5;

  .map-title {
    font-size: 1.125rem;
    color: $primary-blue;
    margin-top: 16px;
  }

  .map-subtitle {
    font-size: 12px;
    color: rgba(0, 212, 255, 0.5);
    margin-top: 4px;
  }
}

.map-info-tag {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 8px 16px;
  border-radius: 8px;
  border-left: 4px solid $primary-blue;
  background: rgba(16, 30, 50, 0.9);

  .info-label {
    font-size: 10px;
    color: #6b7280;
  }

  .info-value {
    font-size: 14px;
    font-weight: 600;
    color: $primary-blue;
  }
}

.map-status-tags {
  position: absolute;
  bottom: 16px;
  left: 16px;
  display: flex;
  gap: 8px;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(16, 30, 50, 0.9);

  &.green {
    border: 1px solid rgba(82, 196, 26, 0.3);
    color: $success-green;
  }

  &.yellow {
    border: 1px solid rgba(250, 173, 20, 0.3);
    color: $warning-gold;
  }
}

// 智慧楼宇
.building-panel {
  flex-shrink: 0;
}

.building-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 12px;
}

.building-card {
  background: rgba(0, 20, 40, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.15);
  border-radius: 8px;
  padding: 12px;
}

.building-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.building-value {
  font-size: 1.5rem;
  font-weight: 700;
  font-family: 'Orbitron', monospace;

  &.orange {
    color: #fb923c;
  }
  &.blue {
    color: #60a5fa;
  }
  &.green {
    color: $success-green;
  }
  &.yellow {
    color: $warning-gold;
  }

  &.small {
    font-size: 1.125rem;
  }
}

.building-detail {
  font-size: 10px;
  color: #6b7280;
  margin-top: 4px;
}

.building-dual {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.progress-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
  margin-top: 8px;

  &.mt-1 {
    margin-top: 4px;
  }
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;

  &.orange {
    background: #fb923c;
  }
  &.blue {
    background: #60a5fa;
  }
  &.green {
    background: $success-green;
  }
  &.yellow {
    background: $warning-gold;
  }
  &.cyan {
    background: linear-gradient(90deg, $primary-blue, #3b82f6);
  }
}

// ==================== 右侧面板 ====================

// 门禁管理
.access-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.access-stat {
  text-align: center;
  padding: 8px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.access-value {
  font-size: 1.125rem;
  font-weight: 700;
  font-family: 'Orbitron', monospace;
  color: $primary-blue;
}

.access-label {
  font-size: 10px;
  color: #6b7280;
}

// 访客预约
.visitor-stats {
  display: flex;
  justify-content: space-around;
}

.visitor-stat {
  text-align: center;
}

.visitor-value {
  font-size: 1.125rem;
  font-weight: 700;
  font-family: 'Orbitron', monospace;

  &.blue {
    color: #60a5fa;
  }
  &.green {
    color: $success-green;
  }
  &.gray {
    color: #9ca3af;
  }
}

.visitor-label {
  font-size: 10px;
  color: #6b7280;
}

// 停车场
.parking-info {
  .parking-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #6b7280;
    margin-bottom: 4px;
  }

  .parking-rate {
    font-weight: 600;
    color: $primary-blue;
  }

  .parking-detail {
    display: flex;
    justify-content: space-between;
    font-size: 10px;
    color: #6b7280;
    margin-top: 4px;
  }
}

// 能源卡片
.energy-card {
  border-left: 4px solid;

  &.yellow {
    border-left-color: $warning-gold;
  }
  &.blue {
    border-left-color: #60a5fa;
  }
}

.energy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.energy-title {
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;

  .yellow & {
    color: $warning-gold;
  }
  .blue & {
    color: #60a5fa;
  }
}

.energy-period {
  font-size: 10px;
  color: #6b7280;
}

.energy-value {
  font-size: 1.25rem;
  font-weight: 700;
  font-family: 'Orbitron', monospace;
  margin-bottom: 4px;

  .yellow & {
    color: #fcd34d;
  }
  .blue & {
    color: #60a5fa;
  }

  .unit {
    font-size: 12px;
    color: #6b7280;
  }
}

.energy-change {
  font-size: 10px;
  margin-top: 4px;

  &.down {
    color: $success-green;
  }
  &.up {
    color: $alert-red;
  }
}

// 费用统计
.cost-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: auto;
}

.cost-item {
  background: rgba(0, 0, 0, 0.4);
  border-radius: 4px;
  padding: 8px;
  text-align: center;
  border: 1px solid;

  &.yellow {
    border-color: rgba(250, 173, 20, 0.3);
  }
  &.blue {
    border-color: rgba(96, 165, 250, 0.3);
  }
  &.orange {
    border-color: rgba(251, 146, 60, 0.3);
  }
}

.cost-label {
  font-size: 10px;
  color: #6b7280;
}

.cost-value {
  font-size: 12px;
  font-weight: 600;

  .yellow & {
    color: $warning-gold;
  }
  .blue & {
    color: #60a5fa;
  }
  .orange & {
    color: #fb923c;
  }
}

// ==================== Footer ====================
.ibms-screen__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  margin: 0 12px 12px;
  padding: 0 16px;
  flex-shrink: 0;
  z-index: 10;
  position: relative;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: $success-green;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $success-green;
  animation: pulse 2s infinite;
}

.footer-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: rgba(0, 212, 255, 0.6);
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

// ==================== 动画 ====================
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  50% {
    opacity: 0.7;
    box-shadow: 0 0 0 8px rgba(82, 196, 26, 0);
  }
}
</style>
