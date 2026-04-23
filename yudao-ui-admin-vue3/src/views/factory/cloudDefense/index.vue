<script setup lang="ts">
import dayjs from 'dayjs'
import { getCloudDefenseOverview, type CloudDefenseOverviewData } from '@/api/factory/cloudDefense'
import { Icon } from '@/components/Icon'
import CloudDefenseDeviceList from './components/CloudDefenseDeviceList.vue'
import CloudDefenseInsightPanel from './components/CloudDefenseInsightPanel.vue'
import CloudDefenseMetricCards from './components/CloudDefenseMetricCards.vue'
import CloudDefenseModeTabs from './components/CloudDefenseModeTabs.vue'
import CloudDefenseSafetyPosturePanel from './components/CloudDefenseSafetyPosturePanel.vue'
import CloudDefenseTopologyCanvas from './components/CloudDefenseTopologyCanvas.vue'
import CloudDefenseZoneGrid from './components/CloudDefenseZoneGrid.vue'

defineOptions({ name: 'FactoryCloudDefense' })

const PERIMETER_MODE = 'perimeter-defense'

const loading = ref(false)
const loadError = ref('')
const overview = ref<CloudDefenseOverviewData | null>(null)
const activeModeCode = ref(PERIMETER_MODE)
const selectedAreaId = ref<number>()
const searchKeyword = ref('')
const currentTime = ref(dayjs())

let clockTimer: ReturnType<typeof setInterval> | undefined

const clampScore = (value: number) => {
  return Math.max(0, Math.min(100, Math.round(value)))
}

const metricCards = computed(() => {
  const metrics = overview.value?.metrics
  return [
    {
      key: 'defense',
      title: '防区状态',
      value: `${metrics?.armedAreaCount ?? 0}/${metrics?.totalAreaCount ?? 0} 已设防`,
      subValue: '周界联防',
      icon: 'ep:shield',
      accent: 'cyan' as const
    },
    {
      key: 'devices',
      title: '在线设备',
      value: `${metrics?.onlineDeviceCount ?? 0}/${metrics?.totalDeviceCount ?? 0}`,
      subValue: '实时设备',
      icon: 'ep:video-camera',
      accent: 'blue' as const
    },
    {
      key: 'alerts',
      title: '今日入侵告警',
      value: `${metrics?.todayAlertCount ?? 0} 次`,
      subValue: '实时态势',
      icon: 'ep:warning',
      accent: 'orange' as const
    },
    {
      key: 'score',
      title: '安全态势评分',
      value: `${metrics?.safetyScore ?? 0}分`,
      subValue: overview.value?.metrics?.safetyLevel || '待评估',
      icon: 'ep:data-analysis',
      accent: 'violet' as const
    }
  ]
})

const displayUpdatedAt = computed(() => {
  return overview.value?.updatedAt
    ? dayjs(overview.value.updatedAt).format('YYYY/MM/DD HH:mm')
    : dayjs().format('YYYY/MM/DD HH:mm')
})
const displayCurrentTime = computed(() => currentTime.value.format('YYYY/MM/DD HH:mm:ss'))
const normalizedSearchKeyword = computed(() => searchKeyword.value.trim().toLowerCase())

const deviceList = computed(() => overview.value?.deviceList || [])
const zoneList = computed(() => overview.value?.zoneList || [])
const topology = computed(() => overview.value?.topology)
const alarmingAreas = computed(() => zoneList.value.filter((item) => item.alarmingZoneCount > 0))
const totalAlarmingZoneCount = computed(() =>
  zoneList.value.reduce((total, item) => total + item.alarmingZoneCount, 0)
)
const armedRate = computed(() => {
  const metrics = overview.value?.metrics
  if (!metrics?.totalAreaCount) {
    return 0
  }
  return metrics.armedAreaCount / metrics.totalAreaCount
})
const onlineRate = computed(() => {
  const metrics = overview.value?.metrics
  if (!metrics?.totalDeviceCount) {
    return 0
  }
  return metrics.onlineDeviceCount / metrics.totalDeviceCount
})
const riskAreas = computed(() => {
  return [...zoneList.value].sort((left, right) => {
    if (right.alarmingZoneCount !== left.alarmingZoneCount) {
      return right.alarmingZoneCount - left.alarmingZoneCount
    }
    if (right.armedZoneCount !== left.armedZoneCount) {
      return right.armedZoneCount - left.armedZoneCount
    }
    return right.deviceCount - left.deviceCount
  })
})
const deviceTypeRanking = computed(() => {
  return Object.entries(
    deviceList.value.reduce<Record<string, number>>((acc, item) => {
      acc[item.typeLabel] = (acc[item.typeLabel] || 0) + 1
      return acc
    }, {})
  )
    .map(([label, count]) => ({ label, count }))
    .sort((left, right) => right.count - left.count)
})
const postureDimensions = computed(() => {
  const score = overview.value?.metrics?.safetyScore ?? 0
  const todayAlerts = overview.value?.metrics?.todayAlertCount ?? 0
  const alarmingAreaCount = alarmingAreas.value.length
  const alarmingZoneCount = totalAlarmingZoneCount.value
  return [
    {
      label: '周界防护',
      value: clampScore(score + armedRate.value * 10 - 3)
    },
    {
      label: '区域管控',
      value: clampScore(score - alarmingAreaCount * 2)
    },
    {
      label: '行为监测',
      value: clampScore(score - todayAlerts - 1)
    },
    {
      label: '应急响应',
      value: clampScore(score + onlineRate.value * 4 - alarmingZoneCount * 2)
    },
    {
      label: '设备状态',
      value: clampScore(score + onlineRate.value * 6 - 5)
    }
  ]
})
const posturePrediction = computed(() => {
  const highestRiskArea = riskAreas.value[0]
  const todayAlerts = overview.value?.metrics?.todayAlertCount ?? 0
  const stable = todayAlerts === 0 && alarmingAreas.value.length === 0
  return {
    trendText: `趋势: ${stable ? '上升' : '波动'}`,
    summary: stable
      ? '未来24小时安全态势平稳'
      : `未来24小时需重点关注${highestRiskArea?.name || '重点区域'}的联防联控`,
    predictionTitle: 'AI安全预测',
    predictionSummary: stable
      ? '未来24小时防空安全态势平稳'
      : `未来24小时存在持续波动风险，建议保持${highestRiskArea?.name || '重点区域'}视频联动与周界布防`,
    predictionTags: [
      `周界防护: ${Math.round(armedRate.value * 100)}%`,
      `区域管控: ${alarmingAreas.value.length > 0 ? '有预警' : '正常'}`,
      `异常事件: ${todayAlerts}起`
    ]
  }
})
const currentInsightPanel = computed(() => {
  const score = overview.value?.metrics?.safetyScore ?? 0
  const todayAlerts = overview.value?.metrics?.todayAlertCount ?? 0
  const highestRiskArea = riskAreas.value[0]
  switch (activeModeCode.value) {
    case 'area-intrusion':
      return {
        title: '区域入侵态势',
        subtitle: '基于真实告警、防区与空间区域聚合的联防看板',
        heroValue: `${alarmingAreas.value.length}`,
        heroLabel: '高风险区域',
        heroDescription: `当前共识别 ${todayAlerts} 起入侵告警，${highestRiskArea?.name || '周界防护区'} 风险优先级最高。`,
        stats: [
          { label: '总区域', value: `${zoneList.value.length}`, accent: 'blue' as const },
          { label: '告警区域', value: `${alarmingAreas.value.length}`, accent: 'orange' as const },
          { label: '联动设备', value: `${deviceList.value.length}`, accent: 'cyan' as const },
          { label: '设防覆盖', value: `${Math.round(armedRate.value * 100)}%`, accent: 'emerald' as const }
        ],
        highlights: riskAreas.value.slice(0, 4).map((item) => ({
          title: item.name,
          description: `${item.deviceCount}设备 / ${item.zoneCount}防区 / ${item.alarmingZoneCount}告警`,
          status: item.alarmingZoneCount > 0 ? '重点关注' : item.statusText
        }))
      }
    case 'behavior-analysis':
      return {
        title: '行为分析概览',
        subtitle: '基于设备能力标签与实时告警结果的行为侧洞察',
        heroValue: `${deviceTypeRanking.value.length}`,
        heroLabel: '行为识别类型',
        heroDescription: `当前在线设备 ${overview.value?.metrics?.onlineDeviceCount ?? 0} 台，行为侧风险以 ${highestRiskArea?.name || '重点区域'} 为主。`,
        stats: [
          { label: '在线设备', value: `${overview.value?.metrics?.onlineDeviceCount ?? 0}`, accent: 'blue' as const },
          { label: '识别类型', value: `${deviceTypeRanking.value.length}`, accent: 'cyan' as const },
          { label: '异常事件', value: `${todayAlerts}`, accent: 'orange' as const },
          { label: '安全评分', value: `${score}`, accent: 'emerald' as const }
        ],
        highlights: deviceTypeRanking.value.slice(0, 4).map((item) => ({
          title: item.label,
          description: `当前接入 ${item.count} 台设备参与行为分析视图`,
          status: item.count > 1 ? '高频能力' : '单点能力'
        }))
      }
    case 'track-tracing':
      return {
        title: '轨迹追踪面板',
        subtitle: '基于区域风险、点位分布与设备状态的联动追踪',
        heroValue: `${topology.value?.points.length || 0}`,
        heroLabel: '有效点位',
        heroDescription: `当前点位链路覆盖 ${zoneList.value.length} 个区域，重点追踪 ${highestRiskArea?.name || '周界防护区'} 的入侵轨迹。`,
        stats: [
          { label: '点位总数', value: `${topology.value?.points.length || 0}`, accent: 'blue' as const },
          { label: '告警点位', value: `${topology.value?.points.filter((item) => item.alarming).length || 0}`, accent: 'orange' as const },
          { label: '在线设备', value: `${overview.value?.metrics?.onlineDeviceCount ?? 0}`, accent: 'cyan' as const },
          { label: '追踪链路', value: `${riskAreas.value.length}`, accent: 'emerald' as const }
        ],
        highlights: (topology.value?.points || []).slice(0, 4).map((item) => ({
          title: `${item.name} 号点位`,
          description: `${item.online ? '在线' : '离线'} / ${item.alarming ? '告警中' : '无告警'} / 区域 ${zoneList.value.find((zone) => zone.areaId === item.areaId)?.name || '--'}`,
          status: item.alarming ? '轨迹焦点' : '可追踪'
        }))
      }
    default:
      return {
        title: '智能巡检概览',
        subtitle: '基于云防设备在线率与防区设防率的实时巡检面板',
        heroValue: `${overview.value?.metrics?.onlineDeviceCount ?? 0}/${overview.value?.metrics?.totalDeviceCount ?? 0}`,
        heroLabel: '巡检覆盖',
        heroDescription: `当前设备在线率 ${Math.round(onlineRate.value * 100)}%，已设防区域 ${overview.value?.metrics?.armedAreaCount ?? 0} 个。`,
        stats: [
          { label: '在线设备', value: `${overview.value?.metrics?.onlineDeviceCount ?? 0}`, accent: 'blue' as const },
          { label: '设防区域', value: `${overview.value?.metrics?.armedAreaCount ?? 0}`, accent: 'emerald' as const },
          { label: '异常防区', value: `${alarmingAreas.value.length}`, accent: 'orange' as const },
          { label: '安全评分', value: `${score}`, accent: 'cyan' as const }
        ],
        highlights: riskAreas.value.slice(0, 4).map((item) => ({
          title: item.name,
          description: `在线 ${item.onlineDeviceCount} / ${item.deviceCount}，健康状态 ${item.healthText}`,
          status: item.alarmingZoneCount > 0 ? '待复核' : '巡检正常'
        }))
      }
  }
})
const displayDevices = computed(() => {
  const source = [...deviceList.value]
  if (activeModeCode.value !== PERIMETER_MODE || !selectedAreaId.value) {
    return source
  }
  return source.sort((left, right) => {
    const leftPriority = Number(left.areaId === selectedAreaId.value)
    const rightPriority = Number(right.areaId === selectedAreaId.value)
    if (rightPriority !== leftPriority) {
      return rightPriority - leftPriority
    }
    if (Number(right.online) !== Number(left.online)) {
      return Number(right.online) - Number(left.online)
    }
    return left.id - right.id
  })
})
const matchesKeyword = (...values: Array<string | undefined | null>) => {
  if (!normalizedSearchKeyword.value) {
    return true
  }
  return values.some((item) => item?.toLowerCase().includes(normalizedSearchKeyword.value))
}
const searchedDevices = computed(() => {
  return displayDevices.value.filter((item) =>
    matchesKeyword(item.name, item.typeLabel, item.areaName, item.location, ...item.capabilityTags)
  )
})
const displayZones = computed(() => {
  return zoneList.value.filter((item) => matchesKeyword(item.name, item.statusText, item.healthText))
})
const headerTags = computed(() => {
  const metrics = overview.value?.metrics
  return [
    `区域 ${metrics?.totalAreaCount ?? 0}`,
    `设备 ${metrics?.totalDeviceCount ?? 0}`,
    `告警 ${metrics?.todayAlertCount ?? 0}`
  ]
})

const loadOverview = async () => {
  loading.value = true
  try {
    const response = await getCloudDefenseOverview()
    overview.value = response
    activeModeCode.value = response?.activeModeCode || PERIMETER_MODE
    selectedAreaId.value = response?.topology?.areas?.[0]?.id
    loadError.value = ''
  } catch (error) {
    console.error('加载立体化云防页面失败:', error)
    loadError.value = '立体化云防真实接口暂不可用，请检查后端服务、数据库或测试数据。'
  } finally {
    loading.value = false
  }
}

const handleSelectArea = (areaId: number) => {
  selectedAreaId.value = areaId
}

const handleSelectPoint = (pointId: number) => {
  const targetPoint = overview.value?.topology?.points.find((item) => item.id === pointId)
  if (targetPoint?.areaId) {
    selectedAreaId.value = targetPoint.areaId
  }
}

watch(activeModeCode, () => {
  if (!overview.value?.topology?.areas?.length) {
    selectedAreaId.value = undefined
    return
  }
  selectedAreaId.value = overview.value.topology.areas[0].id
})

onMounted(() => {
  clockTimer = window.setInterval(() => {
    currentTime.value = dayjs()
  }, 1000)
  loadOverview()
})

onUnmounted(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>

<template>
  <ContentWrap
    :body-style="{
      padding: '0',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      background: 'transparent'
    }"
    style="height: calc(100vh - var(--page-top-gap, 70px) + 10px); padding-top: calc(var(--page-top-gap, 70px) - 8px); margin-bottom: 0"
  >
    <div class="cloud-defense-page" v-loading="loading">
      <ElAlert
        v-if="loadError"
        :title="loadError"
        type="warning"
        :closable="false"
        show-icon
        class="cloud-defense-page__banner"
      />

      <div class="cloud-defense-page__toolbar">
        <div class="cloud-defense-page__title-wrap">
          <h1 class="cloud-defense-page__headline-title">立体化云防</h1>
          <p class="cloud-defense-page__headline-subtitle">周界防护 · 人车管控 · 应急指挥</p>
          <div class="cloud-defense-page__headline-tags">
            <span v-for="tag in headerTags" :key="tag">{{ tag }}</span>
          </div>
        </div>
        <label class="cloud-defense-page__search">
          <Icon icon="ep:search" />
          <input v-model.trim="searchKeyword" type="text" placeholder="搜索设备、区域、位置..." />
          <span>{{ searchedDevices.length }} 条</span>
        </label>
        <div class="cloud-defense-page__clock">{{ displayCurrentTime }}</div>
        <div class="cloud-defense-page__runtime">
          <span>{{ displayUpdatedAt }}</span>
          <em>真实接口数据</em>
        </div>
        <button class="cloud-defense-page__refresh" type="button" @click="loadOverview">
          <Icon icon="ep:refresh-right" />
          <span>刷新</span>
        </button>
      </div>

      <CloudDefenseMetricCards :items="metricCards" />

      <div class="cloud-defense-page__mode-bar">
        <CloudDefenseModeTabs v-model="activeModeCode" :modes="overview?.modes || []" />
      </div>

      <div class="cloud-defense-page__content">
        <div class="cloud-defense-page__main">
          <CloudDefenseTopologyCanvas
            v-if="activeModeCode === PERIMETER_MODE && topology"
            class="cloud-defense-page__topology"
            :title="topology.title"
            :legends="topology.legends"
            :areas="topology.areas"
            :points="topology.points"
            :active-area-id="selectedAreaId"
            @select-area="handleSelectArea"
            @select-point="handleSelectPoint"
          />

          <CloudDefenseSafetyPosturePanel
            v-else-if="activeModeCode === 'safety-posture'"
            :score="overview?.metrics?.safetyScore ?? 0"
            :level="overview?.metrics?.safetyLevel || '待评估'"
            :trend-text="posturePrediction.trendText"
            :summary="posturePrediction.summary"
            :dimensions="postureDimensions"
            :prediction-title="posturePrediction.predictionTitle"
            :prediction-summary="posturePrediction.predictionSummary"
            :prediction-tags="posturePrediction.predictionTags"
          />

          <CloudDefenseInsightPanel
            v-else
            :title="currentInsightPanel.title"
            :subtitle="currentInsightPanel.subtitle"
            :hero-value="currentInsightPanel.heroValue"
            :hero-label="currentInsightPanel.heroLabel"
            :hero-description="currentInsightPanel.heroDescription"
            :stats="currentInsightPanel.stats"
            :highlights="currentInsightPanel.highlights"
          />
        </div>

        <CloudDefenseDeviceList
          class="cloud-defense-page__devices"
          :devices="searchedDevices"
          :active-area-id="selectedAreaId"
        />
      </div>

      <template v-if="activeModeCode === PERIMETER_MODE">
        <CloudDefenseZoneGrid
          :zones="displayZones"
          :active-area-id="selectedAreaId"
          @select="handleSelectArea"
        />
      </template>

      <footer class="cloud-defense-page__footer">
        <span>最后刷新：{{ overview?.updatedAt ? dayjs(overview.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '--' }}</span>
        <span>所有页面数据均来自后端接口与数据库</span>
      </footer>
    </div>
  </ContentWrap>
</template>

<style scoped lang="scss">
.cloud-defense-page {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 5px;
  min-height: 0;
  padding: 6px 8px 4px;
  overflow: auto;
  background:
    radial-gradient(circle at top left, rgba(0, 162, 255, 0.14), transparent 22%),
    radial-gradient(circle at top right, rgba(110, 70, 255, 0.1), transparent 18%),
    linear-gradient(180deg, #06111f 0%, #071524 34%, #050c16 100%);
}

.cloud-defense-page__banner {
  margin-bottom: -2px;
}

.cloud-defense-page__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-bottom: 1px;
}

.cloud-defense-page__title-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  flex-shrink: 0;
}

.cloud-defense-page__headline-title {
  margin: 0;
  font-size: 18px;
  line-height: 1;
  font-weight: 700;
  color: #f3f8ff;
}

.cloud-defense-page__headline-subtitle {
  margin: 0;
  color: rgba(190, 214, 246, 0.68);
  font-size: 11px;
  white-space: nowrap;
}

.cloud-defense-page__headline-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.cloud-defense-page__headline-tags span {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 6px;
  color: rgba(191, 220, 255, 0.78);
  font-size: 10px;
  border: 1px solid rgba(84, 157, 255, 0.14);
  border-radius: 999px;
  background: rgba(7, 20, 42, 0.62);
}

.cloud-defense-page__search {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 220px;
  max-width: 360px;
  height: 30px;
  padding: 0 12px;
  border: 1px solid rgba(84, 157, 255, 0.12);
  border-radius: 10px;
  background: rgba(8, 18, 38, 0.78);
}

.cloud-defense-page__search :deep(svg) {
  color: rgba(168, 200, 247, 0.6);
  font-size: 14px;
}

.cloud-defense-page__search input {
  flex: 1;
  min-width: 0;
  color: #edf5ff;
  font-size: 12px;
  border: none;
  outline: none;
  background: transparent;
}

.cloud-defense-page__search input::placeholder {
  color: rgba(168, 200, 247, 0.42);
}

.cloud-defense-page__search span {
  flex-shrink: 0;
  color: rgba(120, 218, 166, 0.82);
  font-size: 11px;
}

.cloud-defense-page__clock {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 10px;
  color: rgba(222, 236, 255, 0.86);
  font-size: 11px;
  border: 1px solid rgba(84, 157, 255, 0.12);
  border-radius: 10px;
  background: rgba(8, 18, 38, 0.52);
  flex-shrink: 0;
}

.cloud-defense-page__runtime {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  height: 28px;
  border: 1px solid rgba(84, 157, 255, 0.14);
  border-radius: 10px;
  background: rgba(8, 18, 38, 0.7);
  flex-shrink: 0;
}

.cloud-defense-page__runtime span {
  color: rgba(222, 236, 255, 0.9);
  font-size: 11px;
}

.cloud-defense-page__runtime em {
  color: rgba(120, 218, 166, 0.86);
  font-size: 11px;
  font-style: normal;
}

.cloud-defense-page__refresh {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 28px;
  padding: 0 12px;
  color: #e4f0ff;
  font-size: 11px;
  border: 1px solid rgba(84, 157, 255, 0.18);
  border-radius: 10px;
  background: rgba(10, 22, 43, 0.92);
  flex-shrink: 0;
}

.cloud-defense-page__mode-bar {
  padding: 0 0 1px;
  border-bottom: 1px solid rgba(44, 65, 99, 0.72);
}

.cloud-defense-page__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 286px;
  gap: 8px;
  min-height: 0;
}

.cloud-defense-page__main {
  min-width: 0;
  min-height: 0;
}

.cloud-defense-page__topology {
  min-width: 0;
  min-height: 236px;
}

.cloud-defense-page__devices {
  min-height: 0;
}

.cloud-defense-page__footer {
  display: none;
}

@media (max-width: 1600px) {
  .cloud-defense-page__content {
    grid-template-columns: minmax(0, 1fr) 264px;
  }
}

@media (max-width: 1280px) {
  .cloud-defense-page__toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .cloud-defense-page__search {
    width: 100%;
    max-width: none;
  }

  .cloud-defense-page__footer,
  .cloud-defense-page__content {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
