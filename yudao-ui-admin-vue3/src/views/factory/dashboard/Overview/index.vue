<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useWindowSize } from '@vueuse/core'
import { useRouter } from 'vue-router'
import { getFactoryDashboardOverviewData, type FactoryDashboardOverviewData } from '@/api/factory/dashboard'
import { Echart } from '@/components/Echart'
import { Icon } from '@/components/Icon'
import {
  buildFactoryVideoLinkQuery,
  buildPlaybackTimeRange
} from '@/composables/factory/useFactoryVideoLink'
import FactoryThreeStage from '../components/FactoryThreeStage.vue'
import FactoryDashboardShell from '../components/FactoryDashboardShell.vue'
import FactoryMetricCard from '../components/FactoryMetricCard.vue'

defineOptions({ name: 'FactoryDashboardOverview' })

interface OverviewVideoSource {
  id: number
  name: string
  location: string
  level: 'high' | 'medium'
}

interface FactoryThreeStageExpose {
  resetView: () => void
  focusDevices: () => void
  togglePatrol: () => void
}

const router = useRouter()
const { width } = useWindowSize()
const loading = ref(false)
const updatedAt = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
const refreshTimer = ref<number>()
const overviewData = ref<FactoryDashboardOverviewData | null>(null)
const selectedFloorId = ref<number | 'all'>('all')
const loadErrorMessage = ref('')
const stageThreeRef = ref<FactoryThreeStageExpose | null>(null)

const formatNumber = (value?: number | null, digits = 0) => {
  const numericValue = value ?? 0
  return numericValue.toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

const formatTrend = (value?: number | null) => {
  if (value === undefined || value === null) {
    return '真实快照'
  }
  const prefix = value > 0 ? '+' : ''
  return `${prefix}${formatNumber(value, 1)}%`
}

const isCurrentFloor = (floorId?: number | null) => {
  return selectedFloorId.value === 'all' || floorId === selectedFloorId.value
}

const floorTabs = computed(() => {
  const floors = overviewData.value?.floors || []
  return [{ id: 'all' as const, name: '全部楼层' }, ...floors]
})

const selectedFloorName = computed(() => {
  if (selectedFloorId.value === 'all') {
    return '全部楼层'
  }
  return overviewData.value?.floors.find((floor) => floor.id === selectedFloorId.value)?.name || '全部楼层'
})

const metricCards = computed(() => {
  const kpis = overviewData.value?.kpis
  return [
    {
      title: '设备在线率',
      value: formatNumber(kpis?.deviceOnlineRate?.value, 1),
      unit: kpis?.deviceOnlineRate?.unit || '%',
      trend: formatTrend(kpis?.deviceOnlineRate?.trend),
      hint: `在线 ${formatNumber(kpis?.deviceOnlineRate?.online)} / 总数 ${formatNumber(kpis?.deviceOnlineRate?.total)}`,
      icon: 'ep:connection',
      badge: `${formatNumber(kpis?.deviceOnlineRate?.value, 0)}%`,
      theme: 'cyan'
    },
    {
      title: '告警数',
      value: formatNumber(kpis?.alarmCount?.value),
      unit: kpis?.alarmCount?.unit || '条',
      trend: formatTrend(kpis?.alarmCount?.trend),
      hint: `未处理 ${formatNumber(kpis?.alarmCount?.unhandled)} / 已处理 ${formatNumber(kpis?.alarmCount?.handled)}`,
      icon: 'ep:warning',
      badge: formatNumber(kpis?.alarmCount?.value),
      theme: 'amber'
    },
    {
      title: '环境合格率',
      value: formatNumber(kpis?.environmentComplianceRate?.value, 1),
      unit: kpis?.environmentComplianceRate?.unit || '%',
      trend: formatTrend(kpis?.environmentComplianceRate?.trend),
      hint: `合格 ${formatNumber(kpis?.environmentComplianceRate?.qualified)} / 总数 ${formatNumber(kpis?.environmentComplianceRate?.total)}`,
      icon: 'ep:data-analysis',
      badge: `${formatNumber(kpis?.environmentComplianceRate?.value, 0)}%`,
      theme: 'emerald'
    },
    {
      title: '最新日电耗',
      value: formatNumber(kpis?.todayEnergy?.value, 1),
      unit: kpis?.todayEnergy?.unit || 'kWh',
      trend: formatTrend(kpis?.todayEnergy?.trend),
      hint: kpis?.todayEnergy?.statDate
        ? `统计日期 ${dayjs(kpis.todayEnergy.statDate).format('MM-DD')} · 水 ${formatNumber(kpis.todayEnergy.water, 1)} · 气 ${formatNumber(kpis.todayEnergy.gas, 1)}`
        : '暂无能耗统计数据',
      icon: 'ep:lightning',
      badge: dayjs(kpis?.todayEnergy?.statDate).isValid()
        ? dayjs(kpis?.todayEnergy?.statDate).format('MM/DD')
        : '最新',
      theme: 'violet'
    }
  ] as const
})

const deviceList = computed(() => {
  return (overviewData.value?.deviceStatusList || []).filter((item) => isCurrentFloor(item.floorId))
})

const alertList = computed(() => {
  return (overviewData.value?.latestAlerts || []).filter((item) => isCurrentFloor(item.floorId))
})

const maxDeviceItems = computed(() => {
  if (width.value >= 1920) return 7
  if (width.value >= 1600) return 6
  if (width.value >= 1440) return 5
  return 4
})

const maxAlertItems = computed(() => {
  if (width.value >= 1920) return 6
  if (width.value >= 1600) return 5
  if (width.value >= 1440) return 4
  return 3
})

const displayDeviceList = computed(() => deviceList.value.slice(0, maxDeviceItems.value))
const displayAlertList = computed(() => alertList.value.slice(0, maxAlertItems.value))

const filteredSourceList = computed(() => {
  return (overviewData.value?.videoSnapshot?.sources || []).filter((item) => isCurrentFloor(item.floorId))
})

const primaryVideoSource = computed(() => {
  return filteredSourceList.value[0] || overviewData.value?.videoSnapshot?.primarySource || null
})

const primaryVideoLevelText = computed(() => {
  if (!primaryVideoSource.value) {
    return '待命'
  }
  return primaryVideoSource.value.status === 'online' ? '直播中' : '待联动'
})

const quickVideoList = computed(() => {
  return filteredSourceList.value.slice(0, 4)
})

const sceneInfo = computed(() => overviewData.value?.scene)
const onlineDeviceCount = computed(() => deviceList.value.filter((item) => item.online).length)
const unhandledAlertCount = computed(() => alertList.value.filter((item) => !item.handled).length)
const stageActions = computed(() => sceneInfo.value?.actions || [])

const resolveFloorIcon = (floorId: string, floorName: string) => {
  const normalized = `${floorId} ${floorName}`.toLowerCase()
  if (floorId === 'all') return 'ep:operation'
  if (normalized.includes('1') || floorName.includes('仓') || floorName.includes('厂')) return 'ep:office-building'
  if (normalized.includes('2') || floorName.includes('生产') || floorName.includes('车间')) return 'ep:data-analysis'
  if (normalized.includes('3') || floorName.includes('办')) return 'ep:monitor'
  return 'ep:files'
}

const resolveFloorCode = (floorId: string, floorName: string) => {
  if (floorId === 'all') return '全部'
  const normalizedId = String(floorId || '').trim().toUpperCase()
  const normalizedName = String(floorName || '').replace(/\s+/g, '')
  if (normalizedId && normalizedId !== 'ALL' && normalizedId !== normalizedName.toUpperCase() && normalizedId.length <= 8) {
    return normalizedId
  }
  const floorMatch = normalizedName.match(/\d+/)
  if (floorMatch) {
    return `F${floorMatch[0]}`
  }
  return '区域'
}

const resolveStageActionIcon = (action: FactoryDashboardOverviewData['scene']['actions'][number]) => {
  if (action.label.includes('视频')) {
    return 'ep:video-camera'
  }
  if (action.label.includes('巡检') || action.label.includes('漫游')) {
    return 'ep:guide'
  }
  if (action.label.includes('定位')) {
    return 'ep:location'
  }
  if (action.label.includes('重置')) {
    return 'ep:refresh-right'
  }
  return action.actionType === 'route' ? 'ep:arrow-right' : 'ep:message'
}

const environmentCards = computed(() => {
  const snapshot = overviewData.value?.environmentSnapshot
  return [
    { label: '温度', value: snapshot?.temperature, unit: '°C', digits: 1 },
    { label: '湿度', value: snapshot?.humidity, unit: '%RH', digits: 1 },
    { label: 'PM2.5', value: snapshot?.pm25, unit: 'μg/m³', digits: 1 },
    { label: 'CO₂', value: snapshot?.co2, unit: 'ppm', digits: 0 },
    { label: '压差', value: snapshot?.differentialPressure, unit: 'Pa', digits: 0 },
    { label: '洁净度', value: snapshot?.cleanliness, unit: '', digits: 0 }
  ]
})

const formatEnvironmentCardValue = (card: (typeof environmentCards.value)[number]) => {
  if (card.value === undefined || card.value === null || card.value === '') {
    return '--'
  }
  if (typeof card.value === 'number') {
    return formatNumber(card.value, card.digits)
  }
  return card.value
}

const energyChartOptions = computed<EChartsOption>(() => {
  const trend = overviewData.value?.energyTrend || []
  return {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 12,
      right: 12,
      top: 18,
      bottom: 18,
      containLabel: true
    },
    legend: {
      top: 0,
      textStyle: {
        color: 'rgba(214, 235, 249, 0.72)'
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trend.map((item) => dayjs(item.date).format('MM-DD')),
      axisLabel: {
        color: 'rgba(192, 216, 236, 0.68)'
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(103, 146, 177, 0.24)'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: 'rgba(192, 216, 236, 0.68)'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(103, 146, 177, 0.16)'
        }
      }
    },
    series: [
      {
        name: '电',
        type: 'line',
        smooth: true,
        data: trend.map((item) => item.electricity),
        lineStyle: { color: '#60a5fa' },
        itemStyle: { color: '#60a5fa' },
        areaStyle: { color: 'rgba(96, 165, 250, 0.12)' }
      },
      {
        name: '水',
        type: 'line',
        smooth: true,
        data: trend.map((item) => item.water),
        lineStyle: { color: '#22d3ee' },
        itemStyle: { color: '#22d3ee' }
      },
      {
        name: '气',
        type: 'line',
        smooth: true,
        data: trend.map((item) => item.gas),
        lineStyle: { color: '#f59e0b' },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }
})

const resolveErrorMessage = (error: unknown) => {
  const errorMessage =
    (error as { response?: { data?: { msg?: string } }; message?: string })?.response?.data?.msg ||
    (error as { message?: string })?.message
  if (!errorMessage) {
    return '智慧工厂总览接口暂不可用，请稍后重试或确认后端已同步最新接口。'
  }
  return `智慧工厂总览接口暂不可用：${errorMessage}`
}

const loadOverviewData = async (options?: { showSuccessMessage?: boolean; showErrorMessage?: boolean }) => {
  loading.value = true
  try {
    const data = await getFactoryDashboardOverviewData()
    overviewData.value = data
    loadErrorMessage.value = ''
    updatedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
    const validFloorIds = new Set(data.floors.map((floor) => floor.id))
    if (
      selectedFloorId.value !== 'all' &&
      typeof selectedFloorId.value === 'number' &&
      !validFloorIds.has(selectedFloorId.value)
    ) {
      selectedFloorId.value = 'all'
    }
    if (
      selectedFloorId.value === 'all' &&
      data.scene?.currentFloorId &&
      validFloorIds.has(data.scene.currentFloorId)
    ) {
      selectedFloorId.value = data.scene.currentFloorId
    }
    if (options?.showSuccessMessage) {
      ElMessage.success('驾驶舱总览已刷新')
    }
  } catch (error) {
    console.error('加载驾驶舱总览失败:', error)
    loadErrorMessage.value = resolveErrorMessage(error)
    if (options?.showErrorMessage) {
      ElMessage.error(loadErrorMessage.value)
    }
  } finally {
    loading.value = false
  }
}

const refreshDashboard = async () => {
  await loadOverviewData({ showSuccessMessage: true, showErrorMessage: true })
}

const toggleFullscreen = async () => {
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
      return
    }
    await document.documentElement.requestFullscreen()
  } catch (error) {
    ElMessage.warning('当前浏览器环境暂不支持全屏')
  }
}

const navigateToVideo = (query?: Record<string, string>) => {
  router.push({
    path: '/factory/video-fusion',
    query
  })
}

const navigateByRawVideoSource = (
  source?:
    | FactoryDashboardOverviewData['videoSnapshot']['sources'][number]
    | NonNullable<FactoryDashboardOverviewData['videoSnapshot']['primarySource']>
) => {
  if (!source) {
    ElMessage.info('当前区域暂无可联动视频源')
    return
  }
  ElMessage.info(`已选中联动源：${source.name}`)
  navigateToVideo(
    buildFactoryVideoLinkQuery({
      mode: 'live',
      deviceId: source.deviceId || undefined,
      sourceId: String(source.id),
      sourceName: source.name,
      location: source.location
    })
  )
}

const handleSourceClick = (source: OverviewVideoSource) => {
  const matchedSource = filteredSourceList.value.find((item) => item.id === source.id)
  navigateByRawVideoSource(
    matchedSource || {
      id: source.id,
      deviceId: undefined,
      name: source.name,
      location: source.location,
      floorId: undefined,
      floorCode: undefined,
      floorName: undefined,
      status: source.level === 'high' ? 'online' : 'offline'
    }
  )
}

const handleAlertClick = (alert: FactoryDashboardOverviewData['latestAlerts'][number]) => {
  const playbackRange = buildPlaybackTimeRange(alert.alarmTime)
  navigateToVideo(
    buildFactoryVideoLinkQuery({
      mode: alert.deviceId && playbackRange.startTime && playbackRange.endTime ? 'playback' : 'live',
      deviceId: alert.deviceId || undefined,
      alarmId: alert.id,
      sourceName: alert.deviceName || alert.title,
      location: alert.location,
      alarmTime: alert.alarmTime || undefined,
      startTime: playbackRange.startTime,
      endTime: playbackRange.endTime
    })
  )
}

const resetStageView = () => {
  selectedFloorId.value = 'all'
  stageThreeRef.value?.resetView()
}

const syncStageAction = (action: FactoryDashboardOverviewData['scene']['actions'][number]) => {
  const label = action.label || ''
  if (label.includes('巡检') || label.includes('漫游')) {
    stageThreeRef.value?.togglePatrol()
    return
  }
  if (label.includes('定位')) {
    stageThreeRef.value?.focusDevices()
    return
  }
  if (label.includes('重置')) {
    stageThreeRef.value?.resetView()
  }
}

const handleSceneAction = (action: FactoryDashboardOverviewData['scene']['actions'][number]) => {
  syncStageAction(action)
  if (!action.enabled) {
    ElMessage.info(action.target)
    return
  }
  if (action.actionType === 'route') {
    router.push(action.target)
    return
  }
  ElMessage.info(action.target)
}

onMounted(async () => {
  await loadOverviewData({ showErrorMessage: true })
  refreshTimer.value = window.setInterval(() => {
    loadOverviewData()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer.value) {
    window.clearInterval(refreshTimer.value)
  }
})
</script>

<template>
  <FactoryDashboardShell
    title="智慧工厂驾驶舱"
    subtitle="页面按产品原型重做为高保真三栏驾驶舱，继续承接真实聚合接口、主视图区联动与视频融合能力。"
    :status-text="`最近更新时间：${updatedAt}`"
    hide-hero
  >
    <template #hero-actions>
      <ElButton type="primary" @click="toggleFullscreen">
        <Icon icon="ep:full-screen" class="mr-6px" />
        全屏展示
      </ElButton>
      <ElButton plain @click="refreshDashboard">
        <Icon icon="ep:refresh" class="mr-6px" />
        刷新总览
      </ElButton>
    </template>

    <ElAlert
      v-if="loadErrorMessage"
      :title="loadErrorMessage"
      type="warning"
      :closable="false"
      show-icon
      class="overview-alert-banner"
    />

    <div v-loading="loading" class="cockpit-overview">
      <div class="overview-metrics">
        <FactoryMetricCard
          v-for="card in metricCards"
          :key="card.title"
          :title="card.title"
          :value="card.value"
          :unit="card.unit"
          :trend="card.trend"
          :hint="card.hint"
          :icon="card.icon"
          :badge="card.badge"
          :theme="card.theme"
        />
      </div>

      <div class="cockpit-grid">
        <div class="cockpit-column cockpit-column--left">
          <section class="cockpit-panel cockpit-panel--cyan">
            <header class="cockpit-panel__header">
              <div class="cockpit-panel__title-group">
                <span class="cockpit-panel__icon is-cyan">
                  <Icon icon="ep:files" />
                </span>
                <div>
                  <div class="cockpit-panel__title">楼层切换</div>
                </div>
              </div>
              <div class="cockpit-panel__badge">{{ floorTabs.length }} 个区域</div>
            </header>
            <div class="cockpit-floor-grid">
              <button
                v-for="floor in floorTabs"
                :key="floor.id"
                class="cockpit-floor-card"
                :class="{ 'is-active': selectedFloorId === floor.id }"
                type="button"
                @click="selectedFloorId = floor.id"
              >
                <span class="cockpit-floor-card__icon">
                  <Icon :icon="resolveFloorIcon(floor.id, floor.name)" />
                </span>
                <span class="cockpit-floor-card__code">
                  {{ resolveFloorCode(floor.id, floor.name) }}
                </span>
                <span class="cockpit-floor-card__name">{{ floor.name }}</span>
              </button>
            </div>
          </section>

          <section class="cockpit-panel cockpit-panel--emerald">
            <header class="cockpit-panel__header">
              <div class="cockpit-panel__title-group">
                <span class="cockpit-panel__icon is-emerald">
                  <Icon icon="ep:data-analysis" />
                </span>
                <div>
                  <div class="cockpit-panel__title">设备状态</div>
                </div>
              </div>
              <div class="cockpit-panel__badge">{{ onlineDeviceCount }}/{{ deviceList.length }}</div>
            </header>
            <div v-if="displayDeviceList.length" class="cockpit-list cockpit-list--device">
              <div v-for="device in displayDeviceList" :key="device.id" class="cockpit-device-item">
                <div class="cockpit-device-item__main">
                  <span class="cockpit-status-dot" :class="{ 'is-online': device.online }"></span>
                  <div class="cockpit-device-item__meta">
                    <div class="cockpit-device-item__name">{{ device.nickname || device.name }}</div>
                    <div class="cockpit-device-item__location">{{ device.location }}</div>
                  </div>
                </div>
                <span class="cockpit-status-chip" :class="{ 'is-online': device.online }">
                  {{ device.status }}
                </span>
              </div>
            </div>
            <div v-else class="overview-empty">当前楼层暂无设备状态数据</div>
          </section>

          <section class="cockpit-panel cockpit-panel--amber">
            <header class="cockpit-panel__header">
              <div class="cockpit-panel__title-group">
                <span class="cockpit-panel__icon is-amber">
                  <Icon icon="ep:warning" />
                </span>
                <div>
                  <div class="cockpit-panel__title">告警事件</div>
                </div>
              </div>
              <div class="cockpit-panel__badge is-alert">{{ unhandledAlertCount }} 未处理</div>
            </header>
            <div v-if="displayAlertList.length" class="cockpit-list cockpit-list--alert">
              <button
                v-for="alert in displayAlertList"
                :key="alert.id"
                class="cockpit-alert-item"
                type="button"
                @click="handleAlertClick(alert)"
              >
                <div class="cockpit-alert-item__main">
                  <div class="cockpit-alert-item__level">{{ alert.levelLabel }}</div>
                  <div class="cockpit-alert-item__meta">
                    <div class="cockpit-alert-item__title">{{ alert.title }}</div>
                    <div class="cockpit-alert-item__location">{{ alert.location }}</div>
                  </div>
                </div>
                <div class="cockpit-alert-item__side">
                  <span class="cockpit-alert-item__status" :class="{ 'is-handled': alert.handled }">
                    {{ alert.handled ? '已处理' : '未处理' }}
                  </span>
                  <span class="cockpit-alert-item__time">
                    {{ dayjs(alert.alarmTime).isValid() ? dayjs(alert.alarmTime).format('MM-DD HH:mm') : '--' }}
                  </span>
                </div>
              </button>
            </div>
            <div v-else class="overview-empty">当前楼层暂无告警记录</div>
          </section>
        </div>

        <div class="cockpit-column cockpit-column--center">
          <section class="cockpit-panel cockpit-panel--stage">
            <header class="cockpit-stage-head">
              <div class="cockpit-stage-head__content">
                <div class="cockpit-panel__title">{{ sceneInfo?.title || '工厂主视图区' }}</div>
                <div class="cockpit-panel__subtitle">
                  {{ sceneInfo?.description || '当前暂无主视图区说明' }}
                </div>
              </div>
              <div class="cockpit-stage-head__meta">
                <span class="cockpit-stage-pill">{{ selectedFloorName }}</span>
                <span class="cockpit-stage-pill is-cyan">真实联动</span>
              </div>
            </header>

            <div class="cockpit-stage-frame">
              <FactoryThreeStage
                ref="stageThreeRef"
                class="cockpit-stage-three"
                :selected-floor-name="selectedFloorName"
                :device-count="deviceList.length"
                :online-device-count="onlineDeviceCount"
                :alert-count="unhandledAlertCount"
                :video-count="quickVideoList.length"
              />
              <div class="cockpit-stage-footer">
                <div class="cockpit-stage-footer__chips">
                  <span class="cockpit-stage-footer__chip is-online">运行中 {{ onlineDeviceCount }}</span>
                  <span class="cockpit-stage-footer__chip is-alert">告警 {{ unhandledAlertCount }}</span>
                  <span class="cockpit-stage-footer__chip is-video">视频 {{ quickVideoList.length }}</span>
                </div>
                <div class="cockpit-stage-footer__actions">
                  <button class="cockpit-stage-action is-secondary" type="button" @click="resetStageView">
                    <Icon icon="ep:operation" />
                    <span>全部楼层</span>
                  </button>
                  <button
                    v-for="action in stageActions"
                    :key="action.key"
                    class="cockpit-stage-action"
                    :class="{ 'is-disabled': !action.enabled }"
                    type="button"
                    @click="handleSceneAction(action)"
                  >
                    <Icon :icon="resolveStageActionIcon(action)" />
                    <span>{{ action.label }}</span>
                  </button>
                </div>
                <div class="cockpit-stage-footer__link">拖拽旋转 · 滚轮缩放</div>
              </div>
            </div>
          </section>
        </div>

        <div class="cockpit-column cockpit-column--right">
          <section class="cockpit-panel cockpit-panel--cyan">
            <header class="cockpit-panel__header">
              <div>
                <div class="cockpit-panel__title">实时视频</div>
              </div>
              <div class="cockpit-panel__badge">
                {{ formatNumber(overviewData?.videoSnapshot?.online) }}/{{ formatNumber(overviewData?.videoSnapshot?.total) }}
              </div>
            </header>
            <button
              v-if="primaryVideoSource"
              class="overview-primary-video"
              type="button"
              @click="
                handleSourceClick({
                  id: primaryVideoSource.id,
                  name: primaryVideoSource.name,
                  location: primaryVideoSource.location,
                  level: primaryVideoSource.status === 'online' ? 'high' : 'medium'
                })
              "
            >
              <div class="overview-primary-video__screen">
                <div class="overview-primary-video__screen-grid"></div>
                <div class="overview-primary-video__screen-header">
                  <span class="overview-primary-video__screen-title">CAM LIVE</span>
                  <span
                    class="overview-primary-video__screen-status"
                    :class="{ 'is-online': primaryVideoSource.status === 'online' }"
                  >
                    <span class="overview-primary-video__screen-dot"></span>
                    {{ primaryVideoLevelText }}
                  </span>
                </div>
                <div class="overview-primary-video__screen-body">
                  <div class="overview-primary-video__screen-icon">
                    <Icon icon="ep:video-camera-filled" />
                  </div>
                </div>
                <div class="overview-primary-video__screen-footer">
                  <span>{{ primaryVideoSource.name }}</span>
                  <span>{{ primaryVideoSource.location || selectedFloorName }}</span>
                </div>
              </div>
              <div class="overview-primary-video__content">
                <div class="overview-primary-video__eyebrow">首选监控位</div>
                <div class="overview-primary-video__name">{{ primaryVideoSource.name }}</div>
                <div class="overview-primary-video__location">{{ primaryVideoSource.location }}</div>
                <div class="overview-primary-video__hint">点击进入视频融合页，继续承接实时预览或回放处理。</div>
              </div>
            </button>
            <div v-if="quickVideoList.length" class="overview-video-quick-list">
              <button
                v-for="source in quickVideoList"
                :key="source.id"
                class="overview-video-quick-item"
                type="button"
                @click="
                  handleSourceClick({
                    id: source.id,
                    name: source.name,
                    location: source.location,
                    level: source.status === 'online' ? 'high' : 'medium'
                  })
                "
              >
                <span class="overview-video-quick-item__dot" :class="{ 'is-online': source.status === 'online' }"></span>
                <span class="overview-video-quick-item__main">
                  <span class="overview-video-quick-item__name">{{ source.name }}</span>
                  <span class="overview-video-quick-item__location">{{ source.location }}</span>
                </span>
                <span class="overview-video-quick-item__action">联动</span>
              </button>
            </div>
            <div v-else class="overview-empty">当前楼层暂无在线视频源</div>
          </section>

          <section class="cockpit-panel cockpit-panel--violet">
            <header class="cockpit-panel__header">
              <div>
                <div class="cockpit-panel__title">能耗趋势</div>
              </div>
              <div class="cockpit-panel__badge">最近 7 日</div>
            </header>
            <div class="cockpit-energy-card">
              <Echart :options="energyChartOptions" height="228px" />
            </div>
          </section>

          <section class="cockpit-panel cockpit-panel--emerald">
            <header class="cockpit-panel__header">
              <div>
                <div class="cockpit-panel__title">环境监测</div>
              </div>
              <div class="cockpit-panel__badge">
                {{ formatNumber(overviewData?.environmentSnapshot?.qualified) }}/{{ formatNumber(overviewData?.environmentSnapshot?.total) }}
              </div>
            </header>
            <div class="overview-env-grid">
              <div v-for="card in environmentCards" :key="card.label" class="overview-env-card">
                <div class="overview-env-card__label">{{ card.label }}</div>
                <div class="overview-env-card__value">
                  {{ formatEnvironmentCardValue(card) }}
                  <span v-if="card.unit" class="overview-env-card__unit">{{ card.unit }}</span>
                </div>
              </div>
            </div>
            <div class="overview-env-footer">
              <span>{{ overviewData?.environmentSnapshot?.location || '未配置环境点位' }}</span>
              <span>
                {{ overviewData?.environmentSnapshot?.collectedAt ? dayjs(overviewData.environmentSnapshot.collectedAt).format('MM-DD HH:mm') : '暂无采样时间' }}
              </span>
            </div>
          </section>
        </div>
      </div>
    </div>
  </FactoryDashboardShell>
</template>

<style scoped lang="scss">
.overview-alert-banner {
  :deep(.el-alert) {
    align-items: flex-start;
    border: 1px solid rgba(255, 193, 7, 0.18);
    border-radius: 16px;
    background: rgba(58, 38, 7, 0.42);
  }

  :deep(.el-alert__title) {
    line-height: 1.7;
    color: #fdf3d4;
  }
}

.cockpit-overview {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: clamp(10px, 0.9vh, 16px);
  overflow: hidden;
}

.overview-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  flex-shrink: 0;
  gap: clamp(8px, 0.7vw, 12px);
  align-items: stretch;
  opacity: 0.9;
}

.cockpit-grid {
  display: grid;
  flex: 1;
  grid-template-columns: 276px minmax(0, 1.42fr) 296px;
  gap: clamp(10px, 0.9vw, 16px);
  min-height: 0;
  align-items: stretch;
}

.cockpit-column {
  display: flex;
  flex-direction: column;
  gap: clamp(10px, 0.9vh, 16px);
  min-width: 0;
  min-height: 0;
}

.cockpit-panel {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(80, 147, 210, 0.22);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(8, 18, 31, 0.98), rgba(4, 11, 19, 0.96)),
    linear-gradient(135deg, rgba(255, 255, 255, 0.03), transparent 48%);
  box-shadow:
    inset 0 1px 0 rgba(179, 225, 255, 0.08),
    0 16px 38px rgba(0, 0, 0, 0.24);
}

.cockpit-column--left .cockpit-panel:nth-child(1),
.cockpit-column--left .cockpit-panel:nth-child(2),
.cockpit-column--left .cockpit-panel:nth-child(3),
.cockpit-column--right .cockpit-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.cockpit-column--left .cockpit-panel:nth-child(1),
.cockpit-column--left .cockpit-panel:nth-child(2),
.cockpit-column--left .cockpit-panel:nth-child(3) {
  flex: 1 1 0;
}

.cockpit-column--center .cockpit-panel {
  flex: 1 1 auto;
  min-height: 0;
}

.cockpit-column--right .cockpit-panel:nth-child(1),
.cockpit-column--right .cockpit-panel:nth-child(2),
.cockpit-column--right .cockpit-panel:nth-child(3) {
  flex: 1 1 0;
}

.cockpit-panel::before {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  content: '';
  opacity: 0.9;
}

.cockpit-panel--cyan::before,
.cockpit-panel--stage::before {
  background: linear-gradient(90deg, rgba(74, 222, 255, 0), rgba(74, 222, 255, 0.9), rgba(74, 222, 255, 0));
}

.cockpit-panel--emerald::before {
  background: linear-gradient(90deg, rgba(16, 185, 129, 0), rgba(16, 185, 129, 0.9), rgba(16, 185, 129, 0));
}

.cockpit-panel--amber::before {
  background: linear-gradient(90deg, rgba(245, 158, 11, 0), rgba(245, 158, 11, 0.9), rgba(245, 158, 11, 0));
}

.cockpit-panel--violet::before {
  background: linear-gradient(90deg, rgba(139, 92, 246, 0), rgba(139, 92, 246, 0.9), rgba(139, 92, 246, 0));
}

.cockpit-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: clamp(10px, 1vh, 14px) clamp(12px, 0.9vw, 16px) clamp(8px, 0.7vh, 12px);
}

.cockpit-panel__title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cockpit-panel__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  font-size: 14px;
  border-radius: 8px;
  background: rgba(14, 25, 40, 0.9);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.05);
}

.cockpit-panel__icon.is-cyan {
  color: #78dcff;
  background: linear-gradient(180deg, rgba(33, 87, 156, 0.38), rgba(15, 31, 53, 0.94));
}

.cockpit-panel__icon.is-emerald {
  color: #67f0b0;
  background: linear-gradient(180deg, rgba(21, 92, 62, 0.42), rgba(10, 31, 23, 0.94));
}

.cockpit-panel__icon.is-amber {
  color: #ffb648;
  background: linear-gradient(180deg, rgba(107, 58, 14, 0.42), rgba(39, 22, 8, 0.94));
}

.cockpit-panel__eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: rgba(119, 222, 255, 0.74);
}

.cockpit-panel__title {
  margin-top: 2px;
  font-size: clamp(14px, 0.9vw, 18px);
  font-weight: 700;
  line-height: 1.2;
  color: #f5fbff;
}

.cockpit-panel__subtitle {
  max-width: 560px;
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.7;
  color: rgba(197, 223, 242, 0.68);
}

.cockpit-panel__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  min-height: 26px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #dff7ff;
  border: 1px solid rgba(103, 193, 255, 0.22);
  border-radius: 999px;
  background: rgba(9, 24, 37, 0.74);
}

.cockpit-panel__badge.is-alert {
  color: #ffe3a8;
  border-color: rgba(245, 158, 11, 0.26);
  background: rgba(60, 35, 9, 0.58);
}

.cockpit-floor-grid {
  display: grid;
  flex: 1;
  grid-template-columns: repeat(auto-fit, minmax(132px, 1fr));
  align-content: start;
  gap: clamp(8px, 0.7vw, 12px);
  padding: 0 clamp(12px, 0.9vw, 16px) clamp(12px, 0.9vh, 16px);
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.cockpit-floor-card {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: clamp(46px, 5.2vh, 56px);
  padding: clamp(8px, 0.8vh, 12px) clamp(10px, 0.7vw, 14px);
  color: #e7f7ff;
  border: 1px solid rgba(76, 145, 200, 0.22);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(12, 25, 40, 0.78), rgba(8, 17, 29, 0.9));
  cursor: pointer;
  transition: all 0.2s ease;
}

.cockpit-floor-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  font-size: 12px;
  color: rgba(180, 225, 255, 0.8);
}

.cockpit-floor-card:hover,
.cockpit-floor-card.is-active {
  transform: translateY(-1px);
  border-color: rgba(85, 206, 255, 0.42);
  background: linear-gradient(180deg, rgba(13, 46, 75, 0.92), rgba(7, 24, 42, 0.94));
  box-shadow: 0 12px 24px rgba(7, 28, 52, 0.32);
}

.cockpit-floor-card__code {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 20px;
  flex-shrink: 0;
  padding: 0 8px;
  font-size: 11px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.08em;
  white-space: nowrap;
  color: rgba(135, 224, 255, 0.72);
  border-radius: 999px;
  background: rgba(37, 98, 179, 0.18);
}

.cockpit-floor-card__name {
  flex: 1;
  min-width: 0;
  font-size: clamp(12px, 0.72vw, 13px);
  font-weight: 600;
  line-height: 1.2;
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cockpit-list {
  display: flex;
  flex-direction: column;
  gap: clamp(8px, 0.6vh, 12px);
  flex: 1;
  padding: 0 clamp(12px, 0.9vw, 16px) clamp(12px, 0.9vh, 16px);
  min-height: 0;
}

.cockpit-list--device {
  overflow-y: auto;
  overflow-x: hidden;
}

.cockpit-list--alert {
  overflow-y: auto;
  overflow-x: hidden;
}

.cockpit-device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: clamp(52px, 6.3vh, 68px);
  padding: clamp(10px, 0.9vh, 14px) clamp(12px, 0.8vw, 16px);
  border: 1px solid rgba(87, 150, 187, 0.16);
  border-radius: 18px;
  background: linear-gradient(90deg, rgba(21, 36, 59, 0.64), rgba(12, 22, 39, 0.72));
}

.cockpit-device-item__main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.cockpit-device-item__meta {
  min-width: 0;
}

.cockpit-device-item__name {
  font-size: clamp(13px, 0.75vw, 14px);
  font-weight: 600;
  color: #f5fbff;
}

.cockpit-device-item__location {
  margin-top: 5px;
  font-size: clamp(11px, 0.65vw, 12px);
  color: rgba(190, 214, 232, 0.62);
}

.cockpit-status-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: rgba(138, 156, 174, 0.76);
}

.cockpit-status-dot.is-online {
  background: #34d399;
  box-shadow: 0 0 12px rgba(52, 211, 153, 0.6);
}

.cockpit-status-chip {
  flex-shrink: 0;
  padding: 0;
  font-size: 12px;
  font-weight: 700;
  color: rgba(198, 223, 242, 0.72);
  border: none;
  background: transparent;
}

.cockpit-status-chip.is-online {
  color: #b6ffd8;
}

.cockpit-alert-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  width: 100%;
  min-height: clamp(56px, 6.7vh, 74px);
  padding: clamp(10px, 0.9vh, 14px) clamp(12px, 0.8vw, 16px);
  color: #f4fbff;
  border: 1px solid rgba(255, 194, 101, 0.16);
  border-radius: 18px;
  background: linear-gradient(90deg, rgba(26, 34, 57, 0.62), rgba(15, 21, 38, 0.76));
  cursor: pointer;
  transition: all 0.2s ease;
}

.cockpit-alert-item:hover {
  transform: translateY(-1px);
  border-color: rgba(255, 194, 101, 0.34);
  background: rgba(47, 29, 8, 0.42);
}

.cockpit-alert-item__main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.cockpit-alert-item__level {
  flex-shrink: 0;
  min-width: 58px;
  padding: 6px 9px;
  font-size: 10px;
  font-weight: 700;
  color: #ffd685;
  border-radius: 14px;
  background: rgba(77, 50, 10, 0.62);
  text-align: center;
}

.cockpit-alert-item__meta {
  min-width: 0;
}

.cockpit-alert-item__title {
  font-size: clamp(13px, 0.75vw, 14px);
  font-weight: 600;
}

.cockpit-alert-item__location {
  margin-top: 5px;
  font-size: clamp(11px, 0.65vw, 12px);
  color: rgba(213, 224, 236, 0.64);
}

.cockpit-alert-item__side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.cockpit-alert-item__status {
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
  color: #ffb6b6;
  border-radius: 999px;
  background: rgba(91, 17, 17, 0.54);
}

.cockpit-alert-item__status.is-handled {
  color: rgba(196, 213, 226, 0.72);
  background: rgba(31, 43, 54, 0.62);
}

.cockpit-alert-item__time {
  font-size: 12px;
  color: rgba(207, 222, 236, 0.58);
}

.cockpit-panel--stage {
  display: flex;
  flex-direction: column;
  padding: clamp(14px, 1vh, 20px);
  border-color: rgba(92, 187, 255, 0.24);
  background:
    radial-gradient(circle at 50% 8%, rgba(52, 127, 255, 0.16), transparent 36%),
    radial-gradient(circle at 50% 100%, rgba(24, 78, 170, 0.2), transparent 44%),
    linear-gradient(180deg, rgba(8, 18, 33, 0.98), rgba(5, 12, 22, 0.98));
  box-shadow:
    inset 0 1px 0 rgba(186, 225, 255, 0.12),
    0 26px 60px rgba(4, 10, 24, 0.34);
}

.cockpit-stage-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.cockpit-stage-head__content {
  max-width: 760px;
}

.cockpit-stage-head__meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.cockpit-stage-pill {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 14px;
  font-size: 12px;
  font-weight: 700;
  color: #f2fbff;
  border: 1px solid rgba(103, 193, 255, 0.18);
  border-radius: 999px;
  background: rgba(8, 23, 37, 0.74);
}

.cockpit-stage-pill.is-cyan {
  color: #bdf3ff;
}

.cockpit-stage-frame {
  position: relative;
  margin-top: 14px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.cockpit-stage-three {
  height: 100%;
}

.cockpit-stage-footer {
  position: absolute;
  right: 16px;
  bottom: 12px;
  left: 16px;
  z-index: 4;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  pointer-events: none;
}

.cockpit-stage-footer__chips {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1 1 0;
  max-width: 28%;
}

.cockpit-stage-footer__chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  font-size: 11px;
  font-weight: 700;
  color: #dcefff;
  border-radius: 999px;
  background: rgba(10, 23, 39, 0.82);
}

.cockpit-stage-footer__chip.is-online {
  color: #7ef0b6;
}

.cockpit-stage-footer__chip.is-alert {
  color: #ffd37b;
}

.cockpit-stage-footer__chip.is-video {
  color: #8ad7ff;
}

.cockpit-stage-footer__link {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  font-size: 11px;
  color: rgba(202, 227, 246, 0.72);
  border: 1px solid rgba(87, 154, 212, 0.16);
  border-radius: 999px;
  background: rgba(8, 20, 35, 0.7);
  white-space: nowrap;
}

.cockpit-stage-footer__actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  pointer-events: auto;
  overflow: hidden;
  border: 1px solid rgba(79, 141, 204, 0.18);
  border-radius: 18px;
  background: rgba(7, 18, 32, 0.92);
  box-shadow: 0 12px 24px rgba(3, 8, 18, 0.26);
}

.cockpit-stage-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 108px;
  min-height: 50px;
  padding: 0 18px;
  color: #e7f6ff;
  border: none;
  border-right: 1px solid rgba(79, 141, 204, 0.14);
  background: transparent;
  cursor: pointer;
  transition: background 0.2s ease;
}

.cockpit-stage-action:last-child {
  border-right: none;
}

.cockpit-stage-action:hover {
  background: rgba(21, 53, 92, 0.52);
}

.cockpit-stage-action.is-secondary {
  color: #f5fbff;
}

.cockpit-stage-action.is-disabled {
  color: rgba(195, 214, 230, 0.46);
}

.cockpit-stage-action span:last-child {
  font-size: 12px;
  font-weight: 600;
}

.cockpit-stage-frame :deep(.factory-three-stage) {
  border: 1px solid rgba(95, 180, 235, 0.16);
  box-shadow:
    inset 0 1px 0 rgba(164, 222, 255, 0.08),
    0 16px 40px rgba(4, 10, 24, 0.28);
}

.overview-primary-video {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: clamp(10px, 0.9vh, 16px);
  width: calc(100% - 40px);
  margin: 0 20px;
  padding: 0;
  color: #f7fbff;
  background: transparent;
  border: none;
  cursor: pointer;
}

.overview-primary-video__screen {
  position: relative;
  overflow: hidden;
  flex: 1;
  min-height: 0;
  padding: clamp(12px, 1vh, 18px) clamp(12px, 1vw, 18px) clamp(10px, 0.9vh, 16px);
  border: 1px solid rgba(74, 197, 255, 0.16);
  border-radius: 22px;
  background:
    radial-gradient(circle at 50% 8%, rgba(74, 197, 255, 0.16), transparent 42%),
    linear-gradient(180deg, rgba(9, 20, 35, 0.92), rgba(5, 12, 22, 0.96));
}

.overview-primary-video__screen-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(108, 176, 230, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(108, 176, 230, 0.08) 1px, transparent 1px);
  background-size: 28px 28px;
  opacity: 0.2;
}

.overview-primary-video__screen-header,
.overview-primary-video__screen-footer {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.overview-primary-video__screen-footer {
  padding: 8px 10px;
  margin-top: 12px;
  font-size: 12px;
  color: #f5fbff;
  border-radius: 12px;
  background: rgba(2, 8, 15, 0.64);
  backdrop-filter: blur(8px);
}

.overview-primary-video__screen-title {
  padding: 4px 8px;
  font-size: 11px;
  letter-spacing: 0.14em;
  color: rgba(215, 238, 255, 0.82);
  border-radius: 999px;
  background: rgba(6, 17, 29, 0.56);
}

.overview-primary-video__screen-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  font-size: 11px;
  color: rgba(198, 217, 233, 0.8);
  border-radius: 999px;
  background: rgba(6, 17, 29, 0.56);
}

.overview-primary-video__screen-status.is-online {
  color: #d5fff2;
}

.overview-primary-video__screen-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.8);
}

.overview-primary-video__screen-status.is-online .overview-primary-video__screen-dot {
  background: #22c55e;
  box-shadow: 0 0 12px rgba(34, 197, 94, 0.7);
}

.overview-primary-video__screen-body {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: clamp(112px, 16vh, 192px);
}

.overview-primary-video__screen-icon {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: clamp(60px, 5vw, 96px);
  height: clamp(60px, 5vw, 96px);
  font-size: clamp(26px, 2.2vw, 42px);
  color: #8ae8ff;
  border: 1px solid rgba(111, 210, 255, 0.26);
  border-radius: 28px;
  background: rgba(9, 27, 39, 0.78);
  box-shadow: 0 0 40px rgba(74, 197, 255, 0.22);
}

.overview-primary-video__screen-icon::after {
  position: absolute;
  inset: -18px;
  content: '';
  border-radius: 999px;
  background: radial-gradient(circle, rgba(72, 187, 255, 0.14), transparent 68%);
}

.overview-primary-video__content {
  padding: 0 2px 2px;
  text-align: left;
}

.overview-primary-video__eyebrow {
  font-size: 11px;
  letter-spacing: 0.12em;
  color: rgba(152, 212, 255, 0.72);
}

.overview-primary-video__name {
  margin-top: 6px;
  font-size: clamp(15px, 0.95vw, 18px);
  font-weight: 700;
}

.overview-primary-video__location {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(196, 219, 239, 0.7);
}

.overview-primary-video__hint {
  margin-top: 10px;
  font-size: 12px;
  line-height: 1.7;
  color: rgba(200, 223, 243, 0.64);
}

.overview-video-quick-list {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: clamp(8px, 0.6vh, 10px);
  padding: clamp(10px, 0.8vh, 14px) clamp(14px, 1vw, 20px) clamp(14px, 1vh, 20px);
  overflow-y: auto;
  overflow-x: hidden;
}

.overview-video-quick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: clamp(42px, 5vh, 56px);
  padding: clamp(8px, 0.7vh, 10px) clamp(10px, 0.8vw, 12px);
  text-align: left;
  color: #e9f8ff;
  border: 1px solid rgba(83, 170, 228, 0.14);
  border-radius: 14px;
  background: rgba(8, 22, 36, 0.72);
  cursor: pointer;
  transition: all 0.2s ease;
}

.overview-video-quick-item:hover {
  transform: translateY(-1px);
  border-color: rgba(97, 222, 255, 0.36);
  background: rgba(9, 31, 48, 0.88);
}

.overview-video-quick-item__dot {
  width: 10px;
  height: 10px;
  flex-shrink: 0;
  border-radius: 50%;
  background: rgba(148, 163, 184, 0.78);
}

.overview-video-quick-item__dot.is-online {
  background: #22c55e;
  box-shadow: 0 0 12px rgba(34, 197, 94, 0.6);
}

.overview-video-quick-item__main {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
}

.overview-video-quick-item__name {
  font-size: 13px;
  font-weight: 600;
  color: #f3fbff;
}

.overview-video-quick-item__location {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(190, 215, 235, 0.66);
}

.overview-video-quick-item__action {
  flex-shrink: 0;
  font-size: 12px;
  color: #7dd3fc;
}

.cockpit-energy-card {
  flex: 1;
  min-height: 0;
  padding: 0 12px 12px 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.overview-env-grid {
  display: grid;
  flex: 1;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(8px, 0.6vw, 12px);
  padding: 0 clamp(14px, 1vw, 20px);
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.overview-env-card {
  min-height: clamp(78px, 8vh, 106px);
  padding: clamp(10px, 0.9vh, 16px);
  border: 1px solid rgba(76, 174, 137, 0.16);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(12, 31, 36, 0.54), rgba(8, 18, 30, 0.74));
}

.overview-env-card__label {
  font-size: 12px;
  color: rgba(201, 223, 239, 0.68);
}

.overview-env-card__value {
  margin-top: 10px;
  font-size: clamp(18px, 1.15vw, 22px);
  font-weight: 700;
  color: #f6fcff;
}

.overview-env-card__unit {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 400;
  color: rgba(192, 217, 238, 0.68);
}

.overview-env-footer {
  display: flex;
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 12px;
  padding: 0 clamp(14px, 1vw, 20px) clamp(12px, 1vh, 18px);
  font-size: 12px;
  color: rgba(193, 216, 234, 0.7);
}

.overview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 0;
  margin: 0 16px 16px;
  font-size: 13px;
  color: rgba(194, 218, 237, 0.68);
  border: 1px dashed rgba(86, 135, 171, 0.24);
  border-radius: 16px;
  background: rgba(7, 17, 29, 0.42);
}

.cockpit-floor-grid::-webkit-scrollbar,
.cockpit-list--device::-webkit-scrollbar,
.cockpit-list--alert::-webkit-scrollbar {
  width: 6px;
}

.cockpit-floor-grid::-webkit-scrollbar-thumb,
.cockpit-list--device::-webkit-scrollbar-thumb,
.cockpit-list--alert::-webkit-scrollbar-thumb,
.overview-video-quick-list::-webkit-scrollbar-thumb,
.overview-env-grid::-webkit-scrollbar-thumb,
.cockpit-energy-card::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(111, 168, 209, 0.34);
}

.overview-video-quick-list::-webkit-scrollbar,
.overview-env-grid::-webkit-scrollbar,
.cockpit-energy-card::-webkit-scrollbar {
  width: 6px;
}

@media (max-width: 1919px) {
  .cockpit-grid {
    grid-template-columns: 256px minmax(0, 1.32fr) 278px;
  }
}

@media (max-width: 1599px) {
  .cockpit-grid {
    grid-template-columns: 230px minmax(0, 1.22fr) 248px;
  }

  .overview-env-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .cockpit-list--device .cockpit-device-item:nth-child(n + 6),
  .cockpit-list--alert .cockpit-alert-item:nth-child(n + 5),
  .overview-video-quick-list .overview-video-quick-item:nth-child(n + 4) {
    display: none;
  }
}

@media (max-width: 1439px) {
  .cockpit-grid {
    grid-template-columns: 220px minmax(0, 1.16fr) 232px;
  }
}

@media (max-width: 768px) {
  .cockpit-stage-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .cockpit-stage-footer__chips,
  .cockpit-stage-footer__actions {
    max-width: 100%;
    width: 100%;
  }

  .cockpit-stage-footer__actions {
    flex-wrap: wrap;
  }

  .cockpit-stage-action {
    flex: 1 1 calc(50% - 1px);
  }

  .cockpit-stage-head,
  .overview-primary-video__screen-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .cockpit-stage-frame :deep(.overview-scene__dock) {
    left: 16px;
    right: 16px;
  }
}
</style>
