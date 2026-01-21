<template>
  <ContentWrap 
    :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }" 
    style="height: calc(100vh - var(--page-top-gap, 70px)); padding-top: var(--page-top-gap, 70px); margin-bottom: 0"
  >
    <div class="dark-theme-page">
      <div class="video-preview-container">
        <div class="main-layout">
          <div class="smartpss-layout">
            <!-- 左侧设备树面板 -->
            <div class="left-panel">
              <el-collapse v-model="leftPanelActive" accordion>
                <el-collapse-item name="device">
                  <template #title>
                    <span>设备</span>
                  </template>
                  <div class="search-box">
                    <el-input 
                      v-model="deviceSearchKeyword" 
                      placeholder="搜索通道名称..." 
                      clearable 
                      size="small"
                      @keyup.enter="handleChannelSearch"
                      @clear="handleSearchClear"
                    >
                      <template #prefix>
                        <Icon icon="ep:search" />
                      </template>
                      <template #append>
                        <el-button :icon="Search" @click="handleChannelSearch" />
                      </template>
                    </el-input>
                  </div>
                  <el-tree 
                    :data="cameraTreeData" 
                    :props="treeProps" 
                    :lazy="true" 
                    :load="loadTreeNode" 
                    :accordion="true" 
                    node-key="id" 
                    @node-click="handleCameraSelect" 
                    class="device-tree" 
                    :allow-drag="allowDrag"
                  >
                    <template #default="{ data }">
                      <div 
                        class="tree-node" 
                        :draggable="data.type === 'channel'" 
                        @dragstart="handleDragStart($event, data)"
                        @dblclick="handleChannelDoubleClick(data)"
                      >
                        <Icon v-if="data.type === 'building'" icon="ep:office-building" style="color: #409eff" />
                        <Icon v-else-if="data.type === 'floor'" icon="ep:tickets" style="color: #67c23a" />
                        <Icon v-else-if="data.type === 'area'" icon="ep:location" style="color: #e6a23c" />
                        <Icon v-else-if="data.type === 'channels'" icon="ep:folder-opened" style="color: #909399" />
                        <Icon v-else-if="data.type === 'channel'" icon="ep:video-camera" style="color: #f56c6c" />
                        <Icon v-else icon="ep:video-camera" style="color: #f56c6c" />
                        <span>{{ data.name }}</span>
                      </div>
                    </template>
                  </el-tree>
                </el-collapse-item>
              </el-collapse>
            </div>

            <!-- 中间播放区域 -->
            <div class="center-panel">
              <div class="player-section">
                <!-- 播放器网格 -->
                <div class="player-grid" :class="gridLayoutClass" ref="playerGridRef">
                  <div 
                    v-for="(pane, idx) in panes" 
                    :key="idx" 
                    class="player-pane" 
                    :class="{ active: activePane === idx, 'drag-over': dragOverPane === idx }" 
                    @click="handlePaneClick(idx)" 
                    @drop="handleDrop($event, idx)" 
                    @dragover.prevent="handleDragOver($event, idx)" 
                    @dragleave="handleDragLeave"
                  >
                    <video 
                      class="pane-video" 
                      :ref="el => setPaneVideoRef(el, idx)" 
                      :data-index="idx" 
                      muted 
                      playsinline 
                      autoplay
                    ></video>
                    
                    <div class="pane-overlay">
                      <div v-if="pane.isLoading" class="overlay-center loading">
                        <Icon icon="ep:loading" :size="48" class="loading-icon" />
                        <p class="window-label">正在连接...</p>
                      </div>
                      
                      <div v-else-if="!pane.isPlaying" class="overlay-center idle">
                        <Icon icon="ep:video-pause" :size="48" />
                        <p class="window-label">窗口 {{ idx + 1 }}</p>
                        <p class="tip-text">拖拽或双击添加</p>
                      </div>
                      
                      <div v-if="pane.error" class="overlay-center error">
                        <Icon icon="ep:warning-filled" :size="32" style="color: #f56c6c" />
                        <p class="window-label" style="color: #f56c6c; font-size: 12px">{{ pane.error }}</p>
                      </div>
                      
                      <div v-if="pane.channel && pane.isPlaying" class="overlay-bottom">
                        <span class="device-name">
                          <span class="live-dot"></span>
                          {{ pane.channel.channelName || pane.channel.name }}
                        </span>
                      </div>
                      
                      <div v-if="pane.isPlaying" class="pane-toolbar">
                        <el-button size="small" type="danger" @click.stop="handleStopPlay(idx)">
                          <Icon icon="ep:close" />
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 底部控制栏 -->
                <div class="playback-controls">
                  <div class="controls-left">
                    <div class="protocol-info">
                      <Icon icon="ep:connection" style="color: #67c23a;" />
                      <span>多屏预览 - ZLMediaKit</span>
                    </div>
                  </div>
                  
                  <div class="controls-right">
                    <el-button size="small" @click="handleStopAllPlayers" type="danger" :disabled="!hasPlayingPanes">
                      停止全部
                    </el-button>
                    
                    <el-select v-model="gridLayout" size="small" style="width: 96px" @change="setLayout">
                      <el-option :value="4" label="2×2" />
                      <el-option :value="6" label="2×3" />
                      <el-option :value="9" label="3×3" />
                      <el-option :value="16" label="4×4" />
                      <el-option :value="25" label="5×5" />
                    </el-select>
                    
                    <el-button size="small" @click="handleFullscreen">
                      <Icon icon="ep:full-screen" />
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { ContentWrap } from '@/components/ContentWrap'
import { Icon } from '@/components/Icon'
import { getBuildingList } from '@/api/iot/spatial/building'
import { getFloorListByBuildingId } from '@/api/iot/spatial/floor'
import { getAreaListByFloorId } from '@/api/iot/spatial/area'
import { getChannelPage } from '@/api/iot/channel'
import { getLivePlayUrl, stopStream } from '@/api/iot/video/zlm'
import mpegts from 'mpegts.js'

defineOptions({ name: 'MultiScreenPreview' })

interface PaneType {
  channel: any | null
  videoEl: HTMLVideoElement | null
  player: mpegts.Player | null
  rtcConnection: RTCPeerConnection | null
  playMode: 'webrtc' | 'flv' | null
  isPlaying: boolean
  isLoading: boolean
  error: string | null
}

const leftPanelActive = ref<string>('device')
const deviceSearchKeyword = ref('')
const cameraTreeData = ref<any[]>([])
const treeProps = { 
  children: 'children', 
  label: 'name', 
  isLeaf: (data: any) => data.type === 'device' || data.type === 'channel' 
}

const gridLayout = ref<number>(9)
const activePane = ref<number>(0)
const dragOverPane = ref<number>(-1)
const playerGridRef = ref<HTMLElement | null>(null)
const panes = ref<PaneType[]>([])
const currentQuality = ref<number>(1) // 默认子码流，多屏时节省带宽

// Firefox 浏览器检测 - Firefox 的 MSE 有并发限制
const isFirefox = navigator.userAgent.toLowerCase().includes('firefox')

// 播放队列 - Firefox 需要串行初始化播放器，避免 MSE 并发错误
let playQueue: Promise<void> = Promise.resolve()

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
  get host() { return window.location.hostname },
  get httpPort() { return window.location.port ? parseInt(window.location.port) : 80 },
  rtcHost: import.meta.env.VITE_ZLM_RTC_HOST || '39.108.87.226',
  rtcPort: parseInt(import.meta.env.VITE_ZLM_RTC_PORT || '48088')
}

const adaptPlayUrls = (urls: any): any => {
  if (!urls) return urls
  const intranet = isIntranetAccess()
  if (intranet) return urls
  
  const adapted = { ...urls }
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
  
  adapted.wsFlvUrl = replaceWsUrl(urls.wsFlvUrl)
  adapted.flvUrl = replaceHttpUrl(urls.flvUrl)
  adapted.hlsUrl = replaceHttpUrl(urls.hlsUrl)
  if (urls.webrtcUrl) adapted.webrtcUrl = replaceHttpUrl(urls.webrtcUrl)
  
  return adapted
}

const gridLayoutClass = computed<string>(() => {
  const map: Record<number, string> = { 4: 'grid-2x2', 6: 'grid-2x3', 9: 'grid-3x3', 16: 'grid-4x4', 25: 'grid-5x5' }
  return map[gridLayout.value] || 'grid-3x3'
})

const hasPlayingPanes = computed(() => panes.value.some(p => p.isPlaying || p.isLoading))

const setLayout = (val: number) => {
  stopAllPlayersSilently()
  gridLayout.value = val
  panes.value = Array.from({ length: val }, () => ({
    channel: null, videoEl: null, player: null, rtcConnection: null, playMode: null,
    isPlaying: false, isLoading: false, error: null
  }))
  activePane.value = 0
}

const setPaneVideoRef = (el: any, idx: number) => {
  if (el && panes.value[idx]) panes.value[idx].videoEl = el as HTMLVideoElement
}

const loadSpaceTree = async () => {
  try {
    const buildings = await getBuildingList()
    cameraTreeData.value = buildings.map((b: any) => ({
      id: `building-${b.id}`, name: b.name, type: 'building', buildingId: b.id
    }))
  } catch (e: any) {
    ElMessage.error('加载空间树失败')
  }
}

const loadTreeNode = async (node: any, resolve: Function) => {
  try {
    const data = node.data
    let children: any[] = []
    
    if (data.type === 'building') {
      children.push({ id: `channels-building-${data.buildingId}`, name: '通道', type: 'channels', buildingId: data.buildingId })
      const floors = await getFloorListByBuildingId(data.buildingId)
      children.push(...floors.map((f: any) => ({ id: `floor-${f.id}`, name: f.name, type: 'floor', floorId: f.id, buildingId: data.buildingId })))
    } else if (data.type === 'floor') {
      children.push({ id: `channels-floor-${data.floorId}`, name: '通道', type: 'channels', floorId: data.floorId, buildingId: data.buildingId })
      const areas = await getAreaListByFloorId(data.floorId)
      children.push(...areas.map((a: any) => ({ id: `area-${a.id}`, name: a.name, type: 'area', areaId: a.id, floorId: data.floorId })))
    } else if (data.type === 'area') {
      children.push({ id: `channels-area-${data.areaId}`, name: '通道', type: 'channels', areaId: data.areaId, floorId: data.floorId })
    } else if (data.type === 'channels') {
      const params: any = { pageNo: 1, pageSize: 100 }
      if (data.buildingId) params.buildingId = data.buildingId
      if (data.floorId) params.floorId = data.floorId
      if (data.areaId) params.areaId = data.areaId
      const channelsRes = await getChannelPage(params)
      children = (channelsRes.list || []).map((ch: any) => ({
        id: `channel-${ch.id}`, name: ch.channelName || `通道${ch.channelNo}`, type: 'channel', channelId: ch.id, channel: ch
      }))
    }
    resolve(children)
  } catch { resolve([]) }
}

const handleChannelSearch = async () => {
  const keyword = deviceSearchKeyword.value.trim()
  if (!keyword) return
  try {
    const result: any = await getChannelPage({ channelName: keyword, channelType: 'video', pageNo: 1, pageSize: 100 })
    const list = result?.list || []
    if (list.length > 0) {
      cameraTreeData.value = list.map((ch: any) => ({
        id: `channel-${ch.id}`, name: ch.channelName || `通道${ch.channelNo}`, type: 'channel', channelId: ch.id, channel: ch
      }))
    } else {
      cameraTreeData.value = []
    }
  } catch {}
}

const handleSearchClear = () => { deviceSearchKeyword.value = ''; loadSpaceTree() }
const handleCameraSelect = (_data: any) => {}
const allowDrag = (node: any) => node.data.type === 'channel'
const handleDragStart = (e: DragEvent, data: any) => {
  if (data.type !== 'channel') return
  e.dataTransfer!.effectAllowed = 'copy'
  e.dataTransfer!.setData('channel', JSON.stringify(data))
}
const handleDragOver = (_e: DragEvent, paneIndex: number) => { dragOverPane.value = paneIndex }
const handleDragLeave = () => { dragOverPane.value = -1 }
const handleDrop = async (e: DragEvent, paneIndex: number) => {
  e.preventDefault()
  dragOverPane.value = -1
  try {
    const channelData = JSON.parse(e.dataTransfer!.getData('channel'))
    await playChannelInPane(channelData, paneIndex)
  } catch {}
}

const handleChannelDoubleClick = async (data: any) => {
  if (data.type !== 'channel') return
  const emptyPaneIndex = panes.value.findIndex(pane => !pane.channel)
  if (emptyPaneIndex === -1) {
    ElMessage.warning('所有窗口都已占用')
    return
  }
  await playChannelInPane(data, emptyPaneIndex)
}

const handlePaneClick = (paneIndex: number) => { activePane.value = paneIndex }

const createFlvPlayerCore = async (pane: PaneType, videoEl: HTMLVideoElement, wsFlvUrl: string): Promise<boolean> => {
  return new Promise((resolve) => {
    try {
      if (!mpegts.isSupported()) throw new Error('浏览器不支持')
      const player = mpegts.createPlayer({
        type: 'flv', url: wsFlvUrl, isLive: true, hasAudio: false, hasVideo: true
      }, {
        enableWorker: false, 
        enableStashBuffer: true, 
        stashInitialSize: isFirefox ? 256 : 128, 
        lazyLoad: false,
        autoCleanupSourceBuffer: true, 
        autoCleanupMaxBackwardDuration: isFirefox ? 5 : 3,
        liveBufferLatencyChasing: true, 
        liveBufferLatencyMaxLatency: isFirefox ? 2.0 : 1.5, 
        liveSync: true
      })
      player.attachMediaElement(videoEl)
      player.load()
      player.on(mpegts.Events.ERROR, (errorType: any, errorDetail: any) => {
        if (String(errorDetail || '').includes('SourceBuffer') || String(errorDetail || '').includes('MSEError')) return
        pane.error = `错误: ${errorDetail}`
        pane.isPlaying = false
        pane.isLoading = false
      })
      
      const playDelay = isFirefox ? 500 : 100
      setTimeout(async () => {
        try {
          await player.play()
          pane.player = player
          pane.playMode = 'flv'
          pane.isPlaying = true
          pane.isLoading = false
          resolve(true)
        } catch { resolve(false) }
      }, playDelay)
    } catch { resolve(false) }
  })
}

const playWithFLV = async (pane: PaneType, videoEl: HTMLVideoElement, wsFlvUrl: string): Promise<boolean> => {
  if (isFirefox) {
    return new Promise((resolve) => {
      playQueue = playQueue.then(async () => {
        const result = await createFlvPlayerCore(pane, videoEl, wsFlvUrl)
        await new Promise(r => setTimeout(r, 300))
        resolve(result)
      })
    })
  } else {
    return createFlvPlayerCore(pane, videoEl, wsFlvUrl)
  }
}

const playChannelInPane = async (channelData: any, paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return
  if (pane.player) stopPane(paneIndex)
  
  const channel = channelData.channel || channelData
  pane.channel = channel
  pane.isLoading = true
  pane.isPlaying = false
  pane.error = null
  
  try {
    const channelId = channel.id || channelData.channelId
    const rawPlayUrls = await getLivePlayUrl(channelId, currentQuality.value)
    const playUrls = adaptPlayUrls(rawPlayUrls)
    
    if (!playUrls?.wsFlvUrl) throw new Error('无播放地址')
    
    await nextTick()
    const videoEl = pane.videoEl
    if (!videoEl) throw new Error('视频元素未找到')
    
    const success = await playWithFLV(pane, videoEl, playUrls.wsFlvUrl)
    if (!success) throw new Error('播放失败')
  } catch (error: any) {
    pane.error = error.message || '播放失败'
    pane.isLoading = false
  }
}

const stopPane = (paneIndex: number) => {
  const pane = panes.value[paneIndex]
  if (!pane) return
  if (pane.player) {
    try { pane.player.pause(); pane.player.unload(); pane.player.detachMediaElement(); pane.player.destroy() } catch {}
    pane.player = null
  }
  if (pane.videoEl) { pane.videoEl.srcObject = null; pane.videoEl.src = '' }
  if (pane.channel) {
    const channelId = pane.channel.id || pane.channel.channelId
    if (channelId) stopStream(channelId).catch(() => {})
  }
  pane.playMode = null
  pane.isPlaying = false
  pane.isLoading = false
  pane.error = null
  pane.channel = null
}

const handleStopPlay = (paneIndex: number) => stopPane(paneIndex)
const stopAllPlayersSilently = () => panes.value.forEach((_, idx) => stopPane(idx))
const handleStopAllPlayers = async () => {
  const activeCount = panes.value.filter(pane => pane.isPlaying || pane.isLoading).length
  if (activeCount === 0) return
  try {
    await ElMessageBox.confirm(`确定停止所有视频？`, '停止播放', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    stopAllPlayersSilently()
  } catch {}
}

const handleFullscreen = () => {
  const el = playerGridRef.value
  if (el && (el as any).requestFullscreen) (el as any).requestFullscreen()
}

onMounted(async () => {
  setLayout(gridLayout.value)
  await loadSpaceTree()
})

onUnmounted(() => stopAllPlayersSilently())
</script>

<style lang="scss" scoped>
@use '@/styles/dark-theme.scss';

.video-preview-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .main-layout {
    flex: 1;
    overflow: hidden;

    .smartpss-layout {
      height: 100%;
      display: flex;
      gap: 10px;
      padding: 10px;

      .left-panel {
        width: 220px;
        background: #1e1e1e;
        border: 1px solid #3a3a3a;
        border-radius: 4px;
        overflow: hidden;

        :deep(.el-collapse) { background: transparent; border: none; }
        :deep(.el-collapse-item__header) { background: #1e1e1e; color: #e0e0e0; border: 1px solid #3a3a3a; padding: 8px 12px; }
        :deep(.el-collapse-item__wrap) { background: #1e1e1e; border: 1px solid #3a3a3a; border-top: none; }
        :deep(.el-collapse-item__content) { padding: 8px 0 12px; }

        .search-box { padding: 8px 12px; }

        .device-tree {
          flex: 1;
          overflow-y: auto;
          padding: 8px;
          background: transparent;
          :deep(.el-tree-node__content) { height: 28px; &:hover { background: rgba(64, 158, 255, 0.1); } }
        }

        .tree-node { display: flex; align-items: center; gap: 6px; font-size: 12px; cursor: pointer; }
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
          display: flex;
          flex-direction: column;
          padding: 8px;

          .player-grid {
            flex: 1;
            display: grid;
            gap: 4px;
            background: #000;
            padding: 4px;
            border-radius: 4px;

            &.grid-2x2 { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(2, 1fr); }
            &.grid-2x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(2, 1fr); }
            &.grid-3x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(3, 1fr); }
            &.grid-4x4 { grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(4, 1fr); }
            &.grid-5x5 { grid-template-columns: repeat(5, 1fr); grid-template-rows: repeat(5, 1fr); }

            .player-pane {
              position: relative;
              background: #0a0a0a;
              border: 1px solid #2f2f2f;
              overflow: hidden;
              cursor: pointer;

              &.active { border-color: #409eff; }
              &.drag-over { border-color: #67c23a; background: rgba(103, 194, 58, 0.1); }

              .pane-video { width: 100%; height: 100%; background: #0e0e0e; object-fit: contain; }

              .pane-overlay {
                position: absolute;
                inset: 0;
                pointer-events: none;
                z-index: 1;

                .overlay-center {
                  position: absolute;
                  inset: 0;
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  gap: 4px;
                  color: #7a9aba;

                  .window-label { margin: 0; font-size: 12px; color: #a5c0db; }
                  .tip-text { margin: 0; font-size: 10px; color: #6c88a3; }

                  &.loading .loading-icon { animation: rotate 1.5s linear infinite; color: #409eff; }
                }

                .overlay-bottom {
                  position: absolute;
                  bottom: 0;
                  left: 0;
                  right: 0;
                  padding: 4px 8px;
                  background: linear-gradient(0deg, rgba(0,0,0,0.8) 0%, transparent 100%);

                  .device-name { 
                    color: #fff; 
                    font-size: 10px;
                    display: flex;
                    align-items: center;
                    gap: 4px;

                    .live-dot { width: 6px; height: 6px; border-radius: 50%; background: #67c23a; animation: pulse 1.5s infinite; }
                  }
                }

                .pane-toolbar {
                  position: absolute;
                  top: 4px;
                  right: 4px;
                  opacity: 0;
                  transition: opacity 0.2s;
                  pointer-events: auto;
                  z-index: 10;

                  .el-button { padding: 4px; min-height: auto; }
                }
              }

              &:hover .pane-toolbar { opacity: 1; }
            }
          }

          .playback-controls {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 8px;
            margin-top: 4px;

            .controls-left .protocol-info {
              display: flex;
              align-items: center;
              gap: 8px;
              padding: 4px 12px;
              background: rgba(103, 194, 58, 0.1);
              border: 1px solid rgba(103, 194, 58, 0.3);
              border-radius: 4px;
              font-size: 12px;
              color: #a5c0db;
            }

            .controls-right { display: flex; align-items: center; gap: 8px; }
          }
        }
      }
    }
  }
}

@keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
</style>
