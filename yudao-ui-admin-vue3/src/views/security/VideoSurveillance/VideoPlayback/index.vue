<template>
  <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="height: 100%; margin-bottom: 0"
  >
    <div class="dark-theme-page">
      <div class="video-playback-container">
        <!-- 顶部导航栏 -->
        <div class="top-nav">
          <div class="nav-left">
            <span class="page-title">智慧安防 / 视频监控 / 录像回放</span>
          </div>
          <div class="nav-right">
            <el-button @click="handleRefresh" :loading="loading">
              <Icon icon="ep:refresh" />
              刷新
            </el-button>
          </div>
        </div>

        <!-- 主要内容区域 -->
        <div class="main-layout">
            <!-- 左侧面板：设备树 + 时间筛选 -->
            <div class="left-panel">
            <PlaybackDeviceTree
              ref="deviceTreeRef"
              @search="handleSearch"
              @channel-drag-start="handleChannelDragStart"
              @channels-change="handleChannelsChange"
            />
              </div>

          <!-- 中间面板：播放器网格 + 时间轴 + 控制栏 -->
            <div class="center-panel">
            <!-- 播放器网格 -->
              <div class="player-section">
              <PlaybackPlayerGrid
                ref="playerGridRef"
                :panes="panes"
                :active-pane="activePane"
                :grid-layout="gridLayout"
                @update:active-pane="activePane = $event"
                @pane-drop="handlePaneDrop"
                @pane-ref="handlePaneRef"
              />
                    </div>
                    
                  <!-- 时间轴 -->
            <PlaybackTimeline
              :start-time="filterTimeRange[0]"
              :end-time="filterTimeRange[1]"
              :recording-info-list="recordingInfoList"
              :current-play-time="currentPlayTime"
              :show-cursor="isPlaying"
              @timeline-click="handleTimelineClick"
              @time-change="handleTimeChange"
            />

            <!-- 裁剪任务进度面板（可折叠） -->
            <div v-if="cutTaskList.length > 0" class="cut-tasks-panel" :class="{ collapsed: cutTasksCollapsed }">
              <div class="cut-tasks-header" @click="cutTasksCollapsed = !cutTasksCollapsed">
                <Icon icon="ep:download" />
                <span>裁剪任务 ({{ cutTaskList.length }})</span>
                <!-- 折叠时显示简要进度 -->
                <span v-if="cutTasksCollapsed" class="collapsed-summary">
                  {{ cutTasksSummary }}
                </span>
                <div class="header-actions">
                  <el-button 
                    v-if="cutTaskList.length > 1 && !cutTasksCollapsed" 
                    size="small" 
                    type="danger" 
                    text 
                    @click.stop="handleCancelAllCutTasks"
                  >
                    全部取消
                  </el-button>
                  <Icon 
                    :icon="cutTasksCollapsed ? 'ep:arrow-up' : 'ep:arrow-down'" 
                    class="collapse-icon"
                  />
                </div>
              </div>
              <div v-show="!cutTasksCollapsed" class="cut-tasks-list">
                <div 
                  v-for="task in cutTaskList" 
                  :key="task.paneIndex" 
                  class="cut-task-item"
                >
                  <div class="task-info">
                    <span class="task-channel">{{ task.channelName }}</span>
                    <span class="task-status">
                      <template v-if="task.progress >= 100">
                        <Icon icon="ep:circle-check" style="color: #67c23a" />
                        完成
                      </template>
                      <template v-else-if="task.isCutting">
                        <Icon icon="ep:loading" class="is-loading" />
                        录制中
                      </template>
                      <template v-else>
                        <Icon icon="ep:loading" class="is-loading" />
                        准备中
                      </template>
                    </span>
                  </div>
                  <div class="task-progress">
                    <el-progress 
                      :percentage="task.progress" 
                      :stroke-width="8"
                      :show-text="false"
                      :status="task.progress >= 100 ? 'success' : ''"
                    />
                    <span class="progress-text">{{ task.progress }}%</span>
                  </div>
                  <el-button 
                    v-if="task.progress < 100"
                    size="small" 
                    type="danger" 
                    text 
                    circle
                    @click="handleCancelCutTask(task.paneIndex)"
                    title="取消"
                  >
                    <Icon icon="ep:close" />
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 播放控制栏 -->
            <PlaybackControls
              ref="controlsRef"
              :is-playing="isPlaying"
              :is-paused="isPaused"
              :is-muted="isMuted"
              :playback-speed="playbackSpeed"
              :grid-layout="gridLayout"
              :is-cutting="isCutting"
              :cut-progress="cutProgress"
              :clip-start-time="clipStartTime"
              @toggle-play="handleTogglePlay"
              @stop="handleStop"
              @backward="handleBackward"
              @forward="handleForward"
              @toggle-mute="handleToggleMute"
              @screenshot="handleScreenshot"
              @fullscreen="handleFullscreen"
              @speed-change="handleSpeedChange"
              @layout-change="handleLayoutChange"
              @clip-video="handleClipVideo"
              @sync-all="handleSyncAll"
            />
          </div>
          </div>
          </div>
          </div>
  </ContentWrap>
</template>

<script setup lang="ts">
/**
 * 录像回放页面（组件化重构版）
 * - 使用大华 SDK 进行录像查询和回放
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import { DEFAULT_NVR_CONFIG } from '@/composables/useDahuaPlayer'

// 组件
import PlaybackDeviceTree from './components/PlaybackDeviceTree.vue'
import PlaybackPlayerGrid from './components/PlaybackPlayerGrid.vue'
import PlaybackTimeline from './components/PlaybackTimeline.vue'
import PlaybackControls from './components/PlaybackControls.vue'

// Composables - 使用大华 SDK
import { useDahuaPlayback, createPlaybackPanes } from './composables/useDahuaPlayback'

// 类型
import type { PlaybackPane, GridLayoutType, ChannelRecordingInfo, IbmsChannel } from './types'

// ==================== 组件引用 ====================
const deviceTreeRef = ref<InstanceType<typeof PlaybackDeviceTree>>()
const playerGridRef = ref<InstanceType<typeof PlaybackPlayerGrid>>()

// ==================== 大华回放 Composable ====================
const {
  recordsLoading,
  queryMultipleChannelRecords,
  startPlayback: startDahuaPlayback,
  stopPlayback: stopDahuaPlayback,
  pausePlayback,
  resumePlayback,
  setSpeed,
  seekRelative,
  downloadSnapshot,
  startRecordCut,
  stopRecordCut,
  getCutTaskState,
  cutTasks
} = useDahuaPlayback()

// 当前活动窗口的裁剪状态
const currentCutState = computed(() => getCutTaskState(activePane.value))
const isCutting = computed(() => currentCutState.value.isCutting || currentCutState.value.progress > 0 && currentCutState.value.progress < 100)
const cutProgress = computed(() => currentCutState.value.progress)

// 所有裁剪任务列表（用于显示进度面板）
interface CutTaskDisplay {
  paneIndex: number
  channelName: string
  isCutting: boolean
  progress: number
}
const cutTaskList = computed<CutTaskDisplay[]>(() => {
  const list: CutTaskDisplay[] = []
  for (const [paneIndex, task] of cutTasks.value.entries()) {
    list.push({
      paneIndex,
      channelName: task.channelName || `窗口${paneIndex + 1}`,
      isCutting: task.isCutting,
      progress: task.progress
    })
  }
  return list
})

// 裁剪任务面板折叠状态
const cutTasksCollapsed = ref(false)

// 折叠时显示的简要进度摘要
const cutTasksSummary = computed(() => {
  const tasks = cutTaskList.value
  if (tasks.length === 0) return ''
  
  const inProgress = tasks.filter(t => t.progress > 0 && t.progress < 100).length
  const completed = tasks.filter(t => t.progress >= 100).length
  const avgProgress = Math.round(tasks.reduce((sum, t) => sum + t.progress, 0) / tasks.length)
  
  if (completed === tasks.length) {
    return '全部完成'
  }
  return `${inProgress}个进行中, 平均${avgProgress}%`
})

// 取消指定裁剪任务
const handleCancelCutTask = (paneIndex: number) => {
  stopRecordCut(paneIndex)
  ElMessage.info('已取消裁剪任务')
}

// 取消所有裁剪任务
const handleCancelAllCutTasks = () => {
  stopRecordCut() // 不传参数则取消全部
  ElMessage.info('已取消所有裁剪任务')
}

// ==================== 状态 ====================
const loading = computed(() => recordsLoading.value)

// 分屏布局
const gridLayout = ref<GridLayoutType>(6)

// 播放窗格
const panes = ref<PlaybackPane[]>(createPlaybackPanes(gridLayout.value))

// 活动窗格索引
const activePane = ref(0)

// 选中的通道
const selectedChannels = ref<IbmsChannel[]>([])

// 录像信息列表
const recordingInfoList = ref<ChannelRecordingInfo[]>([])

// 筛选时间范围
const filterTimeRange = ref<string[]>([])

// 当前播放时间（毫秒）
const currentPlayTime = ref(0)

// 播放速度
const playbackSpeed = ref(1)

// 全局静音
const isMuted = ref(true)

// 计算属性：是否有窗格在播放
const isPlaying = computed(() => panes.value.some((p) => p.isPlaying))

// 计算属性：当前活动窗格是否暂停
const isPaused = computed(() => {
  const pane = panes.value[activePane.value]
  return pane?.isPaused || false
})

// ==================== 初始化 ====================

// 初始化默认时间范围
const initDefaultTimeRange = () => {
  const d = new Date()
  const start = new Date(d)
  start.setHours(0, 0, 0, 0)
  const end = new Date(d)
  end.setHours(23, 59, 59, 999)

  const fmt = (dt: Date) => {
    const y = dt.getFullYear()
    const m = String(dt.getMonth() + 1).padStart(2, '0')
    const day = String(dt.getDate()).padStart(2, '0')
    const hh = String(dt.getHours()).padStart(2, '0')
    const mm = String(dt.getMinutes()).padStart(2, '0')
    const ss = String(dt.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
  }

  filterTimeRange.value = [fmt(start), fmt(end)]
}

// ==================== 事件处理 ====================

// 刷新
const handleRefresh = () => {
  if (deviceTreeRef.value) {
    deviceTreeRef.value.loadBuildingTree?.()
  }
}

// 通道选择变化
const handleChannelsChange = (channels: IbmsChannel[]) => {
  selectedChannels.value = channels
  console.log('[录像回放] 选中通道:', channels.length, '个')
}

// 搜索录像
const handleSearch = async (
  channels: IbmsChannel[],
  startTime: string,
  endTime: string
) => {
  console.log('[录像回放] 搜索录像:', channels.length, '个通道')
  console.log('[录像回放] 时间范围:', startTime, '~', endTime)

  // 保存时间范围
  filterTimeRange.value = [startTime, endTime]

  // 停止所有播放
  await stopAllPanes()

  // 提取通道号列表（大华 SDK 使用 NVR 通道号）
  const channelNos = channels.map((ch) => ch.channelNo || 1)

  // 查询录像
  const results = await queryMultipleChannelRecords(channelNos, startTime, endTime)
  recordingInfoList.value = results

  // 统计有录像的通道数
  const channelsWithRecording = results.filter(
    (r) => r.segments.some((s) => s.hasRecording)
  ).length
  console.log('[录像回放] 有录像的通道:', channelsWithRecording, '个')

  // 自动分配通道到窗格
  await autoAssignChannelsToPanes(channels, results)

  if (channelsWithRecording > 0) {
    ElMessage.success(`已查询到 ${channelsWithRecording} 个通道有录像，点击时间轴播放`)
  } else {
    ElMessage.warning('未找到录像，请确认时间范围和通道是否正确')
  }
}

// 自动分配通道到窗格
const autoAssignChannelsToPanes = async (
  channels: IbmsChannel[],
  recordingResults: ChannelRecordingInfo[]
) => {
  const maxPanes = Math.min(channels.length, panes.value.length)

  for (let i = 0; i < maxPanes; i++) {
    const channel = channels[i]
    const pane = panes.value[i]
    const channelNo = channel.channelNo

    // 查找该通道的录像信息
    const recordingInfo = recordingResults.find(
      (r) => String(r.channelId) === String(channelNo)
    )
    const hasRecording = recordingInfo?.segments.some((s) => s.hasRecording) || false

    // 绑定通道到窗格
    pane.channel = { name: channel.channelName, channelNo: channel.channelNo }
    pane.channelNo = channelNo
    pane.hasRecording = hasRecording

    console.log(`[录像回放] 窗口 ${i + 1} 绑定通道: ${channel.channelName}, 有录像: ${hasRecording}`)
  }
}

// 通道拖拽开始
const handleChannelDragStart = (_event: DragEvent, channel: IbmsChannel) => {
  console.log('[录像回放] 拖拽通道:', channel.channelName)
}

// 窗格拖拽放置
const handlePaneDrop = async (event: DragEvent, paneIndex: number) => {
  try {
    // 优先获取 IBMS 通道数据
    let channel: IbmsChannel | null = null
    const ibmsDataStr = event.dataTransfer?.getData('ibmsChannel')
    if (ibmsDataStr) {
      channel = JSON.parse(ibmsDataStr)
    }
    
    if (!channel) {
      ElMessage.warning('无法获取通道信息')
      return
    }
    
    console.log('[录像回放] 放置通道到窗口', paneIndex + 1, ':', channel.channelName)
    
    const pane = panes.value[paneIndex]
    if (!pane) return
    
    // 如果窗格正在播放，先停止
    if (pane.isPlaying) {
      await stopDahuaPlayback(pane)
    }

    // 绑定通道
    pane.channel = { name: channel.channelName, channelNo: channel.channelNo }
    pane.channelNo = channel.channelNo

    // 检查是否有录像
    const recordingInfo = recordingInfoList.value.find(
      (r) => String(r.channelId) === String(channel!.channelNo)
    )
    pane.hasRecording = recordingInfo?.segments.some((s) => s.hasRecording) || false

    // 切换到该窗格
    activePane.value = paneIndex

    if (pane.hasRecording) {
      ElMessage.success(`已绑定 ${channel.channelName}，点击时间轴播放`)
    } else {
      ElMessage.warning(`${channel.channelName} 在当前时间范围内无录像`)
    }
  } catch (error) {
    console.error('[录像回放] 拖拽处理失败:', error)
    ElMessage.error('拖拽操作失败')
  }
}

// 设置窗格引用（保留接口兼容性，实际播放器通过固定 ID 获取容器）
const handlePaneRef = (paneIndex: number, el: HTMLElement | null) => {
  if (paneIndex >= 0 && paneIndex < panes.value.length && el) {
    panes.value[paneIndex].videoEl = el as HTMLVideoElement
  }
}

// 解析时间字符串为时间戳（毫秒）
const parseTimeString = (timeStr: string): number => {
  return new Date(timeStr).getTime()
}

// 时间轴点击
const handleTimelineClick = async (clickTime: Date, percent: number) => {
  console.log('[录像回放] 时间轴点击:', clickTime.toISOString(), `(${(percent * 100).toFixed(1)}%)`)
  
  // 更新当前播放时间
  currentPlayTime.value = clickTime.getTime()
  
  // 获取当前活动窗格
  const pane = panes.value[activePane.value]
  if (!pane) {
    ElMessage.warning('请先选择一个播放窗口')
    return
  }
  
  if (!pane.channel) {
    ElMessage.warning(`窗口 ${activePane.value + 1} 未绑定通道，请先拖拽通道到窗口`)
    return
  }
  
  if (!pane.hasRecording) {
    ElMessage.warning(`${pane.channel.name} 在当前时间范围内无录像`)
    return
  }
  
  // 计算回放时间范围
  const clickTimeStr = formatDateTime(clickTime)
  const endTime = filterTimeRange.value[1]
  const clickTimestamp = clickTime.getTime()

  console.log('[录像回放] 开始回放:', pane.channel.name, clickTimeStr, '~', endTime)

  // 如果正在播放，先停止
  if (pane.isPlaying) {
    await stopDahuaPlayback(pane)
  }

  // 查找该通道的录像信息
  const recordingInfo = recordingInfoList.value.find(
    (r) => String(r.channelId) === String(pane.channelNo)
  )
  
  if (!recordingInfo || recordingInfo.segments.length === 0) {
    ElMessage.warning('未找到录像信息')
    pane.isLoading = false
    return
  }

  // 根据点击时间找到包含该时间点的录像文件
  let targetSegment: any = null
  let seekSeconds = 0

  for (const segment of recordingInfo.segments) {
    if (!segment.hasRecording || !segment.filePath) continue
    
    const segStart = parseTimeString(segment.startTime)
    const segEnd = parseTimeString(segment.endTime)
    
    // 检查点击时间是否在该录像片段范围内
    if (clickTimestamp >= segStart && clickTimestamp <= segEnd) {
      targetSegment = segment
      // 计算从录像开始到点击时间的秒数偏移
      seekSeconds = Math.floor((clickTimestamp - segStart) / 1000)
      console.log('[录像回放] 找到匹配的录像片段:', segment.startTime, '~', segment.endTime, '偏移秒数:', seekSeconds)
      break
    }
  }

  // 如果没有精确匹配，找到点击时间之后最近的录像片段
  if (!targetSegment) {
    for (const segment of recordingInfo.segments) {
      if (!segment.hasRecording || !segment.filePath) continue
      
      const segStart = parseTimeString(segment.startTime)
      
      // 找到点击时间之后的第一个录像片段
      if (segStart >= clickTimestamp) {
        targetSegment = segment
        seekSeconds = 0 // 从头开始播放
        console.log('[录像回放] 使用最近的录像片段:', segment.startTime, '~', segment.endTime)
        break
      }
    }
  }

  // 如果还是没找到，使用第一个有录像的片段
  if (!targetSegment) {
    targetSegment = recordingInfo.segments.find((s) => s.hasRecording && s.filePath)
    if (targetSegment) {
      // 如果点击时间在第一个片段之前，从头播放
      // 如果点击时间在最后一个片段之后，从该片段对应位置播放
      const segStart = parseTimeString(targetSegment.startTime)
      if (clickTimestamp > segStart) {
        seekSeconds = Math.floor((clickTimestamp - segStart) / 1000)
      } else {
        seekSeconds = 0
      }
      console.log('[录像回放] 使用第一个有效录像片段:', targetSegment.startTime, '偏移秒数:', seekSeconds)
    }
  }

  if (!targetSegment?.filePath) {
    ElMessage.warning('未找到录像文件路径')
    pane.isLoading = false
    return
  }

  // 开始回放（使用大华 SDK）
  const channelNo = pane.channelNo || 1
  const success = await startDahuaPlayback(
    pane,
    activePane.value,
    channelNo,
    targetSegment.filePath,
    pane.channel?.name,
    seekSeconds // 传递跳转秒数
  )

  if (success) {
    ElMessage.success(`窗口 ${activePane.value + 1} 开始回放`)
  } else {
    ElMessage.error('回放失败，请重试')
  }
}

// 时间变化（拖拽时间轴）
const handleTimeChange = (time: number) => {
  currentPlayTime.value = time
}

// 播放/暂停切换
const handleTogglePlay = async () => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
      return
    }
    
    if (pane.isPaused) {
    await resumePlayback(pane)
    ElMessage.success('已恢复播放')
    } else {
    await pausePlayback(pane)
      ElMessage.success('已暂停')
    }
}

// 停止播放
const handleStop = async () => {
  const pane = panes.value[activePane.value]
  if (!pane) {
    ElMessage.warning('请先选择一个窗口')
    return
  }
  
  await stopDahuaPlayback(pane)
  ElMessage.success('已停止播放')
}

// 后退 30 秒
const handleBackward = () => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  seekRelative(pane, -30)
  ElMessage.success('后退 30 秒')
}

// 前进 30 秒
const handleForward = () => {
  const pane = panes.value[activePane.value]
  if (!pane || !pane.isPlaying) {
    ElMessage.warning('当前窗口没有正在播放的视频')
    return
  }
  
  seekRelative(pane, 30)
  ElMessage.success('前进 30 秒')
}

// 静音切换
const handleToggleMute = () => {
  isMuted.value = !isMuted.value

  panes.value.forEach((pane) => {
    pane.muted = isMuted.value
    // 大华 SDK 播放器静音控制
    if (pane.jessibucaPlayer) {
      try {
        pane.jessibucaPlayer.setVolume?.(isMuted.value ? 0 : 100)
      } catch {}
    }
  })

  ElMessage.success(isMuted.value ? '已静音' : '已取消静音')
}

// 截图
const handleScreenshot = () => {
  const pane = panes.value[activePane.value]
  if (!pane) {
    ElMessage.warning('请先选择一个窗口')
    return
  }
  
  downloadSnapshot(pane, activePane.value)
}

// 全屏
const handleFullscreen = () => {
  const gridEl = playerGridRef.value?.gridRef
  if (gridEl && gridEl.requestFullscreen) {
    gridEl.requestFullscreen()
  }
}

// 播放速度变化
const handleSpeedChange = async (speed: number) => {
  playbackSpeed.value = speed

  const pane = panes.value[activePane.value]
  if (pane && pane.isPlaying) {
    await setSpeed(pane, speed)
    ElMessage.success(`播放速度: ${speed}x`)
  }
}

// 布局变化
const handleLayoutChange = async (layout: GridLayoutType) => {
  // 停止所有播放
  await stopAllPanes()

  // 更新布局
  gridLayout.value = layout
  panes.value = createPlaybackPanes(layout)
  activePane.value = 0

  console.log(`[录像回放] 布局切换为 ${layout} 窗口`)
}

// 剪切录像状态
const clipStartTime = ref<Date | null>(null)
const clipEndTime = ref<Date | null>(null)

// 剪切录像
const handleClipVideo = async () => {
  const paneIndex = activePane.value
  const pane = panes.value[paneIndex]
  
  // 如果当前窗口正在裁剪，则取消
  const cutState = getCutTaskState(paneIndex)
  if (cutState.isCutting || (cutState.progress > 0 && cutState.progress < 100)) {
    stopRecordCut(paneIndex)
    ElMessage.info('已取消该窗口的录像裁剪')
    clipStartTime.value = null
    clipEndTime.value = null
    return
  }

  if (!pane || !pane.isPlaying) {
    ElMessage.warning('请先播放录像')
    return
  }

  if (!clipStartTime.value) {
    // 设置剪切起点
    clipStartTime.value = new Date(currentPlayTime.value)
    ElMessage.success(`剪切起点已设置: ${formatDateTime(clipStartTime.value)}，再次点击设置终点并开始下载`)
  } else {
    // 设置剪切终点并下载
    clipEndTime.value = new Date(currentPlayTime.value)
    
    if (clipEndTime.value <= clipStartTime.value) {
      ElMessage.warning('终点时间必须大于起点时间')
      clipEndTime.value = null
      return
    }
    
    // 检查时长
    const durationSec = (clipEndTime.value.getTime() - clipStartTime.value.getTime()) / 1000
    if (durationSec < 5) {
      ElMessage.warning('裁剪时长至少需要5秒')
      clipEndTime.value = null
      return
    }
    
    if (durationSec > 3600) {
      const confirm = await ElMessageBox.confirm(
        `裁剪时长约 ${Math.round(durationSec / 60)} 分钟，下载可能需要较长时间，是否继续？`,
        '提示',
        { confirmButtonText: '继续', cancelButtonText: '取消' }
      ).catch(() => false)
      
      if (!confirm) {
        clipEndTime.value = null
        return
      }
    }
    
    // 开始裁剪下载（传入窗口索引，支持多通道同时裁剪）
    await downloadClipVideo(
      paneIndex,
      pane.channelNo || 1,
      formatDateTime(clipStartTime.value),
      formatDateTime(clipEndTime.value),
      pane.channel?.name || '录像'
    )
    
    // 重置剪切状态
    clipStartTime.value = null
    clipEndTime.value = null
  }
}

// 下载剪切的录像片段（使用SDK裁剪功能，支持多通道同时裁剪）
const downloadClipVideo = async (
  paneIndex: number,
  channelNo: number,
  startTime: string,
  endTime: string,
  channelName: string
) => {
  try {
    ElMessage.info(`${channelName} 正在准备下载录像片段...`)
    
    // 查找该时间范围内的录像文件
    const recordingInfo = recordingInfoList.value.find(
      (r) => String(r.channelId) === String(channelNo)
    )
    
    if (!recordingInfo || recordingInfo.segments.length === 0) {
      ElMessage.warning('未找到录像文件')
      return
    }
    
    // 找到包含该时间段的录像文件
    const startTs = new Date(startTime).getTime()
    const endTs = new Date(endTime).getTime()
    
    for (const segment of recordingInfo.segments) {
      if (!segment.hasRecording || !segment.filePath) continue
      
      const segStart = new Date(segment.startTime).getTime()
      const segEnd = new Date(segment.endTime).getTime()
      
      // 检查是否有重叠
      if (segStart <= endTs && segEnd >= startTs) {
        console.log(`[录像回放] 窗口${paneIndex + 1} 找到录像文件:`, segment)
        
        // 使用SDK裁剪功能下载（传入窗口索引）
        const success = await startRecordCut(
          paneIndex,
          segment.filePath,
          startTime,
          endTime,
          segment.startTime, // 录像文件的开始时间
          channelName,
          (progress) => {
            // 进度回调
            console.log(`[录像回放] 窗口${paneIndex + 1} ${channelName} 裁剪进度: ${progress}%`)
          },
          () => {
            // 完成回调
            console.log(`[录像回放] 窗口${paneIndex + 1} ${channelName} 裁剪完成`)
          }
        )
        
        if (!success) {
          ElMessage.error(`${channelName} 启动裁剪任务失败`)
        }
        return
      }
    }
    
    ElMessage.warning('未找到该时间段的录像文件')
  } catch (error) {
    console.error('[录像回放] 下载剪切录像失败:', error)
    ElMessage.error('下载失败')
  }
}

// 同步所有窗口到当前时间点
const handleSyncAll = async () => {
  const activeP = panes.value[activePane.value]
  if (!activeP || !activeP.isPlaying) {
    ElMessage.warning('请先在活动窗口播放录像')
    return
  }
  
  const syncTime = new Date(currentPlayTime.value)
  const syncTimeStr = formatDateTime(syncTime)
  
  console.log('[录像回放] 同步所有窗口到:', syncTimeStr)
  
  let syncCount = 0
  
  for (let i = 0; i < panes.value.length; i++) {
    const pane = panes.value[i]
    
    // 跳过活动窗口和没有绑定通道的窗口
    if (i === activePane.value || !pane.channel || !pane.hasRecording) continue
    
    // 如果窗口正在播放，先停止
    if (pane.isPlaying) {
      await stopDahuaPlayback(pane)
    }
    
    // 查找该通道的录像信息
    const recordingInfo = recordingInfoList.value.find(
      (r) => String(r.channelId) === String(pane.channelNo)
    )
    
    if (!recordingInfo || recordingInfo.segments.length === 0) continue
    
    // 找到包含同步时间点的录像文件
    const syncTimestamp = syncTime.getTime()
    let targetSegment: any = null
    let seekSeconds = 0
    
    for (const segment of recordingInfo.segments) {
      if (!segment.hasRecording || !segment.filePath) continue
      
      const segStart = new Date(segment.startTime).getTime()
      const segEnd = new Date(segment.endTime).getTime()
      
      if (syncTimestamp >= segStart && syncTimestamp <= segEnd) {
        targetSegment = segment
        seekSeconds = Math.floor((syncTimestamp - segStart) / 1000)
        break
      }
    }
    
    // 如果没有精确匹配，找最近的
    if (!targetSegment) {
      targetSegment = recordingInfo.segments.find((s) => s.hasRecording && s.filePath)
    }
    
    if (targetSegment?.filePath) {
      // 开始回放
      const success = await startDahuaPlayback(
        pane,
        i,
        pane.channelNo || 1,
        targetSegment.filePath,
        pane.channel?.name,
        seekSeconds
      )
      
      if (success) {
        syncCount++
      }
    }
  }
  
  if (syncCount > 0) {
    ElMessage.success(`已同步 ${syncCount} 个窗口到 ${syncTimeStr}`)
  } else {
    ElMessage.warning('没有其他窗口可以同步')
  }
}

// 停止所有窗格播放
const stopAllPanes = async () => {
  for (const pane of panes.value) {
    if (pane.isPlaying) {
      await stopDahuaPlayback(pane)
    }
  }
}

// 格式化日期时间
const formatDateTime = (d: Date): string => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
}

// ==================== 生命周期 ====================

onMounted(() => {
  initDefaultTimeRange()
})

onUnmounted(() => {
  stopAllPanes()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.video-playback-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .top-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: rgba(0, 0, 0, 0.3);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .nav-left {
      .page-title {
        font-size: 16px;
        font-weight: 500;
        color: rgba(255, 255, 255, 0.9);
      }
    }
  }

  .main-layout {
    flex: 1;
      display: flex;
      gap: 10px;
      padding: 10px;
    overflow: hidden;

      .left-panel {
      width: 260px;
        background: #1e1e1e;
        border: 1px solid #3a3a3a;
        border-radius: 4px;
      overflow: hidden;
      }

      .center-panel {
        flex: 1;
        display: flex;
        flex-direction: column;
        background: #1e1e1e;
        border: 1px solid #3a3a3a;
        border-radius: 4px;
      overflow: hidden;
        
      .player-section {
          flex: 1;
            min-height: 0;
            padding: 8px;
      }
      
      // 裁剪任务进度面板（可折叠）
      .cut-tasks-panel {
        background: linear-gradient(180deg, rgba(64, 158, 255, 0.1) 0%, rgba(30, 30, 30, 0.95) 100%);
        border-top: 1px solid rgba(64, 158, 255, 0.3);
        padding: 8px 12px;
        transition: all 0.3s ease;
        
        &.collapsed {
          padding: 6px 12px;
          background: rgba(64, 158, 255, 0.08);
        }
        
        .cut-tasks-header {
          display: flex;
          align-items: center;
          gap: 8px;
          color: #409eff;
          font-size: 13px;
          font-weight: 500;
          cursor: pointer;
          user-select: none;
          
          &:hover {
            color: #66b1ff;
          }
          
          .collapsed-summary {
            color: rgba(255, 255, 255, 0.6);
            font-size: 12px;
            font-weight: normal;
            margin-left: 8px;
          }
          
          .header-actions {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-left: auto;
          }
          
          .collapse-icon {
            font-size: 14px;
            transition: transform 0.3s;
          }
        }
        
        .cut-tasks-list {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;
          margin-top: 8px;
          
          .cut-task-item {
            display: flex;
            align-items: center;
            gap: 12px;
            background: rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 6px;
            padding: 8px 12px;
            min-width: 220px;
            
            .task-info {
              display: flex;
              flex-direction: column;
              gap: 2px;
              min-width: 80px;
              
              .task-channel {
                color: #fff;
                font-size: 13px;
                font-weight: 500;
              }
              
              .task-status {
                display: flex;
                align-items: center;
                gap: 4px;
                color: rgba(255, 255, 255, 0.6);
                font-size: 11px;
              }
            }
            
            .task-progress {
              display: flex;
              align-items: center;
              gap: 8px;
              flex: 1;
              min-width: 100px;
              
              .el-progress {
                flex: 1;
              }
              
              .progress-text {
                color: #67c23a;
                font-size: 12px;
                font-weight: 500;
                min-width: 36px;
                text-align: right;
              }
            }
          }
        }
        
        // 加载动画
        .is-loading {
          animation: rotating 1.5s linear infinite;
        }
        
        @keyframes rotating {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      }
    }
  }
}
</style>
