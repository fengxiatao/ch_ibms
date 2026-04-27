<template>
  <div class="ibms-screen">
    <!-- 主体内容 -->
    <main class="ibms-screen__main">
      <!-- 左侧：智慧安防 -->
      <section class="left-panel">
        <div
          :class="[
            'glass-panel',
            'panel-full',
            canModuleNav('security') ? 'clickable-module' : 'clickable-module--disabled'
          ]"
          @click="onModuleClick('security')"
        >
          <div class="corner-accent corner-tl"></div>
          <div class="corner-accent corner-tr"></div>
          <div class="corner-accent corner-bl"></div>
          <div class="corner-accent corner-br"></div>
          <div class="module-title">
            <Icon icon="mdi:shield-outline" :size="18" />
            <span>智慧安防</span>
            <Icon v-if="canModuleNav('security')" icon="ep:arrow-right" :size="14" class="module-arrow" />
          </div>
          <div class="panel-content">
            <div class="data-card security-overview">
              <div class="security-rates">
                <div class="rate-ring">
                  <svg class="rate-ring__svg" viewBox="0 0 56 56">
                    <circle
                      cx="28"
                      cy="28"
                      r="24"
                      fill="none"
                      stroke="rgba(255,255,255,0.10)"
                      stroke-width="4"
                    />
                    <circle
                      cx="28"
                      cy="28"
                      r="24"
                      fill="none"
                      stroke="#00d4ff"
                      stroke-width="4"
                      :stroke-dasharray="150.8"
                      :stroke-dashoffset="150.8 * (1 - getSecurityDeviceOnlineRate() / 100)"
                      transform="rotate(-90 28 28)"
                      stroke-linecap="round"
                    />
                  </svg>
                  <div class="rate-ring__value">{{ getSecurityDeviceOnlineRate() }}%</div>
                  <div class="rate-ring__label">设备在线率</div>
                </div>
                <div class="rate-ring">
                  <svg class="rate-ring__svg" viewBox="0 0 56 56">
                    <circle
                      cx="28"
                      cy="28"
                      r="24"
                      fill="none"
                      stroke="rgba(255,255,255,0.10)"
                      stroke-width="4"
                    />
                    <circle
                      cx="28"
                      cy="28"
                      r="24"
                      fill="none"
                      stroke="#52c41a"
                      stroke-width="4"
                      :stroke-dasharray="150.8"
                      :stroke-dashoffset="150.8 * (1 - getSecurityChannelOnlineRate() / 100)"
                      transform="rotate(-90 28 28)"
                      stroke-linecap="round"
                    />
                  </svg>
                  <div class="rate-ring__value green">{{ getSecurityChannelOnlineRate() }}%</div>
                  <div class="rate-ring__label">通道在线率</div>
                </div>
              </div>

              <div class="security-grid">
                <div class="security-item">
                  <div class="security-item__head">
                    <div class="security-item__title">
                      <Icon icon="mdi:cctv" :size="14" />
                      <span>IPC摄像头</span>
                    </div>
                    <div class="security-item__total">{{ getIpcTotal() }}</div>
                  </div>
                  <div class="security-item__foot">
                    <div class="security-item__status green">{{ getIpcOnline() }} 在线</div>
                    <div class="security-item__status red">{{ getIpcOffline() }} 离线</div>
                  </div>
                </div>
                <div class="security-item">
                  <div class="security-item__head">
                    <div class="security-item__title">
                      <Icon icon="mdi:server" :size="14" />
                      <span>视频服务器</span>
                    </div>
                    <div class="security-item__total">{{ getVideoServerTotal() }}</div>
                  </div>
                  <div class="security-item__foot">
                    <div class="security-item__status green">{{ getVideoServerOnline() }} 在线</div>
                    <div class="security-item__status red">{{ getVideoServerOffline() }} 离线</div>
                  </div>
                </div>
                <div class="security-item">
                  <div class="security-item__head">
                    <div class="security-item__title">
                      <Icon icon="mdi:database" :size="14" />
                      <span>存储设备</span>
                    </div>
                    <div class="security-item__total">{{ getStorageTotal() }}</div>
                  </div>
                  <div class="security-item__foot">
                    <div class="security-item__status green">{{ getStorageOnline() }} 在线</div>
                    <div class="security-item__status red">{{ getStorageOffline() }} 离线</div>
                  </div>
                </div>
                <div class="security-item">
                  <div class="security-item__head">
                    <div class="security-item__title">
                      <Icon icon="mdi:video-outline" :size="14" />
                      <span>视频通道</span>
                    </div>
                    <div class="security-item__total">{{ getVideoChannelTotal() }}</div>
                  </div>
                  <div class="security-item__foot">
                    <div class="security-item__status green">{{ getVideoChannelOnline() }} 正常</div>
                    <div class="security-item__status red">{{ getVideoChannelOffline() }} 异常</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="data-card alarm-type">
              <div
                :class="['card-header', canSubNav('alarmType') ? 'card-header--link' : '']"
                :role="canSubNav('alarmType') ? 'button' : undefined"
                :tabindex="canSubNav('alarmType') ? 0 : undefined"
                @click.stop="onSubClick('alarmType')"
                @keydown.enter.stop.prevent="onSubClick('alarmType')"
                @keydown.space.stop.prevent="onSubClick('alarmType')"
              >
                <Icon icon="mdi:alarm-light-outline" :size="14" />
                <span>报警类型分布（今日）</span>
              </div>
              <div ref="alarmTypeChartRef" class="chart-alarm-type"></div>
            </div>

            <div class="data-card access-kpi">
              <div
                :class="['card-header', canSubNav('ePatrol') ? 'card-header--link' : '']"
                :role="canSubNav('ePatrol') ? 'button' : undefined"
                :tabindex="canSubNav('ePatrol') ? 0 : undefined"
                @click.stop="onSubClick('ePatrol')"
                @keydown.enter.stop.prevent="onSubClick('ePatrol')"
                @keydown.space.stop.prevent="onSubClick('ePatrol')"
              >
                <Icon icon="mdi:walk" :size="14" />
                <span>电子巡更</span>
                <span class="badge" :class="getPatrolStatus().type">{{ getPatrolStatus().label }}</span>
              </div>
              <div class="access-kpi__grid">
                <div class="access-kpi__item">
                  <div class="access-kpi__label">今日巡更点位</div>
                  <div class="access-kpi__value">{{ formatNumber(patrolKpi.totalPoints) }}</div>
                </div>
                <div class="access-kpi__item">
                  <div class="access-kpi__label">打卡完成率</div>
                  <div class="access-kpi__value">{{ patrolKpi.completionRate }}%</div>
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

        <!-- 中间视图区域：园区三维孪生（Three.js，资源见 public/park-twin） -->
        <div class="map-view glass-panel map-view--twin">
          <ParkDigitalTwin class="map-park-twin" />
          <div class="map-info-tag">
            <div class="info-label">当前视角</div>
            <div class="info-value">园区数字孪生</div>
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
          :class="[
            'glass-panel',
            'building-panel',
            canModuleNav('building') ? 'clickable-module' : 'clickable-module--disabled'
          ]"
          @click="onModuleClick('building')"
        >
          <div class="module-title">
            <Icon icon="mdi:office-building" :size="18" />
            <span>智慧楼宇</span>
            <Icon v-if="canModuleNav('building')" icon="ep:arrow-right" :size="14" class="module-arrow" />
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
        <div
          :class="[
            'glass-panel',
            'panel-half',
            canModuleNav('access') ? 'clickable-module' : 'clickable-module--disabled'
          ]"
          @click="onModuleClick('access')"
        >
          <div class="corner-accent corner-tl"></div>
          <div class="corner-accent corner-tr"></div>
          <div class="corner-accent corner-bl"></div>
          <div class="corner-accent corner-br"></div>
          <div class="module-title">
            <Icon icon="mdi:card-account-details" :size="18" />
            <span>智慧通行</span>
            <Icon v-if="canModuleNav('access')" icon="ep:arrow-right" :size="14" class="module-arrow" />
          </div>
          <div class="panel-content">
            <!-- 门禁管理 -->
            <div class="data-card">
              <div
                :class="['card-header', canSubNav('door') ? 'card-header--link' : '']"
                :role="canSubNav('door') ? 'button' : undefined"
                :tabindex="canSubNav('door') ? 0 : undefined"
                @click.stop="onSubClick('door')"
                @keydown.enter.stop.prevent="onSubClick('door')"
                @keydown.space.stop.prevent="onSubClick('door')"
              >
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
              <div
                :class="['card-header', canSubNav('visitor') ? 'card-header--link' : '']"
                :role="canSubNav('visitor') ? 'button' : undefined"
                :tabindex="canSubNav('visitor') ? 0 : undefined"
                @click.stop="onSubClick('visitor')"
                @keydown.enter.stop.prevent="onSubClick('visitor')"
                @keydown.space.stop.prevent="onSubClick('visitor')"
              >
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
        <div
          :class="[
            'glass-panel',
            'panel-half',
            canModuleNav('energy') ? 'clickable-module' : 'clickable-module--disabled'
          ]"
          @click="onModuleClick('energy')"
        >
          <div class="corner-accent corner-tl"></div>
          <div class="corner-accent corner-tr"></div>
          <div class="corner-accent corner-bl"></div>
          <div class="corner-accent corner-br"></div>
          <div
            class="module-title module-title--link"
            role="button"
            tabindex="0"
            @click.stop="onSubClick('energy')"
            @keydown.enter.stop.prevent="onSubClick('energy')"
            @keydown.space.stop.prevent="onSubClick('energy')"
          >
            <Icon icon="mdi:lightning-bolt" :size="18" />
            <span>智慧能源</span>
            <Icon
              v-if="canSubNav('energy') || canModuleNav('energy')"
              icon="ep:arrow-right"
              :size="14"
              class="module-arrow"
            />
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
import echarts from '@/plugins/echarts'
import { ElMessage } from 'element-plus'
import { Icon } from '@/components/Icon'
import ParkDigitalTwin from '@/components/parkDigitalTwin/ParkDigitalTwin.vue'
import { getHomeScreenData, type HomeScreenVO } from '@/api/iot/dashboard'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { findPathByPermission, findPathByMenuName, hasMenuByName } from '@/utils/menuLookup'
import { isRouteRegistered } from '@/utils/menuResolver'
import { checkPermi } from '@/utils/permission'
import { MODULE_ENTRIES, SUB_ENTRIES, type HomeEntry } from './home-entries'

dayjs.locale('zh-cn')

defineOptions({ name: 'Index' })

const router = useRouter()
const permissionStore = usePermissionStoreWithOut()

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
const alarmTypeChartRef = ref<HTMLElement>()
const accessChartRef = ref<HTMLElement>()
const electricChartRef = ref<HTMLElement>()

// 图表实例
let alarmTypeChart: echarts.ECharts | null = null
let accessChart: echarts.ECharts | null = null
let electricChart: echarts.ECharts | null = null

// ==================== 计算属性 ====================

const formatNumber = (num: number) => {
  if (num >= 1000) {
    return num.toLocaleString()
  }
  return num
}

const clampPercent = (value: number) => {
  const num = Number.isFinite(value) ? value : 0
  return Math.max(0, Math.min(100, Math.round(num)))
}

type PatrolKpi = {
  totalTasks: number
  completedTasks: number
  pendingTasks: number
  totalPoints: number
  checkedPoints: number
  missedPoints: number
  completionRate: number
}

const patrolKpi = ref<PatrolKpi>({
  totalTasks: 0,
  completedTasks: 0,
  pendingTasks: 0,
  totalPoints: 0,
  checkedPoints: 0,
  missedPoints: 0,
  completionRate: 0
})

const normalizeToDayStart = (d: Date) => {
  const nd = new Date(d)
  nd.setHours(0, 0, 0, 0)
  return nd
}

const generatePatrolKpiByRange = (start: Date, end: Date): PatrolKpi => {
  const days = Math.ceil((end.getTime() - start.getTime()) / (1000 * 3600 * 24)) + 1
  let factor = days / 3.5
  if (factor < 0.6) factor = 0.6
  if (factor > 3.2) factor = 3.2

  const baseTasks = 10
  const baseCompleted = 3
  const baseTotalPoints = 56
  const baseChecked = 32

  const totalTasks = Math.round(baseTasks * factor * 0.7)
  const completedTasks = Math.round(baseCompleted * Math.sqrt(factor) * 0.9)
  const pendingTasks = totalTasks - completedTasks

  const totalPoints = Math.round(baseTotalPoints * factor * 0.8)
  const checkedPoints = Math.round(baseChecked * Math.pow(factor, 0.7) * 0.9)
  const missedPoints = totalPoints - checkedPoints

  const completionRate = Number(((checkedPoints / Math.max(1, totalPoints)) * 100).toFixed(1))

  return {
    totalTasks,
    completedTasks,
    pendingTasks,
    totalPoints,
    checkedPoints,
    missedPoints,
    completionRate
  }
}

const refreshPatrolKpi = () => {
  const end = normalizeToDayStart(new Date())
  const start = new Date(end)
  patrolKpi.value = generatePatrolKpiByRange(start, end)
}

const getPatrolStatus = () => {
  const rate = patrolKpi.value.completionRate
  if (rate >= 70) return { label: '正常', type: 'success' as const }
  return { label: '异常', type: 'danger' as const }
}

const getSecurityDeviceOnlineRate = () => {
  const rate = screenData.value?.deviceStatusStats?.onlineRate
  if (typeof rate === 'number') return clampPercent(rate)
  const total = screenData.value?.deviceStatusStats?.total || 0
  const online = screenData.value?.deviceStatusStats?.online || 0
  if (!total) return 0
  return clampPercent((online / total) * 100)
}

const getSecurityChannelOnlineRate = () => {
  return clampPercent(screenData.value?.securityData?.channelOnlineRate || 0)
}

const getIpcOnline = () => {
  return screenData.value?.securityData?.ipcOnline || 0
}

const getIpcOffline = () => {
  return screenData.value?.securityData?.ipcOffline || 0
}

const getIpcTotal = () => {
  const total = screenData.value?.securityData?.ipcTotal
  if (typeof total === 'number') return total
  return getIpcOnline() + getIpcOffline()
}

const getVideoServerOnline = () => {
  return screenData.value?.securityData?.serverOnline || 0
}

const getVideoServerOffline = () => {
  return screenData.value?.securityData?.serverOffline || 0
}

const getVideoServerTotal = () => {
  return getVideoServerOnline() + getVideoServerOffline()
}

const getStorageOnline = () => {
  return screenData.value?.securityData?.storageOnline || 0
}

const getStorageOffline = () => {
  return screenData.value?.securityData?.storageOffline || 0
}

const getStorageTotal = () => {
  return getStorageOnline() + getStorageOffline()
}

const getVideoChannelOnline = () => {
  return screenData.value?.securityData?.channelOnline || 0
}

const getVideoChannelOffline = () => {
  return screenData.value?.securityData?.channelOffline || 0
}

const getVideoChannelTotal = () => {
  return getVideoChannelOnline() + getVideoChannelOffline()
}

const getAlarmTypeDistribution = () => {
  const dist = screenData.value?.securityData?.alarmTypeDistribution
  if (Array.isArray(dist) && dist.length) {
    return dist
  }
  const total = screenData.value?.todayAlerts || screenData.value?.securityData?.unhandledAlarms || 0
  const a = Math.max(0, Math.round(total * 0.35))
  const b = Math.max(0, Math.round(total * 0.25))
  const c = Math.max(0, Math.round(total * 0.2))
  const d = Math.max(0, total - a - b - c)
  return [
    { name: '紧急报警', value: a },
    { name: '设备报警', value: b },
    { name: '防拆报警', value: c },
    { name: '位移报警', value: d }
  ]
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
    refreshPatrolKpi()

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

// ==================== 模块点击跳转（能力驱动：permission / 菜单名 / 兜底路径） ====================

type ModuleKey = 'security' | 'access' | 'energy' | 'building'
type SubModuleKey = 'alarmType' | 'ePatrol' | 'door' | 'visitor' | 'energy'

const canNavigateHomeEntry = (entry: HomeEntry) => {
  if (entry.permission) {
    return checkPermi([entry.permission])
  }
  if (entry.menuName) {
    return hasMenuByName(entry.menuName, permissionStore.getRouters)
  }
  if (entry.routeName) {
    try {
      const resolved = router.resolve({ name: entry.routeName as any })
      if (resolved.matched.length) return true
    } catch {
      /* ignore */
    }
  }
  if (entry.fallbackPaths?.length) {
    // 兜底路径必须真实存在于当前用户路由树内，才允许点击
    return entry.fallbackPaths.some((p) => isRouteRegistered(p))
  }
  return false
}

const canModuleNav = (k: ModuleKey) => canNavigateHomeEntry(MODULE_ENTRIES[k])
const canSubNav = (k: SubModuleKey) => canNavigateHomeEntry(SUB_ENTRIES[k])

const resolveAndPush = async (candidates: {
  name?: string
  paths: string[]
  reloadIfNotMatched?: boolean
}) => {
  const isFallback404Record = (record: any) => {
    if (!record) return true
    const recordName = String(record.name || '')
    if (
      record.path === '/:pathMatch(.*)*' ||
      record.path === '/:path(.*)*' ||
      recordName === '404Page' ||
      recordName === 'NoFound'
    ) {
      return true
    }
    const routeComponent = record.components?.default || record.component
    const componentText = typeof routeComponent === 'function' ? routeComponent.toString() : ''
    return componentText.includes('Error/404.vue')
  }

  const hasRealMatch = (path: string) => {
    const resolved = router.resolve(path)
    if (!resolved.matched.length) return false
    const lastMatched = resolved.matched[resolved.matched.length - 1]
    if (isFallback404Record(lastMatched)) return false
    // 同时校验 meta.permission：即使路由已注册，若当前用户不具备权限也视为不可达
    const meta = (resolved.meta ?? {}) as Record<string, any>
    const needPerm = meta.permission as string | string[] | undefined
    if (needPerm && String(meta.noPermCheck) !== 'true') {
      const perms = Array.isArray(needPerm) ? needPerm : [needPerm]
      if (!perms.some((p) => checkPermi([p]))) return false
    }
    return true
  }

  if (candidates.name) {
    try {
      await router.push({ name: candidates.name as any })
      return
    } catch {
    }
  }

  for (const path of candidates.paths) {
    if (hasRealMatch(path)) {
      await router.push(path)
      return
    }

    if (candidates.reloadIfNotMatched) {
      const resolvedByPath = router.resolve(path)
      window.location.href = resolvedByPath.href
      return
    }

    try {
      const before = router.currentRoute.value.fullPath
      await router.push(path)
      if (router.currentRoute.value.fullPath !== before) {
        return
      }
    } catch {
    }
  }

  ElMessage.warning('暂未授权或路由未开放，请联系管理员')
}

const navigateByHomeEntry = async (entry: HomeEntry) => {
  if (!entry) {
    return
  }
  const routes = permissionStore.getRouters
  if (entry.permission) {
    if (!checkPermi([entry.permission])) {
      ElMessage.warning('暂无访问权限，请联系管理员')
      return
    }
    const p = findPathByPermission(entry.permission, routes)
    if (p) {
      await resolveAndPush({ paths: [p] })
      return
    }
  }
  if (entry.menuName) {
    const p = findPathByMenuName(entry.menuName, routes)
    if (p) {
      await resolveAndPush({ paths: [p] })
      return
    }
  }
  if (entry.routeName) {
    try {
      await router.push({ name: entry.routeName as any })
      return
    } catch {
      // 继续走静态兜底路径
    }
  }
  if (entry.fallbackPaths?.length) {
    await resolveAndPush({
      paths: entry.fallbackPaths,
      reloadIfNotMatched: entry.reloadIfNotMatched
    })
    return
  }
  ElMessage.warning('暂未授权，请联系管理员')
}

const navigateToSubModule = async (key: SubModuleKey) => {
  await navigateByHomeEntry(SUB_ENTRIES[key])
}

const navigateToModule = async (module: ModuleKey) => {
  await navigateByHomeEntry(MODULE_ENTRIES[module])
}

const onModuleClick = (k: ModuleKey) => {
  if (!canModuleNav(k)) {
    return
  }
  void navigateToModule(k)
}

const onSubClick = (k: SubModuleKey) => {
  if (!canSubNav(k)) {
    return
  }
  void navigateToSubModule(k)
}

// ==================== 图表初始化 ====================

const initCharts = () => {
  if (alarmTypeChartRef.value) {
    alarmTypeChart = echarts.init(alarmTypeChartRef.value)
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
  if (alarmTypeChart) {
    const dist = getAlarmTypeDistribution()
    alarmTypeChart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: ['48%', '72%'],
          center: ['50%', '56%'],
          avoidLabelOverlap: true,
          label: { color: 'rgba(255,255,255,0.75)', fontSize: 10, formatter: '{b}' },
          labelLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' }, length: 10, length2: 8 },
          itemStyle: { borderColor: 'rgba(0,0,0,0.2)', borderWidth: 2 },
          emphasis: { scale: true, scaleSize: 6 },
          data: dist
        }
      ],
      color: ['#ff4d4f', '#faad14', '#00d4ff', '#52c41a']
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
  alarmTypeChart?.resize()
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
  alarmTypeChart?.dispose()
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

.module-title--link {
  cursor: pointer;
  user-select: none;
}

// 可点击模块样式
.clickable-module--disabled {
  cursor: default;
  user-select: none;
  .module-arrow {
    display: none;
  }
  &:hover {
    transform: none;
    box-shadow: none;
    border-color: rgba(0, 212, 255, 0.12);
  }
}

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

.card-header--link {
  cursor: pointer;
  user-select: none;
  transition: color 0.2s ease;

  &:hover {
    color: rgba(255, 255, 255, 0.95);
  }

  &:focus-visible {
    outline: 2px solid rgba(0, 212, 255, 0.6);
    outline-offset: 2px;
    border-radius: 6px;
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

.security-overview {
  padding: 10px;
}

.security-rates {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.rate-ring {
  flex: 1;
  position: relative;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 10px;
  padding: 10px 8px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 92px;
}

.rate-ring__svg {
  width: 56px;
  height: 56px;
}

.rate-ring__value {
  position: absolute;
  top: 38px;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 16px;
  font-weight: 800;
  font-family: 'Orbitron', monospace;
  color: $primary-blue;
  letter-spacing: 0.5px;

  &.green {
    color: $success-green;
  }
}

.rate-ring__label {
  margin-top: 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.65);
}

.security-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.security-item {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 10px;
  padding: 10px;
}

.security-item__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.security-item__title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.security-item__total {
  font-size: 18px;
  font-weight: 800;
  font-family: 'Orbitron', monospace;
  color: rgba(255, 255, 255, 0.92);
}

.security-item__foot {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
}

.security-item__status {
  flex: 1;
  text-align: left;
  white-space: nowrap;

  &.green {
    color: $success-green;
  }
  &.red {
    color: $alert-red;
  }
}

.chart-alarm-type {
  height: 150px;
}

.access-kpi__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.access-kpi__item {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 10px;
  padding: 14px 12px;
}

.access-kpi__label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
  margin-bottom: 8px;
}

.access-kpi__value {
  font-size: 22px;
  font-weight: 800;
  font-family: 'Orbitron', monospace;
  color: rgba(255, 255, 255, 0.92);
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

.map-view--twin {
  flex-direction: column;
  align-items: stretch;
  justify-content: stretch;
}

.map-park-twin {
  flex: 1;
  min-height: 0;
  width: 100%;
  position: relative;
  background: #0b1220;
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
