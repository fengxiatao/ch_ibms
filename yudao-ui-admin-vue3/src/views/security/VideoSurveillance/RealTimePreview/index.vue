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

// API 导入
import { uploadCameraSnapshot } from '@/api/iot/video'
import { nvrPresetControl } from '@/api/iot/video/nvr'

// 类型导入
import type { GridLayoutType, VideoView, PatrolScene, IbmsChannel } from './types'
import { createVideoView, updateVideoView } from '@/api/iot/video/videoView'

defineOptions({ name: 'RealTimePreview' })

// ==================== 组件引用 ====================
const deviceTreeRef = ref()
const viewManagerRef = ref()
const patrolManagerRef = ref()
const ptzControlRef = ref()
const cruiseManagerRef = ref()

// ==================== 大华播放器 ====================
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

// ==================== 播放器状态 ====================
const gridLayout = ref<GridLayoutType>(6)
// 使用类型断言兼容组件 Props
const panes = ref(createPanes(6) as any[])
const activePane = ref(0)
const currentView = ref<VideoView | null>(null)
const isPatrolling = ref(false)
const areaZoomActive = ref(false)  // 区域放大模式

// ==================== 计算属性 ====================

// 正在播放的通道 ID 列表（用于设备树高亮）
const playingChannelIds = computed(() => {
  return panes.value.filter((p) => p.isPlaying && p.config).map((p) => p.config!.channelNo)
})

// 当前活动窗口的通道号（用于云台控制）
const activeChannelNo = computed(() => {
  const pane = panes.value[activePane.value]
  return pane?.config?.channelNo || null
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
  // 必须同时满足：窗口正在播放 且 有播放配置 且 通道支持云台控制
  if (!pane || !pane.isPlaying || !pane.config) {
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
  if (!pane || !pane.isPlaying || !pane.config) {
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
  await stopAllPlayers(panes.value)

  // 更新布局
  gridLayout.value = newLayout
  panes.value = createPanes(newLayout)
  activePane.value = 0
}

// 设置窗格容器元素引用
const handlePaneRef = (paneIndex: number, el: HTMLElement | null) => {
  if (paneIndex >= 0 && paneIndex < panes.value.length) {
    panes.value[paneIndex].container = el
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

// 在指定窗格播放通道（使用大华 SDK）
const playChannelInPane = async (ibmsChannel: IbmsChannel, paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 如果当前窗格正在播放，先停止
  if (pane.isPlaying || pane.player) {
    await stopPlayer(pane)
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

  // 构建播放配置
  // 使用 NVR 作为 WebSocket 连接点（IPC 不支持 WebSocket）
  const config: DahuaPlayerConfig = {
    ip: DEFAULT_NVR_CONFIG.ip,
    port: DEFAULT_NVR_CONFIG.port,
    rtspPort: DEFAULT_NVR_CONFIG.rtspPort,  // RTSP 端口
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
}

// 停止窗格播放
const handleStopPane = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  await stopPlayer(pane)
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

    await stopAllPlayers(panes.value)
    ElMessage.success('已停止所有播放')
  } catch {
    // 取消
  }
}

// 重试播放
const handleRetry = async (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane || !pane.config) return

  // 重新播放
  await stopPlayer(pane)
  await startPreview(pane, pane.config, pane.channelName)
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
  if (!pane || !pane.config) return

  ElMessage.info(`正在切换到${streamType === 'main' ? '主' : '子'}码流...`)

  // 切换码流
  const success = await switchStream(pane, streamType as 'main' | 'sub')
  if (success) {
    ElMessage.success(`已切换到${streamType === 'main' ? '主' : '子'}码流`)
  }
}

// ==================== 云台控制 ====================

const handlePtzMove = async (command: string, speed: number) => {
  const channelNo = activeChannelNo.value
  if (!channelNo) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

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
}

const handlePtzStop = async () => {
  const channelNo = activeChannelNo.value
  if (!channelNo) return

  try {
    // 停止所有方向
    await ptzMove(channelNo, 'Up', 5, true)
  } catch (error) {
    console.error('[云台] 停止失败:', error)
  }
}

// ==================== 预设位 ====================

const handleGotoPreset = async (presetId: number) => {
  const channelNo = activeChannelNo.value
  if (!channelNo) return

  await loginDevice(DEFAULT_NVR_CONFIG)
  await gotoPreset(channelNo, presetId)
}

const handleSetPreset = async (presetNo: number, presetName: string) => {
  const channelNo = activeChannelNo.value
  const deviceId = activeDeviceId.value
  if (!channelNo) return

  // 1. RPC2 设置预设点位置到设备
  await loginDevice(DEFAULT_NVR_CONFIG)
  await setPreset(channelNo, presetNo)
  
  // 2. 通过后端 API（NetSDK）设置预设点名称
  // RPC2 WebSDK 不支持设置名称，需要通过 NetSDK 来设置
  if (deviceId) {
    try {
      await nvrPresetControl(deviceId, {
        channelNo: channelNo,
        presetNo: presetNo,
        action: 'SET',
        presetName: presetName
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

  await loginDevice(DEFAULT_NVR_CONFIG)
  await clearPreset(channelNo, presetId)
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
  if (!pane?.config?.channelNo) {
    ElMessage.warning('该窗口没有播放视频')
    return
  }

  const channelNo = pane.config.channelNo
  const container = pane.container as HTMLElement

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
}

const handlePtzReset = async () => {
  const channelNo = activeChannelNo.value
  if (!channelNo) {
    ElMessage.warning('请先选择一个正在播放的窗口')
    return
  }

  try {
    ElMessage.info('正在复位，请稍候...')
    await loginDevice(DEFAULT_NVR_CONFIG)
    await ptzReset(channelNo, 3000)  // 持续缩小3秒
    ElMessage.success('云台复位完成')
  } catch (error: any) {
    console.error('[云台] 复位失败:', error)
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
    await stopAllPlayers(panes.value)

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

const handleExecuteScene = async (scene: PatrolScene) => {
  console.log('[轮巡] 执行场景:', scene.sceneName, '通道数据:', scene.channels)

  // 停止当前所有播放
  await stopAllPlayers(panes.value)

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
    for (let i = 0; i < scene.channels.length; i++) {
      const channelData = scene.channels[i]
      const paneIndex = (channelData.gridPosition || 1) - 1

      if (paneIndex >= 0 && paneIndex < panes.value.length) {
        // 获取通道号（优先使用 channelNo，否则尝试通过 channelId 查找）
        const channelNo = channelData.channelNo
        const channelName = channelData.channelName || `通道${channelNo}`
        
        if (channelNo && channelNo > 0) {
          console.log('[轮巡] 播放通道:', channelName, '窗格:', paneIndex + 1, 'channelNo:', channelNo)
          
          // 构建播放配置，直接使用保存的数据
          setTimeout(async () => {
            const pane = panes.value[paneIndex]
            if (!pane) return
            
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
            
            await startPreview(pane, config, channelName)
          }, i * 300)
        } else {
          console.warn('[轮巡] 通道号无效:', channelData)
        }
      }
    }
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  console.log('[实时预览] 页面加载 - 大华直连模式')

  // 尝试登录 NVR（用于云台控制）
  try {
    await loginDevice(DEFAULT_NVR_CONFIG)
  } catch (e) {
    console.warn('[实时预览] NVR 登录失败，云台控制可能不可用')
  }
})

onUnmounted(() => {
  console.log('[实时预览] 页面卸载，清理资源')

  // 停止轮巡
  if (isPatrolling.value) {
    patrolManagerRef.value?.stopPatrol()
  }

  // 停止所有播放
  stopAllPlayers(panes.value)
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
  }
}
</style>
