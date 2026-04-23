<template>
  <ContentWrap
    :body-style="{
      padding: '0',
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden'
    }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: max(0px, calc(var(--page-top-gap, 70px) - (var(--app-content-padding) + 10px)));
      margin-bottom: 0;
    "
  >
    <div class="video-surveillance-board dark-theme-page" @click="handleOutsideClick">
      <svg style="display: none">
        <defs>
          <linearGradient id="gradientOnline" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color: #10b981; stop-opacity: 1" />
            <stop offset="100%" style="stop-color: #34d399; stop-opacity: 1" />
          </linearGradient>
          <linearGradient id="gradientChannel" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color: #3b82f6; stop-opacity: 1" />
            <stop offset="100%" style="stop-color: #60a5fa; stop-opacity: 1" />
          </linearGradient>
        </defs>
      </svg>

      <div class="dashboard">
        <div class="overview-grid">
          <div class="card video-section">
            <div class="card-header">
              <h3>
                <Icon icon="mdi:video" :size="18" />
                实时监控画面
              </h3>
              <div class="video-header-right">
                <div class="view-selector" @click.stop="toggleViewMenu">
                  <span class="badge-option">
                    <Icon icon="mdi:view-grid" :size="14" />
                    {{ currentViewName }}
                  </span>
                  <div class="view-menu" :class="{ show: viewMenuVisible }">
                    <div v-for="group in viewGroups" :key="group.id" class="view-group">
                      <div class="view-group-title">{{ group.name }}</div>
                      <div
                        v-for="view in group.children"
                        :key="view.id"
                        class="view-menu-item"
                        :class="{ active: currentViewId === view.id }"
                        @click.stop="selectView(view.id)"
                      >
                        <span>{{ view.name }}</span>
                        <span class="split-info">{{ getViewSplitInfo(view) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <span class="live-indicator"><span class="live-dot"></span> 实时</span>
              </div>
            </div>

            <div class="video-wall" :class="['layout-dynamic', { 'single-max': singleMax }]" :style="videoWallStyle">
              <div
                v-for="camera in displayedCameras"
                :key="camera.id"
                class="cam-preview"
                :class="[camera.status, { maximized: singleMax && maximizedCameraId === camera.id }]"
                @dblclick="openFullscreen(camera.id, $event)"
              >
                <div class="cam-header">
                  <div class="cam-title">
                    <span class="cam-status" :class="camera.status"></span>
                    {{ camera.name }}
                  </div>
                  <button class="cam-max-btn" @click.stop="toggleSingleMax(camera.id)">
                    <Icon icon="mdi:arrow-expand" :size="14" />
                  </button>
                </div>
                <div class="cam-body">
                  <template v-if="camera.deviceId || camera.channelId || camera.channelNo">
                    <video
                      class="cam-video"
                      muted
                      playsinline
                      :ref="(el) => setCameraVideoRef(camera, el as HTMLVideoElement | null)"
                    ></video>
                    <div v-if="camera.isLoading" class="cam-video-overlay">
                      <Icon icon="mdi:loading" :size="36" class="cam-spin" />
                      <span>连接中…</span>
                    </div>
                    <div v-else-if="camera.streamError" class="cam-video-overlay cam-video-error">
                      <Icon icon="mdi:alert-circle" :size="32" />
                      <span>{{ camera.streamError }}</span>
                    </div>
                  </template>
                  <template v-else>
                    <Icon icon="mdi:video" :size="48" />
                  </template>
                </div>
                <div class="cam-footer">
                  <span>{{ camera.resolution }}</span>
                  <span>{{ camera.bitrate }}</span>
                </div>
              </div>
            </div>

            <div class="video-controls">
              <div class="control-group">
                <button class="control-btn" :class="{ active: autoPollEnabled }" @click="toggleAutoPoll">
                  <Icon icon="mdi:sync" :size="14" />
                  自动轮询
                </button>
                <button class="control-btn" @click="openFullscreen()">
                  <Icon icon="mdi:fullscreen" :size="14" />
                  全屏展示
                </button>
              </div>
              <div class="control-group">
                <button class="control-btn" @click="handleScreenshot">
                  <Icon icon="mdi:camera" :size="14" />
                  截图
                </button>
                <button class="control-btn" @click="handleEmergencyRecord">
                  <Icon icon="mdi:content-save" :size="14" />
                  紧急录像
                </button>
              </div>
            </div>

            <div v-if="singleMax" class="maximize-controls">
              <button class="control-btn" @click="exitSingleMax">
                <Icon icon="mdi:arrow-collapse" :size="14" />
                退出放大
              </button>
            </div>

            <div class="region-selector">
              <Icon icon="mdi:map-marker" :size="14" />
              <span>当前显示: 用户预选摄像头 (可自定义)</span>
              <button class="control-btn" style="padding: 4px 8px" @click="openCameraConfig">
                <Icon icon="mdi:pencil" :size="14" />
                配置
              </button>
            </div>
          </div>

          <div class="overview-right">
            <div class="right-top">
              <div class="card">
                <div class="card-header">
                  <h3>
                    <Icon icon="mdi:bell" :size="18" />
                    实时告警
                  </h3>
                  <span class="badge-option alert">
                    <Icon icon="mdi:alert-circle" :size="14" />
                    {{ unreadAlertCount }}条
                  </span>
                </div>
                <div class="alert-timeline">
                  <div v-for="alert in visibleAlertList" :key="alert.id" class="alert-item" :class="alert.type">
                    <div class="alert-icon">
                      <Icon :icon="getAlertIcon(alert.type)" :size="18" />
                    </div>
                    <div class="alert-content">
                      <div class="alert-title">{{ alert.title }}</div>
                      <div class="alert-meta">
                        <span class="alert-time">{{ alert.time }}</span>
                        <span class="alert-location">
                          <Icon icon="mdi:map-marker" :size="14" />
                          {{ alert.location }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="card">
                <div class="card-header">
                  <h3>
                    <Icon icon="mdi:server" :size="18" />
                    设备健康
                  </h3>
                  <span class="badge-option">{{ deviceTotalText }}</span>
                </div>
                <div class="device-dashboard">
                  <div class="device-main-stat">
                    <div class="circular-chart">
                      <svg width="100" height="100" viewBox="0 0 100 100">
                        <circle class="circle-bg" cx="50" cy="50" r="40" />
                        <circle
                          class="circle online"
                          cx="50"
                          cy="50"
                          r="40"
                          :stroke-dasharray="onlineDasharray"
                          stroke-dashoffset="0"
                        />
                      </svg>
                      <div class="chart-text">
                        <div class="chart-value">{{ deviceOnlineRate }}%</div>
                        <div class="chart-label">设备在线率</div>
                      </div>
                    </div>
                    <div class="circular-chart channel-chart">
                      <svg width="100" height="100" viewBox="0 0 100 100">
                        <circle class="circle-bg" cx="50" cy="50" r="40" />
                        <circle
                          class="circle channel"
                          cx="50"
                          cy="50"
                          r="40"
                          :stroke-dasharray="channelDasharray"
                          stroke-dashoffset="0"
                        />
                      </svg>
                      <div class="chart-text">
                        <div class="chart-value">{{ channelOnlineRate }}%</div>
                        <div class="chart-label">通道在线率</div>
                      </div>
                    </div>
                  </div>

                  <div class="device-categories">
                    <div v-for="category in deviceCategories" :key="category.key" class="category-card">
                      <div class="category-header">
                        <span class="category-title">
                          <Icon :icon="category.icon" :size="16" />
                          {{ category.name }}
                        </span>
                        <span class="category-count">{{ category.total }}</span>
                      </div>
                      <div class="category-bar">
                        <div
                          class="category-fill"
                          :class="category.fillClass"
                          :style="{ width: category.percent + '%' }"
                        ></div>
                      </div>
                      <div class="category-status">
                        <span :class="category.okClass">{{ category.okText }}</span>
                        <span class="status-offline">{{ category.badText }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="right-bottom">
              <div class="card right-storage-card">
                <div class="card-header">
                  <h3>
                    <Icon icon="mdi:view-dashboard-outline" :size="18" />
                    录像存储
                  </h3>
                  <span class="badge-option">实时</span>
                </div>
                <div class="recording-stats">
                  <div class="rec-metrics">
                    <div class="metric-box">
                      <div class="metric-value">{{ storageUsageRate }}%</div>
                      <div class="metric-label">
                        <Icon icon="mdi:harddisk" :size="14" />
                        存储池使用率
                      </div>
                    </div>
                    <div class="metric-box">
                      <div class="metric-value">{{ dailyWriteAmount }}</div>
                      <div class="metric-label">
                        <Icon icon="mdi:download" :size="14" />
                        日均写入量
                      </div>
                    </div>
                  </div>
                  <div class="storage-section">
                    <div class="storage-header">
                      <div class="storage-title">
                        <Icon icon="mdi:database" :size="14" />
                        总存储容量
                      </div>
                      <div class="storage-value">{{ storageTotalText }}</div>
                    </div>
                    <div class="storage-bar">
                      <div class="storage-fill" :style="{ width: storageUsageRate + '%' }"></div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="card right-region-card">
                <div class="card-header">
                  <h3>
                    <Icon icon="mdi:chart-donut" :size="18" />
                    监控区域分布
                  </h3>
                  <span class="badge-option">{{ regionTotalText }}</span>
                </div>
                <div class="distribution-section">
                  <div class="pie-container">
                    <div class="pie-chart">
                      <div class="pie-center">
                        <div class="pie-total">{{ regionTotal }}</div>
                        <div class="pie-label">摄像头总数</div>
                      </div>
                    </div>
                  </div>
                  <div class="bars-container">
                    <div v-for="item in regionDistribution" :key="item.key + '-bar'" class="bar-item">
                      <div class="bar-label">
                        <Icon :icon="item.icon" :size="16" />
                        {{ item.name }}
                      </div>
                      <div class="bar-track">
                        <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
                      </div>
                      <div class="bar-value">{{ item.value }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>

      <div v-if="fullscreenVisible" class="fullscreen-overlay" @click.self="closeFullscreen">
        <button class="fullscreen-close" @click="closeFullscreen">
          <Icon icon="mdi:close" :size="18" />
        </button>
        <div class="fullscreen-content">
          <div v-if="fullscreenCamera" class="cam-preview" style="width: 100%; height: 100%">
            <div class="cam-header">
              <div class="cam-title">
                <span class="cam-status" :class="fullscreenCamera.status"></span>
                {{ fullscreenCamera.name }}
              </div>
            </div>
            <div class="cam-body">
              <Icon icon="mdi:video" :size="120" />
            </div>
            <div class="cam-footer">
              <span>{{ fullscreenCamera.resolution }}</span>
              <span>{{ fullscreenCamera.bitrate }}</span>
            </div>
          </div>

          <div v-else class="video-wall layout-dynamic" :style="videoWallStyle">
            <div
              v-for="camera in displayedCameras"
              :key="'fs-' + camera.id"
              class="cam-preview"
              :class="[camera.status]"
            >
              <div class="cam-header">
                <div class="cam-title">
                  <span class="cam-status" :class="camera.status"></span>
                  {{ camera.name }}
                </div>
              </div>
              <div class="cam-body">
                <Icon icon="mdi:video" :size="48" />
              </div>
              <div class="cam-footer">
                <span>{{ camera.resolution }}</span>
                <span>{{ camera.bitrate }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { getVideoView, getVideoViewTree, type VideoViewVO } from '@/api/iot/video/videoView'
import * as SecurityOverviewApi from '@/api/iot/security-overview'
import type { PlayUrlRespVO } from '@/api/iot/security-overview'
import { getChannelPage as getIbmsChannelPage, getChannel as getIbmsChannel } from '@/api/iot/ibms/channel'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'
import mpegts from 'mpegts.js'

defineOptions({ name: 'VideoSurveillanceVisualBoard' })

type CameraStatus = 'online' | 'offline'

interface CameraItem {
  id: number | string
  name: string
  status: CameraStatus
  resolution: string
  bitrate: string
  /** IoT 设备 ID，走安防概览取流 */
  deviceId: number | null
  /** 视图窗格里的通道引用（历史数据多为 NVR 通道号，存于 channelId 字段） */
  channelId: number | null
  /** 窗格上的 NVR 通道号（若有） */
  channelNo: number | null
  /** 解析后用于 ZLM /stop 的 ibms_channel.id */
  zlmIbmsChannelId?: number | null
  isLoading?: boolean
  isPlaying?: boolean
  streamError?: string | null
  videoEl?: HTMLVideoElement | null
  player?: mpegts.Player | null
  /** 用于卸载时是否调用 stopStream（仅 channel 拉流） */
  playSource?: 'device' | 'channel' | null
}

interface ViewPaneItem {
  paneIndex: number
  channelId?: number | string | null
  channelName?: string
  deviceId?: number | string | null
  channelNo?: number | string | null
}

interface ViewMenuItem {
  id: number
  name: string
  gridLayout?: number
  layout?: number
  paneCount?: number
}

interface ViewGroupItem {
  id: number
  name: string
  children: ViewMenuItem[]
}

type VideoViewDetail = VideoViewVO & { id?: number; panes?: ViewPaneItem[]; paneCount?: number }

interface AlertItem {
  id: number
  type: 'critical' | 'warning' | 'info'
  title: string
  time: string
  location: string
}

const clockText = ref('00:00:00')
let clockTimer: number | null = null

const updateClock = () => {
  const now = new Date()
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  clockText.value = `${h}:${m}:${s}`
}

const viewGroups = ref<ViewGroupItem[]>([])
const currentViewId = ref<number | null>(null)
const currentViewGridCount = ref(1)
const viewMenuVisible = ref(false)

const cameraList = ref<CameraItem[]>([])

const currentViewName = computed(() => {
  if (!currentViewId.value) return '无可用视图'
  for (const group of viewGroups.value) {
    const matched = group.children.find((item) => item.id === currentViewId.value)
    if (matched) return matched.name
  }
  return '无可用视图'
})

const displayedCameras = computed(() => cameraList.value)

const getGridColumnCount = (paneCount: number) => {
  if (paneCount <= 1) return 1
  if (paneCount <= 4) return 2
  if (paneCount <= 9) return 3
  return 4
}

const videoWallStyle = computed(() => {
  const paneCount = Math.max(displayedCameras.value.length, currentViewGridCount.value, 1)
  return {
    gridTemplateColumns: `repeat(${getGridColumnCount(paneCount)}, minmax(0, 1fr))`
  }
})

const toggleViewMenu = () => {
  viewMenuVisible.value = !viewMenuVisible.value
}

const getViewPanes = (view: ViewMenuItem | VideoViewDetail) => {
  if ('panes' in view && Array.isArray(view.panes)) {
    return view.panes
  }
  return []
}

const toViewPaneCount = (view: ViewMenuItem | VideoViewDetail) => {
  const panesCount = getViewPanes(view).length
  const rawCount = Number(view.gridLayout ?? view.layout ?? view.paneCount ?? panesCount ?? 1)
  if (Number.isFinite(rawCount) && rawCount > 0) return Math.floor(rawCount)
  return Math.max(panesCount, 1)
}

const getViewSplitInfo = (view: ViewMenuItem) => `${toViewPaneCount(view)}分屏`

const isFirefox = navigator.userAgent.toLowerCase().includes('firefox')

const isIntranetAccess = (): boolean => {
  const hostname = window.location.hostname
  if (hostname === 'localhost' || hostname === '127.0.0.1') return true
  if (hostname.startsWith('192.168.')) return true
  if (hostname.startsWith('10.')) return true
  if (hostname.startsWith('172.')) {
    const secondOctet = parseInt(hostname.split('.')[1])
    if (secondOctet >= 16 && secondOctet <= 31) return true
  }
  return false
}

const PUBLIC_ZLM = {
  get host() {
    return window.location.hostname
  },
  get httpPort() {
    return window.location.port ? parseInt(window.location.port) : 80
  }
}

const adaptPlayUrls = (urls: PlayUrlRespVO | null | undefined): PlayUrlRespVO | null => {
  if (!urls) return null
  if (isIntranetAccess()) return urls
  const adapted: PlayUrlRespVO = { ...urls }
  const publicHost = PUBLIC_ZLM.host
  const publicPort = PUBLIC_ZLM.httpPort
  const publicAddr = publicPort === 80 || publicPort === 443 ? publicHost : `${publicHost}:${publicPort}`
  const isHttps = window.location.protocol === 'https:'
  const httpProtocol = isHttps ? 'https' : 'http'
  const wsProtocol = isHttps ? 'wss' : 'ws'
  const replaceHttpUrl = (url: string): string => {
    if (!url) return url
    let newUrl = url.replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr).replace(/192\.168\.\d+\.\d+/g, publicHost)
    newUrl = newUrl.replace(/^http:/, `${httpProtocol}:`)
    return newUrl
  }
  const replaceWsUrl = (url: string): string => {
    if (!url) return url
    let newUrl = url.replace(/192\.168\.\d+\.\d+:\d+/g, publicAddr).replace(/192\.168\.\d+\.\d+/g, publicHost)
    newUrl = newUrl.replace(/^ws:/, `${wsProtocol}:`)
    return newUrl
  }
  adapted.wsFlvUrl = urls.wsFlvUrl ? replaceWsUrl(urls.wsFlvUrl) : urls.wsFlvUrl
  adapted.flvUrl = urls.flvUrl ? replaceHttpUrl(urls.flvUrl) : urls.flvUrl
  adapted.hlsUrl = urls.hlsUrl ? replaceHttpUrl(urls.hlsUrl) : urls.hlsUrl
  if (urls.webrtcUrl) adapted.webrtcUrl = replaceHttpUrl(urls.webrtcUrl)
  return adapted
}

const pickFlvPlayUrl = (raw: PlayUrlRespVO | null | undefined): string | null => {
  const urls = adaptPlayUrls(raw)
  if (!urls) return null
  if (urls.wsFlvUrl) return urls.wsFlvUrl
  if (urls.flvUrl) return urls.flvUrl
  return null
}

/** 后端分页校验：每页条数最大值 100 */
const IBMS_CHANNEL_PAGE_MAX = 100

const unwrapIbmsChannelList = (res: any): any[] => {
  const page = res && res.list ? res : res?.data
  return page?.list ?? []
}

const findIbmsChannelIdByDeviceAndChannelNo = async (
  deviceId: number,
  channelNo: number
): Promise<number | null> => {
  let pageNo = 1
  while (pageNo <= 50) {
    const res = await getIbmsChannelPage({
      pageNo,
      pageSize: IBMS_CHANNEL_PAGE_MAX,
      deviceId,
      typeCode: 'VT'
    })
    const list = unwrapIbmsChannelList(res)
    const hit = list.find((ch: any) => ch.channelNo === channelNo)
    if (hit?.id) return hit.id
    if (!list.length || list.length < IBMS_CHANNEL_PAGE_MAX) break
    pageNo++
  }
  return null
}

const findIbmsChannelIdByNvrNoPaged = async (
  nvrNo: number,
  deviceId: number | null
): Promise<number | null> => {
  let pageNo = 1
  while (pageNo <= 50) {
    const res = await getIbmsChannelPage({
      pageNo,
      pageSize: IBMS_CHANNEL_PAGE_MAX,
      business: 'security',
      typeCode: 'VT',
      keyword: String(nvrNo),
      ...(deviceId ? { deviceId } : {})
    })
    const list = unwrapIbmsChannelList(res)
    const hit = list.find(
      (ch: any) => ch.channelNo === nvrNo || ch.channelNo === Number(nvrNo)
    )
    if (hit?.id) return hit.id
    if (!list.length || list.length < IBMS_CHANNEL_PAGE_MAX) break
    pageNo++
  }
  return null
}

/**
 * 将视图窗格解析为 ibms_channel.id（ZLM 接口入参）。
 * 与实时预览「保存视图」一致：iot_video_view_pane.channel_id 常存的是 NVR 通道号而非主键。
 */
const resolveIbmsChannelIdForZlm = async (camera: CameraItem): Promise<number | null> => {
  const deviceId = camera.deviceId != null && camera.deviceId > 0 ? camera.deviceId : null
  const explicitNoRaw =
    camera.channelNo != null && camera.channelNo > 0 ? camera.channelNo : NaN
  const explicitNo = Number.isFinite(explicitNoRaw) ? explicitNoRaw : null
  const idFieldRaw = camera.channelId != null && camera.channelId > 0 ? camera.channelId : NaN
  const idField = Number.isFinite(idFieldRaw) ? idFieldRaw : null

  if (!idField && !explicitNo) return null

  if (deviceId && explicitNo) {
    const byDevice = await findIbmsChannelIdByDeviceAndChannelNo(deviceId, explicitNo)
    if (byDevice) return byDevice
  }

  const nvrNo = explicitNo ?? idField
  if (nvrNo != null) {
    const byNo = await findIbmsChannelIdByNvrNoPaged(nvrNo, deviceId)
    if (byNo) return byNo
  }

  if (idField) {
    try {
      const row = await getIbmsChannel(idField)
      if (row?.id) return row.id
    } catch {
      /* 非主键或不存在 */
    }
  }

  return null
}

const stopCameraStream = (camera: CameraItem) => {
  if (camera.player) {
    try {
      camera.player.pause()
      camera.player.unload()
      camera.player.detachMediaElement()
      camera.player.destroy()
    } catch {
      /* ignore */
    }
    camera.player = null
  }
  if (camera.videoEl) {
    try {
      camera.videoEl.srcObject = null
      camera.videoEl.src = ''
    } catch {
      /* ignore */
    }
  }
  if (camera.playSource === 'channel') {
    const sid = camera.zlmIbmsChannelId ?? camera.channelId
    if (sid) stopStream(sid).catch(() => {})
  }
  camera.zlmIbmsChannelId = null
  camera.isPlaying = false
  camera.isLoading = false
  camera.playSource = null
}

const setCameraVideoRef = (camera: CameraItem, el: HTMLVideoElement | null) => {
  if (el) {
    camera.videoEl = el
  } else if (camera.videoEl) {
    camera.videoEl = null
  }
}

const fetchPlayUrlForCamera = async (camera: CameraItem): Promise<PlayUrlRespVO> => {
  if (camera.deviceId) {
    return await SecurityOverviewApi.getPlayUrl(camera.deviceId)
  }
  if (camera.channelId || (camera.channelNo != null && camera.channelNo > 0)) {
    const ibmsId = await resolveIbmsChannelIdForZlm(camera)
    if (!ibmsId) {
      throw new Error('无法解析 IBMS 视频通道（请检查视图绑定或通道是否存在）')
    }
    camera.zlmIbmsChannelId = ibmsId
    // 与实时预览默认一致：主码流 subtype=0（子码流 1 在未配置子流时会导致无法构建 RTSP）
    return await getLivePlayUrl(ibmsId, 0)
  }
  throw new Error('未绑定设备或通道')
}

const attachFlvPlayer = (camera: CameraItem, playUrl: string): Promise<boolean> => {
  return new Promise((resolve) => {
    const videoEl = camera.videoEl
    if (!videoEl || !mpegts.isSupported()) {
      resolve(false)
      return
    }
    try {
      const player = mpegts.createPlayer(
        {
          type: 'flv',
          url: playUrl,
          isLive: true,
          hasAudio: false,
          hasVideo: true
        },
        {
          enableWorker: false,
          enableStashBuffer: true,
          stashInitialSize: isFirefox ? 256 : 128,
          lazyLoad: false,
          // 与 useZlmPlayer 一致：部分 ZLM/FLV 直播时间戳会让 mpegts 算出负的 remove 区间，触发
          // SourceBuffer.remove(end < start) 未捕获异常；关闭自动清理以稳定播放
          autoCleanupSourceBuffer: false,
          autoCleanupMaxBackwardDuration: isFirefox ? 5 : 3,
          liveBufferLatencyChasing: true,
          liveBufferLatencyMaxLatency: isFirefox ? 2.0 : 1.5,
          liveSync: true
        }
      )
      player.attachMediaElement(videoEl)
      player.load()
      player.on(mpegts.Events.ERROR, (errorType: any, errorDetail: any) => {
        if (String(errorDetail || '').includes('SourceBuffer') || String(errorDetail || '').includes('MSEError')) {
          return
        }
        camera.streamError = `播放错误: ${errorDetail || errorType}`
        camera.isPlaying = false
        camera.isLoading = false
      })
      const playDelay = isFirefox ? 500 : 100
      setTimeout(async () => {
        try {
          await player.play()
          camera.player = player
          camera.isPlaying = true
          camera.isLoading = false
          camera.streamError = null
          resolve(true)
        } catch {
          resolve(false)
        }
      }, playDelay)
    } catch {
      resolve(false)
    }
  })
}

const playOneCameraStream = async (camera: CameraItem) => {
  if (!camera.deviceId && !camera.channelId && !camera.channelNo) {
    return
  }
  stopCameraStream(camera)
  camera.isLoading = true
  camera.streamError = null
  camera.isPlaying = false
  try {
    const raw = await fetchPlayUrlForCamera(camera)
    const playUrl = pickFlvPlayUrl(raw)
    if (!playUrl) {
      throw new Error('未获取到 FLV 播放地址（需 wsFlv 或 http-flv）')
    }
    camera.playSource = camera.deviceId ? 'device' : 'channel'
    for (let i = 0; i < 30 && !camera.videoEl; i++) {
      await new Promise((r) => setTimeout(r, 50))
    }
    const ok = await attachFlvPlayer(camera, playUrl)
    if (!ok) {
      throw new Error('浏览器不支持或播放器启动失败')
    }
  } catch (e: any) {
    camera.streamError = e?.message || '取流失败'
    camera.isLoading = false
    camera.isPlaying = false
    camera.status = 'offline'
  }
}

const startWallPlayback = async () => {
  await nextTick()
  const list = cameraList.value.filter((c) => c.deviceId || c.channelId || c.channelNo)
  for (const camera of list) {
    await playOneCameraStream(camera)
    if (isFirefox) {
      await new Promise((r) => setTimeout(r, 320))
    }
  }
}

const stopAllWallPlayers = () => {
  cameraList.value.forEach((c) => stopCameraStream(c))
}

const applyViewToVideoWall = (view: VideoViewDetail) => {
  stopAllWallPlayers()
  currentViewGridCount.value = toViewPaneCount(view)
  const panes = getViewPanes(view)
  const targetPaneCount = Math.max(currentViewGridCount.value, panes.length, 1)
  const paneBySlot = new Map<number, ViewPaneItem>()
  panes.forEach((pane, idx) => {
    const rawPaneIndex = Number(pane.paneIndex)
    const slot = Number.isFinite(rawPaneIndex) && rawPaneIndex > 0 ? Math.floor(rawPaneIndex) - 1 : idx
    if (slot >= 0 && slot < targetPaneCount && !paneBySlot.has(slot)) {
      paneBySlot.set(slot, pane)
    }
  })
  cameraList.value = Array.from({ length: targetPaneCount }, (_, index) => {
    const pane: Partial<ViewPaneItem> = paneBySlot.get(index) ?? {}
    const deviceIdRaw = pane.deviceId != null ? Number(pane.deviceId) : NaN
    const channelIdRaw = pane.channelId != null ? Number(pane.channelId) : NaN
    const channelNoRaw = pane.channelNo != null ? Number(pane.channelNo) : NaN
    const deviceId = Number.isFinite(deviceIdRaw) && deviceIdRaw > 0 ? deviceIdRaw : null
    const channelId = Number.isFinite(channelIdRaw) && channelIdRaw > 0 ? channelIdRaw : null
    const channelNo = Number.isFinite(channelNoRaw) && channelNoRaw > 0 ? channelNoRaw : null
    const stableId = deviceId ? `d-${deviceId}-${index}` : channelId ? `c-${channelId}-${index}` : `empty-${view.id ?? 'v'}-${index}`
    const hasBinding = !!(deviceId || channelId || channelNo)
    return {
      id: stableId,
      name: pane.channelName || `视频通道${index + 1}`,
      status: (hasBinding ? 'online' : 'offline') as CameraStatus,
      resolution: '--',
      bitrate: '--',
      deviceId,
      channelId,
      channelNo,
      zlmIbmsChannelId: null,
      isLoading: hasBinding,
      isPlaying: false,
      streamError: null,
      videoEl: null,
      player: null,
      playSource: null
    }
  })
}

const selectView = async (viewId: number, silent = false) => {
  const viewDetail = (await getVideoView(viewId)) as VideoViewDetail
  currentViewId.value = viewId
  applyViewToVideoWall(viewDetail)
  viewMenuVisible.value = false
  exitSingleMax()
  await startWallPlayback()
  if (!silent) {
    ElMessage.success(`已切换到视图：${currentViewName.value}`)
  }
}

const loadViewGroups = async () => {
  const treeData = (await getVideoViewTree()) as any[]
  const groups = (Array.isArray(treeData) ? treeData : []).map((group: any) => ({
    id: Number(group.id),
    name: group.name || '未命名分组',
    children: (Array.isArray(group.children) ? group.children : [])
      .map((view: any) => ({
        id: Number(view.id),
        name: view.name || '未命名视图',
        gridLayout: view.gridLayout,
        layout: view.layout,
        paneCount: view.paneCount
      }))
      .filter((view: ViewMenuItem) => Number.isFinite(view.id))
  }))
  viewGroups.value = groups
  const defaultGroup = groups.find((group) => group.id === 1) || groups[0]
  const defaultView = defaultGroup?.children?.[0]
  if (defaultView) {
    await selectView(defaultView.id, true)
  } else {
    currentViewId.value = null
    currentViewGridCount.value = 1
    cameraList.value = []
  }
}

const handleOutsideClick = () => {
  if (viewMenuVisible.value) viewMenuVisible.value = false
}

const singleMax = ref(false)
const maximizedCameraId = ref<number | string | null>(null)

const toggleSingleMax = (cameraId: number | string) => {
  if (singleMax.value && maximizedCameraId.value === cameraId) {
    exitSingleMax()
    return
  }
  singleMax.value = true
  maximizedCameraId.value = cameraId
}

const exitSingleMax = () => {
  singleMax.value = false
  maximizedCameraId.value = null
}

const fullscreenVisible = ref(false)
const fullscreenCameraId = ref<number | string | null>(null)

const fullscreenCamera = computed(() => {
  if (!fullscreenCameraId.value) return null
  return cameraList.value.find((c) => c.id === fullscreenCameraId.value) || null
})

const openFullscreen = (cameraId?: number | string, evt?: MouseEvent) => {
  // 双击单窗时优先使用浏览器原生全屏，直接放大当前正在播放的视频节点，避免覆盖层丢流
  if (cameraId != null && evt?.currentTarget instanceof HTMLElement) {
    const el = evt.currentTarget
    const requestFullscreenFn =
      el.requestFullscreen ||
      (el as any).webkitRequestFullscreen ||
      (el as any).mozRequestFullScreen ||
      (el as any).msRequestFullscreen
    if (typeof requestFullscreenFn === 'function') {
      requestFullscreenFn.call(el)
      return
    }
  }
  fullscreenCameraId.value = cameraId ?? null
  fullscreenVisible.value = true
  document.body.style.overflow = 'hidden'
}

const closeFullscreen = () => {
  fullscreenVisible.value = false
  fullscreenCameraId.value = null
  document.body.style.overflow = ''
}

const autoPollEnabled = ref(true)
const toggleAutoPoll = () => {
  autoPollEnabled.value = !autoPollEnabled.value
  ElMessage.success(autoPollEnabled.value ? '已开启自动轮询（模拟）' : '已关闭自动轮询（模拟）')
}

const handleScreenshot = () => {
  ElMessage.success('已触发截图（模拟）')
}

const handleEmergencyRecord = () => {
  ElMessage.success('已触发紧急录像（模拟）')
}

const openCameraConfig = () => {
  ElMessage.info('配置入口预留（原型一致，后续可接入真实配置）')
}

const alertList = ref<AlertItem[]>([
  { id: 1, type: 'critical', title: '出入口异常闯入', time: '14:28:45', location: '出入口-主通道' },
  { id: 2, type: 'warning', title: '停车场可疑停留', time: '14:15:22', location: '停车场-北区' },
  { id: 3, type: 'info', title: '仓库摄像头离线', time: '13:58:10', location: '仓库-1号区' }
])

const unreadAlertCount = computed(() => alertList.value.length)
const visibleAlertList = computed(() => alertList.value.slice(0, 3))

const getAlertIcon = (type: AlertItem['type']) => {
  if (type === 'critical') return 'mdi:alert'
  if (type === 'warning') return 'mdi:alert-circle'
  return 'mdi:information'
}

const deviceOnlineRate = ref(82)
const channelOnlineRate = ref(95)
const deviceTotalText = ref('28设备')

const ringCircumference = 2 * Math.PI * 40
const toDasharray = (percent: number) => {
  const dash = (ringCircumference * percent) / 100
  const gap = ringCircumference - dash
  return `${dash.toFixed(1)} ${gap.toFixed(1)}`
}

const onlineDasharray = computed(() => toDasharray(deviceOnlineRate.value))
const channelDasharray = computed(() => toDasharray(channelOnlineRate.value))

const deviceCategories = ref([
  {
    key: 'ipc',
    icon: 'mdi:video',
    name: 'IPC摄像头',
    total: 20,
    percent: 85,
    fillClass: 'online',
    okClass: 'status-online',
    okText: '17 在线',
    badText: '3 离线'
  },
  {
    key: 'server',
    icon: 'mdi:server',
    name: '视频服务器',
    total: 3,
    percent: 100,
    fillClass: 'online',
    okClass: 'status-online',
    okText: '3 在线',
    badText: '0 离线'
  },
  {
    key: 'storage',
    icon: 'mdi:harddisk',
    name: '存储设备',
    total: 2,
    percent: 100,
    fillClass: 'online',
    okClass: 'status-online',
    okText: '2 正常',
    badText: '0 异常'
  },
  {
    key: 'channel',
    icon: 'mdi:lan',
    name: '视频通道',
    total: 23,
    percent: 95,
    fillClass: 'channel',
    okClass: 'status-channel',
    okText: '22 正常',
    badText: '1 异常'
  }
])

const storageUsageRate = ref(68)
const dailyWriteAmount = ref('286GB')
const storageTotalText = ref('8TB / 12TB')
const storageAvailableText = ref('3.84TB')
const storageAvailableDays = ref(13)

const regionDistribution = ref([
  { key: 'gate', name: '出入口', value: 12, percent: 48, color: 'var(--accent-blue)', icon: 'mdi:door-open' },
  { key: 'parking', name: '停车场', value: 7, percent: 28, color: 'var(--accent-orange)', icon: 'mdi:parking' },
  { key: 'office', name: '办公楼', value: 4, percent: 16, color: 'var(--accent-green)', icon: 'mdi:office-building' },
  { key: 'warehouse', name: '仓库', value: 2, percent: 8, color: 'var(--accent-red)', icon: 'mdi:warehouse' }
])

const regionTotal = computed(() => regionDistribution.value.reduce((sum, item) => sum + item.value, 0))
const regionTotalText = computed(() => `${regionTotal.value}路摄像头`)

const storageHealthLedClass = computed(() => 'good')
const storageHealthText = computed(() => '存储系统正常')
const fpsStatusText = computed(() => '主码流 25fps')

const lastAlertTimeText = computed(() => {
  const latest = alertList.value[0]
  if (!latest) return '最后报警 --:--'
  return `最后报警 ${latest.time.slice(0, 5)}`
})

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 1000)
  loadViewGroups().catch(() => {
    ElMessage.error('加载视图列表失败')
    currentViewId.value = null
    currentViewGridCount.value = 1
    cameraList.value = []
  })
})

onUnmounted(() => {
  stopAllWallPlayers()
  if (clockTimer) window.clearInterval(clockTimer)
  document.body.style.overflow = ''
})
</script>

<style lang="scss" scoped>
.video-surveillance-board {
  --bg-primary: #0a0f1a;
  --bg-card: #111827;
  --bg-hover: #1f2937;
  --border-default: #1e3a5f;
  --border-active: #3b82f6;
  --text-primary: #f1f5f9;
  --text-secondary: #94a3b8;
  --accent-blue: #3b82f6;
  --accent-green: #10b981;
  --accent-orange: #f59e0b;
  --accent-red: #ef4444;
  --glow-blue: 0 0 20px rgba(59, 130, 246, 0.3);

  background: var(--bg-primary);
  color: var(--text-primary);
  padding: clamp(8px, 1.2vh, 14px);
  height: 100%;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-image: radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(16, 185, 129, 0.03) 0%, transparent 50%);
}

.dashboard {
  max-width: 1920px;
  width: 100%;
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-rows: minmax(0, 1fr);
  gap: clamp(8px, 1.1vh, 12px);
  margin: 0 auto;
}


.card {
  background: var(--bg-card);
  border-radius: 14px;
  padding: clamp(10px, 1.1vh, 14px);
  border: 1px solid var(--border-default);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  min-height: 0;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--border-active), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.card:hover {
  border-color: var(--border-active);
  box-shadow: var(--glow-blue);
}

.card:hover::before {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.card-header h3 {
  font-weight: 600;
  font-size: clamp(14px, 0.9vw, 18px);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 2.15fr) minmax(640px, 1.35fr);
  gap: clamp(8px, 1.1vh, 12px);
  min-height: 0;
}

.overview-right {
  display: grid;
  grid-template-rows: minmax(0, 0.88fr) minmax(0, 1.27fr);
  gap: clamp(8px, 1.1vh, 12px);
  min-height: 0;
}

.right-top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: clamp(6px, 0.9vh, 10px);
  min-height: 0;
}

.right-bottom {
  display: grid;
  grid-template-rows: minmax(0, 1.15fr) minmax(0, 0.85fr);
  gap: clamp(8px, 1.1vh, 12px);
  min-height: 0;
}

.right-storage-card .card-header {
  margin-bottom: 8px;
  padding-bottom: 6px;
}

.right-storage-card .rec-metrics {
  gap: 8px;
}

.right-storage-card .metric-box {
  padding: 10px;
}

.right-storage-card .storage-section {
  padding: 10px;
}

.right-storage-card .storage-bar {
  margin-bottom: 8px;
}

.right-storage-card .storage-details {
  font-size: 11px;
  line-height: 1.1;
}

.right-region-card .pie-legend {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.right-region-card .legend-item {
  padding: 6px 8px;
  border-radius: 12px;
}

.right-region-card .bars-container {
  gap: 6px;
}

.right-region-card .bar-item {
  grid-template-columns: 76px 1fr 26px;
  padding: 6px 8px;
  border-radius: 12px;
}

.right-region-card .bar-track {
  height: 8px;
}

.overview-grid > .card,
.right-top > .card,
.right-bottom > .card {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.ai-grid {
  display: none;
}

.badge-option {
  font-size: 0.75rem;
  padding: 3px 10px;
  border-radius: 20px;
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.25);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
}

.badge-option.alert {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.35);
  color: #fecaca;
}

.sim-badge {
  font-size: 0.7rem;
  background: rgba(255, 165, 0, 0.2);
  color: orange;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
}

.video-header-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.view-selector {
  position: relative;
}

.view-menu {
  position: absolute;
  top: 36px;
  right: 0;
  min-width: 180px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 12px;
  padding: 8px;
  display: none;
  z-index: 10;
  backdrop-filter: blur(10px);
  max-height: 360px;
  overflow: auto;
}

.view-menu.show {
  display: block;
}

.view-group + .view-group {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
}

.view-group-title {
  font-size: 0.72rem;
  color: var(--text-secondary);
  padding: 4px 10px 6px;
}

.view-menu-item {
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  border-radius: 10px;
  color: var(--text-secondary);
  cursor: pointer;
}

.view-menu-item:hover {
  background: rgba(59, 130, 246, 0.12);
  color: var(--text-primary);
}

.view-menu-item.active {
  background: rgba(59, 130, 246, 0.18);
  color: var(--text-primary);
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.view-menu-item .split-info {
  opacity: 0.8;
  font-size: 0.75rem;
}

.live-indicator {
  font-size: 0.8rem;
  color: var(--accent-green);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 20px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-green);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.85);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.video-wall {
  display: grid;
  gap: 12px;
  flex: 1;
  min-height: 0;
  grid-auto-rows: 1fr;
}

.cam-preview {
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(30, 58, 95, 0.8);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  transition: 0.25s ease;
  cursor: default;
}

.cam-preview:hover {
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 0 18px rgba(59, 130, 246, 0.18);
}

.cam-preview.offline {
  border-color: rgba(239, 68, 68, 0.35);
  opacity: 0.9;
}

.cam-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: clamp(8px, 1vh, 10px) clamp(10px, 1vw, 12px);
  background: rgba(2, 6, 23, 0.5);
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  flex: 0 0 auto;
}

.cam-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: clamp(12px, 0.85vw, 14px);
  color: var(--text-primary);
}

.cam-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-green);
}

.cam-status.offline {
  background: var(--accent-red);
}

.cam-body {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(148, 163, 184, 0.65);
  min-height: 0;
  overflow: hidden;
  background: #020617;
}

.cam-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  vertical-align: top;
}

.cam-video-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  background: rgba(2, 6, 23, 0.72);
  text-align: center;
  padding: 8px;
  z-index: 2;
}

.cam-video-error {
  color: #fca5a5;
}

.cam-spin {
  animation: cam-spin 1s linear infinite;
}

@keyframes cam-spin {
  to {
    transform: rotate(360deg);
  }
}

.cam-footer {
  display: flex;
  justify-content: space-between;
  padding: clamp(8px, 1vh, 10px) clamp(10px, 1vw, 12px);
  font-size: clamp(11px, 0.8vw, 13px);
  color: var(--text-secondary);
  background: rgba(2, 6, 23, 0.35);
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  flex: 0 0 auto;
}

.cam-max-btn {
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.25);
  color: var(--text-primary);
  width: 30px !important;
  height: 30px !important;
  min-width: 30px;
  min-height: 30px;
  max-width: 30px;
  max-height: 30px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  padding: 0;
  position: static !important;
  inset: auto !important;
  align-self: center;
}

.cam-max-btn:hover {
  background: rgba(59, 130, 246, 0.2);
}

.video-wall.single-max {
  grid-template-columns: 1fr;
  grid-template-rows: 1fr;
  grid-auto-rows: initial;
  gap: 0;
}

.cam-preview.maximized {
  aspect-ratio: auto;
}

.video-wall.single-max .cam-preview {
  display: none;
}

.video-wall.single-max .cam-preview.maximized {
  display: flex;
  grid-column: 1 / -1;
  grid-row: 1 / -1;
  height: 100%;
}

.video-controls {
  display: none;
}

.control-group {
  display: flex;
  gap: 10px;
}

.control-btn {
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.25);
  color: var(--text-primary);
  padding: 8px 12px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 0.85rem;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: 0.2s ease;
}

.control-btn:hover {
  background: rgba(59, 130, 246, 0.2);
}

.control-btn.active {
  background: rgba(16, 185, 129, 0.18);
  border-color: rgba(16, 185, 129, 0.35);
  color: #bbf7d0;
}

.maximize-controls {
  display: none;
}

.region-selector {
  display: none;
}

.alert-timeline {
  display: grid;
  grid-template-rows: repeat(3, minmax(0, 1fr));
  gap: 8px;
  min-height: 0;
  flex: 1;
}

.alert-item {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  min-height: 0;
}

.alert-item.critical {
  border-color: rgba(239, 68, 68, 0.35);
}

.alert-item.warning {
  border-color: rgba(245, 158, 11, 0.35);
}

.alert-item.info {
  border-color: rgba(59, 130, 246, 0.25);
}

.alert-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
  font-size: clamp(12px, 0.8vw, 15px);
}

.alert-meta {
  display: flex;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 11px;
  align-items: center;
}

.alert-location {
  display: inline-flex;
  gap: 6px;
  align-items: center;
}

.device-dashboard {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-height: 0;
}

.device-main-stat {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  flex: 0 0 auto;
}

.circular-chart {
  width: 100%;
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 16px;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.circular-chart svg {
  transform: rotate(-90deg);
  width: 70px;
  height: 70px;
}

.circle-bg {
  fill: none;
  stroke: rgba(148, 163, 184, 0.15);
  stroke-width: 10;
}

.circle {
  fill: none;
  stroke-width: 10;
  stroke-linecap: round;
}

.circle.online {
  stroke: url(#gradientOnline);
}

.circle.channel {
  stroke: url(#gradientChannel);
}

.chart-text {
  position: absolute;
  text-align: center;
}

.chart-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.chart-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.device-categories {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  min-height: 0;
  flex: 1;
  grid-auto-rows: minmax(0, 1fr);
  align-items: stretch;
}

.category-card {
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 16px;
  padding: 8px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  color: var(--text-primary);
  font-weight: 600;
}

.category-title {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  font-size: clamp(11px, 0.8vw, 13px);
}

.category-count {
  font-size: clamp(11px, 0.8vw, 13px);
  color: var(--text-secondary);
}

.category-bar {
  height: 6px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.12);
  overflow: hidden;
  margin-bottom: 4px;
}

.category-fill {
  height: 100%;
}

.category-fill.online {
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.8), rgba(52, 211, 153, 0.9));
}

.category-fill.channel {
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.8), rgba(96, 165, 250, 0.9));
}

.category-status {
  display: flex;
  justify-content: space-between;
  font-size: clamp(11px, 0.75vw, 12.5px);
  line-height: 1.1;
  color: var(--text-secondary);
  margin-top: auto;
  white-space: nowrap;
}

.status-online {
  color: var(--accent-green);
}

.status-channel {
  color: var(--accent-blue);
}

.status-offline {
  color: rgba(239, 68, 68, 0.85);
}

.storage-details span {
  font-size: 11px;
}

.recording-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.rec-metrics {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.metric-box {
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 16px;
  padding: clamp(10px, 1.2vh, 14px);
}

.metric-value {
  font-size: clamp(18px, 2.2vh, 22px);
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.metric-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.storage-section {
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 16px;
  padding: clamp(10px, 1.2vh, 14px);
}

.storage-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.storage-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.storage-value {
  color: var(--text-primary);
  font-weight: 600;
}

.storage-bar {
  height: 8px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.12);
  overflow: hidden;
  margin-bottom: 10px;
}

.storage-fill {
  height: 100%;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.8), rgba(52, 211, 153, 0.9));
}

.storage-details {
  display: flex;
  justify-content: space-between;
  color: var(--text-secondary);
  font-size: clamp(11px, 0.75vw, 13px);
  white-space: nowrap;
}

.distribution-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.pie-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  align-items: center;
}

.right-region-card .distribution-section {
  display: grid;
  grid-template-columns: minmax(220px, 260px) minmax(0, 1fr);
  align-items: stretch;
  gap: 12px;
  min-height: 0;
}

.right-region-card .pie-container {
  grid-template-columns: 1fr;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: start;
}

.right-region-card .pie-chart {
  justify-self: center;
  align-self: center;
}

.right-region-card .bars-container {
  min-width: 0;
  min-height: 0;
  overflow: auto;
  padding-right: 4px;
}

.right-region-card .bars-container::-webkit-scrollbar {
  width: 6px;
}

.right-region-card .bars-container::-webkit-scrollbar-track {
  background: transparent;
}

.right-region-card .bars-container::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.22);
  border-radius: 999px;
}

.right-region-card .bars-container::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.32);
}


.overview-right .metric-box {
  padding: 10px;
}

.overview-right .metric-value {
  font-size: clamp(16px, 1.8vh, 20px);
}

.overview-right .storage-section {
  padding: 10px;
}

.overview-right .pie-chart {
  height: clamp(86px, 11vh, 120px);
}

.right-bottom-grid {
  display: grid;
  grid-template-rows: minmax(0, 1fr);
  gap: 10px;
  min-height: 0;
  flex: 1;
}

.right-bottom-section {
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.right-bottom-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex: 0 0 auto;
}

.right-bottom-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: clamp(12px, 0.85vw, 14px);
  font-weight: 600;
  color: var(--text-primary);
}

.region-overview {
  display: none;
}

.pie-chart {
  width: clamp(120px, 14vh, 150px);
  height: auto;
  aspect-ratio: 1 / 1;
  border-radius: 999px;
  background: conic-gradient(
    var(--accent-blue) 0% 48%,
    var(--accent-orange) 48% 76%,
    var(--accent-green) 76% 92%,
    var(--accent-red) 92% 100%
  );
  border: 1px solid rgba(148, 163, 184, 0.12);
  position: relative;
  box-shadow: 0 0 24px rgba(59, 130, 246, 0.12);
}

.pie-center {
  position: absolute;
  inset: 22px;
  border-radius: 999px;
  background: rgba(10, 15, 26, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.12);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.pie-total {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
}

.pie-label {
  margin-top: 4px;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.12);
  color: var(--text-secondary);
}

.legend-color {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-value {
  margin-left: auto;
  font-weight: 700;
  color: var(--text-primary);
}

.bars-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bar-item {
  display: grid;
  grid-template-columns: 84px 1fr 30px;
  gap: 8px;
  align-items: center;
  padding: 6px 10px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.bar-label {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  font-size: clamp(11px, 0.8vw, 13.5px);
  color: var(--text-secondary);
}

.bar-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.12);
  overflow: hidden;
}

.bar-fill {
  height: 100%;
}

.bar-value {
  font-weight: 700;
  color: var(--text-primary);
  text-align: right;
}

.status-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(15, 23, 42, 0.8);
  padding: clamp(8px, 1vh, 10px) clamp(12px, 1.2vw, 16px);
  border-radius: 12px;
  border: 1px solid var(--border-default);
  font-size: clamp(11px, 0.8vw, 13.5px);
  color: var(--text-secondary);
  backdrop-filter: blur(10px);
  min-height: 44px;
  white-space: nowrap;
  overflow: hidden;
}

.status-group {
  display: flex;
  gap: 24px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-indicator.good {
  background: var(--accent-green);
}

.status-indicator.warn {
  background: var(--accent-orange);
}

.fullscreen-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 24px;
}

.fullscreen-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: rgba(59, 130, 246, 0.18);
  border: 1px solid rgba(59, 130, 246, 0.35);
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fullscreen-content {
  width: min(1600px, 100%);
  height: min(900px, 100%);
}

@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .right-top {
    grid-template-columns: 1fr;
  }

  .right-region-card .distribution-section {
    grid-template-columns: 1fr;
  }

  .pie-container {
    grid-template-columns: 1fr;
  }
}

@media (max-height: 820px) {
  .video-surveillance-board {
    padding: 10px;
  }

  .card {
    padding: 12px;
  }

  .cam-header,
  .cam-footer {
    padding: 8px 10px;
  }

  .pie-chart {
    height: 120px;
  }

  .pie-container {
    grid-template-columns: 1fr;
  }
}
</style>
