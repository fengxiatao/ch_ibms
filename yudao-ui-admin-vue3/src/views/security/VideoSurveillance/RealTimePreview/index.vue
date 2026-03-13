<template>
  <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="
      height: calc(100vh - var(--page-top-gap, 70px));
      padding-top: var(--page-top-gap, 70px);
      margin-bottom: 0;
    "
  >
    <div class="dark-theme-page">
      <div class="realtime-preview-container">
        <!-- 左侧面板：设备树 + 视图 + 云台 -->
        <div class="left-panel">
          <DeviceTreePanel
            ref="deviceTreeRef"
            :playing-channel-ids="playingChannelIds"
            :ptz-enabled="activePaneSupportsPtz"
            @channel-play="handleChannelPlay"
            @channel-drag-start="handleChannelDragStart"
          >
            <!-- 视图管理插槽 -->
            <template #views>
              <ViewManager 
                ref="viewManagerRef" 
                @load-view="handleLoadView"
                @save-view="handleSaveViewSubmit"
              />
            </template>

            <!-- 云台控制插槽 -->
            <template #ptz>
              <PtzControlPanel
                ref="ptzControlRef"
                :enabled="activePaneSupportsPtz"
                :channel-no="activeChannelNo"
                :channel-id="activeChannelId"
                :disabled-reason="ptzDisabledReason"
                @ptz-move="handlePtzMove"
                @ptz-stop="handlePtzStop"
                @goto-preset="handleGotoPreset"
                @set-preset="handleSetPreset"
                @clear-preset="handleClearPreset"
                @open-cruise-manager="handleOpenCruiseManager"
                @toggle-area-zoom="handleToggleAreaZoom"
                @ptz-reset="handlePtzReset"
              />
            </template>
          </DeviceTreePanel>
        </div>

        <!-- 中间面板：播放器网格 -->
        <div class="center-panel">
          <!-- WebRTC 测试开关（不改默认逻辑；点击会改 URL 并刷新） -->
          <div class="mode-indicator">
            <span class="mode-text">当前模式：{{ isIntranet ? '大华直连(RPC2)' : 'ZLM(WebRTC优先)' }}</span>
            <el-button size="small" type="primary" plain @click="toggleForceWebrtc">
              {{ forceWebrtc ? '关闭强制WebRTC' : '强制WebRTC测试' }}
            </el-button>
          </div>
          <VideoPlayerGrid
            ref="playerGridRef"
            :panes="panes"
            :active-pane="activePane"
            :grid-layout="gridLayout"
            :current-view-name="currentView?.name"
            :is-patrolling="isPatrolling"
            :area-zoom-active="areaZoomActive"
            @update:active-pane="activePane = $event"
            @pane-drop="handlePaneDrop"
            @pane-ref="handlePaneRef"
            @stop="handleStopPane"
            @snapshot="handleSnapshot"
            @record="handleRecord"
            @fullscreen="handleFullscreen"
            @stream-switch="handleStreamSwitch"
            @stop-all="handleStopAll"
            @view-save-as="handleViewSaveAs"
            @view-update="handleViewUpdate"
            @retry="handleRetry"
            @layout-change="handleLayoutChange"
            @area-zoom="handleAreaZoom"
          >
            <!-- 轮巡控制插槽 -->
            <template #patrol-controls>
              <PatrolManager
                ref="patrolManagerRef"
                @start="handlePatrolStart"
                @stop="handlePatrolStop"
                @execute-scene="handleExecuteScene"
              />
            </template>
          </VideoPlayerGrid>
        </div>
      </div>
    </div>

    <!-- 巡航管理对话框 -->
    <CruiseManager
      ref="cruiseManagerRef"
      :channel-id="activeChannelId || 0"
      :device-id="0"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'

// 组件导入
import DeviceTreePanel from './components/DeviceTreePanel.vue'
import VideoPlayerGrid from './components/VideoPlayerGrid.vue'
import PtzControlPanel from './components/PtzControlPanel.vue'
import ViewManager from './components/ViewManager.vue'
import PatrolManager from './components/PatrolManager.vue'
import CruiseManager from '@/views/security/VideoSurveillance/components/CruiseManager.vue'

// 大华播放器 Composable
import {
  useDahuaPlayer,
  DEFAULT_NVR_CONFIG,
  type DahuaPlayerConfig,
  type DahuaPlayerPane
} from '@/composables/useDahuaPlayer'

// ZLMediaKit 播放封装
import useZlmPlayer, { type ZlmPlayerInstance } from '@/composables/useZlmPlayer'

// API 导入
import { uploadCameraSnapshot } from '@/api/iot/video'
import { getLivePlayUrl, stopStream, type PlayUrlRespVO } from '@/api/iot/video/zlm'
import { nvrPresetControl, nvrPtzControl, nvrAreaZoom } from '@/api/iot/video/nvr'

// 类型导入
import type { GridLayoutType, VideoView, PatrolScene, PatrolSceneChannel, IbmsChannel } from './types'
import { createVideoView, updateVideoView } from '@/api/iot/video/videoView'

defineOptions({ name: 'RealTimePreview' })

// ==================== 组件引用 ====================
const deviceTreeRef = ref()
const viewManagerRef = ref()
const patrolManagerRef = ref()
const cruiseManagerRef = ref()

// ==================== 大华播放器 / ZLM 播放 ====================
const {
  createPanes,
  startPreview,
  stopPlayer,
  stopAllPlayers,
  capture,
  captureWithData,
  base64ToFile,
  switchStream,
  toggleRecord,
  ptzMove,
  ptzZoom,
  gotoPreset,
  setPreset,
  clearPreset,
  loginDevice,
  areaZoom,
  ptzReset
} = useDahuaPlayer()

const { playLive: playZlmLive, stopInstance: stopZlmInstance } = useZlmPlayer()

// 判断是否内网访问（与其他页面保持一致规则）
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

// 强制 WebRTC 测试开关：内网访问也走 ZLM(WebRTC 优先)
// 用法示例：在地址栏追加 ?forceWebrtc=1
const forceWebrtc = (() => {
  try {
    return new URLSearchParams(window.location.search).get('forceWebrtc') === '1'
  } catch {
    return false
  }
})()

const isIntranet = isIntranetAccess() && !forceWebrtc

const toggleForceWebrtc = () => {
  try {
    // 仅修改 query，严格保留当前 path/hash（避免切换后跳到根路由导致 404）
    const params = new URLSearchParams(window.location.search)
    if (params.get('forceWebrtc') === '1') params.delete('forceWebrtc')
    else params.set('forceWebrtc', '1')
    const nextSearch = params.toString()
    const nextUrl = `${window.location.pathname}${nextSearch ? `?${nextSearch}` : ''}${window.location.hash}`
    window.history.replaceState(null, '', nextUrl)
    window.location.reload()
  } catch {
    // ignore
  }
}

// 内网 IP 正则（192.168.x.x、10.x.x.x、172.16-31.x.x）
const INTRANET_HOST_REG = /(?:192\.168\.\d+\.\d+|10\.\d+\.\d+\.\d+|172\.(?:1[6-9]|2\d|3[01])\.\d+\.\d+)(?::\d+)?/g

// 适配播放地址（外网时将内网 IP 替换为公网可访问地址）
// - ws-flv：通常跟随站点域名/IP（同源或反代）
// - webrtc：很多部署会走独立的 rtcHost/rtcPort（参考旧版实现），因此允许通过 env 覆盖
const adaptPlayUrls = (urls: PlayUrlRespVO | undefined | null): PlayUrlRespVO | null => {
  if (!urls) return null
  if (isIntranet) return urls

  const adapted: PlayUrlRespVO = { ...urls }
  const publicHost = window.location.hostname
  // 本地开发环境（localhost/127.0.0.1）不应把内网流媒体地址改写到本机端口，否则会出现
  // webrtcUrl/wsFlvUrl 指向 http://localhost:3000/... 导致 404。
  if (publicHost === 'localhost' || publicHost === '127.0.0.1') {
    return urls
  }
  const publicPort = window.location.port ? parseInt(window.location.port) : 80
  const publicAddr = publicPort === 80 || publicPort === 443 ? publicHost : `${publicHost}:${publicPort}`
  const isHttps = window.location.protocol === 'https:'
  const httpProtocol = isHttps ? 'https' : 'http'
  const wsProtocol = isHttps ? 'wss' : 'ws'

  const envRtcHost = (import.meta as any).env?.VITE_ZLM_RTC_HOST as string | undefined
  const envRtcPortRaw = (import.meta as any).env?.VITE_ZLM_RTC_PORT as string | undefined
  const envRtcPort = envRtcPortRaw ? parseInt(envRtcPortRaw) : undefined

  /** 将 URL 中的内网 host:port 替换为公网，并统一协议 */
  const replaceHttpUrl = (url?: string): string | undefined => {
    if (!url) return url
    let newUrl = url.replace(INTRANET_HOST_REG, publicAddr)
    newUrl = newUrl.replace(/^http:/, `${httpProtocol}:`)
    return newUrl
  }

  const replaceWsUrl = (url?: string): string | undefined => {
    if (!url) return url
    let newUrl = url.replace(INTRANET_HOST_REG, publicAddr)
    newUrl = newUrl.replace(/^ws:/, `${wsProtocol}:`)
    return newUrl
  }

  const replaceWebrtcUrl = (url?: string): string | undefined => {
    if (!url) return url
    let newUrl = replaceHttpUrl(url) || url
    // 如果部署把 WebRTC 信令单独暴露到公网（旧版实现常见），允许用 env 覆盖 host/port
    if (envRtcHost) {
      try {
        const u = new URL(newUrl)
        u.hostname = envRtcHost
        if (envRtcPort && !Number.isNaN(envRtcPort)) {
          u.port = String(envRtcPort)
        }
        const envSecret = (import.meta as any).env?.VITE_ZLM_SECRET as string | undefined
        if (envSecret && !u.searchParams.get('secret')) {
          u.searchParams.set('secret', envSecret)
        }
        newUrl = u.toString()
      } catch {
        // ignore
      }
    } else {
      // 即使不覆盖 host/port，也允许注入 secret（ZLM 若开启鉴权，需要带 secret）
      try {
        const u = new URL(newUrl)
        const envSecret = (import.meta as any).env?.VITE_ZLM_SECRET as string | undefined
        if (envSecret && !u.searchParams.get('secret')) {
          u.searchParams.set('secret', envSecret)
          newUrl = u.toString()
        }
      } catch {
        // ignore
      }
    }
    return newUrl
  }

  adapted.wsFlvUrl = replaceWsUrl(urls.wsFlvUrl)
  // ZLMediaKit WebRTC：外网使用公网地址，格式为 http(s)://host:port/index/api/webrtc?app=live&stream=xxx&type=play
  adapted.webrtcUrl = replaceWebrtcUrl(urls.webrtcUrl)
  adapted.wsFmp4Url = replaceWsUrl(urls.wsFmp4Url)
  adapted.flvUrl = replaceHttpUrl(urls.flvUrl)
  adapted.fmp4Url = replaceHttpUrl(urls.fmp4Url)
  adapted.hlsUrl = replaceHttpUrl(urls.hlsUrl)
  adapted.rtmpUrl = replaceHttpUrl(urls.rtmpUrl)
  return adapted
}

// 播放器扩展状态（在 Dahua 窗格上附加 ZLM 播放信息）
type ExtendedPane = DahuaPlayerPane & {
  ibmsChannel?: IbmsChannel | null
  zlmInstance?: ZlmPlayerInstance | null
  zlmChannelId?: number | null
}

// ==================== 播放器状态 ====================
const gridLayout = ref<GridLayoutType>(6)
// 使用类型断言兼容组件 Props
const panes = ref(createPanes(6) as ExtendedPane[])
const activePane = ref(0)
const currentView = ref<VideoView | null>(null)
const isPatrolling = ref(false)
const areaZoomActive = ref(false)  // 区域放大模式

// ==================== 计算属性 ====================

// 正在播放的通道 ID 列表（用于设备树高亮）
const playingChannelIds = computed(() => {
  return panes.value
    .filter((p) => p.isPlaying && (p.config || p.ibmsChannel))
    .map((p) => {
      if (p.config?.channelNo) return p.config.channelNo
      const ibmsChannel = p.ibmsChannel as any
      return ibmsChannel?.channelNo
    })
    .filter((id) => !!id)
})

// 当前活动窗口的通道号（用于云台控制）
const activeChannelNo = computed(() => {
  const pane = panes.value[activePane.value]
  if (!pane) return null
  if (pane.config?.channelNo) return pane.config.channelNo
  const ibmsChannel = pane.ibmsChannel as any
  return ibmsChannel?.channelNo || null
})

// 当前活动窗口的通道ID（数据库ID，用于查询预设点等）
const activeChannelId = computed(() => {
  const pane = panes.value[activePane.value]
  const ibmsChannel = pane?.ibmsChannel as any
  return ibmsChannel?.id || null
})

// 当前活动窗口的设备ID（用于调用后端API）
const activeDeviceId = computed(() => {
  const pane = panes.value[activePane.value]
  const ibmsChannel = pane?.ibmsChannel as any
  return ibmsChannel?.deviceId || null
})

// 当前活动窗口是否支持云台
const activePaneSupportsPtz = computed(() => {
  const pane = panes.value[activePane.value]
  // 必须同时满足：窗口正在播放 且 有通道信息 且 通道支持云台控制
  if (!pane || !pane.isPlaying) {
    return false
  }
  // 检查 IBMS 通道是否支持云台
  // 注意：ptzSupport 可能是 boolean (true/false) 或 number (1/0)
  const ibmsChannel = pane.ibmsChannel as any
  if (ibmsChannel) {
    const ptz = ibmsChannel.ptzSupport
    // 严格判断：只有明确为 true 或 1 才支持云台
    return ptz === true || ptz === 1
  }
  // 没有通道信息，默认不支持
  return false
})

// 云台禁用原因
const ptzDisabledReason = computed<'no-playing' | 'no-ptz' | null>(() => {
  const pane = panes.value[activePane.value]
  // 没有播放通道
  if (!pane || !pane.isPlaying) {
    return 'no-playing'
  }
  // 通道不支持云台
  const ibmsChannel = pane.ibmsChannel as any
  if (ibmsChannel) {
    const ptz = ibmsChannel.ptzSupport
    // 不支持云台的情况：明确为 false/0，或者字段不存在
    if (ptz !== true && ptz !== 1) {
      return 'no-ptz'
    }
  } else {
    return 'no-ptz'
  }
  return null
})

// ==================== 布局管理 ====================

const handleLayoutChange = async (newLayout: GridLayoutType) => {
  // 停止所有播放
  await stopAllPanes()

  // 更新布局
  gridLayout.value = newLayout
  panes.value = createPanes(newLayout) as ExtendedPane[]
  activePane.value = 0
}

// 设置窗格容器元素引用
const handlePaneRef = (paneIndex: number, el: HTMLElement | null) => {
  if (paneIndex >= 0 && paneIndex < panes.value.length) {
    panes.value[paneIndex].container = el
  }
}

// ==================== 通用停止逻辑 ====================

const stopPanePlayback = async (pane: ExtendedPane) => {
  if (!pane) return

  if (isIntranet) {
    await stopPlayer(pane)
    return
  }

  // 外网模式：通过 ZLM 停止
  if (pane.zlmInstance) {
    stopZlmInstance(pane.zlmInstance)
    pane.zlmInstance = null
  }

  if (pane.container) {
    pane.container.innerHTML = ''
  }

  if (pane.zlmChannelId) {
    // 通知后端停止拉流
    stopStream(pane.zlmChannelId).catch(() => {})
  }

  pane.isPlaying = false
  pane.isLoading = false
  pane.isRecording = false
  pane.error = null
}

const stopAllPanes = async () => {
  if (isIntranet) {
    await stopAllPlayers(panes.value)
    return
  }

  const list = panes.value.slice()
  for (const pane of list) {
    // eslint-disable-next-line no-await-in-loop
    await stopPanePlayback(pane)
  }
}

// ==================== 通道播放 ====================

// 播放通道（双击）
const handleChannelPlay = async (ibmsChannel: IbmsChannel) => {
  if (!ibmsChannel) {
    ElMessage.warning('请选择有效的通道')
    return
  }

  const totalPanes = panes.value.length
  let targetIdx = activePane.value

  // 如果当前焦点窗口正在播放，直接使用下一个顺序窗口
  if (panes.value[targetIdx]?.isPlaying) {
    targetIdx = (activePane.value + 1) % totalPanes
  }

  await playChannelInPane(ibmsChannel, targetIdx)
}

// 拖拽通道到窗格
const handlePaneDrop = async (event: DragEvent, paneIndex: number) => {
  try {
    // 获取 IBMS 通道数据
    let ibmsChannel: IbmsChannel | null = null
    try {
      ibmsChannel = JSON.parse(event.dataTransfer!.getData('ibmsChannel'))
    } catch {}

    if (!ibmsChannel) {
      // 兼容旧的 channel 格式
      const channelData = event.dataTransfer!.getData('channel')
      if (channelData) {
        const channel = JSON.parse(channelData)
        // 尝试从 DeviceTreePanel 获取 ibmsChannel
        ibmsChannel = deviceTreeRef.value?.findIbmsChannelByWvpId(channel.commonGbChannelId)
      }
    }

    if (!ibmsChannel) {
      ElMessage.warning('无法获取通道信息')
      return
    }

    await playChannelInPane(ibmsChannel, paneIndex)
  } catch (err) {
    console.error('[拖拽] 解析失败:', err)
  }
}

// 在指定窗格播放通道（根据模式选择大华直连或 ZLM 播放）
const playChannelInPane = async (ibmsChannel: IbmsChannel, paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 如果当前窗格正在播放，先停止
  if (pane.isPlaying || pane.player || pane.zlmInstance) {
    await stopPanePlayback(pane)
  }

  // 切换到该窗格
  activePane.value = paneIndex

  // 等待容器就绪
  await nextTick()
  if (!pane.container) {
    const el = document.querySelector(
      `.player-pane[data-index="${paneIndex}"] .player-container`
    ) as HTMLElement
    if (el) {
      pane.container = el
    }
  }

  // 保存 IBMS 通道信息（用于截图上传等功能）
  pane.ibmsChannel = ibmsChannel

  // 内网：大华直连
  if (isIntranet) {
    // 构建播放配置，使用 NVR 作为 WebSocket 连接点（IPC 不支持 WebSocket）
    const config: DahuaPlayerConfig = {
      ip: DEFAULT_NVR_CONFIG.ip,
      port: DEFAULT_NVR_CONFIG.port,
      rtspPort: DEFAULT_NVR_CONFIG.rtspPort, // RTSP 端口
      username: ibmsChannel.username || DEFAULT_NVR_CONFIG.username,
      password: ibmsChannel.password || DEFAULT_NVR_CONFIG.password,
      channelNo: ibmsChannel.channelNo,
      subtype: pane.streamType === 'sub' ? 1 : 0
    }

    // 开始播放
    const success = await startPreview(pane, config, ibmsChannel.channelName)

    if (success) {
      ElMessage.success(`正在播放: ${ibmsChannel.channelName || `通道${config.channelNo}`}`)
    }
    return
  }

  // 外网：通过后端 ZLM 拉流播放
  const channelId = ibmsChannel.id
  if (!channelId) {
    ElMessage.warning('通道ID缺失，无法通过流媒体播放')
    return
  }

  try {
    pane.isLoading = true
    pane.isPlaying = false
    pane.error = null

    const rawUrls = await getLivePlayUrl(channelId, pane.streamType === 'sub' ? 1 : 0)
    const playUrls = adaptPlayUrls(rawUrls)

    if (!playUrls || (!playUrls.wsFlvUrl && !playUrls.webrtcUrl)) {
      throw new Error('未获取到可用的播放地址')
    }

    if (!pane.container) {
      const el = document.querySelector(
        `.player-pane[data-index="${paneIndex}"] .player-container`
      ) as HTMLElement
      if (el) {
        pane.container = el
      }
    }

    if (!pane.container) {
      throw new Error('播放器容器不存在')
    }

    const instance = await playZlmLive({
      container: pane.container,
      urls: {
        wsFlvUrl: playUrls.wsFlvUrl,
        webrtcUrl: playUrls.webrtcUrl
      },
      preferWebrtc: true
    })

    pane.zlmInstance = instance
    pane.zlmChannelId = channelId
    pane.isPlaying = true
    pane.isLoading = false
    pane.error = null

    ElMessage.success(`正在播放: ${ibmsChannel.channelName || `通道${ibmsChannel.channelNo}`}`)
  } catch (e: any) {
    console.error('[ZLM] 播放失败:', e)
    pane.isLoading = false
    pane.isPlaying = false
    pane.error = e?.message || '播放失败，请稍后重试'
  }
}

// 停止窗格播放
const handleStopPane = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  await stopPanePlayback(pane)
  ElMessage.success('已停止播放')
}

// 停止所有播放
const handleStopAll = async () => {
  if (isPatrolling.value) {
    patrolManagerRef.value?.stopPatrol()
    return
  }

  const playingCount = panes.value.filter((p) => p.isPlaying).length
  if (playingCount === 0) {
    ElMessage.warning('当前没有正在播放的视频')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要停止所有正在播放的视频吗？（共 ${playingCount} 个窗口）`,
      '停止所有播放器',
      { type: 'warning' }
    )

    await stopAllPanes()
    ElMessage.success('已停止所有播放')
  } catch {
    // 取消
  }
}

// 重试播放
const handleRetry = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  if (isIntranet) {
    if (!pane.config) return
    // 重新播放（大华直连）
    await stopPlayer(pane)
    await startPreview(pane, pane.config, pane.channelName)
    return
  }

  // 外网模式：按当前 ibmsChannel 重新通过 ZLM 播放
  const ibmsChannel = pane.ibmsChannel as IbmsChannel | undefined
  if (!ibmsChannel) {
    ElMessage.warning('当前窗口缺少通道信息，无法重试播放')
    return
  }
  await playChannelInPane(ibmsChannel, paneIndex)
}

// 拖拽开始（透传）
const handleChannelDragStart = (_event: DragEvent, _channel: any) => {
  // 已在 DeviceTreePanel 中处理
}

// ==================== 截图/录像/全屏 ====================

const handleSnapshot = async (paneIndex: number) => {
  const pane = panes.value[paneIndex] as any // 包含 ibmsChannel 扩展字段
  if (!pane || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }

  try {
    // 获取截图数据
    const { base64, fileName, mimeType } = captureWithData(pane, 'jpg', 1.0)
    
    if (!base64) {
      // 如果获取不到数据，退回到本地截图
      capture(pane, `snapshot_${pane.channelName}_${Date.now()}`)
      return
    }

    // 转换为 File 对象
    const file = base64ToFile(base64, fileName, mimeType)

    // 获取通道ID（从 ibmsChannel 中获取数据库ID，而不是 NVR 通道号）
    const ibmsChannel = pane.ibmsChannel
    if (!ibmsChannel || !ibmsChannel.id) {
      console.warn('[截图] 未找到通道信息，保存到本地')
      capture(pane, `snapshot_${pane.channelName}_${Date.now()}`)
      ElMessage.warning('未找到通道信息，截图已保存到本地')
      return
    }

    const channelId = ibmsChannel.id // 使用数据库中的通道ID
    
    // 上传到服务器
    ElMessage.info('正在上传截图...')
    await uploadCameraSnapshot(channelId, file, 1) // snapshotType: 1 = 手动抓拍
    
    ElMessage.success('截图已保存到服务器')
    console.log('[截图] 上传成功:', fileName, '通道ID:', channelId, '通道名称:', ibmsChannel.channelName)
  } catch (error: any) {
    console.error('[截图] 上传失败:', error)
    // 上传失败时，保存到本地
    capture(pane, `snapshot_${pane.channelName}_${Date.now()}`)
    ElMessage.warning('上传失败，截图已保存到本地')
  }
}

// 录像操作
const handleRecord = async (paneIndex: number) => {
  const pane = panes.value[paneIndex] as DahuaPlayerPane
  if (!pane) return

  await toggleRecord(pane)
}

const handleFullscreen = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane?.container) return

  if (pane.container.requestFullscreen) {
    pane.container.requestFullscreen()
  }
}

// ==================== 码流切换 ====================

const handleStreamSwitch = async (paneIndex: number, streamType: string) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  ElMessage.info(`正在切换到${streamType === 'main' ? '主' : '子'}码流...`)

  pane.streamType = streamType as 'main' | 'sub'

  if (isIntranet) {
    if (!pane.config) return
    // 切换大华码流
    const success = await switchStream(pane, streamType as 'main' | 'sub')
    if (success) {
      ElMessage.success(`已切换到${streamType === 'main' ? '主' : '子'}码流`)
    }
    return
  }

  // 外网模式：重新获取播放地址并通过 ZLM 重播
  const ibmsChannel = pane.ibmsChannel as IbmsChannel | undefined
  if (!ibmsChannel) {
    ElMessage.warning('当前窗口缺少通道信息，无法切换码流')
    return
  }

  await playChannelInPane(ibmsChannel, paneIndex)
}

// ==================== 云台控制 ====================

const handlePtzMove = async (command: string, speed: number) => {
  const channelNo = activeChannelNo.value
  if (!channelNo) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  // 内网：直接通过 RPC2 控制
  if (isIntranet) {
    // 先登录设备
    await loginDevice(DEFAULT_NVR_CONFIG)

    // 映射命令名称
    const directionMap: Record<string, any> = {
      up: 'Up',
      down: 'Down',
      left: 'Left',
      right: 'Right',
      upleft: 'LeftUp',
      upright: 'RightUp',
      downleft: 'LeftDown',
      downright: 'RightDown',
      zoomin: 'ZoomTele',
      zoomout: 'ZoomWide',
      focusin: 'FocusNear',
      focusout: 'FocusFar'
    }

    const dahuaCommand = directionMap[command.toLowerCase()] || command

    try {
      if (['ZoomTele', 'ZoomWide', 'FocusNear', 'FocusFar'].includes(dahuaCommand)) {
        await ptzZoom(channelNo, dahuaCommand, Math.ceil(speed / 30))
      } else {
        await ptzMove(channelNo, dahuaCommand, Math.ceil(speed / 30))
      }
    } catch (error: any) {
      console.error('[云台] 控制失败:', error)
      ElMessage.error('云台控制失败')
    }
    return
  }

  // 外网：通过后端 NVR PTZ 接口控制
  const nvrId = activeDeviceId.value
  if (!nvrId) {
    ElMessage.warning('无法获取 NVR 设备ID，云台控制不可用')
    return
  }

  const cmdMap: Record<string, string> = {
    up: 'UP',
    down: 'DOWN',
    left: 'LEFT',
    right: 'RIGHT',
    upleft: 'LEFT_UP',
    upright: 'RIGHT_UP',
    downleft: 'LEFT_DOWN',
    downright: 'RIGHT_DOWN',
    zoomin: 'ZOOM_IN',
    zoomout: 'ZOOM_OUT',
    focusin: 'FOCUS_NEAR',
    focusout: 'FOCUS_FAR'
  }

  const ptzCommand = cmdMap[command.toLowerCase()] || command.toUpperCase()
  const speedLevel = Math.min(8, Math.max(1, Math.ceil(speed / 32)))

  try {
    await nvrPtzControl(nvrId, {
      channelNo,
      command: ptzCommand,
      speed: speedLevel,
      stop: false
    })
  } catch (error: any) {
    console.error('[云台] 后端 PTZ 控制失败:', error)
    ElMessage.error('云台控制失败')
  }
}

const handlePtzStop = async () => {
  const channelNo = activeChannelNo.value
  if (!channelNo) return

  // 内网：调用 RPC2 停止
  if (isIntranet) {
    try {
      // 停止所有方向
      await ptzMove(channelNo, 'Up', 5, true)
    } catch (error) {
      console.error('[云台] 停止失败:', error)
    }
    return
  }

  // 外网：调用后端 PTZ 停止
  const nvrId = activeDeviceId.value
  if (!nvrId) return

  try {
    await nvrPtzControl(nvrId, {
      channelNo,
      command: 'STOP',
      stop: true
    })
  } catch (error) {
    console.error('[云台] 后端停止失败:', error)
  }
}

// ==================== 预设位 ====================

const handleGotoPreset = async (presetId: number) => {
  const channelNo = activeChannelNo.value
  if (!channelNo) return

  // 内网：通过 RPC2 调用预设位
  if (isIntranet) {
    await loginDevice(DEFAULT_NVR_CONFIG)
    await gotoPreset(channelNo, presetId)
    return
  }

  // 外网：通过后端 PTZ 接口
  const nvrId = activeDeviceId.value
  if (!nvrId) return

  try {
    await nvrPresetControl(nvrId, {
      channelNo,
      presetNo: presetId,
      action: 'GOTO'
    })
  } catch (error) {
    console.error('[云台] 后端调用预设位失败:', error)
    ElMessage.error('调用预设位失败')
  }
}

const handleSetPreset = async (presetNo: number, presetName: string) => {
  const channelNo = activeChannelNo.value
  const deviceId = activeDeviceId.value
  if (!channelNo) return

  // 1) 设置预设点位置
  if (isIntranet) {
    await loginDevice(DEFAULT_NVR_CONFIG)
    await setPreset(channelNo, presetNo)
  }

  // 2) 通过后端 API 设置预设点名称（内外网统一）
  if (deviceId) {
    try {
      await nvrPresetControl(deviceId, {
        channelNo,
        presetNo,
        action: 'SET',
        presetName
      })
      console.log(`[云台] 预设点名称 "${presetName}" 已同步到设备 (deviceId=${deviceId})`)
    } catch (error) {
      console.error('[云台] 同步预设点名称失败:', error)
      // 名称同步失败不影响预设点位置的保存，只记录日志
    }
  } else {
    console.warn('[云台] 无法获取设备ID，跳过名称同步')
  }
}

const handleClearPreset = async (presetId: number) => {
  const channelNo = activeChannelNo.value
  if (!channelNo) return

  // 内网：通过 RPC2 删除预设位
  if (isIntranet) {
    await loginDevice(DEFAULT_NVR_CONFIG)
    await clearPreset(channelNo, presetId)
    return
  }

  // 外网：通过后端接口删除
  const nvrId = activeDeviceId.value
  if (!nvrId) return

  try {
    await nvrPresetControl(nvrId, {
      channelNo,
      presetNo: presetId,
      action: 'CLEAR'
    })
  } catch (error) {
    console.error('[云台] 后端删除预设位失败:', error)
    ElMessage.error('删除预设位失败')
  }
}

// ==================== 区域放大 ====================

const handleToggleAreaZoom = (active: boolean) => {
  areaZoomActive.value = active
  if (active) {
    ElMessage.info('请在视频画面上框选要放大的区域')
  }
}

const handleAreaZoom = async (
  paneIndex: number, 
  rect: { startX: number; startY: number; endX: number; endY: number }
) => {
  const pane = panes.value[paneIndex]
  const ibmsChannel = pane?.ibmsChannel as any
  const channelNo = ibmsChannel?.channelNo || pane?.config?.channelNo
  if (!channelNo) {
    ElMessage.warning('该窗口没有播放视频')
    return
  }

  const container = pane.container as HTMLElement

  // 内网：通过 RPC2 的 3D 定位实现区域放大
  if (isIntranet) {
    try {
      await loginDevice(DEFAULT_NVR_CONFIG)
      await areaZoom(
        channelNo, 
        rect,
        container?.clientWidth || 1920,
        container?.clientHeight || 1080
      )
      ElMessage.success('区域放大执行成功')
      
      // 执行成功后自动退出区域放大模式
      areaZoomActive.value = false
    } catch (error: any) {
      console.error('[云台] 区域放大失败:', error)
      ElMessage.error('区域放大失败')
    }
    return
  }

  // 外网：通过后端 NVR 区域放大接口
  const nvrId = activeDeviceId.value
  if (!nvrId) return

  try {
    const scale = 8192
    const startX = Math.round(rect.startX * scale)
    const startY = Math.round(rect.startY * scale)
    const endX = Math.round(rect.endX * scale)
    const endY = Math.round(rect.endY * scale)

    await nvrAreaZoom(nvrId, {
      channelNo,
      startX,
      startY,
      endX,
      endY
    })
    ElMessage.success('区域放大执行成功')
    areaZoomActive.value = false
  } catch (error: any) {
    console.error('[云台] 后端区域放大失败:', error)
    ElMessage.error('区域放大失败')
  }
}

const handlePtzReset = async () => {
  const channelNo = activeChannelNo.value
  if (!channelNo) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  // 内网：通过 RPC2 执行复位
  if (isIntranet) {
    try {
      ElMessage.info('正在复位，请稍候...')
      await loginDevice(DEFAULT_NVR_CONFIG)
      await ptzReset(channelNo, 3000)  // 持续缩小3秒
      ElMessage.success('云台复位完成')
    } catch (error: any) {
      console.error('[云台] 复位失败:', error)
      ElMessage.error('云台复位失败')
    }
    return
  }

  // 外网：通过后端 PTZ 复位（使用持续 ZOOM_OUT 的方式由后端实现）
  const nvrId = activeDeviceId.value
  if (!nvrId) return

  try {
    ElMessage.info('正在复位，请稍候...')
    await nvrPtzControl(nvrId, {
      channelNo,
      command: 'RESET',
      speed: 8,
      stop: false
    })
    ElMessage.success('云台复位完成')
  } catch (error: any) {
    console.error('[云台] 后端复位失败:', error)
    ElMessage.error('云台复位失败')
  }
}

// ==================== 巡航管理 ====================

const handleOpenCruiseManager = () => {
  if (!activeChannelId.value) {
    ElMessage.warning('请先选择一个通道')
    return
  }
  cruiseManagerRef.value?.open()
}

// ==================== 视图管理 ====================

// 保存为新视图（弹出对话框）
const handleViewSaveAs = async () => {
  // 打开保存对话框，不传 id 表示新建
  viewManagerRef.value?.openSaveDialog(undefined, '', [1])
}

// 更新当前视图
const handleViewUpdate = async () => {
  if (!currentView.value?.id) {
    ElMessage.warning('当前没有加载视图')
    return
  }

  try {
    // 收集当前窗格配置
    const viewData = {
      id: currentView.value.id,
      name: currentView.value.name,
      groupIds: currentView.value.groupIds,
      gridLayout: gridLayout.value,
      panes: panes.value
        .map((pane, index) => ({
          paneIndex: index,
          channelId: pane.config?.channelNo,
          channelName: pane.channelName
        }))
        .filter((p) => p.channelId)
    }

    await updateVideoView(viewData)
    ElMessage.success(`视图 "${currentView.value.name}" 已更新`)
    viewManagerRef.value?.loadViewGroups()
  } catch (error) {
    console.error('[视图] 更新失败:', error)
    ElMessage.error('更新视图失败')
  }
}

// 保存视图提交（从 ViewManager 对话框触发）
const handleSaveViewSubmit = async (viewInfo: { id?: number; name: string; groupIds: number[] }) => {
  try {
    // 收集当前窗格配置
    const viewData = {
      id: viewInfo.id,
      name: viewInfo.name,
      groupIds: viewInfo.groupIds,
      gridLayout: gridLayout.value,
      panes: panes.value
        .map((pane, index) => ({
          paneIndex: index,
          channelId: pane.config?.channelNo,
          channelName: pane.channelName
        }))
        .filter((p) => p.channelId)
    }

    if (viewInfo.id) {
      // 更新现有视图
      await updateVideoView(viewData)
      ElMessage.success(`视图 "${viewInfo.name}" 已更新`)
    } else {
      // 创建新视图
      const newId = await createVideoView(viewData)
      // 设置为当前视图
      currentView.value = {
        id: newId as number,
        name: viewInfo.name,
        groupIds: viewInfo.groupIds,
        gridLayout: gridLayout.value,
        panes: viewData.panes
      } as VideoView
      ElMessage.success(`视图 "${viewInfo.name}" 已保存`)
    }

    viewManagerRef.value?.loadViewGroups()
    // 更新 ViewManager 中的当前视图选中状态
    if (currentView.value?.id) {
      viewManagerRef.value?.setCurrentView(currentView.value.id)
    }
  } catch (error) {
    console.error('[视图] 保存失败:', error)
    ElMessage.error('保存视图失败')
  }
}

const handleLoadView = async (view: VideoView) => {
  try {
    // 加载视图
    console.log('[视图] 加载:', view.name, view)

    // 先停止所有播放
    await stopAllPanes()

    // 设置布局
    const layoutValue = (view.layout || view.gridLayout || 4) as GridLayoutType
    gridLayout.value = layoutValue
    panes.value = createPanes(layoutValue)

    await nextTick()
    await new Promise((r) => setTimeout(r, 300))

    currentView.value = view

    // 恢复窗格通道
    if (view.panes && view.panes.length > 0) {
      for (let i = 0; i < view.panes.length; i++) {
        const paneData = view.panes[i]
        const channelNo = paneData.channelId

        if (channelNo && paneData.paneIndex < panes.value.length) {
          // 查找 IBMS 通道
          const ibmsChannel = await deviceTreeRef.value?.findIbmsChannelByChannelNo(channelNo)
          if (ibmsChannel) {
            console.log('[视图] 恢复窗格:', paneData.paneIndex + 1, ibmsChannel.channelName)
            setTimeout(() => {
              playChannelInPane(ibmsChannel, paneData.paneIndex)
            }, i * 500)
          }
        }
      }
    }

    // 更新 ViewManager 中的当前视图选中状态
    if (view.id) {
      viewManagerRef.value?.setCurrentView(view.id)
    }
  } catch (error) {
    console.error('[视图] 加载失败:', error)
    ElMessage.error('加载视图失败')
  }
}

// ==================== 轮巡管理 ====================

const handlePatrolStart = (plan: any, _tasks: any[], _scenesMap: Map<number, PatrolScene>) => {
  isPatrolling.value = true
  console.log('[轮巡] 已启动:', plan.planName)
}

const handlePatrolStop = () => {
  isPatrolling.value = false
  stopAllPlayers(panes.value)
  ElMessage.info('轮巡已停止')
}

// 轮巡场景的轮播定时器（按格子分组）
const sceneRotationTimers = ref<Map<number, number>>(new Map())

const handleExecuteScene = async (scene: PatrolScene) => {
  console.log('[轮巡] 执行场景:', scene.sceneName, '通道数据:', scene.channels)

  // 清除之前的轮播定时器
  sceneRotationTimers.value.forEach((timer) => clearTimeout(timer))
  sceneRotationTimers.value.clear()

  // 停止当前所有播放
  await stopAllPanes()

  // 解析分屏布局
  let layoutValue: GridLayoutType = 6
  if (typeof scene.gridLayout === 'string' && scene.gridLayout.includes('x')) {
    const [cols, rows] = scene.gridLayout.split('x').map(Number)
    layoutValue = (cols * rows) as GridLayoutType
  } else {
    layoutValue = (parseInt(String(scene.gridLayout)) || scene.gridCount || 6) as GridLayoutType
  }

  // 设置布局
  gridLayout.value = layoutValue
  panes.value = createPanes(layoutValue)

  await nextTick()
  await new Promise((r) => setTimeout(r, 100))

  // 播放场景中的通道
  if (scene.channels && scene.channels.length > 0) {
    // 按格子位置分组通道
    const channelsByGrid = new Map<number, typeof scene.channels>()
    for (const channel of scene.channels) {
      const gridPos = channel.gridPosition || 1
      if (!channelsByGrid.has(gridPos)) {
        channelsByGrid.set(gridPos, [])
      }
      channelsByGrid.get(gridPos)!.push(channel)
    }

    console.log('[轮巡] 按格子分组:', Array.from(channelsByGrid.entries()).map(([k, v]) => 
      `格子${k}: ${v.map(c => c.channelName).join(', ')}`
    ))

    // 为每个格子启动轮播
    channelsByGrid.forEach((channels, gridPos) => {
      const paneIndex = gridPos - 1
      if (paneIndex >= 0 && paneIndex < panes.value.length) {
        // 启动该格子的通道轮播
        startGridRotation(paneIndex, channels, 0)
      }
    })
  }
}

// 格子内通道轮播播放
const startGridRotation = async (
  paneIndex: number, 
  channels: PatrolSceneChannel[], 
  currentIndex: number
) => {
  if (!isPatrolling.value || currentIndex >= channels.length) {
    // 如果还在轮巡中，循环播放该格子的通道
    if (isPatrolling.value && channels.length > 0) {
      startGridRotation(paneIndex, channels, 0)
    }
    return
  }

  const channelData = channels[currentIndex]
  const channelNo = channelData.channelNo
  const channelName = channelData.channelName || `通道${channelNo}`
  const duration = channelData.duration || 15  // 默认15秒

  if (!channelNo || channelNo <= 0) {
    console.warn('[轮巡] 通道号无效，跳过:', channelData)
    // 跳过无效通道，立即播放下一个
    startGridRotation(paneIndex, channels, currentIndex + 1)
    return
  }

  console.log('[轮巡] 格子', paneIndex + 1, '播放通道:', channelName, 
    `(${currentIndex + 1}/${channels.length})`, '时长:', duration, '秒')

  const pane = panes.value[paneIndex]
  if (!pane) return

  // 先停止该格子当前播放
  if (pane.isPlaying) {
    await stopPlayer(pane)
  }

  // 确保容器存在
  await nextTick()
  if (!pane.container) {
    const el = document.querySelector(
      `.player-pane[data-index="${paneIndex}"] .player-container`
    ) as HTMLElement
    if (el) pane.container = el
  }

  // 使用 NVR 配置播放
  const config: DahuaPlayerConfig = {
    ip: DEFAULT_NVR_CONFIG.ip,
    port: DEFAULT_NVR_CONFIG.port,
    rtspPort: DEFAULT_NVR_CONFIG.rtspPort,
    username: DEFAULT_NVR_CONFIG.username,
    password: DEFAULT_NVR_CONFIG.password,
    channelNo: channelNo,
    subtype: pane.streamType === 'sub' ? 1 : 0
  }

  // 等待视频真正开始播放后才开始计时
  const success = await startPreview(pane, config, channelName)

  if (!isPatrolling.value) return  // 轮巡已停止

  if (success) {
    // 播放成功，从视频打开后开始计时
    console.log('[轮巡] 格子', paneIndex + 1, '视频已打开，开始计时', duration, '秒')
    
    const timer = window.setTimeout(() => {
      if (isPatrolling.value) {
        startGridRotation(paneIndex, channels, currentIndex + 1)
      }
    }, duration * 1000)

    // 保存定时器以便清理
    sceneRotationTimers.value.set(paneIndex, timer)
  } else {
    // 播放失败，跳过该通道，立即播放下一个
    console.warn('[轮巡] 格子', paneIndex + 1, '播放失败，跳过:', channelName)
    // 短暂延迟后播放下一个，避免快速连续失败
    setTimeout(() => {
      if (isPatrolling.value) {
        startGridRotation(paneIndex, channels, currentIndex + 1)
      }
    }, 500)
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  console.log('[实时预览] 页面加载 - 模式:', isIntranet ? '大华直连' : 'ZLMediaKit 流媒体')

  if (isIntranet) {
    // 尝试登录 NVR（用于云台控制）
    try {
      await loginDevice(DEFAULT_NVR_CONFIG)
    } catch (e) {
      console.warn('[实时预览] NVR 登录失败，云台控制可能不可用')
    }
  }
})

onUnmounted(async () => {
  console.log('[实时预览] 页面卸载，清理资源')

  // 停止轮巡
  if (isPatrolling.value) {
    patrolManagerRef.value?.stopPatrol()
  }

  // 停止所有播放（await 确保资源被完全销毁）
  await stopAllPanes()
  console.log('[实时预览] 所有播放器已清理完毕')
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.realtime-preview-container {
  height: 100%;
  display: flex;
  gap: 10px;
  padding: 10px;
  background: #121212;
  position: relative;

  .left-panel {
    width: 260px;
    background: #1e1e1e;
    border: 1px solid #333;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .center-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #1e1e1e;
    border: 1px solid #333;
    border-radius: 6px;
    overflow: hidden;
    padding-top: 2px;
    min-width: 0;
    position: relative;
  }
}

.mode-indicator {
  position: absolute;
  top: 8px;
  right: 10px;
  z-index: 20;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border: 1px solid #2f2f2f;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);

  .mode-text {
    font-size: 12px;
    color: #d2d2d2;
    white-space: nowrap;
  }
}
</style>
