<template>
  <ContentWrap
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }"
    style="height: calc(100vh - var(--page-top-gap, 70px)); margin-bottom: 0"
  >
    <div class="wvp-preview-container">
      <!-- 左侧通道树 -->
      <div class="left-panel">
        <div class="panel-header">
          <Icon icon="ep:video-camera" />
          <span>WVP 通道</span>
          <el-tag type="success" size="small">GB28181</el-tag>
        </div>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索通道..."
            clearable
            size="small"
            @keyup.enter="loadChannelList"
            @clear="loadChannelList"
          >
            <template #prefix>
              <Icon icon="ep:search" />
            </template>
          </el-input>
        </div>
        <div class="channel-list" v-loading="channelLoading">
          <div
            v-for="(channel, idx) in filteredChannels"
            :key="getChannelPlayId(channel) || idx"
            class="channel-item"
            :class="{ online: channel.status === 1, playing: isChannelPlaying(channel) }"
            draggable="true"
            @dragstart="handleDragStart($event, channel)"
            @dblclick="handleChannelDoubleClick(channel)"
            @contextmenu.prevent="showChannelContextMenu($event, channel)"
          >
            <div class="channel-info">
              <Icon :icon="channel.status === 1 ? 'ep:video-camera-filled' : 'ep:video-camera'" />
              <span class="channel-name">{{ channel.name || `通道${channel.channelId}` }}</span>
            </div>
            <div class="channel-status">
              <el-tag v-if="channel.status === 1" type="success" size="small">在线</el-tag>
              <el-tag v-else type="info" size="small">离线</el-tag>
            </div>
          </div>
          <el-empty v-if="!channelLoading && filteredChannels.length === 0" description="暂无通道" />
        </div>
        <div class="panel-footer">
          <el-button size="small" @click="loadChannelList" :loading="channelLoading">
            <Icon icon="ep:refresh" />
            刷新
          </el-button>
          <span class="channel-count">共 {{ channelList.length }} 个通道</span>
        </div>
      </div>

      <!-- 中间播放区域 -->
      <div class="center-panel">
        <div class="player-grid" :class="gridLayoutClass">
          <div
            v-for="(pane, idx) in panes"
            :key="'pane-' + idx"
            class="player-pane"
            :class="{ active: activePane === idx, 'drag-over': dragOverPane === idx }"
            @click="activePane = idx"
            @drop="handleDrop($event, idx)"
            @dragover.prevent="dragOverPane = idx"
            @dragleave="dragOverPane = -1"
          >
            <!-- Jessibuca 播放器容器 -->
            <div
              class="video-container jessibuca-container"
              :id="'jessibuca-container-' + idx"
            ></div>

            <!-- 覆盖层 -->
            <div class="pane-overlay">
              <!-- 加载中 -->
              <div v-if="pane.isLoading" class="overlay-center loading">
                <Icon icon="ep:loading" :size="48" class="spin" />
                <p>正在连接...</p>
              </div>

              <!-- 空闲状态 -->
              <div v-else-if="!pane.isPlaying && !pane.error" class="overlay-center idle">
                <Icon icon="ep:video-camera" :size="48" />
                <p>窗口 {{ idx + 1 }}</p>
                <p class="tip">双击或拖拽通道到此处播放</p>
              </div>

              <!-- 错误状态 -->
              <div v-if="pane.error" class="overlay-center error">
                <Icon icon="ep:warning-filled" :size="48" />
                <p>{{ pane.error }}</p>
                <el-button size="small" @click="retryPlay(idx)">重试</el-button>
              </div>

              <!-- 底部信息栏 -->
              <div v-if="pane.isPlaying && pane.channel" class="overlay-bottom">
                <span class="channel-name">
                  <span class="live-dot" :class="{ recording: pane.isPlayback }"></span>
                  {{ pane.channel.name }}
                  <el-tag v-if="pane.isPlayback" size="small" type="warning" style="margin-left: 4px">回放</el-tag>
                </span>
                <el-tag size="small" type="success">WVP</el-tag>
              </div>

              <!-- 悬停工具栏 -->
              <div v-if="pane.isPlaying" class="pane-toolbar">
                <el-button size="small" @click.stop="handleSnapshot(idx)" title="截图">
                  <Icon icon="ep:camera" />
                </el-button>
                <el-button size="small" @click.stop="openRecordDialog(pane.channel)" title="录像回放" v-if="!pane.isPlayback">
                  <Icon icon="ep:video-play" />
                </el-button>
                <el-button size="small" @click.stop="toggleFullscreen(idx)" title="全屏">
                  <Icon icon="ep:full-screen" />
                </el-button>
                <el-button size="small" type="danger" @click.stop="stopPlay(idx)" title="停止">
                  <Icon icon="ep:close" />
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部控制栏 -->
        <div class="control-bar">
          <div class="control-left">
            <span class="info-text">
              <Icon icon="ep:connection" />
              WVP-GB28181 模式
            </span>
          </div>
          <div class="control-center">
            <el-button size="small" @click="stopAllPlayers" :disabled="!hasPlayingPanes">
              <Icon icon="ep:video-pause" />
              停止全部
            </el-button>
          </div>
          <div class="control-right">
            <el-select v-model="gridLayout" size="small" style="width: 100px" @change="handleLayoutChange">
              <el-option :value="1" label="1×1" />
              <el-option :value="4" label="2×2" />
              <el-option :value="6" label="2×3" />
              <el-option :value="9" label="3×3" />
              <el-option :value="16" label="4×4" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 右侧云台控制面板 -->
      <div class="right-panel" v-if="showPtzPanel">
        <div class="panel-header">
          <Icon icon="ep:aim" />
          <span>云台控制</span>
          <el-button text size="small" @click="showPtzPanel = false">
            <Icon icon="ep:close" />
          </el-button>
        </div>
        <div class="ptz-content">
          <!-- 当前通道 -->
          <div class="ptz-channel-info" v-if="activeChannel">
            <el-tag type="primary" size="small">{{ activeChannel.name }}</el-tag>
          </div>
          <div class="ptz-channel-info" v-else>
            <el-tag type="info" size="small">请选择通道</el-tag>
          </div>

          <!-- 云台方向控制 -->
          <div class="ptz-direction">
            <div class="ptz-row">
              <div class="ptz-btn" @mousedown="handlePtz('upleft')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">↖</div>
              <div class="ptz-btn" @mousedown="handlePtz('up')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">↑</div>
              <div class="ptz-btn" @mousedown="handlePtz('upright')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">↗</div>
            </div>
            <div class="ptz-row">
              <div class="ptz-btn" @mousedown="handlePtz('left')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">←</div>
              <div class="ptz-btn center">PTZ</div>
              <div class="ptz-btn" @mousedown="handlePtz('right')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">→</div>
            </div>
            <div class="ptz-row">
              <div class="ptz-btn" @mousedown="handlePtz('downleft')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">↙</div>
              <div class="ptz-btn" @mousedown="handlePtz('down')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">↓</div>
              <div class="ptz-btn" @mousedown="handlePtz('downright')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">↘</div>
            </div>
          </div>

          <!-- 变焦/聚焦/光圈控制 -->
          <div class="ptz-controls">
            <div class="ptz-control-row">
              <span class="label">变焦</span>
              <el-button size="small" @mousedown="handlePtz('zoomin')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">
                <Icon icon="ep:zoom-in" />
              </el-button>
              <el-button size="small" @mousedown="handlePtz('zoomout')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">
                <Icon icon="ep:zoom-out" />
              </el-button>
            </div>
            <div class="ptz-control-row">
              <span class="label">聚焦</span>
              <el-button size="small" @mousedown="handlePtz('focusin')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">
                <Icon icon="ep:plus" />
              </el-button>
              <el-button size="small" @mousedown="handlePtz('focusout')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">
                <Icon icon="ep:minus" />
              </el-button>
            </div>
            <div class="ptz-control-row">
              <span class="label">光圈</span>
              <el-button size="small" @mousedown="handlePtz('irisin')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">
                <Icon icon="ep:plus" />
              </el-button>
              <el-button size="small" @mousedown="handlePtz('irisout')" @mouseup="handlePtzStop" @mouseleave="handlePtzStop">
                <Icon icon="ep:minus" />
              </el-button>
            </div>
          </div>

          <!-- 速度控制 -->
          <div class="ptz-speed">
            <span class="label">速度</span>
            <el-slider v-model="ptzSpeed" :min="1" :max="255" :step="1" size="small" />
            <span class="value">{{ ptzSpeed }}</span>
          </div>

          <!-- 预置位 -->
          <div class="ptz-preset">
            <div class="preset-header">
              <span>预置位</span>
              <el-button size="small" text @click="loadPresets" :loading="presetLoading">
                <Icon icon="ep:refresh" />
              </el-button>
            </div>
            <div class="preset-list" v-if="presets.length > 0">
              <div v-for="preset in presets" :key="preset.presetId" class="preset-item">
                <span>{{ preset.presetName || `预置位${preset.presetId}` }}</span>
                <el-button size="small" type="primary" text @click="callPresetPosition(preset.presetId)">调用</el-button>
              </div>
            </div>
            <div v-else class="preset-empty">暂无预置位</div>
            <div class="preset-actions">
              <el-input-number v-model="newPresetId" :min="1" :max="255" size="small" placeholder="编号" style="width: 80px" />
              <el-input v-model="newPresetName" size="small" placeholder="名称" style="width: 80px; margin-left: 4px" />
              <el-button size="small" type="primary" @click="addPresetPosition" style="margin-left: 4px">添加</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 云台控制开关按钮 -->
      <div class="ptz-toggle" v-if="!showPtzPanel && hasPlayingPanes" @click="showPtzPanel = true">
        <Icon icon="ep:aim" />
      </div>

      <!-- 右键菜单 -->
      <div
        v-if="contextMenu.visible"
        class="context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click.stop
      >
        <div class="context-menu-item" @click="contextMenuPlay">
          <Icon icon="ep:video-camera" />
          <span>实时预览</span>
        </div>
        <div class="context-menu-item" @click="contextMenuRecord">
          <Icon icon="ep:video-play" />
          <span>录像回放</span>
        </div>
        <div class="context-menu-item" @click="contextMenuPtz">
          <Icon icon="ep:aim" />
          <span>云台控制</span>
        </div>
      </div>
    </div>

    <!-- 录像回放对话框 -->
    <el-dialog
      v-model="recordDialog.visible"
      :title="'录像回放 - ' + (recordDialog.channel?.name || '')"
      width="900px"
      :close-on-click-modal="false"
      destroy-on-close
      @close="closeRecordDialog"
    >
      <div class="record-dialog-content">
        <!-- 左侧：日期和录像列表 -->
        <div class="record-sidebar">
          <el-date-picker
            v-model="recordDialog.date"
            type="date"
            placeholder="选择日期"
            size="small"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="queryRecordList"
          />
          <div class="record-list" v-loading="recordDialog.loading">
            <div
              v-for="(record, idx) in recordDialog.records"
              :key="idx"
              class="record-item"
              :class="{ active: recordDialog.selectedIndex === idx }"
              @click="selectRecord(idx)"
            >
              <Icon icon="ep:video-camera" />
              <span>{{ formatRecordTime(record) }}</span>
            </div>
            <el-empty v-if="!recordDialog.loading && recordDialog.records.length === 0" description="暂无录像" />
          </div>
        </div>

        <!-- 右侧：播放器 -->
        <div class="record-player-area">
          <div class="record-player">
            <div class="record-player-container" id="record-player-container"></div>
            <!-- 播放器覆盖层 -->
            <div class="record-overlay" v-if="!recordDialog.playing && !recordDialog.playLoading">
              <Icon icon="ep:video-play" :size="64" />
              <p>选择录像开始播放</p>
            </div>
            <div class="record-overlay loading" v-if="recordDialog.playLoading">
              <Icon icon="ep:loading" :size="48" class="spin" />
              <p>正在加载...</p>
            </div>
          </div>
          <!-- 底部控制栏 -->
          <div class="record-controls" v-if="recordDialog.playing">
            <el-button type="danger" @click="stopRecordPlay">
              <Icon icon="ep:video-pause" />
              停止播放
            </el-button>
            <div class="record-speed-inline">
              <span>倍速:</span>
              <el-select v-model="recordDialog.speed" size="default" style="width: 100px" @change="changeRecordSpeed">
                <el-option :value="0.25" label="0.25x" />
                <el-option :value="0.5" label="0.5x" />
                <el-option :value="1" label="1x 正常" />
                <el-option :value="2" label="2x" />
                <el-option :value="4" label="4x" />
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import dayjs from 'dayjs'
import {
  getChannelList,
  playChannel,
  stopChannel,
  ptzControl,
  queryPresets,
  callPreset,
  addPreset,
  queryRecords,
  startPlayback,
  stopPlayback,
  setPlaybackSpeed,
  getChannelPlayId,
  getChannelGbId,
  type WvpChannelItem,
  type WvpStreamInfo,
  type WvpRecordItem,
  type WvpPreset
} from '@/api/iot/video/wvpClient'

defineOptions({ name: 'RealTimePreviewWvp' })

// ==================== 类型定义 ====================

interface PaneType {
  channel: WvpChannelItem | null
  player: any
  streamInfo: WvpStreamInfo | null
  isPlaying: boolean
  isLoading: boolean
  isPlayback: boolean
  error: string | null
}

// ==================== 状态 ====================

const searchKeyword = ref('')
const channelList = ref<WvpChannelItem[]>([])
const channelLoading = ref(false)

const gridLayout = ref(4)
const activePane = ref(0)
const dragOverPane = ref(-1)
const panes = ref<PaneType[]>([])

const showPtzPanel = ref(false)
const ptzSpeed = ref(50)

// 预置位
const presets = ref<WvpPreset[]>([])
const presetLoading = ref(false)
const newPresetId = ref(1)
const newPresetName = ref('')

// 右键菜单
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  channel: null as WvpChannelItem | null
})

// 录像回放对话框
const recordDialog = ref({
  visible: false,
  channel: null as WvpChannelItem | null,
  date: dayjs().format('YYYY-MM-DD'),
  records: [] as WvpRecordItem[],
  loading: false,
  selectedIndex: -1,
  playing: false,
  playLoading: false,
  paused: false,
  speed: 1,
  streamInfo: null as WvpStreamInfo | null,
  currentTimeStr: '00:00:00'
})

// 录像播放器实例
let recordPlayer: any = null

// Jessibuca 播放器实例管理
const jessibucaPlayers = new Map<number, any>()

// ==================== 计算属性 ====================

const filteredChannels = computed(() => {
  if (!searchKeyword.value) return channelList.value
  const keyword = searchKeyword.value.toLowerCase()
  return channelList.value.filter(
    (ch) =>
      ch.name?.toLowerCase().includes(keyword) ||
      ch.channelId?.toLowerCase().includes(keyword)
  )
})

const gridLayoutClass = computed(() => {
  const map: Record<number, string> = {
    1: 'grid-1x1',
    4: 'grid-2x2',
    6: 'grid-2x3',
    9: 'grid-3x3',
    16: 'grid-4x4'
  }
  return map[gridLayout.value] || 'grid-2x2'
})

const hasPlayingPanes = computed(() => panes.value.some((p) => p.isPlaying))

const activeChannel = computed(() => panes.value[activePane.value]?.channel)

// ==================== 初始化 ====================

const initPanes = (count: number) => {
  panes.value = Array.from({ length: count }, () => ({
    channel: null,
    player: null,
    streamInfo: null,
    isPlaying: false,
    isLoading: false,
    isPlayback: false,
    error: null
  }))
  activePane.value = 0
}

const handleLayoutChange = async (val: number) => {
  // 先停止所有播放器
  await stopAllPlayersAsync()
  // 等待 DOM 更新
  await nextTick()
  // 重新初始化窗格
  initPanes(val)
}

// ==================== 通道列表 ====================

const loadChannelList = async () => {
  channelLoading.value = true
  try {
    const result = await getChannelList({ page: 1, count: 200 })
    channelList.value = result?.list || []
    console.log('[WVP预览] 加载通道列表:', channelList.value.length)
  } catch (e: any) {
    console.error('[WVP预览] 加载通道失败:', e)
    ElMessage.error('加载通道列表失败: ' + (e?.message || '未知错误'))
  } finally {
    channelLoading.value = false
  }
}

const isChannelPlaying = (channel: WvpChannelItem) => {
  const playId = getChannelPlayId(channel)
  return panes.value.some((p) => p.channel && getChannelPlayId(p.channel) === playId && p.isPlaying)
}

// ==================== 右键菜单 ====================

const showChannelContextMenu = (e: MouseEvent, channel: WvpChannelItem) => {
  contextMenu.value = {
    visible: true,
    x: e.clientX,
    y: e.clientY,
    channel
  }
}

const hideContextMenu = () => {
  contextMenu.value.visible = false
}

const contextMenuPlay = () => {
  if (contextMenu.value.channel) {
    handleChannelDoubleClick(contextMenu.value.channel)
  }
  hideContextMenu()
}

const contextMenuRecord = () => {
  if (contextMenu.value.channel) {
    openRecordDialog(contextMenu.value.channel)
  }
  hideContextMenu()
}

const contextMenuPtz = () => {
  if (contextMenu.value.channel) {
    // 先播放通道，然后打开云台面板
    handleChannelDoubleClick(contextMenu.value.channel)
    showPtzPanel.value = true
  }
  hideContextMenu()
}

// ==================== 拖拽操作 ====================

const handleDragStart = (e: DragEvent, channel: WvpChannelItem) => {
  e.dataTransfer!.effectAllowed = 'copy'
  e.dataTransfer!.setData('channel', JSON.stringify(channel))
}

const handleDrop = async (e: DragEvent, paneIndex: number) => {
  e.preventDefault()
  dragOverPane.value = -1
  try {
    const channel = JSON.parse(e.dataTransfer!.getData('channel'))
    await doPlayChannel(channel, paneIndex)
  } catch (err) {
    console.error('[WVP预览] 拖拽失败:', err)
  }
}

// ==================== 双击播放 ====================

const handleChannelDoubleClick = async (channel: WvpChannelItem) => {
  // 找空窗口
  let targetIdx = panes.value.findIndex((p) => !p.channel && !p.isPlaying)
  if (targetIdx === -1) {
    targetIdx = activePane.value
  }
  await doPlayChannel(channel, targetIdx)
}

// ==================== 播放逻辑 ====================

const doPlayChannel = async (channel: WvpChannelItem, paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 停止之前的播放
  if (pane.isPlaying || pane.player) {
    await stopPlayAsync(paneIndex)
  }

  pane.channel = channel
  pane.isLoading = true
  pane.isPlayback = false
  pane.error = null
  activePane.value = paneIndex

  try {
    const playId = getChannelPlayId(channel)
    console.log('[WVP预览] 开始播放:', channel.name, 'PlayID:', playId)

    // 调用 WVP API 获取播放地址
    const streamInfo = await playChannel(playId)
    console.log('[WVP预览] 流信息:', streamInfo)

    if (!streamInfo) {
      throw new Error('未获取到流信息')
    }

    pane.streamInfo = streamInfo

    // 获取 FLV 播放地址（优先 ws_flv）
    const playUrl = streamInfo.ws_flv || streamInfo.wss_flv || streamInfo.flv
    if (!playUrl) {
      throw new Error('未获取到播放地址')
    }

    console.log('[WVP预览] 播放地址:', playUrl)

    await nextTick()

    // 使用 Jessibuca 播放
    const success = await playWithJessibuca(paneIndex, playUrl)
    if (success) {
      pane.isPlaying = true
      ElMessage.success(`正在播放: ${channel.name}`)
    } else {
      throw new Error('播放器连接失败')
    }
  } catch (e: any) {
    console.error('[WVP预览] 播放失败:', e)
    pane.error = e?.message || '播放失败'
    pane.isPlaying = false
  } finally {
    pane.isLoading = false
  }
}

// Jessibuca 播放器
const playWithJessibuca = (idx: number, url: string): Promise<boolean> => {
  return new Promise((resolve) => {
    try {
      const containerId = `jessibuca-container-${idx}`
      const wrapperEl = document.getElementById(containerId)
      if (!wrapperEl) {
        console.error('[Jessibuca] 容器不存在:', containerId)
        resolve(false)
        return
      }

      // 先销毁旧实例
      destroyPlayer(idx)

      // 关键：完全移除旧的内容并创建新的子容器
      wrapperEl.innerHTML = ''
      const playerContainer = document.createElement('div')
      playerContainer.style.width = '100%'
      playerContainer.style.height = '100%'
      wrapperEl.appendChild(playerContainer)

      // 检查 Jessibuca 是否可用
      if (typeof (window as any).Jessibuca === 'undefined') {
        console.error('[Jessibuca] 播放器未加载')
        resolve(false)
        return
      }

      // 创建播放器
      const player = new (window as any).Jessibuca({
        container: playerContainer,
        videoBuffer: 0.2,
        isResize: true,
        loadingText: '加载中...',
        debug: false,
        decoder: '/jessibuca/decoder.js',
        timeout: 15,
        supportDblclickFullscreen: false,
        showBandwidth: false,
        operateBtns: {
          fullscreen: false,
          screenshot: false,
          play: false,
          audio: false
        },
        forceNoOffscreen: true,
        isNotMute: false,
        hasAudio: false
      })

      let resolved = false

      player.on('play', () => {
        console.log(`[Jessibuca] 窗口 ${idx + 1} 播放成功`)
        if (!resolved) {
          resolved = true
          resolve(true)
        }
      })

      player.on('error', (e: any) => {
        console.error(`[Jessibuca] 窗口 ${idx + 1} 错误:`, e)
        if (!resolved) {
          resolved = true
          resolve(false)
        }
      })

      player.on('timeout', () => {
        console.warn(`[Jessibuca] 窗口 ${idx + 1} 超时`)
        if (!resolved) {
          resolved = true
          resolve(false)
        }
      })

      // 保存实例
      jessibucaPlayers.set(idx, player)
      panes.value[idx].player = player

      // 开始播放
      console.log(`[Jessibuca] 窗口 ${idx + 1} 开始播放:`, url)
      player.play(url)

      // 超时保护
      setTimeout(() => {
        if (!resolved) {
          resolved = true
          resolve(false)
        }
      }, 15000)
    } catch (e) {
      console.error('[Jessibuca] 初始化失败:', e)
      resolve(false)
    }
  })
}

// ==================== 销毁播放器 ====================

const destroyPlayer = (idx: number) => {
  const player = jessibucaPlayers.get(idx)
  if (player) {
    try {
      player.off('play')
      player.off('error')
      player.off('timeout')
      if (typeof player.close === 'function') {
        player.close()
      }
      if (typeof player.destroy === 'function') {
        player.destroy()
      }
    } catch (e) {
      console.warn(`[Jessibuca] 窗口 ${idx + 1} 销毁警告:`, e)
    }
    jessibucaPlayers.delete(idx)
  }

  const pane = panes.value[idx]
  if (pane) {
    pane.player = null
  }
}

// ==================== 停止播放 ====================

const stopPlayAsync = async (paneIndex: number): Promise<void> => {
  const pane = panes.value[paneIndex]
  if (!pane) return

  // 先销毁播放器
  destroyPlayer(paneIndex)

  // 清空容器
  const containerId = `jessibuca-container-${paneIndex}`
  const containerEl = document.getElementById(containerId)
  if (containerEl) {
    containerEl.innerHTML = ''
  }

  // 通知 WVP 停止
  if (pane.channel) {
    try {
      await stopChannel(getChannelPlayId(pane.channel))
    } catch (e) {
      // 忽略停止失败
    }
  }

  // 重置状态
  pane.streamInfo = null
  pane.isPlaying = false
  pane.isLoading = false
  pane.isPlayback = false
  pane.error = null
  pane.channel = null
}

const stopPlay = (paneIndex: number) => {
  stopPlayAsync(paneIndex)
}

const stopAllPlayersAsync = async () => {
  const promises = panes.value.map((_, idx) => stopPlayAsync(idx))
  await Promise.all(promises)
}

const stopAllPlayers = () => {
  stopAllPlayersAsync()
}

const retryPlay = (idx: number) => {
  const pane = panes.value[idx]
  if (pane?.channel) {
    doPlayChannel(pane.channel, idx)
  }
}

// ==================== 截图 ====================

const handleSnapshot = (idx: number) => {
  const player = jessibucaPlayers.get(idx)
  const pane = panes.value[idx]
  if (player && pane?.channel) {
    const fileName = `WVP_${pane.channel.name}_${Date.now()}`
    player.screenshot(fileName, 'png', 0.92, 'download')
    ElMessage.success('截图已保存')
  }
}

// ==================== 全屏 ====================

const toggleFullscreen = (idx: number) => {
  const containerId = `jessibuca-container-${idx}`
  const containerEl = document.getElementById(containerId)
  if (containerEl) {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    } else {
      containerEl.requestFullscreen()
    }
  }
}

// ==================== 云台控制 ====================

const handlePtz = async (command: string) => {
  const channel = activeChannel.value
  if (!channel) {
    ElMessage.warning('请先选择播放窗口')
    return
  }

  try {
    await ptzControl(getChannelPlayId(channel), command, ptzSpeed.value)
  } catch (e: any) {
    console.error('[PTZ] 控制失败:', e)
    ElMessage.error('云台控制失败')
  }
}

const handlePtzStop = async () => {
  const channel = activeChannel.value
  if (channel) {
    ptzControl(getChannelPlayId(channel), 'stop', 0).catch(() => {})
  }
}

// ==================== 预置位 ====================

const loadPresets = async () => {
  const channel = activeChannel.value
  if (!channel) {
    ElMessage.warning('请先选择通道')
    return
  }

  presetLoading.value = true
  try {
    presets.value = await queryPresets(getChannelPlayId(channel))
  } catch (e: any) {
    console.error('[预置位] 查询失败:', e)
    ElMessage.error('查询预置位失败')
  } finally {
    presetLoading.value = false
  }
}

const callPresetPosition = async (presetId: number) => {
  const channel = activeChannel.value
  if (!channel) return

  try {
    await callPreset(getChannelPlayId(channel), presetId)
    ElMessage.success('已调用预置位')
  } catch (e: any) {
    console.error('[预置位] 调用失败:', e)
    ElMessage.error('调用预置位失败')
  }
}

const addPresetPosition = async () => {
  const channel = activeChannel.value
  if (!channel) {
    ElMessage.warning('请先选择通道')
    return
  }

  if (!newPresetId.value) {
    ElMessage.warning('请输入预置位编号')
    return
  }

  try {
    await addPreset(getChannelPlayId(channel), newPresetId.value, newPresetName.value || `预置位${newPresetId.value}`)
    ElMessage.success('添加预置位成功')
    loadPresets()
    newPresetId.value = newPresetId.value + 1
    newPresetName.value = ''
  } catch (e: any) {
    console.error('[预置位] 添加失败:', e)
    ElMessage.error('添加预置位失败')
  }
}

// ==================== 录像回放 ====================

const openRecordDialog = (channel: WvpChannelItem | null) => {
  if (!channel) {
    ElMessage.warning('请选择通道')
    return
  }

  recordDialog.value = {
    visible: true,
    channel,
    date: dayjs().format('YYYY-MM-DD'),
    records: [],
    loading: false,
    selectedIndex: -1,
    playing: false,
    playLoading: false,
    paused: false,
    speed: 1,
    streamInfo: null,
    currentTimeStr: '00:00:00'
  }

  nextTick(() => {
    queryRecordList()
  })
}

const closeRecordDialog = async () => {
  // 停止录像播放
  await stopRecordPlay()
  recordDialog.value.visible = false
}

const queryRecordList = async () => {
  const channel = recordDialog.value.channel
  if (!channel) return

  const date = recordDialog.value.date
  if (!date) return

  recordDialog.value.loading = true
  recordDialog.value.records = []
  recordDialog.value.selectedIndex = -1

  try {
    const startTime = `${date} 00:00:00`
    const endTime = `${date} 23:59:59`
    // 录像查询需要使用 gbId（国标ID），不是 commonGbChannelId
    const gbId = getChannelGbId(channel)
    console.log('[录像回放] 查询录像, gbId:', gbId, 'channel:', channel)
    const records = await queryRecords(gbId, startTime, endTime)
    recordDialog.value.records = records
    console.log('[录像回放] 查询结果:', records.length, '条')
  } catch (e: any) {
    console.error('[录像回放] 查询失败:', e)
    ElMessage.error('查询录像失败: ' + (e?.message || '未知错误'))
  } finally {
    recordDialog.value.loading = false
  }
}

const formatRecordTime = (record: WvpRecordItem) => {
  const start = dayjs(record.startTime).format('HH:mm:ss')
  const end = dayjs(record.endTime).format('HH:mm:ss')
  return `${start} - ${end}`
}

const selectRecord = async (idx: number) => {
  recordDialog.value.selectedIndex = idx
  const record = recordDialog.value.records[idx]
  if (!record) return

  await playRecord(record)
}

const playRecord = async (record: WvpRecordItem) => {
  const channel = recordDialog.value.channel
  if (!channel) return

  // 先停止之前的播放
  await stopRecordPlay()

  recordDialog.value.playLoading = true

  try {
    // 录像回放使用 gbId
    const gbId = getChannelGbId(channel)
    console.log('[录像回放] 开始播放, gbId:', gbId)
    const streamInfo = await startPlayback(
      gbId,
      record.startTime,
      record.endTime
    )

    if (!streamInfo) {
      throw new Error('未获取到流信息')
    }

    recordDialog.value.streamInfo = streamInfo

    const playUrl = streamInfo.ws_flv || streamInfo.wss_flv || streamInfo.flv
    if (!playUrl) {
      throw new Error('未获取到播放地址')
    }

    console.log('[录像回放] 播放地址:', playUrl)

    await nextTick()

    // 创建录像播放器
    const success = await createRecordPlayer(playUrl)
    if (success) {
      recordDialog.value.playing = true
      recordDialog.value.paused = false
    } else {
      throw new Error('播放器初始化失败')
    }
  } catch (e: any) {
    console.error('[录像回放] 播放失败:', e)
    ElMessage.error('播放录像失败: ' + (e?.message || '未知错误'))
  } finally {
    recordDialog.value.playLoading = false
  }
}

const createRecordPlayer = (url: string): Promise<boolean> => {
  return new Promise((resolve) => {
    try {
      const containerId = 'record-player-container'
      const wrapperEl = document.getElementById(containerId)
      if (!wrapperEl) {
        console.error('[录像播放器] 容器不存在')
        resolve(false)
        return
      }

      // 销毁旧实例
      if (recordPlayer) {
        try {
          recordPlayer.destroy()
        } catch (e) {}
        recordPlayer = null
      }

      wrapperEl.innerHTML = ''
      const playerContainer = document.createElement('div')
      playerContainer.style.width = '100%'
      playerContainer.style.height = '100%'
      wrapperEl.appendChild(playerContainer)

      if (typeof (window as any).Jessibuca === 'undefined') {
        console.error('[录像播放器] Jessibuca 未加载')
        resolve(false)
        return
      }

      recordPlayer = new (window as any).Jessibuca({
        container: playerContainer,
        videoBuffer: 0.5,
        isResize: true,
        loadingText: '加载中...',
        debug: false,
        decoder: '/jessibuca/decoder.js',
        timeout: 30,
        supportDblclickFullscreen: true,
        showBandwidth: false,
        operateBtns: {
          fullscreen: true,
          screenshot: true,
          play: false,
          audio: true
        },
        forceNoOffscreen: true,
        isNotMute: false,
        hasAudio: true
      })

      let resolved = false

      const markSuccess = () => {
        if (!resolved) {
          console.log('[录像播放器] 播放成功')
          resolved = true
          resolve(true)
        }
      }

      // 监听多个事件确保能捕获到播放成功
      recordPlayer.on('play', markSuccess)
      recordPlayer.on('playing', markSuccess)
      recordPlayer.on('videoInfo', markSuccess)

      recordPlayer.on('error', (e: any) => {
        console.error('[录像播放器] 错误:', e)
        if (!resolved) {
          resolved = true
          resolve(false)
        }
      })

      recordPlayer.on('timeout', () => {
        console.warn('[录像播放器] 超时')
        if (!resolved) {
          resolved = true
          resolve(false)
        }
      })

      recordPlayer.on('timeUpdate', (ts: number) => {
        // timeUpdate 事件说明已经在播放了
        markSuccess()
        // 更新播放时间显示
        const seconds = Math.floor(ts)
        const h = Math.floor(seconds / 3600)
        const m = Math.floor((seconds % 3600) / 60)
        const s = seconds % 60
        recordDialog.value.currentTimeStr = `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
      })

      console.log('[录像播放器] 开始播放:', url)
      recordPlayer.play(url)

      // 3秒后如果还没收到事件，但没有错误，也认为成功
      setTimeout(() => {
        if (!resolved) {
          console.log('[录像播放器] 超时但尝试认为成功')
          resolved = true
          resolve(true)
        }
      }, 3000)

      // 30秒最终超时
      setTimeout(() => {
        if (!resolved) {
          resolved = true
          resolve(false)
        }
      }, 30000)
    } catch (e) {
      console.error('[录像播放器] 初始化失败:', e)
      resolve(false)
    }
  })
}

const stopRecordPlay = async () => {
  // 销毁播放器
  if (recordPlayer) {
    try {
      recordPlayer.destroy()
    } catch (e) {}
    recordPlayer = null
  }

  // 通知 WVP 停止回放
  const channel = recordDialog.value.channel
  const streamInfo = recordDialog.value.streamInfo
  if (channel && streamInfo?.stream) {
    try {
      // 停止回放使用 gbId
      await stopPlayback(getChannelGbId(channel), streamInfo.stream)
    } catch (e) {}
  }

  recordDialog.value.playing = false
  recordDialog.value.paused = false
  recordDialog.value.streamInfo = null
  recordDialog.value.currentTimeStr = '00:00:00'

  // 清空容器
  const containerEl = document.getElementById('record-player-container')
  if (containerEl) {
    containerEl.innerHTML = ''
  }
}

const changeRecordSpeed = async (speed: number) => {
  const channel = recordDialog.value.channel
  const streamInfo = recordDialog.value.streamInfo
  if (!channel || !streamInfo?.stream) {
    ElMessage.warning('请先播放录像')
    return
  }

  try {
    // 倍速 API 使用数据库 ID (Integer)
    const channelDbId = channel.gbId || channel.channelId || channel.id
    // 国标 ID（用于日志）
    const gbDeviceId = channel.gbDeviceId || channel.deviceId
    
    console.log('[录像回放] 设置倍速:', speed)
    console.log('  - 数据库 ID:', channelDbId)
    console.log('  - 国标 ID:', gbDeviceId)
    console.log('  - stream:', streamInfo.stream)
    
    // 调用 WVP API 设置服务端倍速（使用数据库 ID）
    await setPlaybackSpeed(String(channelDbId), streamInfo.stream, speed)
    
    // 设置播放器本地倍速（与 WVP 原版 record.vue 保持一致）
    if (recordPlayer && typeof recordPlayer.setPlaybackRate === 'function') {
      recordPlayer.setPlaybackRate(speed)
      console.log('[录像回放] 已设置播放器本地倍速:', speed)
    }
    
    ElMessage.success(`倍速已设置为 ${speed}x`)
  } catch (e: any) {
    console.error('[录像回放] 设置倍速失败:', e)
    ElMessage.error('设置倍速失败: ' + (e?.message || '未知错误'))
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  initPanes(gridLayout.value)
  loadChannelList()

  // 点击其他地方关闭右键菜单
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  // 同步销毁所有播放器
  panes.value.forEach((_, idx) => destroyPlayer(idx))

  // 销毁录像播放器
  if (recordPlayer) {
    try {
      recordPlayer.destroy()
    } catch (e) {}
    recordPlayer = null
  }

  document.removeEventListener('click', hideContextMenu)
})
</script>

<style lang="scss" scoped>
.wvp-preview-container {
  display: flex;
  height: 100%;
  background: #1a1a2e;
  color: #e0e6ed;
  position: relative;
}

// 左侧面板
.left-panel {
  width: 280px;
  display: flex;
  flex-direction: column;
  background: #16213e;
  border-right: 1px solid #0f3460;

  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-bottom: 1px solid #0f3460;
    font-weight: 600;
  }

  .search-box {
    padding: 12px;
  }

  .channel-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 8px;
  }

  .channel-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    margin-bottom: 4px;
    background: #1a1a2e;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #0f3460;
    }

    &.online {
      border-left: 3px solid #67c23a;
    }

    &.playing {
      background: rgba(64, 158, 255, 0.2);
      border-color: #409eff;
    }

    .channel-info {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
      min-width: 0;

      .channel-name {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .panel-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border-top: 1px solid #0f3460;

    .channel-count {
      font-size: 12px;
      color: #7c8db0;
    }
  }
}

// 中间播放区域
.center-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.player-grid {
  flex: 1;
  display: grid;
  gap: 4px;
  padding: 8px;
  background: #000;
  min-height: 0;

  &.grid-1x1 { grid-template-columns: 1fr; }
  &.grid-2x2 { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(2, 1fr); }
  &.grid-2x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(2, 1fr); }
  &.grid-3x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(3, 1fr); }
  &.grid-4x4 { grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(4, 1fr); }
}

.player-pane {
  position: relative;
  background: #0a0a0a;
  border: 2px solid #2a2a2a;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;

  &.active {
    border-color: #409eff;
    box-shadow: 0 0 8px rgba(64, 158, 255, 0.4);
  }

  &.drag-over {
    border-color: #67c23a;
    background: rgba(103, 194, 58, 0.1);
  }

  .video-container {
    width: 100%;
    height: 100%;
  }

  .pane-overlay {
    position: absolute;
    inset: 0;
    pointer-events: none;

    .overlay-center {
      position: absolute;
      inset: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #7c8db0;

      &.loading .spin {
        animation: spin 1s linear infinite;
      }

      &.error {
        color: #f56c6c;
        pointer-events: auto;
      }

      .tip {
        font-size: 12px;
        color: #5a6a80;
      }
    }

    .overlay-bottom {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 8px 12px;
      background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
      display: flex;
      justify-content: space-between;
      align-items: center;

      .channel-name {
        display: flex;
        align-items: center;
        gap: 6px;
        color: #fff;
        font-size: 12px;

        .live-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: #67c23a;
          animation: pulse 1.5s infinite;

          &.recording {
            background: #e6a23c;
          }
        }
      }
    }

    .pane-toolbar {
      position: absolute;
      top: 8px;
      right: 8px;
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.2s;
      pointer-events: auto;
    }
  }

  &:hover .pane-toolbar {
    opacity: 1;
  }
}

.control-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #16213e;
  border-top: 1px solid #0f3460;

  .info-text {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #7c8db0;
    font-size: 13px;
  }
}

// 右侧云台面板
.right-panel {
  width: 260px;
  background: #16213e;
  border-left: 1px solid #0f3460;
  display: flex;
  flex-direction: column;

  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-bottom: 1px solid #0f3460;
    font-weight: 600;

    .el-button {
      margin-left: auto;
    }
  }

  .ptz-content {
    flex: 1;
    padding: 12px;
    overflow-y: auto;
  }

  .ptz-channel-info {
    margin-bottom: 12px;
    text-align: center;
  }
}

// 云台方向控制
.ptz-direction {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;

  .ptz-row {
    display: flex;
    justify-content: center;
    gap: 4px;
  }

  .ptz-btn {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #1a1a2e;
    border: 1px solid #0f3460;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
    transition: all 0.2s;
    user-select: none;

    &:hover {
      background: #0f3460;
    }

    &:active {
      background: #409eff;
      color: #fff;
    }

    &.center {
      background: #0f3460;
      cursor: default;
      font-size: 11px;
      font-weight: bold;
    }
  }
}

.ptz-controls {
  margin-bottom: 16px;

  .ptz-control-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .label {
      width: 36px;
      font-size: 12px;
      color: #7c8db0;
    }
  }
}

.ptz-speed {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;

  .label {
    width: 36px;
    font-size: 12px;
    color: #7c8db0;
  }

  .el-slider {
    flex: 1;
  }

  .value {
    width: 32px;
    font-size: 12px;
    color: #7c8db0;
    text-align: right;
  }
}

.ptz-preset {
  .preset-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 13px;
  }

  .preset-list {
    max-height: 150px;
    overflow-y: auto;
    margin-bottom: 8px;
  }

  .preset-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 8px;
    background: #1a1a2e;
    border-radius: 4px;
    margin-bottom: 4px;
    font-size: 12px;
  }

  .preset-empty {
    text-align: center;
    color: #7c8db0;
    font-size: 12px;
    padding: 16px;
  }

  .preset-actions {
    display: flex;
    align-items: center;
    margin-top: 8px;
  }
}

// 云台开关按钮
.ptz-toggle {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #16213e;
  border: 1px solid #0f3460;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;

  &:hover {
    background: #0f3460;
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  background: #16213e;
  border: 1px solid #0f3460;
  border-radius: 6px;
  padding: 4px 0;
  min-width: 140px;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);

  .context-menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    cursor: pointer;
    font-size: 13px;
    transition: background 0.2s;

    &:hover {
      background: #0f3460;
    }
  }
}

// 录像回放对话框
.record-dialog-content {
  display: flex;
  height: 500px;
  gap: 16px;
}

.record-sidebar {
  width: 200px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .record-list {
    flex: 1;
    overflow-y: auto;
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    padding: 8px;
  }

  .record-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.active {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
  }
}

.record-player-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.record-player {
  flex: 1;
  position: relative;
  background: #000;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
  min-height: 0;

  .record-player-container {
    width: 100%;
    height: 100%;
  }

  .record-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: #7c8db0;
    background: rgba(0, 0, 0, 0.8);

    &.loading .spin {
      animation: spin 1s linear infinite;
    }
  }
}

.record-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #1e293b;
  border-radius: 0 0 4px 4px;

  .record-speed-inline {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #e0e6ed;
    font-size: 14px;
  }
}

// 动画
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// Element Plus 暗色适配
:deep(.el-input__wrapper) {
  background: #1a1a2e;
  border-color: #0f3460;
  box-shadow: none;
}

:deep(.el-input__inner) {
  color: #e0e6ed;
}

:deep(.el-select) {
  .el-input__wrapper {
    background: #1a1a2e;
    border-color: #0f3460;
  }
}

:deep(.el-slider__runway) {
  background: #0f3460;
}

:deep(.el-slider__bar) {
  background: #409eff;
}
</style>
