<script setup lang="ts">
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getChannelPage, type IbmsChannelRespVO } from '@/api/iot/ibms/channel'
import { getLivePlayUrl, getPlaybackUrl as getZlmPlaybackUrl, stopStream, type PlayUrlRespVO } from '@/api/iot/video/zlm'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@/components/Icon'
import FactoryStreamPlayer from '@/components/FactoryDashboard/FactoryStreamPlayer.vue'
import {
  buildFactoryVideoLinkQuery,
  parseFactoryVideoLinkQuery
} from '@/composables/factory/useFactoryVideoLink'
import {
  adaptStreamPlayUrls,
  pickPreferredPlayUrl,
  pickPreferredProtocol
} from '@/composables/video/streamPlayUtils'
import type { VideoWallSource } from '@/composables/video/useVideoWallPlayback'
import FactoryVideoWall from '@/components/FactoryDashboard/FactoryVideoWall.vue'
import FactoryDashboardShell from '../components/FactoryDashboardShell.vue'

defineOptions({ name: 'FactoryDashboardVideo' })

type VideoFusionTab = 'live' | 'playback' | 'ai'
type VideoFusionViewMode = 'grid' | 'focus'

interface VideoFusionChannel {
  id: number
  sourceId: string
  deviceId?: number
  channelNo: number
  name: string
  location: string
  area: string
  status: string
  online: boolean
  level: 'high' | 'medium' | 'low'
  deviceName: string
  extra: Record<string, any>
}

const MAX_PAGE_SIZE = 100
const route = useRoute()
const router = useRouter()

route.meta.pathKey = true

const pageLoading = ref(false)
const streamLoading = ref(false)
const streamInfo = ref<PlayUrlRespVO | null>(null)
const lastStreamChannelId = ref<number>()
const channelList = ref<VideoFusionChannel[]>([])
const searchKeyword = ref('')
const selectedArea = ref('all')
const selectedChannelId = ref<number>()
const activeTab = ref<VideoFusionTab>('live')
const viewMode = ref<VideoFusionViewMode>('grid')
const showAiOverlay = ref(true)
const playbackStart = ref('')
const playbackEnd = ref('')

const linkContext = computed(() => parseFactoryVideoLinkQuery(route.query))

const preferredPlayUrl = computed(() => pickPreferredPlayUrl(streamInfo.value))
const preferredProtocol = computed(() => pickPreferredProtocol(streamInfo.value))
const hasPlayableStream = computed(() => {
  return Boolean(streamInfo.value?.wsFlvUrl || streamInfo.value?.webrtcUrl || streamInfo.value?.hlsUrl)
})

const normalizeRouteTab = () => {
  const routeTab = Array.isArray(route.query.tab) ? route.query.tab[0] : route.query.tab
  if (routeTab === 'ai') {
    return 'ai' as const
  }
  return linkContext.value.mode === 'playback' ? ('playback' as const) : ('live' as const)
}

const normalizeDateTimeInput = (value?: string, fallbackMinutes = 30) => {
  if (value) {
    const parsed = dayjs(value)
    if (parsed.isValid()) {
      return parsed.format('YYYY-MM-DDTHH:mm:ss')
    }
  }
  return dayjs().subtract(fallbackMinutes, 'minute').format('YYYY-MM-DDTHH:mm:ss')
}

const syncPlaybackRangeFromRoute = () => {
  playbackStart.value = normalizeDateTimeInput(linkContext.value.startTime, 30)
  playbackEnd.value = normalizeDateTimeInput(linkContext.value.endTime, -30)
  if (dayjs(playbackEnd.value).isBefore(dayjs(playbackStart.value))) {
    playbackEnd.value = dayjs(playbackStart.value).add(30, 'minute').format('YYYY-MM-DDTHH:mm:ss')
  }
}

const buildChannelExtra = (extra?: string) => {
  if (!extra) {
    return {}
  }
  try {
    return JSON.parse(extra)
  } catch {
    return {}
  }
}

const mapChannelRow = (row: IbmsChannelRespVO): VideoFusionChannel => {
  const extra = buildChannelExtra(row.extra)
  const online = row.status === 'online'
  return {
    id: row.id,
    sourceId: String(row.id),
    deviceId: row.deviceId,
    channelNo: row.channelNo,
    name: row.name || `通道 ${row.channelNo}`,
    location: row.space || row.deviceName || '未配置位置',
    area: row.space || '未分区',
    status: row.status || 'unknown',
    online,
    level: online ? 'high' : 'medium',
    deviceName: row.deviceName || `设备 ${row.deviceId || '--'}`,
    extra
  }
}

const fetchChannelPage = async () => {
  const result: VideoFusionChannel[] = []
  let pageNo = 1
  let total = 0
  do {
    const pageResult = await getChannelPage({
      pageNo,
      pageSize: MAX_PAGE_SIZE,
      business: 'security',
      typeCode: 'VT'
    })
    const pageData = (pageResult as any)?.list ? pageResult : (pageResult as any)?.data
    const list = pageData?.list || []
    total = Number(pageData?.total || 0)
    result.push(...list.map((item: IbmsChannelRespVO) => mapChannelRow(item)))
    if (!list.length || result.length >= total) {
      break
    }
    pageNo += 1
  } while (true)
  channelList.value = result
}

const areaOptions = computed(() => {
  return Array.from(new Set(channelList.value.map((item) => item.area).filter(Boolean)))
})

const filteredChannels = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  return channelList.value.filter((item) => {
    const matchArea = selectedArea.value === 'all' ? true : item.area === selectedArea.value
    const matchKeyword = keyword
      ? [item.name, item.location, item.deviceName, item.channelNo]
          .map((field) => String(field || '').toLowerCase())
          .some((field) => field.includes(keyword))
      : true
    return matchArea && matchKeyword
  })
})

const selectedChannel = computed(() => {
  return filteredChannels.value.find((item) => item.id === selectedChannelId.value) ||
    channelList.value.find((item) => item.id === selectedChannelId.value) ||
    filteredChannels.value[0] ||
    channelList.value[0]
})

const currentMode = computed(() => (activeTab.value === 'playback' ? 'playback' : 'live'))
const canCreatePlaybackStream = computed(
  () => Boolean(selectedChannel.value?.id && playbackStart.value && playbackEnd.value)
)
const canCreateStream = computed(() => {
  if (!selectedChannel.value?.id) {
    return false
  }
  if (activeTab.value === 'playback') {
    return canCreatePlaybackStream.value
  }
  return true
})

const statusText = computed(() => {
  if (!channelList.value.length) {
    return '当前未加载到视频通道'
  }
  if (selectedChannel.value?.deviceId) {
    return `当前联动设备：${selectedChannel.value.deviceId}`
  }
  return '等待上游联动参数'
})

const streamHintText = computed(() => {
  if (!selectedChannel.value?.deviceId) {
    return '未选设备'
  }
  if (activeTab.value === 'playback' && !canCreatePlaybackStream.value) {
    return '待补时间'
  }
  if (activeTab.value === 'ai') {
    return 'AI待接入'
  }
  return activeTab.value === 'playback' ? '待回放' : '待接流'
})

const onlineCount = computed(() => channelList.value.filter((item) => item.online).length)
const visibleChannels = computed(() => filteredChannels.value.slice(0, 12))
const showSearchToolbar = computed(() => activeTab.value === 'live')
const showPlaybackToolbar = computed(() => activeTab.value === 'playback')
const linkedSourceId = computed(() => Number(linkContext.value.sourceId || 0))
const playbackRecords = computed(() => {
  return filteredChannels.value.slice(0, 16).map((item, index) => ({
    ...item,
    reason: linkContext.value.alarmId && index === 0 ? '告警联动回放' : '设备录像回放',
    type: linkContext.value.alarmId && index === 0 ? '告警回放' : item.online ? '事件回放' : '人工巡检',
    timeRange: `${playbackStart.value.replace('T', ' ')} - ${playbackEnd.value.replace('T', ' ')}`
  }))
})
const aiRecords = computed(() => {
  return filteredChannels.value.slice(0, 16).map((item, index) => ({
    ...item,
    type:
      index % 4 === 0
        ? '人脸识别'
        : index % 4 === 1
          ? '车牌识别'
          : index % 4 === 2
            ? '行为分析'
            : '周界检测',
    status: preferredPlayUrl.value && selectedChannel.value?.id === item.id ? '识别链路已接通' : '待接入',
    summary: item.location
  }))
})

const videoWallSources = computed<VideoWallSource[]>(() => {
  const sources = visibleChannels.value.map((item) => ({
    id: item.id,
    name: item.name,
    location: item.location,
    level: item.level
  }))
  if (!sources.length && selectedChannel.value) {
    return [
      {
        id: selectedChannel.value.id,
        name: selectedChannel.value.name,
        location: selectedChannel.value.location,
        level: selectedChannel.value.level
      }
    ]
  }
  return sources
})

const updateSelectionByContext = () => {
  const target =
    channelList.value.find((item) => item.id === Number(linkContext.value.sourceId)) ||
    channelList.value.find((item) => item.deviceId === linkContext.value.deviceId) ||
    channelList.value.find((item) => item.id === selectedChannelId.value) ||
    channelList.value[0]
  selectedChannelId.value = target?.id
}

const syncRouteQuery = async () => {
  const target = selectedChannel.value
  const nextQuery = buildFactoryVideoLinkQuery({
    mode: currentMode.value,
    deviceId: target?.deviceId,
    alarmId: linkContext.value.alarmId,
    sourceId: target?.sourceId,
    sourceName: target?.name,
    location: target?.location,
    alarmTime: linkContext.value.alarmTime,
    startTime: currentMode.value === 'playback' ? playbackStart.value : undefined,
    endTime: currentMode.value === 'playback' ? playbackEnd.value : undefined
  }) as Record<string, string>

  if (activeTab.value === 'ai') {
    nextQuery.tab = 'ai'
  }

  const currentQuery = route.query
  const stableCurrentQuery = JSON.stringify(
    Object.keys(currentQuery)
      .sort()
      .reduce<Record<string, string>>((accumulator, key) => {
        const value = currentQuery[key]
        if (Array.isArray(value)) {
          if (value[0] !== undefined) {
            accumulator[key] = String(value[0])
          }
        } else if (value !== undefined) {
          accumulator[key] = String(value)
        }
        return accumulator
      }, {})
  )
  const stableNextQuery = JSON.stringify(
    Object.keys(nextQuery)
      .sort()
      .reduce<Record<string, string>>((accumulator, key) => {
        if (nextQuery[key] !== undefined) {
          accumulator[key] = nextQuery[key]
        }
        return accumulator
      }, {})
  )

  if (stableCurrentQuery !== stableNextQuery) {
    await router.replace({
      path: '/factory/video-fusion',
      query: nextQuery
    })
  }
}

const stopCurrentStream = async () => {
  const channelId = lastStreamChannelId.value
  streamInfo.value = null
  lastStreamChannelId.value = undefined
  if (!channelId) {
    return
  }
  try {
    await stopStream(channelId)
  } catch (error) {
    console.warn('停止视频流失败:', error)
  }
}

const loadSelectedStream = async (showSuccessMessage = false) => {
  await stopCurrentStream()
  if (!canCreateStream.value || !selectedChannel.value?.id) {
    return
  }

  streamLoading.value = true
  try {
    const rawStreamInfo =
      currentMode.value === 'playback'
        ? await getZlmPlaybackUrl(selectedChannel.value.id, playbackStart.value, playbackEnd.value)
        : await getLivePlayUrl(selectedChannel.value.id, viewMode.value === 'grid' ? 1 : 0)
    streamInfo.value = adaptStreamPlayUrls(rawStreamInfo)
    lastStreamChannelId.value = selectedChannel.value.id
    if (showSuccessMessage) {
      ElMessage.success(currentMode.value === 'playback' ? '回放流已刷新' : '实时流已刷新')
    }
  } catch (error) {
    console.error('加载视频流失败:', error)
    streamInfo.value = null
    ElMessage.warning(currentMode.value === 'playback' ? '回放流创建失败' : '实时流创建失败')
  } finally {
    streamLoading.value = false
  }
}

const loadChannels = async () => {
  pageLoading.value = true
  try {
    await fetchChannelPage()
    updateSelectionByContext()
  } catch (error) {
    console.error('加载视频通道失败:', error)
    ElMessage.warning('加载视频通道失败，请稍后重试')
  } finally {
    pageLoading.value = false
  }
}

const refreshStreams = async () => {
  if (!selectedChannel.value?.id) {
    ElMessage.warning('当前未选中可播放设备')
    return
  }
  if (activeTab.value === 'playback' && !canCreatePlaybackStream.value) {
    ElMessage.warning('当前回放模式缺少开始时间或结束时间')
    return
  }
  await loadSelectedStream(true)
}

const handleTabChange = async (tab: VideoFusionTab) => {
  activeTab.value = tab
  if (tab === 'playback' && dayjs(playbackEnd.value).isBefore(dayjs(playbackStart.value))) {
    playbackEnd.value = dayjs(playbackStart.value).add(30, 'minute').format('YYYY-MM-DDTHH:mm:ss')
  }
  await syncRouteQuery()
}

const handleChannelSelect = async (channel: VideoFusionChannel, nextViewMode?: VideoFusionViewMode) => {
  selectedChannelId.value = channel.id
  if (nextViewMode) {
    viewMode.value = nextViewMode
  }
  await syncRouteQuery()
}

const handleVideoWallSourceClick = async (source: VideoWallSource) => {
  const target = channelList.value.find((item) => item.id === Number(source.id))
  if (target) {
    await handleChannelSelect(target, 'focus')
  }
}

const handlePlaybackRecordPick = async (channel: VideoFusionChannel) => {
  await handleTabChange('playback')
  await handleChannelSelect(channel, 'focus')
}

const handleAiRecordPick = async (channel: VideoFusionChannel) => {
  await handleTabChange('ai')
  await handleChannelSelect(channel, 'focus')
}

const handleResetFilter = () => {
  searchKeyword.value = ''
  selectedArea.value = 'all'
}

watch(
  () => route.query,
  () => {
    activeTab.value = normalizeRouteTab()
    syncPlaybackRangeFromRoute()
    updateSelectionByContext()
  },
  { immediate: true }
)

watch(
  () => [selectedChannel.value?.id, activeTab.value, playbackStart.value, playbackEnd.value],
  () => {
    loadSelectedStream()
  }
)

watch(filteredChannels, (list) => {
  if (!list.length) {
    selectedChannelId.value = undefined
    stopCurrentStream()
    return
  }
  if (!list.some((item) => item.id === selectedChannelId.value)) {
    selectedChannelId.value = list[0]?.id
  }
})

onMounted(() => {
  loadChannels()
})

onBeforeUnmount(() => {
  stopCurrentStream()
})
</script>

<template>
  <FactoryDashboardShell
    title="智慧工厂视频融合"
    :status-text="statusText"
    hide-hero
    style="padding-top: max(14px, calc(var(--page-top-gap, 70px) - 2px))"
  >
    <div v-loading="pageLoading" class="video-fusion-page">
      <div class="video-fusion-page__topbar">
        <div class="video-fusion-page__topbar-main">
          <div class="video-fusion-tabs">
            <button
              class="video-fusion-tab"
              :class="{ 'is-active': activeTab === 'live' }"
              type="button"
              @click="handleTabChange('live')"
            >
              <Icon icon="ep:video-camera" />
              <span>实时视频</span>
            </button>
            <button
              class="video-fusion-tab"
              :class="{ 'is-active': activeTab === 'playback' }"
              type="button"
              @click="handleTabChange('playback')"
            >
              <Icon icon="ep:refresh-right" />
              <span>视频回放</span>
            </button>
            <button
              class="video-fusion-tab"
              :class="{ 'is-active': activeTab === 'ai' }"
              type="button"
              @click="handleTabChange('ai')"
            >
              <Icon icon="ep:monitor" />
              <span>AI识别</span>
            </button>
          </div>
          <div v-if="activeTab === 'live'" class="video-fusion-online-hint">
            在线：
            <span>{{ onlineCount }}</span>
            / {{ channelList.length || 0 }}
          </div>
        </div>

        <div class="video-fusion-page__topbar-actions">
          <label v-if="activeTab === 'live'" class="video-fusion-topbar-toggle">
            <input v-model="showAiOverlay" type="checkbox" />
            <span>AI叠加</span>
          </label>

          <div v-if="activeTab === 'live'" class="video-fusion-toolbar__group">
            <button
              class="video-fusion-ghost-button"
              :class="{ 'is-active': viewMode === 'grid' }"
              type="button"
              @click="viewMode = 'grid'"
            >
              <Icon icon="ep:grid" />
            </button>
            <button
              class="video-fusion-ghost-button"
              :class="{ 'is-active': viewMode === 'focus' }"
              type="button"
              @click="viewMode = 'focus'"
            >
              <Icon icon="ep:full-screen" />
            </button>
          </div>

          <button class="video-fusion-ghost-button" type="button" @click="refreshStreams">
            <Icon icon="ep:refresh-right" />
          </button>
          <button class="video-fusion-ghost-button" type="button">
            <Icon icon="ep:setting" />
          </button>
        </div>
      </div>

      <div v-if="showSearchToolbar || showPlaybackToolbar" class="video-fusion-toolbar">
        <template v-if="showSearchToolbar">
          <label class="video-fusion-search">
            <Icon icon="ep:search" />
            <input v-model="searchKeyword" type="text" placeholder="搜索摄像头..." />
          </label>

          <div class="video-fusion-toolbar__group">
            <select v-model="selectedArea" class="video-fusion-select">
              <option value="all">全部区域</option>
              <option v-for="item in areaOptions" :key="item" :value="item">
                {{ item }}
              </option>
            </select>
            <button class="video-fusion-ghost-button" type="button" @click="handleResetFilter">
              <Icon icon="ep:refresh-left" />
            </button>
          </div>
        </template>

        <template v-if="showPlaybackToolbar">
          <div class="video-fusion-playback-caption">
            <Icon icon="ep:timer" />
            <span>回放时间窗</span>
          </div>
        </template>

        <div v-if="activeTab === 'playback'" class="video-fusion-toolbar__group is-wide">
          <input v-model="playbackStart" class="video-fusion-datetime" type="datetime-local" step="1" />
          <span class="video-fusion-toolbar__separator">至</span>
          <input v-model="playbackEnd" class="video-fusion-datetime" type="datetime-local" step="1" />
        </div>

        <div class="video-fusion-toolbar__spacer"></div>

        <button v-if="activeTab === 'playback'" class="video-fusion-primary-button" type="button" @click="refreshStreams">
          <Icon icon="ep:refresh-right" />
          <span>刷新回放</span>
        </button>
      </div>

      <div class="video-fusion-body" :class="{ 'is-single-column': activeTab !== 'live' }">
        <section class="video-fusion-main" :class="{ 'is-live-mode': activeTab === 'live' }">
          <header v-if="activeTab !== 'live'" class="video-fusion-main__header">
            <div class="video-fusion-main__meta">
              <div class="video-fusion-main__title">
                {{
                  activeTab === 'playback'
                    ? '录像回放列表'
                    : activeTab === 'ai'
                      ? 'AI智能识别记录'
                      : selectedChannel?.name || '等待选择摄像头'
                }}
              </div>
              <div class="video-fusion-main__subtitle">
                {{
                  activeTab === 'playback'
                    ? '按真实通道与当前时间窗组织回放候选列表'
                    : activeTab === 'ai'
                      ? '基于真实点位承接 AI 模式，识别结果按接入状态展示'
                      : selectedChannel?.location || '请先从右侧选择摄像头或由上游联动进入'
                }}
              </div>
            </div>
            <div class="video-fusion-main__status-list">
              <span class="video-fusion-mini-tag is-dark">模式：{{ activeTab === 'live' ? '实时视频' : activeTab === 'playback' ? '视频回放' : 'AI识别' }}</span>
              <span class="video-fusion-mini-tag" :class="selectedChannel?.online ? 'is-success' : 'is-muted'">
                {{ selectedChannel?.online ? '在线' : '离线' }}
              </span>
              <span class="video-fusion-mini-tag is-cyan">
                {{ preferredPlayUrl ? preferredProtocol : '待接流' }}
              </span>
            </div>
          </header>

          <div v-if="selectedChannel" class="video-fusion-main__content">
            <div v-if="activeTab === 'live' && viewMode === 'focus'" class="video-fusion-focus">
              <div class="video-fusion-focus__player">
                <FactoryStreamPlayer
                  :ws-flv-url="streamInfo?.wsFlvUrl"
                  :webrtc-url="streamInfo?.webrtcUrl"
                  :hls-url="streamInfo?.hlsUrl"
                  :prefer-webrtc="activeTab !== 'playback'"
                  :empty-text="streamHintText"
                />
                <div class="video-fusion-focus__overlay">
                  <div class="video-fusion-focus__overlay-main">
                    <div class="video-fusion-focus__overlay-title">{{ selectedChannel.name }}</div>
                    <div class="video-fusion-focus__overlay-subtitle">
                      {{ selectedChannel.deviceName }} · {{ selectedChannel.location }}
                    </div>
                  </div>
                  <div v-if="activeTab === 'ai' && showAiOverlay" class="video-fusion-ai-panel">
                    <div class="video-fusion-ai-panel__title">AI识别链路</div>
                    <div class="video-fusion-ai-panel__text">当前已复用真实实时流，识别结果按接入状态展示</div>
                  </div>
                </div>
              </div>
              <div class="video-fusion-focus__wall">
                <FactoryVideoWall
                  title="联动视频墙"
                  current-view-name="视频融合速览"
                  :sources="videoWallSources.slice(0, 4)"
                  @source-click="handleVideoWallSourceClick"
                />
              </div>
            </div>

            <div v-else-if="activeTab === 'live'" class="video-fusion-grid">
              <button
                v-for="item in visibleChannels"
                :key="item.id"
                class="video-fusion-card"
                :class="{
                  'is-selected': selectedChannel?.id === item.id,
                  'is-offline': !item.online
                }"
                type="button"
                @click="handleChannelSelect(item)"
                @dblclick="handleChannelSelect(item, 'focus')"
              >
                <div class="video-fusion-card__media">
                  <FactoryStreamPlayer
                    v-if="selectedChannel?.id === item.id && hasPlayableStream"
                    :ws-flv-url="streamInfo?.wsFlvUrl"
                    :webrtc-url="streamInfo?.webrtcUrl"
                    :hls-url="streamInfo?.hlsUrl"
                    :prefer-webrtc="activeTab !== 'playback'"
                    :empty-text="streamHintText"
                  />
                  <div v-else class="video-fusion-card__placeholder">
                    <div class="video-fusion-card__placeholder-surface">
                      <div class="video-fusion-card__placeholder-icon">
                        <Icon icon="ep:video-camera" />
                      </div>
                    </div>
                    <div class="video-fusion-card__placeholder-hint">待接流</div>
                  </div>
                  <div class="video-fusion-card__chips">
                    <span
                      v-if="linkedSourceId && item.id === linkedSourceId"
                      class="video-fusion-mini-tag is-danger"
                    >
                      告警
                    </span>
                    <span
                      v-else-if="selectedChannel?.id === item.id"
                      class="video-fusion-mini-tag is-primary"
                    >
                      联动
                    </span>
                    <span
                      v-else-if="item.extra?.enableStatus === 1"
                      class="video-fusion-mini-tag is-warning"
                    >
                      巡检
                    </span>
                    <span
                      v-else-if="item.extra?.isMonitor === 1"
                      class="video-fusion-mini-tag is-violet"
                    >
                      监控
                    </span>
                  </div>
                  <div class="video-fusion-card__corner">
                    <span class="video-fusion-card__corner-chip" :class="item.online ? 'is-online' : 'is-offline'">
                      {{ item.online ? '在线' : '离线' }}
                    </span>
                    <span class="video-fusion-card__corner-icon">
                      <Icon icon="ep:video-camera" />
                    </span>
                  </div>
                </div>
                <div class="video-fusion-card__content">
                  <div class="video-fusion-card__summary">
                    <div class="video-fusion-card__name">{{ item.name }}</div>
                    <div class="video-fusion-card__meta">{{ item.location }}</div>
                    <span class="video-fusion-card__channel">CH-{{ item.channelNo }}</span>
                  </div>
                </div>
              </button>
            </div>

            <div v-else-if="activeTab === 'playback'" class="video-fusion-records">
              <button
                v-for="record in playbackRecords"
                :key="record.id"
                class="video-fusion-record"
                :class="{ 'is-active': selectedChannel?.id === record.id }"
                type="button"
                @click="handlePlaybackRecordPick(record)"
              >
                <div class="video-fusion-record__media">
                  <Icon icon="ep:video-camera" />
                </div>
                <div class="video-fusion-record__content">
                  <div class="video-fusion-record__title">{{ record.reason }}</div>
                  <div class="video-fusion-record__subtitle">{{ record.name }} ｜ {{ record.timeRange }}</div>
                </div>
                <div class="video-fusion-record__actions">
                  <span class="video-fusion-mini-tag" :class="record.type === '告警回放' ? 'is-danger' : record.type === '人工巡检' ? 'is-cyan' : 'is-success'">
                    {{ record.type }}
                  </span>
                  <span class="video-fusion-record__play">
                    <Icon icon="ep:video-play" />
                  </span>
                </div>
              </button>
            </div>

            <div v-else class="video-fusion-records">
              <button
                v-for="record in aiRecords"
                :key="record.id"
                class="video-fusion-record"
                :class="{ 'is-active': selectedChannel?.id === record.id }"
                type="button"
                @click="handleAiRecordPick(record)"
              >
                <div class="video-fusion-record__avatar" :class="`is-${record.type}`">
                  <span v-if="record.type === '人脸识别'">👤</span>
                  <span v-else-if="record.type === '车牌识别'">🚗</span>
                  <span v-else-if="record.type === '行为分析'">⚠️</span>
                  <span v-else>🛡️</span>
                </div>
                <div class="video-fusion-record__content">
                  <div class="video-fusion-record__title">{{ record.type }}</div>
                  <div class="video-fusion-record__subtitle">{{ record.name }} ｜ {{ record.summary }}</div>
                </div>
                <div class="video-fusion-record__actions">
                  <span class="video-fusion-mini-tag" :class="record.status === '识别链路已接通' ? 'is-success' : 'is-muted'">
                    {{ record.status }}
                  </span>
                  <span class="video-fusion-record__play">
                    <Icon icon="ep:view" />
                  </span>
                </div>
              </button>
            </div>
          </div>

          <ElEmpty v-else description="当前筛选条件下暂无可用摄像头" />
        </section>

        <aside v-if="activeTab === 'live'" class="video-fusion-sidebar">
          <div class="video-fusion-sidebar__header">
            <div>
              <div class="video-fusion-sidebar__title">摄像头列表</div>
              <div class="video-fusion-sidebar__subtitle">{{ filteredChannels.length }} 个摄像头</div>
            </div>
          </div>

          <div class="video-fusion-sidebar__list">
            <div
              v-for="item in filteredChannels"
              :key="item.id"
              class="video-fusion-sidebar-item"
              :class="{
                'is-active': selectedChannel?.id === item.id,
                'is-offline': !item.online
              }"
              @click="handleChannelSelect(item)"
            >
              <div class="video-fusion-sidebar-item__head">
                <div class="video-fusion-sidebar-item__dot" :class="{ 'is-online': item.online }"></div>
                <div class="video-fusion-sidebar-item__name">{{ item.name }}</div>
                <Icon
                  v-if="linkedSourceId && item.id === linkedSourceId"
                  icon="ep:warning"
                  class="video-fusion-sidebar-item__icon"
                />
              </div>
              <div class="video-fusion-sidebar-item__meta">{{ item.location }}</div>
              <div class="video-fusion-sidebar-item__tail">
                <span class="video-fusion-sidebar-item__type">CH-{{ item.channelNo }}</span>
                <span class="video-fusion-sidebar-item__action" @click.stop="handleChannelSelect(item, 'focus')">
                  弹窗
                  <Icon icon="ep:arrow-right" />
                </span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </FactoryDashboardShell>
</template>

<style scoped lang="scss">
.video-fusion-page {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  gap: 16px;
  padding: 10px 4px 0;
}

.video-fusion-page__topbar,
.video-fusion-toolbar,
.video-fusion-main,
.video-fusion-sidebar {
  border: 1px solid rgba(74, 124, 170, 0.2);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(9, 18, 32, 0.96), rgba(5, 12, 22, 0.96)),
    radial-gradient(circle at top left, rgba(39, 104, 235, 0.16), transparent 34%);
  box-shadow:
    inset 0 1px 0 rgba(164, 220, 255, 0.06),
    0 20px 40px rgba(0, 0, 0, 0.22);
}

.video-fusion-page__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 16px 18px;
}

.video-fusion-page__topbar-main {
  display: inline-flex;
  align-items: center;
  gap: 16px;
}

.video-fusion-page__topbar-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.video-fusion-online-hint {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  padding: 0 10px;
  font-size: 12px;
  color: rgba(191, 214, 233, 0.7);
  border: 1px solid rgba(76, 117, 164, 0.24);
  border-radius: 999px;
  background: rgba(10, 24, 40, 0.8);
}

.video-fusion-online-hint span {
  color: #4ade80;
  font-weight: 700;
}

.video-fusion-topbar-toggle {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 36px;
  padding: 0 12px;
  color: rgba(215, 233, 248, 0.76);
  border: 1px solid rgba(83, 139, 196, 0.22);
  border-radius: 12px;
  background: rgba(12, 24, 40, 0.84);
}

.video-fusion-tabs {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px;
  border-radius: 18px;
  background: rgba(16, 27, 46, 0.92);
}

.video-fusion-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 18px;
  color: rgba(195, 216, 236, 0.72);
  border: 1px solid transparent;
  border-radius: 14px;
  background: transparent;
  cursor: pointer;
  transition:
    color 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease,
    transform 0.2s ease;
}

.video-fusion-tab.is-active {
  color: #fff;
  border-color: rgba(91, 171, 255, 0.32);
  background: linear-gradient(135deg, rgba(36, 99, 235, 0.96), rgba(59, 130, 246, 0.72));
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.26);
}

.video-fusion-tab:hover {
  transform: translateY(-1px);
}

.video-fusion-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
}

.video-fusion-toolbar__group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.video-fusion-toolbar__group.is-wide {
  min-width: 0;
}

.video-fusion-toolbar__spacer {
  flex: 1;
}

.video-fusion-toolbar__separator {
  color: rgba(188, 209, 227, 0.6);
}

.video-fusion-playback-caption {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: rgba(205, 226, 244, 0.74);
}

.video-fusion-search {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: min(360px, 100%);
  min-height: 44px;
  padding: 0 14px;
  color: rgba(186, 210, 230, 0.74);
  border: 1px solid rgba(73, 115, 160, 0.32);
  border-radius: 16px;
  background: rgba(12, 24, 40, 0.9);
}

.video-fusion-search input,
.video-fusion-datetime,
.video-fusion-select {
  color: #fff;
  border: none;
  outline: none;
  background: transparent;
}

.video-fusion-search input {
  width: 100%;
}

.video-fusion-select,
.video-fusion-datetime {
  min-height: 44px;
  padding: 0 14px;
  border: 1px solid rgba(73, 115, 160, 0.32);
  border-radius: 16px;
  background: rgba(12, 24, 40, 0.9);
}

.video-fusion-select {
  min-width: 148px;
}

.video-fusion-ghost-button,
.video-fusion-primary-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 36px;
  padding: 0 12px;
  color: #dcefff;
  border: 1px solid rgba(83, 139, 196, 0.26);
  border-radius: 12px;
  background: rgba(12, 24, 40, 0.84);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.video-fusion-ghost-button:hover,
.video-fusion-primary-button:hover,
.video-fusion-ghost-button.is-active {
  transform: translateY(-1px);
  border-color: rgba(101, 180, 255, 0.42);
  background: rgba(15, 39, 62, 0.94);
}

.video-fusion-primary-button {
  color: #fff;
  border-color: rgba(64, 132, 255, 0.38);
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.98), rgba(59, 130, 246, 0.72));
}

.video-fusion-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 286px;
  flex: 1;
  min-height: 0;
  gap: 16px;
}

.video-fusion-body.is-single-column {
  grid-template-columns: 1fr;
}

.video-fusion-main,
.video-fusion-sidebar {
  display: flex;
  min-height: 0;
  flex-direction: column;
}

.video-fusion-main {
  padding: 18px;
}

.video-fusion-main.is-live-mode {
  padding-top: 14px;
}

.video-fusion-main__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.video-fusion-main__meta {
  min-width: 0;
}

.video-fusion-main__title {
  overflow: hidden;
  font-size: 20px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #f4fbff;
}

.video-fusion-main__subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(197, 216, 233, 0.66);
}

.video-fusion-main__status-list {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.video-fusion-main__content {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.video-fusion-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  grid-auto-rows: max-content;
  gap: 18px;
  width: 100%;
  height: auto;
  min-height: min-content;
  align-content: start;
  align-items: start;
  overflow: visible;
  padding-right: 2px;
}

.video-fusion-card {
  position: relative;
  display: grid;
  grid-template-rows: auto auto;
  align-self: start;
  isolation: isolate;
  overflow: hidden;
  height: max-content;
  min-height: 0;
  padding: 0;
  color: #fff;
  text-align: left;
  border: 1px solid rgba(79, 117, 162, 0.18);
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(8, 18, 32, 0.98), rgba(4, 10, 18, 0.96));
  cursor: pointer;
  appearance: none;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.video-fusion-card:hover,
.video-fusion-card.is-selected {
  border-color: rgba(101, 180, 255, 0.44);
  box-shadow:
    0 0 0 1px rgba(101, 180, 255, 0.14),
    0 10px 18px rgba(5, 20, 40, 0.18);
}

.video-fusion-card.is-offline {
  border-color: rgba(79, 117, 162, 0.14);
  background: linear-gradient(180deg, rgba(7, 14, 24, 0.99), rgba(4, 9, 16, 0.98));
}

.video-fusion-card.is-offline .video-fusion-card__media {
  filter: saturate(0.78) brightness(0.82);
}

.video-fusion-card.is-offline .video-fusion-card__name,
.video-fusion-card.is-offline .video-fusion-card__meta,
.video-fusion-card.is-offline .video-fusion-card__channel,
.video-fusion-card.is-offline .video-fusion-card__placeholder-hint,
.video-fusion-card.is-offline .video-fusion-card__corner-icon {
  opacity: 0.72;
}

.video-fusion-card__media {
  position: relative;
  min-height: 0;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-bottom: 1px solid rgba(79, 117, 162, 0.16);
  background: radial-gradient(circle at top, rgba(52, 124, 255, 0.18), transparent 36%), #07111e;
}

.video-fusion-card__media :deep(.factory-stream-player) {
  height: 100% !important;
  min-height: 0 !important;
  border-radius: 0;
}

.video-fusion-card__media :deep(.factory-stream-player__canvas),
.video-fusion-card__media :deep(.factory-stream-player__native),
.video-fusion-card__media :deep(.factory-stream-player__overlay) {
  min-height: 0 !important;
}

.video-fusion-card__media :deep(.factory-stream-player__overlay--empty) {
  inset: auto 10px 10px 10px;
  display: flex;
  min-height: 0 !important;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  padding: 8px 10px;
  border: 1px solid rgba(88, 132, 180, 0.24);
  border-radius: 12px;
  background: rgba(5, 14, 24, 0.86);
  backdrop-filter: none;
}

.video-fusion-card__media :deep(.factory-stream-player__overlay--empty .el-icon) {
  font-size: 14px;
  opacity: 0.84;
}

.video-fusion-card__media :deep(.factory-stream-player__overlay--empty span) {
  overflow: hidden;
  font-size: 12px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-fusion-card__placeholder {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: rgba(98, 168, 255, 0.28);
  pointer-events: none;
}

.video-fusion-card__placeholder-surface {
  display: flex;
  align-items: center;
  justify-content: center;
  width: calc(100% - 12px);
  height: calc(100% - 12px);
  border: 1px solid rgba(69, 110, 156, 0.16);
  border-radius: 16px;
  background:
    radial-gradient(circle at top, rgba(52, 124, 255, 0.12), transparent 42%),
    linear-gradient(180deg, rgba(4, 12, 21, 0.94), rgba(6, 16, 28, 0.98));
  box-shadow: inset 0 0 0 1px rgba(15, 25, 39, 0.42);
}

.video-fusion-card__placeholder-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  opacity: 0.56;
}

.video-fusion-card__placeholder :deep(.el-icon) {
  transform: scale(0.92);
  filter: drop-shadow(0 0 10px rgba(58, 126, 214, 0.18));
}

.video-fusion-card__placeholder-hint {
  position: absolute;
  right: 16px;
  bottom: 16px;
  display: inline-flex;
  align-items: center;
  max-width: calc(100% - 20px);
  padding: 6px 10px;
  overflow: hidden;
  font-size: 11px;
  font-weight: 600;
  color: rgba(226, 238, 255, 0.9);
  text-overflow: ellipsis;
  white-space: nowrap;
  border: 1px solid rgba(88, 132, 180, 0.24);
  border-radius: 12px;
  background: rgba(5, 14, 24, 0.8);
}

.video-fusion-card__chips {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.video-fusion-card__corner {
  position: absolute;
  top: 10px;
  right: 10px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.video-fusion-card__corner-chip {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 8px;
  font-size: 11px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.84);
}

.video-fusion-card__corner-chip.is-online {
  color: #86efac;
}

.video-fusion-card__corner-chip.is-offline {
  color: #cbd5e1;
}

.video-fusion-card__corner-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: rgba(103, 168, 249, 0.84);
}

.video-fusion-card__content {
  display: block;
  flex-shrink: 0;
  min-height: auto;
  align-self: start;
  padding: 4px 12px 5px;
  background: linear-gradient(180deg, rgba(8, 18, 32, 0.98), rgba(4, 10, 18, 0.96));
}

.video-fusion-card__summary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  min-width: 0;
}

.video-fusion-card__name {
  flex: 0 1 auto;
  min-width: 0;
  overflow: hidden;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-fusion-card__channel {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  min-height: 18px;
  padding: 0 5px;
  font-size: 9px;
  color: rgba(191, 214, 233, 0.68);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.74);
}

.video-fusion-card__meta {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  font-size: 11px;
  line-height: 1.2;
  color: rgba(189, 208, 224, 0.64);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-fusion-card__meta::before {
  content: '·';
  margin-right: 6px;
  color: rgba(189, 208, 224, 0.46);
}

.video-fusion-focus {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  width: 100%;
  min-height: 0;
}

.video-fusion-focus__player,
.video-fusion-focus__wall {
  overflow: hidden;
  min-height: 0;
  border: 1px solid rgba(80, 125, 171, 0.18);
  border-radius: 20px;
  background: rgba(7, 17, 29, 0.92);
}

.video-fusion-focus__player {
  position: relative;
}

.video-fusion-focus__player :deep(.factory-stream-player) {
  min-height: 100%;
  border-radius: 0;
}

.video-fusion-focus__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(2, 6, 12, 0.58), transparent 24%, transparent 76%, rgba(2, 6, 12, 0.74));
}

.video-fusion-focus__overlay-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.video-fusion-focus__overlay-subtitle {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(225, 239, 255, 0.78);
}

.video-fusion-ai-panel {
  max-width: 240px;
  padding: 12px 14px;
  border: 1px solid rgba(139, 92, 246, 0.24);
  border-radius: 18px;
  background: rgba(36, 20, 64, 0.54);
}

.video-fusion-ai-panel__title {
  font-size: 13px;
  font-weight: 700;
  color: #d8b4fe;
}

.video-fusion-ai-panel__text {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.6;
  color: rgba(229, 214, 255, 0.74);
}

.video-fusion-focus__wall {
  padding: 14px;
}

.video-fusion-records {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
}

.video-fusion-record {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  color: #fff;
  text-align: left;
  border: 1px solid rgba(80, 123, 168, 0.18);
  border-radius: 18px;
  background: rgba(9, 18, 32, 0.66);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease;
}

.video-fusion-record:hover,
.video-fusion-record.is-active {
  transform: translateY(-1px);
  border-color: rgba(89, 168, 255, 0.42);
  background: rgba(12, 28, 46, 0.92);
}

.video-fusion-record__media,
.video-fusion-record__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 48px;
  font-size: 22px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(19, 49, 90, 0.9), rgba(7, 17, 31, 0.86));
  color: #60a5fa;
}

.video-fusion-record__avatar {
  height: 52px;
  font-size: 24px;
}

.video-fusion-record__avatar.is-人脸识别 {
  background: rgba(30, 64, 175, 0.28);
}

.video-fusion-record__avatar.is-车牌识别 {
  background: rgba(22, 101, 52, 0.28);
}

.video-fusion-record__avatar.is-行为分析 {
  background: rgba(161, 98, 7, 0.24);
}

.video-fusion-record__avatar.is-周界检测 {
  background: rgba(109, 40, 217, 0.22);
}

.video-fusion-record__content {
  min-width: 0;
}

.video-fusion-record__title {
  overflow: hidden;
  font-size: 15px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-fusion-record__subtitle {
  margin-top: 6px;
  overflow: hidden;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(191, 209, 225, 0.66);
}

.video-fusion-record__actions {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.video-fusion-record__play {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: #60a5fa;
  border-radius: 12px;
  background: rgba(30, 64, 175, 0.22);
}

.video-fusion-sidebar {
  padding: 14px;
}

.video-fusion-sidebar__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.video-fusion-sidebar__title {
  font-size: 15px;
  font-weight: 700;
  color: #f4fbff;
}

.video-fusion-sidebar__subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(192, 209, 225, 0.64);
}

.video-fusion-sidebar__list {
  display: flex;
  flex: 1;
  min-height: 0;
  margin-top: 12px;
  flex-direction: column;
  gap: 6px;
  overflow-y: auto;
  padding-right: 2px;
}

.video-fusion-sidebar-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 13px;
  color: #fff;
  text-align: left;
  border: 1px solid rgba(56, 71, 92, 0.72);
  border-radius: 14px;
  background: rgba(31, 41, 55, 0.48);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    transform 0.2s ease,
    background 0.2s ease;
}

.video-fusion-sidebar-item:hover,
.video-fusion-sidebar-item.is-active {
  border-color: rgba(59, 130, 246, 0.5);
  background: rgba(37, 99, 235, 0.18);
}

.video-fusion-sidebar-item.is-offline {
  opacity: 0.6;
}

.video-fusion-sidebar-item__head {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: space-between;
}

.video-fusion-sidebar-item__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #f87171;
}

.video-fusion-sidebar-item__dot.is-online {
  background: #4ade80;
  box-shadow: 0 0 12px rgba(74, 222, 128, 0.72);
}

.video-fusion-sidebar-item__name {
  flex: 1;
  overflow: hidden;
  font-size: 14px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-fusion-sidebar-item__icon {
  color: #fb7185;
}

.video-fusion-sidebar-item__meta,
.video-fusion-sidebar-item__tail {
  margin-left: 16px;
  font-size: 12px;
  color: rgba(156, 163, 175, 0.9);
}

.video-fusion-sidebar-item__tail {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: space-between;
  flex-wrap: nowrap;
}

.video-fusion-sidebar-item__type {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-fusion-sidebar-item__action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #60a5fa;
  white-space: nowrap;
}

.video-fusion-mini-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  font-size: 12px;
  border-radius: 999px;
  background: rgba(18, 34, 54, 0.92);
}

.video-fusion-mini-tag.is-dark {
  color: rgba(220, 237, 255, 0.84);
}

.video-fusion-mini-tag.is-success {
  color: #86efac;
  background: rgba(22, 101, 52, 0.28);
}

.video-fusion-mini-tag.is-primary {
  color: #93c5fd;
  background: rgba(30, 64, 175, 0.28);
}

.video-fusion-mini-tag.is-muted {
  color: #cbd5e1;
  background: rgba(71, 85, 105, 0.34);
}

.video-fusion-mini-tag.is-cyan {
  color: #67e8f9;
  background: rgba(8, 145, 178, 0.24);
}

.video-fusion-mini-tag.is-violet {
  color: #d8b4fe;
  background: rgba(109, 40, 217, 0.22);
}

.video-fusion-mini-tag.is-danger {
  color: #fda4af;
  background: rgba(190, 24, 93, 0.24);
}

.video-fusion-mini-tag.is-warning {
  color: #fcd34d;
  background: rgba(161, 98, 7, 0.24);
}

@media (max-width: 1600px) {
  .video-fusion-body {
    grid-template-columns: minmax(0, 1fr) 274px;
  }

  .video-fusion-focus {
    grid-template-columns: minmax(0, 1fr) 320px;
  }
}

@media (max-width: 1280px) {
  .video-fusion-page__topbar,
  .video-fusion-toolbar {
    flex-wrap: wrap;
  }

  .video-fusion-page__topbar {
    justify-content: flex-start;
  }

  .video-fusion-body {
    grid-template-columns: 1fr;
  }

  .video-fusion-sidebar {
    min-height: 360px;
  }

  .video-fusion-focus {
    grid-template-columns: 1fr;
  }

  .video-fusion-record {
    grid-template-columns: 52px minmax(0, 1fr);
  }

  .video-fusion-record__actions {
    grid-column: 1 / -1;
    justify-content: flex-end;
  }

}

@media (max-width: 1440px) {
  .video-fusion-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .video-fusion-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .video-fusion-toolbar__group.is-wide {
    width: 100%;
    flex-wrap: wrap;
  }

  .video-fusion-datetime {
    flex: 1;
    min-width: 0;
  }
}
</style>
