<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useAppStore } from '@/store/modules/app'

defineOptions({ name: 'BuildingVisualDashboard' })

type DeviceStatusKey = 'running' | 'standby' | 'fault' | 'offline'
type DeviceTypeKey = 'ac' | 'fresh' | 'fan' | 'water'

const appStore = useAppStore()
const isDark = computed(() => appStore.getIsDark)

const updateTime = ref('')
let updateTimer: number | undefined

const metric = reactive({
  deviceTotal: 24,
  typeTotal: {
    ac: 6,
    fresh: 4,
    fan: 8,
    water: 6
  },
  online: 21,
  offline: 3,
  alarmTotal: 5,
  alarmDetail: { urgent: 1, important: 2, info: 2 }
})

const overallStatus = reactive<Record<DeviceStatusKey, number>>({
  running: 7,
  standby: 12,
  fault: 2,
  offline: 3
})

const deviceTypeStatus = reactive<Record<DeviceTypeKey, Record<DeviceStatusKey, number>>>({
  ac: { running: 2, standby: 3, fault: 1, offline: 0 },
  fresh: { running: 1, standby: 2, fault: 0, offline: 1 },
  fan: { running: 2, standby: 4, fault: 1, offline: 1 },
  water: { running: 2, standby: 3, fault: 0, offline: 1 }
})

const zones = [
  {
    name: '一层区域',
    total: 6,
    icon: 'fa6-solid:layer-group',
    detail: { ac: 2, fresh: 1, fan: 2, water: 1 }
  },
  {
    name: '二层区域',
    total: 5,
    icon: 'fa6-solid:layer-group',
    detail: { ac: 1, fresh: 1, fan: 2, water: 1 }
  },
  {
    name: '三层区域',
    total: 8,
    icon: 'fa6-solid:layer-group',
    detail: { ac: 2, fresh: 1, fan: 3, water: 2 }
  },
  {
    name: 'B1机房',
    total: 2,
    icon: 'fa6-solid:box-archive',
    detail: { ac: 1, fresh: 1, fan: 0, water: 0 }
  },
  {
    name: '屋顶层',
    total: 3,
    icon: 'fa6-solid:cloud-sun',
    detail: { ac: 0, fresh: 1, fan: 2, water: 0 }
  }
]

const zoneCount = computed(() => zones.length)

const onlineRate = computed(() => {
  if (metric.deviceTotal <= 0) return 0
  return (metric.online / metric.deviceTotal) * 100
})

const overallTotal = computed(() => {
  return Object.values(overallStatus).reduce((acc, cur) => acc + cur, 0)
})

const overallPercent = computed(() => {
  const total = overallTotal.value || 1
  return {
    running: (overallStatus.running / total) * 100,
    standby: (overallStatus.standby / total) * 100,
    fault: (overallStatus.fault / total) * 100,
    offline: (overallStatus.offline / total) * 100
  }
})

const standbyRatio = computed(() => overallPercent.value.standby)
const runningRatio = computed(() => overallPercent.value.running)

const formatDateTime = (date: Date) => {
  const pad2 = (n: number) => String(n).padStart(2, '0')
  const y = date.getFullYear()
  const m = pad2(date.getMonth() + 1)
  const d = pad2(date.getDate())
  const hh = pad2(date.getHours())
  const mm = pad2(date.getMinutes())
  const ss = pad2(date.getSeconds())
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
}

const setUpdateTime = () => {
  updateTime.value = formatDateTime(new Date())
}

const statusColor = (key: DeviceStatusKey) => {
  if (key === 'running') return '#10b981'
  if (key === 'standby') return '#f59e0b'
  if (key === 'fault') return '#f43f5e'
  return '#94a3b8'
}

const deviceTypeMeta = (key: DeviceTypeKey) => {
  if (key === 'ac') return { name: '空调机组', icon: 'fa6-solid:snowflake', tone: 'blue' }
  if (key === 'fresh') return { name: '新风机组', icon: 'fa6-solid:wind', tone: 'cyan' }
  if (key === 'fan') return { name: '送/排风机', icon: 'fa6-solid:fan', tone: 'emerald' }
  return { name: '给排水系统', icon: 'fa6-solid:droplet', tone: 'indigo' }
}

const deviceTypeOnline = (key: DeviceTypeKey) => {
  const s = deviceTypeStatus[key]
  return s.running + s.standby + s.fault
}

const buildPieOptions = (key: DeviceTypeKey) => {
  const s = deviceTypeStatus[key]
  const data = (Object.keys(s) as DeviceStatusKey[]).map((status) => ({
    value: s[status],
    itemStyle: { color: statusColor(status) }
  }))

  return {
    tooltip: { show: false },
    legend: { show: false },
    series: [
      {
        type: 'pie',
        radius: ['65%', '80%'],
        avoidLabelOverlap: false,
        label: { show: false },
        data
      }
    ]
  } as EChartsOption
}

const pieOptions = computed<Record<DeviceTypeKey, EChartsOption>>(() => ({
  ac: buildPieOptions('ac'),
  fresh: buildPieOptions('fresh'),
  fan: buildPieOptions('fan'),
  water: buildPieOptions('water')
}))

onMounted(() => {
  setUpdateTime()
  updateTimer = window.setInterval(setUpdateTime, 1000)
})

onBeforeUnmount(() => {
  if (updateTimer) window.clearInterval(updateTimer)
})
</script>

<template>
  <div
    class="bd-page"
    :class="{ 'bd-page--dark': isDark }"
    :style="{
      paddingTop: 'max(0px, calc(var(--page-top-gap,70px) - (var(--app-content-padding) + 10px)))'
    }"
  >
    <header class="bd-glass bd-header">
      <div class="bd-header__left">
        <div class="bd-header__logo">
          <Icon icon="fa6-solid:building" />
        </div>
        <div>
          <h1 class="bd-header__title">建筑设备监控数据总览</h1>
          <p class="bd-header__sub">
            <span class="bd-live-dot"></span>
            实时监控中 | 数据更新于 <span class="bd-time">{{ updateTime }}</span>
          </p>
        </div>
      </div>
    </header>

    <section class="bd-metrics">
      <div class="bd-metric bd-metric--blue">
        <div class="bd-metric__top">
          <div>
            <p class="bd-muted">设备总数</p>
            <div class="bd-metric__value">{{ metric.deviceTotal }}</div>
          </div>
          <div class="bd-metric__icon bd-metric__icon--blue">
            <Icon icon="fa6-solid:server" />
          </div>
        </div>
        <div class="bd-tags">
          <span class="bd-tag"
            ><Icon icon="fa6-solid:snowflake" />空调 {{ metric.typeTotal.ac }}</span
          >
          <span class="bd-tag"
            ><Icon icon="fa6-solid:wind" />新风 {{ metric.typeTotal.fresh }}</span
          >
          <span class="bd-tag"><Icon icon="fa6-solid:fan" />风机 {{ metric.typeTotal.fan }}</span>
          <span class="bd-tag"
            ><Icon icon="fa6-solid:droplet" />给排水 {{ metric.typeTotal.water }}</span
          >
        </div>
      </div>

      <div class="bd-metric bd-metric--green">
        <div class="bd-metric__top">
          <div>
            <p class="bd-muted">设备在线率</p>
            <div class="bd-metric__value">{{ onlineRate.toFixed(1) }}%</div>
          </div>
          <div class="bd-metric__icon bd-metric__icon--green">
            <Icon icon="fa6-solid:wifi" />
          </div>
        </div>
        <div class="bd-split">
          <span class="bd-split__item"
            ><span class="bd-strong bd-strong--green">{{ metric.online }}</span> 在线</span
          >
          <span class="bd-split__item"
            ><span class="bd-strong">{{ metric.offline }}</span> 离线</span
          >
        </div>
        <div class="bd-progress">
          <div class="bd-progress__bar" :style="{ width: `${onlineRate}%` }"></div>
        </div>
      </div>

      <div class="bd-metric bd-metric--amber">
        <div class="bd-metric__top">
          <div>
            <p class="bd-muted">待处理告警</p>
            <div class="bd-metric__value">{{ metric.alarmTotal }}</div>
          </div>
          <div class="bd-metric__icon bd-metric__icon--amber">
            <Icon icon="fa6-solid:bell" />
          </div>
        </div>
        <div class="bd-badges">
          <span class="bd-badge bd-badge--rose">紧急 {{ metric.alarmDetail.urgent }}</span>
          <span class="bd-badge bd-badge--amber">重要 {{ metric.alarmDetail.important }}</span>
          <span class="bd-badge bd-badge--blue">提示 {{ metric.alarmDetail.info }}</span>
        </div>
      </div>
    </section>

    <section class="bd-main">
      <div class="bd-glass bd-panel">
        <div class="bd-panel__head">
          <h3 class="bd-panel__title">
            <Icon icon="fa6-solid:location-dot" class="bd-title-icon" /> 各区域建筑设备监控分布
          </h3>
          <span class="bd-pill">{{ zoneCount }}区域</span>
        </div>

        <div class="bd-zone-list">
          <div v-for="zone in zones" :key="zone.name" class="bd-zone">
            <div class="bd-zone__top">
              <span class="bd-zone__name">
                <Icon :icon="zone.icon" class="bd-zone__icon" />
                {{ zone.name }}
              </span>
              <span class="bd-zone__count">{{ zone.total }}台</span>
            </div>
            <div class="bd-zone__grid">
              <div
                class="bd-zone__item"
                :class="{ 'bd-zone__item--disabled': zone.detail.ac === 0 }"
              >
                <Icon icon="fa6-solid:snowflake" /> 空调 {{ zone.detail.ac }}
              </div>
              <div
                class="bd-zone__item"
                :class="{ 'bd-zone__item--disabled': zone.detail.fresh === 0 }"
              >
                <Icon icon="fa6-solid:wind" /> 新风 {{ zone.detail.fresh }}
              </div>
              <div
                class="bd-zone__item"
                :class="{ 'bd-zone__item--disabled': zone.detail.fan === 0 }"
              >
                <Icon icon="fa6-solid:fan" /> 风机 {{ zone.detail.fan }}
              </div>
              <div
                class="bd-zone__item"
                :class="{ 'bd-zone__item--disabled': zone.detail.water === 0 }"
              >
                <Icon icon="fa6-solid:droplet" /> 给排水 {{ zone.detail.water }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="bd-glass bd-panel bd-panel--wide">
        <div class="bd-panel__head bd-panel__head--wrap">
          <h3 class="bd-panel__title">
            <Icon icon="fa6-solid:chart-simple" class="bd-title-icon bd-title-icon--indigo" />
            建筑设备监控状态统计
          </h3>
          <div class="bd-legend">
            <span class="bd-legend__item"><span class="bd-dot bd-dot--running"></span>运行</span>
            <span class="bd-legend__item"><span class="bd-dot bd-dot--standby"></span>待机</span>
            <span class="bd-legend__item"><span class="bd-dot bd-dot--fault"></span>故障</span>
            <span class="bd-legend__item"><span class="bd-dot bd-dot--offline"></span>离线</span>
          </div>
        </div>

        <div class="bd-overall">
          <div class="bd-overall__head">
            <h4 class="bd-overall__title">
              <Icon icon="fa6-solid:chart-pie" class="bd-title-icon" /> 总体状态分布
            </h4>
            <div class="bd-ratios">
              <span class="bd-ratio">待机占比 {{ standbyRatio.toFixed(1) }}%</span>
              <span class="bd-ratio">运行占比 {{ runningRatio.toFixed(1) }}%</span>
            </div>
          </div>

          <div class="bd-status-grid">
            <div class="bd-status bd-status--running">
              <div class="bd-status__value">{{ overallStatus.running }}</div>
              <div class="bd-status__label">运行中</div>
              <div class="bd-mini-progress">
                <div
                  class="bd-mini-progress__bar bd-mini-progress__bar--running"
                  :style="{ width: `${overallPercent.running}%` }"
                ></div>
              </div>
            </div>
            <div class="bd-status bd-status--standby">
              <div class="bd-status__value">{{ overallStatus.standby }}</div>
              <div class="bd-status__label">待机</div>
              <div class="bd-mini-progress">
                <div
                  class="bd-mini-progress__bar bd-mini-progress__bar--standby"
                  :style="{ width: `${overallPercent.standby}%` }"
                ></div>
              </div>
            </div>
            <div class="bd-status bd-status--fault">
              <div class="bd-status__value">{{ overallStatus.fault }}</div>
              <div class="bd-status__label">故障</div>
              <div class="bd-mini-progress">
                <div
                  class="bd-mini-progress__bar bd-mini-progress__bar--fault"
                  :style="{ width: `${overallPercent.fault}%` }"
                ></div>
              </div>
            </div>
            <div class="bd-status bd-status--offline">
              <div class="bd-status__value">{{ overallStatus.offline }}</div>
              <div class="bd-status__label">离线</div>
              <div class="bd-mini-progress">
                <div
                  class="bd-mini-progress__bar bd-mini-progress__bar--offline"
                  :style="{ width: `${overallPercent.offline}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div class="bd-device-grid">
          <div
            v-for="typeKey in ['ac', 'fresh', 'fan', 'water'] as DeviceTypeKey[]"
            :key="typeKey"
            class="bd-device"
          >
            <div class="bd-device__head">
              <div class="bd-device__name">
                <span
                  class="bd-device__icon"
                  :class="`bd-device__icon--${deviceTypeMeta(typeKey).tone}`"
                >
                  <Icon :icon="deviceTypeMeta(typeKey).icon" />
                </span>
                <span class="bd-device__title">{{ deviceTypeMeta(typeKey).name }}</span>
                <span class="bd-device__total">({{ metric.typeTotal[typeKey] }}台)</span>
              </div>
              <span class="bd-device__pill">
                在线{{ deviceTypeOnline(typeKey) }} · 离线{{ deviceTypeStatus[typeKey].offline }}
              </span>
            </div>

            <div class="bd-device__body">
              <div class="bd-pie">
                <Echart :options="pieOptions[typeKey]" height="120px" />
              </div>
              <div class="bd-device__stats">
                <div class="bd-stat">
                  <span class="bd-dot bd-dot--running"></span>运行
                  <span class="bd-stat__val">{{ deviceTypeStatus[typeKey].running }}</span>
                </div>
                <div class="bd-stat">
                  <span class="bd-dot bd-dot--standby"></span>待机
                  <span class="bd-stat__val">{{ deviceTypeStatus[typeKey].standby }}</span>
                </div>
                <div class="bd-stat">
                  <span class="bd-dot bd-dot--fault"></span>故障
                  <span class="bd-stat__val">{{ deviceTypeStatus[typeKey].fault }}</span>
                </div>
                <div class="bd-stat">
                  <span class="bd-dot bd-dot--offline"></span>离线
                  <span class="bd-stat__val">{{ deviceTypeStatus[typeKey].offline }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.bd-page {
  min-height: 100%;
  padding: 16px;
  color: var(--el-text-color-primary);
  background: var(--el-bg-color-page);
}

.bd-glass {
  background: color-mix(in srgb, var(--el-bg-color-overlay) 80%, transparent);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 16px;
  box-shadow:
    0 4px 12px -2px rgb(0 0 0 / 6%),
    0 2px 4px -1px rgb(0 0 0 / 4%);
}

.bd-header {
  padding: 16px;
  margin-bottom: 16px;
}

.bd-header__left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.bd-header__logo {
  display: flex;
  width: 48px;
  height: 48px;
  color: #fff;
  background: var(--el-color-primary);
  border-radius: 14px;
  align-items: center;
  justify-content: center;
}

.bd-header__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.bd-header__sub {
  display: flex;
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  align-items: center;
  gap: 8px;
}

.bd-time {
  color: var(--el-text-color-regular);
}

.bd-live-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: var(--el-color-success);
  border-radius: 999px;
  animation: bdBlink 1.5s infinite;
}

@keyframes bdBlink {
  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.3;
  }
}

.bd-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.bd-metric {
  padding: 16px;
  background: color-mix(in srgb, var(--el-bg-color-overlay) 90%, transparent);
  border: 1px solid var(--el-border-color-lighter);
  border-left: 4px solid transparent;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.bd-metric:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px -8px rgb(0 0 0 / 12%);
}

.bd-metric--blue {
  border-left-color: #3b82f6;
}

.bd-metric--green {
  border-left-color: #10b981;
}

.bd-metric--amber {
  border-left-color: #f59e0b;
}

.bd-metric__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.bd-muted {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.bd-metric__value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
}

.bd-metric__icon {
  display: flex;
  width: 40px;
  height: 40px;
  font-size: 16px;
  border-radius: 10px;
  align-items: center;
  justify-content: center;
}

.bd-metric__icon--blue {
  color: #3b82f6;
  background: color-mix(in srgb, #3b82f6 18%, transparent);
}

.bd-metric__icon--green {
  color: #10b981;
  background: color-mix(in srgb, #10b981 18%, transparent);
}

.bd-metric__icon--amber {
  color: #f59e0b;
  background: color-mix(in srgb, #f59e0b 18%, transparent);
}

.bd-tags {
  display: flex;
  padding-top: 10px;
  margin-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
  flex-wrap: wrap;
  gap: 8px;
}

.bd-tag {
  display: inline-flex;
  padding: 2px 10px;
  font-size: 12px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 999px;
  align-items: center;
  gap: 6px;
}

.bd-split {
  display: flex;
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  justify-content: space-between;
  gap: 12px;
}

.bd-strong {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.bd-strong--green {
  color: var(--el-color-success);
}

.bd-progress {
  height: 8px;
  margin-top: 10px;
  overflow: hidden;
  background: var(--el-fill-color-light);
  border-radius: 999px;
}

.bd-progress__bar {
  height: 100%;
  background: var(--el-color-success);
  border-radius: 999px;
}

.bd-badges {
  display: flex;
  margin-top: 10px;
  gap: 8px;
  flex-wrap: wrap;
}

.bd-badge {
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid transparent;
  border-radius: 8px;
}

.bd-badge--rose {
  color: var(--el-color-danger);
  background: color-mix(in srgb, var(--el-color-danger) 15%, transparent);
  border-color: color-mix(in srgb, var(--el-color-danger) 22%, transparent);
}

.bd-badge--amber {
  color: #b45309;
  background: color-mix(in srgb, #f59e0b 18%, transparent);
  border-color: color-mix(in srgb, #f59e0b 25%, transparent);
}

.bd-badge--blue {
  color: var(--el-color-primary);
  background: color-mix(in srgb, var(--el-color-primary) 15%, transparent);
  border-color: color-mix(in srgb, var(--el-color-primary) 22%, transparent);
}

.bd-main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
}

.bd-panel {
  padding: 16px;
}

.bd-panel--wide {
  grid-column: 2 / 3;
}

.bd-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.bd-panel__head--wrap {
  flex-wrap: wrap;
}

.bd-panel__title {
  display: inline-flex;
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  align-items: center;
  gap: 8px;
}

.bd-title-icon {
  color: var(--el-color-primary);
}

.bd-title-icon--indigo {
  color: #6366f1;
}

.bd-pill {
  padding: 2px 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 999px;
}

.bd-zone-list {
  display: flex;
  max-height: 460px;
  padding-right: 4px;
  overflow: auto;
  flex-direction: column;
  gap: 12px;
}

.bd-zone {
  padding: 12px;
  background: color-mix(in srgb, var(--el-bg-color-overlay) 90%, transparent);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.bd-zone__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.bd-zone__name {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.bd-zone__icon {
  color: color-mix(in srgb, var(--el-color-primary) 70%, var(--el-text-color-secondary));
}

.bd-zone__count {
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 700;
  color: var(--el-color-primary);
  background: color-mix(in srgb, var(--el-color-primary) 16%, transparent);
  border: 1px solid color-mix(in srgb, var(--el-color-primary) 25%, transparent);
  border-radius: 999px;
}

.bd-zone__grid {
  display: grid;
  margin-top: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.bd-zone__item {
  display: flex;
  padding: 8px 10px;
  font-size: 12px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  align-items: center;
  gap: 8px;
}

.bd-zone__item--disabled {
  opacity: 0.55;
}

.bd-legend {
  display: flex;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 999px;
  gap: 10px;
  flex-wrap: wrap;
}

.bd-legend__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.bd-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.bd-dot--running {
  background: #10b981;
}

.bd-dot--standby {
  background: #f59e0b;
}

.bd-dot--fault {
  background: #f43f5e;
}

.bd-dot--offline {
  background: #94a3b8;
}

.bd-overall {
  padding: 14px;
  margin-bottom: 14px;
  background: color-mix(in srgb, var(--el-bg-color-overlay) 92%, transparent);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
}

.bd-overall__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.bd-overall__title {
  display: inline-flex;
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--el-text-color-secondary);
  align-items: center;
  gap: 8px;
}

.bd-ratios {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.bd-ratio {
  padding: 2px 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: color-mix(in srgb, var(--el-bg-color-overlay) 70%, transparent);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 999px;
}

.bd-status-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.bd-status {
  padding: 12px;
  text-align: center;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.bd-status__value {
  margin-bottom: 4px;
  font-size: 22px;
  font-weight: 800;
}

.bd-status__label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.bd-mini-progress {
  height: 6px;
  margin-top: 10px;
  overflow: hidden;
  background: var(--el-fill-color-light);
  border-radius: 999px;
}

.bd-mini-progress__bar {
  height: 100%;
  border-radius: 999px;
}

.bd-mini-progress__bar--running {
  background: #10b981;
}

.bd-mini-progress__bar--standby {
  background: #f59e0b;
}

.bd-mini-progress__bar--fault {
  background: #f43f5e;
}

.bd-mini-progress__bar--offline {
  background: #94a3b8;
}

.bd-status--running {
  background: color-mix(in srgb, #10b981 12%, var(--el-bg-color-overlay));
}

.bd-status--standby {
  background: color-mix(in srgb, #f59e0b 12%, var(--el-bg-color-overlay));
}

.bd-status--fault {
  background: color-mix(in srgb, #f43f5e 10%, var(--el-bg-color-overlay));
}

.bd-status--offline {
  background: color-mix(in srgb, #94a3b8 14%, var(--el-bg-color-overlay));
}

.bd-device-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.bd-device {
  padding: 12px;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
  transition: all 0.2s ease;
}

.bd-device:hover {
  border-color: var(--el-border-color);
  box-shadow: 0 10px 18px -10px rgb(0 0 0 / 16%);
}

.bd-device__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.bd-device__name {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.bd-device__icon {
  display: inline-flex;
  width: 28px;
  height: 28px;
  border-radius: 10px;
  align-items: center;
  justify-content: center;
}

.bd-device__icon--blue {
  color: #3b82f6;
  background: color-mix(in srgb, #3b82f6 18%, transparent);
}

.bd-device__icon--cyan {
  color: #06b6d4;
  background: color-mix(in srgb, #06b6d4 18%, transparent);
}

.bd-device__icon--emerald {
  color: #10b981;
  background: color-mix(in srgb, #10b981 18%, transparent);
}

.bd-device__icon--indigo {
  color: #6366f1;
  background: color-mix(in srgb, #6366f1 18%, transparent);
}

.bd-device__title {
  font-weight: 700;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}

.bd-device__total {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.bd-device__pill {
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 700;
  color: var(--el-color-success);
  white-space: nowrap;
  background: color-mix(in srgb, var(--el-color-success) 14%, transparent);
  border: 1px solid color-mix(in srgb, var(--el-color-success) 22%, transparent);
  border-radius: 999px;
}

.bd-device__body {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 10px;
  align-items: center;
  margin-top: 8px;
}

.bd-pie {
  width: 120px;
  height: 120px;
}

.bd-device__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.bd-stat {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bd-stat__val {
  margin-left: auto;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

@media (width <= 1200px) {
  .bd-main {
    grid-template-columns: 1fr;
  }

  .bd-panel--wide {
    grid-column: auto;
  }
}

@media (width <= 900px) {
  .bd-metrics {
    grid-template-columns: 1fr;
  }

  .bd-device-grid {
    grid-template-columns: 1fr;
  }

  .bd-status-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
