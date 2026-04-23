<script setup lang="ts">
import type { FactoryDashboardOverviewData } from '@/api/factory/dashboard'
import { Icon } from '@/components/Icon'

defineOptions({ name: 'FactoryOverviewScene' })

type DeviceItem = FactoryDashboardOverviewData['deviceStatusList'][number]
type AlertItem = FactoryDashboardOverviewData['latestAlerts'][number]
type VideoSourceItem = FactoryDashboardOverviewData['videoSnapshot']['sources'][number]
type SceneInfo = FactoryDashboardOverviewData['scene']
type SceneAction = SceneInfo['actions'][number]

interface SceneHotspotLayout {
  top: string
  left: string
  width: string
  height: string
}

interface SceneHotspotItem {
  key: string
  name: string
  deviceCount: number
  onlineCount: number
  alertCount: number
  unhandledCount: number
  videoCount: number
  latestAlert?: AlertItem
  primaryVideo?: VideoSourceItem
  layout: SceneHotspotLayout
  tone: 'danger' | 'warning' | 'info'
}

type SceneAreaType = 'storage' | 'production' | 'auxiliary' | 'office' | 'unknown'

interface SceneTopologyZone {
  key: SceneAreaType
  label: string
  shortLabel: string
  tone: 'cyan' | 'pink' | 'emerald' | 'violet' | 'slate'
  top: string
  left: string
  width: string
  height: string
  hotspotCount: number
  deviceCount: number
  alertCount: number
}

interface SceneHotspotPreset {
  key: string
  keywords: RegExp
  order: number
  layout: SceneHotspotLayout
}

interface SceneHotspotAliasRule {
  pattern: RegExp
  replacement: string
}

const unknownHotspotLayouts: SceneHotspotLayout[] = [
  { top: '42%', left: '64%', width: '14%', height: '14%' },
  { top: '58%', left: '64%', width: '14%', height: '14%' },
  { top: '50%', left: '80%', width: '14%', height: '16%' }
]

const hotspotAliasRules: SceneHotspotAliasRule[] = [
  { pattern: /原料预处理区|原料预处理/i, replacement: '预处理' },
  { pattern: /成品仓库/i, replacement: '成品仓' },
  { pattern: /成品库/i, replacement: '成品仓' },
  { pattern: /原料仓库/i, replacement: '原料仓' },
  { pattern: /原材料仓|原材料库/i, replacement: '原料仓' },
  { pattern: /灌装区/i, replacement: '灌装' },
  { pattern: /制作区/i, replacement: '制作' },
  { pattern: /包装区/i, replacement: '包装' },
  { pattern: /实验室|化验室/i, replacement: '实验' },
  { pattern: /办公楼|办公区|行政楼|行政区/i, replacement: '办公' },
  { pattern: /研发楼|研发区|研发中心/i, replacement: '研发' },
  { pattern: /宿舍楼|宿舍区/i, replacement: '宿舍' },
  { pattern: /食堂区/i, replacement: '食堂' },
  { pattern: /更衣室/i, replacement: '更衣' }
]

const props = defineProps<{
  selectedFloorName: string
  devices: DeviceItem[]
  alerts: AlertItem[]
  videoSources: VideoSourceItem[]
  scene?: SceneInfo | null
}>()

const emit = defineEmits<{
  (event: 'action-click', action: SceneAction): void
  (event: 'alert-click', alert: AlertItem): void
  (event: 'video-click', source: VideoSourceItem): void
  (event: 'reset-floor'): void
}>()

const hotspotLayouts: SceneHotspotLayout[] = [
  { top: '10%', left: '8%', width: '24%', height: '18%' },
  { top: '14%', left: '38%', width: '24%', height: '20%' },
  { top: '12%', left: '68%', width: '20%', height: '18%' },
  { top: '40%', left: '12%', width: '26%', height: '20%' },
  { top: '44%', left: '46%', width: '22%', height: '18%' },
  { top: '66%', left: '20%', width: '28%', height: '18%' }
]

const sceneSummary = computed(() => {
  const deviceCount = props.devices.length
  const onlineDeviceCount = props.devices.filter((item) => item.online).length
  const unhandledAlertCount = props.alerts.filter((item) => !item.handled).length
  const videoCount = props.videoSources.length
  return [
    { label: '热点区域', value: rawHotspotList.value.length, tone: 'cyan' },
    { label: '在线设备', value: onlineDeviceCount, tone: 'emerald' },
    { label: '未处理告警', value: unhandledAlertCount, tone: 'amber' },
    { label: '可联动视频', value: videoCount || deviceCount, tone: 'violet' }
  ] as const
})

const resolveAreaKey = (value?: string | null, fallback?: string | null) => {
  const normalizedValue = `${value || fallback || '未标记区域'}`.trim()
  return normalizedValue || '未标记区域'
}

const normalizeAreaName = (value: string) => {
  const normalizedValue = value
    .replace(/[（(][^）)]*[）)]/g, '')
    .replace(/^[A-Z]\d+[-_/]/i, '')
    .replace(/^\d+F[-_/]?/i, '')
    .replace(/^F\d+[-_/]?/i, '')
    .replace(/^[一二三四五六七八九十\d]+层[-_/]?/i, '')
    .replace(/^[A-Z]栋[-_/]?/i, '')
    .replace(/入口|出口|全景|工位\d+|点位\d+|区域[A-Z]?$/gi, '')
    .replace(/[-_/]/g, '')
    .trim()

  const matchedAliasRule = hotspotAliasRules.find((rule) => rule.pattern.test(normalizedValue))
  if (matchedAliasRule) {
    return normalizedValue.replace(matchedAliasRule.pattern, matchedAliasRule.replacement).trim()
  }
  return normalizedValue
}

const resolveAreaType = (name: string): SceneAreaType => {
  if (/仓|仓库|成品|原料/i.test(name)) {
    return 'storage'
  }
  if (/制作|灌装|包装|生产|车间|预处理/i.test(name)) {
    return 'production'
  }
  if (/实验|缓冲|一更|二更|更衣|洁净|辅助/i.test(name)) {
    return 'auxiliary'
  }
  if (/办公|研发|宿舍|食堂|行政/i.test(name)) {
    return 'office'
  }
  return 'unknown'
}

const topologyZonePreset: Record<
  SceneAreaType,
  Omit<SceneTopologyZone, 'hotspotCount' | 'deviceCount' | 'alertCount'>
> = {
  storage: {
    key: 'storage',
    label: '仓储区',
    shortLabel: '仓储',
    tone: 'cyan',
    top: '8%',
    left: '5%',
    width: '24%',
    height: '78%'
  },
  production: {
    key: 'production',
    label: '生产区',
    shortLabel: '生产',
    tone: 'pink',
    top: '18%',
    left: '28%',
    width: '44%',
    height: '56%'
  },
  auxiliary: {
    key: 'auxiliary',
    label: '辅助区',
    shortLabel: '辅助',
    tone: 'emerald',
    top: '68%',
    left: '28%',
    width: '32%',
    height: '16%'
  },
  office: {
    key: 'office',
    label: '办公配套区',
    shortLabel: '办公',
    tone: 'violet',
    top: '10%',
    left: '74%',
    width: '20%',
    height: '74%'
  },
  unknown: {
    key: 'unknown',
    label: '其他区域',
    shortLabel: '其他',
    tone: 'slate',
    top: '38%',
    left: '62%',
    width: '18%',
    height: '22%'
  }
}

const hotspotPresetList: SceneHotspotPreset[] = [
  {
    key: 'finished-goods',
    keywords: /成品仓|成品仓库/i,
    order: 1,
    layout: { top: '6%', left: '6%', width: '20%', height: '16%' }
  },
  {
    key: 'packaging',
    keywords: /包装/i,
    order: 2,
    layout: { top: '26%', left: '6%', width: '20%', height: '18%' }
  },
  {
    key: 'making',
    keywords: /制作|配料|乳化/i,
    order: 3,
    layout: { top: '40%', left: '30%', width: '38%', height: '20%' }
  },
  {
    key: 'filling',
    keywords: /灌装/i,
    order: 4,
    layout: { top: '16%', left: '30%', width: '38%', height: '18%' }
  },
  {
    key: 'raw-materials',
    keywords: /原料仓|原料仓库/i,
    order: 5,
    layout: { top: '60%', left: '6%', width: '20%', height: '22%' }
  },
  {
    key: 'pre-treatment',
    keywords: /预处理/i,
    order: 6,
    layout: { top: '62%', left: '30%', width: '14%', height: '18%' }
  },
  {
    key: 'warehouse-generic',
    keywords: /仓储区|仓库区域|物资仓|仓库/i,
    order: 7,
    layout: { top: '44%', left: '6%', width: '20%', height: '14%' }
  },
  {
    key: 'changing-room1',
    keywords: /一更/i,
    order: 8,
    layout: { top: '82%', left: '30%', width: '9%', height: '10%' }
  },
  {
    key: 'changing-room2',
    keywords: /二更/i,
    order: 9,
    layout: { top: '82%', left: '40%', width: '9%', height: '10%' }
  },
  {
    key: 'buffer',
    keywords: /缓冲/i,
    order: 10,
    layout: { top: '82%', left: '50%', width: '9%', height: '10%' }
  },
  {
    key: 'lab',
    keywords: /实验|化验/i,
    order: 11,
    layout: { top: '82%', left: '60%', width: '15%', height: '10%' }
  },
  {
    key: 'changing-room-generic',
    keywords: /更衣/i,
    order: 12,
    layout: { top: '82%', left: '34%', width: '20%', height: '10%' }
  },
  {
    key: 'workshop-generic',
    keywords: /生产车间|车间[A-Z0-9]?/i,
    order: 13,
    layout: { top: '28%', left: '30%', width: '38%', height: '14%' }
  },
  {
    key: 'office',
    keywords: /办公/i,
    order: 14,
    layout: { top: '70%', left: '76%', width: '18%', height: '18%' }
  },
  {
    key: 'rd',
    keywords: /研发/i,
    order: 15,
    layout: { top: '42%', left: '76%', width: '18%', height: '18%' }
  },
  {
    key: 'dormitory',
    keywords: /宿舍/i,
    order: 16,
    layout: { top: '16%', left: '76%', width: '8%', height: '18%' }
  },
  {
    key: 'canteen',
    keywords: /食堂/i,
    order: 17,
    layout: { top: '16%', left: '86%', width: '8%', height: '18%' }
  }
]

const resolveHotspotPreset = (name: string) => {
  const normalizedName = normalizeAreaName(name)
  return hotspotPresetList.find((preset) => preset.keywords.test(normalizedName))
}

const resolveHotspotTone = (item: Omit<SceneHotspotItem, 'layout' | 'tone'>) => {
  if (item.unhandledCount) {
    return 'danger'
  }
  if (item.videoCount) {
    return 'info'
  }
  if (item.deviceCount) {
    return 'warning'
  }
  return 'info'
}

const sortHotspotByPriority = (
  left: Omit<SceneHotspotItem, 'layout' | 'tone'>,
  right: Omit<SceneHotspotItem, 'layout' | 'tone'>
) => {
  const leftPreset = resolveHotspotPreset(left.name)
  const rightPreset = resolveHotspotPreset(right.name)
  return (
    (leftPreset?.order ?? Number.MAX_SAFE_INTEGER) - (rightPreset?.order ?? Number.MAX_SAFE_INTEGER) ||
    right.unhandledCount - left.unhandledCount ||
    right.alertCount - left.alertCount ||
    right.videoCount - left.videoCount ||
    right.deviceCount - left.deviceCount
  )
}

const buildMergedHotspot = (
  key: string,
  name: string,
  hotspots: Array<Omit<SceneHotspotItem, 'layout' | 'tone'>>,
  layout: SceneHotspotLayout
): SceneHotspotItem => {
  const sortedHotspots = [...hotspots].sort(sortHotspotByPriority)
  const primaryHotspot = sortedHotspots[0]
  const mergedHotspot = hotspots.reduce(
    (accumulator, hotspot) => {
      accumulator.deviceCount += hotspot.deviceCount
      accumulator.onlineCount += hotspot.onlineCount
      accumulator.alertCount += hotspot.alertCount
      accumulator.unhandledCount += hotspot.unhandledCount
      accumulator.videoCount += hotspot.videoCount
      return accumulator
    },
    {
      key,
      name,
      deviceCount: 0,
      onlineCount: 0,
      alertCount: 0,
      unhandledCount: 0,
      videoCount: 0,
      latestAlert: primaryHotspot?.latestAlert,
      primaryVideo: primaryHotspot?.primaryVideo
    } as Omit<SceneHotspotItem, 'layout' | 'tone'>
  )

  return {
    ...mergedHotspot,
    layout,
    tone: resolveHotspotTone(mergedHotspot)
  }
}

const buildHotspotMap = () => {
  const hotspotMap = new Map<string, Omit<SceneHotspotItem, 'layout' | 'tone'>>()

  const getHotspot = (name: string) => {
    if (!hotspotMap.has(name)) {
      hotspotMap.set(name, {
        key: name,
        name,
        deviceCount: 0,
        onlineCount: 0,
        alertCount: 0,
        unhandledCount: 0,
        videoCount: 0
      })
    }
    return hotspotMap.get(name)!
  }

  props.devices.forEach((device) => {
    const hotspot = getHotspot(normalizeAreaName(resolveAreaKey(device.location, device.nickname || device.name)))
    hotspot.deviceCount += 1
    if (device.online) {
      hotspot.onlineCount += 1
    }
  })

  props.alerts.forEach((alert) => {
    const hotspot = getHotspot(normalizeAreaName(resolveAreaKey(alert.location, alert.deviceName || alert.title)))
    hotspot.alertCount += 1
    if (!alert.handled) {
      hotspot.unhandledCount += 1
    }
    if (!hotspot.latestAlert || (alert.alarmTime || '') > (hotspot.latestAlert.alarmTime || '')) {
      hotspot.latestAlert = alert
    }
  })

  props.videoSources.forEach((source) => {
    const hotspot = getHotspot(normalizeAreaName(resolveAreaKey(source.location, source.name)))
    hotspot.videoCount += 1
    if (!hotspot.primaryVideo) {
      hotspot.primaryVideo = source
    }
  })

  return hotspotMap
}

const rawHotspotList = computed(() => {
  return Array.from(buildHotspotMap().values()).sort(sortHotspotByPriority)
})

const hotspotList = computed<SceneHotspotItem[]>(() => {
  const knownHotspots = rawHotspotList.value
    .filter((item) => resolveHotspotPreset(item.name))
    .map((item) => {
      const preset = resolveHotspotPreset(item.name)!
      return {
        ...item,
        layout: preset.layout,
        tone: resolveHotspotTone(item)
      }
    })

  const unknownHotspots = rawHotspotList.value.filter((item) => !resolveHotspotPreset(item.name))
  const visibleUnknownHotspots =
    unknownHotspots.length > unknownHotspotLayouts.length
      ? [
          ...unknownHotspots
            .slice(0, unknownHotspotLayouts.length - 1)
            .map((item, index) => ({
              ...item,
              layout: unknownHotspotLayouts[index],
              tone: resolveHotspotTone(item)
            })),
          buildMergedHotspot(
            'unknown-summary',
            `其他区域 ${unknownHotspots.length - (unknownHotspotLayouts.length - 1)} 处`,
            unknownHotspots.slice(unknownHotspotLayouts.length - 1),
            unknownHotspotLayouts[unknownHotspotLayouts.length - 1]
          )
        ]
      : unknownHotspots.map((item, index) => ({
          ...item,
          layout: unknownHotspotLayouts[index] || hotspotLayouts[index % hotspotLayouts.length],
          tone: resolveHotspotTone(item)
        }))

  return [...knownHotspots, ...visibleUnknownHotspots]
})

const topologyZones = computed<SceneTopologyZone[]>(() => {
  const zoneMetrics = new Map<SceneAreaType, SceneTopologyZone>()
  rawHotspotList.value.forEach((hotspot) => {
    const zoneKey = resolveAreaType(hotspot.name)
    const preset = topologyZonePreset[zoneKey]
    if (!zoneMetrics.has(zoneKey)) {
      zoneMetrics.set(zoneKey, {
        ...preset,
        hotspotCount: 0,
        deviceCount: 0,
        alertCount: 0
      })
    }
    const zone = zoneMetrics.get(zoneKey)!
    zone.hotspotCount += 1
    zone.deviceCount += hotspot.deviceCount
    zone.alertCount += hotspot.alertCount
  })
  return Array.from(zoneMetrics.values())
})

const topologyLegend = computed(() => {
  return topologyZones.value.map((zone) => ({
    key: zone.key,
    label: zone.label,
    tone: zone.tone,
    hotspotCount: zone.hotspotCount
  }))
})

const topologyFlow = computed(() => {
  const productionHotspots = rawHotspotList.value.filter((hotspot) => resolveAreaType(hotspot.name) === 'production')
  return productionHotspots
    .sort((left, right) => {
      return (
        (resolveHotspotPreset(left.name)?.order ?? Number.MAX_SAFE_INTEGER) -
        (resolveHotspotPreset(right.name)?.order ?? Number.MAX_SAFE_INTEGER)
      )
    })
    .slice(0, 4)
    .map((hotspot, index) => ({
      key: hotspot.key,
      label: hotspot.name,
      order: index + 1
    }))
})

const resolveHotspotDisplayName = (hotspot: SceneHotspotItem) => {
  if (hotspot.key === 'unknown-summary') {
    return '其他区域聚合'
  }
  const preset = resolveHotspotPreset(hotspot.name)
  if (!preset) {
    return hotspot.name
  }
  if (preset.key === 'warehouse-generic') {
    return '通用仓储区'
  }
  if (preset.key === 'changing-room-generic') {
    return '更衣缓冲区'
  }
  if (preset.key === 'workshop-generic') {
    return '通用车间区'
  }
  return hotspot.name
}

const resolveHotspotCopy = (hotspot: SceneHotspotItem) => {
  if (hotspot.key === 'unknown-summary') {
    return `聚合 ${hotspot.alertCount || hotspot.deviceCount || hotspot.videoCount} 项未命名区域信号`
  }
  const preset = resolveHotspotPreset(hotspot.name)
  if (!preset) {
    return hotspot.primaryVideo ? '待补充命名映射，已保留视频联动入口' : '待补充命名映射，已保留概况承接位'
  }
  if (preset.key === 'warehouse-generic') {
    return '仓储相关位置名称已归并到通用仓储承接位'
  }
  if (preset.key === 'changing-room-generic') {
    return '更衣室等辅助区域名称已归并到更衣缓冲承接位'
  }
  if (preset.key === 'workshop-generic') {
    return '车间类泛化名称已归并到通用车间承接位'
  }
  return hotspot.primaryVideo ? '已命中预设区域并保留视频联动入口' : '已命中预设区域并保留概况承接位'
}

const primaryActionLabel = (hotspot: SceneHotspotItem) => {
  if (hotspot.unhandledCount && hotspot.latestAlert) {
    return '处理告警'
  }
  if (hotspot.primaryVideo) {
    return '打开视频'
  }
  return '查看概况'
}

const handleHotspotClick = (hotspot: SceneHotspotItem) => {
  if (hotspot.unhandledCount && hotspot.latestAlert) {
    emit('alert-click', hotspot.latestAlert)
    return
  }
  if (hotspot.primaryVideo) {
    emit('video-click', hotspot.primaryVideo)
    return
  }
  emit('reset-floor')
}

const resolveActionIcon = (action: SceneAction) => {
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

const sceneActions = computed(() => props.scene?.actions || [])
</script>

<template>
  <div class="overview-scene">
    <div class="overview-scene__hero">
      <div class="overview-scene__badge">当前楼层 {{ selectedFloorName }}</div>
      <div class="overview-scene__headline">主视图区已切换为区域热点联动视图</div>
      <div class="overview-scene__subline">
        当前版本以真实位置、设备、告警与视频源聚合出热点区域，优先打通楼层筛选、异常关注与视频联动闭环。
      </div>

      <div class="overview-scene__stats">
        <div
          v-for="summary in sceneSummary"
          :key="summary.label"
          class="overview-scene__stat"
          :class="`overview-scene__stat--${summary.tone}`"
        >
          <span>{{ summary.label }}</span>
          <strong>{{ summary.value }}</strong>
        </div>
      </div>

      <div class="overview-scene__canvas">
        <div class="overview-scene__grid"></div>
        <div class="overview-scene__frame"></div>
        <div
          v-for="zone in topologyZones"
          :key="zone.key"
          class="overview-scene__zone"
          :class="`overview-scene__zone--${zone.tone}`"
          :style="{
            top: zone.top,
            left: zone.left,
            width: zone.width,
            height: zone.height
          }"
        >
          <div class="overview-scene__zone-label">{{ zone.label }}</div>
          <div class="overview-scene__zone-meta">
            <span>{{ zone.shortLabel }}</span>
            <span>热点 {{ zone.hotspotCount }}</span>
            <span>设备 {{ zone.deviceCount }}</span>
          </div>
        </div>
        <div v-if="topologyFlow.length" class="overview-scene__flow">
          <div
            v-for="item in topologyFlow"
            :key="item.key"
            class="overview-scene__flow-node"
          >
            <span class="overview-scene__flow-order">{{ item.order }}</span>
            <span class="overview-scene__flow-label">{{ item.label }}</span>
          </div>
        </div>
        <button
          v-for="hotspot in hotspotList"
          :key="hotspot.key"
          class="overview-scene__hotspot"
          :class="`overview-scene__hotspot--${hotspot.tone}`"
          :style="hotspot.layout"
          type="button"
          @click="handleHotspotClick(hotspot)"
        >
          <div class="overview-scene__hotspot-name">{{ resolveHotspotDisplayName(hotspot) }}</div>
          <div class="overview-scene__hotspot-copy">{{ resolveHotspotCopy(hotspot) }}</div>
          <div class="overview-scene__hotspot-meta">
            <span>设备 {{ hotspot.deviceCount }}</span>
            <span>告警 {{ hotspot.alertCount }}</span>
            <span>视频 {{ hotspot.videoCount }}</span>
          </div>
          <div class="overview-scene__hotspot-footer">
            <span v-if="hotspot.unhandledCount">未处理 {{ hotspot.unhandledCount }}</span>
            <span v-else-if="hotspot.onlineCount">在线 {{ hotspot.onlineCount }}</span>
            <span v-else>暂无异常</span>
            <span>{{ primaryActionLabel(hotspot) }}</span>
          </div>
        </button>
        <div v-if="!hotspotList.length" class="overview-scene__empty">
          当前楼层暂无可视化热点数据，已保留主视图区联动承接位。
        </div>
        <div class="overview-scene__dock">
          <button class="overview-scene__tool overview-scene__tool--secondary" type="button" @click="emit('reset-floor')">
            <Icon icon="ep:operation" />
            <span>全部楼层</span>
          </button>
          <button
            v-for="action in sceneActions"
            :key="action.key"
            class="overview-scene__tool"
            :class="{ 'is-disabled': !action.enabled }"
            type="button"
            @click="emit('action-click', action)"
          >
            <Icon :icon="resolveActionIcon(action)" />
            <span>{{ action.label }}</span>
          </button>
        </div>
      </div>
    </div>

    <div class="overview-scene__toolbar-note">
      已接入楼层筛选、热点定位与视频联动协议，后续可继续承接更精细的空间模型表达。
    </div>
    <div v-if="topologyLegend.length" class="overview-scene__legend">
      <span
        v-for="item in topologyLegend"
        :key="item.key"
        class="overview-scene__legend-chip"
        :class="`overview-scene__legend-chip--${item.tone}`"
      >
        {{ item.label }} · {{ item.hotspotCount }}
      </span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.overview-scene {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.overview-scene__hero {
  position: relative;
  min-height: 540px;
  padding: 28px;
  overflow: hidden;
  border: 1px solid rgba(98, 102, 255, 0.22);
  border-radius: 22px;
  background:
    radial-gradient(circle at top right, rgba(74, 87, 255, 0.24), transparent 32%),
    radial-gradient(circle at bottom left, rgba(34, 211, 238, 0.18), transparent 28%),
    linear-gradient(180deg, rgba(8, 16, 42, 0.98), rgba(7, 15, 29, 0.96));
}

.overview-scene__badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  font-size: 12px;
  color: #c8f4ff;
  border-radius: 999px;
  background: rgba(18, 72, 102, 0.68);
}

.overview-scene__headline {
  margin-top: 22px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  color: #f7fbff;
}

.overview-scene__subline {
  max-width: 640px;
  margin-top: 14px;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(203, 223, 244, 0.74);
}

.overview-scene__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.overview-scene__stat {
  min-width: 0;
  padding: 14px 16px;
  border: 1px solid rgba(88, 135, 196, 0.18);
  border-radius: 16px;
  background: rgba(8, 23, 39, 0.54);
}

.overview-scene__stat--cyan {
  border-color: rgba(74, 222, 255, 0.2);
}

.overview-scene__stat--emerald {
  border-color: rgba(52, 211, 153, 0.2);
}

.overview-scene__stat--amber {
  border-color: rgba(245, 158, 11, 0.2);
}

.overview-scene__stat--violet {
  border-color: rgba(167, 139, 250, 0.2);
}

.overview-scene__stat span {
  display: block;
  font-size: 12px;
  color: rgba(194, 220, 241, 0.68);
}

.overview-scene__stat strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
  color: #f6fcff;
}

.overview-scene__canvas {
  position: relative;
  min-height: 360px;
  margin-top: 22px;
  overflow: hidden;
  border: 1px solid rgba(86, 140, 196, 0.16);
  border-radius: 22px;
  background:
    radial-gradient(circle at top center, rgba(34, 211, 238, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(7, 20, 35, 0.94), rgba(5, 11, 22, 0.98));
}

.overview-scene__canvas::after {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 96px;
  content: '';
  background: linear-gradient(180deg, rgba(4, 10, 18, 0), rgba(4, 10, 18, 0.86));
  pointer-events: none;
}

.overview-scene__grid,
.overview-scene__frame {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.overview-scene__grid {
  background-image:
    linear-gradient(rgba(75, 119, 166, 0.12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(75, 119, 166, 0.12) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: linear-gradient(180deg, rgba(255, 255, 255, 0.75), transparent 92%);
}

.overview-scene__frame {
  inset: 18px;
  border: 1px solid rgba(102, 182, 255, 0.12);
  border-radius: 18px;
  box-shadow:
    inset 0 0 0 1px rgba(18, 67, 103, 0.2),
    inset 0 0 40px rgba(27, 88, 155, 0.08);
}

.overview-scene__zone {
  position: absolute;
  padding: 14px 16px;
  border: 1px solid rgba(118, 180, 230, 0.16);
  border-radius: 18px;
  background: rgba(8, 24, 39, 0.24);
  box-shadow: inset 0 1px 0 rgba(173, 229, 255, 0.05);
  pointer-events: none;
}

.overview-scene__zone--cyan {
  background: linear-gradient(180deg, rgba(34, 211, 238, 0.08), rgba(6, 37, 56, 0.1));
}

.overview-scene__zone--pink {
  background: linear-gradient(180deg, rgba(244, 114, 182, 0.1), rgba(60, 16, 37, 0.1));
}

.overview-scene__zone--emerald {
  background: linear-gradient(180deg, rgba(52, 211, 153, 0.08), rgba(10, 48, 36, 0.1));
}

.overview-scene__zone--violet {
  background: linear-gradient(180deg, rgba(167, 139, 250, 0.08), rgba(36, 23, 63, 0.1));
}

.overview-scene__zone--slate {
  background: linear-gradient(180deg, rgba(148, 163, 184, 0.08), rgba(30, 41, 59, 0.1));
}

.overview-scene__zone-label {
  font-size: 13px;
  font-weight: 700;
  color: rgba(233, 246, 255, 0.84);
}

.overview-scene__zone-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
  margin-top: 8px;
  font-size: 12px;
  color: rgba(190, 217, 237, 0.68);
}

.overview-scene__flow {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 58%;
  padding: 10px 12px;
  overflow: hidden;
  border: 1px solid rgba(96, 165, 250, 0.14);
  border-radius: 999px;
  background: rgba(6, 18, 30, 0.6);
  backdrop-filter: blur(8px);
}

.overview-scene__flow-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.overview-scene__flow-node:not(:last-child)::after {
  width: 20px;
  height: 1px;
  content: '';
  background: linear-gradient(90deg, rgba(103, 146, 177, 0.18), rgba(103, 146, 177, 0.5));
}

.overview-scene__flow-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 700;
  color: #08131f;
  border-radius: 50%;
  background: linear-gradient(180deg, #7dd3fc, #38bdf8);
}

.overview-scene__flow-label {
  max-width: 96px;
  overflow: hidden;
  font-size: 12px;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: rgba(223, 241, 255, 0.82);
}

.overview-scene__hotspot {
  position: absolute;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;
  padding: 14px 16px;
  color: #f7fbff;
  text-align: left;
  border: 1px solid rgba(106, 182, 255, 0.24);
  border-radius: 18px;
  background: rgba(7, 26, 45, 0.78);
  box-shadow:
    inset 0 1px 0 rgba(150, 220, 255, 0.08),
    0 16px 24px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.overview-scene__hotspot:hover {
  transform: translateY(-2px);
  border-color: rgba(120, 211, 255, 0.48);
  box-shadow:
    inset 0 1px 0 rgba(170, 231, 255, 0.14),
    0 18px 28px rgba(0, 0, 0, 0.24);
}

.overview-scene__hotspot--danger {
  border-color: rgba(251, 113, 133, 0.28);
  background: linear-gradient(180deg, rgba(55, 16, 25, 0.82), rgba(34, 11, 17, 0.86));
}

.overview-scene__hotspot--warning {
  border-color: rgba(245, 158, 11, 0.24);
  background: linear-gradient(180deg, rgba(53, 28, 8, 0.82), rgba(28, 16, 5, 0.86));
}

.overview-scene__hotspot--info {
  border-color: rgba(74, 222, 255, 0.24);
}

.overview-scene__hotspot-name {
  font-size: 15px;
  font-weight: 700;
  line-height: 1.4;
}

.overview-scene__hotspot-copy {
  font-size: 12px;
  line-height: 1.6;
  color: rgba(210, 229, 243, 0.68);
}

.overview-scene__hotspot-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: rgba(199, 223, 243, 0.72);
}

.overview-scene__hotspot-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: rgba(229, 243, 255, 0.84);
}

.overview-scene__empty {
  position: absolute;
  inset: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  font-size: 13px;
  color: rgba(194, 218, 237, 0.68);
  border: 1px dashed rgba(86, 135, 171, 0.24);
  border-radius: 18px;
  background: rgba(7, 17, 29, 0.42);
}

.overview-scene__dock {
  position: absolute;
  bottom: 20px;
  left: 50%;
  z-index: 2;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  width: calc(100% - 40px);
  transform: translateX(-50%);
}

.overview-scene__toolbar-note {
  font-size: 12px;
  line-height: 1.7;
  color: rgba(191, 216, 236, 0.68);
}

.overview-scene__legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.overview-scene__legend-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  font-size: 12px;
  color: rgba(218, 238, 255, 0.84);
  border: 1px solid rgba(103, 146, 177, 0.18);
  border-radius: 999px;
  background: rgba(8, 25, 40, 0.64);
}

.overview-scene__legend-chip--cyan {
  border-color: rgba(34, 211, 238, 0.24);
}

.overview-scene__legend-chip--pink {
  border-color: rgba(244, 114, 182, 0.24);
}

.overview-scene__legend-chip--emerald {
  border-color: rgba(52, 211, 153, 0.24);
}

.overview-scene__legend-chip--violet {
  border-color: rgba(167, 139, 250, 0.24);
}

.overview-scene__legend-chip--slate {
  border-color: rgba(148, 163, 184, 0.24);
}

.overview-scene__tool {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  color: #ebf7ff;
  border: 1px solid rgba(95, 159, 231, 0.2);
  border-radius: 999px;
  background: rgba(6, 18, 30, 0.82);
  box-shadow:
    inset 0 1px 0 rgba(152, 220, 255, 0.08),
    0 10px 22px rgba(0, 0, 0, 0.24);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    opacity 0.2s ease,
    background 0.2s ease;
}

.overview-scene__tool:hover {
  transform: translateY(-1px);
  border-color: rgba(120, 211, 255, 0.42);
  background: rgba(8, 29, 46, 0.92);
}

.overview-scene__tool.is-disabled {
  opacity: 0.72;
}

.overview-scene__tool--secondary {
  color: #c8efff;
  border-color: rgba(74, 222, 255, 0.18);
}

@media (max-width: 1200px) {
  .overview-scene__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .overview-scene__hero {
    min-height: auto;
  }

  .overview-scene__canvas {
    display: grid;
    gap: 12px;
    min-height: auto;
    padding: 18px;
  }

  .overview-scene__canvas::after {
    display: none;
  }

  .overview-scene__flow {
    position: relative;
    top: auto;
    right: auto;
    max-width: none;
    border-radius: 18px;
  }

  .overview-scene__grid,
  .overview-scene__frame {
    display: none;
  }

  .overview-scene__zone,
  .overview-scene__hotspot,
  .overview-scene__empty,
  .overview-scene__dock {
    position: relative;
    inset: auto;
    width: 100%;
    height: auto;
    transform: none;
  }
}

@media (max-width: 768px) {
  .overview-scene__stats {
    grid-template-columns: 1fr;
  }

  .overview-scene__headline {
    font-size: 22px;
  }
}
</style>
