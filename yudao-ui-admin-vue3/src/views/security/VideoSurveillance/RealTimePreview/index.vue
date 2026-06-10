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
                @current-view-cleared="handleCurrentViewCleared"
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

// API 导入
import { uploadCameraSnapshot } from '@/api/iot/video'
import { nvrPresetControl, nvrPtzControl, nvrAreaZoom } from '@/api/iot/video/nvr'

// 类型导入
import type { GridLayoutType, VideoView, PatrolScene, IbmsChannel, PlayerPane } from './types'
import type { ViewManagerProtocol, ViewSaveDialogPayload } from './viewProtocol'
import { createVideoView, updateVideoView } from '@/api/iot/video/videoView'
import { useZlmPanePlayback } from '@/composables/video/useZlmPanePlayback'
import { useRealtimePaneOrchestrator } from '@/composables/video/useRealtimePaneOrchestrator'
import type { ZlmPlayerInstance } from '@/composables/useZlmPlayer'

defineOptions({ name: 'RealTimePreview' })

// ==================== 组件引用 ====================
const deviceTreeRef = ref()
const viewManagerRef = ref<{ protocol?: ViewManagerProtocol } | undefined>()
const patrolManagerRef = ref()
const cruiseManagerRef = ref()

// ==================== 播放器基础状态 / ZLM 播放 ====================
const createEmptyPane = (index: number): ExtendedPane => ({
  index,
  ibmsChannel: null,
  channelName: '',
  config: null,
  player: null,
  container: null,
  isPlaying: false,
  isLoading: false,
  isPaused: false,
  isRecording: false,
  isPlayback: false,
  error: null,
  streamType: 'sub',
  muted: true,
  zlmInstance: null,
  zlmChannelId: null
})

const createPanes = (count: number): ExtendedPane[] => {
  return Array.from({ length: count }, (_, index) => createEmptyPane(index))
}

const base64ToFile = (base64: string, fileName: string, mimeType: string): File => {
  const base64Data = base64.replace(/^data:image\/\w+;base64,/, '')
  const byteCharacters = atob(base64Data)
  const byteNumbers = new Array(byteCharacters.length)
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i)
  }
  const byteArray = new Uint8Array(byteNumbers)
  const blob = new Blob([byteArray], { type: mimeType })
  return new File([blob], fileName, { type: mimeType })
}

const capturePaneImageData = (pane: ExtendedPane, type: 'jpg' | 'png' = 'jpg') => {
  const container = pane.container
  const source = container?.querySelector<HTMLCanvasElement | HTMLVideoElement>('canvas, video')
  if (!source) {
    return null
  }

  const width = source instanceof HTMLVideoElement ? source.videoWidth : source.width
  const height = source instanceof HTMLVideoElement ? source.videoHeight : source.height
  if (!width || !height) {
    return null
  }

  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    return null
  }

  ctx.drawImage(source, 0, 0, width, height)
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)
  const channelName = pane.channelName || pane.ibmsChannel?.channelName || `channel_${pane.config?.channelNo || 'unknown'}`
  const mimeType = type === 'png' ? 'image/png' : 'image/jpeg'
  const base64 = canvas.toDataURL(mimeType, 0.92)
  return {
    base64,
    fileName: `snapshot_${channelName}_${timestamp}.${type}`,
    mimeType
  }
}

const saveSnapshotLocally = (base64: string, fileName: string) => {
  const link = document.createElement('a')
  link.href = base64
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const {
  stopPanePlayback: stopZlmPanePlayback,
  stopAllPanes: stopAllZlmPanes,
  replacePanePlayback,
  retryPanePlayback,
  switchPaneStream: switchZlmPaneStream
} = useZlmPanePlayback()
const {
  buildViewPayload,
  buildCurrentViewState,
  syncCurrentViewState,
  clearCurrentViewState,
  resetPaneLayout,
  restoreView,
  executeScene,
  stopPatrol
} = useRealtimePaneOrchestrator()

// 播放器扩展状态（ZLM 实时预览窗格）
type ExtendedPane = PlayerPane & {
  index: number
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

const getPreferredRealtimeStreamType = (): 'main' | 'sub' => {
  return gridLayout.value === 1 ? 'main' : 'sub'
}

const applyDefaultPaneStreamType = (pane: ExtendedPane) => {
  pane.streamType = getPreferredRealtimeStreamType()
}

const handleLayoutChange = async (newLayout: GridLayoutType) => {
  await resetPaneLayout({
    layout: newLayout,
    stopAllPanes,
    createPanes: (layout) => createPanes(layout) as ExtendedPane[],
    panesRef: panes as any,
    gridLayoutRef: gridLayout,
    activePaneRef: activePane,
    clearCurrentView: () =>
      clearCurrentViewState({
        currentViewRef: currentView,
        syncCurrentViewSelection: syncViewManagerSelection
      }),
    syncCurrentViewSelection: (viewId) => {
      syncViewManagerSelection(viewId)
    }
  })
}

// 设置窗格容器元素引用
const handlePaneRef = (paneIndex: number, el: HTMLElement | null) => {
  if (paneIndex >= 0 && paneIndex < panes.value.length) {
    panes.value[paneIndex].container = el
  }
}

const resolvePaneContainer = async (paneIndex: number, pane: { container: HTMLElement | null }) => {
  if (pane.container) {
    return pane.container
  }
  await nextTick()
  const el = document.querySelector(
    `.player-pane[data-index="${paneIndex}"] .player-container, .pane-video[data-index="${paneIndex}"]`
  ) as HTMLElement | null
  if (el) {
    pane.container = el
  }
  return pane.container
}

// ==================== 通用停止逻辑 ====================

const stopPanePlayback = async (pane: ExtendedPane) => {
  if (!pane) return
  await stopZlmPanePlayback(pane)
}

const stopAllPanes = async () => {
  await stopAllZlmPanes(panes.value)
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

// 在指定窗格播放通道（统一通过 ZLM 播放）
const playChannelInPane = async (ibmsChannel: IbmsChannel, paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 切换到该窗格
  activePane.value = paneIndex

  // 保存 IBMS 通道信息（用于截图上传等功能）
  pane.ibmsChannel = ibmsChannel
  applyDefaultPaneStreamType(pane)

  try {
    await replacePanePlayback({
      pane,
      paneIndex,
      ibmsChannel,
      resolvePaneContainer
    })

    ElMessage.success(`正在播放: ${ibmsChannel.channelName || `通道${ibmsChannel.channelNo}`}`)
  } catch (e: any) {
    console.error('[ZLM] 播放失败:', e)
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

  try {
    await retryPanePlayback({
      pane,
      paneIndex,
      resolvePaneContainer
    })
  } catch (error: any) {
    ElMessage.warning(error?.message || '当前窗口缺少通道信息，无法重试播放')
  }
}

// 拖拽开始（透传）
const handleChannelDragStart = (_event: DragEvent, _channel: any) => {
  // 已在 DeviceTreePanel 中处理
}

// ==================== 截图/录像/全屏 ====================

const handleSnapshot = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }

  const captureData = capturePaneImageData(pane, 'jpg')
  if (!captureData?.base64) {
    ElMessage.warning('当前播放模式暂不支持截图')
    return
  }

  const ibmsChannel = pane.ibmsChannel
  if (!ibmsChannel?.id) {
    saveSnapshotLocally(captureData.base64, captureData.fileName)
    ElMessage.warning('未找到通道信息，截图已保存到本地')
    return
  }

  try {
    const file = base64ToFile(captureData.base64, captureData.fileName, captureData.mimeType)
    ElMessage.info('正在上传截图...')
    await uploadCameraSnapshot(ibmsChannel.id, file, 1)
    ElMessage.success('截图已保存到服务器')
    console.log('[截图] 上传成功:', captureData.fileName, '通道ID:', ibmsChannel.id, '通道名称:', ibmsChannel.channelName)
  } catch (error: any) {
    console.error('[截图] 上传失败:', error)
    saveSnapshotLocally(captureData.base64, captureData.fileName)
    ElMessage.warning('上传失败，截图已保存到本地')
  }
}

// 录像操作
const handleRecord = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane?.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }

  ElMessage.warning('ZLM 实时预览暂不支持前端本地录像，请使用平台录像/回放功能')
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

  try {
    await switchZlmPaneStream({
      pane,
      paneIndex,
      streamType: streamType as 'main' | 'sub',
      resolvePaneContainer
    })
  } catch (error: any) {
    ElMessage.warning(error?.message || '当前窗口缺少通道信息，无法切换码流')
  }
}

// ==================== 云台控制 ====================

const handlePtzMove = async (command: string, speed: number) => {
  const channelNo = activeChannelNo.value
  if (!channelNo) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

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
  const nvrId = activeDeviceId.value
  if (!channelNo || !nvrId) return

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
  if (!channelNo || !deviceId) {
    console.warn('[云台] 无法获取设备ID，跳过预设点设置')
    return
  }

  try {
    await nvrPresetControl(deviceId, {
      channelNo,
      presetNo,
      action: 'SET',
      presetName
    })
    console.log(`[云台] 预设点 "${presetName}" 已同步到设备 (deviceId=${deviceId})`)
  } catch (error) {
    console.error('[云台] 同步预设点失败:', error)
    ElMessage.error('设置预设位失败')
  }
}

const handleClearPreset = async (presetId: number) => {
  const channelNo = activeChannelNo.value
  const nvrId = activeDeviceId.value
  if (!channelNo || !nvrId) return

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

const getViewManagerProtocol = (): ViewManagerProtocol => ({
  syncCurrentView: (viewId) => viewManagerRef.value?.protocol?.syncCurrentView(viewId),
  reloadViews: async () => {
    await viewManagerRef.value?.protocol?.reloadViews()
  },
  openViewSaveDialog: (payload) => viewManagerRef.value?.protocol?.openViewSaveDialog(payload)
})

const syncViewManagerSelection = (viewId: number | null) => {
  getViewManagerProtocol().syncCurrentView(viewId)
}

const reloadViewManager = async () => {
  await getViewManagerProtocol().reloadViews()
}

const openViewSaveDialog = (payload?: ViewSaveDialogPayload) => {
  getViewManagerProtocol().openViewSaveDialog(payload)
}

// 保存为新视图（弹出对话框）
const handleViewSaveAs = async () => {
  openViewSaveDialog({ groupIds: [1] })
}

// 更新当前视图
const handleViewUpdate = async () => {
  if (!currentView.value?.id) {
    ElMessage.warning('当前没有加载视图')
    return
  }

  try {
    const viewData = buildViewPayload(currentView.value, panes.value, gridLayout.value)

    await updateVideoView(viewData)
    ElMessage.success(`视图 "${currentView.value.name}" 已更新`)
    await reloadViewManager()
  } catch (error) {
    console.error('[视图] 更新失败:', error)
    ElMessage.error('更新视图失败')
  }
}

// 保存视图提交（从 ViewManager 对话框触发）
const handleSaveViewSubmit = async (viewInfo: { id?: number; name: string; groupIds: number[] }) => {
  try {
    const viewData = buildViewPayload(viewInfo, panes.value, gridLayout.value)

    if (viewInfo.id) {
      // 更新现有视图
      await updateVideoView(viewData)
      syncCurrentViewState(
        buildCurrentViewState(
          {
            id: viewInfo.id,
            name: viewInfo.name,
            groupIds: viewInfo.groupIds
          },
          viewData
        ),
        {
          currentViewRef: currentView,
          syncCurrentViewSelection: syncViewManagerSelection
        }
      )
      ElMessage.success(`视图 "${viewInfo.name}" 已更新`)
    } else {
      // 创建新视图
      const newId = await createVideoView(viewData)
      syncCurrentViewState(
        buildCurrentViewState(
          {
            id: newId as number,
            name: viewInfo.name,
            groupIds: viewInfo.groupIds
          },
          viewData
        ),
        {
          currentViewRef: currentView,
          syncCurrentViewSelection: syncViewManagerSelection
        }
      )
      ElMessage.success(`视图 "${viewInfo.name}" 已保存`)
    }

    await reloadViewManager()
  } catch (error) {
    console.error('[视图] 保存失败:', error)
    ElMessage.error('保存视图失败')
  }
}

const handleCurrentViewCleared = (viewId: number) => {
  clearCurrentViewState(
    {
      currentViewRef: currentView,
      syncCurrentViewSelection: syncViewManagerSelection
    },
    viewId
  )
}

const handleLoadView = async (view: VideoView) => {
  try {
    console.log('[视图] 加载:', view.name, view)
    await restoreView({
      view,
      stopAllPanes,
      createPanes,
      panesRef: panes as any,
      gridLayoutRef: gridLayout,
      activePaneRef: activePane,
      isPatrollingRef: isPatrolling,
      setCurrentView: (nextView) => {
        syncCurrentViewState(nextView, {
          currentViewRef: currentView,
          syncCurrentViewSelection: syncViewManagerSelection
        })
      },
      clearCurrentView: () =>
        clearCurrentViewState({
          currentViewRef: currentView,
          syncCurrentViewSelection: syncViewManagerSelection
        }),
      findIbmsChannelById: async (channelId) => {
        return deviceTreeRef.value?.findIbmsChannelById(channelId)
      },
      findIbmsChannelByChannelNo: async (channelNo) => {
        return deviceTreeRef.value?.findIbmsChannelByChannelNo(channelNo)
      },
      playChannelInPane,
      syncCurrentViewSelection: (viewId) => {
        syncViewManagerSelection(viewId)
      }
    })
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
  stopPatrol(isPatrolling, stopAllPanes)
  ElMessage.info('轮巡已停止')
}

const handleExecuteScene = async (scene: PatrolScene) => {
  console.log('[轮巡] 执行场景:', scene.sceneName, '通道数据:', scene.channels)
  await executeScene({
    scene,
    isPatrollingRef: isPatrolling,
    panesRef: panes as any,
    gridLayoutRef: gridLayout,
    activePaneRef: activePane,
    createPanes,
    stopAllPanes,
    clearCurrentView: () =>
      clearCurrentViewState({
        currentViewRef: currentView,
        syncCurrentViewSelection: syncViewManagerSelection
      }),
    syncCurrentViewSelection: (viewId) => {
      syncViewManagerSelection(viewId)
    },
    playChannelByNo: async (paneIndex, channel) => {
      const channelNo = channel.channelNo
      const channelName = channel.channelName || `通道${channelNo}`
      if (!channelNo || channelNo <= 0) {
        console.warn('[轮巡] 通道号无效，跳过:', channel)
        return false
      }

      console.log('[轮巡] 格子', paneIndex + 1, '播放通道:', channelName)
      const pane = panes.value[paneIndex]
      if (!pane) {
        return false
      }

      try {
        const ibmsChannel =
          (channel.channelId ? await deviceTreeRef.value?.findIbmsChannelById(channel.channelId) : null) ||
          (channelNo ? await deviceTreeRef.value?.findIbmsChannelByChannelNo(channelNo) : null)

        if (ibmsChannel) {
          await playChannelInPane(ibmsChannel, paneIndex)
          return true
        }

        console.warn('[轮巡] 未找到 IBMS 通道，跳过:', channel)
        return false
      } catch (error) {
        console.warn('[轮巡] 格子播放失败:', error)
        return false
      }
    }
  })
}

// ==================== 生命周期 ====================

onMounted(async () => {
  console.log('[实时预览] 页面加载 - 模式: ZLMediaKit 流媒体')
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

</style>
